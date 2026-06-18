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

public class SettingsFormControllerTest extends BaseContextMockTest {

    @Test
    public void showForm_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Assert.assertTrue("The SettingsFormController class must have the @Authorized annotation", 
            SettingsFormController.class.isAnnotationPresent(Authorized.class));
            
        Authorized authorizedAnnotation = SettingsFormController.class.getAnnotation(Authorized.class);
        String[] privileges = authorizedAnnotation.value();
        
        Assert.assertEquals("There should be exactly 1 privilege associated with the controller", 1, privileges.length);
        Assert.assertEquals("The required privilege should be MANAGE_GLOBAL_PROPERTIES", 
            PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES, privileges[0]);
    }

    @Test
    public void searchProperties_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Method searchMethod = SettingsFormController.class.getMethod("searchProperties", String.class);
        
        Assert.assertTrue("The class annotation must also apply to searchProperties", 
            SettingsFormController.class.isAnnotationPresent(Authorized.class));
            
        Authorized authorizedAnnotation = SettingsFormController.class.getAnnotation(Authorized.class);
        Assert.assertTrue("The required privilege must contain MANAGE_GLOBAL_PROPERTIES", 
            java.util.Arrays.asList(authorizedAnnotation.value()).contains(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES));
    }
}