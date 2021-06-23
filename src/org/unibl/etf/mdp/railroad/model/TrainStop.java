package org.unibl.etf.mdp.railroad.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TrainStop implements Serializable {
	
	private static final long serialVersionUID = 3262719492355624614L;
	
	private TrainStation trainStation;
	private Date expectedTime;
	private boolean passed;
	private Date actualTime;
	
	public TrainStop(TrainStation trainStation, Date expectedTime, boolean passed, Date actualTime) {
		super();
		this.trainStation = trainStation;
		this.expectedTime = expectedTime;
		this.passed = passed;
		this.actualTime = actualTime;
	}

	/**
	 * @return the trainStation
	 */
	public TrainStation getTrainStation() {
		return trainStation;
	}

	/**
	 * @param trainStation the trainStation to set
	 */
	public void setTrainStation(TrainStation trainStation) {
		this.trainStation = trainStation;
	}

	/**
	 * @return the expectedTime
	 */
	public Date getExpectedTime() {
		return expectedTime;
	}

	/**
	 * @param expectedTime the expectedTime to set
	 */
	public void setExpectedTime(Date expectedTime) {
		this.expectedTime = expectedTime;
	}

	/**
	 * @return the passed
	 */
	public boolean isPassed() {
		return passed;
	}

	/**
	 * @param passed the passed to set
	 */
	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	/**
	 * @return the actualTime
	 */
	public Date getActualTime() {
		return actualTime;
	}

	/**
	 * @param actualTime the actualTime to set
	 */
	public void setActualTime(Date actualTime) {
		this.actualTime = actualTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actualTime, expectedTime, passed, trainStation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TrainStop)) {
			return false;
		}
		TrainStop other = (TrainStop) obj;
		return Objects.equals(actualTime, other.actualTime) && Objects.equals(expectedTime, other.expectedTime)
				&& passed == other.passed && Objects.equals(trainStation, other.trainStation);
	}

	@Override
	public String toString() {
		return "TrainStop [trainStation=" + trainStation + ", expectedTime=" + expectedTime + ", passed=" + passed
				+ ", actualTime=" + actualTime + "]";
	}
	
	
	
}
