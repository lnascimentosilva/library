package com.library.app.author.services.impl;

import java.util.Arrays;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.library.app.author.exception.AuthorNotFoundException;
import com.library.app.author.model.Author;
import com.library.app.author.model.filter.AuthorFilter;
import com.library.app.author.repository.AuthorRepository;
import com.library.app.author.services.AuthorServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.PaginatedData;
import com.library.app.commontests.author.AuthorForTestsRepository;

public class AuthorServicesUTest {
	private static Validator validator;
	private AuthorServices authorServices;

	@Mock
	private AuthorRepository authorRepository;

	@BeforeClass
	public static void initTestClass() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);

		authorServices = new AuthorServicesImpl();

		((AuthorServicesImpl) authorServices).authorRepository = authorRepository;
		((AuthorServicesImpl) authorServices).validator = validator;
	}

	@Test
	public void addAuthorWithNullName() {
		addAuthorWithInvalidName(null);
	}

	@Test
	public void addAuthorWithShortName() {
		addAuthorWithInvalidName("A");
	}

	@Test
	public void addAuthorWithLongName() {
		addAuthorWithInvalidName("This is a very long name that will cause an exception to be thrown");
	}

	@Test
	public void addValidAuthor() {
		Mockito.when(authorRepository.add(AuthorForTestsRepository.robertMartin()))
				.thenReturn(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));

		try {
			final Author authorAdded = authorServices.add(AuthorForTestsRepository.robertMartin());
			Assert.assertThat(authorAdded.getId(), CoreMatchers.equalTo(1L));
		} catch (final FieldNotValidException e) {
			Assert.fail("No error should have been thrown");
		}
	}

	@Test
	public void updateAuthorWithNullName() {
		updateAuthorWithInvalidName(null);
	}

	@Test
	public void updateAuthorWithShortName() {
		updateAuthorWithInvalidName("A");
	}

	@Test
	public void updateAuthorWithLongName() {
		updateAuthorWithInvalidName("This is a very long name that will cause an exception to be thrown");
	}

	@Test(expected = AuthorNotFoundException.class)
	public void updateAuthorNotFound() throws Exception {
		Mockito.when(authorRepository.existsById(1L)).thenReturn(false);

		authorServices.update(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));
	}

	@Test
	public void updateValidAuthor() throws Exception {
		final Author authorToUpdate = AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(),
				1L);
		Mockito.when(authorRepository.existsById(1L)).thenReturn(true);

		authorServices.update(authorToUpdate);
		Mockito.verify(authorRepository).update(authorToUpdate);
	}

	@Test(expected = AuthorNotFoundException.class)
	public void findAuthorByIdNotFound() throws AuthorNotFoundException {
		Mockito.when(authorRepository.findById(1L)).thenReturn(null);

		authorServices.findById(1L);
	}

	@Test
	public void findAuthorById() throws AuthorNotFoundException {
		Mockito.when(authorRepository.findById(1L))
				.thenReturn(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));

		final Author author = authorServices.findById(1L);
		Assert.assertThat(author, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(author.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));
	}

	@Test
	public void findAuthorByFilter() {
		final PaginatedData<Author> authors = new PaginatedData<Author>(1,
				Arrays.asList(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(),
						1L)));
		Mockito.when(authorRepository.findByFilter((AuthorFilter) Matchers.anyObject())).thenReturn(authors);

		final PaginatedData<Author> authorsReturned = authorServices.findByFilter(new AuthorFilter());
		Assert.assertThat(authorsReturned.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(authorsReturned.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));
	}

	private void updateAuthorWithInvalidName(final String name) {
		try {
			authorServices.update(new Author(name));
			Assert.fail("An error should have been thrown");
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo("name")));
		}
	}

	private void addAuthorWithInvalidName(final String name) {
		try {
			authorServices.add(new Author(name));
			Assert.fail("An error should have been thrown");
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo("name")));
		}
	}

}