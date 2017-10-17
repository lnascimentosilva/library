package com.library.app.logaudit.resource;

import static com.library.app.commontests.logaudit.LogAuditForTestsRepository.allLogs;
import static com.library.app.commontests.logaudit.LogAuditForTestsRepository.logAuditWithId;
import static com.library.app.commontests.utils.FileTestNameUtils.getPathFileResponse;
import static com.library.app.commontests.utils.JsonTestUtils.assertJsonMatchesFileContent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;
import com.library.app.commontests.utils.ResourceDefinitions;
import com.library.app.logaudit.model.LogAudit;
import com.library.app.logaudit.model.filter.LogAuditFilter;
import com.library.app.logaudit.repository.LogAuditRepository;

public class LogAuditResourceUTest {
	private LogAuditResource logAuditResource;

	@Mock
	private LogAuditRepository logAuditRepository;

	@Mock
	private UriInfo uriInfo;

	private static final String PATH_RESOURCE = ResourceDefinitions.LOGAUDIT.getResourceName();

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);

		logAuditResource = new LogAuditResource();

		logAuditResource.logAuditRepository = logAuditRepository;
		logAuditResource.uriInfo = uriInfo;
		logAuditResource.logAuditJsonConverter = new LogAuditJsonConverter();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFilter() {
		final MultivaluedMap<String, String> multiMap = mock(MultivaluedMap.class);
		when(uriInfo.getQueryParameters()).thenReturn(multiMap);

		when(logAuditRepository.findByFilter((LogAuditFilter) anyObject())).thenReturn(
				new PaginatedData<LogAudit>(3, getLogs()));

		final Response response = logAuditResource.findByFilter();
		assertThat(response.getStatus(), is(equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "allLogs.json");
	}

	private List<LogAudit> getLogs() {
		final List<LogAudit> logs = allLogs();

		logAuditWithId(logs.get(0), 1L);
		logs.get(0).getUser().setId(1L);

		logAuditWithId(logs.get(1), 2L);
		logs.get(1).getUser().setId(1L);

		logAuditWithId(logs.get(2), 3L);
		logs.get(2).getUser().setId(2L);

		return logs;
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		assertJsonMatchesFileContent(response.getEntity().toString(), getPathFileResponse(PATH_RESOURCE, fileName));
	}

}