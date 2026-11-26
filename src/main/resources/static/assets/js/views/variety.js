/**
 * 交易品种：分页 + 关键词查询
 * 接口：GET /api/tradingvariety/page?keyword&page&size
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  window.HRViews.variety = {
    mixins: [window.ListMixin],

    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>交易品种</h3>',
      '      <span class="muted" style="font-size:12.5px">共 {{ total }} 条</span>',
      '    </div>',
      '    <div class="card-bd">',
      '      <div class="toolbar">',
      '        <el-input v-model="keyword" placeholder="品种名称 / 品种代码 / 交易所 / 行业" clearable',
      '                  style="width:300px" @clear="search" @keyup.enter="search" />',
      '        <el-button type="primary" @click="search">查询</el-button>',
      '        <el-button @click="resetKeyword">重置</el-button>',
      '        <span class="spacer"></span>',
      '        <el-button @click="load" :loading="loading">刷新</el-button>',
      '      </div>',
      '      <el-table :data="rows" v-loading="loading" border stripe>',
      '        <el-table-column prop="varietyId" label="品种ID" width="90" />',
      '        <el-table-column prop="varietyName" label="品种名称" min-width="140" />',
      '        <el-table-column prop="varietyCode" label="品种代码" width="120">',
      '          <template #default="{ row }"><span class="mono">{{ row.varietyCode }}</span></template>',
      '        </el-table-column>',
      '        <el-table-column label="所属交易所" min-width="170">',
      '          <template #default="{ row }">{{ (row.exchange && row.exchange.exchangeName) || "—" }}</template>',
      '        </el-table-column>',
      '        <el-table-column label="所属行业" min-width="140">',
      '          <template #default="{ row }">{{ (row.industry && row.industry.industryName) || "—" }}</template>',
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
          var r = await API.page('/api/tradingvariety/page', {
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
