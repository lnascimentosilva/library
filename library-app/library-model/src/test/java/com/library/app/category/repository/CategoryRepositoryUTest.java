package com.library.app.category.repository;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.library.app.category.model.Category;
import com.library.app.commontests.category.CategoryForTestsRepository;
import com.library.app.commontests.utils.TestBaseRepository;

public class CategoryRepositoryUTest extends TestBaseRepository {

	private CategoryRepository categoryRepository;

	@Before
	public void initTestCase() {
		super.initializeTestDB();

		categoryRepository = new CategoryRepository();
		categoryRepository.em = this.em;
	}

	@After
	public void setDownTestCase() {
		super.closeEntityManager();
	}

	@Test
	public void addCategoryAndFintIt() {
		final Long categoryAddedId = dbCommandExecutor.executeCommand(() -> {
			return categoryRepository.add(CategoryForTestsRepository.java()).getId();
		});

		Assert.assertThat(categoryAddedId, CoreMatchers.is(CoreMatchers.notNullValue()));
		final Category category = categoryRepository.findById(categoryAddedId);
		Assert.assertThat(category, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(category.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.java().getName())));

	}

	@Test
	public void findCategoryByIdNotFound() {
		final Category category = categoryRepository.findById(999L);
		Assert.assertThat(category, CoreMatchers.is(CoreMatchers.nullValue()));

	}

	@Test
	public void findCategoryByIdWithNullId() {
		final Category category = categoryRepository.findById(null);
		Assert.assertThat(category, CoreMatchers.is(CoreMatchers.nullValue()));

	}

	@Test
	public void updateCategory() {
		final Long categoryAddedId = dbCommandExecutor.executeCommand(() -> {
			return categoryRepository.add(CategoryForTestsRepository.java()).getId();
		});

		final Category categoryAfterAdd = categoryRepository.findById(categoryAddedId);
		Assert.assertThat(categoryAfterAdd.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.java().getName())));

		categoryAfterAdd.setName(CategoryForTestsRepository.cleanCode().getName());

		dbCommandExecutor.executeCommand(() -> {
			categoryRepository.update(categoryAfterAdd);
			return null;
		});

		final Category categoryAfterUpdate = categoryRepository.findById(categoryAddedId);
		Assert.assertThat(categoryAfterUpdate.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.cleanCode().getName())));
	}

	@Test
	public void findAllCategories() {
		dbCommandExecutor.executeCommand(() -> {
			CategoryForTestsRepository.allCategories().forEach(categoryRepository::add);
			return null;
		});

		final List<Category> categories = categoryRepository.findAll("name");
		Assert.assertThat(categories.size(), CoreMatchers.is(CoreMatchers.equalTo(4)));
		Assert.assertThat(categories.get(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.architecture().getName())));
		Assert.assertThat(categories.get(1).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.cleanCode().getName())));
		Assert.assertThat(categories.get(2).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.java().getName())));
		Assert.assertThat(categories.get(3).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(CategoryForTestsRepository.networks().getName())));
	}

	@Test
	public void alreadyExistsForAdd() {
		dbCommandExecutor.executeCommand(() -> {
			categoryRepository.add(CategoryForTestsRepository.java());
			return null;
		});

		Assert.assertThat(categoryRepository.alreadyExists(CategoryForTestsRepository.java()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(categoryRepository.alreadyExists(CategoryForTestsRepository.cleanCode()),
				CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	public void alreadyExistsCategoryWithId() {
		final Category java = dbCommandExecutor.executeCommand(() -> {
			categoryRepository.add(CategoryForTestsRepository.cleanCode());
			return categoryRepository.add(CategoryForTestsRepository.java());
		});

		Assert.assertThat(categoryRepository.alreadyExists(java), CoreMatchers.is(CoreMatchers.equalTo(false)));

		java.setName(CategoryForTestsRepository.cleanCode().getName());
		Assert.assertThat(categoryRepository.alreadyExists(java), CoreMatchers.is(CoreMatchers.equalTo(true)));

		java.setName(CategoryForTestsRepository.networks().getName());
		Assert.assertThat(categoryRepository.alreadyExists(java), CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	public void existsById() {
		final Long categoryAddedId = dbCommandExecutor.executeCommand(() -> {
			return categoryRepository.add(CategoryForTestsRepository.java()).getId();
		});

		Assert.assertThat(categoryRepository.existsById(categoryAddedId), CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(categoryRepository.existsById(999L), CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

}
