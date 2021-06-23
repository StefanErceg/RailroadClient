package org.unibl.etf.mdp.railroad.soap;

public class AuthProxy implements org.unibl.etf.mdp.railroad.soap.Auth {
  private String _endpoint = null;
  private org.unibl.etf.mdp.railroad.soap.Auth auth = null;
  
  public AuthProxy() {
    _initAuthProxy();
  }
  
  public AuthProxy(String endpoint) {
    _endpoint = endpoint;
    _initAuthProxy();
  }
  
  private void _initAuthProxy() {
    try {
      auth = (new org.unibl.etf.mdp.railroad.soap.AuthServiceLocator()).getAuth();
      if (auth != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)auth)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)auth)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (auth != null)
      ((javax.xml.rpc.Stub)auth)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.unibl.etf.mdp.railroad.soap.Auth getAuth() {
    if (auth == null)
      _initAuthProxy();
    return auth;
  }
  
  public org.unibl.etf.mdp.railroad.model.User login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException{
    if (auth == null)
      _initAuthProxy();
    return auth.login(username, password);
  }
  
  
}