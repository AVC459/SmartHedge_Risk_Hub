/**
 * 期货合约：分页 + 关键词查询 + 新增 / 编辑 / 删除
 * 接口：GET /api/futuresContract/page、POST /add、PUT /update、DELETE /delete/{id}
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  var METHODS = ['自动更新', '手动更新'];

  function blank() {
    return {
      contractId: 0,
      contractCode: '',
      contractName: '',
      lastTradingDay: '',
      varietyId: null,
      updateMethod: '自动更新'
    };
  }

  window.HRViews.futures = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>期货合约</h3>',
      '      <el-button type="primary" @click="openCreate">新增合约</el-button>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="合约代码 / 合约名称" clearable',
      '                  style="width:280px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="contractId" label="合约ID" width="90" />',
      '        <el-table-column prop="contractCode" label="合约代码" width="130">',
      '          <template #default="{ row }"><span class="mono">{{ row.contractCode }}</span></template>',
      '        </el-table-column>',
      '        <el-table-column prop="contractName" label="合约名称" min-width="150" />',
      '        <el-table-column label="所属品种" min-width="120">',
      '          <template #default="{ row }">{{ (row.tradingVariety && row.tradingVariety.varietyName) || "—" }}</template>',
      '        </el-table-column>',
      '        <el-table-column label="交易所" min-width="160">',
      '          <template #default="{ row }">{{ (row.exchange && row.exchange.exchangeName) || "—" }}</template>',
      '        </el-table-column>',
      '        <el-table-column label="所属行业" min-width="120">',
      '          <template #default="{ row }">{{ (row.industry && row.industry.industryName) || "—" }}</template>',
      '        </el-table-column>',
      '        <el-table-column label="最后交易日" min-width="130">',
      '          <template #default="{ row }">{{ UI.fmtDate(row.lastTradingDay) }}</template>',
      '        </el-table-column>',
      '        <el-table-column prop="updateMethod" label="更新方式" width="110" />',
      '        <el-table-column label="更新时间" min-width="170">',
      '          <template #default="{ row }">{{ UI.fmtDateTime(row.updateTime) }}</template>',
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

      '  <el-dialog v-model="dialog" :title="mode === \'create\' ? \'新增期货合约\' : \'编辑期货合约\'"',
      '             width="600px" :close-on-click-modal="false">',
      '    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">',
      '      <el-form-item label="合约代码" prop="contractCode">',
      '        <el-input v-model="form.contractCode" placeholder="如：RB2610" />',
      '      </el-form-item>',
      '      <el-form-item label="合约名称" prop="contractName">',
      '        <el-input v-model="form.contractName" placeholder="如：螺纹钢2610" />',
      '      </el-form-item>',
      '      <el-form-item label="最后交易日" prop="lastTradingDay">',
      '        <el-date-picker v-model="form.lastTradingDay" type="date" value-format="YYYY-MM-DD"',
      '                        placeholder="请选择" style="width:100%" />',
      '      </el-form-item>',
      '      <el-form-item label="所属品种" prop="varietyId">',
      '        <el-select v-model="form.varietyId" filterable clearable placeholder="请选择交易品种" style="width:100%">',
      '          <el-option v-for="v in varieties" :key="v.varietyId"',
      '                     :label="v.varietyName + \'（\' + v.varietyCode + \'）\'" :value="v.varietyId" />',
      '        </el-select>',
      '      </el-form-item>',
      '      <el-form-item label="更新方式" prop="updateMethod">',
      '        <el-select v-model="form.updateMethod" style="width:100%">',
      '          <el-option v-for="m in methods" :key="m" :label="m" :value="m" />',
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
        varieties: [],
        methods: METHODS,
        dialog: false,
        mode: 'create',
        submitting: false,
        form: blank(),
        rules: {
          contractCode: [{ required: true, message: '请输入合约代码', trigger: 'blur' }],
          contractName: [{ required: true, message: '请输入合约名称', trigger: 'blur' }],
          lastTradingDay: [{ required: true, message: '请选择最后交易日', trigger: 'change' }],
          varietyId: [{ required: true, message: '请选择所属品种', trigger: 'change' }]
        }
      };
    },

    methods: {
      load: async function () {
        this.loading = true;
        try {
          var r = await API.page('/api/futuresContract/page', {
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

      /** 交易品种下拉：使用期货模块自带的 /tradingVarieties 接口 */
      loadVarieties: async function () {
        this.varieties = await UI.safe(function () {
          return API.get('/api/futuresContract/tradingVarieties');
        }, []);
      },

      openCreate: function () {
        this.mode = 'create';
        this.form = blank();
        this.dialog = true;
      },

      /** 编辑：先按行数据即时回填，再调用 /getById 拉取最新详情覆盖 */
      openEdit: async function (row) {
        this.mode = 'edit';
        this.fill(row);
        this.dialog = true;
        var self = this;
        var detail = await UI.safe(function () {
          return API.get('/api/futuresContract/getById', { contractId: row.contractId });
        }, null);
        if (detail) self.fill(detail);
      },

      /** 把后端合约对象写入表单 */
      fill: function (d) {
        this.form = {
          contractId: d.contractId,
          contractCode: d.contractCode || '',
          contractName: d.contractName || '',
          lastTradingDay: UI.toDayString(d.lastTradingDay),
          varietyId: d.varietyId || null,
          updateMethod: d.updateMethod || '自动更新'
        };
      },

      submit: function () {
        var self = this;
        this.$refs.formRef.validate(async function (valid) {
          if (!valid) return;
          self.submitting = true;
          var payload = {
            contractId: Number(self.form.contractId) || 0,
            contractCode: self.form.contractCode,
            contractName: self.form.contractName,
            lastTradingDay: self.form.lastTradingDay,
            varietyId: self.form.varietyId,
            updateMethod: self.form.updateMethod
          };
          var ok = await UI.guard(function () {
            return self.mode === 'create'
              ? API.post('/api/futuresContract/add', payload)
              : API.put('/api/futuresContract/update', payload);
          }, self.mode === 'create' ? '新增成功' : '保存成功');
          self.submitting = false;
          if (ok) {
            self.dialog = false;
            self.load();
          }
        });
      },

      remove: async function (row) {
        var yes = await UI.confirm('删除期货合约', '确认删除合约【' + row.contractCode + '】？');
        if (!yes) return;
        await UI.guard(function () {
          return API.del('/api/futuresContract/delete/' + row.contractId);
        }, '删除成功');
        this.load();
      }
    },

    mounted: async function () {
      await this.loadVarieties();
      this.load();
    }
  };
})();
