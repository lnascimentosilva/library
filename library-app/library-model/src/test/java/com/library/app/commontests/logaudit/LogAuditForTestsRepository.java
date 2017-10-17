package com.library.app.commontests.logaudit;

import static com.library.app.commontests.user.UserForTestsRepository.*;
import static com.library.app.commontests.utils.TestRepositoryUtils.*;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Ignore;

import com.library.app.common.utils.DateUtils;
import com.library.app.logaudit.model.LogAudit;
import com.library.app.logaudit.model.LogAudit.Action;
import com.library.app.user.model.User;

@Ignore
public class LogAuditForTestsRepository {

	private LogAuditForTestsRepository() {
	}

	public static List<LogAudit> allLogs() {
		final LogAudit logAudit1 = new LogAudit(admin(), Action.ADD, "Category");
		logAudit1.setCreatedAt(DateUtils.getAsDateTime("2015-01-08T19:32:22Z"));

		final LogAudit logAudit2 = new LogAudit(admin(), Action.UPDATE, "Category");
		logAudit2.setCreatedAt(DateUtils.getAsDateTime("2015-01-09T19:32:22Z"));

		final LogAudit logAudit3 = new LogAudit(johnDoe(), Action.ADD, "Order");
		logAudit3.setCreatedAt(DateUtils.getAsDateTime("2015-01-10T19:32:22Z"));

		return Arrays.asList(logAudit1, logAudit2, logAudit3);
	}

	public static LogAudit logAuditWithId(final LogAudit logAudit, final Long id) {
		logAudit.setId(id);
		return logAudit;
	}

	public static LogAudit normalizeDependencies(final LogAudit logAudit, final EntityManager em) {
		logAudit.setUser(findByPropertyNameAndValue(em, User.class, "email", logAudit.getUser().getEmail()));
		return logAudit;
	}

}