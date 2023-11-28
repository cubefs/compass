<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import Copy from '~/components/Copy.vue'
import { post } from '~/utils/request'
import PageBar from '~/components/PageBar.vue'
const { t } = useI18n()
const { proxy } = getCurrentInstance()
let visible = $ref(false)
const formInline: any = $ref({
  flowName: '',
  projectName: '',
  taskName: '',
  component: 'realtime'
})
const addForm: any = $ref({
  flowName: '',
  projectName: '',
  taskName: '',
})
const searchInfo = $ref([
  { label: t('common.flowName'), value: 'flowName' },
  { label: t('common.taskName'), value: 'taskName' },
  { label: t('common.projectName'), value: 'projectName' },
])
const pageQuery = $ref({
  page: 1,
  pageSize: 10,
})
const tableData = $ref({
  data: [],
  count: 0,
})
let selectedRow = $ref([])
const rules = $ref<FormRules>({
  flowName: [
    { required: true, message: t('common.inputPlaceholder'), trigger: 'change' },
  ],
  projectName: [
    { required: true, message: t('common.inputPlaceholder'), trigger: 'change' },
  ],
  taskName: [
    { required: true, message: t('common.inputPlaceholder'), trigger: 'change' },
  ],
})
const search = async () => {
  const res = await post('/api/v1/blocklist/list', {
    ...formInline,
    ...pageQuery,
  })
  tableData.data = res.list
  tableData.count = res.total
}
const handleSelectionChange = (val: any) => {
  selectedRow = val
}
const pageChange = (query: any) => {
  pageQuery.pageSize = query.pageSize
  pageQuery.page = query.page
  search()
}
const submit = async (formName: string) => {
  await proxy.$refs[formName].validate()
  await post('/api/v1/blocklist/add', {
    ...addForm,
    component:'realtime'
  })
  ElMessage.success(t('common.success'))
  search()
  visible = false
}
const cancel = (formName: string) => {
  proxy.$refs[formName].resetFields()
  visible = false
}
const remove = async () => {
  if (!selectedRow.length)
    return ElMessage.warning(t('white.selectWarning'))
  await post('/api/v1/blocklist/del', {
    blocklistIds: selectedRow.map(item => item.id),
  }, {
    confirmTips: t('white.confirmTips'),
  })
  ElMessage.success(t('common.success'))
  search()
}
onMounted(() => {
  search()
})
</script>

<template>
  <el-dialog
    v-if="visible"
    v-model="visible"
    :title="$t('common.addBlocklist')"
    width="500px"
  >
    <el-form ref="formRef" :model="addForm" :rules="rules" label-width="30%">
      <el-form-item v-for="item in searchInfo" :key="item.value" :label="`${item.label}：`" :prop="item.value">
        <el-input v-model="addForm[item.value]" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancel('formRef')">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submit('formRef')">
          {{ $t('common.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
  <el-card>
    <div class="title">
      {{ $t('white.blocklist') }}
      <el-tooltip
        effect="dark"
        :content="$t('white.tooltip')"
        placement="top-start"
      >
        <el-icon><QuestionFilled /></el-icon>
      </el-tooltip>
    </div>
    <el-form :inline="true" :model="formInline">
      <el-form-item v-for="item in searchInfo" :key="item.value" :label="`${item.label}：`">
        <el-input v-model="formInline[item.value]" @keyup.enter="search" />
      </el-form-item>
    </el-form>
    <el-button type="primary" @click="remove">
      {{ $t('white.remove') }}
    </el-button>
    <el-button type="primary" @click="visible = true">
      {{ $t('white.add') }}
    </el-button>
    <el-card shadow="never" class="m-t-5">
      <el-table
        :data="tableData.data"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" />
        <el-table-column v-for="item in searchInfo" :key="item.value" :label="item.label">
          <template #default="scope">
            <span><Copy :value="scope.row[item.value]" style="margin:15px 5px 15px 0px;" />{{ scope.row[item.value] }}</span>
          </template>
        </el-table-column>
      </el-table>
      <PageBar :total="tableData.count" :page-query="pageQuery" @change="pageChange" />
    </el-card>
  </el-card>
</template>

<style lang="scss" scoped>
.title {
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
:deep(.el-button--primary){
  background:#00bfbf !important;
}
.el-input {
    width: 80%;
}
</style>

<route lang="yaml">
meta:
  title: 白名单
  name: white
name: realtime-white
</route>
