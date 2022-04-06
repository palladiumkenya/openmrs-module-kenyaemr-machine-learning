package org.openmrs.module.kenyaemrml.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.util.PrivilegeConstants;

public class MLDataExchange {
	
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
	
	private final long recordsPerPull = 50; // Total number of records per request
	
	/**
	 * Initialize the OAuth variables
	 * 
	 * @return true on success or false on failure
	 */
	public boolean initAuthVars() {
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
		
		String facilityCode = "facility.mflcode";
		GlobalProperty globalFacilityCode = Context.getAdministrationService().getGlobalPropertyObject(facilityCode);
		strFacilityCode = globalFacilityCode.getPropertyValue();
		
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
	 * @return total number of records
	 */
	private long getTotalRecordsOnRemote(String bearerToken) {
		BufferedReader reader = null;
		HttpsURLConnection connection = null;
		try {
			URL url = new URL(strDWHbackEndURL + "?code=FND&name=predictions&pageNumber=1&pageSize=1&siteCode="
			        + strFacilityCode);
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
				
				return (pageCount);
			} else {
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
	 * Get the total records on local side
	 * 
	 * @return total number of records
	 */
	private long getTotalRecordsOnLocal() {
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
		
		String strTotalCount = "select count(*) from kenyaemr_ml_patient_risk_score;";
		
		Long totalCount = (Long) Context.getAdministrationService().executeSQL(strTotalCount, true).get(0).get(0);
		
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		return (totalCount);
	}
	
	/**
	 * Pulls records and saves locally
	 * 
	 * @param bearerToken the OAuth2 token
	 * @param totalRemote the total number of records in DWH
	 * @return true when successfull and false on failure
	 */
	private boolean pullAndSave(String bearerToken, long totalRemote) {
		
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
				        + recordsPerPull + "&siteCode=" + strFacilityCode;
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
						for (JsonNode person : extract) {
							if (!getContinuePullingData()) {
								return (false);
							}
							try {
								String riskScore = person.get("risk_score").asText();
								String uuid = person.get("id").asText();
								String patientId = person.get("PatientPID").asText();
								
								Patient patient = patientService.getPatient(Integer.valueOf(patientId));
								PatientRiskScore patientRiskScore = new PatientRiskScore();
								
								patientRiskScore.setRiskScore(Double.valueOf(riskScore));
								patientRiskScore.setSourceSystemUuid(uuid);
								patientRiskScore.setPatient(patient);
								
								mLinKenyaEMRService.saveOrUpdateRiskScore(patientRiskScore);
							}
							catch (Exception ex) {
								//Failed to save record
								System.err.println("ITT ML - Error getting and saving remote records: " + ex.getMessage());
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
				System.err.println("ITT ML - Error getting and saving remote records: " + e.getMessage());
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
			try {
				Thread.sleep(1000);
			}
			catch (Exception ie) {
				Thread.currentThread().interrupt();
			}
			currentPage++;
		}
		return (true);
	}
	
	/**
	 * Fetches the data from data warehouse and saves it locally
	 * 
	 * @return true when successfull and false on failure
	 */
	public boolean fetchDataFromDWH() {
		// Init the auth vars
		boolean varsOk = initAuthVars();
		if (varsOk) {
			//Get the OAuth Token
			String credentials = getClientCredentials();
			//Get the data
			if (credentials != null) {
				//get total count by fetching only one record from remote
				long totalRemote = getTotalRecordsOnRemote(credentials);
				System.out.println("ITT ML - Total Remote Records: " + totalRemote);
				if (totalRemote > 0) {
					//We have remote records - get local total records
					long totalLocal = getTotalRecordsOnLocal();
					System.out.println("ITT ML - Total Local Records: " + totalLocal);
					//if remote records are greater than local, we pull
					if (totalRemote > totalLocal) {
						//We now pull and save
						pullAndSave(credentials, totalRemote);
					} else {
						System.err.println("ITT ML - Records are already in sync");
						return (false);
					}
				} else {
					System.err.println("ITT ML - No records on remote side");
					return (false);
				}
			} else {
				System.err.println("ITT ML - Failed to get the OAuth token");
				return (false);
			}
		} else {
			System.err.println("ITT ML - Failed to get the OAuth Vars");
			return (false);
		}
		return (true);
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
	
}
