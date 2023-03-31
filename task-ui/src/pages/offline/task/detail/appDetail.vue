<script  setup lang='ts'>
import ItemWrapper from '~/pages/offline/application/detail/components/ItemWrapper.vue'
import ErrorTable from '~/pages/offline/application/detail/components/ErrorTable.vue'
import Chart from '~/pages/offline/application/detail/components/AnalyzeChart.vue'
import CpuChart from '~/pages/offline/application/detail/components/CpuChart.vue'
const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})
const selectAppList = $ref([])
props.data.forEach((item, index) => {
  selectAppList[index] = item[0].id
})
function findSelected(item, index) {
  return item.find(seleted => seleted.id === selectAppList[index])
}
function handleApp(id) {
  selectAppList.forEach((item, index) => {
    selectAppList[index] = id
  })
}
defineExpose({
  handleApp,
})
</script>

<template>
  <div v-for="(item, index) in data" :key="index" m-t-5 p-l-5 p-r-5>
    <ItemWrapper
      v-if="findSelected(item, index)?.name"
      :title="findSelected(item, index)?.name"
      :conclusion="findSelected(item, index)?.conclusion"
      :margin-top="false"
    >
      <template #select>
        <el-select v-model="selectAppList[index]" style="width:350px">
          <el-option
            v-for="app in item"
            :key="app.id"
            :label="app.id"
            :value="app.id"
          />
        </el-select>
      </template>
      <Chart v-if="findSelected(item, index)?.type === 'chart' || findSelected(item, index)?.type === 'memoryChart'" :data="findSelected(item, index)?.item.chartList[0]" />
      <ErrorTable v-if="findSelected(item, index)?.type === 'table'" :data="findSelected(item, index)?.item.table" />
      <CpuChart v-if="findSelected(item, index)?.type === 'cpuChart'" :data="findSelected(item, index)?.item" />
    </ItemWrapper>
  </div>
</template>

<style scoped lang='scss'>
.detail-aside-content {
  margin-top: 20px;
}
</style>
