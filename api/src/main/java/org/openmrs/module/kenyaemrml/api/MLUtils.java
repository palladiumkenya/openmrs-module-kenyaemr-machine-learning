package org.openmrs.module.kenyaemrml.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
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
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;

public class MLUtils {
	
	public static String MODEL_ID_REQUEST_VARIABLE = "modelId";
	
	public static String FACILITY_ID_REQUEST_VARIABLE = "facilityId";
	
	public static String ENCOUNTER_DATE_REQUEST_VARIABLE = "encounterDate";
	
	public static String MODEL_PARAMETER_VALUE_OBJECT_KEY = "variableValues";
	
	public static String MODEL_CONFIG_OBJECT_KEY = "modelConfigs";
	
	public static String fetchRequestBody(BufferedReader reader) {
		String requestBodyJsonStr = "";
		try {
			
			String output = "";
			while ((output = reader.readLine()) != null) {
				requestBodyJsonStr += output;
			}
			
		}
		catch (IOException e) {
			
			System.out.println("IOException: " + e.getMessage());
			
		}
		return requestBodyJsonStr;
	}
	
	/**
	 * Extracts model variables from request body Variable values are of float type
	 * 
	 * @param requestBodyString
	 * @return
	 */
	public static ModelInputFields extractHTSCaseFindingVariablesFromRequestBody(String requestBodyString) {
		
		//System.out.println("Request body: " + requestBodyString);
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
		ObjectNode modelConfigs = (ObjectNode) tree.get(MODEL_CONFIG_OBJECT_KEY);
		String facilityName = modelConfigs.has(MLUtils.FACILITY_ID_REQUEST_VARIABLE) ? modelConfigs.get(
		    MLUtils.FACILITY_ID_REQUEST_VARIABLE).asText() : "";
		String encounterDateString = modelConfigs.has(MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE) ? modelConfigs.get(
		    MLUtils.ENCOUNTER_DATE_REQUEST_VARIABLE).asText() : "";
		
		Iterator<Map.Entry<String, JsonNode>> it = variableValues.getFields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> field = it.next();
			String keyId = field.getKey();
			int keyValue = field.getValue().getIntValue();
			modelParams.put(keyId, keyValue);
		}
		
		prepareEncounterModelParams(encounterDateString, modelParams);
		// add facility cut off
		
		JSONObject profile = getHTSFacilityProfile("Facility.Name", facilityName, getFacilityCutOffs());
		String[] otherVars = { "PC1", "PC2", "PC3", "PC4", "PC5", "PC6", "PC7", "PC8", "PC9", "PC10" };
		for (int i = 0; i < otherVars.length; i++) {
			modelParams.put(otherVars[i], profile.get(otherVars[i]));
		}
		
		ModelInputFields inputFields = new ModelInputFields();
		inputFields.setFields(modelParams);
		return inputFields;
	}
	
	private static Map<String, Object> prepareEncounterModelParams(String encounterDateString,
	        Map<String, Object> modelParams) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
	 * @param dayOfWeek
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
		InputStream stream = MLUtils.class.getClassLoader().getResourceAsStream("hts_ml_facility_cut_off.json");
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
	public static JSONArray getFacilityCutOffs() {
		
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
	
}
