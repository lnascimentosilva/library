package com.library.app.commontests.utils;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Ignore;

import com.library.app.author.model.Author;
import com.library.app.book.model.Book;
import com.library.app.category.model.Category;
import com.library.app.logaudit.model.LogAudit;
import com.library.app.order.model.Order;
import com.library.app.user.model.User;

@Ignore
@Stateless
public class TestRepositoryEJB {

	@PersistenceContext
	private EntityManager em;

	private static final List<Class<?>> ENTITIES_TO_REMOVE = Arrays.asList(LogAudit.class, Order.class, Book.class,
			User.class, Category.class, Author.class);

	public void deleteAll() {
		ENTITIES_TO_REMOVE.forEach(this::deleteAllForEntity);
	}

	@SuppressWarnings("unchecked")
	private void deleteAllForEntity(final Class<?> entityClass) {
		final List<Object> rows = em.createQuery("Select e From " + entityClass.getSimpleName() + " e").getResultList();
		for (final Object row : rows) {
			em.remove(row);
		}
	}

	public void add(final Object entity) {
		em.persist(entity);
	}

}