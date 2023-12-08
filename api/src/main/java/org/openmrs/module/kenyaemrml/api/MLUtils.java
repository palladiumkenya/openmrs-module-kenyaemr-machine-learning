package org.openmrs.module.kenyaemrml.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;


public class MLUtils {
	
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	
	public static String MODEL_ID_REQUEST_VARIABLE = "modelId";
	
	public static String FACILITY_ID_REQUEST_VARIABLE = "facilityId";
	
	public static String ENCOUNTER_DATE_REQUEST_VARIABLE = "encounterDate";
	
	public static String MODEL_PARAMETER_VALUE_OBJECT_KEY = "variableValues";
	
	public static String MODEL_CONFIG_OBJECT_KEY = "modelConfigs";

	public static String[] FACILITY_PROFILE_VARIABLES = { "births", "pregnancies", "literacy", "poverty", "anc", "pnc", "sba", "hiv_prev", "hiv_count", "condom", "intercourse", "in_union", "circumcision", "partner_away", "partner_men", "partner_women", "sti", "fb" };

	public static String[] IIT_FACILITY_PROFILE_VARIABLES = {
			"SumTXCurr",
			"births",
			"pregnancies",
			"literacy",
			"poverty",
			"anc",
			"pnc",
			"sba",
			"hiv_prev",
			"hiv_count",
			"condom",
			"intercourse",
			"in_union",
			"circumcision",
			"partner_away",
			"partner_men",
			"partner_women",
			"sti",
			"pop",
			"keph_level_nameLevel.2",
			"keph_level_nameLevel.3",
			"keph_level_nameLevel.4",
			"keph_level_nameLevel.5",
			"keph_level_nameLevel.6",
			"owner_typeFaith",
			"owner_typeNGO",
			"owner_typePrivate",
			"owner_typePublic" };

	public static String fetchRequestBody(BufferedReader reader) {
		String requestBodyJsonStr = "";
		try {
			String output = "";
			while ((output = reader.readLine()) != null) {
				requestBodyJsonStr += output;
			}
		}
		catch (Exception e) {
			System.err.println("Exception getting request body: " + e.getMessage());
		}
		return requestBodyJsonStr;
	}
	
	/**
	 * Extracts HTS model variables from request body Variable values are of float type
	 * 
	 * @param requestBodyString
	 * @return Request body expects sample structure as below { "modelConfigs": {
	 *         "modelId":"hts_xgb", "encounterDate":"2021-06-05",
	 *         "facilityId":"Good Shepherd Ang'iya", "debug":"false" }, "variableValues": {
	 *         "AgeAtTest": 20, "MonthsSinceLastTest": 45, "GenderMale": 1, "GenderFemale": 0,
	 *         "KeyPopulationTypeGP": 1, "KeyPopulationTypeSW": 0, "MaritalStatusMarried": 0,
	 *         "MaritalStatusDivorced": 0, "MaritalStatusPolygamous": 1, "MaritalStatusWidowed": 0,
	 *         "MaritalStatusMinor": 0, "PatientDisabledNotDisabled": 1, "PatientDisabledDisabled":
	 *         0, "EverTestedForHIVYes": 1, "EverTestedForHIVNo": 0, "ClientTestedAsIndividual": 1,
	 *         "ClientTestedAsCouple": 0, "EntryPointVCT": 0, "EntryPointOPD": 0, "EntryPointMTC":
	 *         0, "EntryPointIPD": 0, "EntryPointMOBILE": 1, "EntryPointOther": 0, "EntryPointHB":
	 *         0, "EntryPointPEDS": 0, "TestingStrategyVCT": 1, "TestingStrategyHB": 0,
	 *         "TestingStrategyMOBILE": 0, "TestingStrategyHP": 0, "TestingStrategyNP": 0,
	 *         "TBScreeningNoPresumedTB": 0, "TBScreeningPresumed TB": 1, "ClientSelfTestedNo": 1,
	 *         "ClientSelfTestedYes": 0 } }
	 */
	public static ModelInputFields extractHTSCaseFindingVariablesFromRequestBody(String requestBodyString,
	        String facilityMflCode, String encounterDateString) {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tree = null;
		Map<String, Object> modelParams = new HashMap<String, Object>();
		try {
			tree = (ObjectNode) mapper.readTree(requestBodyString);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * the request body structure should look like { "modelConfigs": {"modelId":"testModel",
		 * "facilityId":"testFacility"}, "variableValues": {[key-value-pairs]} }
		 */
		
		// get content
		ObjectNode variableValues = (ObjectNode) tree.get(MODEL_PARAMETER_VALUE_OBJECT_KEY);
		
		Iterator<Map.Entry<String, JsonNode>> it = variableValues.getFields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> field = it.next();
			String keyId = field.getKey();
			int keyValue = field.getValue().getIntValue();
			modelParams.put(keyId, keyValue);
		}
		
		prepareEncounterModelParams(encounterDateString, modelParams);
		// add facility cut off
		
		JSONObject profile = getHTSFacilityProfile("FacilityCode", facilityMflCode, getHTSFacilityCutOffs());
		
		for (int i = 0; i < FACILITY_PROFILE_VARIABLES.length; i++) {
			modelParams.put(FACILITY_PROFILE_VARIABLES[i], profile.get(FACILITY_PROFILE_VARIABLES[i]));
		}
		
		ModelInputFields inputFields = new ModelInputFields();
		inputFields.setFields(modelParams);
		return inputFields;
	}

