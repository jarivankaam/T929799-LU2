/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.rest.web.v1_0.dto.ClobDataResponseDto;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/clobdata")
public class ClobDatatypeStorageController extends BaseRestController {

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private DatatypeService datatypeService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@Authorized({PrivilegeConstants.ADD_OBS, PrivilegeConstants.EDIT_OBS})
	public ClobDataResponseDto create(@RequestParam MultipartFile file, HttpServletRequest request) {

		if (file == null || file.isEmpty()) {
			throw new ConversionException("The uploaded file cannot be empty.");
		}

		try {
			ClobDatatypeStorage clobData = new ClobDatatypeStorage();
			String encoding = request.getHeader("Content-Encoding");

			clobData.setValue(IOUtils.toString(file.getInputStream(), encoding));
			clobData = datatypeService.saveClobDatatypeStorage(clobData);
			return new ClobDataResponseDto(clobData.getUuid());
		} catch (Exception e) {

			log.error("Failed to read or store uploaded CLOB data: " + e.getMessage(), e);
			throw new ConversionException("Invalid file payload or unsupported content encoding format.");
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
	@Authorized({PrivilegeConstants.GET_OBS})
	public void retrieve(@PathVariable("uuid") String uuid, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(uuid);

		if (clobData == null) {
			throw new ObjectNotFoundException();
		}

		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print(clobData.getValue());
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{uuid}")
	@Authorized({PrivilegeConstants.DELETE_OBS, PrivilegeConstants.PURGE_OBS})
	public void delete(@PathVariable("uuid") String uuid, HttpServletRequest request, HttpServletResponse response) {
		ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(uuid);
		if (clobData == null) {
			throw new ObjectNotFoundException();
		}
		datatypeService.deleteClobDatatypeStorage(clobData);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}