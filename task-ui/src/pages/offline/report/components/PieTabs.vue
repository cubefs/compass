<script setup lang="ts">
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { post } from '~/utils/request'
const circleTab: string = $ref('first')
let firstChart = $ref({})
let secondChart = $ref({})
let data = $ref({
  cpu: {},
  mem: {},
  num: {},
})
const time = $ref([dayjs().subtract(7, 'day').hour(0).minute(0).second(0).millisecond(0).valueOf(), dayjs().hour(0).minute(0).second(0).millisecond(0).valueOf()])
const getPieChart = async () => {
  const res = await post('/api/v1/report/graph', {
    projectName: '',
    start: time[0] / 1000,
    end: time[1] / 1000,
    graphType: 'distribution',
  })
  data = res
  firstChart = circleTab === 'first' ? data.cpu : data.num
  secondChart = data.mem
}
const option: any = {
  title: {
    text: '',
    left: 'center',
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} <br/>{d}%',
  },
  legend: {
    type: 'scroll',
    orient: 'vertical',
    left: 'left',
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
    },
  ],
}
let resizeObserverCpu: any = null
let resizeObserverMem: any = null
let cpuChart: any = null
let memChart: any = null
const init = () => {
  cpuChart = echarts.init(document.getElementById('chart3'))
  cpuChart.setOption(option)
  memChart = echarts.init(document.getElementById('chart4'))
  memChart.setOption(option)
  resizeObserverCpu = new ResizeObserver((entries) => {
    cpuChart.resize()
  })
  resizeObserverCpu.observe(document.getElementById('chart3'))
  resizeObserverMem = new ResizeObserver((entries) => {
    cpuChart.resize()
  })
  resizeObserverMem.observe(document.getElementById('chart4'))
}
watch(
  () => firstChart,
  () => {
    option.title.text = firstChart.name
    option.series[0].data = firstChart.data
    cpuChart.setOption(option)
  },
)
watch(
  () => secondChart,
  () => {
    option.title.text = secondChart.name
    option.series[0].data = secondChart.data
    memChart.setOption(option)
  },
)
const handleClick = (val) => {
  firstChart = val.paneName === 'first' ? data.cpu : data.num
}
onMounted(() => {
  getPieChart()
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
  <el-tabs v-model="circleTab" @tab-click="handleClick">
    <div m-b-10 flex="~">
      <div style="width:62%" />
      <el-date-picker
        v-model="time"
        type="daterange"
        range-separator="-"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="x"
        @change="getPieChart"
      />
      <div style="width:8%" />
    </div>
    <el-tab-pane label="资源分布" name="first" />
    <el-tab-pane label="数量分布" name="second" />
    <div flex="~">
      <div id="chart3" style="height:250px;width:50%" />
      <div v-show="circleTab === 'first'" id="chart4" style="height:250px;width:50%" />
    </div>
  </el-tabs>
</template>

<style lang="scss" scoped>
</style>