	/**
	 * Extracts IIT model variables from request body Variable values are of float type
	 * 
	 * @param requestBodyString
	 * @return Request body expects sample structure as below { "modelConfigs": {
	 *         "modelId":"hts_xgb", "encounterDate":"2021-06-05",
	 *         "facilityId":"Good Shepherd Ang'iya", "debug":"false" }, "variableValues": {
	 *         "AgeAtTest": 20, "MonthsSinceLastTest": 45, "GenderMale": 1, "GenderFemale": 0,
	 *         "KeyPopulationTypeGP": 1, "KeyPopulationTypeSW": 0, "MaritalStatusMarried": 0,
	 *         "MaritalStatusDivorced": 0, "MaritalStatusPolygamous": 1, "MaritalStatusWidowed": 0,
	 *         "MaritalStatusMinor": 0, "PatientDisabledNotDisabled": 1, "PatientDisabledDisabled":
	 *         0, "EverTestedForHIVYes": 1, "EverTestedForHIVNo": 0, "ClientTestedAsIndividual": 1,
	 *         "ClientTestedAsCouple": 0, "EntryPointVCT": 0, "EntryPointOPD": 0, "EntryPointMTC":
	 *         0, "EntryPointIPD": 0, "EntryPointMOBILE": 1, "EntryPointOther": 0, "EntryPointHB":
	 *         0, "EntryPointPEDS": 0, "TestingStrategyVCT": 1, "TestingStrategyHB": 0,
	 *         "TestingStrategyMOBILE": 0, "TestingStrategyHP": 0, "TestingStrategyNP": 0,
	 *         "TBScreeningNoPresumedTB": 0, "TBScreeningPresumed TB": 1, "ClientSelfTestedNo": 1,
	 *         "ClientSelfTestedYes": 0 } }
	 */
	public static ModelInputFields extractIITVariablesFromRequestBody(String requestBodyString, String facilityMflCode, String encounterDateString) {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tree = null;
		Map<String, Object> modelParams = new HashMap<String, Object>();
		try {
			tree = (ObjectNode) mapper.readTree(requestBodyString);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * the request body structure should look like { "modelConfigs": {"modelId":"testModel",
		 * "facilityId":"testFacility"}, "variableValues": {[key-value-pairs]} }
		 */
		
		// get content
		ObjectNode variableValues = (ObjectNode) tree.get(MODEL_PARAMETER_VALUE_OBJECT_KEY);
		
		Iterator<Map.Entry<String, JsonNode>> it = variableValues.getFields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> field = it.next();
			String keyId = field.getKey();
			int keyValue = field.getValue().getIntValue();
			modelParams.put(keyId, keyValue);
		}
		
		prepareEncounterModelParams(encounterDateString, modelParams);
		// add facility cut off
		
		JSONObject profile = getHTSFacilityProfile("FacilityCode", facilityMflCode, getIITFacilityCutOffs());
		
		for (int i = 0; i < IIT_FACILITY_PROFILE_VARIABLES.length; i++) {
			modelParams.put(IIT_FACILITY_PROFILE_VARIABLES[i], profile.get(IIT_FACILITY_PROFILE_VARIABLES[i]));
		}
		
		ModelInputFields inputFields = new ModelInputFields();
		inputFields.setFields(modelParams);
		return inputFields;
	}
	
