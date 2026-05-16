<template>
  <div class="category-management">
    <el-card>
      <template #header>
        <span>分类管理</span>
        <el-button style="float: right" type="primary" size="small" @click="handleAdd">
          添加分类
        </el-button>
      </template>

      <!-- 表格 -->
      <el-table :data="categories" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
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
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '../api/category';

export default {
  name: 'CategoryManagement',
  data() {
    return {
      categories: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '添加分类',
      form: {
        id: null,
        categoryName: '',
        sortOrder: 0
      },
      rules: {
        categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
      },
      submitting: false
    };
  },
  mounted() {
    this.loadCategories();
  },
  methods: {
    async loadCategories() {
      this.loading = true;
      try {
        const response = await getCategoryList();
        this.categories = response || [];
      } catch (error) {
        this.$message.error('加载分类列表失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    handleAdd() {
      this.dialogTitle = '添加分类';
      this.form = { id: null, categoryName: '', sortOrder: 0 };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.dialogTitle = '编辑分类';
      this.form = { ...row };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该分类吗?', '提示', {
          type: 'warning'
        });

        await deleteCategory(row.id);
        this.$message.success('删除成功');
        this.loadCategories();
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
              await updateCategory(this.form.id, this.form);
            } else {
              // 添加
              await addCategory(this.form);
            }
            this.$message.success('操作成功');
            this.dialogVisible = false;
            this.loadCategories();
          } catch (error) {
            this.$message.error('操作失败');
            console.error(error);
          } finally {
            this.submitting = false;
          }
        }
      });
    },
    formatTime(time) {
      if (!time) return '-';
      const date = new Date(time);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.category-management {
  padding: 20px;
}

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff8c00 0%, #ffa033 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #ffa033 0%, #ffb366 100%);
}
</style>
