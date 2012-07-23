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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.VariantTag;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class TagFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(TagFilterView.class);
    public static final String FILTER_NAME = "Tag Filter";
    public static final String FILTER_ID = "tag_filter";

    private List<VariantTag> variantTags;
    private List<VariantTag> appliedTags;
    private ActionListener al;
    private JTextArea ta;
    private final JButton clear;
    private final JButton applyButton;

    public TagFilterView(FilterState state, int queryID) {
        this(queryID);
        if (state.getValues().get("variantTags") != null) {
            applyFilter(stringToVariantTags(state.getValues().get("variantTags")));
        }
    }

    public TagFilterView(int queryID) {
        super(FILTER_NAME, queryID);

        setLayout(new BorderLayout());
        setBorder(ViewUtil.getBigBorder());
        setMaximumSize(new Dimension(200, 80));

        JPanel cp = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0; c.gridy = 0;

        cp.setLayout(gbl);

        /*
         * JPanel content1 = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(content1);
        JPanel content2 = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(content2);
        *
        */

        variantTags = new ArrayList<VariantTag>();
        appliedTags = new ArrayList<VariantTag>();

        clear = new JButton("Clear");
        applyButton = new JButton("Apply");

        try {
            final JComboBox tagNameCB = new JComboBox();
            //tagNameCB.setMaximumSize(new Dimension(1000,30));

            final JComboBox tagValueCB = new JComboBox();
            //tagValueCB.setMaximumSize(new Dimension(1000,30));

            JPanel bottomContainer = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(bottomContainer);

            List<String> tagNames = MedSavantClient.VariantManager.getDistinctTagNames(LoginController.sessionId);

            for (String tag : tagNames) {
                tagNameCB.addItem(tag);
            }

            ta = new JTextArea();
            ta.setRows(10);
            ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            ta.setEditable(false);


            applyButton.setEnabled(false);

            JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
            addButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (tagNameCB.getSelectedItem() == null || tagValueCB.getSelectedItem() == null) {
                        return;
                    }

                    VariantTag tag = new VariantTag((String) tagNameCB.getSelectedItem(), (String) tagValueCB.getSelectedItem());
                    if (variantTags.isEmpty()) {
                        ta.append(tag.toString() + "\n");
                    } else {
                        ta.append("AND " + tag.toString() + "\n");
                    }
                    variantTags.add(tag);
                    applyButton.setEnabled(true);
                }
            });

            clear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    variantTags.clear();
                    ta.setText("");
                    applyButton.setEnabled(true);
                }
            });

            int width = 150;

            ta.setPreferredSize(new Dimension(width,width));
            ta.setMaximumSize(new Dimension(width,width));
            tagNameCB.setPreferredSize(new Dimension(width,25));
            tagValueCB.setPreferredSize(new Dimension(width,25));
            tagNameCB.setMaximumSize(new Dimension(width,25));
            tagValueCB.setMaximumSize(new Dimension(width,25));

            cp.add(new JLabel("Name"), c);
            c.gridx++;
            cp.add(tagNameCB,c);
            c.gridx++;

            c.gridx = 0;
            c.gridy++;

            cp.add(new JLabel("Value"),c);
            c.gridx++;
            cp.add(tagValueCB,c);
            c.gridx++;
            cp.add(addButton,c);

            tagNameCB.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent ie) {
                    updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
                }
            });

            if (tagNameCB.getItemCount() > 0) {
                tagNameCB.setSelectedIndex(0);
                updateTagValues((String) tagNameCB.getSelectedItem(), tagValueCB);
            }

            al = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    applyButton.setEnabled(false);

                    appliedTags = new ArrayList<VariantTag>(variantTags);

                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            try {
                                List<Integer> uploadIDs = MedSavantClient.VariantManager.getUploadIDsMatchingVariantTags(LoginController.sessionId, TagFilterView.tagsToStringArray(variantTags));

                                Condition[] uploadIDConditions = new Condition[uploadIDs.size()];

                                TableSchema table = MedSavantClient.CustomTablesManager.getCustomTableSchema(LoginController.sessionId, MedSavantClient.ProjectManager.getVariantTableName(
                                         LoginController.sessionId,
                                         ProjectController.getInstance().getCurrentProjectID(),
                                         ReferenceController.getInstance().getCurrentReferenceID(),
                                         true));

                                for (int i = 0; i < uploadIDs.size(); i++) {
                                    uploadIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadIDs.get(i));
                                }

                                return new Condition[] {ComboCondition.or(uploadIDConditions) };
                            } catch (Exception ex) {
                                ClientMiscUtils.reportError("Error getting upload IDs: %s", ex);
                            }
                            return new Condition[0];
                        }

                        @Override
                        public String getName() {
                            return FILTER_NAME;
                        }

                        @Override
                        public String getID() {
                            return FILTER_ID;
                        }
                    };
                    FilterController.addFilter(f, getQueryID());
                }
                };

            applyButton.addActionListener(al);

            bottomContainer.add(Box.createHorizontalGlue());
            bottomContainer.add(clear);
            bottomContainer.add(applyButton);


            add(ViewUtil.getClearBorderedScrollPane(ta), BorderLayout.CENTER);
            add(bottomContainer,BorderLayout.SOUTH);

        } catch (Exception ex) {
            ClientMiscUtils.checkSQLException(ex);
        }

        add(cp, BorderLayout.NORTH);
    }

    public final void applyFilter(List<VariantTag> tags) {
        this.variantTags = tags;
        ta.setText("");
        for (int i = 0; i < variantTags.size(); i++) {
            VariantTag tag = variantTags.get(i);
            if (i == 0) {
                ta.append(tag.toString() + "\n");
            } else {
                ta.append("AND " + tag.toString() + "\n");
            }
        }
        al.actionPerformed(null);
    }

    private static void updateTagValues(String tagName, JComboBox tagValueCB) {

        tagValueCB.removeAllItems();

        if (tagName != null) {
            List<String> values;
            try {
                values = MedSavantClient.VariantManager.getValuesForTagName(LoginController.sessionId, tagName);
                for (String val : values) {
                    tagValueCB.addItem(val);
                }
            } catch (Exception ex) {
                ClientMiscUtils.reportError("Error updating tag values: %s", ex);
            }
        }

        tagValueCB.updateUI();
    }

    private static String[][] tagsToStringArray(List<VariantTag> variantTags) {

        String[][] result = new String[variantTags.size()][2];

        int row = 0;
        for (VariantTag t : variantTags) {
            result[row][0] = t.key;
            result[row][1] = t.value;
            row++;
        }

        return result;
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("variantTags", variantTagsToString(appliedTags));
        return new FilterState(Filter.Type.TAG, FILTER_NAME, FILTER_ID, map);
    }

    private String variantTagsToString(List<VariantTag> tags) {
        String s = "";
        for (VariantTag tag : tags) {
            s += tag.key + ":::" + tag.value + ";;;";
        }
        return s;
    }

    private List<VariantTag> stringToVariantTags(String s) {
        List<VariantTag> list = new ArrayList<VariantTag>();
        String[] pairs = s.split(";;;");
        for (String x : pairs) {
            String[] pair = x.split(":::");
            if (pair.length == 2) {
                list.add(new VariantTag(pair[0], pair[1]));
            }
        }
        return list;
    }


    @Override
    public void resetView() {

        clear.doClick();
        ta.setText("");
        applyButton.setEnabled(false);
    }
}