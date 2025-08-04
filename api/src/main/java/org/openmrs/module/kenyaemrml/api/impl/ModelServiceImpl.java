package org.openmrs.module.kenyaemrml.api.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.dmg.pmml.FieldName;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.OutputField;
// import org.omg.CORBA.Request;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaemrml.api.HTSMLService;
import org.openmrs.module.kenyaemrml.api.IITMLService;
import org.openmrs.module.kenyaemrml.api.MLUtils;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.ModelService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.kenyaemrml.iit.Appointment;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaemrml.iit.Treatment;
import org.openmrs.ui.framework.SimpleObject;
// import org.springframework.web.bind.annotation.RequestBody;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service class used to prepare and score models
 */
public class ModelServiceImpl extends BaseOpenmrsService implements ModelService {
	
	private Log log = LogFactory.getLog(this.getClass());

	// Enable/Disable debug mode -- Saves all prediction variables and payload into the DB for debugging
	private final boolean debugMode = true;

	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public ScoringResult htsscore(String modelId, String facilityName, String encounterDate, ModelInputFields inputFields, boolean debug) {
		try {
			Evaluator evaluator;
			HTSMLService hTSMLService = Context.getService(HTSMLService.class);
			evaluator = hTSMLService.getEvaluator();
			// evaluator.verify();
			// ScoringResult scoringResult = new ScoringResult(score(evaluator, inputFields, debug));
			// Get the results
			Map<String, Object> results = score(evaluator, inputFields, debug);
			// Add the thresholds
			results.put("thresholds", MLUtils.getHTSThresholds());
			results.put("moduleVersion", MLUtils.getModuleVersion("kenyaemrml"));
			ScoringResult scoringResult = new ScoringResult(results);
			return scoringResult;
		}
		catch (Exception e) {
			log.error("HTS ML: Exception during preparation of input parameters or scoring of values for HTS model: " + e.getMessage());
			System.err.println("HTS ML: Exception during preparation of input parameters or scoring of values for HTS model: " + e.getMessage());
			e.printStackTrace();
			return(null);
		}
	}

	public ScoringResult iitscore(String modelId, String facilityName, String encounterDate, ModelInputFields inputFields, boolean debug) {
		try {
			Evaluator evaluator;
			IITMLService iITMLService = Context.getService(IITMLService.class);
			evaluator = iITMLService.getEvaluator();
			// evaluator.verify();
			// ScoringResult scoringResult = new ScoringResult(score(evaluator, inputFields, debug));
			// Get the results
			Map<String, Object> results = score(evaluator, inputFields, debug);
			// Add the thresholds
			results.put("thresholds", MLUtils.getIITThresholds());
			ScoringResult scoringResult = new ScoringResult(results);
			return scoringResult;
		}
		catch (Exception e) {
			log.error("IIT ML: Exception during preparation of input parameters or scoring of values for IIT model: " + e.getMessage());
			System.err.println("IIT ML: Exception during preparation of input parameters or scoring of values for IIT model: " + e.getMessage());
			e.printStackTrace();
			return(null);
		}
	}
	
	/**
	 * A method that scores a model
	 * 
	 * @param evaluator
	 * @param inputFields
	 * @return
	 */
	public Map<String, Object> score(Evaluator evaluator, ModelInputFields inputFields, boolean debug) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Map<FieldName, ?> evaluationResultFromEvaluator = evaluator.evaluate(prepareEvaluationArgs(evaluator, inputFields));
		
		List<OutputField> outputFields = evaluator.getOutputFields();
		//List<TargetField> targetFields = evaluator.getTargetFields();
		
