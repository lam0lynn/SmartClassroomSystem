package org.example.pojo;

import java.sql.Date;
import java.util.List;

public class Review {
	private Integer id;
    private Integer classroomId;
    private Integer userId;
    private Integer reservationId;
    private Integer rating;
    private String content;
    private Integer equipmentRating;
    private Integer cleanlinessRating;
    private String images; 
    private Integer status;
    private Date createTime;

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
	public Integer getReservationId() {
		return reservationId;
	}
	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getEquipmentRating() {
		return equipmentRating;
	}
	public void setEquipmentRating(Integer equipmentRating) {
		this.equipmentRating = equipmentRating;
	}
	public Integer getCleanlinessRating() {
		return cleanlinessRating;
	}
	public void setCleanlinessRating(Integer cleanlinessRating) {
		this.cleanlinessRating = cleanlinessRating;
	}
	public String getImages() {
		return images;
	}
	public void setImages(String images) {
		this.images = images;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
    
    

}
