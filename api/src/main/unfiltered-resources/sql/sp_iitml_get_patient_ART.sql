DROP PROCEDURE IF EXISTS sp_iitml_get_patient_ART;
$$
CREATE PROCEDURE `sp_iitml_get_patient_ART`(
	IN in_patient_id int
)
BEGIN
select
	   d.patient_id                                                                    as PatientPK,
       DATE(least(ifnull(mid(min(concat(hiv.visit_date, hiv.date_started_art_at_transferring_facility)), 11),
                    reg.date_started + interval rand() * 1000 year), reg.date_started)) as StartARTDate
from dwapi_etl.etl_hiv_enrollment hiv
         join dwapi_etl.etl_patient_demographics d on d.patient_id = hiv.patient_id
         left outer join (select d.patient_id,
                                 coalesce(max(date(d.effective_discontinuation_date)),
                                          max(date(d.visit_date))) as ExitDate,
                                 (case d.discontinuation_reason
                                      when 159492 then 'Transfer Out'
                                      when 160034 then 'Died'
                                      when 5240 then 'LTFU'
                                      when 819 then 'Stopped Treatment'
                                      else '' end)                 as ExitReason,
                                 max(d.date_last_modified)         as date_last_modified
                          from dwapi_etl.etl_patient_program_discontinuation d
                          where d.program_name = 'HIV'
                          group by d.patient_id) disc on disc.patient_id = hiv.patient_id
         left outer join (select e.patient_id,
                                 e.uuid                                                               as uuid,
                                 min(e.date_started)                                                  as art_start_date,
                                 min(e.date_started)                                                  as date_started_art_this_facility,
                                 e.date_started,
                                 e.gender,
                                 e.dob,
                                 d.ExitDate                                                           as dis_date,
                                 if(d.ExitDate is not null, 1, 0)                                     as TOut,
                                 e.regimen,
                                 e.regimen_line,
                                 e.alternative_regimen,
                                 max(fup.next_appointment_date)                                       as latest_tca,
                                 last_art_date,
                                 last_regimen,
                                 last_regimen_line,
                                 if(enr.transfer_in_date is not null, 1, 0)                           as TIn,
                                 if(enr.transfer_in_date is not null, enr.previous_art_use, NULL)     as PreviousARTUse,
                                 if(enr.transfer_in_date is not null, enr.previous_art_purpose, NULL) as PreviousARTPurpose,
                                 if(enr.transfer_in_date is not null, enr.previous_art_regimen, NULL) as PreviousARTRegimen,
                                 if(enr.transfer_in_date is not null,
                                    enr.transfer_in_date,
                                    NULL)                                                             as DateLastUsed,
                                 max(fup.visit_date)                                                  as latest_vis_date,
                                 e.date_created,
                                 e.date_last_modified
                          from (select e.patient_id,
                                       e.uuid,
                                       p.dob,
                                       p.Gender,
                                       min(e.date_started)                                             as date_started,
                                       max(e.date_started)                                             as last_art_date,
                                       mid(min(concat(e.date_started, e.regimen_name)), 11)            as regimen,
                                       mid(min(concat(e.date_started, e.regimen_line)), 11)            as regimen_line,
                                       mid(max(concat(e.date_started, e.regimen_name)), 11)            as last_regimen,
                                       mid(max(concat(e.date_started, e.regimen_line)), 11)            as last_regimen_line,
                                       max(if(discontinued, 1, 0))                                     as alternative_regimen,
                                       GREATEST(COALESCE(p.date_created, ph.date_created),
                                                COALESCE(ph.date_created, p.date_created))             as date_created,
                                       GREATEST(COALESCE(p.date_last_modified, ph.date_last_modified),
                                                COALESCE(ph.date_last_modified, p.date_last_modified)) as date_last_modified,
                                       e.provider                                                      as provider
                                from dwapi_etl.etl_drug_event e
                                         join dwapi_etl.etl_patient_demographics p on p.patient_id = e.patient_id
                                         left join dwapi_etl.etl_pharmacy_extract ph
                                                   on ph.patient_id = e.patient_id and is_arv = 1
                                where e.program = 'HIV'
                                group by e.patient_id) e
                                   left outer join (select d.patient_id,
                                                           coalesce(max(date(d.effective_discontinuation_date)),
                                                                    max(date(d.visit_date))) ExitDate
                                                    from dwapi_etl.etl_patient_program_discontinuation d
                                                    where d.program_name = 'HIV'
                                                    group by d.patient_id) d on d.patient_id = e.patient_id
                                   inner join (select e.patient_id                                           as patient_id,
                                                      mid(max(concat(e.visit_date, e.transfer_in_date)), 11) as transfer_in_date,
                                                      case mid(max(concat(e.visit_date, e.arv_status)), 11)
                                                          when 1 then 'Yes'
                                                          when 0
                                                              then 'No' end                                  as previous_art_use,
                                                      concat_ws('|', NULLIF(
                                                              case mid(max(concat(pre.visit_date, pre.PMTCT)), 11)
                                                                  when 1065 then 'PMTCT' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.PEP)), 11)
                                                                            when 1 then 'PEP' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.PrEP)), 11)
                                                                            when 1065 then 'PrEP' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.HAART)), 11)
                                                                            when 1185 then 'HAART' end, '')
                                                          )                                                  as previous_art_purpose,
                                                      concat_ws('|', NULLIF(
                                                              case mid(max(concat(pre.visit_date, pre.PMTCT_regimen)), 11)
                                                                  when 164968 then 'AZT+3TC+DTG'
                                                                  when 164969 then 'TDF+3TC+DTG'
                                                                  when 164970 then 'ABC+3TC+DTG'
                                                                  when 164505 then 'TDF+3TC+EFV'
                                                                  when 792 then 'D4T+3TC+NVP'
                                                                  when 160124 then 'AZT+3TC+EFV'
                                                                  when 160104 then 'D4T+3TC+EFV'
                                                                  when 161361 then 'EDF+3TC+EFV'
                                                                  when 104565 then 'EFV+FTC+TDF'
                                                                  when 162201 then '3TC+LPV+TDF+r'
                                                                  when 817 then 'ABC+3TC+AZT'
                                                                  when 162199 then 'ABC+NVP+3TC'
                                                                  when 162200 then '3TC+ABC+LPV+r'
                                                                  when 162565 then '3TC+NVP+TDF'
                                                                  when 1652 then '3TC+NVP+AZT'
                                                                  when 162561 then '3TC+AZT+LPV+r'
                                                                  when 164511 then 'AZT-3TC-ATV+r'
                                                                  when 164512 then 'TDF-3TC-ATV+r'
                                                                  when 162560 then '3TC+D4T+LPV+r'
                                                                  when 162563 then '3TC+ABC+EFV'
                                                                  when 162562 then 'ABC+LPV+R+TDF'
                                                                  when 162559 then 'ABC+DDI+LPV+r' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.PEP_regimen)), 11)
                                                                            when 164968 then 'AZT+3TC+DTG'
                                                                            when 164969 then 'TDF+3TC+DTG'
                                                                            when 164970 then 'ABC+3TC+DTG'
                                                                            when 164505 then 'TDF+3TC+EFV'
                                                                            when 792 then 'D4T+3TC+NVP'
                                                                            when 160124 then 'AZT+3TC+EFV'
                                                                            when 160104 then 'D4T+3TC+EFV'
                                                                            when 1652 then '3TC+NVP+AZT'
                                                                            when 161361 then 'EDF+3TC+EFV'
                                                                            when 104565 then 'EFV+FTC+TDF'
                                                                            when 162201 then '3TC+LPV+TDF+r'
                                                                            when 817 then 'ABC+3TC+AZT'
                                                                            when 162199 then 'ABC+NVP+3TC'
                                                                            when 162200 then '3TC+ABC+LPV+r'
                                                                            when 162565 then '3TC+NVP+TDF'
                                                                            when 162561 then '3TC+AZT+LPV+r'
                                                                            when 164511 then 'AZT+3TC+ATV+r'
                                                                            when 164512 then 'TDF+3TC+ATV+r'
                                                                            when 162560 then '3TC+D4T+LPV+r'
                                                                            when 162563 then '3TC+ABC+EFV'
                                                                            when 162562 then 'ABC+LPV/R+TDF'
                                                                            when 162559 then 'ABC+DDI+LPV+r' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.PrEP_regimen)), 11)
                                                                            when 164968 then 'AZT+3TC+DTG'
                                                                            when 164969 then 'TDF+3TC+DTG'
                                                                            when 164970 then 'ABC+3TC+DTG'
                                                                            when 164505 then 'TDF+3TC+EFV'
                                                                            when 792 then 'D4T+3TC+NVP'
                                                                            when 160124 then 'AZT+3TC+EFV'
                                                                            when 160104 then 'D4T+3TC+EFV'
                                                                            when 161361 then 'EDF+3TC+EFV'
                                                                            when 104565 then 'EFV+FTC+TDF'
                                                                            when 162201 then '3TC+LPV+TDF+r'
                                                                            when 817 then 'ABC+3TC+AZT'
                                                                            when 162199 then 'ABC+NVP+3TC'
                                                                            when 162200 then '3TC+ABC+LPV+r'
                                                                            when 162565 then '3TC+NVP+TDF'
                                                                            when 1652 then '3TC+NVP+AZT'
                                                                            when 162561 then '3TC+AZT+LPV+r'
                                                                            when 164511 then 'AZT+3TC+ATV+r'
                                                                            when 164512 then 'TDF+3TC+ATV+r'
                                                                            when 162560 then '3TC+D4T+LPV+r'
                                                                            when 162563 then '3TC+ABC+EFV'
                                                                            when 162562 then 'ABC+LPV+R+TDF'
                                                                            when 162559 then 'ABC+DDI+LPV+r' end, ''),
                                                                NULLIF(
                                                                        case mid(max(concat(pre.visit_date, pre.HAART_regimen)), 11)
                                                                            when 164968 then 'AZT+3TC+DTG'
                                                                            when 164969 then 'TDF+3TC+DTG'
                                                                            when 164970 then 'ABC+3TC+DTG'
                                                                            when 164505 then 'TDF+3TC+EFV'
                                                                            when 792 then 'D4T+3TC+NVP'
                                                                            when 160124 then 'AZT+3TC+EFV'
                                                                            when 160104 then 'D4T+3TC+EFV'
                                                                            when 161361 then 'EDF+3TC+EFV'
                                                                            when 104565 then 'EFV+FTC+TDF'
                                                                            when 162201 then '3TC+LPV+TDF+r'
                                                                            when 817 then 'ABC+3TC+AZT'
                                                                            when 162199 then 'ABC+NVP+3TC'
                                                                            when 162200 then '3TC+ABC+LPV+r'
                                                                            when 162565 then '3TC+NVP+TDF'
                                                                            when 1652 then '3TC+NVP+AZT'
                                                                            when 162561 then '3TC+AZT+LPV+r'
                                                                            when 164511 then 'AZT+3TC+ATV+r'
                                                                            when 164512 then 'TDF+3TC+ATV+r'
                                                                            when 162560 then '3TC+D4T+LPV+r'
                                                                            when 162563 then '3TC+ABC+EFV'
                                                                            when 162562 then 'ABC+LPV+R+TDF'
                                                                            when 162559 then 'ABC+DDI+LPV+r' end, '')
                                                          )                                                  as previous_art_regimen
                                               from dwapi_etl.etl_hiv_enrollment e
                                                        left join dwapi_etl.etl_pre_hiv_enrollment_art pre
                                                                  on e.encounter_id = pre.encounter_id and e.patient_id = pre.patient_id
                                               group by e.patient_id) enr on enr.patient_id = e.patient_id
                                   left outer join dwapi_etl.etl_patient_hiv_followup fup
                                                   on fup.patient_id = e.patient_id
                          group by e.patient_id) reg on reg.patient_id = hiv.patient_id
         join kenyaemr_etl.etl_default_facility_info i
where d.unique_patient_no is not null and
	d.patient_id = in_patient_id
group by d.patient_id
having min(hiv.visit_date) is not null
   and StartARTDate is not null;
END
$$
