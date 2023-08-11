import type { Method } from 'axios'
import axios from 'axios'
import type { MessageHandler } from 'element-plus'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import { createLoading } from './loading'
import type { TAjaxExt } from '~/interfaces'

const loadingService = createLoading()

let lockCount = 0
let lockInstance: any | null = null

let loadingCount = 0
let loadingInstance: MessageHandler | null = null

axios.defaults.validateStatus = function (status) {
  return status >= 200 && status <= 204 // default
}

function getBaseURL():string {
  let backend = import.meta.env.MODE === 'development' ? import.meta.env.VITE_APP_DEV_BACKEND : import.meta.env.VITE_APP_PROD_BACKEND;
  if(backend === '') {
    return window.location.origin + '/compass';
  }
  return backend + '/compass';
}

axios.defaults.baseURL = getBaseURL()

axios.defaults.withCredentials = true
axios.interceptors.request.use(
  (config:any) => {
    let token = localStorage.getItem('token');
    if (token) {
      config.headers['token'] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
)
const request = function (type: Method) {
  return async function (
    url: string,
    data: Record<string, any> = {},
    ext: TAjaxExt = {},
  ) {
    const {
      lock,
      loading,
      allResponseData = false,
      ignoreMsg = false,
      successTips = '',
      confirmTips = '',
    } = ext
    const config = {} as TAjaxExt
    const currentIsLock
      = lock ?? ['put', 'post', 'patch', 'delete'].includes(type)
    Object.assign(config, ext)
    // 当设置_mock时当条请求使用mock数据
    config.url = url
    config.method = type
    config.headers = config.headers || {}
    if (confirmTips)
      await ElMessageBox.confirm(confirmTips)

    try {
      if (['put', 'post', 'patch'].includes(type))
        config.data = data
      else config.params = data

      // const timestamp = getTimeStamp()
      // config.params = {
      //   timestamp,
      //   ...config.params,
      // }
      if (currentIsLock) {
        lockCount++
        if (!lockInstance) {
          lockInstance = ElLoading.service({
            lock: true,
            text: typeof lock === 'string' ? lock : '请求处理中...',
            spinner: 'el-icon-lock',
            background: 'rgba(0, 0, 0, 0.3)',
          })
        }
      }
      else if (loading !== false) {
        loadingCount++
        if (!loadingInstance)
          loadingInstance = loadingService()
      }
      const res = await axios(config)
      if ([500].includes(+res.data.code)) {
        // ElMessage.error(res.data.msg)
        // throw new Error('请求失败')
        if(res.data && res.data.msg){
          throw new Error(res.data.msg)
        }else{
          throw new Error('内部错误')
        }
      }

      // if (!['0000', 0].includes((res.data as IAjaxResponse).status))
      //   throw res.data

      if (successTips)
        ElMessage.success(typeof successTips === 'boolean' ? '操作成功' : successTips)
      return allResponseData ? res.data : res.data.data
    }
    catch (error: any) {
      // console.log(error)
      // console.log(error.message)
      let errorMsg = '请求错误'
      console.log(typeof error)
      if (typeof error === 'string')
        errorMsg = error
      if (error && typeof error === 'object') {
        if(error.cause)
          errorMsg = error.cause
        if (error.message)
          errorMsg = error.message
        if(error.response && error.response.data)
          errorMsg = error.response.data.message
      }
      !ignoreMsg && ElMessage.error(errorMsg)
      throw error
    }
    finally {
      if (currentIsLock) {
        lockCount--
        if (lockInstance && lockCount <= 0) {
          lockInstance.close()
          lockInstance = null
          lockCount = 0
        }
      }
      else if (loading !== false) {
        loadingCount--
        if (loadingInstance && loadingCount <= 0) {
          loadingInstance.close()
          loadingInstance = null
          loadingCount = 0
        }
      }
      lockCount--
    }
  }
}
export const get = request('get')
export const post = request('post')
export const put = request('put')
export function tableSearch(url: string, payload = {}) {
  return async function (params = {}) {
    const { data, count } = await get(url, { ...params, ...payload })
    return {
      list: data,
      total: count,
    }
  }
}
