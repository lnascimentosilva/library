package com.library.app.category.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.library.app.category.exception.CategoryExistentException;
import com.library.app.category.exception.CategoryNotFoundException;
import com.library.app.category.model.Category;
import com.library.app.category.repository.CategoryRepository;
import com.library.app.category.services.CategoryServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.commontests.category.CategoryForTestsRepository;

public class CategoryServicesUTest {

	private CategoryServices categoryServices;
	private CategoryRepository categoryRepository;
	private Validator validator;

	@Before
	public void initTestCase() {
		categoryServices = new CategoryServicesImpl();
		validator = Validation.buildDefaultValidatorFactory().getValidator();

		categoryRepository = Mockito.mock(CategoryRepository.class);

		((CategoryServicesImpl) categoryServices).validator = this.validator;
		((CategoryServicesImpl) categoryServices).categoryRepository = categoryRepository;
	}

	@Test
	public void addCategoryWithNullName() {
		addCategoryWithInvalidName(null);
	}

	@Test
	public void addCategoryWithShortName() {
		addCategoryWithInvalidName("A");
	}

	@Test
	public void addCategoryWithLongName() {
		addCategoryWithInvalidName("This is a long name that will cause an exception to be thrown");
	}

	@Test(expected = CategoryExistentException.class)
	public void addCategoryWithExistentName() {
		Mockito.when(categoryRepository.alreadyExists(CategoryForTestsRepository.java())).thenReturn(true);

		categoryServices.add(CategoryForTestsRepository.java());
	}

	@Test
	public void addValidCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryRepository.alreadyExists(CategoryForTestsRepository.java())).thenReturn(false);
		Mockito.when(categoryRepository.add(CategoryForTestsRepository.java()))
				.thenReturn(java);

		final Category categoryAdded = categoryServices.add(CategoryForTestsRepository.java());
		Assert.assertThat(categoryAdded.getId(), CoreMatchers.is(CoreMatchers.equalTo(1L)));
	}

	private void addCategoryWithInvalidName(final String name) {

		try {
			categoryServices.add(new Category(name));
			Assert.fail("An exception should have been thrown");
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo("name")));
		}

	}

	@Test
	public void updateCategoryWithNullName() {
		updateCategoryWithInvalidName(null);
	}

	@Test
	public void updateCategoryWithShortName() {
		updateCategoryWithInvalidName("A");
	}

	@Test
	public void updateCategoryWithLongName() {
		updateCategoryWithInvalidName("This is a long name that will cause an exception to be thrown");
	}

	@Test(expected = CategoryExistentException.class)
	public void updateCategoryWithExistentName() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryRepository
				.alreadyExists(java))
				.thenReturn(true);

		categoryServices.update(java);
	}

	@Test(expected = CategoryNotFoundException.class)
	public void updateCategoryNotFound() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryRepository
				.alreadyExists(java))
				.thenReturn(false);
		Mockito.when(categoryRepository
				.existsById(1L))
				.thenReturn(false);

		categoryServices.update(java);
	}

	@Test
	public void updateValidCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryRepository
				.alreadyExists(java))
				.thenReturn(false);
		Mockito.when(categoryRepository
				.existsById(1L))
				.thenReturn(true);

		categoryServices.update(java);

		Mockito.verify(categoryRepository).update(java);
	}

	@Test
	public void findCategoryById() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryRepository.findById(1L)).thenReturn(java);

		final Category category = categoryServices.findById(1L);
		Assert.assertThat(category, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(category.getId(), CoreMatchers.is(CoreMatchers.equalTo(1L)));
		Assert.assertThat(category.getName(), CoreMatchers.is(CoreMatchers.equalTo(java.getName())));
	}

	@Test(expected = CategoryNotFoundException.class)
	public void findCategoryByIdNotFound() {
		Mockito.when(categoryRepository
				.findById(1L))
				.thenReturn(null);

		categoryServices.findById(1L);
	}

	@Test
	public void findAllNoCategories() {
		Mockito.when(categoryRepository.findAll("name")).thenReturn(Collections.emptyList());

		final List<Category> categories = categoryServices.findAll();
		Assert.assertThat(categories.isEmpty(), CoreMatchers.is(CoreMatchers.equalTo(true)));
	}

	@Test
	public void findAllCategories() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);
		final Category networks = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.networks(), 2L);

		Mockito.when(categoryRepository.findAll("name")).thenReturn(
				Arrays.asList(java, networks));

		final List<Category> categories = categoryServices.findAll();
		Assert.assertThat(categories.size(), CoreMatchers.is(CoreMatchers.equalTo(2)));
		Assert.assertThat(categories.get(0).getName(), CoreMatchers.is(CoreMatchers.equalTo(java.getName())));
		Assert.assertThat(categories.get(1).getName(), CoreMatchers.is(CoreMatchers.equalTo(networks.getName())));
	}

	private void updateCategoryWithInvalidName(final String name) {

		try {
			categoryServices.update(new Category(name));
			Assert.fail("An exception should have been thrown");
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo("name")));
		}

	}

}
