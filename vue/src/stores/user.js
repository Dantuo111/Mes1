import { defineStore } from 'pinia'
import request from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user && state.user.role === 1,
    userId: (state) => state.user ? state.user.id : null
  },

  actions: {
    async login(username, password) {
      const res = await request.post('/api/user/login', { username, password })
      if (res.code === 200) {
        this.token = res.data.token
        this.user = res.data.user
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.user))
      }
      return res
    },

    logout() {
      this.token = ''
      this.user = {}
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    async getUserInfo() {
      const res = await request.get('/api/user/info')
      if (res.code === 200) {
        this.user = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
      }
      return res
    }
  }
})
