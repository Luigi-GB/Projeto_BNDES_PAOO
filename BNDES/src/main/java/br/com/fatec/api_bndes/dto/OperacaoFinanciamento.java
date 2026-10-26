package br.com.fatec.api_bndes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha válida do CSV, já limpa e convertida para os tipos adequados.
 * Por ser um record, equals/hashCode comparam todos os campos, o que é usado
 * para identificar linhas duplicadas.
 */
public record OperacaoFinanciamento(
        String cliente,
        String cnpj,
        String descricaoDoProjeto,
        String uf,
        String municipio,
        Long numeroDoContrato,
        LocalDate dataDaContratacao,
        BigDecimal valorContratadoReais,
        BigDecimal valorDesembolsadoReais,
        String fonteDeRecursoDesembolsos,
        String custoFinanceiro,
        BigDecimal juros,
        Integer prazoCarenciaMeses,
        Integer prazoAmortizacaoMeses,
        String modalidadeDeApoio,
        String formaDeApoio,
        String produto,
        String instrumentoFinanceiro,
        Boolean inovacao,
        String areaOperacional,
        String setorCnae,
        String subsetorCnaeAgrupado,
        String subsetorCnaeNome,
        String setorBndes,
        String subsetorBndes,
        String porteDoCliente,
        String naturezaDoCliente,
        String instituicaoFinanceiraCredenciada,
        String cnpjDaInstituicaoFinanceiraCredenciada,
        String tipoDeGarantia,
        String tipoDeExcepcionalidade,
        String situacaoDoContrato) {
}
