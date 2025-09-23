<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from "axios";

// 个人信息（改为自由文本）
const personalProfile = ref('')

// 学习与作息
const preferredStudyTime = ref<'morning' | 'afternoon' | 'evening' | 'late-night' | ''>('')
const dailyFocusMinutes = ref<number>(90)
const weeklyAvailableDays = ref<string[]>([])

// AI 偏好设置
const tone = ref<'friendly' | 'strict' | 'motivational' | 'concise'>('friendly')
const responseLength = ref<'short' | 'medium' | 'long'>('medium')
const allowEmojis = ref<boolean>(true)
const showCitations = ref<boolean>(true)

// 加载状态
const isLoading = ref(false)
const isSaving = ref(false)

// 页面挂载时加载用户设置
onMounted(async () => {
  await loadUserSettings()
})

// 加载用户设置
const loadUserSettings = async () => {
  try {
    isLoading.value = true
    const response = await axios.get("http://localhost:8080/set/show")
    const settings = response.data
    
    if (settings) {
      personalProfile.value = settings.personalProfile || ''
      preferredStudyTime.value = settings.preferredStudyTime || ''
      dailyFocusMinutes.value = settings.dailyFocusMinute || 90
      weeklyAvailableDays.value = settings.weeklyAvailableDays || []
      tone.value = settings.tone || 'friendly'
      responseLength.value = settings.responseLength || 'medium'
      allowEmojis.value = settings.allowEmojis !== null ? settings.allowEmojis : true
      showCitations.value = settings.showCitations !== null ? settings.showCitations : true
    }
  } catch (error) {
    console.error('加载用户设置失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    isSaving.value = true
    await axios.post("http://localhost:8080/set/save", {
        personalProfile: personalProfile.value,
        preferredStudyTime: preferredStudyTime.value,
        dailyFocusMinute: dailyFocusMinutes.value,
        weeklyAvailableDays: weeklyAvailableDays.value,
        tone: tone.value,
        responseLength: responseLength.value,
        allowEmojis: allowEmojis.value,
        showCitations: showCitations.value
    },{
      headers: {'Content-Type': 'application/json'}
    })
    console.log('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="settings-container">
    <div class="page-header">
      <h1 class="page-title">用户与设置</h1>
      <p class="page-subtitle">完善个人信息，提升 AI 学习搭子的个性化建议质量</p>
    </div>

    <div class="settings-grid">
      <!-- 个人信息（自由文本） -->
      <div class="card">
        <h2 class="section-title">个人信息</h2>
        <a-form layout="vertical">
          <a-form-item
            label="请尽可能详细地介绍你的背景与偏好"
            extra="建议包含：学校与专业、年级、目标（考研/保研/四六级/竞赛/实习/求职等）、擅长与薄弱科目、学习风格（番茄、刷题、项目驱动等）、每天可投入学习时长、每周可学习日、兴趣方向、希望AI的语气与风格等。"
          >
            <a-textarea
              v-model:value="personalProfile"
              :rows="8"
              placeholder="示例：我是xx大学计算机大三学生，准备参加明年9月的秋招与英语六级... 我擅长前端与写作，薄弱环节是数据结构与概率统计... 我偏好番茄学习法，每天可投入3小时，周一至周五为主... 希望AI语气友好且有督促，回复尽量简洁并附参考资料链接。"
            />
          </a-form-item>
        </a-form>
      </div>

      <!-- 学习与作息偏好 -->
      <div class="card">
        <h2 class="section-title">学习与作息偏好</h2>
        <a-form layout="vertical">
          <div class="two-col">
            <a-form-item label="偏好学习时段">
              <a-segmented
                v-model:value="preferredStudyTime"
                :options="[
                  { label: '清晨', value: 'morning' },
                  { label: '下午', value: 'afternoon' },
                  { label: '晚上', value: 'evening' },
                  { label: '深夜', value: 'late-night' },
                ]"
              />
            </a-form-item>
            <a-form-item label="每日专注目标（分钟）">
              <a-slider v-model:value="dailyFocusMinutes" :min="30" :max="360" :step="15" />
            </a-form-item>
          </div>

          <a-form-item label="每周可用学习日（多选）">
            <a-checkbox-group v-model:value="weeklyAvailableDays" :options="['周一','周二','周三','周四','周五','周六','周日']" />
          </a-form-item>
        </a-form>
      </div>

      <!-- AI 回复偏好 -->
      <div class="card">
        <h2 class="section-title">AI 回复偏好</h2>
        <a-form layout="vertical">
          <div class="two-col">
            <a-form-item label="语气">
              <a-segmented v-model:value="tone" :options="[
                { label: '友好', value: 'friendly' },
                { label: '严格', value: 'strict' },
                { label: '激励', value: 'motivational' },
                { label: '简洁', value: 'concise' },
              ]" />
            </a-form-item>
            <a-form-item label="回复长度">
              <a-segmented v-model:value="responseLength" :options="[
                { label: '短', value: 'short' },
                { label: '中', value: 'medium' },
                { label: '长', value: 'long' },
              ]" />
            </a-form-item>
          </div>

          <div class="two-col">
            <a-form-item>
              <a-switch v-model:checked="allowEmojis" />
              <span class="switch-label">允许使用表情符号</span>
            </a-form-item>
            <a-form-item>
              <a-switch v-model:checked="showCitations" />
              <span class="switch-label">显示参考来源/链接</span>
            </a-form-item>
          </div>
        </a-form>
      </div>
    </div>

    <div class="actions">
      <a-button type="primary" :loading="isSaving" @click="saveSettings">保存设置</a-button>
      <a-button style="margin-left: 8px" @click="$router.push('/app/chat')">返回聊天</a-button>
    </div>
  </div>
</template>

<style scoped>
.settings-container {
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

.settings-grid {
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

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.switch-label {
  margin-left: 8px;
  color: #374151;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>


