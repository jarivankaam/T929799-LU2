/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.api.ValidationException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.openmrs.module.webservices.rest.web.v1_0.dto.ChangeOwnPasswordRequest;
import org.openmrs.module.webservices.rest.web.v1_0.dto.ChangeOtherPasswordRequest;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/password")
public class ChangePasswordController1_8 extends BaseRestController {

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Authorized()
	public void changeOwnPassword(@RequestBody ChangeOwnPasswordRequest request) {
		if (request == null || request.getOldPassword() == null || request.getNewPassword() == null) {
			throw new ValidationException("Both oldPassword and newPassword are required.");
		}

		String oldPassword = request.getOldPassword();
		String newPassword = request.getNewPassword();

		if (!Context.isAuthenticated()) {
			throw new APIAuthenticationException("Must be authenticated to change your own password");
		}

		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Context.getUserService().changePassword(oldPassword, newPassword);
		}
		catch (APIException ex) {
			throw new ValidationException("Password change failed. Ensure the old password is correct and the new password meets the system complexity requirements.");
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}

	@RequestMapping(value = "/{userUuid}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Authorized({PrivilegeConstants.EDIT_USER_PASSWORDS})
	public void changeOthersPassword(@PathVariable("userUuid") String userUuid, @RequestBody ChangeOtherPasswordRequest request) {

		if (request == null || request.getNewPassword() == null) {
			throw new ValidationException("newPassword is required.");
		}

		String newPassword = request.getNewPassword();

		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		User user;
		try {
			user = Context.getUserService().getUserByUuid(userUuid);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
		}

		if (user == null || user.getUserId() == null) {
			throw new NullPointerException();
		} else {
			Context.getUserService().changePassword(user, newPassword);
		}
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseBody
	public SimpleObject handleNotFound(NullPointerException exception, HttpServletRequest request,
									   HttpServletResponse response) {
		int status = HttpServletResponse.SC_NOT_FOUND;
		response.setStatus(status);

		return buildCleanErrorResponse(status, "Not Found", "User not found");
	}
}