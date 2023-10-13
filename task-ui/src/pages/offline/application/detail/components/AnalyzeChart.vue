<!-- eslint-disable vue/no-mutating-props -->
<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  index: {
    type: String,
    default: '',
  },
  data: {
    type: Object,
    default: () => ({
    }),
  },
})
let chart: any = null
const option = {
  title: {
    text: '',
    textStyle: {
      fontSize: 15,
    },
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
    },
    formatter(params: any) {
      const title = `${params[0].axisId.split('0')[0]}: ${params[0].axisValue}`
      let content = ''
      params.forEach((item) => {
        content = item.value !== '-' ? `${content} <br />${item.marker} ${item.seriesName}: ${item.value}` : content
      })
      return `${title}${content}`
    },
  },
  legend: {
    right: 50,
    data: [],
  },
  barMaxWidth: 50,
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
  series: [],
}
let resizeObserver: any = null
const currentInstance: any = getCurrentInstance()
const update = () => {
  const queueList = props.data.dataList.sort((a, b) => a.xvalue - b.xvalue)
  const category = Object.entries(props.data.dataCategory)
  const legendData = category.map(([key, { name, color }]) => {
    return {
      name,
      color,
      key,
    }
  })
  const barData = category
    .map(([key, { name, color }]) => ({
      name,
      type: 'bar',
      stack: 'memory',
      color,
      key,
    }))
  barData.forEach((bar) => {
    bar.data = queueList.reduce((acc, now) => {
      if (now.yvalues.length === 1) {
        now.yvalues.forEach(item => item.type === bar.key ? acc.push(item.value) : acc.push('-'))
      }
      else if (now.yvalues.length > 1) {
        // Stacking situation
        now.yvalues.forEach(item => item.type === bar.key && acc.push(item.value))
      }
      return acc
    }, [])
  })
  const xName = queueList.map(item => item.xvalue)
  const yAxis = {
    name: props.data.y,
    axisLabel: {
      formatter: `{value} ${props.data.unit}`,
    },
  }
  option.title.text = props.data.des
  option.legend.data = legendData
  option.xAxis.name = props.data.x
  option.xAxis.data = xName
  option.yAxis = yAxis
  option.series = barData
  chart.setOption(option)
}
const init = () => {
  chart = echarts.init(currentInstance.proxy.$refs.test)
  update()
  resizeObserver = new ResizeObserver((entries) => {
    chart.resize()
  })
  resizeObserver.observe(currentInstance.proxy.$refs.test)
}
watch(
  () => props.data,
  () => {
    update()
  },
)
onMounted(() => {
  init()
})
onBeforeUnmount(() => {
  resizeObserver.disconnect()
})
</script>

<template>
  <div ref="test" style="height:300px;width:100%" />
</template>
