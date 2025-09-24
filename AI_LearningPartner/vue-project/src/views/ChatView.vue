<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onMounted } from 'vue'
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

// 从后端加载会话列表
const loadConversations = async () => {
  try {
    const resp = await axios.get('http://localhost:8080/conversation/list')
    const list = resp.data?.data ?? resp.data ?? []
    const normalized: Conversation[] = (list as any[]).map(item => ({
      id: item.id,
      title: item.title || '新对话',
      lastMessage: item.lastMessage || '',
      time: item.updatedAt ? new Date(item.updatedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '刚刚'
    }))
    conversations.value = normalized
    // 初始化映射
    convIdToMessages.value = {}
    // 默认选中第一条会话并加载消息
    if (conversations.value.length > 0) {
      await selectConversation(conversations.value[0].id)
    }
  } catch (e) {
    console.warn('加载会话列表失败', e)
  }
}

// 从后端加载指定会话的消息
const loadMessages = async (conversationId: number) => {
  try {
    const resp = await axios.get(`http://localhost:8080/conversation/${conversationId}/messages`)
    const list = resp.data?.data ?? resp.data ?? []
    const normalized: Message[] = (list as any[]).map((m, idx) => ({
      id: m.id ?? idx,
      role: (m.role === 'assistant' ? 'assistant' : 'user') as 'assistant' | 'user',
      content: m.content ?? '',
      time: m.createdAt ? new Date(m.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '刚刚'
    }))
    convIdToMessages.value[conversationId] = normalized
    messages.value = convIdToMessages.value[conversationId]
  } catch (e) {
    console.warn('加载消息失败', e)
    convIdToMessages.value[conversationId] = []
    messages.value = []
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

// 调用后端创建会话
const createConversationOnServer = async (): Promise<number> => {
  const resp = await axios.post('http://localhost:8080/conversation/create', {
    title: '新对话',
    lastMessage: ''
  })
  return resp.data?.data ?? resp.data
}

// 确保存在后端会话并返回其ID；若当前为本地临时ID（时间戳），则替换为服务端ID
const ensureConversationOnServer = async (): Promise<number> => {
  const isTempId = (id: number | null) => {
    if (!id) return true
    return String(id).length >= 13
  }

  if (!currentConversation.value || isTempId(currentConversation.value)) {
    const serverId = await createConversationOnServer()

    if (currentConversation.value && convIdToMessages.value[currentConversation.value]) {
      const oldId = currentConversation.value
      convIdToMessages.value[serverId] = convIdToMessages.value[oldId]
      delete convIdToMessages.value[oldId]
      const idx = conversations.value.findIndex(c => c.id === oldId)
      if (idx !== -1) {
        conversations.value[idx].id = serverId
      }
    } else {
      convIdToMessages.value[serverId] = []
    }

    if (!conversations.value.find(c => c.id === serverId)) {
      const newConv: Conversation = {
        id: serverId,
        title: '新对话',
        lastMessage: '',
        time: '刚刚'
      }
      conversations.value.unshift(newConv)
    }

    currentConversation.value = serverId
    messages.value = convIdToMessages.value[serverId]
    return serverId
  }

  return currentConversation.value
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

  // 确保使用服务端真实会话ID
  const conversationId = await ensureConversationOnServer()

  // 追加到当前会话消息（本地）
  if (currentConversation.value) {
    if (!convIdToMessages.value[currentConversation.value]) {
      convIdToMessages.value[currentConversation.value] = []
    }
    messages.value = convIdToMessages.value[currentConversation.value]
  }

  messages.value.push(newMessage)
  inputValue.value = ''
  selectedFiles.value = []

  // 将用户消息持久化
  try {
    await axios.post(`http://localhost:8080/conversation/${conversationId}/message`, {
      role: 'user',
      content: newMessage.content
    })
  } catch (e) {
    console.warn('保存用户消息失败（不影响前端显示）', e)
  }

  // 调用AI API（流式）
  isTyping.value = true
  try {
    const payload = {
      content: newMessage.content,
      memoryId: localStorage.getItem('aliyun_memory_id') || undefined,
    }

    // 占位的助手消息（用于流式增量）
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
              let delta = ''
              if (fullText.startsWith(lastFullText)) {
                delta = fullText.slice(lastFullText.length)
              } else {
                delta = fullText
              }
              assistantMsg.content += delta
              lastFullText = fullText
            }
          } catch (_) {}
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

    // 持久化助手消息
    try {
      await axios.post(`http://localhost:8080/conversation/${conversationId}/message`, {
        role: 'assistant',
        content: assistantMsg.content
      })
    } catch (e) {
      console.warn('保存助手消息失败（不影响前端显示）', e)
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

// 新建对话：服务端创建
const createNewConversation = async () => {
  const serverId = await createConversationOnServer()
  const newConv: Conversation = {
    id: serverId,
    title: '新对话',
    lastMessage: '',
    time: '刚刚'
  }
  conversations.value.unshift(newConv)
  currentConversation.value = newConv.id
  convIdToMessages.value[newConv.id] = []
  messages.value = convIdToMessages.value[newConv.id]
}

// 选择对话：加载后端消息
const selectConversation = async (id: number) => {
  currentConversation.value = id
  await loadMessages(id)
}

onMounted(async () => {
  await loadConversations()
})
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
