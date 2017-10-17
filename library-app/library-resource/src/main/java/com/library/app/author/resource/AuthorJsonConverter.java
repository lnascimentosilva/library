package com.library.app.author.resource;

import javax.enterprise.context.ApplicationScoped;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.library.app.author.model.Author;
import com.library.app.common.json.EntityJsonConverter;
import com.library.app.common.json.JsonReader;
import com.library.app.common.json.JsonUtils;

@ApplicationScoped
public class AuthorJsonConverter implements EntityJsonConverter<Author> {

	@Override
	public Author convertFrom(final String json) {
		final JsonObject jsonObject = JsonReader.readAsJsonObject(json);

		final Author author = new Author();
		author.setName(JsonReader.getStringOrNull(jsonObject, "name"));

		return author;
	}

	@Override
	public JsonElement convertToJsonElement(final Author author) {
		final JsonObject jsonObject = (JsonObject) JsonUtils.getJsonElementWithId(author.getId());

		jsonObject.addProperty("name", author.getName());

		return jsonObject;
	}

}