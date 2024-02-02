package org.openmrs.module.kenyaemrml.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.OnArtCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.ModelService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;


public class MLDataExchange {

	// Enable/Disable debug mode
	private final boolean debugMode = false;
	
	public static final String DESCRIPTION = "Description";
	
	public static final String RISK_FACTORS = "RiskFactors";
	
	public static final String EVALUATION_DATE = "EvaluationDate";
	
	public static final String PATIENT_PID = "PatientPID";
	
	public static final String RISK_SCORE = "risk_score";
	
	public static final String PATIENT_UUID = "id";
	
	public static final String KENYAEMR_IIT_MACHINE_LEARNING_LAST_EVALUATION_DATE = "kenyaemr.iit.machine.learning.lastEvaluationDate";
	
	PersonService personService = Context.getPersonService();
	
	PatientService patientService = Context.getPatientService();
	
	MLinKenyaEMRService mLinKenyaEMRService = Context.getService(MLinKenyaEMRService.class);
	
	//OAuth variables
	private static final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
	
	private String strClientId = ""; // clientId
	
	private String strClientSecret = ""; // client secret
	
	private String strScope = ""; // scope
	
	private String strTokenUrl = ""; // Token URL
	
	private String strDWHbackEndURL = ""; // DWH backend URL
	
	private String strAuthURL = ""; // DWH auth URL
	
	private String strFacilityCode = ""; // Facility Code
	
	private String strPagingThreshold = ""; // Paging of response (Number of items per page)
	
	private long recordsPerPull = 400; // Total number of records per request
	
	/**
	 * Initialize the OAuth variables
	 * 
	 * @return true on success or false on failure
	 */
	public boolean initGlobalVars() {
		String dWHbackEndURL = "kenyaemr.iit.machine.learning.backend.url";
		GlobalProperty globalDWHbackEndURL = Context.getAdministrationService().getGlobalPropertyObject(dWHbackEndURL);
		strDWHbackEndURL = globalDWHbackEndURL.getPropertyValue();
		
		String tokenUrl = "kenyaemr.iit.machine.learning.token.url";
		GlobalProperty globalTokenUrl = Context.getAdministrationService().getGlobalPropertyObject(tokenUrl);
		strTokenUrl = globalTokenUrl.getPropertyValue();
		
		String scope = "kenyaemr.iit.machine.learning.scope";
		GlobalProperty globalScope = Context.getAdministrationService().getGlobalPropertyObject(scope);
		strScope = globalScope.getPropertyValue();
		
		String clientSecret = "kenyaemr.iit.machine.learning.client.secret";
		GlobalProperty globalClientSecret = Context.getAdministrationService().getGlobalPropertyObject(clientSecret);
		strClientSecret = globalClientSecret.getPropertyValue();
		
		String clientId = "kenyaemr.iit.machine.learning.client.id";
		GlobalProperty globalClientId = Context.getAdministrationService().getGlobalPropertyObject(clientId);
		strClientId = globalClientId.getPropertyValue();
		
		String authURL = "kenyaemr.iit.machine.learning.authorization.url";
		GlobalProperty globalAuthURL = Context.getAdministrationService().getGlobalPropertyObject(authURL);
		strAuthURL = globalAuthURL.getPropertyValue();
		
		String gpResponsePaging = "kenyaemr.iit.machine.learning.paging";
		GlobalProperty responsePagingString = Context.getAdministrationService().getGlobalPropertyObject(gpResponsePaging);
		strPagingThreshold = responsePagingString.getPropertyValue();
		
		KenyaEmrService emrService = Context.getService(KenyaEmrService.class);
		emrService.getDefaultLocationMflCode();
		strFacilityCode = emrService.getDefaultLocationMflCode();
		
		if (strDWHbackEndURL == null || strTokenUrl == null || strScope == null || strClientSecret == null
		        || strClientId == null || strAuthURL == null) {
			System.err.println("ITT ML - get data: Please set DWH OAuth credentials");
			return (false);
		}
		return (true);
	}
	
