package cn.edu.zjweu.cs.shuzimali.controller;

import cn.edu.zjweu.cs.shuzimali.common.demo;
import cn.edu.zjweu.cs.shuzimali.pojo.Result;
import cn.edu.zjweu.cs.shuzimali.service.impl.AliyunBailianApplicationServiceImpl;
import cn.edu.zjweu.cs.shuzimali.config.AliyunConfig;
import com.aliyun.bailian20231229.models.*;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Arrays;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/aliyunbailian/application/file")
public class FileUploadController {

    @Data
    public static class LeaseRequest {
        private String fileName;
        private String fileMd5;   // 前端计算的散列（若要求MD5需前端提供MD5）
        private String fileSize;  // 字节数，字符串形式
        private String indexId;   // 知识库ID，用于索引任务
    }

    @Data
    public static class UploadAndIndexRequest {
        private String indexId;   // 知识库ID，用于索引任务
    }

    /**
     * 申请文件上传租约。
     *
     * @param client      客户端对象
     * @param categoryId  类目ID
     * @param fileName    文件名称
     * @param fileMd5     文件的MD5值
     * @param fileSize    文件大小（以字节为单位）
     * @param workspaceId 业务空间ID
     * @return 阿里云百炼服务的响应对象
     */
    public static ApplyFileUploadLeaseResponse applyLease(com.aliyun.bailian20231229.Client client, String categoryId, String fileName, String fileMd5, String fileSize, String workspaceId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        ApplyFileUploadLeaseRequest applyFileUploadLeaseRequest = new ApplyFileUploadLeaseRequest();
        applyFileUploadLeaseRequest.setFileName(fileName);
        applyFileUploadLeaseRequest.setMd5(fileMd5);
        applyFileUploadLeaseRequest.setSizeInBytes(fileSize);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.applyFileUploadLeaseWithOptions(categoryId, workspaceId, applyFileUploadLeaseRequest, headers, runtime);
    }

