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

package org.ut.biolab.medsavant.view.genetics;

import java.awt.CardLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel.DataRetriever;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
class TablePanel extends JPanel implements FiltersChangedListener {
    private static final Logger LOG = Logger.getLogger(TablePanel.class.getName());
    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";

    private SearchableTablePanel tablePanel;
    private CardLayout cl;
    private boolean init = false;
    private boolean updateRequired = true;
    private final Object updateLock = new Object();
    private String pageName;
    
    public TablePanel(final String pageName) {
            
        this.pageName = pageName;
        cl = new CardLayout();
        this.setLayout(cl);
        this.add(new WaitPanel("Generating List View"), CARD_WAIT);
        showWaitCard();

        final TablePanel instance = this;
        MedSavantWorker worker = new MedSavantWorker(pageName){
            @Override
            protected Object doInBackground() {

                List<String> fieldNames = new ArrayList<String>();
                List<Class> fieldClasses = new ArrayList<Class>();
                List<Integer> hiddenColumns = new ArrayList<Integer>();

                AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
                for(AnnotationFormat af : afs){
                    for(CustomField field : af.getCustomFields()){
                        fieldNames.add(field.getAlias());
                        switch(field.getColumnType()){
                            case INTEGER:
                            case BOOLEAN:
                                fieldClasses.add(Integer.class);
                                break;
                            case FLOAT:
                            case DECIMAL:
                                fieldClasses.add(Double.class);
                                break;
                            case VARCHAR:
                            default:
                                fieldClasses.add(String.class);
                                break;
                        }
                    }
                }
                if(this.isThreadCancelled()) return null;
                
                DataRetriever retriever = new DataRetriever(){
                    public List<Object[]> retrieve(int start, int limit) {
                        showWaitCard();
                        List<Object[]> result = null;
                        try {
                            result = ResultController.getInstance().getFilteredVariantRecords(start, limit);
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        showShowCard();
                        return result;
                    }

                    public int getTotalNum() {
                        showWaitCard();
                        int result = 0;
                        try {
                            result = ResultController.getInstance().getNumFilteredVariants();
                        } catch (NonFatalDatabaseException ex) {
                            Logger.getLogger(TablePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        showShowCard();
                        return result;
                    }

                    public void retrievalComplete() {
                        synchronized (updateLock){
                            updateRequired = false;
                        }
                    }
                };
                tablePanel = new SearchableTablePanel(pageName, fieldNames, fieldClasses, hiddenColumns, 1000, retriever);
                updateIfRequired();
                
                return null;
            }

            /*@Override
            protected void finish(Object result) {       
                instance.add(tablePanel, CARD_SHOW);             
                FilterController.addFilterListener(instance);       
            }*/

            @Override
            protected void showProgress(double fraction) {
                //do nothing
            }

            @Override
            protected void showSuccess(Object result) {
                instance.add(tablePanel, CARD_SHOW);             
                FilterController.addFilterListener(instance);  
                init = true;
            }

        };
        worker.execute();
        
    }
    
    private void showWaitCard() {
        cl.show(this, CARD_WAIT);    
    }

    private void showShowCard() {
        cl.show(this, CARD_SHOW);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {       
        synchronized (updateLock){
            updateRequired = true;
        }
        if(ViewController.getInstance().getCurrentSectionView() != null && ViewController.getInstance().getCurrentSectionView().getName().equals(pageName)){
            updateIfRequired();
        }
    }
    
    public boolean isInit(){
        return init;
    }
    
    public void updateIfRequired(){
        if(tablePanel == null) return;
        synchronized (updateLock){
            if(updateRequired){
                tablePanel.forceRefreshData();
            }
        }
    }
    
}