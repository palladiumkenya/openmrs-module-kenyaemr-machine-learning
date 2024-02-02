DROP PROCEDURE IF EXISTS sp_iitml_get_visits;
$$
CREATE PROCEDURE `sp_iitml_get_visits`(
	IN in_patient_id int
)
BEGIN
select
       d.patient_id                                                           as PatientPK,
       case
           when fup.visit_date < '1990-01-01' then null
           else CAST(fup.visit_date AS DATE) end                              AS VisitDate,
       case
           when fup.next_appointment_date < '1990-01-01' then null
           else CAST(fup.next_appointment_date AS DATE) end                   AS NextAppointmentDate,
		(case fup.visit_scheduled
            when 1 then "Scheduled"
            when 2 then 'Unscheduled'
            else "" end)                                                      as VisitType,
		t.height                                                               as Height,
		t.weight                                                               as Weight,
        case fup.pregnancy_status
           when 1065 then 'Yes'
           when 1066 then 'No'
           end                                                                as Pregnant,
		(case fup.differentiated_care
            when 164942 then "Standard Care"
            when 164943 then "Fast Track"
            when 164944 then "Community ART Distribution - HCW Led"
            when 164945 then "Community ART Distribution - Peer Led"
            when 164946 then "Facility ART Distribution Group"
            else "" end)                                                      as DifferentiatedCare,
		CASE fup.stability
           WHEN 1 THEN 'Stable'
           WHEN 2
               THEN 'Not Stable' END                                          as StabilityAssessment,
		concat(
               IF(fup.arv_adherence = 159405, 'Good',
                  IF(fup.arv_adherence = 159406, 'Fair', IF(fup.arv_adherence = 159407, 'Poor', ''))),
               IF(fup.arv_adherence in (159405, 159406, 159407), '|', ''),
               IF(fup.ctx_adherence = 159405, 'Good',
                  IF(fup.ctx_adherence = 159406, 'Fair', IF(fup.ctx_adherence = 159407, 'Poor', '')))
           )                                                                  AS Adherence,
		case fup.who_stage
           when 1220 then 1
           when 1221 then 2
           when 1222 then 3
           when 1223 then 4
           when 1204 then 1
           when 1205 then 2
           when 1206 then 3
           when 1207 then 4
           else ''
           end                                                                as WHOStage,
		if(d.gender = 'F', (case fup.breastfeeding when 1065 then 'Yes' when 1066 then 'No' end),
          'N/A')                                                              as Breastfeeding,
		'ART|CTX'                                                              as AdherenceCategory
from dwapi_etl.etl_patient_demographics d
         join dwapi_etl.etl_patient_hiv_followup fup on fup.patient_id = d.patient_id
         join kenyaemr_etl.etl_default_facility_info i
         left join (select t.patient_id,
                           t.visit_date,
                           t.weight,
                           t.height,
                           t.systolic_pressure,
                           t.diastolic_pressure,
                           t.temperature,
                           t.z_score_absolute,
                           t.z_score,
                           t.pulse_rate,
                           t.respiratory_rate,
                           t.oxygen_saturation,
                           t.muac,
                           t.nutritional_status
                    from dwapi_etl.etl_patient_triage t) t
                   on fup.patient_id = t.patient_id and date(fup.visit_date) = date(t.visit_date)
         left join (select de.patient_id, mid(max(concat(de.visit_date, de.regimen)), 11) as regimen
                    from dwapi_etl.etl_drug_event de
                    where de.discontinued is null
                    group by de.patient_id) de on fup.patient_id = de.patient_id
where d.unique_patient_no is not null
  and fup.visit_date > '1990-01-01'
  and fup.next_appointment_date is not null
  and fup.visit_id is not null
  and d.patient_id = in_patient_id
order by fup.visit_date asc;
END
$$
