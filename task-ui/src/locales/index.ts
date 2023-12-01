import { createI18n } from 'vue-i18n'
import zh_CN from './zh'
import en_US from './en'

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: 'zh_CN',
  messages: {
    zh_CN,
    en_US
  }
})

export default i18n