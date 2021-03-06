package com.library.app.user.resource;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.library.app.common.model.filter.PaginationData;
import com.library.app.common.model.filter.PaginationData.OrderMode;
import com.library.app.commontests.utils.FilterExtractorTestUtils;
import com.library.app.user.model.User.UserType;
import com.library.app.user.model.filter.UserFilter;

public class UserFilterExtractorFromUrlUTest {
	private UriInfo uriInfo;

	@Before
	public void initTestCase() {
		uriInfo = Mockito.mock(UriInfo.class);
	}

	@Test
	public void onlyDefaultValues() {
		setUpUriInfo(null, null, null, null, null);

		final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
		final UserFilter userFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
				new PaginationData(0, 10, "name",
						OrderMode.ASCENDING));
		assertFieldsOnFilter(userFilter, null, null);
	}

	@Test
	public void withPaginationAndNameAndUserTypeAndSortAscending() {
		setUpUriInfo("2", "5", "John", UserType.CUSTOMER, "id");

		final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
		final UserFilter userFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.ASCENDING));
		assertFieldsOnFilter(userFilter, "John", UserType.CUSTOMER);
	}

	@Test
	public void withPaginationAndNameAndSortAscendingWithPrefix() {
		setUpUriInfo("2", "5", "John", null, "+id");

		final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
		final UserFilter userFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.ASCENDING));
		assertFieldsOnFilter(userFilter, "John", null);
	}

	@Test
	public void withPaginationAndNameeAndUserTypeAndSortDescending() {
		setUpUriInfo("2", "5", "John", UserType.EMPLOYEE, "-id");

		final UserFilterExtractorFromUrl extractor = new UserFilterExtractorFromUrl(uriInfo);
		final UserFilter userFilter = extractor.getFilter();

		FilterExtractorTestUtils.assertActualPaginationDataWithExpected(userFilter.getPaginationData(),
				new PaginationData(10, 5, "id",
						OrderMode.DESCENDING));
		assertFieldsOnFilter(userFilter, "John", UserType.EMPLOYEE);
	}

	private void setUpUriInfo(final String page, final String perPage, final String name, final UserType type,
			final String sort) {
		final Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("page", page);
		parameters.put("per_page", perPage);
		parameters.put("name", name);
		parameters.put("type", type != null ? type.name() : null);
		parameters.put("sort", sort);

		FilterExtractorTestUtils.setUpUriInfoWithMap(uriInfo, parameters);
	}

	private void assertFieldsOnFilter(final UserFilter userFilter, final String name, final UserType userType) {
		Assert.assertThat(userFilter.getName(), CoreMatchers.is(CoreMatchers.equalTo(name)));
		Assert.assertThat(userFilter.getUserType(), CoreMatchers.is(CoreMatchers.equalTo(userType)));
	}

}