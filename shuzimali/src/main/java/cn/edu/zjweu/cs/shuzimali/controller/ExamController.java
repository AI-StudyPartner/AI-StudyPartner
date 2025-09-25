package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.Factory.UserFactory;
import cn.edu.zjweu.cs.shuzimali.entity.Exam;
import cn.edu.zjweu.cs.shuzimali.mapper.ExamMapper;
import cn.edu.zjweu.cs.shuzimali.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 考试管理控制器
 */
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamMapper examMapper;

    /**
     * 获取当前用户的所有考试
     *
     * @return 考试列表
     */
    @GetMapping("/list")
    public Result getExams() {
        try {
            int userId = Integer.parseInt(UserFactory.getUser().getId());
            List<Exam> exams = examMapper.selectExamsByUserId(userId);
            return Result.success(exams);
        } catch (Exception e) {
            return Result.error("获取考试列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加考试
     *
     * @param exam 考试信息
     * @return 结果
     */
    @PostMapping("/add")
    public Result addExam(@RequestBody Exam exam) {
        try {
            int userId = Integer.parseInt(UserFactory.getUser().getId());
            exam.setUserId(userId);
            exam.setCreateAt(new Date());
            examMapper.insertExam(exam);
            return Result.success(exam);
        } catch (Exception e) {
            return Result.error("添加考试失败: " + e.getMessage());
        }
    }

    /**
     * 删除考试
     *
     * @param examId 考试ID
     * @return 结果
     */
    @DeleteMapping("/delete/{examId}")
    public Result deleteExam(@PathVariable int examId) {
        try {
            int userId = Integer.parseInt(UserFactory.getUser().getId());
            int result = examMapper.deleteExamById(examId, userId);
            if (result > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，考试不存在或无权限");
            }
        } catch (Exception e) {
            return Result.error("删除考试失败: " + e.getMessage());
        }
    }

    /**
     * 更新考试
     *
     * @param exam 考试信息
     * @return 结果
     */
    @PutMapping("/update")
    public Result updateExam(@RequestBody Exam exam) {
        try {
            int userId = Integer.parseInt(UserFactory.getUser().getId());
            Exam existingExam = examMapper.selectExamById(exam.getExamId(), userId);
            if (existingExam != null) {
                exam.setUserId(userId);
                examMapper.updateExam(exam);
                return Result.success("更新成功");
            } else {
                return Result.error("考试不存在或无权限");
            }
        } catch (Exception e) {
            return Result.error("更新考试失败: " + e.getMessage());
        }
    }
}

