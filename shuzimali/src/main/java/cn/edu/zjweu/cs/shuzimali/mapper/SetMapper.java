package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.Set;
import org.apache.ibatis.annotations.Param;

public interface SetMapper {
    Set selectSet(int userId);
    void save(Set set);
    void update(@Param("userId") int userId, @Param("userSet") Set set);
}
