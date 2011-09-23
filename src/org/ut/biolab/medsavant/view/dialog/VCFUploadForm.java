/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.dialog;

import org.ut.biolab.medsavant.vcf.VCFParser;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.ut.biolab.medsavant.db.util.ImportVariantSet;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author AndrewBrook
 */
public class VCFUploadForm extends javax.swing.JDialog {

    private int projectId = 20;
    private int referenceId = 20;
    
    
    private String path;
    private File[] files;

    /** Creates new form VCFUploadForm */
    public VCFUploadForm() {
        
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        initComponents();          
             
        //TODO:
        //projectLabel.setText(ProjectController.getInstance().getCurrentProjectName());
        //referenceLabel.setText(ProjectController.getInstance().getCurrentReferenceName());
        projectLabel.setText("TODO (default 1)");
        referenceLabel.setText("TODO (default 1)");
        
        uploadButton.setEnabled(false);
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);                
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        outputFileField = new javax.swing.JTextField();
        chooseFileButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        progressLabel = new javax.swing.JLabel();
        referenceLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add VCF File to Database");

        jLabel1.setText("Project: ");

        jLabel2.setText("Reference: ");

        jLabel3.setText("File(s) to Upload: ");

        outputFileField.setEditable(false);
        outputFileField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileFieldActionPerformed(evt);
            }
        });

        chooseFileButton.setText("...");
        chooseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButtonActionPerformed(evt);
            }
        });

        uploadButton.setText("Upload");
        uploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadButtonActionPerformed(evt);
            }
        });

        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        progressLabel.setText(" ");

        referenceLabel.setText("jLabel4");

        projectLabel.setText("jLabel5");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(referenceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                            .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(outputFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(projectLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(referenceLabel))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseFileButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uploadButton)
                    .addComponent(progressLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void outputFileFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_outputFileFieldActionPerformed

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Variants");
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.addChoosableFileFilter(new ExtensionFileFilter("vcf"));
        fc.setMultiSelectionEnabled(true);
        
        int result = fc.showDialog(null, null);
        if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
            return;
        }
        
        files = fc.getSelectedFiles();
        path = getPathString(files);
        outputFileField.setText(path);
        uploadButton.setEnabled(true);
    }//GEN-LAST:event_chooseFileButtonActionPerformed

    private String getPathString(File[] files) {
        if (files.length > 1) {
            return files.length + " files";
        } else if (files.length == 1) {
            return files[0].getAbsolutePath();
        } else {
            return "";
        }
    }
    
    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        
        final JDialog instance = this;
        this.setVisible(false);
     
        final JDialog dialog = new JDialog();
        dialog.setTitle("Import VCF");
        final JLabel progressLabel = new JLabel("Beginning import of VCF files. ");
        progressLabel.setHorizontalTextPosition(JLabel.CENTER);
        progressLabel.setHorizontalAlignment(JLabel.CENTER);
        progressLabel.setMinimumSize(new Dimension(300,70));
        progressLabel.setPreferredSize(new Dimension(300,70));
        progressLabel.setFont(ViewUtil.getMediumTitleFont());
        dialog.setContentPane(progressLabel);
        dialog.setDefaultCloseOperation(
            JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(200,50));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
           
        Thread thread = new Thread() {
            @Override
            public void run() {
                int currentfile = 0;
                int totalnumfiles = files.length;
                for (File f : files) {
                    currentfile++;
                    File outfile = new File("temp_tdf"); //TODO: we should have a temporary file directory or something
                    
                    String progress = "Importing file " + currentfile + " of " + totalnumfiles;
                    progressLabel.setText(progress);
                    System.out.println(progress);
                    try {
                        VCFParser.parseVariants(f, outfile);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(VCFUploadForm.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(VCFUploadForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        ImportVariantSet.performImport(outfile, projectId, referenceId);
                    } catch (SQLException ex) {
                        Logger.getLogger(VCFUploadForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.gc();                   
                }
                instance.dispose();
                dialog.dispose();            
            }
        };
        thread.start(); 
    }//GEN-LAST:event_uploadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseFileButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField outputFileField;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JLabel referenceLabel;
    private javax.swing.JButton uploadButton;
    // End of variables declaration//GEN-END:variables
}
