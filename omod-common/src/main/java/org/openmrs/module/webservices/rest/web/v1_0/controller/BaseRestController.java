/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.api.ValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

/**
 * Resource controllers should extend this base class to have standard exception handling done
 * automatically.
 * NOTE: @Controller and @RequestMapping removed from class level to prevent component conflicts.
 */
public class BaseRestController {

	private final int DEFAULT_ERROR_CODE = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	private static final String DISABLE_WWW_AUTH_HEADER_NAME = "Disable-WWW-Authenticate";

	private final String DEFAULT_ERROR_DETAIL = "";

	private final Log log = LogFactory.getLog(getClass());

	protected SimpleObject buildCleanErrorResponse(int errorCode, String message, String errorDetail) {
		SimpleObject cleanErrorResponse = new SimpleObject();
		SimpleObject errorDetails = new SimpleObject();

		errorDetails.put("message", message);
		errorDetails.put("code", String.valueOf(errorCode));
		errorDetails.put("detail", StringUtils.isNotEmpty(errorDetail) ? errorDetail : "An unexpected error occurred. Please contact your system administrator.");

		cleanErrorResponse.put("error", errorDetails);
		return cleanErrorResponse;
	}

	/**
	 * <strong>Should</strong> return unauthorized if not logged in
	 * <strong>Should</strong> return forbidden if logged in
	 */
	@ExceptionHandler(APIAuthenticationException.class)
	@ResponseBody
	public SimpleObject apiAuthenticationExceptionHandler(Exception ex, HttpServletRequest request,
														  HttpServletResponse response) throws Exception {
		int errorCode;
		String errorDetail;
		String message;

		if (Context.isAuthenticated()) {
			errorCode = HttpServletResponse.SC_FORBIDDEN;
			message = "Forbidden";
			errorDetail = "User is logged in but doesn't have the relevant privilege";
		} else {
			errorCode = HttpServletResponse.SC_UNAUTHORIZED;
			message = "Unauthorized";
			errorDetail = "User is not logged in";
			if (shouldAddWWWAuthHeader(request)) {
				response.addHeader("WWW-Authenticate", "Basic realm=\"OpenMRS at " + RestConstants.URI_PREFIX + "\"");
			}
		}
		response.setStatus(errorCode);
		return buildCleanErrorResponse(errorCode, message, errorDetail);
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseBody
	public SimpleObject validationExceptionHandler(ValidationException validationException, HttpServletRequest request,
												   HttpServletResponse response) {
		int status = HttpServletResponse.SC_BAD_REQUEST;
		response.setStatus(status);

		String userMessage = validationException.getMessage();
		if (userMessage != null && userMessage.contains("cannot be null or blank")) {
			userMessage = "One or more required fields are empty or invalid.";
		}

		return buildCleanErrorResponse(status, "Bad Request", userMessage);
	}

	@ExceptionHandler(ConversionException.class)
	@ResponseBody
	public SimpleObject conversionExceptionHandler(ConversionException conversionException, HttpServletRequest request,
												   HttpServletResponse response) {
		int status = HttpServletResponse.SC_BAD_REQUEST;
		response.setStatus(status);
		return buildCleanErrorResponse(status, "Bad Request", conversionException.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public SimpleObject httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException httpMessageNotReadableException, HttpServletRequest request,
															   HttpServletResponse response) {
		int status = HttpServletResponse.SC_BAD_REQUEST;
		response.setStatus(status);
		return buildCleanErrorResponse(status, "Bad Request", "Malformed or unreadable JSON request body.");
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public SimpleObject handleException(Exception ex, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		int errorCode = DEFAULT_ERROR_CODE; // Default 500
		String errorDetail = DEFAULT_ERROR_DETAIL;

		ResponseStatus ann = ex.getClass().getAnnotation(ResponseStatus.class);

		if (ann != null) {
			errorCode = ann.value().value();
			if (StringUtils.isNotEmpty(ann.reason())) {
				errorDetail = ann.reason();
			}
		}
		else if (RestUtil.hasCause(ex, APIAuthenticationException.class)) {
			return apiAuthenticationExceptionHandler(ex, request, response);
		}
		// Voorkom 500 server crash bij missende parameters: vertaal deze APIException naar een nette 400 Bad Request
		else if (ex instanceof org.openmrs.api.APIException && ex.getMessage() != null && ex.getMessage().contains("cannot be null or blank")) {
			errorCode = HttpServletResponse.SC_BAD_REQUEST;
			errorDetail = "One or more required fields are empty or invalid.";
		}
		else if (ex.getClass() == HttpRequestMethodNotSupportedException.class) {
			errorCode = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
			errorDetail = "HTTP method not supported";
		}

		// LOGGING: Exact conform de verwachtingen van de unit-testen (BaseRestControllerTest)
		if (errorCode >= 500) {
			log.error(ex.getMessage(), ex);
		} else {
			log.info(ex.getMessage(), ex);
		}

		response.setStatus(errorCode);

		// VEILIGHEIDSMITIGATIE API OUTPUT: Dwing bij alle 500 serverfouten ALTIJD een generieke melding af richting de client
		String message = (errorCode >= 500) ? "Internal Server Error" : "Error";
		if (errorCode >= 500) {
			errorDetail = "An unexpected error occurred. Please contact your system administrator.";
		}

		return buildCleanErrorResponse(errorCode, message, errorDetail);
	}

	private boolean shouldAddWWWAuthHeader(HttpServletRequest request) {
		return request.getHeader(DISABLE_WWW_AUTH_HEADER_NAME) == null
				|| !request.getHeader(DISABLE_WWW_AUTH_HEADER_NAME).equals("true");
	}

	public String getNamespace() {
		return RestConstants.VERSION_1;
	}

	public String buildResourceName(String resource) {
		String namespace = getNamespace();

		if (StringUtils.isBlank(namespace)) {
			return resource;
		} else {
			if (namespace.startsWith("/")) {
				namespace = namespace.substring(1);
			}
			if (!namespace.endsWith("/")) {
				namespace += "/";
			}
			return namespace + resource;
		}
	}
}