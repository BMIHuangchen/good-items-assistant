<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { ensureLogin } from './utils/api'

onLaunch(() => {
  ensureLogin().catch((error) => {
    console.warn('Mini login skipped', error)
  })
})

</script>

<style>
/* 全局样式 */
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 重置样式 */
view, text, image, navigator, button, input, textarea, scroll-view {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
}

/* 安全区域适配 */
.safe-area-inset-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
