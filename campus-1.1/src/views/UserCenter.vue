<template>
  <div class="user-center">
    <a-tabs v-model:activeKey="activeTab">
      <!-- 其他标签页 -->
      
      <!-- 我的收藏标签页 -->
      <a-tab-pane key="favorites" tab="我的收藏">
        <div class="favorites-container">
          <a-list
            :grid="{ 
              gutter: 24,
              column: 4,
              xs: 1,
              sm: 2,
              md: 3,
              lg: 3,
              xl: 4,
              xxl: 4
            }"
            :data-source="favoriteItems"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <div class="list-item-wrapper">
                  <a-card hoverable :class="['product-card', item.status.toLowerCase()]">
                    <!-- 商品图片区域 -->
                    <template #cover>
                      <div class="card-image-container">
                        <img 
                          :alt="item.name" 
                          :src="item.image" 
                          class="product-image"
                          @error="handleImageError"
                          :onerror="setDefaultImage"
                        />
                        <!-- 状态标签 -->
                        <div class="status-badge" :class="item.status.toLowerCase()">
                          {{ item.statusText }}
                        </div>
                        <div class="price-tag" :class="item.status.toLowerCase()">
                          ¥{{ item.price }}
                        </div>
                      </div>
                    </template>
                    
                    <!-- 标签区域 -->
                    <div class="product-tags">
                      <a-tag color="blue">{{ item.category }}</a-tag>
                      <a-tag color="green">{{ item.campus }}</a-tag>
                      <a-tag :color="getStatusColor(item.status)">
                        {{ item.statusText }}
                      </a-tag>
                      <a-button 
                        type="link" 
                        class="view-detail-btn"
                        @click="viewDetail(item)"
                        :disabled="item.status !== 'AVAILABLE'"
                      >
                        查看详情
                      </a-button>
                    </div>
                    
                    <!-- 商品标题 -->
                    <template #title>
                      <div class="product-title" :class="{ 'unavailable': item.status !== 'AVAILABLE' }">
                        {{ item.name }}
                      </div>
                    </template>
                    
                    <!-- 商品描述 -->
                    <template #description>
                      <div class="product-details">
                        <div class="seller-info">
                          <span class="seller-name">卖家: {{ item.seller.name }}</span>
                        </div>
                        <p class="product-description">{{ item.description }}</p>
                        <!-- 交易方式标签 -->
                        <div class="trade-type-tags">
                          <a-tag v-if="item.canBuy" color="blue">可购买</a-tag>
                          <a-tag v-if="item.canExchange" color="green">可置换</a-tag>
                          <a-tag v-if="!item.canBuy && !item.canExchange" color="red">不可交易</a-tag>
                        </div>
                      </div>
                    </template>
                    
                    <!-- 操作按钮 -->
                    <template #actions>
                      <a-button 
                        type="link" 
                        class="favorite-button favorite-active"
                        @click="removeFavorite(item)"
                        :loading="loading"
                      >
                        <template #icon><heart-filled /></template>
                        取消收藏
                      </a-button>
                      <a-tooltip :title="getActionTooltip(item)">
                        <a-button 
                          type="primary" 
                          @click="viewDetail(item)"
                          :disabled="item.status !== 'AVAILABLE'"
                        >
                          <template #icon><eye-outlined /></template>
                          查看详情
                        </a-button>
                      </a-tooltip>
                    </template>
                  </a-card>
                </div>
              </a-list-item>
            </template>
          </a-list>
          
          <!-- 空状态显示 -->
          <a-empty 
            v-if="favoriteItems.length === 0 && !loading"
            description="暂无收藏的商品"
          >
            <template #extra>
              <a-button type="primary" @click="goToProducts">
                去逛逛
              </a-button>
            </template>
          </a-empty>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { HeartFilled, EyeOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const store = useStore()
const activeTab = ref('profile')
const loading = ref(false)
const favoriteItems = ref([])

// 默认图片URL（使用阿里云的图片）
const defaultImageUrl = 'https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png'

// 处理图片加载错误
const handleImageError = (e) => {
  console.log('图片加载失败，使用默认图片')
  e.target.src = defaultImageUrl
}

// 设置默认图片（用于onerror属性）
const setDefaultImage = "this.onerror=null;this.src='" + defaultImageUrl + "'"

// 获取收藏列表
const fetchFavorites = async () => {
  try {
    loading.value = true
    const userId = store.state.userInfo?.id
    if (!userId) {
      message.error('请先登录')
      router.push('/login')
      return
    }

    console.log('开始获取收藏列表...')
    const response = await axios.get(`http://localhost:8080/api/favorites/${userId}`)
    console.log('收藏列表原始数据:', response.data)

    if (Array.isArray(response.data)) {
      // 获取每个收藏商品的详细信息
      const itemsPromises = response.data.map(async (favorite) => {
        try {
          // 获取商品详情
          const itemResponse = await axios.get(`http://localhost:8080/api/items/${favorite.itemId}`)
          const item = itemResponse.data
          console.log('商品详细信息:', item)

          // 处理图片URL
          let imageUrl = defaultImageUrl // 默认使用阿里云图片
          try {
            if (item.imageUrls && typeof item.imageUrls === 'string') {
              const imageNames = item.imageUrls.split(',').filter(name => name.trim() !== '')
              if (imageNames.length > 0) {
                // 构建完整的图片URL
                const fullImageUrl = `http://localhost:8080/api/items/image/${imageNames[0]}`
                // 验证图片URL是否可访问
                const img = new Image()
                img.src = fullImageUrl
                img.onload = () => {
                  imageUrl = fullImageUrl
                }
                img.onerror = () => {
                  console.log('图片加载失败，使用默认图片:', defaultImageUrl)
                  imageUrl = defaultImageUrl
                }
              }
            }
            console.log('处理后的图片URL:', imageUrl)
          } catch (error) {
            console.error('处理图片URL时出错:', error)
            imageUrl = defaultImageUrl
          }

          // 返回处理后的商品信息
          return {
            id: item.id,
            itemId: favorite.itemId,
            name: item.name || '未知商品',
            price: item.price || 0,
            description: item.description || '暂无描述',
            image: imageUrl,
            category: item.category || '未分类',
            campus: item.campus || '主校区',
            status: item.status || 'UNAVAILABLE',
            canBuy: item.status === 'AVAILABLE' && item.tradeType !== 'EXCHANGE_ONLY',
            canExchange: item.status === 'AVAILABLE' && item.tradeType !== 'SALE_ONLY',
            statusText: getStatusText(item.status),
            seller: {
              id: item.user?.id || 0,
              name: item.user?.username || '未知用户'
            }
          }
        } catch (error) {
          console.error('获取商品详情失败:', error)
          // 即使获取详情失败，也返回一个带默认图片的商品对象
          return {
            id: favorite.itemId,
            itemId: favorite.itemId,
            name: '加载失败的商品',
            price: 0,
            description: '无法获取商品详情',
            image: defaultImageUrl,
            category: '未分类',
            campus: '未知',
            seller: {
              id: 0,
              name: '未知用户'
            }
          }
        }
      })

      // 等待所有商品详情获取完成
      const items = await Promise.all(itemsPromises)
      // 过滤掉null值（虽然现在不会有null了）
      favoriteItems.value = items.filter(item => item !== null)
      console.log('处理后的收藏列表:', favoriteItems.value)
    } else {
      console.error('收藏列表数据格式不正确:', response.data)
      message.error('获取收藏列表失败：数据格式不正确')
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    message.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 查看商品详情
const viewDetail = (item) => {
  router.push(`/product/${item.id}`)
}

// 取消收藏
const removeFavorite = async (item) => {
  try {
    loading.value = true
    const userId = store.state.userInfo?.id
    // 使用itemId而不是id
    await axios.delete(`http://localhost:8080/api/favorites/${userId}/${item.itemId}`)
    message.success('已取消收藏')
    // 重新获取收藏列表
    await fetchFavorites()
  } catch (error) {
    console.error('取消收藏失败:', error)
    message.error('取消收藏失败')
  } finally {
    loading.value = false
  }
}

// 跳转到商品列表
const goToProducts = () => {
  router.push('/')
}

// 在组件挂载时获取收藏列表
onMounted(() => {
  if (store.state.userInfo?.id) {
    fetchFavorites()
  }
})

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'favorites' && store.state.userInfo?.id) {
    fetchFavorites()
  }
})

// 获取状态对应的颜色
const getStatusColor = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success'
    case 'SOLD':
      return 'error'
    case 'RESERVED':
      return 'warning'
    case 'UNAVAILABLE':
      return 'default'
    default:
      return 'default'
  }
}

