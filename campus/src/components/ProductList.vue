<template>
  <div class="product-list">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left">
        <h1 class="title">校园二手交易平台</h1>
      </div>
      <div class="header-right">
        <template v-if="!isLoggedIn">
          <a-button type="primary" @click="$router.push('/login')">登录</a-button>
        </template>
        <template v-else>
          <a-space>
            <a-button type="link" @click="$router.push('/user')">
              个人中心
            </a-button>
            <a-button type="link" @click="handleLogout">
              退出登录
            </a-button>
          </a-space>
        </template>
      </div>
    </div>

    <!-- 商品列表：使用List组件来进行展示商品 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 4, xxl: 4 }"
      :data-source="items"
      :loading="load"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card
            hoverable
            class="product-card"
          >
            <!-- 商品图片区域 -->
            <template #cover>
              <div class="card-image-container">
                <img :alt="item.name" :src="item.image" class="product-image" />
                <!-- 价格标签：右上角显示商品价格  可以进行调整 感觉不太美观-->
                <div class="price-tag">¥{{ item.price }}</div>
              </div>
            </template>
            
            <!-- 标签区域：展示商品分类标签  放到一个小格子里面来展示 但是后面标签太多可能会导致标签跑到外面来了   可以
             限制标签的个数 -->
            <div class="product-tags">
              <a-tag v-for="tag in item.tags" :key="tag" color="blue">
                {{ tag }}
              </a-tag>
            </div>
            
            <!-- 商品标题区域 -->
            <template #title>
              <div class="product-title">{{ item.name }}</div>
            </template>
            
            <!-- 商品描述区域 -->
            <template #description>
              <div class="product-details">
                <!-- 卖家信息：头像和名称 -->
                <div class="seller-info">
                  <a-avatar size="small" :style="{ backgroundColor: getColor(item.seller.id) }">
                    {{ item.seller.name.substring(0, 1) }}
                  </a-avatar>
                  <span class="seller-name">{{ item.seller.name }}</span>
                </div>
                <!-- 商品描述文本 -->
                <p class="product-description">{{ item.description }}</p>
              </div>
            </template>
            
            <!-- 操作按钮区域：购买和置换 -->
            <template #actions>
              <!-- 购买按钮 -->
              <a-button 
                type="primary" 
                @click="() => onAction(item, 'buy')"
                :disabled="!item.canBuy"
                :title="!item.canBuy ? '该商品不可购买' : ''"
              >
                <template #icon><ShoppingCartOutlined /></template>
                购买
              </a-button>
              <!-- 置换按钮 -->
              <a-button 
                @click="() => onAction(item, 'exchange')"
                :disabled="!item.canExchange"
                :title="!item.canExchange ? '该商品不可置换' : ''"
              >
                <template #icon><SwapOutlined /></template>
                置换
              </a-button>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>

    <!-- 当没有商品数据时显示 -->
    <a-empty v-if="items.length === 0 && !load" description="没有找到相关商品" />

    <!-- 确认操作 -->
    <a-modal
      :title="act === 'buy' ? '确认购买' : '确认置换'"
      v-model:visible="show"
      @ok="confirm"
      @cancel="cancel"
      okText="确定"
      cancelText="取消"
    >
      <p>您确定要{{ act === 'buy' ? '购买' : '置换' }}这个商品吗？</p>
      <div v-if="item" class="modal-product-info">
        <p><strong>商品名称:</strong> {{ item.name }}</p>
        <p><strong>价格:</strong> ¥{{ item.price }}</p>
        <p><strong>卖家:</strong> {{ item.seller.name }}</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ShoppingCartOutlined, SwapOutlined, DownOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/user'

const router = useRouter()
const isLoggedIn = ref(false)
const userInfo = ref({})

// 定义组件接收的属性
const props = defineProps({
  searchQuery: {
    type: String,
    default: ''
  }
})

// 状态变量
// items: 商品列表数据
const items = ref([])
// load: 是否正在加载数据
const load = ref(true)
// item: 当前选中的商品（用于确认操作）
const item = ref(null)
// show: 是否显示确认模态框
const show = ref(false)
// act: 当前操作类型（buy 购买 / exchange 置换）
const act = ref(null)

// 前端模拟数据（仅在API调用失败时使用）
// mock: 模拟商品的数据数组
const mock = [
  {
    id: 1,
    name: '二手笔记本电脑',
    price: 2999,
    description: '使用一年的笔记本，性能良好，无维修史',
    image: 'https://picsum.photos/300/200?random=1',
    tags: ['电子产品', '学习'],
    canBuy: true,
    canExchange: false,
    seller: {
      id: 1,
      name: '张三'
    }
  },
  {
    id: 2,
    name: '专业课教材',
    price: 30,
    description: '计算机网络教材，九成新',
    image: 'https://picsum.photos/300/200?random=2',
    tags: ['书籍', '学习'],
    canBuy: true,
    canExchange: true,
    seller: {
      id: 2,
      name: '李四'
    }
  },
  {
    id: 3,
    name: '自行车',
    price: 399,
    description: '二手自行车，性能良好',
    image: 'https://picsum.photos/300/200?random=3',
    tags: ['交通工具', '生活'],
    canBuy: true,
    canExchange: false,
    seller: {
      id: 3,
      name: '王五'
    }
  },
  {
    id: 4,
    name: '篮球',
    price: 50,
    description: '耐克篮球，使用三个月',
    image: 'https://picsum.photos/300/200?random=4',
    tags: ['运动', '生活'],
    canBuy: false,
    canExchange: true,
    seller: {
      id: 4,
      name: '赵六'
    }
  },
  {
    id: 5,
    name: '小米手机',
    price: 1299,
    description: '小米10，使用8个月，外观完好',
    image: 'https://picsum.photos/300/200?random=5',
    tags: ['电子产品', '数码'],
    canBuy: true,
    canExchange: true,
    seller: {
      id: 5,
      name: '周七'
    }
  },
  {
    id: 6,
    name: '耳机',
    price: 129,
    description: '蓝牙耳机，音质好，续航长',
    image: 'https://picsum.photos/300/200?random=6',
    tags: ['电子产品', '数码'],
    canBuy: false,
    canExchange: true,
    seller: {
      id: 6,
      name: '吴八'
    }
  }
]

