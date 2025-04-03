<template>
  <div class="login-container">
    <!-- 登录/注册卡片 -->
    <a-card class="login-card">
      <!-- 标签页切换：登录/注册 -->
      <a-tabs v-model:activeKey="activeKey">
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
            
            <!-- 邮箱输入框 -->
            <a-form-item
              label="邮箱"
              name="email"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱地址' }
              ]"
            >
              <a-input v-model:value="registerForm.email" placeholder="请输入邮箱" />
            </a-form-item>

            <!-- 手机号输入框 -->
            <a-form-item
              label="手机号"
              name="phone"
              :rules="[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
              ]"
            >
              <a-input v-model:value="registerForm.phone" placeholder="请输入手机号" />
            </a-form-item>

            <!-- 学号输入框 -->
            <a-form-item
              label="学号"
              name="studentId"
              :rules="[
                { required: true, message: '请输入学号' },
                { pattern: /^\d{8,12}$/, message: '请输入8-12位数字的学号' }
              ]"
            >
              <a-input v-model:value="registerForm.studentId" placeholder="请输入学号" />
            </a-form-item>

            <!-- 校区选择 -->
            <a-form-item
              label="校区"
              name="campus"
              :rules="[{ required: true, message: '请选择校区' }]"
            >
              <a-select
                v-model:value="registerForm.campus"
                placeholder="请选择校区"
              >
                <a-select-option value="南望山-西区">南望山-西区</a-select-option>
                <a-select-option value="南望山-东区">南望山-东区</a-select-option>
                <a-select-option value="南望山-北区">南望山-北区</a-select-option>
                <a-select-option value="未来城校区">未来城校区</a-select-option>
              </a-select>
            </a-form-item>

            <!-- 用户协议 -->
            <a-form-item name="agreement">
              <a-checkbox v-model:checked="registerForm.agreement">
                我已阅读并同意
                <a @click.prevent="showTerms">《用户协议》</a>
                和
                <a @click.prevent="showPrivacy">《隐私政策》</a>
              </a-checkbox>
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
const activeKey = ref('login') // 当前激活的标签页

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
 * username: 用户名
 * password: 密码
 * confirmPassword: 确认密码
 * email: 邮箱
 * phone: 手机号
 * studentId: 学号
 * campus: 校区
 * agreement: 用户协议
 */
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  studentId: '',
  campus: '主校区',
  agreement: false
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
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    
    console.log('登录响应数据:', response.data)
    
    if (response.status === 200) {
      // 保存用户信息到Vuex store
      store.commit('setUserInfo', {
        id: response.data.userInfo.id,
        username: response.data.userInfo.username,
        avatar: response.data.userInfo.avatar || '',
      })
      store.commit('setLoggedIn', true)
      
      // 保存token到localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
      }
      
      message.success('登录成功')
      
      // 如果有重定向地址，则跳转到重定向地址
      const redirect = router.currentRoute.value.query.redirect
      router.push(redirect || '/')
    } else {
      message.error(response.data?.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response?.data) {
      message.error(error.response.data.message || '登录失败')
    } else if (error.request) {
      message.error('无法连接到服务器，请检查网络连接')
    } else {
      message.error('登录失败：' + error.message)
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
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword || 
      !registerForm.phone || !registerForm.email || !registerForm.studentId || 
      !registerForm.campus || !registerForm.agreement) {
    message.error('请填写完整的注册信息')
    return
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }

  if (!registerForm.agreement) {
    message.error('请阅读并同意用户协议和隐私政策')
    return
  }
  
  loading.value = true
  
  try {
    // 构造请求数据
    const requestData = {
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      phone: registerForm.phone,
      studentId: registerForm.studentId,
      campus: registerForm.campus
    }

    // 打印请求数据
    console.log('注册请求数据:', requestData)

    // 调用注册API
    const response = await axios.post('http://localhost:8080/api/users/register', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    // 打印响应数据
    console.log('注册响应数据:', response.data)
    
    // 检查响应状态
    if (response.status === 200) {
      message.success('注册成功')
      // 清空表单
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
      registerForm.email = ''
      registerForm.phone = ''
      registerForm.studentId = ''
      registerForm.campus = '主校区'
      registerForm.agreement = false
      
      // 切换到登录标签页
      activeKey.value = 'login'
    } else {
      message.error(response.data?.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    // 打印详细错误信息
    if (error.response?.data) {
      console.error('错误响应数据:', error.response.data)
      message.error(error.response.data.message || '注册失败')
    } else if (error.request) {
      console.error('请求未收到响应')
      message.error('注册失败，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
      message.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB！')
    return false
  }
  return false  // 返回false阻止自动上传
}

const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
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