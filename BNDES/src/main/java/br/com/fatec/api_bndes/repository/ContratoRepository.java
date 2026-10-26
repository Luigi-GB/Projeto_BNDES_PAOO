package br.com.fatec.api_bndes.repository;

import br.com.fatec.api_bndes.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

public interface ContratoRepository extends JpaRepository<Contrato, Integer>, JpaSpecificationExecutor<Contrato> {
} //Talvez tenha de mudar de Integer para String
