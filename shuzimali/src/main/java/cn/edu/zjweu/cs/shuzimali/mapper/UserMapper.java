package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface UserMapper {
    List<User> selectUser();
    int register(User user);
}
