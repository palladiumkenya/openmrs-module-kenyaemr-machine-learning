package org.openmrs.module.kenyaemrml.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.jfree.data.time.Month;
import org.json.simple.JSONObject;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.AppointmentDaysMissedDataDefinition;
import org.openmrs.module.kenyaemrml.api.MLUtils;
import org.openmrs.module.kenyaemrml.api.ModelService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.kenyaemrml.iit.Treatment;
//import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.openmrs.module.kenyaemrml.iit.Appointment;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.CallableStatement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import org.codehaus.jackson.map.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		ModelService modelService = Context.getService(ModelService.class);
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
				        "Your facility currently does not support ML. Kindly contact SUPPORT to include your facility code ( "+ facilityMflCode +" ) in the ML Matrix.",
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
				"modelId": "XGB_IIT_01042024",
				"encounterDate": "2023-12-17",
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
		ModelService modelService = Context.getService(ModelService.class);
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
				return new ResponseEntity<Object>("Your facility currently does not support ML. Kindly contact SUPPORT to include your facility code ( " + facilityMflCode + " ) in the ML Matrix.", new HttpHeaders(), HttpStatus.BAD_REQUEST);
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

	@RequestMapping(method = RequestMethod.POST, value = "/testiit")
	@ResponseBody
	public Object variableTest(HttpServletRequest request) {
		try {
			// List<Integer> patients = Arrays.asList(6559, 9261, 9800, 9895, 10705, 15219, 15361, 15723, 16712, 16856, 34089); // Bomu DB
			List<Integer> patients = Arrays.asList(27, 28, 30, 31, 32, 34, 35, 38, 39, 42, 44, 45, 49, 51, 52); // Mukuyuni DB
			// List<Integer> patients = Arrays.asList(3980, 3979, 3978, 3977, 3976, 3975, 3974, 3973, 3972, 3971, 3970, 3969, 3968, 3967, 3966, 3965, 3964, 3963, 3962, 3961); // Mukuyuni DB
			for(Integer patientID : patients) {
				try {
					// REF: https://github.com/palladiumkenya/DWAPI-Queries/blob/dwapi-etl/DWAPI_New_Extracts/Patient.sql
					// REF: https://raw.githubusercontent.com/palladiumkenya/DWAPI-Queries/dwapi-etl/DWAPI_New_Extracts/PatientsVisit.sql
					// REF: https://raw.githubusercontent.com/palladiumkenya/DWAPI-Queries/dwapi-etl/DWAPI_New_Extracts/PatientPharmacy.sql
					// REF: https://raw.githubusercontent.com/palladiumkenya/DWAPI-Queries/dwapi-etl/DWAPI_New_Extracts/PatientsLab.sql
					// REF: https://raw.githubusercontent.com/palladiumkenya/DWAPI-Queries/dwapi-etl/DWAPI_New_Extracts/PatientArt.sql
					// REF: https://github.com/palladiumkenya/DWAPI-Queries/blob/master/DWAPI_New_Extracts/patientbaselines.sql
					// Patient 9895
					long startTime = System.currentTimeMillis();
					long stopTime = 0L;
					long startMemory = getMemoryConsumption();
					long stopMemory = 0L;
					AdministrationService administrationService = Context.getAdministrationService();
					System.err.println("*************************************************************************");
					System.err.println("IIT ML: Start IIT ML TEST: " + new Date());
					//Start Appointments
					// administrationService.executeSQL("CALL sp_populate_dwapi_patient_demographics()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_hiv_followup()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_patient_triage()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_drug_event()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_pharmacy_extract()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_drug_event()", false);
					// administrationService.executeSQL("CALL sp_populate_dwapi_drug_order()", false);

					String visitsQuery = "CALL sp_iitml_get_visits(" + patientID + ")";

					String pharmacyQuery = "CALL sp_iitml_get_pharmacy_visits(" + patientID + ")";

					String demographicsQuery = "CALL sp_iitml_get_patient_demographics(" + patientID + ")";

					String labQuery = "CALL sp_iitml_get_patient_lab(" + patientID + ")";

					String artQuery = "CALL sp_iitml_get_patient_ART(" + patientID + ")";

					String lastETLUpdateQuery = "CALL sp_iitml_get_last_dwapi_etl_update()";

					String cd4Query = "CALL sp_iitml_get_patient_CD4count(" + patientID + ")";

					List<List<Object>> visits = administrationService
							.executeSQL(visitsQuery, true); // PatientPK(0), VisitDate(1), NextAppointmentDate(2), VisitType(3), Height(4), Weight(5),
							// Pregnant(6), DiffentiatedCare(7), StabilityAssessment(8), Adherence(9), WhoStage(10), BreastFeeding(11)
					List<List<Object>> pharmacy = administrationService
							.executeSQL(pharmacyQuery,
									true); // PatientPK(0), DispenseDate(1), ExpectedReturn(2), Drug(3), TreatmentType(4)
					List<List<Object>> demographics = administrationService
							.executeSQL(demographicsQuery,
									true); // PatientPK(0), Gender, PatientSource, MaritalStatus, Age, PopulationType
					List<List<Object>> lab = administrationService
							.executeSQL(labQuery,
									true); // PatientPK(0), ReportedByDate, TestResult, TestName
					List<List<Object>> art = administrationService
							.executeSQL(artQuery,
									true); // PatientPK(0), StartARTDate
					List<List<Object>> lastETLUpdate = administrationService
							.executeSQL(lastETLUpdateQuery,
									true); // INDICATOR_NAME(0), INDICATOR_VALUE(1), INDICATOR_MONTH(2)
					List<List<Object>> cd4Counter = administrationService
							.executeSQL(cd4Query,
									true); // PatientPK(0), lastcd4(1)

					System.err.println("IIT ML: Got visits: " + visits.size());
					System.err.println("IIT ML: Got pharmacy: " + pharmacy.size());
					System.err.println("IIT ML: Got demographics: " + demographics.size());
					System.err.println("IIT ML: Got lab: " + lab.size());
					System.err.println("IIT ML: Got ART: " + art.size());
					System.err.println("IIT ML: Got DWAPI ETL last update: " + lastETLUpdate.size());
					System.err.println("IIT ML: Got Last CD4 count: " + cd4Counter.size());

					// January 2019 reference date
					Date jan2019 = new Date(119, 0, 1);
					Date now = new Date();

					// Last ETL Update
					String lastETLUpdateSt = getLastETLUpdate(lastETLUpdate);
					System.err.println("IIT ML: Last time the ETL was updated (lastETLUpdateSt): " + lastETLUpdateSt);

					// Now that we have visits and pharmacy we can filter the data and apply logic

					// Lateness Section
					//Visits
					Set<Appointment> visitAppts = new HashSet<>();
					for (List<Object> ls : visits) {
						// If NextAppointmentDate is null, dispose it
						if (ls.get(0) != null && ls.get(1) != null && ls.get(2) != null) {
							if (ls.get(0) instanceof Integer && ls.get(1) instanceof Date && ls.get(2) instanceof Date) {
								// check that the date is after jan 2019
								if (((Date) ls.get(1)).after(jan2019) && ((Date) ls.get(2)).after(jan2019)) {
									// check that appointment is less than 365 days from encounter date
									// Calculate the difference in milliseconds
									long differenceInMillis = ((Date) ls.get(2)).getTime() - ((Date) ls.get(1)).getTime();
									// Convert milliseconds to days
									long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);
									if (differenceInDays < 365) {
										// Ensure appointment day is after encounter day
										if (((Date) ls.get(2)).after(((Date) ls.get(1)))) {
											Appointment visit = new Appointment();
											visit.setPatientID((Integer) ls.get(0));
											visit.setEncounterDate((Date) ls.get(1));
											visit.setAppointmentDate((Date) ls.get(2));
											visitAppts.add(visit);
										} else {
											System.err
													.println("IIT ML: appointment before encounter record rejected: " + ls);
										}
									} else {
										System.err.println("IIT ML: 365 days record rejected: " + ls);
									}
								} else {
									System.err.println("IIT ML: 2019 record rejected: " + ls);
								}
							}
						}
					}
					System.err.println("IIT ML: visits before: " + visitAppts.size());
					processRecords(visitAppts);

					//Pharmacy
					Set<Appointment> pharmAppts = new HashSet<>();
					for (List<Object> ls : pharmacy) {
						// patientid and encounter date should never be null
						if (ls.get(0) != null && ls.get(1) != null && ls.get(2) != null) {
							// if appointment date is null set a new date 30 days after encounter
							if (ls.get(2) == null) {
								// Create a Calendar instance
								Calendar calendar = Calendar.getInstance();
								calendar.setTime((Date) ls.get(1));
								// Add 30 days
								calendar.add(Calendar.DAY_OF_MONTH, 30);
								// Get the new Date
								Date newAppt = calendar.getTime();
								ls.set(2, newAppt);
							}
							if (ls.get(0) instanceof Integer && ls.get(1) instanceof Date && ls.get(2) instanceof Date) {
								// check that the date is after jan 2019
								if (((Date) ls.get(1)).after(jan2019) && ((Date) ls.get(2)).after(jan2019)) {
									// check that appointment is less than 365 days from today
									// Calculate the difference in milliseconds
									long differenceInMillis = ((Date) ls.get(2)).getTime() - ((Date) ls.get(1)).getTime();
									// Convert milliseconds to days
									long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);
									if (differenceInDays >= 365) {
										// Create a Calendar instance
										Calendar calendar = Calendar.getInstance();
										calendar.setTime((Date) ls.get(1));
										// Add 30 days
										calendar.add(Calendar.DAY_OF_MONTH, 30);
										// Get the new Date
										Date newAppt = calendar.getTime();
										ls.set(2, newAppt);
									}
									// If appointment day is before encounter day, set appointment to be after 30 days
									if (((Date) ls.get(1)).after(((Date) ls.get(2)))) {
										// Create a Calendar instance
										Calendar calendar = Calendar.getInstance();
										calendar.setTime((Date) ls.get(1));
										// Add 30 days
										calendar.add(Calendar.DAY_OF_MONTH, 30);
										// Get the new Date
										Date newAppt = calendar.getTime();
										ls.set(2, newAppt);
									}

									Appointment visit = new Appointment();
									visit.setPatientID((Integer) ls.get(0));
									visit.setEncounterDate((Date) ls.get(1));
									visit.setAppointmentDate((Date) ls.get(2));
									pharmAppts.add(visit);

								}
							}
						}
					}
					System.err.println("IIT ML: pharmacy before: " + pharmAppts.size());
					processRecords(pharmAppts);

					System.err.println("IIT ML: Got Filtered visits: " + visitAppts.size());
					System.err.println("IIT ML: Got Filtered pharmacy: " + pharmAppts.size());

					//Combine the two sets
					Set<Appointment> allAppts = new HashSet<>();
					allAppts.addAll(visitAppts);
					allAppts.addAll(pharmAppts);
					System.err.println("IIT ML: Prepared appointments before: " + allAppts.size());
					processRecords(allAppts);

					// New model (n_appts)
					System.err.println("IIT ML: Final appointments (n_appts): " + allAppts.size());

					List<Appointment> sortedVisits = sortAppointmentsByEncounterDate(visitAppts);
					List<Appointment> sortedRecords = sortAppointmentsByEncounterDate(allAppts);
					List<Integer> missedRecord = calculateLateness(sortedRecords);

					System.err.println("IIT ML: Appointments: " + sortedRecords);
					System.err.println("IIT ML: Missed before: " + missedRecord);

					Integer missed1 = getMissed1(missedRecord);
					System.err.println("IIT ML: Missed by at least one (missed1): " + missed1);

					Integer missed5 = getMissed5(missedRecord);
					System.err.println("IIT ML: Missed by at least five (missed5): " + missed5);

					Integer missed30 = getMissed30(missedRecord);
					System.err.println("IIT ML: Missed by at least thirty (missed30): " + missed30);

					Integer missed1Last5 = getMissed1Last5(missedRecord);
					System.err.println(
							"IIT ML: Missed by at least one in the latest 5 appointments (missed1_Last5): " + missed1Last5);

					Integer missed5Last5 = getMissed5Last5(missedRecord);
					System.err.println(
							"IIT ML: Missed by at least five in the latest 5 appointments (missed5_Last5): " + missed5Last5);

					Integer missed30Last5 = getMissed30Last5(missedRecord);
					System.err.println(
							"IIT ML: Missed by at least thirty in the latest 5 appointments (missed30_Last5): "
									+ missed30Last5);

					/**
					 * New Model 13/12/2023
					 * Based on the Visits table only
					 * late
					 * late28
					 * average_lateness
					 * late_rate
					 * late28_rate
					 * visit_1
					 * visit_2
					 * visit_3
					 * visit_4
					 * visit_5
					 * late_last10
					 * NextAppointmentDate
					 * late_last3
					 * averagelateness_last3
					 * averagelateness_last10
					 * late_last5
					 * averagelateness_last5
					 * average_tca_last5
					 */

					// New model (late)
					System.err.println("IIT ML: new model (late): " + missed1);

					// New model (late28)
					Integer late28 = getLate28(missedRecord);
					System.err.println("IIT ML: new model (late28): " + late28);

					// New model (averagelateness)
					System.err.println(
							"IIT ML: new model (averagelateness): " + getAverageLateness(missedRecord, allAppts.size()));

					// New model (late_rate)
					System.err.println("IIT ML: new model (late_rate): " + getLateRate(missed1, allAppts.size()));

					// New model (late28_rate)
					System.err.println("IIT ML: new model (late_rate): " + getLate28Rate(late28, allAppts.size()));

					// New model (visit_1)
					System.err.println("IIT ML: new model (visit_1): " + getVisit1(missedRecord));

					// New model (visit_2)
					System.err.println("IIT ML: new model (visit_2): " + getVisit2(missedRecord));

					// New model (visit_3)
					System.err.println("IIT ML: new model (visit_3): " + getVisit3(missedRecord));

					// New model (visit_4)
					System.err.println("IIT ML: new model (visit_4): " + getVisit4(missedRecord));

					// New model (visit_5)
					System.err.println("IIT ML: new model (visit_5): " + getVisit5(missedRecord));

					// New model (late_last10)
					System.err.println("IIT ML: new model (late_last10): " + getLateLast10(missedRecord));

					// New model (NextAppointmentDate)
					System.err.println("IIT ML: new model (NextAppointmentDate): " + getNextAppointmentDate(sortedRecords));

					// New model (late_last3)
					System.err.println("IIT ML: new model (late_last3): " + getLateLast3(missedRecord));

					// New model (averagelateness_last3)
					System.err
							.println("IIT ML: new model (averagelateness_last3): " + getAverageLatenessLast3(missedRecord));

					// New model (averagelateness_last10)
					System.err.println(
							"IIT ML: new model (averagelateness_last10): " + getAverageLatenessLast10(missedRecord));

					// New model (late_last5)
					System.err.println("IIT ML: new model (late_last5): " + getLateLast5(missedRecord));

					// New model (averagelateness_last5)
					System.err
							.println("IIT ML: new model (averagelateness_last5): " + getAverageLatenessLast5(missedRecord));

					// New model (average_tca_last5)
					System.err.println("IIT ML: new model (average_tca_last5): " + getAverageTCALast5(sortedRecords));

					// New model (unscheduled_rate)
					System.err.println("IIT ML: new model (unscheduled_rate): " + getUnscheduledRate(visits));

					// New model (unscheduled_rate_last5)
					System.err.println("IIT ML: new model (unscheduled_rate_last5): " + getUnscheduledRateLast5(visits));

					// New model (MonthApr)
					System.err.println("IIT ML: new model (MonthApr): " + getMonthApr(sortedRecords));

					// New model (MonthAug)
					System.err.println("IIT ML: new model (MonthAug): " + getMonthAug(sortedRecords));

					// New model (MonthDec)
					System.err.println("IIT ML: new model (MonthDec): " + getMonthDec(sortedRecords));

					// New model (MonthFeb)
					System.err.println("IIT ML: new model (MonthFeb): " + getMonthFeb(sortedRecords));

					// New model (MonthJan)
					System.err.println("IIT ML: new model (MonthJan): " + getMonthJan(sortedRecords));

					// New model (MonthJul)
					System.err.println("IIT ML: new model (MonthJul): " + getMonthJul(sortedRecords));

					// New model (MonthJun)
					System.err.println("IIT ML: new model (MonthJun): " + getMonthJun(sortedRecords));

					// New model (MonthMar)
					System.err.println("IIT ML: new model (MonthMar): " + getMonthMar(sortedRecords));

					// New model (MonthMay)
					System.err.println("IIT ML: new model (MonthMay): " + getMonthMay(sortedRecords));

					// New model (MonthNov)
					System.err.println("IIT ML: new model (MonthNov): " + getMonthNov(sortedRecords));

					// New model (MonthOct)
					System.err.println("IIT ML: new model (MonthOct): " + getMonthOct(sortedRecords));

					// New model (MonthSep)
					System.err.println("IIT ML: new model (MonthSep): " + getMonthSep(sortedRecords));

					// New model (DayFri)
					System.err.println("IIT ML: new model (DayFri): " + getDayFri(sortedRecords));

					// New model (DayMon)
					System.err.println("IIT ML: new model (DayMon): " + getDayMon(sortedRecords));

					// New model (DaySat)
					System.err.println("IIT ML: new model (DaySat): " + getDaySat(sortedRecords));

					// New model (DaySun)
					System.err.println("IIT ML: new model (DaySun): " + getDaySun(sortedRecords));

					// New model (DayThu)
					System.err.println("IIT ML: new model (DayThu): " + getDayThu(sortedRecords));

					// New model (DayTue)
					System.err.println("IIT ML: new model (DayTue): " + getDayTue(sortedRecords));

					// New model (DayWed)
					System.err.println("IIT ML: new model (DayWed): " + getDayWed(sortedRecords));

					// End new model

					// (Weight)
					Double weight = getWeight(visits);
					System.err.println("IIT ML: (Weight): " + weight);

					// (Height)
					Double height = getHeight(visits);
					System.err.println("IIT ML: (Height): " + height);

					// (BMI) -- NB: If zero, return NA
					System.err.println("IIT ML: (BMI): " + getBMI(height, weight));

					// Gender (Male == 1 and Female == 2)
					Integer patientGender = 0;
					Integer female = getGenderFemale(demographics);
					Integer male = getGenderMale(demographics);
					if(male == 1) {
						patientGender = 1;
					}
					if(female == 1) {
						patientGender = 2;
					}

					// (GenderFemale)
					System.err.println("IIT ML: (GenderFemale): " + female);

					// (GenderMale)
					System.err.println("IIT ML: (GenderMale): " + male);

					// (PatientSourceOPD)
					System.err.println("IIT ML: (PatientSourceOPD): " + getPatientSourceOPD(demographics));

					// (PatientSourceOther)
					System.err.println("IIT ML: (PatientSourceOther): " + getPatientSourceOther(demographics));

					// (PatientSourceVCT)
					System.err.println("IIT ML: (PatientSourceVCT): " + getPatientSourceVCT(demographics));

					// (Age)
					Long Age = getAgeYears(demographics);
					System.err.println("IIT ML: (Age): " + Age);

					// (MaritalStatusDivorced)
					System.err.println("IIT ML: (MaritalStatusDivorced): " + getMaritalStatusDivorced(demographics, Age));

					// (MaritalStatusMarried)
					System.err.println("IIT ML: (MaritalStatusMarried): " + getMaritalStatusMarried(demographics, Age));

					// (MaritalStatusMinor)
					System.err.println("IIT ML: (MaritalStatusMinor): " + getMaritalStatusMinor(Age));

					// (MaritalStatusOther)
					System.err.println("IIT ML: (MaritalStatusOther): " + getMaritalStatusOther(demographics, Age));

					// (MaritalStatusPolygamous)
					System.err.println("IIT ML: (MaritalStatusPolygamous): " + getMaritalStatusPolygamous(demographics, Age));

					// (MaritalStatusSingle)
					System.err.println("IIT ML: (MaritalStatusSingle): " + getMaritalStatusSingle(demographics, Age));

					// (MaritalStatusWidow)
					System.err.println("IIT ML: (MaritalStatusWidow): " + getMaritalStatusWidow(demographics, Age));

					// Standard Care, Fast Track,
					// (DifferentiatedCarecommunityartdistributionhcwled)
					System.err.println("IIT ML: (DifferentiatedCarecommunityartdistributionhcwled): " + getDifferentiatedCarecommunityartdistributionhcwled(visits));

					// (DifferentiatedCarecommunityartdistributionpeerled)
					System.err.println("IIT ML: (DifferentiatedCarecommunityartdistributionpeerled): " + getDifferentiatedCarecommunityartdistributionpeerled(visits));

					// (DifferentiatedCareexpress)
					System.err.println("IIT ML: (DifferentiatedCareexpress): " + getDifferentiatedCareexpress(visits));

					// (DifferentiatedCarefacilityartdistributiongroup)
					System.err.println("IIT ML: (DifferentiatedCarefacilityartdistributiongroup): " + getDifferentiatedCarefacilityartdistributiongroup(visits));

					// (DifferentiatedCarefasttrack)
					System.err.println("IIT ML: (DifferentiatedCarefasttrack): " + getDifferentiatedCarefasttrack(visits));

					// (DifferentiatedCarestandardcare)
					System.err.println("IIT ML: (DifferentiatedCarestandardcare): " + getDifferentiatedCarestandardcare(visits));

					// (StabilityAssessmentStable)
					System.err.println("IIT ML: (DifferentiatedCarefasttrack): " + getStabilityAssessmentStable(visits));

					// (StabilityAssessmentUnstable)
					System.err.println("IIT ML: (DifferentiatedCarefasttrack): " + getStabilityAssessmentUnstable(visits));

					// (most_recent_art_adherencefair)
					System.err.println("IIT ML: (most_recent_art_adherencefair): " + getMostRecentArtAdherenceFair(visits));

					// (most_recent_art_adherencegood)
					System.err.println("IIT ML: (most_recent_art_adherencegood): " + getMostRecentArtAdherenceGood(visits));

					// (most_recent_art_adherencepoor)
					System.err.println("IIT ML: (most_recent_art_adherencepoor): " + getMostRecentArtAdherencePoor(visits));

					// (Pregnantno)
					System.err.println("IIT ML: (Pregnantno): " + getPregnantNo(visits, patientGender, Age).get("result"));

					// (PregnantNR)
					System.err.println("IIT ML: (PregnantNR): " + getPregnantNR(patientGender, Age));

					// (Pregnantyes)
					System.err.println("IIT ML: (Pregnantyes): " + getPregnantYes(visits, patientGender, Age).get("result"));

					// (Breastfeedingno)
					System.err.println("IIT ML: (Breastfeedingno): " + getBreastFeedingNo(visits, patientGender, Age).get("result"));

					// (BreastfeedingNR)
					System.err.println("IIT ML: (BreastfeedingNR): " + getBreastFeedingNR(patientGender, Age));

					// (Breastfeedingyes)
					System.err.println("IIT ML: (Breastfeedingyes): " + getBreastFeedingYes(visits, patientGender, Age).get("result"));

					// (PopulationTypeGP)
					System.err.println("IIT ML: (PopulationTypeGP): " + getPopulationTypeGP(demographics));

					// (PopulationTypeKP)
					System.err.println("IIT ML: (PopulationTypeKP): " + getPopulationTypeKP(demographics));

					// (AHDNo)
					System.err.println("IIT ML: (AHDNo): " + getAHDNo(visits, cd4Counter, Age).get("result"));

					// (AHDYes)
					System.err.println("IIT ML: (AHDYes): " + getAHDYes(visits, cd4Counter, Age).get("result"));

					// (OptimizedHIVRegimenNo)
					System.err.println("IIT ML: (OptimizedHIVRegimenNo): " + getOptimizedHIVRegimenNo(pharmacy).get("result"));

					// (OptimizedHIVRegimenYes)
					System.err.println("IIT ML: (OptimizedHIVRegimenYes): " + getOptimizedHIVRegimenYes(pharmacy).get("result"));

					// NB: Any number equal or above 200 is considered High Viral Load (HVL). Any below is LDL or suppressed or Low Viral Load (LVL)
					// (most_recent_vlsuppressed)
					System.err.println("IIT ML: (most_recent_vlsuppressed): " + getMostRecentVLsuppressed(lab));

					// (most_recent_vlunsuppressed)
					System.err.println("IIT ML: (most_recent_vlunsuppressed): " + getMostRecentVLunsuppressed(lab));

					// (n_tests_threeyears)
					Integer n_tests_threeyears = getNtestsThreeYears(lab);
					System.err.println("IIT ML: (n_tests_threeyears): " + n_tests_threeyears);

					// (n_hvl_threeyears)
					Integer n_hvl_threeyears = getNHVLThreeYears(lab);
					System.err.println("IIT ML: (n_hvl_threeyears): " + n_hvl_threeyears);

					// (n_lvl_threeyears)
					System.err.println("IIT ML: (n_lvl_threeyears): " + getNLVLThreeYears(lab));

					// (recent_hvl_rate)
					System.err.println("IIT ML: (recent_hvl_rate): " + getRecentHvlRate(n_hvl_threeyears, n_tests_threeyears));

					// (timeOnArt)
					System.err.println("IIT ML: (timeOnArt): " + getTimeOnArt(art));

					// Treatment Section
					//Pharmacy
					Set<Treatment> pharmTreatment = new HashSet<>();
					// PatientPK(0), DispenseDate(1), ExpectedReturn(2), Drug(3), TreatmentType(4)
					for (List<Object> ls : pharmacy) {
						// Limit to last 400 days
						Date dispenseDate = (Date) ls.get(1);
						// Get the difference in days
						long differenceInMilliseconds = now.getTime() - dispenseDate.getTime();
						int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
						if (differenceInDays < 400) {
							Treatment newTreatment = new Treatment();
							newTreatment.setPatientID((Integer) ls.get(0));
							newTreatment.setEncounterDate(dispenseDate);
							newTreatment.setDrug((String) ls.get(3));
							newTreatment.setTreatmentType((String) ls.get(4));
							pharmTreatment.add(newTreatment);
						}
					}

					System.err.println(
							"IIT ML: Total number of regimens - nonfiltered (last 400 days): " + pharmTreatment.size());

					// (num_hiv_regimens) -- Note: If zero, we show NA
					System.err.println("IIT ML: (num_hiv_regimens): " + getNumHivRegimens(pharmTreatment).get("result"));

					//End Appointments
					System.err.println("IIT ML: END IIT ML TEST: " + new Date());

					stopTime = System.currentTimeMillis();
					long elapsedTime = stopTime - startTime;
					System.out.println("IIT ML: Time taken: " + elapsedTime);
					System.out.println("IIT ML: Time taken sec: " + elapsedTime / 1000);

					stopMemory = getMemoryConsumption();
					long usedMemory = stopMemory - startMemory;
					System.out.println("IIT ML: Memory used: " + usedMemory);

					// We got the final table
					System.err.println("IIT ML: Table: " + visitAppts);

					// late: Total number of times late to visit - Visit table only

					System.err.println("*************************************************************************");
				} catch (Exception ex) {
					System.err.println("IIT ML ERROR: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
			return(new SimpleObject());
		}
		catch (Exception e) {
			System.err.println("IIT ML ERROR: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<Object>("Could not process the IIT Test", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	/**
	 * Returns the last time the DWAPI ETL was updated
	 * @return String -- Date or Error
	 */
	private String getLastETLUpdate(List<List<Object>> updateData){
		String ret = "Never Updated";
		try {
			if (updateData != null) {
				// Get the last record
				if (updateData.size() > 0) {
					List<Object> updateObject = updateData.get(updateData.size() - 1);
					if (updateObject.get(1) != null) {
						Date updateDate = (Date) updateObject.get(1);
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						ret = sdf.format(updateDate);
					}
				}
			}
		} catch(Exception ex) {
			System.err.println("IIT ML: Error getting the last time the DWAPI ETL was updated");
			ex.printStackTrace();
		}
		return(ret);
	}

	private Double getUnscheduledRate(List<List<Object>> visits) {
		Double ret = 0.00;
		if(visits != null && visits.size() > 0) {
			Integer addition = 0;
			Integer divider = Math.max(visits.size(), 1); // Avoid divide by zero

			for (List<Object> in : visits) {
				String visitType = (String)in.get(3);
				if(visitType != null && visitType.trim().equalsIgnoreCase("unscheduled")) {
					addition++;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Long getTimeOnArt(List<List<Object>> artRecord) {
		Long ret = 0L;
		if(artRecord != null) {
			// Get the last record
			if (artRecord.size() > 0) {
				List<Object> visitObject = artRecord.get(artRecord.size() - 1);
				if (visitObject.get(1) != null) {
					Date artStartDate = (Date) visitObject.get(1);
					Date now = new Date();
					// Instant artInstant = artStartDate.toInstant();
					// Instant nowInstant = now.toInstant();
					// Get the age in years
					// Duration duration = Duration.between(nowInstant, dobInstant);
					// long years = duration.toDays() / 365;
					LocalDate artLocal = dateToLocalDate(artStartDate);
					LocalDate nowLocal = dateToLocalDate(now);
					long months = Math.abs(ChronoUnit.MONTHS.between(nowLocal, artLocal));
					ret = months;
				}
			}
		}
		return(ret);
	}

	private SimpleObject getAHDNo(List<List<Object>> visits, List<List<Object>> cd4count, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(visits != null && cd4count != null) {
			// Get the last visit
			if (visits.size() > 0 && cd4count.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				List<Object> cd4Object = cd4count.get(cd4count.size() - 1);
				// If WHO Stage in (1,2) or CD4 COUNT is greater than 200  and Age six and above, then 1,
				// if Age five or below or WHO stage in (3,4) or CD4 COUNT is less than or equal to 200, then 0, if Age over 6 and WHO Stage is NULL, then NA
				if (visitObject.get(10) != null && cd4Object.get(1) != null) {
					String whoStage = (String) visitObject.get(10);
					String patientCD4st = (String) cd4Object.get(1);
					patientCD4st = patientCD4st.trim().toLowerCase();
					whoStage = whoStage.trim().toLowerCase();
					Integer whoStageInt = getIntegerValue(whoStage);
					Integer patientCD4 = getIntegerValue(patientCD4st);
					if((((whoStageInt == 1 || whoStageInt == 2)) || patientCD4 > 200)  && Age >= 6) {
						ret.put("result", 1);
						return(ret);
					}
					if((((whoStageInt == 3 || whoStageInt == 4)) || patientCD4 <= 200) && Age <= 5) {
						ret.put("result", 0);
						return(ret);
					}
				}
			}
		}
		return(ret);
	}

	private SimpleObject getAHDYes(List<List<Object>> visits, List<List<Object>> cd4count, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(visits != null && cd4count != null) {
			// Get the last visit
			if (visits.size() > 0 && cd4count.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				List<Object> cd4Object = cd4count.get(cd4count.size() - 1);
				// If WHO Stage in (3,4) or Age five or below or CD4 Count is less than or equal to 200, then 1,
				// if Age is six or over and WHO stage in (1,2) or CD4count is greater than 200, then 0, if Age 6 or over and WHO Stage is NULL, then NA
				if (visitObject.get(10) != null && cd4Object.get(1) != null) {
					String whoStage = (String) visitObject.get(10);
					String patientCD4st = (String) cd4Object.get(1);
					patientCD4st = patientCD4st.trim().toLowerCase();
					whoStage = whoStage.trim().toLowerCase();
					Integer whoStageInt = getIntegerValue(whoStage);
					Integer patientCD4 = getIntegerValue(patientCD4st);
					if((((whoStageInt == 3 || whoStageInt == 4)) || patientCD4 <= 200) && Age <= 5) {
						ret.put("result", 1);
						return(ret);
					}
					if((((whoStageInt == 1 || whoStageInt == 2)) || patientCD4 > 200) && Age >= 6) {
						ret.put("result", 0);
						return(ret);
					}
				}
			}
		}
		return(ret);
	}

	private Double getRecentHvlRate(Integer n_hvl_threeyears,Integer n_test_threeyears) {
		Double ret = 0.00;

		try {
			if (n_test_threeyears != 0) {
				ret = (n_hvl_threeyears * 1.00) / (n_test_threeyears * 1.00);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return(ret);
	}

	private SimpleObject getOptimizedHIVRegimenNo(List<List<Object>> pharmacy) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		// NB: limit to last 400 days
		if(pharmacy != null) {
			if (pharmacy.size() > 0) {
				// The last record
				List<Object> pharmacyObject = pharmacy.get(pharmacy.size() - 1);
				Date now = new Date();
				Date dispenseDate = (Date) pharmacyObject.get(1);
				// Get the difference in days
				long differenceInMilliseconds = now.getTime() - dispenseDate.getTime();
				int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
				if (differenceInDays < 400) {
					// TreatmentType != NULL or Prophylaxis, Drug != NULL
					if (pharmacyObject.get(4) != null && pharmacyObject.get(3) != null) {
						String treatment = (String) pharmacyObject.get(4);
						if (!treatment.trim().equalsIgnoreCase("Prophylaxis")) {
							// Get drug name
							String drugName = (String) pharmacyObject.get(3);
							drugName = drugName.toLowerCase();
							if (drugName.contains("dtg")) {
								ret.put("result", 0);
							} else {
								ret.put("result", 1);
							}
						}
					}
				} else {
					ret.put("result", "NA");
				}
			}
		}
		return(ret);
	}

	private SimpleObject getOptimizedHIVRegimenYes(List<List<Object>> pharmacy) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		// NB: limit to last 400 days
		if(pharmacy != null) {
			if (pharmacy.size() > 0) {
				// The last record
				List<Object> pharmacyObject = pharmacy.get(pharmacy.size() - 1);
				Date now = new Date();
				Date dispenseDate = (Date) pharmacyObject.get(1);
				// Get the difference in days
				long differenceInMilliseconds = now.getTime() - dispenseDate.getTime();
				int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
				if (differenceInDays < 400) {
					// TreatmentType != NULL or Prophylaxis, Drug != NULL
					if (pharmacyObject.get(4) != null && pharmacyObject.get(3) != null) {
						String treatment = (String) pharmacyObject.get(4);
						if(!treatment.trim().equalsIgnoreCase("Prophylaxis")) {
							// Get drug name
							String drugName = (String) pharmacyObject.get(3);
							drugName = drugName.toLowerCase();
							if (drugName.contains("dtg")) {
								ret.put("result", 1);
							} else {
								ret.put("result", 0);
							}
						}
					}
				} else {
					ret.put("result", "NA");
				}
			}
		}
		return(ret);
	}

	private Integer getMostRecentVLsuppressed(List<List<Object>> lab) {
		Integer ret = 0;
		if(lab != null) {
			if (lab.size() > 0) {
				// The last record
				List<Object> labObject = lab.get(lab.size() - 1);
				if (labObject.get(2) != null) {
					String result = (String) labObject.get(2);
					if(getIntegerValue(result.trim()) < 200 || result.trim().equalsIgnoreCase("LDL")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMostRecentVLunsuppressed(List<List<Object>> lab) {
		Integer ret = 0;
		if(lab != null) {
			if (lab.size() > 0) {
				// The last record
				List<Object> labObject = lab.get(lab.size() - 1);
				if (labObject.get(2) != null) {
					String result = (String) labObject.get(2);
					if(getIntegerValue(result.trim()) >= 200) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getNtestsThreeYears(List<List<Object>> lab) {
		Integer ret = 0;
		if(lab != null) {
			// Get for the last 3 years
			if (lab.size() > 0) {
				// Reverse the list to loop from the first
				List<List<Object>> labRev = new ArrayList<>(lab);
				Collections.reverse(labRev);
				// Count number of tests for the last 3 years
				Date now = new Date();
				LocalDate nowLocal = dateToLocalDate(now);
				// A unique test is given by unique order date
				Set<Date> uniqueTests = new HashSet<>();
				for(List<Object> labObject: labRev) {
					if (labObject.get(1) != null && labObject.get(3) != null && labObject.get(4) != null) {
						Date orderDate = (Date) labObject.get(4);
						Date resultDate = (Date) labObject.get(1);
						String testName = (String) labObject.get(3);
						if(!uniqueTests.contains(orderDate) && testName.trim().equalsIgnoreCase("hiv viral load")) {
							LocalDate resultLocal = dateToLocalDate(resultDate);
							long months = Math.abs(ChronoUnit.MONTHS.between(nowLocal, resultLocal));
							if (months <= 36) {
								ret++;
							}
						}
						uniqueTests.add(orderDate);
					}
				}
			}
		}
		return(ret);
	}

	private Integer getNHVLThreeYears(List<List<Object>> lab) {
		Integer ret = 0;
		if(lab != null) {
			// Get for the last 3 years
			if (lab.size() > 0) {
				// Reverse the list to loop from the first
				List<List<Object>> labRev = new ArrayList<>(lab);
				Collections.reverse(labRev);
				// Count number of tests for the last 3 years
				Date now = new Date();
				LocalDate nowLocal = dateToLocalDate(now);
				// A unique test is given by unique order date
				Set<Date> uniqueTests = new HashSet<>();
				for(List<Object> labObject: labRev) {
					if (labObject.get(1) != null && labObject.get(2) != null && labObject.get(3) != null && labObject.get(4) != null) {
						Date orderDate = (Date) labObject.get(4);
						Date resultDate = (Date) labObject.get(1);
						String testName = (String) labObject.get(3);
						if(!uniqueTests.contains(orderDate) && testName.trim().equalsIgnoreCase("hiv viral load")) {
							LocalDate resultLocal = dateToLocalDate(resultDate);
							long months = Math.abs(ChronoUnit.MONTHS.between(nowLocal, resultLocal));
							String result = (String) labObject.get(2);
							if (months <= 36 && getIntegerValue(result.trim()) >= 200) {
								ret++;
							}
						}
						uniqueTests.add(orderDate);
					}
				}
			}
		}
		return(ret);
	}

	private Integer getNLVLThreeYears(List<List<Object>> lab) {
		Integer ret = 0;
		if(lab != null) {
			// Get for the last 3 years
			if (lab.size() > 0) {
				// Reverse the list to loop from the first
				List<List<Object>> labRev = new ArrayList<>(lab);
				Collections.reverse(labRev);
				// Count number of tests for the last 3 years
				Date now = new Date();
				LocalDate nowLocal = dateToLocalDate(now);
				// A unique test is given by unique order date
				Set<Date> uniqueTests = new HashSet<>();
				for(List<Object> labObject: labRev) {
					if (labObject.get(1) != null && labObject.get(2) != null && labObject.get(3) != null && labObject.get(4) != null) {
						Date orderDate = (Date) labObject.get(4);
						Date resultDate = (Date) labObject.get(1);
						String testName = (String) labObject.get(3);
						if(!uniqueTests.contains(orderDate) && testName.trim().equalsIgnoreCase("hiv viral load")) {
							LocalDate resultLocal = dateToLocalDate(resultDate);
							long months = Math.abs(ChronoUnit.MONTHS.between(nowLocal, resultLocal));
							String result = (String) labObject.get(2);
							if (months <= 36 && (getIntegerValue(result.trim()) < 200 || result.trim()
									.equalsIgnoreCase("LDL"))) {
								ret++;
							}
						}
						uniqueTests.add(orderDate);
					}
				}
			}
		}
		return(ret);
	}

	/**
	 * Gets the integer value of a string, otherwise returns zero
	 * @param val
	 * @return
	 */
	public static int getIntegerValue(String val) {
		int ret = 0;
		try {
			ret = (int) Math.ceil(Double.parseDouble(val));
		} catch(Exception ex) {}
		return(ret);
	}

	/**
	 * Gets the long value of a string, otherwise returns zero
	 * @param val
	 * @return
	 */
	public static long getLongValue(String val) {
		long ret = 0;
		try {
			ret = (long) Math.ceil(Double.parseDouble(val));
		} catch(Exception ex) {}
		return(ret);
	}

	private Integer getPopulationTypeKP(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last visit
			if (demographics.size() > 0) {
				List<Object> visitObject = demographics.get(demographics.size() - 1);
				if (visitObject.get(6) != null) {
					String differentiatedCare = (String) visitObject.get(6);
					if(differentiatedCare.trim().equalsIgnoreCase("Key Population")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getPopulationTypeGP(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last visit
			if (demographics.size() > 0) {
				List<Object> visitObject = demographics.get(demographics.size() - 1);
				if (visitObject.get(6) != null) {
					String differentiatedCare = (String) visitObject.get(6);
					if(differentiatedCare.trim().equalsIgnoreCase("General Population")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	/**
	 * Checks if it is a female of child bearing age
	 * @param gender -- Gender 1: Male, Gender 2: Female
	 * @param Age -- The age of patient
	 * @return true or false
	 */
	private boolean isFemaleOfChildBearingAge(Integer gender, Long Age) {
		boolean ret = false;
		if(gender != null && Age != null) {
			if(gender == 2 && (Age > 9 && Age < 50)) {
				ret = true;
			}
		}
		return(ret);
	}

	private SimpleObject getBreastFeedingNo(List<List<Object>> visits, Integer gender, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(11) != null) {
						String isBreastFeeding = (String) visitObject.get(11);
						// Gender 1: Male, Gender 2: Female
						if (isBreastFeeding.trim().equalsIgnoreCase("yes")) {
							ret.put("result", 0);
							return (ret);
						}
						if (isBreastFeeding.trim().equalsIgnoreCase("no")) {
							ret.put("result", 1);
							return (ret);
						}
					} else {
						ret.put("result", "NA");
						return (ret);
					}
				}
			}
		} else {
			ret.put("result", 0);
			return (ret);
		}
		return(ret);
	}

	private Integer getBreastFeedingNR(Integer gender, Long Age) {
		Integer ret = 0;
		// Gender 1: Male, Gender 2: Female
		if(isFemaleOfChildBearingAge(gender, Age)) {
			ret = 0;
		} else {
			ret = 1;
		}
		return(ret);
	}

	private SimpleObject getBreastFeedingYes(List<List<Object>> visits, Integer gender, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(11) != null) {
						String isBreastFeeding = (String) visitObject.get(11);
						// Gender 1: Male, Gender 2: Female
						if (isBreastFeeding.trim().equalsIgnoreCase("no")) {
							ret.put("result", 0);
							return (ret);
						}
						if (isBreastFeeding.trim().equalsIgnoreCase("yes")) {
							ret.put("result", 1);
							return (ret);
						}
					} else {
						ret.put("result", "NA");
						return (ret);
					}
				}
			}
		} else {
			ret.put("result", 0);
			return (ret);
		}
		return(ret);
	}

	private SimpleObject getPregnantNo(List<List<Object>> visits, Integer gender, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(6) != null) {
						String isPregnant = (String) visitObject.get(6);
						// Gender 1: Male, Gender 2: Female
						if (isPregnant.trim().equalsIgnoreCase("yes")) {
							ret.put("result", 0);
							return (ret);
						}
						if (isPregnant.trim().equalsIgnoreCase("no")) {
							ret.put("result", 1);
							return (ret);
						}
					} else {
						ret.put("result", "NA");
						return (ret);
					}
				}
			}
		} else {
			ret.put("result", 0);
			return (ret);
		}

		return(ret);
	}

	private Integer getPregnantNR(Integer gender, Long Age) {
		Integer ret = 0;
		// Gender 1: Male, Gender 2: Female
		if(isFemaleOfChildBearingAge(gender, Age)) {
			ret = 0;
		} else  {
			ret = 1;
		}
		return(ret);
	}

	private SimpleObject getPregnantYes(List<List<Object>> visits, Integer gender, Long Age) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(6) != null) {
						String isPregnant = (String) visitObject.get(6);
						// Gender 1: Male, Gender 2: Female
						if (isPregnant.trim().equalsIgnoreCase("no")) {
							ret.put("result", 0);
							return (ret);
						}
						if (isPregnant.trim().equalsIgnoreCase("yes")) {
							ret.put("result", 1);
							return (ret);
						}
					} else {
						ret.put("result", "NA");
						return (ret);
					}
				}
			}
		} else {
			ret.put("result", 0);
			return (ret);
		}
		return(ret);
	}

	private Integer getMostRecentArtAdherenceFair(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				// Get adherence category (ART) position
				if (visitObject.get(12) != null) {
					String adherencePositions = (String) visitObject.get(12);
					String[] tokens = adherencePositions.split("\\|");

					int artPos = -1;
					for (int i = 0; i < tokens.length; i++) {
						if (tokens[i].trim().equalsIgnoreCase("ART")) {
							System.out.println("IIT ML: Position of 'ART': " + i);
							artPos = i;
							break;
						}
					}

					if(artPos > -1) {
						// We found ART adherence is covered we get the status
						if (visitObject.get(9) != null) {
							String adherenceString = (String) visitObject.get(9);
							System.err.println("IIT ML: Adherence full string: " + adherenceString);
							String[] adherenceTokens = adherenceString.split("\\|");
							System.err.println("IIT ML: Adherence tokens: " + Arrays.toString(adherenceTokens));
							if(adherenceTokens.length > 0) {
								for (int i = 0; i < adherenceTokens.length; i++) {
									if(i == artPos) {
										if (adherenceTokens[i].trim().equalsIgnoreCase("fair")) {
											ret = 1;
											return(ret);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMostRecentArtAdherenceGood(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				// Get adherence category (ART) position
				if (visitObject.get(12) != null) {
					String adherencePositions = (String) visitObject.get(12);
					String[] tokens = adherencePositions.split("\\|");

					int artPos = -1;
					for (int i = 0; i < tokens.length; i++) {
						if (tokens[i].trim().equalsIgnoreCase("ART")) {
							System.out.println("IIT ML: Position of 'ART': " + i);
							artPos = i;
							break;
						}
					}

					if(artPos > -1) {
						// We found ART adherence is covered we get the status
						if (visitObject.get(9) != null) {
							String adherenceString = (String) visitObject.get(9);
							System.err.println("IIT ML: Adherence full string: " + adherenceString);
							String[] adherenceTokens = adherenceString.split("\\|");
							System.err.println("IIT ML: Adherence tokens: " + Arrays.toString(adherenceTokens));
							if(adherenceTokens.length > 0) {
								for (int i = 0; i < adherenceTokens.length; i++) {
									if(i == artPos) {
										if (adherenceTokens[i].trim().equalsIgnoreCase("good")) {
											ret = 1;
											return(ret);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMostRecentArtAdherencePoor(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				// Get adherence category (ART) position
				if (visitObject.get(12) != null) {
					String adherencePositions = (String) visitObject.get(12);
					String[] tokens = adherencePositions.split("\\|");

					int artPos = -1;
					for (int i = 0; i < tokens.length; i++) {
						if (tokens[i].trim().equalsIgnoreCase("ART")) {
							System.out.println("IIT ML: Position of 'ART': " + i);
							artPos = i;
							break;
						}
					}

					if(artPos > -1) {
						// We found ART adherence is covered we get the status
						if (visitObject.get(9) != null) {
							String adherenceString = (String) visitObject.get(9);
							System.err.println("IIT ML: Adherence full string: " + adherenceString);
							String[] adherenceTokens = adherenceString.split("\\|");
							System.err.println("IIT ML: Adherence tokens: " + Arrays.toString(adherenceTokens));
							if(adherenceTokens.length > 0) {
								for (int i = 0; i < adherenceTokens.length; i++) {
									if(i == artPos) {
										if (adherenceTokens[i].trim().equalsIgnoreCase("poor")) {
											ret = 1;
											return(ret);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return(ret);
	}

	private Integer getStabilityAssessmentStable(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(8) != null) {
					String differentiatedCare = (String) visitObject.get(8);
					if(differentiatedCare.trim().equalsIgnoreCase("stable")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getStabilityAssessmentUnstable(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(8) != null) {
					String differentiatedCare = (String) visitObject.get(8);
					if(differentiatedCare.trim().equalsIgnoreCase("not stable")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCarecommunityartdistributionhcwled(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Community ART Distribution - HCW Led")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCarecommunityartdistributionpeerled(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Community ART Distribution - Peer Led")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCareexpress(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Express")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCarefacilityartdistributiongroup(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Facility ART Distribution Group")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCarefasttrack(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Fast Track")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getDifferentiatedCarestandardcare(List<List<Object>> visits) {
		Integer ret = 0;
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Standard Care")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusOther(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String marital = (String) maritalObject.get(4);
					if (!marital.trim().equalsIgnoreCase("single") &&
							!marital.trim().equalsIgnoreCase("divorced") &&
							!marital.trim().equalsIgnoreCase("widow") &&
							!marital.trim().equalsIgnoreCase("separated") &&
							!marital.trim().equalsIgnoreCase("married") &&
							!marital.trim().equalsIgnoreCase("monogamous") &&
							!marital.trim().equalsIgnoreCase("cohabiting") &&
							!marital.trim().equalsIgnoreCase("polygamous") &&
							Age > 15
					) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusSingle(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String gender = (String) maritalObject.get(4);
					if (gender.trim().equalsIgnoreCase("single") && Age > 15) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusWidow(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String marital = (String) maritalObject.get(4);
					if (marital.trim().equalsIgnoreCase("widow") && Age > 15) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusPolygamous(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String marital = (String) maritalObject.get(4);
					if (marital.trim().equalsIgnoreCase("polygamous") && Age > 15) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusDivorced(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String marital = (String) maritalObject.get(4);
					if ((marital.trim().equalsIgnoreCase("divorced") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("separated") && Age > 15)
					) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusMarried(List<List<Object>> demographics, Long Age) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null){
					String marital = (String) maritalObject.get(4);
					if ((marital.trim().equalsIgnoreCase("married") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("monogamous") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("cohabiting") && Age > 15)
					) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMaritalStatusMinor(Long Age) {
		Integer ret = 0;
		if (Age <= 15) {
			ret = 1;
		}
		return(ret);
	}

	private Long getAgeYears(List<List<Object>> demographics) {
		Long ret = 0L;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(2) != null) {
					Date DOB = (Date) maritalObject.get(2);
					Date now = new Date();
					// Instant dobInstant = DOB.toInstant();
					// Instant nowInstant = now.toInstant();
					// Get the age in years
					// Duration duration = Duration.between(nowInstant, dobInstant);
					// long years = duration.toDays() / 365;
					LocalDate dobLocal = dateToLocalDate(DOB);
					LocalDate nowLocal = dateToLocalDate(now);
					long years = Math.abs(ChronoUnit.YEARS.between(nowLocal, dobLocal));
					ret = years;
				}
			}
		}
		return(ret);
	}

	private LocalDate dateToLocalDate(Date dateToConvert) {
		return Instant.ofEpochMilli(dateToConvert.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	private Integer getPatientSourceVCT(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> sourceObject = demographics.get(demographics.size() - 1);
				if(sourceObject.get(3) != null) {
					String source = (String) sourceObject.get(3);
					System.err.println("IIT ML: Raw Patient source is: " + source);
					if (source.trim().equalsIgnoreCase("vct")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getPatientSourceOther(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> sourceObject = demographics.get(demographics.size() - 1);
				if(sourceObject.get(3) != null) {
					String source = (String) sourceObject.get(3);
					System.err.println("IIT ML: Raw Patient source is: " + source);
					if (!source.trim().equalsIgnoreCase("opd") && !source.trim().equalsIgnoreCase("vct")) {
						ret = 1;
					}
				} else {
					ret = 1;
				}
			}
		}
		return(ret);
	}

	private Integer getPatientSourceOPD(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> sourceObject = demographics.get(demographics.size() - 1);
				if(sourceObject.get(3) != null) {
					String source = (String) sourceObject.get(3);
					System.err.println("IIT ML: Raw Patient source is: " + source);
					if (source.trim().equalsIgnoreCase("opd")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getGenderFemale(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> genderObject = demographics.get(demographics.size() - 1);
				if(genderObject.get(1) != null) {
					String gender = (String) genderObject.get(1);
					if (gender.trim().equalsIgnoreCase("female")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getGenderMale(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> genderObject = demographics.get(demographics.size() - 1);
				if(genderObject.get(1) != null) {
					String gender = (String) genderObject.get(1);
					if (gender.trim().equalsIgnoreCase("male")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private Integer getMonthJan(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.JANUARY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthFeb(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.FEBRUARY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthMar(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.MARCH) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthApr(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = monthOfYear == Calendar.APRIL ? 1: 0;
			}
		}
		return(ret);
	}

	private Integer getMonthMay(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.MAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthJun(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.JUNE) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthJul(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.JULY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthAug(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.AUGUST) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthSep(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.SEPTEMBER) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthOct(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = monthOfYear == Calendar.OCTOBER ? 1: 0;
			}
		}
		return(ret);
	}

	private Integer getMonthNov(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.NOVEMBER) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getMonthDec(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				ret = (monthOfYear == Calendar.DECEMBER) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDayMon(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.MONDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDayTue(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.TUESDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDayWed(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.WEDNESDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDayThu(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.THURSDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDayFri(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.FRIDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDaySat(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.SATURDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	private Integer getDaySun(List<Appointment> appointments) {
		Integer ret = 0;
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				ret = (dayOfWeek == Calendar.SUNDAY) ? 1 : 0;
			}
		}
		return(ret);
	}

	public static int getMonthOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}

	public static int getDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	private Double getBMI(Double height, Double weight) {
		// BMI is given by (Weight / ((Height/100)^2))
		Double ret = 0.00;
		if(height > 0 && weight > 0) {
			//constraints
			if(height >= 100 && height <= 250 && weight >= 30 && weight <= 200) {
				ret = weight / (Math.pow((height / 100), 2));
			}
		}
		return(ret);
	}

	private Double getWeight(List<List<Object>> visits) {
		Double ret = 0.00;
		if(visits != null) {
			// Flip the list -- the last becomes the first
			Collections.reverse(visits);
			for (List<Object> in : visits) {
				if(in.get(5) != null) {
					Double weight = (Double) in.get(5);
					return(weight);
				}
			}
		}
		return(ret);
	}

	private Double getHeight(List<List<Object>> visits) {
		Double ret = 0.00;
		if(visits != null) {
			// Flip the list -- the last becomes the first
			Collections.reverse(visits);
			for (List<Object> in : visits) {
				if(in.get(4) != null) {
					Double height = (Double) in.get(4);
					return(height);
				}
			}
		}
		return(ret);
	}

	private Double getUnscheduledRateLast5(List<List<Object>> visits) {
		Double ret = 0.00;
		if(visits != null) {
			Integer addition = 0;

			// Get last 5 visits
			List<List<Object>> workingList = new ArrayList<>();
			int size = visits.size();
			if (size > 5) {
				workingList.addAll(visits.subList(size - 5, size));
			} else {
				workingList.addAll(visits);
			}
			Integer divider = Math.max(workingList.size(), 1); // Avoid divide by zero

			for (List<Object> in : workingList) {
				String visitType = (String)in.get(3);
				if(visitType != null && visitType.trim().equalsIgnoreCase("unscheduled")) {
					addition++;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Double getAverageTCALast5(List<Appointment> appointments) {
		Double ret = 0.00;
		if(appointments != null) {
			List<Appointment> workingList = new ArrayList<>();
			List<Appointment> holdingList = new ArrayList<>();
			// Apparently we should not consider the last encounter. No idea why.
			holdingList.addAll(appointments);
			if(holdingList.size() > 0) {
				holdingList.remove(holdingList.size() - 1);
			}
			int size = holdingList.size();
			if (size > 5) {
				workingList.addAll(holdingList.subList(size - 5, size));
			} else {
				workingList.addAll(holdingList);
			}
			Integer divider = Math.max(workingList.size(), 1);
			if(divider > 0) {
				Integer totalDays = 0;
				for (Appointment in : workingList) {
					// Get the difference in days
					long differenceInMilliseconds = in.getAppointmentDate().getTime() - in.getEncounterDate().getTime();
					int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
					totalDays += differenceInDays;
				}
				ret = ((double)totalDays / (double)divider);
			}
		}
		return(ret);
	}

	private SimpleObject getNumHivRegimens(Set<Treatment> treatments) {
		SimpleObject ret = SimpleObject.create("result", "NA");

		if(treatments != null) {
			Set<String> drugs = new HashSet<>(); // This will ensure we get unique drugs
			for (Treatment in : treatments) {
				System.err.println("IIT ML: got drug: " + in.getDrug() + " Treatment Type: " + in.getTreatmentType());
				if (in.getDrug() != null && in.getTreatmentType() != null && !in.getTreatmentType().trim().equalsIgnoreCase("Prophylaxis")) {
					String drug = in.getDrug().trim().toLowerCase();
					drugs.add(drug);
				}
			}
			ret.put("result", drugs.size() > 0 ? drugs.size() : "NA");
		}
		return(ret);
	}

	private Double getAverageLatenessLast5(List<Integer> missed) {
		Double ret = 0.00;
		if(missed != null) {
			Integer addition = 0;

			// Get last 5 missed
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 5) {
				workingList.addAll(missed.subList(size - 5, size));
			} else {
				workingList.addAll(missed);
			}

			Integer divider = Math.max(workingList.size(), 1);

			for (Integer in : workingList) {
				if(in > 0) {
					addition += in;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Integer getLateLast5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 5) {
				workingList.addAll(missed.subList(size - 5, size));
			} else {
				workingList.addAll(missed);
			}
			for (Integer in : workingList) {
				if (in >= 1) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Double getAverageLatenessLast10(List<Integer> missed) {
		Double ret = 0.00;
		if(missed != null) {
			Integer addition = 0;

			// Get last 10 missed
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 10) {
				workingList.addAll(missed.subList(size - 10, size));
			} else {
				workingList.addAll(missed);
			}

			Integer divider = Math.max(workingList.size(), 1);

			for (Integer in : workingList) {
				if(in > 0) {
					addition += in;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Double getAverageLatenessLast3(List<Integer> missed) {
		Double ret = 0.00;
		if(missed != null) {
			Integer addition = 0;

			// Get last 3 missed
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 3) {
				workingList.addAll(missed.subList(size - 3, size));
			} else {
				workingList.addAll(missed);
			}

			Integer divider = Math.max(workingList.size(), 1);

			for (Integer in : workingList) {
				if(in > 0) {
					addition += in;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Integer getNextAppointmentDate(List<Appointment> allAppts) {
		Integer ret = 0;
		if(allAppts != null) {
			int size = allAppts.size();
			if(size > 0) {
				// Get latest appointment
				Appointment last = allAppts.get(size - 1);
				Date lastNAD = last.getAppointmentDate();
				Date lastEncounterDate = last.getEncounterDate();
				// Get the difference in days
				long differenceInMilliseconds = lastNAD.getTime() - lastEncounterDate.getTime();
				int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
				// Only positive integers
				ret = Math.max(differenceInDays, 0);
			}
		}
		return(ret);
	}

	private Integer getLateLast3(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 3) {
				workingList.addAll(missed.subList(size - 3, size));
			} else {
				workingList.addAll(missed);
			}
			for (Integer in : workingList) {
				if (in >= 1) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getLateLast10(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			List<Integer> workingList = new ArrayList<>();
			int size = missed.size();
			if (size > 10) {
				workingList.addAll(missed.subList(size - 10, size));
			} else {
				workingList.addAll(missed);
			}
			for (Integer in : workingList) {
				if (in >= 1) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getVisit1(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			ret = size > 0 ? missed.get(size - 1) : 0;
		}
		return(ret);
	}

	private Integer getVisit2(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			ret = size > 1 ? missed.get(size - 2) : 0;
		}
		return(ret);
	}

	private Integer getVisit3(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			ret = size > 2 ? missed.get(size - 3) : 0;
		}
		return(ret);
	}

	private Integer getVisit4(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			ret = size > 3 ? missed.get(size - 4) : 0;
		}
		return(ret);
	}

	private Integer getVisit5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			ret = size > 4 ? missed.get(size - 5) : 0;
		}
		return(ret);
	}

	private Double getLate28Rate(Integer late28, Integer n_appts) {
		Double ret = 0.00;
		if(late28 > 0 && n_appts > 0) {
			ret = ((double)late28 / (double)n_appts);
		}
		return(ret);
	}

	private Double getLateRate(Integer late, Integer n_appts) {
		Double ret = 0.00;
		if(late > 0 && n_appts > 0) {
			ret = ((double)late / (double)n_appts);
		}
		return(ret);
	}

	private Double getAverageLateness(List<Integer> missed, Integer divider) {
		Double ret = 0.00;
		if(missed != null && divider > 0.00) {
			Integer addition = 0;
			for (Integer in : missed) {
				if(in > 0) {
					addition += in;
				}
			}
			ret = ((double)addition / (double)divider);
		}
		return(ret);
	}

	private Integer getLate28(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			for (Integer in : missed) {
				if (in >= 28) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed1(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			for (Integer in : missed) {
				if (in >= 1) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			for (Integer in : missed) {
				if (in >= 5) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed30(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			for (Integer in : missed) {
				if (in >= 30) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed1Last5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			List<Integer> subList = missed.subList(Math.max(size - 5, 0), size);
			for (Integer in : subList) {
				if (in >= 1) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed5Last5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			List<Integer> subList = missed.subList(Math.max(size - 5, 0), size);
			for (Integer in : subList) {
				if (in >= 5) {
					ret++;
				}
			}
		}
		return(ret);
	}

	private Integer getMissed30Last5(List<Integer> missed) {
		Integer ret = 0;
		if(missed != null) {
			int size = missed.size();
			List<Integer> subList = missed.subList(Math.max(size - 5, 0), size);
			for (Integer in : subList) {
				if (in >= 30) {
					ret++;
				}
			}
		}
		return(ret);
	}

	static List<Integer> calculateLateness(List<Appointment> records) {
		List<Integer> dateDifferences = new ArrayList<>();

		for (int i = 0; i < records.size() - 1; i++) {
			Appointment currentRecord = records.get(i);
			Appointment nextRecord = records.get(i + 1);

			long differenceInMilliseconds = nextRecord.getEncounterDate().getTime() - currentRecord.getAppointmentDate().getTime();
			int differenceInDays = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));

			// If difference is less than zero, make it zero
			differenceInDays = Math.max(differenceInDays, 0);

			dateDifferences.add(differenceInDays);
		}

		return dateDifferences;
	}

	static List<Appointment> sortAppointmentsByEncounterDate(Set<Appointment> records) {
		List<Appointment> sortedRecords = new ArrayList<>(records);
		sortedRecords.sort(Comparator.comparing(Appointment::getEncounterDate));
		return sortedRecords;
	}

	static void processRecords(Set<Appointment> records) {
		Map<Date, Appointment> resultMap = new HashMap<>();

		for (Appointment record : records) {
			if (!resultMap.containsKey(record.getEncounterDate()) || record.getAppointmentDate().after(resultMap.get(record.getEncounterDate()).getAppointmentDate())) {
				resultMap.put(record.getEncounterDate(), record);
			}
		}

		records.clear();
		records.addAll(resultMap.values());
	}

	static void processRecords1(Set<Appointment> records) {
		records = records.stream()
				.collect(Collectors.toMap(
						record -> record.getPatientID() + "-" + record.getEncounterDate(),
						Function.identity(),
						(record1, record2) -> record1.getEncounterDate().after(record2.getEncounterDate()) ? record1 : record2
				))
				.values().stream()
				.collect(Collectors.toSet());
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
			System.err.println("IIT ML: Error on REST call: /updatepatientiitscore : " + ex.getMessage());
			ex.printStackTrace();
			return new ResponseEntity<Object>("Could not process the patient IIT Score request", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	/**
	 * Get the current memory consumption in MB
	 * @return long - the RAM usage in MB
	 */
	private long getMemoryConsumption() {
		long ret = 0L;
		final long MEGABYTE = 1024L * 1024L;

		// Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);

		// get the MB
		ret = memory / MEGABYTE;
        System.out.println("Used memory is megabytes: " + ret);

		return(ret);
	}

	/**
	 * 
	 * @param dateString
	 * @return
	 */
	public static LocalDateTime convertStringToDate(String dateString) {
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            // Conversion failed, return null
            return null;
        }
    }
}
