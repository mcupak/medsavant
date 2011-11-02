/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.view.patients.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {

    //private List<Object> fieldValues;
    private CohortDetailsSW sw;
    private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    //private String[] cohortNames;
    private JList list;
    private Cohort cohort;
    private Cohort[] cohorts;
    
    private class CohortDetailsSW extends SwingWorker {
        private final Cohort cohort;

        public CohortDetailsSW(Cohort cohort) {
            this.cohort = cohort;
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            List<SimplePatient> patientList = CohortQueryUtil.getIndividualsInCohort(ProjectController.getInstance().getCurrentProjectId(), cohort.getId());
            return patientList;
        }
        
        @Override
        protected void done() {
            try {
                List<SimplePatient> result = (List<SimplePatient>) get();
                setPatientList(result);
                
            } catch (Exception ex) {
                return;
            }
        }
        
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();
        
        details.setLayout(new BorderLayout());
        //.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        
        details.add(ViewUtil.getKeyValuePairPanel("Patients in cohort", patients.size() + ""), BorderLayout.NORTH);
        DefaultListModel lm = new DefaultListModel();
        /*for (Vector v : patients) {
            JLabel l = new JLabel(v.get(CohortViewTableSchema.INDEX_HOSPITALID-1).toString()); l.setForeground(Color.white);
            //details.add(l);
            lm.addElement((String) v.get(CohortViewTableSchema.INDEX_HOSPITALID-1));
        }*/
        for(SimplePatient i : patients) {
            JLabel l = new JLabel(i.toString());
            l.setForeground(Color.white);
            details.add(l);
            lm.addElement(i);
        }
        list = (JList) ViewUtil.clear(new JList(lm));
        list.setBackground(ViewUtil.getDetailsBackgroundColor());
        list.setForeground(Color.white);
        JScrollPane jsp = ViewUtil.getClearBorderlessJSP(list);
        details.add(jsp, BorderLayout.CENTER);
        //list.setOpaque(false);

        details.updateUI();  
    }
    
    public CohortDetailedView() {
        content = this.getContentPanel();
        
        details = ViewUtil.getClearPanel();
        menu = ViewUtil.getButtonPanel();
        
        //menu.add(setDefaultCaseButton());
        //menu.add(setDefaultControlButton());
        menu.add(removeIndividualsButton());
        menu.add(deleteCohortButton());
        menu.setVisible(false);
        
        content.setLayout(new BorderLayout());
        
        content.add(details,BorderLayout.CENTER);
        content.add(menu,BorderLayout.SOUTH);
    }
    
    @Override
    public void setSelectedItem(Vector item) {
        cohort = ((Cohort) item.get(0));
        setTitle(cohort.getName());
        
        details.removeAll();
        details.updateUI();
        
        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsSW(cohort);
        sw.execute();
        
        if(menu != null) menu.setVisible(true);
    }
    
    @Override
    public void setMultipleSelections(List<Vector> items){
        cohorts = new Cohort[items.size()];
        for(int i = 0; i < items.size(); i++){
            cohorts[i] = (Cohort) items.get(i).get(0);
        }
    }
    
    /*
    private JButton setDefaultCaseButton(){
        JButton button = new JButton("Set default Case cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        }); 
        return button;
    }
    
    private JButton setDefaultControlButton(){
        JButton button = new JButton("Set default Control cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO
            }
        }); 
        return button;
    }
     * 
     */
    
    private JButton removeIndividualsButton(){
        JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] selected = list.getSelectedValues();
                int[] patientIds = new int[selected.length];
                for(int i = 0; i < selected.length; i++){
                    //patientIds[i] = (Integer) selected[i];
                    patientIds[i] = ((SimplePatient) selected[i]).getId();
                }
                if(patientIds != null && patientIds.length > 0){
                    
                    try {
                        CohortQueryUtil.removePatientsFromCohort(patientIds, cohort.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sw = new CohortDetailsSW(cohort);
                    sw.execute();
                }
            }
        }); 
        return button;
    }
    
    private JButton deleteCohortButton(){
        JButton button = new JButton("Delete cohort(s)");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(cohorts != null && cohorts.length > 0){
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to delete these cohorts?\nThis cannot be undone.",
                            "Confirm", 
                            JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) return;
                    try {
                        CohortQueryUtil.removeCohorts(cohorts);
                    } catch (SQLException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    parent.refresh();
                }
            }
        }); 
        return button;
    }

    
}