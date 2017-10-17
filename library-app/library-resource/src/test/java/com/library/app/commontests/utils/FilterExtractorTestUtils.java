package com.library.app.commontests.utils;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.mockito.Mockito;

import com.library.app.common.model.filter.PaginationData;

@Ignore
public final class FilterExtractorTestUtils {

	private FilterExtractorTestUtils() {
	}

	public static void assertActualPaginationDataWithExpected(final PaginationData actual,
			final PaginationData expected) {
		Assert.assertThat(actual.getFirstResult(), CoreMatchers.is(CoreMatchers.equalTo(expected.getFirstResult())));
		Assert.assertThat(actual.getMaxResults(), CoreMatchers.is(CoreMatchers.equalTo(expected.getMaxResults())));
		Assert.assertThat(actual.getOrderField(), CoreMatchers.is(CoreMatchers.equalTo(expected.getOrderField())));
		Assert.assertThat(actual.getOrderMode(), CoreMatchers.is(CoreMatchers.equalTo(expected.getOrderMode())));
	}

	@SuppressWarnings("unchecked")
	public static void setUpUriInfoWithMap(final UriInfo uriInfo, final Map<String, String> parameters) {
		final MultivaluedMap<String, String> multiMap = Mockito.mock(MultivaluedMap.class);

		for (final Entry<String, String> keyValue : parameters.entrySet()) {
			Mockito.when(multiMap.getFirst(keyValue.getKey())).thenReturn(keyValue.getValue());
		}

		Mockito.when(uriInfo.getQueryParameters()).thenReturn(multiMap);
	}

}