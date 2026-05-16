<template>
  <div class="merchant-management">
    <el-card>
      <!-- 使用标签页区分商家列表和审核管理 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="商家列表" name="list">
          <!-- 搜索栏 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="商家名称">
              <el-input v-model="searchForm.name" placeholder="请输入商家名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部" clearable>
                <el-option label="营业中" :value="1" />
                <el-option label="休息中" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadMerchants">搜索</el-button>
              <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 商家表格 -->
          <el-table :data="merchants" border stripe v-loading="loading">
            <el-table-column prop="logo" label="店铺图片" width="100">
              <template #default="scope">
                <el-image
                  :src="imageBase(scope.row.logo) || defaultLogo"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 8px;"
                />
              </template>
            </el-table-column>
            <el-table-column prop="merchantName" label="商家名称" />
            <el-table-column prop="phone" label="联系电话" />
            <el-table-column prop="contactPhone" label="商家电话" />
            <el-table-column label="地址" min-width="200">
              <template #default="scope">
                {{ formatAddress(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column prop="avgRating" label="评分" width="120">
              <template #default="scope">
                <el-rate v-model="scope.row.avgRating" disabled show-score allow-half />
              </template>
            </el-table-column>
            <el-table-column prop="salesVolume" label="销量" width="80" />
            <el-table-column prop="deliveryFee" label="配送费" width="80">
              <template #default="scope">
                ¥{{ scope.row.deliveryFee || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="minOrderAmount" label="起送价" width="80">
              <template #default="scope">
                ¥{{ scope.row.minOrderAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                <el-button
                  size="small"
                  :type="scope.row.status === 1 ? 'warning' : 'success'"
                  @click="handleToggleStatus(scope.row)"
                >
                  {{ scope.row.status === 1 ? '停业' : '营业' }}
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
        </el-tab-pane>

        <el-tab-pane label="商家审核" name="audit">
          <!-- 审核搜索栏 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="商家名称">
              <el-input v-model="auditSearchForm.name" placeholder="请输入商家名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadPendingMerchants">搜索</el-button>
              <el-button :icon="Refresh" @click="resetAuditSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 待审核商家表格 -->
          <el-table :data="pendingMerchants" border stripe v-loading="auditLoading">
            <el-table-column prop="logo" label="店铺图片" width="100">
              <template #default="scope">
                <el-image
                  :src="imageBase(scope.row.logo) || defaultLogo"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 8px;"
                />
              </template>
            </el-table-column>
            <el-table-column prop="merchantName" label="商家名称" />
            <el-table-column prop="contactPerson" label="联系人" />
            <el-table-column prop="contactPhone" label="联系电话" />
            <el-table-column label="地址" min-width="200">
              <template #default="scope">
                {{ formatAddress(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column prop="licenseNumber" label="营业执照号" width="150" />
            <el-table-column label="申请时间" width="160">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="拒绝原因" width="180">
              <template #default="scope">
                <span v-if="scope.row.status === 4">{{ scope.row.rejectReason || '-' }}</span>
                <span v-else class="text-gray">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="handleView(scope.row)">查看</el-button>
                <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handleApprove(scope.row)">通过</el-button>
                <el-button v-if="scope.row.status === 0" size="small" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 审核分页 -->
          <el-pagination
            @size-change="handleAuditSizeChange"
            @current-change="handleAuditCurrentChange"
            :current-page="auditPagination.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="auditPagination.size"
            :total="auditPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; text-align: right"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商家详情" width="700px">
      <div v-if="currentMerchant" class="merchant-detail">
        <div class="merchant-header">
          <el-image
            :src="imageBase(currentMerchant.logo) || defaultLogo"
            fit="cover"
            class="merchant-logo"
          />
          <div class="merchant-basic">
            <h3>{{ currentMerchant.merchantName }}</h3>
            <el-rate v-model="currentMerchant.avgRating" disabled show-score allow-half />
            <p class="merchant-status">
              <el-tag :type="getStatusType(currentMerchant.status)">
                {{ getStatusText(currentMerchant.status) }}
              </el-tag>
            </p>
          </div>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家ID">{{ currentMerchant.id }}</el-descriptions-item>
          <el-descriptions-item label="登录手机">{{ currentMerchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentMerchant.contactPerson || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="所在地区" :span="2">
            {{ formatAddress(currentMerchant) }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ currentMerchant.detailAddress || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">{{ currentMerchant.licenseNumber || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="商家分类ID">{{ currentMerchant.categoryId || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="平均评分">{{ currentMerchant.avgRating || 5 }}</el-descriptions-item>
          <el-descriptions-item label="总销量">{{ currentMerchant.salesVolume || 0 }}</el-descriptions-item>
          <el-descriptions-item label="起送价">¥{{ currentMerchant.minOrderAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="配送费">¥{{ currentMerchant.deliveryFee || 0 }}</el-descriptions-item>
          <el-descriptions-item label="营业时间">{{ currentMerchant.businessHours || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatTime(currentMerchant.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="商家简介" :span="2">
            {{ currentMerchant.description || '暂无简介' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { markRaw } from 'vue';
import { getMerchantList, updateMerchantStatus, getPendingMerchants, approveMerchant } from '../api/merchant';
import { Search, Refresh } from '@element-plus/icons-vue';

export default {
  name: 'MerchantManagement',
  data() {
    return {
      // 图标组件
      Search: markRaw(Search),
      Refresh: markRaw(Refresh),
      // 默认Logo
      defaultLogo: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YzZjRmNiIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iI2EwYTBhMCI+5biM5Z+O5bqPPC90ZXh0Pjwvc3ZnPg==',
      // 当前激活的标签页
      activeTab: 'list',
      // 商家列表数据
      searchForm: {
        name: '',
        status: 1
      },
      merchants: [],
      loading: false,
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },
      // 审核数据
      auditSearchForm: {
        name: ''
      },
      pendingMerchants: [],
      auditLoading: false,
      auditPagination: {
        current: 1,
        size: 10,
        total: 0
      },
      // 详情对话框
      detailDialogVisible: false,
      currentMerchant: null
    };
  },
  mounted() {
    this.loadMerchants();
  },
  methods: {
    // 标签页切换
    handleTabChange(tab) {
      if (tab.name === 'audit') {
        this.loadPendingMerchants();
      }
    },
    // 加载商家列表
    async loadMerchants() {
      this.loading = true;
      try {
        const params = {
          keyword: this.searchForm.name,
          status: this.searchForm.status,
          pageNum: this.pagination.current,
          pageSize: this.pagination.size
        };
        const response = await getMerchantList(params);
        this.merchants = response.records || [];
        this.pagination.total = response.total || 0;
      } catch (error) {
        this.$message.error('加载商家列表失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    resetSearch() {
      this.searchForm = { name: '', status: 1 };
      this.pagination.current = 1;
      this.loadMerchants();
    },
    // 加载待审核商家
    async loadPendingMerchants() {
      this.auditLoading = true;
      try {
        const params = {
          keyword: this.auditSearchForm.name,
          pageNum: this.auditPagination.current,
          pageSize: this.auditPagination.size
        };
        const response = await getPendingMerchants(params);
        this.pendingMerchants = response.records || [];
        this.auditPagination.total = response.total || 0;
      } catch (error) {
        this.$message.error('加载待审核商家失败');
        console.error(error);
      } finally {
        this.auditLoading = false;
      }
    },
    resetAuditSearch() {
      this.auditSearchForm = { name: '' };
      this.auditPagination.current = 1;
      this.loadPendingMerchants();
    },
    // 图片路径处理
    imageBase(path) {
      if (!path) return ''
      if (path.startsWith('http')) return path
      const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
      if (path.startsWith('/images/')) return `${baseUrl}${path}`
      if (path.startsWith('images/')) return `${baseUrl}/${path}`
      return `${baseUrl}/images/${path}`
    },
    // 查看商家详情
    handleView(row) {
      this.currentMerchant = row;
      this.detailDialogVisible = true;
    },
    // 切换营业状态
    async handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 2 : 1;
      try {
        await this.$confirm(`确定要${newStatus === 1 ? '开启营业' : '停业'}吗?`, '提示', {
          type: 'warning'
        });

        await updateMerchantStatus(row.id, newStatus);
        row.status = newStatus;
        this.$message.success('操作成功');
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败');
          console.error(error);
        }
      }
    },
    // 审核通过
    async handleApprove(row) {
      try {
        await this.$confirm('确定要通过该商家的入驻申请吗?', '提示', {
          type: 'warning'
        });

        await approveMerchant(row.id, 1, '');
        this.$message.success('审核通过');
        this.loadPendingMerchants();
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('审核失败');
          console.error(error);
        }
      }
    },
    // 审核拒绝
    async handleReject(row) {
      try {
        const { value: reason } = await this.$prompt('请输入拒绝原因', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputValidator: (value) => {
            if (!value || value.trim() === '') {
              return '请输入拒绝原因';
            }
            return true;
          }
        });

        await approveMerchant(row.id, 4, reason);
        this.$message.success('已拒绝该申请');
        this.loadPendingMerchants();
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败');
          console.error(error);
        }
      }
    },
    // 分页方法
    handleSizeChange(val) {
      this.pagination.size = val;
      this.loadMerchants();
    },
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.loadMerchants();
    },
    handleAuditSizeChange(val) {
      this.auditPagination.size = val;
      this.loadPendingMerchants();
    },
    handleAuditCurrentChange(val) {
      this.auditPagination.current = val;
      this.loadPendingMerchants();
    },
    // 格式化地址
    formatAddress(merchant) {
      const parts = [];
      if (merchant.province) parts.push(merchant.province);
      if (merchant.city) parts.push(merchant.city);
      if (merchant.district) parts.push(merchant.district);
      return parts.join('') || '未设置';
    },
    // 格式化时间
    formatTime(time) {
      if (!time) return '-';
      const date = new Date(time);
      return date.toLocaleString('zh-CN');
    },
    // 获取状态文本
    getStatusText(status) {
      const map = { 0: '待审核', 1: '营业中', 2: '休息中', 3: '已下架', 4: '已拒绝' };
      return map[status] || '未知';
    },
    // 获取状态类型
    getStatusType(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger', 4: 'danger' };
      return map[status] || 'info';
    }
  }
};
</script>

<style scoped>
.merchant-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
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

:deep(.el-tabs__item.is-active) {
  color: #ff8c00;
}

:deep(.el-tabs__active-bar) {
  background-color: #ff8c00;
}

.merchant-detail {
  padding: 10px;
}

.merchant-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.merchant-logo {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
}

.merchant-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.merchant-basic h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}

.merchant-status {
  margin: 10px 0 0 0;
}

.text-gray {
  color: #999;
}
</style>
