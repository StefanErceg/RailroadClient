package org.unibl.etf.mdp.railroad.model;

import java.util.Objects;

public class ChatMessage {
	private Integer type;
	private boolean sent; 
	private String content;
	private String time;
	
	public ChatMessage(Integer type, boolean sent, String content, String time) {
		super();
		this.type = type;
		this.sent = sent;
		this.content = content;
		this.time = time;
	}

	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @return the sent
	 */
	public boolean isSent() {
		return sent;
	}

	/**
	 * @param sent the sent to set
	 */
	public void setSent(boolean sent) {
		this.sent = sent;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, sent, time);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ChatMessage)) {
			return false;
		}
		ChatMessage other = (ChatMessage) obj;
		return Objects.equals(content, other.content) && sent == other.sent && Objects.equals(time, other.time);
	}

	@Override
	public String toString() {
		return "ChatMessage [sent=" + sent + ", content=" + content + ", time=" + time + "]";
	}
	
	
	
	
}
