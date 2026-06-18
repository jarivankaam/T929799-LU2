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

import java.io.IOException;
import java.util.List;

import org.openmrs.module.webservices.rest.web.OpenmrsClassScanner;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.ResourceFactory;

/**
 * Default {@link ResourceFactory} that delegates to {@link OpenmrsClassScanner}.
 * Preserves the existing annotation-scanning behaviour while allowing the scanning
 * strategy to be swapped out in tests or alternative deployments.
 */
public class ScanningResourceFactory implements ResourceFactory {

	private final OpenmrsClassScanner scanner;

	public ScanningResourceFactory(OpenmrsClassScanner scanner) {
		this.scanner = scanner;
	}

	@Override
	public List<Class<? extends Resource>> getResourceClasses() throws IOException {
		return scanner.getClasses(Resource.class, true);
	}
}