// 获取操作按钮的提示文本
const getActionTooltip = (item) => {
  if (item.status === 'AVAILABLE') {
    return '查看商品详情'
  }
  return `该商品${item.statusText}，暂不可操作`
}
</script>

<style scoped>
/* 复用ProductList.vue的样式 */
.favorites-container {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 200px);
  width: 100%;
  box-sizing: border-box;
}

.list-item-wrapper {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.product-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background: white;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.card-image-container {
  overflow: hidden;
  height: 240px;
  position: relative;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease, opacity 0.3s ease;
  background: #f5f5f5;
  opacity: 1;
}

.product-image[src=""],
.product-image:not([src]) {
  opacity: 0;
}

.product-image[src="https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png"] {
  object-fit: contain;
  padding: 20px;
}

.price-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
}

.product-tags {
  padding: 12px 16px;
  background-color: white;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.product-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 12px 16px 8px;
  line-height: 1.4;
  height: 50px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.seller-info {
  display: flex;
  align-items: center;
  margin: 0 16px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.seller-name {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
}

.seller-name::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #1890ff;
  border-radius: 50%;
  margin-right: 8px;
}

.product-description {
  color: #666;
  margin: 12px 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  line-height: 1.6;
  height: 44px;
}

.view-detail-btn {
  margin-left: auto;
  padding: 4px 12px;
  height: 28px;
  font-size: 14px;
  color: #1890ff;
  border-radius: 14px;
  background: rgba(24, 144, 255, 0.1);
  transition: all 0.3s ease;
}

.view-detail-btn:hover {
  color: white;
  background: #1890ff;
}

.favorite-button {
  color: #ff4d4f;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.favorite-button:hover {
  color: #ff7875;
}

/* 响应式布局调整 */
@media (max-width: 576px) {
  .favorites-container {
    padding: 12px;
  }
  
  .list-item-wrapper {
    padding: 4px;
  }
}

@media (min-width: 577px) and (max-width: 992px) {
  .list-item-wrapper {
    padding: 6px;
  }
}

/* 状态标签样式 */
.status-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 4px 12px;
  border-radius: 12px;
  color: white;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(4px);
  z-index: 1;
}

.status-badge.available {
  background: linear-gradient(135deg, #52c41a, #389e0d);
}

.status-badge.sold {
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
}

.status-badge.reserved {
  background: linear-gradient(135deg, #faad14, #d48806);
}

.status-badge.unavailable {
  background: linear-gradient(135deg, #bfbfbf, #8c8c8c);
}

/* 商品卡片状态样式 */
.product-card.sold .product-image,
.product-card.unavailable .product-image {
  filter: grayscale(100%);
  opacity: 0.7;
}

.product-card.sold .price-tag,
.product-card.unavailable .price-tag {
  background: linear-gradient(135deg, #bfbfbf, #8c8c8c);
}

.product-title.unavailable {
  color: #8c8c8c;
}

/* 交易方式标签样式 */
.trade-type-tags {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

/* 提示文本样式 */
.unavailable-tip {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}
</style> 