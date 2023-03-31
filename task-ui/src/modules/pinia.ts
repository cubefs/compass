import { createPinia } from 'pinia'
import { type UserModule } from '~/types'

export const install: UserModule = ({ initialState, app }) => {
  const pinia = createPinia()
  app.use(pinia)
  pinia.state.value = (initialState.pinia) || {}
}
