package cn.edu.zjweu.cs.shuzimali.entity;

import java.util.List;

public class Set {
    private int setting_id;
    private int userId;
    private String personalProfile;  // 个人信息
    // 学习与作息
    private String preferredStudyTime;  // 喜好学习时间
    private int dailyFocusMinute; // 每天专注时间
    private List<String> weeklyAvailableDays; // 每周可用时间
    // AI偏好设置
    private String tone;
    private String responseLength;
    private Boolean allowEmojis;
    private Boolean showCitations;

    public int getSetting_id() {
        return setting_id;
    }

    public void setSetting_id(int setting_id) {
        this.setting_id = setting_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPersonalProfile() {
        return personalProfile;
    }

    public void setPersonalProfile(String personalProfile) {
        this.personalProfile = personalProfile;
    }

    public String getPreferredStudyTime() {
        return preferredStudyTime;
    }

    public void setPreferredStudyTime(String preferredStudyTime) {
        this.preferredStudyTime = preferredStudyTime;
    }

    public int getDailyFocusMinute() {
        return dailyFocusMinute;
    }

    public void setDailyFocusMinute(int dailyFocusMinute) {
        this.dailyFocusMinute = dailyFocusMinute;
    }

    public List<String> getWeeklyAvailableDays() {
        return weeklyAvailableDays;
    }

    public void setWeeklyAvailableDays(List<String> weeklyAvailableDays) {
        this.weeklyAvailableDays = weeklyAvailableDays;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(String responseLength) {
        this.responseLength = responseLength;
    }

    public Boolean getAllowEmojis() {
        return allowEmojis;
    }

    public void setAllowEmojis(Boolean allowEmojis) {
        this.allowEmojis = allowEmojis;
    }

    public Boolean getShowCitations() {
        return showCitations;
    }

    public void setShowCitations(Boolean showCitations) {
        this.showCitations = showCitations;
    }
}
