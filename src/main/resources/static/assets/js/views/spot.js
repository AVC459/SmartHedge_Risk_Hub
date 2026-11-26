/**
 * 现货商品：分页 + 关键词查询 + 新增
 * 接口：GET /api/spotproduct/page、POST /api/spotproduct/save、GET /api/origindict/list
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  var UNITS = ['吨', '湿吨', '公斤', '桶', '立方米'];

  function blank() {
    return { id: 0, spotName: '', spotSpec: '', originId: null, spotUnit: '吨' };
  }

  window.HRViews.spot = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>现货商品</h3>',
      '      <el-button type="primary" @click="openCreate">新增现货</el-button>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="现货名称 / 规格" clearable',
      '                  style="width:280px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="id" label="ID" width="70" />',
      '        <el-table-column prop="spotName" label="现货名称" min-width="150" />',
      '        <el-table-column prop="spotSpec" label="规格" min-width="260" show-overflow-tooltip />',
      '        <el-table-column label="产地" min-width="140">',
      '          <template #default="{ row }">{{ (row.origin && row.origin.originName) || "—" }}</template>',
      '        </el-table-column>',
      '        <el-table-column prop="spotUnit" label="单位" width="90" />',
      '        <el-table-column label="更新时间" min-width="170">',
      '          <template #default="{ row }">{{ UI.fmtDateTime(row.updateTime) }}</template>',
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

      '  <el-dialog v-model="dialog" title="新增现货商品" width="560px" :close-on-click-modal="false">',
      '    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">',
      '      <el-form-item label="现货名称" prop="spotName">',
      '        <el-input v-model="form.spotName" placeholder="如：电解铜" />',
      '      </el-form-item>',
      '      <el-form-item label="规格" prop="spotSpec">',
      '        <el-input v-model="form.spotSpec" type="textarea" :rows="2" placeholder="如：阴极铜 Cu≥99.95%" />',
      '      </el-form-item>',
      '      <el-form-item label="产地" prop="originId">',
      '        <el-select v-model="form.originId" filterable clearable placeholder="请选择产地" style="width:100%">',
      '          <el-option v-for="o in origins" :key="o.originId" :label="o.originName" :value="o.originId" />',
      '        </el-select>',
      '      </el-form-item>',
      '      <el-form-item label="计量单位" prop="spotUnit">',
      '        <el-select v-model="form.spotUnit" style="width:100%">',
      '          <el-option v-for="u in units" :key="u" :label="u" :value="u" />',
      '        </el-select>',
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
        origins: [],
        units: UNITS,
        dialog: false,
        submitting: false,
        form: blank(),
        rules: {
          spotName: [{ required: true, message: '请输入现货名称', trigger: 'blur' }]
        }
      };
    },

    methods: {
      load: async function () {
        this.loading = true;
        try {
          var r = await API.page('/api/spotproduct/page', {
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

      loadOrigins: async function () {
        this.origins = await UI.safe(function () {
          return API.get('/api/origindict/list');
        }, []);
      },

      openCreate: function () {
        this.form = blank();
        this.dialog = true;
      },

      submit: function () {
        var self = this;
        this.$refs.formRef.validate(async function (valid) {
          if (!valid) return;
          self.submitting = true;
          var ok = await UI.guard(function () {
            return API.post('/api/spotproduct/save', {
              spotName: self.form.spotName,
              spotSpec: self.form.spotSpec,
              originId: self.form.originId,
              spotUnit: self.form.spotUnit
            });
          }, '新增成功');
          self.submitting = false;
          if (ok) {
            self.dialog = false;
            self.load();
          }
        });
      }
    },

    mounted: async function () {
      await this.loadOrigins();
      this.load();
    }
  };
})();
