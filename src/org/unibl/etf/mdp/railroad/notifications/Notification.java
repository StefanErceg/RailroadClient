package org.unibl.etf.mdp.railroad.notifications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Properties;
import java.util.logging.Level;

import org.unibl.etf.mdp.railroad.Configuration;
import org.unibl.etf.mdp.railroad.Main;
import org.unibl.etf.mdp.railroad.view.Alert;

import javafx.application.Platform;

public class Notification {
	
	private static int MULTICAST_PORT;
	private static String MULTICAST_HOST;
	public static String MULTICAST_DELIMITER;
	
	private static MulticastSocket socket = null;
	private static boolean active = false;
	private static InetAddress address = null;
	private static String username = "";
	
	public static void initialize(String user) {
		try {
			Properties properties = Configuration.readParameters();
			MULTICAST_PORT = Integer.valueOf(properties.getProperty("MULTICAST_PORT"));
			MULTICAST_HOST = properties.getProperty("MULTICAST_HOST");
			MULTICAST_DELIMITER = properties.getProperty("MULTICAST_DELIMITER");
			
			socket = new MulticastSocket(MULTICAST_PORT);
	        address = InetAddress.getByName(MULTICAST_HOST);
	        socket.joinGroup(address);
	        active = true;
	        username = user;
	        listen();
		} catch(Exception e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
	
	private static void listen() {
		byte[] buffer = new byte[1024];
		Thread listener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (active) {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	                try {
						socket.receive(packet);
					} catch (IOException e) {
						Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
					}
	                String received = new String(packet.getData(), 0, packet.getLength());
	                String[] parsed = received.split(MULTICAST_DELIMITER);
	                if (username == null || username.equals(parsed[0])) return;
	                Platform.runLater(new Runnable() {
						@Override
						public void run() {
							new Alert().display(parsed[0] + ": " + parsed[1]);
						}
					}); 
				}
				
			}
		});
		listener.setDaemon(true);
		listener.start();
	}
	
	public static void send(String user, String message) {
		byte[] buffer = new byte[1024];
		String content = user + MULTICAST_DELIMITER + message;
		buffer = content.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, MULTICAST_PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
	

}
