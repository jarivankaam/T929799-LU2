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

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

/**
 * Replaces per-resource if-else chains in {@code getRepresentationDescription()} with a
 * declarative builder. Each representation type is registered once; {@link #get(Representation)}
 * returns a fresh {@link DelegatingResourceDescription} built from the registered setup.
 *
 * <p>Usage:
 * <pre>
 * private static final RepresentationDescriptionFactory DESCRIPTIONS =
 *     RepresentationDescriptionFactory.builder()
 *         .forDefault(d -&gt; {
 *             d.addProperty("uuid");
 *             d.addSelfLink();
 *         })
 *         .forFull(d -&gt; {
 *             d.addProperty("uuid");
 *             d.addProperty("auditInfo");
 *             d.addSelfLink();
 *         })
 *         .build();
 *
 * public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
 *     return DESCRIPTIONS.get(rep);
 * }
 * </pre>
 */
public final class RepresentationDescriptionFactory {

	/** Functional interface for Java-8 compatibility (avoids java.util.function dependency). */
	public interface DescriptionSetup {

		void configure(DelegatingResourceDescription description);
	}

	private final Map<Class<? extends Representation>, DescriptionSetup> setups;

	private RepresentationDescriptionFactory(Map<Class<? extends Representation>, DescriptionSetup> setups) {
		this.setups = setups;
	}

	/**
	 * Returns a freshly built {@link DelegatingResourceDescription} for {@code rep}, or
	 * {@code null} if no setup was registered for that representation type.
	 */
	public DelegatingResourceDescription get(Representation rep) {
		DescriptionSetup setup = setups.get(rep.getClass());
		if (setup == null) {
			return null;
		}
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		setup.configure(description);
		return description;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private final Map<Class<? extends Representation>, DescriptionSetup> setups = new LinkedHashMap<Class<? extends Representation>, DescriptionSetup>();

		public Builder forRef(DescriptionSetup setup) {
			setups.put(RefRepresentation.class, setup);
			return this;
		}

		public Builder forDefault(DescriptionSetup setup) {
			setups.put(DefaultRepresentation.class, setup);
			return this;
		}

		public Builder forFull(DescriptionSetup setup) {
			setups.put(FullRepresentation.class, setup);
			return this;
		}

		/** Register a custom or named representation type explicitly. */
		public Builder forType(Class<? extends Representation> type, DescriptionSetup setup) {
			setups.put(type, setup);
			return this;
		}

		public RepresentationDescriptionFactory build() {
			return new RepresentationDescriptionFactory(new LinkedHashMap<Class<? extends Representation>, DescriptionSetup>(setups));
		}
	}
}
