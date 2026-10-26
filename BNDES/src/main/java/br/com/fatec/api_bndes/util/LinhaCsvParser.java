package br.com.fatec.api_bndes.util;

import br.com.fatec.api_bndes.exception.LinhaInvalidaException;
import br.com.fatec.api_bndes.exception.MotivoRejeicao;

import java.util.ArrayList;
import java.util.List;

/**
 * Divide UMA linha de CSV em campos, respeitando aspas e aspas duplicadas ("").
 * Cada linha física é um registro; campos com quebra de linha dentro das aspas
 * não são suportados (o arquivo do BNDES não tem nenhum).
 */
public final class LinhaCsvParser {

    private LinhaCsvParser() {
    }

    public static List<String> dividir(String linha, char separador) {
        long aspas = linha.chars().filter(c -> c == '"').count();
        if (aspas % 2 != 0) {
            throw new LinhaInvalidaException(MotivoRejeicao.ASPAS_INVALIDAS, null, "quantidade ímpar de aspas");
        }

        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        boolean campoComAspas = false;
        int i = 0;
        int n = linha.length();

        while (i < n) {
            char c = linha.charAt(i);
            if (entreAspas) {
                if (c == '"') {
                    if (i + 1 < n && linha.charAt(i + 1) == '"') {
                        atual.append('"');
                        i += 2;
                        continue;
                    }
                    entreAspas = false;
                    if (i + 1 < n && linha.charAt(i + 1) != separador) {
                        throw new LinhaInvalidaException(MotivoRejeicao.ASPAS_INVALIDAS, null,
                                "texto logo após as aspas de fechamento");
                    }
                } else {
                    atual.append(c);
                }
            } else if (c == '"' && atual.length() == 0 && !campoComAspas) {
                entreAspas = true;
                campoComAspas = true;
            } else if (c == separador) {
                campos.add(atual.toString());
                atual.setLength(0);
                campoComAspas = false;
            } else {
                atual.append(c);
            }
            i++;
        }

        if (entreAspas) {
            throw new LinhaInvalidaException(MotivoRejeicao.ASPAS_INVALIDAS, null, "aspas não fechadas");
        }
        campos.add(atual.toString());
        return campos;
    }
}
