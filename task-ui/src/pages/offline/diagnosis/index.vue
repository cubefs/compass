<script setup lang="ts">
import { ElMessage } from 'element-plus'
import Process from './ProcessDialog.vue'
import { cloudTheme } from '~/utils/setting'
const applicationId: string = $ref('')
const refDialog: any = ref(null)
const router = useRouter()
const { t } = useI18n()
const tableColumn = [
  { label: t('common.applicationId'), value: 'applicationId' },
  { label: t('common.flowName'), value: 'flowName' },
  { label: t('common.taskName'), value: 'taskName' },
  { label: t('common.executionDate'), value: 'executionDate' },
  { label: t('common.creator'), value: 'users' },
]
let taskAppInfo: any = $ref({})
const placeholderText =t('common.inputPlaceholder') + 'spark/mapreduce application id'
async function submit() {
  if (!applicationId)
    return ElMessage.warning(t('common.inputPlaceholder') + 'applicationId')
  refDialog.value.init(applicationId)
}
function searchComplete(info: any) {
  taskAppInfo = info
}
const goReport = () => {
  const routeData = router.resolve({
    name: 'appDetail',
    query: {
      applicationId: taskAppInfo.applicationId,
    },
  })
  window.open(routeData.href, '_blank');
}
</script>

<template>
  <Process ref="refDialog" @search-complete="searchComplete" />
  <el-card h-300>
    <div flex="~" justify-center flex-col items-center>
      <div h-40 />
      <div flex="~" w-160 m-b-10>
        <el-input v-model="applicationId" :placeholder= "placeholderText">
          <template #append>
            <el-button @click="submit">
              {{ $t('diagnosis.oneClick') }}
            </el-button>
          </template>
        </el-input>
      </div>
      <div>
        <div class="result-title" w-270>
          {{ $t('diagnosis.applicationInfo') }}
        </div>
      </div>
      <el-card shadow="never" w-270>
        <el-table v-if="Object.keys(taskAppInfo).length !== 0" :data="[taskAppInfo]" style="width: 100%">
          <el-table-column v-for="item in tableColumn" :key="item.value" :prop="item.value" :label="item.label" show-overflow-tooltip />
        </el-table>
      </el-card>
      <div m-t-10>
        <div class="result-title" w-270>
          {{ $t('diagnosis.diagnosticResult') }}
        </div>
      </div>
      <el-card shadow="never" w-270>
        <div v-if="Object.keys(taskAppInfo).length !== 0">
          <div m-b-3>
            <span
              v-for="(category, c_index) in taskAppInfo.categories"
              :key="category"
              class="category-card"
              :title="category"
              :style="{ 'border-left': `5px solid ${cloudTheme[c_index]}` }"
            >
              {{ category }}
            </span>
          </div>
          <div>
            <span style="font-size:15px" m-r-10>{{ $t('common.duration') }}：{{ taskAppInfo.duration }}</span>
            <span style="font-size:15px" m-r-10>{{ $t('common.resource') }}：{{ taskAppInfo.resource }}</span>
            <span style="font-size:15px">{{ $t('diagnosis.state') }}：{{ taskAppInfo.taskAppState }}</span>
            <el-button text type="primary" style="float:right" @click="goReport">
              {{ $t('diagnosis.viewReport') }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </el-card>
</template>

<style lang="scss" scoped>
.result-title {
  float:left;
  font-size: 19px;
  color: #00bfbf;
  font-weight: bold;
}
.category-card {
  font-size: 20px;
  font-weight: bold;
  border: 2px solid #e0e0e0;
}
</style>

<route lang="yaml">
meta:
  title: 一键诊断
  name: diagnosis
</route>
