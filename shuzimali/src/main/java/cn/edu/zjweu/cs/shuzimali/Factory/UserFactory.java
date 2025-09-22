package cn.edu.zjweu.cs.shuzimali.Factory;

import cn.edu.zjweu.cs.shuzimali.entity.User;
import lombok.Getter;

public class UserFactory {

    private static User user = new User();
    public static User getUser() {return user;}
}
