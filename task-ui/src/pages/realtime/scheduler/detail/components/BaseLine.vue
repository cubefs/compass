<script setup lang="ts">
import G6 from '@antv/g6'
import { DynamicSizeGrid } from 'element-plus'
const props = defineProps({
  info: {
    type: Object,
    default: () => ({
    }),
  },
})
const data = computed(() => {
  return {
    nodes: props.info.nodeList.map(item => ({
      ...item,
      label: item.taskName,
      id: `${item.id}`,
    })),
    edges: props.info.vergeList.map(item => ({
      source: `${item.upstream}`,
      target: `${item.downStream}`,
    })),
  }
})
let resizeObserver: any = null
const tooltip = new G6.Tooltip({
  offsetX: 10,
  offsetY: 10,
  itemTypes: ['node'],
  getContent: (e) => {
    const { duration, durationBaseLine, endTime, endTimeBaseLine, executionDate, flowName, taskState, taskName, startTime, projectName } = e.item.getModel()
    const outDiv = document.createElement('div')
    outDiv.style.width = 'fit-content'
    outDiv.innerHTML = `
      <ul>
        <li>实例名称: ${taskName || '-'}</li>
      </ul>
      <ul>
        <li>作业流: ${flowName || '-'}</li>
      </ul>
      <ul>
        <li>项目名称: ${projectName || '-'}</li>
      </ul>
      <ul>
        <li>执行周期: ${executionDate || '-'}</li>
      </ul>
      <ul>
        <li>开始时间: ${startTime || '-'}</li>
      </ul>
      <ul>
        <li>结束时间: ${endTime || '-'}</li>
      </ul>
      <ul>
        <li>基线时间: ${endTimeBaseLine || '-'}</li>
      </ul>
      <ul>
        <li>基线耗时: ${durationBaseLine || '-'}</li>
      </ul>
      <ul>
        <li>运行耗时: ${duration || '-'}</li>
      </ul>
      <ul>
        <li>任务状态: ${taskState || '-'}</li>
      </ul>`
    return outDiv
  },
})
const init = () => {
  const container = document.getElementById('container')
  const width = container.scrollWidth
  const height = container.scrollHeight || 400
  const graph = new G6.Graph({
    container: 'container',
    width,
    height,
    fitView: true,
    plugins: [tooltip],
    modes: {
      default: ['drag-canvas', 'zoom-canvas'],
    },
    layout: {
      type: 'dagre',
      rankdir: 'LR',
      align: 'UL',
      controlPoints: true,
      nodesepFunc: () => 1,
      ranksepFunc: () => 1,
    },
    defaultNode: {
      size: [100, 25],
      type: 'rect',
      style: {
        lineWidth: 0,
        fill: '#4a90e2',
      },
      labelCfg: {
        style: {
          fontSize: 10,
          fill: '#ffffff',
        },
      },
    },
    defaultEdge: {
      type: 'polyline',
      size: 1,
      color: '#78d3e2',
      style: {
        endArrow: {
          path: 'M 0,0 L 4,2 L 4,-2 Z',
          fill: '#78d3e2',
        },
        radius: 10,
      },
    },
    nodeStateStyles: {
      active: {
        lineWidth: 0,
        fill: '#78d3e2',
      },
    },
  })
  graph.node((node) => {
    if (node.endTimeAbnormal)
      node.style.fill = '#fb4404'
    node.size = [node.taskName.length * 6, 25]
    return node
  })
  graph.data(data.value)
  graph.render()
  graph.on('node:mouseenter', (e) => {
    graph.setItemState(e.item, 'active', true)
  })
  graph.on('node:mouseleave', (e) => {
    graph.setItemState(e.item, 'active', false)
  })
  resizeObserver = new ResizeObserver((entries) => {
    graph.changeSize(container.clientWidth, container.scrollHeight)
  })
  resizeObserver.observe(container)
}
onMounted(() => {
  init()
})
onBeforeUnmount(() => {
  resizeObserver.disconnect()
})
</script>

<template>
  <div style="position: relative;">
    <div id="container" />
    <div class="tips">
      <div>
        <div style="width:30px;height:10px;background:#fb4404;display: inline-block;margin-right: 10px;" /><span>基线异常任务</span>
      </div>
      <div>
        <div style="width:30px;height:10px;background:#4a90e2;display: inline-block;margin-right: 10px;" /><span>正常任务</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.g6-component-tooltip {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 0px 10px 24px 10px;
  box-shadow: rgb(174, 174, 174) 0px 0px 10px;
}
.tips {
  position: absolute;
  top:10px;
  right:40px;
}
</style>
