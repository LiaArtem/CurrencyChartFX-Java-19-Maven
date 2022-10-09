CREATE TABLE CURS (
		  ID INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
		  CURS_DATE DATE NOT NULL,
		  CURR_CODE VARCHAR(3) NOT NULL,
		  RATE DECIMAL(22,6),
		  CONSTRAINT PK_CURS PRIMARY KEY (ID)
          );

CREATE UNIQUE INDEX UK_CURS ON CURS (CURS_DATE, CURR_CODE);

CREATE VIEW CURS_AVG_YEAR (PART_DATE, CURR_CODE, AVG_RATE)
AS
  SELECT kk.PART_DATE,
         kk.CURR_CODE,
         AVG(kk.RATE) AS AVG_RATE
  FROM (
	   SELECT
	         EXTRACT(YEAR FROM k.CURS_DATE) AS PART_DATE,
	         k.CURR_CODE,
	         k.RATE
	   FROM CURS k
       ) kk
  GROUP BY
	   kk.PART_DATE,
	   kk.CURR_CODE;

CREATE VIEW CURS_AVG (PART_DATE, CURR_CODE, AVG_RATE)
AS
	SELECT
	    f.PART_DATE AS PART_DATE,
	    f.CURR_CODE AS CURR_CODE,
	    AVG(f.AVG_RATE) AS AVG_RATE
	FROM
	    (
	    SELECT
	        SUBSTRING(100 + EXTRACT(MONTH FROM k.CURS_DATE) FROM 2 FOR 2)||'-'||SUBSTRING(100 + EXTRACT(DAY FROM k.CURS_DATE) FROM 2 FOR 2) AS PART_DATE,
	        k.CURR_CODE,
	        (k.RATE / a.AVG_RATE)* 100 AS AVG_RATE
	    FROM CURS k
	    INNER JOIN CURS_AVG_YEAR a ON a.PART_DATE = EXTRACT(YEAR FROM k.CURS_DATE) AND a.CURR_CODE = k.CURR_CODE
	    ) f
	GROUP BY
	    f.PART_DATE,
	    f.CURR_CODE;

CREATE VIEW CURS_REPORT (CURS_DATE, CURR_CODE, RATE, AVG_RATE)
AS
	SELECT
	    k.CURS_DATE,
	    k.CURR_CODE,
	    k.RATE,
	    a.AVG_RATE AS AVG_RATE
	FROM CURS k
	INNER JOIN CURS_AVG a ON a.PART_DATE = SUBSTRING(100 + EXTRACT(MONTH FROM k.CURS_DATE) FROM 2 FOR 2)||'-'||SUBSTRING(100 + EXTRACT(DAY FROM k.CURS_DATE) FROM 2 FOR 2) AND a.CURR_CODE = k.CURR_CODE
	WHERE EXTRACT(YEAR FROM k.CURS_DATE) IN ( SELECT EXTRACT(YEAR FROM MAX(kk.CURS_DATE)) FROM CURS kk) AND a.AVG_RATE <= 100;
