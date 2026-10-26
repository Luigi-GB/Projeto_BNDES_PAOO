package br.com.fatec.api_bndes.dto;

import java.util.List;
import java.util.Map;

/** Resultado final da importação, devolvido para a interface. */
public record RelatorioImportacao(
        String arquivo,
        String status,              // SUCESSO, SUCESSO_COM_REJEICOES ou ERRO
        String mensagem,
        String codificacao,
        long linhasLidas,
        long linhasImportadas,
        long linhasRejeitadas,      // inclui as duplicadas
        long linhasDuplicadas,
        long duracaoMs,
        List<MotivoRejeicaoResumo> motivosRejeicao,
        Map<String, Long> avisos,   // problemas leves: a linha foi importada mesmo assim
        List<OperacaoFinanciamento> amostra) {

    public record MotivoRejeicaoResumo(String motivo, long quantidade, List<ExemploRejeicao> exemplos) {
    }

    public record ExemploRejeicao(long linha, String detalhe) {
    }
}
