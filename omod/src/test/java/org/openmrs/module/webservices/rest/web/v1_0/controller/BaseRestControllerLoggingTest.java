/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

public class BaseRestControllerLoggingTest extends BaseModuleWebContextSensitiveTest {

    private BaseRestController controller;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        controller = new BaseRestController();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("192.168.1.100");
        request.setRequestURI("/openmrs/ws/rest/v1/patient");
    }

    @Test
    public void apiAuthenticationExceptionHandler_shouldReturn403WhenAuthenticated() throws Exception {
        APIAuthenticationException ex = new APIAuthenticationException("Test exception");

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        Assert.assertEquals("Response must be 403 for an authenticated user",
                HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    public void apiAuthenticationExceptionHandler_shouldReturn401WithCorrectMessage() throws Exception {
        Context.logout();
        APIAuthenticationException ex = new APIAuthenticationException("User is not logged in");

        Object result = controller.apiAuthenticationExceptionHandler(ex, request, response);

        Assert.assertEquals("Status must be 401",
                HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        Assert.assertNotNull("Response body must not be null", result);

        String resultStr = result.toString();
        Assert.assertFalse("Response must not contain a Java stack trace",
                resultStr.contains("at org.openmrs"));
    }

    @Test
    public void apiAuthenticationExceptionHandler_shouldHaveAccessToClientIp() throws Exception {
        Context.logout();
        APIAuthenticationException ex = new APIAuthenticationException("Unauthorized");

        Assert.assertEquals("Request must contain the client IP address",
                "192.168.1.100", request.getRemoteAddr());

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        String resultStr = response.getContentAsString();
        Assert.assertFalse("The client IP address must not appear in the HTTP response",
                resultStr.contains("192.168.1.100"));
    }

    @Test
    public void apiAuthenticationExceptionHandler_shouldNotLeakInternalDetailsToClient() throws Exception {
        Context.logout();
        String secretInternalMessage = "database_password=SuperSecret123";
        APIAuthenticationException ex = new APIAuthenticationException(secretInternalMessage);

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        String responseBody = response.getContentAsString();
        Assert.assertFalse("Internal details must not appear in the HTTP response",
                responseBody.contains(secretInternalMessage));
    }

    @Test
    public void apiAuthenticationExceptionHandler_shouldHaveAccessToRequestUri() throws Exception {
        APIAuthenticationException ex = new APIAuthenticationException("Unauthorized");

        Assert.assertEquals("Request URI must be available for logging",
                "/openmrs/ws/rest/v1/patient", request.getRequestURI());

        Object result = controller.apiAuthenticationExceptionHandler(ex, request, response);
        Assert.assertNotNull(result);
    }
}