<template>
  <div class="favorite-list">
    <h2 class="page-title">我的收藏</h2>
    
    <!-- 收藏商品列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 4, xxl: 4 }"
      :data-source="favorites"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card
            hoverable
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
                <div class="seller-info">
                  <a-avatar size="small" :style="{ backgroundColor: getColor(item.seller.id) }">
                    {{ item.seller.name.substring(0, 1) }}
                  </a-avatar>
                  <span class="seller-name">{{ item.seller.name }}</span>
                </div>
                <p class="product-description">{{ item.description }}</p>
              </div>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>

    <!-- 空状态展示 -->
    <a-empty v-if="favorites.length === 0 && !loading" description="暂无收藏商品" />
  </div>
</template>

<script setup>
/*
  收藏列表组件
  用于展示用户收藏的商品列表，支持查看商品详情
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 路由实例，用于页面跳转到商品详情
const router = useRouter()
// 收藏商品列表数据，存储用户收藏的所有商品
const favorites = ref([])
// 加载状态标志，true表示正在加载数据，false表示加载完成
const loading = ref(true)

// 获取收藏列表数据的接口函数
// 从后端API获取用户收藏的商品列表，如API调用失败则使用模拟数据
const getFavorites = async () => {
  try {
    // 调用后端API获取收藏列表数据
    const response = await axios.get('http://localhost:3001/api/favorites')
    // 更新收藏列表数据
    favorites.value = response.data
  } catch (error) {
    // 获取数据失败的错误处理
    console.error('获取收藏列表失败:', error)
    
    // 使用模拟数据代替API数据
    // 包含两个示例商品：笔记本电脑和相机
    favorites.value = [
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
  } finally {
    // 无论成功失败，都设置加载状态为false，隐藏加载提示
    loading.value = false
  }
}

// 跳转到商品详情页面的函数
// 参数：id - 商品ID，用于构建详情页URL
const goToDetail = (id) => {
  // 使用router导航到商品详情页，并传递商品ID作为路由参数
  router.push(`/product/${id}`)
}

// 根据卖家ID生成头像颜色的函数
// 参数：id - 卖家ID，用于确定颜色
// 返回：颜色代码字符串，如'#1890ff'
const getColor = (id) => {
  // 预定义颜色数组，包含多种不同颜色
  const colors = ['#1890ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96', '#f5222d']
  // 根据ID取余选择颜色，确保同一用户始终显示相同颜色
  return colors[id % colors.length]
}

// 组件挂载时自动获取收藏列表数据
onMounted(() => {
  // 调用获取收藏列表数据的函数
  getFavorites()
})
</script>

<style scoped>
/* 收藏列表容器样式 */
.favorite-list {
  padding: 24px;
}

/* 页面标题样式 */
.page-title {
  font-size: 24px;
  color: #1890ff;
  margin-bottom: 24px;
}

/* 收藏卡片样式 */
.favorite-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  cursor: pointer;
  transition: all 0.3s;
}

/* 卡片悬停效果 - 仅保留阴影变化 */
.favorite-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  margin-left: 8px;
  font-size: 14px;
  color: #555;
}

/* 商品描述样式 */
.product-description {
  color: #555;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
</style> 