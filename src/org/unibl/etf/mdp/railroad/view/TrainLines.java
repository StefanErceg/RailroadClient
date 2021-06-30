package org.unibl.etf.mdp.railroad.view;

import org.unibl.etf.mdp.railroad.controller.TrainLinesController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TrainLines {

	public void display(String trainStationId) {
		try {
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/unibl/etf/mdp/railroad/view/fxml/train-lines.fxml"));
			stage.setTitle("Railroad - train lines");
			stage.initModality(Modality.APPLICATION_MODAL); 
			Scene scene = new Scene(loader.load());
			stage.setScene(scene);
			TrainLinesController controller = loader.getController();
			controller.initialize(stage, trainStationId);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
