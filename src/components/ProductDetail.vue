<template>
  <div class="product-detail">
    <div class="detail-container" v-if="!loading">
      <!-- 返回按钮 -->
      <a-button class="back-button" @click="goBack">
        <template #icon><ArrowLeftOutlined /></template>
        返回主页
      </a-button>

      <!-- 商品详情卡片 -->
      <a-card class="detail-card">
        <!-- 商品图片区域 -->
        <div class="image-section">
          <img :src="product.image" :alt="product.name" class="product-image" />
          <div class="price-tag">¥{{ product.price }}</div>
        </div>

        <!-- 商品信息区域 -->
        <div class="info-section">
          <h1 class="product-title">{{ product.name }}</h1>
          
          <!-- 标签区域 -->
          <div class="tags-section">
            <a-tag v-for="tag in product.tags" :key="tag" color="blue">
              {{ tag }}
            </a-tag>
          </div>

          <!-- 卖家信息 -->
          <div class="seller-info">
            <span class="seller-label">卖家: </span>
            <span class="seller-name">{{ product.seller.name }}</span>
          </div>

          <!-- 商品描述 -->
          <div class="description-section">
            <h2>商品描述</h2>
            <p>{{ product.description }}</p>
          </div>

          <!-- 操作按钮区域 -->
          <div class="action-buttons">
            <a-button 
              type="primary" 
              @click="handleBuy"
              :disabled="!product.canBuy || !isLoggedIn"
              :title="!product.canBuy ? '该商品不可购买' : (!isLoggedIn ? '请先登录' : '')"
            >
              <template #icon><ShoppingCartOutlined /></template>
              确认购买
            </a-button>
            <a-button 
              @click="handleExchange"
              :disabled="!product.canExchange || !isLoggedIn"
              :title="!product.canExchange ? '该商品不可置换' : (!isLoggedIn ? '请先登录' : '')"
            >
              <template #icon><SwapOutlined /></template>
              确认置换
            </a-button>
            <a-button 
              @click="handleFavorite"
              :disabled="!isLoggedIn"
              :type="isFavorited ? 'default' : 'dashed'"
              :title="!isLoggedIn ? '请先登录' : ''"
            >
              <template #icon><HeartOutlined :style="{ color: isFavorited ? '#ff4d4f' : '' }" /></template>
              {{ isFavorited ? '取消收藏' : '收藏商品' }}
            </a-button>
          </div>
        </div>
      </a-card>

      <!-- 留言区域 -->
      <a-card class="comments-card" title="留言区">
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <div class="comment-header">
              <span class="comment-author">{{ comment.user.name }}</span>
              <span class="comment-time">{{ comment.time }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <a-empty v-else description="暂无留言" />

        <!-- 添加留言 -->
        <div class="comment-form" v-if="isLoggedIn">
          <a-form layout="vertical">
            <a-form-item>
              <a-textarea 
                v-model:value="newComment" 
                placeholder="添加留言..." 
                :rows="3"
                :disabled="!isLoggedIn"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="addComment" :disabled="!newComment.trim()">
                发送留言
              </a-button>
            </a-form-item>
          </a-form>
        </div>
        <a-alert v-else type="info" message="登录后才能发表留言" banner />
      </a-card>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-else>
      <p>请稍等...</p>
    </div>

    <!-- 确认操作模态框 -->
    <a-modal
      :title="act === 'buy' ? '确认购买' : '确认置换'"
      v-model:visible="show"
      @ok="confirm"
      @cancel="cancel"
      okText="确定"
      cancelText="取消"
    >
      <p>您确定要{{ act === 'buy' ? '购买' : '置换' }}这个商品吗？</p>
      <div class="modal-product-info">
        <p><strong>商品名称:</strong> {{ product.name }}</p>
        <p><strong>价格:</strong> ¥{{ product.price }}</p>
        <p><strong>卖家:</strong> {{ product.seller.name }}</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  ArrowLeftOutlined, 
  ShoppingCartOutlined, 
  SwapOutlined,
  HeartOutlined 
} from '@ant-design/icons-vue'
import axios from 'axios'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()

// 状态变量
const product = ref({
  name: '',
  price: 0,
  description: '',
  image: '',
  tags: [],
  canBuy: false,
  canExchange: false,
  seller: {
    id: 0,
    name: ''
  }
})
const loading = ref(true)
const show = ref(false)
const act = ref(null)
const comments = ref([])
const newComment = ref('')
const isFavorited = ref(false)

// 计算属性
const isLoggedIn = computed(() => {
  return store.state.isLoggedIn
})

// 获取商品详情
const getProductDetail = async () => {
  try {
    loading.value = true
    const productId = route.params.id
    const response = await axios.get(`http://localhost:3001/api/products/${productId}`)
    product.value = response.data
    // 检查是否已收藏
    checkIfFavorited()
    // 获取商品留言
    getComments()
  } catch (error) {
    console.error('获取商品详情失败:', error)
    message.error('获取商品详情失败')
    // 使用模拟数据
    product.value = {
      id: Number(route.params.id) || 1,
      name: '二手笔记本电脑',
      price: 2999,
      description: '使用一年的笔记本，性能良好，无维修史。配置：i5处理器，8GB内存，256GB固态硬盘。',
      image: 'https://picsum.photos/800/600?random=1',
      tags: ['电子产品', '学习', '电脑'],
      canBuy: true,
      canExchange: false,
      seller: {
        id: 1,
        name: '张三'
      }
    }
    // 获取模拟留言
    getMockComments()
  } finally {
    loading.value = false
  }
}

