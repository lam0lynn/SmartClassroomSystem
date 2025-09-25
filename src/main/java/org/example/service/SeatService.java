package org.example.service;

import org.example.pojo.Seat;
import java.util.List;

public interface SeatService {
    // 添加座位
    boolean addSeat(Seat seat);

    // 批量添加座位
    boolean addSeats(Integer classroomId, List<String> seatNumbers);

    // 根据ID查询
    Seat getById(Integer id);

    // 根据教室ID查询所有座位
    List<Seat> getByClassroomId(Integer classroomId);

    // 根据教室ID和状态查询座位
    List<Seat> getByClassroomIdAndStatus(Integer classroomId, String status);

    // 更新座位状态
    boolean updateStatus(Integer id, String status);

    // 更新座位信息
    boolean update(Seat seat);

    // 删除座位
    boolean deleteById(Integer id);

    // 查询所有座位
    List<Seat> getAll();
}