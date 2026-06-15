/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.controller;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;

public class SettingsFormControllerTest extends RestControllerTestUtils {

    @Test
    public void showForm_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Context.logout();
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/settings.form");
        MockHttpServletResponse response = handle(request);
        
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    public void searchProperties_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Context.logout();
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/settings.form/search");
        request.addParameter("prefix", "test");
        
        MockHttpServletResponse response = handle(request);
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
}