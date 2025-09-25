package org.example.controller;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.example.pojo.Classroom;
import org.example.service.ClassroomService;
@CrossOrigin
@RestController
@RequestMapping("/classrooms")


@Mapper
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    // 根据教室名称查询教室
    @GetMapping("/name/{name}")
    public Classroom findClassroomByName(@PathVariable String name) {
        return classroomService.findClassroomByName(name);
    }

    // 根据ID查询教室
    @GetMapping("/{id}")
    public Classroom findClassroomById(@PathVariable Integer id) {
        return classroomService.findClassroomById(id);
    }

    // 插入新教室
    @PostMapping("/add")
    public void addClassroom(@RequestBody Classroom classroom) {
        classroomService.insert(classroom);
    }

    // 更新教室信息
    @PutMapping("/{id}")
    public void updateClassroom(@PathVariable Integer id, @RequestBody Classroom classroom) {
        classroom.setId(id);
        classroomService.update(classroom);
    }

    // 删除教室
    @DeleteMapping("/{id}")
    public void deleteClassroom(@PathVariable Integer id) {
        classroomService.delete(id);
    }

    // 动态查询教室
    @PostMapping("/search")
    public List<Classroom> findAllClassroom(@RequestBody Classroom classroom) {
        return classroomService.findAllClassroom(classroom);
    }

    //  查找所有类型
    @GetMapping("/type")
    public List<String> findAllClassroomType() {
        return classroomService.findAllClassroomType();
    }

    // 根据类型查询教室
    @GetMapping("/type/{type}")
    public List<Classroom> findClassroomsByType(@PathVariable String type) {
        Classroom classroom = new Classroom();
        classroom.setType(type);
        return classroomService.findClassroomsByType(classroom);
    }

    //  查找所有状态
    @GetMapping("/status")
    public List<String> findAllClassroomsStatus() {
        return classroomService.findAllClassroomsStatus();
    }

    // 根据状态查询教室
    @GetMapping("/status/{status}")
    public List<Classroom> findClassroomsByStatus(@PathVariable String status) {
        Classroom classroom = new Classroom();
        classroom.setStatus(status);
        return classroomService.findClassroomsByStatus(classroom);
    }

    // 根据楼栋查询教室
    @GetMapping("/building/{building}")
    public List<Classroom> findClassroomsByBuilding(@PathVariable String building) {
        Classroom classroom = new Classroom();
        classroom.setBuilding(building);
        return classroomService.findClassroomsByBuilding(classroom);
    }

    // 根据设备配置查询教室
    @PostMapping("/equipment")
    public List<Classroom> findClassroomsByEquipment(@RequestBody Classroom classroom) {
        return classroomService.findClassroomsByEquipment(classroom);
    }

    // 查询所有教室
    @GetMapping("")
    public List<Classroom> getAllClassrooms() {
        return classroomService.findAllClassroom(null);
    }
}