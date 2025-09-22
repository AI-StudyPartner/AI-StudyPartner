<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { message } from 'ant-design-vue'

type LoginForm = {
  email: string
  password: string
  remember: boolean
}

type RegisterForm = {
  email: string
  password: string
  confirmPassword: string
}

const activeKey = ref('login')
const router = useRouter()
const loading = ref(false)

const loginForm = reactive<LoginForm>({
  email: '',
  password: '',
  remember: true,
})

const registerForm = reactive<RegisterForm>({
  email: '',
  password: '',
  confirmPassword: '',
})

const onLogin = () => {
  loading.value = true
  axios.get('http://localhost:8080/login', {
    params:{
      email: registerForm.email,
      password: registerForm.password
    }
  })
    .then(response => {
      const users = response.data
      const user = users.find((u: any) => u.email === loginForm.email && u.password === loginForm.password)
      if (user) {
        message.success('登录成功')
        // 存储用户信息到localStorage
        localStorage.setItem('user', JSON.stringify({ id: user.id, email: user.email }))
        router.push('/app/chat')
      } else {
        message.error('邮箱或密码错误')
      }
    })
    .catch(error => {
      console.error('登录失败:', error)
      message.error('登录失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
    })
}

const onRegister = () => {
  if (registerForm.password !== registerForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  axios.get('http://localhost:8080/register', {
    params: {
      email: registerForm.email,
      password: registerForm.password
    }
  })
    .then(response => {
      if (response.data.message === '注册成功') {
        message.success('注册成功')
        // 注册成功后自动登录
        localStorage.setItem('user', JSON.stringify({ 
          id: response.data.userId, 
          email: registerForm.email 
        }))
        router.push('/app/chat')
      } else {
        message.error(response.data.message || '注册失败')
      }
    })
    .catch(error => {
      console.error('注册失败:', error)
      message.error('注册失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<template>
  <div class="auth-page">
    <div class="brand">
      <img class="logo" src="/favicon.ico" alt="logo" />
      <div class="title">Learning Partner</div>
    </div>

    <a-card class="auth-card" :bordered="false">
      <a-tabs v-model:activeKey="activeKey" centered>
        <a-tab-pane key="login" tab="登录">
          <a-form layout="vertical" :model="loginForm" @finish="onLogin">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="loginForm.email" size="large" placeholder="name@example.com">
                <template #prefix>
                  <MailOutlined />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="密码" name="password">
              <a-input-password v-model:value="loginForm.password" size="large" placeholder="请输入密码">
                <template #prefix>
                  <LockOutlined />
                </template>
              </a-input-password>
            </a-form-item>

            <div class="actions">
              <a-checkbox v-model:checked="loginForm.remember">记住我</a-checkbox>
              <a-typography-link>忘记密码?</a-typography-link>
            </div>

            <a-button type="primary" size="large" block html-type="submit" :loading="loading">登录</a-button>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="register" tab="注册">
          <a-form layout="vertical" :model="registerForm" @finish="onRegister">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="registerForm.email" size="large" placeholder="name@example.com">
                <template #prefix>
                  <MailOutlined />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="密码" name="password">
              <a-input-password v-model:value="registerForm.password" size="large" placeholder="设置密码">
                <template #prefix>
                  <LockOutlined />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item label="确认密码" name="confirmPassword">
              <a-input-password v-model:value="registerForm.confirmPassword" size="large" placeholder="再次输入密码">
                <template #prefix>
                  <LockOutlined />
                </template>
              </a-input-password>
            </a-form-item>

            <a-button type="primary" size="large" block html-type="submit" :loading="loading">创建账户</a-button>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <div class="footer">© 2025 Learning Partner</div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr auto;
  align-items: center;
  justify-items: center;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 100%);
  padding: 40px 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1d39c4;
  margin-bottom: 16px;
}

.brand .logo {
  width: 28px;
  height: 28px;
}

.brand .title {
  font-size: 18px;
  font-weight: 600;
}

.auth-card {
  width: 100%;
  max-width: 800px;
  aspect-ratio: 4 / 3;
  box-shadow: 0 6px 24px rgba(24, 144, 255, 0.15);
  border-radius: 12px;
  /* 基础字号，随容器宽度适度放大 */
  --auth-base-font: clamp(14px, 0.6vw + 12px, 18px);
}

/* 让内容在等比高度内自然居中显示 */
:deep(.auth-card .ant-card-body) {
  height: 100%;
  display: grid;
  align-content: center;
  padding: 32px 40px;
}

/* Tabs 字号与权重 */
:deep(.auth-card .ant-tabs-tab-btn) {
  font-size: calc(var(--auth-base-font) + 2px);
  font-weight: 600;
}

/* 表单标签与说明文字 */
:deep(.auth-card .ant-form-item-label > label) {
  font-size: var(--auth-base-font);
}

/* 输入框与密码框（large 尺寸再放大一点） */
:deep(.auth-card .ant-input-lg),
:deep(.auth-card .ant-input-affix-wrapper-lg) {
  font-size: var(--auth-base-font);
  padding: 10px 12px;
}

/* 复选框与链接 */
:deep(.auth-card .ant-checkbox-wrapper) {
  font-size: var(--auth-base-font);
}
:deep(.auth-card .ant-typography) {
  font-size: var(--auth-base-font);
}

/* 主按钮尺寸与字号 */
:deep(.auth-card .ant-btn-lg) {
  height: 48px;
  font-size: calc(var(--auth-base-font) + 2px);
}

@media (max-width: 960px) {
  .auth-card {
    aspect-ratio: 4 / 3;
    max-width: 92vw;
  }
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 16px;
}

.footer {
  color: #98a2b3;
  font-size: 12px;
  margin-top: 24px;
}
</style>

