package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.GoalFactory;
import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.Goal;
import cn.edu.zjweu.cs.shuzimali.entity.User;
import cn.edu.zjweu.cs.shuzimali.mapper.FocusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/focus")
public class FocusController {
    @Autowired
    private FocusMapper focusMapper;

    @GetMapping("show-goal")
    public List<Goal> showGoal(){
        User user = UserFactory.getUser();
        int id = Integer.parseInt(user.getId());
        List<Goal> goal = focusMapper.selectById(id);
        return goal;
    }

    @GetMapping("/short-goal")
    public int shortGoal(@RequestParam(value = "shortGoal") String shortGoal
            ,@RequestParam(value = "title") String title){
        User user = UserFactory.getUser();
        int id = Integer.parseInt(user.getId());
        Goal goal = GoalFactory.createShortGoal(shortGoal, id, title);
        focusMapper.insertGoal(goal);
        return goal.getGoalId();
    }
    @GetMapping("/long-goal")
    public int longGoal(@RequestParam(value = "longGoal") String longGoal
            ,@RequestParam(value = "title") String title){
        User user = UserFactory.getUser();
        int id = Integer.parseInt(user.getId());
        Goal goal = GoalFactory.createLongGoal(longGoal, id, title);
        focusMapper.insertGoal(goal);
        return goal.getGoalId();
    }
    @GetMapping("/completed")
    public void completed(@RequestParam(value = "goalId") int goalId
            ,@RequestParam(value = "completed") Boolean completed){
        focusMapper.updateCompleted(goalId,  completed);
    }
    @GetMapping("/delete-goal")
    public void deleteGoal(@RequestParam(value = "goalId") int goalId){
        focusMapper.deleteGoal(goalId);
    }
}
