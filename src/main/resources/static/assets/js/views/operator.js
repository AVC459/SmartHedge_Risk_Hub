/**
 * 经营者：查询 + 本地分页
 * 接口：GET /api/operator/list
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  var STATUS = {
    0: { text: '禁用', type: 'info' },
    1: { text: '启用', type: 'success' },
    2: { text: '已删除', type: 'danger' }
  };

  window.HRViews.operator = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>经营者</h3>',
      '      <span class="muted" style="font-size:12.5px">共 {{ total }} 条</span>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="经营者名称" clearable',
      '                  style="width:260px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="operatorId" label="经营者ID" width="100" />',
      '        <el-table-column prop="operatorName" label="经营者名称" min-width="220" />',
      '        <el-table-column label="状态" width="110">',
      '          <template #default="{ row }">',
      '            <el-tag size="small" :type="statusOf(row.status).type" effect="plain">{{ statusOf(row.status).text }}</el-tag>',
      '          </template>',
      '        </el-table-column>',
      '        <el-table-column label="创建时间" min-width="170">',
      '          <template #default="{ row }">{{ UI.fmtDateTime(row.createTime) }}</template>',
      '        </el-table-column>',
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
      '</div>'
    ].join('\n'),

    data: function () {
      return { all: [] };
    },

    methods: {
      statusOf: function (s) {
        return STATUS[Number(s)] || { text: '未知', type: 'info' };
      },
      load: async function () {
        this.loading = true;
        try {
          var list = await API.get('/api/operator/list');
          var kw = (this.keyword || '').trim();
          this.all = (list || []).filter(function (o) {
            return !kw || (o.operatorName && o.operatorName.indexOf(kw) >= 0);
          });
          this.total = this.all.length;
          this.page = 1;
          this.rows = this.all.slice(0, this.size);
        } catch (e) {
          UI.fail(e);
          this.all = [];
          this.rows = [];
          this.total = 0;
        } finally {
          this.loading = false;
        }
      },
      onPageChange: function (p) {
        this.page = p;
        var start = (p - 1) * this.size;
        this.rows = this.all.slice(start, start + this.size);
      },
      onSizeChange: function (s) {
        this.size = s;
        this.page = 1;
        this.rows = this.all.slice(0, s);
      }
    },

    mounted: function () {
      this.load();
    }
  };
})();
