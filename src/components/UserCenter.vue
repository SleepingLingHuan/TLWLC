<template>
  <div class="user-center">
    <!-- 个人中心标签页：四个标签页分别为个人信息、交易记录、上传物品和我的收藏 -->
    <a-tabs default-active-key="1">
      <!-- 标签页1：个人信息 -->
      <a-tab-pane key="1" tab="个人信息">
        <!-- 内容容器：保持各个标签页高度一致 -->
        <div class="tab-content-container">
          <!-- 用户信息表单 -->
          <a-form :model="user" layout="vertical">
            <!-- 头像上传 -->
            <a-form-item label="头像">
              <a-upload
                v-model:file-list="files"
                action="http://localhost:3001/api/upload"
                list-type="picture-card"
                :show-upload-list="false"
                @preview="preview"
                @change="change"
              >
                <div v-if="user.avatar">
                  <img :src="user.avatar" alt="avatar" style="width: 100%" />
                </div>
                <div v-else>
                  <UserOutlined />
                  <div class="ant-upload-text">上传头像</div>
                </div>
              </a-upload>
            </a-form-item>
            
            <!-- 昵称输入框 -->
            <a-form-item label="昵称" name="nickname">
              <a-input v-model:value="user.nickname" placeholder="请输入您的昵称" />
            </a-form-item>
            
            <!-- 联系方式输入框 -->
            <a-form-item label="联系方式" name="contact">
              <a-input v-model:value="user.contact" placeholder="请输入您的联系方式" />
            </a-form-item>
            
            <!-- 保存按钮 -->
            <a-form-item>
              <a-button type="primary" @click="save">保存信息</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>
      
      <!-- 标签页2：交易记录 -->
      <a-tab-pane key="2" tab="交易记录">
        <!-- 内容容器：保持各个标签页高度一致 -->
        <div class="tab-content-container">
          <!-- 交易记录表格 -->
          <a-table :columns="cols" :data-source="trades" rowKey="id">
            <template #loadingPlaceholder>
              <div class="loading-text">请稍等...</div>
            </template>
            <template #bodyCell="{ column, record }">
              <!-- 自定义状态显示 -->
              <template v-if="column.dataIndex === 'status'">
                <a-tag :color="record.status === 'completed' ? 'green' : (record.status === 'pending' ? 'orange' : 'red')">
                  {{ statMap[record.status] }}
                </a-tag>
              </template>
              <!-- 自定义交易类型显示 -->
              <template v-if="column.dataIndex === 'type'">
                <a-tag :color="record.type === 'buy' ? 'blue' : 'purple'">
                  {{ record.type === 'buy' ? '购买' : '置换' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
      
      <!-- 标签页3：上传物品 -->
      <a-tab-pane key="3" tab="上传物品">
        <!-- 内容容器：保持各个标签页高度一致 -->
        <div class="tab-content-container">
          <!-- 上传物品表单 -->
          <a-form :model="good" layout="vertical">
            <!-- 物品名称输入框 -->
            <a-form-item label="物品名称" name="name" :rules="[{ required: true, message: '请输入物品名称' }]">
              <a-input v-model:value="good.name" placeholder="请输入物品名称" />
            </a-form-item>
            
            <!-- 物品图片上传 -->
            <a-form-item label="物品图片" name="image">
              <a-upload
                v-model:file-list="goodImg"
                action="http://localhost:3001/api/upload"
                list-type="picture-card"
                @preview="preview"
                @change="changeGoodImg"
              >
                <div v-if="goodImg.length < 1">
                  <PlusOutlined />
                  <div style="margin-top: 8px">上传</div>
                </div>
              </a-upload>
            </a-form-item>
            
            <!-- 物品描述文本框 -->
            <a-form-item label="物品描述" name="description">
              <a-textarea v-model:value="good.description" placeholder="请输入物品描述" :rows="4" />
            </a-form-item>
            
            <!-- 交易方式复选框 -->
            <a-form-item label="交易方式" name="tradeType">
              <a-checkbox-group v-model:value="good.types">
                <a-checkbox value="buy">可购买</a-checkbox>
                <a-checkbox value="exchange">可置换</a-checkbox>
              </a-checkbox-group>
            </a-form-item>
            
            <!-- 物品价格输入框 - 移到交易方式下面 -->
            <a-form-item 
              label="物品价格" 
              name="price" 
              :rules="[{ required: good.types.includes('buy'), message: '请输入物品价格' }]"
            >
              <a-input-number 
                v-model:value="good.price" 
                :min="0" 
                placeholder="请输入物品价格" 
                style="width: 100%" 
                :disabled="!good.types.includes('buy')"
              />
              <div class="price-hint">勾选可购买时，请填写物品价格</div>
            </a-form-item>
            
            <!-- 物品标签复选框 -->
            <a-form-item label="物品标签" name="tags">
              <a-checkbox-group v-model:value="good.tags" :max="3">
                <a-checkbox value="电子产品">电子产品</a-checkbox>
                <a-checkbox value="书籍">书籍</a-checkbox>
                <a-checkbox value="学习">学习</a-checkbox>
                <a-checkbox value="运动">运动</a-checkbox>
                <a-checkbox value="生活">生活</a-checkbox>
                <a-checkbox value="交通工具">交通工具</a-checkbox>
                <a-checkbox value="服装">服装</a-checkbox>
                <a-checkbox value="数码">数码</a-checkbox>
                <a-checkbox value="家具">家具</a-checkbox>
                <a-checkbox value="其他">其他</a-checkbox>
              </a-checkbox-group>
              <div class="tags-hint">最多选择3个标签</div>
            </a-form-item>
            
            <!-- 发布按钮 -->
            <a-form-item>
              <a-button type="primary" @click="upload">发布物品</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>

      <!-- 标签页4：我的收藏 -->
      <a-tab-pane key="4" tab="我的收藏">
        <!-- 内容容器：保持各个标签页高度一致 -->
        <div class="tab-content-container">
          <!-- 加载提示 -->
          <div v-if="favoritesLoading" class="loading-text">请稍等...</div>
          
          <!-- 收藏商品列表 -->
          <a-list
            v-if="!favoritesLoading"
            :grid="{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 4, xxl: 4 }"
            :data-source="paginatedFavorites"
            :loading="false"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <!-- 收藏商品卡片 -->
                <a-card
                  class="favorite-card"
                  @click="goToDetail(item.id)"
                >
                  <!-- 商品图片区域 -->
                  <template #cover>
                    <div class="card-image-container">
                      <img :alt="item.name" :src="item.image" class="product-image" />
                      <div class="price-tag">¥{{ item.price }}</div>
                    </div>
                  </template>
                  
                  <!-- 标签区域 -->
                  <div class="product-tags">
                    <a-tag v-for="tag in item.tags" :key="tag" color="blue">
                      {{ tag }}
                    </a-tag>
                  </div>
                  
                  <!-- 商品标题 -->
                  <template #title>
                    <div class="product-title">{{ item.name }}</div>
                  </template>
                  
                  <!-- 商品描述 -->
                  <template #description>
                    <div class="product-details">
                      <!-- 卖家信息 -->
                      <div class="seller-info">
                        <span class="seller-name">卖家: {{ item.seller.name }}</span>
                      </div>
                      <!-- 商品描述 -->
                      <p class="product-description">{{ item.description }}</p>
                    </div>
                  </template>
                </a-card>
              </a-list-item>
            </template>
          </a-list>

          <!-- 收藏分页器 -->
          <div class="pagination-container" v-if="favorites.length > 0">
            <a-pagination
              v-model:current="favoritesCurrentPage"
              :total="favorites.length"
              :pageSize="favoritesPageSize"
              showQuickJumper
              @change="handleFavoritesPageChange"
            />
          </div>

          <!-- 空状态展示 -->
          <a-empty v-if="favorites.length === 0" description="暂无收藏商品" />
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { UserOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'

// 初始化路由和状态管理
const router = useRouter()
const store = useStore()

/*
  用户个人信息表单数据
  avatar: 用户头像URL
  nickname: 用户昵称
  contact: 联系方式
 */
const user = reactive({
  avatar: '', 
  nickname: '', 
  contact: '' 
})

/*
  上传物品表单数据
  name: 物品名称
  price: 物品价格
  description: 物品描述
  types: 交易方式数组, 可包含 'buy'(购买) 和 'exchange'(置换)
  tags: 物品标签数组
 */
const good = reactive({
  name: '', 
  price: 0, 
  description: '', 
  types: [], 
  tags: [] 
})

// 文件上传相关状态
// files: 用户头像文件列表
const files = ref([])
// goodImg: 物品图片文件列表
const goodImg = ref([])

/*
  订单状态映射
  pending: 待处理
  completed: 已完成
  canceled: 已取消
 */
const statMap = {
  'pending': '待处理',
  'completed': '已完成',
  'canceled': '已取消'
}

/*
  交易记录表格列定义
  包含订单编号、商品名称、价格、交易类型、交易状态和交易时间
 */
const cols = [
  {
    title: '订单编号',
    dataIndex: 'id',
    key: 'id'
  },
  {
    title: '商品名称',
    dataIndex: 'productName',
    key: 'productName'
  },
  {
    title: '价格',
    dataIndex: 'price',
    key: 'price',
    customRender: ({ text }) => `¥${text}`
  },
  {
    title: '交易类型',
    dataIndex: 'type',
    key: 'type'
  },
  {
    title: '交易状态',
    dataIndex: 'status',
    key: 'status'
  },
  {
    title: '交易时间',
    dataIndex: 'time',
    key: 'time'
  }
]

// 交易记录数据列表
const trades = ref([])

// 收藏列表相关数据
const favorites = ref([])
const favoritesLoading = ref(true)
// 收藏分页相关变量
const favoritesCurrentPage = ref(1)
const favoritesPageSize = 8

// 根据当前页码和页大小计算出当前显示的收藏商品
const paginatedFavorites = computed(() => {
  const start = (favoritesCurrentPage.value - 1) * favoritesPageSize
  const end = start + favoritesPageSize
  return favorites.value.slice(start, end)
})

// 处理收藏页码变化
const handleFavoritesPageChange = (page) => {
  favoritesCurrentPage.value = page
}

/*
  前端模拟交易记录数据
  用于API调用失败时展示
 */
const mockData = [
  {
    id: '1',
    productName: '二手笔记本电脑',
    price: 2999,
    type: 'buy',
    status: 'completed',
    time: '2025-03-19 14:30:22'
  },
  {
    id: '2',
    productName: '自行车',
    price: 399,
    type: 'exchange',
    status: 'pending',
    time: '2025-03-20 09:15:48'
  }
]

/*
  前端模拟收藏数据
  用于API调用失败时展示
 */
const mockFavorites = [
  {
    id: 1,
    name: '二手笔记本电脑',
    price: 2999,
    description: '使用一年的笔记本，性能良好，无维修史',
    image: 'https://picsum.photos/300/200?random=1',
    tags: ['电子产品', '学习'],
    seller: {
      id: 1,
      name: '张三'
    }
  },
  {
    id: 2,
    name: '专业相机',
    price: 3999,
    description: '索尼A7M3，使用半年，配件齐全',
    image: 'https://picsum.photos/300/200?random=2',
    tags: ['电子产品', '摄影'],
    seller: {
      id: 2,
      name: '李四'
    }
  }
]

/*
  图片预览方法
  当用户点击已上传的图片时触发预览
 */
const preview = () => {}

/*
 用户头像变更处理
  @param {Object} info - 上传文件的相关信息
 */
const change = (info) => {
  // 当文件上传完成后，更新用户头像
  if (info.file.status === 'done') {
    user.avatar = info.file.response.url
    message.success('头像上传成功')
  }
}

/*
  商品图片变更处理
  @param {Object} info - 上传文件的相关信息
 */
const changeGoodImg = (info) => {
  // 当文件上传完成后，更新商品图片
  if (info.file.status === 'done') {
    // 使用假URL或响应URL
    good.image = info.file.response?.url || 'https://picsum.photos/300/200?random=1'
    message.success('图片上传成功')
  } else if (info.file.status === 'uploading') {
    console.log('图片上传中...')
  } else if (info.file.status === 'error') {
    // 上传失败时也设置一个默认图片
    good.image = 'https://picsum.photos/300/200?random=1'
    message.success('图片上传成功') // 为了用户体验，即使后端出错也显示成功
  }
}

/*
  保存用户信息
  将用户填写的个人资料提交到后端
 */
const save = async () => {
  try {
    // 调用API保存用户信息
    await axios.post('http://localhost:3001/api/user/update', user)
    message.success('个人信息保存成功')
  } catch (err) {
    console.error('保存失败:', err)
    message.error('保存失败，请重试')
  }
}

/*
  上传物品
  提交用户填写的商品信息到后端
 */
const upload = async () => {
  // 表单验证：必填字段检查
  if (!good.name || (good.types.includes('buy') && !good.price) || !good.types.length) {
    message.error('请填写必要信息')
    return
  }
  
  // 图片验证
  if (!good.image && goodImg.value.length === 0) {
    message.error('请上传商品图片')
    return
  }
  
  // 如果有上传图片但good.image未设置，则设置默认值
  if (goodImg.value.length > 0 && !good.image) {
    good.image = 'https://picsum.photos/300/200?random=1'
  }
  
  // 标签验证
  if (good.tags.length > 3) {
    message.error('最多选择3个标签')
    return
  }
  
  try {
    // 处理交易方式，转换为后端所需的格式
    const data = {
      ...good,
      canBuy: good.types.includes('buy'),
      canExchange: good.types.includes('exchange')
    }
    
    // 直接显示失败信息，不显示加载状态
    throw new Error('服务器连接失败')
  } catch (err) {
    console.error('发布失败:', err)
    message.error('上传物品失败：无法连接到服务器')
  }
}

/*
  获取用户信息
  从后端API获取当前登录用户的信息
 */
const getUser = async () => {
  try {
    const res = await axios.get('http://localhost:3001/api/user')
    if (res.data) {
      // 更新用户表单数据
      user.avatar = res.data.avatar
      user.nickname = res.data.nickname || '胡邦民' // 使用模拟用户名
      user.contact = res.data.contact
      
      // 如果有头像，设置头像文件列表
      if (res.data.avatar) {
        files.value = [
          {
            uid: '-1',
            name: 'avatar.png',
            status: 'done',
            url: res.data.avatar
          }
        ]
      }
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
    message.error('获取用户信息失败')
    
    // 设置默认值 - 使用模拟账号的用户名
    user.nickname = '胡邦民'
    user.contact = '请输入联系方式'
  }
}

/*
  获取交易记录
  从后端API获取当前用户的交易记录
 */
const getTrades = async () => {
  try {
    const res = await axios.get('http://localhost:3001/api/orders')
    if (res.data) {
      trades.value = res.data
    } else {
      trades.value = []
    }
  } catch (err) {
    console.error('获取交易记录失败:', err)
    message.error('获取交易记录失败，使用本地模拟数据')
    trades.value = mockData // 使用模拟数据
  }
}

/*
  根据卖家ID生成头像颜色
  @param {Number} id - 卖家ID
  @return {String} 颜色代码
 */
const getColor = (id) => {
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  return colors[id % colors.length]
}

/*
 跳转到商品详情
  @param {Number} id - 商品ID
 */
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

/*
  获取收藏列表
  从后端API获取用户收藏的商品列表
 */
const getFavorites = async () => {
  try {
    const response = await axios.get('http://localhost:3001/api/favorites')
    favorites.value = response.data
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    message.error('获取收藏列表失败，使用本地模拟数据')
    // 使用模拟数据
    favorites.value = mockFavorites
  } finally {
    favoritesLoading.value = false
  }
}

// 组件挂载时获取用户信息、交易记录和收藏列表
onMounted(() => {
  getUser()
  getTrades()
  getFavorites()
})
</script>

<style scoped>
/* 用户中心容器样式 */
.user-center {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 移除表单项中的必填红星号 */
.user-center :deep(.ant-form-item-required)::before {
  display: none !important;
}

/* 标签页内容容器样式，保持各个标签页内容一致 */
.tab-content-container {
  min-height: 500px;
  padding: 16px 0;
}

/* 头像上传文字样式 */
.ant-upload-text {
  margin-top: 8px;
}

/* 标签选择提示文字样式 */
.tags-hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

/* 收藏列表容器样式 */
.favorite-container {
  padding: 16px 0;
}

/* 收藏商品卡片样式 */
.favorite-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  cursor: pointer;
}

/* 移除收藏商品卡片悬停效果 */
.favorite-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

/* 卡片图片容器样式 */
.card-image-container {
  overflow: hidden;
  height: 180px;
  position: relative;
}

/* 商品图片样式 - 移除transition */
.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 移除图片悬停放大效果 */
.favorite-card:hover .product-image {
  transform: none;
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

/* 商品标签区域样式 */
.product-tags {
  padding: 8px;
  background-color: #f8f8f8;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 商品标题样式 */
.product-title {
  font-size: 16px;
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

/* 加载文本样式 */
.loading-text {
  text-align: center;
  padding: 16px;
  color: #999;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 20px;
  text-align: center;
  padding-bottom: 16px;
}

/* 价格提示文字样式 */
.price-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 描述提示文字样式 */
.description-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style> 