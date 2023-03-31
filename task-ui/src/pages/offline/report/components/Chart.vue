<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
  name: {
    type: String,
    default: '',
  },
})
const currentInstance: any = getCurrentInstance()
const option: any = {
  title: {
    text: '',
    left: 'center',
  },
  tooltip: {
    trigger: 'item',
  },
  legend: {
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

let resizeObserver: any = null
let chart: any = null
const init = () => {
  chart = echarts.init(document.getElementById(`chart${props.name}`))
  chart.setOption(option)
  resizeObserver = new ResizeObserver((entries) => {
    chart.resize()
  })
  resizeObserver.observe(document.getElementById(`chart${props.name}`))
}
watch(
  () => props.data,
  () => {
    option.title.text = props.name
    option.series[0].data = props.data
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
  <div :id="`chart${name}`" ref="chart" style="height:250px;width:50%" />
</template>

<style lang="scss" scoped>
</style>
