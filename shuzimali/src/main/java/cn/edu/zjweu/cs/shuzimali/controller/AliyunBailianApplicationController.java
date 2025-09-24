package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.pojo.AliyunBailianApplication;
import cn.edu.zjweu.cs.shuzimali.pojo.Result;
import cn.edu.zjweu.cs.shuzimali.service.AliyunBailianApplicationService;
import com.alibaba.dashscope.app.ApplicationResult;

import io.reactivex.Flowable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliyunBailianApplicationController {
    @Autowired
    private AliyunBailianApplicationService aliyunBailianApplicationService;
    @PostMapping(value="/aliyunbailian/application/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flowable<ApplicationResult> assistanceChat(@RequestBody AliyunBailianApplication aliyunBailianApplication){
        try{
            return aliyunBailianApplicationService.assistanceChat(aliyunBailianApplication);
        } catch (Exception e){
            return Flowable.error(e);
        }
    }
    @PostMapping(value="/aliyunbailian/application/create/memory")
    public Result createMemory(@RequestBody String description) {
        System.out.println("创建记忆体ID");
        try {
            return Result.success(aliyunBailianApplicationService.createMemory(description));
        } catch (Exception e){
            return Result.error(e.getMessage());
        }
    }
}
