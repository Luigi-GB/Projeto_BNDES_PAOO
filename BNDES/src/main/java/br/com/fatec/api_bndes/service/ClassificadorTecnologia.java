package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import br.com.fatec.api_bndes.util.Normalizador;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Decide se uma operação é "financiamento de tecnologia", seguindo a decisão do grupo:
 * coluna inovacao = SIM e/ou palavra-chave na descrição do projeto.
 *
 * A lista de palavras é propositalmente enxuta: termos genéricos como INFORMATICA, AUTOMACAO,
 * DIGITAL ou SISTEMA aparecem em linhas de crédito para máquinas, TV digital, obras elétricas etc.
 * e traziam muito falso positivo. Para ampliar ou reduzir o critério, edite só PALAVRAS_CHAVE.
 */
public final class ClassificadorTecnologia {

    public enum Origem { SO_INOVACAO, SO_PALAVRA_CHAVE, AMBOS, NENHUMA }

    private static final Pattern PALAVRAS_CHAVE = Pattern.compile(
            "\\b(TECNOLOG|INOVA|SOFTWARE|TELECOM|P&D|PD&I|PESQUISA E DESENVOLVIMENTO|TIC\\b"
                    + "|INTELIGENCIA ARTIFICIAL|DATA CENTER|SEMICONDUTOR|INDUSTRIA 4\\.0"
                    + "|FIBRA OPTICA|BANDA LARGA|INTERNET)");

    private ClassificadorTecnologia() {
    }

    public static Origem classificar(OperacaoFinanciamento op) {
        boolean inovacao = Boolean.TRUE.equals(op.inovacao());
        boolean palavra = temPalavraChave(op.descricaoDoProjeto());
        if (inovacao && palavra) return Origem.AMBOS;
        if (inovacao) return Origem.SO_INOVACAO;
        if (palavra) return Origem.SO_PALAVRA_CHAVE;
        return Origem.NENHUMA;
    }

    static boolean temPalavraChave(String descricao) {
        if (descricao == null || descricao.isBlank()) return false;
        String limpa = Normalizador.semAcentos(descricao).toUpperCase(Locale.ROOT);
        return PALAVRAS_CHAVE.matcher(limpa).find();
    }
}
