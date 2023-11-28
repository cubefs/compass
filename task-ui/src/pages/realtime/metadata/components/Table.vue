<script setup lang="ts">
import { ElMessage } from 'element-plus'
import Copy from '~/components/Copy.vue'
import { post } from '~/utils/request'
const { t } = useI18n()
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
  { label: t('common.projectName'), prop: 'projectName', copy: true },
  { label: t('common.flowName'), prop: 'flowName', copy: true },
  { label: t('common.taskName'), prop: 'taskName', copy: true },
  { label: t('common.jobName'), prop: 'jobName', copy: true },
  { label: t('common.flinkTrackUrl'), prop: 'flinkTrackUrl', copy: true },
  { label: t('common.startTime'), prop: 'startTime' },
  { label: t('common.creator'), prop: 'username' },
  { label: t('common.taskState'), prop: 'taskState' },
  // { label: '', prop: '' },
])
const findColor = (value: String) => {
  const result = props.colorMap.find(item => item.name === value)
  return result ? result.color : '#2dccc3'
}
const delMeta = async (row) => {
  await post('/api/flink/deleteMetadata', row)
  ElMessage.success(t('common.success'))
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
      <el-table-column :key="123" :label="t('common.operate')">
        <template #default="scope">
        <span>
          <el-button type="primary" text @click="delMeta(scope.row)">
            {{ $t('common.delete') }}
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


