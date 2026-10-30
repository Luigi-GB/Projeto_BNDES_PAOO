package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.AnaliseContrato;
import br.com.fatec.api_bndes.dto.FiltroContrato;
import br.com.fatec.api_bndes.dto.ResumoUf;
import br.com.fatec.api_bndes.exception.DadosInvalidosException;
import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.repository.ContratoRepository;
import br.com.fatec.api_bndes.repository.specification.ContratoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Regras de negócio do CRUD de contratos: validação, normalização e consultas. */
@Service
public class ContratoService {

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB",
            "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO", "IE");

    private final ContratoRepository contratoRepository;

    public ContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    // ------------------------------------------------------------------ consultas

    /** Lista (sem filtro). limite = null devolve tudo; senão devolve só os primeiros registros. */
    public Page<AnaliseContrato> listar(Integer limite) {
        return buscarComFiltros(null, limite);
    }

    /** Filtra pelos critérios preenchidos. O total devolvido na página é o total que casa com o filtro. */
    public Page<AnaliseContrato> buscarComFiltros(FiltroContrato filtro, Integer limite) {
        Sort ordem = Sort.by("id").ascending();
        Pageable pagina = (limite == null || limite <= 0)
                ? Pageable.unpaged(ordem)
                : PageRequest.of(0, limite, ordem);
        Specification<Contrato> spec = ContratoSpecification.comFiltros(filtro);
        return contratoRepository.findAll(spec, pagina).map(this::converterParaDto);
    }

    public Optional<Contrato> buscarPorId(Integer id) {
        return contratoRepository.findById(id);
    }

    public List<ResumoUf> resumirPorUf() {
        return contratoRepository.resumirPorUf();
    }

    // ------------------------------------------------------------------ escrita

    public Contrato salvar(Contrato contrato) {
        normalizar(contrato);
        validar(contrato, null);
        return contratoRepository.save(contrato);
    }

    /** Vazio = id não existe (404). Dados ruins lançam DadosInvalidosException (400). */
    public Optional<Contrato> atualizar(Integer id, Contrato dados) {
        return contratoRepository.findById(id).map(existente -> {
            normalizar(dados);
            validar(dados, id);
            copiar(dados, existente);
            return contratoRepository.save(existente);
        });
    }

    /** @return false se o id não existe (404). */
    public boolean deletar(Integer id) {
        if (!contratoRepository.existsById(id)) {
            return false;
        }
        contratoRepository.deleteById(id);
        return true;
    }

    // ------------------------------------------------------------------ regras

    private void normalizar(Contrato c) {
        c.setCliente(limpar(c.getCliente()));
        c.setCnpj(limpar(c.getCnpj()));
        c.setNumeroDoContrato(limpar(c.getNumeroDoContrato()));
        c.setMunicipio(limpar(c.getMunicipio()));
        String uf = limpar(c.getUf());
        c.setUf(uf == null ? null : uf.toUpperCase(Locale.ROOT));
        c.setPorteDoCliente(limpar(c.getPorteDoCliente()));
        c.setSubsetorBndes(limpar(c.getSubsetorBndes()));
        c.setSituacaoDoContrato(limpar(c.getSituacaoDoContrato()));
    }

    private static String limpar(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }

    private void validar(Contrato c, Integer idAtual) {
        List<String> erros = new ArrayList<>();

        if (c.getCliente() == null) erros.add("cliente é obrigatório");
        if (c.getNumeroDoContrato() == null) {
            erros.add("numeroDoContrato é obrigatório");
        }
        if (c.getDataDaContratacao() == null) {
            erros.add("dataDaContratacao é obrigatória");
        } else if (c.getDataDaContratacao().isAfter(LocalDate.now())) {
            erros.add("dataDaContratacao não pode ser futura");
        }
        if (c.getValorContratadoReais() == null) {
            erros.add("valorContratadoReais é obrigatório");
        } else if (c.getValorContratadoReais().compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("valorContratadoReais deve ser maior que zero");
        }
        if (c.getValorDesembolsadoReais() != null && c.getValorDesembolsadoReais().signum() < 0) {
            erros.add("valorDesembolsadoReais não pode ser negativo");
        }
        if (c.getUf() != null && !UFS.contains(c.getUf())) {
            erros.add("uf inválida: " + c.getUf());
        }
        if (c.getCnpj() != null && c.getCnpj().replaceAll("\\D", "").length() != 14) {
            erros.add("cnpj deve ter 14 dígitos");
        }

        if (!erros.isEmpty()) {
            throw new DadosInvalidosException(erros);
        }
    }

    /** Copia todos os campos de negócio (o id do destino é preservado). */
    private static void copiar(Contrato origem, Contrato destino) {
        destino.setCliente(origem.getCliente());
        destino.setCnpj(origem.getCnpj());
        destino.setDescricaoDoProjeto(origem.getDescricaoDoProjeto());
        destino.setUf(origem.getUf());
        destino.setMunicipio(origem.getMunicipio());
        destino.setNumeroDoContrato(origem.getNumeroDoContrato());
        destino.setDataDaContratacao(origem.getDataDaContratacao());
        destino.setValorContratadoReais(origem.getValorContratadoReais());
        destino.setValorDesembolsadoReais(origem.getValorDesembolsadoReais());
        destino.setFonteDeRecursoDesembolsos(origem.getFonteDeRecursoDesembolsos());
        destino.setCustoFinanceiro(origem.getCustoFinanceiro());
        destino.setJuros(origem.getJuros());
        destino.setPrazoCarenciaMeses(origem.getPrazoCarenciaMeses());
        destino.setPrazoAmortizacaoMeses(origem.getPrazoAmortizacaoMeses());
        destino.setModalidadeDeApoio(origem.getModalidadeDeApoio());
        destino.setFormaDeApoio(origem.getFormaDeApoio());
        destino.setProduto(origem.getProduto());
        destino.setInstrumentoFinanceiro(origem.getInstrumentoFinanceiro());
        destino.setInovacao(origem.getInovacao());
        destino.setAreaOperacional(origem.getAreaOperacional());
        destino.setSetorCnae(origem.getSetorCnae());
        destino.setSubsetorCnaeAgrupado(origem.getSubsetorCnaeAgrupado());
        destino.setSubsetorCnaeNome(origem.getSubsetorCnaeNome());
        destino.setSetorBndes(origem.getSetorBndes());
        destino.setSubsetorBndes(origem.getSubsetorBndes());
        destino.setPorteDoCliente(origem.getPorteDoCliente());
        destino.setNaturezaDoCliente(origem.getNaturezaDoCliente());
        destino.setInstituicaoFinanceiraCredenciada(origem.getInstituicaoFinanceiraCredenciada());
        destino.setCnpjDaInstituicaoFinanceiraCredenciada(origem.getCnpjDaInstituicaoFinanceiraCredenciada());
        destino.setTipoDeGarantia(origem.getTipoDeGarantia());
        destino.setTipoDeExcepcionalidade(origem.getTipoDeExcepcionalidade());
        destino.setSituacaoDoContrato(origem.getSituacaoDoContrato());
    }

    private AnaliseContrato converterParaDto(Contrato op) {
        return new AnaliseContrato(
                op.getId(),
                op.getNumeroDoContrato(),
                op.getCliente(),
                op.getUf(),
                op.getMunicipio(),
                op.getDataDaContratacao(),
                op.getValorContratadoReais(),
                op.getValorDesembolsadoReais(),
                op.getSubsetorBndes(),
                op.getPorteDoCliente(),
                op.getSituacaoDoContrato());
    }
}
