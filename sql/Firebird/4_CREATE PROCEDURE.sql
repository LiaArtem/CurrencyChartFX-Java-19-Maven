CREATE PROCEDURE INSERT_KURS ( P_KURS_DATE VARCHAR(255), P_CURRENCY_CODE VARCHAR(3), P_RATE DECIMAL(22, 6))
AS
BEGIN
	INSERT INTO CURS (CURS_DATE, CURR_CODE, RATE)
	     SELECT :P_KURS_DATE,
                :P_CURRENCY_CODE,
                :P_RATE
         FROM rdb$database
        WHERE NOT EXISTS (SELECT 1 FROM CURS c where c.CURS_DATE = :P_KURS_DATE and c.CURR_CODE = :P_CURRENCY_CODE);
END;