package com.library.app.author.resource;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.library.app.author.exception.AuthorNotFoundException;
import com.library.app.author.model.Author;
import com.library.app.author.model.filter.AuthorFilter;
import com.library.app.author.services.AuthorServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;
import com.library.app.commontests.author.AuthorForTestsRepository;
import com.library.app.commontests.utils.FileTestNameUtils;
import com.library.app.commontests.utils.JsonTestUtils;
import com.library.app.commontests.utils.ResourceDefinitions;

public class AuthorResourceUTest {

	private AuthorResource authorResource;

	private static final String PATH_RESOURCE = ResourceDefinitions.AUTHOR.getResourceName();

	@Mock
	private AuthorServices authorServices;

	@Mock
	private UriInfo uriInfo;

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);
		authorResource = new AuthorResource();

		authorResource.authorServices = authorServices;
		authorResource.authorJsonConverter = new AuthorJsonConverter();
		authorResource.uriInfo = uriInfo;
	}

	@Test
	public void addValidAuthor() {
		Mockito.when(authorServices.add(AuthorForTestsRepository.robertMartin()))
				.thenReturn(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));

		final Response response = authorResource
				.add(JsonTestUtils
						.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "robertMartin.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.CREATED.getCode())));
		JsonTestUtils.assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
	}

	@Test
	public void addAuthorWithNullName() throws Exception {
		Mockito.when(authorServices.add((Author) Matchers.anyObject()))
				.thenThrow(new FieldNotValidException("name", "may not be null"));

		final Response response = authorResource
				.add(JsonTestUtils
						.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "authorWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "authorErrorNullName.json");
	}

	@Test
	public void updateValidAuthor() throws Exception {
		final Response response = authorResource.update(1L,
				JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "robertMartin.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		Mockito.verify(authorServices)
				.update(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));
	}

	@Test
	public void updateAuthorWithNullName() throws Exception {
		Mockito.doThrow(new FieldNotValidException("name", "may not be null")).when(authorServices).update(
				(Author) Mockito.anyObject());

		final Response response = authorResource.update(1L,
				JsonTestUtils
						.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "authorWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "authorErrorNullName.json");
	}

	@Test
	public void updateAuthorNotFound() throws Exception {
		Mockito.doThrow(new AuthorNotFoundException()).when(authorServices)
				.update(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 2L));

		final Response response = authorResource.update(2L,
				JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "robertMartin.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	public void findAuthor() throws AuthorNotFoundException {
		Mockito.when(authorServices.findById(1L))
				.thenReturn(AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));

		final Response response = authorResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "robertMartinFound.json");
	}

	@Test
	public void findAuthorNotFound() throws AuthorNotFoundException {
		Mockito.when(authorServices.findById(1L)).thenThrow(new AuthorNotFoundException());

		final Response response = authorResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFilterNoFilter() {
		final List<Author> authors = Arrays.asList(
				AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.erichGamma(), 2L),
				AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.jamesGosling(), 3L),
				AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.martinFowler(), 4L),
				AuthorForTestsRepository.authorWithId(AuthorForTestsRepository.robertMartin(), 1L));

		final MultivaluedMap<String, String> multiMap = Mockito.mock(MultivaluedMap.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(multiMap);

		Mockito.when(authorServices.findByFilter((AuthorFilter) Mockito.anyObject())).thenReturn(
				new PaginatedData<Author>(authors.size(), authors));

		final Response response = authorResource.findByFilter();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "authorsAllInOnePage.json");
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		JsonTestUtils.assertJsonMatchesFileContent(response.getEntity().toString(),
				FileTestNameUtils.getPathFileResponse(PATH_RESOURCE, fileName));
	}
}
