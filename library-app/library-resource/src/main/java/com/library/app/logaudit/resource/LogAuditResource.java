package com.library.app.logaudit.resource;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.library.app.common.json.JsonUtils;
import com.library.app.common.json.JsonWriter;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;
import com.library.app.logaudit.model.LogAudit;
import com.library.app.logaudit.model.filter.LogAuditFilter;
import com.library.app.logaudit.repository.LogAuditRepository;

@Path("/logsaudit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "ADMINISTRATOR" })
public class LogAuditResource {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	LogAuditRepository logAuditRepository;

	@Inject
	LogAuditJsonConverter logAuditJsonConverter;

	@Context
	UriInfo uriInfo;

	@GET
	public Response findByFilter() {
		final LogAuditFilter logAuditFilter = new LogAuditFilterExtractorFromUrl(uriInfo).getFilter();
		logger.debug("Finding auditing logs using filter: {}", logAuditFilter);

		final PaginatedData<LogAudit> logs = logAuditRepository.findByFilter(logAuditFilter);

		logger.debug("Found {} logs", logs.getNumberOfRows());

		final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(logs,
				logAuditJsonConverter);
		return Response.status(HTTPCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
				.build();
	}

}