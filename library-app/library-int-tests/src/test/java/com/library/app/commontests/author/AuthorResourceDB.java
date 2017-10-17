package com.library.app.commontests.author;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.library.app.author.model.Author;
import com.library.app.author.model.filter.AuthorFilter;
import com.library.app.author.resource.AuthorJsonConverter;
import com.library.app.author.services.AuthorServices;
import com.library.app.common.json.JsonWriter;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;

@Path("/DB/authors")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorResourceDB {

	@Inject
	private AuthorServices authorServices;

	@Inject
	private AuthorJsonConverter authorJsonConverter;

	@POST
	public void addAll() {
		AuthorForTestsRepository.allAuthors().forEach(authorServices::add);
	}

	@GET
	@Path("/{name}")
	public Response findByName(@PathParam("name") final String name) {
		final AuthorFilter authorFilter = new AuthorFilter();
		authorFilter.setName(name);

		final PaginatedData<Author> authors = authorServices.findByFilter(authorFilter);
		if (authors.getNumberOfRows() > 0) {
			final Author author = authors.getRow(0);
			final String authorAsJson = JsonWriter.writeToString(authorJsonConverter.convertToJsonElement(author));
			return Response.status(HTTPCode.OK.getCode()).entity(authorAsJson).build();
		}
		return Response.status(HTTPCode.NOT_FOUND.getCode()).build();
	}

}