	private static Map<String, Object> prepareEncounterModelParams(String encounterDateString,
	        Map<String, Object> modelParams) {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
		if (StringUtils.isNotBlank(encounterDateString)) {
			Date encDate = null;
			try {
				encDate = sdf.parse(encounterDateString);
				Calendar c = Calendar.getInstance();
				c.setTime(encDate);
				int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
				int month = c.get(Calendar.MONTH);
				Map<String, Object> addedDayOfWeekVariables = setDayOfWeekVariables(dayOfWeek, modelParams);
				Map<String, Object> addedMonthVariables = setMonthVariables(month, addedDayOfWeekVariables);
				return addedMonthVariables;
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return modelParams;
		
	}
	
	/**
	 * Sets the day of the week variables required in the ML model
	 * 
	 * @param dayOfWeek
	 * @param modelParams
	 */
	private static Map<String, Object> setDayOfWeekVariables(int dayOfWeek, Map<String, Object> modelParams) {
		
		String[] daysOfWeek = { "dayofweek1", "dayofweek2", "dayofweek3", "dayofweek4", "dayofweek5", "dayofweek6",
		        "dayofweek7" }; // we use the natural array ordering
		// 1- Monday, 7- Sunday
		for (int i = 0; i < daysOfWeek.length; i++) {
			if (i == dayOfWeek - 1) { // substract 1 to align to array index i.e. 0 = 1-1
				modelParams.put(daysOfWeek[i], 1); // set to 1
			} else {
				modelParams.put(daysOfWeek[i], 0); // set to 0
			}
		}
		return modelParams;
	}
	
	/**
	 * Set month variables
	 * 
	 * @param month
	 * @param modelParams
	 */
	private static Map<String, Object> setMonthVariables(int month, Map<String, Object> modelParams) {
		
		String[] months = { "month_of_test1", "month_of_test2", "month_of_test3", "month_of_test4", "month_of_test5",
		        "month_of_test6", "month_of_test7", "month_of_test8", "month_of_test9", "month_of_test10",
		        "month_of_test11", "month_of_test12" }; // we use the natural array ordering
		
		for (int i = 0; i < months.length; i++) {
			if (i == month) {
				modelParams.put(months[i], 1); // set to 1
			} else {
				modelParams.put(months[i], 0); // set to 0
			}
		}
		return modelParams;
	}
	
	/**
	 * @param requestBodyString
	 * @return
	 */
	/**
	 * Extracts model variables from request body
	 * 
	 * @param requestBodyString
	 * @return
	 */
	public static ObjectNode getModelConfig(String requestBodyString) {
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tree = null;
		try {
			tree = (ObjectNode) mapper.readTree(requestBodyString);
			if (tree.has(MLUtils.MODEL_CONFIG_OBJECT_KEY)) {
				return (ObjectNode) tree.get(MLUtils.MODEL_CONFIG_OBJECT_KEY);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Reads bundled HTS case finding facility profile
	 * 
	 * @return
	 */
	public static String readBundledHtsCasefindingFacilityProfileFile() {
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream("hts/hts_ml_facility_cut_off_national_oct_2023.json");
		ObjectMapper mapper = new ObjectMapper();
		try {
			ArrayNode result = mapper.readValue(stream, ArrayNode.class);
			return result.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads bundled IIT case finding facility profile
	 * 
	 * @return
	 */
	public static String readBundledIITCasefindingFacilityProfileFile() {
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream("iit/iit_ml_facility_cut_off_national_dec_2023.json");
		ObjectMapper mapper = new ObjectMapper();
		try {
			ArrayNode result = mapper.readValue(stream, ArrayNode.class);
			return result.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Helper method for getting a facility profile by facility name
	 * 
	 * @param propertyValue
	 * @param propertyName
	 * @return
	 */
	public static JSONObject getHTSFacilityProfile(String propertyName, String propertyValue, JSONArray facilityCutOffArray) {
		
		if (facilityCutOffArray != null) {
			for (int i = 0; i < facilityCutOffArray.size(); i++) {
				JSONObject o = (JSONObject) facilityCutOffArray.get(i);
				if (o.get(propertyName).toString().equals(propertyValue)) {
					return o;
				}
			}
		}
		return null;
	}
	
	/**
	 * Reading content from bundled mapping json file for hts case finding cut-off
	 * 
	 * @return
	 */
	public static JSONArray getHTSFacilityCutOffs() {
		
		JSONParser jsonParser = new JSONParser();
		try {
			//Read JSON file
			Object obj = jsonParser.parse(readBundledHtsCasefindingFacilityProfileFile());
			JSONArray drugsMap = (JSONArray) obj;
			
			return drugsMap;
			
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Reading content from bundled mapping json file for iit score cut-off
	 * 
	 * @return
	 */
	public static JSONArray getIITFacilityCutOffs() {
		
		JSONParser jsonParser = new JSONParser();
		try {
			//Read JSON file
			Object obj = jsonParser.parse(readBundledIITCasefindingFacilityProfileFile());
			JSONArray drugsMap = (JSONArray) obj;
			
			return drugsMap;
			
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Location getDefaultLocation() {
		KenyaEmrService emrService = Context.getService(KenyaEmrService.class);
		return emrService.getDefaultLocation();
	}

	public static String getDefaultMflCode() {
		KenyaEmrService emrService = Context.getService(KenyaEmrService.class);
		return emrService.getDefaultLocationMflCode();
	}

	/**
	 * Get the IIT ML score using direct method (Does not call the backend)
	 * 
	 * @param payload - the Json payload
	 * @return String - The Json response
	 */
	public static String generateIITMLScore(String payload) {
		String mlScore = "";
		try {
			ModelService modelService = Context.getService(ModelService.class);
			String requestBody = payload;
			ObjectNode modelConfigs = MLUtils.getModelConfig(requestBody);
			String facilityMflCode = modelConfigs.get(MLUtils.FACILITY_ID_REQUEST_VARIABLE).asText();
			boolean isDebugMode = modelConfigs.has("debug") && modelConfigs.get("debug").asText().equals("true") ? true : false;

			if (facilityMflCode.equals("")) { // default to the default facility configured in the EMR
				facilityMflCode = MLUtils.getDefaultMflCode();
			}
			
			String modelId = modelConfigs.get(MLUtils.MODEL_ID_REQUEST_VARIABLE).asText();
			String encounterDate = modelConfigs.get(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText();
			
			if (StringUtils.isBlank(facilityMflCode) || StringUtils.isBlank(modelId) || StringUtils.isBlank(encounterDate)) {
				System.err.println("Error: The service requires model, date, and facility information");
				return "";
			}

			JSONObject profile = MLUtils.getHTSFacilityProfile("FacilityCode", facilityMflCode, MLUtils.getIITFacilityCutOffs());
			
			if (profile == null) {
				System.err.println("Error: The facility provided currently doesn't have an IIT cut-off profile. Provide an appropriate facility");
				return "";
			}

			ModelInputFields inputFields = MLUtils.extractIITVariablesFromRequestBody(requestBody, facilityMflCode, encounterDate);

			// System.err.println("IIT Score: Using input fields: " + inputFields);
			
			ScoringResult scoringResult = modelService.iitscore(modelId, facilityMflCode, encounterDate, inputFields, isDebugMode);

			if(scoringResult != null) {
				ObjectMapper mapper = new ObjectMapper();
				mlScore = mapper.writeValueAsString(scoringResult);
				// System.out.println("ITT ML - Got IIT Score JSON as: " + mlScore);
			}
		}
		catch (Exception ex) {
			System.err.println("Error: could not get the IIT ML score: " + ex.getMessage());
			ex.printStackTrace();
		}

		return(mlScore);
	}
	
}
