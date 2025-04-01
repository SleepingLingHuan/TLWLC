<template>
  <div class="home">
    <ProductList :searchQuery="searchQuery" :searchParams="searchParams" />
  </div>
</template>

<script setup>
import ProductList from '../components/ProductList.vue'

// 接收搜索关键词和搜索参数
const props = defineProps({
  searchQuery: {
    type: String,
    default: ''
  },
  searchParams: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped>
.home {
  width: 100%;
}
</style> 