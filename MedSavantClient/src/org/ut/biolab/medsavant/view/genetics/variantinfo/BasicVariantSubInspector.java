/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.ViewController;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.InspectorPanel;
import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspector;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.variants.BrowserPage;
import savant.api.data.DataFormat;
import savant.controller.LocationController;
import savant.controller.TrackController;
import savant.util.Range;
import savant.view.swing.Savant;

/**
 *
 * @author mfiume
 */
public class BasicVariantSubInspector extends SubInspector implements VariantSelectionChangedListener {

    private static String KEY_DNAID = "DNA ID";
    private static String KEY_POSITION = "Position";
    private static String KEY_GENES = "Genes";
    private static String KEY_REF = "Reference";
    private static String KEY_ALT = "Alternate";
    private static String KEY_QUAL = "Quality";
    private static String KEY_DBSNP = "dbSNP ID";
    private static String KEY_TYPE = "Type";
    private static String KEY_ZYGOSITY = "Zygosity";
    private static String KEY_INFO = "Info";
    private Collection<Gene> genes;
    private KeyValuePairPanel p;
    private JComboBox geneBox;
    private VariantRecord selectedVariant;

    public BasicVariantSubInspector() {
        VariantInspector.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Variant Details";
    }
    static String charset = "UTF-8";

