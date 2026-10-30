package br.com.fatec.api_bndes.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma operação de financiamento do BNDES (uma linha do CSV oficial, já tratada). */
@Entity
@Table(name = "contrato",
        uniqueConstraints = @UniqueConstraint(name = "uk_contrato_hash_linha", columnNames = "hash_linha"))
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // id gerado pelo banco

    @Column(name = "cliente", length = 255)
    private String cliente;

    @Column(name = "cnpj", length = 20)
    private String cnpj;

    @Column(name = "descricao_do_projeto", columnDefinition = "TEXT")
    private String descricaoDoProjeto;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "municipio", length = 100)
    private String municipio;

    /**
     * Impressão digital (SHA-256) de todos os campos tratados da linha do CSV. É o que identifica uma
     * linha duplicada (o número do contrato se repete legitimamente). Fica nulo nos contratos cadastrados
     * manualmente. De propósito não tem getter/setter, para não aparecer no JSON da API.
     */
    @Column(name = "hash_linha", length = 64)
    private String hashLinha;

    @Column(name = "numero_do_contrato", nullable = false)
    private String numeroDoContrato;

    @Column(name = "data_da_contratacao")
    private LocalDate dataDaContratacao;

    @Column(name = "valor_contratado_reais", precision = 18, scale = 2)
    private BigDecimal valorContratadoReais;

    @Column(name = "valor_desembolsado_reais", precision = 18, scale = 2)
    private BigDecimal valorDesembolsadoReais;

    @Column(name = "fonte_de_recurso_desembolsos", length = 255)
    private String fonteDeRecursoDesembolsos;

    @Column(name = "custo_financeiro", length = 50)
    private String custoFinanceiro;

    @Column(name = "juros", length = 50)
    private String juros;

    @Column(name = "prazo_carencia_meses")
    private Integer prazoCarenciaMeses;

    @Column(name = "prazo_amortizacao_meses")
    private Integer prazoAmortizacaoMeses;

    @Column(name = "modalidade_de_apoio", length = 100)
    private String modalidadeDeApoio;

    @Column(name = "forma_de_apoio", length = 100)
    private String formaDeApoio;

    @Column(name = "produto", length = 100)
    private String produto;

    @Column(name = "instrumento_financeiro", length = 100)
    private String instrumentoFinanceiro;

    @Column(name = "inovacao")
    private Boolean inovacao;

    @Column(name = "area_operacional", length = 255)
    private String areaOperacional;

    @Column(name = "setor_cnae", length = 255)
    private String setorCnae;

    @Column(name = "subsetor_cnae_agrupado", length = 255)
    private String subsetorCnaeAgrupado;

    @Column(name = "subsetor_cnae_nome", length = 255)
    private String subsetorCnaeNome;

    @Column(name = "setor_bndes", length = 255)
    private String setorBndes;

    @Column(name = "subsetor_bndes", length = 150)
    private String subsetorBndes;

    @Column(name = "porte_do_cliente", length = 50)
    private String porteDoCliente;

    @Column(name = "natureza_do_cliente", length = 100)
    private String naturezaDoCliente;

    @Column(name = "instituicao_financeira_credenciada", length = 255)
    private String instituicaoFinanceiraCredenciada;

    @Column(name = "cnpj_da_instituicao_financeira_credenciada", length = 20)
    private String cnpjDaInstituicaoFinanceiraCredenciada;

    @Column(name = "tipo_de_garantia", length = 255)
    private String tipoDeGarantia;

    @Column(name = "tipo_de_excepcionalidade", length = 255)
    private String tipoDeExcepcionalidade;

    @Column(name = "situacao_do_contrato", length = 50)
    private String situacaoDoContrato;

    public Contrato() {}

    /** Usado só pela importação do CSV. */
    public void aplicarHashLinha(String hash) { this.hashLinha = hash; }

    public Integer getId() { return id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getDescricaoDoProjeto() { return descricaoDoProjeto; }
    public void setDescricaoDoProjeto(String descricaoDoProjeto) { this.descricaoDoProjeto = descricaoDoProjeto; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getNumeroDoContrato() { return numeroDoContrato; }
    public void setNumeroDoContrato(String numeroDoContrato) { this.numeroDoContrato = numeroDoContrato; }

    public LocalDate getDataDaContratacao() { return dataDaContratacao; }
    public void setDataDaContratacao(LocalDate dataDaContratacao) { this.dataDaContratacao = dataDaContratacao; }

    public BigDecimal getValorContratadoReais() { return valorContratadoReais; }
    public void setValorContratadoReais(BigDecimal valorContratadoReais) { this.valorContratadoReais = valorContratadoReais; }

    public BigDecimal getValorDesembolsadoReais() { return valorDesembolsadoReais; }
    public void setValorDesembolsadoReais(BigDecimal valorDesembolsadoReais) { this.valorDesembolsadoReais = valorDesembolsadoReais; }

    public String getFonteDeRecursoDesembolsos() { return fonteDeRecursoDesembolsos; }
    public void setFonteDeRecursoDesembolsos(String fonteDeRecursoDesembolsos) { this.fonteDeRecursoDesembolsos = fonteDeRecursoDesembolsos; }

    public String getCustoFinanceiro() { return custoFinanceiro; }
    public void setCustoFinanceiro(String custoFinanceiro) { this.custoFinanceiro = custoFinanceiro; }

    public String getJuros() { return juros; }
    public void setJuros(String juros) { this.juros = juros; }

    public Integer getPrazoCarenciaMeses() { return prazoCarenciaMeses; }
    public void setPrazoCarenciaMeses(Integer prazoCarenciaMeses) { this.prazoCarenciaMeses = prazoCarenciaMeses; }

    public Integer getPrazoAmortizacaoMeses() { return prazoAmortizacaoMeses; }
    public void setPrazoAmortizacaoMeses(Integer prazoAmortizacaoMeses) { this.prazoAmortizacaoMeses = prazoAmortizacaoMeses; }

    public String getModalidadeDeApoio() { return modalidadeDeApoio; }
    public void setModalidadeDeApoio(String modalidadeDeApoio) { this.modalidadeDeApoio = modalidadeDeApoio; }

    public String getFormaDeApoio() { return formaDeApoio; }
    public void setFormaDeApoio(String formaDeApoio) { this.formaDeApoio = formaDeApoio; }

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public String getInstrumentoFinanceiro() { return instrumentoFinanceiro; }
    public void setInstrumentoFinanceiro(String instrumentoFinanceiro) { this.instrumentoFinanceiro = instrumentoFinanceiro; }

    public Boolean getInovacao() { return inovacao; }
    public void setInovacao(Boolean inovacao) { this.inovacao = inovacao; }

    public String getAreaOperacional() { return areaOperacional; }
    public void setAreaOperacional(String areaOperacional) { this.areaOperacional = areaOperacional; }

    public String getSetorCnae() { return setorCnae; }
    public void setSetorCnae(String setorCnae) { this.setorCnae = setorCnae; }

    public String getSubsetorCnaeAgrupado() { return subsetorCnaeAgrupado; }
    public void setSubsetorCnaeAgrupado(String subsetorCnaeAgrupado) { this.subsetorCnaeAgrupado = subsetorCnaeAgrupado; }

    public String getSubsetorCnaeNome() { return subsetorCnaeNome; }
    public void setSubsetorCnaeNome(String subsetorCnaeNome) { this.subsetorCnaeNome = subsetorCnaeNome; }

    public String getSetorBndes() { return setorBndes; }
    public void setSetorBndes(String setorBndes) { this.setorBndes = setorBndes; }

    public String getSubsetorBndes() { return subsetorBndes; }
    public void setSubsetorBndes(String subsetorBndes) { this.subsetorBndes = subsetorBndes; }

    public String getPorteDoCliente() { return porteDoCliente; }
    public void setPorteDoCliente(String porteDoCliente) { this.porteDoCliente = porteDoCliente; }

    public String getNaturezaDoCliente() { return naturezaDoCliente; }
    public void setNaturezaDoCliente(String naturezaDoCliente) { this.naturezaDoCliente = naturezaDoCliente; }

    public String getInstituicaoFinanceiraCredenciada() { return instituicaoFinanceiraCredenciada; }
    public void setInstituicaoFinanceiraCredenciada(String instituicaoFinanceiraCredenciada) { this.instituicaoFinanceiraCredenciada = instituicaoFinanceiraCredenciada; }

    public String getCnpjDaInstituicaoFinanceiraCredenciada() { return cnpjDaInstituicaoFinanceiraCredenciada; }
    public void setCnpjDaInstituicaoFinanceiraCredenciada(String cnpjDaInstituicaoFinanceiraCredenciada) { this.cnpjDaInstituicaoFinanceiraCredenciada = cnpjDaInstituicaoFinanceiraCredenciada; }

    public String getTipoDeGarantia() { return tipoDeGarantia; }
    public void setTipoDeGarantia(String tipoDeGarantia) { this.tipoDeGarantia = tipoDeGarantia; }

    public String getTipoDeExcepcionalidade() { return tipoDeExcepcionalidade; }
    public void setTipoDeExcepcionalidade(String tipoDeExcepcionalidade) { this.tipoDeExcepcionalidade = tipoDeExcepcionalidade; }

    public String getSituacaoDoContrato() { return situacaoDoContrato; }
    public void setSituacaoDoContrato(String situacaoDoContrato) { this.situacaoDoContrato = situacaoDoContrato; }

}
