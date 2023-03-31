<script>
export default {
  name: 'ExpandBlock',
  props: {
    value: {
      type: String,
      required: true,
    },
    expandType: {
      type: String,
      default: 'icon',
      validator(value) {
        return ['click', 'icon'].includes(value)
      },
    },
  },

  data() {
    return {
      showAll: false,
      isShowAllEnable: false,
      lastClick: {
        clientX: 0,
        clientY: 0,
      },
    }
  },
  computed: {},
  watch: {
    value() {
      this.$nextTick(this.computedshowAll)
    },
  },

  mounted() {
    this.computedshowAll()
  },
  methods: {
    spanClick() {
      this.showAll = !this.showAll
    },
    computedshowAll() {
      const innerHeight = this.$refs.inner.getBoundingClientRect().height
      if (innerHeight > 120)
        this.isShowAllEnable = true
      else
        this.isShowAllEnable = false
    },
    mousedown(e) {
      if (this.expandType !== 'click')
        return
      const { clientX, clientY } = e
      this.lastClick = { clientX, clientY }
    },
    mouseup(e) {
      if (this.expandType !== 'click')
        return
      const { clientX, clientY } = e
      if (clientX === this.lastClick.clientX && clientY === this.lastClick.clientY)
        this.spanClick()
    },
  },
}
</script>

<template>
  <div ref="container" class="expand-container" :class="{ 'expand-disable': !showAll }">
    <span ref="inner" :class="{ 'cursor-pointer': expandType === 'click' }" @mousedown="mousedown" @mouseup="mouseup" v-html="value" />
    <span class="footer-icon cursor-pointer" :class="{ 'footer-icon-before': !showAll }" @click="spanClick">
      {{ !showAll ? '展开' : '收起' }}
    </span>
  </div>
</template>

<style lang="scss" scoped>
  .expand-disable {
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    display: -webkit-box;
  }
  .cursor-pointer {
    cursor: pointer;
  }
  .expand-container {
    position: relative;
    .footer-icon {
      position: absolute;
      bottom: 0px;
      right: 0;
      padding-left: 5px;
      color: #2dccc3;
      background-color: white;
      transition: background-color .25s ease;
      opacity: 0.5;
    }
  }
  .el-table--enable-row-hover .el-table__body tr:hover>td .expand-container .footer-icon {
    background-color: #eef0f7;
  }
</style>
