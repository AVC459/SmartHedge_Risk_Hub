/**
 * 产地字典：分页 + 模糊查询
 * 接口：GET /api/origindict/page?keyword&page&size
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  window.HRViews.origin = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>产地字典</h3>',
      '      <span class="muted" style="font-size:12.5px">共 {{ total }} 条</span>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="产地名称 / 简称" clearable',
      '                  style="width:260px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="originId" label="产地ID" width="90" />',
      '        <el-table-column prop="originName" label="产地名称" min-width="180" />',
      '        <el-table-column prop="originShortName" label="产地简称" min-width="150" />',
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

    methods: {
      load: async function () {
        this.loading = true;
        try {
          var r = await API.page('/api/origindict/page', {
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
      }
    },

    mounted: function () {
      this.load();
    }
  };
})();
