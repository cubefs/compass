<script setup lang="ts">
import dayjs from 'dayjs'
import CardRight from './components/ReportCardRight.vue'
import PieTabs from './components/PieTabs.vue'
import LineTabs from './components/LineTabs.vue'
import { get, post } from '~/utils/request'
dayjs().format()
let projectList = $ref([])
const projectName = $ref('')
const lineTab: string = $ref('first')
let sData = $ref({
  abnormalJobCpuNum: 0,
  abnormalJobCpuNumChainRatio: 0,
  abnormalJobCpuNumDayOnDay: 0,
  abnormalJobCpuNumRatio: 0,
  abnormalJobInstanceNum: 0,
  abnormalJobInstanceNumChainRatio: 0,
  abnormalJobInstanceNumDayOnDay: 0,
  abnormalJobInstanceNumRatio: 0,
  abnormalJobMemoryNum: 0,
  abnormalJobMemoryNumChainRatio: 0,
  abnormalJobMemoryNumDayOnDay: 0,
  abnormalJobMemoryNumRatio: 0,
  abnormalJobNum: 0,
  abnormalJobNumChainRatio: 0,
  abnormalJobNumDayOnDay: 0,
  abnormalJobNumRatio: 0,
  cpuUnit: '',
  jobCpuNum: 0,
  jobInstanceNum: 0,
  jobMemoryNum: 0,
  jobNum: 0,
  memoryUnit: '',
})
const getProjectList = async () => {
  const res = await get('/api/v1/report/projects')
  projectList = res
}
const getStatistics = async () => {
  const res = await get('/api/v1/report/statistics', {
    projectName: projectName,
  })
  if(res !== null){
    sData = res
  }
}
onMounted(() => {
  getProjectList()
  getStatistics()
})
</script>

<template>
  <el-card>
    项目
    <el-select @change="getStatistics" v-model="projectName">
      <el-option
        v-for="item in projectList"
        :key="item"
        :label="item"
        :value="item"
      />
    </el-select>
  </el-card>

  <el-card class="m-t-5">
    <div m-b-5>
      <span style="background: #f2f2f2; padding: 2px; font-size: 14px;">最近数据周期：{{ dayjs().format('YYYY-MM-DD') }}</span>
      <el-button type="primary" class="f-r" @click="getStatistics">
        刷新
      </el-button>
    </div>
    <el-scrollbar>
      <div class="report-card-list">
        <div
          class="report-card"
          style="border-left: 5px solid #00bfbf"
        >
          <div class="report-card-left">
            <div class="left-first">
              诊断任务数
            </div>
            <div class="left-second">
              {{ sData.abnormalJobNum }}
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">活跃任务数</span><span>{{ sData.jobNum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.abnormalJobNumChainRatio" :ratio="sData.abnormalJobNumRatio" :day-on-day="sData.abnormalJobNumDayOnDay" />
        </div>
        <div
          class="report-card"
          style="border-left: 5px solid #02a7f0"
        >
          <div class="report-card-left">
            <div class="left-first">
              诊断实例数
            </div>
            <div class="left-second">
              {{ sData.abnormalJobInstanceNum }}
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">运行实例数</span><span>{{ sData.jobInstanceNum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.abnormalJobInstanceNumChainRatio" :ratio="sData.abnormalJobInstanceNumRatio" :day-on-day="sData.abnormalJobInstanceNumDayOnDay" />
        </div>
        <div
          class="report-card"
          style="border-left: 5px solid #f59a23"
        >
          <div class="report-card-left">
            <div class="left-first">
              任务CPU消耗数
            </div>
            <div class="left-second">
              {{ sData.abnormalJobCpuNum }}&nbsp;<span class="unit">{{ sData.cpuUnit }}</span>
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">总CPU消耗数</span><span>{{ sData.jobCpuNum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.abnormalJobCpuNumChainRatio" :ratio="sData.abnormalJobCpuNumRatio" :day-on-day="sData.abnormalJobCpuNumDayOnDay" :unit="sData.cpuUnit" />
        </div>
        <div
          class="report-card"
          style="border-left: 5px solid #70b603"
        >
          <div class="report-card-left">
            <div class="left-first">
              任务内存消耗数
            </div>
            <div class="left-second">
              {{ sData.abnormalJobMemoryNum }}&nbsp;<span class="unit">{{ sData.memoryUnit }}</span>
            </div>
            <div class="left-third">
              <span m-r-3 style="color:#7f7f7f">总内存消耗数</span><span>{{ sData.jobMemoryNum }}</span>
            </div>
          </div>
          <CardRight :chain-ratio="sData.abnormalJobMemoryNumChainRatio" :ratio="sData.abnormalJobMemoryNumRatio" :day-on-day="sData.abnormalJobMemoryNumDayOnDay" :unit="sData.memoryUnit" />
        </div>
      </div>
    </el-scrollbar>
  </el-card>
  <el-card class="m-t-5">
    <LineTabs :projectName="projectName"/>
  </el-card>
  <el-card class="m-t-5" m-b-10>
    <PieTabs :projectName="projectName"/>
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
  display:flex;
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
    top:10px;
    right:0px;
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
:deep(.el-button--primary){
  background:#00bfbf !important;
}
</style>

<route lang="yaml">
meta:
  title: 报告总览
  name: report
name: report
</route>
