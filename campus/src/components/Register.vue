<template>
  <div class="register-container">
    <a-card title="用户注册" :style="{ width: '400px' }">
      <a-form
        :model="formState"
        name="register"
        @finish="onFinish"
        autocomplete="off"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名!' }]"
        >
          <a-input v-model:value="formState.username" placeholder="用户名">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码!' }]"
        >
          <a-input-password v-model:value="formState.password" placeholder="密码">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="phone"
          :rules="[
            { required: true, message: '请输入手机号!' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号!' }
          ]"
        >
          <a-input v-model:value="formState.phone" placeholder="手机号">
            <template #prefix>
              <PhoneOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="email"
          :rules="[
            { required: true, message: '请输入邮箱!' },
            { type: 'email', message: '请输入正确的邮箱地址!' }
          ]"
        >
          <a-input v-model:value="formState.email" placeholder="邮箱">
            <template #prefix>
              <MailOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="studentId"
          :rules="[{ required: true, message: '请输入学号!' }]"
        >
          <a-input v-model:value="formState.studentId" placeholder="学号">
            <template #prefix>
              <IdcardOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>
            注册
          </a-button>
        </a-form-item>

        <div class="form-footer">
          <a @click="$router.push('/login')">已有账号？立即登录</a>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { 
  UserOutlined, 
  LockOutlined, 
  PhoneOutlined, 
  MailOutlined,
  IdcardOutlined 
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(false);

const formState = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  studentId: '',
});

const onFinish = async (values) => {
  loading.value = true;
  try {
    console.log('发送注册请求:', values);
    const response = await userApi.register(values);
    console.log('注册响应:', response);
    
    if (response.token) {
      message.success('注册成功，请登录');
      router.push('/login');
    } else {
      message.error(response.message || '注册失败');
    }
  } catch (error) {
    console.error('注册错误:', error);
    if (error.response) {
      message.error(error.response.data?.message || '注册失败');
    } else if (error.request) {
      message.error('无法连接到服务器，请检查网络连接');
    } else {
      message.error('注册失败：' + error.message);
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.form-footer {
  text-align: center;
  margin-top: 16px;
}

.form-footer a {
  color: #1890ff;
  cursor: pointer;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style> 