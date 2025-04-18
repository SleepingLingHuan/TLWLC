/**
 * Vuex Store 配置
 * 用于管理全局状态，特别是用户登录状态和信息
 */
import { createStore } from 'vuex'

/**
 * 创建并配置Vuex Store
 * 包含用户登录状态、用户信息以及相关的操作方法
 */
const store = createStore({
  /**
   * 状态定义
   * 包含应用全局状态数据
   */
  state() {
    return {
      isLoggedIn: false, // 用户是否已登录
      userInfo: null
    }
  },
  
  /**
   * 状态修改方法
   * 所有状态的变更必须通过这些方法进行
   */
  mutations: {
    /**
     * 设置登录状态
     * @param {Object} state - 当前状态
     * @param {boolean} value - 是否已登录
     */
    setLoggedIn(state, value) {
      state.isLoggedIn = value
    },
    
    /**
     * 设置用户信息
     * @param {Object} state - 当前状态
     * @param {Object} userInfo - 用户信息对象
     */
    setUserInfo(state, userInfo) {
      state.userInfo = userInfo
    },
    
    /**
     * 清除用户信息
     * 清除用户信息和登录状态
     * @param {Object} state - 当前状态
     */
    clearUserInfo(state) {
      state.userInfo = null
      state.isLoggedIn = false
    }
  },
  
  /**
   * 异步操作方法
   * 处理需要异步操作的逻辑，如从本地存储中读取登录信息等
   */
  actions: {
    /**
     * 检查登录状态
     * 从localStorage中读取保存的用户信息
     * @param {Object} param0 - 包含commit方法的上下文对象
     */
    checkLoginStatus({ commit }) {
      // 从本地存储获取登录状态
      const userInfo = localStorage.getItem('user')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        commit('setUserInfo', user)
        commit('setLoggedIn', true)
      }
    },
    
    /**
     * 登录操作
     * 保存用户信息到localStorage并更新状态
     * @param {Object} param0 - 包含commit方法的上下文对象
     * @param {Object} user - 用户信息对象
     */
    login({ commit }, user) {
      // 保存到本地存储
      localStorage.setItem('user', JSON.stringify(user))
      commit('setUserInfo', user)
      commit('setLoggedIn', true)
    },
    
    /**
     * 退出登录操作
     * 清除localStorage中的用户信息并更新状态
     * @param {Object} param0 - 包含commit方法的上下文对象
     */
    logout({ commit }) {
      // 清除本地存储
      localStorage.removeItem('user')
      commit('clearUserInfo')
    }
  }
})

export default store 