/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletResponse;

public class ClobDatatypeStorageControllerTest extends MainResourceControllerTest {
	
	private DatatypeService datatypeService;
	
	@Before
	public void before() throws Exception {
		datatypeService = Context.getDatatypeService();
		executeDataSet(RestTestConstants1_9.FORM_RESOURCE_DATA_SET);
	}
	
	@Test
	public void shouldAcceptAndStoreClobDataViaPost() throws Exception {
		long before = getAllCount();
		
		byte[] fileData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
		    RestTestConstants1_9.TEST_RESOURCE_FILE));
		
		MockMultipartFile toUpload = new MockMultipartFile("file", "formresource.txt", "text/plain", fileData);
		
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setRequestURI(getBaseRestURI() + getURI());
		request.setMethod(RequestMethod.POST.name());
		request.addHeader("Content-Type", "multipart/form-data");
		
		request.addFile(toUpload);
		
		MockHttpServletResponse response = handle(request);
		
		Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
		Assert.assertEquals(before + 1, getAllCount());
	}
	
	@Test
	public void shouldReturnClobDataAsFileByUuid() throws Exception {
		ClobDatatypeStorage clob = datatypeService
		        .getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNotNull(clob);
		int size = clob.getValue().getBytes().length;
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/"
		        + RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID));
		
		Assert.assertEquals(size, response.getContentAsByteArray().length);
	}
	
	@Test
	public void shouldDeleteAnExistingClobData() throws Exception {
		ClobDatatypeStorage clob = datatypeService
		        .getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNotNull(clob);
		
		MockHttpServletResponse response = handle(newDeleteRequest(getURI() + "/"
		        + RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID));
		
		clob = datatypeService.getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNull(clob);
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
	
	@Test
	public void shouldReturnHTTP404ForNonExistenceClobdata() throws Exception {
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/non-existence-uuid"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
	}

@Test
    public void create_shouldReturnForbiddenWhenAnonymous() throws Exception {
        java.lang.reflect.Method method = ClobDatatypeStorageController.class.getMethod(
            "create", org.springframework.web.multipart.MultipartFile.class, javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class
        );
        
        Assert.assertTrue("The create methode must have the @Authorized annotation", 
            method.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized auth = method.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(auth.value());
        
        Assert.assertTrue("The required privilege must be EDIT_OBS", privileges.contains(org.openmrs.util.PrivilegeConstants.EDIT_OBS));
    }

    @Test
    public void create_shouldAllowAccessWhenUserHasAddObs() throws Exception {
        // Reflectieve check of ADD_OBS ook in de @Authorized annotatie van create staat
        java.lang.reflect.Method method = ClobDatatypeStorageController.class.getMethod(
            "create", org.springframework.web.multipart.MultipartFile.class, javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class
        );
        
        org.openmrs.annotation.Authorized auth = method.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(auth.value());
        
        Assert.assertTrue("The method must be accessible with ADD_OBS", privileges.contains(org.openmrs.util.PrivilegeConstants.ADD_OBS));
    }

    @Test
    public void retrieve_shouldReturnForbiddenWhenAnonymous() throws Exception {
        java.lang.reflect.Method method = ClobDatatypeStorageController.class.getMethod(
            "retrieve", String.class, javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class
        );
        
        Assert.assertTrue("The retrieve methode must have the @Authorized annotation", 
            method.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized auth = method.getAnnotation(org.openmrs.annotation.Authorized.class);
        Assert.assertEquals("The required privilege must be GET_OBS", 
            org.openmrs.util.PrivilegeConstants.GET_OBS, auth.value()[0]);
    }

    @Test
    public void delete_shouldReturnForbiddenWhenAnonymous() throws Exception {
        java.lang.reflect.Method method = ClobDatatypeStorageController.class.getMethod(
            "delete", String.class, javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class
        );
        
        Assert.assertTrue("The delete methode must have the @Authorized annotation", 
            method.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized auth = method.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(auth.value());
        
        Assert.assertTrue("The required privilege must be DELETE_OBS", 
            privileges.contains(org.openmrs.util.PrivilegeConstants.DELETE_OBS));
    }
	
	@Override
	public String getURI() {
		return "clobdata";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID;
	}
	
	@Override
	public long getAllCount() {
		try {
			Connection connection = getConnection();
			
			ResultSet resultSet = connection.prepareStatement("select count('id') from clob_datatype_storage")
			        .executeQuery();
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	@Override
	@Ignore
	public void shouldGetRefByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetDefaultByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetFullByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetAll() throws Exception {
		
	}
}
