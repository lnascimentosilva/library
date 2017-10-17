package com.library.app.logaudit.interceptor;

import java.security.Principal;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.app.logaudit.model.LogAudit;
import com.library.app.logaudit.repository.LogAuditRepository;
import com.library.app.user.exception.UserNotFoundException;
import com.library.app.user.services.UserServices;

public class LogAuditInterceptor {

	@Inject
	private LogAuditRepository logAuditRepository;

	@Inject
	private UserServices userServices;

	@Inject
	private Principal principal;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@AroundInvoke
	public Object intercept(final InvocationContext invocationContext) throws Exception {
		logger.debug("Interceptor being executed..");

		final Object toReturn = invocationContext.proceed();

		try {
			processAuditableAnnotation(invocationContext);
		} catch (final UserNotFoundException e) {
			logger.info("No user found for " + principal.getName());
		}

		return toReturn;
	}

	private void processAuditableAnnotation(final InvocationContext invocationContext) throws UserNotFoundException {
		final Auditable auditable = invocationContext.getMethod().getAnnotation(Auditable.class);
		if (auditable != null) {
			final String elementName = invocationContext.getParameters()[0].getClass().getSimpleName();
			final LogAudit logAudit = new LogAudit(userServices.findByEmail(principal.getName()), auditable.action(),
					elementName);
			logger.debug("Creating log audit {}", logAudit);
			logAuditRepository.add(logAudit);
		}
	}

}