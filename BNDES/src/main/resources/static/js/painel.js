const NS = 'http://www.w3.org/2000/svg';
const fmtInt = new Intl.NumberFormat('pt-BR');
const CORES = ['#0b5cad', '#e8830c', '#067647'];

const EXPLICACAO = {
    COMBINADO: 'Entra o contrato que tem inovação = SIM ou palavra de tecnologia na descrição do projeto.',
    INOVACAO: 'Entra só o contrato marcado pelo BNDES com inovação = SIM.',
    PALAVRA_CHAVE: 'Entra só o contrato cuja descrição cita tecnologia, software, telecom, P&D, etc.',
};

const seletor = document.getElementById('criterio');
seletor.addEventListener('change', carregar);
carregar();

async function carregar() {
    const criterio = seletor.value;
    document.getElementById('explica-criterio').textContent = EXPLICACAO[criterio];
    try {
        const resposta = await fetch('/api/painel/tecnologia?criterio=' + encodeURIComponent(criterio));
        const corpo = await resposta.json().catch(() => null);
        if (!resposta.ok) return mostrarErro((corpo && corpo.mensagem) || 'Erro ' + resposta.status + ' ao buscar os dados.');
        desenhar(corpo);
    } catch (e) {
        mostrarErro('Não foi possível se comunicar com o servidor.');
    }
}

function mostrarErro(msg) {
    document.getElementById('conteudo').hidden = true;
    document.getElementById('subtitulo').textContent = '';
    const erro = document.getElementById('erro');
    erro.textContent = msg;
    erro.hidden = false;
}

// ------------------------------------------------------------------ formatação

function moedaCurta(v) {
    const n = Math.abs(v);
    const f = (x, s) => 'R$ ' + x.toLocaleString('pt-BR', { maximumFractionDigits: x >= 100 ? 0 : 1 }) + s;
    if (n >= 1e9) return f(v / 1e9, ' bi');
    if (n >= 1e6) return f(v / 1e6, ' mi');
    if (n >= 1e3) return f(v / 1e3, ' mil');
    return f(v, '');
}
/** Arredonda o topo do eixo para 1, 2, 2,5, 5 ou 10 × potência de 10 (eixo com marcas redondas). */
function maxRedondo(v) {
    const base = Math.pow(10, Math.floor(Math.log10(v)));
    return [1, 2, 2.5, 5, 10].map(f => f * base).find(c => c >= v);
}
const pct = x => (x * 100).toLocaleString('pt-BR', { maximumFractionDigits: 1 }) + '%';
const razao = (a, b) => (b > 0 ? a / b : 0);

// ------------------------------------------------------------------ helpers DOM/SVG

function el(tag, classe, texto, filhos) {
    const e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    (filhos || []).forEach(f => e.appendChild(f));
    return e;
}

function sv(tag, attrs, texto, filhos) {
    const e = document.createElementNS(NS, tag);
    Object.entries(attrs || {}).forEach(([k, v]) => e.setAttribute(k, v));
    if (texto !== undefined && texto !== null) e.textContent = texto;
    (filhos || []).forEach(f => e.appendChild(f));
    return e;
}

function curto(s, max) { return s.length > max ? s.slice(0, max - 1) + '…' : s; }
function texto(id, t) { document.getElementById(id).textContent = t; }

// ------------------------------------------------------------------ gráficos

/** Barras horizontais em HTML (o texto não encolhe nem corta): itens = [{nome, valor, contratos}] */
function barrasH(alvo, itens, total) {
    const maximo = Math.max(...itens.map(i => i.valor), 1);
    alvo.className = 'barras-h';
    alvo.replaceChildren(...itens.map(it => {
        const barra = el('div', 'barra-h');
        barra.style.width = Math.max(1, (it.valor / maximo) * 100) + '%';
        const linha = el('div', 'linha-h', null, [
            el('div', 'rot', it.nome),
            el('div', 'trilho', null, [barra]),
            el('div', 'val', `${moedaCurta(it.valor)} · ${pct(razao(it.valor, total))}`),
        ]);
        linha.title = `${it.nome}\n${moedaCurta(it.valor)} (${pct(razao(it.valor, total))} do total)\n${fmtInt.format(it.contratos)} contratos`;
        return linha;
    }));
}

/** Barras verticais por ano. */
function barrasAno(alvo, pontos) {
    const W = 1000, H = 300, m = { e: 84, d: 10, t: 12, b: 30 };
    const maximo = maxRedondo(Math.max(...pontos.map(p => p.valor), 1));
    const passo = (W - m.e - m.d) / pontos.length, alt = H - m.t - m.b;
    const filhos = [];
    for (let k = 0; k <= 4; k++) {
        const v = (maximo / 4) * k, y = m.t + alt - (v / maximo) * alt;
        filhos.push(sv('line', { class: 'grade-y', x1: m.e, x2: W - m.d, y1: y, y2: y }));
        filhos.push(sv('text', { x: m.e - 8, y: y + 4, 'text-anchor': 'end', class: 'suave' }, moedaCurta(v)));
    }
    pontos.forEach((p, i) => {
        const x = m.e + i * passo, h = (p.valor / maximo) * alt;
        filhos.push(sv('g', null, null, [
            sv('title', null, `${p.ano}\n${moedaCurta(p.valor)} em tecnologia (${fmtInt.format(p.contratos)} contratos)\n` +
                `${pct(razao(p.valor, p.valorTotalAno))} de tudo que o BNDES contratou no ano`),
            sv('rect', { class: 'barra', x: x + passo * 0.15, y: m.t + alt - h, width: passo * 0.7, height: Math.max(h, 0), rx: 2 }),
        ]));
        if (i % 2 === 0) filhos.push(sv('text', { x: x + passo / 2, y: H - 10, 'text-anchor': 'middle', class: 'suave' }, String(p.ano)));
    });
    alvo.className = 'rolavel';
    alvo.replaceChildren(sv('svg', { viewBox: `0 0 ${W} ${H}`, role: 'img' }, null, filhos));
}

