package org.openmrs.module.kenyaemrml.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLUtils;
import org.openmrs.module.kenyaemrml.api.service.ModelService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * The main controller for ML in KenyaEMR
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/keml")
public class MachineLearningRestController extends BaseRestController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(method = RequestMethod.POST, value = "/casefindingscore")
	@ResponseBody
	public Object processModel(HttpServletRequest request) {
		ModelService modelService = new ModelService();
		String requestBody = null;
		try {
			requestBody = MLUtils.fetchRequestBody(request.getReader());
			ObjectNode modelConfigs = MLUtils.getModelConfig(requestBody);
			String facilityName = modelConfigs.get(MLUtils.FACILITY_ID_REQUEST_VARIABLE).asText();
			boolean isDebugMode = modelConfigs.has("debug") && modelConfigs.get("debug").asText().equals("true") ? true
			        : false;
			
			if (facilityName.equals("")) { // default to the default facility configured in the EMR
				facilityName = MLUtils.getDefaultLocation().getName();
			}
			
			String modelId = modelConfigs.get(MLUtils.MODEL_ID_REQUEST_VARIABLE).asText();
			String encounterDate = modelConfigs.get(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText();
			
			if (StringUtils.isBlank(facilityName) || StringUtils.isBlank(modelId) || StringUtils.isBlank(encounterDate)) {
				return new ResponseEntity<Object>("The service requires model, date, and facility information",
				        new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			JSONObject profile = MLUtils.getHTSFacilityProfile("Facility.Name", facilityName, MLUtils.getFacilityCutOffs());
			
			if (profile == null) {
				return new ResponseEntity<Object>(
				        "The facility provided currently doesn't have an HTS cut-off profile. Provide an appropriate facility",
				        new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			ModelInputFields inputFields = MLUtils.extractHTSCaseFindingVariablesFromRequestBody(requestBody, facilityName,
			    encounterDate);
			
			ScoringResult scoringResult = modelService.score(modelId, facilityName, encounterDate, inputFields, isDebugMode);
			return scoringResult;
		}
		catch (IOException e) {
			return new ResponseEntity<Object>("Could not process the request", new HttpHeaders(),
			        HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
