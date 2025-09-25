package org.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.mapper.UserMapper;
import org.example.pojo.User;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
    private UserMapper userMapper;
	
	

    @Override
    public List<User> findUsersByRole(String role) {
        return userMapper.findUsersByRole(role);
    }

    @Override
    public List<User> findUsersByCollege(String college) {
        return userMapper.findUsersByCollege(college);
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Override
    public User findUserById(int id) {
        return userMapper.findUserById(id);
    }

    @Override
    public int insert(User user) {
        // 1. 检查用户名是否重复
        if (userMapper.findUserByName(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 设置默认值（演示用简化逻辑）
        if (user.getRole() == null) {
            user.setRole("student");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 3. 直接插入（密码不加密）
        return userMapper.insert(user);
    }

    @Override
    public int update(User user) {
        return userMapper.update(user);
    }

    @Override
    public int delete(int id) {
        return userMapper.delete(id);
    }

    @Override
    public List<User> findAll(User user) {
        return userMapper.findAll(user);
    }
    
    @Override
    public User login(String username, String password) {
        // 直接调用 Mapper 的 login 方法
        User user = userMapper.login(username, password);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }
}
