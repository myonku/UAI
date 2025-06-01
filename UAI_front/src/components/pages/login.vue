<template>

    <el-form class="form-signIn" style="margin-top: 30px;" v-on:submit.prevent="login" ref="registrationForm">
        <div class="text-center mb-1">
            <el-image class="mb-4 img image_wrapper" src="/sd.jpg" alt="" />
            <h1 class="h2 mb-3 font-weight-normal"><strong>欢迎！</strong></h1>
            <p><small>若还没有账号，请<router-link class="tce" to="/register">前往注册</router-link></small></p>
        </div>

        <el-form-item label="用户名" prop="user" :rules="usernameRules" label-position="left" label-width="60"
            size="large">
            <el-input v-model="user" placeholder="请输入用户名" ref="usernameInput" />
        </el-form-item>

        <el-form-item label="密码" prop="pass" :rules="passwordRules" label-position="left" label-width="60" size="large">
            <el-input type="password" v-model="pass" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item>
            <div style="width: 100%; text-align: right">
                <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
            </div>
        </el-form-item>

        <el-form-item style="margin-left: 0;">
            <div style="text-align: center; width: 100%;">
                <el-button type="primary" @click="login">登录</el-button>
            </div>
        </el-form-item>

        <p class="mt-3 mb-3 text-muted text-center"><small>若忘记账户密码，请<router-link class="tce"
                    :to="{ name: 'login' }">前往验证</router-link>，或联系管理员：leun@gmail.com</small></p>
        <p class="mt-3 mb-3 text-muted text-center" v-loading.fullscreen.lock="this.fullscreenLoading"><small>© 2025
                @UAI</small></p>
    </el-form>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElNotification } from 'element-plus';
import axios from 'axios';

const user = ref(localStorage.getItem('user') || '');
const pass = ref(localStorage.getItem('pass') || '');
const rememberMe = ref(!!(localStorage.getItem('user') && localStorage.getItem('pass')));
const fullscreenLoading = ref(false);
const usernameInput = ref(null);

const router = useRouter();
const route = useRoute();

const usernameRules = [
    {
        validator: (rule, value, callback) => {
            if (user.value.length < 3 || user.value.length > 12) {
                callback(new Error('用户名长度应在3到12个字符'));
            } else {
                callback();
            }
        },
        trigger: 'blur',
    },
];

const passwordRules = [
    {
        validator: (rule, value, callback) => {
            if (pass.value.length < 8 || pass.value.length > 16) {
                callback(new Error('密码长度应在8到16个字符'));
            } else {
                callback();
            }
        },
        trigger: 'blur',
    },
];

const handleCheckboxChange = () => {
    if (rememberMe.value) {
        localStorage.setItem('user', user.value);
        localStorage.setItem('pass', pass.value);
    } else {
        localStorage.removeItem('user');
        localStorage.removeItem('pass');
    }
};

const login = async () => {
    fullscreenLoading.value = true;
    const valid =
        user.value.length >= 3 &&
        user.value.length <= 12 &&
        pass.value.length >= 8 &&
        pass.value.length <= 16;
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
    handleCheckboxChange();
    try {
        const params = {
            username: user.value,
            password: pass.value,
        }
        const response = await axios.post('/api/auth/login', params, {
            headers: { 'Content-Type': 'application/json' }
        });
        fullscreenLoading.value = false;
        if (response.data === 'notfound') {
            ElNotification({
                title: '用户不存在',
                message: '用户名不存在，请检查后再试',
                type: 'error',
                position: 'bottom-right',
            });
        } else if (response.data === 'error') {
            ElNotification({
                title: '验证错误',
                message: '密码与用户名不匹配，请检查后再试',
                type: 'error',
                position: 'bottom-right',
            });
        } else if (typeof response.data === 'string' || typeof response.data === 'number') {
            // 登录成功，跳转主页并存储token
            sessionStorage.setItem('token', response.data);
            await router.push({ name: 'home', query: { notification: user.value } });
        } else {
            ElNotification({
                title: '未知响应',
                message: '服务器返回了未知的数据',
                type: 'error',
                position: 'bottom-right',
            });
        }
    } catch (error) {
        fullscreenLoading.value = false;
        ElNotification({
            title: '登录失败',
            message: '系统出现了一点小问题，请稍后再试',
            type: 'error',
            position: 'bottom-right',
        });
    }
};

onMounted(() => {
    usernameInput.value && usernameInput.value.focus();
    const notificationMessage = route.query.notification;
    if (notificationMessage) {
        ElNotification({
            title: '系统消息',
            message: '用户 ' + notificationMessage + ' 的登录信息已被注销',
            type: 'info',
            position: 'bottom-right',
        });
        router.replace({ name: route.name });
    }
});
</script>

<style>
.form-signIn {
    max-width: 480px;
    margin: auto;
    padding-top: 11vh;
}

.text-center {
    text-align: center;
}

.image_wrapper {
    height: 158px;
    width: auto;
}
</style>