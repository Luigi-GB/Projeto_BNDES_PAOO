package br.com.fatec.api_bndes.service;

import br.com.fatec.api_bndes.model.Cliente;
import br.com.fatec.api_bndes.repository.ClienteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    private final ArrayList<Cliente> listaCliente = new ArrayList<>();

    // Listar todos os cliente
    public List<Cliente> listar() {
        return clienteRepository.findAll(Sort.by("id").ascending());
    }
    // Buscar por índice
    public Optional<Cliente> buscarPorId(Integer id) {
        return clienteRepository.findById(id);
    }
    // Adicionar cliente individual
    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
    // Atualizar cliente
    public Optional<Cliente> atualizar(Integer id, Cliente clienteAtualizado) {
        return clienteRepository.findById(id).map(clienteExistente -> {
            clienteExistente.setNome(clienteAtualizado.getNome());
            return clienteRepository.save(clienteExistente);
        });
    }
    // Deletar cliente
    public void deletar(Integer id) {
        clienteRepository.deleteById(id);
    }
}

