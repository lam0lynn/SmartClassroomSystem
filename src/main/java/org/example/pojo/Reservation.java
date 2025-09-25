package org.example.pojo;

import java.sql.Date;

public class Reservation {
	private Integer id;
    private Integer classroomId;
    private Integer userId;
    private Date startTime;
    private Date endTime;
    private String purpose;
    private Integer participants;
    private String status;
    private Integer approverId;
    private Date approveTime;
    private String rejectReason;
    private Date checkInTime;
    private Date checkOutTime;
    private Integer actualParticipants;
    private Date createTime;
    private Date updateTime;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getClassroomId() {
		return classroomId;
	}
	public void setClassroomId(Integer classroomId) {
		this.classroomId = classroomId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public Integer getParticipants() {
		return participants;
	}
	public void setParticipants(Integer participants) {
		this.participants = participants;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getApproverId() {
		return approverId;
	}
	public void setApproverId(Integer approverId) {
		this.approverId = approverId;
	}
	public Date getApproveTime() {
		return approveTime;
	}
	public void setApproveTime(Date approveTime) {
		this.approveTime = approveTime;
	}
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	public Date getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	public Date getCheckOutTime() {
		return checkOutTime;
	}
	public void setCheckOutTime(Date checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
	public Integer getActualParticipants() {
		return actualParticipants;
	}
	public void setActualParticipants(Integer actualParticipants) {
		this.actualParticipants = actualParticipants;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
    
    

}
