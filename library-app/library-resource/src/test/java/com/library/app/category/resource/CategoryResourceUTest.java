package com.library.app.category.resource;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.library.app.category.exception.CategoryExistentException;
import com.library.app.category.exception.CategoryNotFoundException;
import com.library.app.category.model.Category;
import com.library.app.category.services.CategoryServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.HTTPCode;
import com.library.app.commontests.category.CategoryForTestsRepository;
import com.library.app.commontests.utils.FileTestNameUtils;
import com.library.app.commontests.utils.JsonTestUtils;
import com.library.app.commontests.utils.ResourceDefinitions;

public class CategoryResourceUTest {

	private CategoryResource categoryResource;

	private static final String PATH_RESOURCE = ResourceDefinitions.CATEGORY.getResourceName();

	@Mock
	private CategoryServices categoryServices;

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);
		categoryResource = new CategoryResource();

		categoryResource.categoryServices = this.categoryServices;
		categoryResource.categoryJsonConverter = new CategoryJsonConverter();
	}

	@Test
	public void addValidCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.when(categoryServices.add(CategoryForTestsRepository.java())).thenReturn(java);

		final Response response = categoryResource
				.add(JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
						"category.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.CREATED.getCode())));
		JsonTestUtils.assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
	}

	@Test
	public void addExistentCategory() {
		Mockito.when(categoryServices.add(CategoryForTestsRepository.java()))
				.thenThrow(new CategoryExistentException());

		final Response response = categoryResource
				.add(JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
						"category.json")));

		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));

		assertJsonResponseWithFile(response, "categoryAlreadyExists.json");
	}

	@Test
	public void addCategoryWithNullName() {
		Mockito.when(categoryServices.add(new Category()))
				.thenThrow(new FieldNotValidException("name", "may not be null"));

		final Response response = categoryResource
				.add(JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
						"categoryWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "categoryErrorNullName.json");
	}

	@Test
	public void updateValidCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);
		final Response response = categoryResource.update(1L,
				JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "category.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		Mockito.verify(categoryServices).update(java);
	}

	@Test
	public void updateCategoryWithNameBelongingToOtherCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);

		Mockito.doThrow(new CategoryExistentException()).when(categoryServices).update(java);

		final Response response = categoryResource.update(1L,
				JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "category.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "categoryAlreadyExists.json");
	}

	@Test
	public void updateCategoryWithNullName() {
		final Category categoryWithNullName = CategoryForTestsRepository.categoryWithId(new Category(), 1L);
		Mockito.doThrow(new FieldNotValidException("name", "may not be null")).when(categoryServices)
				.update(categoryWithNullName);

		final Response response = categoryResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "categoryWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "categoryErrorNullName.json");
	}

	@Test
	public void updateCategoryNotFound() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 2L);
		Mockito.doThrow(new CategoryNotFoundException()).when(categoryServices).update(java);

		final Response response = categoryResource.update(2L,
				JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "category.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
		assertJsonResponseWithFile(response, "categoryNotFound.json");
	}

	@Test
	public void findCategory() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);
		Mockito.when(categoryServices.findById(1L)).thenReturn(java);

		final Response response = categoryResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "categoryFound.json");
	}

	@Test
	public void findCategoryNotFound() {
		Mockito.when(categoryServices.findById(1L)).thenThrow(new CategoryNotFoundException());

		final Response response = categoryResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	public void findAllNoCategory() {
		Mockito.when(categoryServices.findAll()).thenReturn(new ArrayList<>());

		final Response response = categoryResource.findAll();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "emptyListOfCategories.json");
	}

	@Test
	public void findAllTwoCategories() {
		final Category java = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.java(), 1L);
		final Category networks = CategoryForTestsRepository.categoryWithId(CategoryForTestsRepository.networks(), 2L);

		Mockito.when(categoryServices.findAll()).thenReturn(
				Arrays.asList(java, networks));

		final Response response = categoryResource.findAll();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "twoCategories.json");
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		JsonTestUtils.assertJsonMatchesFileContent(response.getEntity().toString(),
				FileTestNameUtils.getPathFileResponse(PATH_RESOURCE, fileName));
	}

}
