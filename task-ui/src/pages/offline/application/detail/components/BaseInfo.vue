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
        <span class="text">applicationID: &nbsp;{{ info.taskInfo?.applicationId || '-' }}</span>
        <span class="text">任务流: &nbsp;{{ info.taskInfo?.flowName || '-' }}</span>
        <span class="text">实例: &nbsp;{{ info.taskInfo?.taskName || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">运行耗时: &nbsp;{{ info.taskInfo?.appTime || '-' }}</span>
        <span class="text">{{ `内存消耗:  &nbsp;${info.taskInfo?.memorySeconds}` || '-' }}</span>
        <span class="text">{{ `CPU消耗:  &nbsp;${info.taskInfo?.vcoreSeconds}` || '-' }}</span>
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
      <div class="text-group">
        <span class="text">spark.driver.memoryOverhead: &nbsp;{{ info.appInfo?.driverOverhead || '-' }}</span>
        <span class="text">spark.driver.memory: &nbsp;{{ info.appInfo?.driverMemory || '-' }}</span>
        <span class="text">spark.executor.memoryOverhead: &nbsp;{{ info.appInfo?.executorOverhead || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">spark.executor.memory: &nbsp;{{ info.appInfo?.executorMemory || '-' }}</span>
        <span class="text">spark.executor.cores: &nbsp;{{ info.appInfo?.executorCores || '-' }}</span>
        <span class="text">spark.dynamicAllocation.maxExecutors: &nbsp;{{ info.appInfo?.maxExecutors || '-' }}</span>
      </div>
      <div class="text-group">
        <span class="text">spark.default.parallelism: &nbsp;{{ info.appInfo?.parallelism || '-' }}</span>
        <span class="text">spark.sql.shuffle.partitions: &nbsp;{{ info.appInfo?.shufflePartitions || '-' }}</span>
        <span class="text" />
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
  margin : 0px 0px -1px -1px;
}
</style>

