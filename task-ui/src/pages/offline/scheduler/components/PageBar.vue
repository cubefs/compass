<script setup lang="ts">
const props = defineProps({
  pageSizes: {
    type: Array,
    default: () => [10, 20, 30, 50],
  },
  pageQuery: {
    type: Object,
    default: () => ({
      page: 1,
      pageSize: 10,
    }),
  },
  total: {
    type: Number,
    default: 20,
  },
})
const emit = defineEmits(['change'])
const handleSizeChange = (value: number) => {
  emit('change', { page: props.pageQuery.page, pageSize: value })
}
const handleCurrentChange = (value: number) => {
  emit('change', { page: value, pageSize: props.pageQuery.pageSize })
}
</script>

<template>
  <div>
    <el-pagination
      v-model:currentPage="pageQuery.page"
      v-model:page-size="pageQuery.pageSize"
      style="float:right;margin:10px"
      :page-sizes="pageSizes"
      layout="total, prev, pager, next, sizes"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>
