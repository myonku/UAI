<template>
  <div class="unauthorized-page">
    <h1>Unauthorized Access</h1>
    <p>You do not have permission to view this page.</p>
    <el-button type="primary" @click="$router.push({ name: 'home' })">Go to Home</el-button>
  </div>
</template>