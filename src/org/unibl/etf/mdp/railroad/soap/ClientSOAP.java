package org.unibl.etf.mdp.railroad.soap;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.rpc.ServiceException;

import org.unibl.etf.mdp.railroad.model.User;

public class ClientSOAP {
	
	public static User login(String username, String password) {
		AuthServiceLocator locator = new AuthServiceLocator();
		try {
			Auth auth = locator.getAuth();
			return auth.login(username, password);
		} catch (ServiceException | RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<User> getUsers() {
		UserServiceLocator locator = new UserServiceLocator();
		try {
			org.unibl.etf.mdp.railroad.soap.User user = locator.getUser();
			return new ArrayList<>(Arrays.asList(user.getUsers()));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
