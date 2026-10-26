package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.dto.AnaliseContrato;
import br.com.fatec.api_bndes.dto.FiltroContrato;
import br.com.fatec.api_bndes.model.Contrato;
import br.com.fatec.api_bndes.repository.ContratoRepository;
import br.com.fatec.api_bndes.repository.specification.ContratoSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContratoService {
    private final ContratoRepository contratoRepository;
    public ContratoService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    // Buscar todos os contrato
    public List<AnaliseContrato> buscar() {
        return contratoRepository.findAll(Sort.by("id").ascending())
                .stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }
    // Filtros de busca
    public List<AnaliseContrato> buscarComFiltros(FiltroContrato filtro) {
        // Constrói a specification com base nos campos preenchidos no DTO
        Specification<Contrato> spec = ContratoSpecification.comFiltros(filtro);

        // Executa a consulta no banco
        List<Contrato> operacoes = contratoRepository.findAll(spec);

        // Mapeia Entidade -> Response DTO
        return operacoes.stream()
                .map(op -> new AnaliseContrato(
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
                        op.getSituacaoDoContrato()
                ))
                .collect(Collectors.toList());
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

    // Método auxiliar para evitar duplicação de código no mapeamento
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
                op.getSituacaoDoContrato()
        );
    }

    private void validarArquivo(MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException( "Selecione um arquivo para importar." );
        }

        String nomeArquivo = arquivo.getOriginalFilename();

        if (nomeArquivo == null) {
            throw new IllegalArgumentException( "O arquivo selecionado é inválido." );
        }

        String nomeEmMinusculo = nomeArquivo.toLowerCase();

        if (!nomeEmMinusculo.endsWith(".txt") && !nomeEmMinusculo.endsWith(".csv")) {

            throw new IllegalArgumentException( "Selecione um arquivo TXT ou CSV." );
        }
    }
    @Transactional
    public int importarArquivo(MultipartFile arquivo) {

        validarArquivo(arquivo);

        List<Contrato> contratosImportados = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(
                        arquivo.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String linha;
            int numeroLinha = 0;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;

                linha = linha.trim();

                if (linha.isEmpty()) {
                    throw new IllegalArgumentException(
                            "A linha " + numeroLinha + " está vazia."
                    );
                }

                String[] dados = linha.split(";", -1);

                if (dados.length != 21) {
                    throw new IllegalArgumentException(
                            "Formato inválido na linha " + numeroLinha
                    );
                }

                String nome = dados[0].trim();
                String idadeTexto = dados[1].trim();

                if (numeroLinha == 1) {
                    nome = nome.replace("\uFEFF", "");
                }

                if (nome.isEmpty()) {
                    throw new IllegalArgumentException( "O nome está vazio na linha " + numeroLinha + "." );
                }

                int idade;

                try {
                    idade = Integer.parseInt(idadeTexto);
                } catch (NumberFormatException erro) {
                    throw new IllegalArgumentException( "A idade da linha " + numeroLinha + " deve ser um número inteiro." );
                }

                if (idade < 0 || idade > 180) {
                    throw new IllegalArgumentException( "A idade da linha " + numeroLinha + " deve estar entre 0 e 180 anos." );
                }

                Contrato contrato = new Contrato();
                contrato.setCliente(nome); //*****
                contrato.setPrazoCarenciaMeses(idade); //*****

                contratosImportados.add(contrato);
            }

            if (contratosImportados.isEmpty()) {
                throw new IllegalArgumentException(
                        "O arquivo não contém contratos."
                );
            }

            contratoRepository.saveAll(contratosImportados);

            return contratosImportados.size();

        } catch (IllegalArgumentException erro) {
            throw erro;

        } catch (Exception erro) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo."
            );
        }
    }

}
