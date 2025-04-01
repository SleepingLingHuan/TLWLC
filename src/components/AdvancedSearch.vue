<template>
  <div class="advanced-search">
    <!-- 搜索按钮 -->
    <a-button @click="showDrawer" size="small">
      <template #icon><FilterOutlined /></template>
      搜索
    </a-button>
    
    <!-- 综合搜索 -->
    <a-drawer
      title="搜索"
      placement="right"
      :visible="visible"
      @close="onClose"
      width="300"
    >
      <a-form layout="vertical">
        <!-- 关键词搜索字段 -->
        <a-form-item label="关键词">
          <a-input v-model:value="filters.keyword" placeholder="输入关键词" />
        </a-form-item>
        
        <!-- 价格区间选择 -->
        <a-form-item label="价格区间">
          <a-row :gutter="8">
            <a-col :span="11">
              <a-input-number 
                v-model:value="filters.minPrice" 
                placeholder="最低价" 
                style="width: 100%"
                :min="0"
              />
            </a-col>
            <a-col :span="2" style="text-align: center">-</a-col>
            <a-col :span="11">
              <a-input-number 
                v-model:value="filters.maxPrice" 
                placeholder="最高价" 
                style="width: 100%"
                :min="0"
              />
            </a-col>
          </a-row>
        </a-form-item>
        
        <!-- 商品标签多选框 -->
        <a-form-item label="商品标签">
          <a-select
            v-model:value="filters.tags"
            mode="multiple"
            placeholder="选择标签"
            style="width: 100%"
            :maxTagCount="3"
          >
            <a-select-option value="电子产品">电子产品</a-select-option>
            <a-select-option value="书籍">书籍</a-select-option>
            <a-select-option value="学习">学习</a-select-option>
            <a-select-option value="运动">运动</a-select-option>
            <a-select-option value="生活">生活</a-select-option>
            <a-select-option value="交通工具">交通工具</a-select-option>
            <a-select-option value="服装">服装</a-select-option>
            <a-select-option value="数码">数码</a-select-option>
            <a-select-option value="家具">家具</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- 发布时间选择 -->
        <a-form-item label="发布时间">
          <a-select v-model:value="filters.time" placeholder="选择时间范围">
            <a-select-option value="all">全部时间</a-select-option>
            <a-select-option value="today">今天</a-select-option>
            <a-select-option value="week">本周内</a-select-option>
            <a-select-option value="month">本月内</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- 排序方式选择 -->
        <a-form-item label="排序方式">
          <a-radio-group v-model:value="filters.sort">
            <a-radio-button value="price_asc">价格从低到高</a-radio-button>
            <a-radio-button value="price_desc">价格从高到低</a-radio-button>
            <a-radio-button value="time_desc">最新发布</a-radio-button>
            <a-radio-button value="default">默认排序</a-radio-button>
          </a-radio-group>
          <div class="sort-hint">什么都不选默认按照价格由低到高</div>
        </a-form-item>
        
        <!-- 交易方式多选框 -->
        <a-form-item label="交易方式">
          <a-checkbox-group v-model:value="filters.tradeType">
            <a-checkbox value="buy">可购买</a-checkbox>
            <a-checkbox value="exchange">可置换</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        
        <!-- 操作按钮组 -->
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              搜索
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup>
/*
  高级搜索组件
  提供商品的多条件筛选功能，包括价格、标签、时间、排序等
 */
import { ref, reactive } from 'vue'
import { FilterOutlined } from '@ant-design/icons-vue'

// 定义事件
const emit = defineEmits(['search'])

// 控制抽屉的显示与隐藏状态
const visible = ref(false)

/*
  过滤条件对象
  包含所有可用于筛选商品的条件
 */
const filters = reactive({
  keyword: '',           // 关键词搜索
  minPrice: null,        // 价格下限
  maxPrice: null,        // 价格上限
  tags: [],              // 选中的商品标签数组
  time: 'all',           // 发布时间范围
  sort: 'price_asc',     // 排序方式，默认价格从低到高
  tradeType: []          // 交易方式数组（购买/置换）
})

/*
  显示高级搜索抽屉
 */
const showDrawer = () => {
  visible.value = true
}

/*
  关闭高级搜索抽屉
 */
const onClose = () => {
  visible.value = false
}

/*
  处理搜索按钮点击事件
  将筛选条件转换为查询参数并触发搜索事件
 */
const handleSearch = () => {
  // 构建查询参数对象
  const params = {}
  
  // 只添加有值的过滤条件，避免发送空值
  if (filters.keyword) params.keyword = filters.keyword
  if (filters.minPrice !== null) params.minPrice = filters.minPrice
  if (filters.maxPrice !== null) params.maxPrice = filters.maxPrice
  if (filters.tags.length) params.tags = filters.tags.join(',')
  if (filters.time !== 'all') params.time = filters.time
  
  // 确保有排序方式，如果没有指定则使用默认排序
  params.sort = filters.sort || 'default'
  
  if (filters.tradeType.length) params.tradeType = filters.tradeType.join(',')
  
  // 触发搜索事件，将参数传递给父组件
  emit('search', params)
  
  // 提交后关闭
  onClose()
}

/*
  重置所有筛选条件
  将所有条件恢复为默认值
 */
const handleReset = () => {
  filters.keyword = ''
  filters.minPrice = null
  filters.maxPrice = null
  filters.tags = []
  filters.time = 'all'
  filters.sort = 'price_asc'
  filters.tradeType = []
}
</script>

<style scoped>
/* 高级搜索按钮样式 */
.advanced-search {
  margin-right: 16px;
}

/* 排序提示文字样式 */
.sort-hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style> 