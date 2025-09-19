<script setup lang="ts">
import { ref } from 'vue'

// 简历（前端原型）
const resumeFiles = ref<any[]>([])
const beforeUpload = (file: File) => { resumeFiles.value = [file]; return false }

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
          <h2 class="section-title">简历上传（保留春秋招准备）</h2>
          <a-upload :before-upload="beforeUpload" :file-list="resumeFiles as any">
            <a-button type="primary">选择简历文件</a-button>
          </a-upload>
          <p class="hint">前端原型，仅展示所选文件，不会上传。</p>
        </div>

        <div class="card">
          <h2 class="section-title">求职建议与视频（Mock）</h2>
          <div class="video-grid">
            <a-card v-for="v in jobVideos" :key="v.title" hoverable class="video-card">
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
@media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
</style>


