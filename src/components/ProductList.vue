<template>
  <div class="product-list">
    <!-- 加载提示 -->
    <div v-if="load" class="loading-text">请稍等...</div>
    
    <!-- 商品列表：使用List组件来进行展示商品 -->
    <a-list
      v-if="!load"
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 4, xxl: 4 }"
      :data-source="paginatedItems"
      :loading="false"
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
                  <span class="seller-name">卖家: {{ item.seller.name }}</span>
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

    <!-- 分页器 -->
    <div class="pagination-container" v-if="items.length > 0">
      <a-pagination
        v-model:current="currentPage"
        :total="items.length"
        :pageSize="pageSize"
        showQuickJumper
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ShoppingCartOutlined, SwapOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { message } from 'ant-design-vue'

// 路由实例，用于页面跳转操作
const router = useRouter()

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

// 获取商品数据的接口函数
// 从后端API获取商品列表，支持关键词搜索和高级筛选功能
// 若API调用失败则使用模拟数据
const getData = async () => {
  try {
    // 设置加载状态为true，显示加载提示
    load.value = true
    
    // 这里原本是调用后端API的代码
    // 由于没有实际后端，这里使用本地模拟数据
    
    // 延迟500毫秒模拟网络请求延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 复制模拟数据以避免修改原始数据
    let filteredItems = [...mock]
    
    // 根据搜索参数进行高级过滤
    if (props.searchParams) {
      // 根据关键词过滤商品
      // 搜索商品名称、描述和标签中是否包含关键词
      if (props.searchParams.keyword) {
        const key = props.searchParams.keyword.toLowerCase()
        filteredItems = filteredItems.filter(product => 
          product.name.toLowerCase().includes(key) || 
          product.description.toLowerCase().includes(key) ||
          product.tags.some(tag => tag.toLowerCase().includes(key))
        )
      }
      
      // 根据最低价格过滤
      if (props.searchParams.minPrice) {
        filteredItems = filteredItems.filter(item => item.price >= props.searchParams.minPrice)
      }
      
      // 根据最高价格过滤
      if (props.searchParams.maxPrice) {
        filteredItems = filteredItems.filter(item => item.price <= props.searchParams.maxPrice)
      }
      
      // 根据商品标签过滤
      if (props.searchParams.tags) {
        const tagsList = props.searchParams.tags.split(',')
        if (tagsList.length) {
          // 只要商品的标签中包含搜索标签之一即符合条件
          filteredItems = filteredItems.filter(item => 
            item.tags.some(tag => tagsList.includes(tag))
          )
        }
      }
      
      // 根据交易方式过滤（购买/置换）
      if (props.searchParams.tradeType) {
        const tradeTypes = props.searchParams.tradeType.split(',')
        // 只看可购买商品
        if (tradeTypes.includes('buy') && !tradeTypes.includes('exchange')) {
          filteredItems = filteredItems.filter(item => item.canBuy)
        } 
        // 只看可置换商品
        else if (!tradeTypes.includes('buy') && tradeTypes.includes('exchange')) {
          filteredItems = filteredItems.filter(item => item.canExchange)
        } 
        // 看可购买或可置换的商品
        else if (tradeTypes.includes('buy') && tradeTypes.includes('exchange')) {
          filteredItems = filteredItems.filter(item => item.canBuy || item.canExchange)
        }
      }
    }
    
    // 根据排序参数对商品进行排序
    if (props.searchParams && props.searchParams.sort) {
      switch (props.searchParams.sort) {
        // 按价格从低到高排序
        case 'price_asc':
          filteredItems.sort((a, b) => a.price - b.price)
          break
        // 按价格从高到低排序
        case 'price_desc':
          filteredItems.sort((a, b) => b.price - a.price)
          break
        // 按发布时间降序排序（新的在前）
        case 'time_desc':
          // 模拟时间排序，使用ID代替时间（ID大的视为新发布的）
          filteredItems.sort((a, b) => b.id - a.id)
          break
        // 默认按价格从低到高排序
        default:
          filteredItems.sort((a, b) => a.price - b.price)
          break
      }
    } else {
      // 如果没有指定排序方式，默认按价格从低到高排序
      filteredItems.sort((a, b) => a.price - b.price)
    }
    
    // 更新商品列表数据
    items.value = filteredItems
  } catch (err) {
    // 获取数据失败的错误处理
    console.error('获取商品列表失败:', err)
    // 显示错误提示
    message.error('获取商品列表失败，使用本地模拟数据')
    
    // 直接使用默认排序的模拟数据
    const sortedMock = [...mock].sort((a, b) => a.price - b.price)
    items.value = sortedMock
  } finally {
    // 无论成功失败，都设置加载状态为false，隐藏加载提示
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

// 监听搜索参数变化，重新获取数据
watch([() => props.searchParams], () => {
  getData()
})

// 组件挂载时获取数据
onMounted(() => {
  getData()
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

/* 加载提示文本样式 */
.loading-text {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

/* 分页器容器样式 */
.pagination-container {
  margin-top: 20px;
  text-align: center;
  padding-bottom: 24px;
}
</style> 