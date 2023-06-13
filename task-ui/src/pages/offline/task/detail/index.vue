<script setup lang="ts">
import TimeChart from './components/TimeChart.vue'
import BaseLine from './components/BaseLine.vue'
import AppDetail from './appDetail.vue'
import ItemWrapper from '~/pages/offline/application/detail/components/ItemWrapper.vue'
import ErrorTable from '~/pages/offline/application/detail/components/ErrorTable.vue'
import { get, post } from '~/utils/request'
import { cloudTheme } from '~/utils/setting'

const { query } = useRoute()
const params = {
  executionDate: query.executionDate,
  flowName: query.flowName,
  projectName: query.projectName,
  taskName: query.taskName,
}
let tryNumber: number = $ref(0)
let activeCard = $ref(-1)
let activeNames = $ref([])
let jobInfo = $ref({
  flowName: '',
  executionDate: '',
  taskName: '',
})
let taskApps = $ref([])
let summary = $ref([])
let appInfo = $ref([])
let timeConsuming = $ref({
  item: null,
  name: '',
  conclusion: {},
})
let baseLine = $ref({
  item: null,
  name: '',
  conclusion: {},
})
let errorLog = $ref({
  item: null,
  name: '',
  conclusion: {},
})
const statusMap = ['未处理', '已处理']
const getAppList = async () => {
  const res = await get('/api/v1/job/apps', params)
  tryNumber = Object.keys(res.taskApps)?.length
  jobInfo = res?.jobInfo
  taskApps = Object.values(res?.taskApps) || []
  nextTick(() => {
    activeNames = taskApps.map((item, index) => index)
  })
}
const getAppInfo = async () => {
  const res = await post('/api/v1/job/appDiagnoseInfo', {
    ...params,
    tryNumber,
  })
  appInfo = res.map((item) => {
    const arr: any[] = []
    Object.keys(item).forEach((x) => {
      arr.push({
        id: x,
        ...item[x],
      })
    })
    return arr
  })
}
const getSummary = async () => {
  const res = await post('/api/v1/job/summary', {
    ...params,
    tryNumber,
  })
  summary = res
}
const getJobInfo = async () => {
  const res = await post('/api/v1/job/jobDiagnoseInfo', {
    ...params,
    tryNumber,
  })
  timeConsuming = res[0]
  baseLine = res[1]
  errorLog = res[2]
}
const router = useRouter()
const back = () => {
  router.push({
    path: '/offline/task',
  })
}
const refAppDetail: any = ref(null)
const handleApp = (id, index) => {
  activeCard = index
  refAppDetail.value.handleApp(id)
}
onMounted(async () => {
  await getAppList()
  getSummary()
  getJobInfo()
  getAppInfo()
})
</script>

<template>
  <el-card shadow="never">
    <div class="detail-header">
      <el-icon style="font-size: 40px;">
        <Calendar />
      </el-icon>
      <div class="detail-header-text">
        <div style="margin-bottom: 5px">
          <el-row>
            <el-col :span="4">
              作业流：{{ jobInfo.flowName }}
            </el-col>
            <el-col :span="4">
              实例：{{ jobInfo.taskName }}
            </el-col>
            <el-col :span="8">
              执行周期：{{ jobInfo.executionDate }}
            </el-col>
          </el-row>
        </div>
        <div>
          <el-row>
            <el-col :span="4">
              CPU消耗：{{ jobInfo?.resource?.split(' ').slice(0, 2).join(' ') || '-' }}
            </el-col>
            <el-col :span="4">
              内存消耗：{{ jobInfo?.resource?.split(' ').slice(2).join(' ') || '-' }}
            </el-col>
            <el-col :span="4">
              处理状态：
              <el-tag v-if="/^[0-9]+.?[0-9]*/.test(jobInfo.taskStatus)">
                {{ statusMap[jobInfo.taskStatus] }}
              </el-tag>
              <span v-else>-</span>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
    <el-button class="back-btn" @click="back">
      <el-icon><Back /></el-icon>返回
    </el-button>
  </el-card>
  <div v-if="taskApps.length" style="width: 100%;">
    <el-row>
      <el-col :span="5">
        <div class="detail-aside">
          <el-scrollbar>
            <div class="detail-aside-title">
              执行次数
            </div>
            <div>
              <el-collapse v-model="activeNames">
                <el-collapse-item v-for="(item, index) in taskApps" :key="index" :name="index">
                  <template #title>
                    <span class="m-l-5">第 {{ index + 1 }} 次执行</span>
                  </template>
                  <div v-for="(_item, _index) in item" :key="_item.applicationId" class="detail-aside-content">
                    <div class="appCard" :style="{ background: activeCard === _index ? '#eaf9f5' : '' }" @click="handleApp(_item.applicationId, _index)">
                      <div>{{ _item.applicationId }}</div>
                      <div>
                        <span
                          v-for="(category, c_index) in _item.categories"
                          :key="category"
                          class="category-card"
                          :title="category"
                          :style="{ 'border-left': `3px solid ${cloudTheme[c_index]}` }"
                        >
                          {{ category }}
                        </span>
                      </div>
                      <div><span class="m-r-5">运行耗时：{{ _item.duration }}</span><span>资源消耗：{{ _item.resource }}</span></div>
                    </div>
                    <div h-1 />
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </el-scrollbar>
        </div>
      </el-col>
      <el-col :span="19">
        <div class="detail-aside">
          <el-scrollbar>
            <div class="detail-aside-title">
              诊断详情
            </div>
            <div class="detail-content-summary">
              <div v-for="item in summary" :key="item" v-html="item" />
            </div>
            <ItemWrapper v-if="errorLog.item" :title="errorLog.name" :conclusion="errorLog.conclusion" class="detail-content-component">
              <ErrorTable :data="errorLog.item?.tableList[0]" :width-list="[120, 180, 180, '', 270]" />
            </ItemWrapper>
            <ItemWrapper v-if="timeConsuming.item" :title="timeConsuming.name" :conclusion="timeConsuming.conclusion" class="detail-content-component">
              <TimeChart :data="timeConsuming.item?.chartList[0]" :threshold="timeConsuming.item?.threshold" />
            </ItemWrapper>
            <ItemWrapper v-if="baseLine.item" :title="baseLine.name" :conclusion="baseLine.conclusion" class="detail-content-component">
              <BaseLine :info="baseLine.item" />
            </ItemWrapper>
            <AppDetail v-if="appInfo.length" ref="refAppDetail" :data="appInfo" />
          </el-scrollbar>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.back-btn {
  position: absolute;
  top:20px;
  right:25px;
}
.detail-header {
  display: flex;
  align-items: center;
  .detail-header-text {
    margin-left: 15px;
    font-size: 14px;
    width: 100%;
    .text-detail {
      margin-right: 40px;
    }
  }
}
.detail-aside {
  height: calc(100vh - 195px);
  border: 1px solid #e4e7ed;
  border-radius: 1px;
  background: white;
  .detail-aside-title {
    background: #f2f2f2;
    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 24px;
      background-color: #2fc29b;
      margin-right: 5px;
      vertical-align: middle;
    }
  }
  .detail-aside-content {
    padding: 10px 10px 0px 10px;
  }
}
.detail-content-summary {
  margin: 20px 20px 0px 20px;
  padding: 20px;
  background: #e0e0e0;
  border-radius: 4px;
}
.detail-content-component {
  margin: 20px 20px 0px 20px;
}
.appCard {
  border: 1px solid #e0e0e0;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
}
.category-card {
  box-shadow: none;
  padding: 0px 6px;
}
</style>

<route lang="yaml">
meta:
  name: application
name: taskDetail
</route>
