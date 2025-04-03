<template>
  <div class="advanced-search">
    <!-- 搜索按钮 -->
    <a-button @click="showDrawer" type="primary" size="small">
      <template #icon><SearchOutlined /></template>
      搜索
    </a-button>
    
    <!-- 搜索抽屉 -->
    <a-drawer
      title="搜索"
      placement="right"
      :visible="visible"
      @close="onClose"
      width="320"
    >
      <a-form layout="vertical">
        <!-- 关键词搜索 -->
        <a-form-item label="关键词">
          <a-input 
            v-model:value="filters.keyword" 
            placeholder="输入商品名称或描述关键词"
            allow-clear
          />
        </a-form-item>
        
        <!-- 价格区间 -->
        <a-form-item label="价格区间">
          <a-space>
            <a-input-number 
              v-model:value="filters.minPrice" 
              placeholder="最低价" 
              :min="0"
              style="width: 120px"
            />
            <span>-</span>
            <a-input-number 
              v-model:value="filters.maxPrice" 
              placeholder="最高价" 
              :min="0"
              style="width: 120px"
            />
          </a-space>
        </a-form-item>
        
        <!-- 商品分类 -->
        <a-form-item label="商品分类">
          <a-select
            v-model:value="filters.category"
            placeholder="选择商品分类"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="电子产品">电子产品</a-select-option>
            <a-select-option value="图书教材">图书教材</a-select-option>
            <a-select-option value="生活用品">生活用品</a-select-option>
            <a-select-option value="服装服饰">服装服饰</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- 校区选择 -->
        <a-form-item label="校区">
          <a-select
            v-model:value="filters.campus"
            placeholder="选择校区"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="南望山-西区">南望山-西区</a-select-option>
            <a-select-option value="南望山-东区">南望山-东区</a-select-option>
            <a-select-option value="南望山-北区">南望山-北区</a-select-option>
            <a-select-option value="未来城校区">未来城校区</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- 发布时间 -->
        <a-form-item label="发布时间">
          <a-select
            v-model:value="filters.time"
            placeholder="选择时间范围"
            style="width: 100%"
          >
            <a-select-option value="all">全部时间</a-select-option>
            <a-select-option value="today">今天</a-select-option>
            <a-select-option value="week">本周内</a-select-option>
            <a-select-option value="month">本月内</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- 排序方式 -->
        <a-form-item label="排序方式">
          <a-radio-group v-model:value="filters.sort" button-style="solid">
            <a-radio-button value="price_asc">价格从低到高</a-radio-button>
            <a-radio-button value="price_desc">价格从高到低</a-radio-button>
            <a-radio-button value="time_desc">最新发布</a-radio-button>
          </a-radio-group>
        </a-form-item>
        
        <!-- 交易方式 -->
        <a-form-item label="交易方式">
          <a-checkbox-group v-model:value="filters.tradeType">
            <a-checkbox value="buy">可购买</a-checkbox>
            <a-checkbox value="exchange">可置换</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        
        <!-- 操作按钮 -->
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

// 定义事件
const emit = defineEmits(['search'])

// 控制抽屉显示状态
const visible = ref(false)

// 搜索条件对象
const filters = reactive({
  keyword: '',           // 关键词
  minPrice: null,        // 最低价格
  maxPrice: null,        // 最高价格
  category: undefined,   // 商品分类
  campus: undefined,     // 校区
  time: 'all',          // 发布时间
  sort: 'price_asc',    // 排序方式
  tradeType: []         // 交易方式
})

// 显示搜索抽屉
const showDrawer = () => {
  visible.value = true
}

// 关闭搜索抽屉
const onClose = () => {
  visible.value = false
}

// 处理搜索
const handleSearch = async () => {
  try {
    // 构建查询参数
    const params = {}
    
    // 只添加有值的过滤条件
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.minPrice !== null) params.minPrice = filters.minPrice
    if (filters.maxPrice !== null) params.maxPrice = filters.maxPrice
    if (filters.category) params.category = filters.category
    if (filters.campus) params.campus = filters.campus
    if (filters.time !== 'all') params.time = filters.time
    if (filters.sort) params.sort = filters.sort
    if (filters.tradeType.length) params.tradeType = filters.tradeType.join(',')
    
    // 调用后端API获取搜索结果
    const response = await axios.get('http://localhost:8080/api/items/search', { params })
    
    // 先触发搜索事件，传递搜索结果
    emit('search', response.data)
    
    // 更新URL参数
    const queryString = Object.entries(params)
      .filter(([_, value]) => value !== undefined && value !== null && value !== '')
      .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
      .join('&')
    window.history.pushState({}, '', queryString ? `?${queryString}` : '/')
    
    // 关闭抽屉
    onClose()
    
    // 根据搜索结果数量显示不同的提示
    if (response.data && response.data.length > 0) {
      message.success(`找到 ${response.data.length} 个符合条件的商品`)
    } else {
      message.info('没有找到符合条件的商品，请尝试调整搜索条件')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    message.error('搜索失败，请重试')
  }
}

// 重置搜索条件
const handleReset = () => {
  // 重置所有字段为初始值
  Object.assign(filters, {
    keyword: '',
    minPrice: null,
    maxPrice: null,
    category: undefined,
    campus: undefined,
    time: 'all',
    sort: 'price_asc',
    tradeType: []
  })
  
  // 显示提示
  message.success('搜索条件已重置')
}
</script>

<style scoped>
.advanced-search {
  margin-right: 16px;
}

:deep(.ant-drawer-body) {
  padding: 24px;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.ant-checkbox-group) {
  display: flex;
  gap: 16px;
}
</style> 