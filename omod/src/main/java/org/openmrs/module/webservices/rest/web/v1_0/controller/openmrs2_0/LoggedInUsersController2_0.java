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

import org.openmrs.annotation.Authorized;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/loggedinusers")
public class LoggedInUsersController2_0 extends BaseRestController {

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Authorized({PrivilegeConstants.GET_USERS})
	public Object getLoggedInUsers(HttpSession httpSession) {

		ServletContext servletContext = httpSession.getServletContext();
		@SuppressWarnings("unchecked")
		Map<String, String> currentUsers = (Map<String, String>) servletContext.getAttribute(WebConstants.CURRENT_USERS);
		if (currentUsers == null) {
			currentUsers = new HashMap<>();
		}

		List<String> userNames = new ArrayList<>(currentUsers.values());
		Collections.sort(userNames);
		return userNames;
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public SimpleObject handleException(Exception exception, HttpServletResponse response) {
		int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		response.setStatus(status);
		return buildCleanErrorResponse(status, "Internal Server Error", exception.getMessage());
	}
}