package br.com.fatec.api_bndes.repository;

import br.com.fatec.api_bndes.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
}
