package org.unibl.etf.mdp.railroad.controller;

import org.unibl.etf.mdp.railroad.model.User;
import org.unibl.etf.mdp.railroad.rest.TrainStations;
import org.unibl.etf.mdp.railroad.soap.Client;
import org.unibl.etf.mdp.railroad.view.Login;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class DashboardController {

	private Stage stage;
	private User user;
	
	@FXML
	private MenuItem logoutItem;
	
	public void initialize(Stage stage, User user) {
		this.stage = stage;
		this.user = user;
		Client.getUsers().forEach((a) -> {
			System.out.println(a.getFirstName());
		});
		TrainStations.getTrainStations().forEach((trainStation) -> {
			System.out.println(trainStation);
		});
	}
	
	public void logout() {
		new Login().display();
		stage.close();
	}
	
}
