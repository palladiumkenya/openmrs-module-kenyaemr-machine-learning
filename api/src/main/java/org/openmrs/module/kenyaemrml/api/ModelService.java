package org.openmrs.module.kenyaemrml.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.kenyaemrml.domain.ModelInputFields;
import org.openmrs.module.kenyaemrml.domain.ScoringResult;
import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
import org.openmrs.ui.framework.SimpleObject;

public interface ModelService extends OpenmrsService {
	
	ScoringResult htsscore(String modelId, String facilityName, String encounterDate, ModelInputFields inputFields,
	        boolean debug);
	
	ScoringResult iitscore(String modelId, String facilityName, String encounterDate, ModelInputFields inputFields,
	        boolean debug);

	Map<String, Object> score(Evaluator evaluator, ModelInputFields inputFields, boolean debug);
	
	Map<FieldName, FieldValue> prepareEvaluationArgs(Evaluator evaluator, ModelInputFields inputFields);

	Obs getLatestObs(Patient patient, String conceptIdentifier);

	Integer getNumberOfObs(Patient patient, String conceptIdentifier);

	Integer getHighViralLoadCount(Patient patient);

	Double getLatestViralLoadCount(Patient patient);

	Double getLatestHighViralLoadCount(Patient patient);

	Double getLatestLowViralLoadCount(Patient patient);

	Double getLatestSuppressedViralLoadCount(Patient patient);

	Integer getAllViralLoadCountLastThreeYears(Patient patient);

	Integer getHighViralLoadCountLastThreeYears(Patient patient);

	Integer getLowViralLoadCount(Patient patient);

	Integer getSuppressedViralLoadCount(Patient patient);

	Integer getTotalAppointments(Patient patient);

	SimpleObject getMissedAppointments(Patient patient);

	Integer getHonouredAppointments(Patient patient);

	SimpleObject getEncDetails(Set<Obs> obsList, Encounter e, List<Encounter> allClinicalEncounters);

	boolean hasVisitOnDate(Date appointmentDate, Patient patient, List<Encounter> allEncounters);

	boolean wasScheduledVisit(Encounter encounter);

	Integer getTotalUnscheduledVisits(Patient patient);
	
	Integer getLatestARTAdherence(Patient patient);

	Integer getLatestCTXAdherence(Patient patient);

	Integer getTotalPoorARTAdherence(Patient patient);

	Integer getTotalFairARTAdherence(Patient patient);

	Integer getTotalPoorCTXAdherence(Patient patient);

	Integer getTotalFairCTXAdherence(Patient patient);

	Integer getTotalARTAdherence(Patient patient);

	Integer getTotalCTXAdherence(Patient patient);

	Double getChangeInWeightInTheLastSixMonths(Patient patient);

	Double getChangeInBMIInTheLastSixMonths(Patient patient);

	Obs getObsByConceptPatientAndDate(Patient patient, Concept concept, Date date);

	PatientRiskScore generatePatientRiskScore(Patient patient);

	long getMemoryConsumption();
}
