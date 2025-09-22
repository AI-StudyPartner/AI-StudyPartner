package cn.edu.zjweu.cs.shuzimali.Factory;

import cn.edu.zjweu.cs.shuzimali.entity.Set;

import java.util.List;

public class SetFactory {
    public static Set createSet(
            int id,
            String profile, String preferredStudyTime,
            int dailyFocusMinute, List<String> weeklyAvailableDays,
            String tone, String responseLength,
            Boolean allowEmojis, Boolean showCitations){
        Set set = new Set();
        set.setUserId(id);
        set.setPersonalProfile(profile);
        set.setPreferredStudyTime(preferredStudyTime);
        set.setDailyFocusMinute(dailyFocusMinute);
        set.setWeeklyAvailableDays(weeklyAvailableDays);
        set.setTone(tone);
        set.setResponseLength(responseLength);
        set.setAllowEmojis(allowEmojis);
        set.setShowCitations(showCitations);
        return set;
    }
}
