<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import axios from "axios";
import { ElMessage, ElMessageBox } from 'element-plus';

interface UserDto {
    id: string;
    username: string | null;
    email: string | null;
    role: string;
    avatarPath: string | null;
    status: string;
}

// 选项数据
const roleOptions = ref([
    { value: 'admin', label: '管理员' },
    { value: 'staff', label: '员工' },
    { value: 'student', label: '学生' },
    { value: 'default', label: '默认用户' }
]);

const statusOptions = ref([
    { value: 'all', label: '全部' },
    { value: 'active', label: '活跃' },
    { value: 'inactive', label: '未激活' },
    { value: 'banned', label: '已封禁' }
]);

const users = ref<UserDto[]>([]);
const loading = ref(false);

// 搜索和过滤
const searchQuery = ref('');

// 分页
const currentPage = ref(1);
const pageSize = ref(10);

// 编辑对话框
const editDialogVisible = ref(false);
const editUser = ref(null);
const editForm = ref({
    username: '',
    email: '',
    role: 'student',
    status: 'active',
    avatarPath: ''
});


const filteredUsers = computed(() => {
    return users.value.filter(user => {
        // 搜索过滤
        const matchesSearch =
            !searchQuery.value ||
            (user.username && user.username.toLowerCase().includes(searchQuery.value.toLowerCase())) ||
            (user.email && user.email.toLowerCase().includes(searchQuery.value.toLowerCase())) ||
            (user.role && user.role.toLowerCase().includes(searchQuery.value.toLowerCase()))
        return matchesSearch;
    });
});

const paginatedData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return filteredUsers.value.slice(start, end);
});

// 显示角色名称
const roleDisplay = (role: string) => {
    const option = roleOptions.value.find(r => r.value === role);
    return option ? option.label : role;
};

// 显示状态名称
const statusDisplay = (status: string) => {
    const option = statusOptions.value.find(s => s.value === status);
    return option ? option.label : status;
};


const getUserList = async () => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        console.log('没有登录信息');
        return;
    }
    loading.value = true;
    try {
        const response = await axios.get('/api/user/all', {
            headers: {
                'token': token,
                'Content-Type': 'application/json'
            }
        });
        if (response.status === 200) {
            users.value = response.data;
        } else {
            console.error('获取用户列表失败:', response.data);
        }
    } catch (error) {
        console.error('获取用户列表时发生错误:', error);
    } finally {
        loading.value = false;
    }
};

const showEditDialog = (user: any) => {
    editUser.value = user;

    if (user) {
        // 编辑现有用户
        editForm.value = {
            username: user.username,
            email: user.email,
            role: user.role,
            status: user.status,
            avatarPath: user.avatarPath
        };
    } else {
        // 添加新用户
        editForm.value = {
            username: '',
            email: '',
            role: 'student',
            status: 'active',
            avatarPath: ''
        };
    }

    editDialogVisible.value = true;
};

// 处理头像变化
const handleAvatarChange = (file: any) => {
    // 实际项目中应上传文件并获取URL
    const reader = new FileReader();
    reader.onload = (e) => {
        if (typeof e.target?.result === 'string') {
            editForm.value.avatarPath = e.target.result;
        } else {
            editForm.value.avatarPath = '';
        }
    };
    reader.readAsDataURL(file.raw);
};

// 确认删除
const confirmDelete = (user: UserDto) => {
    ElMessageBox.confirm(
        `确定要删除用户 "${user.username}" 吗？此操作不可撤销。`,
        '删除确认',
        {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'error',
            center: true,
        }
    ).then(() => {
        users.value = users.value.filter(u => u.id !== user.id);
        // 调用API删除用户
        ElMessage.success('用户已删除');
    }).catch(() => {
        // 取消删除
    });
};

onMounted(async () => {
    await getUserList();
});

</script>

<template>
    <div class="main_con">
        <h4>用户管理</h4>
        <br>
        <!-- 用户表格 -->
        <el-table :data="paginatedData" v-loading="loading" stripe height="350">
            <el-table-column label="头像" width="100">
                <template #default="{ row }">
                    <img :src="row.avatarPath || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                        class="user-avatar" alt="用户头像" style="height: 28px;" />
                </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" width="220">
                <template #default="{ row }">
                    <span>{{ row.email || '未设置' }}</span>
                </template>
            </el-table-column>
            <el-table-column label="角色" width="150">
                <template #default="{ row }">
                    <span :class="['role-tag', `role-${row.role}`]">
                        {{ roleDisplay(row.role) }}
                    </span>
                </template>
            </el-table-column>

            <el-table-column label="状态" width="120">
                <template #default="{ row }">
                    <span :class="['status-tag', `status-${row.status}`]">
                        {{ statusDisplay(row.status) }}
                    </span>
                </template>
            </el-table-column>

            <el-table-column width="180" fixed="right">
                <template #header>
                    <el-input v-model="searchQuery" placeholder="搜索" />
                </template>
                <template #default="{ row }">
                    <el-button size="small" type="danger" @click="confirmDelete(row)">
                        删除
                    </el-button>
                    <el-button size="small" type="primary" @click="showEditDialog(row)">
                        编辑
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <div style="height: 20px;"></div>
        <!-- 分页控件 -->
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="filteredUsers.length"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" background />
    </div>
    <el-dialog v-model="editDialogVisible" align-center :title="editUser ? '编辑用户' : '添加新用户'" width="500px"
        class="edit-dialog" :close-on-click-modal="false">
        <el-form :model="editForm" label-width="80px" class="edit-form">
            <el-form-item label="用户名">
                <el-input v-model="editForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="邮箱">
                <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="角色">
                <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
                    <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value" />
                </el-select>
            </el-form-item>

            <el-form-item label="状态">
                <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
                    <el-option v-for="status in statusOptions" :key="status.value" :label="status.label"
                        :value="status.value" />
                </el-select>
            </el-form-item>

            <el-form-item label="头像">
                <el-upload class="avatar-uploader" action="#" :show-file-list="false" :auto-upload="false"
                    :on-change="handleAvatarChange">
                    <img v-if="editForm.avatarPath" :src="editForm.avatarPath" class="user-avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>
        </el-form>

        <template #footer>
            <div class="form-footer">
                <el-button @click="editDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="">
                    {{ editUser ? '保存更改' : '添加用户' }}
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<style scoped>
.main_con {
    text-align: center;
    padding-left: 10%;
    padding-right: 10%;
    padding-top: 10px;
    padding-bottom: 20px;
    border: 1px solid lightgray;
    border-radius: 6px;
    margin-bottom: 40px;
}

.el-pagination {
    justify-content: center;
}
</style>