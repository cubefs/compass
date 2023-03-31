import ElementPlus from 'element-plus'
import { type UserModule } from '~/types'

import 'element-plus/dist/index.css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

export const install: UserModule = ({ app }) => {
  app.use(ElementPlus)
  for (const [key, component] of Object.entries(ElementPlusIconsVue))
    app.component(key, component)
}
