/**
 * UI 层：日期格式化、消息提示、删除确认、列表页公共 mixin。
 */
window.UI = (function () {
  'use strict';

  function pad(n) { return String(n).padStart(2, '0'); }

  /**
   * 后端 java.util.Date 默认序列化为时间戳（数字），也可能是字符串，这里统一兼容。
   */
  function toDate(v) {
    if (v === null || v === undefined || v === '') return null;
    if (v instanceof Date) return isNaN(v.getTime()) ? null : v;
    if (typeof v === 'number') {
      var d = new Date(v);
      return isNaN(d.getTime()) ? null : d;
    }
    var s = String(v).trim().replace('T', ' ').replace(/\..+$/, '');
    var d2 = new Date(s.replace(/-/g, '/'));
    return isNaN(d2.getTime()) ? null : d2;
  }

  function fmtDate(v) {
    var d = toDate(v);
    if (!d) return '—';
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
  }

  function fmtDateTime(v) {
    var d = toDate(v);
    if (!d) return '—';
    return fmtDate(d) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
  }

  /** el-date-picker 的 Date 对象转 'yyyy-MM-dd' */
  function toDayString(v) {
    var d = toDate(v);
    return d ? (d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())) : '';
  }

  function dash(v) {
    return (v === null || v === undefined || v === '') ? '—' : v;
  }

  function ElMsg() { return window.ElementPlus.ElMessage; }
  function ElBox() { return window.ElementPlus.ElMessageBox; }

  function ok(msg) { ElMsg().success(msg || '操作成功'); }
  function warn(msg) { ElMsg().warning(msg || '提示'); }

  function fail(e) {
    ElMsg().error((e && e.message) ? e.message : '操作失败');
  }

  /** 统一异常包裹，避免每个方法都写 try/catch */
  async function guard(fn, successMsg) {
    try {
      await fn();
      if (successMsg) ok(successMsg);
      return true;
    } catch (e) {
      fail(e);
      return false;
    }
  }

  /** 危险操作确认 */
  async function confirm(title, text) {
    try {
      await ElBox().confirm(text || '此操作不可撤销，确认继续？', title || '危险操作确认', {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      });
      return true;
    } catch (e) {
      return false;
    }
  }

  /** 状态标签：1 启用 / 0 停用，其余原样显示 */
  function statusTag(status) {
    return Number(status) === 1
      ? { text: '启用', type: 'success' }
      : { text: '停用', type: 'info' };
  }

  /** 安全加载下拉选项，失败返回空数组不阻塞页面 */
  async function safe(fn, fallback) {
    try { return await fn(); } catch (e) { return fallback === undefined ? [] : fallback; }
  }

  function unique(arr) {
    return arr.filter(function (x, i) { return x && arr.indexOf(x) === i; });
  }

  return {
    pad: pad,
    toDate: toDate,
    fmtDate: fmtDate,
    fmtDateTime: fmtDateTime,
    toDayString: toDayString,
    dash: dash,
    ok: ok,
    warn: warn,
    fail: fail,
    guard: guard,
    confirm: confirm,
    statusTag: statusTag,
    safe: safe,
    unique: unique
  };
})();

/**
 * 列表页公共 mixin：分页状态与翻页动作。
 * 各视图需自行实现 load() 方法。
 */
window.ListMixin = {
  data: function () {
    return {
      rows: [],
      total: 0,
      page: 1,
      size: 10,
      keyword: '',
      loading: false
    };
  },
  methods: {
    search: function () { this.page = 1; this.load(); },
    resetKeyword: function () { this.keyword = ''; this.search(); },
    onPageChange: function (p) { this.page = p; this.load(); },
    onSizeChange: function (s) { this.size = s; this.page = 1; this.load(); }
  }
};
