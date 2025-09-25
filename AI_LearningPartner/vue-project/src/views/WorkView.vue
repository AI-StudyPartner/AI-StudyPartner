<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 简历（前端原型）
const resumeFiles = ref<any[]>([])
const beforeUpload = (file: File) => { resumeFiles.value = [file]; return false }

// 添加本地存储功能
const storedResumes = ref<any[]>([])

// 保存简历到本地存储
const saveResumeToLocal = () => {
  if (resumeFiles.value.length > 0) {
    const file = resumeFiles.value[0]

    // 使用 FileReader 读取文件内容
    const reader = new FileReader()
    reader.onload = (e) => {
      const fileData = e.target?.result as string

      // 创建文件对象
      const fileObject = {
        id: Date.now(),
        name: file.name,
        size: file.size,
        type: file.type,
        lastModified: file.lastModified,
        data: fileData, // 文件内容（base64编码）
        uploadTime: new Date().toLocaleString()
      }

      // 从本地存储获取已保存的简历
      const resumes = JSON.parse(localStorage.getItem('localResumes') || '[]')
      resumes.push(fileObject)
      localStorage.setItem('localResumes', JSON.stringify(resumes))

      // 更新界面显示
      loadStoredResumes()

      // 清空上传列表
      resumeFiles.value = []
    }

    // 读取文件为 DataURL (base64)
    reader.readAsDataURL(file)
  }
}

// 从本地存储加载已保存的简历
const loadStoredResumes = () => {
  const resumes = JSON.parse(localStorage.getItem('localResumes') || '[]')
  storedResumes.value = resumes
}

// 删除简历
const deleteStoredResume = (id: number) => {
  const resumes = JSON.parse(localStorage.getItem('localResumes') || '[]')
  const updatedResumes = resumes.filter((resume: any) => resume.id !== id)
  localStorage.setItem('localResumes', JSON.stringify(updatedResumes))
  loadStoredResumes()
}

// 下载简历
const downloadStoredResume = (resume: any) => {
  // 创建一个下载链接
  const link = document.createElement('a')
  link.href = resume.data
  link.download = resume.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 组件挂载时加载已保存的简历
onMounted(() => {
  loadStoredResumes()
})

// 理想岗位（直接就业）
const idealPosition = ref<string>('前端工程师')

const jobVideos = ref([
  { title: '校招简历速通指南', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
  { title: '技术面试高频题', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
])
</script>
<template>
  <div class="work-container">
    <div class="page-header">
      <h1 class="page-title">To 就业</h1>
      <p class="page-subtitle">一切为了更好的就业</p>
    </div>

    <div class="grid">
      <!-- 左列：理想岗位 + 就业准备 -->
      <div class="left">
        <div class="card">
          <h2 class="section-title">理想岗位</h2>
          <a-input v-model:value="idealPosition" placeholder="例如：前端工程师 / 算法工程师" />
          <p class="hint">填写你想从事的岗位名称，后续内容将围绕该方向展示。</p>
        </div>

        <div class="card">
          <h2 class="section-title">就业准备（Mock）</h2>
          <ul class="bullets">
            <li>梳理课程项目与竞赛，形成简历要点</li>
            <li>刷题与八股：数据结构、网络、操作系统、数据库</li>
            <li>关注目标公司的招聘日程与投递入口</li>
          </ul>
        </div>
      </div>

      <!-- 右列：简历上传 + 视频推荐 -->
      <div class="right">
        <div class="card">
          <h2 class="section-title">简历上传</h2>
          <a-upload :before-upload="beforeUpload" :file-list="resumeFiles as any">
            <a-button type="primary">选择简历文件</a-button>
          </a-upload>
          <a-button
            type="primary"
            @click="saveResumeToLocal"
            :disabled="resumeFiles.length === 0"
            style="margin-top: 10px"
          >
            保存到本地
          </a-button>

          <!-- 显示已保存的简历 -->
          <div v-if="storedResumes.length > 0" style="margin-top: 20px;">
            <h3 style="margin-bottom: 10px;">已保存的简历</h3>
            <div
              v-for="resume in storedResumes"
              :key="resume.id"
              class="resume-item"
            >
              <div class="resume-info">
                <div>
                  <div><strong>{{ resume.name }}</strong></div>
                  <div class="resume-meta">
                    大小: {{ (resume.size / 1024).toFixed(1) }} KB |
                  </div>
                </div>
                <div class="resume-actions">
                  <a-button
                    type="link"
                    @click="downloadStoredResume(resume)"
                    size="small"
                  >
                    下载
                  </a-button>
                  <a-button
                    type="link"
                    danger
                    @click="deleteStoredResume(resume.id)"
                    size="small"
                  >
                    删除
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.work-container { padding: 24px; background: #f7f8fa; min-height: 100%; }
.page-header { text-align: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 600; color: #1f2937; margin: 0 0 8px 0; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; max-width: 1200px; margin: 0 auto; }
.left, .right { display: flex; flex-direction: column; gap: 16px; }
.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.06); border: 1px solid #f0f0f0; }
.section-title { font-size: 16px; font-weight: 600; color: #1f2937; margin: 0 0 16px 0; }
.hint { color: #6b7280; margin-top: 8px; }
.bullets { margin: 0; padding-left: 18px; }
.bullets li { margin-bottom: 6px; }
.video-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.video-card { padding: 0; }
.cover { width: 100%; height: 90px; object-fit: cover; border-radius: 6px; }
.v-title { margin-top: 8px; }

/* 简历项样式 */
.resume-item {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 10px;
  background-color: #fafafa;
}

.resume-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-meta {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.resume-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
  .resume-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>




