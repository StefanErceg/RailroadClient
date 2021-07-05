package org.unibl.etf.mdp.railroad;

import java.util.logging.Level;

import org.unibl.etf.mdp.railroad.view.Login;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	public static ErrorLog errorLog = new ErrorLog();


	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			new Login().display();
		} catch(Exception e) {
			errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		
	}

    public static void main(String[] args) {
        try {
        	if (!Configuration.checkIfFileExists()) Configuration.writeConfiguration();
            launch(args);
            

        } catch (Exception e) {
        	errorLog.getLogger().log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }

}
