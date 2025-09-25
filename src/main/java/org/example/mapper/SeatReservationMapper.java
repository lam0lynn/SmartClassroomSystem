package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.SeatReservation;
import org.example.pojo.SeatReservationVO;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeatReservationMapper {
    // 插入座位预约
    int insert(SeatReservation seatReservation);

    // 根据ID查询
    SeatReservation selectById(@Param("id") Integer id);

    // 根据用户ID查询
    List<SeatReservation> selectByUserId(@Param("userId") Integer userId);

    // 根据座位ID查询
    List<SeatReservation> selectBySeatId(@Param("seatId") Integer seatId);

    // 根据座位ID和时间查询冲突预约
    List<SeatReservation> selectConflicts(@Param("seatId") Integer seatId,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);

    // 更新预约状态
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    // 更新预约信息
    int update(SeatReservation seatReservation);

    // 删除预约
    int deleteById(@Param("id") Integer id);

    // 查询所有预约
    List<SeatReservation> selectAll();

    // 根据状态查询预约
    List<SeatReservation> selectByStatus(@Param("status") String status);

    // 根据用户ID和状态查询
    List<SeatReservation> selectByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status);

    // 查询预约详情（关联用户和座位信息）
    List<SeatReservationVO> selectReservationDetails(@Param("userId") Integer userId);

    // 查询所有预约详情（管理员用）
    List<SeatReservationVO> selectAllReservationDetails();

    // 根据预约时间范围查询
    List<SeatReservation> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    // 根据座位ID和时间范围查询
    List<SeatReservation> selectBySeatIdAndTimeRange(@Param("seatId") Integer seatId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
}