	/**
	 * Get the Token
	 * 
	 * @return the token as a string and null on failure
	 */
	private String getClientCredentials() {
		
		String auth = strClientId + ":" + strClientSecret;
		String authentication = Base64.getEncoder().encodeToString(auth.getBytes());
		BufferedReader reader = null;
		HttpsURLConnection connection = null;
		String returnValue = "";
		try {
			StringBuilder parameters = new StringBuilder();
			parameters.append("grant_type=" + URLEncoder.encode("client_credentials", "UTF-8"));
			parameters.append("&");
			parameters.append("scope=" + URLEncoder.encode(strScope, "UTF-8"));
			URL url = new URL(strTokenUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic " + authentication);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/json");
			PrintStream os = new PrintStream(connection.getOutputStream());
			os.print(parameters);
			os.close();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			String response = out.toString();
			Matcher matcher = pat.matcher(response);
			if (matcher.matches() && matcher.groupCount() > 0) {
				returnValue = matcher.group(1);
			} else {
				System.err.println("IIT ML - Error : Token pattern mismatch");
			}
			
		}
		catch (Exception e) {
			System.err.println("IIT ML - Error : " + e.getMessage());
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {}
			}
			connection.disconnect();
		}
		return returnValue;
	}
	
	/**
	 * Get the total records on remote side
	 * 
	 * @param bearerToken the OAuth2 token
	 * @return long available number of records
	 */
	private long getAvailableRecordsOnRemoteSide(String bearerToken, String fromDate) {
		BufferedReader reader = null;
		HttpsURLConnection connection = null;
		try {
			URL url = new URL(strDWHbackEndURL + "?code=FND&name=predictions&pageNumber=1&pageSize=1&siteCode="
			        + strFacilityCode + "&fromDate=" + fromDate);
			System.out.println("Getting available data count using: " + url);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			String response = out.toString();
			
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode jsonNode = (ObjectNode) mapper.readTree(response);
			if (jsonNode != null) {
				long pageCount = jsonNode.get("pageCount").getLongValue();
				System.out.println("Got available data count as: " + pageCount);
				return (pageCount);
			} else {
				System.out.println("No available data");
				return (0);
			}
		}
		catch (Exception e) {
			System.err.println("ITT ML - Error getting total remote records: " + e.getMessage());
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {}
			}
			connection.disconnect();
		}
		return (0);
	}
	
	/**
	 * Pulls records and saves locally
	 * 
	 * @param bearerToken the OAuth2 token
	 * @param totalRemote the available number of records in DWH
	 * @param lastEvaluationDate the last evaluation date
	 * @return true when successfull and false on failure
	 */
	private boolean pullAndSave(String bearerToken, long totalRemote, String lastEvaluationDate) {

		if (StringUtils.isNotBlank(strPagingThreshold)) {
			long configuredValue = Long.valueOf(strPagingThreshold);
			if (configuredValue > 0) {
				recordsPerPull = configuredValue;
			}
		}
		long totalPages = (long) (Math.ceil((totalRemote * 1.0) / (recordsPerPull * 1.0)));
		
		long currentPage = 1;
		for (int i = 0; i < totalPages; i++) {
			if (!getContinuePullingData()) {
				return (false);
			}
			BufferedReader reader = null;
			HttpsURLConnection connection = null;
			try {

				String fullURL = strDWHbackEndURL + "?code=FND&name=predictions&pageNumber=" + currentPage + "&pageSize="
				        + recordsPerPull + "&siteCode=" + strFacilityCode + "&fromDate=" + lastEvaluationDate;
				System.out.println("Pulling data using: " + fullURL);
				URL url = new URL(fullURL);
				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
				connection.setDoOutput(true);
				connection.setRequestMethod("GET");
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = null;
				StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
				while ((line = reader.readLine()) != null) {
					out.append(line);
				}
				String response = out.toString();
				
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode jsonNode = (ObjectNode) mapper.readTree(response);
				if (jsonNode != null) {
					
					JsonNode extract = jsonNode.get("extract");
					if (extract.isArray() && extract.size() > 0) {
						for (JsonNode personObject : extract) {
							if (!getContinuePullingData()) {
								return (false);
							}

							try {
								String riskScore = personObject.get(RISK_SCORE).asText();
								String uuid = personObject.get(PATIENT_UUID).asText();
								String patientId = personObject.get(PATIENT_PID).asText();

								//Get the description and riskFactors from payload -- optional
								String description = "";
								String riskFactors = "";
								Date evaluationDate = new Date();

								try {
									description = personObject.get(DESCRIPTION).asText();
								} catch(Exception ex) {}
								try {
									riskFactors = personObject.get(RISK_FACTORS).asText();
								} catch(Exception ex) {}
								try {
									String evalDate = personObject.get(EVALUATION_DATE).asText();
									SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
									evaluationDate = formatter.parse(evalDate);
								} catch(Exception ex) {}

								Patient patient = patientService.getPatient(Integer.valueOf(patientId));
								PatientRiskScore patientRiskScore = new PatientRiskScore();
								
								patientRiskScore.setRiskScore(Double.valueOf(riskScore));
								patientRiskScore.setSourceSystemUuid(uuid);
								patientRiskScore.setPatient(patient);

								patientRiskScore.setDescription(description);
								patientRiskScore.setRiskFactors(riskFactors);
								patientRiskScore.setEvaluationDate(evaluationDate);
								
								mLinKenyaEMRService.saveOrUpdateRiskScore(patientRiskScore);
							}
							catch (Exception ex) {
								//Failed to save record
								System.err.println("IIT ML - Error getting and saving remote records: " + ex.getMessage());
							}
						}
					} else {
						System.err.println("ITT ML - JSON Data extraction problem. Exiting");
						if (reader != null) {
							try {
								reader.close();
							}
							catch (Exception ex) {}
						}
						if (reader != null) {
							try {
								connection.disconnect();
							}
							catch (Exception er) {}
						}
						return(false);
					}
				}
			}
			catch (Exception e) {
				System.err.println("IIT ML - Error getting and saving remote records: " + e.getMessage());
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException e) {}
				}
				if (reader != null) {
					try {
						connection.disconnect();
					}
					catch (Exception er) {}
				}
			}
			
			setDataPullStatus((long)Math.floor(((currentPage * 1.00 / totalPages * 1.00) * totalRemote)), totalRemote);
			currentPage++;

			try {
				//Delay for 5 seconds
				Thread.sleep(5000);
			}
			catch (Exception ie) {
				Thread.currentThread().interrupt();
			}
		}

		// update the last evaluation date

		Date latestRiskEvaluationDate = mLinKenyaEMRService.getLatestRiskEvaluationDate();
		if (latestRiskEvaluationDate != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMMdd");
			GlobalProperty globalLastEvaluationDate = Context.getAdministrationService().getGlobalPropertyObject(KENYAEMR_IIT_MACHINE_LEARNING_LAST_EVALUATION_DATE);
			globalLastEvaluationDate.setPropertyValue(format.format(latestRiskEvaluationDate));
			Context.getAdministrationService().saveGlobalProperty(globalLastEvaluationDate);
		}

		return (true);
	}
	
	/**
	 * Fetches the data from data warehouse and saves it locally
	 * 
	 * @return true when successfull and false on failure
	 */
	public boolean fetchDataFromDWH() {
		// We have finished the data pull task. We now set the flag.
		setStatusOfPullDataTask(true);

		// Init the auth vars
		boolean varsOk = initGlobalVars();
		if (varsOk) {
			//Get the OAuth Token
			String credentials = getClientCredentials();
			//Get the data
			if (credentials != null) {
				// Get the last evaluation date
				GlobalProperty globalLastEvaluationDate = Context.getAdministrationService().getGlobalPropertyObject(
				    KENYAEMR_IIT_MACHINE_LEARNING_LAST_EVALUATION_DATE);
				String lastEvaluationDate = globalLastEvaluationDate.getPropertyValue();
				
				//get total count by fetching only one record from remote (start from last evaluation date)
				long totalRemote = getAvailableRecordsOnRemoteSide(credentials, lastEvaluationDate);
				System.out.println("ITT ML - Total Remote Records: " + totalRemote);
				
				if (totalRemote > 0) {
					//We now pull and save
					pullAndSave(credentials, totalRemote, lastEvaluationDate);
				} else {
					System.err.println("ITT ML - No records on remote side");
					setStatusOfPullDataTask(false);
					return (false);
				}
			} else {
				System.err.println("ITT ML - Failed to get the OAuth token");
				setStatusOfPullDataTask(false);
				return (false);
			}
		} else {
			System.err.println("ITT ML - Failed to get the OAuth Vars");
			setStatusOfPullDataTask(false);
			return (false);
		}

		// We have finished the data pull task. We now set the flag.
		setStatusOfPullDataTask(false);

		return (true);
	}

	/**
	 * Sets the started/stopped status of the pull data task
	 * 
	 * @param stat true - running, false - stopped
	 */
	public void setStatusOfPullDataTask(Boolean stat) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			if(stat) { // running
				user.setUserProperty("stopIITMLPull", "0");
				user.setUserProperty("IITMLPullRunning", "1");
			} else { // stopped
				user.setUserProperty("stopIITMLPull", "1");
				user.setUserProperty("IITMLPullRunning", "0");
			}
		}
	}
	
	/**
	 * Enables or disables the pull data thread
	 * 
	 * @return false if pulling should be stopped and true if pull should continue
	 */
	public boolean getContinuePullingData() {
		User user = Context.getUserContext().getAuthenticatedUser();
		if (user != null) {
			String stopIITMLPull = user.getUserProperty("stopIITMLPull");
			if (stopIITMLPull != null) {
				stopIITMLPull = stopIITMLPull.trim();
				if (stopIITMLPull.equalsIgnoreCase("0")) {
					return (true);
				} else if (stopIITMLPull.equalsIgnoreCase("1")) {
					return (false);
				}
			} else {
				System.err.println("ITT ML - Failed to get the stop pull var");
			}
		} else {
			//User has logged out, stop pulling
			System.err.println("ITT ML - User has logged out, stop the pull");
			return (false);
		}
		return (true);
	}

	/**
	 * Enables or disables the generate IIT scores thread
	 * 
	 * @return false if generating should be stopped and true if generating should continue
	 */
	public boolean getContinueGeneratingIITScores() {
		if(debugMode) System.err.println("IIT ML: Checking if score generation is stopped");
		User user = Context.getUserContext().getAuthenticatedUser();
		if (user != null) {
			String stopIITMLGen = user.getUserProperty("stopIITMLGen");
			if (stopIITMLGen != null) {
				stopIITMLGen = stopIITMLGen.trim();
				if(debugMode) System.err.println("IIT ML: var is: " + stopIITMLGen);
				if (stopIITMLGen.equalsIgnoreCase("0")) {
					if(debugMode) System.err.println("IIT ML: Checking if score generation is stopped: Found var 0. Continue");
					return (true);
				} else if (stopIITMLGen.equalsIgnoreCase("1")) {
					if(debugMode) System.err.println("IIT ML: Checking if score generation is stopped: Found var 1. Stop");
					return (false);
				}
			} else {
				if(debugMode) System.err.println("ITT ML - Failed to get the IIT stop gen var");
			}
		} else {
			//User has logged out, stop generating
			if(debugMode) System.err.println("ITT ML - User has logged out, stop the generation of scores");
			return (false);
		}
		return (true);
	}
	
	/**
	 * sets the status of data pull so that it is accessible to the user
	 * 
	 * @param done number of records done
	 * @param total the total number of records
	 */
	public void setDataPullStatus(long done, long total) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if (user != null) {
			user.setUserProperty("IITMLPullDone", String.valueOf(done));
			user.setUserProperty("IITMLPullTotal", String.valueOf(total));
		} else {
			//User has logged out
			System.err.println("ITT ML - User has logged out, unable to set pull status");
		}
	}

	/**
	 * sets the status of score generation so that it is accessible to the user
	 * 
	 * @param done number of records done
	 * @param total the total number of records
	 */
	public void setScoreGenerationStatus(long done, long total) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if (user != null) {
			user.setUserProperty("IITMLGenDone", String.valueOf(done));
			user.setUserProperty("IITMLGenTotal", String.valueOf(total));
		} else {
			//User has logged out
			System.err.println("ITT ML - User has logged out, unable to set score generation status");
		}
	}

	/**
	 * Fetches latest IIT scores
	 * 
	 * @return true when successfull and false on failure
	 */
	public boolean generateIITScores() {
		Boolean ret = false;

		PatientService patientService = Context.getPatientService();
		ModelService modelService = Context.getService(ModelService.class);

		// Check if IIT is enabled
        String iitFeatureEnabled = "kenyaemrml.iitml.feature.enabled";
        GlobalProperty gpIITFeatureEnabled = Context.getAdministrationService().getGlobalPropertyObject(iitFeatureEnabled);

        if(gpIITFeatureEnabled != null && gpIITFeatureEnabled.getPropertyValue().trim().equalsIgnoreCase("true")) {

			// We have started the IIT score generation task. We now set the flag.
			setStatusOfIITGenScoresTask(true);

			// Get all patients
			List<Patient> allPatients = patientService.getAllPatients();
			if(debugMode) System.err.println("IIT ML Gen For All Task: Got all patients: " + allPatients.size());

			// Get patients on HIV program
			Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);

			// loop checking for patients without current IIT scores
			long totalRemote = allPatients.size();
			long totalPages = allPatients.size();
			long currentPage = 1;
			for (Patient patient : allPatients) {
				if (!getContinueGeneratingIITScores()) {
					setStatusOfIITGenScoresTask(false);
					if(debugMode) System.err.println("IIT ML: Gen was manualy stopped");
					return (false);
				}
				try {
					if (patient != null) {
						// Check if patient is currently on ART
						if(currentInArt(patient)) {
							if(debugMode) System.err.println("IIT ML: Analyzing patient: " + patient.getId());
							ProgramWorkflowService pwfservice = Context.getProgramWorkflowService();
							List<PatientProgram> programs = pwfservice.getPatientPrograms(patient, hivProgram, null, null, null,null, false);
							if (programs.size() > 0 && isActiveOnProgram(programs)) {
								if(patient.getDead() == false) {
									Date lastScore = mLinKenyaEMRService.getPatientLatestRiskEvaluationDate(patient);
									// check if a greencard has been filled since the last score
									if((lastScore == null || greenCardFilledSinceLastScore(patient, lastScore))) {
										PatientRiskScore patientRiskScore = modelService.generatePatientRiskScore(patient);
										// Save/Update to DB (for reports)
										if(debugMode) System.err.println("IIT ML: Got risk score for patient: " + patient.getId());
										if(debugMode) System.err.println("IIT ML: Saving to DB: " + patientRiskScore);
										mLinKenyaEMRService.saveOrUpdateRiskScore(patientRiskScore);
									} else {
										if(debugMode) System.err.println("IIT ML: Patient not viable: " + patient.getId());
									}
								} else {
									if(debugMode) System.err.println("IIT ML: Patient not viable: " + patient.getId());
								}
							} else {
								if(debugMode) System.err.println("IIT ML: Patient not viable: " + patient.getId());
							}
						} else {
							if(debugMode) System.err.println("IIT ML: Patient not on ART: " + patient.getId());
						}
					}
				} catch(Exception e) {
					if(debugMode) System.err.println("IIT ML: Error getting score. Patient: " + patient.getId() + " Error: " + e.getMessage());
					e.printStackTrace();
				}

				setScoreGenerationStatus((long)Math.floor(((currentPage * 1.00 / totalPages * 1.00) * totalRemote)), totalRemote);
				currentPage++;
			}

			// We have finished the generation task. We now set the flag.
			setStatusOfIITGenScoresTask(false);
		}

		return(ret);
	}

	/**
	 * Checks if the patient is currently active on the given set of programs
	 * @return
	 */
	private boolean isActiveOnProgram(List<PatientProgram> programs) {
		boolean ret = false;
		if(programs != null) {
			for(PatientProgram program: programs) {
				if(program.getActive()) {
					return(true);
				}
			}
		}
		return(ret);
	}

	/**
	 * Checks whether the patient is current on ART
	 * @return true if patient is current on ART
	 *
	 * */

	public Boolean currentInArt(Patient patient) {
		CalculationResult patientCurrentInART = EmrCalculationUtils.evaluateForPatient(OnArtCalculation.class, null, patient);
		return (patientCurrentInART != null ? (Boolean) patientCurrentInART.getValue() : false);

	}

	/**
	 * Sets the started/stopped status of the generate IIT scores task
	 * 
	 * @param stat true - running, false - stopped
	 */
	public void setStatusOfIITGenScoresTask(Boolean stat) {
		User user = Context.getUserContext().getAuthenticatedUser();
		if(user != null) {
			if(stat) { // running
				user.setUserProperty("stopIITMLGen", "0");
				user.setUserProperty("IITMLGenRunning", "1");
			} else { // stopped
				user.setUserProperty("stopIITMLGen", "1");
				user.setUserProperty("IITMLGenRunning", "0");
			}
		}
	}


	/**
	 * Checks if a greencard has been filled since the last IIT ML score
	 * @param patient - the patient
	 * @param lastscore - the last score
	 * @return true if a greencard has been filled or false otherwise
	 */
	public Boolean greenCardFilledSinceLastScore(Patient patient, Date lastscore) {
		Boolean ret = false;
		//check if a green card has been filled since the last score
		Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
		List<Form> hivCareForms = Arrays.asList(hivGreenCardForm);
		Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
					.setIncludeVoided(false)
					.setFromDate(lastscore)
					// .setToDate(new Date())
					.setPatient(patient)
					.setEnteredViaForms(hivCareForms)
					.setLocation(defaultLocation)
					.createEncounterSearchCriteria();
		List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
		if(hivCareEncounters.size() > 0) {
			ret = true;
		} else {
			ret = false;
		}
		return(ret);
	}

	/**
	 * Generates and saves the IIT risk scores on demand (press of the generate button)
	 * @param patientsGroup
	 * @return true when successfull and false on failure
	 */
	public Boolean generateAndSave(HashSet<Patient> patientsGroup) {
		Boolean ret = false;

		// Check if IIT is enabled
        String iitFeatureEnabled = "kenyaemrml.iitml.feature.enabled";
        GlobalProperty gpIITFeatureEnabled = Context.getAdministrationService().getGlobalPropertyObject(iitFeatureEnabled);

        if(gpIITFeatureEnabled != null && gpIITFeatureEnabled.getPropertyValue().trim().equalsIgnoreCase("true")) {
			ModelService modelService = Context.getService(ModelService.class);
			long totalRemote = patientsGroup.size();
			long totalPages = patientsGroup.size();
			long currentPage = 1;
			for (Patient patient : patientsGroup) {
				if (!getContinueGeneratingIITScores()) {
					System.out.println("IIT ML: Gen was manualy stopped");
					return (false);
				}
				System.out.println("IIT ML Score: Generating a new risk score || and saving to DB");

				try {
					PatientRiskScore patientRiskScore = modelService.generatePatientRiskScore(patient);
					// Save/Update to DB (for reports)
					System.out.println("IIT ML: Got risk score for patient: " + patient.getId());
					System.out.println("IIT ML: Saving to DB: " + patientRiskScore);
					mLinKenyaEMRService.saveOrUpdateRiskScore(patientRiskScore);
				} catch(Exception ex) {
					System.err.println("IIT ML Score: ERROR: Failed to generate patient score: " + ex.getMessage());
					ex.printStackTrace();	
				}
				setScoreGenerationStatus((long)Math.floor(((currentPage * 1.00 / totalPages * 1.00) * totalRemote)), totalRemote);
				currentPage++;
			}
		} else {
			System.out.println("IIT ML: IIT feature has not been enabled");
		}

		return(ret);
	}

	/**
	 * Fetches IIT ML scores (Scheduled Task Based)
	 * 
	 * @return true when successfull and false on failure
	 */
	public void generateIITScoresTask() {

		PatientService patientService = Context.getPatientService();

		// Get all patients
		List<Patient> allPatients = patientService.getAllPatients();

		// Get patients on HIV program
		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);

		// loop checking for patients without current IIT scores
		HashSet<Patient> patientsGroup = new HashSet<Patient>();
		for (Patient patient : allPatients) {
			try {
				if (patient != null) {
					ProgramWorkflowService pwfservice = Context.getProgramWorkflowService();
					List<PatientProgram> programs = pwfservice.getPatientPrograms(patient, hivProgram, null, null, null,null, false);
					if (programs.size() > 0) {
						if(patient.getDead() == false) {
							Date lastScore = mLinKenyaEMRService.getPatientLatestRiskEvaluationDate(patient);
							// check if a greencard has been filled since the last score
							if((lastScore == null || greenCardFilledSinceLastScore(patient, lastScore))) {
								patientsGroup.add(patient);
							}
						}
					}
				}
			} catch(Exception ex) {}
		}
		System.out.println("IIT ML Task: Patients to be scored: " + patientsGroup.size());
		generateMLScoresFetch(patientsGroup);
	}

	/**
	 * Generates IIT ML risk scores as a scheduled task given the list of patients
	 * @param patientsGroup - the given list of patients
	 */
	public void generateMLScoresFetch(HashSet<Patient> patientsGroup) {
		ModelService modelService = Context.getService(ModelService.class);
		for (Patient patient : patientsGroup) {
			try {
				System.out.println("IIT ML Score: Scoring patient: " + patient.getId());
				PatientRiskScore patientRiskScore = modelService.generatePatientRiskScore(patient);
				// Save/Update to DB (for reports)
				mLinKenyaEMRService.saveOrUpdateRiskScore(patientRiskScore);
			} catch(Exception ex) {
				System.err.println("IIT ML Score: ERROR: Failed to generate patient score: " + ex.getMessage());
				ex.printStackTrace();	
			}
		}
	}
	
}
