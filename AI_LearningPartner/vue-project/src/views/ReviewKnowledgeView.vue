<script setup lang="ts">
import { ref } from 'vue'

const fileList = ref<any[]>([])
const uploading = ref(false)

const handleBeforeUpload = (file: File) => {
  fileList.value = [file]
  return false
}

const handleRemove = () => {
  fileList.value = []
}
</script>

<template>
  <div class="rk-container">
    <div class="page-header">
      <h1 class="page-title">复盘与知识库</h1>
      <p class="page-subtitle">上传学习资料/笔记，右侧查看历史复盘记录与云知识库</p>
    </div>

    <div class="rk-grid">
      <!-- 左侧：上传区 -->
      <div class="left card">
        <h2 class="section-title">上传文件</h2>
        <a-upload
          :before-upload="handleBeforeUpload"
          :file-list="fileList as any"
          :show-upload-list="true"
          @remove="handleRemove"
        >
          <a-button type="primary">选择文件</a-button>
        </a-upload>
        <p class="hint">当前为前端原型，选择文件后仅做列表展示，不会上传。</p>
      </div>

      <!-- 右侧：Tab -->
      <div class="right card">
        <a-tabs default-active-key="history">
          <a-tab-pane key="history" tab="历史复盘记录">
            <div class="empty-image">
              <img src="data:image/svg+xml;utf8,<?xml version='1.0' encoding='UTF-8'?><svg xmlns='http://www.w3.org/2000/svg' width='480' height='320'><rect width='100%' height='100%' fill='%23f5f5f5'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-size='18'>空白示例图（思维导图占位）</text></svg>" alt="empty" />
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rk-container {
  padding: 24px;
  background: #f7f8fa;
  min-height: 100%;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.rk-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.hint {
  color: #6b7280;
  margin-top: 8px;
}

.empty-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 360px;
  background: #fafafa;
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
}

.empty-image img {
  max-width: 100%;
  height: auto;
}

@media (max-width: 900px) {
  .rk-grid {
    grid-template-columns: 1fr;
  }
}
</style>