    @Override
    public JPanel getInfoPanel() {
        if (p == null) {
            p = new KeyValuePairPanel(4);
            p.addKey(KEY_DNAID);
            p.addKey(KEY_POSITION);
            p.addKey(KEY_REF);
            p.addKey(KEY_ALT);
            p.addKey(KEY_TYPE);
            p.addKey(KEY_ZYGOSITY);
            p.addKey(KEY_QUAL);
            p.addKey(KEY_DBSNP);

            p.addKey(KEY_GENES);
            p.addKey(KEY_INFO);

            geneBox = new JComboBox();
            ViewUtil.makeSmall(geneBox);
            int geneDropdownWidth = 130;
            geneBox.setMinimumSize(new Dimension(geneDropdownWidth, 30));
            geneBox.setPreferredSize(new Dimension(geneDropdownWidth, 30));
            geneBox.setMaximumSize(new Dimension(geneDropdownWidth, 30));
            p.setValue(KEY_GENES, geneBox);

            JButton genomeBrowserButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BROWSER));
            genomeBrowserButton.setToolTipText("View region in genome browser");
            genomeBrowserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocationController.getInstance().setLocation(selectedVariant.getChrom(), new Range((int) (selectedVariant.getPosition() - 20), (int) (selectedVariant.getPosition() + 21)));
                    ViewController.getInstance().getMenu().switchToSubSection(BrowserPage.getInstance());
                }
            });

            JButton bamButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.BAMFILE));
            bamButton.setToolTipText("<html>Load read alignments for this<br/> sample in genome browser</html>");
            bamButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {

                    String dnaID = selectedVariant.getDnaID();
                    String bamPath;
                    try {
                        bamPath = MedSavantClient.PatientManager.getReadAlignmentPathForDNAID(
                                LoginController.sessionId,
                                ProjectController.getInstance().getCurrentProjectID(),
                                dnaID);
                        if (bamPath != null && !bamPath.equals("")) {
                            /*int response = DialogUtils.askYesNo("Load Read Alignments",
                             "<html>The read alignments for this sample<br>"
                             + "are available. Would you like to load them<br>"
                             + "as a track in the genome browser?</html>");*/
                            int response = DialogUtils.YES;
                            if (response == DialogUtils.YES) {
                                BrowserPage.addTrackFromURLString(bamPath, DataFormat.ALIGNMENT);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(BasicVariantSubInspector.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
            geneInspectorButton.setToolTipText("Inspect this gene");
            geneInspectorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    GeneInspector.getInstance().setGene((Gene) (geneBox).getSelectedItem());
                    InspectorPanel.getInstance().switchToGeneInspector();
                }
            });

            p.setAdditionalColumn(KEY_GENES, 0, geneInspectorButton);

            JLabel l = new JLabel("This will eventually show a chart");
            p.setDetailComponent(KEY_QUAL, l);

            final JToggleButton button = ViewUtil.getTexturedToggleButton("SHOW");
            ViewUtil.makeSmall(button);
            button.setToolTipText("Toggle Info");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    p.toggleDetailVisibility(KEY_INFO);
                    button.setText(button.isSelected() ? "HIDE" : "SHOW");
                }
            });
            p.setValue(KEY_INFO, button);


            int col = 0;

            p.setAdditionalColumn(KEY_DNAID, col, KeyValuePairPanel.getCopyButton(KEY_DNAID,p));
            p.setAdditionalColumn(KEY_DBSNP, col, KeyValuePairPanel.getCopyButton(KEY_DBSNP,p));
            p.setAdditionalColumn(KEY_POSITION, col, KeyValuePairPanel.getCopyButton(KEY_POSITION,p));
            p.setAdditionalColumn(KEY_QUAL, col, getChartButton(KEY_QUAL));

            col++;
            p.setAdditionalColumn(KEY_DBSNP, col, getNCBIButton(KEY_DBSNP));
            p.setAdditionalColumn(KEY_POSITION, col, genomeBrowserButton);
            p.setAdditionalColumn(KEY_DNAID, col, bamButton);

        }
        return p;
    }

    public boolean showHeader() {
        return false;
    }

    private String checkNull(Object o) {
        if (o == null) {
            return KeyValuePairPanel.NULL_VALUE;
        }
        String s = o.toString();
        if (s.equals("")) {
            return KeyValuePairPanel.NULL_VALUE;
        }

        return s;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        try {
            if (p == null) {
                return;
            }
            if (r == null) {
                // TODO show other card
                return;
            }

            selectedVariant = r;

            p.setValue(KEY_DNAID, r.getDnaID());
            p.setValue(KEY_POSITION, r.getChrom() + ":"  + ViewUtil.numToString(r.getPosition()));
            p.setValue(KEY_REF, r.getRef());
            p.setValue(KEY_ALT, r.getAlt());

            p.setValue(KEY_TYPE, checkNull(r.getType()));
            p.setValue(KEY_ZYGOSITY, checkNull(r.getZygosity()));

            p.setValue(KEY_QUAL, ViewUtil.numToString(r.getQual()));
            p.setValue(KEY_DBSNP, checkNull(r.getDbSNPID()));

            p.setDetailComponent(KEY_INFO, getInfoKVPPanel(r.getCustomInfo()));

            String bamPath = MedSavantClient.PatientManager.getReadAlignmentPathForDNAID(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectID(),
                    r.getDnaID());

            JButton bamButton = (JButton) p.getAdditionalColumn(KEY_DNAID, 1).getComponent(0);
            if (bamPath != null && !bamPath.equals("")) {
                bamButton.setVisible(true);
            } else {
                bamButton.setVisible(false);
            }

        } catch (Exception ex) {
            Logger.getLogger(BasicVariantSubInspector.class.getName()).log(Level.SEVERE, null, ex);
        }


        generateGeneIntersections(r);



    }

    private void generateGeneIntersections(VariantRecord r) {
        try {

            if (genes == null) {
                genes = GeneSetController.getInstance().getCurrentGenes();
            }

            Gene g0 = null;
            JComboBox b = geneBox;
            b.removeAllItems();

            for (Gene g : genes) {
                if (g0 == null) {
                    g0 = g;
                }
                if (g.getChrom().equals(r.getChrom()) && r.getPosition() > g.getStart() && r.getPosition() < g.getEnd()) {
                    b.addItem(g);
                }
            }

            /*if (g0 != null) {
             GeneInspector.getInstance().setGene(g0);
             }
             */
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching genes: %s", ex);
        }
    }



    private Component getFilterButton(final String key) {

        JButton button = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER));
        button.setToolTipText("Filter " + key);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
            }
        });
        return button;
    }

    private Component getChartButton(final String key) {
        final JToggleButton button = ViewUtil.getTexturedToggleButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART_SMALL));
        button.setToolTipText("Chart " + key);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                p.toggleDetailVisibility(key);
            }
        });
        return button;
    }

    private Component getNCBIButton(final String key) {
        JButton ncbiButton = ViewUtil.getTexturedButton("", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));

        //LinkButton ncbiButton = new LinkButton("NCBI");
        ncbiButton.setToolTipText("Lookup " + key + " at NCBI");
        ncbiButton.addActionListener(new ActionListener() {
            String baseUrl = "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?searchType=adhoc_search&rs=";

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URL url = new URL(baseUrl + URLEncoder.encode(p.getValue(key), charset));
                    java.awt.Desktop.getDesktop().browse(url.toURI());
                } catch (Exception ex) {
                    DialogUtils.displayError("Problem launching website.");
                }
            }
        });

        return ncbiButton;
    }

    private KeyValuePairPanel getInfoKVPPanel(String customInfo) {

        String[] pairs = customInfo.split(";");

        KeyValuePairPanel kvp = new KeyValuePairPanel();

        for (String pair : pairs) {
            String[] splitPair = pair.split("=");

            if (splitPair.length == 2) {
                kvp.addKey(splitPair[0]);
                kvp.setValue(splitPair[0], splitPair[1]);
            }

        }

        return kvp;
    }
}