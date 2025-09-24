package cn.edu.zjweu.cs.shuzimali.service;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

@Service
public interface AliyunBailianDashScopeService {
    Flowable<GenerationResult> sendAiContent(String aliyunBailianContent, String aiModel);
}
