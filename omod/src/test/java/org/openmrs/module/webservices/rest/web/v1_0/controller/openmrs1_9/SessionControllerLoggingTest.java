/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SessionControllerLoggingTest extends BaseModuleWebContextSensitiveTest {

	private static final String SESSION_ID = "test-session-logging";

	private SessionController1_9 controller;

	private HttpServletRequest request;

	private ListAppender<ILoggingEvent> logAppender;

	private Logger controllerLogger;

	@Before
	public void setUp() {
		controller = Context.getRegisteredComponents(SessionController1_9.class).iterator().next();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setSession(new MockHttpSession(new MockServletContext(), SESSION_ID));
		request = mockRequest;

		controllerLogger = (Logger) LoggerFactory.getLogger(SessionController1_9.class);
		logAppender = new ListAppender<>();
		logAppender.start();
		controllerLogger.addAppender(logAppender);
	}

	@After
	public void tearDown() {
		controllerLogger.detachAppender(logAppender);
	}

	@Test
	public void delete_shouldLogLogoutAtInfoLevel() {
		Assert.assertTrue("User must be logged in before the test", Context.isAuthenticated());
		String expectedUsername = Context.getAuthenticatedUser().getUsername();

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;
		boolean foundLogoutLog = logs.stream()
			.anyMatch(e -> e.getLevel() == Level.INFO
				&& e.getFormattedMessage().contains("[SECURITY]")
				&& e.getFormattedMessage().contains("logged out")
				&& e.getFormattedMessage().contains(expectedUsername));

		Assert.assertTrue("Logout must be logged at INFO level with the username", foundLogoutLog);
	}

	@Test
	public void delete_shouldLogoutAndLog() {
		Assert.assertTrue(Context.isAuthenticated());

		controller.delete(request);

		Assert.assertFalse("User must be logged out after delete()", Context.isAuthenticated());
		Assert.assertNull("Session must be invalidated after delete()", request.getSession(false));

		List<ILoggingEvent> logs = logAppender.list;
		boolean hasSecurityLog = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains("[SECURITY]"));

		Assert.assertTrue("A [SECURITY] log entry must be present after logout", hasSecurityLog);
	}

	@Test
	public void delete_shouldNotLogSessionId() {
		Assert.assertTrue(Context.isAuthenticated());

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;
		boolean sessionIdInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(SESSION_ID));

		Assert.assertFalse("The session ID must never appear in the logs", sessionIdInLogs);
	}

	@Test
	public void delete_shouldLogUsernameButNotUserUuid() {
		Assert.assertTrue(Context.isAuthenticated());
		String userUuid = Context.getAuthenticatedUser().getUuid();
		String username = Context.getAuthenticatedUser().getUsername();

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;

		boolean usernameLogged = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(username));
		Assert.assertTrue("The username must be logged on logout", usernameLogged);

		boolean uuidInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(userUuid));
		Assert.assertFalse("The user UUID must not appear in the logs", uuidInLogs);
	}
}