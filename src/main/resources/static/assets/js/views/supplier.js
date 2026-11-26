/**
 * 供应商档案：分页 + 关键词查询 + 新增 / 编辑 / 删除
 * 接口：GET /api/supplier/page、POST /add、PUT /update、DELETE /delete/{id}
 * 说明：新增/编辑的请求体为 { supplier, categoryIds }
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  function blank() {
    return {
      supplierId: 0,
      supplierName: '',
      contactPhone: '',
      address: '',
      creditCode: '',
      operatorId: null,
      score: 60,
      status: 1,
      categoryIds: []
    };
  }

  window.HRViews.supplier = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>供应商档案</h3>',
      '      <el-button type="primary" @click="openCreate">新增供应商</el-button>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="供应商名称 / 电话 / 地址" clearable',
      '                  style="width:300px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="supplierId" label="ID" width="70" />',
      '        <el-table-column prop="supplierName" label="供应商名称" min-width="200" />',
      '        <el-table-column prop="contactPhone" label="联系电话" width="140" />',
      '        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />',
      '        <el-table-column prop="creditCode" label="统一信用代码" min-width="180">',
      '          <template #default="{ row }"><span class="mono">{{ UI.dash(row.creditCode) }}</span></template>',
      '        </el-table-column>',
      '        <el-table-column prop="operatorName" label="所属经营者" min-width="150">',
      '          <template #default="{ row }">{{ UI.dash(row.operatorName) }}</template>',
      '        </el-table-column>',
      '        <el-table-column prop="score" label="评分" width="80" />',
      '        <el-table-column label="状态" width="100">',
      '          <template #default="{ row }">',
      '            <el-tag size="small" :type="UI.statusTag(row.status).type" effect="plain">',
      '              {{ UI.statusTag(row.status).text }}',
      '            </el-tag>',
      '          </template>',
      '        </el-table-column>',
      '        <el-table-column label="操作" width="130" fixed="right">',
      '          <template #default="{ row }">',
      '            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>',
      '            <el-button link type="danger" @click="remove(row)">删除</el-button>',
      '          </template>',
      '        </el-table-column>',
      '        <template #empty><div class="empty-tip">暂无数据</div></template>',
      '      </el-table>',
      '      <div class="pager">',
      '        <el-pagination background layout="total, sizes, prev, pager, next, jumper"',
      '          :total="total" :current-page="page" :page-size="size" :page-sizes="[10, 20, 50]"',
      '          @current-change="onPageChange" @size-change="onSizeChange" />',
      '      </div>',
      '    </div>',
      '  </div>',

      '  <el-dialog v-model="dialog" :title="mode === \'create\' ? \'新增供应商\' : \'编辑供应商\'"',
      '             width="640px" :close-on-click-modal="false">',
      '    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">',
      '      <el-form-item label="供应商名称" prop="supplierName">',
      '        <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />',
      '      </el-form-item>',
      '      <el-form-item label="联系电话">',
      '        <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />',
      '      </el-form-item>',
      '      <el-form-item label="地址">',
      '        <el-input v-model="form.address" placeholder="请输入地址" />',
      '      </el-form-item>',
      '      <el-form-item label="统一信用代码">',
      '        <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />',
      '      </el-form-item>',
      '      <el-form-item label="所属经营者">',
      '        <el-select v-model="form.operatorId" filterable clearable placeholder="请选择经营者" style="width:100%">',
      '          <el-option v-for="o in operators" :key="o.operatorId" :label="o.operatorName" :value="o.operatorId" />',
      '        </el-select>',
      '      </el-form-item>',
      '      <el-form-item label="供货品类">',
      '        <el-select v-model="form.categoryIds" multiple filterable clearable placeholder="可多选" style="width:100%">',
      '          <el-option v-for="c in categories" :key="c.categoryId"',
      '                     :label="c.categoryName + (c.origin ? \'（\' + c.origin + \'）\' : \'\')" :value="c.categoryId" />',
      '        </el-select>',
      '      </el-form-item>',
      '      <el-form-item label="评分">',
      '        <el-input-number v-model="form.score" :min="0" :max="100" controls-position="right" />',
      '      </el-form-item>',
      '      <el-form-item label="状态">',
      '        <el-radio-group v-model="form.status">',
      '          <el-radio :value="1">启用</el-radio>',
      '          <el-radio :value="0">停用</el-radio>',
      '        </el-radio-group>',
      '      </el-form-item>',
      '    </el-form>',
      '    <template #footer>',
      '      <el-button @click="dialog = false">取消</el-button>',
      '      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>',
      '    </template>',
      '  </el-dialog>',
      '</div>'
    ].join('\n'),

    data: function () {
      return {
        operators: [],
        categories: [],
        dialog: false,
        mode: 'create',
        submitting: false,
        form: blank(),
        rules: {
          supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
        }
      };
    },

    methods: {
      load: async function () {
        this.loading = true;
        try {
          var r = await API.page('/api/supplier/page', {
            keyword: this.keyword, page: this.page, size: this.size
          });
          this.rows = r.rows || [];
          this.total = r.total || 0;
        } catch (e) {
          UI.fail(e);
          this.rows = [];
          this.total = 0;
        } finally {
          this.loading = false;
        }
      },

      loadOptions: async function () {
        this.operators = await UI.safe(function () {
          return API.get('/api/operator/list');
        }, []);
        this.categories = await UI.safe(function () {
          return API.get('/api/supplyCategory/list');
        }, []);
      },

      openCreate: function () {
        this.mode = 'create';
        this.form = blank();
        this.dialog = true;
      },

      openEdit: function (row) {
        this.mode = 'edit';
        this.form = {
          supplierId: row.supplierId,
          supplierName: row.supplierName || '',
          contactPhone: row.contactPhone || '',
          address: row.address || '',
          creditCode: row.creditCode || '',
          operatorId: row.operatorId || null,
          score: row.score === null || row.score === undefined ? 60 : row.score,
          status: row.status === null || row.status === undefined ? 1 : row.status,
          categoryIds: []
        };
        this.dialog = true;
      },

      submit: function () {
        var self = this;
        this.$refs.formRef.validate(async function (valid) {
          if (!valid) return;
          self.submitting = true;
          var payload = {
            supplier: {
              supplierId: Number(self.form.supplierId) || 0,
              supplierName: self.form.supplierName,
              contactPhone: self.form.contactPhone,
              address: self.form.address,
              creditCode: self.form.creditCode,
              operatorId: self.form.operatorId,
              score: self.form.score,
              status: self.form.status
            },
            categoryIds: self.form.categoryIds || []
          };
          var ok = await UI.guard(function () {
            return self.mode === 'create'
              ? API.post('/api/supplier/add', payload)
              : API.put('/api/supplier/update', payload);
          }, self.mode === 'create' ? '新增成功' : '保存成功');
          self.submitting = false;
          if (ok) {
            self.dialog = false;
            self.load();
          }
        });
      },

      remove: async function (row) {
        var yes = await UI.confirm('删除供应商', '确认删除供应商【' + row.supplierName + '】？');
        if (!yes) return;
        await UI.guard(function () {
          return API.del('/api/supplier/delete/' + row.supplierId);
        }, '删除成功');
        this.load();
      }
    },

    mounted: async function () {
      await this.loadOptions();
      this.load();
    }
  };
})();
