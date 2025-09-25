<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { 
  PlayCircleOutlined, 
  PauseCircleOutlined, 
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  FieldTimeOutlined
} from '@ant-design/icons-vue'

// 番茄钟相关状态
const isRunning = ref(false)
const isPaused = ref(false)
const timeLeft = ref(25 * 60) // 25分钟，以秒为单位
const totalTime = ref(25 * 60)
const timer = ref<number | null>(null)

// 全屏与样式
const isFullscreen = ref(false)
type TimerStyle = 'ring' | 'digital'
const timerStyle = ref<TimerStyle>('ring')
const nowDisplay = ref('')

const updateNow = () => {
  const d = new Date()
  const hh = d.getHours().toString().padStart(2, '0')
  const mm = d.getMinutes().toString().padStart(2, '0')
  nowDisplay.value = `${hh}:${mm}`
}

// 自定义时间设置
const customMinutes = ref(25)
const customSeconds = ref(0)
const showTimeSettings = ref(false)

// 番茄钟完成记录
interface PomodoroRecord {
  id: number
  duration: number // 以秒为单位
  completedAt: string
  type: 'work' | 'break'
  topic: string
}

const pomodoroRecords = ref<PomodoroRecord[]>([])
const topicStats = computed(() => {
  const map: Record<string, number> = {}
  let total = 0
  pomodoroRecords.value.forEach(r => { const t = r.topic || '未填写'; map[t] = (map[t]||0) + r.duration; total += r.duration })
  const entries = Object.entries(map).map(([topic, duration]) => ({ topic, duration }))
  return { total, entries }
})

const pieColors = ['#1677ff', '#52c41a', '#faad14', '#eb2f96', '#13c2c2']

// 近7天学习时长（小时，Mock）
const weekHours = ref<number[]>([2.5, 3, 4, 1, 0.5, 5, 6])
const maxHour = computed(() => Math.max(1, ...weekHours.value))
const weekPoints = computed(() => {
  const w = 320, h = 160, pad = 28
  const innerW = w - pad * 2
  const innerH = h - pad * 2
  const stepX = innerW / (weekHours.value.length - 1)
  return weekHours.value.map((v, i) => {
    const x = pad + i * stepX
    const y = pad + innerH - (v / maxHour.value) * innerH
    return `${x},${y}`
  }).join(' ')
})
const weekLabels = computed(() => {
  const days = ['一','二','三','四','五','六','日']
  const today = new Date().getDay() || 7
  return Array.from({ length: 7 }, (_, i) => days[(today - 7 + 1 + i) % 7])
})
const weekTotal = computed(() => weekHours.value.reduce((a,b)=>a+b,0))
const aiComment = computed(() => {
  const anyOver8 = weekHours.value.some(h => h >= 8)
  const recentAvg = weekHours.value.slice(-3).reduce((a,b)=>a+b,0)/3
  const prevAvg = weekHours.value.slice(0,4).reduce((a,b)=>a+b,0)/4
  if (recentAvg + 0.01 < prevAvg) return `最近3天平均 ${recentAvg.toFixed(1)} 小时，低于前段时间 ${prevAvg.toFixed(1)} 小时。别松懈，我们一起稳住节奏！`
  if (anyOver8) return `本周出现高强度专注（≥8小时），太棒了！保持休息与补水，持续稳步推进～`
  if (weekTotal.value >= 20) return `本周累计 ${weekTotal.value.toFixed(1)} 小时，进度良好，继续冲！`
  return `本周累计 ${weekTotal.value.toFixed(1)} 小时。建议设定一个每日小目标，循序渐进更稳。`
})

// 目标管理相关状态
interface Goal {
  id: number
  title: string
  description: string
  type: 'short' | 'long'
  completed: boolean
  createdAt: string
}

const shortTermGoals = ref<Goal[]>([])
const longTermGoals = ref<Goal[]>([])
const newGoalTitle = ref('')
const newGoalDescription = ref('')
const newGoalType = ref<'short' | 'long'>('short')
const showAddGoal = ref(false)

