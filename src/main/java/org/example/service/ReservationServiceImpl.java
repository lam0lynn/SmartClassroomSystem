package org.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.mapper.ReservationMapper;
import org.example.pojo.Reservation;

@Service
public class ReservationServiceImpl implements ReservationService{

	    @Autowired
	    private ReservationMapper reservationMapper;

	    // 更新预约信息
	    @Override
	    public int update(Reservation rv) {
	        return reservationMapper.update(rv);
	    }

	    // 删除预约
	    @Override
	    public int delete(int id) {
	        return reservationMapper.delete(id);
	    }

	    // 根据条件动态查询预约
	    @Override
	    public List<Reservation> findAllReservations(Reservation rv) {
	        return reservationMapper.findAllReservations(rv);
	    }

	    // 根据状态查询预约
	    @Override
	    public List<Reservation> findReservationsByStatus(String status) {
	        return reservationMapper.findReservationsByStatus(status);
	    }

	    // 根据ID查询预约
	    @Override
	    public Reservation findById(int id) {
	        return reservationMapper.findById(id);
	    }

	    // 根据教室ID查询预约
	    @Override
	    public List<Reservation> findReservationsByClassroomId(int classroomId) {
	        return reservationMapper.findReservationsByClassroomId(classroomId);
	    }

	    // 根据用户ID查询预约
	    @Override
	    public List<Reservation> findReservationsByUserId(int userId) {
	        return reservationMapper.findReservationsByUserId(userId);
	    }

	    // 新增预约
	    @Override
	    public int insert(Reservation rv) {
	        return reservationMapper.insert(rv);
	    }
	}

