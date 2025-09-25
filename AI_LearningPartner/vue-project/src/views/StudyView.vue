<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'

const scheduleFiles = ref<any[]>([])
const beforeUpload = (file: File) => {
  scheduleFiles.value = [file]
  return false
}

// 计算文件MD5（浏览器端）。这里用 SubtleCrypto 的 SHA-256 代替 MD5，若后端要求严格MD5，可改为spark-md5库。
async function hashFileSha256Base16(file: File): Promise<string> {
  const buf = await file.arrayBuffer()
  const digest = await crypto.subtle.digest('SHA-256', buf)
  const bytes = Array.from(new Uint8Array(digest))
  return bytes.map(b => b.toString(16).padStart(2, '0')).join('')
}

const uploading = ref(false)
const uploadHint = ref('')

const requestLeaseAndUpload = async () => {
  if (!scheduleFiles.value.length) return
  const file = scheduleFiles.value[0] as File
  uploading.value = true
  uploadHint.value = '准备上传...'
  try {
    // 直接走后端直传，避免前端跨域与签名头不一致
    const form = new FormData()
    form.append('file', file)
    uploadHint.value = '上传中...'
    const resp = await fetch('http://localhost:8080/aliyunbailian/application/file/direct-upload', {
      method: 'POST',
      body: form
    })
    if (!resp.ok) throw new Error(`上传失败 HTTP ${resp.status}`)
    uploadHint.value = '上传成功'
  } catch (e: any) {
    console.error(e)
    uploadHint.value = `上传失败：${e?.message || e}`
  } finally {
    uploading.value = false
  }
}

const uploadAndIndex = async () => {
  if (!scheduleFiles.value.length) return
  const file = scheduleFiles.value[0] as File
  uploading.value = true
  uploadHint.value = '上传并入库中...'
  try {
    const form = new FormData()
    form.append('file', file)
    const resp = await fetch('http://localhost:8080/aliyunbailian/application/file/direct-upload-and-index', {
      method: 'POST',
      body: form
    })
    if (!resp.ok) throw new Error(`入库请求失败 HTTP ${resp.status}`)
    const data = await resp.json()
    const payload = (data?.data ?? data)
    uploadHint.value = `入库任务已提交，jobId=${payload?.jobId || 'unknown'}`
  } catch (e: any) {
    console.error(e)
    uploadHint.value = `入库失败：${e?.message || e}`
  } finally {
    uploading.value = false
  }
}

// Mock 数据 - 当前课程列表
const currentCourses = ref([
  '高等数学',
  '英语阅读', 
  '数据结构',
  '离散数学',
  '体育',
  '操作系统',
  '概率统计',
  '计算机网络',
  '线性代数',
])

const upcomingExams = ref([
  { id: 1, name: '大学英语六级', date: '2025-12-15', daysLeft: 89 },
  { id: 2, name: '软考中级', date: '2025-11-20', daysLeft: 64 },
])

// 删除考试
const deleteExam = (examId: number) => {
  upcomingExams.value = upcomingExams.value.filter(exam => exam.id !== examId)
}

// 添加自定义考试
const showAddExamModal = ref(false)
const newExam = ref({
  name: '',
  date: ''
})

