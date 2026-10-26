package br.com.fatec.api_bndes.repository;

import br.com.fatec.api_bndes.model.OperacaoImportada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OperacaoRepository extends JpaRepository<OperacaoImportada, Long> {

    /** Hash de todas as linhas já gravadas, para a importação descartar o que já está no banco. */
    @Query("select o.hashLinha from OperacaoImportada o")
    List<String> todosOsHashes();
}
