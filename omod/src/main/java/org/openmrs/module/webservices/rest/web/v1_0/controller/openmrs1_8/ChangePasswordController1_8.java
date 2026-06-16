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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.api.ValidationException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	// @Autowired en @Qualifier zijn hier weggehaald om NullPointerExceptions te voorkomen

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Authorized()
	public void changeOwnPassword(@RequestBody Map<String, String> body) {
		ChangeOwnPasswordRequest dto = new ChangeOwnPasswordRequest();
		dto.setOldPassword(body.get("oldPassword"));
		dto.setNewPassword(body.get("newPassword"));

		String oldPassword = dto.getOldPassword();
		String newPassword = dto.getNewPassword();

		if (!Context.isAuthenticated()) {
			throw new APIAuthenticationException("Must be authenticated to change your own password");
		}
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			// Gewijzigd naar de statische OpenMRS Context
			Context.getUserService().changePassword(oldPassword, newPassword);
		}
		catch (APIException ex) {
			throw new ValidationException(ex.getMessage());
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}

	@RequestMapping(value = "/{userUuid}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Authorized({PrivilegeConstants.EDIT_USER_PASSWORDS})
	public void changeOthersPassword(@PathVariable("userUuid") String userUuid, @RequestBody ChangeOtherPasswordRequest request) {
		String newPassword = request.getNewPassword();
		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		User user;
		try {
			// Gewijzigd naar de statische OpenMRS Context
			user = Context.getUserService().getUserByUuid(userUuid);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
		}

		if (user == null || user.getUserId() == null) {
			throw new NullPointerException();
		} else {
			// Gewijzigd naar de statische OpenMRS Context
			Context.getUserService().changePassword(user, newPassword);
		}
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseBody
	public SimpleObject handleNotFound(NullPointerException exception, HttpServletRequest request,
									   HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return RestUtil.wrapErrorResponse(exception, "User not found");
	}
}