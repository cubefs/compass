<script setup lang="ts">
import * as echarts from 'echarts'
const props = defineProps({
  dataInput: {
    type: Object,
    default: () => ({
    }),
  },
})
let chart: any = null

let resizeObserver: any = null

const addBarChart = (chartItem, childChartDiv) => {
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
    },
    yAxis: {
      type: 'category',
      data: chartItem.bars.map(d => {
        return d.key
      }),
    },
    barMaxWidth: 30,
    series: [],
  }

  console.log(chartItem)
  var seriesLine = {
    data: chartItem.bars.map(d => {
      return d.value
    }),
    type: 'bar',
    label: {
      show: true,
      position: 'top',
      formatter: (params: any) => {
        return `${Number(params.data).toFixed(2)} ${chartItem.YAxisUnit}`
      },
    },
  }
  option.series = seriesLine
  console.log(option)
  childChartDiv.setOption(option)
  console.log('end')
}
const addLineChart = (chartItem, childChart) => {
  const option: any = {
    title: {
      text: '',
    },
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      type: "scroll",
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'time',
    },
    yAxis: {
      type: 'value',
    },
    series: [
    ],
  }
  option.yAxis.axisLabel = {
    show: true,
    interval: 'auto',
    formatter: val => `${val}${chartItem.YAxisUnit}`
  }
  option.tooltip.formatter = function (params) {
    var relVal = params[0].name;
    for (var i = 0, l = params.length; i < l; i++) {
      console.log('tooltip数据值', params[i])
      //遍历出来的值一般是字符串，需要转换成数字，再进项tiFixed四舍五入
      console.log(params[i].value[1])
      relVal += params[i].axisValueLabel + '<br/>' + params[i].marker + params[i].seriesName + ' : ' + (Number(params[i].value[1])).toFixed(2) + chartItem.YAxisUnit + '<br/>'
    }
    return relVal;
  };
  console.log(chartItem)
  var seriesLine = chartItem.line.data.slice(0,6).map((d: any) => {
    console.log(d)
    let metric = JSON.stringify(d.metric)
    if (metric == "{}") {
      metric = ""
    }
    if (chartItem.YAxisValueType == 'Percent') {

      return {
        name: chartItem.line.label + metric,
        type: 'line',
        data: d.values.map(x => {
          return [x[0] * 1000, x[1] * 100]
        }),
      }
    } else {
      return {
        name: chartItem.line.label + metric,
        type: 'line',
        data: d.values.map(x => {
          return [x[0] * 1000, x[1]]
        }),
      }
    }

  })
  option.series = seriesLine
  console.log(option)
  childChart.setOption(option)
}

const update = () => {
  console.log(props.dataInput)
  let report = props.dataInput
  console.log(report)
  var index = 0
  for (var chartItem of report.IDiagnosisRuleCharts) {
    if (chartItem.type == 'Line') {
      // console.log(chartItem)
      console.log('update line')
      const currentInstance: any = getCurrentInstance()
      let parentDiv = currentInstance.proxy.$refs.realtime
      var childChartDiv = document.createElement("div")
      childChartDiv.style.cssText = "height:300px;width:100%"
      childChartDiv.id = 'realtimeChild' + index
      parentDiv.appendChild(childChartDiv)
      let childChart = echarts.init(childChartDiv)
      resizeObserver = new ResizeObserver((entries) => {
        childChart.resize()
      })
      resizeObserver.observe(childChartDiv)
      addLineChart(chartItem, childChart)
      index = index + 1
    } else if (chartItem.type == 'Bar') {
      // console.log(chartItem)
      console.log('update bar')
      const currentInstance: any = getCurrentInstance()
      let parentDiv = currentInstance.proxy.$refs.realtime
      var childChartDiv = document.createElement("div")
      childChartDiv.style.cssText = "height:300px;width:100%"
      childChartDiv.id = 'realtimeChild' + index
      parentDiv.appendChild(childChartDiv)
      let childChart = echarts.init(childChartDiv)
      resizeObserver = new ResizeObserver((entries) => {
        childChart.resize()
      })
      resizeObserver.observe(childChartDiv)
      addBarChart(chartItem, childChart)
      index = index + 1
    }
  }
}

const init = () => {
  // const currentInstance: any = getCurrentInstance()
  // chart = echarts.init(currentInstance.proxy.$refs.realtime)
  update()
  // resizeObserver = new ResizeObserver((entries) => {
  //   chart.resize()
  // })
  // resizeObserver.observe(currentInstance.proxy.$refs.realtime)

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
  <div ref="realtime" style="height:300px;width:100%" />
</template>
