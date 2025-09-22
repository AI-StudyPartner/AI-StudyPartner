package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.SetFactory;
import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.Set;
import cn.edu.zjweu.cs.shuzimali.mapper.SetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/set")
public class SetController {
    @Autowired
    SetMapper setMapper;

    /**
     *
     * @param profile
     * @param preferredStudyTime
     * @param dailyFocusMinute
     * @param weeklyAvailableDays
     * @param tone
     * @param responseLength
     * @param allowEmojis
     * @param showCitations
     */
    @RequestMapping("/save")
    public void save(@RequestParam(value = "personalProfile") String profile,
                     @RequestParam(value = "preferredStudyTime") String preferredStudyTime,
                     @RequestParam(value = "dailyFocusMinute") int dailyFocusMinute ,
                     @RequestParam(value = "weeklyAvailableDays") List<String> weeklyAvailableDays,
                     @RequestParam(value = "tone") String tone,
                     @RequestParam(value = "responseLength") String responseLength,
                     @RequestParam(value = "allowEmojis") Boolean allowEmojis,
                     @RequestParam(value = "showCitations") Boolean showCitations
    ) {

        int userId = Integer.parseInt(UserFactory.getUser().getId());
        Set set = SetFactory.createSet(
                userId,
                profile,
                preferredStudyTime,
                dailyFocusMinute,
                weeklyAvailableDays,
                tone,
                responseLength,
                allowEmojis,
                showCitations
        );

        setMapper.save(set);
    }
}
