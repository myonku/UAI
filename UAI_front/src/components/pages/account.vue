<template>
    <div class="user-profile-container">
        <!-- 第一部分：基本信息展示 -->
        <el-descriptions title="基本信息" border>
            <el-descriptions-item :rowspan="2" :width="140" label="头像" align="center">
                <el-image style="width: 100px; height: 100px; border-radius: 8px;" :src="avatarPath || defaultAvatar"
                    fit="cover" />
            </el-descriptions-item>
            <el-descriptions-item label="用户名">{{ user.username || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ user.id }}</el-descriptions-item>
            <el-descriptions-item label="用户角色">
                <el-tag :type="roleTagType" size="small">{{ roleDisplayName }}</el-tag>
            </el-descriptions-item>
        </el-descriptions>

        <div style="height: 50px"></div>

        <el-descriptions border>
            <el-descriptions-item label="账户状态">
                <el-tag :type="statusTagType" size="small">{{ statusDisplayName }}</el-tag>
            </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮区域 -->
        <div class="action-buttons">
            <el-button type="primary" plain @click="dialogFormVisible = true">更新信息</el-button>
            <el-button type="success" plain @click="updateUserDetails">刷新数据</el-button>
            <el-button type="info" plain @click="showPasswordDialog = true">修改密码</el-button>
        </div>

        <!--基本信息更新表单 -->
        <el-dialog v-model="dialogFormVisible" title="更新基本信息" width="500px" align-center>
            <el-form :model="form" label-width="100px">
                <el-form-item label="用户名">
                    <el-input v-model="form.username" />
                </el-form-item>
                <el-form-item label="邮箱">
                    <el-input v-model="form.email" />
                </el-form-item>
                <el-form-item label="头像">
                    <div class="avatar-uploader">
                        <el-upload class="avatar-upload" action="#" :show-file-list="false"
                            :before-upload="beforeAvatarUpload" :http-request="handleAvatarUpload">
                            <img v-if="form.avatarPath" :src="form.avatarPath" class="avatar" alt="" />
                            <el-icon v-else class="avatar-uploader-icon">
                                <Plus />
                            </el-icon>
                        </el-upload>
                        <div class="upload-tip">
                            点击上传新头像 (JPG/PNG, 小于500KB)
                        </div>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogFormVisible = false">取消</el-button>
                <el-button type="primary" @click="updateUserInfo">保存更改</el-button>
            </template>
        </el-dialog>

        <!--密码修改表单 -->
        <el-dialog v-model="showPasswordDialog" title="修改密码" width="500px" align-center>
            <el-form :model="passwordForm" label-width="120px" :rules="passwordRules" ref="passwordFormRef">
                <el-form-item label="当前密码" prop="currentPassword">
                    <el-input v-model="passwordForm.currentPassword" type="password" show-password
                        placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password
                        placeholder="请确认新密码" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showPasswordDialog = false">取消</el-button>
                <el-button type="primary" @click="updatePassword">确认修改</el-button>
            </template>
        </el-dialog>

        <!-- 课程信息区域 -->
        <el-descriptions>
            <el-descriptions-item>
                <div class="course-info">
                    <h3>课程信息</h3>
                    <div class="stats">
                        <div class="stat-card">
                            <div class="stat-value">{{ user.courseNum }}</div>
                            <div class="stat-label">已选课程</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">{{ user.totalScore }}</div>
                            <div class="stat-label">已获学分</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-value">{{ user.noCreditCourseNum }}</div>
                            <div class="stat-label">在学课程</div>
                        </div>
                    </div>
                </div>
            </el-descriptions-item>
        </el-descriptions>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-overlay">
            <el-icon class="loading-icon">
                <Loading />
            </el-icon>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import { Plus, Loading } from '@element-plus/icons-vue';
import axios from 'axios';

const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 用户数据
const user = reactive({
    id: '',
    username: '',
    email: '',
    avatarPath: '',
    role: '',
    status: '',
    courseNum: 0,
    totalScore: 0,
    noCreditCourseNum: 0
});

// 基本信息表单
const form = reactive({
    username: '',
    email: '',
    avatarPath: ''
});

// 密码表单
const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
});

// UI状态
const dialogFormVisible = ref(false);
const showPasswordDialog = ref(false);
const loading = ref(false);

// 计算属性
const roleDisplayName = computed(() => {
    switch (user.role) {
        case 'student': return '学生';
        case 'staff': return '员工';
        case 'admin': return '管理员';
        case 'default': return '普通用户';
        default: return user.role || '未知';
    }
});

const roleTagType = computed(() => {
    switch (user.role) {
        case 'admin': return 'danger';
        case 'staff': return 'warning';
        case 'student': return 'success';
        default: return 'info';
    }
});

const statusDisplayName = computed(() => {
    switch (user.status) {
        case 'active': return '活跃';
        case 'inactive': return '未激活';
        case 'banned': return '已封禁';
        default: return user.status || '未知';
    }
});

