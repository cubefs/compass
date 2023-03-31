<script setup lang="ts">
import GcDialog from './GcDialog.vue'
const props = defineProps({
  gcInfo: {
    type: Array,
    default: () => {
      return []
    },
  },
})
const { query: { applicationId } } = useRoute()
const refDialog: any = ref(null)
const openDialog = (id) => {
  refDialog.value.init(applicationId, id)
}
</script>

<template>
  <div class="gc-footer">
    <div class="gc-title">
      <p>GC分析</p>
      <p>(TOP 10)</p>
    </div>
    <div p-3>
      <span
        v-for="item in gcInfo"
        :key="item.executorId"
        class="gc-item"
      >
        <span>[{{ item.nodeType }}]</span>
        <span class="gc-link" @click="openDialog(item.executorId)">{{ item.hostName }}</span>
      </span>
    </div>
    <GcDialog ref="refDialog" />
  </div>
</template>

<style lang="scss" scoped>
.gc-footer {
  display: flex;
  min-height: 100px;
}
.gc-title {
  min-width: 130px;
  border-right: 1px solid #d7d7d7;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.gc-item {
  display: inline-block;
  margin: 5px;
  width: 255px;
}
.gc-link {
  color: #23cccd;
  cursor: pointer;
  white-space: nowrap;
  display: inline-block;
  vertical-align: bottom;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
