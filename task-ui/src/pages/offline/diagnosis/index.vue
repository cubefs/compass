<script setup lang="ts">
import { ElMessage } from 'element-plus'
import Process from './ProcessDialog.vue'
import { cloudTheme } from '~/utils/setting'
const applicationId: string = $ref('')
const refDialog: any = ref(null)
const router = useRouter()
const tableColumn = [
  { label: 'application_id', value: 'applicationId' },
  { label: '工作流', value: 'flowName' },
  { label: '实例', value: 'taskName' },
  { label: '执行周期', value: 'executionDate' },
  { label: '创建人', value: 'users' },
]
let taskAppInfo: any = $ref({})
async function submit() {
  if (!applicationId)
    return ElMessage.warning('请输入applicationId')
  refDialog.value.init(applicationId)
}
function searchComplete(info: any) {
  taskAppInfo = info
}
const goReport = () => {
  router.push({
    name: 'appDetail',
    query: {
      applicationId: taskAppInfo.applicationId,
    },
  })
}
</script>

<template>
  <Process ref="refDialog" @search-complete="searchComplete" />
  <el-card h-300>
    <div flex="~" justify-center flex-col items-center>
      <div h-40 />
      <div flex="~" w-160 m-b-10>
        <el-input v-model="applicationId" placeholder="请输入spark/mapreduce application id">
          <template #append>
            <el-button @click="submit">
              一键诊断
            </el-button>
          </template>
        </el-input>
      </div>
      <div>
        <div class="result-title" w-270>
          任务信息
        </div>
      </div>
      <el-card shadow="never" w-270>
        <el-table v-if="Object.keys(taskAppInfo).length !== 0" :data="[taskAppInfo]" style="width: 100%">
          <el-table-column v-for="item in tableColumn" :key="item.value" :prop="item.value" :label="item.label" show-overflow-tooltip />
        </el-table>
      </el-card>
      <div m-t-10>
        <div class="result-title" w-270>
          诊断结果
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
            <span style="font-size:15px" m-r-10>运行耗时：{{ taskAppInfo.duration }}</span>
            <span style="font-size:15px">资源消耗：{{ taskAppInfo.resource }}</span>
            <el-button text type="primary" style="float:right" @click="goReport">
              点击查看诊断报告
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
