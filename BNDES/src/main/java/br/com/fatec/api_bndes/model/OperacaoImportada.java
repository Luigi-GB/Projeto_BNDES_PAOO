package br.com.fatec.api_bndes.model;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Linha do CSV já limpa, gravada no banco. O número do contrato NÃO é único (ele se repete legitimamente);
 * quem identifica a linha é o hash de todos os campos limpos, usado para saber se ela já foi importada.
 */
@Entity
@Table(name = "operacao_financiamento",
        uniqueConstraints = @UniqueConstraint(name = "uk_operacao_hash", columnNames = "hash_linha"))
public class OperacaoImportada {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "hash_linha", nullable = false, length = 64)
    private String hashLinha;

    @Column(name = "cliente", length = 500)
    private String cliente;

    @Column(name = "cnpj", length = 500)
    private String cnpj;

    @Column(name = "descricao_do_projeto", columnDefinition = "TEXT")
    private String descricaoDoProjeto;

    @Column(name = "uf", length = 500)
    private String uf;

    @Column(name = "municipio", length = 500)
    private String municipio;

    @Column(name = "numero_do_contrato")
    private Long numeroDoContrato;

    @Column(name = "data_da_contratacao")
    private LocalDate dataDaContratacao;

    @Column(name = "valor_contratado_reais", precision = 18, scale = 2)
    private BigDecimal valorContratadoReais;

    @Column(name = "valor_desembolsado_reais", precision = 18, scale = 2)
    private BigDecimal valorDesembolsadoReais;

    @Column(name = "fonte_de_recurso_desembolsos", length = 1000)
    private String fonteDeRecursoDesembolsos;

    @Column(name = "custo_financeiro", length = 500)
    private String custoFinanceiro;

    @Column(name = "juros", precision = 18, scale = 6)
    private BigDecimal juros;

    @Column(name = "prazo_carencia_meses")
    private Integer prazoCarenciaMeses;

    @Column(name = "prazo_amortizacao_meses")
    private Integer prazoAmortizacaoMeses;

    @Column(name = "modalidade_de_apoio", length = 500)
    private String modalidadeDeApoio;

    @Column(name = "forma_de_apoio", length = 500)
    private String formaDeApoio;

    @Column(name = "produto", length = 500)
    private String produto;

    @Column(name = "instrumento_financeiro", length = 500)
    private String instrumentoFinanceiro;

    @Column(name = "inovacao")
    private Boolean inovacao;

    @Column(name = "area_operacional", length = 500)
    private String areaOperacional;

    @Column(name = "setor_cnae", length = 500)
    private String setorCnae;

    @Column(name = "subsetor_cnae_agrupado", length = 500)
    private String subsetorCnaeAgrupado;

    @Column(name = "subsetor_cnae_nome", length = 500)
    private String subsetorCnaeNome;

    @Column(name = "setor_bndes", length = 500)
    private String setorBndes;

    @Column(name = "subsetor_bndes", length = 500)
    private String subsetorBndes;

    @Column(name = "porte_do_cliente", length = 500)
    private String porteDoCliente;

    @Column(name = "natureza_do_cliente", length = 500)
    private String naturezaDoCliente;

    @Column(name = "instituicao_financeira_credenciada", length = 500)
    private String instituicaoFinanceiraCredenciada;

    @Column(name = "cnpj_da_instituicao_financeira_credenciada", length = 500)
    private String cnpjDaInstituicaoFinanceiraCredenciada;

    @Column(name = "tipo_de_garantia", length = 500)
    private String tipoDeGarantia;

    @Column(name = "tipo_de_excepcionalidade", length = 500)
    private String tipoDeExcepcionalidade;

    @Column(name = "situacao_do_contrato", length = 500)
    private String situacaoDoContrato;

    protected OperacaoImportada() {
    }

    public OperacaoImportada(OperacaoFinanciamento op, String hashLinha) {
        this.hashLinha = hashLinha;
        this.cliente = op.cliente();
        this.cnpj = op.cnpj();
        this.descricaoDoProjeto = op.descricaoDoProjeto();
        this.uf = op.uf();
        this.municipio = op.municipio();
        this.numeroDoContrato = op.numeroDoContrato();
        this.dataDaContratacao = op.dataDaContratacao();
        this.valorContratadoReais = op.valorContratadoReais();
        this.valorDesembolsadoReais = op.valorDesembolsadoReais();
        this.fonteDeRecursoDesembolsos = op.fonteDeRecursoDesembolsos();
        this.custoFinanceiro = op.custoFinanceiro();
        this.juros = op.juros();
        this.prazoCarenciaMeses = op.prazoCarenciaMeses();
        this.prazoAmortizacaoMeses = op.prazoAmortizacaoMeses();
        this.modalidadeDeApoio = op.modalidadeDeApoio();
        this.formaDeApoio = op.formaDeApoio();
        this.produto = op.produto();
        this.instrumentoFinanceiro = op.instrumentoFinanceiro();
        this.inovacao = op.inovacao();
        this.areaOperacional = op.areaOperacional();
        this.setorCnae = op.setorCnae();
        this.subsetorCnaeAgrupado = op.subsetorCnaeAgrupado();
        this.subsetorCnaeNome = op.subsetorCnaeNome();
        this.setorBndes = op.setorBndes();
        this.subsetorBndes = op.subsetorBndes();
        this.porteDoCliente = op.porteDoCliente();
        this.naturezaDoCliente = op.naturezaDoCliente();
        this.instituicaoFinanceiraCredenciada = op.instituicaoFinanceiraCredenciada();
        this.cnpjDaInstituicaoFinanceiraCredenciada = op.cnpjDaInstituicaoFinanceiraCredenciada();
        this.tipoDeGarantia = op.tipoDeGarantia();
        this.tipoDeExcepcionalidade = op.tipoDeExcepcionalidade();
        this.situacaoDoContrato = op.situacaoDoContrato();
    }

    /** Reconstrói o registro limpo (usado pelo painel de gráficos). */
    public OperacaoFinanciamento paraOperacao() {
        return new OperacaoFinanciamento(cliente, cnpj, descricaoDoProjeto, uf, municipio, numeroDoContrato, dataDaContratacao, valorContratadoReais, valorDesembolsadoReais, fonteDeRecursoDesembolsos, custoFinanceiro, juros, prazoCarenciaMeses, prazoAmortizacaoMeses, modalidadeDeApoio, formaDeApoio, produto, instrumentoFinanceiro, inovacao, areaOperacional, setorCnae, subsetorCnaeAgrupado, subsetorCnaeNome, setorBndes, subsetorBndes, porteDoCliente, naturezaDoCliente, instituicaoFinanceiraCredenciada, cnpjDaInstituicaoFinanceiraCredenciada, tipoDeGarantia, tipoDeExcepcionalidade, situacaoDoContrato);
    }
}
