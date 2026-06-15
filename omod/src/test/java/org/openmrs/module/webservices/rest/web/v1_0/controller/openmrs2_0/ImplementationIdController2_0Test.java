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

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.openmrs.ImplementationId;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.openmrs.scheduler.SchedulerException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.junit.Assert;
import org.openmrs.api.context.Context;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImplementationIdController2_0Test extends RestControllerTestUtils {

	@Mock
	private AdministrationService administrationService;

	@Captor
	private ArgumentCaptor<ImplementationId> implementationIdArgumentCaptor;

	@Before
	public void before() throws SchedulerException {
		initMocks();
	}

	@Test
	public void shouldGetCurrentConfiguration() throws Exception {
		// set initial config (mock)
		ImplementationId implementationId = new ImplementationId();
		implementationId.setImplementationId("implementationId");
		implementationId.setPassphrase("passphrase");
		implementationId.setName("name");
		implementationId.setDescription("description");
		when(administrationService.getImplementationId()).thenReturn(implementationId);

		// make GET call
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));

		assertEquals("name", PropertyUtils.getProperty(result, "name"));
		assertEquals("implementationId", PropertyUtils.getProperty(result, "implementationId"));
		assertEquals("description", PropertyUtils.getProperty(result, "description"));
		assertEquals("passphrase", PropertyUtils.getProperty(result, "passphrase"));
	}

	@Test
	public void shouldUpdateCurrentConfiguration() throws Exception {
		// make POST call
		String json = "{\"name\": \"name\",\"description\": \"description\",\"implementationId\": \"implementationId\",\"passphrase\": \"passphrase\"}";
		MockHttpServletRequest req = newPostRequest(getURI(), json);
		handle(req);

		verify(administrationService).setImplementationId(implementationIdArgumentCaptor.capture());
		ImplementationId savedImplementationId = implementationIdArgumentCaptor.getValue();

		assertEquals("name", savedImplementationId.getName());
		assertEquals("implementationId", savedImplementationId.getImplementationId());
		assertEquals("description", savedImplementationId.getDescription());
		assertEquals("passphrase", savedImplementationId.getPassphrase());
	}

	private String getURI() {
		return "implementationid";
	}

	@Test
    public void updateCurrentConfiguration_shouldReturnForbiddenWhenAnonymous() throws Exception {
        java.lang.reflect.Method postMethod = ImplementationIdController2_0.class.getMethod("updateCurrentConfiguration", org.openmrs.ImplementationId.class);
        
        Assert.assertTrue("The updateCurrentConfiguration method must have the @Authorized annotation", 
            postMethod.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized methodAuth = postMethod.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(methodAuth.value());
        
        Assert.assertTrue("Method level annotation must require MANAGE_IMPLEMENTATION_ID", 
            privileges.contains(org.openmrs.util.PrivilegeConstants.MANAGE_IMPLEMENTATION_ID));
    }

    @Test
    public void getCurrentConfiguration_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Assert.assertTrue("The ImplementationIdController2_0 class must have the @Authorized annotation", 
            ImplementationIdController2_0.class.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized classAuth = ImplementationIdController2_0.class.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(classAuth.value());
        
        Assert.assertTrue("Class level annotation must require MANAGE_IMPLEMENTATION_ID", 
            privileges.contains(org.openmrs.util.PrivilegeConstants.MANAGE_IMPLEMENTATION_ID));
    }
}
