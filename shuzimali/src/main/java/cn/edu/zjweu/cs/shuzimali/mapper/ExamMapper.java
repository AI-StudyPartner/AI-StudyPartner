package cn.edu.zjweu.cs.shuzimali.mapper;

import cn.edu.zjweu.cs.shuzimali.entity.Exam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试Mapper接口
 */
public interface ExamMapper {
    /**
     * 根据用户ID查询考试列表
     * @param userId 用户ID
     * @return 考试列表
     */
    List<Exam> selectExamsByUserId(int userId);

    /**
     * 添加考试
     * @param exam 考试对象
     */
    void insertExam(Exam exam);

    /**
     * 根据考试ID删除考试
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteExamById(@Param("examId") int examId, @Param("userId") int userId);

    /**
     * 更新考试
     * @param exam 考试对象
     */
    void updateExam(Exam exam);

    /**
     * 根据考试ID查询考试
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 考试对象
     */
    Exam selectExamById(@Param("examId") int examId, @Param("userId") int userId);
}
