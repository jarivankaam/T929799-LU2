package org.openmrs.module.webservices.rest.web.controller;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;

public class SwaggerDocControllerTest extends RestControllerTestUtils {

    @Test
    public void debug_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Context.logout();
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/apiDocs/debug");
        request.addParameter("tag", "test");
        
        MockHttpServletResponse response = handle(request);
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    public void debug_shouldEscapeHtmlPayloadStringToPreventXSS() throws Exception {
        Context.logout();
        Context.addProxyPrivilege(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS);
        try {
            String xssPayload = "<script>alert('ReflectedXSS')</script>";
            
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/apiDocs/debug");
            request.addParameter("tag", xssPayload);
            
            MockHttpServletResponse response = handle(request);
            String responseContent = response.getContentAsString();
            
            Assert.assertTrue(responseContent.contains("&lt;script&gt;alert('ReflectedXSS')&lt;/script&gt;"));
            Assert.assertFalse(responseContent.contains(xssPayload));
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS);
        }
    }
}