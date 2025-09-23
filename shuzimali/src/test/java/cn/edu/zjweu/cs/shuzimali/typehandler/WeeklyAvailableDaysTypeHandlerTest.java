package cn.edu.zjweu.cs.shuzimali.typehandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * WeeklyAvailableDaysTypeHandler 测试类
 */
public class WeeklyAvailableDaysTypeHandlerTest {

    private WeeklyAvailableDaysTypeHandler typeHandler;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        typeHandler = new WeeklyAvailableDaysTypeHandler();
        // 使用内存数据库进行测试
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb");
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        // 准备测试数据
        List<String> testList = Arrays.asList("Monday", "Wednesday", "Friday");
        
        // 创建测试表
        String createTable = "CREATE TABLE test_table (id INT, days VARCHAR(255))";
        connection.createStatement().execute(createTable);
        
        // 测试插入
        String insertSql = "INSERT INTO test_table (id, days) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setInt(1, 1);
        typeHandler.setNonNullParameter(ps, 2, testList, null);
        ps.executeUpdate();
        
        // 验证结果
        String selectSql = "SELECT days FROM test_table WHERE id = 1";
        ResultSet rs = connection.createStatement().executeQuery(selectSql);
        assertTrue(rs.next());
        assertEquals("Monday,Wednesday,Friday", rs.getString("days"));
    }

    @Test
    void testGetNullableResult() throws SQLException {
        // 准备测试数据
        String testString = "Monday,Wednesday,Friday";
        
        // 创建测试表
        String createTable = "CREATE TABLE test_table (id INT, days VARCHAR(255))";
        connection.createStatement().execute(createTable);
        
        // 插入测试数据
        String insertSql = "INSERT INTO test_table (id, days) VALUES (1, ?)";
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setString(1, testString);
        ps.executeUpdate();
        
        // 测试查询
        String selectSql = "SELECT days FROM test_table WHERE id = 1";
        ResultSet rs = connection.createStatement().executeQuery(selectSql);
        assertTrue(rs.next());
        
        List<String> result = typeHandler.getNullableResult(rs, "days");
        
        // 验证结果
        assertEquals(3, result.size());
        assertTrue(result.contains("Monday"));
        assertTrue(result.contains("Wednesday"));
        assertTrue(result.contains("Friday"));
    }

    @Test
    void testEmptyList() throws SQLException {
        // 测试空列表
        List<String> emptyList = Arrays.asList();
        
        String createTable = "CREATE TABLE test_table (id INT, days VARCHAR(255))";
        connection.createStatement().execute(createTable);
        
        String insertSql = "INSERT INTO test_table (id, days) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setInt(1, 1);
        typeHandler.setNonNullParameter(ps, 2, emptyList, null);
        ps.executeUpdate();
        
        String selectSql = "SELECT days FROM test_table WHERE id = 1";
        ResultSet rs = connection.createStatement().executeQuery(selectSql);
        assertTrue(rs.next());
        assertEquals("", rs.getString("days"));
    }

    @Test
    void testNullValue() throws SQLException {
        // 测试null值
        String createTable = "CREATE TABLE test_table (id INT, days VARCHAR(255))";
        connection.createStatement().execute(createTable);
        
        String insertSql = "INSERT INTO test_table (id, days) VALUES (1, NULL)";
        connection.createStatement().execute(insertSql);
        
        String selectSql = "SELECT days FROM test_table WHERE id = 1";
        ResultSet rs = connection.createStatement().executeQuery(selectSql);
        assertTrue(rs.next());
        
        List<String> result = typeHandler.getNullableResult(rs, "days");
        assertTrue(result.isEmpty());
    }

    @Test
    void testWhitespaceHandling() throws SQLException {
        // 测试包含空格的字符串
        String testString = " Monday , Wednesday , Friday ";
        
        String createTable = "CREATE TABLE test_table (id INT, days VARCHAR(255))";
        connection.createStatement().execute(createTable);
        
        String insertSql = "INSERT INTO test_table (id, days) VALUES (1, ?)";
        PreparedStatement ps = connection.prepareStatement(insertSql);
        ps.setString(1, testString);
        ps.executeUpdate();
        
        String selectSql = "SELECT days FROM test_table WHERE id = 1";
        ResultSet rs = connection.createStatement().executeQuery(selectSql);
        assertTrue(rs.next());
        
        List<String> result = typeHandler.getNullableResult(rs, "days");
        
        // 验证结果（应该去除空格）
        assertEquals(3, result.size());
        assertEquals("Monday", result.get(0));
        assertEquals("Wednesday", result.get(1));
        assertEquals("Friday", result.get(2));
    }
}
