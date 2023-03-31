<script setup lang="ts">
import Tabs from '../application/components/ChartsTab.vue'
import Table from './components/Table.vue'
import Categories from './components/CategoriesBar.vue'
import PageBar from './components/PageBar.vue'
import { get, post } from '~/utils/request'
import { cloudTheme } from '~/utils/setting'
const formInline: any = $ref({
  projectName: '',
  flowName: '',
  taskName: '',
  username: '',
  time: [],
})
const searchInfo = $ref([
  { label: '项目：', value: 'projectName' },
  { label: '工作流：', value: 'flowName' },
  { label: '实例：', value: 'taskName' },
  { label: '创建人：', value: 'username' },
])
const tableData = $ref({
  data: [],
  count: 0,
})
const categories = $ref({
  data: [],
})
const pageQuery = $ref({
  page: 1,
  pageSize: 10,
})
let tabType: string = $ref('memory')
let chartData = $ref([])
let unit = $ref('')
const getChart = async (params: any, type: string) => {
  tabType = type
  let res: any
  switch (type) {
    case 'memory':
      res = await post('/api/v1/job/graph', {
        ...params,
        graphType: 'memoryTrend',
      })
      break
    case 'cpu':
      res = await post('/api/v1/job/graph', {
        ...params,
        graphType: 'cpuTrend',
      })
      break
    case 'num':
      res = await post('/api/v1/job/graph', {
        ...params,
        graphType: 'numTrend',
      })
      break
  }
  chartData = res.data
  unit = res.unit || ''
}
const tabChange = (type: string) => {
  const params = getParams()
  getChart(params, type)
}
const getParams = () => {
  const params = {
    ...formInline,
    ...pageQuery,
    categories: getSelect(),
  }
  if (params.time.length) {
    params.start = params.time[0]
    params.end = params.time[1]
  }
  delete params.time
  return params
}
const getCategory = async () => {
  const res = await get('/api/v1/job/categories')
  categories.data = res.map((item: any, index: number) => ({
    name: item,
    selected: false,
    color: cloudTheme[index],
  }))
}
const search = () => {
  const params = getParams()
  getTableData(params)
  getChart(params, tabType)
}
const getTableData = async (params: any) => {
  const res = await post('/api/v1/job/list', params)
  tableData.data = res.jobInfos
  tableData.count = res.count
}
// 获取被选择的标签
const getSelect = () => {
  return categories.data.reduce((acc, now) => {
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
  getCategory()
  search()
})
</script>

<template>
  <el-card class="card">
    <Tabs :data="chartData" :unit="unit" @tab-change="tabChange" />
    <el-form :inline="true" :model="formInline">
      <el-form-item v-for="item in searchInfo" :key="item.value" :label="item.label">
        <el-input v-model="formInline[item.value]" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="时间：">
        <el-date-picker
          v-model="formInline.time"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="x"
          @change="search"
        />
      </el-form-item>
    </el-form>
    <Categories :categories="categories.data" @change="selectChange" />
    <Table :data="tableData.data" :color-map="categories.data" @search="search" />
    <PageBar :total="tableData.count" :page-query="pageQuery" @change="pageChange" />
  </el-card>
</template>

<style lang="scss" scoped>
.card{
  overflow-y:auto;
 }
</style>

<route lang="yaml">
meta:
  title: 任务运行
  name: task
</route>
