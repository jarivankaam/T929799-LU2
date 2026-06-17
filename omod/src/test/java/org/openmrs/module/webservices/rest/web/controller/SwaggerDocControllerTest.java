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
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.test.BaseContextMockTest;
import java.lang.reflect.Method;

public class SwaggerDocControllerTest extends BaseContextMockTest {

    private SwaggerDocController controller = new SwaggerDocController();

    @Test
    public void debug_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Method debugMethod = SwaggerDocController.class.getMethod("debug", String.class);
        
        Assert.assertTrue("De debug methode moet de @Authorized annotatie hebben", 
            debugMethod.isAnnotationPresent(Authorized.class));
            
        Authorized authorizedAnnotation = debugMethod.getAnnotation(Authorized.class);
        String[] privileges = authorizedAnnotation.value();
        
        Assert.assertEquals("There should be exactly 1 privilege associated with the method", 1, privileges.length);
        Assert.assertEquals("The required privilege should be VIEW_ADMIN_FUNCTIONS", 
            PrivilegeConstants.VIEW_ADMIN_FUNCTIONS, privileges[0]);
    }

    @Test
    public void debug_shouldEscapeHtmlPayloadStringToPreventXSS() throws Exception {
        String xssPayload = "<script>alert('ReflectedXSS')</script>";
        
        String responseContent = controller.debug(xssPayload);
        
        Assert.assertFalse("The response must not contain the raw script tags", 
            responseContent.contains(xssPayload));

        Assert.assertTrue("The response must contain the text 'Debugging Tag:'", 
            responseContent.contains("Debugging Tag:"));
        Assert.assertTrue("The response must be escaped (should not contain '<' character from the payload)", 
            !responseContent.contains("<script>"));
    }
}