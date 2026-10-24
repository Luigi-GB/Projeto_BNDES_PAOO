package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.repository.ContratoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContratoService {
    private final ContratoRepository contratoRepository;
    public ContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    private final ArrayList<Contrato> listaContrato = new ArrayList<>();

    // Listar todos os contrato
    public List<Contrato> listar() {
        return contratoRepository.findAll(Sort.by("id").ascending());
    }
    // Buscar por índice
    public Optional<Contrato> buscarPorId(Integer id) {
        return contratoRepository.findById(id);
    }
    // Adicionar contrato individual
    public Contrato salvar(Contrato contrato) {
        return contratoRepository.save(contrato);
    }
    // Atualizar contrato
    public Optional<Contrato> atualizar(Integer id, Contrato contratoAtualizado) {
        return contratoRepository.findById(id).map(contratoExistente -> {
            contratoExistente.setCliente(contratoAtualizado.getCliente());
            contratoExistente.setCnpj(contratoAtualizado.getCnpj());
            contratoExistente.setDescricaoDoProjeto(contratoAtualizado.getDescricaoDoProjeto());
            contratoExistente.setUf(contratoAtualizado.getUf());
            contratoExistente.setMunicipio(contratoAtualizado.getMunicipio());
            contratoExistente.setNumeroDoContrato(contratoAtualizado.getNumeroDoContrato());
            contratoExistente.setDataDaContratacao(contratoAtualizado.getDataDaContratacao());
            contratoExistente.setValorContratadoReais(contratoAtualizado.getValorContratadoReais());
            contratoExistente.setValorDesembolsadoReais(contratoAtualizado.getValorDesembolsadoReais());
            contratoExistente.setCustoFinanceiro(contratoAtualizado.getCustoFinanceiro());
            contratoExistente.setJuros(contratoAtualizado.getJuros());
            contratoExistente.setPrazoCarenciaMeses(contratoAtualizado.getPrazoCarenciaMeses());
            contratoExistente.setPrazoAmortizacaoMeses(contratoAtualizado.getPrazoAmortizacaoMeses());
            contratoExistente.setModalidadeDeApoio(contratoAtualizado.getModalidadeDeApoio());
            contratoExistente.setFormaDeApoio(contratoAtualizado.getFormaDeApoio());
            contratoExistente.setProduto(contratoAtualizado.getProduto());
            contratoExistente.setInstrumentoFinanceiro(contratoAtualizado.getInstrumentoFinanceiro());
            contratoExistente.setSubsetorBndes(contratoAtualizado.getSubsetorBndes());
            contratoExistente.setPorteDoCliente(contratoAtualizado.getPorteDoCliente());
            contratoExistente.setNaturezaDoCliente(contratoAtualizado.getNaturezaDoCliente());
            contratoExistente.setSituacaoDoContrato(contratoAtualizado.getSituacaoDoContrato());

            return contratoRepository.save(contratoExistente);
        });
    }
    // Deletar contrato
    public void deletar(Integer id) {
        contratoRepository.deleteById(id);
    }

}
