<template>
  <div class="login-container">
    <!-- 登录/注册卡片 -->
    <a-card class="login-card">
      <!-- 标签页切换：登录/注册 -->
      <a-tabs default-active-key="login">
        <!-- 登录标签页 -->
        <a-tab-pane key="login" tab="登录">
          <!-- 登录表单 -->
          <a-form :model="loginForm" layout="vertical">
            <!-- 账号输入框 -->
            <a-form-item
              label="账号"
              name="username"
              :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input v-model:value="loginForm.username" placeholder="请输入账号" />
            </a-form-item>
            
            <!-- 密码输入框 -->
            <a-form-item
              label="密码"
              name="password"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" />
            </a-form-item>
            
            <!-- 登录按钮 -->
            <a-form-item>
              <a-button type="primary" block @click="handleLogin" :loading="loading">
                登录
              </a-button>
            </a-form-item>
            
            <!-- 测试账号提示 -->
            <div class="test-account-tip">
              <p>测试账号：123456</p>
              <p>测试密码：hubangmin</p>
            </div>
          </a-form>
        </a-tab-pane>
        
        <!-- 注册标签页 -->
        <a-tab-pane key="register" tab="注册">
          <!-- 注册表单 -->
          <a-form :model="registerForm" layout="vertical">
            <!-- 账号输入框 -->
            <a-form-item
              label="账号"
              name="username"
              :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input v-model:value="registerForm.username" placeholder="请输入账号" />
            </a-form-item>
            
            <!-- 密码输入框 -->
            <a-form-item
              label="密码"
              name="password"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <a-input-password v-model:value="registerForm.password" placeholder="请输入密码" />
            </a-form-item>
            
            <!-- 确认密码输入框 -->
            <a-form-item
              label="确认密码"
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password v-model:value="registerForm.confirmPassword" placeholder="请确认密码" />
            </a-form-item>
            
            <!-- 昵称输入框 -->
            <a-form-item
              label="昵称"
              name="nickname"
              :rules="[{ required: true, message: '请输入昵称' }]"
            >
              <a-input v-model:value="registerForm.nickname" placeholder="请输入昵称" />
            </a-form-item>
            
            <!-- 注册按钮 -->
            <a-form-item>
              <a-button type="primary" block @click="handleRegister" :loading="loading">
                注册
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
/**
 * 登录与注册组件
 * 提供用户登录和注册功能
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { useStore } from 'vuex'

// 路由和状态管理器初始化
const router = useRouter()
const store = useStore()
const loading = ref(false) // 加载状态

/**
 * 登录表单数据
 * username: 用户账号
 * password: 用户密码
 */
const loginForm = reactive({
  username: '',
  password: ''
})

/**
 * 注册表单数据
 * username: 用户账号
 * password: 用户密码
 * confirmPassword: 确认密码
 * nickname: 用户昵称
 */
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

/**
 * 验证确认密码是否与密码一致
 * @param {Object} rule - 验证规则
 * @param {string} value - 输入的确认密码值
 * @throws {Error} 密码不一致时抛出错误
 */
const validateConfirmPassword = async (rule, value) => {
  if (value !== registerForm.password) {
    throw new Error('两次输入的密码不一致')
  }
}

/**
 * 处理用户登录
 * 验证表单并提交登录请求
 */
const handleLogin = async () => {
  // 表单验证
  if (!loginForm.username || !loginForm.password) {
    message.error('请输入账号和密码')
    return
  }
  
  loading.value = true
  
  try {
    // 调用登录API
    const response = await axios.post('http://localhost:3001/api/user/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 模拟登录成功
    if (loginForm.username === '123456' && loginForm.password === 'hubangmin') {
      // 保存用户信息到Vuex
      store.commit('setUser', {
        id: 1,
        name: '胡邦民', // 使用模拟用户名
        avatar: '',
        username: loginForm.username
      })
      store.commit('setLoggedIn', true)
      
      message.success('登录成功')
      
      // 跳转回之前的页面或首页
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } else {
      message.error('账号或密码错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    
    // 模拟登录判断
    if (loginForm.username === '123456' && loginForm.password === 'hubangmin') {
      // 保存用户信息到Vuex
      store.commit('setUser', {
        id: 1,
        name: '胡邦民', // 使用模拟用户名
        avatar: '',
        username: loginForm.username
      })
      store.commit('setLoggedIn', true)
      
      message.success('登录成功')
      
      // 跳转回之前的页面或首页
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } else {
      message.error('账号或密码错误')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 处理用户注册
 * 验证表单并提交注册请求
 */
const handleRegister = async () => {
  // 表单验证
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword || !registerForm.nickname) {
    message.error('请填写完整的注册信息')
    return
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  
  try {
    // 调用注册API
    const response = await axios.post('http://localhost:3001/api/user/register', {
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname
    })
    
    message.success('注册成功，请登录')
    
    // 清空注册表单
    Object.keys(registerForm).forEach(key => {
      registerForm[key] = ''
    })
    
    // 切换到登录标签
    document.querySelector('.ant-tabs-tab[data-node-key="login"]')?.click()
  } catch (error) {
    console.error('注册失败:', error)
    message.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 登录页容器样式 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
  padding: 24px;
}

/* 登录卡片样式 */
.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 测试账号提示样式 */
.test-account-tip {
  margin-top: 16px;
  padding: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
  color: #555;
}

.test-account-tip p {
  margin: 4px 0;
}

/* 移除表单项中的必填红星号 */
.login-container :deep(.ant-form-item-required)::before {
  display: none !important;
}
</style> 