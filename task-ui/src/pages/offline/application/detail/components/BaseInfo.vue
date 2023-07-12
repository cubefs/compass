<script setup lang="ts">
import { get } from '~/utils/request'
const props = defineProps({
  info: {
    type: Object,
    defalult: () => {
      return {}
    },
  },
})

const groupedEnv = computed(() => {
  const entries = Object.entries(props.info.env)
  const groups = []
  for (let i = 0; i < entries.length; i += 3) {
    let group = entries.slice(i, i + 3).map(([key, value]) => `${key}: ${value}`)
    while (group.length < 3) {
      group.push('')
    }
    groups.push(group)
  }
  return groups
})

</script>

<template>
  <div>
    <div class="item-title">
      基本信息
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">applicationID: &nbsp;{{ info.taskInfo?.applicationId || '-' }}</span>
        <span class="text">任务流: &nbsp;{{ info.taskInfo?.flowName || '-' }}</span>
        <span class="text">实例: &nbsp;{{ info.taskInfo?.taskName || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">运行耗时: &nbsp;{{ info.taskInfo?.appTime || '-' }}</span>
        <span class="text">{{ `内存消耗: &nbsp;${info.taskInfo?.memorySeconds}` || '-' }}</span>
        <span class="text">{{ `CPU消耗: &nbsp;${info.taskInfo?.vcoreSeconds}` || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      集群信息
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">集群名称: &nbsp;{{ info.clusterInfo?.clusterName || '-' }}</span>
        <span class="text">执行队列: &nbsp;{{ info.clusterInfo?.executeQueue || '-' }}</span>
        <span class="text">执行用户: &nbsp;{{ info.clusterInfo?.executeUser || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      运行参数
    </div>
    <div style="border: 1px solid #d7d7d7; border-top: none;">
      <div class="text-group" v-for="(group, index) in groupedEnv" :key="index">
        <span class="text" v-for="item in group" :key="item" :title="item">{{ item || '-' }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.item-title {
  padding: 5px;
  font-size: 14px;
  font-weight: 600;
  background: #f2f2f2;
  border: 1px solid #d7d7d7;
  border-left: 3px solid #00bfbf;
  border-collapse: collapse;
}

.text-group {
  display: flex;
  border-collapse: collapse;
}

.text {
  flex: 1;
  padding: 5px;
  border: 1px solid #d7d7d7;
  border-top: none;
  border-right: none;
  border-collapse: collapse;
  margin: 0px 0px -1px -1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

