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