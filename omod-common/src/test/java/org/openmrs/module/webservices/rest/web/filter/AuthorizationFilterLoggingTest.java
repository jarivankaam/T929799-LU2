/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

/**
 * Tests voor de logging in {@link AuthorizationFilter}.
 * Verifieert dat beveiligingsrelevante gebeurtenissen correct worden gelogd
 * en dat geen gevoelige data (wachtwoorden) in de logs verschijnt.
 *
 * Extends BaseModuleWebContextSensitiveTest zodat de OpenMRS context
 * beschikbaar is voor RestUtil.isIpAllowed().
 */
public class AuthorizationFilterLoggingTest extends BaseModuleWebContextSensitiveTest {

	private AuthorizationFilter filter;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockFilterChain chain;

	private ListAppender<ILoggingEvent> logAppender;

	private Logger filterLogger;

	@Before
	public void setUp() {
		filter = new AuthorizationFilter();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();

		// Koppel een ListAppender aan de logger van AuthorizationFilter
		filterLogger = (Logger) LoggerFactory.getLogger(AuthorizationFilter.class);
		logAppender = new ListAppender<>();
		logAppender.start();
		filterLogger.addAppender(logAppender);
	}

	@After
	public void tearDown() {
		filterLogger.detachAppender(logAppender);
	}

	// =========================================================
	// Helper methode
	// =========================================================

	private String encodeCredentials(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(Charset.forName("UTF-8")));
	}

	// =========================================================
	// Tests: succesvolle acties
	// =========================================================

	/**
	 * Verifieert dat een geslaagde inlogpoging wordt gelogd op INFO-niveau
	 * met de gebruikersnaam en de [SECURITY] tag.
	 */
	@Test
	public void doFilter_shouldLogSuccessfulLoginAtInfoLevel() throws Exception {
		request.addHeader("Authorization", encodeCredentials("admin", "Admin1234"));
		request.setRemoteAddr("127.0.0.1");

		filter.doFilter(request, response, chain);

		List<ILoggingEvent> logs = logAppender.list;
		boolean foundLog = logs.stream()
			.anyMatch(e -> e.getLevel() == Level.INFO
				&& e.getFormattedMessage().contains("[SECURITY]")
				&& e.getFormattedMessage().contains("Successful login")
				&& e.getFormattedMessage().contains("admin"));

		Assert.assertTrue("Geslaagde login moet gelogd worden op INFO-niveau met [SECURITY] tag", foundLog);
	}

	// =========================================================
	// Tests: mislukte acties
	// =========================================================

	/**
	 * Verifieert dat een mislukte inlogpoging wordt gelogd op WARN-niveau
	 * met de gebruikersnaam maar ZONDER het wachtwoord.
	 */
	@Test
	public void doFilter_shouldLogFailedLoginAtWarnLevel() throws Exception {
		request.addHeader("Authorization", encodeCredentials("admin", "foutWachtwoord"));
		request.setRemoteAddr("127.0.0.1");

		filter.doFilter(request, response, chain);

		List<ILoggingEvent> logs = logAppender.list;
		boolean foundWarnLog = logs.stream()
			.anyMatch(e -> e.getLevel() == Level.WARN
				&& e.getFormattedMessage().contains("[SECURITY]")
				&& e.getFormattedMessage().contains("Failed login")
				&& e.getFormattedMessage().contains("admin"));

		Assert.assertTrue("Een mislukte inlogpoging moet gelogd worden op WARN-niveau", foundWarnLog);
	}

	// =========================================================
	// Tests: afwezigheid van gevoelige data
	// =========================================================

	/**
	 * Verifieert dat het wachtwoord NIET verschijnt in de logs bij een mislukte login.
	 * Dit is cruciaal voor NEN-7510 A.8.15: gevoelige data mag niet worden gelogd.
	 */
	@Test
	public void doFilter_shouldNotLogPasswordOnFailedLogin() throws Exception {
		String geheimWachtwoord = "SuperGeheimWachtwoord123!";
		request.addHeader("Authorization", encodeCredentials("admin", geheimWachtwoord));
		request.setRemoteAddr("127.0.0.1");

		filter.doFilter(request, response, chain);

		List<ILoggingEvent> logs = logAppender.list;
		boolean passwordInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(geheimWachtwoord));

		Assert.assertFalse("Het wachtwoord mag NOOIT in de logs verschijnen", passwordInLogs);
	}

	/**
	 * Verifieert dat het wachtwoord NIET verschijnt in de logs bij een geslaagde login.
	 */
	@Test
	public void doFilter_shouldNotLogPasswordOnSuccessfulLogin() throws Exception {
		String wachtwoord = "Admin1234";
		request.addHeader("Authorization", encodeCredentials("admin", wachtwoord));
		request.setRemoteAddr("127.0.0.1");

		filter.doFilter(request, response, chain);

		List<ILoggingEvent> logs = logAppender.list;
		boolean passwordInLogs = logs.stream()
			.anyMatch(e -> e.getFormattedMessage().contains(wachtwoord));

		Assert.assertFalse("Het wachtwoord mag NOOIT in de logs verschijnen", passwordInLogs);
	}

	/**
	 * Verifieert dat de [SECURITY] tag aanwezig is in alle beveiligingsrelevante logregels.
	 */
	@Test
	public void doFilter_shouldIncludeSecurityTagInAllSecurityLogs() throws Exception {
		request.addHeader("Authorization", encodeCredentials("admin", "foutWachtwoord"));
		request.setRemoteAddr("127.0.0.1");

		filter.doFilter(request, response, chain);

		List<ILoggingEvent> logs = logAppender.list;
		boolean allSecurityLogsTagged = logs.stream()
			.filter(e -> e.getLevel() == Level.WARN || e.getLevel() == Level.INFO)
			.filter(e -> e.getFormattedMessage().contains("login")
				|| e.getFormattedMessage().contains("Login")
				|| e.getFormattedMessage().contains("IP")
				|| e.getFormattedMessage().contains("Session"))
			.allMatch(e -> e.getFormattedMessage().contains("[SECURITY]"));

		Assert.assertTrue("Alle security-logregels moeten de [SECURITY] tag bevatten",
			allSecurityLogsTagged);
	}
}