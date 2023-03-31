import { defineStore } from 'pinia'

export const useStore = defineStore({
  id: 'globalState',
  state: () => ({
    username: localStorage.getItem('username'),
  }),
  getters: {
  },
  actions: {
    updateUser(username: String) {
      this.username = username
    },
  },
})
