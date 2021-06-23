/**
 * Auth.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.unibl.etf.mdp.railroad.soap;

public interface Auth extends java.rmi.Remote {
    public org.unibl.etf.mdp.railroad.model.User login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
}
