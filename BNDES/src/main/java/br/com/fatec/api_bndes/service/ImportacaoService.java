package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import br.com.fatec.api_bndes.dto.RelatorioImportacao;
import br.com.fatec.api_bndes.dto.RelatorioImportacao.ExemploRejeicao;
import br.com.fatec.api_bndes.dto.RelatorioImportacao.MotivoRejeicaoResumo;
import br.com.fatec.api_bndes.exception.ArquivoInvalidoException;
import br.com.fatec.api_bndes.exception.LinhaInvalidaException;
import br.com.fatec.api_bndes.exception.MotivoRejeicao;
import br.com.fatec.api_bndes.repository.ContratoRepository;
import br.com.fatec.api_bndes.util.LinhaCsvParser;
import br.com.fatec.api_bndes.util.Normalizador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class ImportacaoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final int MAX_EXEMPLOS_POR_MOTIVO = 5;
    private static final int TAMANHO_AMOSTRA = 10;
    private static final int MAX_DETALHE = 80;
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    /** Sem essas colunas não há como montar um registro útil: o arquivo inteiro é recusado. */
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "cliente", "cnpj", "numero_do_contrato", "data_da_contratacao", "valor_contratado_reais");

    private static final int TAMANHO_LOTE = 500;

    private final boolean removerAcentos;
    private final ContratoRepository contratoRepository;

    public ImportacaoService(@Value("${importacao.remover-acentos:true}") boolean removerAcentos,
                             ContratoRepository contratoRepository) {
        this.removerAcentos = removerAcentos;
        this.contratoRepository = contratoRepository;
    }

    /**
     * Ponto de entrada do upload: valida o arquivo recebido e importa. Tudo ocorre numa única transação:
     * se a gravação no banco falhar no meio, nada fica pela metade.
     */
    @Transactional
    public RelatorioImportacao importar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo enviado está vazio.");
        }
        String nome = arquivo.getOriginalFilename() == null ? "arquivo.csv" : arquivo.getOriginalFilename();
        String minusculo = nome.toLowerCase(Locale.ROOT);
        if (!minusculo.endsWith(".csv") && !minusculo.endsWith(".txt")) {
            throw new ArquivoInvalidoException("Envie um arquivo no formato .csv.");
        }
        try {
            return importar(nome, arquivo.getBytes());
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo enviado.");
        }
    }

    @Transactional
    public RelatorioImportacao importar(String nomeArquivo, byte[] conteudo) {
        long inicio = System.nanoTime();

        if (conteudo == null || conteudo.length == 0) {
            throw new ArquivoInvalidoException("O arquivo enviado está vazio.");
        }

        Charset charset = detectarCharset(conteudo);
        String texto = new String(conteudo, charset);
        if (texto.startsWith("\uFEFF")) {
            texto = texto.substring(1);     // BOM
        }

        ConversorOperacao conversor = new ConversorOperacao(removerAcentos, LocalDate.now());
        Acumulador acc = new Acumulador();

        try (BufferedReader leitor = new BufferedReader(new StringReader(texto))) {
            String cabecalho = leitor.readLine();
            if (cabecalho == null || cabecalho.isBlank()) {
                throw new ArquivoInvalidoException("O arquivo não tem cabeçalho (primeira linha vazia).");
            }

            char separador = detectarSeparador(cabecalho);
            List<String> colunas = lerCabecalho(cabecalho, separador);
            Map<String, Integer> posicao = indexar(colunas);

            List<String> ausentes = COLUNAS_OBRIGATORIAS.stream().filter(c -> !posicao.containsKey(c)).toList();
            if (!ausentes.isEmpty()) {
                throw new ArquivoInvalidoException("Colunas obrigatórias ausentes no cabeçalho: " + String.join(", ", ausentes) + ".");
            }

            String linha;
            long numeroLinha = 1;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                acc.lidas++;
                processarLinha(linha, numeroLinha, separador, colunas, conversor, acc);
            }
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo: " + e.getMessage());
        }

        persistir(acc);   // grava no PostgreSQL (Neon), pulando contratos que já existem

        return montarRelatorio(nomeArquivo, charset, acc, (System.nanoTime() - inicio) / 1_000_000);
    }

    // ------------------------------------------------------------ processamento

    private void processarLinha(String linha, long numeroLinha, char separador, List<String> colunas,
                                ConversorOperacao conversor, Acumulador acc) {
        try {
            if (linha.isBlank()) {
                throw new LinhaInvalidaException(MotivoRejeicao.LINHA_EM_BRANCO, null, "linha vazia");
            }

            List<String> campos = LinhaCsvParser.dividir(linha, separador);
            if (campos.size() != colunas.size()) {
                throw new LinhaInvalidaException(MotivoRejeicao.COLUNAS_DIFERENTES, null,
                        "esperado " + colunas.size() + ", encontrado " + campos.size());
            }

            Map<String, String> bruto = new HashMap<>();
            for (int i = 0; i < colunas.size(); i++) {
                bruto.put(colunas.get(i), campos.get(i));
            }

            List<String> avisosDaLinha = new ArrayList<>();
            OperacaoFinanciamento operacao = conversor.converter(bruto, avisosDaLinha);

            Long primeiraOcorrencia = acc.vistas.get(operacao);
            if (primeiraOcorrencia != null) {
                acc.duplicadas++;
                throw new LinhaInvalidaException(MotivoRejeicao.LINHA_DUPLICADA, null,
                        "idêntica à linha " + primeiraOcorrencia);
            }
            acc.vistas.put(operacao, numeroLinha);

            acc.aceitas.add(operacao);
            avisosDaLinha.forEach(a -> acc.avisos.merge(a, 1L, Long::sum));
        } catch (LinhaInvalidaException e) {
            acc.rejeitadas++;
            acc.registrarRejeicao(e, numeroLinha);
        }
    }

    // ---------------------------------------------------------------- persistência

    /**
     * Política de reimportação: o número do contrato é único. Contrato que já está no banco (ou que se
     * repete dentro do próprio arquivo) NÃO é regravado: conta como rejeitado/duplicado. Assim,
     * importar o mesmo arquivo duas vezes não duplica dados nem altera o que já foi gravado.
     */
    private void persistir(Acumulador acc) {
        Set<String> numerosVistos = new HashSet<>();
        for (int inicio = 0; inicio < acc.aceitas.size(); inicio += TAMANHO_LOTE) {
            List<OperacaoFinanciamento> lote =
                    acc.aceitas.subList(inicio, Math.min(inicio + TAMANHO_LOTE, acc.aceitas.size()));

            Set<String> existentes = new HashSet<>(contratoRepository.findNumerosExistentes(
                    lote.stream().map(o -> String.valueOf(o.numeroDoContrato())).toList()));

            List<br.com.fatec.api_bndes.model.Contrato> paraSalvar = new ArrayList<>();
            for (OperacaoFinanciamento op : lote) {
                String numero = String.valueOf(op.numeroDoContrato());
                long linha = acc.vistas.getOrDefault(op, 0L);
                if (existentes.contains(numero)) {
                    rejeitarDuplicada(acc, linha, "contrato " + numero + " já existe no banco");
                    continue;
                }
                if (!numerosVistos.add(numero)) {
                    rejeitarDuplicada(acc, linha, "número de contrato " + numero + " repetido no arquivo");
                    continue;
                }
                paraSalvar.add(ContratoMapper.paraEntidade(op));
                if (acc.amostra.size() < TAMANHO_AMOSTRA) {
                    acc.amostra.add(op);
                }
            }
            contratoRepository.saveAll(paraSalvar);
            acc.importadas += paraSalvar.size();
        }
    }

    private void rejeitarDuplicada(Acumulador acc, long linha, String detalhe) {
        acc.duplicadas++;
        acc.rejeitadas++;
        acc.registrarRejeicao(new LinhaInvalidaException(MotivoRejeicao.LINHA_DUPLICADA, "numero_do_contrato", detalhe), linha);
    }

    // ---------------------------------------------------------------- cabeçalho

    private char detectarSeparador(String cabecalho) {
        char melhor = ';';
        long maior = -1;
        for (char candidato : new char[]{';', ',', '\t'}) {
            long ocorrencias = cabecalho.chars().filter(c -> c == candidato).count();
            if (ocorrencias > maior) {
                maior = ocorrencias;
                melhor = candidato;
            }
        }
        return melhor;
    }

    private List<String> lerCabecalho(String cabecalho, char separador) {
        try {
            return LinhaCsvParser.dividir(cabecalho, separador).stream()
                    .map(c -> Normalizador.semAcentos(c.trim()).toLowerCase(PT_BR))
                    .toList();
        } catch (LinhaInvalidaException e) {
            throw new ArquivoInvalidoException("Cabeçalho inválido: " + e.getMessage() + ".");
        }
    }

    private Map<String, Integer> indexar(List<String> colunas) {
        Map<String, Integer> posicao = new HashMap<>();
        for (int i = 0; i < colunas.size(); i++) {
            posicao.putIfAbsent(colunas.get(i), i);
        }
        return posicao;
    }

    // ----------------------------------------------------------------- charset

    /**
     * Se o arquivo é UTF-8 válido, usa UTF-8; caso contrário assume Windows-1252, que é o caso do
     * arquivo do BNDES (ele tem o travessão "–", byte 0x96, que não existe em ISO-8859-1 puro).
     * O Windows-1252 é compatível com o ISO-8859-1 para todos os acentos do português.
     */
    private Charset detectarCharset(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException e) {
            return WINDOWS_1252;
        }
    }

    // ---------------------------------------------------------------- relatório

    private RelatorioImportacao montarRelatorio(String nomeArquivo, Charset charset, Acumulador acc, long duracaoMs) {
        List<MotivoRejeicaoResumo> motivos = acc.rejeicoes.entrySet().stream()
                .map(e -> new MotivoRejeicaoResumo(e.getKey(), e.getValue().quantidade, e.getValue().exemplos))
                .sorted(Comparator.comparingLong(MotivoRejeicaoResumo::quantidade).reversed())
                .toList();

        String status;
        String mensagem;
        if (acc.lidas == 0) {
            status = "ERRO";
            mensagem = "Erro: o arquivo tem cabeçalho, mas nenhuma linha de dados.";
        } else if (acc.importadas == 0) {
            status = "ERRO";
            mensagem = "Erro: nenhuma linha foi importada. As " + formatar(acc.lidas) + " linhas lidas foram rejeitadas.";
        } else if (acc.rejeitadas == 0) {
            status = "SUCESSO";
            mensagem = "Sucesso: todas as " + formatar(acc.importadas) + " linhas foram importadas.";
        } else {
            status = "SUCESSO_COM_REJEICOES";
            mensagem = "Importação concluída: " + formatar(acc.importadas) + " de " + formatar(acc.lidas)
                    + " linhas importadas e " + formatar(acc.rejeitadas) + " rejeitadas"
                    + (acc.duplicadas > 0 ? " (" + formatar(acc.duplicadas) + " duplicadas)" : "") + ".";
        }

        return new RelatorioImportacao(nomeArquivo, status, mensagem, charset.name(),
                acc.lidas, acc.importadas, acc.rejeitadas, acc.duplicadas, duracaoMs,
                motivos, acc.avisos, acc.amostra);
    }

    private static String formatar(long n) {
        return String.format(PT_BR, "%,d", n);
    }

    private static String truncar(String s) {
        return s.length() <= MAX_DETALHE ? s : s.substring(0, MAX_DETALHE) + "…";
    }

    // -------------------------------------------------------------- acumuladores

    private static class Acumulador {
        long lidas;
        long importadas;
        long rejeitadas;
        long duplicadas;
        final Map<OperacaoFinanciamento, Long> vistas = new HashMap<>();
        final Map<String, Long> avisos = new LinkedHashMap<>();
        final Map<String, MotivoAcumulado> rejeicoes = new LinkedHashMap<>();
        final List<OperacaoFinanciamento> amostra = new ArrayList<>();
        final List<OperacaoFinanciamento> aceitas = new ArrayList<>();

        void registrarRejeicao(LinhaInvalidaException e, long linha) {
            MotivoAcumulado m = rejeicoes.computeIfAbsent(e.chaveDeAgrupamento(), k -> new MotivoAcumulado());
            m.quantidade++;
            if (m.exemplos.size() < MAX_EXEMPLOS_POR_MOTIVO) {
                m.exemplos.add(new ExemploRejeicao(linha, truncar(e.getMessage() == null ? "" : e.getMessage())));
            }
        }
    }

    private static class MotivoAcumulado {
        long quantidade;
        final List<ExemploRejeicao> exemplos = new ArrayList<>();
    }
}
