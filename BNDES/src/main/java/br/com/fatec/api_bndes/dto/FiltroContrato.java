package br.com.fatec.api_bndes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FiltroContrato {

    private String uf;
    private String municipio;
    private String porteDoCliente;
    private String subsetorBndes;
    private String situacaoDoContrato;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Getters e Setters
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getPorteDoCliente() { return porteDoCliente; }
    public void setPorteDoCliente(String porteDoCliente) { this.porteDoCliente = porteDoCliente; }

    public String getSubsetorBndes() { return subsetorBndes; }
    public void setSubsetorBndes(String subsetorBndes) { this.subsetorBndes = subsetorBndes; }

    public String getSituacaoDoContrato() { return situacaoDoContrato; }
    public void setSituacaoDoContrato(String situacaoDoContrato) { this.situacaoDoContrato = situacaoDoContrato; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
}
