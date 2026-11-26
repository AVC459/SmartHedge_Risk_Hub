/**
 * 运营概览：各业务表记录数统计与模块入口
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  window.HRViews.dashboard = {
    template: [
      '<div>',
      '  <div class="stat-grid">',
      '    <div class="stat-card" v-for="c in cards" :key="c.key" @click="go(c.key)">',
      '      <div class="stat-label">{{ c.label }}</div>',
      '      <div class="stat-value">{{ c.value === null ? "——" : c.value }}</div>',
      '      <div class="stat-open">进入维护 →</div>',
      '    </div>',
      '  </div>',

      '  <div class="card">',
      '    <div class="card-hd"><h3>模块说明</h3></div>',
      '    <div class="card-bd">',
      '      <el-descriptions :column="2" border>',
      '        <el-descriptions-item label="交易品种">品种名称、代码、所属交易所与行业，期货合约的基础数据。</el-descriptions-item>',
      '        <el-descriptions-item label="供货品类">供应商可申报的品类清单，含规格、产地与计量单位。</el-descriptions-item>',
      '        <el-descriptions-item label="产地字典">现货商品的产地/交割地基础字典。</el-descriptions-item>',
      '        <el-descriptions-item label="经营者">平台会员单位，供应商需挂靠经营者名下。</el-descriptions-item>',
      '        <el-descriptions-item label="现货商品">带规格与产地的实物商品，支持新增。</el-descriptions-item>',
      '        <el-descriptions-item label="供应商档案">供应商信息与供货品类关联，支持增删改查。</el-descriptions-item>',
      '        <el-descriptions-item label="期货合约">合约代码、名称、最后交易日与更新方式，支持增删改查。</el-descriptions-item>',
      '        <el-descriptions-item label="知识库问答">上传 PDF 后由本地 Ollama 模型基于知识库作答，附引用来源。</el-descriptions-item>',
      '      </el-descriptions>',
      '    </div>',
      '  </div>',
      '</div>'
    ].join('\n'),

    data: function () {
      return {
        cards: [
          { key: 'variety', label: '交易品种', value: null },
          { key: 'spot', label: '现货商品', value: null },
          { key: 'supplier', label: '供应商', value: null },
          { key: 'futures', label: '期货合约', value: null },
          { key: 'category', label: '供货品类', value: null },
          { key: 'operator', label: '经营者', value: null }
        ]
      };
    },

    methods: {
      go: function (key) {
        window.location.hash = '#/' + key;
      },
      setValue: function (key, v) {
        var c = this.cards.filter(function (x) {
          return x.key === key;
        })[0];
        if (c) c.value = v;
      },
      countPage: async function (key, path) {
        try {
          var r = await API.page(path, { page: 1, size: 1 });
          this.setValue(key, r.total || 0);
        } catch (e) {
          this.setValue(key, null);
        }
      },
      countList: async function (key, path) {
        try {
          var list = await API.get(path);
          this.setValue(key, (list || []).length);
        } catch (e) {
          this.setValue(key, null);
        }
      }
    },

    mounted: function () {
      this.countPage('variety', '/api/tradingvariety/page');
      this.countPage('spot', '/api/spotproduct/page');
      this.countPage('supplier', '/api/supplier/page');
      this.countPage('futures', '/api/futuresContract/page');
      this.countList('category', '/api/supplyCategory/list');
      this.countList('operator', '/api/operator/list');
    }
  };
})();
