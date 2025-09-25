package org.example.service;

import org.example.mapper.SeatMapper;
import org.example.mapper.SeatReservationMapper;
import org.example.pojo.Seat;
import org.example.pojo.SeatReservation;
 import org.example.pojo.SeatReservationVO;
import org.example.service.SeatReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatReservationServiceImpl implements SeatReservationService {

    @Autowired
    private SeatReservationMapper seatReservationMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public boolean reserveSeat(Integer userId, Integer seatId, LocalDateTime reserveTime) {
        // 检查座位是否可预约
        if (!isSeatAvailable(seatId, reserveTime, reserveTime.plusHours(2))) {
            return false;
        }

        SeatReservation reservation = new SeatReservation(userId, seatId, reserveTime);
        boolean result = seatReservationMapper.insert(reservation) > 0;

        if (result) {
            // 更新座位状态为已预约
            seatMapper.updateStatus(seatId, "已预约");
        }

        return result;
    }

    @Override
    public boolean cancelReservation(Integer reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation != null) {
            reservation.setStatus("已取消");
            boolean result = seatReservationMapper.updateStatus(reservationId, "已取消") > 0;

            if (result) {
                // 更新座位状态为空闲
                seatMapper.updateStatus(reservation.getSeatId(), "空闲");
            }

            return result;
        }
        return false;
    }

    @Override
    public boolean checkIn(Integer reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation != null) {
            reservation.setStatus("已签到");
            boolean result = seatReservationMapper.updateStatus(reservationId, "已签到") > 0;

            if (result) {
                // 更新座位状态为使用中
                seatMapper.updateStatus(reservation.getSeatId(), "使用中");
            }

            return result;
        }
        return false;
    }

    @Override
    public boolean checkOut(Integer reservationId) {
        SeatReservation reservation = seatReservationMapper.selectById(reservationId);
        if (reservation != null) {
            reservation.setStatus("已签退");
            boolean result = seatReservationMapper.updateStatus(reservationId, "已签退") > 0;

            if (result) {
                // 更新座位状态为空闲
                seatMapper.updateStatus(reservation.getSeatId(), "空闲");
            }

            return result;
        }
        return false;
    }

    @Override
    public SeatReservation getById(Integer id) {
        return seatReservationMapper.selectById(id);
    }

     @Override
     public List<SeatReservationVO> getByUserId(Integer userId) {
         // 返回带有用户、座位、教室信息的详情列表
         return seatReservationMapper.selectReservationDetails(userId);
     }

    @Override
    public List<SeatReservation> getBySeatId(Integer seatId) {
        return seatReservationMapper.selectBySeatId(seatId);
    }

    @Override
    public boolean isSeatAvailable(Integer seatId, LocalDateTime startTime, LocalDateTime endTime) {
        List<SeatReservation> conflicts = seatReservationMapper.selectConflicts(
                seatId, startTime.toString(), endTime.toString());
        return conflicts.isEmpty();
    }

    @Override
    public boolean updateStatus(Integer id, String status) {
        return seatReservationMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean update(SeatReservation seatReservation) {
        return seatReservationMapper.update(seatReservation) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return seatReservationMapper.deleteById(id) > 0;
    }

     @Override
     public List<SeatReservationVO> getAll() {
         // 管理员查看所有预约详情
         return seatReservationMapper.selectAllReservationDetails();
     }
}