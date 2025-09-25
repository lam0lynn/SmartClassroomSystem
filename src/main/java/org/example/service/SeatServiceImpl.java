package org.example.service;

import org.example.mapper.SeatMapper;
import org.example.pojo.Seat;
import org.example.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public boolean addSeat(Seat seat) {
        return seatMapper.insert(seat) > 0;
    }

    @Override
    public boolean addSeats(Integer classroomId, List<String> seatNumbers) {
        for (String seatNumber : seatNumbers) {
            Seat seat = new Seat(classroomId, seatNumber);
            if (seatMapper.insert(seat) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Seat getById(Integer id) {
        return seatMapper.selectById(id);
    }

    @Override
    public List<Seat> getByClassroomId(Integer classroomId) {
        return seatMapper.selectByClassroomId(classroomId);
    }

    @Override
    public List<Seat> getByClassroomIdAndStatus(Integer classroomId, String status) {
        return seatMapper.selectByClassroomIdAndStatus(classroomId, status);
    }

    @Override
    public boolean updateStatus(Integer id, String status) {
        return seatMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean update(Seat seat) {
        return seatMapper.update(seat) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return seatMapper.deleteById(id) > 0;
    }

    @Override
    public List<Seat> getAll() {
        return seatMapper.selectAll();
    }
}