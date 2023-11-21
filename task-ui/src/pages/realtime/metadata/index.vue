<script setup lang="ts">
import Tabs from '../application/components/ChartsTab.vue'
import Table from './components/Table.vue'
import PageBar from './components/PageBar.vue'
import { get, post } from '~/utils/request'
import { cloudTheme } from '~/utils/setting'
const { t } = useI18n()
const formInline: any = $ref({
  projectName: '',
  flowName: '',
  taskName: '',
  username: '',
  diagnosisRule: [],
  taskState: '',
  time: [],
})
const searchInfo = $ref([
  { label: t('common.projectName')+'：', value: 'projectName' },
  { label: t('common.flowName')+'：', value: 'flowName' },
  { label: t('common.taskName')+'：', value: 'taskName' },
  { label: t('common.jobName')+'：', value: 'jobName' },
  { label: t('common.creator')+'：', value: 'username' },
])
const tableData = $ref({
  data: [],
  count: 0,
})
const diagnosisRule = $ref({
  data: [],
})
const pageQuery = $ref({
  page: 1,
  pageSize: 10,
})
let tabType: string = $ref('memory')
let chartData = $ref([])
let unit = $ref('')
const getParams = () => {
  const params = {
    ...formInline,
    ...pageQuery,
  }
  if (params.time != null && params.time.length) {
    params.startTs = params.time[0] / 1000
    params.endTs = params.time[1] / 1000
  }
  delete params.time
  return params
}
const search = () => {
  const params = getParams()
  getTableData(params)
}
const getTableData = async (params: any) => {
  const res = await post('/api/flink/pageMetadata', params)
  tableData.data = res.list
  tableData.count = res.total
  console.log(res)
}
// 获取被选择的标签
const getSelect = () => {
  return diagnosisRule.data.reduce((acc, now) => {
    if (now.selected)
      acc.push(now.name)
    return acc
  }, [])
}
const selectChange = () => {
  search()
}
const pageChange = (query: any) => {
  pageQuery.pageSize = query.pageSize
  pageQuery.page = query.page
  search()
}
onMounted(() => {
  search()
})
</script>

<template>
  <el-card class="card">
    <el-form :inline="true" :model="formInline">
      <el-form-item v-for="item in searchInfo" :key="item.value" :label="item.label">
        <el-input v-model="formInline[item.value]" @keyup.enter="search" />
      </el-form-item>
      <el-form-item :label="t('common.taskState')">
        <el-select v-model="formInline.taskState" @change="search">
          <el-option :label="t('common.all')" value=""></el-option>
          <el-option label="RUNNING" value="RUNNING"></el-option>
          <el-option label="FINISHED" value="FINISHED"></el-option>
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="运行开始时间：">
        <el-date-picker v-model="formInline.time" type="datetimerange" range-separator="-" start-placeholder="开始时间"
          end-placeholder="结束时间" value-format="x" @change="search" />
      </el-form-item> -->
    </el-form>
    <Table :data="tableData.data" :color-map="diagnosisRule.data" @search="search" />
    <PageBar :total="tableData.count" :page-query="pageQuery" @change="pageChange" />
  </el-card>
</template>

<style lang="scss" scoped>
.card {
  overflow-y: auto;
}

.el-table .cell .el-tooltip {
  white-space: pre-wrap;
}
</style>

<route lang="yaml">
meta:
  title: 实时任务元数据
  name: metadata
</route>
