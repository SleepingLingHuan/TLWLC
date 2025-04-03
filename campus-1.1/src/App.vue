<script setup>
/**
 * App组件 - 应用程序的主要入口组件
 * 负责全局布局、导航和用户登录状态管理
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { UserOutlined, LogoutOutlined, FilterOutlined, HomeOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import ProductList from './components/ProductList.vue'
import UserCenter from './components/UserCenter.vue'
import AdvancedSearch from './components/AdvancedSearch.vue'  // 引入高级搜索组件

// 路由和状态管理器初始化
const router = useRouter()
const store = useStore()

/**
 * 应用程序状态变量
 * tab: 当前激活的标签页（'home'为首页，'userCenter'为个人中心）
 * searchParams: 搜索参数对象
 */
const tab = ref('home')   // 当前激活的标签页
const searchParams = ref({}) // 搜索参数

/**
 * 计算属性 - 登录状态和用户信息
 * isLoggedIn: 是否已登录
 * username: 当前登录用户的名称
 */
const isLoggedIn = computed(() => store.state.isLoggedIn)
const username = computed(() => store.state.userInfo?.username || '未登录') // 使用可选链操作符访问username

/**
 * 切换导航标签页处理函数
 * @param {Object} param0 - 包含key属性的对象，表示选中的标签页
 */
const handleTabChange = ({ key }) => {
  tab.value = key
  if (key === 'home') {
    router.push('/')
  } else if (key === 'userCenter') {
    // 如果未登录，跳转到登录页
    if (!isLoggedIn.value) {
      message.warning('请先登录')
      router.push('/login')
    } else {
      router.push('/user-center')
    }
  }
}

/**
 * 处理退出登录
 * 清除用户状态并跳转回首页
 */
const handleLogout = () => {
  store.dispatch('logout')
  message.success('已退出登录')
  
  // 如果当前在个人中心页，跳转回首页
  if (router.currentRoute.value.path === '/user-center') {
    router.push('/')
  }
}

/**
 * 处理高级搜索操作
 * @param {Object} params - 高级搜索参数
 */
const handleAdvancedSearch = (params) => {
  // 确保params对象存在
  if (!params) params = {};
  
  // 如果没有排序方式，添加默认排序(价格从低到高)
  if (!params.sort) {
    params.sort = 'price_asc';
  }
  
  searchParams.value = params;
}

/**
 * 处理显示搜索抽屉
 */
const showDrawer = () => {
  // 实现显示搜索抽屉的逻辑
}

const handleUserClick = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/user-center')
}

const handleUploadClick = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/upload')
}
</script>

<template>
  <div class="app">
    <a-layout>
      <!-- 页面顶部导航栏 -->
      <a-layout-header class="header">
        <!-- 左侧菜单区域 -->
        <div class="header-left">
          <a-menu
            mode="horizontal"
            theme="dark"
            :selected-keys="[tab]"
            @select="handleTabChange"
          >
            <a-menu-item key="home">
              <HomeOutlined />
              首页
            </a-menu-item>
            <a-menu-item key="upload" @click="handleUploadClick">
              <PlusOutlined />
              发布商品
            </a-menu-item>
            <a-menu-item key="userCenter" @click="handleUserClick">
              <UserOutlined />
              个人中心
            </a-menu-item>
          </a-menu>
        </div>
        
        <!-- 中间标题区域 -->
        <div class="header-center">
          <h1 class="title">校园二手交易平台</h1>
        </div>
        
        <!-- 右侧搜索与用户区域 -->
        <div class="header-right">
          <!-- 高级搜索组件 -->
          <AdvancedSearch @search="handleAdvancedSearch" class="header-search" />
          
          <!-- 未登录状态显示登录按钮 -->
          <div v-if="!isLoggedIn" class="user-info">
            <a-button type="link" @click="$router.push('/login')">登录</a-button>
          </div>
          
          <!-- 已登录状态显示用户信息和退出按钮 -->
          <div v-else class="user-info">
            <span class="welcome-text">你好，{{ username }}</span>
            <a-button @click="handleLogout">
              <template #icon><LogoutOutlined /></template>
              退出
            </a-button>
          </div>
        </div>
      </a-layout-header>
      
      <!-- 页面主体内容区域 -->
      <a-layout-content class="content">
        <!-- 使用路由视图组件，传递搜索参数 -->
        <router-view :searchParams="searchParams" :key="JSON.stringify(searchParams)" />
      </a-layout-content>
      
      <!-- 页面底部版权区域 -->
      <a-layout-footer class="footer">
        <!-- 底部版权信息已移除 -->
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style>
/* app: 整个应用容器样式 */
.app {
  min-height: 100vh;
}

/* header: 顶部导航栏样式 */
.header {
  background: #001529;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 64px;
  line-height: 64px;
}

/* header-left: 导航栏左侧区域样式 */
.header-left {
  flex: 1;
}

/* header-center: 导航栏中间区域样式 */
.header-center {
  flex: 1;
  text-align: center;
}

/* header-right: 导航栏右侧区域样式 */
.header-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

/* title: 平台标题样式 */
.title {
  color: #fff;
  margin: 0;
  font-size: 22px;
}

/* user-info: 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
}

/* welcome-text: 欢迎文本样式 */
.welcome-text {
  color: #fff;
  margin-right: 8px;
}

/* header-search: 顶部搜索区域样式 */
.header-search {
  display: inline-block;
}

/* content: 主体内容区域样式 */
.content {
  padding: 20px;
  margin: 16px auto;
  max-width: 1200px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 500px;
}

/* footer: 底部版权区域样式 */
.footer {
  text-align: center;
  background: #f0f2f5;
  padding: 16px;
}

/* body: 全局页面样式 */
body {
  background: #f0f2f5;
  margin: 0;
  padding: 0;
}
</style>
