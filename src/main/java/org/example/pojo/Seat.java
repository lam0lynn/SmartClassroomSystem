package org.example.pojo;

import java.time.LocalDateTime;

public class Seat {
    private Integer id;
    private Integer classroomId;
    private String seatNumber;
    private String status; // 空闲/已预约/使用中
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public Seat() {}

    public Seat(Integer classroomId, String seatNumber) {
        this.classroomId = classroomId;
        this.seatNumber = seatNumber;
        this.status = "空闲";
    }

    // Getter和Setter方法
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", classroomId=" + classroomId +
                ", seatNumber='" + seatNumber + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}