/**
  获取商品数据
  从后端API获取商品列表，失败时使用模拟数据
 */
const getData = async () => {
  try {
    load.value = true // 设置加载状态为true
    // 构建API请求URL，如有搜索关键词则添加到查询参数中
    const url = 'http://localhost:3001/api/products' + (props.searchQuery ? `?keyword=${encodeURIComponent(props.searchQuery)}` : '')
    const res = await axios.get(url) // 发送GET请求获取数据
    if (res.data) {
      items.value = res.data // 将获取的数据赋值给商品列表
    }
  } catch (err) {
    // 请求失败处理
    console.error('获取商品列表失败:', err)
    message.error('获取商品列表失败，使用本地模拟数据')
    
    // 如果有搜索关键词，则过滤模拟数据
    if (props.searchQuery) {
      const key = props.searchQuery.toLowerCase()
      // 在名称、描述和标签中搜索关键词
      items.value = mock.filter(product => 
        product.name.toLowerCase().includes(key) || 
        product.description.toLowerCase().includes(key) ||
        product.tags.some(tag => tag.toLowerCase().includes(key))
      )
    } else {
      items.value = mock // 使用全部模拟数据
    }
  } finally {
    load.value = false // 无论成功失败，都设置加载状态为false
  }
}

/**
  处理购买或置换操作
  @param {Object} product - 要操作的商品对象
  @param {String} type - 操作类型：'buy'购买或'exchange'置换
 */
const onAction = (product, type) => {
  // 如果商品不支持该交易方式，直接返回
  if ((type === 'buy' && !product.canBuy) || (type === 'exchange' && !product.canExchange)) {
    return
  }
  
  // 设置当前选中的商品和操作类型，并显示确认模态框
  item.value = product
  act.value = type
  show.value = true
}

/**
  确认操作提交
  向后端API提交购买或置换请求
 */
const confirm = async () => {
  if (!item.value) return // 如果没有选中商品，直接返回

  try {
    // 调用后端API创建订单
    await axios.post('http://localhost:3001/api/orders', {
      productId: item.value.id,
      type: act.value
    })
    // 操作成功提示
    message.success(act.value === 'buy' ? '购买请求已发送' : '置换请求已发送')
    cancel() // 关闭模态框并清除状态
  } catch (err) {
    // 操作失败处理
    console.error('操作失败:', err)
    message.error('操作失败，请重试')
  }
}

/**
  取消操作
  关闭模态框并清除相关状态
 */
const cancel = () => {
  show.value = false // 隐藏模态框
  item.value = null // 清除选中商品
  act.value = null // 清除操作类型
}

/**
  根据卖家ID生成头像颜色
  @param {Number} id - 卖家ID
  @return {String} 颜色代码
 */
const getColor = (id) => {
  // 预定义颜色数组
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  // 根据ID取余选择颜色
  return colors[id % colors.length]
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  if (token) {
    getUserInfo()
  }
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    userInfo.value = response
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  isLoggedIn.value = false
  userInfo.value = {}
  message.success('退出成功')
}

// 监听搜索关键词变化，重新获取数据
watch(() => props.searchQuery, () => {
  getData()
})

// 组件挂载时获取数据
onMounted(() => {
  getData()
  checkLoginStatus()
})
</script>

<style scoped>
/* 商品列表容器样式 */
.product-list {
  padding: 24px;
}

/* 商品卡片样式 */
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

/* 图片容器样式 */
.card-image-container {
  overflow: hidden;
  height: 220px;
  position: relative;
}

/* 图片样式 */
.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

/* 图片悬停效果 */
.product-card:hover .product-image {
  transform: scale(1.08);
}

/* 价格标签样式 */
.price-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: rgba(245, 34, 45, 0.85);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 18px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 标签区域样式 */
.product-tags {
  padding: 8px 16px;
  background-color: #f8f8f8;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 商品标题样式 */
.product-title {
  font-size: 18px;
  color: #1890ff;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 卖家信息样式 */
.seller-info {
  display: flex;
  align-items: center;
  margin: 8px 0;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f0f0;
}

/* 卖家名称样式 */
.seller-name {
  margin-left: 8px;
  font-size: 14px;
  color: #555;
}

/* 商品描述样式 */
.product-description {
  color: #555;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 显示2行，超出部分省略 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

/* 模态框商品信息样式 */
.modal-product-info {
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

/* 添加新的样式 */
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

.header-left {
  flex: 1;
}

.header-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.header-right :deep(.ant-btn-link) {
  color: #fff;
}

.header-right :deep(.ant-btn-link:hover) {
  color: #1890ff;
}

.title {
  color: #fff;
  margin: 0;
  font-size: 22px;
}
</style> 