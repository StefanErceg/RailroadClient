package org.unibl.etf.mdp.railroad.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.unibl.etf.mdp.railroad.Configuration;
import org.unibl.etf.mdp.railroad.Main;
import org.unibl.etf.mdp.railroad.archive.ArchiveInterface;
import org.unibl.etf.mdp.railroad.chat.ChatUser;
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
import javafx.scene.input.KeyCode;
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
	
	private static String ARCHIVE_DIRECTORY;
	private static String CHAT_FILES_DIRECTORY;
	
	private static String CHAT_HOST;
	private static int CHAT_PORT;
	private static String KEYSTORE;
	private static String KEYSTORE_PASS;
	
	private static Integer FILE_PORT;

	private static final Integer INTRODUCTION = 1;
	private static final Integer TEXT = 2;
	private static final Integer FILE = 3;
	private static final Integer BYE = 4;
	private static final Integer USERS = 5;
	private static final Integer ADD_USER = 6;
	private static final Integer REMOVE_USER = 7;
	
	private Stage stage;
	private User user;
	private ArrayList<TrainStation> trainStations;
	private static HashMap<String, ArrayList<ChatUser>> onlineUsers = null;
	
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
		try {
			Properties properties = Configuration.readParameters();
			ARCHIVE_DIRECTORY = properties.getProperty("ARCHIVE_DIRECTORY");
			CHAT_FILES_DIRECTORY = properties.getProperty("CHAT_FILES_DIRECTORY");
			CHAT_HOST = properties.getProperty("CHAT_HOST");
			CHAT_PORT = Integer.valueOf(properties.getProperty("CHAT_PORT"));
			KEYSTORE = properties.getProperty("KEYSTORE");
			KEYSTORE_PASS = properties.getProperty("KEYSTORE_PASS");
			FILE_PORT = Integer.valueOf(properties.getProperty("FILE_PORT"));
			
		} catch(IOException e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		this.stage = stage;
		this.user = user;
		this.stationID.setText(user.getLocationId());
		this.trainStations = TrainStations.getTrainStations();
		trainStationsComboBox.setOnAction(null);
		trainStationsComboBox.getItems().addAll(trainStations);
		trainStationsComboBox.getSelectionModel().select(trainStations.stream().filter((element) -> element.getId().equals(user.getLocationId())).findFirst().orElse(null));
		trainStationsComboBox.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				clearChat();
				generateUsers();
				selectedUser = "";
				selectedUserLabel.setText("");
				
			}
		});
		unreadCounts = new HashMap<String, Label>();

		Notification.initialize(user.getUsername());
		System.setProperty("java.security.policy", ARCHIVE_DIRECTORY + File.separator + "policy" + File.separator + "client_policyfile.txt");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(FILE_PORT);
			archive = (ArchiveInterface) registry.lookup("Archive");
		} catch (RemoteException e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		} catch (NotBoundException e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		
		System.setProperty("javax.net.ssl.trustStore", KEYSTORE);
		System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASS);
		
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			socket = (SSLSocket) socketFactory.createSocket(CHAT_HOST, CHAT_PORT);
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			chats = new HashMap<String, ArrayList<ChatMessage>>();
			messageTextField.setOnKeyReleased(event -> {
				  if (event.getCode() == KeyCode.ENTER){
					  sendMessage();
					  }
					});
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
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					Integer option = 0;
					while (socketActive) {
						try {
							option = in.readInt();
							if (option.equals(USERS)) {
								Integer length = in.readInt();
								byte[] usersBytes = new byte[length];
								in.read(usersBytes);
								onlineUsers = (HashMap<String, ArrayList<ChatUser>>) deserialize(usersBytes);
								generateUsers();
							} else if (option.equals(ADD_USER) || option.equals(REMOVE_USER)) {
								Integer usernameLength = in.readInt();
								byte[] usernameBuffer = new byte[usernameLength];
								in.read(usernameBuffer, 0, usernameLength);
								String username = new String(usernameBuffer);
								Integer stationIdLength = in.readInt();
								byte[] stationIdBuffer = new byte[stationIdLength];
								in.read(stationIdBuffer, 0, stationIdLength);
								String trainStationId = new String(stationIdBuffer);
								if (option.equals(ADD_USER)) {
									handleNewUser(username, trainStationId);
								} else handleRemoveUser(username, trainStationId);
							}
							else {
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
								if (!from.equals(selectedUser)) {
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
								if (!from.equals(selectedUser)) {
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
							}
						} catch (IOException e) {
							Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
							socketActive = false;
						} catch (ClassNotFoundException e) {
							Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
						}
					}
				}
			});
			chatListener.setDaemon(true);
			chatListener.start();
		} catch (IOException e) {
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
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
		if ("".equals(content) || "".equals(selectedUser)) return;
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
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
	
	public void sendFile() {
		if ("".equals(selectedUser)) return;
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
				Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
			} catch (IOException e) {
				Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
			}
			
		}
	}
	
	
	public void generateUsers() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				usersWrap.getChildren().clear();
				String trainStationId = trainStationsComboBox.getSelectionModel().getSelectedItem().getId();
				if (trainStationId == null || onlineUsers == null || !onlineUsers.containsKey(trainStationId)) return;
				onlineUsers.get(trainStationId).stream().filter((element) -> element.getTrainStationId().equals(trainStationId) && !element.getUsername().equals(user.getUsername()))
				.forEach((element) -> usersWrap.getChildren().add(create(element.getUsername(), "")));
			}
		});
	
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
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
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
			Main.errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
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
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				clearChat();
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
		});
		
	}
	
	private void clearChat() {
		chat.getChildren().clear();
	}
	
	private void handleNewUser(String username, String trainStationId) {
		if (!onlineUsers.containsKey(trainStationId)) {
			onlineUsers.put(trainStationId, new ArrayList<ChatUser>());
		}
		onlineUsers.get(trainStationId).add(new ChatUser(username, trainStationId));
		if (trainStationsComboBox.getSelectionModel().getSelectedItem().getId().equals(trainStationId)) {
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					usersWrap.getChildren().add(create(username, ""));
				}
			});
			
		}
	}
	
	private void handleRemoveUser(String username, String trainStationId) {
		if (!onlineUsers.containsKey(trainStationId)) return;
		ArrayList<ChatUser> allUsers =  onlineUsers.get(trainStationId);
		onlineUsers.replace(trainStationId,(ArrayList<ChatUser>) allUsers.stream().filter(user -> !user.getUsername().equals(username)).collect(Collectors.toList()));
		if (trainStationsComboBox.getSelectionModel().getSelectedItem().getId().equals(trainStationId)) {
			generateUsers();
		}
		if (selectedUser.equals(username)) {
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					clearChat();
					
				}
			});
			
		}
	}
	
	private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
}
