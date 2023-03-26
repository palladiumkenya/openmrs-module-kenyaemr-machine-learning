package org.openmrs.module.kenyaemrml.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.kenyaemrml.exception.ScoringException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.db.hibernate.HibernateMLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.ui.framework.SimpleObject;
import java.text.SimpleDateFormat;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.service.ModelService;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.util.PrivilegeConstants;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.impl.BaseOpenmrsService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.openmrs.Patient;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.api.db.MLinKenyaEMRDao;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;

import org.openmrs.Program;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import java.util.Set;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.context.annotation.Bean;
import org.openmrs.api.context.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.wrapper.PatientWrapper;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.util.PrivilegeConstants;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openmrs.ui.framework.WebConstants;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.wrapper.PatientWrapper;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.CalculationUtils;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.InitialArtStartDateCalculation;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;

import org.openmrs.module.kenyaemr.util.EncounterBasedRegimenUtils;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.api.MLinKenyaEMRService;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.openmrs.Visit;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.reporting.indicator.HivCareVisitsIndicator;
import org.openmrs.module.kenyaemr.reporting.library.shared.common.CommonCohortLibrary;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.SimpleIndicatorResult;
import org.openmrs.module.reporting.indicator.evaluator.IndicatorEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.kenyaemrml.api.MLUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.wrapper.PatientWrapper;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.nio.charset.Charset;
// import javax.xml.bind.JAXBException;

/**
 * Service class used to prepare and score models
 */
public class ModelService extends BaseOpenmrsService {
	
	private Log log = LogFactory.getLog(this.getClass());

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
			String fullModelZipFileName = modelId.concat(".pmml.zip");
			fullModelZipFileName = "hts/" + fullModelZipFileName;
			InputStream stream = ModelService.class.getClassLoader().getResourceAsStream(fullModelZipFileName);
			BufferedInputStream bistream = new BufferedInputStream(stream);
			// Model name
			String fullModelFileName = modelId.concat(".pmml");

			System.out.println("model zip file: " + fullModelZipFileName);
			System.out.println("model xml text file: " + fullModelFileName);
			// Get ZipEntry
			// // ZipInputStream zis = new ZipInputStream(bistream, Charset.forName("UTF-8"));
			ZipInputStream zis = new ZipInputStream(bistream);
			ZipEntry ze = null;

