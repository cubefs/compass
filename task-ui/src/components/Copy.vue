<script setup lang="ts">
import {
  DocumentCopy,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
const { t } = useI18n()
const props = defineProps({
  value: {
    type: String,
    default: '',
  },
  config: {
    type: Object,
    default: () => {
      return {
        successMessage: 'Success',
        title: 'Copy',
      }
    },
  },
})
const contentCopy = () => {
  const input = document.createElement('input')
  input.value = props.value || window.location.href
  input.style = 'position: fixed;top: 0;z-index: -1'
  document.body.appendChild(input)
  input.focus()
  input.setSelectionRange(0, input.value.length)
  const bool = document.execCommand('copy')
  document.body.removeChild(input)
  if (bool)
    ElMessage.success(t('common.success'))
}
</script>

<template>
  <el-icon style="cursor: pointer" :title="config.title" @click="contentCopy">
    <DocumentCopy />
  </el-icon>
</template>

