<template>
  <div class="product-detail">
    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else-if="product" class="detail-content">
      <!-- 商品图片区域 -->
      <div class="image-section">
        <img :src="product.image" :alt="product.name" class="product-image" />
        <div class="price-tag">¥{{ product.price }}</div>
      </div>

      <!-- 商品信息区域 -->
      <div class="info-section">
        <h1 class="product-title">{{ product.name }}</h1>
        
        <!-- 商品标签 -->
        <div class="product-tags">
          <a-tag color="blue">{{ product.category }}</a-tag>
          <a-tag color="green">{{ product.campus }}</a-tag>
        </div>

        <!-- 卖家信息 -->
        <div class="seller-info">
          <a-avatar :style="{ backgroundColor: getColor(product?.seller?.id || 0) }">
            {{ product?.seller?.name?.charAt(0) || '?' }}
          </a-avatar>
          <span class="seller-name">卖家：{{ product?.seller?.name || '未知' }}</span>
        </div>

        <!-- 商品描述 -->
        <div class="product-description">
          <h3>商品描述</h3>
          <p>{{ product?.description || '暂无描述' }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <a-button 
            type="primary" 
            @click="handleBuy"
            :disabled="!product?.canBuy"
            :title="!product?.canBuy ? '该商品不可购买' : ''"
          >
            <template #icon><ShoppingCartOutlined /></template>
            购买
          </a-button>
          <a-button 
            @click="handleExchange"
            :disabled="!product?.canExchange"
            :title="!product?.canExchange ? '该商品不可置换' : ''"
          >
            <template #icon><SwapOutlined /></template>
            置换
          </a-button>
          <a-button 
            type="link" 
            @click="toggleFavorite"
            :class="{ 'favorite-active': isFavorite }"
            :loading="favoriteLoading"
          >
            <template #icon><HeartOutlined /></template>
            {{ isFavorite ? '取消收藏' : '收藏' }}
          </a-button>
        </div>
      </div>
    </div>

    <div v-else class="error-container">
      <a-result
        status="error"
        title="商品不存在"
        sub-title="该商品可能已被下架或删除"
      >
        <template #extra>
          <a-button type="primary" @click="goBack">返回列表</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ShoppingCartOutlined, SwapOutlined, HeartOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

const route = useRoute()
const router = useRouter()
const store = useStore()

const loading = ref(true)
const product = ref(null)
const favoriteLoading = ref(false)
const isFavorite = ref(false)

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true
    const response = await axios.get(`http://localhost:8080/api/items/${route.params.id}`)
    if (response.data) {
      product.value = response.data
      // 检查是否已收藏
      if (store.state.isLoggedIn) {
        checkFavoriteStatus()
      }
    } else {
      message.error('商品不存在')
      router.push('/')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    if (error.response?.status === 404) {
      message.error('商品不存在')
      router.push('/')
    } else {
      message.error('获取商品详情失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  const user = store.state.userInfo
  if (!user || !user.id || !product.value?.id) return

  try {
    const response = await axios.get(`http://localhost:8080/api/favorites/${user.id}/${product.value.id}`)
    isFavorite.value = response.data
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorite.value = false
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  const user = store.state.userInfo
  if (!user || !user.id) {
    message.error('请先登录')
    router.push('/login')
    return
  }

  if (!product.value?.id) {
    message.error('商品信息无效')
    return
  }

  try {
    favoriteLoading.value = true
    if (isFavorite.value) {
      await axios.delete(`http://localhost:8080/api/favorites/${user.id}/${product.value.id}`)
      message.success('已取消收藏')
    } else {
      await axios.post(`http://localhost:8080/api/favorites/${user.id}/${product.value.id}`)
      message.success('已添加到收藏')
    }
    isFavorite.value = !isFavorite.value
  } catch (error) {
    console.error('切换收藏状态失败:', error)
    message.error('操作失败，请重试')
  } finally {
    favoriteLoading.value = false
  }
}

// 处理购买
const handleBuy = () => {
  if (!product.value?.canBuy) {
    message.warning('该商品不可购买')
    return
  }
  // TODO: 实现购买逻辑
  message.info('购买功能开发中...')
}

// 处理置换
const handleExchange = () => {
  if (!product.value?.canExchange) {
    message.warning('该商品不可置换')
    return
  }
  // TODO: 实现置换逻辑
  message.info('置换功能开发中...')
}

// 返回列表页
const goBack = () => {
  router.push('/')
}

// 生成头像颜色
const getColor = (id) => {
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  return colors[id % colors.length]
}

onMounted(() => {
  fetchProductDetail()
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
  min-height: 400px;
}

.loading-text {
  margin-top: 16px;
  color: #999;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.image-section {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
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
  padding: 16px;
}

.product-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
}

.product-tags {
  margin-bottom: 16px;
}

.seller-info {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.seller-name {
  margin-left: 12px;
  font-size: 16px;
  color: #666;
}

.product-description {
  margin-bottom: 24px;
}

.product-description h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
}

.product-description p {
  color: #666;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.favorite-active {
  color: #f5222d;
}

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style> 