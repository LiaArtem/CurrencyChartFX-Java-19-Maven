package com.example.currencychartfxmaven;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.BasicDBObject;
import org.bson.*;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class mongodb {

    public static BsonValue getBsonValue(Object obj) {
        if (obj instanceof Integer) {
            return new BsonInt32((Integer) obj);
        }

        if (obj instanceof String) {
            return new BsonString((String) obj);
        }

        if (obj instanceof Long) {
            return new BsonInt64((Long) obj);
        }

        if (obj instanceof Date) {
            return new BsonDateTime(((Date) obj).getTime());
        }
        if (obj instanceof Double || obj instanceof Float) {
            assert obj instanceof Double;
            return new BsonDouble((Double) obj);
        }
        return new BsonNull();
    }

    public static void AddTableMongoDB(String mCurrCode, String[][] mArray) {

        curr_chart_Controller.ConnectionFileDataDB ConnDB = new curr_chart_Controller.ConnectionFileDataDB("MongoDB");
        if (ConnDB.GetRezult()) { return; }

        try {
            MongoClient client = new MongoClient(ConnDB.MongoDBHost, ConnDB.MongoDBPort);
            MongoDatabase database = client.getDatabase(ConnDB.MongoDBDatabase);
            MongoCollection<Document> collection = database.getCollection(ConnDB.MongoDBCollection);

            // Создание уникального индекса
            boolean m_exists_index = false;
            for (Object index : collection.listIndexes()) {
                if (index.toString().contains("curs_date_1_curr_code_1")) {
                    m_exists_index = true;
                }
            }
            if (!m_exists_index) {
                String json_index = "{ curs_date: 1, curr_code: 1 }";
                collection.createIndex(BasicDBObject.parse(json_index), new IndexOptions().unique(true));
            }

            // подготовка данных
            List<Document> list = new ArrayList<>();
            for (int iii = 0; iii < mArray[0].length; iii++) {
                Document document = new Document();
                int year = Integer.parseInt(mArray[0][iii].substring(0, 4));
                int month = Integer.parseInt(mArray[0][iii].substring(4, 6));
                int day = Integer.parseInt(mArray[0][iii].substring(6, 8));
                Date dt = java.util.Date.from(ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIDNIGHT, ZoneOffset.systemDefault()).toInstant());
                document.append("curs_date", getBsonValue(dt));
                document.append("curr_code", getBsonValue(mCurrCode));
                document.append("rate", getBsonValue(Main.getString_Double(mArray[1][iii])));
                document.append("forc", getBsonValue(1));

                // проверка на уникальность
                Bson filter = Filters.and(eq("curs_date", getBsonValue(dt)),
                        eq("curr_code", getBsonValue(mCurrCode)));
                if (collection.countDocuments(filter) == 0) {
                    list.add(document);
                }
            }

            // Записать данные
            if (!list.isEmpty()) {
                collection.insertMany(list);
            }
            client.close();

        } catch (Exception e) {
            Main.MessageBoxError(e.getMessage(), "Ошибка AddTableMongoDB");
        }
    }

}
