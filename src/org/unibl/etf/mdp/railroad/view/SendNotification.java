package org.unibl.etf.mdp.railroad.view;

import org.unibl.etf.mdp.railroad.controller.SendNotificationController;
import org.unibl.etf.mdp.railroad.model.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SendNotification {
	
	public void display(User user) {
		try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/unibl/etf/mdp/railroad/view/fxml/send-notification.fxml"));
	        Stage stage = new Stage();
	        stage.setTitle("Railroad - send notification");
	        Scene scene = new Scene(loader.load());
	        stage.setScene(scene);
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.setHeight(400);
	        stage.setWidth(600);
	        stage.setResizable(false);
	        SendNotificationController controller = loader.getController();
	        controller.initialize(stage, user);
	        stage.showAndWait();
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	}
