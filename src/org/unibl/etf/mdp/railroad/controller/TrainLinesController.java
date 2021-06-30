package org.unibl.etf.mdp.railroad.controller;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.unibl.etf.mdp.railroad.model.TrainLine;
import org.unibl.etf.mdp.railroad.rest.TrainLines;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TrainLinesController {
	
	private Stage stage;
	private String trainStationId;
	private ArrayList<TrainLine> trainLines;
	
	@FXML
	private VBox trainLinesVBox;

	
	public void initialize(Stage stage, String trainStationId) {
		this.stage = stage;
		this.trainStationId = trainStationId;
		this.trainLines = TrainLines.getTrainLineByTrainStation(trainStationId);
		renderTrainLines();
	}
	
	public void close() {
		stage.close();
	}
	
	private void renderTrainLines() {
		trainLinesVBox.getChildren().clear();
		this.trainLines.forEach((trainLine) -> trainLinesVBox.getChildren().add(createRow(trainLine, trainStationId)));
	}
	
	private void updateTrainLines(TrainLine updated) {
		this.trainLines = new ArrayList<TrainLine>(trainLines.stream().map((trainLine) -> trainLine.getId().equals(updated.getId()) ? updated : trainLine).collect(Collectors.toList()));
		renderTrainLines();
	}
	
	private HBox createRow (TrainLine trainLine, String trainStationId) {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setSpacing(50);
		hbox.setPadding(new Insets(5));
		Button markButton = new Button("Passed");
		markButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				TrainLine updated = TrainLines.markPassed(trainLine.getId(), trainStationId);
				if (updated != null) {
				updateTrainLines(updated);
				}
			}
		});
		try {
		Boolean passed = trainLine.getStops().stream().filter((stop) -> trainStationId.equals(stop.getTrainStation().getId())).findFirst().get().isPassed();
		if (passed) markButton.setDisable(true);
		} catch(NoSuchElementException e) {
			e.printStackTrace();
		}
		hbox.getChildren().add(markButton);
		trainLine.getStops().stream().forEach((stop) -> hbox.getChildren().add
				(new Label(stop.getTrainStation().getName() + ", " + stop.getTrainStation().getCity().getName() + "  (" 
							+ stop.getExpectedTime() + ", " + ( stop.getActualTime() != null ? stop.getActualTime() : " *") + ")")));
		return hbox;
	}
}


