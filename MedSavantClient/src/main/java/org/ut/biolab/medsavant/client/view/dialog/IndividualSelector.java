package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class IndividualSelector extends JDialog implements BasicPatientColumns {

    Set<String> selectedIndividuals;
    private static final Log LOG = LogFactory.getLog(IndividualSelector.class);
    private JPanel middlePanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private static final String[] COLUMN_NAMES = new String[]{
        PATIENT_ID.getAlias(),
        FAMILY_ID.getAlias(),
        HOSPITAL_ID.getAlias(),
        IDBIOMOM.getAlias(),
        IDBIODAD.getAlias(),
        GENDER.getAlias(),
        AFFECTED.getAlias(),
        DNA_IDS.getAlias(),
        PHENOTYPES.getAlias()};
    private static final Class[] COLUMN_CLASSES = new Class[]{Integer.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class, String.class, String.class};
    private static final int[] HIDDEN_COLUMNS = new int[]{0, 3, 4, 6};
    private SearchableTablePanel stp;
    private JLabel numselections;
    private IndividualReceiver retriever;
    private HashSet<Integer> selectedRows;
    private JButton ok;
    private boolean hasMadeSelections;

    public IndividualSelector() {
        super(MedSavantFrame.getInstance(), true);
        setTitle("Select Individuals");
        this.setPreferredSize(new Dimension(700, 600));
        this.setMinimumSize(new Dimension(700, 600));
        selectedIndividuals = new HashSet<String>();
        selectedRows = new HashSet<Integer>();
        initUI();
        refresh();
    }

    private void refresh() {
        stp.forceRefreshData();
    }

    private void initUI() {

        JPanel p = new JPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        this.add(p);

        topPanel = ViewUtil.getClearPanel();
        middlePanel = ViewUtil.getClearPanel();
        bottomPanel = ViewUtil.getClearPanel();

        p.add(topPanel);
        p.add(middlePanel);
        p.add(bottomPanel);

        // middle
        middlePanel.setLayout(new BorderLayout());

        retriever = new IndividualReceiver();

        stp = new SearchableTablePanel("Individuals", COLUMN_NAMES, COLUMN_CLASSES, HIDDEN_COLUMNS,
                true, true, Integer.MAX_VALUE, false, SearchableTablePanel.TableSelectionType.ROW, Integer.MAX_VALUE, retriever);
        stp.setExportButtonVisible(false);

        middlePanel.add(stp, BorderLayout.CENTER);

        // bottom
        ViewUtil.applyVerticalBoxLayout(bottomPanel);

        numselections = ViewUtil.getTitleLabel("0 individual(s) selected");

        JPanel text = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(text);

        JButton clearIndividuals = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CLOSE));
        clearIndividuals.setToolTipText("Clear selections");
        clearIndividuals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                clearSelections();
            }
        });

        text.add(numselections);
        text.add(Box.createHorizontalStrut(5));
        text.add(clearIndividuals);
        bottomPanel.add(ViewUtil.centerHorizontally(text));

        final JButton addAllIndividuals = ViewUtil.getSoftButton("Add All");
        bottomPanel.add(addAllIndividuals);

        final JButton addIndividuals = ViewUtil.getSoftButton("Add Selected");
        bottomPanel.add(addIndividuals);

        JPanel buttons = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(buttons);

        buttons.add(addAllIndividuals);
        buttons.add(addIndividuals);

        JPanel windowControlPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(windowControlPanel);
        windowControlPanel.add(Box.createHorizontalGlue());

        JButton cancel = new JButton("Cancel");
        bottomPanel.add(ViewUtil.centerHorizontally(buttons));

        ok = new JButton("OK");
        bottomPanel.add(ViewUtil.alignRight(ok));
        ok.setEnabled(false);

        windowControlPanel.add(cancel);
        windowControlPanel.add(ok);

        bottomPanel.add(windowControlPanel);

        final JDialog instance = this;

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                instance.setVisible(false);
                setIndividualsChosen(true);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                instance.setVisible(false);
                setIndividualsChosen(false || hasMadeSelections);
            }
        });


        addIndividuals.setEnabled(false);

        stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {

                    int rows[] = stp.getTable().getSelectedRows();

                    boolean someSelection = rows.length > 0;

                    addIndividuals.setEnabled(someSelection);

                    if (someSelection) {
                        addIndividuals.setText("Add Selected (" + rows.length + ")");
                    } else {
                        addIndividuals.setText("Add Selected");
                    }
                }
            }
        });

        stp.getTable().getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                addAllIndividuals.setText("Add All (" + stp.getTable().getModel().getRowCount() + ")");
            }
        });

        ActionListener addAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelections(false);
            }
        };

        ActionListener addAllAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelections(true);
            }
        };

        addIndividuals.addActionListener(addAction);
        addAllIndividuals.addActionListener(addAllAction);

        this.pack();
        this.setLocationRelativeTo(MedSavantFrame.getInstance());
    }

    private void setIndividualsChosen(boolean b) {
        this.hasMadeSelections = b;
    }

    private void refreshSelectionIndicator() {

        String tooltipText = "<html>";

        for (String o : this.selectedIndividuals) {
            tooltipText += o + "<br>";
        }
        tooltipText += "</html>";

        this.numselections.setToolTipText(tooltipText);

        stp.setToggledRows(selectedRows);

        numselections.setText(this.selectedIndividuals.size() + " individual(s) selected");

        ok.setEnabled(this.selectedIndividuals.size() > 0);
    }

    private void clearSelections() {
        selectedIndividuals.removeAll(selectedIndividuals);
        selectedRows.removeAll(selectedRows);

        stp.setToggledRows(null);
        refreshSelectionIndicator();
    }

    private void addSelections(boolean all) {

        List<Object[]> selected = new ArrayList<Object[]>();
        int[] rows;

        if (all) {
            int rowCount = stp.getTable().getModel().getRowCount();
            rows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                rows[i] = i;
            }
        } else {
            rows = stp.getTable().getSelectedRows();
        }

        for (int r : rows) {
            int realRow = stp.getActualRowAt(r);
            Object[] o = retriever.getIndividuals().get(realRow);
            selected.add(o);
        }

        for (int i : rows) {
            selectedRows.add(stp.getActualRowAt(i));
        }


        for (Object[] s : selected) {
            selectedIndividuals.add(s[2].toString());
        }

        refreshSelectionIndicator();
    }

    public Set<String> getSelectedIndividuals() {
        return selectedIndividuals;
    }

    public void setSelectedIndividuals(Set<String> s) {
        selectedRows.removeAll(selectedRows);
        selectedIndividuals = s;
        hasMadeSelections = true;
        System.out.println("Forecefully setting selections");

        for (String arbitraryHostpitalID : s) {
            int rowNumber = 0;
            List<Object[]> individuals = retriever.getIndividuals();
            for (Object[] inOrderRow : individuals) {
                if (inOrderRow[2].equals(arbitraryHostpitalID)) {
                    selectedRows.add(rowNumber);
                    System.out.println("\t match at row" + rowNumber);
                }
                rowNumber++;
            }
        }
        refreshSelectionIndicator();
    }

    public boolean hasMadeSelection() {
        return hasMadeSelections;
    }

    public void resetSelections() {
        setSelectedIndividuals(new HashSet<String>());
        this.hasMadeSelections = false;
    }

    public static class IndividualReceiver extends DataRetriever<Object[]> {
        //private DataRetriever<Object[]> getIndividualsRetriever() {

        private List<Object[]> individuals;

        public List<Object[]> getIndividuals() {
            return individuals;
        }

        @Override
        public List<Object[]> retrieve(int start, int limit) throws Exception {
            if (individuals == null) {
                setIndividuals();
            }
            return individuals;
        }

        @Override
        public int getTotalNum() {
            if (individuals == null) {
                try {
                    setIndividuals();
                } catch (Exception ex) {
                    LOG.error(ex);
                }
            }
            return individuals.size();
        }

        @Override
        public void retrievalComplete() {
        }

        private void setIndividuals() throws SQLException, RemoteException {
            try {
                individuals = MedSavantClient.PatientManager.getBasicPatientInfo(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID(), Integer.MAX_VALUE);

            } catch (SessionExpiredException e) {
                MedSavantExceptionHandler.handleSessionExpiredException(e);
            }
        }
    }
}
