/**
 * 应用入口：登录鉴权、菜单、hash 路由、布局外壳。
 *
 * 登录态流转：checking（校验中）→ guest（未登录，展示登录页）→ in（已登录，展示控制台）。
 * 路由守卫：进入应用前先请求 /api/user/current，未登录一律停留在登录页；
 *          会话过期时由 API 层派发 hr-unauthorized 事件，自动退回登录页。
 */
(function () {
  'use strict';

  var mountPoint = document.getElementById('app');

  /* 依赖加载失败时的兜底提示 */
  if (typeof window.Vue === 'undefined' || typeof window.ElementPlus === 'undefined') {
    mountPoint.innerHTML = [
      '<div class="boot-error">',
      '  <h3>前端依赖加载失败</h3>',
      '  <div>页面依赖 <code>assets/vendor/</code> 下的 Vue3 与 Element Plus，当前未能加载成功。</div>',
      '</div>'
    ].join('');
    return;
  }

  /* 菜单：children 只有一项的分组，侧边栏直接渲染为一级菜单项，不再折叠为下拉 */
  var MENU = [
    { id: 'g-0', group: '首页', children: [{ key: 'dashboard', title: '运营概览' }] },
    {
      id: 'g-1',
      group: '基础数据',
      children: [
        { key: 'variety', title: '交易品种' },
        { key: 'category', title: '供货品类' },
        { key: 'origin', title: '产地字典' },
        { key: 'operator', title: '经营者' }
      ]
    },
    {
      id: 'g-2',
      group: '现货与供应商',
      children: [
        { key: 'spot', title: '现货商品' },
        { key: 'supplier', title: '供应商档案' }
      ]
    },
    {
      id: 'g-3',
      group: '衍生品合约',
      children: [{ key: 'futures', title: '期货合约' }]
    },
    {
      id: 'g-4',
      group: '智能助手 (AI)',
      children: [{ key: 'aiKnowledge', title: '知识库问答' }]
    }
  ];

  var meta = {};
  var openedGroups = [];
  MENU.forEach(function (g) {
    g.children.forEach(function (i) {
      meta[i.key] = { title: i.title, group: g.group };
    });
    if (g.children.length > 1) openedGroups.push(g.id);
  });

  var App = {
    template: [
      '<div class="app-root">',

      /* ---------- 启动态：校验登录 ---------- */
      '  <div v-if="auth === \'checking\'" class="boot-screen">正在校验登录状态…</div>',

      /* ---------- 登录 / 注册 ---------- */
      '  <div v-else-if="auth === \'guest\'" class="login-page">',
      '    <div class="login-card">',
      '      <div class="login-brand">',
      '        <div class="brand-mark">SH</div>',
      '        <div class="brand-text"><strong>SmartHedge</strong><span>风险中枢控制台</span></div>',
      '      </div>',
      '      <div class="login-tabs">',
      '        <span :class="[\'login-tab\', mode === \'login\' ? \'on\' : \'\']" @click="switchMode(\'login\')">登录</span>',
      '        <span :class="[\'login-tab\', mode === \'register\' ? \'on\' : \'\']" @click="switchMode(\'register\')">注册</span>',
      '      </div>',

      '      <el-form v-if="mode === \'login\'" :model="loginForm" label-position="top" @submit.prevent="doLogin">',
      '        <el-form-item label="用户名">',
      '          <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名"',
      '                    clearable @keyup.enter="doLogin" />',
      '        </el-form-item>',
      '        <el-form-item label="密码">',
      '          <el-input v-model="loginForm.password" size="large" type="password" show-password',
      '                    placeholder="请输入密码" @keyup.enter="doLogin" />',
      '        </el-form-item>',
      '        <el-button type="primary" size="large" class="login-btn"',
      '                   :loading="logging" @click="doLogin">登 录</el-button>',
      '      </el-form>',

      '      <el-form v-else :model="regForm" label-position="top" @submit.prevent="doRegister">',
      '        <el-form-item label="用户名">',
      '          <el-input v-model="regForm.username" size="large" placeholder="设置登录用户名"',
      '                    clearable @keyup.enter="doRegister" />',
      '        </el-form-item>',
      '        <el-form-item label="昵称（选填）">',
      '          <el-input v-model="regForm.nickname" size="large" placeholder="默认与用户名相同"',
      '                    clearable @keyup.enter="doRegister" />',
      '        </el-form-item>',
      '        <el-form-item label="密码">',
      '          <el-input v-model="regForm.password" size="large" type="password" show-password',
      '                    placeholder="至少 6 位" @keyup.enter="doRegister" />',
      '        </el-form-item>',
      '        <el-form-item label="确认密码">',
      '          <el-input v-model="regForm.confirm" size="large" type="password" show-password',
      '                    placeholder="再次输入密码" @keyup.enter="doRegister" />',
      '        </el-form-item>',
      '        <el-button type="primary" size="large" class="login-btn"',
      '                   :loading="logging" @click="doRegister">注 册</el-button>',
      '      </el-form>',
      '    </div>',
      '  </div>',

      /* ---------- 控制台 ---------- */
      '  <div v-else class="app-shell">',
      '    <aside class="sidebar">',
      '      <div class="brand">',
      '        <div class="brand-mark">SH</div>',
      '        <div class="brand-text"><strong>SmartHedge</strong><span>风险中枢控制台</span></div>',
      '      </div>',
      '      <el-menu class="side-menu" :default-active="activeKey" :default-openeds="openedGroups"',
      '               background-color="transparent" text-color="#a9bad4" active-text-color="#ffffff"',
      '               @select="go">',
      '        <template v-for="g in menu" :key="g.id">',
      '          <el-menu-item v-if="g.children.length === 1" :index="g.children[0].key">',
      '            {{ g.children[0].title }}',
      '          </el-menu-item>',
      '          <el-sub-menu v-else :index="g.id">',
      '            <template #title>{{ g.group }}</template>',
      '            <el-menu-item v-for="i in g.children" :key="i.key" :index="i.key">{{ i.title }}</el-menu-item>',
      '          </el-sub-menu>',
      '        </template>',
      '      </el-menu>',
      '      <div class="side-foot">',
      '        <span :class="[\'dot\', online === true ? \'ok\' : (online === false ? \'bad\' : \'\')]"></span>',
      '        <span>{{ onlineText }}</span>',
      '      </div>',
      '    </aside>',

      '    <section class="main">',
      '      <header class="topbar">',
      '        <div class="crumb"><small>{{ groupTitle }}</small>{{ pageTitle }}</div>',
      '        <div class="top-actions">',
      '          <span class="user-chip">{{ userName }}</span>',
      '          <el-button size="small" @click="logout">退出登录</el-button>',
      '        </div>',
      '      </header>',
      '      <div class="content">',
      '        <component :is="activeKey" :key="activeKey" />',
      '      </div>',
      '    </section>',
      '  </div>',

      '</div>'
    ].join('\n'),

    data: function () {
      return {
        menu: MENU,
        openedGroups: openedGroups,
        auth: 'checking',
        mode: 'login',
        user: {},
        loginForm: { username: '', password: '' },
        regForm: { username: '', nickname: '', password: '', confirm: '' },
        logging: false,
        activeKey: 'dashboard',
        online: null
      };
    },

    computed: {
      pageTitle: function () {
        return (meta[this.activeKey] || {}).title || '未知页面';
      },
      groupTitle: function () {
        return (meta[this.activeKey] || {}).group || '';
      },
      userName: function () {
        return this.user.nickname || this.user.username || '';
      },
      onlineText: function () {
        if (this.online === true) return '后端已连接';
        if (this.online === false) return '后端未连接';
        return '正在检测后端…';
      }
    },

    methods: {
      go: function (key) {
        if (meta[key]) window.location.hash = '#/' + key;
      },

      syncFromHash: function () {
        if (this.auth !== 'in') return;
        var raw = window.location.hash || '';
        var k = raw.replace(/^#\/?/, '').split('?')[0];
        this.activeKey = meta[k] ? k : 'dashboard';
      },

      /** 路由守卫：向后端确认登录态，未登录则停留在登录页 */
      checkLogin: async function () {
        try {
          var resp = await fetch('/api/user/current', { credentials: 'same-origin' });
          var res = await resp.json();
          if (res && res.success && res.data) {
            this.user = res.data;
            this.auth = 'in';
            this.enterApp();
          } else {
            this.auth = 'guest';
          }
        } catch (e) {
          this.auth = 'guest';
        }
      },

      doLogin: async function () {
        if (this.logging) return;
        var u = (this.loginForm.username || '').trim();
        var p = this.loginForm.password || '';
        if (!u || !p) {
          UI.warn('请输入用户名和密码');
          return;
        }
        this.logging = true;
        try {
          var data = await API.post('/api/user/login', { username: u, password: p });
          this.user = data || {};
          this.loginForm.password = '';
          this.auth = 'in';
          UI.ok('登录成功，欢迎回来');
          this.enterApp();
        } catch (e) {
          UI.fail(e);
        } finally {
          this.logging = false;
        }
      },

      switchMode: function (m) {
        this.mode = m;
        this.logging = false;
      },

      /** 注册新账号：成功后回到登录页并带出用户名 */
      doRegister: async function () {
        if (this.logging) return;
        var u = (this.regForm.username || '').trim();
        var n = (this.regForm.nickname || '').trim();
        var p = this.regForm.password || '';
        var c = this.regForm.confirm || '';
        if (!u || !p) {
          UI.warn('请输入用户名和密码');
          return;
        }
        if (p.length < 6) {
          UI.warn('密码长度至少 6 位');
          return;
        }
        if (p !== c) {
          UI.warn('两次输入的密码不一致');
          return;
        }
        this.logging = true;
        try {
          await API.post('/api/user/register', { username: u, password: p, nickname: n });
          UI.ok('注册成功，请登录');
          this.loginForm.username = u;
          this.loginForm.password = '';
          this.regForm = { username: '', nickname: '', password: '', confirm: '' };
          this.mode = 'login';
        } catch (e) {
          UI.fail(e);
        } finally {
          this.logging = false;
        }
      },

      logout: function () {
        var self = this;
        window.ElementPlus.ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
          type: 'warning',
          confirmButtonText: '退出',
          cancelButtonText: '取消'
        }).then(function () {
          API.post('/api/user/logout').catch(function () {});
          self.user = {};
          self.online = null;
          self.mode = 'login';
          self.loginForm = { username: '', password: '' };
          self.regForm = { username: '', nickname: '', password: '', confirm: '' };
          self.activeKey = 'dashboard';
          self.auth = 'guest';
          window.location.hash = '';
        }).catch(function () {});
      },

      /** 登录成功后：同步路由并探测后端连通性 */
      enterApp: function () {
        this.syncFromHash();
        this.checkHealth();
      },

      /** 会话过期：API 层派发 hr-unauthorized 事件后退回登录页 */
      onUnauthorized: function () {
        if (this.auth === 'in') {
          this.user = {};
          this.online = null;
          this.auth = 'guest';
        }
      },

      /** 用经营者列表接口做连通性探针 */
      checkHealth: async function () {
        try {
          await API.get('/api/operator/list');
          this.online = true;
        } catch (e) {
          this.online = false;
        }
      }
    },

    mounted: function () {
      window.addEventListener('hashchange', this.syncFromHash);
      window.addEventListener('hr-unauthorized', this.onUnauthorized);
      this.checkLogin();
    }
  };

  var app = window.Vue.createApp(App);

  app.use(window.ElementPlus, {
    locale: window.ElementPlusLocaleZhCn || undefined
  });

  app.config.globalProperties.UI = window.UI;
  app.config.globalProperties.API = window.API;

  var views = window.HRViews || {};
  Object.keys(views).forEach(function (k) {
    app.component(k, views[k]);
  });

  app.mount('#app');
})();
