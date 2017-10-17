package com.library.app.commontests.utils;

import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.library.app.common.json.JsonReader;
import com.library.app.common.model.HTTPCode;

@Ignore
public class IntTestUtils {

	public static Long addElementWithFileAndGetId(final ResourceClient resourceClient, final String pathResource,
			final String mainFolder, final String fileName) {
		final Response response = resourceClient.resourcePath(pathResource).postWithFile(
				FileTestNameUtils.getPathFileRequest(mainFolder, fileName));
		return assertResponseIsCreatedAndGetId(response);
	}

	public static Long addElementWithContentAndGetId(final ResourceClient resourceClient, final String pathResource,
			final String content) {
		final Response response = resourceClient.resourcePath(pathResource).postWithContent(content);
		return assertResponseIsCreatedAndGetId(response);
	}

	public static String findById(final ResourceClient resourceClient, final String pathResource, final Long id) {
		final Response response = resourceClient.resourcePath(pathResource + "/" + id).get();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		return response.readEntity(String.class);
	}

	private static Long assertResponseIsCreatedAndGetId(final Response response) {
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.CREATED.getCode())));
		final Long id = JsonTestUtils.getIdFromJson(response.readEntity(String.class));
		Assert.assertThat(id, CoreMatchers.is(CoreMatchers.notNullValue()));
		return id;
	}

	public static JsonArray assertJsonHasTheNumberOfElementsAndReturnTheEntries(final Response response,
			final int expectedTotalRecords, final int expectedEntriesForThisPage) {
		final JsonObject result = JsonReader.readAsJsonObject(response.readEntity(String.class));

		final int totalRecords = result.getAsJsonObject("paging").get("totalRecords").getAsInt();
		Assert.assertThat(totalRecords, CoreMatchers.is(CoreMatchers.equalTo(expectedTotalRecords)));

		final JsonArray entries = result.getAsJsonArray("entries");
		Assert.assertThat(entries.size(), CoreMatchers.is(CoreMatchers.equalTo(expectedEntriesForThisPage)));

		return entries;
	}

}