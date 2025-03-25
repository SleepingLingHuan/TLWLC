<template>
  <div class="home-container">
    <a-layout>
      <a-layout-header class="header">
        <div class="header-left">
          <h1 class="title">校园二手交易平台</h1>
        </div>
        <div class="header-right">
          <a-button type="link" @click="handleLogout">退出登录</a-button>
        </div>
      </a-layout-header>
      
      <a-layout-content class="content">
        <a-card title="欢迎使用校园二手交易平台">
          <p>您已成功登录系统！</p>
          <p>这里可以展示商品列表、用户信息等内容。</p>
        </a-card>
      </a-layout-content>
      
      <a-layout-footer class="footer">
        <p>© 2024 校园二手交易平台</p>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup>
import { message } from 'ant-design-vue';

const handleLogout = () => {
  localStorage.removeItem('token');
  message.success('已退出登录');
  window.location.href = '/login';
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.title {
  margin: 0;
  color: #1890ff;
}

.content {
  padding: 24px;
  min-height: 280px;
}

.footer {
  text-align: center;
  background: #f0f2f5;
  padding: 16px;
}
</style> 