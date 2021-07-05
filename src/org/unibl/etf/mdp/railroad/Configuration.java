package org.unibl.etf.mdp.railroad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
public static String CONFIG_PATH = "config" + File.separator + "config.properties";
	
	public static void writeConfiguration() throws IOException {
		File folder = new File("config");
		if(!folder.exists()) {
			folder.mkdir();
		}
		
		File file = new File(CONFIG_PATH);
		file.createNewFile();
		
		Properties properties = new Properties();
		properties.setProperty("CHAT_HOST", "127.0.0.1");
		properties.setProperty("CHAT_PORT", "8443");
		properties.setProperty("KEYSTORE", System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "SSL" + File.separator + "server_keystore.jks");
		properties.setProperty("KEYSTORE_PASS", "railroadserver");
		properties.setProperty("FILE_PORT", "1099");
		properties.setProperty("ARCHIVE_DIRECTORY", System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "Archive");
		properties.setProperty("CHAT_FILES_DIRECTORY", System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "Chat");
		properties.setProperty("LOGS_PATH", System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "Client" + File.separator + "error.log");
		properties.setProperty("MULTICAST_PORT", "20000");
		properties.setProperty("MULTICAST_HOST", "224.0.0.11");
		properties.setProperty("MULTICAST_DELIMITER", "###");
		properties.setProperty("REST_BASE", "http://localhost:8080/RailroadCentral/api");
		
		FileOutputStream out = new FileOutputStream(file);
		properties.store(out, null);
		
		out.close();
	}
	
	public static Properties readParameters() throws IOException {	
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(new File(CONFIG_PATH));
		properties.load(in);
		in.close();		
		
		return properties;
	}
	
	public static boolean checkIfFileExists() {
		return new File(CONFIG_PATH).exists();
	}
	
}
