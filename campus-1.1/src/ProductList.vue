<template>
  <div class="product-list">
    <div v-for="item in items" :key="item.id" class="product-card">
      <img :src="item.imageUrls ? item.imageUrls.split(',')[0] : 'https://picsum.photos/300/200'" :alt="item.name">
      <div class="product-info">
        <h3>{{ item.name }}</h3>
        <p class="price">¥{{ item.price }}</p>
        <p class="description">{{ item.description }}</p>
        <div class="tags">
          <span class="tag">{{ item.category }}</span>
          <span class="tag">{{ item.campus }}</span>
        </div>
        <div class="seller">
          <span>卖家：{{ item.user.username }}</span>
        </div>
        <div class="actions">
          <button @click="handlePurchase(item)" :disabled="item.status !== 'AVAILABLE'">购买</button>
          <button @click="handleExchange(item)" :disabled="item.status !== 'AVAILABLE'">交换</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { itemApi, transactionApi } from './api';

export default {
  name: 'ProductList',
  setup() {
    const items = ref([]);

    const fetchItems = async () => {
      try {
        // 这里假设我们有一个获取所有物品的 API
        const response = await itemApi.getAllItems();
        items.value = response.data;
      } catch (error) {
        console.error('获取物品列表失败:', error);
      }
    };

    const handlePurchase = async (item) => {
      try {
        // 这里需要传入当前用户 ID
        const userId = 1; // 临时使用固定用户 ID
        await transactionApi.purchaseItem({
          itemId: item.id,
          buyerId: userId
        });
        alert('购买申请已提交');
      } catch (error) {
        console.error('购买失败:', error);
        alert('购买失败，请重试');
      }
    };

    const handleExchange = async (item) => {
      try {
        // 这里需要传入当前用户 ID 和用于交换的物品 ID
        const userId = 1; // 临时使用固定用户 ID
        const exchangeItemId = 2; // 临时使用固定物品 ID
        await transactionApi.exchangeItem({
          itemId: item.id,
          userId: userId,
          exchangeItemId: exchangeItemId
        });
        alert('交换申请已提交');
      } catch (error) {
        console.error('交换失败:', error);
        alert('交换失败，请重试');
      }
    };

    onMounted(() => {
      fetchItems();
    });

    return {
      items,
      handlePurchase,
      handleExchange
    };
  }
};
</script>

<style scoped>
.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.product-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.price {
  color: #e74c3c;
  font-size: 1.2em;
  font-weight: bold;
}

.description {
  color: #666;
  margin: 10px 0;
}

.tags {
  display: flex;
  gap: 10px;
  margin: 10px 0;
}

.tag {
  background-color: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9em;
}

.seller {
  color: #666;
  font-size: 0.9em;
  margin: 10px 0;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  background-color: #3498db;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover {
  background-color: #2980b9;
}

button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}
</style> 