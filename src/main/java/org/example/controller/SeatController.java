package org.example.controller;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.pojo.Seat;
import org.example.service.SeatService;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/seats")
@Mapper
public class SeatController {

    @Autowired
    private SeatService seatService;

    // 根据ID查询座位
    @GetMapping("/{id}")
    public Seat findSeatById(@PathVariable Integer id) {
        return seatService.getById(id);
    }

    // 根据教室ID查询座位
    @GetMapping("/classroom/{classroomId}")
    public List<Seat> findSeatsByClassroomId(@PathVariable Integer classroomId) {
        return seatService.getByClassroomId(classroomId);
    }

    // 根据教室ID和状态查询座位
    @GetMapping("/classroom/{classroomId}/status/{status}")
    public List<Seat> findSeatsByClassroomIdAndStatus(@PathVariable Integer classroomId, @PathVariable String status) {
        return seatService.getByClassroomIdAndStatus(classroomId, status);
    }

    // 插入新座位
    @PostMapping("/add")
    public void addSeat(@RequestBody Seat seat) {
        seatService.addSeat(seat);
    }

    // 批量添加座位
    @PostMapping("/batch-add")
    public void batchAddSeats(@RequestBody Map<String, Object> request) {
        Integer classroomId = (Integer) request.get("classroomId");
        @SuppressWarnings("unchecked")
        List<String> seatNumbers = (List<String>) request.get("seatNumbers");
        seatService.addSeats(classroomId, seatNumbers);
    }

    // 更新座位信息
    @PutMapping("/{id}")
    public void updateSeat(@PathVariable Integer id, @RequestBody Seat seat) {
        seat.setId(id);
        seatService.update(seat);
    }

    // 更新座位状态
    @PutMapping("/{id}/status/{status}")
    public void updateSeatStatus(@PathVariable Integer id, @PathVariable String status) {
        seatService.updateStatus(id, status);
    }

    // 删除座位
    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Integer id) {
        seatService.deleteById(id);
    }

    // 查询所有座位
    @GetMapping("")
    public List<Seat> getAllSeats() {
        return seatService.getAll();
    }
}