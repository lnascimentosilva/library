package com.library.app.author.repository;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.library.app.author.model.Author;
import com.library.app.author.model.filter.AuthorFilter;
import com.library.app.common.model.PaginatedData;
import com.library.app.common.model.filter.PaginationData;
import com.library.app.common.model.filter.PaginationData.OrderMode;
import com.library.app.commontests.author.AuthorForTestsRepository;
import com.library.app.commontests.utils.TestBaseRepository;

public class AuthorRepositoryUTest extends TestBaseRepository {

	private AuthorRepository authorRepository;

	@Before
	public void initTestCase() {
		super.initializeTestDB();

		authorRepository = new AuthorRepository();
		authorRepository.em = this.em;
	}

	@After
	public void setDownTestCase() {
		super.closeEntityManager();
	}

	@Test
	public void addAuthorAndFintIt() {
		final Long authorAddedId = dbCommandExecutor.executeCommand(() -> {
			return authorRepository.add(AuthorForTestsRepository.robertMartin()).getId();
		});

		Assert.assertThat(authorAddedId, CoreMatchers.is(CoreMatchers.notNullValue()));
		final Author author = authorRepository.findById(authorAddedId);
		Assert.assertThat(author, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(author.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));

	}

	@Test
	public void findAuthorByIdNotFound() {
		final Author author = authorRepository.findById(999L);
		Assert.assertThat(author, CoreMatchers.is(CoreMatchers.nullValue()));
	}

	@Test
	public void updateAuthor() {
		final Long authorAddedId = dbCommandExecutor.executeCommand(() -> {
			return authorRepository.add(AuthorForTestsRepository.robertMartin()).getId();
		});
		Assert.assertThat(authorAddedId, CoreMatchers.is(CoreMatchers.notNullValue()));

		final Author author = authorRepository.findById(authorAddedId);
		Assert.assertThat(author.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));

		author.setName("Uncle Bob");
		dbCommandExecutor.executeCommand(() -> {
			authorRepository.update(author);
			return null;
		});

		final Author authorAfterUpdate = authorRepository.findById(authorAddedId);
		Assert.assertThat(authorAfterUpdate.getName(), CoreMatchers.is(CoreMatchers.equalTo("Uncle Bob")));
	}

	@Test
	public void existsById() {
		final Long authorAddedId = dbCommandExecutor.executeCommand(() -> {
			return authorRepository.add(AuthorForTestsRepository.robertMartin()).getId();
		});

		Assert.assertThat(authorRepository.existsById(authorAddedId), CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(authorRepository.existsById(999l), CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	public void findByFilterNoFilter() {
		loadDataForFindByFilter();

		final PaginatedData<Author> result = authorRepository.findByFilter(new AuthorFilter());
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(4)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(4)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.erichGamma().getName())));
		Assert.assertThat(result.getRow(1).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.jamesGosling().getName())));
		Assert.assertThat(result.getRow(2).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.martinFowler().getName())));
		Assert.assertThat(result.getRow(3).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));
	}

	@Test
	public void findByFilterFilteringByNameAndPaginatingAndOrderingDescending() {
		loadDataForFindByFilter();

		final AuthorFilter authorFilter = new AuthorFilter();
		authorFilter.setName("o");
		authorFilter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.DESCENDING));

		PaginatedData<Author> result = authorRepository.findByFilter(authorFilter);
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(3)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(2)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.robertMartin().getName())));
		Assert.assertThat(result.getRow(1).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.martinFowler().getName())));

		authorFilter.setPaginationData(new PaginationData(2, 2, "name", OrderMode.DESCENDING));
		result = authorRepository.findByFilter(authorFilter);

		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(3)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(AuthorForTestsRepository.jamesGosling().getName())));

	}

	private void loadDataForFindByFilter() {
		dbCommandExecutor.executeCommand(() -> {
			authorRepository.add(AuthorForTestsRepository.robertMartin());
			authorRepository.add(AuthorForTestsRepository.jamesGosling());
			authorRepository.add(AuthorForTestsRepository.martinFowler());
			authorRepository.add(AuthorForTestsRepository.erichGamma());

			return null;
		});
	}

}
