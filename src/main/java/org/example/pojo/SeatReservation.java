package org.example.pojo;

import java.time.LocalDateTime;

public class SeatReservation {
    private Integer id;
    private Integer userId;
    private Integer seatId;
    private LocalDateTime reserveTime;
    private String status; // 已预约/已签到/已签退/已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public SeatReservation() {}

    public SeatReservation(Integer userId, Integer seatId, LocalDateTime reserveTime) {
        this.userId = userId;
        this.seatId = seatId;
        this.reserveTime = reserveTime;
        this.status = "已预约";
    }

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

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "SeatReservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", seatId=" + seatId +
                ", reserveTime=" + reserveTime +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}