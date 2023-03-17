package org.openmrs.module.kenyaemrml.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONObject;
import org.openmrs.module.kenyaemrml.api.MLUtils;
import org.openmrs.module.kenyaemrml.api.service.ModelService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.webservices.rest.SimpleObject;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * The main controller for ML in KenyaEMR
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/keml")
public class MachineLearningRestController extends BaseRestController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The HTS case finding POST request.
	 * ### Sample Input Payload
		{
			"modelConfigs": {
				"modelId": "hts_xgb_1211_jan_2023",
				"encounterDate": "2023-03-03",
				"facilityId": "14607",
				"debug": "true"
			},
			"variableValues": {
				"Age": 31,
				"PopulationTypeGP": 1,
				"PopulationTypeKP": 0,
				"PopulationTypePRIORITY": 0,
				"KeyPopulationFSW": 0,
				"KeyPopulationMSM": 0,
				"KeyPopulationNR": 1,
				"KeyPopulationOther": 0,
				"KeyPopulationPWID": 0,
				"PriorityPopulationAGYW": 0,
				"PriorityPopulationFISHERMEN": 0,
				"PriorityPopulationNR": 1,
				"PriorityPopulationOTHER": 0,
				"DepartmentEMERGENCY": 0,
				"DepartmentIPD": 0,
				"DepartmentOPD": 0,
				"DepartmentPMTCT": 0,
				"DepartmentVCT": 1,
				"IsHealthWorkerNO": 1,
				"IsHealthWorkerNR": 0,
				"IsHealthWorkerYES": 0,
				"SexuallyActiveNO": 0,
				"SexuallyActiveNR": 0,
				"SexuallyActiveYES": 1,
				"NewPartnerNO": 0,
				"NewPartnerNR": 0,
				"NewPartnerYES": 1,
				"PartnerHIVStatusNEGATIVE": 0,
				"PartnerHIVStatusNR": 0,
				"PartnerHIVStatusPOSITIVE": 0,
				"PartnerHIVStatusUNKNOWN": 1,
				"NumberOfPartnersMULTIPLE": 0,
				"NumberOfPartnersNR": 1,
				"NumberOfPartnersSINGLE": 0,
				"AlcoholSexALWAYS": 0,
				"AlcoholSexNEVER": 0,
				"AlcoholSexNR": 1,
				"AlcoholSexSOMETIMES": 0,
				"MoneySexNO": 1,
				"MoneySexNR": 0,
				"MoneySexYES": 0,
				"CondomBurstNO": 1,
				"CondomBurstNR": 0,
				"CondomBurstYES": 0,
				"UnknownStatusPartnerNO": 1,
				"UnknownStatusPartnerNR": 0,
				"UnknownStatusPartnerYES": 0,
				"KnownStatusPartnerNO": 1,
				"KnownStatusPartnerNR": 0,
				"KnownStatusPartnerYES": 0,
				"PregnantNO": 1,
				"PregnantNR": 0,
				"PregnantYES": 0,
				"BreastfeedingMotherNO": 1,
				"BreastfeedingMotherNR": 0,
				"BreastfeedingMotherYES": 0,
				"ExperiencedGBVNO": 1,
				"ExperiencedGBVYES": 0,
				"CurrentlyOnPrepNO": 0,
				"CurrentlyOnPrepNR": 0,
				"CurrentlyOnPrepYES": 1,
				"CurrentlyHasSTINO": 1,
				"CurrentlyHasSTINR": 0,
				"CurrentlyHasSTIYES": 0,
				"SharedNeedleNO": 0,
				"SharedNeedleNR": 1,
				"SharedNeedleYES": 0,
				"NeedleStickInjuriesNO": 0,
				"NeedleStickInjuriesNR": 1,
				"NeedleStickInjuriesYES": 0,
				"TraditionalProceduresNO": 1,
				"TraditionalProceduresNR": 0,
				"TraditionalProceduresYES": 0,
				"MothersStatusNEGATIVE": 0,
				"MothersStatusNR": 1,
				"MothersStatusPOSITIVE": 0,
				"MothersStatusUNKNOWN": 0,
				"ReferredForTestingNO": 0,
				"ReferredForTestingYES": 1,
				"GenderFEMALE": 1,
				"GenderMALE": 0,
				"MaritalStatusDIVORCED": 0,
				"MaritalStatusMARRIED": 0,
				"MaritalStatusMINOR": 0,
				"MaritalStatusPOLYGAMOUS": 0,
				"MaritalStatusSINGLE": 0,
				"EverTestedForHivNO": 0,
				"EverTestedForHivYES": 0,
				"MonthsSinceLastTestLASTSIXMONTHS": 0,
				"MonthsSinceLastTestMORETHANTWOYEARS": 1,
				"MonthsSinceLastTestNR": 0,
				"MonthsSinceLastTestONETOTWOYEARS": 0,
				"MonthsSinceLastTestSEVENTOTWELVE": 0,
				"ClientTestedAsCOUPLE": 0,
				"ClientTestedAsINDIVIDUAL": 0,
				"EntryPointIPD": 0,
				"EntryPointOPD": 0,
				"EntryPointOTHER": 0,
				"EntryPointPEDIATRIC": 0,
				"EntryPointPMTCT_ANC": 0,
				"EntryPointPMTCT_MAT_PNC": 0,
				"EntryPointTB": 0,
				"EntryPointVCT": 1,
				"EntryPointVMMC": 0,
				"TestStrategyHB": 0,
				"TestStrategyHP": 0,
				"TestStrategyINDEX": 0,
				"TestStrategyMO": 0,
				"TestStrategyNP": 0,
				"TestStrategyOTHER": 0,
				"TestStrategySNS": 0,
				"TestStrategyVI": 0,
				"TestStrategyVS": 1,
				"TbScreeningCONFIRMEDTB": 0,
				"TbScreeningNOPRESUMEDTB": 0,
				"TbScreeningPRESUMEDTB": 0,
				"ClientSelfTestedNO": 0,
				"ClientSelfTestedYES": 1,
				"CoupleDiscordantNO": 0,
				"CoupleDiscordantNR": 0,
				"CoupleDiscordantYES": 1,
				"SEXUALNO": 1,
				"SEXUALYES": 0,
				"SOCIALNO": 1,
				"SOCIALYES": 0,
				"NONENO": 1,
				"NONEYES": 0,
				"NEEDLE_SHARINGNO": 0,
				"NEEDLE_SHARINGYES": 1,
				"ReceivedPrEPNO": 0,
				"ReceivedPrEPYES": 1,
				"ReceivedPEPNO": 0,
				"ReceivedPEPYES": 1,
				"ReceivedTBNO": 1,
				"ReceivedTBYES": 0,
				"ReceivedSTINO": 0,
				"ReceivedSTIYES": 1,
				"GBVSexualNO": 1,
				"GBVSexualYES": 0,
				"GBVPhysicalNO": 1,
				"GBVPhysicalYES": 0,
				"GBVEmotionalNO": 1,
				"GBVEmotionalYES": 0,
				"dayofweekFRIDAY": 1,
				"dayofweekMONDAY": 0,
				"dayofweekSATURDAY": 0,
				"dayofweekSUNDAY": 0,
				"dayofweekTHURSDAY": 0,
				"dayofweekTUESDAY": 0,
				"dayofweekWEDNESDAY": 0
			}
		}
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/casefindingscore")
	@ResponseBody
	public Object processHTSModel(HttpServletRequest request) {
		ModelService modelService = new ModelService();
		String requestBody = null;
		try {
			requestBody = MLUtils.fetchRequestBody(request.getReader());
			ObjectNode modelConfigs = MLUtils.getModelConfig(requestBody);
			String facilityMflCode = modelConfigs.get(MLUtils.FACILITY_ID_REQUEST_VARIABLE).asText();
			boolean isDebugMode = modelConfigs.has("debug") && modelConfigs.get("debug").asText().equals("true") ? true
			        : false;
			
			if (facilityMflCode.equals("")) { // default to the default facility configured in the EMR
				facilityMflCode = MLUtils.getDefaultMflCode();
			}
			
			String modelId = modelConfigs.get(MLUtils.MODEL_ID_REQUEST_VARIABLE).asText();
			String encounterDate = modelConfigs.get(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText();
			
			if (StringUtils.isBlank(facilityMflCode) || StringUtils.isBlank(modelId) || StringUtils.isBlank(encounterDate)) {
				return new ResponseEntity<Object>("The service requires model, date, and facility information",
				        new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			JSONObject profile = MLUtils.getHTSFacilityProfile("FacilityCode", facilityMflCode, MLUtils.getHTSFacilityCutOffs());
			
			if (profile == null) {
				return new ResponseEntity<Object>(
				        "The facility provided currently doesn't have an HTS cut-off profile. Provide an appropriate facility",
				        new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			ModelInputFields inputFields = MLUtils.extractHTSCaseFindingVariablesFromRequestBody(requestBody, facilityMflCode,
			    encounterDate);

			// System.err.println("HTS Score: Using input fields: " + inputFields);
			
			ScoringResult scoringResult = modelService.htsscore(modelId, facilityMflCode, encounterDate, inputFields, isDebugMode);
			return scoringResult;
		}
		catch (IOException e) {
			return new ResponseEntity<Object>("Could not process the request", new HttpHeaders(),
			        HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	/**
	 * The IIT risk score POST request.
	 * ##### Sample Input Payload:

		{
			"modelConfigs": {
				"modelId": "XGB_IIT_02232023",
				"encounterDate": "2023-03-17",
				"facilityId": "14607",
				"debug": "true"
			},
			"variableValues": {
				"Age": 17,
				"births": 0,
				"pregnancies": 0,
				"literacy": 0,
				"poverty": 0,
				"anc": 0,
				"pnc": 0,
				"sba": 0,
				"hiv_prev": 0,
				"hiv_count": 0,
				"condom": 0,
				"intercourse": 0,
				"in_union": 0,
				"circumcision": 0,
				"partner_away": 0,
				"partner_men": 0,
				"partner_women": 0,
				"sti": 0,
				"fb": 0,
				"n_appts": 0,
				"missed1": 0,
				"missed5": 0,
				"missed30": 0,
				"missed1_last5": 0,
				"missed5_last5": 0,
				"missed30_last5": 0,
				"num_hiv_regimens": 0,
				"n_visits_lastfive": 0,
				"n_unscheduled_lastfive": 0,
				"BMI": 0,
				"changeInBMI": 0,
				"Weight": 0,
				"changeInWeight": 0,
				"num_adherence_ART": 0,
				"num_adherence_CTX": 0,
				"num_poor_ART": 0,
				"num_poor_CTX": 0,
				"num_fair_ART": 0,
				"num_fair_CTX": 0,
				"n_tests_all": 0,
				"n_hvl_all": 0,
				"n_tests_threeyears": 0,
				"n_hvl_threeyears": 0,
				"timeOnArt": 0,
				"AgeARTStart": 0,
				"recent_hvl_rate": 0,
				"total_hvl_rate": 0,
				"art_poor_adherence_rate": 0,
				"art_fair_adherence_rate": 0,
				"ctx_poor_adherence_rate": 0,
				"ctx_fair_adherence_rate": 0,
				"unscheduled_rate": 0,
				"all_late30_rate": 0,
				"all_late5_rate": 0,
				"all_late1_rate": 0,
				"recent_late30_rate": 0,
				"recent_late5_rate": 0,
				"recent_late1_rate": 0,
				"GenderFemale": 0,
				"GenderMale": 0,
				"PatientSourceCCC": 0,
				"PatientSourceIPDAdult": 0,
				"PatientSourceMCH": 0,
				"PatientSourceOPD": 0,
				"PatientSourceOther": 0,
				"PatientSourceTBClinic": 0,
				"PatientSourceVCT": 0,
				"MaritalStatusDivorced": 0,
				"MaritalStatusMarried": 0,
				"MaritalStatusOther": 0,
				"MaritalStatusPolygamous": 0,
				"MaritalStatusSingle": 0,
				"MaritalStatusWidow": 0,
				"PopulationTypeGeneralPopulation": 0,
				"PopulationTypeKeyPopulation": 0,
				"TreatmentTypeART": 0,
				"TreatmentTypePMTCT": 0,
				"OptimizedHIVRegimenNo": 0,
				"OptimizedHIVRegimenYes": 0,
				"Other_RegimenNo": 0,
				"Other_RegimenYes": 0,
				"PregnantNo": 0,
				"PregnantNR": 0,
				"PregnantYes": 0,
				"DifferentiatedCareCommunityARTDistributionHCWLed": 0,
				"DifferentiatedCareCommunityARTDistributionpeerled": 0,
				"DifferentiatedCareFacilityARTdistributionGroup": 0,
				"DifferentiatedCareFastTrack": 0,
				"DifferentiatedCareStandardCare": 0,
				"most_recent_art_adherencefair": 0,
				"most_recent_art_adherencegood": 0,
				"most_recent_art_adherencepoor": 0,
				"most_recent_ctx_adherencefair": 0,
				"most_recent_ctx_adherencegood": 0,
				"most_recent_ctx_adherencepoor": 0,
				"StabilityAssessmentStable": 0,
				"StabilityAssessmentUnstable": 0,
				"most_recent_vlHVL": 0,
				"most_recent_vlLVL": 0,
				"most_recent_vlSuppressed": 0
			}
		}
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/iitscore")
	@ResponseBody
	public Object processIITModel(HttpServletRequest request) {
		ModelService modelService = new ModelService();
		String requestBody = null;
		try {
			requestBody = MLUtils.fetchRequestBody(request.getReader());
			ObjectNode modelConfigs = MLUtils.getModelConfig(requestBody);
			String facilityMflCode = modelConfigs.get(MLUtils.FACILITY_ID_REQUEST_VARIABLE).asText();
			boolean isDebugMode = modelConfigs.has("debug") && modelConfigs.get("debug").asText().equals("true") ? true : false;

			if (facilityMflCode.equals("")) { // default to the default facility configured in the EMR
				facilityMflCode = MLUtils.getDefaultMflCode();
			}
			
			String modelId = modelConfigs.get(MLUtils.MODEL_ID_REQUEST_VARIABLE).asText();
			String encounterDate = modelConfigs.get(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText();
			
			if (StringUtils.isBlank(facilityMflCode) || StringUtils.isBlank(modelId) || StringUtils.isBlank(encounterDate)) {
				return new ResponseEntity<Object>("The service requires model, date, and facility information", new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			JSONObject profile = MLUtils.getHTSFacilityProfile("FacilityCode", facilityMflCode, MLUtils.getIITFacilityCutOffs());
			
			if (profile == null) {
				return new ResponseEntity<Object>("The facility provided currently doesn't have an HTS cut-off profile. Provide an appropriate facility", new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			ModelInputFields inputFields = MLUtils.extractIITVariablesFromRequestBody(requestBody, facilityMflCode, encounterDate);

			// System.err.println("IIT Score: Using input fields: " + inputFields);
			
			ScoringResult scoringResult = modelService.iitscore(modelId, facilityMflCode, encounterDate, inputFields, isDebugMode);
			return scoringResult;
		}
		catch (IOException e) {
			return new ResponseEntity<Object>("Could not process the IIT Score request", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	/**
	 * Generates a new risk score for the patient and saves it into DB
	 * //Query JSON
	 * {
	 * 		"patientId": 234
	 * }
	 * 
	 * @param patientId - The patient id
	 * @return the score JSON {"patientId": 234, "riskScore": 0.12345678, "riskDescription": "High Risk"}
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/updatepatientiitscore")
	@ResponseBody
	public Object getPatientIITRiskScore(HttpServletRequest request) {
		String requestBody = null;
		try {
			requestBody = MLUtils.fetchRequestBody(request.getReader());
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode payload = (ObjectNode) mapper.readTree(requestBody);
			Integer patientId = payload.get("patientId").asInt();
			System.out.println("IIT score got patient id as: " + patientId);
			PatientRiskScore latestRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatientRealTime(Context.getPatientService().getPatient(patientId));
			SimpleObject ret = new SimpleObject();
			ret.put("patientId", patientId);
			ret.put("riskScore", latestRiskScore.getRiskScore());
			ret.put("riskDescription", latestRiskScore.getDescription());
			System.out.println("IIT score got patient risk score as: " + latestRiskScore.getRiskScore());
			return ret;
		} catch(Exception ex) {
			return new ResponseEntity<Object>("Could not process the patient IIT Score request", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
