package cn.edu.zjweu.cs.shuzimali.service.impl;

import cn.edu.zjweu.cs.shuzimali.config.AliyunConfig;
import cn.edu.zjweu.cs.shuzimali.pojo.AliyunBailianApplication;
import cn.edu.zjweu.cs.shuzimali.service.AliyunBailianApplicationService;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.app.RagOptions;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.aliyun.tea.TeaException;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

@Service
public class AliyunBailianApplicationServiceImpl implements AliyunBailianApplicationService {
    public static com.aliyun.bailian20231229.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(AliyunConfig.accessKeyId)
                .setAccessKeySecret(AliyunConfig.accessKeySecret);
        config.endpoint = "bailian.cn-beijing.aliyuncs.com";
        return new com.aliyun.bailian20231229.Client(config);
    }
    @Override
    public Flowable<ApplicationResult> assistanceChat(AliyunBailianApplication aliyunBailianApplication)
        throws NoApiKeyException, InputRequiredException, ApiException {
        ApplicationParam param = ApplicationParam.builder()
                .apiKey(AliyunConfig.apikey)
                .appId(aliyunBailianApplication.getAppId())
                .prompt(aliyunBailianApplication.getContent())
                .memoryId(aliyunBailianApplication.getMemoryId())
                .ragOptions(RagOptions.builder()
                        .pipelineIds(aliyunBailianApplication.getKnowledgeBaseId())
                        .build())
                .incrementalOutput(true)
                .build();
        Application application = new Application();
        return application.streamCall(param);
    }
    @Override
    public String createMemory(String aliyunBailianMemoryContent) throws Exception {
        com.aliyun.bailian20231229.Client client = AliyunBailianApplicationServiceImpl.createClient();
        com.aliyun.bailian20231229.models.CreateMemoryRequest createMemoryRequest = new com.aliyun.bailian20231229.models.CreateMemoryRequest();
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        try {
            return client.createMemoryWithOptions(AliyunConfig.workplaceId, createMemoryRequest,headers,runtime).getBody().getMemoryId();
        } catch (TeaException error) {
            System.out.println(error.getMessage());
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
            return error.message;
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            System.out.println(error.getMessage());
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
            return error.message;
        }
    }
}
