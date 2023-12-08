DROP PROCEDURE IF EXISTS sp_iitml_get_pharmacy_visits;
$$
CREATE PROCEDURE `sp_iitml_get_pharmacy_visits`(
	IN in_patient_id int
)
BEGIN
select
       d.patient_id                                                 as PatientPK,
       ph.visit_date                                                as DispenseDate,
       DATE_ADD(ph.visit_date, INTERVAL ph.duration DAY)            as ExpectedReturn,
       Drug,
       CASE
           WHEN is_arv = 1 THEN 'ARV'
           WHEN is_ctx = 1 OR is_dapsone = 1 THEN 'Prophylaxis' END AS TreatmentType
from (SELECT *
      FROM ((select patient_id,
                    uuid,
                    visit_id,
                    visit_date,
                    encounter_id,
                    (case drug when 105281 then 'CTX' when 74250 then 'DAPSONE' end) as drug,
                    is_arv,
                    is_ctx,
                    is_dapsone,
                    drug_name                                                        as drugreg,
                    frequency,
                    visit_date                                                       as DispenseDate,
                    duration_in_days                                                 as duration,
                    duration_in_days                                                 as PeriodTaken,
                    DATE_ADD(visit_date, INTERVAL duration_in_days DAY)              as ExpectedReturn,
                    CASE WHEN is_ctx = 1 OR is_dapsone = 1 THEN 'Prophylaxis' END    AS TreatmentType,
                    ''                                                               as RegimenLine,
                    ''                                                               as regimen,
                    CASE
                        WHEN is_ctx = 1 THEN 'CTX'
                        WHEN is_dapsone = 1 THEN 'DAPSONE' END                       AS ProphylaxisType,
                    ''                                                               as previousRegimen,
                    ''                                                               as RegimenChangeSwitchReason,
                    ''                                                               as StopRegimenReason,
                    ''                                                               as StopRegimenDate,
                    ''                                                               as prev_Regimen,
                    ph.date_created,
                    ph.date_last_modified,
                    ph.voided                                                        as voided
             from dwapi_etl.etl_pharmacy_extract ph
             where is_arv = 1
             group by ph.encounter_id
             order by patient_id, DispenseDate)
            UNION ALL
            (SELECT e.patient_id                                                                        as patient_id,
                    e.uuid                                                                              as uuid,
                    ''                                                                                  as visit_id,
                    e.visit_date,
                    e.encounter_id,
                    regimen                                                                                drug,
                    1                                                                                   as is_arv,
                    0                                                                                   as is_ctx,
                    0                                                                                   as is_dapsone,
                    regimen_name                                                                        as drugreg,
                    ''                                                                                     frequency,
                    date_started                                                                        as DispenseDate,
                    null                                                                                as duration,
                    null                                                                                as PeriodTaken,
                    null                                                                                as ExpectedReturn,
                    'ARV'                                                                               AS TreatmentType,
                    e.regimen_line                                                                      as regimen_line,
                    e.regimen                                                                           as regimen,
                    ''                                                                                  as ProphylaxisType,
                    @prev_regimen                                                                          previousRegimen,
                    (case reason_discontinued
                         when 160559 then 'Risk of pregnancy'
                         when 160561 then 'New drug available'
                         when 160567 then 'New diagnosis of Tuberculosis'
                         when 160569 then 'Virological failure'
                         when 159598 then 'Non-compliance with treatment or therapy'
                         when 1754 then 'Drugs out of stock'
                         when 1434 then 'Pregnancy'
                         when 1253 then 'Completed PMTCT'
                         when 843 then 'Clinical treatment failure'
                         when 160566 then 'Immunological failure'
                         when 102 then 'Drug toxicity'
                         when 5622 then 'Other'
                         else '' end)                                                                   as RegimenChangeSwitchReason,
                    (case reason_discontinued
                         when 102 then 'Drug toxicity'
                         when 160567 then 'New diagnosis of Tuberculosis'
                         when 160569 then 'Virologic failure'
                         when 159598 then 'Non-compliance with treatment or therapy'
                         when 1754 then 'Medications unavailable'
                         when 160016 then 'Planned Treatment interruption'
                         when 1434 then 'Currently pregnant'
                         when 1253 then 'Completed PMTCT'
                         when 843 then 'Regimen failure'
                         when 5622 then 'Other'
                         when 160559 then 'Risk of pregnancy'
                         when 160561 then 'New drug available'
                         else '' end)                                                                   as StopRegimenReason,
                    if(discontinued = 1, date_discontinued, NULL)                                       as StopRegimenDate,
                    @prev_regimen := e.regimen                                                             prev_regimen,
                    e.date_created                                                                      as date_created,
                    e.date_last_modified                                                                as date_last_modified,
                    e.voided                                                                            as voided
             FROM dwapi_etl.etl_drug_event e,
                  (SELECT @s := 0, @prev_regimen := -1, @x := 0, @prev_regimen_line := -1) s
             where e.program = 'HIV'
             group by e.encounter_id
             ORDER BY e.patient_id, e.date_started)
            UNION ALL
            (select patient_id,
                    uuid,
                    visit_id,
                    visit_date,
                    encounter_id,
                    drug_short_name                                                             as drug,
                    1                                                                           as is_arv,
                    ''                                                                          as is_ctx,
                    ''                                                                          as is_dapsone,
                    drug_name                                                                   as drugreg,
                    frequency,
                    visit_date                                                                  as DispenseDate,
                    (case duration_units
                         when 'DAYS' then duration
                         when 'MONTHS' then duration * 30
                         when 'WEEKS' then duration * 7 end)                                    as duration,
                    (case duration_units
                         when 'DAYS' then duration
                         when 'MONTHS' then duration * 30
                         when 'WEEKS' then duration * 7 end)                                    as PeriodTaken,
                    DATE_ADD(visit_date, INTERVAL (case duration_units
                                                       when 'DAYS' then duration
                                                       when 'MONTHS' then duration * 30
                                                       when 'WEEKS' then duration * 7 end) DAY) as ExpectedReturn,
                    ''                                                                          AS TreatmentType,
                    ''                                                                          as RegimenLine,
                    drug_name                                                                   as regimen,
                    ''                                                                          AS ProphylaxisType,
                    ''                                                                          as previousRegimen,
                    ''                                                                          as RegimenChangeSwitchReason,
                    ''                                                                          as StopRegimenReason,
                    ''                                                                          as StopRegimenDate,
                    ''                                                                          as prev_Regimen,
                    do.date_created,
                    do.date_last_modified,
                    do.voided                                                                   as voided
             from dwapi_etl.etl_drug_order do
             group by do.order_group_id, do.patient_id, do.encounter_id
             order by do.patient_id, DispenseDate)) A
      order by A.DispenseDate, A.patient_id) ph
         join dwapi_etl.etl_patient_demographics d on d.patient_id = ph.patient_id
         left outer join concept_name cn on cn.concept_id = ph.drug and cn.concept_name_type = 'FULLY_SPECIFIED'
    and cn.locale = 'en'
         left outer join concept_name cn2 on cn2.concept_id = ph.drug and cn2.concept_name_type = 'SHORT'
    and cn.locale = 'en'
         left outer join dwapi_etl.etl_patient_hiv_followup fup on fup.encounter_id = ph.encounter_id
    and fup.patient_id = ph.patient_id
         join kenyaemr_etl.etl_default_facility_info i
where unique_patient_no is not null
  and drugreg is not null
  and d.patient_id = in_patient_id
order by ph.visit_date asc;
END
$$
