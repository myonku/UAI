<script setup lang="ts">

</script>

<template>
  <div class="form-register">
    <el-form ref="registerForm" :model="{ username, password, email, confirmPassword }" :rules="rules" @submit.prevent>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="username" placeholder="请输入用户名" @blur="checkUsernameAvailability"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input type="password" v-model="confirmPassword" placeholder="请再次输入密码"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="email" placeholder="请输入邮箱"></el-input>
      </el-form-item>
      <div class="text-center">
        <el-button type="primary" @click.prevent="$refs.registerForm.validate()">注册</el-button>
      </div>
    </el-form>
  </div>
</template>

<style scoped>

</style>