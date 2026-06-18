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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.ResourceFactory;

public class ScanningResourceFactoryTest {

	@Test
	public void getResourceClasses_shouldDelegateToScanner() throws IOException {
		OpenmrsClassScanner scanner = mock(OpenmrsClassScanner.class);
		List<Class<? extends Resource>> expected = Collections.emptyList();
		when(scanner.getClasses(Resource.class, true)).thenReturn(expected);

		ResourceFactory factory = new ScanningResourceFactory(scanner);
		assertThat(factory.getResourceClasses(), is(expected));
	}

	@Test
	public void getResourceClasses_shouldReturnAllClassesFromScanner() throws IOException {
		OpenmrsClassScanner scanner = mock(OpenmrsClassScanner.class);
		List<Class<? extends Resource>> expected = Arrays.<Class<? extends Resource>> asList(
		    StubResourceA.class, StubResourceB.class);
		when(scanner.getClasses(Resource.class, true)).thenReturn(expected);

		ResourceFactory factory = new ScanningResourceFactory(scanner);
		List<Class<? extends Resource>> result = factory.getResourceClasses();

		assertThat(result.size(), is(2));
		assertThat(result, is(expected));
	}

	private static class StubResourceA implements Resource {

		@Override
		public String getUri(Object instance) {
			return null;
		}
	}

	private static class StubResourceB implements Resource {

		@Override
		public String getUri(Object instance) {
			return null;
		}
	}
}
