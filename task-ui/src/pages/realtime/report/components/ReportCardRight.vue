<script setup lang="ts">
const props = defineProps({
  ratio: {
    type: Number,
    default: 0,
  },
  chainRatio: {
    type: Number,
    default: 0,
  },
  dayOnDay: {
    type: Number,
    default: 0,
  },
  unit: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="report-card-right">
    <div class="right-first">
      占比
    </div>
    <div class="right-second">
      {{ (ratio * 100).toFixed(2) }}%
    </div>
    <div class="right-third">
      <div flex="~" flex-col items-center p-r-3>
        <div :style="{ color: chainRatio >= 0 ? '#ec808d' : '#73a596' }">
          {{ chainRatio >= 0 ? '↑' : '↓' }}&nbsp;{{ (chainRatio * 100).toFixed(2) }}%
        </div>
        <div style="color:#7f7f7f">
          环比
        </div>
      </div>
      <div flex="~" flex-col items-center p-l-3>
        <div :style="{ color: dayOnDay >= 0 ? '#ec808d' : '#73a596' }">
          {{ dayOnDay >= 0 ? '↑' : '↓' }}&nbsp;{{ (dayOnDay * 100).toFixed(2) }}%
        </div>
        <div style="color:#7f7f7f">
          同比
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.report-card-right {
  width: 50%;
  padding: 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  .right-first {
    margin-top: 10px;
  }
  .right-second {
    font-size: 32px;
    font-weight: bold;
  }
  .right-third {
    font-size: 14px;
    margin-top: 8px;
    display: flex;
  }
}
</style>
