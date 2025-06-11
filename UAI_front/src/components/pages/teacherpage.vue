<template>

    <div class="sform">
        <div class="table-header sform_item">
            <h3>评分</h3>
        </div>
        <el-row>
            <el-col :span="12">
                <el-table height="300" stripe="stripe" class="sform_item" @current-change="handleCurrentChange"
                    :data="filterTableData0" style="width: 100%">
                    <el-table-column label="课程名称" prop="name" />
                    <el-table-column label="描述" prop="description" />
                    <el-table-column align="right">
                        <template #header>
                            <el-input v-model="search_x" placeholder="搜索" />
                        </template>
                    </el-table-column>
                </el-table>
            </el-col>
            <el-col :span="12">
                <el-table :loading="isLoading" height="300" stripe="stripe" class="sform_item" :data="filterTableData1"
                    style="width: 100%">
                    <el-table-column label="学生" prop="userName" />
                    <el-table-column label="成绩" prop="creditValue" />
                    <el-table-column align="right">
                        <template #header>
                            <el-input v-model="search_y" placeholder="搜索" />
                        </template>
                        <template #default="scope">
                            <el-button :disabled="scope.row.creditValue != '未录入学分'" size="small" type="primary"
                                @click="handleCreditEdit(scope.$index, scope.row)">
                                编辑
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-col>
        </el-row>
    </div>

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

    <el-dialog align-center v-model="editDialogVisible" title="编辑课程" width="500px" :close-on-click-modal="false">
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px" status-icon>
            <el-form-item label="课程名称" prop="name">
                <el-input v-model="editForm.name" />
            </el-form-item>
            <el-form-item label="描述" prop="description">
                <el-input v-model="editForm.description" type="textarea" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEdit">保存</el-button>
        </template>
    </el-dialog>

    <el-dialog align-center v-model="creditDialogVisible" title="录入学分" width="400px">
        <el-form :model="creditForm" :rules="creditRules" ref="creditFormRef" label-width="80px">
            <el-form-item label="学分" prop="creditValue">
                <el-input v-model.number="creditForm.creditValue" type="number" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="creditDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitCredit">保存</el-button>
        </template>
    </el-dialog>

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

interface StudentCredit {
    id: string
    userName: string
    userId: string
    courseId: string
    courseName: string
    creditValue: number
}

const isLoading = ref(false)
const selectedCourses = ref<Course[]>([])
const fullscreenLoading = ref(false)
const search_x = ref('')
const search_y = ref('')
const filterTableData0 = computed(() =>
    selectedCourses.value.filter(
        (data) =>
            !search_x.value ||
            data.name.toLowerCase().includes(search_x.value.toLowerCase()) || 
            data.description.toLowerCase().includes(search_x.value.toLowerCase())
    )
)
const filterTableData1 = computed(() =>
    studentCredits.value.filter(
        (data) =>
            !search_y.value ||
            data.userName.toLowerCase().includes(search_y.value.toLowerCase()) || 
            data.courseName.toLowerCase().includes(search_y.value.toLowerCase())
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

const currentRow = ref<Course>()
const handleCurrentChange = async (val: Course | undefined) => {
    currentRow.value = val
    await getStudent(val?.id!)
}

// 当前课程下的学生学分信息
const studentCredits = ref<StudentCredit[]>([])

// 根据课程ID获取学生学分信息
const getStudent = async(id: string) => {
    if (!id) {
        studentCredits.value = []
        return
    }
    try {
        isLoading.value = true
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('没有登录信息');
            return;
        }
        const response = await axios.get(`/api/user/course/${id}/students`, {
            headers: {
                'token': token
            }
        });
        if (response.status === 200) {
            // 替换creditValue为默认值（如0）
            studentCredits.value = (response.data || []).map((item: StudentCredit) => ({
            ...item,
            creditValue: item.creditValue == null ? "未录入学分" : item.creditValue
            }))
        } else {
            studentCredits.value = []
        }
    } catch (error) {
        console.error('获取学生学分信息失败', error)
        studentCredits.value = []
    } finally {
        isLoading.value = false
    }
}

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

const creditDialogVisible = ref(false)
const creditFormRef = ref<FormInstance>()
const creditForm = reactive({
    studentId: '',
    courseId: '',
    creditValue: null
})
const creditRules = reactive<FormRules>({
    creditValue: [{ required: true, message: '请输入学分', trigger: 'blur', type: 'number' }]
})

const handleCreditEdit = (_index: number, row: StudentCredit) => {
    creditForm.studentId = row.id
    creditForm.courseId = row.courseId
    creditForm.creditValue = null
    creditDialogVisible.value = true
}

const submitCredit = async () => {
    if (!creditFormRef.value) return
    await creditFormRef.value.validate(async (valid) => {
        if (!valid) return
        try {
            const response = await axios.post('/api/credit/credits', creditForm)
            if (response.status === 200) {
                ElNotification({ title: '系统消息', message: '学分录入成功', type: 'success', position: 'bottom-right' })
                creditDialogVisible.value = false
                await getStudent(creditForm.courseId)
            } else {
                ElNotification({ title: '系统消息', message: '学分录入失败', type: 'error', position: 'bottom-right' })
            }
        } catch (e) {
            ElNotification({ title: '系统消息', message: '学分录入失败', type: 'error', position: 'bottom-right' })
        }
    })
}

const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
    id: '',
    name: '',
    description: ''
})
const editRules = reactive<FormRules>({
    name: [{ required: true, message: '请填写课程名称', trigger: 'change' }],
    description: [{ required: true, message: '请输入描述', trigger: 'change' }]
})

const handleEdit = async (_index: number, row: Course) => {
    editForm.id = row.id
    editForm.name = row.name
    editForm.description = row.description
    editDialogVisible.value = true
}

const submitEdit = async() => {
    if (!editFormRef.value) return
    await editFormRef.value.validate(async (valid) => {
        if (!valid) return
        try {
            const response = await axios.put(
                `/api/course/courses/${editForm.id}`,
                {
                    id: editForm.id,
                    name: editForm.name,
                    description: editForm.description
                }
            )
            if (response.status === 200) {
                ElNotification({
                    title: '系统消息',
                    message: '课程信息修改成功',
                    type: 'success',
                    position: 'bottom-right'
                })
                editDialogVisible.value = false
                await loadData()
            } else {
                ElNotification({
                    title: '系统消息',
                    message: '课程信息修改失败',
                    type: 'error',
                    position: 'bottom-right'
                })
            }
        } catch (e) {
            ElNotification({
                title: '系统消息',
                message: '课程信息修改失败',
                type: 'error',
                position: 'bottom-right'
            })
        }
    })
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