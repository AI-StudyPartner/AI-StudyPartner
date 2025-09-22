package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.Goal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FocusMapper {
    void insertGoal(Goal goal);
    List<Goal> selectById(int id);
    void updateCompleted(@Param("id")int id, @Param("completed") Boolean completed);
    void deleteGoal(int id);
}
