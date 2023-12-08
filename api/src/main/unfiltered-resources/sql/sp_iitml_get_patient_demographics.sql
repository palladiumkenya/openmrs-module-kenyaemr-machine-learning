DROP PROCEDURE IF EXISTS sp_iitml_get_patient_demographics;
$$
CREATE PROCEDURE `sp_iitml_get_patient_demographics`(
	IN in_patient_id int
)
BEGIN
select
       d.patient_id                                                                as PatientPK,
       case d.gender when 'M' then 'Male' when 'F' then 'Female' end               as Gender,
       d.dob                                                                       as DOB,
       case max(hiv.entry_point)
           when 160542 then 'OPD'
           when 160563 then 'Other'
           when 160539 then 'VCT'
           when 160538 then 'PMTCT'
           when 160541 then 'TB'
           when 160536 then 'IPD - Adult'
           /*else cn.name*/
           end                                                                     as PatientSource,
       UPPER(d.marital_status)                                                     as MaritalStatus,
       UPPER(d.education_level)                                                    as EducationLevel,
       if(c.client_id is not null, 'Key population', (select CASE
                                                                 WHEN mid(max(concat(f.visit_date, f.population_type)), 11) = 164929
                                                                     THEN 'Key population'
                                                                 WHEN mid(max(concat(f.visit_date, f.population_type)), 11) = 164928
                                                                     THEN 'General Population'
                                                                 END
                                                      from dwapi_etl.etl_patient_hiv_followup f
                                                      WHERE f.encounter_id = max(enr.encounter_id)
                                                      group by f.patient_id))      AS PopulationType
from dwapi_etl.etl_hiv_enrollment hiv
         inner join dwapi_etl.etl_patient_demographics d on hiv.patient_id = d.patient_id
         left outer join dwapi_etl.etl_mch_enrollment mch on mch.patient_id = d.patient_id
         left outer join dwapi_etl.etl_patient_hiv_followup enr on enr.patient_id = d.patient_id
         left outer join dwapi_etl.etl_tb_enrollment tb on tb.patient_id = d.patient_id
         left outer join dwapi_etl.etl_drug_event de on de.patient_id = d.patient_id
         left join concept_name ts on ts.concept_id = hiv.relationship_of_treatment_supporter and
                                      ts.concept_name_type = 'FULLY_SPECIFIED' and ts.locale = 'en'
         left join person_address patAd ON patAd.person_id = d.patient_id and patAd.voided = 0
         left join
     (select Patient_Id,
             program,
             if(mid(max(concat(date_enrolled, date_completed)), 20) is null, 'Active', 'Inactive') as status,
             date_created,
             date_last_modified
      from dwapi_etl.etl_patient_program
      group by Patient_Id, program) as prg on prg.patient_id = d.patient_id
         left join dwapi_etl.etl_contact c
                   on c.client_id = d.patient_id and prg.status = 'Active' and prg.program = 'KP'
         join kenyaemr_etl.etl_default_facility_info i
where unique_patient_no is not null and
	d.patient_id = in_patient_id
group by d.patient_id
order by d.patient_id;
END
$$
