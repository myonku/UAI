<template>
    <div style="height: 100%;width: 100%;overflow: hidden">

        <el-page-header @back="goBack">
            <template #content>
                <div class="flex items-center">
                    <el-image style="height: 32px;margin-right: 10px;" class="mr-3" src="/sd.png" />
                </div>
            </template>
            <template #extra>
                <el-switch style="margin-right: 16px;" v-model="isDark" inline-prompt @change="customToggleDark">
                    <template #active-action>
                        <el-icon>
                            <Moon />
                        </el-icon>
                    </template>
                    <template #inactive-action>
                        <el-icon>
                            <Sunny />
                        </el-icon>
                    </template>
                </el-switch>
                <el-button style="" v-if="mobile" @click="drawer = true"><el-icon>
                        <Menu />
                    </el-icon>
                </el-button>
                <el-dropdown placement="bottom-end" style="margin-left: 10px;">
                    <el-avatar class="mr-3" :size="32" :src="`http://127.0.0.1:8080${currentAvatar}`" />
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item disabled>————</el-dropdown-item>
                            <el-dropdown-item>
                                <div style="height: 100%;width: 100%;text-align: center;" @click="user">个人信息</div>
                            </el-dropdown-item>
                            <el-dropdown-item>
                                <div style="height: 100%;width: 100%;text-align: center;" @click="logout">退出登录</div>
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>

            </template>
        </el-page-header>

        <el-drawer v-if="mobile" v-model="drawer" title="" :with-header="false" direction="ltr" size="80%">
            <el-menu :default-active="currentRoute" style="height: 80%;" :router="true">
                <el-menu-item index="1" :route="{ name: 'home' }" @click="clickRoute('1')">
                    <el-icon>
                        <House />
                    </el-icon>
                    <template #title>主页</template>
                </el-menu-item>

                <el-menu-item index="2" :route="{ name: 'user' }" @click="clickRoute('2')">
                    <el-icon>
                        <User />
                    </el-icon>
                    <template #title>个人</template>
                </el-menu-item>

                <el-menu-item index="3" :route="{ name: 'username' }" @click="clickRoute('3')">
                    <el-icon>
                        <UserFilled />
                    </el-icon>
                    <template #title>教师</template>
                </el-menu-item>

                <el-menu-item index="4" :route="{ name: 'admin' }" @click="clickRoute('4')">
                    <el-icon>
                        <Avatar />
                    </el-icon>
                    <template #title>管理员</template>
                </el-menu-item>
            </el-menu>
        </el-drawer>

        <el-container style="height: 100%;margin-right: 6px">
            <!-- 左侧 -->
            <el-aside v-if="!mobile" :style="{ width: isCollapse ? '63.2px' : '200px' }" style="position: relative;">
                <el-menu :default-active="currentRoute" class="el-menu-vertical-demo" :collapse="isCollapse"
                    :router="true">
                    <el-menu-item index="1" :route="{ name: 'home' }" @click="clickRoute('1')">
                        <el-icon>
                            <House />
                        </el-icon>
                        <template #title>主页</template>
                    </el-menu-item>

                    <el-menu-item index="2" :route="{ name: 'user' }" @click="clickRoute('2')">
                        <el-icon>
                            <User />
                        </el-icon>
                        <template #title>个人</template>
                    </el-menu-item>

                    <el-menu-item index="3" :route="{ name: 'username' }" @click="clickRoute('3')">
                        <el-icon>
                            <UserFilled />
                        </el-icon>
                        <template #title>教师</template>
                    </el-menu-item>

                    <el-menu-item index="4" :route="{ name: 'admin' }" @click="clickRoute('4')">
                        <el-icon>
                            <Avatar />
                        </el-icon>
                        <template #title>管理员</template>
                    </el-menu-item>

                    <hr>
                    <el-menu-item @click="handleCollapse">
                        <el-icon>
                            <template v-if="!isCollapse">
                                <Fold />
                            </template>
                            <template v-else>
                                <Expand />
                            </template>
                        </el-icon>
                        <template #title>{{ isCollapse ? '展开' : '收起' }}</template>
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <!-- 主体 -->
            <el-main :style="{ transition: '0.2s' }" style="overflow-y: auto;height: 100%;position: relative;">
                <router-view v-slot="{ Component }">
                    <component ref="child" :is="Component"></component>
                </router-view>
            </el-main>
        </el-container>

    </div>

