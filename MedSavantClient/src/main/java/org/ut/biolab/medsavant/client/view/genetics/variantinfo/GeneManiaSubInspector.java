/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.region.RegionController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.DownloadTask.DownloadState;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GenemaniaInfoRetriever.NoRelatedGenesInfoException;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author khushi
 */
public class GeneManiaSubInspector extends SubInspector implements Listener<Gene> {

    private static final Log LOG = LogFactory.getLog(GeneManiaSubInspector.class);
    private final String name;
    private GenemaniaInfoRetriever genemania;
    private JPanel panel;
    private javax.swing.JProgressBar progressBar;
    private JLabel progressMessage;
    protected KeyValuePairPanel kvp;
    private JPanel kvpPanel;
    private JPanel settingsPanel;
    private JButton settingsButton;
    private Set<Gene> genes;
    //private Gene gene;
    private GeneManiaSettingsDialog genemaniaSettings;
    private boolean rankByVarFreq;
    private Thread genemaniaAlgorithmThread;
    private boolean dataPresent;
    private int currSizeOfArray;
    private JPanel graph;
    private Listener<Object> geneListener;

    public GeneManiaSubInspector() {
        name = "Related Genes";
        dataPresent = true;
    }

    public void setGeneListener(Listener<Object> geneListener) {
        this.geneListener = geneListener;
    }

    @Override
    public String getName() {
        return name;
    }

