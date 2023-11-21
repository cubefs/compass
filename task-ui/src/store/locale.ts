import { defineStore } from 'pinia'

export const useLocaleStore = defineStore({
  id: 'localeState',
  state: () => ({
    locale: localStorage.getItem('locale') || 'zh_CN',
  }),
  getters: {
  },
  actions: {
    updateLocale(locale: String) {
      this.locale = locale
      localStorage.setItem('locale', locale)
    },
  },
})
