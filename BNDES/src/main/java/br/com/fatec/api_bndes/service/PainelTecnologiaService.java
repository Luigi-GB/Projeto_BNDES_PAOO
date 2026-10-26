package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.PainelTecnologia;
import br.com.fatec.api_bndes.dto.PainelTecnologia.Fatia;
import br.com.fatec.api_bndes.dto.PainelTecnologia.PontoAno;
import br.com.fatec.api_bndes.dto.PainelTecnologia.Resumo;
import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.service.ClassificadorTecnologia.Origem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PainelTecnologiaService {

    /** COMBINADO = inovação ou palavra-chave; INOVACAO = só coluna inovacao = SIM; PALAVRA_CHAVE = só descrição. */
    public static boolean criterioValido(String criterio) {
        return List.of("COMBINADO", "INOVACAO", "PALAVRA_CHAVE").contains(criterio);
    }

    private static final int TOP = 8;

    public PainelTecnologia montar(List<Contrato> operacoes, String criterio, String arquivo) {
        long totalContratos = 0;
        BigDecimal totalValor = BigDecimal.ZERO;
        long techContratos = 0;
        BigDecimal techValor = BigDecimal.ZERO;
        BigDecimal techDesembolsado = BigDecimal.ZERO;

        Map<Integer, Acc> anoTech = new TreeMap<>();
        Map<Integer, BigDecimal> anoTotal = new TreeMap<>();
        Map<String, Acc> setor = new HashMap<>();
        Map<String, Acc> uf = new HashMap<>();
        Map<String, Acc> porte = new HashMap<>();
        Map<String, Acc> cliente = new HashMap<>();
        Map<Origem, Acc> origem = new HashMap<>();

        for (Contrato op : operacoes) {
            BigDecimal valor = op.getValorContratadoReais();
            if (valor == null || op.getDataDaContratacao() == null) continue;   // registro sem valor/data não entra nas somas
            totalContratos++;
            totalValor = totalValor.add(valor);
            int ano = op.getDataDaContratacao().getYear();
            anoTotal.merge(ano, valor, BigDecimal::add);
            anoTech.computeIfAbsent(ano, k -> new Acc());   // garante o ano mesmo sem tecnologia

            Origem o = ClassificadorTecnologia.classificar(op);
            if (!entra(o, criterio)) continue;

            techContratos++;
            techValor = techValor.add(valor);
            if (op.getValorDesembolsadoReais() != null) techDesembolsado = techDesembolsado.add(op.getValorDesembolsadoReais());

            anoTech.get(ano).somar(valor);
            origem.computeIfAbsent(o, k -> new Acc()).somar(valor);
            setor.computeIfAbsent(nome(op.getSubsetorCnaeAgrupado()), k -> new Acc()).somar(valor);
            uf.computeIfAbsent(nome(op.getUf()), k -> new Acc()).somar(valor);
            porte.computeIfAbsent(nome(op.getPorteDoCliente()), k -> new Acc()).somar(valor);
            cliente.computeIfAbsent(nome(op.getCliente()), k -> new Acc()).somar(valor);
        }

        BigDecimal ticket = techContratos == 0 ? BigDecimal.ZERO
                : techValor.divide(BigDecimal.valueOf(techContratos), 2, RoundingMode.HALF_UP);

        List<PontoAno> porAno = new ArrayList<>();
        anoTech.forEach((ano, acc) -> porAno.add(new PontoAno(ano, acc.n, acc.valor, anoTotal.get(ano))));

        List<Fatia> porCriterio = new ArrayList<>();
        porCriterio.add(fatia("Só coluna inovação", origem.get(Origem.SO_INOVACAO)));
        porCriterio.add(fatia("Só palavra-chave", origem.get(Origem.SO_PALAVRA_CHAVE)));
        porCriterio.add(fatia("Inovação + palavra-chave", origem.get(Origem.AMBOS)));

        return new PainelTecnologia(criterio, arquivo,
                new Resumo(totalContratos, techContratos, totalValor, techValor, techDesembolsado, ticket),
                porAno, porCriterio, top(setor, TOP), top(uf, TOP), top(porte, 10), top(cliente, TOP));
    }

    private static boolean entra(Origem o, String criterio) {
        return switch (criterio) {
            case "INOVACAO" -> o == Origem.SO_INOVACAO || o == Origem.AMBOS;
            case "PALAVRA_CHAVE" -> o == Origem.SO_PALAVRA_CHAVE || o == Origem.AMBOS;
            default -> o != Origem.NENHUMA;
        };
    }

    private static String nome(String s) {
        return s == null || s.isBlank() ? "NÃO INFORMADO" : s;
    }

    private static Fatia fatia(String nome, Acc a) {
        return a == null ? new Fatia(nome, 0, BigDecimal.ZERO) : new Fatia(nome, a.n, a.valor);
    }

    private static List<Fatia> top(Map<String, Acc> mapa, int limite) {
        return mapa.entrySet().stream()
                .map(e -> fatia(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Fatia::valor).reversed())
                .limit(limite)
                .toList();
    }

    private static class Acc {
        long n;
        BigDecimal valor = BigDecimal.ZERO;

        void somar(BigDecimal v) {
            n++;
            valor = valor.add(v);
        }
    }
}
