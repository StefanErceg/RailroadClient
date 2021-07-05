package org.unibl.etf.mdp.railroad.chat;

import java.io.Serializable;
import java.util.Objects;

public class ChatUser implements Serializable {

	private static final long serialVersionUID = 4888234790162636287L;
	private String username;
	private String trainStationId;
	
	public ChatUser(String username, String trainStationId) {
		super();
		this.username = username;
		this.trainStationId = trainStationId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the trainStationId
	 */
	public String getTrainStationId() {
		return trainStationId;
	}

	/**
	 * @param trainStationId the trainStationId to set
	 */
	public void setTrainStationId(String trainStationId) {
		this.trainStationId = trainStationId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(trainStationId, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ChatUser)) {
			return false;
		}
		ChatUser other = (ChatUser) obj;
		return Objects.equals(trainStationId, other.trainStationId) && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "ChatUser [username=" + username + ", trainStationId=" + trainStationId + "]";
	}
	
	
	
	

}
