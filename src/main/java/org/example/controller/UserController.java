package org.example.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.example.pojo.User;
import org.example.service.UserService;

//根据service来
@CrossOrigin
@RestController
@RequestMapping("/users")



public class UserController {
	
	@Autowired
	private UserService userService;

    // 添加新用户
    @PostMapping("/add")
    public void addUser(@RequestBody User user) {
        userService.insert(user);
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public void updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.delete(id);
    }

    // 根据角色查询用户
    @GetMapping("/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.findUsersByRole(role);
    }

    // 根据学院查询用户
    @GetMapping("/college/{college}")
    public List<User> getUsersByCollege(@PathVariable String college) {
        return userService.findUsersByCollege(college);
    }

    // 根据用户名查询用户
    @GetMapping("/name/{username}")
    public User getUserByName(@PathVariable String username) {
        return userService.findUserByName(username);
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        return userService.findUserById(id);
    }

    // 动态查询用户
    @PostMapping("/search")
    public List<User> searchUsers(@RequestBody User user) {
        return userService.findAll(user);
    }
	
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            userService.insert(user);
            return ResponseEntity.ok("注册成功，用户ID: " + user.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.login(params.get("username"), params.get("password"));
            session.setAttribute("currentUser", user);
            response.put("success", true);
            response.put("user", user);
            return response;
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.removeAttribute("currentUser"); // 清除Session
        return ResponseEntity.ok("退出成功");
    }
	
    @GetMapping("/test")
    public String test() {
        return "ok";
    }

    // 查询所有用户
    @GetMapping("")
    public List<User> getAllUsers() {
        return userService.findAll(null);
    }

    @PutMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(params.get("userId"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            // 1. 查询用户
            User user = userService.findUserById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            // 2. 校验原密码
            if (!user.getPassword().equals(oldPassword)) {
                response.put("success", false);
                response.put("message", "原密码错误");
                return response;
            }
            // 3. 修改密码
            user.setPassword(newPassword);
            userService.update(user);
            response.put("success", true);
            response.put("message", "修改成功");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修改失败：" + e.getMessage());
            return response;
        }
    }

}
