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

package org.ut.biolab.medsavant.view.manage;

import java.awt.Dialog;
import javax.swing.JDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.settings.VersionSettings;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 * Dialog for adding and removing databases.  Actually, the real purpose of this dialog is just to retrieve the admin username and
 * password.
 *
 * @author mfiume
 */
public class AddRemoveDatabaseDialog extends JDialog {
    private static final Log LOG = LogFactory.getLog(AddRemoveDatabaseDialog.class);
    private final boolean removing;

    public AddRemoveDatabaseDialog(String host, String port, String dbName, boolean removing) {
        super(DialogUtils.getFrontWindow(), Dialog.ModalityType.APPLICATION_MODAL);
        this.removing = removing;
        initComponents();

        setResizable(false);

        setLocationRelativeTo(getParent());
        hostField.setText(host);
        portField.setText(port);
        databaseField.setText(dbName);

        databaseField.requestFocus();

        if (removing) {
            okButton.setText("Remove");
            setTitle("Remove Database");
        }
        MiscUtils.registerCancelButton(cancelButton);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        javax.swing.JPanel detailsPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        databaseField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();

        setTitle("Create Database");
        setBackground(new java.awt.Color(217, 222, 229));

        okButton.setText("Create");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        detailsPanel.setBackground(getBackground());
        detailsPanel.setOpaque(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("SERVER ADDRESS");

        hostField.setFont(new java.awt.Font("Lucida Grande", 0, 15)); // NOI18N
        hostField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("SERVER PORT");

        portField.setFont(new java.awt.Font("Lucida Grande", 0, 15));
        portField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("DATABASE NAME");

        databaseField.setFont(new java.awt.Font("Lucida Grande", 0, 15));
        databaseField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("ADMIN USERNAME");

        passwordField.setFont(new java.awt.Font("Lucida Grande", 0, 15));
        passwordField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("ADMIN_PASSWORD");

        userField.setFont(new java.awt.Font("Lucida Grande", 0, 15));
        userField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        org.jdesktop.layout.GroupLayout detailsPanelLayout = new org.jdesktop.layout.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(databaseField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, userField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(databaseField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(userField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(285, Short.MAX_VALUE)
                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton)
                .addContainerGap())
            .add(detailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(detailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        String dbName = databaseField.getText();
        if (removing) {
            if (DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This operation cannot be undone.", dbName) == DialogUtils.YES) {
                try {
                    MedSavantClient.initializeRegistry(hostField.getText(), portField.getText());
                    MedSavantClient.SetupAdapter.removeDatabase(hostField.getText(), Integer.parseInt(portField.getText()), databaseField.getText(), userField.getText(), passwordField.getPassword());
                    setVisible(false);
                    DialogUtils.displayMessage("Database Removed", String.format("<html>Database <i>%s</i> successfully removed.</html>", databaseField.getText()));
                } catch (Exception ex) {
                    LOG.error("Error removing database.", ex);
                    DialogUtils.displayException("Sorry", String.format("<html>Database could not be removed:<br>%s<br></html>", MiscUtils.getMessage(ex)), ex);
                }
            }
        } else {
            new IndeterminateProgressDialog("Creating Database", String.format("<html>Creating database <i>%s</i>. Please wait.</html>", dbName)) {
                @Override
                public void run() {
                    try {
                        MedSavantClient.initializeRegistry(hostField.getText(), portField.getText());
                        MedSavantClient.SetupAdapter.createDatabase(hostField.getText(), Integer.parseInt(portField.getText()), databaseField.getText(), userField.getText(), passwordField.getPassword(), VersionSettings.getVersionString());
                        AddRemoveDatabaseDialog.this.setVisible(false);
                        DialogUtils.displayMessage("Database Created", String.format("<html>Database <i>%s</i> successfully created.</html>", databaseField.getText()));
                    } catch (Throwable ex) {
                        ClientMiscUtils.reportError("Database could not be created: %s\nPlease check the settings and try again.", ex);
                    }
                }
            }.setVisible(true);            
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField databaseField;
    private javax.swing.JTextField hostField;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField portField;
    private javax.swing.JTextField userField;
    // End of variables declaration//GEN-END:variables

    public String getHost() {
        return hostField.getText();
    }

    public int getPort() {
        return Integer.parseInt(portField.getText());
    }

    public String getDatabase() {
        return databaseField.getText();
    }
}
