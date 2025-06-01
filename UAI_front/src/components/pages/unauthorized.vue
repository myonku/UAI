<template>
    <div class="unauthorized-page">
        <h1>无访问权限</h1>
        <p>当前账户的权限等级不足，禁止对该页面的访问。</p>
        <el-button type="primary" @click="$router.push({ name: 'home' })">回到首页</el-button>
    </div>
</template>