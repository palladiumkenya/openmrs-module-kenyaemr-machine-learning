package org.openmrs.module.kenyaemrml.iit;

import java.util.Date;

public class Treatment {
	private Integer patientID;
	private Date encounterDate;
	private String drug;
	private String treatmentType;

	public Treatment() {
	}

	public Treatment(Integer patientID, Date encounterDate, String drug, String treatmentType) {
		this.patientID = patientID;
		this.encounterDate = encounterDate;
		this.drug = drug;
		this.treatmentType = treatmentType;
	}

	public Integer getPatientID() {
		return patientID;
	}

	public void setPatientID(Integer patientID) {
		this.patientID = patientID;
	}

	public Date getEncounterDate() {
		return encounterDate;
	}

	public void setEncounterDate(Date encounterDate) {
		this.encounterDate = encounterDate;
	}

	public String getDrug() {
		return drug;
	}

	public void setDrug(String drug) {
		this.drug = drug;
	}

	public String getTreatmentType() {
		return treatmentType;
	}

	public void setTreatmentType(String treatmentType) {
		this.treatmentType = treatmentType;
	}

	@Override
	public String toString() {
		return "Treatment{" +
				"patientID=" + patientID +
				", encounterDate=" + encounterDate +
				", drug='" + drug + '\'' +
				", treatmentType='" + treatmentType + '\'' +
				'}';
	}
}
