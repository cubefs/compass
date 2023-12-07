<script lang="ts" setup>
import Login from '../pages/login/index.vue'
import {
  Expand,
  Fold,
  User,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import reportLogo from '~/access/icon/report.png'
import diagnosisLogo from '~/access/icon/diagnosis.png'
import taskLogo from '~/access/icon/task.png'
import appLogo from '~/access/icon/application.png'
import whiteLogo from '~/access/icon/whitelist.png'
import logo from '~/access/icon/logo.png'
import { useStore } from '~/store/user'
import { useLocaleStore } from '~/store/locale'
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
const { t } = useI18n()
const currentRoute = useRoute()
const router = useRouter()
const store = useStore()
const localeStore = useLocaleStore()
let activeRoute: string = $ref('offline')
let currentLocale: string = $ref(localeStore.locale)
locale.value = currentLocale

if(currentRoute.path.indexOf('realtime')!=-1){
  activeRoute = 'realtime'
}

// Only show the ui of spark or flink
let showUI: boolean = true
if (__APP_UI__) {
  if (__APP_UI__ == 'spark') {
    activeRoute = 'offline'
    showUI = false
  } else if (__APP_UI__ == 'flink') {
    activeRoute = 'realtime'
    showUI = false
  }
}

const handleSelect = (index: string) => {
  if (activeRoute !== index) {
    activeRoute = index
    router.push({
      path: `/${activeRoute}/report`,
    })
  }
}
let isCollapse = $ref(true)
let asideWidth = $ref('63px')
function handleScalin() {
  isCollapse = !isCollapse
  if (asideWidth === '63px') {
    setTimeout(() => {
      asideWidth = '170px'
    }, 300)
  }
  else {
    setTimeout(() => {
      asideWidth = '63px'
    }, 300)
  }
}
const logout = async () => {
  localStorage.removeItem('username')
  localStorage.removeItem('token')
  store.updateUser('')
  ElMessage.success(t('menu.logoutSuccess'))
  router.push({
    name: 'login',
  })
}
const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      logout()
      break
  }
}
const handleI18n = (command: string) => {
  switch (command) {
    case 'toggle':
      currentLocale = currentLocale === 'zh_CN' ? 'en_US' : 'zh_CN'
      locale.value = currentLocale
      console.log(locale.value)
      localeStore.updateLocale(locale.value)
      localStorage.setItem('language', locale.value)
      break
  }

}
const getHeight = () => {
  return `${window.screen.availHeight - 200}px`
}
</script>

<template>
  <Login v-if="!store.username"/>
  <el-container v-else>
    <el-header>
      <el-menu
        class="header"
        mode="horizontal"
        active-text-color="#00bfbf"
        :default-active="activeRoute"
        :ellipsis="false"
        @select="handleSelect"
      >
        <el-image class="title-logo" :src="logo" />
        <div class="title-space" />
        <el-menu-item index="offline" v-if="showUI">
          <template #title>
            {{ $t('menu.offline') }}
          </template>
        </el-menu-item>
        <el-menu-item index="realtime" v-if="showUI">
          <template #title>
            {{ $t('menu.realtime') }}
          </template>
        </el-menu-item>
        <div class="flex-grow" />
        <el-dropdown class="user-box" @command="handleI18n">
          <span style="display: flex;align-items: center; margin-right: 20px">
            {{ currentLocale === 'zh_CN' ? '中文' : 'English' }}
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu style="width:100px">
              <el-dropdown-item command="toggle">
                {{ currentLocale === 'zh_CN' ? 'English' : '中文' }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown class="user-box" @command="handleCommand">
          <span
            style="display: flex;align-items: center; margin-right: 10px"
          >
            <el-icon style="font-size:19px; margin-right:8px"><User /></el-icon>{{ store.username }}
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu style="width:100px">
              <el-dropdown-item command="logout">
                {{ $t('menu.logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-menu>
    </el-header>
    <el-container>
      <el-aside :width="asideWidth" style="position:absolute;z-index: 5;">
        <el-menu
          mode="vertical"
          active-text-color="#00bfbf"
          :collapse="isCollapse"
          router
          style="height: 100vh"
          :default-active="currentRoute.path"
          class="menu"
        >
          <el-menu-item :index="`/${activeRoute}/report`">
            <el-icon><el-image class="logo" :src="reportLogo" /></el-icon>
            <template #title>
              {{ $t('menu.reportOverview') }}
            </template>
          </el-menu-item>
          <el-menu-item :index="`/${activeRoute}/diagnosis`">
            <el-icon><el-image class="logo" :src="diagnosisLogo" /></el-icon>
            <template #title>
              {{ $t('menu.oneClick') }}
            </template>
          </el-menu-item>
          <el-menu-item :index="`/${activeRoute}/scheduler`">
            <el-icon><el-image class="logo" :src="taskLogo" /></el-icon>
            <template #title>
              {{ $t('menu.schedulerTask') }}
            </template>
          </el-menu-item>
          <el-menu-item v-if="`${activeRoute}`=='offline'" :index="`/${activeRoute}/application`">
            <el-icon><el-image class="logo" :src="appLogo" /></el-icon>
            <template #title>
              {{ $t('menu.computingTask') }}
            </template>
          </el-menu-item>
          <el-menu-item v-if="`${activeRoute}`=='realtime'" :index="`/${activeRoute}/metadata`">
            <el-icon><el-image class="logo" :src="appLogo" /></el-icon>
            <template #title>
              {{ $t('menu.metadata') }}
            </template>
          </el-menu-item>
          <el-menu-item :index="`/${activeRoute}/white`">
            <el-icon><el-image class="logo" :src="whiteLogo" /></el-icon>
            <template #title>
              {{ $t('menu.blocklist') }}
            </template>
          </el-menu-item>
          <el-tooltip
            effect="dark"
            :content="isCollapse ? t('menu.open') : t('menu.close')"
            placement="top-start"
          >
            <el-button class="scalin-btn" :link="true" :icon="isCollapse ? Expand : Fold" @click="handleScalin" />
          </el-tooltip>
        </el-menu>
      </el-aside>
      <div style="width:63px" />
      <el-main>
        <el-scrollbar :height="getHeight()">
          <div style="padding-left: 20px;">
            <router-view />
          </div>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.container {
  position: relative;
}
.scalin-btn {
  position: absolute;
  left: 15px;
  bottom: 65px;
  font-size: 24px;
  color: #978f8f;
}

.logo {
  width: 23px;
  height: 23px;
}

.title-logo {
  width: 40px;
  height: 40px;
  margin-left: 12px;
  margin-top: 9px;
  margin-bottom: 9px;
}

.title-space {
  width: 2.5%;
}

.menu {
  :deep(.is-active){
    background-color: #f2f2f2 !important;
    border-right: 2px solid #00bfbf;
  }
}
.header {
  :deep(.is-active){
    border:none;
    border-top: 2px solid #00bfbf !important;
  }
}
.title-item {
  display: flex;
  align-items: center;
}
:deep(.el-menu--horizontal) {
    margin-left: -21px;
    margin-right: -21px;
}
:deep(.el-header) {
    z-index: 1;
}





</style>
