<template>
  <el-form class="form-register" style="margin-top: 30px;" ref="registrationForm">
    <div class="text-center mb-1">
      <el-image class="mb-4 img image_wrapper" src="/sd.jpg" alt="" height="158" />
      <h1 class="h2 mb-3 font-weight-normal"><strong>用户注册</strong></h1>
      <p><small>在此注册初始账户，需要向管理人员申请更多权限。</small></p>
      <p><small>若已有账号，可<router-link class="tce" to="/login">前往登录</router-link></small></p>
    </div>

    <el-form-item label="用户名" prop="username" :rules="usernameRules" label-position="left" label-width="80"
                  size="large">
      <el-input type="text" v-model="username" placeholder="请输入用户名" ref="usernameInput" />
    </el-form-item>

    <el-form-item label="邮箱（可选）" prop="email" size="large">
      <el-input type="text" v-model="email" placeholder="请输入邮箱" ref="emailInput" />
    </el-form-item>

    <el-form-item label="密码" prop="password" :rules="passwordRules" label-position="left" label-width="80"
                  size="large">
      <el-input type="password" v-model="password" placeholder="请输入密码" ref="passwordInput" show-password />
    </el-form-item>

    <el-form-item label="确认密码" prop="confirmPassword" :rules="confirmPasswordRules" label-position="left"
                  label-width="80" size="large">
      <el-input type="password" v-model="confirmPassword" placeholder="请确认密码" ref="confirmPasswordInput"
                show-password />
    </el-form-item>

    <el-form-item style="margin-left: 0;">
      <div style="text-align: center; width: 100%;">
        <el-button type="primary" @click="register">注册</el-button>
      </div>
    </el-form-item>

    <p class="mt-3 mb-3 text-muted text-center"><small>若忘记账户密码，请<router-link class="tce" :to="{ name: 'login' }">前往验证</router-link>，或联系管理员：ctre@gmail.com</small></p>
    <p class="mt-3 mb-3 text-muted text-center" v-loading.fullscreen.lock="this.fullscreenLoading"><small>© 2025 @UAI</small></p>
  </el-form>
</template>

<style>
.form-register {
  padding-top: 6vh;
  width: 480px;
  margin: auto;
}

.text-center {
  text-align: center;
}
.image_wrapper{
  width: auto;
  height: 158px;
}
</style>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { useRouter } from 'vue-router';

const fullscreenLoading = ref(false);
const username = ref('');
const password = ref('');
const email = ref('');
const confirmPassword = ref('');
const usernameInput = ref(null);

const router = useRouter();

const checkUsernameAvailability = async (rule, value, callback) => {
  if (username.value.length < 3 || username.value.length > 12) {
    return callback(new Error('用户名长度应在3到12个字符'));
  } else {
    try {
      let params = new URLSearchParams();
      params.append('name', username.value);
      if (email.value) params.append('email', email.value);
      const response = await axios.post('/api/auth/checkRegister', params);
      const data = response.data;
      if (data === 'success') {
        callback();
      } else {
        callback(new Error('用户名或邮箱已被使用'));
      }
    } catch (error) {
      console.error('检查用户名可用性时出错:', error);
      callback(new Error('无法检查用户数据，请稍后重试'));
    }
  }
};

const passwordValidator = (rule, value, callback) => {
  if (password.value.length < 8 || password.value.length > 16) {
    callback(new Error('密码长度应在8到16个字符'));
  } else {
    callback();
  }
};

const confirmPasswordValidator = (rule, value, callback) => {
  if (confirmPassword.value !== password.value) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const usernameRules = [
  { validator: checkUsernameAvailability, trigger: 'blur' },
];
const passwordRules = [
  { validator: passwordValidator, trigger: 'blur' },
];
const confirmPasswordRules = [
  { validator: confirmPasswordValidator, trigger: 'blur' },
];

const register = async () => {
  fullscreenLoading.value = true;
  const valid =
      username.value.length >= 3 &&
      username.value.length <= 12 &&
      password.value.length >= 8 &&
      password.value.length <= 16;
  if (!valid) {
    fullscreenLoading.value = false;
    ElNotification({
      title: '检查错误',
      message: '有一些字段似乎没有被正确填写，请检查后再试',
      type: 'error',
      position: 'bottom-right',
    });
    return;
  }
  try {
    const user = {
      username: username.value,
      password: password.value,
      email: email.value,
    };
    const response = await axios.post('/api/auth/register', user, {
      headers: { 'Content-Type': 'application/json' },
    });
    if (response.data === 'success') {
      fullscreenLoading.value = false;
      ElNotification({
        title: '系统消息',
        message: '注册成功！即将前往登录',
        type: 'success',
        position: 'bottom-right',
      });
      setTimeout(() => {
        router.push({ name: 'login' });
      }, 600);
    }
  } catch (err) {
    fullscreenLoading.value = false;
    ElNotification({
      title: '系统消息',
      message: '注册失败，请稍后再试',
      type: 'error',
      position: 'bottom-right',
    });
  }
};

onMounted(() => {
  usernameInput.value && usernameInput.value.focus();
});
</script>