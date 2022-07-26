package com.example.currencychartfxmaven;

import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class curr_chart_Controller {

    private final ObservableList<String> curr_code_List = FXCollections.observableArrayList("USD-Доллар США", "EUR-Евро", "GBP-Фунт");
    private final ObservableList<String> month_com_List = FXCollections.observableArrayList("01-Январь", "02-Февраль", "03-Март", "04-Апрель",
                                                                                "05-Май", "06-Июнь", "07-Июль", "08-Август",
                                                                                "09-Сентябрь", "10-Октябрь", "11-Ноябрь", "12-Декабрь");
    private final ObservableList<String> day_com_List = FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
                                                                                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                                                                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                                                                    "31");
    private final ObservableList<String> source_code_List = FXCollections.observableArrayList("Из файла JSON", "С сайта НБУ");

    private final ObservableList<String> report_code_List = FXCollections.observableArrayList("Отчет - Без базы данных", "Отчет - база данных SQLite", "Отчет - база данных MySQL",
                                                                                            "Отчет - база данных PostgreSQL", "Отчет - база данных Oracle", "Отчет - база данных MSSQL",
                                                                                             "Отчет - база данных AzureSQL");
    private static final String tec_kat = new File("").getAbsolutePath();
    private static final String tec_kat_curs = tec_kat + File.separator + "temp";

    @FXML private ComboBox<String> curr_code;
    @FXML private ComboBox<String> year_com;
    @FXML private TextField minus_year;
    @FXML private ComboBox<String> month_com;
    @FXML private TextField minus_month;
    @FXML private TextField plus_month;
    @FXML private CheckBox check_month;
    @FXML private ComboBox<String> day_com;
    @FXML private TextField minus_day;
    @FXML private TextField plus_day;
    @FXML private CheckBox check_day;
    @FXML private CheckBox average_value_curr;
    @FXML private CheckBox visible_points;
    //@FXML private Button Calc_button;
    @FXML private Button button_curs_nbu;
    @FXML private LineChart<String, Number> chart;
    //@FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> source_code;
    @FXML private CheckBox del_cache_file;
    @FXML private CheckBox not_create_cache_file;
    @FXML private ComboBox<String> report_code;

    @FXML
    private void initialize() throws TransformerException {
        // в интерфейсе
        // по умолчанию - валюта
        curr_code.setItems(curr_code_List);
        curr_code.getSelectionModel().select(0); // первое значение

        // по умолчанию - год
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year
        for (int i = 1; i <= 5; i++ ) {
            year_com.getItems().add(Integer.toString(year--));
        }
        year_com.getSelectionModel().select(0); // первое значение
        minus_year.setText("3");

        // по умолчанию - месяц
        month_com.setItems(month_com_List);
        int month = now.get(Calendar.MONTH);
        month_com.getSelectionModel().select(month);
        minus_month.setText("1");
        plus_month.setText("1");
        check_month.setSelected(false);

        // по умолчанию - день
        day_com.setItems(day_com_List);
        int day = now.get(Calendar.DAY_OF_MONTH);
        day_com.getSelectionModel().select(day - 1);
        minus_day.setText("15");
        plus_day.setText("15");
        check_day.setSelected(true);

        // по умолчанию - источник
        source_code.setItems(source_code_List);
        source_code.getSelectionModel().select(0); // первое значение
        del_cache_file.setDisable(true);
        del_cache_file.setSelected(false);
        not_create_cache_file.setDisable(true);
        not_create_cache_file.setSelected(false);

        // по умолчанию - отчет
        report_code.setItems(report_code_List);
        report_code.getSelectionModel().select(0); // первое значение

        // Создание базы данных SQLite и создание таблицы для добавления данных
        CreateTableSQLite();

        // Заполенение и прорисовка графика
        Calc_range();
    }

    // кнопка - Обновить график
    @FXML
    private void Calc_buttonActionPerformed() throws TransformerException {
        // Обновить график
        // расчет диапазонов и вывод данных
        Calc_range();
    }

    // кнопка - Ссылка на курсы НБУ
    @FXML
    private void button_curs_nbuActionPerformed() {
        // Ссылка на курсы НБУ
        try {
            Desktop d = Desktop.getDesktop();
            d.browse(new URI("https://bank.gov.ua/control/uk/curmetal/currency/search/form/period"));
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    // поле со списком - источник данных
    @FXML
    private void source_codeAction() {
        // блокируем кнопку если из сайта
        button_curs_nbu.setDisable(false);
        del_cache_file.setDisable(true);
        not_create_cache_file.setDisable(true);
        if (source_code.getSelectionModel().getSelectedIndex() == 1) {
            button_curs_nbu.setDisable(true);
            del_cache_file.setDisable(false);
            not_create_cache_file.setDisable(false);
        }
    }

    // кнопка - Report
    @FXML
    private void Report_buttonActionPerformed() throws JRException, IOException {
        switch (report_code.getSelectionModel().getSelectedIndex()) {
            case 1 -> Report_SQLite();
            case 2 -> Report_MySQL();
            case 3 -> Report_PostgreSQL();
            case 4 -> Report_Oracle();
            case 5 -> Report_MSSQL();
            case 6 -> Report_AzureSQL();
            default -> Report_No_DB();
        }
    }

    // Report - No DB
    private void Report_No_DB() throws JRException, IOException {
        // Генерация отчета
        String file_name = "StyledTextReport";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        // DataSource - без базы данных. Используется пустой источник данных.
        JRDataSource dataSource = new JREmptyDataSource();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - DB SQLite
    private void Report_SQLite() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReport";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionSQLite();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionSQLite");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - DB MySQL
    private void Report_MySQL() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReportMySQL";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionMySQL();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionMySQL");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - PostgreSQL
    private void Report_PostgreSQL() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReportPostgreSQL";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionPostgreSQL();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionPostgreSQL");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - DB Oracle
    private void Report_Oracle() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReportOracle";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionOracle();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionOracle");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - DB MSSQL
    private void Report_MSSQL() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReportMSSQL";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionMSSQL();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionMSSQL");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Report - DB AzureSQL
    private void Report_AzureSQL() throws JRException, IOException {
        // Генерация отчета
        String file_name = "CurrencyChartFXMavenReportAzureSQL";
        String mPath_sample = tec_kat + File.separator + "report_sample";
        String mPath_export = tec_kat + File.separator + "report_export";

        // Компиляция jrxml файла
        JasperReport jasperReport = JasperCompileManager
                .compileReport(mPath_sample + File.separator + file_name + ".jrxml");

        // Параметры для отчета
        Map<String, Object> parameters = new HashMap<>();

        Connection connection = ConnectionAzureSQL();
        if (connection == null) {
            Main.MessageBoxError("Ошибка подключения к DB", "Ошибка ConnectionAzureSQL");
            return;
        }

        // DataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

        // Проверка папки для экспорта
        boolean mkdirs_result = new File(mPath_export).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath_export + " Каталог уже существует");
        }

        // Экспорт в PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint,
                mPath_export + File.separator + file_name + ".pdf");

        // Отобразить файл на экране
        File file = new File(mPath_export + File.separator + file_name + ".pdf");
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file);
        }
        else {
            Main.MessageBoxError("Открытие файла pdf не поддерживается", "Ошибка");
        }
    }

    // Расчет значений графика и его прорисовка
    private void Calc_range() throws TransformerException {
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year        
        int year_now = year;
        // расчитываем диапазон
        int mYear = (int) Main.getString_Double(minus_year.getText()) + 1;
        int m_is_average_value_curr;
        int m_is_visible_points;

        // год
        LocalDate[] mDate1 = new LocalDate [mYear];
        LocalDate[] mDate2 = new LocalDate [mYear];

        year = year_now - mYear + 1;
        for (int i = 0; i <= mYear - 1; i++) {

            // день
            if (check_day.isSelected()) {

                int mMonthS = month_com.getSelectionModel().getSelectedIndex() + 1;
                int mDay = (int) Main.getString_Double(plus_day.getText()) + (int) Main.getString_Double(minus_day.getText()) + 1;
                int mDayS = day_com.getSelectionModel().getSelectedIndex();

                // стартовая дата        
                mDate1[i] = LocalDate.parse ("01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                // выходим на день
                if (mDayS > 0) {
                    mDate1[i] = mDate1[i].plusDays(mDayS);
                }
                // минус дней
                if ((int) Main.getString_Double(minus_day.getText()) > 0) {
                    mDate1[i] = mDate1[i].minusDays((int) Main.getString_Double(minus_day.getText()));
                }
                // плюс необходимое кол-во дней
                mDate2[i] = mDate1[i].plusDays(mDay);
            }
            // месяц
            else if (check_month.isSelected()) {

                int mMonth = (int) Main.getString_Double(plus_month.getText()) + (int) Main.getString_Double(minus_month.getText()) + 1;
                int mMonthS = month_com.getSelectionModel().getSelectedIndex() - (int) Main.getString_Double(minus_month.getText()) + 1;
                if (mMonthS < 0) {
                    mMonthS = 12 + mMonthS;
                    if (i == 0) { year = year - 1; }
                }

                mDate1[i] = LocalDate.parse ( "01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                // плюс необходимо кол-во месяцев
                mDate2[i] = mDate1[i].plusMonths(mMonth);
                // минус 1 день
                mDate2[i] = mDate2[i].minusDays(1);
            }
            // год
            else {
                mDate1[i] = LocalDate.parse ( "01.01." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
                mDate2[i] = LocalDate.parse ( "31.12." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
            }

            year++;
        }

        // добавляем в панель - график
        String mCurrCode = curr_code.getSelectionModel().getSelectedItem().substring(0, 3);
        String[][] mArray = null;
        int source_index = source_code.getSelectionModel().getSelectedIndex();

        if (source_index == 0) {
            // проверка на существования графика в json
            String mPath = tec_kat_curs + File.separator + mCurrCode;
            if (!new File(mPath).mkdirs()) {
                // ищем файлы с расширением json
                File dir = new File(mPath);
                File[] matchingFiles = dir.listFiles((dir1, name) -> name.endsWith("json"));
                assert matchingFiles != null;
                if (matchingFiles.length == 0) {
                    source_code.getSelectionModel().select(1); // второе значение
                    Main.MessageBoxWarn(mPath, "Переключено на получение данных с сайта НБУ.\nНе найден файл *.json в каталоге - загрузить с https://bank.gov.ua/control/uk/curmetal/currency/search/form/period");
                }
            } else {
                source_code.getSelectionModel().select(1); // второе значение
                Main.MessageBoxWarn(mPath, "Переключено на получение данных с сайта НБУ.\nНе найден файл *.json в каталоге - загрузить с https://bank.gov.ua/control/uk/curmetal/currency/search/form/period");
            }
        }

        source_index = source_code.getSelectionModel().getSelectedIndex();
        if (source_index == 0) {
            mArray = getCursNbu(mCurrCode, mDate1, mDate2);
        } else if (source_index == 1) {
            mArray = getCursNbu_WEB(mCurrCode, mDate1, mDate2);
        }

        // считать среднее значение
        m_is_average_value_curr = 0;
        if (average_value_curr.isSelected()) { m_is_average_value_curr = 1; }

        // показывать значения в точках
        m_is_visible_points = 0;
        if (visible_points.isSelected()) { m_is_visible_points = 1; }

        // рисуем график
        LineChartGraphic(mArray, m_is_average_value_curr, m_is_visible_points);

        // Добавление данных в базу SQLite
        assert mArray != null;
        AddTableSQLite(mCurrCode, mArray);

        // Добавление данных в базу DB MySQL
        AddTableMySQL(mCurrCode, mArray);

        // Добавление данных в базу DB PostgreSQL
        AddTablePostgreSQL(mCurrCode, mArray);

        // Добавление данных в базу DB Oracle
        AddTableOracle(mCurrCode, mArray);

        // Добавление данных в базу DB MSSQL
        AddTableMSSQL(mCurrCode, mArray);

        // Добавление данных в базу DB AzureSQL
        AddTableAzureSQL(mCurrCode, mArray);

    }

    // Получить курс НБУ (С сайта JSON)
    private String [][] getCursNbu(String mCurrCode, LocalDate [] mDate1, LocalDate [] mDate2)
    {
        String mPath = tec_kat_curs + File.separator + mCurrCode;
        File file = null;
        String p_name_date = ""; String p_name_rate = ""; String p_name_curs = "";
        double m_rate = 100;
        String [][] mArray = new String[2][];
        ArrayList<String> mArray_Date = new ArrayList<>();
        ArrayList<String> mArray_Curs = new ArrayList<>();

        ////////////////////////////////////////////////////////////////////////////////////
        // https://bank.gov.ua/control/uk/curmetal/currency/search/form/period
        // проверяем существование каталога, елс его нет создаем его
        boolean mkdirs_result = new File(mPath).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath + " Каталог уже существует");
        }
        // ищем файлы с расширением json
        File dir = new File(mPath);
        File[] matchingFiles = dir.listFiles((dir1, name) -> name.endsWith("json"));
        // берем файл последний из списка
        assert matchingFiles != null;
        for (File file_row:matchingFiles) {
            file = file_row;
        }

        if (file == null) {
            Main.MessageBoxError(mPath , "Не найден файл *.json в каталоге - загрузить с https://bank.gov.ua/control/uk/curmetal/currency/search/form/period");
        }

        assert file != null;
        if (!file.exists()) {
            Main.MessageBoxError(mPath, "Не найден файл *.json в каталоге - загрузить с https://bank.gov.ua/control/uk/curmetal/currency/search/form/period");
        }

        // json
        //Official hrivnya exchange rates.json - en
        //Офіційний курс гривні щодо іноземних валют.json - uk
        String m_file_name = file.getName();
        if (m_file_name.equals("Official hrivnya exchange rates.json")) { p_name_date = "Date"; p_name_rate = "Unit"; p_name_curs = "Official hrivnya exchange rates, UAH"; }
        else if (m_file_name.equals("Офіційний курс гривні щодо іноземних валют.json")) { p_name_date = "Дата"; p_name_rate = "Кількість одиниць"; p_name_curs = "Офіційний курс гривні, грн"; }

        if (file.isFile()) {
            try {
                List<String> listDate = new ArrayList<>();
                List<Double> listCurs = new ArrayList<>();
                JsonReader reader = new JsonReader(new FileReader(file.getPath()));
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        String name_conv = new String(name.getBytes(), StandardCharsets.UTF_8);
                        if (name_conv.equals(p_name_date)) {
                            listDate.add(reader.nextString());      // дата
                        } else if (name_conv.equals(p_name_rate)) {        // количество
                            m_rate = reader.nextDouble();
                        } else if (name_conv.equals(p_name_curs)) {        // курс
                            listCurs.add(reader.nextDouble() / m_rate);
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                }
                reader.endArray();
                reader.close();

                // после получения списков, заполняем массив нужными данными
                for (int i = 0; i < mDate1.length; i++) {
                    int num_list = 0;
                    for (String list : listDate) {
                        LocalDate list_date = getDateString(list);
                        if (list_date.compareTo(mDate1[i]) >= 0 && list_date.compareTo(mDate2[i]) <= 0) {
                            // преобразование DD.MM.YYYY в YYYYMMDD
                            list = list.substring(6, 10) + list.substring(3, 5) + list.substring(0, 2);
                            mArray_Date.add(list);
                            mArray_Curs.add(listCurs.get(num_list).toString());
                        }
                        num_list++;
                    }
                }

                if (mArray_Date.size() == 0)
                {
                    Main.MessageBoxError("Курсы из файла \n" +  m_file_name + "\n не загружены, возможно изменилась структура", "");
                }

                // переносим в общий массив
                mArray[0] = mArray_Date.toArray(new String[0]);
                mArray[1] = mArray_Curs.toArray(new String[0]);

            } catch (IOException e) {
                e.printStackTrace();
                Main.MessageBoxError(e.toString(), "");
            }
        }
        return mArray;
    }

    // Получить курс НБУ (Онлайн XML)
    private String [][] getCursNbu_WEB(String mCurrCode, LocalDate [] mDate1, LocalDate [] mDate2) throws TransformerException {
        String mPath = tec_kat_curs + File.separator + mCurrCode;
        File file;
        String mPathXml;
        String [][] mArray = new String[2][];
        ArrayList<String> mArray_Date = new ArrayList<>();
        ArrayList<String> mArray_Curs = new ArrayList<>();
        String p_periodStartTime;
        String p_periodEndTime;
        int p_currency_id = 0;
        //
        p_periodStartTime = mDate1[0].format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
        p_periodEndTime = mDate2[mDate2.length - 1].format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
        //
        switch (mCurrCode) {
            case "USD" -> p_currency_id = 169;
            case "EUR" -> p_currency_id = 196;
            case "GBP" -> p_currency_id = 163;
        }

        // действия, если папка не существует, создаем
        boolean mkdirs_result = new File(mPath).mkdirs();
        if (!mkdirs_result) {
            System.out.println(mPath + " Каталог уже существует");
        }

        mPathXml = mPath + File.separator + "search_results (" + p_periodStartTime + " - " + p_periodEndTime + ").xml";
        file = new File(mPathXml);

        // Удаляет Кеш файл
        if (del_cache_file.isSelected()) {
            boolean delete_result = file.delete();
            if (!delete_result) {
                System.out.println(mPathXml + " Файла не существует");
            }
            file = new File(mPathXml);
        }

        // не сохранять на диске
        if (not_create_cache_file.isSelected()) {
            file = new File("");
        }

        Document doc = null; // документ xml
        // Если нет файла взять его с сайта
        if (!file.exists()) {
            ////////////////////////////////////////////////////////////////////////////////////
            // чтение файла с НБУ
            // https://old.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=169&periodStartTime=01.11.2018&periodEndTime=02.11.2018&outer=xml&execute=%D0%92%D0%B8%D0%BA%D0%BE%D0%BD%D0%B0%D1%82%D0%B8
            try {
                URL xmlURL = new URL("https://old.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=" + p_currency_id + "&periodStartTime=" + p_periodStartTime + "&periodEndTime=" + p_periodEndTime + "&outer=xml&execute=%D0%92%D0%B8%D0%BA%D0%BE%D0%BD%D0%B0%D1%82%D0%B8");
                InputStream xml = xmlURL.openStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(xml);
                xml.close();
                // не сохранять на диске (выключено)
                if (!not_create_cache_file.isSelected()) {
                    // сохранение на локальном диске
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    Source source = new DOMSource(doc);
                    Result result = new StreamResult(new FileOutputStream(mPathXml));
                    transformer.transform(source, result);
                    // перечитать созданный файл
                    file = new File(mPathXml);
                }
            }
            catch (IOException | ParserConfigurationException | SAXException | TransformerConfigurationException e){
                e.printStackTrace();
                Main.MessageBoxError(e.toString(), "");
            }
        }

        try {
            List<String> listDate = new ArrayList<>();
            List<Double> listCurs = new ArrayList<>();
            LocalDate m_date_old = null;
            // чтение XML файла
            // не сохранять на диске (выключено)
            if (!not_create_cache_file.isSelected()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(file);
            }
            assert doc != null;
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("currency");
            for (int s = 0; s < nodeLst.getLength(); s++) {
                org.w3c.dom.Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element fstElement = (Element) fstNode;
                    Element message = (Element) fstElement.getElementsByTagName("date").item(0);
                    String date_text = message.getTextContent();
                    message = (Element) fstElement.getElementsByTagName("exchange_rate").item(0);
                    String curs_text = (message.getTextContent().replace(",", "."));
                    message = (Element) fstElement.getElementsByTagName("number_of_units").item(0);
                    String rate_text = message.getTextContent();

                    LocalDate m_date = getDateString(date_text);
                    int kol_day = Period.between(mDate1[0], m_date).getDays();
                    // если в начале не хватает курсов дополняем
                    double curs_rate = Double.parseDouble(curs_text) / Double.parseDouble(rate_text);
                    if (s == 0 && kol_day > 0) {
                        LocalDate m_date_temp = mDate1[0];
                        for (int k = 0; k < kol_day; k++) {
                            listDate.add(m_date_temp.format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" )));      // дата
                            listCurs.add(curs_rate);
                            m_date_temp = m_date_temp.plusDays(1);
                        }
                    }
                    // восстанавливаем курсы если есть пустые участки
                    if (s > 0) {
                        assert m_date_old != null;
                        kol_day = Period.between(m_date_old, m_date).getDays();
                        if (kol_day > 1) {
                            LocalDate m_date_temp = m_date_old;
                            for (int k = 0; k < kol_day - 1; k++) {
                                m_date_temp = m_date_temp.plusDays(1);
                                listDate.add(m_date_temp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));      // дата
                                listCurs.add(curs_rate);
                            }
                        }
                    }

                    listDate.add(date_text);      // дата
                    listCurs.add(curs_rate);
                    m_date_old = m_date;
                }
            }

            // после получения списков, заполняем массив нужными данными
            for (int i = 0; i < mDate1.length; i++) {
                int num_list = 0;
                for (String list : listDate) {
                    LocalDate list_date = getDateString(list);
                    if (list_date.compareTo(mDate1[i]) >= 0 && list_date.compareTo(mDate2[i]) <= 0) {
                        // преобразование DD.MM.YYYY в YYYYMMDD
                        list = list.substring(6, 10) + list.substring(3, 5) + list.substring(0, 2);
                        mArray_Date.add(list);
                        mArray_Curs.add(listCurs.get(num_list).toString());
                    }
                    num_list++;
                }
            }

            if (mArray_Date.size() == 0) {
                Main.MessageBoxError("Курсы не загружены, возможно изменилась структура", "");
            }

            // переносим в общий массив
            mArray[0] = mArray_Date.toArray(new String[0]);
            mArray[1] = mArray_Curs.toArray(new String[0]);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            Main.MessageBoxError(e.toString(), "");
        }

        return mArray;
    }

    // Прорисовка графика
    private void LineChartGraphic(String [][] mArray, int is_average_value_curr, int is_visible_points) {
        if (mArray == null) { return; }

        // отчищаем график
        chart.getData().clear();
        chart.setAnimated(false);

        // Определяем годы
        int m_year_temp = Integer.parseInt(mArray[0][0].substring(0, 4));
        int m_year_base = m_year_temp;

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName(Integer.toString(m_year_temp));

        double [][] mArray_average = new double [1][1];
        double m_average_value = 0;
        if (is_average_value_curr == 1) {
            // определяем сколько годов
            int m_average_num;
            int m_kol_year = Integer.parseInt(mArray[0][mArray[0].length - 1].substring(0, 4)) - Integer.parseInt(mArray[0][0].substring(0, 4)) + 1;
            mArray_average = new double [2][m_kol_year];
            int m_base_year = Integer.parseInt(mArray[0][0].substring(0, 4));
            // идем по годам
            for (int ii = 0; ii < m_kol_year; ii++) {
                // идем по всему массиву
                m_average_value = 0;
                m_average_num = 0;
                for (int iii = 0; iii < mArray[0].length; iii++) {
                    if (mArray[1][iii] != null) {
                        if (Integer.toString(m_base_year).equals(mArray[0][iii].substring(0, 4))) {
                            m_average_value += Double.parseDouble(mArray[1][iii].replace(",", "."));
                            m_average_num++;
                        }
                    }
                }
                if (m_average_num == 0) { m_average_num = 1; }
                m_average_value = m_average_value / m_average_num;
                if (m_average_value == 0) { m_average_value = 1; }
                mArray_average[0][ii] = m_base_year;
                mArray_average[1][ii] = m_average_value;
                // следующий год
                m_base_year++;
            }
        }

        double m_base_min = 0, m_base_max = 0, m_base_init = 0;
        for (int ii = 0; ii < mArray[0].length; ii++)
        {
            m_year_temp = Integer.parseInt(mArray[0][ii].substring(0, 4));
            // если год меняется, генерируем новый график
            if (m_year_base != m_year_temp) {
                // добавляем при каждом изменении, кроме первого
                if (ii > 0) {
                    chart.getData().add(series1);
                }
                // переинициализация
                series1 = new XYChart.Series<>();
                series1.setName(Integer.toString(m_year_temp));
                m_year_base = m_year_temp;
            }
            // добавление значений
            if (mArray[1][ii] != null) {
                int m_day = Integer.parseInt(mArray[0][ii].substring(6, 8));
                int m_months = Integer.parseInt(mArray[0][ii].substring(4, 6));
                int m_year = Integer.parseInt(mArray[0][ii].substring(0, 4));

                // убираем высокосный, чтобы не было ошибок
                if (m_months == 2 && m_day == 29) {
                    m_day = 28;
                }

                String ValueDate = m_day + "." + m_months;
                double DoubleData = Double.parseDouble(mArray[1][ii].replace(",", "."));

                if (is_average_value_curr == 1) {
                    for (int iii = 0; iii < mArray_average[0].length; iii++) {
                        if (mArray_average[0][iii] == m_year) {
                            m_average_value = mArray_average[1][iii];
                        }
                    }
                    series1.getData().add(new XYChart.Data<>(ValueDate, DoubleData / m_average_value));
                }
                else {
                    series1.getData().add(new XYChart.Data<>(ValueDate, DoubleData));
                }

                // поиск мин. и макс. значения
                double m_value_data = DoubleData;
                if (is_average_value_curr == 1) m_value_data = DoubleData / m_average_value;
                if (m_base_init == 0) {
                    m_base_min = m_value_data;
                    m_base_max = m_value_data;
                    m_base_init = 1;
                }
                if (m_base_min > m_value_data) m_base_min = m_value_data;
                if (m_base_max < m_value_data) m_base_max = m_value_data;

            }
        }
        // последний год
        chart.getData().add(series1);

        // ручное масштабирование
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(m_base_min - (m_base_max - m_base_min)/20);
        yAxis.setUpperBound(m_base_max + (m_base_max - m_base_min)/20);
        yAxis.setTickUnit((m_base_max - m_base_min)/20);

        // Всплывающие подсказки в узлах
        if (is_visible_points == 1) {
            int kol_charts = chart.getData().size();
            for (int k = 0; k < kol_charts; k++) {
                ObservableList<XYChart.Data> dataList;
                dataList = ((XYChart.Series) chart.getData().get(k)).getData();
                dataList.forEach(data -> {
                    Node node = data.getNode();
                    //Tooltip tooltip = new Tooltip('(' + data.getXValue().toString() + ';' + data.getYValue().toString() + ')');
                    Tooltip tooltip = new Tooltip(data.getYValue().toString());
                    Tooltip.install(node, tooltip);
                });
            }
        }
    }

    // Валидация даты
    public boolean isDateValid(String m_date, String format)
    {
        if (m_date.isEmpty()) { return false; }
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern ( format );
            LocalDate.parse ( m_date , f );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Преобразование текста в дату
    public LocalDate getDateString(String m_date, String format)
    {
        if (!isDateValid(m_date, format)) { return null; }
        DateTimeFormatter f = DateTimeFormatter.ofPattern ( format );
        return LocalDate.parse ( m_date , f );
    }

    // Преобразование текста в дату
    public LocalDate getDateString(String m_date)
    {
        return getDateString(m_date, "dd.MM.yyyy");
    }

    public static class ConnectionFileDataDB
    {
        private String JDBCConnection;
        private String DBUser;
        private String DBPassword;
        private String DBInsertSchema;
        private String DBInsertProcedure;
        private boolean Rezult;

        public ConnectionFileDataDB(String typeDB) {

            // Получить параметры
            String mPath = tec_kat + File.separatorChar + "DBConnSettings.json";
            File file = new File(mPath);
            if (!file.exists()) {
                Rezult = false; // не успешно
                System.out.println(mPath + " Тип " + typeDB + " Файл не найден, Запись в DB невозможна");
                return;
            }

            if (file.isFile()) {
                try {
                    JsonReader reader = new JsonReader(new FileReader(file.getPath()));
                    reader.beginObject();
                    while(reader.hasNext()){
                        String name = reader.nextName();
                        if (name.equals("Connection" + typeDB)) {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                name = reader.nextName();
                                switch (name) {
                                    case "JDBCConnection" -> JDBCConnection = reader.nextString();
                                    case "DBUser" -> DBUser = reader.nextString();
                                    case "DBPassword" -> DBPassword = reader.nextString();
                                    case "DBInsertSchema" -> DBInsertSchema = reader.nextString();
                                    case "DBInsertProcedure" -> DBInsertProcedure = reader.nextString();
                                    default -> reader.skipValue();
                                }
                            }
                            reader.endObject();
                            Rezult = true; // успешно
                        }
                        else { // те которые пропускем, все равно проходим
                            reader.beginObject();
                            while (reader.hasNext()) { reader.skipValue(); }
                            reader.endObject();
                        }
                    }
                    reader.close();

                    if (!Rezult) {
                        System.out.println(mPath + " Тип " + typeDB + " в файл не найден. Запись в DB невозможна");
                    }

                } catch (IOException e) {
                    System.out.println(mPath + " " + e);
                    Rezult = false; // не успешно
                }
            }
        }

        public boolean GetRezult() { return !Rezult; }
    }

    // Подключение к DB и создание базы данных SQLite
    public Connection ConnectionSQLite()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("SQLite");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection);
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка ConnectionSQLite");
        }
        return connection;
    }

    // Создание базы данных SQLite и создание таблицы для добавления данных
    public void CreateTableSQLite()
    {
        Connection connection = null;
        try
        {
            // create a database connection
            connection = ConnectionSQLite();
            if (connection == null) { return; }

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            // создать таблицу, если её нет
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS CURS " +
                            "( ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                              "CURS_DATE TEXT NOT NULL," +
                              "CURR_CODE TEXT NOT NULL," +
                              "RATE REAL NOT NULL CHECK(RATE > 0)" +
                            ");");

            // создать индекс, если его нет
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS UK_CURS ON CURS (CURS_DATE, CURR_CODE);");

            // создать представление, если его нет
            statement.executeUpdate("CREATE VIEW IF NOT EXISTS CURS_AVG_YEAR\n" +
                    "AS\n" +
                    "SELECT SUBSTR(k.CURS_DATE, 1, 4) as PART_DATE,\n" +
                    "       k.CURR_CODE,\n" +
                    "       avg(k.RATE) as AVG_RATE\n" +
                    "FROM CURS k\n" +
                    "GROUP BY substr(k.CURS_DATE, 1, 4), k.CURR_CODE;");

            // создать представление, если его нет
            statement.executeUpdate("CREATE VIEW IF NOT EXISTS CURS_AVG\n" +
                    "AS\n" +
                    "SELECT f.PART_DATE,\n" +
                    "       f.CURR_CODE,\n" +
                    "\t   AVG(f.AVG_RATE) as AVG_RATE\n" +
                    "FROM (\n" +
                    "SELECT substr(k.CURS_DATE, 6,5) as PART_DATE,\n" +
                    "       k.CURR_CODE,\n" +
                    "       (k.RATE/a.AVG_RATE)*100 as AVG_RATE \n" +
                    "FROM CURS k\n" +
                    "INNER JOIN CURS_AVG_YEAR a ON a.PART_DATE = substr(k.CURS_DATE, 1, 4) AND a.CURR_CODE = k.CURR_CODE\n" +
                    ") f\n" +
                    "GROUP BY f.PART_DATE, f.CURR_CODE\n");

            // создать представление, если его нет
            statement.executeUpdate("CREATE VIEW IF NOT EXISTS CURS_REPORT\n" +
                    "AS\n" +
                    "SELECT k.CURS_DATE,\n" +
                    "       k.CURR_CODE,\n" +
                    "\t   k.RATE,\n" +
                    "       a.AVG_RATE as AVG_RATE\n" +
                    "FROM CURS k\n" +
                    "INNER JOIN CURS_AVG a ON a.PART_DATE = substr(k.CURS_DATE, 6, 5) AND a.CURR_CODE = k.CURR_CODE\n" +
                    "WHERE substr(k.CURS_DATE, 1, 4) IN (SELECT substr(MAX(date(kk.CURS_DATE)),1,4) FROM CURS kk) AND a.AVG_RATE <= 100\n" +
                    "ORDER BY 1");
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка CreateTableSQLite");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка CreateTableSQLite");
            }
        }
    }

    // Добавление данных в базу SQLite
    // SELECT * from CURS k where date(k.curs_date) between date('2021-11-07') and date('2021-11-10')
    public void AddTableSQLite(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionSQLite();
            if (connection == null) { return; }

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "INSERT OR IGNORE INTO CURS(curs_date, curr_code, rate) VALUES(?, ?, ?);";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                                                mArray[0][iii].substring(4, 6) + "-" +
                                                mArray[0][iii].substring(6, 8) + " 00:00:00"
                                                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableSQLite");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTableSQLite");
            }
        }
    }

    // Подключение к DB MySQL
    public Connection ConnectionMySQL()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("MySQL");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection, ConnDB.DBUser, ConnDB.DBPassword);
        }
        catch(SQLException e)
        {
            connection = null;
            System.out.println("ConnectionMySQL: " + e.getMessage());
        }
        return connection;
    }

    // Добавление данных в базу MySQL
    public void AddTableMySQL(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionMySQL();
            if (connection == null) { return; }

            ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("MySQL");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "CALL `" + ConnDB.DBInsertSchema + "`.`" + ConnDB.DBInsertProcedure + "`(?, ?, ?);";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                        mArray[0][iii].substring(4, 6) + "-" +
                        mArray[0][iii].substring(6, 8) + " 00:00:00"
                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableMySQL");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTableMySQL");
            }
        }
    }

    // Подключение к DB PostgreSQL
    public Connection ConnectionPostgreSQL()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("PostgreSQL");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection, ConnDB.DBUser, ConnDB.DBPassword);
        }
        catch(SQLException e)
        {
            connection = null;
            System.out.println("ConnectionPostgreSQL: " + e.getMessage());
        }
        return connection;
    }

    // Добавление данных в базу PostgreSQL
    public void AddTablePostgreSQL(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionPostgreSQL();
            if (connection == null) { return; }

            ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("PostgreSQL");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "CALL " + ConnDB.DBInsertSchema + "." + ConnDB.DBInsertProcedure + "(?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                        mArray[0][iii].substring(4, 6) + "-" +
                        mArray[0][iii].substring(6, 8) + " 00:00:00"
                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTablePostgreSQL");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTablePostgreSQL");
            }
        }
    }

    // Подключение к DB Oracle
    public Connection ConnectionOracle()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("Oracle");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection, ConnDB.DBUser, ConnDB.DBPassword);
        }
        catch(SQLException e)
        {
            connection = null;
            System.out.println("ConnectionOracle: " + e.getMessage());
        }
        return connection;
    }

    // Добавление данных в базу Oracle
    public void AddTableOracle(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionOracle();
            if (connection == null) { return; }

            ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("Oracle");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "CALL " + ConnDB.DBInsertSchema + "." + ConnDB.DBInsertProcedure + "(?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                        mArray[0][iii].substring(4, 6) + "-" +
                        mArray[0][iii].substring(6, 8)
                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableOracle");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTableOracle");
            }
        }
    }

    // Подключение к DB MSSQL
    public Connection ConnectionMSSQL()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("MSSQL");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection);
        }
        catch(SQLException e)
        {
            connection = null;
            System.out.println("ConnectionMSSQL: " + e.getMessage());
        }
        return connection;
    }

    // Добавление данных в базу MSSQL
    public void AddTableMSSQL(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionMSSQL();
            if (connection == null) { return; }

            ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("MSSQL");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "{ CALL " + ConnDB.DBInsertProcedure + "(?, ?, ?) }";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                        mArray[0][iii].substring(4, 6) + "-" +
                        mArray[0][iii].substring(6, 8)
                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableMSSQL");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTableMSSQL");
            }
        }
    }

    // Подключение к DB AzureSQL
    public Connection ConnectionAzureSQL()
    {
        ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("AzureSQL");
        if (ConnDB.GetRezult()) { return null; }

        Connection connection;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(ConnDB.JDBCConnection);
        }
        catch(SQLException e)
        {
            connection = null;
            System.out.println("ConnectionAzureSQL: " + e.getMessage());
        }
        return connection;
    }

    // Добавление данных в базу AzureSQL
    public void AddTableAzureSQL(String mCurrCode, String[][] mArray)
    {
        Connection connection = null;
        try {
            // create a database connection
            connection = ConnectionMSSQL();
            if (connection == null) { return; }

            ConnectionFileDataDB ConnDB = new ConnectionFileDataDB("AzureSQL");

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            for (int iii = 0; iii < mArray[0].length; iii++) {
                String INSERT_SQL = "{ CALL " + ConnDB.DBInsertProcedure + "(?, ?, ?) }";
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                ps.setString(1, mArray[0][iii].substring(0, 4) + "-" +
                        mArray[0][iii].substring(4, 6) + "-" +
                        mArray[0][iii].substring(6, 8)
                ); // TEXT как строки ISO8601 ("YYYY-MM-DD HH:MM:SS").
                ps.setString(2, mCurrCode);
                ps.setDouble(3, Main.getString_Double(mArray[1][iii]));
                ps.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableAzureSQL");
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                Main.MessageBoxError(e.getMessage(), "Ошибка AddTableAzureSQL");
            }
        }
    }
}
