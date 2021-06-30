package org.unibl.etf.mdp.railroad.controller;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.unibl.etf.mdp.railroad.model.TrainStation;
import org.unibl.etf.mdp.railroad.model.User;
import org.unibl.etf.mdp.railroad.rest.TrainStations;
import org.unibl.etf.mdp.railroad.soap.ClientSOAP;
import org.unibl.etf.mdp.railroad.view.Login;
import org.unibl.etf.mdp.railroad.view.TrainLines;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class DashboardController {

	private Stage stage;
	private User user;
	private ArrayList<User> users;
	private ArrayList<TrainStation> trainStations;
	
	@FXML
	private MenuItem logoutItem;
	@FXML
	private MenuItem timetableItem;
	@FXML
	private ComboBox<TrainStation> trainStationsComboBox;
	@FXML
	private Label stationID;
	@FXML
	private VBox usersWrap;
	
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
		generateUsers();
	}
	
	
	public void generateUsers() {
		usersWrap.getChildren().clear();
		String trainStationId = trainStationsComboBox.getSelectionModel().getSelectedItem().getId();
		if (trainStationId == null) return;
		users.stream().filter((element) -> element.getLocationId().equals(trainStationId) && !element.getUsername().equals(user.getUsername()))
		.forEach((element) -> usersWrap.getChildren().add(create(element.getUsername(), "2")));
	}
	
	public void logout() {
		new Login().display();
		stage.close();
	}
	
	public void openTimetable() {
		new TrainLines().display(user.getLocationId());
	}
	
	private static HBox create(String username, String unreadCount) {
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
		  hbox.getChildren().add(nameLabel);
		  hbox.getChildren().add(spacer);
		  hbox.getChildren().add(unreadCountLabel);
		  return hbox;
	  }
	//active color:  #88a9f2
}
