package org.example.service;

import java.util.List;

import org.example.pojo.Reservation;

public interface ReservationService {
	 // 更新预约信息
    public int update(Reservation rv);
    
    // 删除预约
    public int delete(int id);
    
    // 根据条件动态查询预约
    public List<Reservation> findAllReservations( Reservation rv);
    
    // 根据状态查询预约
    public List<Reservation> findReservationsByStatus( String status);
    
    // 根据ID查询预约
    public Reservation findById( int id);
    
    
    // 根据教室ID查询预约
    public List<Reservation> findReservationsByClassroomId( int classroomId);
    
    // 根据用户ID查询预约
    public List<Reservation> findReservationsByUserId(int userId);

    // 新增预约
    public int insert(Reservation rv);
}
