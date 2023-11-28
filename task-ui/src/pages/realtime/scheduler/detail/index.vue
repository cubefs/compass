<script setup lang="ts">
import ItemWrapper from './components/ItemWrapper.vue'
import BaseInfo from './components/BaseInfo.vue'
import ErrorTable from './components/ErrorTable.vue'
import Chart from './components/AnalyzeChart.vue'
import CpuChart from './components/CpuChart.vue'
import RealtimeChart from './components/RealtimeChart.vue'
import { get, post } from '~/utils/request'
import { cloudTheme } from '~/utils/setting'
let detailInfo: any = $ref({
  runTimeAnalyze: [],
})
let realtimeDiagnosis: any = $ref({})
let reportsProp: any = $ref([])
const route = useRoute()
const memoryData = computed(() => {
  return detailInfo?.resourcesAnalyze[1]
})
const cpuData = computed(() => {
  return detailInfo?.resourcesAnalyze[0]
})
// interface RuleChart{
//   type:String
// }
interface Report {
  IDiagnosisRuleCharts: [String]
  conclusion: string
  title: string
}
onBeforeMount(async () => {
  // const res = await Promise.all([
  //   post(`/api/flink/getReport`, { "id": `${route.query.id}` }),
  //   // get(`/api/v1/app/report/runError?applicationId=${route.query.applicationId}`),
  //   // get(`/api/v1/app/report/runInfo?applicationId=${route.query.applicationId}`),
  //   // get(`/api/v1/app/report/runResource?applicationId=${route.query.applicationId}`),
  //   // get(`/api/v1/app/report/runTime?applicationId=${route.query.applicationId}`),
  // ])
  // console.log(res[0])
  let pr = post(`/api/flink/getReport`, { "id": `${route.query.id}` })
  pr.then(res => {
    realtimeDiagnosis = res
  })
  await pr
  console.log(realtimeDiagnosis)
  let reports: [any] = []
  for (var index in realtimeDiagnosis.reports) {
    console.log(realtimeDiagnosis.reports[index])
    let r1 = JSON.parse(realtimeDiagnosis.reports[index])
    console.log(r1)
    reports.push(r1)
  }
  console.log(realtimeDiagnosis)
  console.log(reports)
  for (var r of reports) {
    reportsProp.push(r)
  }
  // detailInfo = {
  //   runErrorAnalyze: res[0].filter((item: any) => item.item),
  //   runInfo: res[1],
  //   resourcesAnalyze: res[2],
  //   runTimeAnalyze: res[3].filter((item: any) => item.item),
  // }
})
</script>

<template>
  <el-card>
    <p class="detail-title">
      {{ $t('application.applicationDetails') }}
    </p>
    <div v-if="realtimeDiagnosis.flinkTaskDiagnosis">
      <ItemWrapper :title="$t('common.diagnosisType')" class="m-b-5">
        <div>
          <span v-for="(item, index) in realtimeDiagnosis.flinkTaskDiagnosis.diagnosisTypes" :key="item"
            class="category-card" :title="item" :style="{ 'border-left': `3px solid ${cloudTheme[index]}` }">
            {{ item }}
          </span>
        </div>
      </ItemWrapper>
      <BaseInfo :info="realtimeDiagnosis.flinkTaskDiagnosis" />
      <ItemWrapper v-for="(item, index) in reportsProp" :key="item.title" :title="item.title"
        :conclusion="{ conclusion: item.conclusion }">
        <RealtimeChart :dataInput="item" :width-list="[120, 180, 180, '', 270]" />
      </ItemWrapper>
      <!-- <ItemWrapper v-for="(item, index) in detailInfo.runErrorAnalyze" :key="item.name" :title="item.name" :conclusion="item.conclusion">
                  <ErrorTable :data="item.item.table" :width-list="[120, 180, 180, '', 270]" />
                </ItemWrapper>
                <ItemWrapper v-if="memoryData.item" :title="memoryData.name" :conclusion="memoryData.conclusion" :gc-info="memoryData.item?.computeNodeList">
                  <Chart :data="memoryData.item.chartList[0]" />
                </ItemWrapper>
                <ItemWrapper :title="cpuData.name" :conclusion="cpuData.conclusion">
                  <CpuChart :data="cpuData.item" />
                </ItemWrapper>
                <ItemWrapper v-for="(item, index) in detailInfo.runTimeAnalyze" :key="item.name" :title="item.name" :conclusion="item.conclusion">
                  <div v-if="item.type === 'chart'">
                    <Chart v-for="(itemC, indexC) in item.item.chartList" :key="indexC" :index="`${index}-${indexC}`" :data="itemC" />
                  </div>
                  <div v-if="item.type === 'table'">
                    <ErrorTable :data="item.item.table" />
                  </div>
                </ItemWrapper> -->
    </div>
  </el-card>
</template>

<style lang="scss" scoped>
.detail-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  margin-top: 10px;
  margin-bottom: 30px;
}
</style>

<route lang="yaml">
meta:
  name: application
name: realtime-taskDetail
</route>
