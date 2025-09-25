package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Seat;
import java.util.List;

@Mapper
public interface SeatMapper {
    // 插入座位
    int insert(Seat seat);

    // 根据ID查询
    Seat selectById(@Param("id") Integer id);

    // 根据教室ID查询所有座位
    List<Seat> selectByClassroomId(@Param("classroomId") Integer classroomId);

    // 根据教室ID和状态查询座位
    List<Seat> selectByClassroomIdAndStatus(@Param("classroomId") Integer classroomId, @Param("status") String status);

    // 根据座位编号查询
    Seat selectBySeatNumber(@Param("classroomId") Integer classroomId, @Param("seatNumber") String seatNumber);

    // 更新座位状态
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    // 更新座位信息
    int update(Seat seat);

    // 删除座位
    int deleteById(@Param("id") Integer id);

    // 查询所有座位
    List<Seat> selectAll();

    // 根据状态查询座位
    List<Seat> selectByStatus(@Param("status") String status);

    // 批量插入座位
    int batchInsert(@Param("seats") List<Seat> seats);

    // 根据教室ID删除所有座位
    int deleteByClassroomId(@Param("classroomId") Integer classroomId);
}