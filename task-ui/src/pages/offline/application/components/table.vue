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
const router = useRouter()
const tableInfo = $ref([
  { label: 'applicationID', prop: 'applicationId', width: 300, copy: true },
  { label: '工作流', prop: 'flowName', copy: true },
  { label: '实例', prop: 'taskName', copy: true },
  { label: '执行周期', prop: 'executionDate' },
  { label: '运行耗时', prop: 'duration' },
  { label: '资源消耗', prop: 'resource' },
  { label: '创建人', prop: 'users' },
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
  ElMessage.success('添加成功')
}
const goReport = (row) => {
  router.push({
    name: 'appDetail',
    query: {
      applicationId: row.applicationId,
    },
  })
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
          <span v-else>{{ scope.row[item.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column type="expand">
        <template #default="scope">
          <div class="bottom-bar">
            <div>
              <span>诊断类型：</span>
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
              <el-button v-if="scope.row.applicationId" type="primary" text @click="addWhite(scope.row)">
                添加白名单
              </el-button>
              <el-button v-if="scope.row.applicationId" type="primary" text @click="goReport(scope.row)">
                查看详情
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
