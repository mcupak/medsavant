/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

/**
 *
 * @author khushi
 */
public class GeneManiaSettingsSubPanel extends javax.swing.JFrame {

    /**
     * Creates new form GeneManiaSettingsSubPanel
     */
    public GeneManiaSettingsSubPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        limitTo = new javax.swing.JLabel();
        geneLimit = new javax.swing.JTextField();
        relatedGenes = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        rankBy = new javax.swing.JLabel();
        varFreq = new javax.swing.JRadioButton();
        genemaniaScore = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JSeparator();
        networks = new javax.swing.JLabel();
        coexp = new javax.swing.JCheckBox();
        spd = new javax.swing.JCheckBox();
        gi = new javax.swing.JCheckBox();
        coloc = new javax.swing.JCheckBox();
        path = new javax.swing.JCheckBox();
        predict = new javax.swing.JCheckBox();
        pi = new javax.swing.JCheckBox();
        other = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        networkWeighting = new javax.swing.JLabel();
        equal = new javax.swing.JPanel();
        average = new javax.swing.JRadioButton();
        geneOntology = new javax.swing.JPanel();
        bp = new javax.swing.JRadioButton();
        mf = new javax.swing.JRadioButton();
        cc = new javax.swing.JRadioButton();
        queryDependent = new javax.swing.JPanel();
        automatic = new javax.swing.JRadioButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        limitTo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        limitTo.setText("Limit to");

        geneLimit.setColumns(3);
        geneLimit.setText("10");
        geneLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneLimitActionPerformed(evt);
            }
        });

        relatedGenes.setText("related genes");

        rankBy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rankBy.setText("Rank by");

        buttonGroup1.add(varFreq);
        varFreq.setText("Variation Frequency");

        buttonGroup1.add(genemaniaScore);
        genemaniaScore.setText("GeneMANIA Score");

        networks.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        networks.setText("Networks");

        coexp.setText("Co-expression");
        coexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coexpActionPerformed(evt);
            }
        });

        spd.setText("Shared Protein Domains");
        spd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spdActionPerformed(evt);
            }
        });

        gi.setText("Genetic interactions");
        gi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giActionPerformed(evt);
            }
        });

        coloc.setText("Co-localization");
        coloc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colocActionPerformed(evt);
            }
        });

        path.setText("Pathway interactions");

        predict.setText("Predicted");
        predict.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                predictActionPerformed(evt);
            }
        });

        pi.setText("Physical interactions");
        pi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                piActionPerformed(evt);
            }
        });

        other.setText("Other");

        networkWeighting.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        networkWeighting.setText("Network weighting");

        equal.setBackground(new java.awt.Color(255, 255, 255));
        equal.setBorder(javax.swing.BorderFactory.createTitledBorder("Equal weighting"));
        equal.setOpaque(false);

        buttonGroup2.add(average);
        average.setText("Equal by network");

        javax.swing.GroupLayout equalLayout = new javax.swing.GroupLayout(equal);
        equal.setLayout(equalLayout);
        equalLayout.setHorizontalGroup(
            equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(equalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(average)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        equalLayout.setVerticalGroup(
            equalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(average)
        );

        geneOntology.setBackground(new java.awt.Color(255, 255, 255));
        geneOntology.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology (GO)- based weighting"));
        geneOntology.setOpaque(false);

        buttonGroup2.add(bp);
        bp.setText("Biological process based");
        bp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bpActionPerformed(evt);
            }
        });

        buttonGroup2.add(mf);
        mf.setText("Molecular function based");
        mf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mfActionPerformed(evt);
            }
        });

        buttonGroup2.add(cc);
        cc.setText("Cellular component based");

        javax.swing.GroupLayout geneOntologyLayout = new javax.swing.GroupLayout(geneOntology);
        geneOntology.setLayout(geneOntologyLayout);
        geneOntologyLayout.setHorizontalGroup(
            geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneOntologyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bp)
                    .addComponent(mf)
                    .addComponent(cc))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        geneOntologyLayout.setVerticalGroup(
            geneOntologyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneOntologyLayout.createSequentialGroup()
                .addComponent(bp, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cc))
        );

        queryDependent.setBackground(new java.awt.Color(255, 255, 255));
        queryDependent.setBorder(javax.swing.BorderFactory.createTitledBorder("Query-dependent weighting"));
        queryDependent.setOpaque(false);

        buttonGroup2.add(automatic);
        automatic.setText("Automatically selected weighting method");

        javax.swing.GroupLayout queryDependentLayout = new javax.swing.GroupLayout(queryDependent);
        queryDependent.setLayout(queryDependentLayout);
        queryDependentLayout.setHorizontalGroup(
            queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryDependentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(automatic)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        queryDependentLayout.setVerticalGroup(
            queryDependentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(automatic)
        );

        okButton.setText("OK");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator1)
                        .addComponent(queryDependent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(equal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(networkWeighting)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rankBy)
                                    .addComponent(limitTo))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(relatedGenes))
                                    .addComponent(varFreq)
                                    .addComponent(genemaniaScore)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(coexp)
                                    .addComponent(gi)
                                    .addComponent(path)
                                    .addComponent(pi))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(coloc)
                                    .addComponent(spd)
                                    .addComponent(other)
                                    .addComponent(predict)))
                            .addComponent(networks))
                        .addComponent(geneOntology, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator2))
                    .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {genemaniaScore, varFreq});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limitTo)
                    .addComponent(geneLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(relatedGenes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(varFreq)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genemaniaScore)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rankBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(39, 39, 39)))
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(networks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coexp)
                    .addComponent(spd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(gi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(coloc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(path)
                    .addComponent(predict))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pi)
                    .addComponent(other))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(networkWeighting)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(equal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(geneOntology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryDependent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void geneLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneLimitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_geneLimitActionPerformed

    private void coexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coexpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_coexpActionPerformed

    private void spdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_spdActionPerformed

    private void giActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_giActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_giActionPerformed

    private void colocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_colocActionPerformed

    private void piActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_piActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_piActionPerformed

    private void bpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bpActionPerformed

    private void mfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mfActionPerformed

    private void predictActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_predictActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_predictActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton automatic;
    private javax.swing.JRadioButton average;
    private javax.swing.JRadioButton bp;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton cc;
    private javax.swing.JCheckBox coexp;
    private javax.swing.JCheckBox coloc;
    private javax.swing.JPanel equal;
    private javax.swing.JTextField geneLimit;
    private javax.swing.JPanel geneOntology;
    private javax.swing.JRadioButton genemaniaScore;
    private javax.swing.JCheckBox gi;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel limitTo;
    private javax.swing.JRadioButton mf;
    private javax.swing.JLabel networkWeighting;
    private javax.swing.JLabel networks;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox other;
    private javax.swing.JCheckBox path;
    private javax.swing.JCheckBox pi;
    private javax.swing.JCheckBox predict;
    private javax.swing.JPanel queryDependent;
    private javax.swing.JLabel rankBy;
    private javax.swing.JLabel relatedGenes;
    private javax.swing.JCheckBox spd;
    private javax.swing.JRadioButton varFreq;
    // End of variables declaration//GEN-END:variables
}