    /**
     * 上传文件到临时存储。
     *
     * @param preSignedUrl 上传租约中的 URL
     * @param headers      上传请求的头部
     * @param fileBytes    文件字节内容
     * @throws Exception 如果上传过程中发生错误
     */
    public static void uploadFile(String preSignedUrl, Map<String, String> headers, byte[] fileBytes) throws Exception {
        URL url = new URL(preSignedUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);

        // 设置上传请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // 上传文件内容
        conn.getOutputStream().write(fileBytes);

        int responseCode = conn.getResponseCode();
        conn.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("上传失败: " + responseCode);
        }
    }

    /**
     * 添加文件到类目中
     *
     * @param client      客户端对象
     * @param leaseId     租约ID
     * @param parser      文件解析器
     * @param categoryId  类目ID
     * @param workspaceId 业务空间ID
     * @return 阿里云百炼服务的响应
     */
    public static AddFileResponse addFile(com.aliyun.bailian20231229.Client client, String leaseId, String parser, String categoryId, String workspaceId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        AddFileRequest addFileRequest = new AddFileRequest();
        addFileRequest.setLeaseId(leaseId);
        addFileRequest.setParser(parser);
        addFileRequest.setCategoryId(categoryId);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.addFileWithOptions(workspaceId, addFileRequest, headers, runtime);
    }

    /**
     * 向一个非结构化知识库追加导入已解析的文件
     *
     * @param client      客户端（Client）
     * @param workspaceId 业务空间ID
     * @param indexId     知识库ID
     * @param fileId      文件ID
     * @param sourceType  数据类型
     * @return 阿里云百炼服务的响应
     */
    public static SubmitIndexAddDocumentsJobResponse submitIndexAddDocumentsJob(com.aliyun.bailian20231229.Client client, String workspaceId, String indexId, String fileId, String sourceType) throws Exception {
        Map<String, String> headers = new HashMap<>();
        SubmitIndexAddDocumentsJobRequest submitIndexAddDocumentsJobRequest = new SubmitIndexAddDocumentsJobRequest();
        submitIndexAddDocumentsJobRequest.setIndexId(indexId);
        // 确保documentIds是List类型
        submitIndexAddDocumentsJobRequest.setDocumentIds(Arrays.asList(fileId));
        submitIndexAddDocumentsJobRequest.setSourceType(sourceType);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.submitIndexAddDocumentsJobWithOptions(workspaceId, submitIndexAddDocumentsJobRequest, headers, runtime);
    }

    /**
     * 查询索引任务状态。
     *
     * @param client      客户端对象
     * @param workspaceId 业务空间ID
     * @param jobId       任务ID
     * @param indexId     知识库ID
     * @return 阿里云百炼服务的响应对象
     */
    public static GetIndexJobStatusResponse getIndexJobStatus(com.aliyun.bailian20231229.Client client, String workspaceId, String jobId, String indexId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        GetIndexJobStatusRequest getIndexJobStatusRequest = new GetIndexJobStatusRequest();
        getIndexJobStatusRequest.setIndexId(indexId);
        getIndexJobStatusRequest.setJobId(jobId);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.getIndexJobStatusWithOptions(workspaceId, getIndexJobStatusRequest, headers, runtime);
    }

    /**
     * 从指定的非结构化知识库中永久删除一个或多个文件
     *
     * @param client      客户端（Client）
     * @param workspaceId 业务空间ID
     * @param indexId     知识库ID
     * @param fileId      文件ID
     * @return 阿里云百炼服务的响应
     */
    public static DeleteIndexDocumentResponse deleteIndexDocument(com.aliyun.bailian20231229.Client client, String workspaceId, String indexId, String fileId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        DeleteIndexDocumentRequest deleteIndexDocumentRequest = new DeleteIndexDocumentRequest();
        deleteIndexDocumentRequest.setIndexId(indexId);
        deleteIndexDocumentRequest.setDocumentIds(Collections.singletonList(fileId));
        RuntimeOptions runtime = new RuntimeOptions();
        return client.deleteIndexDocumentWithOptions(workspaceId, deleteIndexDocumentRequest, headers, runtime);
    }

    /**
     * 计算文件的MD5值
     *
     * @param fileBytes 文件字节内容
     * @return 文件的MD5值
     */
    public static String calculateMD5(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(fileBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @PostMapping("/lease")
    public Result getUploadLease(@RequestBody LeaseRequest req) {
        try {
            com.aliyun.bailian20231229.Client client = AliyunBailianApplicationServiceImpl.createClient();
            
            // 检查是否提供了indexId
            if (req.getIndexId() == null || req.getIndexId().isEmpty()) {
                System.out.println("警告：未提供indexId，索引任务可能失败");
            }
            
            ApplyFileUploadLeaseResponse lease = applyLease(
                    client,
                    AliyunConfig.categoryId,
                    req.getFileName(),
                    req.getFileMd5(),
                    req.getFileSize(),
                    AliyunConfig.workplaceId
            );

            // 检查响应是否有效
            if (lease == null || lease.getBody() == null) {
                return Result.error("申请上传租约失败，响应为空");
            }
            
            if (lease.getBody().getData() == null) {
                return Result.error("申请上传租约失败，响应数据为空: " + lease.getBody().getRequestId() +
                                  ", 错误码: " + lease.getBody().getCode() + 
                                  ", 错误信息: " + lease.getBody().getMessage());
            }
            
            if (lease.getBody().getData().getParam() == null) {
                return Result.error("申请上传租约失败，参数为空: " + lease.getBody().getRequestId());
            }

            // 从响应中提取直传URL与需要的头
            String url = lease.getBody().getData().getParam().getUrl();
            Map<String, String> headers = new HashMap<>();
            Object headersObj = lease.getBody().getData().getParam().getHeaders();
            if (headersObj instanceof Map<?, ?> mapObj) {
                for (Map.Entry<?, ?> e : mapObj.entrySet()) {
                    String key = String.valueOf(e.getKey());
                    String val = String.valueOf(e.getValue());
                    headers.put(key, val);
                }
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("url", url);
            resp.put("headers", headers);
            resp.put("method", lease.getBody().getData().getParam().getMethod()); // 一般为 PUT
            resp.put("indexId", req.getIndexId()); // 将indexId传回给前端
            return Result.success(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/direct-upload")
    public Result serverSideUpload(@RequestPart("file") MultipartFile file) {
        try {
            // 1) 申请租约（后端默认配置）
            com.aliyun.bailian20231229.Client client = AliyunBailianApplicationServiceImpl.createClient();

            // 计算MD5（Base16小写）
            byte[] bytes = file.getBytes();
            String md5Hex = calculateMD5(bytes);

            ApplyFileUploadLeaseResponse lease = applyLease(
                    client,
                    AliyunConfig.categoryId,
                    file.getOriginalFilename(),
                    md5Hex,
                    String.valueOf(file.getSize()),
                    AliyunConfig.workplaceId
            );

            // 兜底校验：若Data为null，直接返回后端错误信息，避免NPE
            if (lease == null || lease.getBody() == null || lease.getBody().getData() == null) {
                String rawLease = String.valueOf(lease);
                String bodyStr = null;
                try { bodyStr = String.valueOf(lease.getBody()); } catch (Throwable ignored) {}
                return Result.error("申请上传租约失败，请检查 workspaceId / indexId 配置是否正确，以及AK权限是否开通。rawLease=" + rawLease + "; body=" + bodyStr);
            }

            String url = lease.getBody().getData().getParam().getUrl();
            Object headersObj = lease.getBody().getData().getParam().getHeaders();

            // 转换headers
            Map<String, String> headersMap = new HashMap<>();
            if (headersObj instanceof Map<?, ?> mapObj) {
                for (Map.Entry<?, ?> e : mapObj.entrySet()) {
                    headersMap.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }

            // 2) 服务端直传（避免前端CORS/签名头不匹配）
            uploadFile(url, headersMap, bytes);

            return Result.success("uploaded");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/direct-upload-and-index")
    public Result serverSideUploadAndIndex(@RequestPart("file") MultipartFile file) {
        try {
            // 准备 client 与工具
            com.aliyun.bailian20231229.Client client = AliyunBailianApplicationServiceImpl.createClient();

            // 计算MD5（hex）
            byte[] bytes = file.getBytes();
            String md5Hex = calculateMD5(bytes);

            // 1) 申请租约
            ApplyFileUploadLeaseResponse lease = applyLease(
                    client,
                    AliyunConfig.categoryId,
                    file.getOriginalFilename(),
                    md5Hex,
                    String.valueOf(file.getSize()),
                    AliyunConfig.workplaceId
            );
            
            // 检查响应是否有效
            if (lease == null || lease.getBody() == null) {
                return Result.error("申请上传租约失败，响应为空");
            }
            
            if (lease.getBody().getData() == null) {
                return Result.error("申请上传租约失败，响应数据为空: " + lease.getBody().getRequestId() +
                                  ", 错误码: " + lease.getBody().getCode() + 
                                  ", 错误信息: " + lease.getBody().getMessage());
            }
            
            if (lease.getBody().getData().getParam() == null) {
                return Result.error("申请上传租约失败，参数为空: " + lease.getBody().getRequestId());
            }

            String url = lease.getBody().getData().getParam().getUrl();
            Object headersObj = lease.getBody().getData().getParam().getHeaders();
            
            // 转换headers
            Map<String, String> headersMap = new HashMap<>();
            if (headersObj instanceof Map<?, ?> mapObj) {
                for (Map.Entry<?, ?> e : mapObj.entrySet()) {
                    headersMap.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }

            // 2) 服务端直传
            uploadFile(url, headersMap, bytes);
            System.out.println("文件上传完成");

            // 3) 添加文件到类目中
            String leaseId = lease.getBody().getData().getFileUploadLeaseId();
            System.out.println("租约ID: " + leaseId);
            
            AddFileResponse addFileResponse = addFile(client, leaseId, "DASHSCOPE_DOCMIND", AliyunConfig.categoryId, AliyunConfig.workplaceId);
            System.out.println("添加文件到类目完成");
            
            if (addFileResponse == null || addFileResponse.getBody() == null) {
                return Result.error("添加文件到类目失败，响应为空");
            }
            
            if (addFileResponse.getBody().getData() == null) {
                return Result.error("添加文件到类目失败，响应数据为空: " + 
                                  "Status=" + addFileResponse.getBody().getStatus() + 
                                  ", Code=" + addFileResponse.getBody().getCode() + 
                                  ", Message=" + addFileResponse.getBody().getMessage());
            }
            
            String fileId = addFileResponse.getBody().getData().getFileId();
            System.out.println("文件ID: " + fileId);

            // 4) 提交入库任务（索引）
            // 使用从AddFile接口返回的fileId
            System.out.println("使用的indexId: " + AliyunConfig.knowledgeBaseId);
            System.out.println("使用的fileId: " + fileId);
            
            var submitResp = submitIndexAddDocumentsJob(client, AliyunConfig.workplaceId, AliyunConfig.knowledgeBaseId, fileId, "DATA_CENTER_FILE");
            
            // 检查提交响应
            if (submitResp == null) {
                System.out.println("提交索引任务失败，响应为空");
                return Result.error("提交索引任务失败，响应为空");
            }
            
            if (submitResp.getBody() == null) {
                System.out.println("提交索引任务失败，响应体为空");
                return Result.error("提交索引任务失败，响应体为空");
            }
            
            System.out.println("索引提交响应: " + submitResp);
            
            if (submitResp.getBody().getData() == null) {
                System.out.println("提交索引任务失败，响应数据为空");
                return Result.error("提交索引任务失败，响应数据为空");
            }

            String rawSubmit = String.valueOf(submitResp.getBody());
            String jobId = null;
            
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(rawSubmit);
                com.fasterxml.jackson.databind.JsonNode dataNode = root.has("Data") ? root.get("Data") : (root.has("data") ? root.get("data") : null);
                if (dataNode != null) {
                    if (dataNode.has("Id")) jobId = dataNode.get("Id").asText();
                    else if (dataNode.has("id")) jobId = dataNode.get("id").asText();
                }
            } catch (Throwable ignored) {}
            
            if (jobId == null) {
                java.util.regex.Matcher jm = java.util.regex.Pattern.compile("\"Id\"\s*:\s*\"([^\"]+)\"").matcher(rawSubmit);
                if (jm.find()) jobId = jm.group(1);
                if (jobId == null) jobId = "unknown";
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "submitted");
            resp.put("fileId", fileId);
            resp.put("jobId", jobId);
            resp.put("leaseUrl", url);
            resp.put("rawSubmitBody", rawSubmit);
            return Result.success(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/index-status")
    public Result indexStatus(@RequestParam("jobId") String jobId) {
        try {
            com.aliyun.bailian20231229.Client client = AliyunBailianApplicationServiceImpl.createClient();
            var statusResp = getIndexJobStatus(client, AliyunConfig.workplaceId, jobId, AliyunConfig.categoryId);
            return Result.success(statusResp.getBody());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