/*
  获取商品留言
  从后端API获取商品的留言列表
 */
const getComments = async () => {
  try {
    const productId = route.params.id
    const response = await axios.get(`http://localhost:3001/api/comments?productId=${productId}`)
    comments.value = response.data
  } catch (error) {
    console.error('获取留言失败:', error)
    // 使用模拟数据
    getMockComments()
  }
}

/*
 获取模拟留言
 生成模拟的商品留言数据
 */
const getMockComments = () => {
  comments.value = [
    {
      id: 1,
      content: '这个价格合适吗？可以便宜一点吗？',
      time: '2025-03-29 14:23',
      user: {
        id: 2,
        name: '李四'
      }
    },
    {
      id: 2,
      content: '请问这个商品还在吗？',
      time: '2025-03-29 09:45',
      user: {
        id: 3,
        name: '王五'
      }
    }
  ]
}

/*
  添加留言
  将用户留言提交到后端并更新本地显示
 */
const addComment = async () => {
  if (!newComment.value.trim()) return
  
  try {
    const comment = {
      productId: product.value.id,
      content: newComment.value
    }
    
    await axios.post('http://localhost:3001/api/comments', comment)
    message.success('留言发送成功')
    
    // 添加到本地显示
    comments.value.push({
      id: Date.now(),
      content: newComment.value,
      time: new Date().toLocaleString(),
      user: {
        id: store.state.user.id || 1,
        name: store.state.user.name || '当前用户'
      }
    })
    
    // 清空输入框
    newComment.value = ''
  } catch (error) {
    console.error('留言失败:', error)
    message.error('留言失败，请重试')
    
    // 模拟添加
    if (isLoggedIn.value) {
      comments.value.push({
        id: Date.now(),
        content: newComment.value,
        time: new Date().toLocaleString(),
        user: {
          id: store.state.user.id || 1,
          name: store.state.user.name || '当前用户'
        }
      })
      newComment.value = ''
    }
  }
}

// 检查是否已收藏
const checkIfFavorited = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await axios.get(`http://localhost:3001/api/favorites/check/${product.value.id}`)
    isFavorited.value = response.data.isFavorited
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    // 模拟检查
    isFavorited.value = Math.random() > 0.5
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 处理购买
const handleBuy = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  
  act.value = 'buy'
  show.value = true
}

// 处理置换
const handleExchange = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  
  act.value = 'exchange'
  show.value = true
}

// 处理收藏
const handleFavorite = async () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    if (isFavorited.value) {
      // 取消收藏
      await axios.delete(`http://localhost:3001/api/favorites/${product.value.id}`)
      message.success('取消收藏成功')
      isFavorited.value = false
    } else {
      // 添加收藏
      await axios.post('http://localhost:3001/api/favorites', {
        productId: product.value.id
      })
      message.success('收藏成功')
      isFavorited.value = true
    }
  } catch (error) {
    console.error(isFavorited.value ? '取消收藏失败:' : '收藏失败:', error)
    message.error(isFavorited.value ? '取消收藏失败，请重试' : '收藏失败，请重试')
    
    // 模拟操作
    isFavorited.value = !isFavorited.value
  }
}

// 确认操作
const confirm = async () => {
  try {
    await axios.post('http://localhost:3001/api/orders', {
      productId: product.value.id,
      type: act.value
    })
    message.success(act.value === 'buy' ? '购买请求已发送' : '置换请求已发送')
    show.value = false
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败，请重试')
  }
}

// 取消操作
const cancel = () => {
  show.value = false
  act.value = null
}

// 根据卖家ID生成头像颜色
const getColor = (id) => {
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  return colors[id % colors.length]
}

onMounted(() => {
  getProductDetail()
})
</script>

<style scoped>
.product-detail {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.back-button {
  margin-bottom: 16px;
}

.detail-card {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.image-section {
  flex: 1;
  position: relative;
}

.product-image {
  width: 100%;
  height: auto;
  border-radius: 8px;
  max-height: 500px;
  object-fit: contain;
}

.price-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  background-color: rgba(245, 34, 45, 0.85);
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 24px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.info-section {
  flex: 1;
}

.product-title {
  font-size: 24px;
  color: #1890ff;
  margin-bottom: 16px;
}

.tags-section {
  margin-bottom: 16px;
}

.seller-info {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.seller-label {
  margin-right: 8px;
  font-size: 16px;
  color: #555;
}

.seller-name {
  margin-left: 8px;
  font-size: 16px;
  color: #555;
}

.description-section {
  margin-bottom: 24px;
}

.description-section h2 {
  font-size: 18px;
  margin-bottom: 8px;
}

.description-section p {
  color: #666;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.modal-product-info {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-top: 16px;
}

.comments-card {
  margin-top: 24px;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: bold;
  color: #1890ff;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #555;
  line-height: 1.5;
}

.comment-form {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .detail-card {
    flex-direction: column;
  }
  
  .image-section {
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
}
</style> 