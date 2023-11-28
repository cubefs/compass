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
      {{ $t('application.basicInfo') }}
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">{{ $t('common.applicationId') }}: &nbsp;{{ info.taskInfo?.applicationId || '-' }}</span>
        <span class="text">{{ $t('common.flowName') }}: &nbsp;{{ info.taskInfo?.flowName || '-' }}</span>
        <span class="text">{{ $t('common.taskName') }}: &nbsp;{{ info.taskInfo?.taskName || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">{{ $t('common.duration') }}: &nbsp;{{ info.taskInfo?.appTime || '-' }}</span>
        <span class="text">{{ $t('common.memorySeconds') }}: &nbsp;{{ info.taskInfo?.memorySeconds || '-' }}</span>
        <span class="text">{{ $t('common.vcoreSeconds') }}: &nbsp;{{ info.taskInfo?.vcoreSeconds || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      {{ $t('application.clusterInfo') }}
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">{{ $t('application.clusterName') }}: &nbsp;{{ info.clusterInfo?.clusterName || '-' }}</span>
        <span class="text">{{ $t('application.queue') }}: &nbsp;{{ info.clusterInfo?.executeQueue || '-' }}</span>
        <span class="text">{{ $t('application.user') }}: &nbsp;{{ info.clusterInfo?.executeUser || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      {{ $t('application.environment') }}
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

