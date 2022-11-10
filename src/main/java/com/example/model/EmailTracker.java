package com.example.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;



@Entity
@Table(name = "email_tracker")
public class EmailTracker {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id_email_tracker")
	private Integer idEmailTracker;
	@ManyToOne
	@JoinColumn(name = "fk_id_email", referencedColumnName = "id_email")
	private Integer fkIdEmail;
	@Column(name = "action_time")
	private String actionType;
	@Column(name = "is_captured")
	private Boolean isCaptured;
	@Column(name = "captured_time")
	private Timestamp capturedTime;

	public Integer getIdEmailTracker() {
		return idEmailTracker;
	}

	public void setIdEmailTracker(Integer idEmailTracker) {
		this.idEmailTracker = idEmailTracker;
	}

	public Integer getFkIdEmail() {
		return fkIdEmail;
	}

	public void setFkIdEmail(Integer fkIdEmail) {
		this.fkIdEmail = fkIdEmail;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Boolean getIsCaptured() {
		return isCaptured;
	}

	public void setIsCaptured(Boolean isCaptured) {
		this.isCaptured = isCaptured;
	}

	public Timestamp getCapturedTime() {
		return capturedTime;
	}

	public void setCapturedTime(Timestamp capturedTime) {
		this.capturedTime = capturedTime;
	}

}
