<script setup>
import { ref } from 'vue'
import ProductList from './components/ProductList.vue'
import UserCenter from './components/UserCenter.vue'

// tab: 当前激活的标签页（'home'为首页，'userCenter'为个人中心）
const tab = ref('home')
// search: 搜索框中的内容
const search = ref('')

// doSearch: 执行搜索操作的函数
// value: 用户输入的搜索关键词
const doSearch = (value) => {
  search.value = value
}
</script>

<template>
  <router-view></router-view>
</template>

<style>
#app {
  width: 100%;
  height: 100%;
}

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
}

/* title: 平台标题样式 */
.title {
  color: #fff;
  margin: 0;
  font-size: 22px;
}

/* content: 主体内容区域样式 */
.content {
  padding: 20px;
  margin: 16px auto;
  max-width: 1200px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
