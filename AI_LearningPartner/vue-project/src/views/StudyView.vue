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


const upcomingExams = ref([
  { id: 1, name: '大学英语六级', date: '2025-12-15', daysLeft: 89 },
  { id: 2, name: '软考中级', date: '2025-11-20', daysLeft: 64 },
])

// 从后端获取考试列表
const fetchExams = async () => {
  try {
    const response = await axios.get('http://localhost:8080/exam/list')
    if (response.data.code === 1) {
      // 根据后端返回的数据格式进行处理
      upcomingExams.value = response.data.data.map((exam: any) => {
        const examDate = new Date(exam.examDate)
        const today = new Date()
        const daysLeft = Math.ceil((examDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
        return {
          id: exam.examId,
          name: exam.examName,
          date: exam.examDate,
          daysLeft: daysLeft
        }
      })
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
  }
}

// 删除考试
const deleteExam = async (examId: number) => {
  try {
    const response = await axios.delete(`http://localhost:8080/exam/delete/${examId}`)
    if (response.data.code === 1) {
      upcomingExams.value = upcomingExams.value.filter(exam => exam.id !== examId)
    } else {
      console.error('删除考试失败:', response.data.msg)
    }
  } catch (error) {
    console.error('删除考试失败:', error)
  }
}

// 添加自定义考试
const showAddExamModal = ref(false)
const newExam = ref({
  name: '',
  date: ''
})

const addExam = async () => {
  if (newExam.value.name && newExam.value.date) {
    try {
      const examData = {
        examName: newExam.value.name,
        examDate: newExam.value.date,
        reminder: true
      }

      const response = await axios.post('http://localhost:8080/exam/add', examData)
      if (response.data.code === 1) {
        const examDate = new Date(newExam.value.date)
        const today = new Date()
        const daysLeft = Math.ceil((examDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

        // 添加到列表中
        upcomingExams.value.push({
          id: Date.now(), // 实际项目中应该使用后端返回的ID
          name: newExam.value.name,
          date: newExam.value.date,
          daysLeft: daysLeft
        })

        newExam.value = { name: '', date: '' }
        showAddExamModal.value = false
      } else {
        console.error('添加考试失败:', response.data.msg)
      }
    } catch (error) {
      console.error('添加考试失败:', error)
    }
  }
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

      </div>

      <!-- 右列：AI建议 + 已学课程 + 考试倒计时 + 推荐视频 -->
      <div class="right">

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


.row-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}


@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

}
</style>
