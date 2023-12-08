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

	PatientRiskScore generatePatientRiskScore(Patient patient);

	long getMemoryConsumption();
}
