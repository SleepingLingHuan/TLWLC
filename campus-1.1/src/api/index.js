import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token');
          window.location.href = '/login';
          break;
        case 403:
          // 权限不足
          console.error('权限不足');
          break;
        case 404:
          // 请求的资源不存在
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error('发生错误:', error.response.data);
      }
    }
    return Promise.reject(error);
  }
);

// 用户相关 API
export const userApi = {
  register: (userData) => api.post('/auth/register', userData),
  login: (credentials) => api.post('/auth/login', credentials),
  getUserInfo: () => api.get('/user/info'),
  updateUserInfo: (data) => api.put('/user/info', data),
  getUserItems: (userId) => api.get(`/users/${userId}/items`)
};

// 物品相关 API
export const itemApi = {
  getAllItems: () => api.get('/api/items'),
  uploadItem: (itemData) => api.post('/api/items', itemData),
  updateItem: (itemId, itemData) => api.put(`/api/items/${itemId}`, itemData),
  getItem: (itemId) => api.get(`/api/items/${itemId}`),
  deleteItem: (itemId) => api.delete(`/api/items/${itemId}`),
  getUserItems: () => api.get('/api/items/user'),
  getUserFavorites: () => api.get('/api/items/favorites')
};

// 交易相关 API
export const transactionApi = {
  purchaseItem: (data) => api.post('/transactions/purchase', data),
  exchangeItem: (data) => api.post('/transactions/exchange', data),
  confirmTransaction: (transactionId) => api.put(`/transactions/${transactionId}/confirm`),
  rejectTransaction: (transactionId) => api.put(`/transactions/${transactionId}/reject`)
};

export default api; 