/**
 * 供货品类：关键词查询 + 本地分页
 * 接口：GET /api/supplyCategory/list、GET /api/supplyCategory/search?keyword
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  window.HRViews.category = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>供货品类</h3>',
      '      <span class="muted" style="font-size:12.5px">共 {{ total }} 条</span>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="品类名称" clearable',
      '                  style="width:260px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="categoryId" label="品类ID" width="90" />',
      '        <el-table-column prop="categoryName" label="品类名称" min-width="140" />',
      '        <el-table-column prop="specifications" label="规格参数" min-width="280" show-overflow-tooltip />',
      '        <el-table-column prop="origin" label="产地 / 交割地" min-width="140" />',
      '        <el-table-column prop="unit" label="计量单位" width="110" />',
      '        <el-table-column label="创建时间" min-width="170">',
      '          <template #default="{ row }">{{ UI.fmtDateTime(row.createTime) }}</template>',
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
      load: async function () {
        this.loading = true;
        try {
          var list = await API.get(this.keyword ? '/api/supplyCategory/search' : '/api/supplyCategory/list',
            this.keyword ? { keyword: this.keyword } : {});
          this.all = list || [];
          this.total = this.all.length;
          var start = (this.page - 1) * this.size;
          this.rows = this.all.slice(start, start + this.size);
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
