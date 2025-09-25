package org.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.mapper.ClassroomMapper;
import org.example.pojo.Classroom;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomMapper classroomMapper;

    @Override
    public Classroom findClassroomByName(String classroomName) {
        return classroomMapper.findClassroomByName(classroomName);
    }

    @Override
    public Classroom findClassroomById(int id) {
        return classroomMapper.findClassroomById(id);
    }

    @Override
    public int insert(Classroom cs) {
        return classroomMapper.insert(cs);
    }

    @Override
    public int update(Classroom cs) {
        return classroomMapper.update(cs);  
    }

    @Override
    public int delete(int id) {
        return classroomMapper.delete(id);
    }

    @Override
    public List<Classroom> findAllClassroom(Classroom cs) {
        return classroomMapper.findAllClassroom(cs);
    }

    @Override
    public List<String> findAllClassroomType() {
        return classroomMapper.findAllClassroomType();
    }

    @Override
    public List<Classroom> findClassroomsByType(Classroom cs) {
        return classroomMapper.findClassroomsByType(cs);
    }

    @Override
    public List<Classroom> findClassroomsByStatus(Classroom cs) {
        return classroomMapper.findClassroomsByStatus(cs);
    }

    @Override
    public List<Classroom> findClassroomsByBuilding(Classroom cs) {
        return classroomMapper.findClassroomsByBuilding(cs);
    }

    @Override
    public List<Classroom> findClassroomsByEquipment(Classroom cs) {
        return classroomMapper.findClassroomsByEquipment(cs);
    }

    @Override
    public List<Classroom> findAll() {
        return classroomMapper.findAll();
    }

    @Override
    public List<String> findAllClassroomsStatus() {
        return classroomMapper.findAllClassroomsStatus();
    }
}