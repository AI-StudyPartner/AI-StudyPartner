package cn.edu.zjweu.cs.shuzimali.service.impl;

import cn.edu.zjweu.cs.shuzimali.config.AliyunConfig;
import cn.edu.zjweu.cs.shuzimali.service.AliyunBailianDashScopeService;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AliyunBailianScopeServiceImpl implements AliyunBailianDashScopeService {
    private static GenerationParam buildGenerationParam(Message userMsg, String llmModel) {
        return GenerationParam.builder()
                .apiKey(AliyunConfig.apikey)
                .model(llmModel)
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput( true)
                .build();
    }
    public static Flowable<GenerationResult> streamCallWithMessage(Generation gen, Message userMsg, String llmModel)
        throws ApiException, NoApiKeyException, InputRequiredException {
        GenerationParam param = buildGenerationParam(userMsg, llmModel);
        Flowable<GenerationResult> result = gen.streamCall(param);
        return result;
    }
    @Override
    public Flowable<GenerationResult> sendAiContent(String aliyunBailianContent, String aiModel) {
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(aliyunBailianContent).build();
            return streamCallWithMessage(gen, userMsg, aiModel);
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            return Flowable.error(e);
        }
    }
}
