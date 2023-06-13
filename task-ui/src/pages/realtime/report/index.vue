<script setup lang="ts">
import dayjs from 'dayjs'
import CardRight from './components/ReportCardRight.vue'
import PieTabs from './components/PieTabs.vue'
import LineTabs from './components/LineTabs.vue'
import { get, post } from '~/utils/request'
const projectList = $ref([])
const project = $ref('')
const lineTab: string = $ref('first')
let sData = $ref({
  "generalViewNumberDto": {
    "baseTaskCntSum": 1,
    "exceptionTaskCntSum": 0,
    "resourceTaskCntSum": 1,
    "totalCoreNumSum": 4,
    "totalMemNumSum": 8192,
    "cutCoreNumSum": 3,
    "cutMemNumSum": 6144,
    "date": "2023-05-15 14:20:01"
  },
  "generalViewNumberDtoDay1Before": {
    "baseTaskCntSum": 1,
    "exceptionTaskCntSum": 0,
    "resourceTaskCntSum": 1,
    "totalCoreNumSum": 4,
    "totalMemNumSum": 8192,
    "cutCoreNumSum": 2,
    "cutMemNumSum": 4096,
    "date": "2023-05-14 14:20:01"
  },
  "generalViewNumberDtoDay7Before": {
    "baseTaskCntSum": 1,
    "exceptionTaskCntSum": 0,
    "resourceTaskCntSum": 0,
    "totalCoreNumSum": 1,
    "totalMemNumSum": 2048,
    "cutCoreNumSum": 0,
    "cutMemNumSum": 0,
    "date": "2023-05-08 14:20:01"
  },
  "abnormalJobNumRatio": 0,
  "abnormalJobNumChainRatio": 0,
  "abnormalJobNumDayOnDay": 0,
  "resourceJobNumRatio": 0,
  "resourceJobNumChainRatio": 0,
  "resourceJobNumDayOnDay": 0,
  "cpuUnit": 0,
  "resourceCpuNumRatio": 0,
  "resourceCpuNumChainRatio": 0,
  "resourceCpuNumDayOnDay": 0,
  "memoryUnit": 0,
  "resourceMemoryNumRatio": 0,
  "resourceMemoryNumChainRatio": 0,
  "resourceMemoryNumDayOnDay": 0,
})
const getRealtimeStatistics = async () => {
  const time = $ref([dayjs().subtract(1, 'day').hour(0).minute(0).second(0).millisecond(0).valueOf(), dayjs().hour(0).minute(0).second(0).millisecond(0).valueOf()])
  var endTs = time[1] / 1000
  var startTs = time[0] / 1000
  console.log(endTs)
  console.log(startTs)
  try {
    const res = await post('/api/realtime/taskDiagnosis/getGeneralViewNumber', {
      startTs: startTs,
      endTs: endTs,
    })
    console.log(res)
    sData = res
  } catch (error) {
    console.log(error)
  }
}
onMounted(() => {
  getRealtimeStatistics()
})
</script>

<template>
  <el-card>
    项目
    <el-select v-model="project">
      <el-option v-for="item in projectList" :key="item" :label="item" :value="item" />
    </el-select>
  </el-card>

  <el-card class="m-t-5">
    <div m-b-5>
      <span style="background: #f2f2f2; padding: 2px; font-size: 14px;">最近数据周期：{{ dayjs().format('YYYY-MM-DD') }}</span>
      <el-button type="primary" class="f-r" @click="getRealtimeStatistics">
        刷新
      </el-button>
    </div>
    <el-scrollbar>
      <div class="report-card-list">
        <div class="report-card" style="border-left: 5px solid #00bfbf">
          <div class="report-card-left">
            <div class="left-first">
              异常作业数
            </div>
            <div class="left-second">
              {{ sData.generalViewNumberDto.exceptionTaskCntSum }}
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">诊断作业数</span><span>{{ sData.generalViewNumberDto.baseTaskCntSum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.abnormalJobNumChainRatio" :ratio="sData.abnormalJobNumRatio"
            :day-on-day="sData.abnormalJobNumDayOnDay" />
        </div>
        <div class="report-card" style="border-left: 5px solid #02a7f0">
          <div class="report-card-left">
            <div class="left-first">
              可优化资源作业数
            </div>
            <div class="left-second">
              {{ sData.generalViewNumberDto.resourceTaskCntSum }}
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">诊断作业数</span><span>{{ sData.generalViewNumberDto.baseTaskCntSum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.resourceJobNumChainRatio" :ratio="sData.resourceJobNumRatio"
            :day-on-day="sData.resourceJobNumDayOnDay" />
        </div>
        <div class="report-card" style="border-left: 5px solid #f59a23">
          <div class="report-card-left">
            <div class="left-first">
              可优化CPU数
            </div>
            <div class="left-second">
              {{ sData.generalViewNumberDto.cutCoreNumSum }}&nbsp;<span class="unit">{{ sData.cpuUnit }}</span>
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">CPU总数</span><span>{{ sData.generalViewNumberDto.totalCoreNumSum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.resourceCpuNumChainRatio" :ratio="sData.resourceCpuNumRatio"
            :day-on-day="sData.resourceCpuNumDayOnDay" :unit="sData.cpuUnit" />
        </div>
        <div class="report-card" style="border-left: 5px solid #70b603">
          <div class="report-card-left">
            <div class="left-first">
              可优化内存数
            </div>
            <div class="left-second">
              {{ sData.generalViewNumberDto.cutMemNumSum }}&nbsp;<span class="unit">{{ sData.memoryUnit }}</span>
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">内存数总数</span><span>{{ sData.generalViewNumberDto.totalMemNumSum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.resourceMemoryNumChainRatio" :ratio="sData.resourceMemoryNumRatio"
            :day-on-day="sData.resourceMemoryNumDayOnDay" :unit="sData.memoryUnit" />
        </div>
      </div>
    </el-scrollbar>
  </el-card>
  <el-card class="m-t-5">
    <LineTabs />
  </el-card>
  <el-card class="m-t-5" m-b-10>
    <PieTabs />
  </el-card>
</template>

<style lang="scss" scoped>
.report-card-list {
  display: flex;
  width: 1000px;
}

.report-card {
  border: 1px solid #e0e0e0;
  box-shadow: 2px 1px 5px 2px #eaeaea;
  border-radius: 3px;
  height: 150px;
  display: flex;
  width: 424px;
  margin-right: 20px;
  flex-shrink: 0;
  margin-bottom: 20px;
  font-family: auto;
}

.report-card-left {
  width: 50%;
  padding: 5px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;

  &::after {
    content: '';
    position: absolute;
    display: inline-block;
    top: 10px;
    right: 0px;
    width: 1px;
    height: 90%;
    background-color: #dfdfdf;
    margin-right: 5px;
    vertical-align: middle;
  }

  .left-first {
    margin-top: 10px;
  }

  .left-second {
    margin-top: 5px;
    font-size: 32px;
    font-weight: bold;
  }

  .left-third {
    margin-top: 15px;
    font-size: 14px;
  }
}

.unit {
  font-size: 13px;
  font-weight: bold;
}

:deep(.el-button--primary) {
  background: #00bfbf !important;
}
</style>

<route lang="yaml">
meta:
  title: 报告总览
  name: report
name: realtime-report
</route>
