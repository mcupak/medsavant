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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.reference.ReferenceListener;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 * @author AndrewBrook
 */
public class FilterHistoryPanel extends JPanel implements ReferenceListener, FiltersChangedListener {

    private static final Log LOG = LogFactory.getLog(FilterHistoryPanel.class);

    private int maxRecords = 0;
    private JTable table;
    private ProgressTableModel model;
    private Mode mode = Mode.GLOBAL;

    @Override
    public void referenceAdded(String name) {
    }

    @Override
    public void referenceRemoved(String name) {
    }

    @Override
    public void referenceChanged(String name) {
        this.reset();
    }

    @Override
    public void filtersChanged() {
        //final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
        //        "Applying Filter",
        //        "Filter is being applied. Please wait.",
        //        true);

        final Filter filter = FilterController.getLastFilter();
        final String action = FilterController.getLastActionString();

        new MedSavantWorker<Void>("FilterHistoryPanel") {

            @Override
            protected void showProgress(double fraction) {
            }

            @Override
            protected void showSuccess(Void result) {
            }

            @Override
            protected Void doInBackground() throws RemoteException, SQLException {
                int numLeft = ResultController.getInstance().getNumFilteredVariants();
                addFilterSet(filter, action, numLeft);
                return null;
            }
            
        }.execute();
    }
    private enum Mode {GLOBAL, RELATIVE};

    private Color TOTAL_COLOR = new Color(225,244,254);
    private Color REMAINING_COLOR = new Color(72,181,249);
    private Color PREVIOUS_COLOR = new Color(179,255,217);
    private Color NEW_COLOR = new Color(0,153,77);

    public FilterHistoryPanel() {

        ReferenceController.getInstance().addReferenceListener(this);
        FilterController.addFilterListener(this);

        this.setBackground(new Color(100,100,100));
        this.setBorder(ViewUtil.getMediumBorder());
        
        this.setPreferredSize(new Dimension(200,300));
        this.setName("History");
        this.setLayout(new BorderLayout());
        table = new JTable() {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (model.getColumnClass(column).equals(JPanel.class)) {
                    return new JPanelRenderer();
                }
                return super.getCellRenderer(row, column);
            }
        };
        model = new ProgressTableModel();
        table.setModel(model);

        JPanel modePanel = ViewUtil.getClearPanel();

        ButtonGroup group = new ButtonGroup();
        JRadioButton globalButton = new JRadioButton("Global");
        ViewUtil.makeSmall(globalButton);
        globalButton.setForeground(Color.white);
        globalButton.setSelected(true);
        globalButton.setOpaque(false);
        JRadioButton relativeButton = new JRadioButton("Relative to previous change");
        ViewUtil.makeSmall(relativeButton);
        relativeButton.setForeground(Color.white);
        relativeButton.setOpaque(false);
        globalButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                changeMode(Mode.GLOBAL);
            }
        });
        relativeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                changeMode(Mode.RELATIVE);
            }
        });
        group.add(globalButton);
        group.add(relativeButton);
        modePanel.add(globalButton);
        modePanel.add(relativeButton);
        this.add(modePanel, BorderLayout.NORTH);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(table);
        this.add(scrollPane, BorderLayout.CENTER);

        reset();

        //FilterController.addFilterListener(this);

    }

    public final void reset() {
        model.clear();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    maxRecords = ResultController.getInstance().getNumFilteredVariants();
                } catch (Exception ex) {
                    LOG.error("Error getting num filtered variants.", ex);
                }
                if (maxRecords != -1) {
                    model.addRow("Total", "", maxRecords);
                }
                table.repaint();
            }
        };
        t.start();
    }

    private synchronized void changeMode(Mode mode) {
        if (this.mode == mode) return;
        this.mode = mode;
        model.setMode(mode);
        table.getColumnModel().getColumn(3).setHeaderValue(model.getColumnName(3));
        table.getTableHeader().resizeAndRepaint();
        table.repaint();
    }

    private void addFilterSet(Filter filter, String action, int numLeft) {
        model.addRow(filter.getName(), action, numLeft);
        table.updateUI();
        this.repaint();

    }

    private class ProgressTableModel extends AbstractTableModel {

        private Mode mode = Mode.GLOBAL;

        private String[] globalColumnNames = {"Filter Name", "Action", "Records", "% of Total"};
        private String[] relativeColumnNames = {"Filter Name", "Action", "Records", "Change"};
        private Class[] columnClasses = {String.class, String.class, Integer.class, JPanel.class};

        List<String> names = new ArrayList<String>();
        List<String> actions = new ArrayList<String>();
        List<Integer> records = new ArrayList<Integer>();
        //List<String> parameters = new ArrayList<String>();

        public void addRow(String name, String action, int numRecords) {
            names.add(name);
            actions.add(action);
            records.add(numRecords);
            //parameters.add(param);
        }


        public void clear() {
            names.clear();
            actions.clear();
            records.clear();
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        @Override
        public String getColumnName(int column) {
            if (mode == Mode.GLOBAL) {
                return globalColumnNames[column];
            } else {
                return relativeColumnNames[column];
            }
        }

        @Override
        public int getRowCount() {
            return names.size();
        }

        @Override
        public int getColumnCount() {
            if (mode == Mode.GLOBAL) {
                return globalColumnNames.length;
            } else {
                return relativeColumnNames.length;
            }
        }

        @Override
        public Class getColumnClass(int column) {
            return columnClasses[column];
         }

        @Override
        public Object getValueAt(final int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return names.get(rowIndex);
                case 1:
                    return actions.get(rowIndex);
                case 2:
                    return records.get(rowIndex);
                case 3:
                    final JPanel p;
                    if (mode == Mode.GLOBAL) {
                        p = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Dimension dim = getSize();
                                g.setColor(REMAINING_COLOR);
                                g.fillRect(0,0,(int)(dim.width * ((double)records.get(rowIndex) / (double)records.get(0))), dim.height);
                            }
                        };
                        p.setBackground(TOTAL_COLOR);
                    } else {
                        p = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                if (rowIndex == 0) return;
                                Dimension dim = getSize();
                                double ratio = (double)records.get(rowIndex) / (double)records.get(rowIndex - 1);
                                if (ratio > 1) {
                                    ratio = 1.0 / ratio;
                                    g.setColor(NEW_COLOR);
                                    g.fillRect(0, 0, dim.width, dim.height);
                                    g.setColor(PREVIOUS_COLOR);
                                    g.fillRect(0, 0, (int)(dim.width * ratio), dim.height);
                                } else {
                                    g.setColor(PREVIOUS_COLOR);
                                    g.fillRect(0, 0, dim.width, dim.height);
                                    g.setColor(NEW_COLOR);
                                    g.fillRect(0, 0, (int)(dim.width * ratio), dim.height);
                                }
                            }
                        };
                    }
                    return p;
                default:
                    return null;
            }
        }
    }

    private class JPanelRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component)value;
        }

    }

}
