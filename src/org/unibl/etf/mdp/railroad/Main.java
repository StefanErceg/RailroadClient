package org.unibl.etf.mdp.railroad;

import org.unibl.etf.mdp.railroad.view.Login;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	


	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			new Login().display();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

    public static void main(String[] args) {
        try {
            launch(args);
            

        } catch (Exception ex) {
           System.out.println(ex.getMessage());
          	ex.printStackTrace();
        }
    }

}
