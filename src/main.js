import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import './style.css'
import router from './router'
import store from './store'

const app = createApp(App)
app.use(Antd)
app.use(router)
app.use(store)

// 检查登录状态
store.dispatch('checkLoginStatus')

app.mount('#app')
