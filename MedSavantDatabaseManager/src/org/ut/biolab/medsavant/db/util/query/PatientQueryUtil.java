/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.util.query;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.DBSettings;

/**
 *
 * @author Andrew
 */
public class PatientQueryUtil {
    
    public static List<Vector> getBasicPatientInfo(int projectId, int limit) throws SQLException, NonFatalDatabaseException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getPatientTableSchema(tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        List<Vector> result = new ArrayList<Vector>();
        while(rs.next()){
            Vector v = new Vector();
            v.add(rs.getInt(1));
            for(int i = 2; i < 7; i++){
                v.add(rs.getString(i));
            }
            result.add(v);
        }
        return result;
    }
    
    public static Vector getPatientRecord(int projectId, int patientId) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getPatientTableSchema(tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), patientId));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        rs.next();
        Vector v = new Vector();
        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
            v.add(rs.getObject(i));
        }
        return v;
    }
    
    public static List<String> getPatientFieldAliases(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        List<String> result = new ArrayList<String>();
        result.add(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID);
        result.add(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID);
        result.add(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID);
        result.add(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID);
        result.add(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        while(rs.next()){
            result.add(rs.getString(1));
        }
        return result;
    }
    
    public static List<CustomField> getPatientFields(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        List<CustomField> result = new ArrayList<CustomField>();
        result.add(new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", false, DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, ""));
        result.add(new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, ""));
        result.add(new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, ""));
        result.add(new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, ""));
        result.add(new CustomField(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, ""));
        
        while(rs.next()){
            result.add(new CustomField(
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), 
                    rs.getBoolean(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }
        return result;
    }
    
    public static String getPatientTablename(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatienttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        rs.next();
        return rs.getString(1);
    }
    
     
    public static void createPatientTable(int projectid, File patientFormatFile) throws SQLException, ParserConfigurationException, SAXException, IOException {

        String patientTableName = DBSettings.createPatientTableName(projectid);        
        Connection c = ConnectionController.connect();

        //create basic fields
        String query = 
                "CREATE TABLE `" + patientTableName + "` ("
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,";
        
        //add any extra fields
        List<CustomField> customFields = new ArrayList<CustomField>();
        if(patientFormatFile != null){
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(patientFormatFile);
            doc.getDocumentElement().normalize();

            NodeList fields = doc.getElementsByTagName("field");
            for(int i = 0; i < fields.getLength(); i++){
                Element field = (Element)(fields.item(i));       
                CustomField current = new CustomField(
                        field.getAttribute("name"),
                        field.getAttribute("type"),
                        field.getAttribute("filterable").equals("true"),
                        field.getAttribute("alias"),
                        field.getAttribute("description")
                        );
                customFields.add(current);
                query += current.generateSchema();
            }        
        }
        
        query += "PRIMARY KEY (`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "`)"
                + ") ENGINE=MyISAM;";
        
        //create table
        c.createStatement().execute(query);

        //add to tablemap
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        InsertQuery query1 = new InsertQuery(patientMapTable.getTable());
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME), patientTableName);
        c.createStatement().executeUpdate(query1.toString());
        
        //populate format table
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        c.setAutoCommit(false);
        for(int i = 0; i < customFields.size(); i++){
            CustomField a = customFields.get(i);
            InsertQuery query2 = new InsertQuery(patientFormatTable.getTable());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), a.getColumnName());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), a.getColumnType());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (a.isFilterable() ? "1" : "0"));
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), a.getAlias());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), a.getDescription());
            c.createStatement().executeUpdate(query2.toString());
        }
        c.commit();
        c.setAutoCommit(true);   
        
    }
    
    public static void removePatient(int projectId, int[] patientIds) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        TableSchema table = CustomTables.getPatientTableSchema(tablename);
        
        Connection c = ConnectionController.connect();
        c.setAutoCommit(false);       
        for(int id : patientIds){
            //remove all references
            CohortQueryUtil.removePatientReferences(projectId, id); 
            
            //remove from patient table
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
        }
        c.commit();
        c.setAutoCommit(true);
    }
    
    public static void addPatient(int projectId, List<CustomField> cols, List<String> values) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        TableSchema table = CustomTables.getPatientTableSchema(tablename);
        
        InsertQuery query = new InsertQuery(table.getTable());
        for(int i = 0; i < Math.min(cols.size(), values.size()); i++){
            query.addColumn(new DbColumn(table.getTable(), cols.get(i).getColumnName(), cols.get(i).getColumnType(), 100), values.get(i));
        }
        
        ConnectionController.connect().createStatement().executeUpdate(query.toString()); 
        
        /*
        String query = "INSERT INTO " + tablename + " (";
        for(int i = 0; i < cols.size(); i++){
            query += "`" + cols.get(i).getColumnName() + "`";
            if(i != cols.size()-1){
                query += ",";
            }
        }
        query += ") VALUES (";
        for(int i = 0; i < cols.size(); i++){
            switch(cols.get(i).getFieldType()){
                case VARCHAR:
                case DATE:
                case TIMESTAMP:
                    query += "'" + values.get(i) + "'";
                    break;
                case BOOLEAN:
                    query += (Boolean.parseBoolean(values.get(i)) ? "1" : "0");
                default:
                    query += values.get(i);
            }
            if(i != cols.size()-1){
                query += ",";
            }
        }
        query += ");";
        
        Connection c = ConnectionController.connect();
        c.createStatement().executeUpdate(query);*/
    }

    public static List<String> getDNAIdsWithValuesInRange(int projectId, String columnName, Range r) throws NonFatalDatabaseException, SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getPatientTableSchema(tablename);
        
        DbColumn currentDNAId = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        q.addCondition(BinaryCondition.greaterThan(testColumn, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(testColumn, r.getMax(), true));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        
        List<String> result = new ArrayList<String>();
        while(rs.next()){          
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }
    
     
    public static List<String> getDNAIdsForList(TableSchema table, List<String> list, String columnAlias) throws NonFatalDatabaseException, SQLException {
 
        DbColumn currentDNAId = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnAlias);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        
        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryCondition.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));   
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){          
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }

}