const addExam = () => {
  if (newExam.value.name && newExam.value.date) {
    const examDate = new Date(newExam.value.date)
    const today = new Date()
    const daysLeft = Math.ceil((examDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
    
    upcomingExams.value.push({
      id: Date.now(),
      name: newExam.value.name,
      date: newExam.value.date,
      daysLeft: daysLeft
    })
    
    newExam.value = { name: '', date: '' }
    showAddExamModal.value = false
  }
}

const videos = ref([
  { title: '数据结构与算法入门', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
  { title: '操作系统核心概念', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
  { title: '计算机网络体系结构', url: 'https://www.youtube.com', cover: 'https://via.placeholder.com/160x90' },
])

// AI学习建议（模拟数据）
const aiSuggestions = ref([
  {
    type: 'study-method',
    title: '学习方法建议',
    content: '建议采用番茄工作法，每25分钟专注学习后休息5分钟，提高学习效率。',
    icon: '📚'
  },
  {
    type: 'exam-prep',
    title: '考试准备提醒',
    content: '距离英语六级考试还有89天，建议每天背诵30个单词，练习听力30分钟。',
    icon: '📝'
  },
  {
    type: 'course-focus',
    title: '课程重点提醒',
    content: '数据结构课程建议重点掌握树和图的基本操作，多做编程练习。',
    icon: '🎯'
  },
  {
    type: 'time-management',
    title: '时间管理建议',
    content: '根据你的学习习惯，建议将难度较大的课程安排在上午学习。',
    icon: '⏰'
  },
])

// 已学课程（仅展示课程名称，Mock）
type Course = { name: string }
const courses = ref<Course[]>([
  { name: '高等数学A(上)' },
  { name: '大学英语(一)' },
  { name: '线性代数' },
  { name: '离散数学' },
  { name: 'C语言程序设计' },
  { name: '数据结构' },
  { name: '数据库原理' },
  { name: '概率统计' },
])
const courseColumns = [
  { title: '课程名称', dataIndex: 'name' },
]

// 导入历史课表（CSV 解析，仅识别“name”列；无法识别时追加示例）
const importCoursesBefore = (file: File) => {
  const reader = new FileReader()
  reader.onload = () => {
    const text = String(reader.result || '')
    try {
      const lines = text.split(/\r?\n/).filter(Boolean)
      const header = lines[0].toLowerCase()
      const isCsv = header.includes('name')
      if (!isCsv) {
        // 简单兜底：无法解析则追加几条示例
        courses.value = [...courses.value,
        { name: '操作系统实验' },
        { name: '软件工程' },
        ]
        return
      }
      const dataLines = lines.slice(1)
      const nameIndex = header.split(',').findIndex(h => h.trim() === 'name')
      const imported: Course[] = dataLines.map(line => {
        const cols = line.split(',')
        const name = (nameIndex >= 0 ? cols[nameIndex] : cols[0]) || '未命名课程'
        return { name: name.trim() }
      })
      courses.value = [...courses.value, ...imported]
    } catch (e) {
      console.warn('解析失败，已插入示例课程')
      courses.value = [...courses.value, { name: '计算机网络实验' }]
    }
  }
  reader.readAsText(file)
  return false
}
</script>

<template>
  <div class="study-container">
    <div class="page-header">
      <h1 class="page-title">To学业</h1>
      <p class="page-subtitle">一切为了更好的学业</p>
    </div>

    <div class="grid">
      <!-- 左列：上传 + 课表 -->
      <div class="left">
        <div class="card">
          <h2 class="section-title">上传课表/考试安排</h2>
          <a-upload :before-upload="beforeUpload" :file-list="scheduleFiles as any" :show-upload-list="true">
            <a-button type="primary">选择文件</a-button>
          </a-upload>
          <div style="margin-top:8px; display:flex; gap:8px; flex-wrap:wrap;">
            <a-button type="primary" :loading="uploading" @click="requestLeaseAndUpload">仅上传到临时存储</a-button>
            <a-button type="primary" ghost :loading="uploading" @click="uploadAndIndex">上传并入库到知识库</a-button>
          </div>
          <p class="hint">{{ uploadHint }}</p>
        </div>

        <div class="card">
          <h2 class="section-title">当前课程</h2>
          <div class="course-list">
            <div v-for="course in currentCourses" :key="course" class="course-item">
              {{ course }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右列：AI建议 + 已学课程 + 考试倒计时 + 推荐视频 -->
      <div class="right">
        <div class="card ai-suggestions-card">
          <h2 class="section-title">AI学习建议</h2>
          <div class="suggestions-list">
            <div v-for="suggestion in aiSuggestions" :key="suggestion.type" class="suggestion-item">
              <div class="suggestion-icon">{{ suggestion.icon }}</div>
              <div class="suggestion-content">
                <div class="suggestion-title">{{ suggestion.title }}</div>
                <div class="suggestion-text">{{ suggestion.content }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="row-header">
            <h2 class="section-title">已学课程（可导入）</h2>
            <a-upload :before-upload="importCoursesBefore" :show-upload-list="false">
              <a-button size="small">上传历史课表(CSV)</a-button>
            </a-upload>
          </div>
          <div class="course-list learned-courses">
            <div v-for="course in courses" :key="course.name" class="course-item learned-course-item">
              {{ course.name }}
            </div>
          </div>
        </div>
        <div class="card">
          <div class="row-header">
            <h2 class="section-title">近期重要考试</h2>
            <a-button type="primary" size="small" @click="showAddExamModal = true">
              自定义添加考试
            </a-button>
          </div>
          <a-list :data-source="upcomingExams">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.name" :description="`考试日期：${item.date}`" />
                <template #actions>
                  <span>还有 <b>{{ item.daysLeft }}</b> 天</span>
                  <a-button type="text" danger size="small" @click="deleteExam(item.id)">
                    删除
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <div class="card">
          <h2 class="section-title">学习视频推荐（Mock）</h2>
          <div class="video-grid">
            <a-card v-for="v in videos" :key="v.title" hoverable class="video-card">
              <img :src="v.cover" class="cover" alt="cover" />
              <div class="v-title">
                <a :href="v.url" target="_blank">{{ v.title }}</a>
              </div>
            </a-card>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加考试模态框 -->
    <a-modal
      v-model:open="showAddExamModal"
      title="添加考试"
      @ok="addExam"
      @cancel="showAddExamModal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="考试名称">
          <a-input v-model:value="newExam.name" placeholder="请输入考试名称" />
        </a-form-item>
        <a-form-item label="考试日期">
          <a-date-picker 
            v-model:value="newExam.date" 
            style="width: 100%"
            placeholder="请选择考试日期"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>

</template>

<style scoped>
.study-container {
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

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.left,
.right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .06);
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

.timetable {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.t-row {
  display: grid;
  grid-template-columns: 120px 1fr 1fr 1fr;
}

.t-header {
  background: #f9fafb;
  font-weight: 600;
}

.t-cell {
  padding: 10px 12px;
  border-top: 1px solid #f0f0f0;
}

.day {
  background: #fafafa;
  font-weight: 500;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.video-card {
  padding: 0;
}

.cover {
  width: 100%;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
}

.v-title {
  margin-top: 8px;
}

.row-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.course-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.course-item {
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  font-size: 14px;
  color: #495057;
  white-space: nowrap;
  flex-shrink: 0;
}

.learned-course-item {
  padding: 6px 10px;
  font-size: 13px;
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  color: #1976d2;
  white-space: nowrap;
  flex-shrink: 0;
}

.ai-suggestions-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid #e0f2fe;
  transition: all 0.2s ease;
}

.suggestion-item:hover {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.suggestion-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.suggestion-content {
  flex: 1;
}

.suggestion-title {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 4px;
}

.suggestion-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.4;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .video-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
