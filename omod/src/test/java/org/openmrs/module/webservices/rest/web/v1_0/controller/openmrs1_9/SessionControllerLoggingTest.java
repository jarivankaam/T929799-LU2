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

/**
 * Tests voor de logging in {@link SessionController1_9}.
 * Verifieert dat uitloggen correct wordt gelogd en geen gevoelige data bevat.
 */
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

		// Koppel ListAppender aan de logger van SessionController1_9
		controllerLogger = (Logger) LoggerFactory.getLogger(SessionController1_9.class);
		logAppender = new ListAppender<>();
		logAppender.start();
		controllerLogger.addAppender(logAppender);
	}

	@After
	public void tearDown() {
		controllerLogger.detachAppender(logAppender);
	}

	// =========================================================
	// Tests: succesvolle acties
	// =========================================================

	/**
	 * Verifieert dat uitloggen wordt gelogd op INFO-niveau
	 * met de gebruikersnaam en de [SECURITY] tag.
	 */
	@Test
	public void delete_shouldLogLogoutAtInfoLevel() {
		Assert.assertTrue("Gebruiker moet ingelogd zijn voor de test", Context.isAuthenticated());
		String expectedUsername = Context.getAuthenticatedUser().getUsername();

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;
		boolean foundLogoutLog = logs.stream()
			.anyMatch(e -> e.getLevel() == Level.INFO
				&& e.getFormattedMessage().contains("[SECURITY]")
				&& e.getFormattedMessage().contains("logged out")
				&& e.getFormattedMessage().contains(expectedUsername));

		Assert.assertTrue("Uitloggen moet gelogd worden op INFO-niveau met de gebruikersnaam",
			foundLogoutLog);
	}

	/**
	 * Verifieert dat de gebruiker daadwerkelijk uitgelogd is na delete()
	 * (bestaande functionaliteitstest, gecombineerd met logging-check).
	 */
	@Test
	public void delete_shouldLogoutAndLog() {
		Assert.assertTrue(Context.isAuthenticated());

		controller.delete(request);

		// Gebruiker moet uitgelogd zijn
		Assert.assertFalse("Gebruiker moet uitgelogd zijn na delete()", Context.isAuthenticated());
		Assert.assertNull("Sessie moet ongeldig zijn na delete()", request.getSession(false));

		// En er moet een logregel zijn
		List<ILoggingEvent> logs = logAppender.list;
		boolean hasSecurityLog = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains("[SECURITY]"));

		Assert.assertTrue("Er moet een [SECURITY] logregel zijn na uitloggen", hasSecurityLog);
	}

	// =========================================================
	// Tests: afwezigheid van gevoelige data
	// =========================================================

	/**
	 * Verifieert dat bij uitloggen geen gevoelige sessiedata in de logs verschijnt.
	 * Sessie-ID's mogen niet gelogd worden om session hijacking te voorkomen.
	 */
	@Test
	public void delete_shouldNotLogSessionId() {
		Assert.assertTrue(Context.isAuthenticated());

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;
		boolean sessionIdInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(SESSION_ID));

		Assert.assertFalse("Het sessie-ID mag niet in de logs verschijnen", sessionIdInLogs);
	}

	/**
	 * Verifieert dat de gebruikersnaam wel gelogd wordt maar geen andere
	 * persoonlijk identificeerbare informatie (PII) zoals UUID's van de gebruiker.
	 */
	@Test
	public void delete_shouldLogUsernameButNotUserUuid() {
		Assert.assertTrue(Context.isAuthenticated());
		String userUuid = Context.getAuthenticatedUser().getUuid();
		String username = Context.getAuthenticatedUser().getUsername();

		controller.delete(request);

		List<ILoggingEvent> logs = logAppender.list;

		// Gebruikersnaam mag wel gelogd worden
		boolean usernameLogged = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(username));
		Assert.assertTrue("De gebruikersnaam moet gelogd worden bij uitloggen", usernameLogged);

		// UUID mag niet gelogd worden
		boolean uuidInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(userUuid));
		Assert.assertFalse("De UUID van de gebruiker mag niet in de logs verschijnen", uuidInLogs);
	}
}
