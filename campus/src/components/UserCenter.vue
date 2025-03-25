<template>
  <div class="user-center">
    <a-tabs default-active-key="1">
      <a-tab-pane key="1" tab="个人信息">
        <a-form :model="user" layout="vertical">
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
          
          <a-form-item label="昵称" name="nickname">
            <a-input v-model:value="user.nickname" placeholder="请输入您的昵称" />
          </a-form-item>
          
          <a-form-item label="联系方式" name="contact">
            <a-input v-model:value="user.contact" placeholder="请输入您的联系方式" />
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" @click="save">保存信息</a-button>
          </a-form-item>
        </a-form>
      </a-tab-pane>
      
      <a-tab-pane key="2" tab="交易记录">
        <a-table :columns="cols" :data-source="trades" rowKey="id">
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="record.status === 'completed' ? 'green' : (record.status === 'pending' ? 'orange' : 'red')">
                {{ statMap[record.status] }}
              </a-tag>
            </template>
            <template v-if="column.dataIndex === 'type'">
              <a-tag :color="record.type === 'buy' ? 'blue' : 'purple'">
                {{ record.type === 'buy' ? '购买' : '置换' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
      
      <a-tab-pane key="3" tab="上传物品">
        <a-form :model="good" layout="vertical">
          <a-form-item label="物品名称" name="name" :rules="[{ required: true, message: '请输入物品名称' }]">
            <a-input v-model:value="good.name" placeholder="请输入物品名称" />
          </a-form-item>
          
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
          
          <a-form-item label="物品价格" name="price" :rules="[{ required: true, message: '请输入物品价格' }]">
            <a-input-number v-model:value="good.price" :min="0" placeholder="请输入物品价格" style="width: 100%" />
          </a-form-item>
          
          <a-form-item label="物品描述" name="description">
            <a-textarea v-model:value="good.description" placeholder="请输入物品描述" :rows="4" />
          </a-form-item>
          
          <a-form-item label="交易方式" name="tradeType">
            <a-checkbox-group v-model:value="good.types">
              <a-checkbox value="buy">可购买</a-checkbox>
              <a-checkbox value="exchange">可置换</a-checkbox>
            </a-checkbox-group>
          </a-form-item>
          
          <a-form-item label="物品标签" name="tags">
            <a-select
              v-model:value="good.tags"
              mode="multiple"
              placeholder="请选择标签（最多3个）"
              :max-tag-count="3"
              :max-tag-placeholder="(count) => `+${count}`"
              @change="handleTagChange"
            >
              <a-select-option value="电子产品">电子产品</a-select-option>
              <a-select-option value="学习">学习</a-select-option>
              <a-select-option value="生活">生活</a-select-option>
              <a-select-option value="运动">运动</a-select-option>
              <a-select-option value="数码">数码</a-select-option>
              <a-select-option value="书籍">书籍</a-select-option>
              <a-select-option value="交通工具">交通工具</a-select-option>
              <a-select-option value="服装">服装</a-select-option>
              <a-select-option value="其他">其他</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" @click="upload">发布物品</a-button>
          </a-form-item>
        </a-form>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { UserOutlined, PlusOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { message } from 'ant-design-vue'

// user: 用户个人信息表单数据
const user = reactive({
  avatar: '', // 头像URL
  nickname: '', // 用户昵称
  contact: '' // 联系方式
})

// good: 上传物品表单数据
const good = reactive({
  name: '', // 物品名称
  price: 0, // 物品价格
  description: '', // 物品描述
  types: [], // 交易方式: ['buy', 'exchange']
  tags: [] // 物品标签
})

// 文件上传相关
// files: 用户头像文件列表
const files = ref([])
// goodImg: 物品图片文件列表
const goodImg = ref([])

// 交易记录相关
// statMap: 订单状态映射
const statMap = {
  'pending': '待处理',
  'completed': '已完成',
  'canceled': '已取消'
}

// cols: 交易记录表格列定义
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

// trades: 交易记录数据列表
const trades = ref([])

// mockData: 前端模拟交易记录数据（仅在API调用失败时使用）
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

/**
  图片预览方法
  当用户点击已上传的图片时触发预览
 */
const preview = () => {}

/**
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

/**
  商品图片变更处理
  @param {Object} info - 上传文件的相关信息
 */
const changeGoodImg = (info) => {
  // 当文件上传完成后，更新商品图片
  if (info.file.status === 'done') {
    good.image = info.file.response.url
    message.success('图片上传成功')
  }
}

/**
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

/**
  上传物品
  提交用户填写的商品信息到后端
 */
const upload = async () => {
  // 表单验证：必填字段检查
  if (!good.name || !good.price || !good.types.length) {
    message.error('请填写必要信息')
    return
  }
  
  // 图片验证
  if (!good.image) {
    message.error('请上传商品图片')
    return
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
    
    // 调用API创建商品
    await axios.post('http://localhost:3001/api/products', data)
    message.success('物品发布成功')
    
    // 重置表单：清空所有字段
    Object.keys(good).forEach(key => {
      if (Array.isArray(good[key])) {
        good[key] = []
      } else if (typeof good[key] === 'number') {
        good[key] = 0
      } else {
        good[key] = ''
      }
    })
    goodImg.value = []
  } catch (err) {
    console.error('发布失败:', err)
    message.error('发布失败，请重试')
  }
}

/**
  获取用户信息
  从后端API获取当前登录用户的信息
 */
const getUser = async () => {
  try {
    const res = await axios.get('http://localhost:3001/api/user')
    if (res.data) {
      // 更新用户表单数据
      user.avatar = res.data.avatar
      user.nickname = res.data.nickname
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
    
    // 设置默认值
    user.nickname = '用户'
    user.contact = '请设置联系方式'
  }
}

/**
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

// 处理标签选择变化
const handleTagChange = (selectedTags) => {
  if (selectedTags.length > 3) {
    message.warning('最多只能选择3个标签')
    good.tags = selectedTags.slice(0, 3)
  }
}

// 组件挂载时获取用户信息和交易记录
onMounted(() => {
  getUser()
  getTrades()
})
</script>

<style scoped>
.user-center {
  padding: 16px;
}

.ant-upload-text {
  margin-top: 8px;
}

.tags-hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style> 