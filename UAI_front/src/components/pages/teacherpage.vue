<template>

    <div class="sform">
        <div class="table-header sform_item">
            <h3>关联课程</h3>
        </div>
        <el-table height="300" stripe="stripe" class="sform_item" :data="filterTableData0" style="width: 100%">
            <el-table-column label="课程名称" prop="name" />
            <el-table-column label="描述" prop="description" />
            <el-table-column align="right">
                <template #header>
                    <el-input v-model="search_x" placeholder="搜索" />
                </template>
                <template #default="scope">
                    <el-button size="small" type="danger" @click="confirmDelete(scope.$index, scope.row)">
                        删除
                    </el-button>
                    <el-button size="small" type="primary" @click="handleEdit(scope.$index, scope.row)">
                        编辑
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>

    <div class="sform">
        <h3 class="sform_item">新增课程</h3>
        <el-form :model="form" :rules="rules" label-width="auto" class="sform_item" ref="ruleFormRef" status-icon>
            <el-form-item label="创建者">
                <el-input v-model="form.username" type="text" disabled />
            </el-form-item>
            <el-form-item label="课程名称" prop="name">
                <el-input v-model="form.name" type="text" />
            </el-form-item>
            <el-form-item label="课程描述" prop="description">
                <el-input v-model="form.description" type="textarea" />
            </el-form-item>
            <el-form-item>
                <div style="text-align: right; width: 100%;">
                    <el-button @click="resetForm(ruleFormRef)">重置</el-button>
                    <el-button type="primary" @click="validateForm(ruleFormRef)">提交</el-button>
                </div>
            </el-form-item>
        </el-form>
    </div>
    <div style="height: 60px;" v-loading.fullscreen.lock="fullscreenLoading"></div>
</template>

<script lang="ts" setup>
import axios from 'axios';
import { ElMessageBox, ElNotification, type FormInstance, type FormRules } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue'

interface Course {
    id: string
    name: string
    description: string
}

// 已选课程数据（包含学分信息）
const selectedCourses = ref<Course[]>([])
const fullscreenLoading = ref(false)
const search_x = ref('')
const filterTableData0 = computed(() =>
    selectedCourses.value.filter(
        (data) =>
            !search_x.value ||
            data.name.toLowerCase().includes(search_x.value.toLowerCase())
    )
)
interface RuleForm {
    username: string
    name: string
    description: string
}

const ruleFormRef = ref<FormInstance>()
const form = reactive<RuleForm>({
    username: '',
    name: '',
    description: ''
})

const handleDelete = async (index: number, row: Course) => {
    try {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('没有登录信息');
            return;
        }
        const response = await axios.delete('/api/course/delete',
            {
                headers: {
                    'id': row.id
                }
            }
        );
        if (response.status === 200) {
            selectedCourses.value.splice(index, 1);
            ElNotification({
                title: '系统消息',
                message: "课程删除成功",
                type: 'success',
                position: 'bottom-right',
            })
        } else {
            ElNotification({
                title: '系统消息',
                message: "课程删除失败",
                type: 'error',
                position: 'bottom-right',
            })
        }
    } catch (error) {
        ElNotification({
            title: '系统消息',
            message: "课程删除失败",
            type: 'error',
            position: 'bottom-right',
        })
    }
}

const handleEdit = async (_index: number, row: Course) => {
    // 编辑功能可以在这里实现
    console.log('编辑课程:', row);
}

// 加载数据
const loadData = async () => {
    try {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('没有登录信息');
            return;
        }
        // 课程及信息
        const selectedRes = await axios.get(`/api/course/teacher/courses`,
            {
                headers: {
                    'token': token
                }
            }
        );
        if (selectedRes.status != 200) console.log('获取课程失败');
        else selectedCourses.value = selectedRes.data;

    } catch (e) {
        console.error('加载课程数据失败', e);
    }
}

const confirmDelete = (index: number, course: Course) => {
    ElMessageBox.confirm(
        `确定要删除课程 "${course.name}" 吗？此操作不可撤销。`,
        '删除确认',
        {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'error',
            center: true,
        }
    ).then(() => {
        handleDelete(index, course);
    }).catch(() => {
        // 取消删除
    });
};

const getUserName = async () => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        console.error("Token is required.");
        return;
    }
    try {
        const response = await axios.get('/api/user/username', {
            headers: {
                'token': token
            }
        });
        form.username = response.data || "未知";
    } catch (error) {
        console.error("Error:", error);
    }
}

const rules = reactive<FormRules<RuleForm>>(
    {
        name: [{ required: true, message: '请填写课程名称', trigger: 'change' }],
        description: [{ required: true, message: '请输入描述', trigger: 'change' }],
    }
)

const validateForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return
    await formEl.validate((valid, fields) => {
        if (valid) {
            submitForm()
        } else {
            console.log('error submit!', fields)
        }
    })
}

const submitForm = async () => {
    try {
        fullscreenLoading.value = true;
        const token = sessionStorage.getItem('token');
        const { username, ...formData } = form;
        const response = await axios.post(
            '/api/course/addCourse',
            formData,
            {
                headers: {
                    token: token || ''
                }
            }
        );
        fullscreenLoading.value = false;
        if (response.status == 200) {
            resetForm(ruleFormRef.value)
            ElNotification({
                title: '系统消息',
                message: "课程创建成功",
                type: 'success',
                position: 'bottom-right',
            })
            loadData()
        }
    } catch (error: any) {
        fullscreenLoading.value = false;
        ElNotification({
            title: '系统消息',
            message: "课程创建失败，请稍后再试",
            type: 'error',
            position: 'bottom-right',
        });
    }
}

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.resetFields();
}

onMounted(
    async () => {
        await getUserName();
        await loadData();
    }
)

</script>

<style>
.fh {
    padding-bottom: 20px;
    border-bottom: 1px solid lightgray;
    font-weight: normal;
}

.sform {
    text-align: center;
    padding-left: 10%;
    padding-top: 10px;
    padding-bottom: 20px;
    border: 1px solid lightgray;
    border-radius: 6px;
    margin-bottom: 40px;
}

.sform_item {
    max-width: 86% !important;
}

.el-alert {
    margin-bottom: 20px !important;
}

.alert-con {
    margin: 10px 0 30px 0;
}

.el-alert:first-child {
    margin: 0;
}
</style>