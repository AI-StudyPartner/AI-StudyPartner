<script setup lang="ts">
import { ref, reactive } from 'vue'
import { SendOutlined, PlusOutlined, MoreOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

// 对话列表数据
interface Conversation {
  id: number
  title: string
  lastMessage: string
  time: string
}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  time: string
  attachments?: Array<{ name: string; type: string; url: string }>
}

// 解析后端返回的SSE文本，提取最新一条 data:{...} 里的 output.text
function extractSseText(raw: string): string {
  if (!raw) return ''
  try {
    // 常见情况：多行包含 data: 前缀
    const lines = raw.split(/\r?\n/)
    const jsonPayloads: any[] = []
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) continue
      if (trimmed.startsWith('data:')) {
        const jsonStr = trimmed.slice('data:'.length).trim()
        try {
          const obj = JSON.parse(jsonStr)
          jsonPayloads.push(obj)
        } catch (_) {
          // 忽略非JSON行
        }
      } else {
        // 有些网关可能直接返回JSON
        try {
          const obj = JSON.parse(trimmed)
          jsonPayloads.push(obj)
        } catch (_) {
          // 忽略
        }
      }
    }
    // 取最后一个包含 output.text 的对象
    for (let i = jsonPayloads.length - 1; i >= 0; i--) {
      const obj = jsonPayloads[i]
      const text = obj?.output?.text
      if (typeof text === 'string') return text
    }
    return ''
  } catch {
    return ''
  }
}

const conversations = ref<Conversation[]>([])
// 每个会话的消息映射
const convIdToMessages = ref<Record<number, Message[]>>({})

// 当前对话
const currentConversation = ref<number | null>(null)

// 当前消息列表（由映射派生）
const messages = ref<Message[]>([])

// 输入框
const inputValue = ref('')
const isTyping = ref(false)
// 选择的附件（不立即上传）
const selectedFiles = ref<File[]>([])

const beforeAttach = (file: File) => {
  selectedFiles.value = [...selectedFiles.value, file]
  return false
}

const removeAttach = (file: any) => {
  selectedFiles.value = selectedFiles.value.filter(f => f !== file)
}

