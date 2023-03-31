import type { AxiosRequestConfig } from 'axios'

export type TAjaxExt = {
  lock?: boolean | string
  loading?: boolean | string
  ignoreMsg?: boolean
  allResponseData?: boolean
  tokenInspect?: boolean
  successTips?: string | boolean
  confirmTips?: string
} & AxiosRequestConfig
