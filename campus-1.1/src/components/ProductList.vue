<template>
  <div class="product-list">
    <!-- 加载提示 -->
    <div v-if="load" class="loading-text">请稍等...</div>
    
    <!-- 商品列表：使用List组件来进行展示商品 -->
    <a-list
      v-if="!load"
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
      :data-source="displayedItems"
      :loading="false"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <div class="list-item-wrapper">
            <a-card
              hoverable
              class="product-card"
            >
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
                  <!-- 价格标签：右上角显示商品价格  可以进行调整 感觉不太美观-->
                  <div class="price-tag">¥{{ item.price }}</div>
                </div>
              </template>
              
              <!-- 标签区域：展示商品分类标签 -->
              <div class="product-tags">
                <a-tag color="blue" class="tag-item">{{ item.category }}</a-tag>
                <a-tag color="green" class="tag-item">{{ item.campus }}</a-tag>
                <a-button 
                  type="link" 
                  class="view-detail-btn"
                  @click="viewDetail(item)"
                >
                  查看详情
                </a-button>
              </div>
              
              <!-- 商品标题区域 -->
              <div class="product-title">{{ item.name }}</div>
              
              <!-- 商品描述区域 -->
              <template #description>
                <div class="product-details">
                  <!-- 卖家信息：头像和名称 -->
                  <div class="seller-info">
                    <span class="seller-name">卖家: {{ item.seller.name }}</span>
                  </div>
                  <!-- 商品描述文本 -->
                  <p class="product-description">{{ item.description }}</p>
                </div>
              </template>
              
              <!-- 操作按钮区域 -->
              <div class="product-actions">
                <a-button 
                  type="text" 
                  class="action-btn"
                  @click="toggleFavorite(item)"
                >
                  <template #icon>
                    <HeartOutlined v-if="!item.isFavorite" />
                    <HeartFilled v-else style="color: #ff4d4f" />
                  </template>
                  收藏
                </a-button>
                <a-button 
                  type="primary" 
                  class="action-btn"
                  @click="handleBuy(item)"
                >
                  购买
                </a-button>
                <a-button 
                  type="default" 
                  class="action-btn"
                  @click="handleExchange(item)"
                >
                  置换
                </a-button>
              </div>
            </a-card>
          </div>
        </a-list-item>
      </template>
    </a-list>

    <!-- 当没有商品数据时显示 -->
    <a-empty 
      v-if="items.length === 0 && !load" 
      :description="hasSearchParams ? '没有找到符合条件的商品，请尝试调整搜索条件' : '暂无商品数据'" 
    >
      <template #extra>
        <a-button v-if="hasSearchParams" type="primary" @click="clearSearch">
          清除搜索条件
        </a-button>
      </template>
    </a-empty>

    <!-- 确认操作 -->
    <a-modal
      :title="act === 'buy' ? '确认购买' : '确认置换'"
      v-model:open="show"
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

    <!-- 分页器 -->
    <div class="pagination-container" v-if="items.length > 0">
      <div class="pagination-header">
        <a-button 
          type="primary" 
          @click="showFavorites = !showFavorites"
          class="favorites-toggle"
        >
          {{ showFavorites ? '显示全部' : '显示收藏' }}
        </a-button>
        <a-pagination
          v-model:current="currentPage"
          :total="totalItems"
          :pageSize="pageSize"
          showQuickJumper
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ShoppingCartOutlined, SwapOutlined, HeartOutlined, HeartFilled } from '@ant-design/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

// 路由实例，用于页面跳转操作
const router = useRouter()
const route = useRoute()
const store = useStore()

// 定义组件接收的属性
const props = defineProps({
  // searchParams: 接收搜索参数的对象，包含关键词、价格范围、分类标签等筛选条件
  searchParams: {
    type: Object,
    default: () => ({})
  }
})

// 状态变量
// items: 商品列表数据数组，存储所有符合条件的商品
const items = ref([])
// load: 加载状态标志，true表示正在加载数据，false表示加载完成
const load = ref(true)
// item: 当前选中的商品对象，用于确认购买或置换操作
const item = ref(null)
// show: 模态框显示状态，true表示显示确认模态框
const show = ref(false)
// act: 当前操作类型，'buy'表示购买，'exchange'表示置换
const act = ref(null)
// 分页相关变量
// currentPage: 当前页码，默认为第1页
const currentPage = ref(1)
// pageSize: 每页显示的商品数量，固定为8个
const pageSize = 8

