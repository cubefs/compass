<script setup lang="ts">
import ExpandBlock from '~/components/ExpandBlock.vue'
const props = defineProps({
  data: {
    type: Object,
    default: () => {
      return {
        data: [],
        titles: {},
      }
    },
  },
  widthList: {
    type: Array,
    default: () => {
      return []
    },
  },
})
const label = computed(() => {
  return {
    label: Object.values(props.data.titles),
    value: Object.keys(props.data.titles),
  }
})
</script>

<template>
  <el-table :data="data.data" style="width: 100%" :height="data.data ? 250 : 100 ">
    <el-table-column v-for="(item, index) in label.label" :key="index" :prop="label.value[index]" :label="label.label[index]" :width="widthList[index]">
      <template #default="scope">
        <ExpandBlock v-if="label.value[index] === 'logContent'" :value="scope.row.logContent" />
        <span v-else-if="label.value[index] === 'advice'" v-html="scope.row.advice" />
        <span v-else>{{ scope.row[label.value[index]] }}</span>
      </template>
    </el-table-column>
  </el-table>
</template>
