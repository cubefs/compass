<script setup lang="ts">
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { post } from '~/utils/request'
const lineTab: string = $ref('first')
let cpuData = $ref({})
let memData = $ref({})
let data: any = $ref([])
const time = $ref([dayjs().subtract(7, 'day').hour(0).minute(0).second(0).millisecond(0).valueOf(), dayjs().hour(0).minute(0).second(0).millisecond(0).valueOf()])
const getLineChart = async () => {
  const res = await Promise.all([
    post('/api/v1/report/graph', {
      projectName: '',
      start: time[0] / 1000,
      end: time[1] / 1000,
      graphType: 'cpuTrend',
    }),
    post('/api/v1/report/graph', {
      projectName: '',
      start: time[0] / 1000,
      end: time[1] / 1000,
      graphType: 'memoryTrend',
    }),
    await post('/api/v1/report/graph', {
      projectName: '',
      start: time[0] / 1000,
      end: time[1] / 1000,
      graphType: 'numTrend',
    }),
  ])
  data = res
  cpuData = lineTab === 'first' ? data[0].trendGraph : data[2].trendGraph
  memData = data[1].trendGraph
}
const option = {
  title: {
    text: '',
  },
  tooltip: {
    trigger: 'axis',
  },
  legend: {
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: [],
  },
  yAxis: {
    name: '',
    type: 'value',
  },
  series: [
  ],
}
let cpuChart: any = null
let memChart: any = null
let resizeObserverCpu: any = null
let resizeObserverMem: any = null
const init = () => {
  cpuChart = echarts.init(document.getElementById('chart1'))
  cpuChart.setOption(option)
  memChart = echarts.init(document.getElementById('chart2'))
  memChart.setOption(option)
  resizeObserverCpu = new ResizeObserver((entries) => {
    cpuChart.resize()
  })
  resizeObserverCpu.observe(document.getElementById('chart1'))
  resizeObserverMem = new ResizeObserver((entries) => {
    cpuChart.resize()
  })
  resizeObserverMem.observe(document.getElementById('chart2'))
}
watch(
  () => cpuData,
  () => {
    option.title.text = cpuData.name
    option.yAxis.name = cpuData.unit ? `单位（${cpuData.unit}）` : ''
    option.xAxis.data = cpuData.jobUsage.data.map(item => item.date.slice(0,10))
    option.series = [
      {
        name: cpuData.jobUsage.name,
        type: 'line',
        data: cpuData.jobUsage.data.map(item => item.count),
      },
      {
        name: cpuData.totalUsage.name,
        type: 'line',
        data: cpuData.totalUsage.data.map(item => item.count),
      },
    ]
    cpuChart.setOption(option)
  },
)
watch(
  () => memData,
  () => {
    option.title.text = memData.name
    option.yAxis.name = `单位（${memData.unit}）`
    option.xAxis.data = memData.jobUsage.data.map(item => item.date.slice(0,10))
    option.series = [
      {
        name: memData.jobUsage.name,
        type: 'line',
        data: memData.jobUsage.data.map(item => item.count),
      },
      {
        name: memData.totalUsage.name,
        type: 'line',
        data: memData.totalUsage.data.map(item => item.count),
      },
    ]
    memChart.setOption(option)
  },
)
const handleClick = (val) => {
  cpuData = val.paneName === 'first' ? data[0].trendGraph : cpuData = data[2].trendGraph
}
onMounted(() => {
  getLineChart()
  init()
})
onBeforeUnmount(() => {
  resizeObserverCpu.disconnect()
  resizeObserverMem.disconnect()
  if (cpuChart) {
    cpuChart.clear()
    cpuChart.dispose()
    cpuChart = null
  }
})
</script>

<template>
  <el-tabs v-model="lineTab" @tab-click="handleClick">
    <div m-b-10 flex="~">
      <div style="width:62%" />
      <el-date-picker
        v-model="time"
        type="daterange"
        range-separator="-"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="x"
        @change="getLineChart"
      />
      <div style="width:8%" />
    </div>
    <el-tab-pane label="资源趋势" name="first" />
    <el-tab-pane label="数量趋势" name="second" />
    <div flex="~">
      <div id="chart1" key="chart1" style="height:250px;width:50%" />
      <div v-show="lineTab === 'first'" id="chart2" style="height:250px;width:50%" />
    </div>
  </el-tabs>
</template>

<style lang="scss" scoped>
</style>
