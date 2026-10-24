package br.com.fatec.api_bndes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FiltroContrato {

    private String numeroDoContrato;
    private String cliente;
    private String uf;
    private String municipio;
    private LocalDate dataDaContratacao;
    private BigDecimal valorContratadoReais;
    private BigDecimal valorDesembolsadoReais;
    private String subsetorBndes;
    private String porteDoCliente;
    private String situacaoDoContrato;

    // Construtor a partir da Entidade (facilita conversões via Projection ou ModelMapper/MapStruct)
    public FiltroContrato(String numeroDoContrato, String cliente, String uf, String municipio,
                               LocalDate dataDaContratacao, BigDecimal valorContratadoReais,
                               BigDecimal valorDesembolsadoReais, String subsetorBndes,
                               String porteDoCliente, String situacaoDoContrato) {
        this.numeroDoContrato = numeroDoContrato;
        this.cliente = cliente;
        this.uf = uf;
        this.municipio = municipio;
        this.dataDaContratacao = dataDaContratacao;
        this.valorContratadoReais = valorContratadoReais;
        this.valorDesembolsadoReais = valorDesembolsadoReais;
        this.subsetorBndes = subsetorBndes;
        this.porteDoCliente = porteDoCliente;
        this.situacaoDoContrato = situacaoDoContrato;
    }

    // Getters
    public String getNumeroDoContrato() { return numeroDoContrato; }
    public String getCliente() { return cliente; }
    public String getUf() { return uf; }
    public String getMunicipio() { return municipio; }
    public LocalDate getDataDaContratacao() { return dataDaContratacao; }
    public BigDecimal getValorContratadoReais() { return valorContratadoReais; }
    public BigDecimal getValorDesembolsadoReais() { return valorDesembolsadoReais; }
    public String getSubsetorBndes() { return subsetorBndes; }
    public String getPorteDoCliente() { return porteDoCliente; }
    public String getSituacaoDoContrato() { return situacaoDoContrato; }
}
