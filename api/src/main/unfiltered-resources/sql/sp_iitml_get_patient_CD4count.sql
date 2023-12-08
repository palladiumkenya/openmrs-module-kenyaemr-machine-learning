DROP PROCEDURE IF EXISTS sp_iitml_get_patient_CD4count;
$$
CREATE PROCEDURE `sp_iitml_get_patient_CD4count`(
	IN in_patient_id INT
)
BEGIN
select
	   d.patient_id                                                                            as PatientPK,
       mid(max(concat(l.visit_date, l.test_result)), 11)                                       as lastcd4
from kenyaemr_etl.etl_patient_hiv_followup fup
         join kenyaemr_etl.etl_patient_demographics d on d.patient_id = fup.patient_id
         join (select e.patient_id,
                      e.uuid,
                      date_add(date_add(min(e.visit_date), interval 3 month), interval 1 day)  as enrollment_date,
                      date_add(date_add(min(e.visit_date), interval 6 month), interval 1 day)  as six_month_date,
                      date_add(date_add(min(e.visit_date), interval 12 month), interval 1 day) as twelve_month_date,
                      mid(min(concat(e.visit_date, e.who_stage)), 11)                          as ewho,
                      left(min(concat(date(e.visit_date), e.who_stage)), 10)                   as ewho_date,
                      e.date_created,
                      e.date_last_modified
               from kenyaemr_etl.etl_hiv_enrollment e
               group by e.patient_id) p_dates on p_dates.patient_id = fup.patient_id
         left outer join kenyaemr_etl.etl_laboratory_extract l
                         on l.patient_id = fup.patient_id and l.lab_test in (5497, 730)
where d.unique_patient_no is not null and
	d.patient_id = in_patient_id
group by fup.patient_id;
END
$$
