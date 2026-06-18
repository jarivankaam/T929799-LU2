/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.response;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class ExceptionFactoryTest {

	@Test
	public void notFound_shouldReturnObjectNotFoundException() {
		assertThat(ExceptionFactory.notFound("msg"), instanceOf(ObjectNotFoundException.class));
	}

	@Test
	public void notFound_withMessage_shouldPreserveMessage() {
		assertThat(ExceptionFactory.notFound("not here").getMessage(), is("not here"));
	}

	@Test
	public void badRequest_shouldReturnIllegalRequestException() {
		assertThat(ExceptionFactory.badRequest("msg"), instanceOf(IllegalRequestException.class));
	}

	@Test
	public void badRequest_withCause_shouldPreserveCause() {
		Throwable cause = new RuntimeException("root");
		assertThat(ExceptionFactory.badRequest("msg", cause).getCause(), is(cause));
	}

	@Test
	public void serverError_shouldReturnGenericRestException() {
		assertThat(ExceptionFactory.serverError("msg"), instanceOf(GenericRestException.class));
	}

	@Test
	public void serverError_withCause_shouldPreserveCause() {
		Throwable cause = new RuntimeException("root");
		assertThat(ExceptionFactory.serverError("msg", cause).getCause(), is(cause));
	}

	@Test
	public void forStatus_shouldMapNotFoundToObjectNotFoundException() {
		assertThat(ExceptionFactory.forStatus(HttpStatus.NOT_FOUND, "msg"), instanceOf(ObjectNotFoundException.class));
	}

	@Test
	public void forStatus_shouldMapBadRequestToIllegalRequestException() {
		assertThat(ExceptionFactory.forStatus(HttpStatus.BAD_REQUEST, "msg"), instanceOf(IllegalRequestException.class));
	}

	@Test
	public void forStatus_shouldMapInternalServerErrorToGenericRestException() {
		assertThat(ExceptionFactory.forStatus(HttpStatus.INTERNAL_SERVER_ERROR, "msg"),
		    instanceOf(GenericRestException.class));
	}

	@Test
	public void forStatus_withCause_shouldPreserveCause() {
		Throwable cause = new RuntimeException("root");
		assertThat(ExceptionFactory.forStatus(HttpStatus.NOT_FOUND, "msg", cause).getCause(), is(cause));
	}
}
