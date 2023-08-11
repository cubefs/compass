<script setup lang="ts">
import { ElMessage } from 'element-plus'
import Copy from '~/components/Copy.vue'
import { post } from '~/utils/request'
const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
  colorMap: {
    type: Array,
    default: () => [],
  },
})
const emit = defineEmits(['search'])
const router = useRouter()
const tableInfo = $ref([
  { label: '项目', prop: 'projectName', copy: true },
  { label: '工作流', prop: 'flowName', copy: true },
  { label: '实例', prop: 'taskName', copy: true },
  { label: '作业名', prop: 'jobName', copy: true },
  { label: 'Tracking Url', prop: 'flinkTrackUrl', copy: true },
  { label: '运行开始时间', prop: 'startTime' },
  { label: '创建人', prop: 'username' },
  { label: '状态', prop: 'taskState' },
  // { label: '', prop: '' },
])
const findColor = (value: String) => {
  const result = props.colorMap.find(item => item.name === value)
  return result ? result.color : '#2dccc3'
}
const delMeta = async (row) => {
  await post('/api/realtime/taskDiagnosis/deleteMetadata', row)
  ElMessage.success('删除成功')
  emit('search')
}
</script>

<template>
  <el-card shadow="never">
    <el-table :data="data" default-expand-all>

      <el-table-column v-for="item in tableInfo" :key="item.label" :label="item.label" :prop="item.prop"
        :width="item.width || ''" :show-overflow-tooltip="true">
        <template #default="scope">
          <span v-if="item.copy">
            <Copy :value="scope.row[item.prop]" style="margin:15px 5px 15px 15px;" />{{ scope.row[item.prop] }}
          </span>
          <span v-else>{{ scope.row[item.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column :key="123" :label="'操作'">
        <template #default="scope">
        <span>
          <el-button type="primary" text @click="delMeta(scope.row)">
            删除
          </el-button>
        </span>
      </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style lang="scss" scoped>
:deep() .el-table td,
.el-table th {
  padding: 6px 2px;
}

// :deep() .el-table tbody td:last-child .cell {
//   visibility: hidden;
// }

:deep() .el-table tr td {
  border: none;
}

:deep() .el-table table {
  border-collapse: collapse;
}

:deep() .el-table tbody tr:nth-of-type(even) {
  border-bottom: 1px solid #e4e7ed;
}

.bottom-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin: 0 20px;
}
</style>


