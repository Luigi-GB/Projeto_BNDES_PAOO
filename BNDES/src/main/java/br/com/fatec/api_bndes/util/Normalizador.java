package br.com.fatec.api_bndes.util;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Funções de limpeza e conversão de valores brutos do CSV. */
public final class Normalizador {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    /** Textos que significam "sem informação" na base do BNDES. */
    private static final Set<String> VALORES_NULOS =
            Set.of("", "-", "--", "---", "----------", "N/A", "NA", "NULL", "NULO", "NAN");

    private static final DateTimeFormatter DATA_ISO =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern MARCAS_DE_ACENTO = Pattern.compile("\\p{M}+");
    private static final Pattern MOEDA_E_ESPACOS = Pattern.compile("(?i)R\\$|\\s");
    private static final Pattern MILHAR_BR = Pattern.compile("\\d{1,3}(\\.\\d{3})+");
    private static final Pattern NUMERO_SIMPLES = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Pattern INTEIRO = Pattern.compile("\\d+([.,]0+)?");
    private static final Pattern SEPARADOR_DECIMAL = Pattern.compile("[.,]");
    private static final Pattern DATA_ISO_INICIO = Pattern.compile("\\d{4}-\\d{2}-\\d{2}.*");

    private Normalizador() {
    }

    /**
     * Tira espaços das pontas, junta espaços repetidos, converte para maiúsculas
     * e (opcionalmente) remove acentos. Retorna null quando o valor é vazio ou um
     * marcador de "sem informação" (ex.: "----------").
     */
    public static String texto(String bruto, boolean removerAcentos) {
        if (bruto == null) {
            return null;
        }
        String s = limparEspacosETipografia(bruto);
        if (removerAcentos) {
            s = semAcentos(s);
        }
        s = s.toUpperCase(PT_BR);
        return VALORES_NULOS.contains(s) ? null : s;
    }

    /**
     * Em uma única passada: remove espaços das pontas, junta espaços repetidos (inclusive o espaço
     * não separável) e troca travessões (–, byte 0x96 do Windows-1252) e aspas curvas por hífen e aspas simples.
     */
    private static String limparEspacosETipografia(String bruto) {
        StringBuilder sb = new StringBuilder(bruto.length());
        boolean espacoPendente = false;
        for (int i = 0; i < bruto.length(); i++) {
            char c = bruto.charAt(i);
            if (Character.isWhitespace(c) || c == '\u00A0') {
                espacoPendente = sb.length() > 0;
                continue;
            }
            if (espacoPendente) {
                sb.append(' ');
                espacoPendente = false;
            }
            if (c >= '\u2010' && c <= '\u2015') {
                c = '-';
            } else if (c == '\u2018' || c == '\u2019') {
                c = '\'';
            } else if (c == '\u201C' || c == '\u201D') {
                c = '"';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String semAcentos(String s) {
        if (apenasAscii(s)) {
            return s;   // caminho rápido: a maioria dos campos não tem acento
        }
        return MARCAS_DE_ACENTO.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
    }

    private static boolean apenasAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converte valores como "9090000,0", "1.234,56", "1234.56" ou "R$ 10,5" para BigDecimal,
     * sem passar por double. Lança NumberFormatException se o formato for ambíguo ou inválido.
     */
    public static BigDecimal monetario(String bruto) {
        String s = MOEDA_E_ESPACOS.matcher(bruto).replaceAll("");
        String sinal = "";
        if (s.startsWith("-") || s.startsWith("+")) {
            sinal = s.startsWith("-") ? "-" : "";
            s = s.substring(1);
        }

        int ultimaVirgula = s.lastIndexOf(',');
        int ultimoPonto = s.lastIndexOf('.');

        if (ultimaVirgula >= 0 && ultimoPonto >= 0) {
            // o último separador é o decimal; o outro é separador de milhar
            char decimal = ultimaVirgula > ultimoPonto ? ',' : '.';
            char milhar = decimal == ',' ? '.' : ',';
            s = s.replace(String.valueOf(milhar), "").replace(decimal, '.');
        } else if (ultimaVirgula >= 0) {
            s = s.replace(',', '.');           // "1234,5" -> "1234.5"
        } else if (ultimoPonto >= 0 && MILHAR_BR.matcher(s).matches()) {
            s = s.replace(".", "");            // "1.234.567" é milhar no padrão brasileiro
        }

        if (!NUMERO_SIMPLES.matcher(s).matches()) {
            throw new NumberFormatException("formato monetário inválido: " + bruto);
        }
        return new BigDecimal(sinal + s);
    }

    /** Aceita "24", "24,0" ou "24.0". Lança NumberFormatException para qualquer outra coisa. */
    public static int inteiro(String bruto) {
        String s = bruto.trim();
        if (INTEIRO.matcher(s).matches()) {
            return Integer.parseInt(SEPARADOR_DECIMAL.split(s)[0]);
        }
        throw new NumberFormatException("não é um inteiro não negativo: " + bruto);
    }

    /** Aceita yyyy-MM-dd (padrão do BNDES) e dd/MM/yyyy. Datas impossíveis (30/02) são rejeitadas. */
    public static LocalDate data(String bruto) throws DateTimeParseException {
        String s = bruto.trim();
        if (DATA_ISO_INICIO.matcher(s).matches()) {
            return LocalDate.parse(s.substring(0, 10), DATA_ISO);
        }
        return LocalDate.parse(s, DATA_BR);
    }

    public static boolean cnpjDigitosValidos(String cnpj14) {
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digitoVerificador(cnpj14, pesos1) == cnpj14.charAt(12) - '0'
                && digitoVerificador(cnpj14, pesos2) == cnpj14.charAt(13) - '0';
    }

    private static int digitoVerificador(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public static String formatarCnpj(String d) {
        return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8)
                + "/" + d.substring(8, 12) + "-" + d.substring(12, 14);
    }
}
