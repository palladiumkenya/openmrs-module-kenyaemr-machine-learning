package org.openmrs.module.kenyaemrml.iit;

import java.util.Date;

public class Appointment {
    private Integer patientID;
    private Date encounterDate;
    private Date appointmentDate;

    public Appointment(Integer patientID, Date encounterDate, Date appointmentDate) {
        this.patientID = patientID;
        this.encounterDate = encounterDate;
        this.appointmentDate = appointmentDate;
    }
    public Appointment() {
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
    public Date getAppointmentDate() {
        return appointmentDate;
    }
    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    @Override
    public String toString() {
        return "Appointments [patientID=" + patientID + ", encounterDate=" + encounterDate + ", appointmentDate="
                + appointmentDate + "]";
    }
}
