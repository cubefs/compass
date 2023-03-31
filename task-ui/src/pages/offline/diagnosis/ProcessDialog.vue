<script setup lang="ts">
import { get, post } from '~/utils/request'
interface processInfo {
  msg: string
  speed: number
}
const emit = defineEmits(['searchComplete'])
let dialogVisible: boolean = $ref(false)
let applicationId = ''
let processInfoList: processInfo[] = $ref([])
async function init(id: string) {
  applicationId = id
  diagnosisSearch()
  dialogVisible = true
}
const timer: any = null
let loading: boolean = $ref(true)
async function diagnosisSearch() {
  const res = await get('/api/v1/app/diagnose', { applicationId })
  processInfoList = res.processInfoList
  if (res.status !== 'succeed') {
    setTimeout(() => {
      diagnosisSearch()
    }, 5000)
  }
  else {
    loading = false
    emit('searchComplete', res.taskAppInfo)
    setTimeout(() => {
      dialogVisible = false
    }, 2000)
  }
}
defineExpose({
  init,
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    width="550px"
  >
    <template #header>
      <span>
        进度
      </span>
      <el-icon v-if="loading" class="is-loading">
        <Loading />
      </el-icon>
    </template>
    <div w-full flex="~" flex-col items-center>
      <div
        v-for="item in processInfoList"
        :key="item.msg"
        w-120
        flex="~"
        flex-col
        m-b-5
      >
        <div m-b-2>
          {{ item.msg }}
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage="item.speed" />
      </div>
    </div>
  </el-dialog>
</template>

