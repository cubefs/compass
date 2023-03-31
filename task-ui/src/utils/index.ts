
import Dayjs from 'dayjs'
export function getTimeStamp() {
  return Date.now()
}
export function tableEnumRender<T extends Record<string, any>, V extends { row: T }, K extends keyof T>(_enum: any, key: K) {
  return function (data: V) {
    const item = data.row
    const newKey = item[key]
    return _enum[newKey]
  }
}
export function tableDateRender<T extends Record<string, any>, V extends { row: T }, K extends keyof T>(key: K, format = 'YYYY-MM-DD') {
  return function (data: V) {
    const item = data.row
    const val = item[key]
    return date(val, format)
  }
}
export function date(timestamp: number | string, format = 'YYYY-MM-DD') {
  return Dayjs(timestamp).format(format)
}
