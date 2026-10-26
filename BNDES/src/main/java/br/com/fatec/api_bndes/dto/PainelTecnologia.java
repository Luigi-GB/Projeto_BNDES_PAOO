package br.com.fatec.api_bndes.dto;

import java.math.BigDecimal;
import java.util.List;

/** Dados já agregados para os gráficos do painel de financiamentos de tecnologia. */
public record PainelTecnologia(
        String criterio,
        String arquivo,
        Resumo resumo,
        List<PontoAno> porAno,
        List<Fatia> porCriterio,
        List<Fatia> porSetor,
        List<Fatia> porUf,
        List<Fatia> porPorte,
        List<Fatia> topClientes) {

    public record Resumo(long contratosTotal, long contratosTecnologia,
                         BigDecimal valorTotal, BigDecimal valorTecnologia,
                         BigDecimal desembolsadoTecnologia, BigDecimal ticketMedioTecnologia) {
    }

    /** Um ano do gráfico de evolução: o que foi tecnologia e o total do BNDES no mesmo ano. */
    public record PontoAno(int ano, long contratos, BigDecimal valor, BigDecimal valorTotalAno) {
    }

    /** Uma barra/fatia de gráfico (setor, UF, porte, cliente ou critério). */
    public record Fatia(String nome, long contratos, BigDecimal valor) {
    }
}
