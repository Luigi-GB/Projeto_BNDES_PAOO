package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.util.HashLinha;

/** Converte a linha já tratada do CSV (DTO) na entidade que vai para o banco. */
public final class ContratoMapper {

    private ContratoMapper() {
    }

    public static Contrato paraEntidade(OperacaoFinanciamento op) {
        Contrato c = new Contrato();
        c.aplicarHashLinha(HashLinha.de(op));
        c.setCliente(cortar(op.cliente(), 255));
        c.setCnpj(cortar(op.cnpj(), 20));
        c.setDescricaoDoProjeto(op.descricaoDoProjeto());
        c.setUf(cortar(op.uf(), 2));
        c.setMunicipio(cortar(op.municipio(), 100));
        c.setNumeroDoContrato(op.numeroDoContrato() == null ? null : String.valueOf(op.numeroDoContrato()));
        c.setDataDaContratacao(op.dataDaContratacao());
        c.setValorContratadoReais(op.valorContratadoReais());
        c.setValorDesembolsadoReais(op.valorDesembolsadoReais());
        c.setFonteDeRecursoDesembolsos(cortar(op.fonteDeRecursoDesembolsos(), 255));
        c.setCustoFinanceiro(cortar(op.custoFinanceiro(), 50));
        c.setJuros(op.juros() == null ? null : op.juros().toPlainString());
        c.setPrazoCarenciaMeses(op.prazoCarenciaMeses());
        c.setPrazoAmortizacaoMeses(op.prazoAmortizacaoMeses());
        c.setModalidadeDeApoio(cortar(op.modalidadeDeApoio(), 100));
        c.setFormaDeApoio(cortar(op.formaDeApoio(), 100));
        c.setProduto(cortar(op.produto(), 100));
        c.setInstrumentoFinanceiro(cortar(op.instrumentoFinanceiro(), 100));
        c.setInovacao(op.inovacao());
        c.setAreaOperacional(cortar(op.areaOperacional(), 255));
        c.setSetorCnae(cortar(op.setorCnae(), 255));
        c.setSubsetorCnaeAgrupado(cortar(op.subsetorCnaeAgrupado(), 255));
        c.setSubsetorCnaeNome(cortar(op.subsetorCnaeNome(), 255));
        c.setSetorBndes(cortar(op.setorBndes(), 255));
        c.setSubsetorBndes(cortar(op.subsetorBndes(), 150));
        c.setPorteDoCliente(cortar(op.porteDoCliente(), 50));
        c.setNaturezaDoCliente(cortar(op.naturezaDoCliente(), 100));
        c.setInstituicaoFinanceiraCredenciada(cortar(op.instituicaoFinanceiraCredenciada(), 255));
        c.setCnpjDaInstituicaoFinanceiraCredenciada(cortar(op.cnpjDaInstituicaoFinanceiraCredenciada(), 20));
        c.setTipoDeGarantia(cortar(op.tipoDeGarantia(), 255));
        c.setTipoDeExcepcionalidade(cortar(op.tipoDeExcepcionalidade(), 255));
        c.setSituacaoDoContrato(cortar(op.situacaoDoContrato(), 50));
        return c;
    }

    /** Garante que o texto cabe na coluna; evita que uma linha estranha derrube a importação inteira. */
    private static String cortar(String valor, int max) {
        return valor == null || valor.length() <= max ? valor : valor.substring(0, max);
    }
}
