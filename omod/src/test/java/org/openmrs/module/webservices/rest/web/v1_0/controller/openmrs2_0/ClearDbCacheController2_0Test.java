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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Assert;

public class ClearDbCacheController2_0Test extends RestControllerTestUtils {
    
    private static final String CLEAR_DB_CACHE_URI = "cleardbcache";
    
    @Autowired
    private PersonService personService;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private static final Class PERSON_NAME_CLASS = PersonName.class;
    
    private static final Integer ID_2 = 2;
    
    private static final Integer ID_8 = 8;
    
    private static final String QUERY_REGION = "test";
    
    @Test
    public void clearDbCache_shouldEvictTheEntityFromTheCaches() throws Exception {
        PersonName name = personService.getPersonName(ID_2);
        personService.getPerson(name.getPerson().getPersonId());
        
        Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId = ?0");
        query.setInteger(0, 9351);
        query.setCacheable(true);
        query.setCacheRegion(QUERY_REGION);
        query.list();
        
        final String data = "{\"resource\": \"person\", \"subResource\": \"name\", \"uuid\": \"" + name.getUuid() + "\"}";
        
        MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
        
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }
    
    @Test
    public void clearDbCache_shouldEvictAllEntitiesOfTheSpecifiedTypeFromTheCaches() throws Exception {
        PersonName name1 = personService.getPersonName(ID_2);
        PersonName name2 = personService.getPersonName(ID_8);
        personService.getPerson(name1.getPerson().getPersonId()).getNames();
        personService.getPerson(name2.getPerson().getPersonId()).getNames();
        
        Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId IN (?0, ?1)");
        query.setInteger(0, name1.getPersonNameId());
        query.setInteger(1, name2.getPersonNameId());
        query.setCacheable(true);
        query.setCacheRegion(QUERY_REGION);
        query.list();
        
        final String data = "{\"resource\": \"person\", \"subResource\": \"name\"}";
        
        MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
        
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }
    
    @Test
    public void clearDbCache_shouldEvictAllEntitiesFromTheCaches() throws Exception {
        PersonName name1 = personService.getPersonName(ID_2);
        PersonName name2 = personService.getPersonName(ID_8);
        personService.getPerson(name1.getPerson().getPersonId()).getNames();
        personService.getPerson(name2.getPerson().getPersonId()).getNames();
        locationService.getLocation(ID_2);
        
        Query query = sessionFactory.getCurrentSession().createQuery("FROM PersonName WHERE personNameId IN (?0, ?1)");
        query.setInteger(0, name1.getPersonNameId());
        query.setInteger(1, name2.getPersonNameId());
        query.setCacheable(true);
        query.setCacheRegion(QUERY_REGION);
        query.list();
        
        MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, "{}"));
        
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }
    
    @Test
    public void clearDbCache_shouldNotFailIfNoEntityIsFoundMatchingTheSpecifiedUuid() throws Exception {
        final String uuid = "some-uuid";
        assertNull(personService.getPersonNameByUuid(uuid));
        final String data = "{\"resource\": \"person\", \"subResource\": \"name\", \"uuid\": \"" + uuid + "\"}";
        
        MockHttpServletResponse response = handle(newPostRequest(CLEAR_DB_CACHE_URI, data));
        
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    @Test
    public void clearDbCache_shouldReturnForbiddenWhenAnonymous() throws Exception {
        Assert.assertTrue("The ClearDbCacheController2_0 class must have the @Authorized annotation", 
            ClearDbCacheController2_0.class.isAnnotationPresent(org.openmrs.annotation.Authorized.class));
            
        org.openmrs.annotation.Authorized classAuth = ClearDbCacheController2_0.class.getAnnotation(org.openmrs.annotation.Authorized.class);
        java.util.List<String> privileges = java.util.Arrays.asList(classAuth.value());
        
        Assert.assertTrue("Class level annotation must require VIEW_ADMIN_FUNCTIONS privilege", 
            privileges.contains(org.openmrs.util.PrivilegeConstants.VIEW_ADMIN_FUNCTIONS));
    }

    @Test
    public void clearDbCache_shouldAllowAccessWhenUserIsAdmin() throws Exception {
        org.openmrs.annotation.Authorized classAuth = ClearDbCacheController2_0.class.getAnnotation(org.openmrs.annotation.Authorized.class);
        
        Assert.assertNotNull("The class authorization rules must be defined", classAuth);
        Assert.assertEquals("The required privilege must strictly match VIEW_ADMIN_FUNCTIONS", 
            org.openmrs.util.PrivilegeConstants.VIEW_ADMIN_FUNCTIONS, classAuth.value()[0]);
    }
    
}