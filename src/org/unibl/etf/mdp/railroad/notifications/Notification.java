package org.unibl.etf.mdp.railroad.notifications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.unibl.etf.mdp.railroad.view.Alert;

import javafx.application.Platform;

public class Notification {
	
	private static final int PORT = 20000;
	private static final String HOST = "224.0.0.11";
	public static final String DELIMITER = "###";
	
	private static MulticastSocket socket = null;
	private static boolean active = false;
	private static InetAddress address = null;
	private static String username = "";
	
	public static void initialize(String user) {
		try {
		socket = new MulticastSocket(PORT);
        address = InetAddress.getByName(HOST);
        socket.joinGroup(address);
        active = true;
        username = user;
        listen();
		} catch(Exception e) {
			e.printStackTrace();
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
						e.printStackTrace();
					}
	                String received = new String(packet.getData(), 0, packet.getLength());
	                String[] parsed = received.split(DELIMITER);
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
		String content = user + DELIMITER + message;
		buffer = content.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
