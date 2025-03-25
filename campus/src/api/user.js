import api from './config';

export const userApi = {
    // 用户登录
    login: (data) => {
        return api.post('/api/auth/login', data);
    },
    
    // 用户注册
    register: (data) => {
        return api.post('/api/auth/register', data);
    },
    
    // 获取用户信息
    getUserInfo: () => {
        return api.get('/api/user/info');
    },
    
    // 更新用户信息
    updateUserInfo: (data) => {
        return api.put('/api/user/info', data);
    }
}; 