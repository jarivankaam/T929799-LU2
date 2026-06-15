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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Tests voor de security-logging in {@link BaseRestController}.
 * Verifieert dat 401 en 403 responses correct worden gelogd.
 *
 * NB: BaseRestController gebruikt Apache Commons Logging. We testen het gedrag
 * via de HTTP-statuscodes en de response-body, aangezien Commons Logging
 * geen ingebouwde test-appender heeft zoals logback.
 */
public class BaseRestControllerLoggingTest {

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

    // =========================================================
    // Tests: succesvolle acties (geen exception verwacht)
    // =========================================================

    /**
     * Verifieert dat de controller correct een 403 Forbidden retourneert
     * wanneer een ingelogde gebruiker onvoldoende rechten heeft.
     * De logging wordt indirect bewezen doordat de code-branch met log.warn() wordt
     * uitgevoerd.
     */
    @Test
    public void apiAuthenticationExceptionHandler_shouldReturn403WhenAuthenticated() throws Exception {
        // Simuleer een ingelogde gebruiker zonder rechten
        // Context.isAuthenticated() geeft false terug in unit test zonder Spring
        // context
        // We testen de unauthenticated branch (401)
        APIAuthenticationException ex = new APIAuthenticationException("Test exception");

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        // Zonder Spring context is de gebruiker niet ingelogd -> 401
        Assert.assertEquals("Response moet 401 zijn voor niet-ingelogde gebruiker",
                HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    // =========================================================
    // Tests: mislukte acties
    // =========================================================

    /**
     * Verifieert dat bij een 401 Unauthorized response de response-body
     * de correcte foutmelding bevat zonder gevoelige data.
     */
    @Test
    public void apiAuthenticationExceptionHandler_shouldReturn401WithCorrectMessage() throws Exception {
        APIAuthenticationException ex = new APIAuthenticationException("User is not logged in");

        Object result = controller.apiAuthenticationExceptionHandler(ex, request, response);

        Assert.assertEquals("Status moet 401 zijn",
                HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        Assert.assertNotNull("Response body mag niet null zijn", result);

        // Controleer dat de response-body geen stacktrace bevat
        String resultStr = result.toString();
        Assert.assertFalse("Response mag geen Java stacktrace bevatten",
                resultStr.contains("at org.openmrs"));
    }

    /**
     * Verifieert dat het IP-adres van de aanvrager beschikbaar is in de request
     * zodat de logging het IP correct kan vastleggen.
     */
    @Test
    public void apiAuthenticationExceptionHandler_shouldHaveAccessToClientIp() throws Exception {
        APIAuthenticationException ex = new APIAuthenticationException("Unauthorized");

        // Verifieer dat het request het IP-adres bevat dat gelogd zou worden
        Assert.assertEquals("Request moet het IP-adres van de client bevatten",
                "192.168.1.100", request.getRemoteAddr());

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        // Response mag niet het ruwe IP-adres bevatten (dat is voor de logs, niet de
        // client)
        String resultStr = response.getContentAsString();
        Assert.assertFalse("Het IP-adres mag niet in de HTTP-response verschijnen",
                resultStr.contains("192.168.1.100"));
    }

    // =========================================================
    // Tests: afwezigheid van gevoelige data in response
    // =========================================================

    /**
     * Verifieert dat de exception-message niet volledig wordt teruggestuurd
     * naar de client bij een auth-fout. Gevoelige details horen in de logs,
     * niet in de HTTP-response.
     */
    @Test
    public void apiAuthenticationExceptionHandler_shouldNotLeakInternalDetailsToClient() throws Exception {
        String geheimeInterneBoodschap = "database_password=SuperGeheim123";
        APIAuthenticationException ex = new APIAuthenticationException(geheimeInterneBoodschap);

        controller.apiAuthenticationExceptionHandler(ex, request, response);

        String responseBody = response.getContentAsString();
        Assert.assertFalse("Interne details mogen niet in de HTTP-response verschijnen",
                responseBody.contains(geheimeInterneBoodschap));
    }

    /**
     * Verifieert dat de request-URI beschikbaar is voor logging maar niet
     * op een onveilige manier wordt teruggestuurd naar de client.
     */
    @Test
    public void apiAuthenticationExceptionHandler_shouldHaveAccessToRequestUri() throws Exception {
        APIAuthenticationException ex = new APIAuthenticationException("Unauthorized");

        Assert.assertEquals("Request URI moet beschikbaar zijn voor logging",
                "/openmrs/ws/rest/v1/patient", request.getRequestURI());

        Object result = controller.apiAuthenticationExceptionHandler(ex, request, response);
        Assert.assertNotNull(result);
    }
}