// 收藏相关状态
const favorites = ref([])
const showFavorites = ref(false)
const favoriteLoading = ref(false)

// 计算属性：当前页显示的商品列表
// 根据当前页码和页大小计算出当前页面应该显示的商品数据
const paginatedItems = computed(() => {
  // 计算当前页的起始索引
  const start = (currentPage.value - 1) * pageSize
  // 计算当前页的结束索引
  const end = start + pageSize
  // 返回当前页的商品数据切片
  return items.value.slice(start, end)
})

// 处理页码变化事件
// 当用户点击分页器中的页码时触发
const handlePageChange = (page) => {
  // 更新当前页码
  currentPage.value = page
}

// 前端模拟数据（仅在API调用失败时使用）
// mock: 模拟商品的数据数组，包含各种测试商品
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

// 默认图片URL
const defaultImageUrl = 'https://gw.alipayobjects.com/zos/rmsportal/JiqGstEfoWAOHiTxclqi.png'

// 处理图片加载错误
const handleImageError = (e) => {
  e.target.src = defaultImageUrl
}

// 设置默认图片（用于onerror属性）
const setDefaultImage = "this.onerror=null;this.src='" + defaultImageUrl + "'"

// 获取商品列表数据
const fetchItems = async (queryParams = {}) => {
  try {
    load.value = true
    console.log('开始获取商品列表...')
    
    const response = await axios.get('http://localhost:8080/api/items/search', { params: queryParams })
    
    if (Array.isArray(response.data)) {
      items.value = response.data.map(item => {
        // 处理图片URL
        let imageUrl
        try {
          const imageUrls = item.imageUrls ? item.imageUrls.split(',') : []
          imageUrl = imageUrls.length > 0 
            ? `http://localhost:8080/api/items/image/200/${imageUrls[0]}` 
            : defaultImageUrl
        } catch (error) {
          console.error('处理图片URL时出错:', error)
          imageUrl = defaultImageUrl
        }
        
        return {
          id: item.id,
          name: item.name,
          price: item.price,
          description: item.description,
          image: imageUrl,
          category: item.category || '未分类',
          campus: item.campus || '主校区',
          canBuy: item.status === 'AVAILABLE',
          canExchange: item.status === 'AVAILABLE',
          seller: {
            id: item.user?.id || 0,
            name: item.user?.username || '未知用户'
          }
        }
      })
    } else {
      throw new Error('后端返回的数据格式不正确')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    message.error('获取商品列表失败，使用默认数据')
    items.value = mock
  } finally {
    load.value = false
  }
}

// 处理购买或置换按钮点击事件
// 参数：product - 要操作的商品对象，type - 操作类型（'buy'购买或'exchange'置换）
const onAction = (product, type) => {
  // 检查商品是否支持当前操作类型，不支持则直接返回
  if ((type === 'buy' && !product.canBuy) || (type === 'exchange' && !product.canExchange)) {
    return
  }
  
  // 跳转到商品详情页面，查看更多信息或确认操作
  router.push(`/product/${product.id}`)
}

// 确认操作提交函数
// 向后端API提交购买或置换请求
const confirm = async () => {
  // 如果没有选中商品，直接返回
  if (!item.value) return

  try {
    // 调用后端API创建订单，发送商品ID和操作类型
    await axios.post('http://localhost:3001/api/orders', {
      productId: item.value.id,
      type: act.value
    })
    // 操作成功提示：根据操作类型显示不同消息
    message.success(act.value === 'buy' ? '购买请求已发送' : '置换请求已发送')
    // 关闭模态框并清除状态
    cancel()
  } catch (err) {
    // 操作失败处理
    console.error('操作失败:', err)
    // 显示错误提示
    message.error('操作失败，请重试')
  }
}

// 取消操作函数
// 关闭模态框并清除相关状态
const cancel = () => {
  show.value = false // 隐藏模态框
  item.value = null // 清除选中商品
  act.value = null // 清除操作类型
}

// 根据卖家ID生成头像颜色函数
// 参数：id - 卖家ID
// 返回：颜色代码字符串
const getColor = (id) => {
  // 预定义颜色数组
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  // 根据ID取余选择颜色
  return colors[id % colors.length]
}

// 检查商品是否已收藏
const isFavorite = (itemId) => {
  return favorites.value.includes(itemId)
}

// 切换收藏状态
const toggleFavorite = async (item) => {
  try {
    const user = store.state.userInfo
    if (!user || !user.id) {
      message.error('请先登录')
      router.push('/login')
      return
    }
    
    favoriteLoading.value = true
    const isCurrentlyFavorite = favorites.value.includes(item.id)
    
    if (isCurrentlyFavorite) {
      await axios.delete(`http://localhost:8080/api/favorites/${user.id}/${item.id}`)
      favorites.value = favorites.value.filter(id => id !== item.id)
      message.success('已取消收藏')
    } else {
      await axios.post(`http://localhost:8080/api/favorites/${user.id}/${item.id}`)
      favorites.value.push(item.id)
      message.success('已添加到收藏')
    }
  } catch (error) {
    console.error('切换收藏状态失败:', error)
    if (error.response) {
      message.error(error.response.data || '操作失败，请重试')
    } else {
      message.error('网络错误，请检查网络连接')
    }
  } finally {
    favoriteLoading.value = false
  }
}

// 从后端加载收藏状态
const loadFavorites = async () => {
  try {
    const user = store.state.userInfo
    if (!user || !user.id) {
      favorites.value = []
      return
    }
    
    favoriteLoading.value = true
    const response = await axios.get(`http://localhost:8080/api/favorites/${user.id}`)
    if (response.data) {
      favorites.value = response.data
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    if (error.response) {
      message.error(error.response.data || '加载收藏失败，请重试')
    } else {
      message.error('网络错误，请检查网络连接')
    }
    favorites.value = []
  } finally {
    favoriteLoading.value = false
  }
}

// 监听用户状态变化
watch(() => store.state.userInfo, (newUser) => {
  if (newUser && newUser.id) {
    loadFavorites()
  } else {
    favorites.value = []
    showFavorites.value = false
  }
}, { immediate: true })

// 组件挂载时获取数据
onMounted(() => {
  const user = store.state.userInfo
  if (user && user.id) {
    loadFavorites()
  }
  fetchItems()
})

// 计算显示的商品列表
const displayedItems = computed(() => {
  // 首先根据收藏状态过滤
  const filteredItems = showFavorites.value 
    ? items.value.filter(item => favorites.value.includes(item.id))
    : items.value
  
  // 然后进行分页
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredItems.slice(start, end)
})

// 计算总商品数量（用于分页）
const totalItems = computed(() => {
  return showFavorites.value 
    ? items.value.filter(item => favorites.value.includes(item.id)).length
    : items.value.length
})

// 监听收藏状态变化，重置页码
watch(showFavorites, () => {
  currentPage.value = 1
})

// 监听搜索参数变化
watch(() => props.searchParams, (newParams) => {
  console.log('搜索参数变化，重新获取商品列表')
  if (newParams && Object.keys(newParams).length > 0) {
    // 直接使用搜索结果更新items
    items.value = Array.isArray(newParams) ? newParams.map(item => {
      // 处理图片URL
      let imageUrl
      try {
        const imageUrls = item.imageUrls ? item.imageUrls.split(',') : []
        imageUrl = imageUrls.length > 0 
          ? `http://localhost:8080/api/items/image/200/${imageUrls[0]}` 
          : defaultImageUrl
      } catch (error) {
        console.error('处理图片URL时出错:', error)
        imageUrl = defaultImageUrl
      }
      
      return {
        id: item.id,
        name: item.name,
        price: item.price,
        description: item.description,
        image: imageUrl,
        category: item.category || '未分类',
        campus: item.campus || '主校区',
        canBuy: item.status === 'AVAILABLE',
        canExchange: item.status === 'AVAILABLE',
        seller: {
          id: item.user?.id || 0,
          name: item.user?.username || '未知用户'
        }
      }
    }) : []
    // 重置页码
    currentPage.value = 1
  } else {
    // 如果没有搜索参数，重新获取所有商品
    fetchItems()
  }
}, { immediate: true, deep: true })

// 定义事件
const emit = defineEmits(['search'])

// 添加查看详情按钮
const viewDetail = (item) => {
  router.push(`/product/${item.id}`)
}

// 计算是否有搜索参数
const hasSearchParams = computed(() => {
  if (!props.searchParams) return false
  const params = props.searchParams
  return Object.keys(params).length > 0 && 
         Object.values(params).some(value => 
           value !== undefined && 
           value !== '' && 
           (Array.isArray(value) ? value.length > 0 : true)
         )
})

// 清除搜索条件
const clearSearch = () => {
  // 重置所有搜索相关状态
  props.searchParams = {}
  currentPage.value = 1
  // 触发父组件的搜索事件，传递空对象表示清除搜索条件
  emit('search', {})
  // 移除提示信息
  message.success('已清除搜索条件')
  // 重新获取所有商品
  fetchItems()
}
</script>

<style scoped>
/* 商品列表容器样式 */
.product-list {
  padding: 16px;
  background: #f8f9fa;
  min-height: calc(100vh - 64px);
  width: 100%;
  box-sizing: border-box;
  max-width: 1920px;
  margin: 0 auto;
}

/* 列表项包装器样式 */
.list-item-wrapper {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  transition: transform 0.3s ease;
}

/* 商品卡片样式 */
.product-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: white;
  border: none;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

/* 图片容器样式 */
.card-image-container {
  position: relative;
  padding-top: 100%; /* 1:1 比例，使图片呈现为正方形 */
  overflow: hidden;
  background: #f5f5f5;
}

/* 图片样式优化 */
.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

/* 价格标签样式 */
.price-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 77, 79, 0.95);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 商品标签样式 */
.product-tags {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
  font-size: 12px;
  line-height: 20px;
  height: 22px;
  border-radius: 4px;
  padding: 0 8px;
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
  border: none;
}

.tag-item:nth-child(2) {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

/* 商品标题样式 */
.product-title {
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

/* 商品描述样式 */
.product-description {
  color: #666;
  margin: 0 12px 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  line-height: 1.6;
  height: 44px;
}

/* 操作按钮区域样式 */
.product-actions {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.action-btn {
  flex: 1;
  height: 32px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.action-btn[type="primary"] {
  background: #1890ff;
  border-color: #1890ff;
}

.action-btn[type="primary"]:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

/* 查看详情按钮样式 */
.view-detail-btn {
  color: #1890ff;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.view-detail-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #40a9ff;
}

/* 响应式布局调整 */
@media (max-width: 576px) {
  .product-list {
    padding: 8px;
  }
  
  .list-item-wrapper {
    padding: 4px;
  }

  .product-title {
    font-size: 14px;
    padding: 8px 12px;
  }

  .price-tag {
    font-size: 12px;
    padding: 4px 8px;
  }

  .product-actions {
    padding: 8px;
    gap: 4px;
  }

  .action-btn {
    height: 28px;
    font-size: 12px;
  }
}

/* 分页器样式优化 */
.pagination-container {
  margin-top: 24px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.favorites-toggle {
  border-radius: 6px;
  height: 32px;
  font-weight: 500;
  background: #1890ff;
  border-color: #1890ff;
}

.favorites-toggle:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

/* 空状态样式优化 */
:deep(.ant-empty) {
  padding: 40px;
  background: white;
  border-radius: 12px;
  margin: 24px 0;
}

/* 加载状态样式 */
.loading-text {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 16px;
  background: white;
  border-radius: 12px;
  margin: 24px 0;
}

/* 标签响应式调整 */
.product-tags {
  padding: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;  /* 允许标签换行 */
}

.tag-item {
  margin: 2px;
  font-size: 12px;
  line-height: 20px;
  height: 22px;
}

@media (max-width: 576px) {
  .tag-item {
    font-size: 10px;
    line-height: 18px;
    height: 20px;
    margin: 1px;
  }
  
  .product-tags {
    padding: 2px;
    gap: 1px;
  }
}

/* 按钮响应式调整 */
.product-actions {
  padding: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 4px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: nowrap;  /* 防止按钮换行 */
}

.action-btn {
  flex: 1;
  min-width: 0;
  white-space: nowrap;  /* 防止按钮文字换行 */
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分页器响应式调整 */
@media (max-width: 576px) {
  .pagination-container {
    margin-top: 16px;
    padding: 8px;
  }

  .pagination-header {
    flex-direction: column;
    gap: 8px;
  }

  .favorites-toggle {
    width: 100%;
    margin-right: 0;
  }
}
</style> 