<script setup lang="ts">
import { h, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  AppstoreOutlined,
  MessageOutlined,
  AimOutlined,
  ReadOutlined,
  RocketOutlined,
  CalendarOutlined,
  SettingOutlined,
  UserOutlined,
  SearchOutlined,
  GlobalOutlined,
  BellOutlined,
} from '@ant-design/icons-vue'

const selectedKeys = ref<string[]>(['chat'])
const openKeys = ref<string[]>(['learn-career'])
const router = useRouter()
const route = useRoute()

// 动态面包屑和菜单选中状态
const breadcrumbItems = computed(() => {
  const path = route.path
  if (path.includes('/chat')) {
    selectedKeys.value = ['chat']
    return [{ title: '聊天区' }]
  } else if (path.includes('/focus')) {
    selectedKeys.value = ['focus']
    return [{ title: '专注空间' }]
  } else if (path.includes('/study')) {
    selectedKeys.value = ['to-study']
    return [{ title: '学业与就业' }, { title: 'To 学业' }]
  } else if (path.includes('/work')) {
    selectedKeys.value = ['to-work']
    return [{ title: '学业与就业' }, { title: 'To 就业' }]
  } else if (path.includes('/settings')) {
    selectedKeys.value = ['user-settings']
    return [{ title: '用户与设置' }]
  }
  selectedKeys.value = ['chat']
  return [{ title: '聊天区' }]
})

type MenuClickInfo = { key: string }
const onMenuClick = (info: MenuClickInfo) => {
  selectedKeys.value = [String(info.key)]
  
  // 根据菜单项导航到对应页面
  switch (info.key) {
    case 'chat':
      router.push('/app/chat')
      break
    case 'focus':
      router.push('/app/focus')
      break
    case 'to-study':
      router.push('/app/study')
      break
    case 'to-work':
      router.push('/app/work')
      break
    case 'user-settings':
      router.push('/app/settings')
      break
  }
}
</script>

<template>
  <a-layout class="app-layout">
    <a-layout-sider class="sider" :width="200" theme="light">
      <div class="brand">
        <AppstoreOutlined class="brand-icon" />
        <span class="brand-text">AI 学习搭子</span>
      </div>
      <a-menu
        mode="inline"
        :selectedKeys="selectedKeys"
        :openKeys="openKeys"
        @click="onMenuClick"
      >
        <a-menu-item key="chat">
          <template #icon>
            <MessageOutlined />
          </template>
          聊天区
        </a-menu-item>

        <a-menu-item key="focus">
          <template #icon>
            <AimOutlined />
          </template>
          专注空间
        </a-menu-item>

        <a-sub-menu key="learn-career">
          <template #icon>
            <ReadOutlined />
          </template>
          <template #title>学业与就业</template>
          <a-menu-item key="to-study">
            <template #icon>
              <ReadOutlined />
            </template>
            To 学业
          </a-menu-item>
          <a-menu-item key="to-work">
            <template #icon>
              <RocketOutlined />
            </template>
            To 就业
          </a-menu-item>
        </a-sub-menu>

        

        <a-menu-item key="user-settings">
          <template #icon>
            <SettingOutlined />
          </template>
          用户与设置
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <div class="left">
          <a-breadcrumb>
            <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item.title">
              {{ item.title }}
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="right">
          <a-space size="large">
            <a-button type="text"><SearchOutlined /></a-button>
            <a-button type="text"><GlobalOutlined /></a-button>
            <a-button type="text"><BellOutlined /></a-button>
            <a-avatar size="small"><UserOutlined /></a-avatar>
            <a-button type="text"><SettingOutlined /></a-button>
          </a-space>
        </div>
      </a-layout-header>

      <a-layout-content class="content">
        <!-- 子路由内容渲染 -->
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
  
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: #f7f8fa;
}

.sider {
  border-right: 1px solid #f0f0f0;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
  font-weight: 600;
}

.brand-icon {
  color: #1677ff;
  font-size: 18px;
}

.brand-text {
  color: #1f2937;
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 56px;
  border-bottom: 1px solid #f0f0f0;
}

.content {
  padding: 0;
  height: calc(100vh - 56px);
  overflow-y: auto;
}
</style>

