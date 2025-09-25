package org.example.service;

import java.util.List;

import org.example.pojo.User;

public interface UserService {
	
public List<User> findUsersByRole(String role);
    
	public List<User> findUsersByCollege(String college);
    
	public User findUserByName(String username);
	
	public User findUserById(int id);
	
	public int insert(User user);
	
	public int update(User user);
	
	public int delete(int id);
	
	public List<User> findAll(User user);
	
	public User login(String username, String password);

}
