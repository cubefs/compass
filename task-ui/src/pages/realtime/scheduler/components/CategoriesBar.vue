<script setup lang="ts">
const props = defineProps({
  categories: {
    type: Array,
    default: () => [],
  },
})
const { t } = useI18n()
const labelText =t('common.diagnosisType') + ':'
const emit = defineEmits(['change'])
const selectTag = (item) => {
  item.selected = !item.selected
  emit('change')
}
</script>

<template>
  <el-form label-width="85px">
    <el-form-item :label="labelText">
      <div>
        <el-tag
          v-for="item in categories"
          :key="item"
          style="margin:5px; cursor: pointer"
          :style="item.selected ? { color: 'white' } : {}"
          effect="plain"
          size="large"
          :color="item.selected ? '#00bfbf' : ''"
          round
          @click="selectTag(item)"
        >
          {{ item.name }}
        </el-tag>
      </div>
    </el-form-item>
  </el-form>
</template>
