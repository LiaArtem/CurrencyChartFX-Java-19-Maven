# CurrencyChartFX-Java-19-Maven

Maven JavaFX IntelliJ IDEA project - Java 19, JavaFX, Maven, JasperReports, JDBC (Oracle, MS SQL, Azure SQL, PostgreSQL, MySQL, IBM DB2, SQLite).
Construction of charts of currencies of NBU on years for watching of tendencies of change.

Первичная настройка:
---------------------------------------------------------------------------------
- скачать и установить IntelliJ IDEA Community
- скачать и установить Git
- скачать и установить jdk-19_windows-x64_bin.exe (19.0.0)
- скачать и установить SceneBuilder-18.0.0
- скачать и установить TIB_js-studiocomm_6.20.0_windows_x86_64.exe + запустить и закрыть.
- настроить Github в IntelliJ IDEA Community (Settings - Version Control - Github)

Разворачивание - настройка:
---------------------------------------------------------------------------------
- Скачать и распаковать javafx (19.0.0) в папку проекта путь: ./javafx-sdk/

Настройка JavaFX:
---------------------------------------------------------------------------------
- https://www.jetbrains.com/help/idea/javafx.html#check-plugin
- IntelliJ IDEA -> File -> Settings -> Languages and Frameworks -> JavaFX -> Указать путь к SceneBuilder (C:\Users\Admin\AppData\Local\SceneBuilder\SceneBuilder.exe)
- Папка при смене версии JavaFX не менять = \javafx-sdk\

Настройка отчетов:
---------------------------------------------------------------------------------
- TIB_js-studiocomm_6.20.0_windows_x86_64.exe, запустить TIBCO Jaspersoft Studio
- распаковать JaspersoftWorkspace.7z в C:\Users\Admin\JaspersoftWorkspace
- изменить настройки Datasource если необходимо
  - !!! При разработке отчетов в Jaspersoft® Studio для MSSQL 2019 возникает ошибка
    java.lang.UnsatisfiedLinkError: Native Library .\mssql-jdbc_auth-X.X.X.x64.dll already loaded in another classloader) методы лечения в интернете не подошли
    При выполнении в java не появляется, видимо проблема Jaspersoft® Studio со встроенной работой с jre11

---------------------------------------------------------------------------------
Настройка баз данных (+ JDBC Driver):
---------------------------------------------------------------------------------
- Oracle XE
  - после установки меняем в глоб. реестре:
    - Компьютер\HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraDB21Home1 c AMERICAN_AMERICA.WE8MSWIN1252
      на NLS_LANG = AMERICAN_AMERICA.AL32UTF8 (либо AMERICAN_AMERICA.CL8MSWIN1251)

  - Oracle SQL Developer выполяем скрипты из папки .\sql\oracle\
    - под пользователем SYS (1_CREATE_DATABASE_AND_USER.sql)
    - остальные под пользователем TEST_USER
    - !!! Перед загрузкой скриптов нужно настроить обязательно (экспорт таблиц выполнен в UTF-8).
    - !!! Настраиваем кодировку с среде Oracle SQL Developer - Tools -> Preferences -> Environment -> Encoding (меняем на UTF-8).

