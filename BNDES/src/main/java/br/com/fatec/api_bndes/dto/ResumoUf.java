package br.com.fatec.api_bndes.dto;

import java.math.BigDecimal;

/** Uma barra do gráfico: operações de uma UF. */
public record ResumoUf(String uf, long quantidade, BigDecimal valorContratado) {
}
