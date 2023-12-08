/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemrml.iit;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Patient;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A model for IIT - interruption in treatment score for a patient IIT evaluates the possibility of
 * a patient to have interruption in ARV treatment based on a number of factors It is a model class.
 * It should extend either {@link BaseOpenmrsObject} or {@link BaseOpenmrsMetadata}.
 */
public class PatientRiskScore extends BaseOpenmrsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Patient patient;
	
	private String sourceSystemUuid;
	
	private Double riskScore;
	
	private Date evaluationDate;

	private String description;

	private String riskFactors;

	private String payload;

	private String Age;
	private String AHDNo;
	private String AHDYes;
	private String average_tca_last5;
	private String averagelateness;
	private String averagelateness_last10;
	private String averagelateness_last3;
	private String averagelateness_last5;
	private String BMI;
	private String Breastfeedingno;
	private String BreastfeedingNR;
	private String Breastfeedingyes;
	private String DayFri;
	private String DayMon;
	private String DaySat;
	private String DaySun;
	private String DayThu;
	private String DayTue;
	private String DayWed;
	private String DifferentiatedCarecommunityartdistributionhcwled;
	private String DifferentiatedCarecommunityartdistributionpeerled;
	private String DifferentiatedCareexpress;
	private String DifferentiatedCarefacilityartdistributiongroup;
	private String DifferentiatedCarefasttrack;
	private String DifferentiatedCarestandardcare;
	private String GenderFemale;
	private String GenderMale;
	private String late;
	private String late_last10;
	private String late_last3;
	private String late_last5;
	private String late_rate;
	private String late28;
	private String late28_rate;
	private String MaritalStatusDivorced;
	private String MaritalStatusMarried;
	private String MaritalStatusMinor;
	private String MaritalStatusOther;
	private String MaritalStatusPolygamous;
	private String MaritalStatusSingle;
	private String MaritalStatusWidow;
	private String MonthApr;
	private String MonthAug;
	private String MonthDec;
	private String MonthFeb;
	private String MonthJan;
	private String MonthJul;
	private String MonthJun;
	private String MonthMar;
	private String MonthMay;
	private String MonthNov;
	private String MonthOct;
	private String MonthSep;
	private String most_recent_art_adherencefair;
	private String most_recent_art_adherencegood;
	private String most_recent_art_adherencepoor;
	private String most_recent_vlsuppressed;
	private String most_recent_vlunsuppressed;
	private String n_appts;
	private String n_hvl_threeyears;
	private String n_lvl_threeyears;
	private String n_tests_threeyears;
	private String NextAppointmentDate;
	private String num_hiv_regimens;
	private String OptimizedHIVRegimenNo;
	private String OptimizedHIVRegimenYes;
	private String PatientSourceOPD;
	private String PatientSourceOther;
	private String PatientSourceVCT;
	private String PopulationTypeGP;
	private String PopulationTypeKP;
	private String Pregnantno;
	private String PregnantNR;
	private String Pregnantyes;
	private String recent_hvl_rate;
	private String StabilityAssessmentStable;
	private String StabilityAssessmentUnstable;
	private String timeOnArt;
	private String unscheduled_rate;
	private String visit_1;
	private String visit_2;
	private String visit_3;
	private String visit_4;
	private String visit_5;
	private String Weight;

	private String mflCode;
	private String cccNumber;
	private String lastDwapiEtlUpdate;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(final Integer id) {
		this.id = id;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public String getSourceSystemUuid() {
		return sourceSystemUuid;
	}
	
	public void setSourceSystemUuid(String sourceSystemUuid) {
		this.sourceSystemUuid = sourceSystemUuid;
	}
	
	public Double getRiskScore() {
		return riskScore;
	}
	
	public void setRiskScore(Double riskScore) {
		this.riskScore = riskScore;
	}
	
	public Date getEvaluationDate() {
		return evaluationDate;
	}
	
	public void setEvaluationDate(Date evaluationDate) {
		this.evaluationDate = evaluationDate;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRiskFactors() {
		return riskFactors;
	}

	public void setRiskFactors(String riskFactors) {
		this.riskFactors = riskFactors;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getMflCode() {
		return mflCode;
	}

	public void setMflCode(String mflCode) {
		this.mflCode = mflCode;
	}

	public String getCccNumber() {
		return cccNumber;
	}

	public void setCccNumber(String cccNumber) {
		this.cccNumber = cccNumber;
	}

	public String getAge() {
		return Age;
	}

	public void setAge(String age) {
		Age = age;
	}

	public String getAHDNo() {
		return AHDNo;
	}

	public void setAHDNo(String AHDNo) {
		this.AHDNo = AHDNo;
	}

	public String getAHDYes() {
		return AHDYes;
	}

	public void setAHDYes(String AHDYes) {
		this.AHDYes = AHDYes;
	}

	public String getAverage_tca_last5() {
		return average_tca_last5;
	}

	public void setAverage_tca_last5(String average_tca_last5) {
		this.average_tca_last5 = average_tca_last5;
	}

	public String getAveragelateness() {
		return averagelateness;
	}

	public void setAveragelateness(String averagelateness) {
		this.averagelateness = averagelateness;
	}

	public String getAveragelateness_last10() {
		return averagelateness_last10;
	}

	public void setAveragelateness_last10(String averagelateness_last10) {
		this.averagelateness_last10 = averagelateness_last10;
	}

	public String getAveragelateness_last3() {
		return averagelateness_last3;
	}

	public void setAveragelateness_last3(String averagelateness_last3) {
		this.averagelateness_last3 = averagelateness_last3;
	}

	public String getAveragelateness_last5() {
		return averagelateness_last5;
	}

	public void setAveragelateness_last5(String averagelateness_last5) {
		this.averagelateness_last5 = averagelateness_last5;
	}

	public String getBMI() {
		return BMI;
	}

	public void setBMI(String BMI) {
		this.BMI = BMI;
	}

	public String getBreastfeedingno() {
		return Breastfeedingno;
	}

	public void setBreastfeedingno(String breastfeedingno) {
		Breastfeedingno = breastfeedingno;
	}

	public String getBreastfeedingNR() {
		return BreastfeedingNR;
	}

	public void setBreastfeedingNR(String breastfeedingNR) {
		BreastfeedingNR = breastfeedingNR;
	}

	public String getBreastfeedingyes() {
		return Breastfeedingyes;
	}

	public void setBreastfeedingyes(String breastfeedingyes) {
		Breastfeedingyes = breastfeedingyes;
	}

	public String getDayFri() {
		return DayFri;
	}

	public void setDayFri(String dayFri) {
		DayFri = dayFri;
	}

	public String getDayMon() {
		return DayMon;
	}

	public void setDayMon(String dayMon) {
		DayMon = dayMon;
	}

	public String getDaySat() {
		return DaySat;
	}

	public void setDaySat(String daySat) {
		DaySat = daySat;
	}

	public String getDaySun() {
		return DaySun;
	}

	public void setDaySun(String daySun) {
		DaySun = daySun;
	}

	public String getDayThu() {
		return DayThu;
	}

	public void setDayThu(String dayThu) {
		DayThu = dayThu;
	}

	public String getDayTue() {
		return DayTue;
	}

	public void setDayTue(String dayTue) {
		DayTue = dayTue;
	}

	public String getDayWed() {
		return DayWed;
	}

	public void setDayWed(String dayWed) {
		DayWed = dayWed;
	}

	public String getDifferentiatedCarecommunityartdistributionhcwled() {
		return DifferentiatedCarecommunityartdistributionhcwled;
	}

	public void setDifferentiatedCarecommunityartdistributionhcwled(
			String differentiatedCarecommunityartdistributionhcwled) {
		DifferentiatedCarecommunityartdistributionhcwled = differentiatedCarecommunityartdistributionhcwled;
	}

	public String getDifferentiatedCarecommunityartdistributionpeerled() {
		return DifferentiatedCarecommunityartdistributionpeerled;
	}

	public void setDifferentiatedCarecommunityartdistributionpeerled(
			String differentiatedCarecommunityartdistributionpeerled) {
		DifferentiatedCarecommunityartdistributionpeerled = differentiatedCarecommunityartdistributionpeerled;
	}

	public String getDifferentiatedCareexpress() {
		return DifferentiatedCareexpress;
	}

	public void setDifferentiatedCareexpress(String differentiatedCareexpress) {
		DifferentiatedCareexpress = differentiatedCareexpress;
	}

	public String getDifferentiatedCarefacilityartdistributiongroup() {
		return DifferentiatedCarefacilityartdistributiongroup;
	}

	public void setDifferentiatedCarefacilityartdistributiongroup(String differentiatedCarefacilityartdistributiongroup) {
		DifferentiatedCarefacilityartdistributiongroup = differentiatedCarefacilityartdistributiongroup;
	}

	public String getDifferentiatedCarefasttrack() {
		return DifferentiatedCarefasttrack;
	}

	public void setDifferentiatedCarefasttrack(String differentiatedCarefasttrack) {
		DifferentiatedCarefasttrack = differentiatedCarefasttrack;
	}

	public String getDifferentiatedCarestandardcare() {
		return DifferentiatedCarestandardcare;
	}

	public void setDifferentiatedCarestandardcare(String differentiatedCarestandardcare) {
		DifferentiatedCarestandardcare = differentiatedCarestandardcare;
	}

	public String getGenderFemale() {
		return GenderFemale;
	}

	public void setGenderFemale(String genderFemale) {
		GenderFemale = genderFemale;
	}

	public String getGenderMale() {
		return GenderMale;
	}

	public void setGenderMale(String genderMale) {
		GenderMale = genderMale;
	}

	public String getLate() {
		return late;
	}

	public void setLate(String late) {
		this.late = late;
	}

	public String getLate_last10() {
		return late_last10;
	}

	public void setLate_last10(String late_last10) {
		this.late_last10 = late_last10;
	}

	public String getLate_last3() {
		return late_last3;
	}

	public void setLate_last3(String late_last3) {
		this.late_last3 = late_last3;
	}

	public String getLate_last5() {
		return late_last5;
	}

	public void setLate_last5(String late_last5) {
		this.late_last5 = late_last5;
	}

	public String getLate_rate() {
		return late_rate;
	}

	public void setLate_rate(String late_rate) {
		this.late_rate = late_rate;
	}

	public String getLate28() {
		return late28;
	}

	public void setLate28(String late28) {
		this.late28 = late28;
	}

	public String getLate28_rate() {
		return late28_rate;
	}

	public void setLate28_rate(String late28_rate) {
		this.late28_rate = late28_rate;
	}

	public String getMaritalStatusDivorced() {
		return MaritalStatusDivorced;
	}

	public void setMaritalStatusDivorced(String maritalStatusDivorced) {
		MaritalStatusDivorced = maritalStatusDivorced;
	}

	public String getMaritalStatusMarried() {
		return MaritalStatusMarried;
	}

	public void setMaritalStatusMarried(String maritalStatusMarried) {
		MaritalStatusMarried = maritalStatusMarried;
	}

	public String getMaritalStatusMinor() {
		return MaritalStatusMinor;
	}

	public void setMaritalStatusMinor(String maritalStatusMinor) {
		MaritalStatusMinor = maritalStatusMinor;
	}

	public String getMaritalStatusOther() {
		return MaritalStatusOther;
	}

	public void setMaritalStatusOther(String maritalStatusOther) {
		MaritalStatusOther = maritalStatusOther;
	}

	public String getMaritalStatusPolygamous() {
		return MaritalStatusPolygamous;
	}

	public void setMaritalStatusPolygamous(String maritalStatusPolygamous) {
		MaritalStatusPolygamous = maritalStatusPolygamous;
	}

	public String getMaritalStatusSingle() {
		return MaritalStatusSingle;
	}

	public void setMaritalStatusSingle(String maritalStatusSingle) {
		MaritalStatusSingle = maritalStatusSingle;
	}

	public String getMaritalStatusWidow() {
		return MaritalStatusWidow;
	}

	public void setMaritalStatusWidow(String maritalStatusWidow) {
		MaritalStatusWidow = maritalStatusWidow;
	}

	public String getMonthApr() {
		return MonthApr;
	}

	public void setMonthApr(String monthApr) {
		MonthApr = monthApr;
	}

	public String getMonthAug() {
		return MonthAug;
	}

	public void setMonthAug(String monthAug) {
		MonthAug = monthAug;
	}

	public String getMonthDec() {
		return MonthDec;
	}

	public void setMonthDec(String monthDec) {
		MonthDec = monthDec;
	}

	public String getMonthFeb() {
		return MonthFeb;
	}

	public void setMonthFeb(String monthFeb) {
		MonthFeb = monthFeb;
	}

	public String getMonthJan() {
		return MonthJan;
	}

	public void setMonthJan(String monthJan) {
		MonthJan = monthJan;
	}

	public String getMonthJul() {
		return MonthJul;
	}

	public void setMonthJul(String monthJul) {
		MonthJul = monthJul;
	}

	public String getMonthJun() {
		return MonthJun;
	}

	public void setMonthJun(String monthJun) {
		MonthJun = monthJun;
	}

	public String getMonthMar() {
		return MonthMar;
	}

	public void setMonthMar(String monthMar) {
		MonthMar = monthMar;
	}

	public String getMonthMay() {
		return MonthMay;
	}

	public void setMonthMay(String monthMay) {
		MonthMay = monthMay;
	}

	public String getMonthNov() {
		return MonthNov;
	}

	public void setMonthNov(String monthNov) {
		MonthNov = monthNov;
	}

	public String getMonthOct() {
		return MonthOct;
	}

	public void setMonthOct(String monthOct) {
		MonthOct = monthOct;
	}

	public String getMonthSep() {
		return MonthSep;
	}

	public void setMonthSep(String monthSep) {
		MonthSep = monthSep;
	}

	public String getMost_recent_art_adherencefair() {
		return most_recent_art_adherencefair;
	}

	public void setMost_recent_art_adherencefair(String most_recent_art_adherencefair) {
		this.most_recent_art_adherencefair = most_recent_art_adherencefair;
	}

	public String getMost_recent_art_adherencegood() {
		return most_recent_art_adherencegood;
	}

	public void setMost_recent_art_adherencegood(String most_recent_art_adherencegood) {
		this.most_recent_art_adherencegood = most_recent_art_adherencegood;
	}

	public String getMost_recent_art_adherencepoor() {
		return most_recent_art_adherencepoor;
	}

	public void setMost_recent_art_adherencepoor(String most_recent_art_adherencepoor) {
		this.most_recent_art_adherencepoor = most_recent_art_adherencepoor;
	}

	public String getMost_recent_vlsuppressed() {
		return most_recent_vlsuppressed;
	}

	public void setMost_recent_vlsuppressed(String most_recent_vlsuppressed) {
		this.most_recent_vlsuppressed = most_recent_vlsuppressed;
	}

	public String getMost_recent_vlunsuppressed() {
		return most_recent_vlunsuppressed;
	}

	public void setMost_recent_vlunsuppressed(String most_recent_vlunsuppressed) {
		this.most_recent_vlunsuppressed = most_recent_vlunsuppressed;
	}

	public String getN_appts() {
		return n_appts;
	}

	public void setN_appts(String n_appts) {
		this.n_appts = n_appts;
	}

	public String getN_hvl_threeyears() {
		return n_hvl_threeyears;
	}

	public void setN_hvl_threeyears(String n_hvl_threeyears) {
		this.n_hvl_threeyears = n_hvl_threeyears;
	}

	public String getN_lvl_threeyears() {
		return n_lvl_threeyears;
	}

	public void setN_lvl_threeyears(String n_lvl_threeyears) {
		this.n_lvl_threeyears = n_lvl_threeyears;
	}

	public String getN_tests_threeyears() {
		return n_tests_threeyears;
	}

	public void setN_tests_threeyears(String n_tests_threeyears) {
		this.n_tests_threeyears = n_tests_threeyears;
	}

	public String getNextAppointmentDate() {
		return NextAppointmentDate;
	}

	public void setNextAppointmentDate(String nextAppointmentDate) {
		NextAppointmentDate = nextAppointmentDate;
	}

	public String getNum_hiv_regimens() {
		return num_hiv_regimens;
	}

	public void setNum_hiv_regimens(String num_hiv_regimens) {
		this.num_hiv_regimens = num_hiv_regimens;
	}

	public String getOptimizedHIVRegimenNo() {
		return OptimizedHIVRegimenNo;
	}

	public void setOptimizedHIVRegimenNo(String optimizedHIVRegimenNo) {
		OptimizedHIVRegimenNo = optimizedHIVRegimenNo;
	}

	public String getOptimizedHIVRegimenYes() {
		return OptimizedHIVRegimenYes;
	}

	public void setOptimizedHIVRegimenYes(String optimizedHIVRegimenYes) {
		OptimizedHIVRegimenYes = optimizedHIVRegimenYes;
	}

	public String getPatientSourceOPD() {
		return PatientSourceOPD;
	}

	public void setPatientSourceOPD(String patientSourceOPD) {
		PatientSourceOPD = patientSourceOPD;
	}

	public String getPatientSourceOther() {
		return PatientSourceOther;
	}

	public void setPatientSourceOther(String patientSourceOther) {
		PatientSourceOther = patientSourceOther;
	}

	public String getPatientSourceVCT() {
		return PatientSourceVCT;
	}

	public void setPatientSourceVCT(String patientSourceVCT) {
		PatientSourceVCT = patientSourceVCT;
	}

	public String getPopulationTypeGP() {
		return PopulationTypeGP;
	}

	public void setPopulationTypeGP(String populationTypeGP) {
		PopulationTypeGP = populationTypeGP;
	}

	public String getPopulationTypeKP() {
		return PopulationTypeKP;
	}

	public void setPopulationTypeKP(String populationTypeKP) {
		PopulationTypeKP = populationTypeKP;
	}

	public String getPregnantno() {
		return Pregnantno;
	}

	public void setPregnantno(String pregnantno) {
		Pregnantno = pregnantno;
	}

	public String getPregnantNR() {
		return PregnantNR;
	}

	public void setPregnantNR(String pregnantNR) {
		PregnantNR = pregnantNR;
	}

	public String getPregnantyes() {
		return Pregnantyes;
	}

	public void setPregnantyes(String pregnantyes) {
		Pregnantyes = pregnantyes;
	}

	public String getRecent_hvl_rate() {
		return recent_hvl_rate;
	}

	public void setRecent_hvl_rate(String recent_hvl_rate) {
		this.recent_hvl_rate = recent_hvl_rate;
	}

	public String getStabilityAssessmentStable() {
		return StabilityAssessmentStable;
	}

	public void setStabilityAssessmentStable(String stabilityAssessmentStable) {
		StabilityAssessmentStable = stabilityAssessmentStable;
	}

	public String getStabilityAssessmentUnstable() {
		return StabilityAssessmentUnstable;
	}

	public void setStabilityAssessmentUnstable(String stabilityAssessmentUnstable) {
		StabilityAssessmentUnstable = stabilityAssessmentUnstable;
	}

	public String getTimeOnArt() {
		return timeOnArt;
	}

	public void setTimeOnArt(String timeOnArt) {
		this.timeOnArt = timeOnArt;
	}

	public String getUnscheduled_rate() {
		return unscheduled_rate;
	}

	public void setUnscheduled_rate(String unscheduled_rate) {
		this.unscheduled_rate = unscheduled_rate;
	}

	public String getVisit_1() {
		return visit_1;
	}

	public void setVisit_1(String visit_1) {
		this.visit_1 = visit_1;
	}

	public String getVisit_2() {
		return visit_2;
	}

	public void setVisit_2(String visit_2) {
		this.visit_2 = visit_2;
	}

	public String getVisit_3() {
		return visit_3;
	}

	public void setVisit_3(String visit_3) {
		this.visit_3 = visit_3;
	}

	public String getVisit_4() {
		return visit_4;
	}

	public void setVisit_4(String visit_4) {
		this.visit_4 = visit_4;
	}

	public String getVisit_5() {
		return visit_5;
	}

	public void setVisit_5(String visit_5) {
		this.visit_5 = visit_5;
	}

	public String getWeight() {
		return Weight;
	}

	public void setWeight(String weight) {
		Weight = weight;
	}

	public String getLastDwapiEtlUpdate() {
		return lastDwapiEtlUpdate;
	}

	public void setLastDwapiEtlUpdate(String lastDwapiEtlUpdate) {
		this.lastDwapiEtlUpdate = lastDwapiEtlUpdate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		PatientRiskScore that = (PatientRiskScore) o;
		return id.equals(that.id) && patient.equals(that.patient) && sourceSystemUuid.equals(that.sourceSystemUuid)
		        && riskScore.equals(that.riskScore) && evaluationDate.equals(that.evaluationDate);
	}
	
	@Override
	public String toString() {
		return "PatientRiskScore [description=" + description + ", evaluationDate=" + evaluationDate + ", id=" + id
				+ ", patient=" + patient + ", riskFactors=" + riskFactors + ", riskScore=" + riskScore
				+ ", sourceSystemUuid=" + sourceSystemUuid + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, patient, sourceSystemUuid, riskScore, evaluationDate);
	}
}
