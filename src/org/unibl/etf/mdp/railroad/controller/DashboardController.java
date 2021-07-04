package org.unibl.etf.mdp.railroad.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.unibl.etf.mdp.railroad.archive.ArchiveInterface;
import org.unibl.etf.mdp.railroad.model.ChatMessage;
import org.unibl.etf.mdp.railroad.model.TrainStation;
import org.unibl.etf.mdp.railroad.model.User;
import org.unibl.etf.mdp.railroad.notifications.Notification;
import org.unibl.etf.mdp.railroad.rest.TrainStations;
import org.unibl.etf.mdp.railroad.soap.ClientSOAP;
import org.unibl.etf.mdp.railroad.view.Alert;
import org.unibl.etf.mdp.railroad.view.Login;
import org.unibl.etf.mdp.railroad.view.SendNotification;
import org.unibl.etf.mdp.railroad.view.TrainLines;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class DashboardController {
	
	private static final String ARCHIVE_DIRECTORY =  System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "Archive";
	private static final String CHAT_FILES_DIRECTORY = System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "Chat";
	
	private static final String CHAT_HOST = "127.0.0.1";
	private static final int PORT = 8443;
	private static final String KEYSTORE = System.getProperty("user.home") + File.separator + "Railroad" + File.separator + "SSL" + File.separator + "server_keystore.jks";
	private static final String KEYSTORE_PASS = "railroadserver";
	

	public static final Integer INTRODUCTION = 1;
	public static final Integer TEXT = 2;
	public static final Integer FILE = 3;
	public static final Integer BYE = 4;
	
	private Stage stage;
	private User user;
	private ArrayList<User> users;
	private ArrayList<TrainStation> trainStations;
	
	private HashMap<String, ArrayList<ChatMessage>> chats;
	private HashMap<String, Label> unreadCounts;
	
	private String selectedUser = "";
	
	private DataOutputStream out;
	private DataInputStream in;
	
	private SSLSocket socket;
	private boolean socketActive = false;
	
	@FXML
	private MenuItem logoutItem;
	@FXML
	private MenuItem timetableItem;
	@FXML
	private ComboBox<TrainStation> trainStationsComboBox;
	@FXML
	private Label stationID, selectedUserLabel;
	@FXML
	private VBox usersWrap, chat;
	@FXML
	private TextField messageTextField;
	
	private ArchiveInterface archive;
	
	public void initialize(Stage stage, User user) {
		this.stage = stage;
		this.user = user;
		this.stationID.setText(user.getLocationId());
		this.users = new ArrayList<User>(ClientSOAP.getUsers().stream().filter((element) -> element.getUsername() != user.getUsername()).collect(Collectors.toList()));
		this.trainStations = TrainStations.getTrainStations();
		trainStationsComboBox.setOnAction(null);
		trainStationsComboBox.getItems().addAll(trainStations);
		trainStationsComboBox.getSelectionModel().select(trainStations.stream().filter((element) -> element.getId().equals(user.getLocationId())).findFirst().orElse(null));
		trainStationsComboBox.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				generateUsers();
				
			}
		});
		unreadCounts = new HashMap<String, Label>();
		generateUsers();
		Notification.initialize(user.getUsername());
		System.setProperty("java.security.policy", ARCHIVE_DIRECTORY + File.separator + "policy" + File.separator + "client_policyfile.txt");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(1099);
			archive = (ArchiveInterface) registry.lookup("Archive");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
		System.setProperty("javax.net.ssl.trustStore", KEYSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASS);
		
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			socket = (SSLSocket) socketFactory.createSocket(CHAT_HOST, PORT);
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			chats = new HashMap<String, ArrayList<ChatMessage>>();

			stage.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
				@Override
				public void handle(javafx.stage.WindowEvent event) {
					closeSocketConnection();
				}
			});
			socketActive = true;
			out.writeInt(INTRODUCTION);
			byte[] usernameBytes = user.getUsername().getBytes();
			out.writeInt(usernameBytes.length);
			out.write(usernameBytes);
			byte[] stationIdBytes = user.getLocationId().getBytes();
			out.writeInt(stationIdBytes.length);
			out.write(stationIdBytes);
			out.flush();
			Thread chatListener = new Thread(new Runnable() {
				@Override
				public void run() {
					Integer option = 0;
					while (socketActive) {
						try {
							option = in.readInt();
							Integer fromLength = in.readInt();
							byte[] fromBuffer = new byte[fromLength];
							in.read(fromBuffer, 0, fromLength);
							String from = new String(fromBuffer);
							Integer hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
							Integer minutes = Calendar.getInstance().get(Calendar.MINUTE);
							String time = hours.toString() + ":" + (minutes < 10 ? "0" + minutes.toString() : minutes.toString());
							if (option.equals(TEXT)) {
								Integer dataLength = in.readInt();
								byte[] dataBuffer = new byte[dataLength];
								in.read(dataBuffer, 0, dataLength);
								String data = new String(dataBuffer);
								addMessage(TEXT, false, from, data, time);
								if (from != selectedUser) {
									Label unreadCount = unreadCounts.get(from);
									if (unreadCount != null) {
										String text = unreadCount.getText();
										Integer value = "".equals(text) ? 1 : Integer.valueOf(text) + 1;
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												unreadCount.setText(value.toString());
											}
											
										});
										
									}
								} else {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										renderMessage(false, data, time);
									}
								}); 
								}
							} else if (option.equals(FILE)) {
								Integer dataLength = in.readInt();
								byte[] dataBuffer = new byte[dataLength];
								Integer dataLoaded = 0;
								Integer loopLoaded = 0;
								while(dataLoaded < dataLength) {
								loopLoaded = in.read(dataBuffer, dataLoaded, dataLength - dataLoaded);
								dataLoaded += loopLoaded;
								}
								Integer filenameLength = in.readInt();
								byte[] filenameBytes = new byte[filenameLength];
								in.read(filenameBytes, 0, filenameLength);
								String filename = new String(filenameBytes);
								
								FileOutputStream fileOut = new FileOutputStream(new File(CHAT_FILES_DIRECTORY + File.separator + filename));
								fileOut.write(dataBuffer);
								fileOut.flush();
								fileOut.close();
								addMessage(FILE, false, from, filename, time);
								if (from != selectedUser) {
									Label unreadCount = unreadCounts.get(from);
									if (unreadCount != null) {
										String text = unreadCount.getText();
										Integer value = "".equals(text) ? 1 : Integer.valueOf(text) + 1;
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												unreadCount.setText(value.toString());
											}
											
										});
										
									}
								} else {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										renderFileSymbol(false, filename, time);
									}
								}); 
								}
								
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			chatListener.setDaemon(true);
			chatListener.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void addMessage(Integer type,boolean sent, String from, String content, String time) {
		if (!chats.containsKey(from)) {
			chats.put(from, new ArrayList<ChatMessage>());
		}
		ArrayList<ChatMessage> messages = chats.get(from);
	
		messages.add(new ChatMessage(type, sent, content, time));
	}
	
	
	public void sendMessage() {
		String content = messageTextField.getText();
		if ("".equals(content)) return;
		try {
		out.writeInt(TEXT);
		byte[] usernameBytes = user.getUsername().getBytes();
		out.writeInt(usernameBytes.length);
		out.write(usernameBytes);
	    byte[] bytes = selectedUser.getBytes();
	    out.writeInt(bytes.length);
	    out.write(bytes);
	    bytes = content.getBytes();
	    out.writeInt(bytes.length);
	    out.write(bytes);
	    out.flush();
	    messageTextField.clear();
	    Integer hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		Integer minutes = Calendar.getInstance().get(Calendar.MINUTE);
		String time = hours.toString() + ":" + (minutes < 10 ? "0" + minutes.toString() : minutes.toString());
		addMessage(TEXT, true, selectedUser, content, time);
	    renderMessage(true, content, time);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendFile() {
		File file = new FileChooser().showOpenDialog(stage);
		if (file != null && file.isFile()) {
			String filename = file.getName();
			try {
				byte[] data = new byte[(int) file.length()];
				data = Files.readAllBytes(file.toPath());
				out.writeInt(FILE);
				byte[] usernameBytes = user.getUsername().getBytes();
				out.writeInt(usernameBytes.length);
				out.write(usernameBytes);
			    byte[] bytes = selectedUser.getBytes();
			    out.writeInt(bytes.length);
			    out.write(bytes);
			    out.writeInt(data.length);
			    out.write(data);
			    byte[] filenameBytes = filename.getBytes();
			    out.writeInt(filenameBytes.length);
			    out.write(filenameBytes);
			    out.flush();
			    Integer hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				Integer minutes = Calendar.getInstance().get(Calendar.MINUTE);
				String time = hours.toString() + ":" + (minutes < 10 ? "0" + minutes.toString() : minutes.toString());
				addMessage(FILE, true, selectedUser, filename, time);
			    renderFileSymbol(true, filename, time);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	public void generateUsers() {
		usersWrap.getChildren().clear();
		String trainStationId = trainStationsComboBox.getSelectionModel().getSelectedItem().getId();
		if (trainStationId == null) return;
		users.stream().filter((element) -> element.getLocationId().equals(trainStationId) && !element.getUsername().equals(user.getUsername()))
		.forEach((element) -> usersWrap.getChildren().add(create(element.getUsername(), "")));
	}
	
	public void logout() {
		closeSocketConnection();
		new Login().display();
		stage.close();
	}
	
	public void openTimetable() {
		new TrainLines().display(user.getLocationId());
	}
	
	public void sendNotification() {
		new SendNotification().display(user);
	}
	
	public void archiveReport() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select PDF report");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF document", "*.pdf"));
		File selected = fileChooser.showOpenDialog(stage);
		try {
			byte[] data = Files.readAllBytes(selected.toPath());
			String name = selected.getName();
			archive.upload(data, name, user.getUsername());
			new Alert().display("Report successfully archived!");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		
	}
	
	private void closeSocketConnection() {
		try {
			out.writeInt(BYE);
			byte[] usernameBytes = user.getUsername().getBytes();
			out.writeInt(usernameBytes.length);
			out.write(usernameBytes);
			byte[] stationIdBytes = user.getLocationId().getBytes();
			out.writeInt(stationIdBytes.length);
			out.write(stationIdBytes);
			out.flush();
			socketActive = false;
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void renderMessage(boolean sent, String content, String time) {
		HBox hbox = new HBox();
		VBox.setVgrow(hbox, Priority.ALWAYS);
		VBox.setMargin(hbox, new Insets(5));
		Label timeLabel = new Label(time);
		timeLabel.setFont(Font.font("Monospaced", 12.0));
		Label messageLabel = new Label(content);
		messageLabel.setFont(Font.font("Monospaced", 16.0));
		messageLabel.setStyle("-fx-border-color: #5e89e6; -fx-border-radius: 8; ");
		HBox.setMargin(messageLabel, new Insets(0, 20, 0, 20));
		messageLabel.setPadding(new Insets(0, 5, 0, 5));
		messageLabel.setWrapText(true);
		if (sent) {
			hbox.setAlignment(Pos.CENTER_RIGHT);
			hbox.getChildren().addAll(timeLabel, messageLabel);
		} else {
			hbox.setAlignment(Pos.CENTER_LEFT);
			hbox.getChildren().addAll(messageLabel, timeLabel);
		}
		chat.getChildren().add(hbox);
	}
	
	private void renderFileSymbol(boolean sent, String name, String time) {
		HBox hbox = new HBox();
		VBox.setVgrow(hbox, Priority.ALWAYS);
		VBox.setMargin(hbox, new Insets(5));
		Label timeLabel = new Label(time);
		timeLabel.setFont(Font.font("Monospaced", 12.0));
		Label nameLabel = new Label(name);
		nameLabel.setFont(Font.font("Monospaced", 16.0));
		nameLabel.setStyle("-fx-border-color: #5e89e6; -fx-border-radius: 8; ");
		HBox.setMargin(nameLabel, new Insets(0, 20, 0, 20));
		nameLabel.setPadding(new Insets(0, 5, 0, 5));
		nameLabel.setWrapText(true);
		File file = new File(System.getProperty("user.dir") + File.separator +"assets" + File.separator + "icons" + File.separator + "file.png");
	    Image img = new Image(file.toURI().toString());
		ImageView image = new ImageView();
		image.setImage(img);
		image.setFitWidth(30);
		image.setFitHeight(30);
		HBox.setMargin(image, new Insets(10));
		if (sent) {
			hbox.setAlignment(Pos.CENTER_RIGHT);
			hbox.getChildren().addAll(timeLabel, image, nameLabel);
		} else {
			hbox.setAlignment(Pos.CENTER_LEFT);
			hbox.getChildren().addAll(image, nameLabel, timeLabel);
		}
		chat.getChildren().add(hbox);
	}

	
	private HBox create(String username, String unreadCount) {
		  HBox hbox = new HBox();
		  hbox.setPrefHeight(30);
		hbox.setPadding(new Insets(5));
		  hbox.setStyle("-fx-border-color: #000000cc; -fx-border-width:1px; -fx-border-radius: 8; -fx-background-radius: 8;");
		  hbox.setAlignment(Pos.CENTER_LEFT);
		  Region spacer = new Region();
		  HBox.setHgrow(spacer, Priority.ALWAYS);
		  Label nameLabel = new Label(username);
		  nameLabel.setFont(Font.font("Monospaced", 16.0));
		  Label unreadCountLabel = new Label(unreadCount);
		  unreadCountLabel.setFont(Font.font("Monospaced", 16.0));
		  unreadCounts.put(username, unreadCountLabel);
		  hbox.getChildren().add(nameLabel);
		  hbox.getChildren().add(spacer);
		  hbox.getChildren().add(unreadCountLabel);
		  hbox.setOnMouseClicked((new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					selectedUser = username;
					selectedUserLabel.setText(username);
					unreadCountLabel.setText("");
					updateChat();
				}
		  }));
		  return hbox;
	  }
	
	private void updateChat() {
		chat.getChildren().clear();
		ArrayList<ChatMessage> messages = chats.get(selectedUser);
		if (messages != null) {
			messages.forEach(message -> {
				if (message.getType().equals(TEXT)) {
					renderMessage(message.isSent(), message.getContent(), message.getTime());
				} 
				else if (message.getType().equals(FILE)) {
					renderFileSymbol(message.isSent(), message.getContent(), message.getTime());
				}
			});
		}
	}
}
