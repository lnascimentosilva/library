package com.library.app.category.services.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.validation.Validator;

import com.library.app.category.exception.CategoryExistentException;
import com.library.app.category.exception.CategoryNotFoundException;
import com.library.app.category.model.Category;
import com.library.app.category.repository.CategoryRepository;
import com.library.app.category.services.CategoryServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.utils.ValidationUtils;
import com.library.app.logaudit.interceptor.Auditable;
import com.library.app.logaudit.interceptor.LogAuditInterceptor;
import com.library.app.logaudit.model.LogAudit.Action;

@Stateless
@Interceptors(LogAuditInterceptor.class)
public class CategoryServicesImpl implements CategoryServices {

	@Inject
	Validator validator;

	@Inject
	CategoryRepository categoryRepository;

	@Override
	@Auditable(action = Action.ADD)
	public Category add(final Category category) throws FieldNotValidException, CategoryExistentException {
		validate(category);

		return categoryRepository.add(category);

	}

	private void validate(final Category category) {
		ValidationUtils.validateEntityFields(validator, category);

		if (categoryRepository.alreadyExists(category)) {
			throw new CategoryExistentException();
		}
	}

	@Override
	@Auditable(action = Action.UPDATE)
	public void update(final Category category)
			throws FieldNotValidException, CategoryExistentException, CategoryNotFoundException {
		this.validate(category);

		if (!categoryRepository.existsById(category.getId())) {
			throw new CategoryNotFoundException();
		}

		categoryRepository.update(category);
	}

	@Override
	public Category findById(final Long id) throws CategoryNotFoundException {
		final Category category = categoryRepository.findById(id);
		if (category == null) {
			throw new CategoryNotFoundException();
		}
		return category;
	}

	@Override
	public List<Category> findAll() {
		return categoryRepository.findAll("name");
	}

}
