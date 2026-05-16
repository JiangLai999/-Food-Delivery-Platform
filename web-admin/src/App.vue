<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  console.log('秒送餐管理后台已启动')
})
</script>

<style>
@import './styles/brand.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  position: relative;
}
</style>
