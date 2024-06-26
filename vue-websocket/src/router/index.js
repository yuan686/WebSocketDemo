import Vue from 'vue'
import VueRouter from 'vue-router'
import ChatView from '@/views/ChatView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'ChatView',
    component: ChatView
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

export default router
