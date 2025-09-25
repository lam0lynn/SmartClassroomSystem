package org.example.service;

import org.example.pojo.SeatReservation;
import org.example.pojo.SeatReservationVO;
import java.time.LocalDateTime;
import java.util.List;

public interface SeatReservationService {
    // 预约座位
    boolean reserveSeat(Integer userId, Integer seatId, LocalDateTime reserveTime);

    // 取消预约
    boolean cancelReservation(Integer reservationId);

    // 签到
    boolean checkIn(Integer reservationId);

    // 签退
    boolean checkOut(Integer reservationId);

    // 根据ID查询
    SeatReservation getById(Integer id);

    // 根据用户ID查询
    List<SeatReservationVO> getByUserId(Integer userId);

    // 根据座位ID查询
    List<SeatReservation> getBySeatId(Integer seatId);

    // 检查座位是否可预约
    boolean isSeatAvailable(Integer seatId, LocalDateTime startTime, LocalDateTime endTime);

    // 更新预约状态
    boolean updateStatus(Integer id, String status);

    // 更新预约信息
    boolean update(SeatReservation seatReservation);

    // 删除预约
    boolean deleteById(Integer id);

    // 查询所有预约（管理员用）
    List<SeatReservationVO> getAll();
}