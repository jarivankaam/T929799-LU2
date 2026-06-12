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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Log log = LogFactory.getLog(getClass());

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public SimpleObject handleAllUnhandledExceptions(Exception ex, HttpServletResponse response) {
        // 1. Zorg dat de administrator de fout nog altijd in de Docker/Tomcat logs ziet
        log.error("GLOBAL APP CRASH DETECTED: " + ex.getMessage(), ex);

        // 2. Zet de HTTP status dwingend op 500
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // 3. Bouw handmatig de schone JSON response op (GEEN RestUtil gebruiken!)
        SimpleObject cleanErrorResponse = new SimpleObject();
        SimpleObject errorDetails = new SimpleObject();

        errorDetails.put("message", "Internal Server Error");
        errorDetails.put("code", "500");
        errorDetails.put("detail", "An unexpected error occurred. Please contact your system administrator.");

        cleanErrorResponse.put("error", errorDetails);

        return cleanErrorResponse;
    }
}