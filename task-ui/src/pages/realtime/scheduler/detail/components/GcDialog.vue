<script  setup lang='ts'>
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { get } from '~/utils/request'
let dialogVisible: boolean = $ref(false)
let tableData: any = $ref([])
let chartData = {
  tenuredUsed: [],
  youngUsed: [],
  heapUsed: [],
}
const option = {
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross', // Default line, optional as：'line' | 'shadow'
      label: {
        backgroundColor: '#6a7985',
      },
    },
  },
  legend: {
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: [
    {
      type: 'category',
      boundaryGap: false,
      data: [],
    },
  ],
  yAxis: [
    {
      type: 'value',
      name: '单位（KB}'
    },
  ],
  series: [],
}
const lineKeyMap = [
  { prop: 'tenuredUsed', name: '老年代' },
  { prop: 'youngUsed', name: '新生代' },
  { prop: 'heapUsed', name: '堆内存' },
]
let chart: any = null
const update = () => {
  const mark = {}
  lineKeyMap.forEach(({ prop }) => {
    chartData[prop].forEach(list => !Object.prototype.hasOwnProperty.call(mark, list.timestamp) && (mark[list.timestamp] = true))
  })
  const xDataArr = Object.entries(mark).sort((a, b) => a[0] - b[0])
  const xData = xDataArr.map(([key, value]) => dayjs(Number(key)).format('YYYY-MM-DD HH:mm:ss SSS'))
  const series = lineKeyMap.map(item => ({
    name: item.name,
    type: 'line',
    areaStyle: {},
    emphasis: {
      focus: 'series',
    },
    data: chartData[item.prop].map(_item => _item.used),
  }))
  option.series = series
  option.xAxis[0].data = xData
  chart.setOption(option)
}
async function init(applicationId, executorId) {
  dialogVisible = true
  const res = await get('/api/v1/app/gc', {
    applicationId,
    executorId,
  })
  const { maxHeapAllocatedSize, maxHeapUsedSize, totalTime, ygcountAndDuration, fgcountAndDuration, gccountAndDuration, tenuredUsed, youngUsed, heapUsed } = res
  tableData = [{
    maxHeapAllocatedSize,
    maxHeapUsedSize,
    totalTime,
    ygcountAndDuration,
    fgcountAndDuration,
    gccountAndDuration,
  }]
  chartData = {
    tenuredUsed,
    youngUsed,
    heapUsed,
  }
  chart = echarts.init(document.getElementById('chart'))
  update()
}
defineExpose({
  init,
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="GC日志分析"
    width="1000px"
  >
    <div style="margin-top: -20px;font-size: 16px;padding-bottom:10px;">
      GC概览
    </div>
    <el-table :data="tableData" style="width: 100%" border>
      <el-table-column prop="maxHeapAllocatedSize" label="最大分配内存" width="180" />
      <el-table-column prop="maxHeapUsedSize" label="最大使用内存" width="180" />
      <el-table-column prop="totalTime" label="运行时间" />
      <el-table-column prop="ygcountAndDuration" label="YGC次数/耗时(s)" />
      <el-table-column prop="fgcountAndDuration" label="FGC次数/耗时(s)" />
      <el-table-column prop="gccountAndDuration" label="GC次数/耗时(s)" />
    </el-table>
    <div class="title">
      运行内存
    </div>
    <div id="chart" style="height:300px;width:100%" />
  </el-dialog>
</template>

<style scoped lang='scss'>
.title {
  font-size: 16px;
  padding-top:10px;
  padding-bottom:10px;
}
</style>
