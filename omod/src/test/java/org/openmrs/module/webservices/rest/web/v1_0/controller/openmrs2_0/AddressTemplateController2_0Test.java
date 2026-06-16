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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.http.HttpServletResponse;

public class AddressTemplateController2_0Test extends MainResourceControllerTest {

	@Override
	public String getURI() {
		return "addresstemplate";
	}
	
	@Test
	public void shouldGetAddressTemplate() throws Exception {
		String xml;
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("addressTemplate.xml")) {
			xml = IOUtils.toString(inputStream, "UTF-8");
		}
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE, xml);
		
		MockHttpServletRequest req = newGetRequest(getURI());
		
		SimpleObject result = deserialize(handle(req));
		
		String json;
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("addressTemplate.json")) {
			json = IOUtils.toString(inputStream, "UTF-8");
		}
		Assert.assertThat(result, Matchers.is(SimpleObject.parseJson(json)));
	}

	@Test
    public void get_shouldReturnForbiddenWhenAnonymous() throws Exception {
        java.lang.reflect.Method getMethod = AddressTemplateController2_0.class.getMethod("get", org.springframework.web.context.request.WebRequest.class);
        
        Assert.assertTrue("The get method must have the @Authorized annotation", 
            getMethod.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized methodAuth = getMethod.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(methodAuth.value());
        
        Assert.assertTrue("Method level annotation must require GET_PATIENTS privilege", 
            privileges.contains(org.openmrs.util.PrivilegeConstants.GET_PATIENTS));
    }

    @Test
    public void get_shouldAllowAccessWhenUserHasGetPatients() throws Exception {
        java.lang.reflect.Method getMethod = AddressTemplateController2_0.class.getMethod("get", org.springframework.web.context.request.WebRequest.class);
        org.openmrs.annotation.Authorized methodAuth = getMethod.getAnnotation(org.openmrs.annotation.Authorized.class);
        
        Assert.assertNotNull("The @Authorized annotation configuration must not be null", methodAuth);
        Assert.assertEquals("The required privilege must strictly match GET_PATIENTS", 
            org.openmrs.util.PrivilegeConstants.GET_PATIENTS, methodAuth.value()[0]);
    }
	
	@Override
	public String getUuid() {
		return null;
	}

	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public void shouldGetAll() throws Exception {
		
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
		
	}
}
