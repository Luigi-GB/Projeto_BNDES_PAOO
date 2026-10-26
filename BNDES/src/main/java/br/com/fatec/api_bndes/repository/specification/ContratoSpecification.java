package br.com.fatec.api_bndes.repository.specification;

import br.com.fatec.api_bndes.dto.FiltroContrato;
import br.com.fatec.api_bndes.model.Contrato;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ContratoSpecification {
    public static Specification<Contrato> comFiltros(FiltroContrato filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return criteriaBuilder.conjunction();
            }

            // Filtro por UF (Exact Match)
            if (filtro.getUf() != null && !filtro.getUf().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("uf")),
                        filtro.getUf().toUpperCase()
                ));
            }

            // Filtro por Município (Like / Contém)
            if (filtro.getMunicipio() != null && !filtro.getMunicipio().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("municipio")),
                        "%" + filtro.getMunicipio().toUpperCase() + "%"
                ));
            }

            // Filtro por Porte do Cliente
            if (filtro.getPorteDoCliente() != null && !filtro.getPorteDoCliente().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("porteDoCliente")),
                        filtro.getPorteDoCliente().toUpperCase()
                ));
            }

            // Filtro por Subsetor BNDES
            if (filtro.getSubsetorBndes() != null && !filtro.getSubsetorBndes().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("subsetorBndes")),
                        "%" + filtro.getSubsetorBndes().toUpperCase() + "%"
                ));
            }

            // Filtro por Situação do Contrato
            if (filtro.getSituacaoDoContrato() != null && !filtro.getSituacaoDoContrato().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("situacaoDoContrato")),
                        filtro.getSituacaoDoContrato().toUpperCase()
                ));
            }

            // Filtro por Período de Contratação (Data Início)
            if (filtro.getDataInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dataDaContratacao"),
                        filtro.getDataInicio()
                ));
            }

            // Filtro por Período de Contratação (Data Fim)
            if (filtro.getDataFim() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dataDaContratacao"),
                        filtro.getDataFim()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
