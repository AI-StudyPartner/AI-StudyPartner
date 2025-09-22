package cn.edu.zjweu.cs.shuzimali;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.zjweu.cs.shuzimali.mapper")
public class ShuzimaliApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShuzimaliApplication.class, args);
	}

}
