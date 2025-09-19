import { createRouter, createWebHistory } from 'vue-router'

const Auth = () => import('../views/Auth.vue')
const AppLayout = () => import('../views/AppLayout.vue')
const ChatView = () => import('../views/ChatView.vue')
const FocusView = () => import('../views/FocusView.vue')
const UserSettingsView = () => import('../views/UserSettingsView.vue')
const ReviewKnowledgeView = () => import('../views/ReviewKnowledgeView.vue')
const StudyView = () => import('../views/StudyView.vue')
const WorkView = () => import('../views/WorkView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/auth',
    },
    {
      path: '/auth',
      name: 'auth',
      component: Auth,
    },
    {
      path: '/app',
      name: 'app',
      component: AppLayout,
      children: [
        {
          path: '',
          redirect: '/app/chat'
        },
        {
          path: 'chat',
          name: 'chat',
          component: ChatView,
        },
        {
          path: 'focus',
          name: 'focus',
          component: FocusView,
        }
        ,
        {
          path: 'settings',
          name: 'settings',
          component: UserSettingsView,
        }
        ,
        {
          path: 'review',
          name: 'review',
          component: ReviewKnowledgeView,
        }
        ,
        {
          path: 'study',
          name: 'study',
          component: StudyView,
        }
        ,
        {
          path: 'work',
          name: 'work',
          component: WorkView,
        }
      ]
    },
  ],
})

export default router
