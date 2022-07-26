# CurrencyChartFX-Java-18-Maven

Maven JavaFX IntelliJ IDEA project - Construction of charts of currencies of NBU on years for watching of tendencies of change.
- Java 18, JavaFX, Maven, JasperReports, JDBC (Azure SQL, MS SQL 2019, Oracle XE 21c, PostgreSQL 14, SQLite, MySQL).

Первичная настройка:
---------------------------------------------------------------------------------
- скачать и установить IntelliJ IDEA Community
- скачать и установить Git
- скачать и установить jdk-18_windows-x64_bin.exe (18.0.2)
- скачать и установить SceneBuilder-18.0.0
- скачать и установить TIB_js-studiocomm_6.19.1_windows_x86_64.exe + запустить и закрыть.
- скачать и настроить Maven
- настроить Github в IntelliJ IDEA Community (Settings - Version Control - Github)

Разворачивание - настройка:
---------------------------------------------------------------------------------
- Скачать и распаковать javafx (18.0.2) в папку проекта путь: ./javafx-sdk/

Настройка JavaFX:
---------------------------------------------------------------------------------
- https://www.jetbrains.com/help/idea/javafx.html#check-plugin
- IntelliJ IDEA -> File -> Settings -> Languages and Frameworks -> JavaFX -> Указать путь к SceneBuilder (C:\Users\Admin\AppData\Local\SceneBuilder\SceneBuilder.exe)
- Папка при смене версии JavaFX не менять = \javafx-sdk\

Настройка отчетов:
---------------------------------------------------------------------------------
- TIB_js-studiocomm_6.19.1_windows_x86_64.exe, запустить TIBCO Jaspersoft Studio-6.19.1
- распаковать JaspersoftWorkspace.7z в C:\Users\Admin\JaspersoftWorkspace
- изменить настройки Datasource если необходимо
  - !!! При разработке отчетов в Jaspersoft® Studio 6.19.1 для MSSQL 2019 возникает ошибка
    java.lang.UnsatisfiedLinkError: Native Library .\mssql-jdbc_auth-X.X.X.x64.dll already loaded in another classloader) методы лечения в интернете не подошли
    При выполнении в java не появляется, видимо проблема Jaspersoft® Studio 6.18.1

---------------------------------------------------------------------------------
Настройка баз данных (+ JDBC Driver):
---------------------------------------------------------------------------------
- Oracle XE 21с
  - после установки меняем в глоб. реестре:
    - Компьютер\HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE\KEY_OraDB21Home1 c AMERICAN_AMERICA.WE8MSWIN1252
      на NLS_LANG = AMERICAN_AMERICA.AL32UTF8 (либо AMERICAN_AMERICA.CL8MSWIN1251)

  - Oracle SQL Developer выполяем скрипты из папки .\sql\oracle\
    - под пользователем SYS (1_CREATE_DATABASE_AND_USER.sql)
    - остальные под пользователем TEST_USER
    - !!! Перед загрузкой скриптов нужно настроить обязательно (экспорт таблиц выполнен в UTF-8).
    - !!! Настраиваем кодировку с среде Oracle SQL Developer - Tools -> Preferences -> Environment -> Encoding (меняем на UTF-8).

---------------------------------------------------------------------------------
- MS SQL 2019
  - скачать Download Microsoft JDBC Driver for SQL Server - (sqljdbc_X.X.X.X_rus.zip).
  - Файл mssql-jdbc_auth-X.X.X.x64.dll скопировать в windows\system32 для подключения в java
  Для работы jdbc:
  - You need to Go to Start > Microsoft SQL Server > Configuration Tools > SQL Server Configuration Manager
  - SQL Server Configuration Manager > SQL Server Network Configuration > Protocols for MSSQLSERVER
    - Где вы найдете протокол TCP/IP, если он отключен, затем Включите его.
    - Нажмите на TCP/IP, вы найдете его свойства.
    - !!! Вкладка Protocol - Enabled - Yes
    - !!! Вкладка IP Addresses - IP10 - Enabled - Yes (там где IP address - 127.0.0.1)
    - !!! Вкладка IP Addresses - IP9  - Enabled - Yes (там где IP address - ::1)
    - В этих свойствах Удалите все динамические порты TCP и добавьте значение 1433 во все TCP-порт (если они есть, по умолчанию не было)
    - Перезапустите службы SQL Server > SQL Server

  - Microsoft SQL Server Management Studio 18 выполяем скрипты из папки .\sql\mssql\
    !!!! 1_CREATE_DATABASE.sql в меню (Query -> SQLCMD Mode)

---------------------------------------------------------------------------------
- Azure SQL
  - скачать Download Microsoft JDBC Driver for SQL Server - (sqljdbc_X.X.X.X_rus.zip).
  - Файл mssql-jdbc_auth-X.X.X.x64.dll скопировать в windows\system32 для подключения в java

  - Microsoft SQL Server Management Studio 18 выполяем скрипты из папки .\sql\azuredb\
    !!!! 1_CREATE_DATABASE.sql в меню (Query -> SQLCMD Mode)

---------------------------------------------------------------------------------
- PostgreSQL 14
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
