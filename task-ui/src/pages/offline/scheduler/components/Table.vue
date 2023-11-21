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
  { label: t('common.flowName'), prop: 'flowName', copy: true },
  { label: t('common.taskName'), prop: 'taskName', copy: true },
  { label: t('common.projectName'), prop: 'projectName' },
  { label: t('common.executionDate'), prop: 'executionDate', width: 200 },
  { label: t('common.duration'), prop: 'duration' },
  { label: t('common.resource'), prop: 'resource' },
  // { label: '处理状态', prop: 'taskStatus' },
  { label: t('common.creator'), prop: 'users' },
  { label: t('common.otherInfo'), prop: 'others' },
])
const findColor = (value: String) => {
  const result = props.colorMap.find(item => item.name === value)
  return result ? result.color : '#2dccc3'
}
const addWhite = async (row) => {
  await post('/api/v1/blocklist/add', {
    flowName: row.flowName,
    projectName: row.projectName,
    taskName: row.taskName,
  })
  ElMessage.success(t('diagnosis.addSuccess'))
}
const handleState = async (row) => {
  if (row.taskStatus === 1)
    return
  await post('/api/v1/job/updateState', {
    flowName: row.flowName,
    projectName: row.projectName,
    taskName: row.taskName,
    executionDate: row.executionDate,
    tryNumber: row.tryNumber,
  }, {
    confirmTips: '是否将该记录及其之前记录设置为已处理？',
  })
  ElMessage.success(t('common.success'))
  emit('search')
}
const goReport = (row) => {
  const routeData = router.resolve({
    name: 'taskDetail',
    query: {
      flowName: row.flowName,
      projectName: row.projectName,
      taskName: row.taskName,
      executionDate: row.executionDate,
    },
  })
  window.open(routeData.href, '_blank');
}
</script>

<template>
  <el-card shadow="never">
    <el-table :data="data" default-expand-all>
      <el-table-column
        v-for="item in tableInfo"
        :key="item.label"
        :label="item.label"
        :prop="item.prop"
        :width="item.width || ''"
        show-overflow-tooltip
      >
        <template #default="scope">
          <span v-if="item.copy"><Copy :value="scope.row[item.prop]" style="margin:15px 5px 15px 0px;" />{{ scope.row[item.prop] }}</span>
          <span v-else-if="item.label === '处理状态'"><el-tag class="pointer" :type="scope.row.taskStatus === 1 ? '' : 'success'" @click="handleState(scope.row)">{{ scope.row.taskStatus === 1 ? '已处理' : '未处理' }}</el-tag></span>
          <span v-else-if="item.label === t('common.otherInfo')">{{ scope.row.others.length ? scope.row.others.join(',') : '-' }}</span>
          <span v-else>{{ scope.row[item.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column type="expand">
        <template #default="scope">
          <div class="bottom-bar">
            <div>
              <span>{{ $t('common.diagnosisType') }}：</span>
              <span
                v-for="item in scope.row.categories"
                :key="item"
                class="category-card"
                :title="item"
                :style="{ 'border-left': `3px solid ${findColor(item)}` }"
              >
                {{ item }}
              </span>
            </div>
            <div>
              <el-button type="primary" text @click="addWhite(scope.row)">
                {{ $t('common.addBlocklist') }}
              </el-button>
              <el-button type="primary" text @click="goReport(scope.row)">
                {{ $t('common.viewDetails') }}
              </el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style lang="scss" scoped>
  :deep() .el-table td, .el-table th {
    padding: 6px 2px;
  }
  :deep() .el-table tbody td:last-child .cell {
    visibility: hidden;
  }
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
