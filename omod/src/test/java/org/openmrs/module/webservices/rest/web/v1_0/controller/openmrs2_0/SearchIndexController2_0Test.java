/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.test.BaseContextMockTest;
import org.junit.Assert;
import org.openmrs.api.context.Context;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.util.PrivilegeConstants;

public class SearchIndexController2_0Test extends BaseContextMockTest {

	@Mock
	ContextDAO contextDAO;

	private SearchIndexController2_0 controller = new SearchIndexController2_0();
	
	@Test
	public void updateSearchIndex_shouldUpdateTheEntireSearchIndex() throws Exception {
		controller.updateSearchIndex(null);

		Mockito.verify(contextDAO, Mockito.times(1)).updateSearchIndex();
	}
	
	@Test
	public void updateSearchIndex_shouldUpdateTheEntireSearchIndexAsynchronously() throws Exception {
		controller.updateSearchIndex("{\"async\": true}");
		
		Mockito.verify(contextDAO, Mockito.times(1)).updateSearchIndexAsync();
	}

    @Test
    public void updateSearchIndex_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Assert.assertTrue("The SearchIndexController2_0 class must have the @Authorized annotation", 
            SearchIndexController2_0.class.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized classAuth = SearchIndexController2_0.class.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> classPrivileges = java.util.Arrays.asList(classAuth.value());
        
        Assert.assertTrue("Class level annotation must require MANAGE_SEARCH_INDEX", 
            classPrivileges.contains(PrivilegeConstants.MANAGE_SEARCH_INDEX));
    }

    @Test
    public void updateSearchIndex_shouldAllowAccessWhenUserHasManageSearchIndex() throws Exception {
        java.lang.reflect.Method method = SearchIndexController2_0.class.getMethod("updateSearchIndex", String.class);
        
        Assert.assertTrue("The updateSearchIndex method must have the @Authorized annotation", 
            method.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized methodAuth = method.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> methodPrivileges = java.util.Arrays.asList(methodAuth.value());
        
        Assert.assertTrue("Method level annotation must require MANAGE_SEARCH_INDEX", 
            methodPrivileges.contains(PrivilegeConstants.MANAGE_SEARCH_INDEX));
        Assert.assertTrue("Method level annotation must require VIEW_ADMIN_FUNCTIONS", 
            methodPrivileges.contains(PrivilegeConstants.VIEW_ADMIN_FUNCTIONS));
    }	
}
