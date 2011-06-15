/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class PatientPage implements Page {

    public PatientPage() {
    }

    public String getName() {
        return "Patient";
    }

    public Component getView() {
        JPanel p = new JPanel();
        p.setBackground(Color.lightGray);
        return p;
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setBackground(Color.black);
        return p;
    }

}