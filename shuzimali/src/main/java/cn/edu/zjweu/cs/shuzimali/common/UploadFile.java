package cn.edu.zjweu.cs.shuzimali.common;

import com.aliyun.bailian20231229.models.ApplyFileUploadLeaseResponse;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UploadFile {
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
    public ApplyFileUploadLeaseResponse applyLease(com.aliyun.bailian20231229.Client client, String categoryId, String fileName, String fileMd5, String fileSize, String workspaceId) throws Exception {
        Map<String, String> headers = new HashMap<>();
        com.aliyun.bailian20231229.models.ApplyFileUploadLeaseRequest applyFileUploadLeaseRequest = new com.aliyun.bailian20231229.models.ApplyFileUploadLeaseRequest();
        applyFileUploadLeaseRequest.setFileName(fileName);
        applyFileUploadLeaseRequest.setMd5(fileMd5);
        applyFileUploadLeaseRequest.setSizeInBytes(fileSize);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        ApplyFileUploadLeaseResponse applyFileUploadLeaseResponse = null;
        applyFileUploadLeaseResponse = client.applyFileUploadLeaseWithOptions(categoryId, workspaceId, applyFileUploadLeaseRequest, headers, runtime);
        return applyFileUploadLeaseResponse;
    }
    public static void uploadFile(String preSignedUrl, String filePath) {
        HttpURLConnection connection = null;
        try {
            // 创建URL对象
            URL url = new URL(preSignedUrl);
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法用于文件上传，需与您在上一步中调用ApplyFileUploadLease接口实际返回的Data.Param中Method字段的值一致
            connection.setRequestMethod("PUT");
            // 允许向connection输出，因为这个连接是用于上传文件的
            connection.setDoOutput(true);
            connection.setRequestProperty("X-bailian-extra", "请替换为您在上一步中调用ApplyFileUploadLease接口实际返回的Data.Param.Headers中X-bailian-extra字段的值");
            connection.setRequestProperty("Content-Type", "请替换为您在上一步中调用ApplyFileUploadLease接口实际返回的Data.Param.Headers中Content-Type字段的值（返回空值时，传空值即可）");
            // 读取文件并通过连接上传
            try (DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                 FileInputStream fileInputStream = new FileInputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                outStream.flush();
            }
            // 检查响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 文件上传成功处理
                System.out.println("File uploaded successfully.");
            } else {
                // 文件上传失败处理
                System.out.println("Failed to upload the file. ResponseCode: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        String preSignedUrlOrHttpUrl = "请替换为您在上一步中调用ApplyFileUploadLease接口实际返回的Data.Param中Url字段的值";
        // 将本地文件上传至临时存储
        String filePath = "请替换为您需要上传文件的实际本地路径（以Linux为例：/xxx/xxx/阿里云百炼系列手机产品介绍.docx）";
        uploadFile(preSignedUrlOrHttpUrl, filePath);
    }
}