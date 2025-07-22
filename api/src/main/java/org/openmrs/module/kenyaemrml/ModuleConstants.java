package org.openmrs.module.kenyaemrml;

public class ModuleConstants {
	
	/**
	 * Module ID
	 */
	public static final String MODULE_ID = "kenyaemrml";
	
	/**
	 * App IDs
	 */
	public static final String APP_ML_PREDICTIONS = MODULE_ID + ".predictions";
	
	/**
	 * IIT Score Encounter Type
	 */
	public static final String IIT_SCORE_ENCOUNTER_TYPE = "1dab4593-b09d-4c5b-83fe-f041092145d3";
	
	/**
	 * IIT Score Value Concept (numeric)
	 */
	public static final String IIT_SCORE_RESULT_CONCEPT = "167162AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * IIT Score Description Question Concept (qualitative question? - VERY HIGH, HIGH, MEDIUM, LOW)
	 */
	public static final String IIT_SCORE_DESCRIPTION_QUESTION_CONCEPT = "167163AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * IIT Score Description Answer Concept (qualitative answer - LOW)
	 */
	public static final String IIT_SCORE_DESCRIPTION_LOW_ANSWER_CONCEPT = "1407AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * IIT Score Description Answer Concept (qualitative answer - MEDIUM)
	 */
	public static final String IIT_SCORE_DESCRIPTION_MEDIUM_ANSWER_CONCEPT = "1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * IIT Score Description Answer Concept (qualitative answer - HIGH)
	 */
	public static final String IIT_SCORE_DESCRIPTION_HIGH_ANSWER_CONCEPT = "1408AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	/**
	 * IIT Score Description Answer Concept (qualitative answer - VERY HIGH)
	 */
	public static final String IIT_SCORE_DESCRIPTION_VERYHIGH_ANSWER_CONCEPT = "167164AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

	/**
	 * Global parameter to check whether to use API for iit prediction
	 */
	public static final String GP_IIT_USE_API = "kenyaemr.iit.machine.learning.useAPI";

	/**
	 * Global parameter to get the IIT API URL
	 */
	public static final String GP_IIT_API_URL = "kenyaemr.iit.machine.learning.APIURL";

	/**
	 * Global parameter to get the IIT API Username
	 */
	public static final String GP_IIT_API_USERNAME = "kenyaemr.iit.machine.learning.APIusername";

	/**
	 * Global parameter to get the IIT API Password
	 */
	public static final String GP_IIT_API_PASSWORD = "kenyaemr.iit.machine.learning.APIpassword";
}
