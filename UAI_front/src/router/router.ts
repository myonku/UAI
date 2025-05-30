import { createRouter, createWebHashHistory } from "vue-router";
import axios from "axios";
import login from "@/components/pages/login.vue"
import register from "@/components/pages/register.vue"
import home from "@/components/pages/homepage.vue"
import teacher from "@/components/pages/teacherpage.vue"
import admin from "@/components/pages/adminpage.vue"
import user from "@/components/pages/account.vue"
import unauthorized from "@/components/pages/unauthorized.vue"

const router = createRouter({

    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            redirect: {name: "login"}
        },
        {
            name: "login",
            path: "/login",
            component: login
        },
        {
            name: "register",
            path: "/register",
            component: register
        },
        {
            name: "main",
            path: "/main",
            component: () => import('@/components/main/layout.vue'),
            redirect: { name: "home" },
            meta: { requiresAuth: true},// 需要授权
            children: [
                {
                    path: "home",
                    name: "home",
                    component: home,
                    meta: { requiresAuth: true },
                },
                {
                    path: "user",
                    name: "user",
                    component: user,
                    meta: { requiresAuth: true },
                },
                {
                    path: "teacher",
                    name: "teacher",
                    component: teacher,
                    meta: { requiresAuth: true, requiresStaff: true }, // 需要 staff 权限
                },
                {
                    path: "unauthorized",
                    name: "unauthorized",
                    component: unauthorized,
                },
                {
                    path: "admin",
                    name: "admin",
                    component: admin,
                    meta: { requiresAuth: true, requiresStaff: true, requiresSAdmin: true }, // 需要 admin 权限
                },
                { path: '', redirect: { name: 'home' } }
            ]
        },
        { path: '/:catchAll(.*)', redirect: '/login' } // 处理未知路由
    ]
});

// 全局路由守卫
router.beforeEach((to, _from, next) => {
    const token = to.query.token || sessionStorage.getItem('token'); // 从 sessionStorage 中获取 token
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!token) {
            next({ name: 'login' }); // 如果没有 token，重定向到登录页面
        } else {
            sessionStorage.setItem('token', token.toString()); // 存储 token
            axios.post('/api/auth/verify_token', { token }).then((response: any) => {
                const role = response.data;
                if (role) {
                    // 需要 staff 权限的路由 meta.requiresStaff === true
                    if (to.matched.some(record => record.meta.requiresStaff)) {
                        if (role === 'staff' || role === 'admin') {
                            if (to.matched.some(record => record.meta.requiresSAdmin)) {
                                if (role === 'admin') {
                                    next(); // admin 角色放行
                                } else {
                                    next({ name: 'unauthorized' }); // 非 admin 无权限
                                }
                            }
                            next(); // staff 角色放行
                        } else {
                            next({ name: 'unauthorized' }); // 非 staff 无权限
                        }
                    } else {
                        next(); // 不需要 staff 权限的路由放行
                    }
                } else {
                    next({ name: 'unauthorized' }); // 验证失败
                }
            })
                .catch(() => {
                    next({ name: 'login' }); // 请求失败重定向
                });
        }
    } else {
        next(); // 不需要验证的路由直接放行
    }
});

export default router