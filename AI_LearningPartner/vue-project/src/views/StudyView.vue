<script setup lang="ts">
import { ref } from 'vue'

const scheduleFiles = ref<any[]>([])
const beforeUpload = (file: File) => {
  scheduleFiles.value = [file]
  return false
}

// Mock 数据
const timetable = ref([
  { day: '周一', items: ['高等数学', '英语阅读', ''] },
  { day: '周二', items: ['数据结构', '离散数学', '体育'] },
  { day: '周三', items: ['操作系统', '概率统计', ''] },
  { day: '周四', items: ['计算机网络', '综合实验', ''] },
  { day: '周五', items: ['线性代数', '专业选修', '班会'] },
])

const upcomingExams = ref([
  { name: '大学英语六级', date: '2025-12-15', daysLeft: 89 },
  { name: '软考中级', date: '2025-11-20', daysLeft: 64 },
])

const videos = ref([
  { title: '数据结构与算法入门', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
  { title: '操作系统核心概念', url: 'https://www.bilibili.com', cover: 'https://via.placeholder.com/160x90' },
  { title: '计算机网络体系结构', url: 'https://www.youtube.com', cover: 'https://via.placeholder.com/160x90' },
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
      <h1 class="page-title">校园学业</h1>
      <p class="page-subtitle">上传课表或考试安排，查看课程表与备考推荐</p>
    </div>

    <div class="grid">
      <!-- 左列：上传 + 课表 -->
      <div class="left">
        <div class="card">
          <h2 class="section-title">上传课表/考试安排</h2>
          <a-upload :before-upload="beforeUpload" :file-list="scheduleFiles as any">
            <a-button type="primary">选择文件</a-button>
          </a-upload>
          <p class="hint">前端原型，仅展示所选文件，不会上传。</p>
        </div>

        <div class="card">
          <h2 class="section-title">课程表（Mock）</h2>
          <div class="timetable">
            <div class="t-row t-header">
              <div>星期</div>
              <div>上午</div>
              <div>下午</div>
              <div>晚上</div>
            </div>
            <div v-for="row in timetable" :key="row.day" class="t-row">
              <div class="t-cell day">{{ row.day }}</div>
              <div class="t-cell" v-for="(item,i) in row.items" :key="i">{{ item || '-' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右列：已学课程 + 考试倒计时 + 推荐视频 -->
      <div class="right">
        <div class="card">
          <div class="row-header">
            <h2 class="section-title">已学课程（可导入）</h2>
            <a-upload :before-upload="importCoursesBefore" :show-upload-list="false">
              <a-button size="small">上传历史课表(CSV)</a-button>
            </a-upload>
          </div>
          <a-table :columns="courseColumns" :data-source="courses" :pagination="{ pageSize: 5 }" row-key="name" size="small" />
        </div>
        <div class="card">
          <h2 class="section-title">近期重要考试</h2>
          <a-list :data-source="upcomingExams">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.name" :description="`考试日期：${item.date}`" />
                <template #actions>
                  <span>还有 <b>{{ item.daysLeft }}</b> 天</span>
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
  </div>
  
</template>

<style scoped>
.study-container {
  padding: 24px;
  background: #f7f8fa;
  min-height: 100%;
}

.page-header { text-align: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 600; color: #1f2937; margin: 0 0 8px 0; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; max-width: 1200px; margin: 0 auto; }
.left, .right { display: flex; flex-direction: column; gap: 16px; }
.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.06); border: 1px solid #f0f0f0; }
.section-title { font-size: 16px; font-weight: 600; color: #1f2937; margin: 0 0 16px 0; }
.hint { color: #6b7280; margin-top: 8px; }

.timetable { border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }
.t-row { display: grid; grid-template-columns: 120px 1fr 1fr 1fr; }
.t-header { background: #f9fafb; font-weight: 600; }
.t-cell { padding: 10px 12px; border-top: 1px solid #f0f0f0; }
.day { background: #fafafa; font-weight: 500; }

.video-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.video-card { padding: 0; }
.cover { width: 100%; height: 90px; object-fit: cover; border-radius: 6px; }
.v-title { margin-top: 8px; }

.row-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }

@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
  .video-grid { grid-template-columns: 1fr 1fr; }
}
</style>


