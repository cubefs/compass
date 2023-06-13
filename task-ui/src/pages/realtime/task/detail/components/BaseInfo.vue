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
      基本信息
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">applicationID: &nbsp;{{ info?.applicationId || '-' }}</span>
        <span class="text">任务流: &nbsp;{{ info?.flowName || '-' }}</span>
        <span class="text">实例: &nbsp;{{ info?.taskName || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">运行耗时: &nbsp;{{ info?.timeCost || '-' }}</span>
        <span class="text">{{ `内存消耗: &nbsp;${info?.memCost}` || '-' }}</span>
        <span class="text">{{ `CPU消耗: &nbsp;${info?.resourceCost}` || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      集群信息
    </div>
    <div style="border-left: 1px solid #d7d7d7; border-right: 1px solid #d7d7d7;">
      <div class="text-group">
        <span class="text">集群名称: &nbsp;{{ info?.clusterName || '-' }}</span>
        <span class="text">执行队列: &nbsp;{{ info?.queue || '-' }}</span>
        <span class="text">执行用户: &nbsp;{{ info?.executeUser || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      运行参数
    </div>
    <div style="border: 1px solid #d7d7d7; border-top: none;">
      <div class="text-group">
        <span class="text">并行度: &nbsp;{{ info?.parallel || '-' }}</span>
        <span class="text">Tm Slot: &nbsp;{{ info?.tmSlot || '-' }}</span>
        <span class="text">Tm Core: &nbsp;{{ info?.tmCore || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">Tm Memory: &nbsp;{{ info?.tmMem || '-' }}</span>
        <span class="text">Jm Memory: &nbsp;{{ info?.jmMem || '-' }}</span>
        <span class="text">Tm 个数: &nbsp;{{ info?.tmNum || '-' }}</span>
      </div>
    </div>
    <div class="item-title">
      诊断参数
    </div>
    <div style="border: 1px solid #d7d7d7; border-top: none;">
      <div class="text-group">
        <span class="text">诊断开始时间: &nbsp;{{ info?.diagnosisStartTime || '-' }}</span>
        <span class="text">诊断结束时间: &nbsp;{{ info?.diagnosisEndTime || '-' }}</span>
        <span class="text">建议并行度: &nbsp;{{ info?.diagnosisParallel || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">建议Tm Mem: &nbsp;{{ info?.diagnosisTmMemSize+'MB' || '-' }}</span>
        <span class="text">建议Tm Slot: &nbsp;{{ info?.diagnosisTmSlotNum || '-' }}</span>
        <span class="text">建议Tm Core: &nbsp;{{ info?.diagnosisTmCoreNum || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">建议Jm Mem: &nbsp;{{ info?.diagnosisJmMemSize+'MB' || '-' }}</span>
      </div>
      <div class="item-title">
        资源建议
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

