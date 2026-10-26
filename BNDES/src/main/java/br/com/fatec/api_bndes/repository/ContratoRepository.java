package br.com.fatec.api_bndes.repository;

import br.com.fatec.api_bndes.dto.ResumoUf;
import br.com.fatec.api_bndes.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Integer>, JpaSpecificationExecutor<Contrato> {

    boolean existsByNumeroDoContrato(String numeroDoContrato);

    boolean existsByNumeroDoContratoAndIdNot(String numeroDoContrato, Integer id);

    /** Devolve, entre os números informados, os que já existem no banco (usado na reimportação). */
    @Query("select c.numeroDoContrato from Contrato c where c.numeroDoContrato in :numeros")
    List<String> findNumerosExistentes(@Param("numeros") Collection<String> numeros);

    /** Base do gráfico: quantidade e valor contratado agrupados por UF. */
    @Query("select new br.com.fatec.api_bndes.dto.ResumoUf(c.uf, count(c), sum(c.valorContratadoReais)) "
            + "from Contrato c where c.uf is not null group by c.uf order by sum(c.valorContratadoReais) desc")
    List<ResumoUf> resumirPorUf();
}
