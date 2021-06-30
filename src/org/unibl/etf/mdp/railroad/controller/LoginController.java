package org.unibl.etf.mdp.railroad.controller;

import org.unibl.etf.mdp.railroad.model.User;
import org.unibl.etf.mdp.railroad.soap.ClientSOAP;
import org.unibl.etf.mdp.railroad.view.Dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
	
	private Stage stage;
	
	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Button loginButton;

	public void initialize(Stage stage) {
		this.stage = stage;
	}
	
	public void login() {
		String username = usernameTextField.getText();
		String password = usernameTextField.getText();
		if ("".equals(username) || "".equals(password)) return;
		User user = ClientSOAP.login(username, password);
		if (user != null) {
			new Dashboard().display(user);
			stage.close();
		}
	}

}