			while ((ze = zis.getNextEntry()) != null) {
				if(ze.getName().trim().equalsIgnoreCase(fullModelFileName)) {
					// Building a model evaluator from a PMML file
					Evaluator evaluator = new LoadingModelEvaluatorBuilder().load(zis).build();
					evaluator.verify();
					ScoringResult scoringResult = new ScoringResult(score(evaluator, inputFields, debug));
					System.out.println("Received the scoring result");
					return scoringResult;
				}
			}
		}
		catch (Exception e) {
			log.error("Exception during preparation of input parameters or scoring of values for HTS model: " + e.getMessage());
			System.err.println("Exception during preparation of input parameters or scoring of values for HTS model: " + e.getMessage());
			e.printStackTrace();
			return(null);
		}

		// Upon Failure
		System.err.println("Exception during scoring of HTS model: unzip failed");
		return(null);
	}

	public ScoringResult iitscore(String modelId, String facilityName, String encounterDate, ModelInputFields inputFields, boolean debug) {
		
		try {
			//Model zip file
			String fullModelZipFileName = modelId.concat(".pmml.zip");
			fullModelZipFileName = "iit/" + fullModelZipFileName;
			InputStream stream = ModelService.class.getClassLoader().getResourceAsStream(fullModelZipFileName);
			BufferedInputStream bistream = new BufferedInputStream(stream);
			// Model name
			String fullModelFileName = modelId.concat(".pmml");

			System.out.println("model zip file: " + fullModelZipFileName);
			System.out.println("model xml text file: " + fullModelFileName);
			// Get ZipEntry
			// // ZipInputStream zis = new ZipInputStream(bistream, Charset.forName("UTF-8"));
			ZipInputStream zis = new ZipInputStream(bistream);
			ZipEntry ze = null;

            while ((ze = zis.getNextEntry()) != null) {
				if(ze.getName().trim().equalsIgnoreCase(fullModelFileName)) {
					// Building a model evaluator from a PMML file
					Evaluator evaluator = new LoadingModelEvaluatorBuilder().load(zis).build();
					evaluator.verify();
					ScoringResult scoringResult = new ScoringResult(score(evaluator, inputFields, debug));
					System.out.println("Received the scoring result");
					return scoringResult;
				}
			}
		}
		catch (Exception e) {
			log.error("Exception during preparation of input parameters or scoring of values for IIT model: " + e.getMessage());
			System.err.println("Exception during preparation of input parameters or scoring of values for IIT model: " + e.getMessage());
			e.printStackTrace();
			return(null);
		}

		// Upon Failure
		System.err.println("Exception during scoring of IIT model: unzip failed");
		return(null);
		
	}
	
	/**
	 * A method that scores a model
	 * 
	 * @param evaluator
	 * @param inputFields
	 * @return
	 */
	private Map<String, Object> score(Evaluator evaluator, ModelInputFields inputFields, boolean debug) {
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
			return result;
		}
	}
	
	/**
	 * Performs variable mapping
	 * 
	 * @param evaluator
	 * @param inputFields
	 * @return variable-value pair
	 */
	private Map<FieldName, FieldValue> prepareEvaluationArgs(Evaluator evaluator, ModelInputFields inputFields) {
		Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
		
		List<InputField> evaluatorFields = evaluator.getActiveFields();
		
		for (InputField evaluatorField : evaluatorFields) {
			FieldName evaluatorFieldName = evaluatorField.getName();
			String evaluatorFieldNameValue = evaluatorFieldName.getValue();
			
			Object inputValue = inputFields.getFields().get(evaluatorFieldNameValue);
			
			if (inputValue == null) {
				log.warn("Model value not found for the following field: " + evaluatorFieldNameValue);
			}
			
			arguments.put(evaluatorFieldName, evaluatorField.prepare(inputValue));
		}
		return arguments;
	}

	/**
	 * Get the latest OBS (Observation)
	 * @param patient
	 * @param conceptIdentifier
	 * @return
	 */
	public Obs getLatestObs(Patient patient, String conceptIdentifier) {
		Concept concept = Context.getConceptService().getConceptByUuid(conceptIdentifier);
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obs.size() > 0) {
			// these are in reverse chronological order
			return obs.get(0);
		}
		return null;
	}

	/**
	 * Get the total number of OBS (Observations)
	 * @param patient
	 * @param conceptIdentifier - The concept UUID
	 * @return
	 */
	public Integer getNumberOfObs(Patient patient, String conceptIdentifier) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(conceptIdentifier);
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obs.size() > 0) {
			ret = obs.size();
		}
		return ret;
	}

	/**
	 * Get total high viral load observations
	 * @param patient
	 * @return Integer - The count
	 */
	public Integer getHighViralLoadCount(Patient patient) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(Dictionary.HIV_VIRAL_LOAD);
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList.size() > 0) {
			for (Obs cur : obsList) {
				Double vl = cur.getValueNumeric();
				if(vl >= 1000.00) {
					ret++;
				}
			}
		}
		return ret;
	}

	/**
	 * Get all viral load observations in the last 3 yrs
	 * @param patient
	 * @return Integer - The count
	 */
	public Integer getAllViralLoadCountLastThreeYears(Patient patient) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(Dictionary.HIV_VIRAL_LOAD);
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList.size() > 0) {
			for (Obs cur : obsList) {
				Date obsDate = cur.getObsDatetime();
				Date currentDate = new Date();
				Age age = new Age(obsDate, currentDate);
				long diff = age.getFullYears();
				if(diff <= 3) {
					ret++;
				}
			}
		}
		return ret;
	}

	/**
	 * Get high viral load observations in the last 3 yrs
	 * @param patient
	 * @return Integer - The count
	 */
	public Integer getHighViralLoadCountLastThreeYears(Patient patient) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(Dictionary.HIV_VIRAL_LOAD);
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList.size() > 0) {
			for (Obs cur : obsList) {
				Double vl = cur.getValueNumeric();
				if(vl >= 1000.00) {
					Date obsDate = cur.getObsDatetime();
					Date currentDate = new Date();
					Age age = new Age(obsDate, currentDate);
					long diff = age.getFullYears();
					if(diff <= 3) {
						ret++;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Get total low viral load observations
	 * @param patient
	 * @return Integer - The count
	 */
	public Integer getLowViralLoadCount(Patient patient) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(Dictionary.HIV_VIRAL_LOAD);
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList.size() > 0) {
			for (Obs cur : obsList) {
				Double vl = cur.getValueNumeric();
				if(vl >= 200.00 && vl < 1000.00) {
					ret++;
				}
			}
		}
		return ret;
	}

	/**
	 * Get total suppressed viral load observations
	 * @param patient
	 * @return Integer - The count
	 */
	public Integer getSuppressedViralLoadCount(Patient patient) {
		Integer ret = 0;
		Concept concept = Context.getConceptService().getConceptByUuid(Dictionary.HIV_VIRAL_LOAD);
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList.size() > 0) {
			for (Obs cur : obsList) {
				Double vl = cur.getValueNumeric();
				if(vl < 200.00) {
					ret++;
				}
			}
		}
		return ret;
	}

	/**
	 * Get Total Appointments
	 */
	public Integer getTotalAppointments(Patient patient) {
		Integer ret = 0;
		PatientWrapper patientWrapper = new PatientWrapper(patient);
		
		Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
		List<Encounter> hivClinicalEncounters = patientWrapper.allEncounters(hivGreenCardForm);
		
		// get hiv greencard list of observations
		if (hivClinicalEncounters != null) {
			ret = hivClinicalEncounters.size();
		}
		return(ret);
	}

	/**
	 * Get Missed Appointments
	 */
	public SimpleObject getMissedAppointments(Patient patient) {
		SimpleObject ret = new SimpleObject();
		Integer total = 0;
		Boolean lastfive = false;
		PatientWrapper patientWrapper = new PatientWrapper(patient);
		
		Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
		List<Encounter> hivClinicalEncounters = patientWrapper.allEncounters(hivGreenCardForm);
		Collections.reverse(hivClinicalEncounters); // Sort Descending
		
		// get hiv greencard list of observations
		if (hivClinicalEncounters != null) {
			for (int i = 0; i < hivClinicalEncounters.size(); i++) {
				Encounter enc = hivClinicalEncounters.get(i);
				SimpleObject encDetails = getEncDetails(enc.getObs(), enc, hivClinicalEncounters);
				Boolean appointmentHonoured = (Boolean) encDetails.get("honoured");
				if(appointmentHonoured == false) {
					if(i < 5) {
						lastfive = true;
					}
					total++;
				}
			}
		}
		ret.put("total", total);
		ret.put("lastfive", lastfive);
		return(ret);
	}

	/**
	 * Get Honoured Appointments
	 */
	public Integer getHonouredAppointments(Patient patient) {
		Integer ret = 0;
		PatientWrapper patientWrapper = new PatientWrapper(patient);
		
		Form hivGreenCardForm = MetadataUtils.existing(Form.class, HivMetadata._Form.HIV_GREEN_CARD);
		List<Encounter> hivClinicalEncounters = patientWrapper.allEncounters(hivGreenCardForm);
		Collections.reverse(hivClinicalEncounters); // Sort Descending
		
		// get hiv greencard list of observations
		if (hivClinicalEncounters != null) {
			for (int i = 0; i < hivClinicalEncounters.size(); i++) {
				Encounter enc = hivClinicalEncounters.get(i);
				SimpleObject encDetails = getEncDetails(enc.getObs(), enc, hivClinicalEncounters);
				Boolean appointmentHonoured = (Boolean) encDetails.get("honoured");
				if(appointmentHonoured == true) {
					ret++;
				}
			}
		}
		return(ret);
	}

	/**
	 * Extract TCA information from encounters and order them based on Date
	 * 
	 * @param
	 * @return
	 */
	SimpleObject getEncDetails(Set<Obs> obsList, Encounter e, List<Encounter> allClinicalEncounters) {
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
		Integer tcaDateConcept = 5096;
		String tcaDateString = null;
		Date tcaDate = null;
		int appointmentDuration = 0;
		Boolean appointmentHonoured = false;
		for (Obs obs : obsList) {
			
			if (obs.getConcept().getConceptId().equals(tcaDateConcept)) {
				tcaDate = obs.getValueDate();
				tcaDateString = tcaDate != null ? DATE_FORMAT.format(tcaDate) : "";
				appointmentDuration = Days.daysBetween(new LocalDate(e.getEncounterDatetime()), new LocalDate(tcaDate))
				        .getDays();
				if (hasVisitOnDate(tcaDate, e.getPatient(), allClinicalEncounters)) {
					appointmentHonoured = true;
				}
			}
		}
		return SimpleObject.create("encDate", DATE_FORMAT.format(e.getEncounterDatetime()), "tcaDate",
		    tcaDateString != null ? tcaDateString : "", "encounter", Arrays.asList(e), "form", e.getForm(), "patientId", e
		            .getPatient().getPatientId(), "appointmentPeriod", appointmentDuration, "honoured", appointmentHonoured);
	}

	/**
	 * Was there a visit on the appointment day?
	 */
	
	private boolean hasVisitOnDate(Date appointmentDate, Patient patient, List<Encounter> allEncounters) {
		boolean hasVisitOnDate = false;
		for (Encounter e : allEncounters) {
			int sameDay = new LocalDate(e.getEncounterDatetime()).compareTo(new LocalDate(appointmentDate));
			
			if (sameDay == 0) {
				hasVisitOnDate = true;
				break;
			}
		}
		return hasVisitOnDate;
	}

	/**
	 * Determines whether the given encounter was part of a scheduled visit
	 * @param encounter the encounter
	 * @return true if was part of scheduled visit
	 */
	private boolean wasScheduledVisit(Encounter encounter) {
		// Firstly look for a scheduled visit obs which has value = true
		Concept scheduledVisit = Dictionary.getConcept(Dictionary.SCHEDULED_VISIT);
		for (Obs obs : encounter.getAllObs()) {
			if (obs.getConcept().equals(scheduledVisit) && obs.getValueAsBoolean()) {
				return true;
			}
		}

		Date visitDate = (encounter.getVisit() != null) ? encounter.getVisit().getStartDatetime() : encounter.getEncounterDatetime();
		Concept returnVisitDate = Dictionary.getConcept(Dictionary.RETURN_VISIT_DATE);
		List<Obs> returnVisitObss = Context.getObsService().getObservationsByPersonAndConcept(encounter.getPatient(), returnVisitDate);

		for (Obs returnVisitObs : returnVisitObss) {
			if (returnVisitObs != null && returnVisitObs.getValueDate() != null && DateUtils.isSameDay(returnVisitObs.getValueDate(), visitDate)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get total unscheduled visits since 2019
	 */
	public Integer getTotalUnscheduledVisits(Patient patient) {
		Integer ret = 0;
		try {
			List<Form> hivCareForms = Arrays.asList(
				MetadataUtils.existing(Form.class, HivMetadata._Form.CLINICAL_ENCOUNTER_HIV_ADDENDUM),
				MetadataUtils.existing(Form.class, HivMetadata._Form.MOH_257_VISIT_SUMMARY)
			);
			// List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(null, defaultLocation, fromDate, toDate, hivCareForms, null, null, null, null, false);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			Date firstDate = sdf.parse("01/01/2019");
			Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();
			//String prefix = Context.getService(KenyaEmrService.class).getDefaultLocationMflCode();
			EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
						.setIncludeVoided(false)
						.setFromDate(firstDate)
						.setToDate(new Date())
						.setPatient(patient)
						.setEnteredViaForms(hivCareForms)
						.setLocation(defaultLocation)
						.createEncounterSearchCriteria();
			List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
			for (Encounter enc : hivCareEncounters) {
				if (!wasScheduledVisit(enc)) {
					ret++;
				}
			}
		} catch(Exception ex) {
			System.err.println("Error: could not get the total unscheduled visits: " + ex.getMessage());
			ex.printStackTrace();
		}
		return(ret);
	}
	

	/**
	 * Gets the latest patient IIT score
	 */
	public PatientRiskScore getLatestPatientRiskScoreByPatient(Patient patient) {
		PatientRiskScore patientRiskScore = new PatientRiskScore();
		SimpleObject modelConfigs = new SimpleObject();
		SimpleObject patientPredictionVariables = new SimpleObject();
		SimpleObject mlScoringRequestPayload = new SimpleObject();
		
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
		modelConfigs.put("modelId", "XGB_IIT_02232023");
		String today = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        modelConfigs.put("encounterDate", today);
        modelConfigs.put("facilityId", "");
        modelConfigs.put("debug", "true");
		// Prediction Variables
		Integer Age = patient.getAge();
		patientPredictionVariables.put("Age", Age);
		// Start Facility Profile Matrix
		patientPredictionVariables.put("births", 0);
		patientPredictionVariables.put("pregnancies", 0);
		patientPredictionVariables.put("literacy", 0);
		patientPredictionVariables.put("poverty", 0);
		patientPredictionVariables.put("anc", 0);
		patientPredictionVariables.put("pnc", 0);
		patientPredictionVariables.put("sba", 0);
		patientPredictionVariables.put("hiv_prev", 0);
		patientPredictionVariables.put("hiv_count", 0);
		patientPredictionVariables.put("condom", 0);
		patientPredictionVariables.put("intercourse", 0);
		patientPredictionVariables.put("in_union", 0);
		patientPredictionVariables.put("circumcision", 0);
		patientPredictionVariables.put("partner_away", 0);
		patientPredictionVariables.put("partner_men", 0);
		patientPredictionVariables.put("partner_women", 0);
		patientPredictionVariables.put("sti", 0);
		patientPredictionVariables.put("fb", 0);
		// End Facility Profile Matrix

		Integer n_appts = getTotalAppointments(patient);
		patientPredictionVariables.put("n_appts", n_appts);
		
		// Source missed appointments
		patientPredictionVariables.put("missed1", 0);
		patientPredictionVariables.put("missed5", 0);
		patientPredictionVariables.put("missed30", 0);
		patientPredictionVariables.put("missed1_last5", 0);
		patientPredictionVariables.put("missed5_last5", 0);
		patientPredictionVariables.put("missed30_last5", 0);

		SimpleObject soTotalMissedAppointments = getMissedAppointments(patient);
		Integer totalMissedAppointments = (Integer) soTotalMissedAppointments.get("total");
		if(totalMissedAppointments > 0) {
			patientPredictionVariables.put("missed1", totalMissedAppointments);
		} else if(totalMissedAppointments > 5) {
			patientPredictionVariables.put("missed5", totalMissedAppointments);
		} else if(totalMissedAppointments > 30) {
			patientPredictionVariables.put("missed30", totalMissedAppointments);
		}
		Boolean lastfive = (Boolean) soTotalMissedAppointments.get("lastfive");
		if(totalMissedAppointments > 0 && lastfive == true) {
			patientPredictionVariables.put("missed1_last5", totalMissedAppointments);
		} else if(totalMissedAppointments > 5 && lastfive == true) {
			patientPredictionVariables.put("missed5_last5", totalMissedAppointments);
		} else if(totalMissedAppointments > 30 && lastfive == true) {
			patientPredictionVariables.put("missed30_last5", totalMissedAppointments);
		}

		// Source total regimens for patient
		patientPredictionVariables.put("num_hiv_regimens", 0);

		List<SimpleObject> arvHistory = EncounterBasedRegimenUtils.getRegimenHistoryFromObservations(patient, "ARV");
		Integer numberOfRegimens = arvHistory.size();
		patientPredictionVariables.put("num_hiv_regimens", numberOfRegimens);

		// Source visits (last five)
		patientPredictionVariables.put("n_visits_lastfive", 0);

		List<Visit> allVisits = Context.getVisitService().getVisitsByPatient(patient);
		Integer numOfVisits = allVisits.size();
		if(numOfVisits >= 5) {
			patientPredictionVariables.put("n_visits_lastfive", 5);
		} else {
			patientPredictionVariables.put("n_visits_lastfive", numOfVisits);
		}

		// Source unscheduled visits (last five)
		patientPredictionVariables.put("n_unscheduled_lastfive", 0);

		Integer unscheduledVisits = getTotalUnscheduledVisits(patient);
		if(unscheduledVisits >= 5) {
			patientPredictionVariables.put("n_unscheduled_lastfive", 5);
		} else {
			patientPredictionVariables.put("n_unscheduled_lastfive", unscheduledVisits);
		}

		// Source BMI
		Obs obsWeight = getLatestObs(patient, Dictionary.WEIGHT_KG);
		Obs obsHeight = getLatestObs(patient, Dictionary.HEIGHT_CM);
		Double conWeight = 0.00;
		Double conHeight = 0.00;
		if (obsWeight != null && obsHeight != null) {
			conWeight = obsWeight.getValueNumeric();
			conHeight = obsHeight.getValueNumeric();
			Double bmi = conWeight / ((conHeight/100.00) * (conHeight/100.00));
			patientPredictionVariables.put("BMI", bmi);
		} else {
			patientPredictionVariables.put("BMI", 0);
		}		
		
		patientPredictionVariables.put("changeInBMI", 0);

		//Source Weight
		Obs obsPatientWeight = getLatestObs(patient, Dictionary.WEIGHT_KG);
		Double conPatientWeight = 0.00;
		if (obsPatientWeight != null) {
			conPatientWeight = obsPatientWeight.getValueNumeric();
			patientPredictionVariables.put("Weight", conPatientWeight);
		} else {
			patientPredictionVariables.put("Weight", 0);
		}

		patientPredictionVariables.put("changeInWeight", 0);

		patientPredictionVariables.put("num_adherence_ART", 0);
		patientPredictionVariables.put("num_adherence_CTX", 0);

		patientPredictionVariables.put("num_poor_ART", 0);
		patientPredictionVariables.put("num_poor_CTX", 0);

		patientPredictionVariables.put("num_fair_ART", 0);
		patientPredictionVariables.put("num_fair_CTX", 0);

		// Source ALL VL TESTS
		patientPredictionVariables.put("n_tests_all", 0);

		Integer totalTests = getNumberOfObs(patient, Dictionary.HIV_VIRAL_LOAD);
		patientPredictionVariables.put("n_tests_all", totalTests);

		// Source High VL
		patientPredictionVariables.put("n_hvl_all", 0);

		Integer highVLTotal = getHighViralLoadCount(patient);
		patientPredictionVariables.put("n_hvl_all", highVLTotal);

		// Source ALL VL TESTS (Last 3 yrs)
		patientPredictionVariables.put("n_tests_threeyears", 0);

		Integer n_tests_threeyears = getAllViralLoadCountLastThreeYears(patient);
		patientPredictionVariables.put("n_tests_threeyears", n_tests_threeyears);

		// Source HIGH VL TESTS (Last 3 yrs)
		patientPredictionVariables.put("n_hvl_threeyears", 0);

		Integer n_hvl_threeyears = getHighViralLoadCountLastThreeYears(patient);
		patientPredictionVariables.put("n_hvl_threeyears", n_hvl_threeyears);

		// Source ART data
		Obs obsARTStartDate = getLatestObs(patient, Dictionary.ANTIRETROVIRAL_TREATMENT_START_DATE);
		Date dtARTStartDate = null;
		if (obsARTStartDate != null) {
			dtARTStartDate = obsARTStartDate.getValueDatetime();
		}

		patientPredictionVariables.put("timeOnArt", 0);

		if(dtARTStartDate != null) {
			Date currentDate = new Date();
			long diffInMillies = Math.abs(currentDate.getTime() - dtARTStartDate.getTime());
    		long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS); // Time in days
			patientPredictionVariables.put("timeOnArt", diff);
		}

		patientPredictionVariables.put("AgeARTStart", 0);

		if(dtARTStartDate != null) {
			Date birthDate = patient.getBirthdate();
			Age age = new Age(birthDate, dtARTStartDate);
			long diff = age.getFullYears();
			patientPredictionVariables.put("AgeARTStart", diff);
		}

		patientPredictionVariables.put("recent_hvl_rate", 0);
		patientPredictionVariables.put("total_hvl_rate", 0);

		patientPredictionVariables.put("art_poor_adherence_rate", 0);
		patientPredictionVariables.put("art_fair_adherence_rate", 0);

		patientPredictionVariables.put("ctx_poor_adherence_rate", 0);
		patientPredictionVariables.put("ctx_fair_adherence_rate", 0);

		// Source the unscheduled rate
		patientPredictionVariables.put("unscheduled_rate", 0);

		if(numOfVisits > 0 && unscheduledVisits > 0) {
			if(numOfVisits > 0) {
				Integer calcNumOfVisits = (numOfVisits >= 5) ? 5 : numOfVisits;
				Integer calcUnscheduledVisits = (unscheduledVisits >= 5) ? 5 : unscheduledVisits;
				Double unscheduled_rate = ((calcUnscheduledVisits * 1.00) / (calcNumOfVisits * 1.00));
				patientPredictionVariables.put("unscheduled_rate", unscheduled_rate);
			}
		}

		patientPredictionVariables.put("all_late30_rate", 0.00);
		patientPredictionVariables.put("all_late5_rate", 0.00);
		patientPredictionVariables.put("all_late1_rate", 0.00);

		Integer late30s = (Integer) patientPredictionVariables.get("missed30");
		Double all_late30_rate = 0.00;
		if(n_appts > 0 && late30s > 0) {
			all_late30_rate = ((late30s * 1.00) / (n_appts * 1.00));
		}
		patientPredictionVariables.put("all_late30_rate", all_late30_rate);

		Integer late5s = (Integer) patientPredictionVariables.get("missed5");
		Double all_late5_rate = 0.00;
		if(n_appts > 0 && late5s > 0) {
			all_late5_rate = ((late5s * 1.00) / (n_appts * 1.00));
		}
		patientPredictionVariables.put("all_late5_rate", all_late5_rate);

		Integer late1s = (Integer) patientPredictionVariables.get("missed1");
		Double all_late1_rate = 0.00;
		if(n_appts > 0 && late1s > 0) {
			all_late1_rate = ((late1s * 1.00) / (n_appts * 1.00));
		}
		patientPredictionVariables.put("all_late1_rate", all_late1_rate);

		patientPredictionVariables.put("recent_late30_rate", 0.00);
		patientPredictionVariables.put("recent_late5_rate", 0.00);
		patientPredictionVariables.put("recent_late1_rate", 0.00);

		Integer lastFiveVisits = (Integer) patientPredictionVariables.get("n_visits_lastfive");

		Integer missed30_last5 = (Integer) patientPredictionVariables.get("missed30_last5");
		Double recent_late30_rate = 0.00;
		if(lastFiveVisits > 0 && missed30_last5 > 0) {
			recent_late30_rate = ((missed30_last5 * 1.00) / (lastFiveVisits * 1.00));
		}
		patientPredictionVariables.put("recent_late30_rate", recent_late30_rate);

		Integer missed5_last5 = (Integer) patientPredictionVariables.get("missed5_last5");
		Double recent_late5_rate = 0.00;
		if(lastFiveVisits > 0 && missed5_last5 > 0) {
			recent_late5_rate = ((missed5_last5 * 1.00) / (lastFiveVisits * 1.00));
		}
		patientPredictionVariables.put("recent_late5_rate", recent_late5_rate);

		Integer missed1_last5 = (Integer) patientPredictionVariables.get("missed1_last5");
		Double recent_late1_rate = 0.00;
		if(lastFiveVisits > 0 && missed1_last5 > 0) {
			recent_late1_rate = ((missed1_last5 * 1.00) / (lastFiveVisits * 1.00));
		}
		patientPredictionVariables.put("recent_late1_rate", recent_late1_rate);

		// Source Gender
		String inGender = patient.getGender().trim().toLowerCase();
		if(inGender.equalsIgnoreCase("m")) {
			patientPredictionVariables.put("GenderMale", 1);
		} else {
			patientPredictionVariables.put("GenderMale", 0);
		}
		if(inGender.equalsIgnoreCase("f")) {
			patientPredictionVariables.put("GenderFemale", 1);
		} else {
			patientPredictionVariables.put("GenderFemale", 0);
		}

		// Source Patient Source (Entry Point)
		Obs obsEntryPoint = getLatestObs(patient, Dictionary.METHOD_OF_ENROLLMENT);
		Concept conEntryPoint = null;
		if (obsEntryPoint != null) {
			conEntryPoint = obsEntryPoint.getValueCoded();
		}

		patientPredictionVariables.put("PatientSourceCCC", 0);
		patientPredictionVariables.put("PatientSourceIPDAdult", 0);
		patientPredictionVariables.put("PatientSourceMCH", 0);
		patientPredictionVariables.put("PatientSourceOPD", 0);
		patientPredictionVariables.put("PatientSourceOther", 0);
		patientPredictionVariables.put("PatientSourceTBClinic", 0);
		patientPredictionVariables.put("PatientSourceVCT", 0);

		if(conEntryPoint != null) {
			if (conEntryPoint == Context.getConceptService().getConceptByUuid("159940AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceVCT", 1);
			} else if (conEntryPoint == Context.getConceptService().getConceptByUuid("160541AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceTBClinic", 1);
			} else if (conEntryPoint == Context.getConceptService().getConceptByUuid("160542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceOPD", 1);
			} else if (conEntryPoint == Context.getConceptService().getConceptByUuid("160456AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceMCH", 1);
			} else if (conEntryPoint == Context.getConceptService().getConceptByUuid("5485AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceIPDAdult", 1);
			} else if (conEntryPoint == Context.getConceptService().getConceptByUuid("162050AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PatientSourceCCC", 1);
			} else {
				patientPredictionVariables.put("PatientSourceOther", 1);
			}
		}

		// Source Marital Status
		Obs obsMaritalStatus = getLatestObs(patient, Dictionary.CIVIL_STATUS);
		Concept conMaritalStatus = null;
		if (obsMaritalStatus != null) {
			conMaritalStatus = obsMaritalStatus.getValueCoded();
		}

		patientPredictionVariables.put("MaritalStatusDivorced", 0);
		patientPredictionVariables.put("MaritalStatusMarried", 0);
		patientPredictionVariables.put("MaritalStatusOther", 0);
		patientPredictionVariables.put("MaritalStatusPolygamous", 0);
		patientPredictionVariables.put("MaritalStatusSingle", 0);
		patientPredictionVariables.put("MaritalStatusWidow", 0);
		
		if(conMaritalStatus != null) {
			if (conMaritalStatus == Dictionary.getConcept(Dictionary.DIVORCED)) {
				patientPredictionVariables.put("MaritalStatusDivorced", 1);
			} else if (conMaritalStatus == Dictionary.getConcept(Dictionary.MARRIED_MONOGAMOUS)) {
				patientPredictionVariables.put("MaritalStatusMarried", 1);
			} else if (conMaritalStatus == Dictionary.getConcept(Dictionary.MARRIED_POLYGAMOUS)) {
				patientPredictionVariables.put("MaritalStatusPolygamous", 1);
			} else if (conMaritalStatus == Dictionary.getConcept(Dictionary.WIDOWED)) {
				patientPredictionVariables.put("MaritalStatusWidow", 1);
			} else if (conMaritalStatus == Dictionary.getConcept(Dictionary.NEVER_MARRIED)) {
				patientPredictionVariables.put("MaritalStatusSingle", 1);			
			} else {
				patientPredictionVariables.put("MaritalStatusOther", 1);
			}
		}

		// Source Population Type
		Obs obsPopulationType = getLatestObs(patient, "cf543666-ce76-4e91-8b8d-c0b54a436a2e");
		Concept conPopulationType = null;
		if (obsPopulationType != null) {
			conPopulationType = obsPopulationType.getValueCoded();
		}

		patientPredictionVariables.put("PopulationTypeGeneralPopulation", 0);
		patientPredictionVariables.put("PopulationTypeKeyPopulation", 0);
		patientPredictionVariables.put("PopulationTypePriorityPopulation", 0);
		
		if(conPopulationType != null) {
			if (conPopulationType == Context.getConceptService().getConceptByUuid("5d308c8c-ad49-45e1-9885-e5d09a8e5587")) {
				patientPredictionVariables.put("PopulationTypeGeneralPopulation", 1);
			}
			if (conPopulationType == Context.getConceptService().getConceptByUuid("bf850dd4-309b-4cbd-9470-9d8110ea5550")) {
				patientPredictionVariables.put("PopulationTypeKeyPopulation", 1);
			}
			if (conPopulationType == Context.getConceptService().getConceptByUuid("167143AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
				patientPredictionVariables.put("PopulationTypePriorityPopulation", 1);
			}
		}

		// Source Treatment Type
		patientPredictionVariables.put("TreatmentTypeART", 0);
		patientPredictionVariables.put("TreatmentTypePMTCT", 0);

		// Source Optimized HIV Regimen
		patientPredictionVariables.put("OptimizedHIVRegimenNo", 0);
		patientPredictionVariables.put("OptimizedHIVRegimenYes", 0);

		// Source Other Regimen
		patientPredictionVariables.put("Other_RegimenNo", 0);
		patientPredictionVariables.put("Other_RegimenYes", 0);

		// Source pregnant variable
		Obs obsPregancyStatus = getLatestObs(patient, Dictionary.PREGNANCY_STATUS);
		Concept conPregancyStatus = null;
		if (obsPregancyStatus != null) {
			conPregancyStatus = obsPregancyStatus.getValueCoded();
		}

		if(conPregancyStatus != null) {
			if (conPregancyStatus == Dictionary.getConcept(Dictionary.NO)) {
				patientPredictionVariables.put("PregnantNo", 1);
			} else {
				patientPredictionVariables.put("PregnantNo", 0);
			}
			if (conPregancyStatus == Dictionary.getConcept(Dictionary.YES)) {
				patientPredictionVariables.put("PregnantYes", 1);
			} else {
				patientPredictionVariables.put("PregnantYes", 0);
			}
			if (conPregancyStatus == Dictionary.getConcept(Dictionary.UNKNOWN)) {
				patientPredictionVariables.put("PregnantNR", 1);
			} else {
				patientPredictionVariables.put("PregnantNR", 0);
			}
			if (conPregancyStatus != Dictionary.getConcept(Dictionary.NO) && conPregancyStatus != Dictionary.getConcept(Dictionary.YES) && conPregancyStatus != Dictionary.getConcept(Dictionary.UNKNOWN)) {
				patientPredictionVariables.put("PregnantNR", 1);
			}
		} else {
			patientPredictionVariables.put("PregnantNo", 0);
			patientPredictionVariables.put("PregnantYes", 0);
			patientPredictionVariables.put("PregnantNR", 1);
		}

		//Source Differentiated Care
		patientPredictionVariables.put("DifferentiatedCareCommunityARTDistributionHCWLed", 0);
		patientPredictionVariables.put("DifferentiatedCareCommunityARTDistributionpeerled", 0);
		patientPredictionVariables.put("DifferentiatedCareFacilityARTdistributionGroup", 0);
		patientPredictionVariables.put("DifferentiatedCareFastTrack", 0);
		patientPredictionVariables.put("DifferentiatedCareStandardCare", 0);

		//Source most recent art adherence
		patientPredictionVariables.put("most_recent_art_adherencefair", 0);
		patientPredictionVariables.put("most_recent_art_adherencegood", 0);
		patientPredictionVariables.put("most_recent_art_adherencepoor", 0);

		//Source most recent ctx adherence
		patientPredictionVariables.put("most_recent_ctx_adherencefair", 0);
		patientPredictionVariables.put("most_recent_ctx_adherencegood", 0);
		patientPredictionVariables.put("most_recent_ctx_adherencepoor", 0);

		//Source Stability Assessment
		Obs obsStabilityAssessment = getLatestObs(patient, "1855AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		Boolean bolStabilityAssessment = null; // NB: Null is ok as default because we can test for it in case we dont get an answer below
		if (obsStabilityAssessment != null) {
			bolStabilityAssessment = obsStabilityAssessment.getValueBoolean();
		}

		patientPredictionVariables.put("StabilityAssessmentStable", 0);
		patientPredictionVariables.put("StabilityAssessmentUnstable", 0);

		if(bolStabilityAssessment != null) {
			if(bolStabilityAssessment == true) {
				patientPredictionVariables.put("StabilityAssessmentStable", 1);
			} else {
				patientPredictionVariables.put("StabilityAssessmentUnstable", 1);
			}
		}

		//Source Most Recent VL
		patientPredictionVariables.put("most_recent_vlHVL", 0);
		patientPredictionVariables.put("most_recent_vlLVL", 0);
		patientPredictionVariables.put("most_recent_vlSuppressed", 0);

		//Label
		patientPredictionVariables.put("label", 1);

		// Load model configs and variables
		mlScoringRequestPayload.put("modelConfigs", modelConfigs);
		mlScoringRequestPayload.put("variableValues", patientPredictionVariables);

		// Get JSON Payload
		String payload = mlScoringRequestPayload.toJson();
		System.out.println("IIT ML Prediction Payload: " + payload);
		
		// Get the IIT ML score
		try {
			//Extract score from payload
			// String mlScoreResponse = MLUtils.getIITMLScorePayloadMethod(payload);
			String mlScoreResponse = MLUtils.getIITMLScoreDirectMethod(payload);

			if(mlScoreResponse != null && !mlScoreResponse.trim().equalsIgnoreCase("")) {
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode jsonNode = (ObjectNode) mapper.readTree(mlScoreResponse);
				if (jsonNode != null) {
					Double riskScore = jsonNode.get("result").get("predictions").get("Probability_1").getDoubleValue();
					System.out.println("Got ML score as: " + riskScore);
					patientRiskScore.setRiskFactors("");
					patientRiskScore.setRiskScore(riskScore);
					patientRiskScore.setPatient(patient);
					if(riskScore <= decIITLowRiskThreshold) {
						patientRiskScore.setDescription("Low Risk");
					} else if((riskScore > decIITLowRiskThreshold) && (riskScore <= decIITMediumRiskThreshold)) {
						patientRiskScore.setDescription("Medium Risk");
					} else if((riskScore > decIITMediumRiskThreshold) && (riskScore <= decIITHighRiskThreshold)) {
						patientRiskScore.setDescription("High Risk");
					} else if(riskScore > decIITHighRiskThreshold) {
						patientRiskScore.setDescription("Highest Risk");
					}
					System.out.println("Got ML Description as: " + patientRiskScore.getDescription());
					patientRiskScore.setEvaluationDate(new Date());
					return(patientRiskScore);
				} else {
					System.err.println("Error: Unable to get ML score");
				}
			}
		}
		catch (Exception e) {
			System.err.println("ITT ML - Could not get the IIT Score: " + e.getMessage());
			e.printStackTrace();
		}

		//In case of an error
		patientRiskScore.setRiskFactors("");
		patientRiskScore.setRiskScore(1.00);
		patientRiskScore.setPatient(patient);
		patientRiskScore.setDescription("Unknown Risk");
		patientRiskScore.setEvaluationDate(new Date());
		return(patientRiskScore);
	}
}
