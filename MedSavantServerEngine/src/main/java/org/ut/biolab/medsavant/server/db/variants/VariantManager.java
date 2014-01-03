/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.server.db.variants;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFileTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantStarredTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTagColumns;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.NetworkManager;
import org.ut.biolab.medsavant.server.serverapi.PatientManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.shard.io.FileUtils;
import org.ut.biolab.medsavant.shard.server.ShardedVariantManagerHelper;
import org.ut.biolab.medsavant.shard.server.ShardedVariantManagerUtilsHelper;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.model.ScatterChartMap;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.VariantComment;
import org.ut.biolab.medsavant.shared.model.VariantTag;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

/**
 * Sharded version of VariantManager.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class VariantManager extends MedSavantServerUnicastRemoteObject implements VariantManagerAdapter, BasicVariantColumns {

    private static final long serialVersionUID = -8095354382487804508L;
    private static final Log LOG = LogFactory.getLog(VariantManager.class);
    // thresholds for querying and drawing
    private static final int COUNT_ESTIMATE_THRESHOLD = 1000;
    private static final int BIN_TOTAL_THRESHOLD = 1000000;
    private static final int PATIENT_HEATMAP_THRESHOLD = 1000;
    // Stages within the upload process.
    private static final double LOG_FRACTION = 0.05;
    private static final double DUMP_FRACTION = 0.1;
    private static final double SORTING_FRACTION = 0.1;
    private static final double CUSTOM_FIELD_FRACTION = 0.05;
    private static final double ANNOTATING_FRACTION = 0.15;
    private static final double SPLITTING_FRACTION = 0.05;
    private static final double MERGING_FRACTION = 0.05;
    private static final double CREATING_TABLES_FRACTION = 0.05;
    private static final double SUBSET_FRACTION = 0.05;
    private static final double LOAD_TABLE_FRACTION = 0.15; // Happens twice
    private static VariantManager instance;
    private static ShardedVariantManagerHelper helper;
    private static ShardedVariantManagerUtilsHelper utilsHelper;
    // public static boolean REMOVE_TMP_FILES = false;
    static boolean REMOVE_WORKING_DIR = true;

    private VariantManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized VariantManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new VariantManager();
            helper = new ShardedVariantManagerHelper();
            utilsHelper = new ShardedVariantManagerUtilsHelper();
        }
        return instance;
    }

    /**
     * Make all variant tables live for a project. All users accessing this
     * database will be logged out.
     */
    @Override
    public void publishVariants(String sessID, int projectID) throws Exception {

        LOG.info("Beginning publish of all tables for project " + projectID);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            // get update ids and references
            LOG.info("Getting map of update ids");
            int[] refIDs = ProjectManager.getInstance().getReferenceIDsForProject(sessID, projectID);
            Map<Integer, Integer> ref2Update = new HashMap<Integer, Integer>();
            for (int refID : refIDs) {
                ref2Update.put(refID, ProjectManager.getInstance().getNewestUpdateID(sessID, projectID, refID, false));
            }

            // update annotation log table
            LOG.info("Setting log status to published");
            for (Integer refId : ref2Update.keySet()) {
                AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, ref2Update.get(refId), Status.PUBLISHED);
            }

            // publish
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID),
                    "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing tables");
            for (Integer refId : ref2Update.keySet()) {
                ProjectManager.getInstance().publishVariantTable(conn, projectID, refId, ref2Update.get(refId));
            }

            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }

    /**
     * Make a variant table live. All users accessing this database will be
     * logged out.
     */
    @Override
    public void publishVariants(String sessID, int projID, int refID, int updID) throws Exception {
        LOG.info("Publishing table. pid:" + projID + " refid:" + refID + " upid:" + updID);
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            LOG.info("Setting log status to published");
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updID, Status.PUBLISHED);
            LOG.info("Releasing database lock.");
            SettingsManager.getInstance().releaseDBLock(conn);
            LOG.info("Terminating active sessions");
            SessionController.getInstance().terminateSessionsForDatabase(SessionController.getInstance().getDatabaseForSession(sessID),
                    "Administrator (" + SessionController.getInstance().getUserForSession(sessID) + ") published new variants");
            LOG.info("Publishing table");
            ProjectManager.getInstance().publishVariantTable(conn, projID, refID, updID);
            LOG.info("Publish complete");
        } finally {
            conn.close();
        }
    }

    /*
     * Remove an unpublished variant table.
     */
    @Override
    public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception {
        LOG.info("Cancelling publish. pid:" + projectID + " refid:" + referenceID + " upid:" + updateID);
        ProjectManager.getInstance().removeTables(sid, projectID, referenceID, updateID, updateID);
        LOG.info("Cancel complete");
    }

    /**
     * Perform updates to custom vcf fields and other annotations. Will result
     * in the creation of a new, unpublished, up-to-date variant table. This
     * method is used only by ProjectWizard.modifyProject().
     */
    @Override
    public int updateTable(String sessID, int projID, int refID, int[] annotIDs, CustomField[] customFields, boolean autoPublish, String email) throws Exception {

        EmailLogger.logByEmail("Update STARTED", "Update started. " + annotIDs.length + " annotation(s) will be performed. You will be notified again upon completion.", email);
        try {
            int updateID = ImportUpdateManager.doUpdate(sessID, projID, refID, annotIDs, customFields, autoPublish);

            EmailLogger.logByEmail("Update COMPLETED", "Update completed. " + annotIDs.length + " annotation(s) were performed.", email);

            return updateID;
        } catch (Exception e) {
            EmailLogger.logByEmail("Update FAILED", "Update failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error(e);
            throw e;
        }

    }

    /**
     * Import variant files which have been transferred from a client.
     */
    @Override
    public int uploadVariants(String sessID, int[] transferIDs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish) throws Exception {

        LOG.info("Importing variants by transferring from client");

        NetworkManager netMgr = NetworkManager.getInstance();
        File[] vcfFiles = new File[transferIDs.length];
        String[] sourceNames = new String[transferIDs.length];

        int i = 0;
        for (int id : transferIDs) {
            vcfFiles[i] = netMgr.getFileByTransferID(sessID, id);
            sourceNames[i] = netMgr.getSourceNameByTransferID(sessID, id);
            i++;
        }

        return uploadVariants(sessID, vcfFiles, sourceNames, projID, refID, tags, includeHomoRef, email, autoPublish);
    }

    /**
     * Use when variant files are already on the server. Performs variant import
     * of an entire directory.
     */
    @Override
    public int uploadVariants(String sessID, File dirContainingVCFs, int projID, int refID, String[][] tags, boolean includeHomoRef, String email, boolean autoPublish)
            throws RemoteException, SessionExpiredException, IOException, Exception {

        LOG.info("Importing variants already stored on server in dir " + dirContainingVCFs.getAbsolutePath());

        if (!dirContainingVCFs.exists()) {
            LOG.info("Directory from which to load variants does not exist, bailing out.");
            return -1;
        }

        File[] vcfFiles = dirContainingVCFs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                return name.endsWith(".vcf") || name.endsWith(".vcf.gz");
            }
        });

        if (vcfFiles.length == 0) {
            LOG.info("Directory exists but contains no .vcf or .vcf.gz files.");
            return -1;
        }

        return uploadVariants(sessID, vcfFiles, null, projID, refID, tags, includeHomoRef, email, autoPublish);
    }

    /**
     * Start the upload process for new vcf files. Will result in the creation
     * of a new, unpublished, up-to-date variant table.
     * 
     * @param sessID
     *            uniquely identifies the client
     * @param vcfFiles
     *            local VCF files on the server's file-system
     * @param sourceNames
     *            if non-null, client-side names of uploaded files
     */
    public int uploadVariants(String sessionID, File[] vcfFiles, String[] sourceNames, int projectID, int referenceID, String[][] tags, boolean includeHomoRef, String email,
            boolean autoPublish) throws Exception {

        EmailLogger.logByEmail("Upload STARTED", "Upload started. " + vcfFiles.length + " file(s) will be imported. You will be notified again upon completion.", email);
        try {
            int updateID = ImportUpdateManager.doImport(sessionID, projectID, referenceID, autoPublish, vcfFiles, includeHomoRef, tags);
            EmailLogger.logByEmail("Upload COMPLETED", "Upload completed. " + vcfFiles.length + " file(s) were imported.", email);

            return updateID;
        } catch (Exception e) {
            EmailLogger.logByEmail("Upload FAILED", "Upload failed with error: " + MiscUtils.getStackTrace(e), email);
            LOG.error(e);
            throw e;
        }
    }

    @Override
    public int removeVariants(String sessID, int projID, int refID, List<SimpleVariantFile> files, boolean autoPublish, String email) throws Exception {
        LOG.info("Beginning removal of variants");

        ConnectionController.registerBackgroundUsageOfSession(sessID);

        EmailLogger.logByEmail("Removal STARTED", "Removal started. " + files.size() + " files(s) will be removed. You will be notified again upon completion.", email);

        String user = SessionController.getInstance().getUserForSession(sessID);

        // generate directory
        LOG.info("Generating base directory");
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        LOG.info("Base directory: " + baseDir.getCanonicalPath());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir);
        p.waitFor();

        // add log
        LOG.info("Adding log and generating update id");
        int updateId = AnnotationLogManager.getInstance().addAnnotationLogEntry(sessID, projID, refID, org.ut.biolab.medsavant.shared.model.AnnotationLog.Action.REMOVE_VARIANTS);

        try {

            // dump existing except for files
            ProjectManager projMgr = ProjectManager.getInstance();
            String existingTableName = projMgr.getVariantTableName(sessID, projID, refID, false);
            File existingVariantsFile = new File(baseDir, "proj" + projID + "_ref" + refID + "_update" + updateId);
            LOG.info("Dumping variants to file");
            String conditions = "";
            for (int i = 0; i < files.size(); i++) {
                conditions += "!(" + UPLOAD_ID.getColumnName() + "=" + files.get(i).getUploadId() + " AND " + FILE_ID.getColumnName() + "=" + files.get(i).getFileId() + ")";
                if (i != files.size() - 1) {
                    conditions += " AND ";
                }
            }
            VariantManagerUtils.variantTableToTSVFile(sessID, existingTableName, existingVariantsFile, conditions, true, 0);

            // create the staging table
            LOG.info("Creating new variant table for resulting variants");
            projMgr.setCustomVariantFields(sessID, projID, refID, updateId,
                    projMgr.getCustomVariantFields(sessID, projID, refID, projMgr.getNewestUpdateID(sessID, projID, refID, false)));
            String tableName = projMgr.createVariantTable(sessID, projID, refID, updateId, AnnotationManager.getInstance().getAnnotationIDs(sessID, projID, refID), true);
            String tableNameSub = projMgr.createVariantTable(sessID, projID, refID, updateId, AnnotationManager.getInstance().getAnnotationIDs(sessID, projID, refID), false, true);

            // upload to staging table
            LOG.info("Uploading variants to table: " + tableName);
            VariantManagerUtils.uploadTSVFileToVariantTable(sessID, existingVariantsFile, tableName);

            // upload to sub table
            File subFile = new File(existingVariantsFile.getAbsolutePath() + "_subset");
            LOG.info("Generating sub file: " + subFile.getAbsolutePath());
            VariantManagerUtils.generateSubset(existingVariantsFile, subFile);
            LOG.info("Importing to: " + tableNameSub);
            VariantManagerUtils.uploadTSVFileToVariantTable(sessID, subFile, tableNameSub);

            /*
             * if (REMOVE_TMP_FILES) { boolean deleted =
             * existingVariantsFile.delete(); LOG.info("Deleting " +
             * existingVariantsFile.getAbsolutePath() + " - " + (deleted ?
             * "successful" : "failed")); deleted = subFile.delete();
             * LOG.info("Deleting " + subFile.getAbsolutePath() + " - " +
             * (deleted ? "successful" : "failed")); }
             */

            // get annotation ids
            AnnotationManager annotMgr = AnnotationManager.getInstance();
            int[] annotIDs = annotMgr.getAnnotationIDs(sessID, projID, refID);

            // add entries to tablemap
            projMgr.addTableToMap(sessID, projID, refID, updateId, false, tableName, annotIDs, tableNameSub);

            // cleanup
            LOG.info("Dropping old table(s)");
            int newestId = projMgr.getNewestUpdateID(sessID, projID, refID, true);
            int minId = -1;
            int maxId = newestId - 1;
            projMgr.removeTables(sessID, projID, refID, minId, maxId);

            // TODO: remove files

            // TODO: server logs

            // set as pending
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.PENDING);

            if (autoPublish) {
                publishVariants(sessID, updateId);
            }

            EmailLogger.logByEmail("Removal COMPLETED", "Removal completed. " + files.size() + " file(s) were removed.", email);

            return updateId;

        } catch (Exception e) {
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessID, updateId, Status.ERROR);
            EmailLogger.logByEmail("Removal FAILED", "Removal failed with error: " + MiscUtils.getStackTrace(e), email);
            throw e;
        } finally {
            ConnectionController.unregisterBackgroundUsageOfSession(sessID);
        }
    }

    @Override
    public int exportVariants(String sessID, int projID, int refID, Condition[][] conditions, boolean orderedByPosition, boolean zipOutputFile) throws SQLException,
            RemoteException, SessionExpiredException, IOException, InterruptedException {

        // generate directory
        File baseDir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());
        Process p = Runtime.getRuntime().exec("chmod -R o+w " + baseDir.getCanonicalPath());
        p.waitFor();

        String filename = ProjectManager.getInstance().getProjectName(sessID, projID).replace(" ", "") + "-varexport-" + System.currentTimeMillis() + ".tdf";
        File file = new File(baseDir, filename);

        LOG.info("Exporting variants to " + file.getAbsolutePath());

        long start = System.currentTimeMillis();
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);
        if (orderedByPosition) {
            query.addOrderings(table.getDBColumn(POSITION));
        }
        String intoString = "INTO OUTFILE \"" + FileUtils.getParametrizedFilePath(file) + "\" " + "FIELDS TERMINATED BY '"
                + StringEscapeUtils.escapeJava(VariantManagerUtils.FIELD_DELIMITER) + "' " + "ENCLOSED BY '" + VariantManagerUtils.ENCLOSED_BY + "' " + "ESCAPED BY '"
                + StringEscapeUtils.escapeJava(VariantManagerUtils.ESCAPE_CHAR) + "'" // +
                                                                                      // " LINES TERMINATED BY '\\r\\n' ";
        ;
        String queryString = query.toString().replace("FROM", intoString + "FROM");

        LOG.info(queryString);
        // ConnectionController.executeQuery(sessID, queryString);

        // export variants from shards - note: this relies on the sharding
        // strategy based on position, otherwise the data in VCF file won't be
        // in the correct order
        utilsHelper.exportVariantTablesToSingleFile(file, queryString);

        LOG.info("Done exporting variants to " + file.getAbsolutePath());
        LOG.info("Export took " + ((System.currentTimeMillis() - start) / 1000) + " seconds");

        if (zipOutputFile) {
            LOG.info("Zipping export...");
            File zipFile = new File(file.getAbsoluteFile() + ".zip");
            IOUtils.zipFile(file, zipFile);
            boolean deleted = file.delete();
            LOG.info("Deleting " + file.getAbsolutePath() + " - " + (deleted ? "successful" : "failed"));
            file = zipFile;
            LOG.info("Done zipping");
        }

        // add file to map and send the id back
        int fileID = NetworkManager.getInstance().openReaderOnServer(sessID, file);
        return fileID;
    }

    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException, SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, new Condition[1][], start, limit);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException,
            SessionExpiredException {
        return getVariants(sessionId, projectId, referenceId, conditions, start, limit, null);
    }

    @Override
    public List<Object[]> getVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions, int start, int limit, String[] orderByCols) throws SQLException,
            RemoteException, SessionExpiredException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessionId, ProjectManager.getInstance().getVariantTableName(sessionId, projectId, referenceId, true));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);

        LOG.debug(query);

        return helper.getVariants(sessionId, query, table, start, limit, orderByCols);
    }

    @Override
    public int getVariantCount(String sid, int projectId, int referenceId) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[0][], true);
    }

    @Override
    public int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        return getFilteredVariantCount(sid, projectId, referenceId, conditions, false);
    }

    private int getFilteredVariantCount(String sid, int projectId, int referenceId, Condition[][] conditions, boolean forceExact) throws SQLException, RemoteException,
            SessionExpiredException {

        // String name =
        // ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId,
        // referenceId, true);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float subMultiplier = (Float) variantTableInfo[2];

        if (tablename == null) {
            return -1;
        }

        // try to get a reasonable approximation
        if (tablenameSub != null && !forceExact && conditions.length > 0) {
            int estimate = (int) (getNumFilteredVariantsHelper(sid, tablenameSub, conditions) * subMultiplier);
            if (estimate >= COUNT_ESTIMATE_THRESHOLD) {
                return estimate; // TODO: this should be rounded instead of a
                                 // precise one
            }
        }

        // Approximation not good enough; use actual data.
        return getNumFilteredVariantsHelper(sid, tablename, conditions);
    }

    public int getNumFilteredVariantsHelper(String sessID, String tablename, Condition[][] conditions) throws SQLException, RemoteException, SessionExpiredException {
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        LOG.info(q);

        int res = helper.getNumFilteredVariants(sessID, q, table);

        LOG.info("Number of variants remaining: " + res);

        return res;
    }

    /*
     * Convenience method
     */
    @Override
    public int getVariantCountForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException,
            SessionExpiredException {

        if (dnaIDs.isEmpty()) {
            return 0;
        }

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);
        Condition dnaCondition = new InCondition(table.getDBColumn(DNA_ID.getColumnName()), dnaIDs);

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[] { ComboCondition.and(dnaCondition, ComboCondition.or(c1)) };

        return getFilteredVariantCount(sessID, projID, refID, new Condition[][] { finalCondition });
    }

    @Override
    public boolean willApproximateCountsForConditions(String sid, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException,
            SessionExpiredException {
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        return total >= BIN_TOTAL_THRESHOLD;
    }

    @Override
    public Map<Range, Long> getFilteredFrequencyValuesForNumericColumn(String sid, int projectId, int referenceId, Condition[][] conditions, CustomField column, boolean logBins)
            throws InterruptedException, SQLException, RemoteException, SessionExpiredException {

        // pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        return helper.getFilteredFrequencyValuesForNumericColumn(sid, q, table, column, multiplier, logBins);
    }

    @Override
    public Map<String, Integer> getFilteredFrequencyValuesForCategoricalColumn(String sessID, int projID, int refID, Condition[][] conditions, String colName) throws SQLException,
            RemoteException, SessionExpiredException {

        // pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sessID, projID, refID, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sessID, projID, refID, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sessID, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);
            multiplier = 1;
        }

        DbColumn column = table.getDBColumn(colName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(column);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        q.addGroupings(column);

        if (column.getColumnNameSQL().equals(ALT.getColumnName()) || column.getColumnNameSQL().equals(REF.getColumnName())) {
            q.addCondition(createNucleotideCondition(column));
        }

        return helper.getFilteredFrequencyValuesForCategoricalColumn(sessID, q, table, column.getColumnNameSQL(), multiplier);
    }

    @Override
    public ScatterChartMap getFilteredFrequencyValuesForScatter(String sid, int projectId, int referenceId, Condition[][] conditions, String columnnameX, String columnnameY,
            boolean columnXCategorical, boolean columnYCategorical, boolean sortKaryotypically) throws InterruptedException, SQLException, RemoteException, SessionExpiredException {

        // pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        DbColumn columnX = table.getDBColumn(columnnameX);
        DbColumn columnY = table.getDBColumn(columnnameY);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        // q.addColumns(columnX, columnY);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        if (columnnameX.equals(ALT.getColumnName()) || columnnameX.equals(REF.getColumnName())) {
            q.addCondition(createNucleotideCondition(columnX));
        }

        if (columnnameY.equals(ALT.getColumnName()) || columnnameY.equals(REF.getColumnName())) {
            q.addCondition(createNucleotideCondition(columnY));
        }

        return helper.getFilteredFrequencyValuesForScatter(sid, q, table, columnnameX, columnnameY, columnXCategorical, columnYCategorical, sortKaryotypically, multiplier);
    }

    /*
     * Convenience method
     */
    @Override
    public int getVariantCountInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException,
            RemoteException, SessionExpiredException {

        String name = ProjectManager.getInstance().getVariantTableName(sid, projectId, referenceId, true);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid, name);

        Condition[] rangeConditions = new Condition[] { BinaryCondition.equalTo(table.getDBColumn(CHROM), chrom),
                BinaryCondition.greaterThan(table.getDBColumn(POSITION), start, true), BinaryCondition.lessThan(table.getDBColumn(POSITION), end, false) };

        Condition[] c1 = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c1[i] = ComboCondition.and(conditions[i]);
        }

        Condition[] finalCondition = new Condition[] { ComboCondition.and(ComboCondition.and(rangeConditions), ComboCondition.or(c1)) };

        return getFilteredVariantCount(sid, projectId, referenceId, new Condition[][] { finalCondition });
    }

    @Override
    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException,
            RemoteException, SessionExpiredException {

        // pick table from approximate or exact
        TableSchema table;
        int total = getFilteredVariantCount(sid, projectId, referenceId, conditions);
        Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sid, projectId, referenceId, true);
        String tablename = (String) variantTableInfo[0];
        String tablenameSub = (String) variantTableInfo[1];
        float multiplier = (Float) variantTableInfo[2];
        if (total >= BIN_TOTAL_THRESHOLD) {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablenameSub);
        } else {
            table = CustomTables.getInstance().getCustomTableSchema(sid, tablename);
            multiplier = 1;
        }

        // TableSchema table =
        // CustomTables.getInstance().getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId,
        // referenceId, true));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());

        queryBase.addColumns(table.getDBColumn(CHROM));

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(CHROM));

        addConditionsToQuery(queryBase, conditions);

        return helper.getChromosomeHeatMap(sid, queryBase, table, POSITION.getColumnName(), binsize, multiplier);
    }

    @Override
    public int getPatientCountWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException,
            RemoteException, SessionExpiredException {

        // TODO: approximate counts??
        // might not be a good idea... don't want to miss a dna id

        TableSchema table = getCustomTableSchema(sid, projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DNA_ID.getColumnName() + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(POSITION), end);
        q.addCondition(ComboCondition.and(cond));

        return helper.getPatientCountWithVariantsInRange(sid, q, table);
    }

    @Override
    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) {
        Condition[] c = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c[i] = ComboCondition.and(conditions[i]);
        }
        query.addCondition(ComboCondition.or(c));
    }

    @Override
    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(String sessID, int projID, int refID, Condition[][] conditions, List<String> dnaIds, int limit)
            throws SQLException, RemoteException, SessionExpiredException {
        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sessID, projID, refID);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(DNA_ID), table.getDBColumn(CHROM), table.getDBColumn(POSITION));
        addConditionsToQuery(query, conditions);
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DNA_ID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        query.addCondition(ComboCondition.or(dnaIdConditions));

        return helper.getSavantBookmarkPositionsForDNAIDs(sessID, query, table, limit, results);
    }

    @Override
    public Map<String, Integer> getNumVariantsInFamily(String sessID, int projID, int refID, String famID, Condition[][] conditions) throws SQLException, RemoteException,
            SessionExpiredException {

        // TODO: approximate counts

        String name = ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true);

        if (name == null) {
            return null;
        }

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(DNA_ID));
        q.addCustomColumns(FunctionCall.countAll());
        q.addGroupings(table.getDBColumn(DNA_ID));
        addConditionsToQuery(q, conditions);

        Map<String, String> patientToDNAIDMap = PatientManager.getInstance().getDNAIDsForFamily(sessID, projID, famID);
        Map<String, List<String>> betterPatientToDNAIDMap = new HashMap<String, List<String>>();

        List<String> dnaIDs = new ArrayList<String>();
        for (String patientID : patientToDNAIDMap.keySet()) {
            String dnaIDString = patientToDNAIDMap.get(patientID);
            List<String> idList = new ArrayList<String>();
            for (String dnaID : dnaIDString.split(",")) {
                if (dnaID != null && !dnaID.isEmpty()) {
                    dnaIDs.add(dnaID);
                    idList.add(dnaID);
                }
            }
            betterPatientToDNAIDMap.put(patientID, idList);
        }

        Map<String, Integer> dnaIDsToCountMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {

            Condition[] dnaIDConditions = new Condition[dnaIDs.size()];

            int i = 0;
            for (String dnaID : dnaIDs) {
                dnaIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DNA_ID), dnaID);
                i++;
            }

            q.addCondition(ComboCondition.or(dnaIDConditions));

            dnaIDsToCountMap = helper.getNumVariantsInFamily(sessID, q, table, dnaIDsToCountMap);
        }

        Map<String, Integer> patientIDTOCount = new HashMap<String, Integer>();
        for (String patientID : betterPatientToDNAIDMap.keySet()) {
            int count = 0;
            for (String dnaID : betterPatientToDNAIDMap.get(patientID)) {
                if (dnaIDsToCountMap.containsKey(dnaID)) {
                    count += dnaIDsToCountMap.get(dnaID);
                }
            }
            patientIDTOCount.put(patientID, count);
        }

        return patientIDTOCount;
    }

    @Override
    public void cancelUpload(String sid, int uploadId, String tableName) {
        try {

            // remove log entry
            AnnotationLogManager.getInstance().removeAnnotationLogEntry(sid, uploadId);

            // drop staging table
            DBUtils.dropTable(sid, tableName);

        } catch (Exception ex) {
            LOG.warn("Error cancelling upload " + uploadId + " for " + tableName, ex);
        }
    }

    @Override
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException, SessionExpiredException {

        PooledConnection conn = ConnectionController.connectPooled(sid);
        try {
            TableSchema variantTagTable = MedSavantDatabase.VariantTagTableSchema;

            conn.setAutoCommit(false);

            // add tags
            for (int i = 0; i < variantTags.length && !Thread.currentThread().isInterrupted(); i++) {
                InsertQuery query = variantTagTable.insert(VariantTagColumns.UPLOAD_ID, uploadID, VariantTagColumns.TAGKEY, variantTags[i][0], VariantTagColumns.TAGVALUE,
                        variantTags[i][1]);
                conn.createStatement().executeUpdate(query.toString());
            }
            if (Thread.currentThread().isInterrupted()) {
                conn.rollback();
            } else {
                conn.commit();
            }
        } finally {
            conn.close();
        }
    }

    public void removeTags(String sessID, int uploadID) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, MedSavantDatabase.VariantTagTableSchema.delete(VariantTagColumns.UPLOAD_ID, uploadID).toString());
    }

    @Override
    public List<String> getDistinctTagNames(String sessID) throws SQLException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.VariantTagTableSchema.distinct().select(VariantTagColumns.TAGKEY).toString());

        List<String> tagNames = new ArrayList<String>();
        while (rs.next()) {
            tagNames.add(rs.getString(1));
        }

        return tagNames;
    }

    @Override
    public List<String> getValuesForTagName(String sessID, String tagName) throws SQLException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID,
                MedSavantDatabase.VariantTagTableSchema.distinct().where(VariantTagColumns.TAGKEY, tagName).select(VariantTagColumns.TAGVALUE).toString());

        List<String> tagValues = new ArrayList<String>();
        while (rs.next()) {
            tagValues.add(rs.getString(1));
        }

        return tagValues;

    }

    @Override
    public List<Integer> getUploadIDsMatchingVariantTags(String sessID, String[][] variantTags) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VariantTagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(VariantTagColumns.UPLOAD_ID));

        Condition[] orConditions = new Condition[variantTags.length];

        Set<String> seenConditions = new HashSet<String>();
        int duplicates = 0;

        for (int i = 0; i < variantTags.length; i++) {

            String strRepresentation = variantTags[i][0] + ":" + variantTags[i][1];

            if (seenConditions.contains(strRepresentation)) {
                duplicates++;
            } else {

                orConditions[i] = ComboCondition.and(new Condition[] { BinaryCondition.equalTo(table.getDBColumn(VariantTagColumns.TAGKEY), variantTags[i][0]),
                        BinaryCondition.equalTo(table.getDBColumn(VariantTagColumns.TAGVALUE), variantTags[i][1]) });
                seenConditions.add(strRepresentation);
            }
        }

        q.addCondition(ComboCondition.or(orConditions));
        q.addGroupings(table.getDBColumn(VariantTagColumns.UPLOAD_ID));
        q.addHaving(BinaryCondition.equalTo(FunctionCall.countAll(), variantTags.length - duplicates));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<Integer> results = new ArrayList<Integer>();
        while (rs.next()) {
            results.add(rs.getInt(1));
        }

        return results;
    }

    @Override
    public SimpleVariantFile[] getUploadedFiles(String sessID, int projID, int refID) throws SQLException, RemoteException, SessionExpiredException {

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, ProjectManager.getInstance().getVariantTableName(sessID, projID, refID, true));
        TableSchema tagTable = MedSavantDatabase.VariantTagTableSchema;
        TableSchema pendingTable = MedSavantDatabase.VariantpendingupdateTableSchema;
        TableSchema fileTable = MedSavantDatabase.VariantFileTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(UPLOAD_ID), table.getDBColumn(FILE_ID));

        ResultSet idRs = ConnectionController.executeQuery(sessID, query.toString());

        List<Integer> uplIDs = new ArrayList<Integer>();
        List<Integer> fileIDs = new ArrayList<Integer>();
        while (idRs.next()) {
            uplIDs.add(idRs.getInt(1));
            fileIDs.add(idRs.getInt(2));
        }

        if (uplIDs.isEmpty()) {
            return new SimpleVariantFile[0];
        }

        Condition[] idConditions = new Condition[uplIDs.size()];
        for (int i = 0; i < uplIDs.size(); i++) {
            idConditions[i] = ComboCondition.and(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uplIDs.get(i)),
                    BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileIDs.get(i)));
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(tagTable.getTable());
        q.addFromTable(pendingTable.getTable());
        q.addFromTable(fileTable.getTable());
        q.addColumns(tagTable.getDBColumn(VariantTagColumns.UPLOAD_ID), tagTable.getDBColumn(VariantTagColumns.TAGVALUE),
                fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME),
                pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_USER));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VariantTagColumns.TAGKEY), VariantTag.UPLOAD_DATE));
        q.addCondition(BinaryCondition.equalTo(tagTable.getDBColumn(VariantTagColumns.UPLOAD_ID), pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        q.addCondition(BinaryCondition.equalTo(pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        q.addCondition(BinaryCondition.equalTo(fileTable.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID),
                pendingTable.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPLOAD_ID)));
        q.addCondition(ComboCondition.or(idConditions));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<SimpleVariantFile> result = new ArrayList<SimpleVariantFile>();
        while (rs.next()) {
            result.add(new SimpleVariantFile(rs.getInt(1), rs.getInt(3), rs.getString(4), rs.getString(1), rs.getString(5)));
        }
        return result.toArray(new SimpleVariantFile[0]);
    }

    @Override
    public List<String[]> getTagsForUpload(String sessID, int uplID) throws SQLException, RemoteException, SessionExpiredException {

        ResultSet rs = ConnectionController.executeQuery(sessID,
                MedSavantDatabase.VariantTagTableSchema.where(VariantTagColumns.UPLOAD_ID, uplID).select(VariantTagColumns.TAGKEY, VariantTagColumns.TAGVALUE).toString());

        List<String[]> result = new ArrayList<String[]>();
        while (rs.next()) {
            result.add(new String[] { rs.getString(1), rs.getString(2) });
        }
        return result;
    }

    /*
     * @Override public Set<StarredVariant> getStarredVariants(String sid, int
     * projectId, int referenceId) throws SQLException, RemoteException,
     * SessionExpiredException {
     * 
     * TableSchema table = MedSavantDatabase.VariantStarredTableSchema;
     * 
     * SelectQuery q = new SelectQuery(); q.addFromTable(table.getTable());
     * q.addColumns(
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
     * q.addCondition
     * (BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_PROJECT_ID), projectId));
     * q.addCondition(BinaryCondition.equalTo
     * (table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
     * referenceId));
     * 
     * ResultSet rs = ConnectionController.executeQuery(sid, q.toString());
     * 
     * Set<StarredVariant> result = new HashSet<StarredVariant>();
     * while(rs.next()) { result.add(new StarredVariant(
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
     * rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION),
     * rs.getTimestamp(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP))); }
     * return result; }
     */
    @Override
    public List<VariantComment> getVariantComments(String sid, int projectId, int referenceId, int uploadId, int fileID, int variantID) throws SQLException, RemoteException,
            SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
                table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION), table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), fileID));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), variantID));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        List<VariantComment> result = new ArrayList<VariantComment>();
        while (rs.next()) {
            result.add(new VariantComment(rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), rs
                    .getInt(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), rs.getInt(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), rs
                    .getInt(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), rs.getString(VariantStarredTableSchema.COLUMNNAME_OF_USER), rs
                    .getString(VariantStarredTableSchema.COLUMNNAME_OF_DESCRIPTION), rs.getTimestamp(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP)));
        }
        return result;
    }

    @Override
    public void addVariantComments(String sid, List<VariantComment> variants) throws SQLException, RemoteException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        for (VariantComment variant : variants) {
            InsertQuery q = new InsertQuery(table.getTable());
            List<DbColumn> columnsList = table.getColumns();
            Column[] columnsArray = new Column[columnsList.size()];
            columnsArray = columnsList.toArray(columnsArray);
            q.addColumns(columnsArray, variant.toArray(variant.getProjectId(), variant.getReferenceId()));
            c.createStatement().executeUpdate(q.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    /*
     * private void updateStarredVariant(Connection c, int projectId, int
     * referenceId, VariantComment variant) throws SQLException {
     * 
     * TableSchema table = MedSavantDatabase.VariantStarredTableSchema;
     * 
     * UpdateQuery q = new UpdateQuery(table.getTable());
     * //q.addSetClause(table.
     * getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * variant.getUser());
     * q.addSetClause(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_DESCRIPTION), variant.getDescription());
     * q.addSetClause(table
     * .getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP),
     * variant.getTimestamp());
     * q.addCondition(BinaryCondition.equalTo(table.getDBColumn
     * (VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
     * q.addCondition
     * (BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_REFERENCE_ID), referenceId));
     * q.addCondition(BinaryCondition
     * .equalTo(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_UPLOAD_ID), variant.getUploadId()));
     * q.addCondition(BinaryCondition
     * .equalTo(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_FILE_ID), variant.getFileId()));
     * q.addCondition(BinaryCondition
     * .equalTo(table.getDBColumn(VariantStarredTableSchema
     * .COLUMNNAME_OF_VARIANT_ID), variant.getVariantId()));
     * q.addCondition(BinaryCondition
     * .equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER),
     * variant.getUser()));
     * 
     * c.createStatement().executeUpdate(q.toString()); }
     */
    @Override
    public void removeVariantComments(String sessID, List<VariantComment> comments) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        Connection c = ConnectionController.connectPooled(sessID);
        c.setAutoCommit(false);

        for (VariantComment vc : comments) {
            DeleteQuery q = new DeleteQuery(table.getTable());
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), vc.getProjectId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), vc.getReferenceId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_UPLOAD_ID), vc.getUploadId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_FILE_ID), vc.getFileId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_VARIANT_ID), vc.getVariantId()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_USER), vc.getUser()));
            q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_TIMESTAMP), vc.getTimestamp()));

            ConnectionController.executeUpdate(sessID, q.toString());
        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    private int getTotalNumStarred(String sid, int projectId, int referenceId) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantStarredTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantStarredTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));

        ResultSet rs = ConnectionController.executeQuery(sid, q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public static void addEntryToFileTable(String sid, int uploadId, int fileId, String fileName) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        InsertQuery q = new InsertQuery(table.getTable());
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadId);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileId);
        q.addColumn(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME), fileName);

        ConnectionController.executeUpdate(sid, q.toString());
    }

    public void removeEntryFromFileTable(String sessID, int uploadID, int fileID) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        DeleteQuery q = new DeleteQuery(table.getTable());
        q.addCondition(ComboCondition.and(new Condition[] { BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadID),
                BinaryCondition.equalTo(table.getDBColumn(VariantFileTableSchema.COLUMNNAME_OF_FILE_ID), fileID) }));

        ConnectionController.executeUpdate(sessID, q.toString());
    }

    @Override
    public Map<SimplePatient, Integer> getPatientHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<SimplePatient> patients) throws SQLException,
            RemoteException, SessionExpiredException {

        // get dna ids
        List<String> dnaIds = new ArrayList<String>();
        for (SimplePatient sp : patients) {
            for (String id : sp.getDnaIds()) {
                if (!dnaIds.contains(id)) {
                    dnaIds.add(id);
                }
            }
        }

        Map<String, Integer> dnaIdMap = getDNAIDHeatMap(sessID, projID, refID, conditions, dnaIds);

        // map back to simple patients;
        Map<SimplePatient, Integer> result = new HashMap<SimplePatient, Integer>();
        for (SimplePatient p : patients) {
            Integer count = 0;
            for (String dnaId : p.getDnaIds()) {
                Integer i = dnaIdMap.get(dnaId);
                if (i != null) {
                    count += i;
                }
            }
            result.put(p, count);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getDNAIDHeatMap(String sessID, int projID, int refID, Condition[][] conditions, Collection<String> dnaIDs) throws SQLException, RemoteException,
            SessionExpiredException {

        Map<String, Integer> dnaIDMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {
            // combine conditions
            Condition[] c1 = new Condition[conditions.length];
            for (int i = 0; i < conditions.length; i++) {
                c1[i] = ComboCondition.and(conditions[i]);
            }
            Condition c2 = ComboCondition.or(c1);

            Object[] variantTableInfo = ProjectManager.getInstance().getVariantTableInfo(sessID, projID, refID, true);
            String tablename = (String) variantTableInfo[0];
            String tablenameSub = (String) variantTableInfo[1];
            float multiplier = (Float) variantTableInfo[2];

            // be careful if table names are null
            List<String> dnaIDs2 = new ArrayList<String>();
            TableSchema subTable = null;
            if (tablenameSub != null) {
                subTable = CustomTables.getInstance().getCustomTableSchema(sessID, tablenameSub);

                getDNAIDHeatMapHelper(sessID, subTable, multiplier, dnaIDs, c2, true, dnaIDMap);

                // Determine dnaIDs with no value yet.
                for (String id : dnaIDs) {
                    if (!dnaIDMap.containsKey(id)) {
                        dnaIDs2.add(id);
                    }
                }
            }

            TableSchema table = null;
            if (tablename != null) {
                table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

                // get remaining dna ids from actual table
                if (!dnaIDs2.isEmpty()) {
                    getDNAIDHeatMapHelper(sessID, table, 1, dnaIDs2, c2, false, dnaIDMap);
                }
            }
        }

        return dnaIDMap;
    }

    private void getDNAIDHeatMapHelper(String sessID, TableSchema table, float multiplier, Collection<String> dnaIDs, Condition c, boolean useThreshold, Map<String, Integer> map)
            throws SQLException, SessionExpiredException {
        // generate conditions from dna ids
        Condition dnaCondition = new InCondition(table.getDBColumn(DNA_ID), dnaIDs);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addColumns(table.getDBColumn(DNA_ID));
        q.addCondition(ComboCondition.and(new Condition[] { dnaCondition, c }));
        q.addGroupings(table.getDBColumn(DNA_ID));

        map = helper.getDNAIDHeatMap(sessID, q, table, PATIENT_HEATMAP_THRESHOLD, multiplier, useThreshold, map);
    }

    private Condition createNucleotideCondition(DbColumn column) {
        return ComboCondition.or(BinaryCondition.equalTo(column, "A"), BinaryCondition.equalTo(column, "C"), BinaryCondition.equalTo(column, "G"),
                BinaryCondition.equalTo(column, "T"));
    }

    private Annotation[] getAnnotationsFromIDs(int[] annotIDs, String sessID) throws RemoteException, SessionExpiredException, SQLException {
        int numAnnotations = annotIDs.length;
        Annotation[] annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sessID, annotIDs[i]);
            LOG.info("\t" + (i + 1) + ". " + annotations[i].getProgram() + " " + annotations[i].getReferenceName() + " " + annotations[i].getVersion());
        }
        return annotations;
    }
}
