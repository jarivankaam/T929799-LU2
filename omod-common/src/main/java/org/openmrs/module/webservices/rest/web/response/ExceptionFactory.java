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

import org.springframework.http.HttpStatus;

/**
 * Central factory for {@link ResponseException} instances.
 * <p>
 * Replaces scattered {@code new XxxException()} calls with semantic factory methods, and provides a
 * generic {@link #forStatus(HttpStatus, String)} entry-point that maps an HTTP status code to the
 * canonical exception type for that status.
 * <p>
 * When multiple exception types share the same status code the semantic methods should be preferred
 * (e.g. prefer {@link #unknownResource(String)} over {@link #forStatus(HttpStatus, String)} with
 * 404) so the precise subtype is preserved.
 */
public final class ExceptionFactory {
	
	private ExceptionFactory() {
	}
	
	// ── 404 Not Found ────────────────────────────────────────────────────────
	
	public static ObjectNotFoundException notFound() {
		return new ObjectNotFoundException();
	}
	
	public static ObjectNotFoundException notFound(String message) {
		return new ObjectNotFoundException(message);
	}
	
	public static UnknownResourceException unknownResource(String message) {
		return new UnknownResourceException(message);
	}
	
	public static ObjectMismatchException objectMismatch(String message) {
		return new ObjectMismatchException(message, null);
	}
	
	// ── 400 Bad Request ──────────────────────────────────────────────────────
	
	public static IllegalRequestException badRequest(String message) {
		return new IllegalRequestException(message);
	}
	
	public static IllegalRequestException badRequest(String message, Throwable cause) {
		return new IllegalRequestException(message, cause);
	}
	
	public static InvalidSearchException invalidSearch(String message) {
		return new InvalidSearchException(message);
	}
	
	public static ConversionException conversionFailed(String message) {
		return new ConversionException(message);
	}
	
	public static ConversionException conversionFailed(String message, Throwable cause) {
		return new ConversionException(message, cause);
	}
	
	public static IllegalPropertyException illegalProperty(String message) {
		return new IllegalPropertyException(message);
	}
	
	public static ResourceDoesNotSupportOperationException operationNotSupported() {
		return new ResourceDoesNotSupportOperationException();
	}
	
	public static ResourceDoesNotSupportOperationException operationNotSupported(String message) {
		return new ResourceDoesNotSupportOperationException(message);
	}
	
	// ── 500 Internal Server Error ─────────────────────────────────────────────
	
	public static GenericRestException serverError(String message) {
		return new GenericRestException(message);
	}
	
	public static GenericRestException serverError(String message, Throwable cause) {
		return new GenericRestException(message, cause);
	}
	
	// ── Generic status-code mapping ───────────────────────────────────────────
	
	/**
	 * Maps an HTTP status to the canonical {@link ResponseException} subtype. Use the semantic
	 * methods above when the specific subtype matters.
	 */
	public static ResponseException forStatus(HttpStatus status, String message) {
		switch (status) {
			case NOT_FOUND:
				return new ObjectNotFoundException(message);
			case BAD_REQUEST:
				return new IllegalRequestException(message);
			case INTERNAL_SERVER_ERROR:
				return new GenericRestException(message);
			default:
				return new GenericRestException(message);
		}
	}
	
	public static ResponseException forStatus(HttpStatus status, String message, Throwable cause) {
		switch (status) {
			case NOT_FOUND:
				return new ObjectNotFoundException(message, cause);
			case BAD_REQUEST:
				return new IllegalRequestException(message, cause);
			case INTERNAL_SERVER_ERROR:
				return new GenericRestException(message, cause);
			default:
				return new GenericRestException(message, cause);
		}
	}
}
