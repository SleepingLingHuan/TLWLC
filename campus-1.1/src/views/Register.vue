<template>
  <div class="register-container">
    <div class="register-content">
      <div class="register-header">
        <h1>校园二手交易平台</h1>
        <p>欢迎加入我们的校园二手交易社区</p>
      </div>

      <a-form
        :model="formState"
        name="register"
        @finish="onFinish"
        :rules="rules"
        layout="vertical"
        class="register-form"
      >
        <a-form-item label="账号" name="account">
          <a-input 
            v-model:value="formState.account" 
            placeholder="请输入账号"
            size="large"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password 
            v-model:value="formState.password" 
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password 
            v-model:value="formState.confirmPassword" 
            placeholder="请再次输入密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="邮箱" name="email">
          <a-input 
            v-model:value="formState.email" 
            placeholder="请输入邮箱"
            size="large"
          >
            <template #prefix>
              <MailOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="手机号" name="phone">
          <a-input 
            v-model:value="formState.phone" 
            placeholder="请输入手机号"
            size="large"
          >
            <template #prefix>
              <PhoneOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="校区" name="campus">
          <a-select 
            v-model:value="formState.campus" 
            placeholder="请选择校区"
            size="large"
          >
            <a-select-option value="南望山-西区">南望山-西区</a-select-option>
            <a-select-option value="南望山-东区">南望山-东区</a-select-option>
            <a-select-option value="南望山-北区">南望山-北区</a-select-option>
            <a-select-option value="未来城校区">未来城校区</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="学号" name="studentId">
          <a-input
            v-model:value="formState.studentId"
            size="large"
            placeholder="请输入学号"
          >
            <template #prefix>
              <IdcardOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="agreement">
          <a-checkbox v-model:checked="formState.agreement">
            我已阅读并同意
            <a @click.prevent="showTerms">《用户协议》</a>
            和
            <a @click.prevent="showPrivacy">《隐私政策》</a>
          </a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loading" 
            block
            size="large"
          >
            注册
          </a-button>
        </a-form-item>

        <div class="form-footer">
          <div class="login-link">
            已有账号？<a @click="goToLogin">立即登录</a>
          </div>
          <div class="terms">
            注册即表示同意
            <a @click="showTerms">《用户协议》</a>
            和
            <a @click="showPrivacy">《隐私政策》</a>
          </div>
        </div>
      </a-form>
    </div>

    <!-- 用户协议弹窗 -->
    <a-modal
      v-model:visible="termsVisible"
      title="用户协议"
      @ok="termsVisible = false"
      :footer="null"
      width="600px"
    >
      <div class="terms-content">
        <h3>1. 服务说明</h3>
        <p>校园二手交易平台是一个面向在校学生的二手物品交易平台，旨在为同学们提供一个安全、便捷的交易环境。</p>
        
        <h3>2. 用户责任</h3>
        <p>用户在使用本平台服务时，应当遵守相关法律法规，不得发布虚假信息或进行违法交易。</p>
        
        <h3>3. 交易规则</h3>
        <p>平台鼓励诚信交易，用户应当如实描述商品信息，合理定价，遵守交易约定。</p>
        
        <h3>4. 隐私保护</h3>
        <p>平台将严格保护用户的个人信息，未经用户同意，不会向第三方披露。</p>
      </div>
    </a-modal>

    <!-- 隐私政策弹窗 -->
    <a-modal
      v-model:visible="privacyVisible"
      title="隐私政策"
      @ok="privacyVisible = false"
      :footer="null"
      width="600px"
    >
      <div class="privacy-content">
        <h3>1. 信息收集</h3>
        <p>我们收集的信息包括：用户名、密码、邮箱、手机号等基本信息。</p>
        
        <h3>2. 信息使用</h3>
        <p>收集的信息将用于：账号管理、交易通知、安全验证等必要用途。</p>
        
        <h3>3. 信息保护</h3>
        <p>我们采用加密技术保护您的个人信息安全。</p>
        
        <h3>4. 信息共享</h3>
        <p>未经您的同意，我们不会与第三方共享您的个人信息。</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  PhoneOutlined,
  EnvironmentOutlined,
  IdcardOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const loading = ref(false)
const termsVisible = ref(false)
const privacyVisible = ref(false)

const campusOptions = [
  { value: '南望山-西区', label: '南望山-西区' },
  { value: '南望山-东区', label: '南望山-东区' },
  { value: '南望山-北区', label: '南望山-北区' },
  { value: '未来城校区', label: '未来城校区' }
]

const formState = reactive({
  account: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  studentId: '',
  campus: undefined,
  agreement: false
})

const validatePass = async (rule, value) => {
  if (value === '') {
    throw new Error('请输入密码')
  } else if (value.length < 6) {
    throw new Error('密码长度不能小于6位')
  } else {
    return Promise.resolve()
  }
}

const validatePass2 = async (rule, value) => {
  if (value === '') {
    throw new Error('请再次输入密码')
  } else if (value !== formState.password) {
    throw new Error('两次输入的密码不一致')
  } else {
    return Promise.resolve()
  }
}

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '账号只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  campus: [
    { required: true, message: '请选择校区', trigger: 'change' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{8,12}$/, message: '请输入8-12位数字的学号', trigger: 'blur' }
  ],
  agreement: [
    { required: true, message: '请阅读并同意用户协议和隐私政策', trigger: 'change' }
  ]
}

const onFinish = async (values) => {
  try {
    loading.value = true
    const response = await axios.post('http://localhost:8080/api/users/register', {
      username: values.account,
      password: values.password,
      email: values.email,
      phone: values.phone,
      studentId: values.studentId,
      campus: values.campus
    })
    
    if (response.data.success) {
      message.success(response.data.message)
      router.push('/login')
    } else {
      message.error(response.data.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('注册失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

const showTerms = () => {
  termsVisible.value = true
}

const showPrivacy = () => {
  privacyVisible.value = true
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
  padding: 24px;
}

.register-content {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  padding: 32px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  color: #1890ff;
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: bold;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  max-width: 100%;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-label) {
  padding-bottom: 4px;
}

:deep(.ant-form-item-label > label) {
  font-size: 14px;
  color: #333;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 6px;
  height: 40px;
  padding: 0 12px;
}

:deep(.ant-input) {
  font-size: 14px;
}

:deep(.ant-select-selector) {
  border-radius: 6px !important;
  height: 40px !important;
  padding: 0 12px !important;
}

:deep(.ant-select-selection-item) {
  line-height: 38px !important;
  font-size: 14px;
}

:deep(.ant-btn) {
  border-radius: 6px;
  height: 40px;
  font-size: 15px;
  font-weight: 500;
}

:deep(.ant-checkbox-wrapper) {
  font-size: 13px;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.login-link {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #1890ff;
  cursor: pointer;
  font-weight: 500;
}

.terms {
  font-size: 12px;
  color: #999;
}

.terms a {
  color: #1890ff;
  cursor: pointer;
}

.terms-content,
.privacy-content {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.terms-content h3,
.privacy-content h3 {
  color: #1890ff;
  margin: 16px 0 8px;
  font-size: 16px;
}

.terms-content p,
.privacy-content p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 14px;
}
</style> 