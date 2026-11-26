/**
 * AI 知识库问答（RAG）
 *
 * 后端为同一应用内的 /api/ai/** 接口（Spring AI + 本地 Ollama），与页面同源同端口。
 * 问答走 SSE 流式接口：先收到 sources 事件渲染「引用来源」，再逐段收到 token 做打字机效果。
 * 标准 EventSource 只支持 GET，这里用 fetch + ReadableStream 自行解析 SSE 帧。
 */
(function () {
  'use strict';
  window.HRViews = window.HRViews || {};

  /** 与主应用同源，无需单独启动 AI 服务 */
  var AI_BASE = '';

  var QUICK_QUESTIONS = [
    '螺纹钢的保证金率和涨跌停板幅度是多少？',
    '风险度怎么计算？超过多少会被强行平仓？',
    '限价单一直不成交是什么原因？',
    '当日无负债结算是怎么一回事？',
    '临近最后交易日有什么风险？',
    '手续费是怎么收取的？'
  ];

  window.HRViews.aiKnowledge = {
    template: [
      '<div>',
      '  <div class="card">',
      '    <div class="card-hd">',
      '      <h3>大宗商品智能客服</h3>',
      '      <span class="muted" style="font-size:12.5px">',
      '        知识库 {{ stats.documentCount || 0 }} 篇 / {{ stats.chunkCount || 0 }} 个切片',
      '        · 相似度阈值 {{ stats.similarityThreshold }}',
      '      </span>',
      '    </div>',
      '    <div class="card-bd">',
      '      <el-tabs v-model="tab">',

      '        <el-tab-pane label="智能问答" name="chat">',
      '          <div class="quick-box">',
      '            <span class="muted" style="font-size:12.5px">试试这些问题：</span>',
      '            <el-tag v-for="q in quickQuestions" :key="q" size="small" effect="plain"',
      '                    class="quick-tag" @click="useQuick(q)">{{ q }}</el-tag>',
      '          </div>',

      '          <div class="chat-box" ref="chatBox">',
      '            <div v-if="!messages.length" class="empty-tip">还没有提问，输入问题开始咨询</div>',
      '            <div v-for="(m, i) in messages" :key="i" class="qa-item">',
      '              <div class="qa-q"><span class="who">我问</span>{{ m.q }}</div>',
      '              <div class="qa-a">',
      '                <span class="who ai">助手</span>',
      '                <div class="answer" style="white-space: pre-wrap;">{{ m.a || "正在检索知识库…" }}</div>',
      '                <div v-if="m.citations.length" class="cites">',
      '                  <div class="cite-title">引用来源（{{ m.citations.length }} 段）</div>',
      '                  <div v-for="(c, k) in m.citations" :key="k" class="cite">',
      '                    <div class="cite-hd">',
      '                      <el-tag size="small" effect="dark" type="info">{{ c.category }}</el-tag>',
      '                      <span class="cite-src">{{ c.source }} · 第 {{ c.chunkIndex }} 段</span>',
      '                      <span class="muted" style="font-size:12px">相似度 {{ fmtScore(c.score) }}</span>',
      '                    </div>',
      '                    <div class="cite-snippet">{{ c.snippet }}</div>',
      '                  </div>',
      '                </div>',
      '                <div v-if="m.elapsed" class="muted" style="font-size:12px;margin-top:6px">',
      '                  生成耗时 {{ m.elapsed }} ms',
      '                </div>',
      '              </div>',
      '            </div>',
      '          </div>',

      '          <div class="ask-bar">',
      '            <el-input v-model="question" type="textarea" :rows="2" resize="none"',
      '                      placeholder="输入与商品规则、交易流程、风险控制相关的问题，Ctrl + Enter 发送"',
      '                      @keydown.ctrl.enter="send" />',
      '            <div class="ask-actions">',
      '              <span class="muted" style="font-size:12px">本地 7B 模型推理较慢，约 30-60 秒，请耐心等待</span>',
      '              <span class="spacer"></span>',
      '              <el-button @click="messages = []" :disabled="busy">清空</el-button>',
      '              <el-button type="primary" @click="send" :loading="busy" :disabled="!question.trim()">提问</el-button>',
      '            </div>',
      '          </div>',
      '        </el-tab-pane>',

      '        <el-tab-pane label="知识库管理" name="kb">',
      '          <div class="toolbar">',
      '            <el-input v-model="category" placeholder="分类名（如：商品规则 / 风控说明）" clearable',
      '                      style="width:260px" />',
      '            <el-upload :http-request="upload" :show-file-list="false" accept=".txt,.md,.pdf">',
      '              <el-button type="primary">上传文档</el-button>',
      '            </el-upload>',
      '            <span class="spacer"></span>',
      '            <el-button @click="loadDocs" :loading="docLoading">刷新</el-button>',
      '            <el-button type="warning" @click="rebuild" :loading="rebuilding">重建索引</el-button>',
      '          </div>',
      '          <div class="muted" style="font-size:12.5px;margin:8px 0">',
      '            支持 .pdf / .txt / .md。上传后源文件保存在项目根目录 knowledge 文件夹，并按 token 切分后向量化入库。',
      '          </div>',

      '          <el-table :data="docs" v-loading="docLoading" border stripe>',
      '            <el-table-column prop="fileName" label="文件名" min-width="220" />',
      '            <el-table-column prop="category" label="分类" min-width="140" />',
      '            <el-table-column prop="chunkCount" label="切片数" width="90" />',
      '            <el-table-column label="大小" width="110">',
      '              <template #default="{ row }">{{ fmtSize(row.sizeBytes) }}</template>',
      '            </el-table-column>',
      '            <el-table-column prop="importedAt" label="导入时间" min-width="170" />',
      '            <el-table-column label="操作" width="90" fixed="right">',
      '              <template #default="{ row }">',
      '                <el-button link type="danger" size="small" @click="removeDoc(row)">删除</el-button>',
      '              </template>',
      '            </el-table-column>',
      '            <template #empty><div class="empty-tip">知识库暂无文档</div></template>',
      '          </el-table>',
      '        </el-tab-pane>',

      '      </el-tabs>',
      '    </div>',
      '  </div>',
      '</div>'
    ].join('\n'),

    data: function () {
      return {
        aiBase: AI_BASE,
        tab: 'chat',
        question: '',
        busy: false,
        messages: [],
        quickQuestions: QUICK_QUESTIONS,
        stats: {},
        docs: [],
        category: '',
        docLoading: false,
        rebuilding: false,
        statsTimer: null
      };
    },

    methods: {
      fmtScore: function (s) {
        return s === null || s === undefined ? '—' : Number(s).toFixed(3);
      },
      fmtSize: function (b) {
        if (!b) return '—';
        if (b < 1024) return b + ' B';
        if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
        return (b / 1024 / 1024).toFixed(1) + ' MB';
      },
      useQuick: function (q) {
        this.question = q;
        this.send();
      },
      scrollBottom: function () {
        var self = this;
        this.$nextTick(function () {
          var el = self.$refs.chatBox;
          if (el) el.scrollTop = el.scrollHeight;
        });
      },

      /** SSE 流式问答：手动解析 event/data 帧 */
      send: async function () {
        var q = (this.question || '').trim();
        if (!q || this.busy) return;

        var item = { q: q, a: '', citations: [], elapsed: null };
        this.messages.push(item);
        this.question = '';
        this.busy = true;
        this.scrollBottom();

        try {
          var resp = await fetch(AI_BASE + '/api/ai/ask/stream', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json;charset=UTF-8' },
            body: JSON.stringify({ question: q })
          });
          if (!resp.ok || !resp.body) throw new Error('AI 接口未响应，请确认应用与 Ollama 均已启动');

          var reader = resp.body.getReader();
          var decoder = new TextDecoder('utf-8');
          var buffer = '';
          var self = this;

          while (true) {
            var chunk = await reader.read();
            if (chunk.done) break;
            buffer += decoder.decode(chunk.value, { stream: true });

            var idx;
            while ((idx = buffer.indexOf('\n\n')) >= 0) {
              var block = buffer.slice(0, idx);
              buffer = buffer.slice(idx + 2);
              self.onFrame(block, item);
            }
          }
        } catch (e) {
          UI.fail(e);
          item.a = '请求失败：' + (e && e.message ? e.message : e);
        } finally {
          this.busy = false;
          this.scrollBottom();
        }
      },

      /** 解析单个 SSE 帧 */
      onFrame: function (block, item) {
        var event = 'message';
        var data = '';
        block.split('\n').forEach(function (line) {
          if (line.indexOf('event:') === 0) event = line.slice(6).trim();
          else if (line.indexOf('data:') === 0) data += line.slice(5).trim();
        });
        if (!data) return;

        if (event === 'sources') {
          try {
            var s = JSON.parse(data);
            item.citations = s.citations || [];
          } catch (e) {
            item.citations = [];
          }
        } else if (event === 'token') {
          try {
            item.a += JSON.parse(data);
          } catch (e) {
            item.a += data;
          }
          this.scrollBottom();
        } else if (event === 'done') {
          try {
            item.elapsed = JSON.parse(data).elapsedMs;
          } catch (e) {
            item.elapsed = null;
          }
        } else if (event === 'error') {
          try {
            item.a = JSON.parse(data);
          } catch (e) {
            item.a = data;
          }
        }
      },

      loadStats: async function () {
        try {
          var resp = await fetch(AI_BASE + '/api/ai/kb/stats');
          var res = await resp.json();
          if (res && res.success) this.stats = res.data || {};
        } catch (e) {
          /* 轮询期间偶发失败时保留上一次结果，避免数字闪烁归零 */
        }
      },

      /** 每 5 秒刷新一次统计，保证「N 篇 / M 个切片」实时 */
      startStatsPolling: function () {
        var self = this;
        this.stopStatsPolling();
        this.statsTimer = setInterval(function () {
          self.loadStats();
        }, 5000);
      },

      stopStatsPolling: function () {
        if (this.statsTimer) {
          clearInterval(this.statsTimer);
          this.statsTimer = null;
        }
      },

      loadDocs: async function () {
        this.docLoading = true;
        try {
          var resp = await fetch(AI_BASE + '/api/ai/kb/list');
          var res = await resp.json();
          this.docs = (res && res.data) || [];
        } catch (e) {
          UI.fail(e);
          this.docs = [];
        } finally {
          this.docLoading = false;
        }
      },

      upload: async function (opt) {
        var fd = new FormData();
        fd.append('file', opt.file);
        fd.append('category', this.category || '未分类');
        try {
          var resp = await fetch(AI_BASE + '/api/ai/kb/upload', { method: 'POST', body: fd });
          var res = await resp.json();
          if (res && res.success) {
            UI.ok('导入完成');
            this.loadDocs();
            this.loadStats();
          } else {
            UI.fail(new Error((res && res.msg) || '导入失败'));
          }
        } catch (e) {
          UI.fail(e);
        }
      },

      removeDoc: async function (row) {
        var yes = await UI.confirm('删除知识库文档', '将同时移除《' + row.fileName + '》的全部向量切片，确认删除？');
        if (!yes) return;
        try {
          var resp = await fetch(AI_BASE + '/api/ai/kb/' + encodeURIComponent(row.docId), { method: 'DELETE' });
          var res = await resp.json();
          if (res && res.success) {
            UI.ok('已删除');
            this.loadDocs();
            this.loadStats();
          } else {
            UI.fail(new Error((res && res.msg) || '删除失败'));
          }
        } catch (e) {
          UI.fail(e);
        }
      },

      rebuild: async function () {
        this.rebuilding = true;
        try {
          var resp = await fetch(AI_BASE + '/api/ai/kb/rebuild', { method: 'POST' });
          var res = await resp.json();
          if (res && res.success) {
            UI.ok('重建完成：' + res.data.documentCount + ' 篇文档 / ' + res.data.chunkCount + ' 个切片');
            this.loadDocs();
            this.loadStats();
          } else {
            UI.fail(new Error((res && res.msg) || '重建失败'));
          }
        } catch (e) {
          UI.fail(e);
        } finally {
          this.rebuilding = false;
        }
      }
    },

    watch: {
      /** 切到「知识库管理」页签时立即刷新一次 */
      tab: function (v) {
        if (v === 'kb') {
          this.loadStats();
          this.loadDocs();
        }
      }
    },

    mounted: function () {
      this.loadStats();
      this.loadDocs();
      this.startStatsPolling();
    },

    unmounted: function () {
      this.stopStatsPolling();
    }
  };
})();
