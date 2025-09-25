package cn.edu.zjweu.cs.shuzimali.entity;

import java.util.Date;

/**
 * 考试实体类
 */
public class Exam {
    private int examId;
    private int userId;
    private String examName;
    private Date examDate;
    private boolean reminder;
    private Date createAt;

    public Exam() {
    }

    public Exam(int examId, int userId, String examName, Date examDate, boolean reminder, Date createAt) {
        this.examId = examId;
        this.userId = userId;
        this.examName = examName;
        this.examDate = examDate;
        this.reminder = reminder;
        this.createAt = createAt;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
