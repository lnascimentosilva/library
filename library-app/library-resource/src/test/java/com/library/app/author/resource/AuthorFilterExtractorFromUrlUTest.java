package com.library.app.author.resource;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.library.app.author.model.filter.AuthorFilter;
import com.library.app.common.model.filter.PaginationData;
import com.library.app.common.model.filter.PaginationData.OrderMode;
import com.library.app.commontests.utils.FilterExtractorTestUtils;

public class AuthorFilterExtractorFromUrlUTest {

	@Mock
	private UriInfo uriInfo;

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onlyDefaultValues() {
		setUpUriInfo(null, null, null, null);

		final AuthorFilterExtractorFromUrl extractor = new AuthorFilterExtractorFromUrl(uriInfo);
		final AuthorFilter authorFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(authorFilter.getPaginationData(),
				new PaginationData(0, 10, "name",
						OrderMode.ASCENDING));
		Assert.assertThat(authorFilter.getName(), CoreMatchers.is(CoreMatchers.nullValue()));
	}

	@Test
	public void withPaginationAndNameAndSortAscending() {
		setUpUriInfo("2", "5", "Robert", "id");

		final AuthorFilterExtractorFromUrl extractor = new AuthorFilterExtractorFromUrl(uriInfo);
		final AuthorFilter authorFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(authorFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.ASCENDING));
		Assert.assertThat(authorFilter.getName(), CoreMatchers.is(CoreMatchers.equalTo("Robert")));
	}

	@Test
	public void withPaginationAndNameAndSortAscendingWithPrefix() {
		setUpUriInfo("2", "5", "Robert", "+id");

		final AuthorFilterExtractorFromUrl extractor = new AuthorFilterExtractorFromUrl(uriInfo);
		final AuthorFilter authorFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(authorFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.ASCENDING));
		Assert.assertThat(authorFilter.getName(), CoreMatchers.is(CoreMatchers.equalTo("Robert")));
	}

	@Test
	public void withPaginationAndNameAndSortDescending() {
		setUpUriInfo("2", "5", "Robert", "-id");

		final AuthorFilterExtractorFromUrl extractor = new AuthorFilterExtractorFromUrl(uriInfo);
		final AuthorFilter authorFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(authorFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.DESCENDING));
		Assert.assertThat(authorFilter.getName(), CoreMatchers.is(CoreMatchers.equalTo("Robert")));
	}

	private void setUpUriInfo(final String page, final String perPage, final String name, final String sort) {
		final Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("page", page);
		parameters.put("per_page", perPage);
		parameters.put("name", name);
		parameters.put("sort", sort);

		FilterExtractorTestUtils.setUpUriInfoWithMap(uriInfo, parameters);
	}

}
