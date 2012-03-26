package org.ut.biolab.medsavant.db.util.shared;

import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author mfiume
 */
public class MedSavantServerUnicastRemoteObject extends UnicastRemoteObject {

    private static final int EXPORT_PORT = 3232;

    public MedSavantServerUnicastRemoteObject() throws java.rmi.RemoteException {
        super(EXPORT_PORT);
    }

    public int getPort() {
        return EXPORT_PORT;
    }
}