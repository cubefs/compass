<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  data: {
    type: Object,
    default: () => ({
    }),
  },
})
let chart: any = null
const option = {
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
    type: 'value',
    show: false,
  },
  yAxis: {
    type: 'category',
    data: ['executor资源', 'driver资源'],
  },
  barMaxWidth: 30,
  series: [],
}
let resizeObserver: any = null
const update = () => {
  const series = [
    {
      name: props.data.dataCategory.efficient.name,
      color: props.data.dataCategory.efficient.color,
      type: 'bar',
      stack: '总量',
      data: [100 - props.data.executorWastePercent, 100 - props.data.driverWastePercent],
      label: {
        show: true,
        position: 'top',
        formatter: (params: any) => {
          return `${params.seriesName}${Number(params.data).toFixed(2)}%`
        },
      },
    },
    {
      name: props.data.dataCategory.waste.name,
      color: props.data.dataCategory.waste.color,
      type: 'bar',
      stack: '总量',
      data: [props.data.executorWastePercent, props.data.driverWastePercent],
      label: {
        show: true,
        position: 'top',
        formatter: (params: any) => {
          return `${params.seriesName}${Number(params.data).toFixed(2)}%`
        },
      },
    },
  ]
  option.series = series
  chart.setOption(option)
}
const init = () => {
  const currentInstance: any = getCurrentInstance()
  chart = echarts.init(currentInstance.proxy.$refs.cpu)
  update()
  resizeObserver = new ResizeObserver((entries) => {
    chart.resize()
  })
  resizeObserver.observe(currentInstance.proxy.$refs.cpu)
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
  <div ref="cpu" style="height:300px;width:100%" />
</template>
