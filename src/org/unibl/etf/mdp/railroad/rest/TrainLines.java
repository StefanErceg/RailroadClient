package org.unibl.etf.mdp.railroad.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unibl.etf.mdp.railroad.model.TrainLine;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TrainLines {
	public static final String base = Root.base + "/trainLines";

	public static ArrayList<TrainLine> getTrainLineByTrainStation(String trainStationId) {
		try {
			JSONArray array = Utils.readArray(base + "/byTrainStation/" + trainStationId);
			Gson gson = new Gson();
			ArrayList<TrainLine> trainLines = new ArrayList<TrainLine>();
			if (array != null) {
				for (int index = 0; index < array.length(); index++) {
					trainLines.add(gson.fromJson(array.optJSONObject(index).toString(), TrainLine.class));
				}
			}
			return trainLines;
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static TrainLine markPassed(String trainLineId, String trainStationId) {
		try {
			URL url = new URL(base + "/" + trainLineId + "/" + trainStationId);
			Gson gson = new Gson();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", "application/json");
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new Exception("Error happened while updating train line");
			} else {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
					String line = null;
					String data = "";
					while ((line = reader.readLine()) != null) {
						data+=line;
					}
					TrainLine trainLine = gson.fromJson(new JSONObject(data).toString(),TrainLine.class);
					return trainLine;
				}
				catch (Exception e) { 
					e.printStackTrace();
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