const statusTagType = computed(() => {
    switch (user.status) {
        case 'active': return 'success';
        case 'inactive': return 'warning';
        case 'banned': return 'danger';
        default: return 'info';
    }
});

const passwordRules = {
    currentPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 8, message: '密码长度至少为8个字符', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value === passwordForm.currentPassword) {
                    callback(new Error('新密码不能与当前密码相同'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ],
    confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== passwordForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ]
};

const getUserDetails = async () => {
    loading.value = true;
    try {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.log("Token is required.");
            return
        }
        const response = await axios.get('/api/user/userInfo',{
            headers: {
                'Content-Type': 'application/json',
                'token': token
            }
        });

        // 更新用户数据
        Object.assign(user, response.data);
        // 初始化表单数据
        form.username = user.username;
        form.email = user.email;
        form.avatarPath = user.avatarPath;

    } catch (error) {
        ElMessage.error('获取用户信息失败: ' + (error.response?.data?.message || error.message));
    } finally {
        loading.value = false;
    }
};

const beforeAvatarUpload = (file) => {
    const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt500K = file.size / 1024 < 500;

    if (!isImage) {
        ElMessage.error('只能上传 JPG/PNG 格式的图片!');
        return false;
    }

    if (!isLt500K) {
        ElMessage.error('图片大小不能超过 500KB!');
        return false;
    }

    return true;
};

// 处理头像上传
const handleAvatarUpload = async (options) => {
    const file = options.file;

    try {
        loading.value = true;
        const formData = new FormData();
        formData.append('avatar', file);

        const token = sessionStorage.getItem('token');
        const response = await axios.post('/api/user/avatar', formData, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'multipart/form-data'
            }
        });

        // 更新头像
        user.avatarPath = response.data.avatarPath;
        form.avatarPath = response.data.avatarPath;

        ElMessage.success('头像更新成功');
    } catch (error) {
        ElMessage.error('头像上传失败: ' + (error.response?.data?.message || error.message));
    } finally {
        loading.value = false;
    }
};

// 更新用户信息
const updateUserInfo = async () => {
    try {
        loading.value = true;
        const token = sessionStorage.getItem('token');
        await axios.put(`/api/user/${user.id}`, {
            username: form.username,
            email: form.email,
            password: "" // 不修改密码时传空字符串，后端会忽略
        }, {
            headers: { 'token': token }
        });
        dialogFormVisible.value = false;
        ElMessage.success('用户信息更新成功');
        await getUserDetails();
    } catch (error) {
        ElMessage.error('更新失败: ' + (error.response?.data?.message || error.message));
    } finally {
        loading.value = false;
    }
};

// 更新密码
const updatePassword = async () => {
    try {
        loading.value = true;
        const token = sessionStorage.getItem('token');
        await axios.put(`/api/user/${user.id}`, {
            username: user.username,
            email: user.email,
            // !important: 该avatar字段用来临时取代currentPassword字段，不用于信息更新
            avatarPath: passwordForm.currentPassword,
            password: passwordForm.newPassword
        }, {
            headers: { 'token': token }
        });

        showPasswordDialog.value = false;
        passwordForm.currentPassword = '';
        passwordForm.newPassword = '';
        passwordForm.confirmPassword = '';

        ElMessage.success('密码更新成功');
        await getUserDetails();
    } catch (error) {
        if (error.response && error.response.status == 401) {
            ElMessage.error('原密码错误');
        } else if (error.response && error.response.status == 403) {
            ElMessage.error('新密码不能和旧密码相同');
        } else {
            ElMessage.error('未知错误，更新密码失败');
            console.error('更新密码时发生错误:', error);
        }
    } finally {
        loading.value = false;
    }
};

// 刷新用户数据
const updateUserDetails = async () => {
    await getUserDetails();
    ElNotification.success({
        title: '刷新成功',
        message: '用户数据已更新',
        position: 'bottom-right'
    });
};

// 组件挂载时加载用户数据
onMounted(async () => {
    await getUserDetails();
});
</script>

<style>
.el-descriptions {
    padding: 16px;
    border: 1px solid lightgray;
    border-radius: 6px;
}

.action-buttons {
    margin: 20px 0;
    text-align: center;
}

.course-info h3 {
    margin-top: 0;
    color: #606266;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
}

.stats {
    display: flex;
    justify-content: space-around;
    margin-top: 15px;
}

.stat-card {
    text-align: center;
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    width: 30%;
}

.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #409eff;
}

.stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 5px;
}

.avatar-uploader {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.avatar-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 150px;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-upload:hover {
    border-color: #409eff;
}

.avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
}

.upload-tip {
    margin-top: 10px;
    color: #606266;
    font-size: 12px;
    text-align: center;
}

.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
}

.loading-icon {
    font-size: 48px;
    color: #409eff;
    animation: rotate 2s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}
</style>
