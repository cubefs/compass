<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  data: {
    type: Object,
    default: () => {},
  },
  index: {
    type: Number,
    default: 0,
  },
})
const currentInstance: any = getCurrentInstance()
const option: any = {
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
    type: 'value',
  },
  series: [
  ],
}

let resizeObserver: any = null
let chart: any = null
const init = () => {
  chart = echarts.init(document.getElementById(`chart${props.index}`))
  chart.setOption(option)
  resizeObserver = new ResizeObserver((entries) => {
    chart.resize()
  })
  resizeObserver.observe(document.getElementById(`chart${props.index}`))
}
watch(
  () => props.data,
  () => {
    option.title.text = props.data.name
    option.xAxis.data = props.data.jobUsage.map(item => item.date)
    option.series = [
      {
        name: '',
        type: 'line',
        data: props.data.jobUsage.map(item => item.count),
      },
      {
        name: '',
        type: 'line',
        data: props.data.totalUsage.map(item => item.count),
      },
    ]
    chart.setOption(option)
  },
)
onBeforeUnmount(() => {
  resizeObserver.disconnect()
})
onMounted(async () => {
  init()
})
</script>

<template>
  <div :id="`chart${index}`" ref="chart" style="height:250px;width:50%" />
</template>

<style lang="scss" scoped>
</style>
