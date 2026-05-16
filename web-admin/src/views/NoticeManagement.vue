<template>
  <div class="notice-management">
    <el-card>
      <template #header>
        <span>公告管理</span>
        <el-button style="float: right" type="primary" size="small" @click="handleAdd">
          发布公告
        </el-button>
      </template>

      <!-- 表格 -->
      <el-table :data="notices" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            {{ ['系统通知', '活动公告', '维护公告'][scope.row.type] }}
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标" width="100">
          <template #default="scope">
            {{ ['全部', '用户', '商家', '骑手'][scope.row.targetType] }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="系统通知" :value="0" />
            <el-option label="活动公告" :value="1" />
            <el-option label="维护公告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标用户" prop="targetType">
          <el-select v-model="form.targetType" style="width: 100%">
            <el-option label="全部" :value="0" />
            <el-option label="用户" :value="1" />
            <el-option label="商家" :value="2" />
            <el-option label="骑手" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getNoticeList, publishNotice, updateNotice, deleteNotice } from '../api/notice';

export default {
  name: 'NoticeManagement',
  data() {
    return {
      notices: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '发布公告',
      form: {
        id: null,
        title: '',
        type: 0,
        targetType: 0,
        content: '',
        status: 1
      },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      submitting: false
    };
  },
  mounted() {
    this.loadNotices();
  },
  methods: {
    async loadNotices() {
      this.loading = true;
      try {
        const response = await getNoticeList();
        this.notices = response.records || [];
      } catch (error) {
        this.$message.error('加载公告列表失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    handleAdd() {
      this.dialogTitle = '发布公告';
      this.form = {
        id: null,
        title: '',
        type: 0,
        targetType: 0,
        content: '',
        status: 1
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.dialogTitle = '编辑公告';
      this.form = { ...row };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该公告吗?', '提示', {
          type: 'warning'
        });

        await deleteNotice(row.id);
        this.$message.success('删除成功');
        this.loadNotices();
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败');
          console.error(error);
        }
      }
    },
    handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          this.submitting = true;
          try {
            if (this.form.id) {
              // 编辑
              await updateNotice(this.form.id, this.form);
            } else {
              // 发布
              await publishNotice(this.form);
            }
            this.$message.success('操作成功');
            this.dialogVisible = false;
            this.loadNotices();
          } catch (error) {
            this.$message.error('操作失败');
            console.error(error);
          } finally {
            this.submitting = false;
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.notice-management {
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