// 发送消息
const sendMessage = async () => {
  if (!inputValue.value.trim()) return

  const newMessage: Message = {
    id: Date.now(),
    role: 'user',
    content: inputValue.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    attachments: selectedFiles.value.map(f => ({
      name: f.name,
      type: f.type,
      url: URL.createObjectURL(f)
    }))
  }

  // 如果没有会话，自动创建
  if (!currentConversation.value) {
    const newConv: Conversation = {
      id: Date.now(),
      title: '新对话',
      lastMessage: '',
      time: '刚刚'
    }
    conversations.value.unshift(newConv)
    currentConversation.value = newConv.id
    convIdToMessages.value[newConv.id] = []
    messages.value = convIdToMessages.value[newConv.id]
  }

  // 追加到当前会话消息
  if (currentConversation.value) {
    if (!convIdToMessages.value[currentConversation.value]) {
      convIdToMessages.value[currentConversation.value] = []
    }
    messages.value = convIdToMessages.value[currentConversation.value]
  }

  messages.value.push(newMessage)
  inputValue.value = ''
  selectedFiles.value = []

  // 调用AI API（流式）
  isTyping.value = true
  try {
    const payload = {
      content: newMessage.content,
      memoryId: localStorage.getItem('aliyun_memory_id') || undefined,
      // knowledgeBaseId: ['uhym8y9eq3']
    }

    // 先放一个占位的助手消息，后续逐步填充内容
    const assistantMsg: Message = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    messages.value.push(assistantMsg)

    // 已有占位助手消息，关闭全局打字指示器，避免出现第二个AI气泡
    isTyping.value = false

    const response = await fetch('http://localhost:8080/aliyunbailian/application/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok || !response.body) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let lastFullText = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // 以换行拆分，尽可能处理完整的行，剩余半行留到下次
      const parts = buffer.split(/\r?\n/)
      buffer = parts.pop() || ''

      for (const line of parts) {
        const trimmed = line.trim()
        if (!trimmed) continue
        if (trimmed.startsWith('data:')) {
          const payloadStr = trimmed.slice('data:'.length).trim()
          try {
            const obj = JSON.parse(payloadStr)
            const fullText = obj?.output?.text
            if (typeof fullText === 'string') {
              // 计算增量（不少后端会推送完整前缀文本）
              let delta = ''
              if (fullText.startsWith(lastFullText)) {
                delta = fullText.slice(lastFullText.length)
              } else {
                // 后端可能只推送片段，直接使用
                delta = fullText
              }
              assistantMsg.content += delta
              lastFullText = fullText
            }
          } catch (_) {
            // 忽略非JSON
          }
        }
      }

      // 更新会话预览
      if (currentConversation.value) {
        const conv = conversations.value.find(c => c.id === currentConversation.value)
        if (conv) {
          conv.lastMessage = assistantMsg.content
          conv.time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        }
      }
    }

    // 处理buffer里最后半行
    if (buffer.trim().startsWith('data:')) {
      try {
        const obj = JSON.parse(buffer.trim().slice('data:'.length).trim())
        const fullText = obj?.output?.text
        if (typeof fullText === 'string') {
          const delta = fullText.startsWith(lastFullText) ? fullText.slice(lastFullText.length) : fullText
          assistantMsg.content += delta
        }
      } catch (_) {}
    }
  } catch (error) {
    console.error('API调用失败:', error)
    const errorReply: Message = {
      id: Date.now() + 2,
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    messages.value.push(errorReply)
  } finally {
    // 确保结束时不显示打字指示器
    isTyping.value = false
    // 更新会话预览
    if (currentConversation.value) {
      const conv = conversations.value.find(c => c.id === currentConversation.value)
      if (conv) {
        conv.lastMessage = messages.value[messages.value.length - 1]?.content || ''
        conv.time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
    }
  }
}

// 新建对话
const createNewConversation = () => {
  const newConv: Conversation = {
    id: Date.now(),
    title: '新对话',
    lastMessage: '',
    time: '刚刚'
  }
  conversations.value.unshift(newConv)
  currentConversation.value = newConv.id
  convIdToMessages.value[newConv.id] = []
  messages.value = convIdToMessages.value[newConv.id]
}

// 选择对话
const selectConversation = (id: number) => {
  currentConversation.value = id
  messages.value = convIdToMessages.value[id] || []
}

// 删除对话
const deleteConversation = (id: number) => {
  const index = conversations.value.findIndex(c => c.id === id)
  if (index === -1) return
  conversations.value.splice(index, 1)
  delete convIdToMessages.value[id]

  if (currentConversation.value === id) {
    if (conversations.value.length > 0) {
      const newId = conversations.value[0].id
      currentConversation.value = newId
      messages.value = convIdToMessages.value[newId] || []
    } else {
      currentConversation.value = null
      messages.value = []
    }
  }
}
</script>

<template>
  <div class="chat-container">
    <!-- 左侧对话列表 -->
    <div class="conversation-sidebar">
      <div class="sidebar-header">
        <a-button type="primary" @click="createNewConversation" class="new-chat-btn">
          <template #icon>
            <PlusOutlined />
          </template>
          新建对话
        </a-button>
      </div>

      <div class="conversation-list">
        <div v-if="conversations.length === 0" class="empty-conversations">
          <div class="empty-text">暂无对话，点击上方按钮开始新对话</div>
        </div>
        <div v-for="conv in conversations" :key="conv.id"
          :class="['conversation-item', { active: currentConversation === conv.id }]">
          <div class="conv-main" @click="selectConversation(conv.id)">
            <div class="conv-title">{{ conv.title }}</div>
            <div class="conv-preview">{{ conv.lastMessage || '开始新对话...' }}</div>
          </div>
          <div class="conv-actions">
            <a-button type="text" size="small" @click.stop="deleteConversation(conv.id)">
              <DeleteOutlined />
            </a-button>
            <div class="conv-time">{{ conv.time }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-title">AI学习搭子</div>
        <a-button type="text" class="more-btn">
          <MoreOutlined />
        </a-button>
      </div>

      <!-- 消息列表 -->
      <div class="messages-container">
        <div class="messages-list">
          <div v-if="messages.length === 0 && !isTyping" class="empty-messages">
            <div class="empty-message-text">开始与AI学习搭子对话吧！</div>
          </div>
          <div v-for="message in messages" :key="message.id" :class="['message', message.role]">
            <div class="message-avatar">
              <a-avatar v-if="message.role === 'assistant'" :size="32" style="background-color: #1677ff">
                AI
              </a-avatar>
              <a-avatar v-else :size="32" style="background-color: #52c41a">
                U
              </a-avatar>
            </div>
            <div class="message-content">
              <template v-if="message.role === 'assistant' && !message.content">
                <div class="typing-indicator"><span></span><span></span><span></span></div>
              </template>
              <template v-else>
                <div class="message-text">{{ message.content }}</div>
              </template>
              <div v-if="message.attachments && message.attachments.length" class="attachments">
                <template v-for="att in message.attachments">
                  <img v-if="att.type.startsWith('image/')" :src="att.url" class="att-image" :alt="att.name" />
                  <div v-else class="att-file">
                    <a :href="att.url" target="_blank">{{ att.name }}</a>
                  </div>
                </template>
              </div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>

          <!-- 正在输入指示器 -->
          <div v-if="isTyping" class="message assistant">
            <div class="message-avatar">
              <a-avatar :size="32" style="background-color: #1677ff">AI</a-avatar>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <a-upload
            :before-upload="beforeAttach"
            :file-list="(selectedFiles as any)"
            :show-upload-list="false"
          >
            <a-button class="attach-btn" title="添加附件">+</a-button>
          </a-upload>

          <a-textarea v-model:value="inputValue" placeholder="随意输点什么吧...（可附带图片/文件）" :auto-size="{ minRows: 1, maxRows: 4 }"
            @keydown.enter.prevent="sendMessage" class="message-input" />

          <a-button type="primary" @click="sendMessage" :disabled="!inputValue.trim() && selectedFiles.length === 0" class="send-btn">
            <SendOutlined />
          </a-button>
        </div>

        <!-- 选中文件预览/可移除 -->
        <div v-if="selectedFiles.length" class="attach-preview">
          <div class="attach-item" v-for="file in selectedFiles" :key="file.name">
            <span class="name">{{ file.name }}</span>
            <a-button type="link" size="small" @click="removeAttach(file)">移除</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  height: 100%;
  background: #f5f5f5;
}

.conversation-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.new-chat-btn {
  width: 100%;
  height: 40px;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conversation-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #e6f7ff;
  border-right: 3px solid #1677ff;
}

.conv-title {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-main { flex: 1; min-width: 0; }
.conv-actions { display: flex; align-items: center; gap: 6px; }

.conv-preview {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.conv-time {
  font-size: 11px;
  color: #bfbfbf;
}

.empty-conversations {
  padding: 20px;
  text-align: center;
}

.empty-text {
  color: #8c8c8c;
  font-size: 14px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  flex-shrink: 0;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.more-btn {
  color: #8c8c8c;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.messages-list {
  max-width: 800px;
  margin: 0 auto;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.empty-message-text {
  color: #8c8c8c;
  font-size: 16px;
}

.message {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  display: inline-block;
}

.message.user .message-content {
  text-align: right;
  align-self: flex-end;
}

.message-text {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .message-text {
  background: #1677ff;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 12px;
  width: fit-content;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #8c8c8c;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {

  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-10px);
  }
}

.input-area {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
  flex-shrink: 0;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.attach-btn {
  min-width: 40px;
}

.message-input {
  flex: 1;
  border-radius: 20px;
  resize: none;
}

.send-btn {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.attach-preview {
  max-width: 800px;
  margin: 8px auto 0;
}

.attach-item { 
  font-size: 12px; 
  color: #6b7280; 
}

.attachments { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px; }
.att-image { width: 120px; height: 80px; object-fit: cover; border-radius: 8px; border: 1px solid #eee; }
.att-file { font-size: 12px; }
</style>
