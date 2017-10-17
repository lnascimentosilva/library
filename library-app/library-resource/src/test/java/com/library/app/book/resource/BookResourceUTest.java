package com.library.app.book.resource;

import static com.library.app.commontests.book.BookArgumentMatcher.bookEq;
import static com.library.app.commontests.book.BookForTestsRepository.bookWithId;
import static com.library.app.commontests.book.BookForTestsRepository.cleanCode;
import static com.library.app.commontests.book.BookForTestsRepository.designPatterns;
import static com.library.app.commontests.utils.FileTestNameUtils.getPathFileRequest;
import static com.library.app.commontests.utils.FileTestNameUtils.getPathFileResponse;
import static com.library.app.commontests.utils.JsonTestUtils.assertJsonMatchesExpectedJson;
import static com.library.app.commontests.utils.JsonTestUtils.assertJsonMatchesFileContent;
import static com.library.app.commontests.utils.JsonTestUtils.readJsonFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.library.app.author.exception.AuthorNotFoundException;
import com.library.app.author.model.Author;
import com.library.app.author.resource.AuthorJsonConverter;
import com.library.app.book.exception.BookNotFoundException;
import com.library.app.book.model.Book;
import com.library.app.book.model.filter.BookFilter;
import com.library.app.book.services.BookServices;
import com.library.app.category.exception.CategoryNotFoundException;
import com.library.app.category.model.Category;
import com.library.app.category.resource.CategoryJsonConverter;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;
import com.library.app.commontests.utils.ResourceDefinitions;

public class BookResourceUTest {
	private BookResource bookResource;

	@Mock
	private BookServices bookServices;

	@Mock
	private UriInfo uriInfo;

