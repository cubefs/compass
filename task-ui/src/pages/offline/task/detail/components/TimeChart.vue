<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  data: {
    type: Object,
    default: () => ({
    }),
  },
  threshold: {
    type: Number,
    default: 0,
  },
})
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
    name: `单位（${props.data.unit}）`
  },
  barMaxWidth: 30,
  series: [],
}
watchEffect(() => {
  const series = [
    {
      name: props.data.dataCategory.duration.name,
      color: props.data.dataCategory.duration.color,
      type: 'line',
      data: props.data.dataList.map(item => item.yvalues[0].value),
      smooth: true,
      markLine: {
        symbol: ['', 'none'],
        lineStyle: {
          type: 'solid',
          color: '#70b7e6',
        },
        data: [{
          yAxis: props.threshold.toFixed(0),
        }],
      },
    },
  ]
  option.title.text = props.data.des
  option.xAxis.data = props.data.dataList.map(item => item.xvalue)
  option.yAxis.max = extent => extent.max > props.threshold.toFixed(0) ? extent.max : props.threshold.toFixed(0)
  option.series = series
})
let resizeObserver: any = null
const init = () => {
  const currentInstance: any = getCurrentInstance()
  const chart = echarts.init(currentInstance.proxy.$refs.chart)
  chart.setOption(option)
  resizeObserver = new ResizeObserver((entries) => {
    chart.resize()
  })
  resizeObserver.observe(currentInstance.proxy.$refs.chart)
}
onMounted(() => {
  init()
})
onBeforeUnmount(() => {
  resizeObserver.disconnect()
})
</script>

<template>
  <div ref="chart" style="height:250px;width:100%" />
</template>
