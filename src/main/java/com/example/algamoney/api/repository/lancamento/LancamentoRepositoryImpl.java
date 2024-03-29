package com.example.algamoney.api.repository.lancamento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.util.StringUtils;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.filter.LancamentoFilter;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery{

	@PersistenceContext	
	private EntityManager manager;
	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		//restrições (cláusulas) do where
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}

//	@Override
//	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
//		
//		CriteriaBuilder builder = manager.getCriteriaBuilder();
//		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
//		Root<Lancamento> root = criteria.from(Lancamento.class);
//		
//		//restrições (cláusulas) do where
//		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
//		criteria.where(predicates);
//		
//		TypedQuery<Lancamento> query = manager.createQuery(criteria);
//		
//		adicionarRestricoesDePaginacao(query, pageable);
//		
//		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
//	}
//
	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {

		List<Predicate> predicates = new ArrayList<>();

		//where descricao like %filtro%
		//metamodel não deu certo
		if(!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(
				builder.lower(root.get("descricao")),
				"%" + lancamentoFilter.getDescricao() + "%"
			));
		}
		
		if(lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(builder.greaterThanOrEqualTo(
				root.get("dataVencimento"),
				lancamentoFilter.getDataVencimentoDe()
			));
		}
		
		if(lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(builder.lessThanOrEqualTo(
				root.get("dataVencimento"),
				lancamentoFilter.getDataVencimentoAte()
			));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {

		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}
	
	private void adicionarRestricoesDePaginacao(TypedQuery<Lancamento> query, Pageable pageable) {
		
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
		
	}

}


