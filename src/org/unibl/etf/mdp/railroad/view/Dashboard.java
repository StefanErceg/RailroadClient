package org.unibl.etf.mdp.railroad.view;

import org.unibl.etf.mdp.railroad.controller.DashboardController;
import org.unibl.etf.mdp.railroad.model.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Dashboard {

	public void display(User user) {
		
		try {
		Stage stage = new Stage();
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/unibl/etf/mdp/railroad/view/fxml/client-main.fxml"));
        stage.setTitle("Railroad");
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        DashboardController controller = loader.getController();
        controller.initialize(stage, user);
        stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
