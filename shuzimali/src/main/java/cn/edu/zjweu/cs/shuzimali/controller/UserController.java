package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.User;
import cn.edu.zjweu.cs.shuzimali.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Resource
    UserMapper userMapper;
    @GetMapping(value = "/login")
    public  List<User> login(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
        List<User> users = userMapper.selectUser();
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                User userPro = UserFactory.getUser();
                userPro.setId(user.getId());
            }
        }
        return users;
    }
    @GetMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password) {
        User user = UserFactory.getUser();
        user.setEmail(email);
        user.setPassword(password);
        
        int result = userMapper.register(user);
        
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("message", "注册成功");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "注册失败");
            return ResponseEntity.status(500).body(response);
        }
    }
}
