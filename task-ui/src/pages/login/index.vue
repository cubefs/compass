<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import { post } from '~/utils/request'
import { useStore } from '~/store/user.ts'
import logo from '~/access/icon/logo2.png'
const { t } = useI18n()
const store = useStore()
const form = reactive({
  username: '',
  password: '',
  // schedulerType: '',
})
const canLogin = computed(() => {
  return !!form.username && !!form.password
})
const router = useRouter()
const onSubmit = async () => {
  const res = await post('/user/login', form)
  localStorage.setItem('token', res.token)
  localStorage.setItem('username', res.username)
  store.updateUser(res.username)
  router.push({
    name: 'report'
  })
  setTimeout(() => {
    location.reload()
  }, 100)
}
</script>

<template>
  <div class="content">
    <div class="login">
      <!-- <el-image class="title-logo" :src="logo" /> -->
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.username" :rows="10" :placeholder="t('login.username')" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password :placeholder="t('login.password')" />
        </el-form-item>
      </el-form>
      <el-button type="primary" :class="{disabled: !canLogin}" style="width:100%;height:40px;font-size: 16px;font-weight: bold;margin-top: 20px;" :disabled="!canLogin" @click="onSubmit">
       {{ $t('login.login')}}
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.content {
  background: white;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-content: center;
  font-family: auto;
}
.login {
  margin-top: 200px;
  width: 400px;
  height: 230px;
}
.title-logo {
  width: 275px;
  text-align: center;
  margin-left: 62px;
}
:deep(.el-input__inner) {
  height: 40px;
  font-size: 16px;
}
:deep(.el-button--primary){
  background:#00bfbf !important;
}
</style>

<style>
input:-webkit-autofill,

input:-webkit-autofill:hover,

input:-webkit-autofill:focus,

input:-webkit-autofill:active {
  -webkit-transition: "color 9999s ease-out, background-color 9999s ease-out";
  -webkit-transition-delay: 9999s;
}
</style>