		for (OutputField targetField : outputFields) {
			FieldName targetFieldName = targetField.getName();
			Object targetFieldValue = evaluationResultFromEvaluator.get(targetField.getName());
			
			if (targetFieldValue instanceof Computable) {
				targetFieldValue = ((Computable) targetFieldValue).getResult();
			}
			
			result.put(targetFieldName.getValue(), targetFieldValue);
		}
		//TODO: this is purely for debugging
		if (debug) {
			Map<String, Object> modelInputs = new HashMap<String, Object>();
			Map<String, Object> combinedResult = new HashMap<String, Object>();
			for (Map.Entry<String, Object> entry : inputFields.getFields().entrySet()) {
				modelInputs.put(entry.getKey(), entry.getValue());
			}
			combinedResult.put("predictions", result);
			combinedResult.put("ModelInputs", modelInputs);
			
			return combinedResult;
		} else {
			Map<String, Object> predictions = new HashMap<String, Object>();
			predictions.put("predictions", result);
			return predictions;
		}
	}
	
	/**
	 * Performs variable mapping
	 * 
	 * @param evaluator
	 * @param inputFields
	 * @return variable-value pair
	 */
	public Map<FieldName, FieldValue> prepareEvaluationArgs(Evaluator evaluator, ModelInputFields inputFields) {
		Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
		
		List<InputField> evaluatorFields = evaluator.getActiveFields();
		
		for (InputField evaluatorField : evaluatorFields) {
			FieldName evaluatorFieldName = evaluatorField.getName();
			String evaluatorFieldNameValue = evaluatorFieldName.getValue();
			
			Object inputValue = inputFields.getFields().get(evaluatorFieldNameValue);
			
			if (inputValue == null) {
				System.err.println("ML: Model value not found for the following field: " + evaluatorFieldNameValue);
				log.warn("ML: Model value not found for the following field: " + evaluatorFieldNameValue);
			}
			
			arguments.put(evaluatorFieldName, evaluatorField.prepare(inputValue));
		}
		return arguments;
	}

	/**
	 * Gets the latest patient IIT score
	 */
	public PatientRiskScore generatePatientRiskScore(Patient patient) {
		PatientRiskScore patientRiskScore = new PatientRiskScore();

		GlobalProperty gpUseAPI = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_IIT_USE_API);

		if(gpUseAPI != null) {
			String stUseAPI = gpUseAPI.getPropertyValue();
			if(stUseAPI != null && stUseAPI.trim().equalsIgnoreCase("true")) {
				return(generatePatientRiskScoreRemote(patient));
			} else if(stUseAPI != null && stUseAPI.trim().equalsIgnoreCase("false")) {
				return(generatePatientRiskScoreLocal(patient));
			} else {
				return(generatePatientRiskScoreLocal(patient));
			}
		}
		
		patientRiskScore = generatePatientRiskScoreLocal(patient);

		return(patientRiskScore);
	}

	/**
	 * Gets the latest patient IIT score from remote API
	 */
	public PatientRiskScore generatePatientRiskScoreRemote(Patient patient) {
		PatientRiskScore patientRiskScore = new PatientRiskScore();

		GlobalProperty gpIITAPIUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_IIT_API_URL);
		GlobalProperty gpIITAPIUsername = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_IIT_API_USERNAME);
		GlobalProperty gpIITAPIPassword = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_IIT_API_PASSWORD);

		if(gpIITAPIUrl == null || gpIITAPIUsername == null || gpIITAPIPassword == null) {
			System.err.println("Machine learning module: Error : IIT remote API : Some global parameters are not set. Cannot continue");
			return(patientRiskScore);
		}

		System.err.println("Machine learning module: Using IIT remote API");

		try {
			String stIITAPIUrl = gpIITAPIUrl.getPropertyValue();
			String stIITAPIUsername = gpIITAPIUsername.getPropertyValue();
			String stIITAPIPassword = gpIITAPIPassword.getPropertyValue();

			if(stIITAPIUrl == null || stIITAPIUsername == null || stIITAPIPassword == null) {
				System.err.println("Machine learning module: Error : IIT remote API : Some global parameters are not set. Cannot continue");
				return(patientRiskScore);
			}

			String auth = stIITAPIUsername + ":" + stIITAPIPassword;
			String authentication = Base64.getEncoder().encodeToString(auth.getBytes());

			// Create the payload
			// Get the hash of the patient UUID
			// String patientUuid = patient.getUuid();
			// String hashedPatientUuid = MLUtils.getSHA256Hash(patientUuid);
			// or hash of ID
			// String hashedPatientId = MLUtils.getSHA256Hash(String.valueOf(patient.getId()));
			// or just the patient id
			String hashedPatientId = String.valueOf(patient.getId());

			String facilityMflCode = MLUtils.getDefaultMflCode();

			Date evaluationDate = null;
			Double riskScore = null;
			String description = null;
			String riskFactors = null;
			String startDate = "2015-01-01";
			String endDate = formatDate(new Date(), "yyyy-MMM-dd");

			SimpleObject rawPayload = SimpleObject.create("ppk", hashedPatientId, "sc", facilityMflCode, "start_date", startDate, "end_date", endDate);
			String payload = rawPayload.toJson();

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, payload);
			Request request = new Request.Builder()
				.url(stIITAPIUrl)
				.method("POST", body)
				.addHeader("Content-Type", "application/json")
				.addHeader("Authorization", authentication)
				.build();
			Response response = client.newCall(request).execute();

			// We extract the data
			if (response.isSuccessful()) {
				okhttp3.ResponseBody responseBody = response.body();
				if (responseBody != null) {
					String responseString = responseBody.string();
					org.json.JSONObject json = new org.json.JSONObject(responseString);

					if (json.has("result")) {
						org.json.JSONObject result = json.getJSONObject("result");
						double predOut = result.getDouble("pred_out");
						String predCat = result.getString("pred_cat");

						System.out.println("Machine learning module: Got remote result: pred_out: " + predOut);
						System.out.println("Machine learning module: Got remote result: pred_cat: " + predCat);

						patientRiskScore.setRiskScore(predOut);
						patientRiskScore.setDescription(predCat);
						patientRiskScore.setPatient(patient);
						String randUUID = UUID.randomUUID().toString(); 
						patientRiskScore.setSourceSystemUuid(randUUID);
						patientRiskScore.setRiskFactors("");
						patientRiskScore.setRiskScore(riskScore);
						patientRiskScore.setEvaluationDate(new Date());

					} else if (json.has("detail")) {
						String error = json.getString("detail");
						System.err.println("Machine learning module: IIT Error: " + error);
					} else {
						System.err.println("Machine learning module: Unexpected JSON structure: " + responseString);
					}
				} else {
					System.err.println("Machine learning module: Response body is null.");
				}
			} else {
				System.err.println("Machine learning module: Request failed with code: " + response.code());
			}

		} catch(Exception ex) {
			System.err.println("Machine learning module: Error getting remote iit score: " + ex.getMessage());
			ex.printStackTrace();
		}

		return(patientRiskScore);
	}

	/**
	 * Format date using a given format
	 * @param date
	 * @return
	 */
	public String formatDate(Date date, String format) {
		DateFormat dateFormatter = new SimpleDateFormat(format);
		return date == null ? "" : dateFormatter.format(date);
	}

	/**
	 * Gets the latest patient IIT score from local PMML
	 */
	public PatientRiskScore generatePatientRiskScoreLocal(Patient patient) {
		System.err.println("Machine learning module: Using IIT local PMML");

		long startTime = System.currentTimeMillis();
		long stopTime = 0L;
		long startMemory = getMemoryConsumption();
		long stopMemory = 0L;

		PatientRiskScore patientRiskScore = new PatientRiskScore();
		SimpleObject modelConfigs = new SimpleObject();
		SimpleObject patientPredictionVariables = new SimpleObject();
		SimpleObject mlScoringRequestPayload = new SimpleObject();
		
		try {
			//Threshold
			String iitLowRiskThresholdGlobal = "kenyaemrml.iit.lowRiskThreshold";
			GlobalProperty globalIITLowRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(iitLowRiskThresholdGlobal);
			String strIITLowRiskThreshold = globalIITLowRiskThreshold.getPropertyValue();
			Double decIITLowRiskThreshold = Double.valueOf(strIITLowRiskThreshold);

			String iitMediumRiskThresholdGlobal = "kenyaemrml.iit.mediumRiskThreshold";
			GlobalProperty globalIITMediumRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(iitMediumRiskThresholdGlobal);
			String strIITMediumRiskThreshold = globalIITMediumRiskThreshold.getPropertyValue();
			Double decIITMediumRiskThreshold = Double.valueOf(strIITMediumRiskThreshold);

			String iitHighRiskThresholdGlobal = "kenyaemrml.iit.highRiskThreshold";
			GlobalProperty globalIITHighRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(iitHighRiskThresholdGlobal);
			String strIITHighRiskThreshold = globalIITHighRiskThreshold.getPropertyValue();
			Double decIITHighRiskThreshold = Double.valueOf(strIITHighRiskThreshold);
			// Model Configuration
			modelConfigs.put("modelId", "XGB_IIT_02052024");
			String today = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
			modelConfigs.put("encounterDate", today);
			modelConfigs.put("facilityId", "");
			modelConfigs.put("debug", "true");

			AdministrationService administrationService = Context.getAdministrationService();
			Integer patientID = patient.getId();

			// Call DB Queries
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
							true); // PatientPK(0), ReportedByDate, TestResult
			List<List<Object>> art = administrationService
					.executeSQL(artQuery,
							true); // PatientPK(0), StartARTDate
			List<List<Object>> lastETLUpdate = administrationService
					.executeSQL(lastETLUpdateQuery,
							true); // INDICATOR_NAME(0), INDICATOR_VALUE(1), INDICATOR_MONTH(2)
			List<List<Object>> cd4Counter = administrationService
					.executeSQL(cd4Query,
							true); // PatientPK(0), lastcd4(1)

			if(debugMode) System.err.println("IIT ML: Got visits: " + visits.size());
			if(debugMode) System.err.println("IIT ML: Got pharmacy: " + pharmacy.size());
			if(debugMode) System.err.println("IIT ML: Got demographics: " + demographics.size());
			if(debugMode) System.err.println("IIT ML: Got lab: " + lab.size());
			if(debugMode) System.err.println("IIT ML: Got ART: " + art.size());
			if(debugMode) System.err.println("IIT ML: DWAPI ETL last update: " + lastETLUpdate.size());
			if(debugMode) System.err.println("IIT ML: Got Last CD4 count: " + cd4Counter.size());

			// January 2019 reference date
			Date jan2019 = new Date(119, 0, 1);
			Date now = new Date();

			// Prediction Variables

			// Start Facility Profile Matrix
			// This is set to initial zeros. The real values will be picked from the location matrix file

			patientPredictionVariables.put("anc", 0);
			patientPredictionVariables.put("sti", 0);
			patientPredictionVariables.put("SumTXCurr", 0);
			patientPredictionVariables.put("poverty", 0);
			patientPredictionVariables.put("pregnancies", 0);
			patientPredictionVariables.put("pnc", 0);
			patientPredictionVariables.put("pop", 0);
			patientPredictionVariables.put("sba", 0);
			patientPredictionVariables.put("owner_typeFaith", 0);
			patientPredictionVariables.put("owner_typeNGO", 0);
			patientPredictionVariables.put("owner_typePrivate", 0);
			patientPredictionVariables.put("owner_typePublic", 0);
			patientPredictionVariables.put("partner_away", 0);
			patientPredictionVariables.put("partner_men", 0);
			patientPredictionVariables.put("partner_women", 0);
			patientPredictionVariables.put("literacy", 0);
			patientPredictionVariables.put("hiv_count", 0);
			patientPredictionVariables.put("hiv_prev", 0);
			patientPredictionVariables.put("in_union", 0);
			patientPredictionVariables.put("intercourse", 0);
			patientPredictionVariables.put("keph_level_nameLevel.2", 0);
			patientPredictionVariables.put("keph_level_nameLevel.3", 0);
			patientPredictionVariables.put("keph_level_nameLevel.4", 0);
			patientPredictionVariables.put("keph_level_nameLevel.5", 0);
			patientPredictionVariables.put("keph_level_nameLevel.6", 0);
			patientPredictionVariables.put("circumcision", 0);
			patientPredictionVariables.put("condom", 0);
			patientPredictionVariables.put("births", 0);

			// End Facility Profile Matrix

			// Last ETL Update
			String lastETLUpdateSt = getLastETLUpdate(lastETLUpdate);
			if(debugMode) System.err.println("IIT ML: Last time the ETL was updated (lastETLUpdateSt): " + lastETLUpdateSt);

			// Start Local Pull And Display
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
								if(debugMode) System.err.println("IIT ML: 365 days record rejected: " + ls);
							}
						} else {
							if(debugMode) System.err.println("IIT ML: 2019 record rejected: " + ls);
						}
					}
				}
			}
			if(debugMode) System.err.println("IIT ML: visits before: " + visitAppts.size());
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
			if(debugMode) System.err.println("IIT ML: pharmacy before: " + pharmAppts.size());
			processRecords(pharmAppts);

			if(debugMode) System.err.println("IIT ML: Got Filtered visits: " + visitAppts.size());
			if(debugMode) System.err.println("IIT ML: Got Filtered pharmacy: " + pharmAppts.size());

			//Combine the two sets
			Set<Appointment> allAppts = new HashSet<>();
			allAppts.addAll(visitAppts);
			allAppts.addAll(pharmAppts);
			if(debugMode) System.err.println("IIT ML: Prepared appointments before: " + allAppts.size());
			processRecords(allAppts);

			// New model (n_appts)
			Integer n_appts = allAppts.size();
			if(debugMode) System.err.println("IIT ML: Final appointments (n_appts): " + n_appts);

			List<Appointment> sortedVisits = sortAppointmentsByEncounterDate(visitAppts);
			List<Appointment> sortedRecords = sortAppointmentsByEncounterDate(allAppts);
			List<Integer> missedRecord = calculateLateness(sortedRecords);

			if(debugMode) System.err.println("IIT ML: Missed before: " + missedRecord);

			Integer missed1 = getMissed1(missedRecord);
			if(debugMode) System.err.println("IIT ML: Missed by at least one (missed1): " + missed1);

			Integer missed5 = getMissed5(missedRecord);
			if(debugMode) System.err.println("IIT ML: Missed by at least five (missed5): " + missed5);

			Integer missed30 = getMissed30(missedRecord);
			if(debugMode) System.err.println("IIT ML: Missed by at least thirty (missed30): " + missed30);

			Integer missed1Last5 = getMissed1Last5(missedRecord);
			if(debugMode) System.err.println(
					"IIT ML: Missed by at least one in the latest 5 appointments (missed1_Last5): " + missed1Last5);

			Integer missed5Last5 = getMissed5Last5(missedRecord);
			if(debugMode) System.err.println(
					"IIT ML: Missed by at least five in the latest 5 appointments (missed5_Last5): " + missed5Last5);

			Integer missed30Last5 = getMissed30Last5(missedRecord);
			if(debugMode) System.err.println(
					"IIT ML: Missed by at least thirty in the latest 5 appointments (missed30_Last5): "
							+ missed30Last5);

			/**
			 * New Model 05/02/2024
			 * Based on the Visits table only
			 * late
			 * late28
			 * average_lateness
			 * late_rate
			 * late28_rate
			 * visit_1
			 * visit_2 -- removed
			 * visit_3 -- removed
			 * visit_4 -- removed
			 * visit_5 -- removed
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
			if(debugMode) System.err.println("IIT ML: new model (late): " + missed1);

			// New model (late28)
			Integer late28 = getLate28(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (late28): " + late28);

			// New model (averagelateness)
			Double averagelateness = getAverageLateness(missedRecord, allAppts.size());
			if(debugMode) System.err.println(
					"IIT ML: new model (averagelateness): " + averagelateness);

			// New model (late_rate)
			Double late_rate = getLateRate(missed1, allAppts.size());
			if(debugMode) System.err.println("IIT ML: new model (late_rate): " + late_rate);

			// New model (late28_rate)
			Double late28_rate = getLate28Rate(late28, allAppts.size());
			if(debugMode) System.err.println("IIT ML: new model (late28_rate): " + late28_rate);

			// New model (visit_1)
			Integer visit_1 = getVisit1(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (visit_1): " + visit_1);

			// New model (late_last10)
			Integer late_last10 = getLateLast10(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (late_last10): " + late_last10);

			// New model (NextAppointmentDate)
			Integer NextAppointmentDate = getNextAppointmentDate(sortedRecords);
			if(debugMode) System.err.println("IIT ML: new model (NextAppointmentDate): " + NextAppointmentDate);

			// New model (late_last3)
			Integer late_last3 = getLateLast3(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (late_last3): " + late_last3);

			// New model (averagelateness_last3)
			Double averagelateness_last3 = getAverageLatenessLast3(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (averagelateness_last3): " + averagelateness_last3);

			// New model (averagelateness_last10)
			Double averagelateness_last10 = getAverageLatenessLast10(missedRecord);
			if(debugMode) System.err.println(
					"IIT ML: new model (averagelateness_last10): " + averagelateness_last10);

			// New model (late_last5)
			Integer late_last5 = getLateLast5(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (late_last5): " + late_last5);

			// New model (averagelateness_last5)
			Double averagelateness_last5 = getAverageLatenessLast5(missedRecord);
			if(debugMode) System.err.println("IIT ML: new model (averagelateness_last5): " + averagelateness_last5);

			// New model (average_tca_last5)
			Double average_tca_last5 = getAverageTCALast5(sortedRecords);
			if(debugMode) System.err.println("IIT ML: new model (average_tca_last5): " + average_tca_last5);

			// New model (unscheduled_rate)
			Double unscheduled_rate = getUnscheduledRate(visits);
			if(debugMode) System.err.println("IIT ML: new model (unscheduled_rate): " + unscheduled_rate);

			// New model (unscheduled_rate_last5)
			Double unscheduled_rate_last5 = getUnscheduledRateLast5(visits);
			if(debugMode) System.err.println("IIT ML: new model (unscheduled_rate_last5): " + unscheduled_rate_last5);

			// End new model

			Integer patientGender = getGender(demographics);

			// (Gender)
			Integer GenderFemale = 0;
			Integer GenderMale = 0;
			if(patientGender == 1) {
				GenderMale = 1;
			} else if(patientGender == 2) {
				GenderFemale = 1;
			}
			if(debugMode) System.err.println("IIT ML: (Gender): " + patientGender);

			// (Age)
			Long Age = getAgeYears(demographics);
			if(debugMode) System.err.println("IIT ML: (Age): " + Age);

			// (timeOnArt)
			Long timeOnArt = getTimeOnArt(art);
			if(debugMode) System.err.println("IIT ML: (timeOnArt): " + timeOnArt);

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

			if(debugMode) System.err.println(
					"IIT ML: Total number of regimens - nonfiltered (last 400 days): " + pharmTreatment.size());

			// (num_hiv_regimens) -- Note: If zero, we show NA
			SimpleObject num_hiv_regimens = getNumHivRegimens(pharmTreatment);
			if(debugMode) System.err.println("IIT ML: (num_hiv_regimens): " + num_hiv_regimens.get("result"));

			// End Local Pull And Display

			// Start Pulled Variables

			//One time vars
			patientPredictionVariables.put("Age", Age);
			patientPredictionVariables.put("average_tca_last5", average_tca_last5);
			patientPredictionVariables.put("averagelateness", averagelateness);
			patientPredictionVariables.put("averagelateness_last10", averagelateness_last10);
			patientPredictionVariables.put("averagelateness_last3", averagelateness_last3);
			patientPredictionVariables.put("averagelateness_last5", averagelateness_last5);
			patientPredictionVariables.put("timeOnArt", timeOnArt);
			patientPredictionVariables.put("unscheduled_rate", unscheduled_rate);
			patientPredictionVariables.put("visit_1", visit_1);
			patientPredictionVariables.put("late", missed1);
			patientPredictionVariables.put("late_last10", late_last10);
			patientPredictionVariables.put("late_last3", late_last3);
			patientPredictionVariables.put("late_last5", late_last5);
			patientPredictionVariables.put("late_rate", late_rate);
			patientPredictionVariables.put("late28", late28);
			patientPredictionVariables.put("late28_rate", late28_rate);
			patientPredictionVariables.put("GenderFemale", GenderFemale);
			patientPredictionVariables.put("GenderMale", GenderMale);
			patientPredictionVariables.put("n_appts", n_appts);
			patientPredictionVariables.put("NextAppointmentDate", NextAppointmentDate);
			patientPredictionVariables.put("num_hiv_regimens", num_hiv_regimens.get("result"));

			//Grouped vars

			// Breast Feeding
			patientPredictionVariables.put("Breastfeedingno", -10000.0);
			patientPredictionVariables.put("BreastfeedingNR", 0);
			patientPredictionVariables.put("Breastfeedingyes", -10000.0);
			getBreastFeeding(patientPredictionVariables, visits, patientGender, Age);

			// Day of week
			patientPredictionVariables.put("DayFri", 0);
			patientPredictionVariables.put("DayMon", 0);
			patientPredictionVariables.put("DaySat", 0);
			patientPredictionVariables.put("DaySun", 0);
			patientPredictionVariables.put("DayThu", 0);
			patientPredictionVariables.put("DayTue", 0);
			patientPredictionVariables.put("DayWed", 0);
			getPayloadDayOfWeek(patientPredictionVariables, sortedRecords);

			// Differentiated care
			patientPredictionVariables.put("DifferentiatedCarecommunityartdistributionhcwled", 0);
			patientPredictionVariables.put("DifferentiatedCarecommunityartdistributionpeerled", 0);
			patientPredictionVariables.put("DifferentiatedCareexpress", 0);
			patientPredictionVariables.put("DifferentiatedCarefacilityartdistributiongroup", 0);
			patientPredictionVariables.put("DifferentiatedCarefasttrack", 0);
			patientPredictionVariables.put("DifferentiatedCarestandardcare", 0);
			getDifferentiatedCare(patientPredictionVariables, visits);

			// Marital Status
			patientPredictionVariables.put("MaritalStatusDivorced", 0);
			patientPredictionVariables.put("MaritalStatusMarried", 0);
			patientPredictionVariables.put("MaritalStatusMinor", 0);
			patientPredictionVariables.put("MaritalStatusOther", 0);
			patientPredictionVariables.put("MaritalStatusPolygamous", 0);
			patientPredictionVariables.put("MaritalStatusSingle", 0);
			patientPredictionVariables.put("MaritalStatusWidow", 0);
			getMaritalStatus(patientPredictionVariables, demographics, Age);

			// Month of year
			patientPredictionVariables.put("MonthApr", 0);
			patientPredictionVariables.put("MonthAug", 0);
			patientPredictionVariables.put("MonthDec", 0);
			patientPredictionVariables.put("MonthFeb", 0);
			patientPredictionVariables.put("MonthJan", 0);
			patientPredictionVariables.put("MonthJul", 0);
			patientPredictionVariables.put("MonthJun", 0);
			patientPredictionVariables.put("MonthMar", 0);
			patientPredictionVariables.put("MonthMay", 0);
			patientPredictionVariables.put("MonthNov", 0);
			patientPredictionVariables.put("MonthOct", 0);
			patientPredictionVariables.put("MonthSep", 0);
			getPayloadMonthOfYear(patientPredictionVariables, sortedRecords);

			// Most recent ART adherence
			patientPredictionVariables.put("most_recent_art_adherencefair", 0);
			patientPredictionVariables.put("most_recent_art_adherencegood", 0);
			patientPredictionVariables.put("most_recent_art_adherencepoor", 0);
			getMostRecentArtAdherence(patientPredictionVariables, visits);

			// Optimized HIV Regimen
			patientPredictionVariables.put("OptimizedHIVRegimenNo", -10000.0F);
			patientPredictionVariables.put("OptimizedHIVRegimenYes", -10000.0F);
			getOptimizedHIVRegimen(patientPredictionVariables, pharmacy);

			// Pregnant
			patientPredictionVariables.put("Pregnantno", -10000.0F);
			patientPredictionVariables.put("PregnantNR", 0);
			patientPredictionVariables.put("Pregnantyes", -10000.0F);
			getPregnant(patientPredictionVariables, visits, patientGender, Age);

			// Stability Assessment
			patientPredictionVariables.put("StabilityAssessmentStable", 0);
			patientPredictionVariables.put("StabilityAssessmentUnstable", 0);
			getStabilityAssessment(patientPredictionVariables, visits);
			
			// End Pulled Variables

			// Load model configs and variables
			mlScoringRequestPayload.put("modelConfigs", modelConfigs);
			mlScoringRequestPayload.put("variableValues", patientPredictionVariables);

			// Get JSON Payload
			String payload = mlScoringRequestPayload.toJson();
			if(debugMode) System.err.println("IIT ML: Prediction Payload: " + payload);
			
			// Get the IIT ML score
			try {
				//Extract score from payload
				String mlScoreResponse = MLUtils.generateIITMLScore(payload);

				if(mlScoreResponse != null && !mlScoreResponse.trim().equalsIgnoreCase("")) {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode jsonNode = (ObjectNode) mapper.readTree(mlScoreResponse);
					if (jsonNode != null) {
						if(debugMode) System.err.println("IIT ML: Got ML Score Payload as: " + mlScoreResponse);
						Double riskScore = 0.00;
						JsonNode result = jsonNode.get("result");
						if(result != null) {
							JsonNode predictions = result.get("predictions");
							if(predictions != null) {
								JsonNode probability = predictions.get("probability(1)");
								if(probability != null) {
									riskScore = probability.getDoubleValue();
								}
							}
						}

						System.out.println("IIT ML: Got ML score as: " + riskScore);

						// Check if there is an existing record. In case we want to save, we need to modify record instead of creating a new one
						PatientRiskScore currentPatientRiskScore = Context.getService(MLinKenyaEMRService.class).getLatestPatientRiskScoreByPatient(patient);
						if(currentPatientRiskScore != null) {
							patientRiskScore = currentPatientRiskScore;
						} else {
							patientRiskScore.setPatient(patient);
							String randUUID = UUID.randomUUID().toString(); 
							patientRiskScore.setSourceSystemUuid(randUUID);
						}

						patientRiskScore.setRiskFactors("");
						patientRiskScore.setRiskScore(riskScore);
						
						if(riskScore <= decIITLowRiskThreshold) {
							patientRiskScore.setDescription("Low Risk");
						} else if((riskScore > decIITLowRiskThreshold) && (riskScore <= decIITMediumRiskThreshold)) {
							patientRiskScore.setDescription("Medium Risk");
						} else if((riskScore > decIITMediumRiskThreshold) && (riskScore <= decIITHighRiskThreshold)) {
							patientRiskScore.setDescription("High Risk");
						} else if(riskScore > decIITHighRiskThreshold) {
							patientRiskScore.setDescription("Highest Risk");
						}

						System.out.println("IIT ML: Got ML Description as: " + patientRiskScore.getDescription());
						patientRiskScore.setEvaluationDate(new Date());

						// if we are in debug mode:
						if(debugMode == true) {
							try {
								patientRiskScore.setPayload(payload);
								patientRiskScore = extractPayload(patientRiskScore, mlScoringRequestPayload);

								patientRiskScore.setLastDwapiEtlUpdate(lastETLUpdateSt);

								String facilityMflCode = MLUtils.getDefaultMflCode();
								patientRiskScore.setMflCode(facilityMflCode);

								Hibernate.initialize(patient.getIdentifiers()); // fix lazy loading
								String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
								PatientIdentifierType patientIdentifierType = Context.getPatientService()
										.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
								PatientIdentifier cccNumberId = patient
										.getPatientIdentifier(patientIdentifierType); // error with lazy loading
								String cccNumber = cccNumberId.getIdentifier();
								patientRiskScore.setCccNumber(cccNumber);
							}
							catch (Exception ex) {
								System.err.println("ITT ML: Could not add payload, ccc or mfl " + ex.getMessage());
								ex.printStackTrace();
							}
						}
						
						if(debugMode) System.err.println("IIT ML: PatientRiskScore is: " + patientRiskScore.toString());

						stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						if(debugMode) System.err.println("Time taken: " + elapsedTime);
						if(debugMode) System.err.println("Time taken sec: " + elapsedTime / 1000);

						stopMemory = getMemoryConsumption();
						long usedMemory = stopMemory - startMemory;
						if(debugMode) System.err.println("Memory used: " + usedMemory);

						return(patientRiskScore);
					} else {
						System.err.println("IIT ML: Error: Unable to get ML score");
					}
				}
			}
			catch (Exception em) {
				System.err.println("ITT ML: Could not get the IIT Score: Error Calling IIT Model " + em.getMessage());
				em.printStackTrace();
			}
		}
		catch (Exception ex) {
			System.err.println("ITT ML: Could not get the IIT Score: Error sourcing model vars " + ex.getMessage());
			ex.printStackTrace();
		}

		//In case of an error
		patientRiskScore.setRiskFactors("");
		patientRiskScore.setRiskScore(0.00);
		patientRiskScore.setPatient(patient);
		patientRiskScore.setDescription("Unknown Risk");
		patientRiskScore.setEvaluationDate(new Date());
		String randUUID = UUID.randomUUID().toString(); 
		patientRiskScore.setSourceSystemUuid(randUUID);

		stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Time taken: " + elapsedTime);
		System.out.println("Time taken sec: " + elapsedTime / 1000);

		stopMemory = getMemoryConsumption();
		long usedMemory = stopMemory - startMemory;
		System.out.println("Memory used: " + usedMemory);

		return(patientRiskScore);
	}

	private PatientRiskScore extractPayload(PatientRiskScore prs, SimpleObject mlScoringRequestPayload) {
		SimpleObject load = (SimpleObject) mlScoringRequestPayload.get("variableValues");

		try {
			prs.setAge(convertToString(load.get("Age")));
			prs.setAverage_tca_last5(convertToString(load.get("average_tca_last5")));
			prs.setAveragelateness(convertToString(load.get("averagelateness")));
			prs.setAveragelateness_last10(convertToString(load.get("averagelateness_last10")));
			prs.setAveragelateness_last3(convertToString(load.get("averagelateness_last3")));
			prs.setAveragelateness_last5(convertToString(load.get("averagelateness_last5")));
			prs.setBreastfeedingno(convertToString(load.get("Breastfeedingno")));
			prs.setBreastfeedingNR(convertToString(load.get("BreastfeedingNR")));
			prs.setBreastfeedingyes(convertToString(load.get("Breastfeedingyes")));
			prs.setDayFri(convertToString(load.get("DayFri")));
			prs.setDayMon(convertToString(load.get("DayMon")));
			prs.setDaySat(convertToString(load.get("DaySat")));
			prs.setDaySun(convertToString(load.get("DaySun")));
			prs.setDayThu(convertToString(load.get("DayThu")));
			prs.setDayTue(convertToString(load.get("DayTue")));
			prs.setDayWed(convertToString(load.get("DayWed")));
			prs.setDifferentiatedCarecommunityartdistributionhcwled(convertToString(load.get("DifferentiatedCarecommunityartdistributionhcwled")));
			prs.setDifferentiatedCarecommunityartdistributionpeerled(convertToString(load.get("DifferentiatedCarecommunityartdistributionpeerled")));
			prs.setDifferentiatedCareexpress(convertToString(load.get("DifferentiatedCareexpress")));
			prs.setDifferentiatedCarefacilityartdistributiongroup(convertToString(load.get("DifferentiatedCarefacilityartdistributiongroup")));
			prs.setDifferentiatedCarefasttrack(convertToString(load.get("DifferentiatedCarefasttrack")));
			prs.setDifferentiatedCarestandardcare(convertToString(load.get("DifferentiatedCarestandardcare")));
			prs.setGenderFemale(convertToString(load.get("GenderFemale")));
			prs.setGenderMale(convertToString(load.get("GenderMale")));
			prs.setLate(convertToString(load.get("late")));
			prs.setLate_last10(convertToString(load.get("late_last10")));
			prs.setLate_last3(convertToString(load.get("late_last3")));
			prs.setLate_last5(convertToString(load.get("late_last5")));
			prs.setLate_rate(convertToString(load.get("late_rate")));
			prs.setLate28(convertToString(load.get("late28")));
			prs.setLate28_rate(convertToString(load.get("late28_rate")));
			prs.setMaritalStatusDivorced(convertToString(load.get("MaritalStatusDivorced")));
			prs.setMaritalStatusMarried(convertToString(load.get("MaritalStatusMarried")));
			prs.setMaritalStatusMinor(convertToString(load.get("MaritalStatusMinor")));
			prs.setMaritalStatusOther(convertToString(load.get("MaritalStatusOther")));
			prs.setMaritalStatusPolygamous(convertToString(load.get("MaritalStatusPolygamous")));
			prs.setMaritalStatusSingle(convertToString(load.get("MaritalStatusSingle")));
			prs.setMaritalStatusWidow(convertToString(load.get("MaritalStatusWidow")));
			prs.setMonthApr(convertToString(load.get("MonthApr")));
			prs.setMonthAug(convertToString(load.get("MonthAug")));
			prs.setMonthDec(convertToString(load.get("MonthDec")));
			prs.setMonthFeb(convertToString(load.get("MonthFeb")));
			prs.setMonthJan(convertToString(load.get("MonthJan")));
			prs.setMonthJul(convertToString(load.get("MonthJul")));
			prs.setMonthJun(convertToString(load.get("MonthJun")));
			prs.setMonthMar(convertToString(load.get("MonthMar")));
			prs.setMonthMay(convertToString(load.get("MonthMay")));
			prs.setMonthNov(convertToString(load.get("MonthNov")));
			prs.setMonthOct(convertToString(load.get("MonthOct")));
			prs.setMonthSep(convertToString(load.get("MonthSep")));
			prs.setMost_recent_art_adherencefair(convertToString(load.get("most_recent_art_adherencefair")));
			prs.setMost_recent_art_adherencegood(convertToString(load.get("most_recent_art_adherencegood")));
			prs.setMost_recent_art_adherencepoor(convertToString(load.get("most_recent_art_adherencepoor")));
			prs.setN_appts(convertToString(load.get("n_appts")));
			prs.setNextAppointmentDate(convertToString(load.get("NextAppointmentDate")));
			prs.setNum_hiv_regimens(convertToString(load.get("num_hiv_regimens")));
			prs.setOptimizedHIVRegimenNo(convertToString(load.get("OptimizedHIVRegimenNo")));
			prs.setOptimizedHIVRegimenYes(convertToString(load.get("OptimizedHIVRegimenYes")));
			prs.setPregnantno(convertToString(load.get("Pregnantno")));
			prs.setPregnantNR(convertToString(load.get("PregnantNR")));
			prs.setPregnantyes(convertToString(load.get("Pregnantyes")));
			prs.setStabilityAssessmentStable(convertToString(load.get("StabilityAssessmentStable")));
			prs.setStabilityAssessmentUnstable(convertToString(load.get("StabilityAssessmentUnstable")));
			prs.setTimeOnArt(convertToString(load.get("timeOnArt")));
			prs.setUnscheduled_rate(convertToString(load.get("unscheduled_rate")));
			prs.setVisit_1(convertToString(load.get("visit_1")));

		} catch(Exception ex) {
			System.err.println("IIT ML: Got error extracting payload variables for debug: " + ex.getMessage());
			ex.printStackTrace();
		}

		return(prs);
	}

	/**
	 * Converts any input object into a string and returns "" if it is not possible
	 * @param input -- input Object
	 * @return String
	 */
	private String convertToString(Object input) {
		if (input == null) {
			return "";
		}
	
		if (input instanceof String || input instanceof Integer || input instanceof Double ||
			input instanceof Long || input instanceof Float) {
			return input.toString();
		}
	
		return "";
	}

	/**
	 * Get the current memory consumption in MB
	 * @return long - the RAM usage in MB
	 */
	public long getMemoryConsumption() {
		long ret = 0L;
		final long MEGABYTE = 1024L * 1024L;

		// Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        if(debugMode) System.err.println("IIT ML: Used memory is bytes: " + memory);

		// get the MB
		ret = memory / MEGABYTE;
        if(debugMode) System.err.println("IIT ML: Used memory is megabytes: " + ret);

		return(ret);
	}

	// START variable calculations
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
					java.time.LocalDate artLocal = dateToLocalDate(artStartDate);
					java.time.LocalDate nowLocal = dateToLocalDate(now);
					long months = Math.abs(ChronoUnit.MONTHS.between(nowLocal, artLocal));
					ret = months;
				}
			}
		}
		return(ret);
	}

	/**
	 * Gets if the patient has an optimized HIV Regimen
	 * @param patientPredictionVariables
	 * @param pharmacy
	 */
	private void getOptimizedHIVRegimen(SimpleObject patientPredictionVariables, List<List<Object>> pharmacy) {
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
								patientPredictionVariables.put("OptimizedHIVRegimenYes", 1);
								patientPredictionVariables.put("OptimizedHIVRegimenNo", 0);
							} else {
								patientPredictionVariables.put("OptimizedHIVRegimenYes", 0);
								patientPredictionVariables.put("OptimizedHIVRegimenNo", 1);
							}
						}
					}
				} else {
					patientPredictionVariables.put("OptimizedHIVRegimenYes", -10000.0F);
					patientPredictionVariables.put("OptimizedHIVRegimenNo", -10000.0F);
				}
			}
		}
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
			
	/**
	 * Check if patient is breastfeeding
	 * @param patientPredictionVariables
	 * @param visits
	 * @param gender
	 * @param Age
	 */
	private void getBreastFeeding(SimpleObject patientPredictionVariables, List<List<Object>> visits, Integer gender, Long Age) {

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(11) != null) {
						String isBreastFeeding = (String) visitObject.get(11);
						// Gender 1: Male, Gender 2: Female
						if (isBreastFeeding.trim().equalsIgnoreCase("yes")) {
							patientPredictionVariables.put("Breastfeedingno", 0);
							patientPredictionVariables.put("Breastfeedingyes", 1);
						}
						if (isBreastFeeding.trim().equalsIgnoreCase("no")) {
							patientPredictionVariables.put("Breastfeedingno", 1);
							patientPredictionVariables.put("Breastfeedingyes", 0);
						}
					} else {
						patientPredictionVariables.put("Breastfeedingno", -10000.0);
						patientPredictionVariables.put("Breastfeedingyes", -10000.0);
					}
				}
			}
			patientPredictionVariables.put("BreastfeedingNR", 0);
		} else {
			patientPredictionVariables.put("BreastfeedingNR", 1);
		}
	}

	/**
	 * Check whether the patient is pregnant
	 * @param patientPredictionVariables
	 * @param visits
	 * @param gender
	 * @param Age
	 */
	private void getPregnant(SimpleObject patientPredictionVariables, List<List<Object>> visits, Integer gender, Long Age) {

		if(isFemaleOfChildBearingAge(gender, Age)) {
			if (visits != null) {
				// Get the last visit
				if (visits.size() > 0) {
					List<Object> visitObject = visits.get(visits.size() - 1);
					if (visitObject.get(6) != null) {
						String isPregnant = (String) visitObject.get(6);
						// Gender 1: Male, Gender 2: Female
						if (isPregnant.trim().equalsIgnoreCase("yes")) {
							patientPredictionVariables.put("Pregnantno", 0);
							patientPredictionVariables.put("Pregnantyes", 1);
						}
						if (isPregnant.trim().equalsIgnoreCase("no")) {
							patientPredictionVariables.put("Pregnantno", 1);
							patientPredictionVariables.put("Pregnantyes", 0);
						}
					} else {
						patientPredictionVariables.put("Pregnantno", -10000.0F);
						patientPredictionVariables.put("Pregnantyes", -10000.0F);
					}
				}
			}
			patientPredictionVariables.put("PregnantNR", 0);
		} else {
			patientPredictionVariables.put("PregnantNR", 1);
		}
	}

	/**
	 * Gets the most recent ART adherence for the patient
	 * @param patientPredictionVariables
	 * @param visits
	 */
	private void getMostRecentArtAdherence(SimpleObject patientPredictionVariables, List<List<Object>> visits) {
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
							if(debugMode) System.err.println("IIT ML: Position of 'ART': " + i);
							artPos = i;
							break;
						}
					}

					if(artPos > -1) {
						// We found ART adherence is covered we get the status
						if (visitObject.get(9) != null) {
							String adherenceString = (String) visitObject.get(9);
							if(debugMode) System.err.println("IIT ML: Adherence full string: " + adherenceString);
							String[] adherenceTokens = adherenceString.split("\\|");
							if(debugMode) System.err.println("IIT ML: Adherence tokens: " + Arrays.toString(adherenceTokens));
							if(adherenceTokens.length > 0) {
								for (int i = 0; i < adherenceTokens.length; i++) {
									if(i == artPos) {
										if (adherenceTokens[i].trim().equalsIgnoreCase("fair")) {
											patientPredictionVariables.put("most_recent_art_adherencefair", 1);
										} else if (adherenceTokens[i].trim().equalsIgnoreCase("good")) {
											patientPredictionVariables.put("most_recent_art_adherencegood", 1);
										} else if (adherenceTokens[i].trim().equalsIgnoreCase("poor")) {
											patientPredictionVariables.put("most_recent_art_adherencepoor", 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
			
	/**
	 * Get the stability assessment of the patient
	 * @param visits
	 */
	private void getStabilityAssessment(SimpleObject patientPredictionVariables, List<List<Object>> visits) {
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(8) != null) {
					String differentiatedCare = (String) visitObject.get(8);
					if(differentiatedCare.trim().equalsIgnoreCase("stable")) {
						patientPredictionVariables.put("StabilityAssessmentStable", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("not stable")) {
						patientPredictionVariables.put("StabilityAssessmentUnstable", 1);
					}
				}
			}
		}
	}		

	/**
	 * Gets differentiated care of the patient
	 * @param visits
	 * @return
	 */
	private void getDifferentiatedCare(SimpleObject patientPredictionVariables, List<List<Object>> visits) {
		if(visits != null) {
			// Get the last visit
			if (visits.size() > 0) {
				List<Object> visitObject = visits.get(visits.size() - 1);
				if (visitObject.get(7) != null) {
					String differentiatedCare = (String) visitObject.get(7);
					if(differentiatedCare.trim().equalsIgnoreCase("Standard Care")) {
						patientPredictionVariables.put("DifferentiatedCarestandardcare", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("Fast Track")) {
						patientPredictionVariables.put("DifferentiatedCarefasttrack", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("Facility ART Distribution Group")) {
						patientPredictionVariables.put("DifferentiatedCarefacilityartdistributiongroup", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("Express")) {
						patientPredictionVariables.put("DifferentiatedCareexpress", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("Community ART Distribution - Peer Led")) {
						patientPredictionVariables.put("DifferentiatedCarecommunityartdistributionpeerled", 1);
					} else if(differentiatedCare.trim().equalsIgnoreCase("Community ART Distribution - HCW Led")) {
						patientPredictionVariables.put("DifferentiatedCarecommunityartdistributionhcwled", 1);
					}
				}
			}
		}
	}

	/**
	 * Get marital status of patient
	 * @param demographics
	 * @param Age
	 * @return
	 */
	private void getMaritalStatus(SimpleObject patientPredictionVariables, List<List<Object>> demographics, Long Age) {
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> maritalObject = demographics.get(demographics.size() - 1);
				if(maritalObject.get(4) != null) {
					String marital = (String) maritalObject.get(4);
					if (Age <= 15) {
						patientPredictionVariables.put("MaritalStatusMinor", 1);
					} else if ((marital.trim().equalsIgnoreCase("divorced") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("separated") && Age > 15)
					) {
						patientPredictionVariables.put("MaritalStatusDivorced", 1);
					} else if (!marital.trim().equalsIgnoreCase("single") &&
							!marital.trim().equalsIgnoreCase("divorced") &&
							!marital.trim().equalsIgnoreCase("widow") &&
							!marital.trim().equalsIgnoreCase("separated") &&
							!marital.trim().equalsIgnoreCase("married") &&
							!marital.trim().equalsIgnoreCase("monogamous") &&
							!marital.trim().equalsIgnoreCase("cohabiting") &&
							!marital.trim().equalsIgnoreCase("polygamous") &&
							Age > 15
					) {
						patientPredictionVariables.put("MaritalStatusOther", 1);
					} else if (marital.trim().equalsIgnoreCase("single") && Age > 15) {
						patientPredictionVariables.put("MaritalStatusSingle", 1);
					} else if (marital.trim().equalsIgnoreCase("widow") && Age > 15) {
						patientPredictionVariables.put("MaritalStatusWidow", 1);
					} else if (marital.trim().equalsIgnoreCase("polygamous") && Age > 15) {
						patientPredictionVariables.put("MaritalStatusPolygamous", 1);
					} else if ((marital.trim().equalsIgnoreCase("married") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("monogamous") && Age > 15) ||
							(marital.trim().equalsIgnoreCase("cohabiting") && Age > 15)
					) {
						patientPredictionVariables.put("MaritalStatusMarried", 1);
					}
				}
			}
		}
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
					java.time.LocalDate dobLocal = dateToLocalDate(DOB);
					java.time.LocalDate nowLocal = dateToLocalDate(now);
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

	private Integer getGender(List<List<Object>> demographics) {
		Integer ret = 0;
		if(demographics != null) {
			// Get the last appointment
			if (demographics.size() > 0) {
				List<Object> genderObject = demographics.get(demographics.size() - 1);
				if(genderObject.get(1) != null) {
					String gender = (String) genderObject.get(1);
					if (gender.trim().equalsIgnoreCase("female")) {
						ret = 2;
					} else if (gender.trim().equalsIgnoreCase("male")) {
						ret = 1;
					}
				}
			}
		}
		return(ret);
	}

	private void getPayloadMonthOfYear(SimpleObject patientPredictionVariables, List<Appointment> appointments) {
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int monthOfYear = getMonthOfYear(NAD);
				switch (monthOfYear) {
					case Calendar.JANUARY:
						patientPredictionVariables.put("MonthJan", 1);
						break;
					case Calendar.FEBRUARY:
						patientPredictionVariables.put("MonthFeb", 1);
						break;
					case Calendar.MARCH:
						patientPredictionVariables.put("MonthMar", 1);
						break;
					case Calendar.APRIL:
						patientPredictionVariables.put("MonthApr", 1);
						break;
					case Calendar.MAY:
						patientPredictionVariables.put("MonthMay", 1);
						break;
					case Calendar.JUNE:
						patientPredictionVariables.put("MonthJun", 1);
						break;
					case Calendar.JULY:
						patientPredictionVariables.put("MonthJul", 1);
						break;
					case Calendar.AUGUST:
						patientPredictionVariables.put("MonthAug", 1);
						break;
					case Calendar.SEPTEMBER:
						patientPredictionVariables.put("MonthSep", 1);
						break;
					case Calendar.OCTOBER:
						patientPredictionVariables.put("MonthOct", 1);
						break;
					case Calendar.NOVEMBER:
						patientPredictionVariables.put("MonthNov", 1);
						break;
					case Calendar.DECEMBER:
						patientPredictionVariables.put("MonthDec", 1);
						break;
					default:
						System.err.println("IIT ML: ERROR: Month of year not found");
						break;
				}
			}
		}
	}

	public static void getPayloadDayOfWeek(SimpleObject patientPredictionVariables, List<Appointment> appointments) {
		if(appointments != null) {
			// Get the last appointment
			if(appointments.size() > 0) {
				Appointment latestAppointment = appointments.get(appointments.size() - 1);
				Date NAD = latestAppointment.getAppointmentDate();
				int dayOfWeek = getDayOfWeek(NAD);
				switch (dayOfWeek) {
					case Calendar.MONDAY:
						patientPredictionVariables.put("DayMon", 1);
						break;
					case Calendar.TUESDAY:
						patientPredictionVariables.put("DayTue", 1);
						break;
					case Calendar.WEDNESDAY:
						patientPredictionVariables.put("DayWed", 1);
						break;
					case Calendar.THURSDAY:
						patientPredictionVariables.put("DayThu", 1);
						break;
					case Calendar.FRIDAY:
						patientPredictionVariables.put("DayFri", 1);
						break;
					case Calendar.SATURDAY:
						patientPredictionVariables.put("DaySat", 1);
						break;
					case Calendar.SUNDAY:
						patientPredictionVariables.put("DaySun", 1);
						break;
					default:
						System.err.println("IIT ML: ERROR: Day of week not found");
						break;
				}	
			}
		}
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
		SimpleObject ret = SimpleObject.create("result", -10000);

		if(treatments != null) {
			Set<String> drugs = new HashSet<>(); // This will ensure we get unique drugs
			for (Treatment in : treatments) {
				if(debugMode) System.err.println("IIT ML: got drug: " + in.getDrug() + " Treatment Type: " + in.getTreatmentType());
				if (in.getDrug() != null && in.getTreatmentType() != null && !in.getTreatmentType().trim().equalsIgnoreCase("Prophylaxis")) {
					String drug = in.getDrug().trim().toLowerCase();
					drugs.add(drug);
				}
			}
			ret.put("result", drugs.size() > 0 ? drugs.size() : -10000);
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
						// Date updateDate = (Date) updateObject.get(1);
						LocalDateTime updateDate = (LocalDateTime) updateObject.get(1);
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						ret = sdf.format(updateDate);
					}
				}
			}
		} catch(Exception ex) {
			if(debugMode) System.err.println("IIT ML: Error getting the last time the DWAPI ETL was updated");
			ex.printStackTrace();
		}
		return(ret);
	}
	// END variable calculations
}
