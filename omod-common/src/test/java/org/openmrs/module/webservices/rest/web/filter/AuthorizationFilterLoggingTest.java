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
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterLoggingTest {

    private AuthorizationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger filterLogger;

    private MockedStatic<RestUtil> mockedRestUtil;
    private MockedStatic<Context> mockedContext;

    @Before
    public void setUp() {
        filter = new AuthorizationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();

        mockedRestUtil = Mockito.mockStatic(RestUtil.class);
        mockedRestUtil.when(() -> RestUtil.isIpAllowed(Mockito.anyString())).thenReturn(true);
        mockedContext = Mockito.mockStatic(Context.class);
        mockedContext.when(Context::isAuthenticated).thenReturn(false);
        mockedContext.when(() -> Context.authenticate("admin", "Admin123")).thenAnswer(inv -> null);
        mockedContext.when(() -> Context.authenticate(Mockito.eq("admin"), Mockito.argThat(p -> !p.equals("Admin123"))))
                .thenThrow(new org.openmrs.api.context.ContextAuthenticationException("Bad credentials"));

        filterLogger = (Logger) LoggerFactory.getLogger(AuthorizationFilter.class);
        filterLogger.setLevel(Level.DEBUG);
        logAppender = new ListAppender<>();
        logAppender.start();
        filterLogger.addAppender(logAppender);
    }

    @After
    public void tearDown() {
        filterLogger.detachAppender(logAppender);
        mockedRestUtil.close();
        mockedContext.close();
    }

    private String encodeCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    @Test
    public void doFilter_shouldLogSuccessfulLoginAtInfoLevel() throws Exception {
        request.addHeader("Authorization", encodeCredentials("admin", "Admin123"));
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, chain);

        List<ILoggingEvent> logs = logAppender.list;
        boolean foundLog = logs.stream()
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("[SECURITY]")
                        && e.getFormattedMessage().contains("Successful login")
                        && e.getFormattedMessage().contains("admin"));

        Assert.assertTrue("Successful login must be logged at INFO level with [SECURITY] tag", foundLog);
    }

    @Test
    public void doFilter_shouldLogFailedLoginAtWarnLevel() throws Exception {
        request.addHeader("Authorization", encodeCredentials("admin", "wrongPassword"));
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, chain);

        List<ILoggingEvent> logs = logAppender.list;
        boolean foundWarnLog = logs.stream()
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("[SECURITY]")
                        && e.getFormattedMessage().contains("Failed login")
                        && e.getFormattedMessage().contains("admin"));

        Assert.assertTrue("A failed login attempt must be logged at WARN level", foundWarnLog);
    }

    @Test
    public void doFilter_shouldNotLogPasswordOnFailedLogin() throws Exception {
        String secretPassword = "SuperSecretPassword123!";
        request.addHeader("Authorization", encodeCredentials("admin", secretPassword));
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, chain);

        boolean passwordInLogs = logAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains(secretPassword));

        Assert.assertFalse("The password must NEVER appear in the logs", passwordInLogs);
    }

    @Test
    public void doFilter_shouldNotLogPasswordOnSuccessfulLogin() throws Exception {
        String password = "Admin123";
        request.addHeader("Authorization", encodeCredentials("admin", password));
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, chain);

        boolean passwordInLogs = logAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains(password));

        Assert.assertFalse("The password must NEVER appear in the logs", passwordInLogs);
    }

    @Test
    public void doFilter_shouldIncludeSecurityTagInAllSecurityLogs() throws Exception {
        request.addHeader("Authorization", encodeCredentials("admin", "wrongPassword"));
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, chain);

        boolean allTagged = logAppender.list.stream()
                .filter(e -> e.getLevel() == Level.WARN || e.getLevel() == Level.INFO)
                .filter(e -> e.getFormattedMessage().contains("login")
                        || e.getFormattedMessage().contains("Login")
                        || e.getFormattedMessage().contains("IP")
                        || e.getFormattedMessage().contains("Session"))
                .allMatch(e -> e.getFormattedMessage().contains("[SECURITY]"));

        Assert.assertTrue("All security log entries must contain the [SECURITY] tag", allTagged);
    }
}