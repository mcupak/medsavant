package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Action;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Status;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.DefaultvariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.PatientformatTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.PatienttablemapTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.model.structure.MedSavantDatabase.VarianttablemapTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public class ProjectQueryUtil {
    
    public static List<String> getProjectNames() throws SQLException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());

        List<String> results = new ArrayList<String>();
        
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }   
    
    public static boolean containsProject(String projectName) throws SQLException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addAllColumns();
        query.addFromTable(table.getTable());
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        return rs.next();
    }

    public static int getProjectId(String projectName) throws SQLException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }

    public static void removeReferenceForProject(int project_id, int ref_id) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(table.getTable());
        query1.addColumns(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query1.addCondition(ComboCondition.and(
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id), 
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query1.toString());
        
        while (rs.next()) {
            String tableName = rs.getString(1);
            DBUtil.dropTable(tableName);
        }
        
        DeleteQuery query2 = new DeleteQuery(table.getTable());
        query2.addCondition(ComboCondition.and(
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id), 
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));
    }

    public static String getProjectName(int projectid) throws SQLException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs1 = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        if (rs1.next()) {
            return rs1.getString(1);
        } else {
            return null;
        }
    }
    
    public static String createVariantTable(int projectid, int referenceid) throws SQLException {
        return createVariantTable(projectid, referenceid, 0, null, false, true);
    }

    public static String createVariantTable(int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging, boolean addToTableMap) throws SQLException {
        
        String variantTableInfoName = isStaging ? DBSettings.createVariantStagingTableName(projectid, referenceid, updateid) : DBSettings.createVariantTableName(projectid, referenceid);

        Connection c = (ConnectionController.connect(DBSettings.DBNAME));
   
        String query = 
                "CREATE TABLE `" + variantTableInfoName + "` ("
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_UPLOAD_ID + "` int(11) NOT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_FILE_ID + "` int(11) NOT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_VARIANT_ID + "` int(11) NOT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_DNA_ID + "` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_CHROM + "` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_POSITION + "` int(11) NOT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_DBSNP_ID + "` varchar(45) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_REF + "` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_ALT + "` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_QUAL + "` float(10,0) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_FILTER + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_AA + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_AC + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_AF + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_AN + "` int(11) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_BQ + "` float DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_CIGAR + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_DB + "` int(1) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_DP + "` int(11) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_END + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_H2 + "` int(1) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_MQ + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_MQ0 + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_NS + "` int(11) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_SB + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_SOMATIC + "` int(1) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_VALIDATED + "` int(1) DEFAULT NULL,"
                + "`" + DefaultvariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,";
        
        //add each annotation
        if(annotationIds != null){
            for(int annotationId : annotationIds){
                query += getAnnotationSchema(annotationId);
            }
        }
        
        query = query.substring(0, query.length()-1); //remove last comma
        query += ") ENGINE=BRIGHTHOUSE;";

        c.createStatement().execute(query);

        if(!isStaging && addToTableMap){
            TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
            InsertQuery query1 = new InsertQuery(table.getTable());
            query1.addColumn(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query1.addColumn(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid);
            query1.addColumn(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), variantTableInfoName);

            c.createStatement().execute(query1.toString());
        }

        return variantTableInfoName;
    }
    
    private static String getAnnotationSchema(int annotationId){
        
        AnnotationFormat format = null;
        try {
            format = AnnotationQueryUtil.getAnnotationFormat(annotationId);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return format.generateSchema();
    }

    public static int getNumberOfRecordsInVariantTable(int projectid, int refid) throws SQLException {
        String variantTableName = ProjectQueryUtil.getVariantTablename(projectid,refid);
        return DBUtil.getNumRecordsInTable(variantTableName);
    }

    public static String getVariantTablename(int projectid, int refid) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query.addCondition(ComboCondition.and(
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid), 
                BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid)));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        rs.next();
        return rs.getString(1);
    }
    
    
    public static int addProject(String name, File patientFormatFile) throws SQLException, ParserConfigurationException, SAXException, IOException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), name);

        PreparedStatement stmt = (ConnectionController.connect(DBSettings.DBNAME)).prepareStatement(query.toString(),
                Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int projectid = res.getInt(1);

        PatientQueryUtil.createPatientTable(projectid, patientFormatFile);

        return projectid;
    }

    public static void removeProject(String projectName) throws SQLException {
        
        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        if (rs.next()) {
            removeProject(rs.getInt(1));
        }
    }
      
      
    public static void removeProject(int projectid) throws SQLException {
        
        
        Connection c = ConnectionController.connect(DBSettings.DBNAME);
        
        TableSchema projectTable = MedSavantDatabase.ProjectTableSchema;
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        
        //remove from project table
        DeleteQuery q1 = new DeleteQuery(projectTable.getTable());
        q1.addCondition(BinaryCondition.equalTo(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID, projectid));
        c.createStatement().execute(q1.toString());
        
        //remove patient table
        SelectQuery q2 = new SelectQuery();
        q2.addFromTable(patientMapTable.getTable());
        q2.addColumns(patientMapTable.getDBColumn(PatienttablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        q2.addCondition(BinaryCondition.equalTo(patientMapTable.getDBColumn(PatienttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        
        ResultSet rs1 = ConnectionController.connect().createStatement().executeQuery(q2.toString());
      
        rs1.next();
        String patientTableName = rs1.getString(PatienttablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME);
        c.createStatement().execute("DROP TABLE IF EXISTS " + patientTableName);
        
        //remove from patient format table
        DeleteQuery q3 = new DeleteQuery(patientFormatTable.getTable());
        q3.addCondition(BinaryCondition.equalTo(patientFormatTable.getDBColumn(PatientformatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q3.toString());

        //remove from patient tablemap
        DeleteQuery q4 = new DeleteQuery(patientMapTable.getTable());
        q4.addCondition(BinaryCondition.equalTo(patientMapTable.getDBColumn(PatienttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q4.toString());
        
        //remove variant tables
        SelectQuery q5 = new SelectQuery();
        q5.addFromTable(variantMapTable.getTable());
        q5.addColumns(variantMapTable.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        q5.addCondition(BinaryCondition.equalTo(variantMapTable.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        
        ResultSet rs2 = c.createStatement().executeQuery(q5.toString());
          
        while(rs2.next()) {
            String variantTableName = rs2.getString(1);
            c.createStatement().execute("DROP TABLE IF EXISTS " + variantTableName);
        }
        
        //remove from variant tablemap
        DeleteQuery q6 = new DeleteQuery(variantMapTable.getTable());
        q6.addCondition(BinaryCondition.equalTo(variantMapTable.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q6.toString());

        //remove cohort entries
        List<Integer> cohortIds = CohortQueryUtil.getCohortIds(projectid);
        for(Integer cohortId : cohortIds){
            CohortQueryUtil.removeCohort(cohortId);
        }
        
    }   
    
    public static void setAnnotations(int projectid, int refid, String annotation_ids) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.toString());
        query.addSetClause(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annotation_ids);
        query.addCondition(ComboCondition.and(BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid)));
        query.addCondition(ComboCondition.and(BinaryCondition.equalTo(table.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid)));
        
        (ConnectionController.connect()).createStatement().execute(query.toString());
        
        AnnotationLogQueryUtil.addAnnotationLogEntry(projectid, refid, Action.UPDATE_TABLE, Status.PENDING);
    }
    
    public static List<ProjectDetails> getProjectDetails(int projectId) throws SQLException {
        
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(variantMapTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER, 
                variantMapTable.getTable(), 
                refTable.getTable(), 
                BinaryCondition.equalTo(variantMapTable.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryCondition.equalTo(variantMapTable.getDBColumn(VarianttablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        
        ResultSet rs = ConnectionController.connect().createStatement().executeQuery(query.toString());
        
        List<ProjectDetails> result = new ArrayList<ProjectDetails>();
        while(rs.next()){
            result.add(new ProjectDetails(
                    rs.getInt(VarianttablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), 
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME), 
                    rs.getString(VarianttablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS)));
        }
        
        return result;
    }

}