	private static final String PATH_RESOURCE = ResourceDefinitions.BOOK.getResourceName();

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);

		bookResource = new BookResource();

		final BookJsonConverter bookJsonConverter = new BookJsonConverter();
		bookJsonConverter.categoryJsonConverter = new CategoryJsonConverter();
		bookJsonConverter.authorJsonConverter = new AuthorJsonConverter();

		bookResource.bookServices = bookServices;
		bookResource.uriInfo = uriInfo;
		bookResource.bookJsonConverter = bookJsonConverter;
	}

	@Test
	public void addValidBook() throws Exception {
		final Book expectedBook = cleanCode();
		expectedBook.setCategory(new Category(1L));
		expectedBook.setAuthors(Arrays.asList(new Author(2L)));
		when(bookServices.add(bookEq(expectedBook))).thenReturn(bookWithId(cleanCode(), 1L));

		final Response response = bookResource.add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "cleanCode.json")));
		assertThat(response.getStatus(), is(equalTo(HTTPCode.CREATED.getCode())));
		assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
	}

	@Test
	public void addBookWithNullTitle() throws Exception {
		addBookWithValidationError(new FieldNotValidException("title", "may not be null"), "cleanCode.json",
				"bookErrorNullTitle.json");
	}

	@Test
	public void addBookWithInexistentCategory() throws Exception {
		addBookWithValidationError(new CategoryNotFoundException(), "cleanCode.json",
				"bookErrorInexistentCategory.json");
	}

	@Test
	public void addBookWithInexistentAuthor() throws Exception {
		addBookWithValidationError(new AuthorNotFoundException(), "cleanCode.json", "bookErrorInexistentAuthor.json");
	}

	@Test
	public void updateValidBook() throws Exception {
		final Response response = bookResource.update(1L,
				readJsonFile(getPathFileRequest(PATH_RESOURCE, "cleanCode.json")));

		assertThat(response.getStatus(), is(equalTo(HTTPCode.OK.getCode())));
		assertThat(response.getEntity().toString(), is(equalTo("")));

		final Book expectedBook = bookWithId(cleanCode(), 1L);
		expectedBook.setCategory(new Category(1L));
		expectedBook.setAuthors(Arrays.asList(new Author(2L)));
		verify(bookServices).update(bookEq(expectedBook));
	}

	@Test
	public void updateBookWithNullTitle() throws Exception {
		updateBookWithError(new FieldNotValidException("title", "may not be null"), HTTPCode.VALIDATION_ERROR,
				"cleanCode.json", "bookErrorNullTitle.json");
	}

	@Test
	public void updateBookWithInexistentCategory() throws Exception {
		updateBookWithError(new CategoryNotFoundException(), HTTPCode.VALIDATION_ERROR, "cleanCode.json",
				"bookErrorInexistentCategory.json");
	}

	@Test
	public void updateBookWithInexistentAuthor() throws Exception {
		updateBookWithError(new AuthorNotFoundException(), HTTPCode.VALIDATION_ERROR, "cleanCode.json",
				"bookErrorInexistentAuthor.json");
	}

	@Test
	public void updateBookNotFound() throws Exception {
		updateBookWithError(new BookNotFoundException(), HTTPCode.NOT_FOUND, "cleanCode.json",
				"bookErrorInexistentAuthor.json");
	}

	@Test
	public void findBook() throws BookNotFoundException {
		final Book book = bookWithId(designPatterns(), 1L);
		book.getCategory().setId(1L);
		for (int i = 1; i <= book.getAuthors().size(); i++) {
			book.getAuthors().get(i - 1).setId(new Long(i));
		}

		when(bookServices.findById(1L)).thenReturn(book);

		final Response response = bookResource.findById(1L);
		assertThat(response.getStatus(), is(equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "designPatternsFound.json");
	}

	@Test
	public void findBookNotFound() throws BookNotFoundException {
		when(bookServices.findById(1L)).thenThrow(new BookNotFoundException());

		final Response response = bookResource.findById(1L);
		assertThat(response.getStatus(), is(equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByBookNoFilter() {
		final List<Book> books = Arrays.asList(bookWithId(cleanCode(), 1L), bookWithId(designPatterns(), 2L));
		Long currentCategoryId = 1L;
		Long currentAuthorId = 1L;
		for (final Book book : books) {
			book.getCategory().setId(currentCategoryId++);
			for (int i = 0; i < book.getAuthors().size(); i++) {
				book.getAuthors().get(i).setId(currentAuthorId++);
			}
		}

		final MultivaluedMap<String, String> multiMap = mock(MultivaluedMap.class);
		when(uriInfo.getQueryParameters()).thenReturn(multiMap);

		when(bookServices.findByFilter((BookFilter) anyObject())).thenReturn(
				new PaginatedData<Book>(books.size(), books));

		final Response response = bookResource.findByFilter();
		assertThat(response.getStatus(), is(equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "booksAllInOnePage.json");
	}

	private void addBookWithValidationError(final Exception exceptionToBeThrown, final String requestFileName,
			final String responseFileName) throws Exception {
		when(bookServices.add((Book) anyObject())).thenThrow(exceptionToBeThrown);

		final Response response = bookResource.add(readJsonFile(getPathFileRequest(PATH_RESOURCE, requestFileName)));
		assertThat(response.getStatus(), is(equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, responseFileName);
	}

	private void updateBookWithError(final Exception exceptionToBeThrown, final HTTPCode expectedHTTPCode,
			final String requestFileName,
			final String responseFileName) throws Exception {
		doThrow(exceptionToBeThrown).when(bookServices).update(bookWithId(cleanCode(), 1L));

		final Response response = bookResource.update(1L,
				readJsonFile(getPathFileRequest(PATH_RESOURCE, requestFileName)));
		assertThat(response.getStatus(), is(equalTo(expectedHTTPCode.getCode())));
		if (expectedHTTPCode != HTTPCode.NOT_FOUND) {
			assertJsonResponseWithFile(response, responseFileName);
		}
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		assertJsonMatchesFileContent(response.getEntity().toString(), getPathFileResponse(PATH_RESOURCE, fileName));
	}
}