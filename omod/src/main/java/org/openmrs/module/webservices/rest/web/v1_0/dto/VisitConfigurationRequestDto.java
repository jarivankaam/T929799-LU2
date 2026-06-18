/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.dto;

import java.util.List;

public class VisitConfigurationRequestDto {

    private Boolean enableVisits;

    private String encounterVisitsAssignmentHandler;

    private Boolean startAutoCloseVisitsTask;

    private List<VisitTypeDto> visitTypesToAutoClose;

    public Boolean getEnableVisits() {
        return enableVisits;
    }

    public void setEnableVisits(Boolean enableVisits) {
        this.enableVisits = enableVisits;
    }

    public String getEncounterVisitsAssignmentHandler() {
        return encounterVisitsAssignmentHandler;
    }

    public void setEncounterVisitsAssignmentHandler(String encounterVisitsAssignmentHandler) {
        this.encounterVisitsAssignmentHandler = encounterVisitsAssignmentHandler;
    }

    public Boolean getStartAutoCloseVisitsTask() {
        return startAutoCloseVisitsTask;
    }

    public void setStartAutoCloseVisitsTask(Boolean startAutoCloseVisitsTask) {
        this.startAutoCloseVisitsTask = startAutoCloseVisitsTask;
    }

    public List<VisitTypeDto> getVisitTypesToAutoClose() {
        return visitTypesToAutoClose;
    }

    public void setVisitTypesToAutoClose(List<VisitTypeDto> visitTypesToAutoClose) {
        this.visitTypesToAutoClose = visitTypesToAutoClose;
    }

    public static class VisitTypeDto {
        private String uuid;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
}