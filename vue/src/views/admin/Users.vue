<template>
  <div class="users-page">
    <div class="page-card">
      <div class="page-header">
        <h3>用户管理</h3>
        <el-button type="primary" @click="openDialog(null)">
          <Icon icon="mdi:account-plus" width="16" style="margin-right:4px" />新增用户
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索用户名/昵称/手机号" clearable style="width:220px" />
        <el-select v-model="query.role" placeholder="角色" clearable style="width:140px">
          <el-option label="普通员工" :value="0" />
          <el-option label="管理员" :value="1" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <Icon icon="mdi:magnify" width="16" style="margin-right:4px" />搜索
        </el-button>
      </div>

      <!-- 用户列表表格 -->
      <el-table :data="tableData" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'success' : ''">{{ row.role === 1 ? '管理员' : '普通员工' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" size="small" circle @click="openDialog(row)">
                <Icon icon="mdi:pencil" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip :content="row.status === 1 ? '禁用' : '启用'" placement="top">
              <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" circle @click="handleToggleStatus(row)">
                <Icon :icon="row.status === 1 ? 'mdi:lock' : 'mdi:lock-open'" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button type="danger" size="small" circle @click="handleDelete(row)">
                <Icon icon="mdi:delete" width="14" />
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width:100%">
            <el-option label="普通员工" :value="0" />
            <el-option label="管理员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../../api'

export default {
  name: 'UsersPage',
  components: { Icon },
  setup() {
    const query = reactive({ keyword: '', role: null, status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10

    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: null,
      username: '',
      password: '',
      nickname: '',
      phone: '',
      role: 0,
      status: 1
    })

    const rules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }]
    }

    const loadData = async () => {
      const res = await request.get('/api/user/list', {
        params: {
          keyword: query.keyword || undefined,
          role: query.role !== null ? query.role : undefined,
          status: query.status !== null ? query.status : undefined,
          pageNum: pageNum.value,
          pageSize
        }
      })
      if (res.code === 200) {
        tableData.value = res.data.list
        total.value = res.data.total
      }
    }

    const handleSearch = () => {
      pageNum.value = 1
      loadData()
    }

    const openDialog = (row) => {
      if (row) {
        isEdit.value = true
        Object.assign(form, {
          id: row.id,
          username: row.username,
          password: '',
          nickname: row.nickname || '',
          phone: row.phone || '',
          role: row.role,
          status: row.status
        })
      } else {
        isEdit.value = false
        Object.assign(form, {
          id: null,
          username: '',
          password: '',
          nickname: '',
          phone: '',
          role: 0,
          status: 1
        })
      }
      dialogVisible.value = true
    }

    const resetForm = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }

    const handleSave = async () => {
      if (!formRef.value) return
      await formRef.value.validate(async (valid) => {
        if (!valid) return
        const data = { ...form }
        if (isEdit.value) {
          delete data.password
        }
        const res = await request.post('/api/user/save', data)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.delete(`/api/user/delete/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    const handleToggleStatus = (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.post('/api/user/updateStatus', { id: row.id, status: newStatus })
        if (res.code === 200) {
          ElMessage.success(`${action}成功`)
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    onMounted(loadData)

    return {
      query, tableData, total, pageNum, pageSize,
      dialogVisible, isEdit, formRef, form, rules,
      loadData, handleSearch, openDialog, resetForm,
      handleSave, handleDelete, handleToggleStatus
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h3 {
  font-size: 16px;
  color: var(--text-primary);
}
</style>
