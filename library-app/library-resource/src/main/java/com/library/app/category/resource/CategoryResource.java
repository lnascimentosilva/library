package com.library.app.category.resource;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.library.app.category.exception.CategoryExistentException;
import com.library.app.category.exception.CategoryNotFoundException;
import com.library.app.category.model.Category;
import com.library.app.category.services.CategoryServices;
import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.json.JsonUtils;
import com.library.app.common.json.JsonWriter;
import com.library.app.common.json.OperationResultJsonWriter;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.OperationResult;
import com.library.app.common.model.PaginatedData;
import com.library.app.common.model.ResourceMessage;
import com.library.app.common.model.StandardsOperationResults;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "EMPLOYEE" })
public class CategoryResource {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final ResourceMessage RESOURCE_MESSAGE = new ResourceMessage("category");

	@Inject
	CategoryServices categoryServices;

	@Inject
	CategoryJsonConverter categoryJsonConverter;

	@POST
	public Response add(final String body) {
		logger.debug("Adding a new category with body {}", body);
		Category category = categoryJsonConverter.convertFrom(body);

		HTTPCode httpCode = HTTPCode.CREATED;
		OperationResult result = null;
		try {
			category = categoryServices.add(category);
			result = OperationResult.success(JsonUtils.getJsonElementWithId(category.getId()));
		} catch (final FieldNotValidException e) {
			logger.error("The field \"{}\" of the category is not valid", e.getFieldName(), e);
			httpCode = HTTPCode.VALIDATION_ERROR;
			result = StandardsOperationResults.getOperationResultInvalidField(RESOURCE_MESSAGE, e);
		} catch (final CategoryExistentException e) {
			logger.error("There's already a category for the given name \"{}\"", category.getName(), e);
			httpCode = HTTPCode.VALIDATION_ERROR;
			result = StandardsOperationResults.getOperationResultExistent(RESOURCE_MESSAGE, "name");
		}

		logger.debug("Returning the operation result after adding category: {}", result);
		return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
	}

	@PUT
	@Path("/{id}")
	public Response update(@PathParam("id") final Long id, final String body) {
		logger.debug("Updating the category {} with body {}", id, body);
		final Category category = categoryJsonConverter.convertFrom(body);
		category.setId(id);

		HTTPCode httpCode = HTTPCode.OK;
		OperationResult result;
		try {
			categoryServices.update(category);
			result = OperationResult.success();
		} catch (final FieldNotValidException e) {
			logger.error("The field \"{}\" of the category is not valid", e.getFieldName(), e);
			httpCode = HTTPCode.VALIDATION_ERROR;
			result = StandardsOperationResults.getOperationResultInvalidField(RESOURCE_MESSAGE, e);
		} catch (final CategoryExistentException e) {
			logger.error("There's already a category for the given name \"{}\"", category.getName(), e);
			httpCode = HTTPCode.VALIDATION_ERROR;
			result = StandardsOperationResults.getOperationResultExistent(RESOURCE_MESSAGE, "name");
		} catch (final CategoryNotFoundException e) {
			logger.error("No category found for the given id \"{}\"", id, e);
			httpCode = HTTPCode.NOT_FOUND;
			result = StandardsOperationResults.getOperationResultNotFound(RESOURCE_MESSAGE);
		}

		logger.debug("Returning the operation result after updating category: {}", result);
		return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
	}

	@GET
	@Path("/{id}")
	public Response findById(@PathParam("id") final Long id) {
		logger.debug("Find category: {}", id);
		ResponseBuilder responseBuilder;
		try {
			final Category category = categoryServices.findById(id);
			final OperationResult result = OperationResult
					.success(categoryJsonConverter.convertToJsonElement(category));
			responseBuilder = Response.status(HTTPCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
			logger.debug("Category found: {}", category);
		} catch (final CategoryNotFoundException e) {
			logger.error("No category found for the given id \"{}\"", id, e);
			responseBuilder = Response.status(HTTPCode.NOT_FOUND.getCode());
		}

		return responseBuilder.build();
	}

	@GET
	@PermitAll
	public Response findAll() {
		logger.debug("Find all categories");

		final List<Category> categories = categoryServices.findAll();

		logger.debug("Found {} categories", categories.size());

		final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
				new PaginatedData<Category>(categories.size(), categories), categoryJsonConverter);

		return Response.status(HTTPCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
				.build();
	}

}
