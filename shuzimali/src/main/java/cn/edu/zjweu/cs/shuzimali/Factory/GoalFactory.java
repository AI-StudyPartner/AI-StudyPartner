package cn.edu.zjweu.cs.shuzimali.Factory;

import cn.edu.zjweu.cs.shuzimali.entity.Goal;

public class GoalFactory {
    public static Goal createShortGoal(String shortGoalContent ,int id,String title){
        Goal shortGoal = new Goal();
        shortGoal.setGoalContent(shortGoalContent);
        shortGoal.setTitle(title);
        shortGoal.setUserId(id);
        shortGoal.setCompleted(false);
        shortGoal.setGoalType("short");
        return shortGoal;
    }
    public static Goal createLongGoal(String longGoalContent ,int id, String title){
        Goal longGoal = new Goal();
        longGoal.setGoalContent(longGoalContent);
        longGoal.setTitle(title);
        longGoal.setUserId(id);
        longGoal.setGoalType("long");
        longGoal.setCompleted(false);
        return longGoal;
    }
}
