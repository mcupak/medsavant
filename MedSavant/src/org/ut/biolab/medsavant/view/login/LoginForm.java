/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginForm.java
 *
 * Created on Jun 20, 2011, 11:11:22 AM
 */
package org.ut.biolab.medsavant.view.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.MedSavantProgramInformation;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.dialog.AddDatabaseDialog;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LoginForm extends javax.swing.JPanel implements LoginListener {

    private static class SpiralPanel extends JPanel {
        private final Image img;

        public SpiralPanel() {
            img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOGO).getImage();
        }
        
        public void paintComponent(Graphics g) {
            //g.setColor(Color.black);
            //g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.drawImage(img, this.getWidth()/2-img.getWidth(null)/2, this.getHeight()/2-img.getHeight(null)/2, null);
        }
    }

    /** Creates new form LoginForm */
    public LoginForm() {
        
        LoginController.addLoginListener(this);
        
        initComponents();
        
        
        field_username.setText(LoginController.getUsername());
        field_password.setText(LoginController.getPassword());

        this.field_username.setText(SettingsController.getInstance().getUsername());
        if (SettingsController.getInstance().getRememberPassword()) {
            this.field_password.setText(SettingsController.getInstance().getPassword());
        }

        this.label_versioninformation.setText(MedSavantProgramInformation.getVersion() + " " + MedSavantProgramInformation.getReleaseType().toUpperCase());

        label_status.setText(" ");
        this.panel_title.add(Box.createVerticalGlue(),0); 
        
        spiralPanel.setLayout(new BorderLayout());
        spiralPanel.add(new SpiralPanel(),BorderLayout.CENTER); 
        
        this.panel_details.setVisible(false);
        this.button_create_db.setVisible(false);
        
        this.field_database.setText(SettingsController.getInstance().getValue(SettingsController.KEY_DB_NAME));
        this.field_port.setText(SettingsController.getInstance().getValue(SettingsController.KEY_DB_PORT));
         this.field_hostname.setText(SettingsController.getInstance().getValue(SettingsController.KEY_DB_HOST));
        
        
        this.setOpaque(false);
        this.setMaximumSize(new Dimension(400, 400));
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel_title = new javax.swing.JPanel();
        field_username = new javax.swing.JTextField();
        field_password = new javax.swing.JPasswordField();
        spiralPanel = new javax.swing.JPanel();
        label_versioninformation = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        panel_details = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        field_hostname = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        field_port = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        button_create_db = new javax.swing.JButton();
        button_login = new javax.swing.JButton();
        label_status = new javax.swing.JLabel();
        field_database = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        panel_title.setBackground(new java.awt.Color(217, 222, 229));
        panel_title.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel_title.setMaximumSize(new java.awt.Dimension(400, 32767));
        panel_title.setMinimumSize(new java.awt.Dimension(400, 800));

        field_username.setColumns(25);
        field_username.setFont(new java.awt.Font("Arial", 1, 18));
        field_username.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        field_username.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_usernameKeyPressed(evt);
            }
        });

        field_password.setColumns(25);
        field_password.setFont(new java.awt.Font("Arial", 0, 18));
        field_password.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        field_password.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_passwordKeyPressed(evt);
            }
        });

        spiralPanel.setPreferredSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout spiralPanelLayout = new javax.swing.GroupLayout(spiralPanel);
        spiralPanel.setLayout(spiralPanelLayout);
        spiralPanelLayout.setHorizontalGroup(
            spiralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 360, Short.MAX_VALUE)
        );
        spiralPanelLayout.setVerticalGroup(
            spiralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );

        label_versioninformation.setFont(new java.awt.Font("Tahoma", 0, 14));
        label_versioninformation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_versioninformation.setText("version information");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("username");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("password");

        jToggleButton1.setText("Connection Settings");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        panel_details.setOpaque(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("hostname");

        field_hostname.setFont(new java.awt.Font("Arial", 1, 18));
        field_hostname.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        field_hostname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_hostnameKeyPressed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("port");

        field_port.setFont(new java.awt.Font("Arial", 1, 18));
        field_port.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        field_port.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_portKeyPressed(evt);
            }
        });

        button_create_db.setText("Create Database");
        button_create_db.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_create_dbActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel_detailsLayout = new javax.swing.GroupLayout(panel_details);
        panel_details.setLayout(panel_detailsLayout);
        panel_detailsLayout.setHorizontalGroup(
            panel_detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_detailsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(button_create_db)
                    .addGap(106, 106, 106))
                .addComponent(field_hostname, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addGroup(panel_detailsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(panel_detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                    .addContainerGap())
                .addComponent(field_port, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
        );
        panel_detailsLayout.setVerticalGroup(
            panel_detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_detailsLayout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_hostname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_create_db)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        button_login.setBackground(new java.awt.Color(0, 0, 0));
        button_login.setText("Login");
        button_login.setOpaque(false);
        button_login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_loginActionPerformed(evt);
            }
        });

        label_status.setFont(new java.awt.Font("Tahoma", 0, 14));
        label_status.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label_status.setText("  ");

        field_database.setFont(new java.awt.Font("Arial", 1, 18));
        field_database.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        field_database.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                field_databaseKeyPressed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("database");

        javax.swing.GroupLayout panel_titleLayout = new javax.swing.GroupLayout(panel_title);
        panel_title.setLayout(panel_titleLayout);
        panel_titleLayout.setHorizontalGroup(
            panel_titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label_versioninformation, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(spiralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(field_username, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(field_password, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addGroup(panel_titleLayout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(field_database, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
            .addComponent(panel_details, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel_titleLayout.createSequentialGroup()
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 174, Short.MAX_VALUE)
                .addComponent(button_login))
            .addComponent(label_status, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
        );
        panel_titleLayout.setVerticalGroup(
            panel_titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_titleLayout.createSequentialGroup()
                .addComponent(spiralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_versioninformation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_database, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_details, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(label_status)
                .addGap(3, 3, 3)
                .addGroup(panel_titleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(button_login)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(45, 45, 45, 45);
        add(panel_title, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        add(jPanel1, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        add(jPanel2, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        add(jPanel3, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    private void field_passwordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_passwordKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_passwordKeyPressed

    private void button_loginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_loginActionPerformed
        this.loginUsingEnteredUsernameAndPassword();
    }//GEN-LAST:event_button_loginActionPerformed

    private void field_usernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_usernameKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
}//GEN-LAST:event_field_usernameKeyPressed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        this.panel_details.setVisible(!this.panel_details.isVisible());
        this.button_create_db.setVisible(!this.button_create_db.isVisible());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void field_hostnameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_hostnameKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_hostnameKeyPressed

    private void field_portKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_portKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_portKeyPressed

    private void field_databaseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_field_databaseKeyPressed
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            loginUsingEnteredUsernameAndPassword();
        }
    }//GEN-LAST:event_field_databaseKeyPressed

    private void button_create_dbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_create_dbActionPerformed
        AddDatabaseDialog d = new AddDatabaseDialog(this.field_hostname.getText(),this.field_port.getText(),this.field_database.getText());
        d.setVisible(true);
        
    }//GEN-LAST:event_button_create_dbActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_create_db;
    private javax.swing.JButton button_login;
    private javax.swing.JTextField field_database;
    private javax.swing.JTextField field_hostname;
    private javax.swing.JPasswordField field_password;
    private javax.swing.JTextField field_port;
    private javax.swing.JTextField field_username;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel label_status;
    private javax.swing.JLabel label_versioninformation;
    private javax.swing.JPanel panel_details;
    private javax.swing.JPanel panel_title;
    private javax.swing.JPanel spiralPanel;
    // End of variables declaration//GEN-END:variables

    private void loginUsingEnteredUsernameAndPassword() {
        
        final int port;
        try { port = Integer.parseInt(field_port.getText()); }
        catch (Exception e) { this.field_port.requestFocus(); return; }
        
        SettingsController.getInstance().setValue(SettingsController.KEY_DB_NAME,this.field_database.getText());
        SettingsController.getInstance().setValue(SettingsController.KEY_DB_PORT,this.field_port.getText());
        SettingsController.getInstance().setValue(SettingsController.KEY_DB_HOST,this.field_hostname.getText());
        
        ConnectionController.setDBName(SettingsController.getInstance().getValue(SettingsController.KEY_DB_NAME));
        ConnectionController.setPort(Integer.parseInt(SettingsController.getInstance().getValue(SettingsController.KEY_DB_PORT)));
        ConnectionController.setHost(SettingsController.getInstance().getValue(SettingsController.KEY_DB_HOST));
        
        this.label_status.setText("signing in...");
        this.label_status.setFont(new Font("Tahoma", Font.PLAIN, 14));
        this.label_status.setForeground(Color.black);
        this.button_login.setEnabled(false);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginController.login(field_username.getText(),field_password.getText());
            }
        });
        //new Thread(new RunLogin(this,this.field_username.getText(),this.field_password.getText())).start();
    }

    public void notifyOfUnsuccessfulLogin(Exception ex) {
        
        if (ex instanceof NonFatalDatabaseException) {
        
            NonFatalDatabaseException ex0 = (NonFatalDatabaseException) ex;
        if (!LoginController.isLoggedIn()) {
            if (ex0.getExceptionType() == NonFatalDatabaseException.ExceptionType.TYPE_ACCESS_DENIED) {
                this.label_status.setText("login incorrect");
            } else if (ex0.getExceptionType() == NonFatalDatabaseException.ExceptionType.TYPE_DB_CONNECTION_FAILURE
                    ) {
                this.label_status.setText("error accessing database");
            } else if (ex0.getExceptionType() == NonFatalDatabaseException.ExceptionType.TYPE_UNKNOWN) {
                this.label_status.setText("login failure");
            }
            this.label_status.setFont(new Font("Tahoma", Font.PLAIN, 14));
            this.label_status.setForeground(Color.red);
            this.field_username.requestFocus();
            this.button_login.setEnabled(true);
        }
        } else {
            ex.printStackTrace();
            this.label_status.setText("error accessing database");
            this.label_status.setFont(new Font("Tahoma", Font.PLAIN, 14));
            this.label_status.setForeground(Color.red);
            this.field_username.requestFocus();
            this.button_login.setEnabled(true);
        }
    }

    public void loginEvent(LoginEvent evt) {
        switch(evt.getType()) {
            case LOGGED_IN:
                break;
            case LOGGED_OUT:
                break;
            case LOGIN_FAILED:
                notifyOfUnsuccessfulLogin(evt.getException());
                break;
        }
    }
}
