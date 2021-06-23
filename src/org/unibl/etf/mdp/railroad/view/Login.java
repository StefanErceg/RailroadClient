package org.unibl.etf.mdp.railroad.view;

import org.unibl.etf.mdp.railroad.controller.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

	public class Login {

	    public void display() {
	    	try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/unibl/etf/mdp/railroad/view/fxml/login.fxml"));
	        Stage stage = new Stage();
	        stage.setTitle("Railroad");
	        Scene scene = new Scene(loader.load());
	        stage.setScene(scene);
	        stage.setHeight(400);
	        stage.setWidth(600);
	        stage.setResizable(false);
	        LoginController controller = loader.getController();
	        controller.initialize(stage);
	        stage.show();
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    }


	}
