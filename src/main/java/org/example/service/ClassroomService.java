package org.example.service;

import java.util.List;

import org.example.pojo.Classroom;

public interface ClassroomService {
	
	public Classroom findClassroomByName(String classroomName);
	
	public Classroom findClassroomById(int id);
	
	public int insert(Classroom cs);
	
	public int update(Classroom cs);
	
	public int delete(int id);
	
	public List<Classroom> findAllClassroom(Classroom cs);
	
	public List<Classroom> findClassroomsByType(Classroom cs);
	
	public List<Classroom> findClassroomsByStatus(Classroom cs);

	public List<Classroom> findClassroomsByBuilding(Classroom cs);
	
	public List<Classroom> findClassroomsByEquipment(Classroom cs);

	public List<Classroom> findAll();

	public List<String> findAllClassroomType();

	public List<String> findAllClassroomsStatus();

}