DROP PROCEDURE IF EXISTS sp_iitml_get_last_dwapi_etl_update;
$$
CREATE PROCEDURE `sp_iitml_get_last_dwapi_etl_update`()
BEGIN
SELECT script_name                      as 'INDICATOR_NAME',
       stop_time                        as 'INDICATOR_VALUE',
       DATE_FORMAT(stop_time, '%Y%b%d') as 'INDICATOR_MONTH'
FROM kenyaemr_etl.etl_script_status s
where s.error is null
  and script_name = 'population_of_dwapi_tables'
order by INDICATOR_VALUE desc
limit 1;
END
$$
