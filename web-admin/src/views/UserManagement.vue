<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <span>用户管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true">
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="封禁" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadUsers">搜索</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="users" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script>
import { markRaw } from 'vue';
import { getUserList, updateUserStatus } from '../api/user';
import { Search, Refresh } from '@element-plus/icons-vue';

export default {
  name: 'UserManagement',
  data() {
    return {
      // 图标组件 - 使用 markRaw 防止响应式包装
      Search: markRaw(Search),
      Refresh: markRaw(Refresh),
      // 表单数据
      searchForm: {
        phone: '',
        status: null
      },
      users: [],
      loading: false,
      pagination: {
        current: 1,
        size: 10,
        total: 0
      }
    };
  },
  mounted() {
    this.loadUsers();
  },
  methods: {
    async loadUsers() {
      this.loading = true;
      try {
        const params = {
          phone: this.searchForm.phone,
          status: this.searchForm.status,
          pageNum: this.pagination.current,
          pageSize: this.pagination.size
        };
        const response = await getUserList(params);
        this.users = response.records || [];
        this.pagination.total = response.total || 0;
      } catch (error) {
        this.$message.error('加载用户列表失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    resetSearch() {
      this.searchForm = { phone: '', status: null };
      this.pagination.current = 1;
      this.loadUsers();
    },
    async handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1;
      try {
        await this.$confirm(`确定要${newStatus === 1 ? '封禁' : '解封'}该用户吗?`, '提示', {
          type: 'warning'
        });

        await updateUserStatus(row.id, newStatus);
        row.status = newStatus;
        this.$message.success('操作成功');
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败');
          console.error(error);
        }
      }
    },
    handleSizeChange(val) {
      this.pagination.size = val;
      this.loadUsers();
    },
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.loadUsers();
    }
  }
};
</script>

<style scoped>
.user-management {
  padding: 20px;
}

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff8c00 0%, #ffa033 100%);
  border: none;
}
</style>
