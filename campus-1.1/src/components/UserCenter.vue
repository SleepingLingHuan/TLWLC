<template>
  <div class="user-center">
    <!-- 个人中心标签页：四个标签页分别为个人信息、交易记录、上传物品和我的收藏 -->
    <a-tabs default-active-key="1">
      <!-- 标签页1：个人信息 -->
      <a-tab-pane key="1" tab="个人信息">
        <div class="tab-content-container">
          <div class="user-info-container">
            <div class="info-header">
              <h2>基本信息</h2>
              <a-button type="primary" @click="startEdit" v-if="!isEditing">
                <template #icon><EditOutlined /></template>
                编辑信息
              </a-button>
            </div>
            
            <a-form
              v-if="isEditing"
              :model="editForm"
              layout="vertical"
              @finish="handleSave"
            >
              <a-form-item
                label="用户名"
                name="username"
                :rules="[{ required: true, message: '请输入用户名' }]"
              >
                <a-input v-model:value="editForm.username" />
              </a-form-item>
              
              <a-form-item
                label="学号"
                name="studentId"
                :rules="[{ required: true, message: '请输入学号' }]"
              >
                <a-input v-model:value="editForm.studentId" />
              </a-form-item>
              
              <a-form-item
                label="校区"
                name="campus"
                :rules="[{ required: true, message: '请选择校区' }]"
              >
                <a-select v-model:value="editForm.campus">
                  <a-select-option value="南望山-西区">南望山-西区</a-select-option>
                  <a-select-option value="南望山-东区">南望山-东区</a-select-option>
                  <a-select-option value="南望山-北区">南望山-北区</a-select-option>
                  <a-select-option value="未来城校区">未来城校区</a-select-option>
                </a-select>
              </a-form-item>
              
              <a-form-item
                label="邮箱"
                name="email"
                :rules="[
                  { required: true, message: '请输入邮箱' },
                  { type: 'email', message: '请输入正确的邮箱格式' }
                ]"
              >
                <a-input v-model:value="editForm.email" />
              </a-form-item>
              
              <a-form-item
                label="手机号"
                name="phone"
                :rules="[
                  { required: true, message: '请输入手机号' },
                  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式' }
                ]"
              >
                <a-input v-model:value="editForm.phone" />
              </a-form-item>
              
              <a-form-item>
                <a-space>
                  <a-button type="primary" html-type="submit" :loading="saving">
                    保存
                  </a-button>
                  <a-button @click="cancelEdit">
                    取消
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
            
            <a-descriptions v-else bordered>
              <a-descriptions-item label="用户名">
                {{ userInfo.username }}
              </a-descriptions-item>
              <a-descriptions-item label="学号">
                {{ userInfo.studentId }}
              </a-descriptions-item>
              <a-descriptions-item label="校区">
                {{ userInfo.campus }}
              </a-descriptions-item>
              <a-descriptions-item label="邮箱">
                {{ userInfo.email }}
              </a-descriptions-item>
              <a-descriptions-item label="手机号">
                {{ userInfo.phone }}
              </a-descriptions-item>
            </a-descriptions>
          </div>
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
            <a-form-item 
              label="商品名称" 
              name="name" 
              :rules="[{ required: true, message: '请输入商品名称', min: 2, max: 50 }]"
            >
              <a-input v-model:value="good.name" placeholder="请输入商品名称" />
            </a-form-item>
            
            <!-- 物品描述文本框 -->
            <a-form-item 
              label="商品描述" 
              name="description"
              :rules="[{ required: true, message: '请输入商品描述', min: 10, max: 500 }]"
            >
              <a-textarea v-model:value="good.description" placeholder="请输入商品描述" :rows="4" />
            </a-form-item>

            <!-- 商品类别 -->
            <a-form-item
              label="商品类别"
              name="category"
              :rules="[{ required: true, message: '请选择商品类别' }]"
            >
              <a-select v-model:value="good.category" placeholder="请选择商品类别">
                <a-select-option value="电子产品">电子产品</a-select-option>
                <a-select-option value="图书教材">图书教材</a-select-option>
                <a-select-option value="生活用品">生活用品</a-select-option>
                <a-select-option value="服装服饰">服装服饰</a-select-option>
                <a-select-option value="其他">其他</a-select-option>
              </a-select>
            </a-form-item>

            <!-- 价格输入框 -->
            <a-form-item 
              label="商品价格" 
              name="price" 
              :rules="[{ required: true, type: 'number', min: 0, max: 100000, message: '请输入合理的价格' }]"
            >
              <a-input-number 
                v-model:value="good.price" 
                :min="0" 
                :max="100000"
                :precision="2"
                style="width: 100%" 
                placeholder="请输入价格" 
              />
            </a-form-item>

            <!-- 校区选择 -->
            <a-form-item
              label="校区"
              name="campus"
              :rules="[{ required: true, message: '请选择校区' }]"
            >
              <a-select v-model:value="good.campus" placeholder="请选择校区">
                <a-select-option value="南望山-西区">南望山-西区</a-select-option>
                <a-select-option value="南望山-东区">南望山-东区</a-select-option>
                <a-select-option value="南望山-北区">南望山-北区</a-select-option>
                <a-select-option value="未来城校区">未来城校区</a-select-option>
              </a-select>
            </a-form-item>
            
            <!-- 物品图片上传 -->
            <a-form-item 
              label="商品图片" 
              name="files"
              :rules="[{ required: true, validator: validateFiles }]"
            >
              <a-upload
                v-model:file-list="goodImg"
                list-type="picture-card"
                :max-count="5"
                :before-upload="beforeUpload"
                @preview="handlePreview"
                @remove="handleRemove"
                @change="handleChange"
                :customRequest="() => false"
                :action="null"
              >
                <div v-if="goodImg.length < 5">
                  <plus-outlined />
                  <div style="margin-top: 8px">上传</div>
                </div>
              </a-upload>
              <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
                <img alt="example" style="width: 100%" :src="previewImage" />
              </a-modal>
            </a-form-item>
            
            <!-- 发布按钮 -->
            <a-form-item>
              <a-button type="primary" @click="handleUpload" :loading="uploading" block>
                发布商品
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-tab-pane>

      <!-- 标签页4：我的收藏 -->
      <a-tab-pane key="4" tab="我的收藏">
        <div class="tab-content-container">
          <!-- 加载提示 -->
          <div v-if="favoritesLoading" class="loading-text">加载收藏中...</div>
          
          <!-- 收藏商品列表 -->
          <template v-if="!favoritesLoading && favorites.length > 0">
            <div class="favorites-grid">
              <a-card 
                v-for="item in paginatedFavorites" 
                :key="item.id"
                hoverable 
                class="favorite-card"
              >
                <!-- 商品图片区域 -->
                <div class="card-image-container">
                  <img :alt="item.name" :src="item.image" class="product-image" />
                  <div class="price-tag">¥{{ item.price }}</div>
                </div>
                
                <!-- 商品标签区域 -->
                <div class="product-tags">
                  <a-tag color="blue">{{ item.category }}</a-tag>
                  <a-tag color="green">{{ item.campus }}</a-tag>
                </div>
                
                <!-- 商品标题和描述 -->
                <div class="product-content">
                  <h3 class="product-title">{{ item.name }}</h3>
                  <p class="product-description">{{ item.description }}</p>
                </div>
                
                <!-- 操作按钮区域 -->
                <div class="product-actions">
                  <a-button 
                    type="primary" 
                    @click="cancelFavorite(item)"
                    :loading="item.loading"
                  >
                    取消收藏
                  </a-button>
                  <a-button 
                    type="link" 
                    @click="viewDetail(item)"
                  >
                    查看详情
                  </a-button>
                </div>
              </a-card>
            </div>

            <!-- 分页器 -->
            <div class="pagination-container">
              <a-pagination
                v-model:current="favoritesCurrentPage"
                :total="favorites.length"
                :pageSize="favoritesPageSize"
                showQuickJumper
                @change="handleFavoritesPageChange"
              />
            </div>
          </template>

          <!-- 空状态提示 -->
          <a-empty 
            v-if="favorites.length === 0 && !favoritesLoading" 
            description="暂无收藏商品"
            class="empty-state"
          />
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { UserOutlined, PlusOutlined, EditOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { message } from 'ant-design-vue'
import { useStore } from 'vuex'
import { itemApi } from '../api'

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
  image: 物品图片URL
 */
const good = reactive({
  name: '', 
  price: 0, 
  description: '', 
  types: [], 
  tags: [], 
  image: '',
  category: undefined,
  campus: undefined,
  files: []
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
const favoritesLoading = ref(false)
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
  if (info.file.status === 'done') {
    good.image = info.file.response.url
    message.success('图片上传成功')
  } else if (info.file.status === 'error') {
    console.error('图片上传失败:', info.file.error)
    message.error(info.file.error?.message || '图片上传失败')
  }
}

/*
  保存用户信息
  将用户填写的个人资料提交到后端
 */
const save = async () => {
  try {
    // 调用API保存用户信息
    await axios.put('http://localhost:8080/api/user/info', {
      nickname: user.nickname,
      phone: user.contact
    })
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
  // 表单验证
  if (!good.name) {
    message.error('请输入物品名称')
    return
  }
  
  if (good.types.length === 0) {
    message.error('请选择至少一种交易方式')
    return
  }
  
  if (good.types.includes('buy') && !good.price) {
    message.error('请填写物品价格')
    return
  }
  
  if (!good.image && goodImg.value.length === 0) {
    message.error('请上传商品图片')
    return
  }
  
  if (good.tags.length === 0) {
    message.error('请选择至少一个标签')
    return
  }
  
  try {
    // 准备请求数据
    const itemData = {
      name: good.name,
      price: good.price,
      description: good.description,
      canBuy: good.types.includes('buy'),
      canExchange: good.types.includes('exchange'),
      tags: good.tags,
      image: good.image
    }
    
    // 调用API上传商品
    const response = await itemApi.uploadItem(itemData)
    
    if (response) {
      // 上传成功提示
      message.success('商品发布成功')
      
      // 重置表单
      good.name = ''
      good.price = 0
      good.description = ''
      good.types = []
      good.tags = []
      good.image = ''
      goodImg.value = []
    } else {
      throw new Error('上传失败')
    }
    
  } catch (err) {
    console.error('发布失败:', err)
    message.error(err.response?.data?.message || '发布失败，请重试')
  }
}

/*
  获取用户信息
  从后端API获取当前登录用户的信息
 */
const getUser = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/info')
    if (res.data) {
      // 更新用户表单数据
      user.avatar = res.data.avatar
      user.nickname = res.data.username
      user.contact = res.data.phone
      
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

// 加载收藏商品列表
const loadFavorites = async () => {
  try {
    const user = store.state.userInfo
    if (!user || !user.id) {
      favorites.value = []
      return
    }

    favoritesLoading.value = true
    const response = await axios.get(`http://localhost:8080/api/favorites/${user.id}`)
    if (response.data) {
      // 获取收藏的商品ID列表
      const favoriteIds = response.data
      
      // 获取商品详细信息
      const itemsResponse = await axios.get('http://localhost:8080/api/items')
      const allItems = itemsResponse.data
      
      // 过滤出收藏的商品
      favorites.value = allItems.filter(item => favoriteIds.includes(item.id))
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    message.error('加载收藏失败，请重试')
    favorites.value = []
  } finally {
    favoritesLoading.value = false
  }
}

// 取消收藏
const cancelFavorite = async (item) => {
  try {
    const user = store.state.userInfo
    if (!user || !user.id) {
      message.error('请先登录')
      return
    }

    item.loading = true
    await axios.delete(`http://localhost:8080/api/favorites/${user.id}/${item.id}`)
    message.success('已取消收藏')
    
    // 从列表中移除
    favorites.value = favorites.value.filter(f => f.id !== item.id)
    
    // 如果当前页没有数据了，跳转到上一页
    if (paginatedFavorites.value.length === 0 && favoritesCurrentPage.value > 1) {
      favoritesCurrentPage.value--
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    message.error('取消收藏失败，请重试')
  } finally {
    item.loading = false
  }
}

// 查看商品详情
const viewDetail = (item) => {
  router.push(`/product/${item.id}`)
}

// 监听用户状态变化
watch(() => store.state.userInfo, (newUser) => {
  if (newUser && newUser.id) {
    loadFavorites()
  } else {
    favorites.value = []
  }
}, { immediate: true })

// 商品图片相关状态
const previewVisible = ref(false)
const previewImage = ref('')
const uploading = ref(false)

// 图片上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB！')
    return false
  }
  return false  // 返回false阻止自动上传
}

// 处理图片预览
const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview
  previewVisible.value = true
}

// 将文件转换为Base64
const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

// 处理图片取消预览
const handleCancel = () => {
  previewVisible.value = false
}

// 处理图片移除
const handleRemove = (file) => {
  const index = goodImg.value.indexOf(file)
  const newFileList = goodImg.value.slice()
  newFileList.splice(index, 1)
  goodImg.value = newFileList
}

// 自定义文件验证函数
const validateFiles = async (rule, value) => {
  if (goodImg.value.length === 0) {
    throw new Error('请上传商品图片')
  }
}

// 处理图片上传状态改变
const handleChange = ({ fileList: newFileList }) => {
  goodImg.value = newFileList.map(file => {
    if (file.status === 'error') {
      file.status = 'done'
    }
    return file
  })
}

// 处理商品上传
const handleUpload = async () => {
  if (!store.state.isLoggedIn) {
    message.error('请先登录')
    return
  }

  if (!store.state.userInfo || !store.state.userInfo.id) {
    message.error('用户信息无效，请重新登录')
    return
  }

  if (goodImg.value.length === 0) {
    message.error('请上传至少一张商品图片')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    const userId = store.state.userInfo.id.toString()
    formData.append('userId', userId)
    formData.append('name', good.name)
    formData.append('description', good.description)
    formData.append('category', good.category)
    formData.append('price', good.price)
    formData.append('campus', good.campus)

    // 添加图片文件
    goodImg.value.forEach((file) => {
      if (file.originFileObj) {
        formData.append('files', file.originFileObj)
      }
    })

    const response = await axios.post('http://localhost:8080/api/items/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.data) {
      message.success('商品发布成功')
      // 重置表单
      good.name = ''
      good.description = ''
      good.category = undefined
      good.price = undefined
      good.campus = undefined
      goodImg.value = []
    } else {
      throw new Error('发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    if (error.response) {
      message.error(error.response.data || '发布失败，请重试')
    } else if (error.request) {
      message.error('无法连接到服务器，请检查网络连接')
    } else {
      message.error('发布失败：' + error.message)
    }
  } finally {
    uploading.value = false
  }
}

// 用户信息状态
const userInfo = ref({
  username: '',
  studentId: '',
  campus: '',
  email: '',
  phone: '',
  createdAt: null
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 首先从store中获取用户基本信息
    const storeUserInfo = store.state.userInfo
    if (!storeUserInfo) {
      message.error('请先登录')
      router.push('/login')
      return
    }

    // 设置用户基本信息
    userInfo.value = {
      username: storeUserInfo.username || '未设置',
      studentId: storeUserInfo.studentId || '未设置',
      campus: storeUserInfo.campus || '未设置',
      email: storeUserInfo.email || '未设置',
      phone: storeUserInfo.phone || '未设置',
      createdAt: storeUserInfo.createdAt || new Date().toISOString()
    }

    // 如果需要获取更多用户信息，可以调用API
    try {
      const response = await axios.get(`http://localhost:8080/api/users/${storeUserInfo.id}`)
      if (response.data) {
        // 更新用户信息
        Object.assign(userInfo.value, response.data)
      }
    } catch (error) {
      console.warn('获取额外用户信息失败，使用基本信息显示')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  }
}

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo()
  getTrades()
  const user = store.state.userInfo
  if (user && user.id) {
    loadFavorites()
  }
})

// 编辑相关状态
const isEditing = ref(false)
const saving = ref(false)
const editForm = reactive({
  username: '',
  studentId: '',
  campus: '',
  email: '',
  phone: ''
})

// 开始编辑
const startEdit = () => {
  Object.assign(editForm, userInfo.value)
  isEditing.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
}

// 保存编辑
const handleSave = async () => {
  try {
    saving.value = true
    const response = await axios.put(`http://localhost:8080/api/users/${store.state.userInfo.id}`, editForm)
    if (response.data) {
      message.success('个人信息更新成功')
      // 更新本地数据
      Object.assign(userInfo.value, editForm)
      // 更新store中的用户信息
      store.commit('updateUserInfo', editForm)
      isEditing.value = false
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    message.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}
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

/* 收藏商品网格布局 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  padding: 16px 0;
}

/* 收藏商品卡片样式优化 */
.favorite-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  transition: all 0.3s;
}

/* 商品内容区域样式 */
.product-content {
  flex: 1;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
}

/* 商品操作按钮区域样式 */
.product-actions {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
}

/* 空状态样式优化 */
.empty-state {
  margin: 48px 0;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

/* 分页容器样式优化 */
.pagination-container {
  margin: 32px 0;
  padding: 16px 0;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

/* 图片容器样式 */
.card-image-container {
  overflow: hidden;
  height: 220px;
  position: relative;
}

/* 商品图片样式 */
.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-card:hover .product-image {
  transform: scale(1.05);
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
  font-size: 16px;
  color: #1890ff;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 商品描述样式 */
.product-description {
  color: #666;
  font-size: 14px;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 加载文本样式 */
.loading-text {
  text-align: center;
  padding: 24px;
  color: #1890ff;
  font-size: 16px;
}

/* 用户信息容器样式 */
.user-info-container {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 描述列表样式优化 */
:deep(.ant-descriptions) {
  background: #fff;
}

:deep(.ant-descriptions-title) {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

:deep(.ant-descriptions-item-label) {
  width: 100px;
  color: #666;
}

:deep(.ant-descriptions-item-content) {
  color: #333;
  font-weight: 500;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.info-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-form-item-label) {
  font-weight: 500;
}
</style> 