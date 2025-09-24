package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.pojo.AliyunBailianDashScope;
import cn.edu.zjweu.cs.shuzimali.service.AliyunBailianDashScopeService;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class AliyunBailianDashScopeController {
    @Autowired
    private AliyunBailianDashScopeService aliyunBailianDashScopeService;
    @PostMapping(value="/aliyunbailian/dashscope/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flowable<ServerSentEvent<GenerationResult>>
    assistantChatWithoutApplication(@RequestBody AliyunBailianDashScope aliyunBailianDashScope){
        AtomicInteger counter = new AtomicInteger(0);
        return aliyunBailianDashScopeService.sendAiContent(aliyunBailianDashScope.getContent(),aliyunBailianDashScope.getLlmMadel())
                .map(result -> ServerSentEvent.<GenerationResult>builder()
                        .id(String.valueOf(counter.getAndIncrement()))
                        .data(result)
                        .build());
    }
}
