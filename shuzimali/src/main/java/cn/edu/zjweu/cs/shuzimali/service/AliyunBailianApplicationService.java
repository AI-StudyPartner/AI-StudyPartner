package cn.edu.zjweu.cs.shuzimali.service;

import cn.edu.zjweu.cs.shuzimali.pojo.AliyunBailianApplication;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;

public interface AliyunBailianApplicationService {
    Flowable<ApplicationResult> assistanceChat(AliyunBailianApplication aliyunBailianApplication) throws NoApiKeyException, InputRequiredException, ApiException;
    String createMemory(String aliyunBailianMemoryContent) throws Exception;
}
