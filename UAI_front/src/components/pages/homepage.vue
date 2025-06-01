<template>
    <h2>课程选修</h2>
    <div class="sform">
        <div class="table-header sform_item">
            <h3>已选课程</h3>
        </div>
        <el-table height="300" stripe="stripe" class="sform_item" :data="filterTableData0" style="width: 100%">
            <el-table-column label="课程名称" prop="name" />
            <el-table-column label="描述" prop="description" />
            <el-table-column label="状态" align="center">
                <template #default="{ row }">
                    <el-tag type="success" v-if="row.creditValue === 0 || row.creditValue === null">无学分</el-tag>
                    <el-tag type="warning" v-else>有学分</el-tag>
                </template>
            </el-table-column>
            <el-table-column align="right">
                <template #header>
                    <el-input v-model="search_x" placeholder="搜索" />
                </template>
                <template #default="scope">
                    <el-button size="small" type="danger" @click="confirmDelete(scope.$index, scope.row)"
                        :disabled="scope.row.creditValue != null && scope.row.creditValue > 0">
                        退选
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>

    <div class="sform">
        <h3 class="sform_item">可选课程</h3>
        <el-table height="300" stripe="stripe" class="sform_item" :data="filterTableData1" style="width: 100%">
            <el-table-column label="课程名称" prop="name" />
            <el-table-column label="描述" prop="description" />
            <el-table-column align="right">
                <template #header>
                    <el-input v-model="search_z" placeholder="搜索" />
                </template>
                <template #default="scope">
                    <el-button size="small" type="primary" @click="handleSelect(scope.$index, scope.row)">
                        选课
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>

    <div style="height: 60px;"></div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from "element-plus";

interface Course {
    id: string
    name: string
    description: string
    creditValue?: number | null
}
// 已选课程数据（包含学分信息）
const selectedCourses = ref<Course[]>([])
// 可选课程数据
const availableCourses = ref<Course[]>([])

onMounted(
    async () => {
        await loadData(); // 页面加载时获取数据
    }
)

const search_x = ref('')
const search_z = ref('')
const filterTableData0 = computed(() =>
    selectedCourses.value.filter(
        (data) =>
            !search_x.value ||
            data.name.toLowerCase().includes(search_x.value.toLowerCase())
    )
)
const filterTableData1 = computed(() =>
    availableCourses.value.filter(
        (data) =>
            !search_z.value ||
            data.name.toLowerCase().includes(search_z.value.toLowerCase())
    )
)
// 处理退选
const handleDelete = async (_index: number, row: Course) => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        console.log('没有登录信息');
        return;
    }
    try {
        const response = await axios.post(`/api/course/student/drop`,
            { courseId: row.id }, {
            headers: {
                'token': token,
                'Content-Type': 'application/json'
            }
        });
        if (response.status === 200) {
            await loadData(); // 重新加载数据
            ElMessage({
                type: 'success',
                message: '退选成功',
            })
        } else {
            ElMessage({
                type: 'error',
                message: response.data || '退选失败，请稍后再试',
            })
        }
    } catch (error) {
        ElMessage({
            type: 'error',
            message: '退选失败，请稍后再试',
        })
    }
}
const confirmDelete = (index: number, course: Course) => {
    ElMessageBox.confirm(
        `确定要退选课程 "${course.name}" 吗？`,
        '退选确认',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
            center: true,
        }
    ).then(() => {
        handleDelete(index, course);
    }).catch(() => {
        // 取消删除
    });
};
// 处理选课
const handleSelect = async (_index: number, row: Course) => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        console.log('没有登录信息');
        return;
    }
    try {
        const response = await axios.post(`/api/course/student/select`,
            { courseId: row.id }, {
            headers: {
                'token': token,
                'Content-Type': 'application/json'
            }
        });
        if (response.status === 200) {
            await loadData(); // 重新加载数据
            ElMessage({
                type: 'success',
                message: '选课成功',
            })
        } else {
            ElMessage({
                type: 'error',
                message: response.data || '选课失败，请稍后再试',
            })
        }
    } catch (error) {
        ElMessage({
            type: 'error',
            message: '选课失败，请稍后再试',
        })
    }
}

// 加载数据
const loadData = async () => {
    try {
        const token = sessionStorage.getItem('token');
        if (!token) {
            console.error('没有登录信息');
            return;
        }
        // 获取已选课程及学分信息
        const selectedRes = await axios.get(`/api/course/student/${token}/selected`);
        if (selectedRes.status != 200) console.log('获取已选课程失败');
        else selectedCourses.value = selectedRes.data;

        // 获取可选课程
        const availableRes = await axios.get(`/api/course/student/${token}/available`);
        if (availableRes.status != 200) console.log('获取可选课程失败');
        else availableCourses.value = availableRes.data;

    } catch (e) {
        console.error('加载课程数据失败', e);
    }
}

</script>

<style scoped>
.sform {
    padding-left: 10%;
    text-align: center;
    padding-top: 10px;
    padding-bottom: 20px;
    border: 1px solid lightgray;
    border-radius: 6px;
    margin-bottom: 40px;
}

.sform_item {
    max-width: 86% !important;
}
</style>