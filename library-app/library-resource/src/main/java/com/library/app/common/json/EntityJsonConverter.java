package com.library.app.common.json;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public interface EntityJsonConverter<T> {

	public T convertFrom(final String json);

	public JsonElement convertToJsonElement(final T entity);

	public default JsonElement convertToJsonElement(final List<T> entities) {
		final JsonArray jsonArray = new JsonArray();

		entities.forEach((e) -> {
			jsonArray.add(convertToJsonElement(e));
		});

		return jsonArray;
	}

}