/** Barra empilhada + legenda para os três critérios de classificação. */
function pilha(alvo, itens) {
    const soma = itens.reduce((s, i) => s + i.valor, 0);
    const barra = el('div', 'pilha', null, itens.map((it, i) => {
        const seg = el('div');
        seg.style.width = (razao(it.valor, soma) * 100) + '%';
        seg.style.background = CORES[i];
        seg.title = `${it.nome}: ${moedaCurta(it.valor)}`;
        return seg;
    }));
    const legenda = el('div', 'legenda', null, itens.map((it, i) => {
        const cor = el('span', 'cor');
        cor.style.background = CORES[i];
        return el('div', 'item', null, [
            cor,
            el('div', null, null, [el('div', null, it.nome),
                el('div', 'det', `${fmtInt.format(it.contratos)} contratos`)]),
            el('div', 'num', `${moedaCurta(it.valor)} · ${pct(razao(it.valor, soma))}`),
        ]);
    }));
    alvo.replaceChildren(barra, legenda);
}

// ------------------------------------------------------------------ montagem da página

function cartao(rotulo, valor, sub) {
    return el('div', 'cartao', null, [el('div', 'rotulo', rotulo), el('div', 'valor', valor), el('div', 'sub', sub)]);
}

function lider(itens, total, sufixo) {
    if (!itens.length || total <= 0) return '';
    const topo = itens[0];
    return `${topo.nome} lidera, com ${pct(topo.valor / total)} do valor de tecnologia${sufixo || ''}.`;
}

function desenhar(d) {
    document.getElementById('erro').hidden = true;
    document.getElementById('conteudo').hidden = false;
    const r = d.resumo, v = r.valorTecnologia;
    texto('subtitulo', `Dados do arquivo ${d.arquivo}: ${fmtInt.format(r.contratosTotal)} contratos importados, ` +
        `${fmtInt.format(r.contratosTecnologia)} identificados como tecnologia.`);

    document.getElementById('cartoes').replaceChildren(
        cartao('Contratos de tecnologia', fmtInt.format(r.contratosTecnologia),
            `${pct(razao(r.contratosTecnologia, r.contratosTotal))} de todos os contratos`),
        cartao('Valor contratado', moedaCurta(v), `${pct(razao(v, r.valorTotal))} do total do BNDES`),
        cartao('Valor desembolsado', moedaCurta(r.desembolsadoTecnologia),
            `${pct(razao(r.desembolsadoTecnologia, v))} do contratado já foi liberado`),
        cartao('Ticket médio', moedaCurta(r.ticketMedioTecnologia), 'valor médio por contrato'));

    // evolução
    barrasAno(document.getElementById('g-ano'), d.porAno);
    const pico = d.porAno.reduce((a, b) => (b.valor > a.valor ? b : a), d.porAno[0]);
    texto('l-ano', `O pico foi em ${pico.ano}: ${moedaCurta(pico.valor)}, ${pct(razao(pico.valor, pico.valorTotalAno))} ` +
        `de tudo que o BNDES contratou naquele ano. Passe o mouse sobre as barras para ver os detalhes de cada ano.`);

    // critério
    pilha(document.getElementById('g-criterio'), d.porCriterio);
    texto('l-criterio', 'Mostra o quanto a escolha do critério muda o resultado: contratos que só aparecem pela descrição ' +
        'não estão marcados como inovação pelo BNDES. Use o filtro acima para comparar.');

    // demais
    barrasH(document.getElementById('g-porte'), d.porPorte, v);
    const topPorte = d.porPorte[0];
    texto('l-porte', lider(d.porPorte, v, '') + (topPorte ? ` Em número de contratos, ele responde por ${pct(razao(topPorte.contratos, r.contratosTecnologia))}.` : ''));
    barrasH(document.getElementById('g-setor'), d.porSetor, v);
    texto('l-setor', lider(d.porSetor, v, ' (setor CNAE do cliente)'));
    barrasH(document.getElementById('g-uf'), d.porUf, v);
    texto('l-uf', lider(d.porUf, v, '') + ' “IE” é um valor da própria base, sem estado associado.');
    barrasH(document.getElementById('g-cliente'), d.topClientes, v);
    const top3 = d.topClientes.slice(0, 3).reduce((s, c) => s + c.valor, 0);
    texto('l-cliente', `Os 3 maiores clientes somam ${pct(razao(top3, v))} do valor de tecnologia: poucos contratos grandes pesam muito.`);
}
