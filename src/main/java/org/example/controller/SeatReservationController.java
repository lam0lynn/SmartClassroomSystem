package org.example.controller;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.pojo.SeatReservation;
import org.example.pojo.SeatReservationVO;
import org.example.service.SeatReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/seat-reservations")
@Mapper
public class SeatReservationController {

    @Autowired
    private SeatReservationService seatReservationService;

    // 根据ID查询座位预约
    @GetMapping("/{id}")
    public SeatReservation findSeatReservationById(@PathVariable Integer id) {
        return seatReservationService.getById(id);
    }

    // 根据用户ID查询座位预约
    @GetMapping("/user/{userId}")
    public List<SeatReservationVO> findSeatReservationsByUserId(@PathVariable Integer userId) {
        return seatReservationService.getByUserId(userId);
    }

    // 根据座位ID查询座位预约
    @GetMapping("/seat/{seatId}")
    public List<SeatReservation> findSeatReservationsBySeatId(@PathVariable Integer seatId) {
        return seatReservationService.getBySeatId(seatId);
    }

    // 预约座位
    @PostMapping("/reserve")
    public void reserveSeat(@RequestBody Map<String, Object> request) {
        Integer userId = (Integer) request.get("userId");
        Integer seatId = (Integer) request.get("seatId");
        String reserveTimeStr = (String) request.get("reserveTime");
        LocalDateTime reserveTime = LocalDateTime.parse(reserveTimeStr);
        seatReservationService.reserveSeat(userId, seatId, reserveTime);
    }

    // 取消预约
    @PostMapping("/cancel/{reservationId}")
    public void cancelReservation(@PathVariable Integer reservationId) {
        seatReservationService.cancelReservation(reservationId);
    }

    // 签到
    @PostMapping("/checkin/{reservationId}")
    public void checkIn(@PathVariable Integer reservationId) {
        seatReservationService.checkIn(reservationId);
    }

    // 签退
    @PostMapping("/checkout/{reservationId}")
    public void checkOut(@PathVariable Integer reservationId) {
        seatReservationService.checkOut(reservationId);
    }

    // 检查座位是否可预约
    @PostMapping("/check-availability")
    public boolean checkSeatAvailability(@RequestBody Map<String, Object> request) {
        Integer seatId = (Integer) request.get("seatId");
        String startTimeStr = (String) request.get("startTime");
        String endTimeStr = (String) request.get("endTime");
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        return seatReservationService.isSeatAvailable(seatId, startTime, endTime);
    }

    // 更新座位预约状态
    @PutMapping("/{id}/status/{status}")
    public void updateSeatReservationStatus(@PathVariable Integer id, @PathVariable String status) {
        seatReservationService.updateStatus(id, status);
    }

    // 更新座位预约信息
    @PutMapping("/{id}")
    public void updateSeatReservation(@PathVariable Integer id, @RequestBody SeatReservation seatReservation) {
        seatReservation.setId(id);
        seatReservationService.update(seatReservation);
    }

    // 删除座位预约
    @DeleteMapping("/{id}")
    public void deleteSeatReservation(@PathVariable Integer id) {
        seatReservationService.deleteById(id);
    }

    // 查询所有座位预约
    @GetMapping("")
    public List<SeatReservationVO> getAllSeatReservations() {
        return seatReservationService.getAll();
    }
}