package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.Set;

public interface SetMapper {
    Set selectSet(int userId);
    void save(Set set);
}
