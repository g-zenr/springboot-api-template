package com.template.api.common.paging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.template.api.common.include.IncludeOptions;

/**
 * Reusable list execution that honors {@link IncludeOptions} flags.
 *
 * <ul>
 *   <li>{@code include=countOnly} — {@link #countOnly} / no row fetch</li>
 *   <li>{@link IncludeOptions#wantsCount()} — run {@code COUNT(*)} for page totals</li>
 *   <li>Otherwise fetch {@code limit + 1} rows for {@code hasNext} without count</li>
 * </ul>
 */
@Component
public class PagedReads {

	/** Sentinel when total count was not computed. */
	public static final long TOTAL_UNKNOWN = -1L;

	private final EntityManager entityManager;

	public PagedReads(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Count matching rows only (no content load). Used for {@code include=countOnly}.
	 */
	public <E> CountResponse countOnly(Class<E> entityClass, Specification<E> specification) {
		return new CountResponse(count(entityClass, specification));
	}

	/**
	 * Load a page of entities, map to DTOs, and build {@link PageResponse}.
	 * Does not apply count-only short-circuit — call {@link #countOnly} first when needed.
	 */
	public <E, T> PageResponse<T> find(
			Class<E> entityClass,
			Specification<E> specification,
			Pageable pageable,
			PageQuery query,
			Function<E, T> mapper) {
		IncludeOptions includes = IncludeOptions.parse(query.getInclude());
		boolean withCount = includes.wantsCount();

		List<E> entities;
		long totalElements;
		int totalPages;
		boolean hasNext;
		boolean last;
		boolean first = pageable.getPageNumber() == 0;
		boolean hasPrevious = pageable.getPageNumber() > 0;

		if (withCount) {
			entities = fetchSlice(entityClass, specification, pageable);
			totalElements = count(entityClass, specification);
			totalPages = pageable.getPageSize() == 0
					? 1
					: (int) Math.ceil((double) totalElements / (double) pageable.getPageSize());
			hasNext = (long) (pageable.getPageNumber() + 1) * pageable.getPageSize() < totalElements;
			last = !hasNext;
		}
		else {
			int limit = pageable.getPageSize();
			Pageable fetchPlusOne = org.springframework.data.domain.PageRequest.of(
					pageable.getPageNumber(),
					limit + 1,
					pageable.getSort());
			List<E> fetched = fetchSlice(entityClass, specification, fetchPlusOne);
			hasNext = fetched.size() > limit;
			entities = hasNext ? new ArrayList<>(fetched.subList(0, limit)) : fetched;
			totalElements = TOTAL_UNKNOWN;
			totalPages = -1;
			last = !hasNext;
		}

		List<T> content = entities.stream().map(mapper).toList();
		return PageResponse.ofMapped(content, pageable, totalElements, totalPages, first, last, hasNext, hasPrevious, query);
	}

	private <E> List<E> fetchSlice(Class<E> entityClass, Specification<E> specification, Pageable pageable) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> cq = cb.createQuery(entityClass);
		Root<E> root = cq.from(entityClass);
		Predicate predicate = toPredicate(specification, root, cq, cb);
		if (predicate != null) {
			cq.where(predicate);
		}
		cq.select(root);
		cq.orderBy(toOrders(cb, root, pageable.getSort()));

		TypedQuery<E> typed = entityManager.createQuery(cq);
		typed.setFirstResult((int) pageable.getOffset());
		typed.setMaxResults(pageable.getPageSize());
		return typed.getResultList();
	}

	private <E> long count(Class<E> entityClass, Specification<E> specification) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<E> root = cq.from(entityClass);
		Predicate predicate = toPredicate(specification, root, cq, cb);
		if (predicate != null) {
			cq.where(predicate);
		}
		cq.select(cb.count(root));
		return entityManager.createQuery(cq).getSingleResult();
	}

	private static <E> Predicate toPredicate(
			Specification<E> specification,
			Root<E> root,
			CriteriaQuery<?> query,
			CriteriaBuilder cb) {
		if (specification == null) {
			return null;
		}
		return specification.toPredicate(root, query, cb);
	}

	private static <E> List<Order> toOrders(CriteriaBuilder cb, Root<E> root, Sort sort) {
		if (sort == null || sort.isUnsorted()) {
			return List.of();
		}
		List<Order> orders = new ArrayList<>();
		for (Sort.Order order : sort) {
			orders.add(order.isAscending()
					? cb.asc(root.get(order.getProperty()))
					: cb.desc(root.get(order.getProperty())));
		}
		return orders;
	}
}