</template>

<style>
@import 'element-plus/theme-chalk/dark/css-vars.css';

.el-drawer .drawer-content {
    overflow: hidden;
    /* 禁止滚动 */
}

.el-main {
    padding: 28px;
}

.el-page-header {
    padding: 10px;
    margin-left: 12px;
    margin-right: 12px;
    border-bottom: 1px solid lightgray;
}

.el-drawer {
    height: 100%;
    overflow: hidden;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 200px;
    min-height: 400px;
}

.el-menu {
    height: 100%;
}

.el-aside {
    transition: width 0.3s;
    /* 侧边栏宽度的过渡动画 */
    height: 100%;
    overflow: hidden;
}
</style>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useDark } from "@vueuse/core";
import { useRoute, useRouter } from 'vue-router';
import { ElNotification } from 'element-plus';
import { Avatar, Expand, Fold, House, Moon, Sunny, User, UserFilled } from "@element-plus/icons-vue";
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const isDark = useDark();
const drawer = ref(false)
const mobile = ref(false)   //是否为小屏幕
const isCollapse = ref(false)    //调整侧边栏
const screenWidth = ref(window.innerWidth)
const currentAvatar = ref('')
const currentName = ref('')
//
const child = ref()


const handleResize = () => {
    screenWidth.value = window.innerWidth
}

const currentRoute = computed(() => {
    if (route.name === 'home') return '1';
    else if (route.name === 'user') return '2';
    else if (route.name === 'username') return '3';
    else if (route.name === 'admin') return '4';
    return ''; // 默认空，如果没有匹配的
});

onMounted(async () => {
    window.addEventListener("resize", handleResize)
    mobile.value = screenWidth.value < 992; // 根据尺寸初始化isMobile
    const notificationMessage = route.query.notification;
    await getUserInfo();
    if (notificationMessage) {
        // 显示通知
        ElNotification({
            title: '系统消息',
            message: "欢迎，" + notificationMessage,
            type: 'success',
            position: 'bottom-right',
        });
    }
})

//监听屏幕宽度的变化，进行侧边栏的收缩和展开
watch(screenWidth, (newValue, oldValue) => {
    mobile.value = newValue < 992;
})

const handleCollapse = () => {    //调整侧边导航
    isCollapse.value = !isCollapse.value;
}

const clickRoute = (x) => {
    drawer.value = false;
    currentRoute.value = x
}

const goBack = () => {
    if (window.history.length <= 1) {
        router.push({ name: 'home' });
    } else {
        router.go(-1);
    }
}

const getUserInfo = async () => {
    const token = sessionStorage.getItem('token'); // 从 sessionStorage 中获取 token
    if (!token) {
        console.log("Token is required.");
        return;
    }
    try {
        const response = await axios.get('/api/user/userInfo', {
            headers: {
                'Content-Type': 'application/json',
                'token': token,
            }
        });
        const data = response.data;
        // 更新状态
        currentName.value = data.username ?? "未知"; // 处理空值
        currentAvatar.value = data.avatarPath ?? "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"; // 处理空值
    } catch (error) {
        console.log("Error:", error);
    }
}

const user = () => {
    router.push({ name: 'user' });
}

const logout = () => {
    // 清除存储在 sessionStorage 中的 token
    sessionStorage.removeItem('token');
    // 跳转到登录页面
    router.push({ name: 'login', query: { notification: currentName.value } });
};
</script>