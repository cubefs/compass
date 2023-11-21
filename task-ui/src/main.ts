import { ViteSSG } from 'vite-ssg'

import '@unocss/reset/tailwind.css'
import './styles/main.css'
import 'uno.css'
import { createWebHashHistory } from 'vue-router'
import type { UserModule } from './types'
import App from './App.vue'
import generatedRoutes from '~pages'
import i18n from './locales'

export const createApp = ViteSSG(
  App,
  { routes: generatedRoutes, base: '/compass/portal/', history: createWebHashHistory() },
  (ctx) => {
    ctx.app.config.globalProperties.$i18n = i18n
    ctx.app.use(i18n)
    // install all modules under `modules/`
    Object.values(import.meta.glob<{ install: UserModule }>('./modules/*.ts', { eager: true }))
      .forEach(i => i.install?.(ctx))
  },
)

