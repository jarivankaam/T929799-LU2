/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

public class RepresentationDescriptionFactoryTest {

	private static final RepresentationDescriptionFactory FACTORY = RepresentationDescriptionFactory.builder()
	        .forRef(d -> d.addProperty("uuid"))
	        .forDefault(d -> {
		        d.addProperty("uuid");
		        d.addProperty("name");
	        })
	        .forFull(d -> {
		        d.addProperty("uuid");
		        d.addProperty("name");
		        d.addProperty("auditInfo");
	        })
	        .build();

	@Test
	public void get_shouldReturnDescriptionForDefaultRepresentation() {
		DelegatingResourceDescription desc = FACTORY.get(new DefaultRepresentation());
		assertNotNull(desc);
		assertThat(desc.getProperties(), hasKey("uuid"));
		assertThat(desc.getProperties(), hasKey("name"));
		assertThat(desc.getProperties(), not(hasKey("auditInfo")));
	}

	@Test
	public void get_shouldReturnDescriptionForFullRepresentation() {
		DelegatingResourceDescription desc = FACTORY.get(new FullRepresentation());
		assertNotNull(desc);
		assertThat(desc.getProperties(), hasKey("uuid"));
		assertThat(desc.getProperties(), hasKey("name"));
		assertThat(desc.getProperties(), hasKey("auditInfo"));
	}

	@Test
	public void get_shouldReturnDescriptionForRefRepresentation() {
		DelegatingResourceDescription desc = FACTORY.get(new RefRepresentation());
		assertNotNull(desc);
		assertThat(desc.getProperties(), hasKey("uuid"));
		assertThat(desc.getProperties(), not(hasKey("name")));
	}

	@Test
	public void get_shouldReturnNullForUnregisteredRepresentation() {
		Representation unknown = new Representation() {

			@Override
			public String getRepresentation() {
				return "unknown";
			}
		};
		assertNull(FACTORY.get(unknown));
	}

	@Test
	public void get_shouldReturnFreshInstanceOnEachCall() {
		DelegatingResourceDescription first = FACTORY.get(new DefaultRepresentation());
		DelegatingResourceDescription second = FACTORY.get(new DefaultRepresentation());
		assertNotSame(first, second);
	}
}
