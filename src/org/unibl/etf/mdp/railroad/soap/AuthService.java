/**
 * AuthService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.unibl.etf.mdp.railroad.soap;

public interface AuthService extends javax.xml.rpc.Service {
    public java.lang.String getAuthAddress();

    public org.unibl.etf.mdp.railroad.soap.Auth getAuth() throws javax.xml.rpc.ServiceException;

    public org.unibl.etf.mdp.railroad.soap.Auth getAuth(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
