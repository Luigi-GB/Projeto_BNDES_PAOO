const API = '/api/operacoes';
const LIMITE_TABELA = 200; // a tabela mostra só os primeiros registros; o contador mostra o total real

const UFS = ['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA', 'PB',
    'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO', 'IE'];

const CAMPOS_FORM = ['cliente', 'cnpj', 'numeroDoContrato', 'dataDaContratacao', 'valorContratadoReais',
    'valorDesembolsadoReais', 'uf', 'municipio', 'subsetorBndes', 'porteDoCliente', 'situacaoDoContrato',
    'modalidadeDeApoio', 'produto', 'descricaoDoProjeto'];

const fmtInteiro = new Intl.NumberFormat('pt-BR');
const fmtMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

const $ = id => document.getElementById(id);

let contratoEmEdicao = null; // objeto completo vindo da API (o PUT envia todos os campos)

// ------------------------------------------------------------------ utilidades

// Cria elementos com textContent (nunca innerHTML), para que o conteúdo do CSV não injete HTML.
function el(tag, classe, texto, filhos) {
    const e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    (filhos || []).forEach(f => e.appendChild(f));
    return e;
}

function mensagem(texto, tipo) {
    const m = $('mensagem');
    m.textContent = texto;
    m.className = 'mensagem ' + tipo; // sucesso | erro | aviso
    m.hidden = false;
    m.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

async function chamar(url, opcoes) {
    try {
        const resposta = await fetch(url, opcoes);
        const corpo = resposta.status === 204 ? null : await resposta.json().catch(() => null);
        return { ok: resposta.ok, status: resposta.status, corpo, total: resposta.headers.get('X-Total-Count') };
    } catch (e) {
        return { ok: false, status: 0, corpo: null, total: null };
    }
}

function textoDoErro(r) {
    if (r.status === 0) return 'Erro: não foi possível se comunicar com o servidor.';
    return `Erro HTTP ${r.status}: ` + ((r.corpo && r.corpo.mensagem) || 'a requisição não foi aceita.');
}

function formatarData(iso) {
    if (!iso) return '';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
}

function vazioParaNulo(v) {
    const t = (v ?? '').toString().trim();
    return t === '' ? null : t;
}

// ------------------------------------------------------------------ UF (selects)

function preencherUf(select, primeiraOpcao) {
    select.replaceChildren(el('option', null, primeiraOpcao));
    select.firstChild.value = '';
    UFS.forEach(uf => {
        const o = el('option', null, uf);
        o.value = uf;
        select.appendChild(o);
    });
}

// ------------------------------------------------------------------ tabela, contador e filtros

function filtrosAtuais() {
    const p = new URLSearchParams();
    const uf = $('flt-uf').value;
    const subsetor = $('flt-subsetor').value.trim();
    const porte = $('flt-porte').value.trim();
    if (uf) p.set('uf', uf);
    if (subsetor) p.set('subsetorBndes', subsetor);
    if (porte) p.set('porteDoCliente', porte);
    return p;
}

async function carregarTabela() {
    const p = filtrosAtuais();
    p.set('limite', LIMITE_TABELA);
    const filtrado = p.size > 1;
    const url = filtrado ? `${API}/filtro?${p}` : `${API}?${p}`;

    const r = await chamar(url);
    if (!r.ok) {
        $('contador').textContent = '';
        return mensagem(textoDoErro(r), 'erro');
    }
    const linhas = r.corpo || [];
    const total = Number(r.total ?? linhas.length);

    $('contador').textContent = total === 0
        ? 'Nenhum registro encontrado.'
        : `Exibindo ${fmtInteiro.format(linhas.length)} de ${fmtInteiro.format(total)} registros` +
          (filtrado ? ' (com filtros aplicados)' : '') + '.';

    $('corpo-tabela').replaceChildren(...linhas.map(linhaDaTabela));
}

function linhaDaTabela(c) {
    const editar = el('button', 'mini', 'Editar');
    editar.addEventListener('click', () => iniciarEdicao(c.id));
    const excluir = el('button', 'mini perigo', 'Excluir');
    excluir.addEventListener('click', () => excluirContrato(c.id));

    return el('tr', null, null, [
        el('td', null, c.id),
        el('td', null, c.numeroDoContrato),
        el('td', null, c.cliente),
        el('td', null, c.uf || ''),
        el('td', null, formatarData(c.dataDaContratacao)),
        el('td', 'num', c.valorContratadoReais == null ? '' : fmtMoeda.format(c.valorContratadoReais)),
        el('td', null, c.porteDoCliente || ''),
        el('td', null, c.situacaoDoContrato || ''),
        el('td', null, null, [editar, excluir]),
    ]);
}

$('aplicar').addEventListener('click', carregarTabela);
$('limpar').addEventListener('click', () => {
    $('flt-uf').value = '';
    $('flt-subsetor').value = '';
    $('flt-porte').value = '';
    carregarTabela();
});

// ------------------------------------------------------------------ gráfico (valor por UF)

async function carregarGrafico() {
    const r = await chamar(`${API}/grafico/uf`);
    const area = $('grafico');
    if (!r.ok) return area.replaceChildren(el('p', 'meta', 'Não foi possível carregar o gráfico.'));
    const dados = (r.corpo || []).slice(0, 10);
    if (dados.length === 0) return area.replaceChildren(el('p', 'meta', 'Sem dados ainda. Importe o arquivo ou cadastre contratos.'));

    const maior = Math.max(...dados.map(d => Number(d.valorContratado)));
    area.replaceChildren(...dados.map(d => {
        const barra = el('div', 'barra-valor');
        barra.style.width = (maior > 0 ? (Number(d.valorContratado) / maior) * 100 : 0) + '%';
        return el('div', 'barra-linha', null, [
            el('b', null, d.uf),
            el('div', 'barra-fundo', null, [barra]),
            el('span', 'barra-rotulo', `${fmtMoeda.format(d.valorContratado)} · ${fmtInteiro.format(d.quantidade)} op.`),
        ]);
    }));
}

// ------------------------------------------------------------------ cadastro / edição

function lerFormulario() {
    const dados = {};
    CAMPOS_FORM.forEach(campo => { dados[campo] = vazioParaNulo($('f-' + campo).value); });
    // valores monetários seguem como número no JSON (o servidor converte para BigDecimal)
    ['valorContratadoReais', 'valorDesembolsadoReais'].forEach(c => {
        if (dados[c] !== null) dados[c] = Number(dados[c]);
    });
    return dados;
}

function limparFormulario() {
    CAMPOS_FORM.forEach(campo => { $('f-' + campo).value = ''; });
    contratoEmEdicao = null;
    $('titulo-form').textContent = 'Cadastrar contrato';
    $('salvar').textContent = 'Cadastrar';
    $('cancelar').hidden = true;
}

$('cancelar').addEventListener('click', limparFormulario);

async function iniciarEdicao(id) {
    const r = await chamar(`${API}/${id}`);
    if (!r.ok) return mensagem(textoDoErro(r), 'erro');
    contratoEmEdicao = r.corpo;
    CAMPOS_FORM.forEach(campo => { $('f-' + campo).value = r.corpo[campo] ?? ''; });
    $('titulo-form').textContent = `Editando contrato (ID ${id})`;
    $('salvar').textContent = 'Salvar alterações';
    $('cancelar').hidden = false;
    $('titulo-form').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

$('salvar').addEventListener('click', async () => {
    const dados = lerFormulario();
    const editando = contratoEmEdicao !== null;
    // na edição, parte do contrato completo para não apagar os campos que o formulário não mostra
    const corpo = editando ? Object.assign({}, contratoEmEdicao, dados) : dados;

    const r = await chamar(editando ? `${API}/${contratoEmEdicao.id}` : API, {
        method: editando ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(corpo),
    });

    if (!r.ok) return mensagem(textoDoErro(r), 'erro');
    mensagem(editando
        ? `Contrato ID ${r.corpo.id} atualizado com sucesso (HTTP ${r.status}).`
        : `Contrato cadastrado com sucesso. ID ${r.corpo.id} (HTTP ${r.status}).`, 'sucesso');
    limparFormulario();
    await Promise.all([carregarTabela(), carregarGrafico()]);
});

async function excluirContrato(id) {
    if (!confirm(`Excluir o contrato de ID ${id}?`)) return;
    const r = await chamar(`${API}/${id}`, { method: 'DELETE' });
    if (!r.ok) return mensagem(textoDoErro(r), 'erro');
    mensagem(`Contrato ID ${id} excluído (HTTP ${r.status}).`, 'sucesso');
    if (contratoEmEdicao && contratoEmEdicao.id === id) limparFormulario();
    await Promise.all([carregarTabela(), carregarGrafico()]);
}

// ------------------------------------------------------------------ busca por ID

$('buscar-id').addEventListener('click', async () => {
    const id = $('busca-id').value.trim();
    const detalhe = $('detalhe');
    if (!id) return mensagem('Informe um ID para buscar.', 'aviso');

    const r = await chamar(`${API}/${encodeURIComponent(id)}`);
    if (r.status === 404) {
        detalhe.hidden = true;
        return mensagem(`Nenhum registro com ID ${id} (HTTP 404 — não encontrado).`, 'erro');
    }
    if (!r.ok) {
        detalhe.hidden = true;
        return mensagem(textoDoErro(r), 'erro');
    }
    const c = r.corpo;
    const itens = Object.entries(c)
        .filter(([, v]) => v !== null && v !== '')
        .map(([k, v]) => el('div', null, null, [el('b', null, k), el('span', null, String(v))]));
    detalhe.replaceChildren(el('div', 'mensagem sucesso', `Registro encontrado (HTTP ${r.status}).`),
        el('div', 'detalhe-grade', null, itens));
    detalhe.hidden = false;
    $('mensagem').hidden = true;
});

// ------------------------------------------------------------------ importação

$('botao').addEventListener('click', () => $('arquivo').click());

$('arquivo').addEventListener('change', async () => {
    const arquivo = $('arquivo').files[0];
    if (!arquivo) return;
    $('nome-arquivo').textContent = arquivo.name;
    await enviar(arquivo);
    $('arquivo').value = ''; // permite escolher o mesmo arquivo de novo
});

async function enviar(arquivo) {
    $('botao').disabled = true;
    $('resultado').hidden = true;
    $('carregando').hidden = false;

    const dados = new FormData();
    dados.append('arquivo', arquivo);

    const r = await chamar(`${API}/importar`, { method: 'POST', body: dados });
    $('carregando').hidden = true;
    $('botao').disabled = false;

    // 200 e 400 com corpo de relatório (linhasLidas) mostram o relatório; outros erros mostram a mensagem
    if (r.corpo && r.corpo.linhasLidas !== undefined) {
        mostrarRelatorio(r.corpo);
        if (r.ok) await Promise.all([carregarTabela(), carregarGrafico()]);
    } else {
        $('resultado').replaceChildren(el('div', 'mensagem erro', textoDoErro(r)));
        $('resultado').hidden = false;
    }
}

function cartao(rotulo, valor, tipo) {
    return el('div', 'cartao ' + (tipo || ''), null, [
        el('div', 'rotulo', rotulo),
        el('div', 'valor', fmtInteiro.format(valor)),
    ]);
}

function mostrarRelatorio(r) {
    const classe = r.status === 'SUCESSO' ? 'sucesso' : r.status === 'ERRO' ? 'erro' : 'aviso';
    const partes = [el('div', 'mensagem ' + classe, r.mensagem)];

    partes.push(el('div', 'cartoes', null, [
        cartao('Linhas lidas', r.linhasLidas),
        cartao('Importadas', r.linhasImportadas, 'ok'),
        cartao('Rejeitadas', r.linhasRejeitadas, r.linhasRejeitadas > 0 ? 'ruim' : ''),
        cartao('Duplicadas (já nas rejeitadas)', r.linhasDuplicadas),
    ]));

    partes.push(el('div', 'meta',
        `Arquivo: ${r.arquivo} · Codificação: ${r.codificacao} · Tempo: ${fmtInteiro.format(r.duracaoMs)} ms`));

    if (r.motivosRejeicao.length > 0) partes.push(blocoMotivos(r.motivosRejeicao));
    if (Object.keys(r.avisos).length > 0) partes.push(blocoAvisos(r.avisos));
    if (r.amostra.length > 0) partes.push(blocoAmostra(r.amostra));

    $('resultado').replaceChildren(...partes);
    $('resultado').hidden = false;
}

function blocoMotivos(motivos) {
    const bloco = el('div', 'bloco', null, [el('h2', null, 'Por que algumas linhas não foram carregadas')]);
    motivos.forEach(m => {
        const exemplos = el('ul', null, null, m.exemplos.map(
            x => el('li', null, `linha ${fmtInteiro.format(x.linha)}: ${x.detalhe}`)));
        bloco.appendChild(el('details', null, null, [
            el('summary', null, null, [
                el('span', null, m.motivo),
                el('span', 'qtd', fmtInteiro.format(m.quantidade) + ' linhas'),
            ]),
            exemplos,
        ]));
    });
    bloco.appendChild(el('div', 'meta', 'Cada linha rejeitada é contada uma vez, pelo primeiro problema encontrado. Os exemplos mostram até 5 linhas por motivo.'));
    return bloco;
}

function blocoAvisos(avisos) {
    const lista = el('ul', null, null, Object.entries(avisos).map(
        ([texto, qtd]) => el('li', null, `${texto}: ${fmtInteiro.format(qtd)} linhas`)));
    return el('div', 'bloco', null, [
        el('h2', null, 'Avisos (linhas importadas mesmo assim)'),
        lista,
    ]);
}

function blocoAmostra(linhas) {
    const colunas = [
        ['Cliente', l => l.cliente],
        ['CNPJ', l => l.cnpj],
        ['Contrato', l => l.numeroDoContrato],
        ['Data', l => formatarData(l.dataDaContratacao)],
        ['UF', l => l.uf || ''],
        ['Valor contratado', l => fmtMoeda.format(l.valorContratadoReais), true],
        ['Valor desembolsado', l => l.valorDesembolsadoReais == null ? '' : fmtMoeda.format(l.valorDesembolsadoReais), true],
        ['Situação', l => l.situacaoDoContrato || ''],
    ];
    const cabecalho = el('tr', null, null, colunas.map(c => el('th', null, c[0])));
    const corpo = linhas.map(l => el('tr', null, null,
        colunas.map(c => el('td', c[2] ? 'num' : null, String(c[1](l))))));
    return el('div', 'bloco', null, [
        el('h2', null, `Prévia dos dados importados (primeiras ${linhas.length} linhas gravadas)`),
        el('div', 'tabela-rolavel', null, [
            el('table', null, null, [el('thead', null, null, [cabecalho]), el('tbody', null, null, corpo)]),
        ]),
    ]);
}

// ------------------------------------------------------------------ início

preencherUf($('f-uf'), '—');
preencherUf($('flt-uf'), 'Todas');
carregarTabela();
carregarGrafico();
