const entrada = document.getElementById('arquivo');
const botao = document.getElementById('botao');
const nomeArquivo = document.getElementById('nome-arquivo');
const carregando = document.getElementById('carregando');
const resultado = document.getElementById('resultado');

const fmtInteiro = new Intl.NumberFormat('pt-BR');
const fmtMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

botao.addEventListener('click', () => entrada.click());

entrada.addEventListener('change', async () => {
    const arquivo = entrada.files[0];
    if (!arquivo) return;
    nomeArquivo.textContent = arquivo.name;
    await enviar(arquivo);
    entrada.value = ''; // permite escolher o mesmo arquivo de novo
});

async function enviar(arquivo) {
    botao.disabled = true;
    resultado.hidden = true;
    carregando.hidden = false;

    const dados = new FormData();
    dados.append('arquivo', arquivo);

    try {
        const resposta = await fetch('/api/importacao', { method: 'POST', body: dados });
        const corpo = await resposta.json().catch(() => null);
        if (!resposta.ok) {
            mostrarErro((corpo && corpo.mensagem) || `Erro ${resposta.status} ao enviar o arquivo.`);
        } else {
            mostrarRelatorio(corpo);
        }
    } catch (e) {
        mostrarErro('Erro: não foi possível se comunicar com o servidor.');
    } finally {
        carregando.hidden = true;
        botao.disabled = false;
    }
}

// Cria elementos com textContent (nunca innerHTML), para que o conteúdo do CSV não injete HTML.
function el(tag, classe, texto, filhos) {
    const e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    (filhos || []).forEach(f => e.appendChild(f));
    return e;
}

function mostrarErro(mensagem) {
    resultado.replaceChildren(el('div', 'mensagem erro', mensagem));
    resultado.hidden = false;
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

    if (r.linhasImportadas > 0) {
        const link = el('a', 'link-painel', 'Ver gráficos de financiamentos de tecnologia →');
        link.href = 'painel.html';
        partes.push(link);
    }

    resultado.replaceChildren(...partes);
    resultado.hidden = false;
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

function formatarData(iso) {
    if (!iso) return '';
    const [a, m, d] = iso.split('-');
    return `${d}/${m}/${a}`;
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
        el('h2', null, `Prévia dos dados importados (primeiras ${linhas.length} linhas)`),
        el('div', 'tabela-rolavel', null, [
            el('table', null, null, [el('thead', null, null, [cabecalho]), el('tbody', null, null, corpo)]),
        ]),
    ]);
}
