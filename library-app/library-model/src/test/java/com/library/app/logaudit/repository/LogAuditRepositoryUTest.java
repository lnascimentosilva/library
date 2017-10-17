package com.library.app.logaudit.repository;

import static com.library.app.commontests.logaudit.LogAuditForTestsRepository.*;
import static com.library.app.commontests.user.UserForTestsRepository.*;
import static com.library.app.commontests.utils.TestRepositoryUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.library.app.common.model.PaginatedData;
import com.library.app.common.model.filter.PaginationData;
import com.library.app.common.model.filter.PaginationData.OrderMode;
import com.library.app.common.utils.DateUtils;
import com.library.app.commontests.utils.TestBaseRepository;
import com.library.app.logaudit.model.LogAudit;
import com.library.app.logaudit.model.filter.LogAuditFilter;
import com.library.app.user.model.Employee;

public class LogAuditRepositoryUTest extends TestBaseRepository {

	private LogAuditRepository logAuditRepository;

	@Before
	public void initTestCase() {
		initializeTestDB();

		logAuditRepository = new LogAuditRepository();
		logAuditRepository.em = em;

		loadUsers();
		loadForFindByFilter();
	}

	@After
	public void setDownTestCase() {
		closeEntityManager();
	}

	@Test
	public void findByFilterNoFilter() {
		final PaginatedData<LogAudit> logs = logAuditRepository.findByFilter(new LogAuditFilter());
		assertThat(logs.getNumberOfRows(), is(equalTo(3)));
		assertThat(logs.getRows().size(), is(equalTo(3)));
		assertThat(DateUtils.formatDateTime(logs.getRow(0).getCreatedAt()), is(equalTo("2015-01-10T19:32:22Z")));
		assertThat(DateUtils.formatDateTime(logs.getRow(1).getCreatedAt()), is(equalTo("2015-01-09T19:32:22Z")));
		assertThat(DateUtils.formatDateTime(logs.getRow(2).getCreatedAt()), is(equalTo("2015-01-08T19:32:22Z")));
	}

	@Test
	public void findByFilterWithPagination() {
		final LogAuditFilter logAuditFilter = new LogAuditFilter();
		logAuditFilter.setPaginationData(new PaginationData(0, 2, "createdAt", OrderMode.ASCENDING));

		PaginatedData<LogAudit> logs = logAuditRepository.findByFilter(logAuditFilter);
		assertThat(logs.getNumberOfRows(), is(equalTo(3)));
		assertThat(logs.getRows().size(), is(equalTo(2)));
		assertThat(DateUtils.formatDateTime(logs.getRow(0).getCreatedAt()), is(equalTo("2015-01-08T19:32:22Z")));
		assertThat(DateUtils.formatDateTime(logs.getRow(1).getCreatedAt()), is(equalTo("2015-01-09T19:32:22Z")));

		logAuditFilter.setPaginationData(new PaginationData(2, 2, "createdAt", OrderMode.ASCENDING));

		logs = logAuditRepository.findByFilter(logAuditFilter);
		assertThat(logs.getNumberOfRows(), is(equalTo(3)));
		assertThat(logs.getRows().size(), is(equalTo(1)));
		assertThat(DateUtils.formatDateTime(logs.getRow(0).getCreatedAt()), is(equalTo("2015-01-10T19:32:22Z")));
	}

	@Test
	public void findByFilterFinteringByDate() {
		final LogAuditFilter logAuditFilter = new LogAuditFilter();
		logAuditFilter.setStartDate(DateUtils.getAsDateTime("2015-01-09T19:32:22Z"));
		logAuditFilter.setEndDate(DateUtils.getAsDateTime("2015-01-10T19:32:22Z"));
		logAuditFilter.setPaginationData(new PaginationData(0, 2, "createdAt", OrderMode.DESCENDING));

		final PaginatedData<LogAudit> logs = logAuditRepository.findByFilter(logAuditFilter);
		assertThat(logs.getNumberOfRows(), is(equalTo(2)));
		assertThat(logs.getRows().size(), is(equalTo(2)));
		assertThat(DateUtils.formatDateTime(logs.getRow(0).getCreatedAt()), is(equalTo("2015-01-10T19:32:22Z")));
		assertThat(DateUtils.formatDateTime(logs.getRow(1).getCreatedAt()), is(equalTo("2015-01-09T19:32:22Z")));
	}

	@Test
	public void findByFilterFinteringByUser() {
		final LogAuditFilter logAuditFilter = new LogAuditFilter();
		logAuditFilter.setUserId(findByPropertyNameAndValue(em, Employee.class, "email", admin().getEmail()).getId());
		logAuditFilter.setPaginationData(new PaginationData(0, 2, "createdAt", OrderMode.DESCENDING));

		final PaginatedData<LogAudit> logs = logAuditRepository.findByFilter(logAuditFilter);
		assertThat(logs.getNumberOfRows(), is(equalTo(2)));
		assertThat(logs.getRows().size(), is(equalTo(2)));
		assertThat(DateUtils.formatDateTime(logs.getRow(0).getCreatedAt()), is(equalTo("2015-01-09T19:32:22Z")));
		assertThat(DateUtils.formatDateTime(logs.getRow(1).getCreatedAt()), is(equalTo("2015-01-08T19:32:22Z")));

	}

	private void loadForFindByFilter() {
		dbCommandExecutor.executeCommand(() -> {
			allLogs().forEach((logAudit) -> logAuditRepository.add(normalizeDependencies(logAudit, em)));
			return null;
		});
	}

	private void loadUsers() {
		dbCommandExecutor.executeCommand(() -> {
			allUsers().forEach(em::persist);
			return null;
		});
	}

}
