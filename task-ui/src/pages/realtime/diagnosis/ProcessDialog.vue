<script setup lang="ts">
import { get, post } from '~/utils/request'
import dayjs from 'dayjs'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
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
  const time = $ref([dayjs().subtract(1, 'day').hour(0).minute(0).second(0).millisecond(0).valueOf(), dayjs().hour(0).minute(0).second(0).millisecond(0).valueOf()])
  var endTs = time[1] / 1000
  var startTs = time[0] / 1000
  var res
  try {
    resp = await post('/api/flink/diagnosis', {
      appId: applicationId,
      start: startTs,
      end: endTs,
    })
    res = resp.data
  } catch (error) {
    loading = false
    setTimeout(() => {
      dialogVisible = false
    }, 2000)
    console.log(error)
    return
  }
  console.log(res)
  // processInfoList = res.processInfoList
  if (res.status != 'succeed') {
    loading = false
    setTimeout(() => {
      dialogVisible = false
    }, 2000)
    // setTimeout(() => {
    //   diagnosisSearch()
    // }, 5000)
  } else {
    loading = false
    if (res.flinkTaskAnalysis == null) {
      ElMessage.error(t('diagnosis.diagnosisFailed'))
    } else {
      emit('searchComplete', res.flinkTaskAnalysis)
    }
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
  <el-dialog v-model="dialogVisible" width="550px">
    <template #header>
      <span>
        {{ $t('diagnosis.progress') }}
      </span>
      <el-icon v-if="loading" class="is-loading">
        <Loading />
      </el-icon>
    </template>
    <div w-full flex="~" flex-col items-center>
      <div v-for="item in processInfoList" :key="item.msg" w-120 flex="~" flex-col m-b-5>
        <div m-b-2>
          {{ item.msg }}
        </div>
        <el-progress :text-inside="true" :stroke-width="26" :percentage="item.speed" />
      </div>
    </div>
  </el-dialog>
</template>

