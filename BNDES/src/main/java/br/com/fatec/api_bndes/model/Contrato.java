package br.com.fatec.api_bndes.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //Id de cada contrato no bd

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

    @Column(name = "numero_do_contrato", nullable = false, unique = true)
    private String numeroDoContrato;

    @Column(name = "data_da_contratacao")
    private LocalDate dataDaContratacao;

    @Column(name = "valor_contratado_reais", precision = 18, scale = 2)
    private BigDecimal valorContratadoReais;

    @Column(name = "valor_desembolsado_reais", precision = 18, scale = 2)
    private BigDecimal valorDesembolsadoReais;

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

    @Column(name = "subsetor_bndes", length = 150)
    private String subsetorBndes;

    @Column(name = "porte_do_cliente", length = 50)
    private String porteDoCliente;

    @Column(name = "natureza_do_cliente", length = 100)
    private String naturezaDoCliente;

    @Column(name = "situacao_do_contrato", length = 50)
    private String situacaoDoContrato;

    public Contrato(){}

    public Integer getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }
    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCnpj() {
        return cnpj;
    }
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getDescricaoDoProjeto() {
        return descricaoDoProjeto;
    }
    public void setDescricaoDoProjeto(String descricaoDoProjeto) {
        this.descricaoDoProjeto = descricaoDoProjeto;
    }

    public String getUf() { return uf; }
    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getMunicipio() {
        return municipio;
    }
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getNumeroDoContrato() {
        return numeroDoContrato;
    }
    public void setNumeroDoContrato(String numeroDoContrato) { this.numeroDoContrato = numeroDoContrato; }

    public LocalDate getDataDaContratacao() {
        return dataDaContratacao;
    }
    public void setDataDaContratacao(LocalDate dataDaContratacao) {
        this.dataDaContratacao = dataDaContratacao;
    }

    public BigDecimal getValorContratadoReais() {
        return valorContratadoReais;
    }
    public void setValorContratadoReais(BigDecimal valorContratadoReais) { this.valorContratadoReais = valorContratadoReais; }

    public BigDecimal getValorDesembolsadoReais() {
        return valorDesembolsadoReais;
    }
    public void setValorDesembolsadoReais(BigDecimal valorDesembolsadoReais) { this.valorDesembolsadoReais = valorDesembolsadoReais; }

    public String getCustoFinanceiro() {
        return custoFinanceiro;
    }
    public void setCustoFinanceiro(String custoFinanceiro) {
        this.custoFinanceiro = custoFinanceiro;
    }

    public String getJuros() {
        return juros;
    }
    public void setJuros(String juros) {
        this.juros = juros;
    }

    public Integer getPrazoCarenciaMeses() {
        return prazoCarenciaMeses;
    }
    public void setPrazoCarenciaMeses(Integer prazoCarenciaMeses) {
        this.prazoCarenciaMeses = prazoCarenciaMeses;
    }

    public Integer getPrazoAmortizacaoMeses() {
        return prazoAmortizacaoMeses;
    }
    public void setPrazoAmortizacaoMeses(Integer prazoAmortizacaoMeses) { this.prazoAmortizacaoMeses = prazoAmortizacaoMeses; }

    public String getModalidadeDeApoio() {
        return modalidadeDeApoio;
    }
    public void setModalidadeDeApoio(String modalidadeDeApoio) {
        this.modalidadeDeApoio = modalidadeDeApoio;
    }

    public String getFormaDeApoio() {
        return formaDeApoio;
    }
    public void setFormaDeApoio(String formaDeApoio) {
        this.formaDeApoio = formaDeApoio;
    }

    public String getProduto() {
        return produto;
    }
    public void setProduto(String produto) {
        this.produto = produto;
    }

    public String getInstrumentoFinanceiro() {
        return instrumentoFinanceiro;
    }
    public void setInstrumentoFinanceiro(String instrumentoFinanceiro) {
        this.instrumentoFinanceiro = instrumentoFinanceiro;
    }

    public String getSubsetorBndes() {
        return subsetorBndes;
    }
    public void setSubsetorBndes(String subsetorBndes) {
        this.subsetorBndes = subsetorBndes;
    }

    public String getPorteDoCliente() {
        return porteDoCliente;
    }
    public void setPorteDoCliente(String porteDoCliente) {
        this.porteDoCliente = porteDoCliente;
    }

    public String getNaturezaDoCliente() {
        return naturezaDoCliente;
    }
    public void setNaturezaDoCliente(String naturezaDoCliente) {
        this.naturezaDoCliente = naturezaDoCliente;
    }

    public String getSituacaoDoContrato() {
        return situacaoDoContrato;
    }
    public void setSituacaoDoContrato(String situacaoDoContrato) {
        this.situacaoDoContrato = situacaoDoContrato;
    }
}
