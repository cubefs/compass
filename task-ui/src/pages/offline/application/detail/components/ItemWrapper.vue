<script setup lang="ts">
import GcAnalyze from './GcAnalyze.vue'

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
  conclusion: {
    type: Object,
    default: () => ({
      conclusion: '',
      conclusionDesc: '',
    }),
  },
  gcInfo: {
    type: Array,
    default: () => {
      return []
    },
  },
  marginTop: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <div :class="{ 'm-t-5': marginTop }" style=" width:100%; height:100%; overflow:hidden;">
    <div class="item-title">
      {{ title }}
      <slot name="select" class="item-select" />
    </div>
    <div class="item-content">
      <slot />
    </div>
    <!-- <div v-if="gcInfo && gcInfo.length" class="item-footer">
      <GcAnalyze :gc-info="gcInfo" />
    </div> -->
    <div v-if="conclusion && conclusion.conclusion" class="item-footer">
      <div class="item-footer-title">
        分析结论：
        <el-tooltip placement="top">
          <template  #content>
            <div v-html="conclusion.conclusionDesc"></div>
          </template >
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="item-footer-content" v-html="conclusion.conclusion" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.item-title {
  padding: 8px;
  font-size: 16px;
  font-weight: 700;
  background: #f2f2f2;
  border: 1px solid #d7d7d7;
  border-left: 3px solid #00bfbf;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-content {
  border-left: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  padding:5px
}
.item-footer {
  border:1px solid #d7d7d7;
  border-top: none;
  min-height: 40px;
  display: flex;
  .item-footer-title {
    border-right: 1px solid #d7d7d7;
    min-width: 130px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .item-footer-content {
    width:1200px;
    display: inline;
    align-items: center;
    margin: 10px;
    font-size: 14px;
  }
}
</style>