    private void buildPanel() {
        kvp = new KeyValuePairPanel(2);
        kvp.setKeysVisible(false);
        kvpPanel = new JPanel();
        kvpPanel.setLayout(new BorderLayout());
        kvpPanel.add(kvp, BorderLayout.CENTER);
        JPanel currGenePanel = ViewUtil.getClearPanel();
        JPanel pMessagePanel = ViewUtil.getClearPanel();
        settingsPanel = ViewUtil.getClearPanel();
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressMessage = new JLabel();
        progressMessage.setVisible(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genemaniaSettings.showSettings();
                if (genemaniaSettings.getUpdateQueryNeeded()) {
                    rankByVarFreq = genemaniaSettings.getRankByVarFreq();
                    updateRelatedGenesPanel(genes);
                }
            }
        });

        pMessagePanel.add(progressMessage);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(settingsButton, BorderLayout.EAST);
        graph = ViewUtil.getClearPanel();
        panel.add(kvpPanel);
        panel.add(currGenePanel);
        panel.add(pMessagePanel);
        panel.add(progressBar);


        panel.add(settingsPanel);
        panel.add(graph);
    }

    private void showDownloadingLabel() {
        panel.removeAll();
        panel.add(new JLabel("GeneMANIA Downloading..."));
        panel.revalidate();
        panel.repaint();
    }
    private DownloadTask currentDownloadTask;

    private void registerDownloadListener() {
        try {
            DownloadTask downloadTask = GenemaniaInfoRetriever.getGeneManiaDownloadTask();
            if (currentDownloadTask != downloadTask) {
                currentDownloadTask = downloadTask;

                final DownloadTask dt = downloadTask;
                dt.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("downloadState")) {
                            DownloadState ds = (DownloadState) evt.getNewValue();
                            if (ds == DownloadState.CANCELLED) {
                                panel.removeAll();
                                panel.add(downloadGeneManiaButton);
                                panel.revalidate();
                                panel.repaint();
                            } else if (ds == DownloadState.FINISHED) {
                                //this should always be true.
                                if (DirectorySettings.isGeneManiaInstalled()) {
                                    try {
                                        dataPresent = true;
                                        genemania = new GenemaniaInfoRetriever();
                                        genemaniaSettings = new GeneManiaSettingsDialog(genemania);
                                        panel.removeAll();
                                        buildPanel();
                                        updateRelatedGenesPanel(genes);
                                    } catch (IOException e) {
                                        DialogUtils.displayMessage("Error downloading GeneMANIA files");
                                        LOG.error("Error downloading GeneMANIA files " + e);
                                        dataPresent = false;
                                    }
                                } else {
                                    LOG.error("Error downloading GeneMANIA files.");
                                }
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
        }
    }
    private JButton downloadGeneManiaButton = new JButton("Download GeneMANIA");

    @Override
    public JPanel getInfoPanel() {
        panel = ViewUtil.getClearPanel();
        try {
            if (DirectorySettings.isGeneManiaInstalled()) {
                genemania = new GenemaniaInfoRetriever();
                genemaniaSettings = new GeneManiaSettingsDialog(genemania);
            } else {
                if (GenemaniaInfoRetriever.isGeneManiaDownloading()) {
                    showDownloadingLabel();
                    registerDownloadListener();
                } else {
                    downloadGeneManiaButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                showDownloadingLabel();
                                registerDownloadListener();
                                GenemaniaInfoRetriever.getGeneManiaDownloadTask().execute();

                            } catch (IOException e) {
                                DialogUtils.displayMessage("Error downloading GeneMANIA files " + e);
                                dataPresent = false;
                            }
                        }
                    });
                    panel.add(downloadGeneManiaButton);
                }
                dataPresent = false;
                LOG.debug("Returning panel " + (panel == null));
                return panel;
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }

        buildPanel();
        return panel;
    }

    public void setGenes(Set<Gene> geneSet) {
        if (geneSet != null) {
            if (!dataPresent) {
                genes = geneSet;
            } else {
                LOG.debug("Received genes " + geneSet);
                if (genes == null || !genes.equals(geneSet)) {
                    updateRelatedGenesPanel(geneSet);
                }
            }
        }
    }

    @Override
    public void handleEvent(Gene g) {
        if (g != null) {
            Set gs = new CopyOnWriteArraySet<Gene>();
            gs.add(g);
            setGenes(gs);
        }
    }

    private JPopupMenu getRegionSetsMenu(Gene gene) {
        final JPopupMenu regionSets = new JPopupMenu();
        final RegionController regionController = RegionController.getInstance();
        final Gene g = gene;
        try {
            for (RegionSet s : regionController.getRegionSets()) {
                final RegionSet finalRegionSet = s;
                JMenuItem menuItem = new JMenuItem(s.getName());
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            regionController.addToRegionSet(finalRegionSet, g.getChrom(), g.getStart(), g.getEnd(), g.getName());
                            DialogUtils.displayMessage(String.format("Successfully added %s to %s list", g.getName(), finalRegionSet.getName()));
                        } catch (Exception ex) {
                            ClientMiscUtils.reportError(String.format("Unable to add %s to %s list: %%s", g.getName(), finalRegionSet.getName()), ex);
                        }
                    }
                });
                regionSets.add(menuItem);
            }
        } catch (Exception ex) {
            return new JPopupMenu();
        }
        return regionSets;
    }

    protected JButton[] getRowButtons(final Gene finalGene, final String kvpKey) {
        JButton geneInspectorButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR));
        geneInspectorButton.setToolTipText("Inspect this gene");
        geneInspectorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (geneListener != null) {
                    geneListener.handleEvent(finalGene);
                }
            }
        });

        final JButton addToRegionListButton = ViewUtil.getTexturedButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADDKVP));
        addToRegionListButton.setToolTipText("Add to Region List");
        addToRegionListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                getRegionSetsMenu(finalGene).show(addToRegionListButton, 0, addToRegionListButton.getHeight());
            }
        });

        EntrezButton geneLinkButton = new EntrezButton(finalGene.getName());

        return new JButton[]{geneInspectorButton, addToRegionListButton, geneLinkButton};
    }

    private void addGeneToKeyValuePanel(final Gene finalGene, int i) throws SQLException, RemoteException, InterruptedException {
        if (finalGene != null) {
            final String key = Integer.toString(i);
            kvp.addKey(key);
            JLabel geneName = new JLabel(finalGene.getName());
            kvp.setValue(key, geneName);
            JButton[] buttons = getRowButtons(finalGene, key);
            DecimalFormat df = new DecimalFormat("######.##");
            JLabel vc = new JLabel(df.format(GeneSetFetcher.getInstance().getNormalizedVariantCount(finalGene)));
            // BigDecimal bd = new BigDecimal(Double.toString(GeneSetFetcher.getInstance().getNormalizedVariantCount(finalGene)));
            // bd = bd.setScale(VARIANT_FREQ_DECIMAL_PLACES, RoundingMode.HALF_UP);           
            // JLabel vc = new JLabel(bd.toString());                        
            kvp.setAdditionalColumn(key, 0, vc);
            int colInd = 2;
            for (JButton button : buttons) {
                kvp.setAdditionalColumn(key, colInd++, (JComponent) button);
            }
        }
    }

    protected void updateRelatedGenesPanel(Set<Gene> g) {
        genes = g;
        kvpPanel.removeAll();
        kvpPanel.invalidate();
        kvpPanel.updateUI();
        kvp = new KeyValuePairPanel(5);
        kvp.setKeysVisible(false);
        kvpPanel.add(kvp);
        progressBar.setVisible(true);
        progressMessage.setVisible(true);
        progressBar.setIndeterminate(true);
        progressMessage.setText("Querying GeneMANIA for related genes");

        final Object lock = new Object();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean setMsgOff = true;
                boolean buildGraph = true;
                if (!Thread.interrupted()) {
                    try {
                        List<String> geneNames = new ArrayList();
                        for (Gene gene : genes) {
                            geneNames.add(gene.getName());
                        }
                        List<String> notInGenemania = new ArrayList<String>(geneNames);
                        notInGenemania.removeAll(GenemaniaInfoRetriever.getValidGenes(geneNames));
                        geneNames = GenemaniaInfoRetriever.getValidGenes(geneNames);
                        genemania.setGenes(geneNames);

                        if (notInGenemania.size() > 0) {
                            String message = "<html><center>Following gene(s) not found in GeneMANIA: ";
                            for (String invalidGene : notInGenemania) {
                                message += "<br>" + invalidGene;
                            }
                            message += "</center></html>";
                            progressMessage.setText(message);
                            setMsgOff = false;
                            buildGraph = false;
                        }
                        GeneSetFetcher geneSetFetcher = GeneSetFetcher.getInstance();
                        if (genemania.getGenes().size() > 0) {
                            int i = 1;
                            String zero = Integer.toString(0);
                            Font HEADER_FONT = new Font("Arial", Font.BOLD, 10);
                            kvp.addKey(zero);
                            JLabel geneHeader = new JLabel("Gene".toUpperCase());
                            geneHeader.setFont(HEADER_FONT);
                            kvp.setValue(zero, geneHeader);
                            JLabel varFreqHeader = new JLabel("<html>VARIATION<br>FREQUENCY<br>(var/kb)</html>");
                            varFreqHeader.setFont(HEADER_FONT);
                            kvp.setAdditionalColumn(zero, 0, varFreqHeader);
                            JLabel genemaniaHeader = new JLabel("<html>GENEMANIA<br>SCORE</html>");
                            genemaniaHeader.setFont(HEADER_FONT);

                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }

                            if (rankByVarFreq) {
                                Iterator<org.ut.biolab.medsavant.shared.model.Gene> itr =
                                        geneSetFetcher.getGenesByNumVariants(genemania.getRelatedGeneNamesByScore()).iterator();

                                //skip the first one (it's the name of selected gene already displayed)
                                itr.next();


                                while (itr.hasNext()) {
                                    addGeneToKeyValuePanel(itr.next(), i++);
                                }

                                currSizeOfArray = i - 1;
                            } else {
                                Iterator<String> itr = genemania.getRelatedGeneNamesByScore().iterator();

                                //skip the first one (it's the name of selected gene already displayed)
                                itr.next();

                                List<String> tmp = new LinkedList<String>();
                                while (itr.hasNext()) {
                                    tmp.add(itr.next());
                                }

                                System.out.println("start populating table" + System.currentTimeMillis());

                                /*while (itr.hasNext()) {
                                
                                 //getNormalizedVariantCount(gene)
                                 addGeneToKeyValuePanel(GeneSetFetcher.getInstance().getGene(itr.next()), i++);
                                 }*/

                                for (String foo : tmp) {
                                    addGeneToKeyValuePanel(GeneSetFetcher.getInstance().getGene(foo), i++);
                                }
                                System.out.println("done thread" + System.currentTimeMillis());

                                currSizeOfArray = i - 1;
                            }
                        }

                    } catch (InterruptedException e) {
                        LOG.error(e);
                        buildGraph = false;
                    } catch (NoRelatedGenesInfoException e) {
                        LOG.error(e);
                        progressMessage.setText(e.getMessage());
                        setMsgOff = false;
                        buildGraph = false;
                    } catch (Exception ex) {
                        LOG.error(ex);
                        buildGraph = false;
                        ClientMiscUtils.reportError("Error retrieving data from GeneMANIA: %s", ex);
                    } catch (Error e) {
                        LOG.error(e);
                    } finally {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);

                        if (setMsgOff) {
                            progressMessage.setVisible(false);
                        }
                    }

                }

                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        if (genemaniaAlgorithmThread == null) {
            genemaniaAlgorithmThread = new Thread(r);
        } else {
            genemaniaAlgorithmThread.interrupt();
            genemaniaAlgorithmThread = new Thread(r);

        }

        final Runnable geneDescriptionFetcher = new Runnable() {
            @Override
            public void run() {
                for (int j = 1; j <= currSizeOfArray; j++) {
                    try {
                        String geneName = kvp.getValue(Integer.toString(j));
                        Gene gene = GeneSetFetcher.getInstance().getGene(geneName);
                        String d = gene.getDescription();
                        kvp.setToolTipForValue(Integer.toString(j), d);
                    } catch (Exception e) {
                        //do nothing (don't set tool tip to anything)
                    }
                }
            }
        };
        //}


        genemaniaAlgorithmThread.start();

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        lock.wait();
                        Thread toolTipGenerator = new Thread(geneDescriptionFetcher);
                        Thread varFreqCalculator = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= currSizeOfArray; i++) {
                                    try {
                                        String geneName = kvp.getValue(Integer.toString(i));
                                        Gene gene = GeneSetFetcher.getInstance().getGene(geneName);
                                        kvp.setAdditionalColumn(Integer.toString(i), 0, new JLabel(Double.toString(GeneSetFetcher.getInstance().getNormalizedVariantCount(gene))));
                                        kvp.invalidate();
                                        kvp.updateUI();
                                    } catch (Exception ex) {
                                        //don't put in any variation frequency
                                    }
                                }
                            }
                        });
                        toolTipGenerator.start();
                        varFreqCalculator.start();
                    }
                } catch (Exception e) {
                }
            }
        };

        Thread t2 = new Thread(r2);
        t2.start();
    }
}