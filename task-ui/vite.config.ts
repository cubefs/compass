import path from 'path'
import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'
import Pages from 'vite-plugin-pages'
import generateSitemap from 'vite-ssg-sitemap'
import Layouts from 'vite-plugin-vue-layouts'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Inspect from 'vite-plugin-inspect'
import Unocss from 'unocss/vite'

function getAppUI(): string {
  var index = process.argv.indexOf('--ui');
  if (index < 0) {
    return 'all'
  }
  var apps = []
  for (var i = index + 1; i < process.argv.length; i++) {
    var v = process.argv[i]
    if (v.startsWith('--')) {
      break
    }
    apps.push(v)
  }
  if (apps.length == 0) {
    return 'all'
  }
  return apps.join(',')
}

export default defineConfig(() => {
  var apps = getAppUI() // flink, spark, ...
  console.log("build ui for " + apps)

  return {
    define: {
      '__APP_UI__': JSON.stringify(apps)
    },
    resolve: {
      alias: {
        '~/': `${path.resolve(__dirname, 'src')}/`,
      },
    },
    base: './',
    plugins: [
      Vue({
        include: [/\.vue$/, /\.md$/],
        reactivityTransform: true,
      }),

      Pages({
        extensions: ['vue', 'md'],
      }),

      Layouts(),

      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'vue-i18n',
          'vue/macros',
          '@vueuse/head',
          '@vueuse/core',
        ],
        dts: 'src/auto-imports.d.ts',
        dirs: [
          'src/composables',
          'src/store',
        ],
        vueTemplate: true,
      }),

      Components({
        extensions: ['vue', 'md'],
        include: [/\.vue$/, /\.vue\?vue/, /\.md$/],
        dts: 'src/components.d.ts',
      }),

      Unocss(),

      Inspect(),
    ],

    test: {
      include: ['test/**/*.test.ts'],
      environment: 'jsdom',
      deps: {
        inline: ['@vue', '@vueuse', 'vue-demi'],
      },
    },

    ssgOptions: {
      script: 'async',
      formatting: 'minify',
      onFinished() { generateSitemap() },
    },
}})
