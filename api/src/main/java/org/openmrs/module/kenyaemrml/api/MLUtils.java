package org.openmrs.module.kenyaemrml.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

public class MLUtils {
	
	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	
	public static String MODEL_ID_REQUEST_VARIABLE = "modelId";
	
	public static String FACILITY_ID_REQUEST_VARIABLE = "facilityId";
	
	public static String ENCOUNTER_DATE_REQUEST_VARIABLE = "encounterDate";
	
	public static String MODEL_PARAMETER_VALUE_OBJECT_KEY = "variableValues";
	
	public static String MODEL_CONFIG_OBJECT_KEY = "modelConfigs";
	
	public static String[] HTS_FACILITY_PROFILE_VARIABLES = { "births", "pregnancies", "literacy", "poverty", "anc", "pnc",
	        "sba", "hiv_prev", "hiv_count", "condom", "intercourse", "in_union", "circumcision", "partner_away",
	        "partner_men", "partner_women", "sti", "pop" };
	
	public static String[] IIT_FACILITY_PROFILE_VARIABLES = { "SumTXCurr", "births", "pregnancies", "literacy", "poverty",
	        "anc", "pnc", "sba", "hiv_prev", "hiv_count", "condom", "intercourse", "in_union", "circumcision",
	        "partner_away", "partner_men", "partner_women", "sti", "pop", "keph_level_nameLevel.2",
	        "keph_level_nameLevel.3", "keph_level_nameLevel.4", "keph_level_nameLevel.5", "keph_level_nameLevel.6",
	        "owner_typeFaith", "owner_typeNGO", "owner_typePrivate", "owner_typePublic" };
	
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
			JsonNode keyValueNode = field.getValue();
			Object keyValue = mapper.convertValue(keyValueNode, Object.class);
			modelParams.put(keyId, keyValue);
		}
		
		JSONObject profile = getHTSFacilityProfile("FacilityCode", facilityMflCode, getHTSFacilityCutOffs());
		
		for (int i = 0; i < HTS_FACILITY_PROFILE_VARIABLES.length; i++) {
			modelParams.put(HTS_FACILITY_PROFILE_VARIABLES[i], profile.get(HTS_FACILITY_PROFILE_VARIABLES[i]));
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
			JsonNode keyValueNode = field.getValue();
			Object keyValue = mapper.convertValue(keyValueNode, Object.class);
			modelParams.put(keyId, keyValue);
		}
		
		JSONObject profile = getHTSFacilityProfile("FacilityCode", facilityMflCode, getIITFacilityCutOffs());
		
		for (int i = 0; i < IIT_FACILITY_PROFILE_VARIABLES.length; i++) {
			modelParams.put(IIT_FACILITY_PROFILE_VARIABLES[i], profile.get(IIT_FACILITY_PROFILE_VARIABLES[i]));
		}
		
		ModelInputFields inputFields = new ModelInputFields();
		inputFields.setFields(modelParams);
		return inputFields;
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
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream(
		    "hts/hts_ml_facility_cut_off_national_sept_2024.json");
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
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream(
		    "iit/iit_ml_facility_cut_off_national_dec_2023.json");
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
	 * Get the current HTS thresholds for this facility from the matrix file
	 * 
	 * @return SimpleObject - The thresholds
	 */
	public static SimpleObject getHTSThresholds() {
		SimpleObject ret = SimpleObject.create("Very_High", 0.00, "High", 0.00, "Medium", 0.00);
		// Fetch from global properties incase the matrix has no thresholds

		// Low Risk
		String lowRiskThreshold = "kenyaemrml.hts.lowRiskThreshold";
		GlobalProperty gpLowRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(lowRiskThreshold);

		if(gpLowRiskThreshold != null) {
			String stLowRiskThreshold = gpLowRiskThreshold.getPropertyValue();
			if(stLowRiskThreshold != null) {
				try {
					Double numLowRiskThreshold = Double.valueOf(stLowRiskThreshold.trim());
					ret.put("Medium", numLowRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// Medium Risk
		String mediumRiskThreshold = "kenyaemrml.hts.mediumRiskThreshold";
		GlobalProperty gpMediumRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(mediumRiskThreshold);

		if(gpMediumRiskThreshold != null) {
			String stMediumRiskThreshold = gpMediumRiskThreshold.getPropertyValue();
			if(stMediumRiskThreshold != null) {
				try {
					Double numMediumRiskThreshold = Double.valueOf(stMediumRiskThreshold.trim());
					ret.put("High", numMediumRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// High Risk
		String highRiskThreshold = "kenyaemrml.hts.highRiskThreshold";
		GlobalProperty gpHighRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(highRiskThreshold);

		if(gpHighRiskThreshold != null) {
			String stHighRiskThreshold = gpHighRiskThreshold.getPropertyValue();
			if(stHighRiskThreshold != null) {
				try {
					Double numHighRiskThreshold = Double.valueOf(stHighRiskThreshold.trim());
					ret.put("Very_High", numHighRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// Check if the matrix has the thresholds
		String facilityMflCode = getDefaultMflCode();
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream("hts/hts_ml_facility_cut_off_national_sept_2024.json");
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(stream);
			for (JsonNode node : rootNode) {
				if (node.get("FacilityCode").asText().trim().equalsIgnoreCase(facilityMflCode)) {
					// Extract values for Very_High, High, and Medium
					double veryHigh = node.get("Very_High").asDouble();
					double high = node.get("High").asDouble();
					double medium = node.get("Medium").asDouble();
	
					// Create object containing the values
					return SimpleObject.create(
							"Very_High", veryHigh,
							"High", high,
							"Medium", medium
					);
				}
			}
		} catch (Exception e) {
			System.err.println("HTS ML Error: Failed to get thresholds from matrix. We get from globals: " + e.getMessage());
			// e.printStackTrace();
		}
		return (ret);
	}
	
	/**
	 * Get the current IIT thresholds for this facility from the matrix file
	 * 
	 * @return SimpleObject - The thresholds
	 */
	public static SimpleObject getIITThresholds() {
		SimpleObject ret = SimpleObject.create("Very_High", 0.00, "High", 0.00, "Medium", 0.00);
		// Fetch from global properties incase the matrix has no thresholds

		// Low Risk
		String lowRiskThreshold = "kenyaemrml.iit.lowRiskThreshold";
		GlobalProperty gpLowRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(lowRiskThreshold);

		if(gpLowRiskThreshold != null) {
			String stLowRiskThreshold = gpLowRiskThreshold.getPropertyValue();
			if(stLowRiskThreshold != null) {
				try {
					Double numLowRiskThreshold = Double.valueOf(stLowRiskThreshold.trim());
					ret.put("Medium", numLowRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// Medium Risk
		String mediumRiskThreshold = "kenyaemrml.iit.mediumRiskThreshold";
		GlobalProperty gpMediumRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(mediumRiskThreshold);

		if(gpMediumRiskThreshold != null) {
			String stMediumRiskThreshold = gpMediumRiskThreshold.getPropertyValue();
			if(stMediumRiskThreshold != null) {
				try {
					Double numMediumRiskThreshold = Double.valueOf(stMediumRiskThreshold.trim());
					ret.put("High", numMediumRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// High Risk
		String highRiskThreshold = "kenyaemrml.iit.highRiskThreshold";
		GlobalProperty gpHighRiskThreshold = Context.getAdministrationService().getGlobalPropertyObject(highRiskThreshold);

		if(gpHighRiskThreshold != null) {
			String stHighRiskThreshold = gpHighRiskThreshold.getPropertyValue();
			if(stHighRiskThreshold != null) {
				try {
					Double numHighRiskThreshold = Double.valueOf(stHighRiskThreshold.trim());
					ret.put("Very_High", numHighRiskThreshold);
				} catch(Exception e) {}
			}
		}

		// Check if the matrix has the thresholds
		String facilityMflCode = getDefaultMflCode();
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream("iit/iit_ml_facility_cut_off_national_dec_2023.json");
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(stream);
			for (JsonNode node : rootNode) {
				if (node.get("FacilityCode").asText().trim().equalsIgnoreCase(facilityMflCode)) {
					// Extract values for Very_High, High, and Medium
					double veryHigh = node.get("Very_High").asDouble();
					double high = node.get("High").asDouble();
					double medium = node.get("Medium").asDouble();
	
					// Create object containing the values
					return SimpleObject.create(
							"Very_High", veryHigh,
							"High", high,
							"Medium", medium
					);
				}
			}
		} catch (Exception e) {
			System.err.println("IIT ML: Failed to get thresholds from matrix. We get from globals: " + e.getMessage());
			// e.printStackTrace();
		}
		return (ret);
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
	 * Helper method for getting a facility profile by facility name
	 * 
	 * @param propertyValue
	 * @param propertyName
	 * @return
	 */
	public static JSONObject getIITFacilityProfile(String propertyName, String propertyValue, JSONArray facilityCutOffArray) {
		
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
			JSONArray facilitiesMap = (JSONArray) obj;
			
			return facilitiesMap;
		}
		catch (Exception e) {
			System.err.println("HTS ML ERROR: Failed to parse the facility matrix file: " + e.getMessage());
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
			JSONArray facilitiesMap = (JSONArray) obj;
			
			return facilitiesMap;
		}
		catch (Exception e) {
			System.err.println("IIT ML ERROR: Failed to parse the facility matrix file: " + e.getMessage());
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
			boolean isDebugMode = modelConfigs.has("debug") && modelConfigs.get("debug").asText().equals("true") ? true
			        : false;
			
			if (facilityMflCode.equals("")) { // default to the default facility configured in the EMR
				facilityMflCode = MLUtils.getDefaultMflCode();
			}
			
			System.out.println("OrderEntry Module: Got the facility MFL as: " + facilityMflCode);
			
			String modelId = modelConfigs.get(MLUtils.MODEL_ID_REQUEST_VARIABLE).asText();
			String encounterDate = modelConfigs.get(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText();
			
			if (StringUtils.isBlank(facilityMflCode) || StringUtils.isBlank(modelId) || StringUtils.isBlank(encounterDate)) {
				System.err.println("Error: The service requires model, date, and facility information");
				return "";
			}
			
			JSONObject profile = MLUtils.getHTSFacilityProfile("FacilityCode", facilityMflCode,
			    MLUtils.getIITFacilityCutOffs());
			
			if (profile == null) {
				System.err.println("Error: The facility provided: " + facilityMflCode
				        + " currently doesn't have an IIT cut-off profile. Provide an appropriate facility");
				return "";
			}
			
			ModelInputFields inputFields = MLUtils.extractIITVariablesFromRequestBody(requestBody, facilityMflCode,
			    encounterDate);
			
			// System.err.println("IIT Score: Using input fields: " + inputFields);
			
			ScoringResult scoringResult = modelService.iitscore(modelId, facilityMflCode, encounterDate, inputFields,
			    isDebugMode);
			
			if (scoringResult != null) {
				ObjectMapper mapper = new ObjectMapper();
				mlScore = mapper.writeValueAsString(scoringResult);
				// System.out.println("ITT ML - Got IIT Score JSON as: " + mlScore);
			}
		}
		catch (Exception ex) {
			System.err.println("Error: could not get the IIT ML score: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return (mlScore);
	}
	
	/**
	 * Gets the sha256 hash of a string
	 */
	public static String getSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // Convert bytes to hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
	
	public static String getModuleVersion(String moduleId) {
		// Retrieve the module by its ID
		Module module = ModuleFactory.getModuleById(moduleId);
		
		if (module != null) {
			// Return the version of the module
			return module.getVersion();
		}
		
		return "Module not found!";
	}
	
}
