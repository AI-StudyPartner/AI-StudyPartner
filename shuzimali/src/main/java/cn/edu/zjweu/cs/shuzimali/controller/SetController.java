package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.SetFactory;
import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.Set;
import cn.edu.zjweu.cs.shuzimali.mapper.SetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/set")
public class SetController {
    @Autowired
    SetMapper setMapper;
    @GetMapping("/show")
    public Set show() {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        Set set = setMapper.selectSet(userId);
        return set;
    }
    /**
     *
     */
    @RequestMapping("/save")
    public void save(@RequestBody Set set) {
        int userId = Integer.parseInt(UserFactory.getUser().getId());
        set.setUserId(userId);
        Set userSet = setMapper.selectSet(userId);
        if (userSet != null){
            setMapper.update(userId, set);
        } else {
            setMapper.save(set);
        }
    }
}
