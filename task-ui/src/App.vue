<script setup lang="ts">
// elementplus中文包
import zh_CN from 'element-plus/lib/locale/lang/zh-cn'
import en_US from 'element-plus/lib/locale/lang/en'
import Default from '~/layouts/default.vue'
import { useLocaleStore } from '~/store/locale.ts'
useHead({
  title: 'Compass',
})
const localeStore = useLocaleStore()

onMounted(() => {
  document.body.style.setProperty('--el-color-primary', '#00bfbf')
  document.body.style.setProperty('--el-color-primary-light-9', '#eaf9f5')
  document.body.style.setProperty('--el-color-primary-light-3', '#03d5d5')
})
</script>

<template>
  <el-config-provider :locale="localeStore.locale === 'zh_CN' ? zh_CN : en_US" :key="localeStore.locale">
    <Default style="font-family: auto;" />
  </el-config-provider>
</template>

