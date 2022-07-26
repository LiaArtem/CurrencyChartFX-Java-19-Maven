USE [DatabaseTestDB];

/* --------------------------------------------------- */
/* [dbo].[SEQ_KURS] */
/* --------------------------------------------------- */
GO
PRINT N'Идет создание Последовательность [dbo].[SEQ_KURS]…';
GO
CREATE SEQUENCE [dbo].[SEQ_KURS]
    AS BIGINT
    START WITH 1
    INCREMENT BY 1
    CACHE 10;

/* --------------------------------------------------- */
/* [dbo].[CURRENCY] */
/* --------------------------------------------------- */
GO
PRINT N'Идет создание Таблица [dbo].[CURRENCY]…';
GO
CREATE TABLE [dbo].[CURRENCY] (
    [Id]         INT            IDENTITY (1, 1) NOT NULL,
    [CODE]       NVARCHAR (3)   NOT NULL,
    [NAME]       NVARCHAR (255) NOT NULL,
    [SHORT_NAME] NVARCHAR (3)   NOT NULL,
    CONSTRAINT [PK_CURRENCY] PRIMARY KEY CLUSTERED ([Id] ASC)
);

GO
PRINT N'Идет создание Индекс [dbo].[CURRENCY].[UK_CURRENCY_CODE]…';
GO
CREATE UNIQUE NONCLUSTERED INDEX [UK_CURRENCY_CODE]
    ON [dbo].[CURRENCY]([CODE] ASC);

/* --------------------------------------------------- */
/* [dbo].[KURS] */
/* --------------------------------------------------- */

GO
PRINT N'Идет создание Таблица [dbo].[KURS]…';
GO
CREATE TABLE [dbo].[KURS] (
    [Id]            INT           NOT NULL,
    [CURRENCY_CODE] NVARCHAR (3)  NOT NULL,
    [KURS_DATE]     DATE          NOT NULL,
    [RATE]          MONEY         NOT NULL,
    [FORC]          INT           NOT NULL,
    [SYS_DATE]      DATETIME2 (7) NOT NULL,
    PRIMARY KEY CLUSTERED ([Id] ASC)
);

GO
PRINT N'Идет создание Индекс [dbo].[KURS].[I_KURS_CURR_CODE_AND_DATE]…';
GO
CREATE NONCLUSTERED INDEX [I_KURS_CURR_CODE_AND_DATE]
    ON [dbo].[KURS]([CURRENCY_CODE] ASC, [KURS_DATE] ASC);

GO
PRINT N'Идет создание Ограничение по умолчанию ограничение без названия для [dbo].[KURS]…';
GO
ALTER TABLE [dbo].[KURS]
    ADD DEFAULT SYSDATETIME() FOR [SYS_DATE];

GO
PRINT N'Идет создание Ограничение по умолчанию ограничение без названия для [dbo].[KURS]…';
GO
ALTER TABLE [dbo].[KURS]
    ADD DEFAULT (NEXT VALUE FOR dbo.SEQ_KURS) FOR [Id];

GO
PRINT N'Идет создание Внешний ключ [dbo].[FK_KURS_CURRENCY_CODE]…';
GO
ALTER TABLE [dbo].[KURS]
    ADD CONSTRAINT [FK_KURS_CURRENCY_CODE] FOREIGN KEY ([CURRENCY_CODE]) REFERENCES [dbo].[CURRENCY] ([CODE]);

GO
PRINT N'Идет создание Проверочное ограничение [dbo].[CK_KURS_FORC]…';
GO
ALTER TABLE [dbo].[KURS]
    ADD CONSTRAINT [CK_KURS_FORC] CHECK ([FORC]>(0));

GO
PRINT N'Идет создание Проверочное ограничение [dbo].[CK_KURS_RATE]…';
GO
ALTER TABLE [dbo].[KURS]
    ADD CONSTRAINT [CK_KURS_RATE] CHECK ([RATE]>(0));

/* --------------------------------------------------- */
/* [dbo].[VIEW_KURS] */
/* --------------------------------------------------- */
GO
PRINT N'Идет создание Представление [dbo].[VIEW_KURS]…';
GO
CREATE VIEW [dbo].[VIEW_KURS]
AS 
	SELECT k.CURRENCY_CODE,
	       c.SHORT_NAME as CURRENCY_SNAME,
		   c.NAME as CURRENCY_NAME,
		   k.KURS_DATE,
		   k.RATE,
		   k.FORC
	FROM [KURS] k
	INNER JOIN [CURRENCY] AS c ON c.CODE = k.CURRENCY_CODE

