package br.com.fatec.api_bndes.repository;

import br.com.fatec.api_bndes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
