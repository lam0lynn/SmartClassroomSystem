package org.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.example.pojo.User;

@Mapper
public interface UserMapper  {
	

//	分类查询
	public List<User> findUsersByRole(String role);
    
	public List<User> findUsersByCollege(String college);
    
	public User findUserByName(String username);
	
	public User findUserById(int id);
	
	public int insert(User user);
	
	public int update(User user);
	
	public int delete(int id);
	
	public List<User> findAll(User user);

	User login(@Param("username") String username, @Param("password") String password);

}
