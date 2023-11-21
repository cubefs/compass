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
</script>

<template>
  <div>
    <div class="item-title">
      {{ $t('application.basicInfo') }}
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">{{ $t('common.applicationId') }}: &nbsp;{{ info?.applicationId || '-' }}</span>
        <span class="text">{{ $t('common.flowName') }}: &nbsp;{{ info?.flowName || '-' }}</span>
        <span class="text">{{ $t('common.taskName') }}: &nbsp;{{ info?.taskName || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">{{ $t('common.duration') }}: &nbsp;{{ info?.duration || '-' }}</span>
        <span class="text">{{ $t('common.memCost') }}: &nbsp;{{ info?.memCost || '-' }}</span>
        <span class="text">{{ $t('common.resourceCost') }}: &nbsp;{{ info?.resourceCost || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">{{ $t('common.jobName') }}: &nbsp;{{ info?.jobName || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      {{ $t('application.clusterInfo') }}
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">{{ $t('application.clusterName') }}: &nbsp;{{ info?.clusterName || '-' }}</span>
        <span class="text">{{ $t('application.queue') }}: &nbsp;{{ info?.queue || '-' }}</span>
        <span class="text">{{ $t('application.user') }}: &nbsp;{{ info?.username || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      {{ $t('application.environment') }}
    </div>
    <div style="border: 1px solid #d7d7d7; border-top: none;">
      <div class="text-group">
        <span class="text">{{ $t('application.parallel') }}: &nbsp;{{ info?.parallel || '-' }}</span>
        <span class="text">Tm Slot: &nbsp;{{ info?.tmSlot || '-' }}</span>
        <span class="text">Tm Core: &nbsp;{{ info?.tmCore || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">Tm Memory: &nbsp;{{ info?.tmMem || '-' }}</span>
        <span class="text">Jm Memory: &nbsp;{{ info?.jmMem || '-' }}</span>
        <span class="text">{{ $t('application.tmNum') }}: &nbsp;{{ info?.tmNum || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      {{ $t('application.diagnosisParameters') }}
    </div>
    <div style="border: 1px solid #d7d7d7; border-top: none;">
      <div class="text-group">
        <span class="text">{{ $t('application.diagnosisStartTime') }}: &nbsp;{{ info?.diagnosisStartTime || '-' }}</span>
        <span class="text">{{ $t('application.diagnosisEndTime') }}: &nbsp;{{ info?.diagnosisEndTime || '-' }}</span>
        <span class="text">{{ $t('application.diagnosisParallel') }}: &nbsp;{{ info?.diagnosisParallel || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">{{ $t('application.diagnosisTmMemory') }}: &nbsp;{{ info?.diagnosisTmMemory+'MB' || '-' }}</span>
        <span class="text">{{ $t('application.diagnosisTmSlotNum') }}: &nbsp;{{ info?.diagnosisTmSlotNum || '-' }}</span>
        <span class="text">{{ $t('application.diagnosisTmCoreNum') }}: &nbsp;{{ info?.diagnosisTmCoreNum || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">{{ $t('application.diagnosisJmMemory') }}: &nbsp;{{ info?.diagnosisJmMemory+'MB' || '-' }}</span>
      </div>
      <div class="item-title">
        {{ $t('application.resourceAdvice') }}
      </div>
      <div class="text-group">
        <span class="text">{{ info?.resourceAdvice || '-' }}</span>
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
  white-space: pre-wrap;
}
</style>

