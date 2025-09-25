package org.example.controller;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.pojo.Reservation;
import org.example.service.ReservationService;
@CrossOrigin
@RestController
@RequestMapping("/reservations")


@Mapper
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // 更新预约信息
    @PutMapping("/{id}")
    public int update(@PathVariable Integer id, @RequestBody Reservation rv) {
        rv.setId(id);
        return reservationService.update(rv);
    }

    // 删除预约
    @DeleteMapping("/{id}")
    public int delete(@PathVariable Integer id) {
        return reservationService.delete(id);
    }

    // 根据条件动态查询预约
    @GetMapping("")
    public List<Reservation> findAllReservations(@ModelAttribute Reservation rv) {
        return reservationService.findAllReservations(rv);
    }

    // 根据状态查询预约
    @GetMapping("/status/{status}")
    public List<Reservation> findReservationsByStatus(@PathVariable String status) {
        return reservationService.findReservationsByStatus(status);
    }

    // 根据ID查询预约
    @GetMapping("/{id}")
    public Reservation findById(@PathVariable Integer id) {
        return reservationService.findById(id);
    }

    // 根据教室ID查询预约
    @GetMapping("/classroom/{classroomId}")
    public List<Reservation> findReservationsByClassroomId(@PathVariable Integer classroomId) {
        return reservationService.findReservationsByClassroomId(classroomId);
    }

    // 根据用户ID查询预约
    @GetMapping("/user/{userId}")
    public List<Reservation> findReservationsByUserId(@PathVariable Integer userId) {
        return reservationService.findReservationsByUserId(userId);
    }

    // 添加新预约
    @PostMapping("/add")
    public int addReservation(@RequestBody Reservation reservation) {
        return reservationService.insert(reservation);
    }
}



