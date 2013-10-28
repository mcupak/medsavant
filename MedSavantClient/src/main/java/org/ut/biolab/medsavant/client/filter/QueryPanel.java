/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import org.ut.biolab.medsavant.client.cohort.CohortFilterView;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.region.RegionSetFilterView;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * Panel which holds together a group of related filters which are ANDed
 * together to form a single query. Multiple
 * <code>QueryPanel</code> can be ORed together.
 *
 * @author mfiume
 */
public class QueryPanel extends CollapsiblePanes {

    static final Color INACTIVE_KEY_COLOR = Color.GRAY;
    static final Color ACTIVE_KEY_COLOR = new Color(72, 181, 249); // Color.red;
    private int queryID;
    private Map<String, FilterHolder> filterHolders = new TreeMap<String, FilterHolder>();

    public QueryPanel(int queryID) {
        this.queryID = queryID;

        setOpaque(false);
        setBorder(null);

        CollapsiblePane p1 = new CollapsiblePane("Search Conditions");
        p1.setBorder(null);
        p1.setStyle(CollapsiblePane.TREE_STYLE);
        p1.setCollapsible(false);

        ViewUtil.applyVerticalBoxLayout(p1.getContentPane());
        p1.add(populateFilterList());

        add(p1);
        addExpansion();

        p1.repaint();

    }

    public Collection<FilterHolder> getFilterHolders() {
        return filterHolders.values();
    }

    private boolean isFilterable(ColumnType type) {
        switch (type) {
            case INTEGER:
            case FLOAT:
            case DECIMAL:
            case BOOLEAN:
            case VARCHAR:
                return true;
            default:
                return false;
        }
    }

    private JPanel populateFilterList() {
        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);
        ViewUtil.applyVerticalBoxLayout(panes);
        panes.setBorder(null);

        try {
            // Cohort filter
            List<FilterHolder> catHolders = new ArrayList<FilterHolder>();
            catHolders.add(new SimpleFilterHolder(CohortFilterView.class, queryID));

            // Add from patient table
            for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
                if (field.isFilterable() && isFilterable(field.getColumnType())) {
                    catHolders.add(new FieldFilterHolder(field, WhichTable.PATIENT, queryID));
                }
            }
            panes.add(addFilterCategory("Conditions on Individuals", catHolders, false), BorderLayout.CENTER);

            // Add from variant table
            AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
            for (AnnotationFormat af : afs) {
                for (CustomField field : af.getCustomFields()) {
                    if (field.isFilterable() && isFilterable(field.getColumnType())) {
                        catHolders.add(new FieldFilterHolder(field, WhichTable.VARIANT, queryID));
                    }
                }

                // force Tag filter into default variant conditions
                if (af.getProgram().equals(AnnotationFormat.ANNOTATION_FORMAT_DEFAULT)) {
                    //tag filter
                    catHolders.add(new SimpleFilterHolder(TagFilterView.class, queryID));
                }

                String name = af.getProgram();
                if (!name.toLowerCase().contains("conditions")) {
                    name = name + " Conditions";
                }
                panes.add(addFilterCategory(ViewUtil.ellipsize(name, 40), catHolders, false), BorderLayout.CENTER);
            }

            // Ontology filters
            catHolders.add(new OntologyFilterHolder(OntologyType.GO, queryID));
            catHolders.add(new OntologyFilterHolder(OntologyType.HPO, queryID));
            catHolders.add(new OntologyFilterHolder(OntologyType.OMIM, queryID));

            // Region list filter
            catHolders.add(new SimpleFilterHolder(RegionSetFilterView.class, queryID));
            panes.add(addFilterCategory("Ontology and Region Conditions", catHolders, true), BorderLayout.CENTER);

            panes.add(addFilterCategory("Plugin Conditions", catHolders, false));


        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to load search conditions: %s", ex);
        }

        return panes;
    }

    private CollapsiblePane addFilterCategory(String title, List<FilterHolder> catHolders, boolean longRunning) throws PropertyVetoException {

        CollapsiblePane cPane = new CollapsiblePane(title);
        cPane.setCollapsed(true);
        cPane.setOpaque(false);
        cPane.setLayout(new BorderLayout());

        cPane.setStyle(CollapsiblePane.PLAIN_STYLE);

        KeyValuePairPanel kvp = new KeyValuePairPanel(2);
        cPane.add(kvp, BorderLayout.CENTER);

        for (FilterHolder f : catHolders) {
            f.addTo(kvp, longRunning);
        }

        FilterController.getInstance().addListener(new FilterEventListener(cPane, kvp));
        for (FilterHolder h : catHolders) {
            filterHolders.put(h.getFilterID(), h);
        }
        catHolders.clear();

        return cPane;
    }

    /**
     * Given state loaded in from a saved file, find the correct filter holder
     * and load in the given info.
     */
    public void loadFilterView(FilterState state) throws Exception {
        FilterHolder h = filterHolders.get(state.getFilterID());
        if (h != null) {
            h.loadFilterView(state);
            h.openFilterView();
        } else {
            throw new Exception(String.format("Unknown filter ID \"%s\"", state.getFilterID()));
        }
    }

    private class FilterEventListener implements Listener<FilterEvent> {

        private final String baseTitle;
        private final CollapsiblePane collapsiblePane;
        private final KeyValuePairPanel keyValuePanel;

        private FilterEventListener(CollapsiblePane cPane, KeyValuePairPanel kvp) {
            collapsiblePane = cPane;
            keyValuePanel = kvp;
            baseTitle = cPane.getTitle();
        }

        @Override
        public void handleEvent(FilterEvent event) {
            Filter changedFilter = event.getFilter();

            if (keyValuePanel.containsKey(changedFilter.getName())) {
                JComponent xButton = keyValuePanel.getAdditionalColumn(changedFilter.getName(), 0);
                switch (event.getType()) {
                    case ADDED:
                    case MODIFIED:
                        keyValuePanel.setKeyColour(changedFilter.getName(), ACTIVE_KEY_COLOR);
                        collapsiblePane.setTitle(String.format("<html><font color=\"#48B5F9\">%s</font></html>", baseTitle));   // Assumes that ACTIVE_KEY_COLOR is in fact red.
                        xButton.setVisible(true);
                        break;
                    case REMOVED:
                        keyValuePanel.setKeyColour(changedFilter.getName(), INACTIVE_KEY_COLOR);
                        collapsiblePane.setTitle(baseTitle);
                        xButton.setVisible(false);
                        break;
                }
            }
        }
    }
}
