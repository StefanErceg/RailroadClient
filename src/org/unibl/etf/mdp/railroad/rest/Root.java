package org.unibl.etf.mdp.railroad.rest;

import java.util.logging.Level;

import org.unibl.etf.mdp.railroad.Configuration;
import org.unibl.etf.mdp.railroad.Main;

public class Root {
	public static String base = "";
	
	static {
		try {
			base = Configuration.readParameters().getProperty("REST_BASE");
		} catch (Exception e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
}
