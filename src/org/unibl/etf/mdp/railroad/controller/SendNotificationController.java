package org.unibl.etf.mdp.railroad.controller;

import org.unibl.etf.mdp.railroad.model.User;
import org.unibl.etf.mdp.railroad.notifications.Notification;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class SendNotificationController {
	
	private Stage stage;
	private User user;
	@FXML
	private TextArea textArea;
	
	public void initialize(Stage stage, User user) {
		this.stage = stage;
		this.user = user;
	}
	
	public void cancel() {
		stage.close();
	}
	
	public void send() {
		String content = textArea.getText();
		if ("".equals(content)) return;
		Notification.send(user.getUsername(), content);
		stage.close();
	}

}
