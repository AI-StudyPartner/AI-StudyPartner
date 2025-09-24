package cn.edu.zjweu.cs.shuzimali.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AliyunBailianApplication {
    private String content;
    private final String appId = "bf963293c1d54f4085a2e210f69c6d0a";
    private String memoryId;
    private List<String> knowledgeBaseId = List.of("uhym8y9eq3");
}
