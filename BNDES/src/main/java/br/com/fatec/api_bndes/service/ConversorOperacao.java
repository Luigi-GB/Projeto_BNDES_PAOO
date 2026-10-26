package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;
import br.com.fatec.api_bndes.exception.LinhaInvalidaException;
import br.com.fatec.api_bndes.exception.MotivoRejeicao;
import br.com.fatec.api_bndes.util.Normalizador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Transforma os campos brutos de uma linha (coluna -> texto) em uma OperacaoFinanciamento.
 * Se algo estiver errado, lança LinhaInvalidaException e a linha é ignorada pelo serviço.
 */
public class ConversorOperacao {

    // As colunas "municipio_codigo" e "subsetor_cnae_codigo" são desconsideradas de propósito:
    // não são lidas, validadas, gravadas nem usadas na detecção de duplicidade.

    /** 27 UFs + "IE", valor especial que existe na própria base do BNDES. */
    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB",
            "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO", "IE");

    private static final Pattern NUMERO_CONTRATO = Pattern.compile("\\d{1,18}");
    private static final Pattern NAO_DIGITOS = Pattern.compile("\\D");

    private static final int ANO_MINIMO = 1950;

    private final boolean removerAcentos;
    private final LocalDate hoje;

    public ConversorOperacao(boolean removerAcentos, LocalDate hoje) {
        this.removerAcentos = removerAcentos;
        this.hoje = hoje;
    }

    /** @param avisos recebe problemas leves (a linha continua válida) */
    public OperacaoFinanciamento converter(Map<String, String> bruto, List<String> avisos) {
        verificarTextoCorrompido(bruto);

        LocalDate data = data(bruto, "data_da_contratacao");
        BigDecimal contratado = dinheiro(bruto, "valor_contratado_reais", true);
        if (contratado.signum() == 0) {
            throw new LinhaInvalidaException(MotivoRejeicao.VALOR_MONETARIO_INVALIDO,
                    "valor_contratado_reais", "valor contratado igual a zero");
        }
        BigDecimal desembolsado = dinheiro(bruto, "valor_desembolsado_reais", false);
        if (desembolsado != null && desembolsado.compareTo(contratado) > 0) {
            avisos.add("Valor desembolsado maior que o valor contratado");
        }

        String municipio = texto(bruto, "municipio");
        if (municipio != null && Normalizador.semAcentos(municipio).equals("SEM MUNICIPIO")) {
            municipio = null;
        }

        BigDecimal juros = dinheiroSemArredondar(bruto, "juros");

        return new OperacaoFinanciamento(
                obrigatorio(bruto, "cliente"),
                cnpj(bruto, "cnpj", true, avisos),
                texto(bruto, "descricao_do_projeto"),
                uf(bruto),
                municipio,
                numeroContrato(bruto),
                data,
                contratado,
                desembolsado,
                texto(bruto, "fonte_de_recurso_desembolsos"),
                texto(bruto, "custo_financeiro"),
                juros,
                inteiro(bruto, "prazo_carencia_meses"),
                inteiro(bruto, "prazo_amortizacao_meses"),
                texto(bruto, "modalidade_de_apoio"),
                texto(bruto, "forma_de_apoio"),
                texto(bruto, "produto"),
                texto(bruto, "instrumento_financeiro"),
                simNao(bruto, "inovacao"),
                texto(bruto, "area_operacional"),
                texto(bruto, "setor_cnae"),
                texto(bruto, "subsetor_cnae_agrupado"),
                texto(bruto, "subsetor_cnae_nome"),
                texto(bruto, "setor_bndes"),
                texto(bruto, "subsetor_bndes"),
                texto(bruto, "porte_do_cliente"),
                texto(bruto, "natureza_do_cliente"),
                texto(bruto, "instituicao_financeira_credenciada"),
                cnpj(bruto, "cnpj_da_instituicao_financeira_credenciada", false, avisos),
                texto(bruto, "tipo_de_garantia"),
                texto(bruto, "tipo_de_excepcionalidade"),
                texto(bruto, "situacao_do_contrato"));
    }

    // ---------------------------------------------------------------- helpers

    private void verificarTextoCorrompido(Map<String, String> bruto) {
        for (Map.Entry<String, String> e : bruto.entrySet()) {
            if (e.getValue() != null && temCaractereCorrompido(e.getValue())) {
                throw new LinhaInvalidaException(MotivoRejeicao.TEXTO_CORROMPIDO, e.getKey(),
                        "caractere inválido no campo");
            }
        }
    }

    /** Controles (exceto tab), DEL, controles C1 (0x7F-0x9F) e U+FFFD, o "caractere de substituição". */
    private static boolean temCaractereCorrompido(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c <= 0x08 || c == 0x0B || c == 0x0C || (c >= 0x0E && c <= 0x1F)
                    || (c >= 0x7F && c <= 0x9F) || c == 0xFFFD) {
                return true;
            }
        }
        return false;
    }

    private String texto(Map<String, String> bruto, String coluna) {
        return Normalizador.texto(bruto.get(coluna), removerAcentos);
    }

    private String obrigatorio(Map<String, String> bruto, String coluna) {
        String valor = texto(bruto, coluna);
        if (valor == null) {
            throw vazio(coluna);
        }
        return valor;
    }

    private LinhaInvalidaException vazio(String coluna) {
        return new LinhaInvalidaException(MotivoRejeicao.CAMPO_OBRIGATORIO_VAZIO, coluna, "campo vazio");
    }

    private LocalDate data(Map<String, String> bruto, String coluna) {
        String valor = obrigatorio(bruto, coluna);
        try {
            LocalDate data = Normalizador.data(valor);
            if (data.isAfter(hoje)) {
                throw new LinhaInvalidaException(MotivoRejeicao.DATA_INVALIDA, coluna, "data futura: " + valor);
            }
            if (data.getYear() < ANO_MINIMO) {
                throw new LinhaInvalidaException(MotivoRejeicao.DATA_INVALIDA, coluna,
                        "data anterior a " + ANO_MINIMO + ": " + valor);
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new LinhaInvalidaException(MotivoRejeicao.DATA_INVALIDA, coluna, "'" + valor + "'");
        }
    }

    private BigDecimal dinheiro(Map<String, String> bruto, String coluna, boolean obrigatorio) {
        BigDecimal valor = dinheiroSemArredondar(bruto, coluna);
        if (valor == null) {
            if (obrigatorio) {
                throw vazio(coluna);
            }
            return null;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Converte para BigDecimal, rejeita negativos e normaliza zeros à direita (2,50 == 2,5). */
    private BigDecimal dinheiroSemArredondar(Map<String, String> bruto, String coluna) {
        String valor = texto(bruto, coluna);
        if (valor == null) {
            return null;
        }
        try {
            BigDecimal numero = Normalizador.monetario(valor);
            if (numero.signum() < 0) {
                throw new LinhaInvalidaException(MotivoRejeicao.VALOR_MONETARIO_INVALIDO, coluna,
                        "valor negativo: " + valor);
            }
            numero = numero.stripTrailingZeros();
            return numero.scale() < 0 ? numero.setScale(0) : numero;
        } catch (NumberFormatException e) {
            throw new LinhaInvalidaException(MotivoRejeicao.VALOR_MONETARIO_INVALIDO, coluna, "'" + valor + "'");
        }
    }

    private Integer inteiro(Map<String, String> bruto, String coluna) {
        String valor = texto(bruto, coluna);
        if (valor == null) {
            return null;
        }
        try {
            return Normalizador.inteiro(valor);
        } catch (NumberFormatException e) {
            throw new LinhaInvalidaException(MotivoRejeicao.NUMERO_INVALIDO, coluna, "'" + valor + "'");
        }
    }

    private Long numeroContrato(Map<String, String> bruto) {
        String valor = obrigatorio(bruto, "numero_do_contrato");
        if (!NUMERO_CONTRATO.matcher(valor).matches()) {
            throw new LinhaInvalidaException(MotivoRejeicao.NUMERO_INVALIDO, "numero_do_contrato",
                    "'" + valor + "'");
        }
        return Long.parseLong(valor);
    }

    private String cnpj(Map<String, String> bruto, String coluna, boolean obrigatorio, List<String> avisos) {
        String valor = texto(bruto, coluna);
        if (valor == null) {
            if (obrigatorio) {
                throw vazio(coluna);
            }
            return null;
        }
        String digitos = NAO_DIGITOS.matcher(valor).replaceAll("");
        if (digitos.length() != 14 || digitos.chars().distinct().count() == 1) {
            throw new LinhaInvalidaException(MotivoRejeicao.CNPJ_INVALIDO, coluna, "'" + valor + "'");
        }
        if (!Normalizador.cnpjDigitosValidos(digitos)) {
            avisos.add("CNPJ com dígitos verificadores inconsistentes");
        }
        return Normalizador.formatarCnpj(digitos);
    }

    private String uf(Map<String, String> bruto) {
        String valor = texto(bruto, "uf");
        if (valor != null && !UFS.contains(valor)) {
            throw new LinhaInvalidaException(MotivoRejeicao.UF_INVALIDA, "uf", "'" + valor + "'");
        }
        return valor;
    }

    private Boolean simNao(Map<String, String> bruto, String coluna) {
        String valor = texto(bruto, coluna);
        if (valor == null) {
            return null;
        }
        String semAcento = Normalizador.semAcentos(valor);
        return switch (semAcento) {
            case "SIM", "S", "TRUE", "1" -> Boolean.TRUE;
            case "NAO", "N", "FALSE", "0" -> Boolean.FALSE;
            default -> throw new LinhaInvalidaException(MotivoRejeicao.VALOR_INVALIDO, coluna,
                    "esperado SIM/NÃO, veio '" + valor + "'");
        };
    }
}
