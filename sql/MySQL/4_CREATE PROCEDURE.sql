USE `test_schemas`;
DROP procedure IF EXISTS `INSERT_CURS`;

DELIMITER $$
USE `test_schemas`$$
CREATE PROCEDURE `INSERT_CURS` (p_curs_date datetime, p_curr_code varchar(3), p_rate decimal(38,8))
BEGIN
   INSERT INTO CURS (CURS_DATE, CURR_CODE, RATE)
       SELECT p_curs_date, p_curr_code, p_rate
       FROM DUAL
       WHERE NOT EXISTS (SELECT 1 FROM CURS c where c.curs_date = p_curs_date and c.curr_code = p_curr_code);
END$$

DELIMITER ;