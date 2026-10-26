package br.com.fatec.api_bndes.util;

import br.com.fatec.api_bndes.dto.OperacaoFinanciamento;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Impressão digital (SHA-256) de uma linha limpa: duas linhas com todos os campos iguais têm o mesmo hash. */
public final class HashLinha {

    private HashLinha() {
    }

    public static String de(OperacaoFinanciamento op) {
        try {
            // o toString do record lista todos os campos já limpos, sempre na mesma ordem
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(op.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
