<template>
  <div class="login-container">
    <a-card title="用户登录" :style="{ width: '400px' }">
      <a-form
        :model="formState"
        name="login"
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

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>
            登录
          </a-button>
        </a-form-item>

        <div class="form-footer">
          <a @click="$router.push('/register')">还没有账号？立即注册</a>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(false);

const formState = reactive({
  username: '',
  password: '',
});

const onFinish = async (values) => {
  loading.value = true;
  try {
    console.log('发送登录请求:', values);
    const response = await userApi.login(values);
    console.log('登录响应:', response);
    
    if (response.token) {
      message.success('登录成功');
      localStorage.setItem('token', response.token);
      router.push('/');
    } else {
      message.error(response.message || '登录失败');
    }
  } catch (error) {
    console.error('登录错误:', error);
    if (error.response) {
      console.error('错误响应数据:', error.response.data);
      message.error(error.response.data?.message || '登录失败');
    } else if (error.request) {
      console.error('请求错误:', error.request);
      message.error('无法连接到服务器，请检查网络连接');
    } else {
      console.error('其他错误:', error.message);
      message.error('登录失败：' + error.message);
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
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