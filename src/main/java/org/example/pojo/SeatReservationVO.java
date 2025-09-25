package org.example.pojo;

import java.time.LocalDateTime;

public class SeatReservationVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String realName;
    private Integer seatId;
    private String seatNumber;
    private Integer classroomId;
    private String classroomName;
    private LocalDateTime reserveTime;
    private String status;
    private LocalDateTime createTime;

    // 构造函数
    public SeatReservationVO() {}

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Integer classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public LocalDateTime getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(LocalDateTime reserveTime) {
        this.reserveTime = reserveTime;
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

    @Override
    public String toString() {
        return "SeatReservationVO{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", seatId=" + seatId +
                ", seatNumber='" + seatNumber + '\'' +
                ", classroomId=" + classroomId +
                ", classroomName='" + classroomName + '\'' +
                ", reserveTime=" + reserveTime +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}