// 番茄钟功能
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

const progress = computed(() => {
  return ((totalTime.value - timeLeft.value) / totalTime.value) * 100
})

const doStartTimer = () => {
  isRunning.value = true
  timer.value = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      stopTimer()
      addPomodoroRecord(totalTime.value)
      console.log('番茄钟完成！')
    }
  }, 1000) as unknown as number
}

const startTimer = () => {
  if (isPaused.value) {
    isPaused.value = false
    doStartTimer()
    return
  }
  if (!currentFocusTopic.value.trim()) {
    showFocusTopic.value = true
    return
  }
  doStartTimer()
}

const pauseTimer = () => {
  isPaused.value = true
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const stopTimer = () => {
  isRunning.value = false
  isPaused.value = false
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const resetTimer = () => {
  stopTimer()
  timeLeft.value = totalTime.value
}

// 时间设置功能
const setCustomTime = (minutes: number, seconds: number = customSeconds.value) => {
  customMinutes.value = minutes
  customSeconds.value = seconds
  const clampedSec = Math.max(0, Math.min(59, seconds))
  totalTime.value = minutes * 60 + clampedSec
  timeLeft.value = totalTime.value
  showTimeSettings.value = false
}

const applyCustomTime = () => {
  if (customMinutes.value >= 0 && customMinutes.value <= 120) {
    setCustomTime(customMinutes.value, customSeconds.value)
  }
}

// 番茄钟完成记录功能
const currentFocusTopic = ref('')
const showFocusTopic = ref(false)

const addPomodoroRecord = (duration: number, type: 'work' | 'break' = 'work') => {
  const record: PomodoroRecord = {
    id: Date.now(),
    duration,
    completedAt: new Date().toISOString(),
    type,
    topic: currentFocusTopic.value || '未填写'
  }
  pomodoroRecords.value.unshift(record)
  currentFocusTopic.value = ''
}

const formatDuration = (seconds: number) => {
  const minutes = Math.floor(seconds / 60)
  return `${minutes}分钟`
}

const formatRecordTime = (isoString: string) => {
  const date = new Date(isoString)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 目标管理功能
const loadGoals = async () => {
  try {
    const resp = await axios.get('http://localhost:8080/focus/show-goal')
    const goals = resp?.data || []
    
    console.log('后端返回的目标数据:', goals) // 调试日志
    
    // 清空现有目标
    shortTermGoals.value = []
    longTermGoals.value = []
    
    // 根据 goalType 分类，并进行字段映射
    goals.forEach((item: any) => {
      const goal: Goal = {
        id: item.goalId || item.goal_id || item.id,
        title: item.title,
        description: item.goalContent || item.goal_content || item.description || '',
        type: item.goalType || item.goal_type || item.type,
        completed: item.completed, 
        createdAt: item.createAt || item.create_at || item.createdAt || new Date().toISOString()
      }
      
      if (goal.type === 'short') {
        shortTermGoals.value.push(goal)
      } else if (goal.type === 'long') {
        longTermGoals.value.push(goal)
      }
    })
    
    console.log('处理后的短期目标:', shortTermGoals.value)
    console.log('处理后的长期目标:', longTermGoals.value)
  } catch (e) {
    console.error('加载目标失败', e)
  }
}

const addGoal = async () => {
  if (!newGoalTitle.value.trim()) return
  try {
    // 根据目标类型选择不同的接口
    const apiUrl = newGoalType.value === 'short' 
      ? 'http://localhost:8080/focus/short-goal'
      : 'http://localhost:8080/focus/long-goal'
    
    const resp = await axios.get(apiUrl, {
      params: { 
        [newGoalType.value === 'short' ? 'shortGoal' : 'longGoal']: newGoalDescription.value 
        ,title : newGoalTitle.value
      }
    })
    const returned = resp?.data
    const goalId = typeof returned === 'object' && returned !== null && 'id' in returned
      ? (returned as any).id
      : Number(returned)

    const goal: Goal = {
      id: goalId,
      title: newGoalTitle.value,
      description: newGoalDescription.value,
      type: newGoalType.value,
      completed: false,
      createdAt: new Date().toISOString()
    }

    if (newGoalType.value === 'short') {
      shortTermGoals.value.push(goal)
    } else {
      longTermGoals.value.push(goal)
    }

    // 重置表单
    newGoalTitle.value = ''
    newGoalDescription.value = ''
    showAddGoal.value = false
  } catch (e) {
    console.error('创建目标失败', e)
  }
}

const toggleGoal = async (goalId: number, type: 'short' | 'long') => {
  const goals = type === 'short' ? shortTermGoals.value : longTermGoals.value
  const goal = goals.find(g => g.id === goalId)

  if (goal) {
    try {
      await axios.get("http://localhost:8080/focus/completed",{
        params:{
          goalId: goalId,
          completed: !goal.completed
        }
      })
      goal.completed = !goal.completed
    } catch (error) {
      console.error('更新目标状态失败:', error)
      // 可以添加用户提示，如 message.error('更新失败')
    }
  }
}


const deleteGoal = (goalId: number, type: 'short' | 'long') => {
  const goals = type === 'short' ? shortTermGoals.value : longTermGoals.value
  const index = goals.findIndex(g => g.id === goalId)
  axios.get("http://localhost:8080/focus/delete-goal",{
    params:{
        goalId : goalId
    }
  })
  if (index > -1) {
    goals.splice(index, 1)
  }
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})

// 退出全屏快捷键
onMounted(() => {
  const onKey = (e: KeyboardEvent) => {
    if (e.key === 'Escape') isFullscreen.value = false
  }
  window.addEventListener('keydown', onKey)
  onUnmounted(() => window.removeEventListener('keydown', onKey))
  updateNow()
  const t = setInterval(updateNow, 1000)
  onUnmounted(() => clearInterval(t))
  
  // 加载目标列表
  loadGoals()
})
</script>

<template>
  <div class="focus-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">专注空间</h1>
      <p class="page-subtitle">保持专注，高效学习</p>
    </div>

    <div class="focus-content">
      <!-- 左侧：番茄钟 -->
      <div class="pomodoro-section">
        <div class="pomodoro-card">
          <div class="pomodoro-header">
            <h2 class="section-title">番茄钟</h2>
            <a-space>
              <a-button 
                type="text" 
                size="small" 
                @click="timerStyle = timerStyle === 'ring' ? 'digital' : 'ring'"
                class="settings-btn"
              >
                <template #icon>
                  <FieldTimeOutlined />
                </template>
                切换样式
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click="isFullscreen = !isFullscreen"
                class="settings-btn"
              >
                <template #icon>
                  <component :is="isFullscreen ? FullscreenExitOutlined : FullscreenOutlined" />
                </template>
                {{ isFullscreen ? '退出全屏' : '全屏' }}
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click="showTimeSettings = true"
                class="settings-btn"
              >
                <template #icon>
                  <EditOutlined />
                </template>
                设置时间
              </a-button>
            </a-space>
          </div>
          
          <!-- 计时展示 -->
          <div class="timer-container" :class="['style-'+timerStyle]">
            <div class="timer-circle" v-if="timerStyle==='ring'">
              <svg class="progress-ring" width="200" height="200">
                <circle
                  class="progress-ring-circle-bg"
                  stroke="#f0f0f0"
                  stroke-width="8"
                  fill="transparent"
                  r="90"
                  cx="100"
                  cy="100"
                />
                <circle
                  class="progress-ring-circle"
                  stroke="#1677ff"
                  stroke-width="8"
                  fill="transparent"
                  r="90"
                  cx="100"
                  cy="100"
                  :stroke-dasharray="`${2 * Math.PI * 90}`"
                  :stroke-dashoffset="`${2 * Math.PI * 90 * (1 - progress / 100)}`"
                />
              </svg>
              <div class="timer-display">
                <div class="time-text">{{ formatTime(timeLeft) }}</div>
                <div class="timer-status">
                  <span v-if="!isRunning && !isPaused">准备开始</span>
                  <span v-else-if="isPaused">已暂停</span>
                  <span v-else>专注中</span>
                </div>
              </div>
            </div>
            <div v-else class="digital-time">{{ nowDisplay }}</div>
          </div>

          <!-- 控制按钮 -->
          <div class="timer-controls">
            <a-button 
              v-if="!isRunning" 
              type="primary" 
              size="large" 
              @click="startTimer"
              class="control-btn"
            >
              <template #icon>
                <PlayCircleOutlined />
              </template>
              开始
            </a-button>
            
            <a-button 
              v-else-if="isPaused" 
              type="primary" 
              size="large" 
              @click="startTimer"
              class="control-btn"
            >
              <template #icon>
                <PlayCircleOutlined />
              </template>
              继续
            </a-button>
            
            <a-button 
              v-else 
              type="default" 
              size="large" 
              @click="pauseTimer"
              class="control-btn"
            >
              <template #icon>
                <PauseCircleOutlined />
              </template>
              暂停
            </a-button>
            
            <a-button 
              type="text" 
              size="large" 
              @click="resetTimer"
              class="control-btn"
            >
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </div>
        </div>

        <!-- 番茄钟统计饼图 -->
        <div class="records-card">
          <h2 class="section-title">专注统计</h2>
          <div v-if="topicStats.total === 0" class="empty-records"><p>暂无记录</p></div>
          <div v-else class="pie-wrap">
            <svg viewBox="0 0 64 64" class="pie">
              <template v-for="(e, idx) in topicStats.entries">
                <circle r="20" cx="32" cy="32" pathLength="100"
                  :stroke-dasharray="((e.duration / topicStats.total) * 100) + ' ' + (100 - (e.duration / topicStats.total) * 100)"
                  :stroke-dashoffset="topicStats.entries.slice(0, idx).reduce((s, p) => s + (p.duration/topicStats.total)*100, 0)"
                  :style="{ '--c': pieColors[idx % pieColors.length] }"/>
              </template>
            </svg>
            <div class="legend">
              <div class="legend-item" v-for="(e, idx) in topicStats.entries" :key="e.topic">
                <span class="dot" :style="{ background: pieColors[idx % pieColors.length] }"></span>
                <span class="name">{{ e.topic }}</span>
                <span class="val">{{ Math.round((e.duration / topicStats.total) * 100) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 近一周学习时长折线图（Mock） -->
      <div class="records-card">
        <h2 class="section-title">近一周学习时长</h2>
        <div class="week-chart">
          <svg :viewBox="`0 0 320 160`" class="linechart">
            <defs>
              <linearGradient id="areaFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#1677ff" stop-opacity="0.35" />
                <stop offset="100%" stop-color="#1677ff" stop-opacity="0.02" />
              </linearGradient>
            </defs>
            <polyline :points="weekPoints" fill="none" stroke="#1677ff" stroke-width="2" />
            <polygon :points="`${weekPoints} 292,132 28,132`" fill="url(#areaFill)" />
            <g v-for="(v,i) in weekHours" :key="i">
              <circle :cx="28 + i*(264/6)" :cy="132 - (v/maxHour)*104" r="3" fill="#1677ff" />
            </g>
            <g class="axes">
              <line x1="28" y1="28" x2="28" y2="132" stroke="#e5e7eb" />
              <line x1="28" y1="132" x2="292" y2="132" stroke="#e5e7eb" />
              <text v-for="(d,i) in weekLabels" :key="'d'+i" :x="28 + i*(264/6)" y="148" text-anchor="middle" fill="#6b7280" font-size="10">周{{ d }}</text>
              <text x="0" y="32" fill="#6b7280" font-size="10">(小时)</text>
            </g>
          </svg>
        </div>
        <div class="ai-comment">{{ aiComment }}</div>
      </div>

      <!-- 右侧：目标管理 -->
      <div class="goals-section">
        <!-- 短期目标 -->
        <div class="goals-card">
          <div class="goals-header">
            <h2 class="section-title">短期目标</h2>
            <a-button 
              type="primary" 
              size="small" 
              @click="showAddGoal = true; newGoalType = 'short'"
            >
              <template #icon>
                <PlusOutlined />
              </template>
              添加目标
            </a-button>
          </div>
          
          <div class="goals-list">
            <div v-if="shortTermGoals.length === 0" class="empty-goals">
              <p>暂无短期目标，点击上方按钮添加</p>
            </div>
            <div 
              v-for="goal in shortTermGoals" 
              :key="goal.id" 
              :class="['goal-item', { completed: goal.completed }]"
            >
              <a-checkbox 
                :checked="goal.completed" 
                @change="toggleGoal(goal.id, 'short')"
                class="goal-checkbox"
              />
              <div class="goal-content">
                <div class="goal-title">{{ goal.title }}</div>
                <div v-if="goal.description" class="goal-description">{{ goal.description }}</div>
              </div>
              <a-button 
                type="text" 
                size="small" 
                @click="deleteGoal(goal.id, 'short')"
                class="delete-btn"
              >
                <DeleteOutlined />
              </a-button>
            </div>
          </div>
        </div>

        <!-- 长期目标 -->
        <div class="goals-card">
          <div class="goals-header">
            <h2 class="section-title">长期目标</h2>
            <a-button 
              type="primary" 
              size="small" 
              @click="showAddGoal = true; newGoalType = 'long'"
            >
              <template #icon>
                <PlusOutlined />
              </template>
              添加目标
            </a-button>
          </div>
          
          <div class="goals-list">
            <div v-if="longTermGoals.length === 0" class="empty-goals">
              <p>暂无长期目标，点击上方按钮添加</p>
            </div>
            <div 
              v-for="goal in longTermGoals" 
              :key="goal.id" 
              :class="['goal-item', { completed: goal.completed }]"
            >
              <a-checkbox 
                :checked="goal.completed" 
                @change="toggleGoal(goal.id, 'long')"
                class="goal-checkbox"
              />
              <div class="goal-content">
                <div class="goal-title">{{ goal.title }}</div>
                <div v-if="goal.description" class="goal-description">{{ goal.description }}</div>
              </div>
              <a-button 
                type="text" 
                size="small" 
                @click="deleteGoal(goal.id, 'long')"
                class="delete-btn"
              >
                <DeleteOutlined />
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 全屏番茄钟 -->
    <div v-if="isFullscreen" class="fullscreen-overlay" @click.self="isFullscreen = false">
      <div class="fullscreen-content">
        <div class="pomodoro-card">
          <div class="pomodoro-header">
            <h2 class="section-title">番茄钟（全屏）</h2>
            <a-space>
            <a-button type="text" size="small" @click="timerStyle = timerStyle === 'ring' ? 'digital' : 'ring'" class="settings-btn">
                <template #icon><FieldTimeOutlined /></template>
                切换样式
              </a-button>
              <a-button type="text" size="small" @click="isFullscreen = false" class="settings-btn">
                <template #icon><FullscreenExitOutlined /></template>
                退出全屏
              </a-button>
            </a-space>
          </div>
          <div class="timer-container" :class="['style-'+timerStyle]">
            <div class="timer-circle" v-if="timerStyle==='ring'">
              <svg class="progress-ring" width="360" height="360">
                <circle stroke="#f0f0f0" stroke-width="10" fill="transparent" r="170" cx="180" cy="180" />
                <circle
                  class="progress-ring-circle"
                  stroke="#1677ff"
                  stroke-width="10"
                  fill="transparent"
                  r="170"
                  cx="180"
                  cy="180"
                  :stroke-dasharray="`${2 * Math.PI * 170}`"
                  :stroke-dashoffset="`${2 * Math.PI * 170 * (1 - progress / 100)}`"
                />
              </svg>
              <div class="timer-display">
                <div class="time-text" style="font-size:56px">{{ formatTime(timeLeft) }}</div>
                <div class="timer-status">
                  <span v-if="!isRunning && !isPaused">准备开始</span>
                  <span v-else-if="isPaused">已暂停</span>
                  <span v-else>专注中</span>
                </div>
              </div>
            </div>
            <div v-else class="digital-time" style="font-size:64px">{{ nowDisplay }}</div>
          </div>
          <div class="timer-controls">
            <a-button v-if="!isRunning" type="primary" size="large" @click="startTimer" class="control-btn">
              <template #icon><PlayCircleOutlined /></template>开始
            </a-button>
            <a-button v-else-if="isPaused" type="primary" size="large" @click="startTimer" class="control-btn">
              <template #icon><PlayCircleOutlined /></template>继续
            </a-button>
            <a-button v-else type="default" size="large" @click="pauseTimer" class="control-btn">
              <template #icon><PauseCircleOutlined /></template>暂停
            </a-button>
            <a-button type="text" size="large" @click="resetTimer" class="control-btn">
              <template #icon><ReloadOutlined /></template>重置
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 时间设置模态框 -->
    <a-modal
      v-model:open="showTimeSettings"
      title="设置番茄钟时间"
      @ok="applyCustomTime"
      @cancel="showTimeSettings = false"
    >
      <div class="time-settings">
        <div class="preset-times">
          <h4>快速选择</h4>
          <div class="preset-buttons">
            <a-button 
              v-for="minutes in [15, 20, 25, 30, 45, 60]" 
              :key="minutes"
              :type="customMinutes === minutes ? 'primary' : 'default'"
              @click="setCustomTime(minutes, 0)"
              class="preset-btn"
            >
              {{ minutes }}分钟
            </a-button>
          </div>
        </div>
        
        <a-divider />
        
        <div class="custom-time">
          <h4>自定义时间</h4>
          <div style="display:flex; gap:8px;">
            <a-input-number v-model:value="customMinutes" :min="0" :max="120" addon-after="分钟" style="flex:1" />
            <a-input-number v-model:value="customSeconds" :min="0" :max="59" addon-after="秒" style="flex:1" />
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 专注内容填写 -->
    <a-modal :open="showFocusTopic" title="开始前填写本次专注内容" @ok="() => { if(currentFocusTopic && currentFocusTopic.trim()){ showFocusTopic = false; doStartTimer() } }" @cancel="() => { showFocusTopic = false }">
      <a-input v-model:value="currentFocusTopic" placeholder="例如：高数第3章、英语六级阅读、项目代码重构..." />
      <p style="margin-top:8px;color:#6b7280;">提示：填写后用于统计不同专注内容的占比</p>
    </a-modal>

    <!-- 添加目标模态框 -->
    <a-modal
      v-model:open="showAddGoal"
      title="添加新目标"
      @ok="addGoal"
      @cancel="showAddGoal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="目标类型">
          <a-radio-group v-model:value="newGoalType">
            <a-radio value="short">短期目标</a-radio>
            <a-radio value="long">长期目标</a-radio>
          </a-radio-group>
        </a-form-item>
        
        <a-form-item label="目标标题" required>
          <a-input 
            v-model:value="newGoalTitle" 
            placeholder="请输入目标标题"
            @pressEnter="addGoal"
          />
        </a-form-item>
        
        <a-form-item label="目标描述">
          <a-textarea 
            v-model:value="newGoalDescription" 
            placeholder="请输入目标描述（可选）"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.focus-container {
  padding: 24px;
  background: #f7f8fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.focus-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.pomodoro-section,
.goals-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pomodoro-card,
.goals-card,
.records-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.pomodoro-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pomodoro-header .section-title {
  margin: 0;
}

.settings-btn {
  color: #1677ff;
}

.settings-btn:hover {
  color: #4096ff;
  background: #f0f8ff;
}

.goals-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.goals-header .section-title {
  margin: 0;
}

/* 番茄钟样式 */
.timer-container {
  display: flex;
  justify-content: center;
  margin: 32px 0;
}

.timer-circle {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring {
  transform: rotate(-90deg);
  transition: stroke-dashoffset 0.3s ease;
}

.progress-ring-circle {
  transition: stroke-dashoffset 0.3s ease;
}

.timer-display {
  position: absolute;
  text-align: center;
}

.time-text {
  font-size: 36px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.timer-status {
  font-size: 14px;
  color: #6b7280;
}

.timer-controls {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.control-btn {
  min-width: 100px;
}

/* 模拟表盘样式 */
.analog-clock {
  position: relative;
  width: 220px;
  height: 220px;
  border: 8px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analog-clock .dial {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: inset 0 0 0 2px #f0f0f0;
}

.analog-clock .hand {
  position: absolute;
  width: 2px;
  background: #1f2937;
  transform-origin: bottom center;
  bottom: 50%;
  left: 50%;
}
.analog-clock .hand.minute { height: 70px; }
.analog-clock .hand.second { height: 90px; background: #1677ff; }
.analog-clock .center-dot { width: 8px; height: 8px; background: #1f2937; border-radius: 50%; position: absolute; }
.analog-time { position: absolute; bottom: -32px; font-weight: 600; color: #1f2937; }

/* 全屏遮罩 */
.fullscreen-overlay {
  position: fixed;
  inset: 0;
  background: #f7f8fa;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.fullscreen-content { width: 100%; height: 100%; display:flex; align-items:center; justify-content:center; }
.fullscreen-content .pomodoro-card { width: 70%; }

.digital-time { font-size: 42px; font-weight: 700; color: #1f2937; letter-spacing: 2px; }

/* 目标管理样式 */
.goals-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-goals {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.goal-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.goal-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.goal-item.completed {
  background: #f0f9ff;
  border-color: #bfdbfe;
}

.goal-item.completed .goal-title {
  text-decoration: line-through;
  color: #6b7280;
}

.goal-checkbox {
  margin-right: 12px;
  margin-top: 2px;
}

.goal-content {
  flex: 1;
  min-width: 0;
}

.goal-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
  word-break: break-word;
}

.goal-description {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  word-break: break-word;
}

.delete-btn {
  color: #ef4444;
  margin-left: 8px;
}

.delete-btn:hover {
  color: #dc2626;
  background: #fef2f2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .focus-content {
    grid-template-columns: 1fr;
  }
  
  .timer-circle svg {
    width: 160px;
    height: 160px;
  }
  
  .time-text {
    font-size: 28px;
  }
  
  .timer-controls {
    flex-direction: column;
    align-items: center;
  }
  
  .control-btn {
    width: 200px;
  }
}

/* 记录相关样式 */
.records-list {
  max-height: 300px;
  overflow-y: auto;
}

.empty-records {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.record-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.record-icon {
  color: #52c41a;
  font-size: 16px;
  margin-right: 12px;
}

.record-content {
  flex: 1;
}

.record-duration {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.record-time {
  font-size: 12px;
  color: #6b7280;
}

.more-records {
  text-align: center;
  margin-top: 12px;
}

/* 简单饼图样式（纯SVG） */
.pie-wrap { display: flex; gap: 16px; align-items: center; }
.pie { width: 160px; height: 160px; transform: rotate(-90deg); border-radius: 50%; overflow: hidden; }
.pie circle { fill: none; stroke: var(--c); stroke-width: 24; }
.legend { flex: 1; }
.legend-item { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.legend-item .dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.legend-item .name { flex: 1; color: #374151; }
.legend-item .val { color: #6b7280; }

/* 周学习时长折线图 */
.week-chart { width: 100%; }
.linechart { width: 100%; height: 200px; }
.ai-comment { margin-top: 8px; color: #374151; background: #f8fafc; border: 1px solid #eef2f7; padding: 8px 12px; border-radius: 8px; }

/* 时间设置相关样式 */
.time-settings h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.preset-times {
  margin-bottom: 16px;
}

.preset-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.preset-btn {
  text-align: center;
}

.custom-time {
  margin-top: 16px;
}

/* 饼图颜色集合 */
:root { --pie1:#1677ff; --pie2:#52c41a; --pie3:#faad14; --pie4:#eb2f96; --pie5:#13c2c2; }
</style>