---------------------------------------------------------------------------------
- MS SQL
  - Microsoft SQL Server Management Studio выполяем скрипты из папки .\sql\mssql\
    !!!! *.sql в меню (Query -> SQLCMD Mode)

  Для работы jdbc:
  - You need to Go to Start > Microsoft SQL Server > Configuration Tools > SQL Server Configuration Manager
  - SQL Server Configuration Manager > SQL Server Network Configuration > Protocols for MSSQLSERVER
    - Где вы найдете протокол TCP/IP, если он отключен, затем Включите его.
    - Нажмите на TCP/IP, вы найдете его свойства.
    - !!! Вкладка Protocol - Enabled - Yes
    - !!! Вкладка IP Addresses - IPXX - Enabled - Yes (там где IP address - 127.0.0.1)
    - !!! Вкладка IP Addresses - IPXX  - Enabled - Yes (там где IP address - ::1)
    - В этих свойствах Удалите все динамические порты TCP и добавьте значение 1433 во все TCP-порт (если они есть, по умолчанию не было)
    - Перезапустите службы SQL Server > SQL Server

  Включаем Windows Aunthentication:
  - скачать или взять с mssql-jdbc_auth-X.X.X.x64.dll (Download Microsoft JDBC Driver for SQL Server - (sqljdbc_X.X.X.X_rus.zip))
  - Файл mssql-jdbc_auth-X.X.X.x64.dll скопировать в windows\system32 для подключения в java
  - В файле - DBConnSettings.json значение Security_mode_WA = true

  Включаем SQL Server Aunthentication:
  - Запускаем Microsoft SQL Server Management Studio
  - Имя сервера (SQL Server .....) -> правой клавишей Properties -> Security -> SQL Server and Windows Aunthentication mode включаем
  - Перезапускаем сервер
  - Включаем пользователя sa (Security -> Logins -> Properties)
    - General - Password (устанавливаем пароль = 12345678)
    - General (Default database - TestDB)
    - Status (Login - Enabled)
  - В файле - DBConnSettings.json значение Security_mode_WA = true

---------------------------------------------------------------------------------
- Azure SQL
  - скачать Download Microsoft JDBC Driver for SQL Server - (sqljdbc_X.X.X.X_rus.zip).
  - Файл mssql-jdbc_auth-X.X.X.x64.dll скопировать в windows\system32 для подключения в java

  - Microsoft SQL Server Management Studio 18 выполяем скрипты из папки .\sql\azuredb\
    !!!! *.sql в меню (Query -> SQLCMD Mode)

---------------------------------------------------------------------------------
- PostgreSQL
  - DBeaver выполяем скрипты из папки .\sql\postgeesql\ (при подключении вкладка PostgreSQL отображать все базы данных)
    - открыть SQL скрипт -> Выполнить SQL скрипт (Alt+X) (Файлы со скриптами в UTF8 - база в cp1251, при открытии могут быть иероглифы,
      тогда просто скопировать текст и вставить в окно SQL скрипта и выполнить)

---------------------------------------------------------------------------------
- MySQL
  - MySQL Workbench выполяем скрипты из папки .\sql\mysql\

---------------------------------------------------------------------------------
- SQLite
  - ничего, создание таблиц, представлений и процедур автоматизировано в коде программы

---------------------------------------------------------------------------------
- IBM DB2
  - устанавливаем DB2 Community Edition (логин: db2admin, пароль: 12345678.
  - создаем базу данных: SAMPLE (Create sample database)
  - выдаем админ. права пользователю db2admin:
    - запускаем из Пуск -> Командное окно DB2 - Администратор
    -> db2 connect to SAMPLE
    -> db2 grant DBADM on DATABASE to user db2admin
    -> db2 terminate
  - запускаем DBeaver
  - подключаемся к серверу:
    - тип - DB2 LUW
    - сервер - localhost:25000
    - база данных - SAMPLE
    - пользователь - db2admin
    - пароль - 12345678
  - выполняем скрипты .\sql\IBM DB2\

---------------------------------------------------------------------------------
Сборка:
---------------------------------------------------------------------------------
- Build - Build Artifacts.. - Build

Fix:
Exception in thread "main" java.lang.SecurityException: Invalid signature file digest for Manifest main attributes
- IntelliJ IDEA -> File -> Project Structure -> Add New (Artifacts) -> jar -> From Modules With Dependencies on the Create Jar From Module Window:
- Select you main class
- JAR File from Libraries Select copy to the output directory and link via manifest

!!! Для JAR удалять Artifacts и создавать заново при добавлении новых в maven (IntelliJ IDEA -> File -> Project Structure -> Add New (Artifacts) -> jar).

Fix:
Убрать ?????? в консоли если кодировка windows (cp1251) - IntelliJ IDEA -> File -> Settings -> Editor -> FileEncodings -> Global Encoding -> windows-1251

---------------------------------------------------------------------------------
Загрузка первичных курсов
---------------------------------------------------------------------------------
- https://bank.gov.ua/control/uk/curmetal/currency/search/form/period
- Указать период и экспорт JSON