/* --------------------------------------------------- */
/* [dbo].[VIEW_KURS_REPORT] */
/* --------------------------------------------------- */
GO
PRINT N'Идет создание Представление [dbo].[VIEW_KURS_REPORT]…';
GO
CREATE VIEW [dbo].[VIEW_KURS_REPORT]
	AS
WITH KURS_AVG_YEAR (PART_YEAR, CURRENCY_CODE, AVG_RATE) AS
(
  SELECT CONVERT(nvarchar(4), DATEPART(year,k.KURS_DATE)) AS PART_YEAR,
         k.CURRENCY_CODE,
         AVG(k.RATE/k.FORC) AS AVG_RATE
    FROM KURS k
GROUP BY CONVERT(nvarchar(4), DATEPART(year,k.KURS_DATE)), k.CURRENCY_CODE

),
KURS_AVG (PART_MONTH_DATE, CURRENCY_CODE, AVG_RATE) AS
                (
                SELECT f.PART_MONTH_DATE,
					   f.CURRENCY_CODE,
					   AVG(f.AVG_RATE) as AVG_RATE
				FROM (SELECT SUBSTRING(CONVERT(nvarchar(10), k.KURS_DATE, 23), 6, 5) as PART_MONTH_DATE,
							 k.CURRENCY_CODE,
							 (k.RATE/a.AVG_RATE)*100 as AVG_RATE
					  FROM KURS k
					  INNER JOIN KURS_AVG_YEAR a ON a.PART_YEAR = CONVERT(nvarchar(4), DATEPART(year,k.KURS_DATE)) AND a.CURRENCY_CODE = k.CURRENCY_CODE
                      ) f
				GROUP BY f.PART_MONTH_DATE, f.CURRENCY_CODE
),
KURS_AVG_YEAR_MAX (PART_YEAR) AS
(
  SELECT CONVERT(nvarchar(4), MAX(DATEPART(year,kk.KURS_DATE))) AS PART_YEAR
  FROM KURS kk
)
 SELECT k.KURS_DATE,
		k.CURRENCY_CODE,
		k.RATE,
		a.AVG_RATE as AVG_RATE
FROM KURS k
INNER JOIN KURS_AVG a ON a.CURRENCY_CODE = k.CURRENCY_CODE AND a.PART_MONTH_DATE = SUBSTRING(CONVERT(nvarchar(10), k.KURS_DATE, 23), 6, 5) AND a.AVG_RATE <= 100
INNER JOIN KURS_AVG_YEAR_MAX y ON y.PART_YEAR = CONVERT(nvarchar(4), DATEPART(year,k.KURS_DATE))
WHERE k.CURRENCY_CODE = '840'

GO
/* --------------------------------------------------- */
/* Шаблон скрипта после развертывания */
/* --------------------------------------------------- */
--Run scripts 
BEGIN 
    PRINT 'Добавление кодов валют' 
    begin transaction;
    
    PRINT 'UAH-Гривня'    
    INSERT INTO [dbo].[CURRENCY] ([CODE] ,[NAME] ,[SHORT_NAME]) 
    SELECT '980','Гривня','UAH'
	WHERE NOT EXISTS (SELECT 1 FROM [dbo].[CURRENCY] WHERE CODE = '980');

    PRINT 'USD-Долар США'    
    INSERT INTO [dbo].[CURRENCY] ([CODE] ,[NAME] ,[SHORT_NAME]) 
    SELECT '840','Долар США','USD'
	WHERE NOT EXISTS (SELECT 1 FROM [dbo].[CURRENCY] WHERE CODE = '840');

    PRINT 'EUR-Євро'    
    INSERT INTO [dbo].[CURRENCY] ([CODE] ,[NAME] ,[SHORT_NAME]) 
    SELECT '978','Євро','EUR'
	WHERE NOT EXISTS (SELECT 1 FROM [dbo].[CURRENCY] WHERE CODE = '978');

    PRINT 'GBP-Фунт стерлінгів'    
    INSERT INTO [dbo].[CURRENCY] ([CODE] ,[NAME] ,[SHORT_NAME]) 
    SELECT '826','Фунт стерлінгів','GBP'
	WHERE NOT EXISTS (SELECT 1 FROM [dbo].[CURRENCY] WHERE CODE = '826');

    commit transaction;
END
GO