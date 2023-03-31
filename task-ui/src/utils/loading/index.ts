import type { MessageHandler } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import './loading.scss'
export function createLoading() {
  let stacks: MessageHandler[] = []
  function loading(message = '正在加载...') {
    loading.closeAll()
    const instance = ElMessage({
      customClass: 'cloud-loading-namespace',
      icon: Loading,
      duration: 0,
      message,
    })
    stacks.push(instance)
    return instance
  }

  loading.closeAll = function () {
    stacks.forEach((v) => {
      v.close()
    })
    stacks = []
  }
  return loading
}
