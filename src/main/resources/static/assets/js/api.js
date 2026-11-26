/**
 * API 层：统一封装对 SmartHedge_Risk_Hub 后端 /api/** 的调用。
 *
 * 约定：
 *  - 前端静态资源与后端同源同端口（放在 src/main/resources/static），因此无需跨域配置。
 *  - 后端统一返回 Result<T>：{ success, code, msg, data, total }
 *  - MyBatis-Plus 分页参数统一为 page / size / keyword。
 */
window.API = (function () {
  'use strict';

  var BASE = '';

  /** 拼 URL query，跳过空值 */
  function buildURL(path, params) {
    var url = BASE + path;
    if (!params) return url;
    var qs = new URLSearchParams();
    Object.keys(params).forEach(function (k) {
      var v = params[k];
      if (v !== undefined && v !== null && v !== '') qs.append(k, v);
    });
    var s = qs.toString();
    if (s) url += (url.indexOf('?') >= 0 ? '&' : '?') + s;
    return url;
  }

  function pickMsg(payload, resp) {
    if (payload && typeof payload === 'object') {
      return payload.message || payload.msg || payload.error || null;
    }
    return null;
  }

  /** 底层请求：返回后端 Result 原始对象 */
  async function request(path, options) {
    options = options || {};
    var method = (options.method || 'GET').toUpperCase();
    var url = buildURL(path, options.params);
    var init = { method: method, headers: {}, credentials: 'same-origin' };

    if (options.body !== undefined) {
      init.headers['Content-Type'] = 'application/json;charset=UTF-8';
      init.body = JSON.stringify(options.body);
    }

    var resp;
    try {
      resp = await fetch(url, init);
    } catch (e) {
      throw new Error('无法连接后端服务，请确认 SmartHedge_Risk_Hub 已启动');
    }

    var text = '';
    try { text = await resp.text(); } catch (e) { text = ''; }

    var payload = null;
    if (text) {
      try { payload = JSON.parse(text); } catch (e) { payload = null; }
    }

    /* 登录态失效：通知应用层退回登录页 */
    if (resp.status === 401 || (payload && payload.code === 401)) {
      window.dispatchEvent(new Event('hr-unauthorized'));
    }

    if (!resp.ok) {
      throw new Error(pickMsg(payload) || text.slice(0, 200) || ('HTTP ' + resp.status));
    }
    if (!payload || typeof payload !== 'object') {
      throw new Error('后端返回的不是合法 JSON');
    }
    return payload;
  }

  /**
   * 解包 Result。
   * 注意：/api/optionContractText/findByPage 存在 Result 内再包一层 Result 的情况，这里做兼容。
   * @returns {*} data
   */
  function unwrap(res) {
    if (!res || typeof res !== 'object') throw new Error('空响应');
    if (res.success === false) throw new Error(res.msg || '操作失败');

    var data = res.data;
    var total = res.total || 0;

    if (data && typeof data === 'object' && !Array.isArray(data) &&
        Object.prototype.hasOwnProperty.call(data, 'success') &&
        Object.prototype.hasOwnProperty.call(data, 'data')) {
      if (data.success === false) throw new Error(data.msg || '操作失败');
      total = data.total || total;
      data = data.data;
    }
    if (Array.isArray(data)) {
      if (!total) total = data.length;
    }
    return { data: data, total: total };
  }

  async function get(path, params) {
    return unwrap(await request(path, { params: params })).data;
  }

  async function post(path, body, params) {
    return unwrap(await request(path, { method: 'POST', body: body, params: params })).data;
  }

  async function put(path, body, params) {
    return unwrap(await request(path, { method: 'PUT', body: body, params: params })).data;
  }

  async function del(path, params) {
    return unwrap(await request(path, { method: 'DELETE', params: params })).data;
  }

  /**
   * 分页列表
   * 统一返回 { rows, total }：rows 为当前页记录数组，total 为总记录数。
   * 注意：unwrap 返回的字段名是 data，而各列表视图统一按 r.rows 取值，
   * 因此这里必须做一次字段映射，否则表格会一直渲染成「暂无数据」，
   * 而分页器却能正确显示总数（因为 total 字段名一致）。
   * @param path 接口路径
   * @param opts { keyword, page, size, extra }  extra 为附加查询参数（如 varietyName）
   */
  async function page(path, opts) {
    opts = opts || {};
    var params = {
      keyword: opts.keyword,
      page: opts.page || 1,
      size: opts.size || 10
    };
    Object.assign(params, opts.extra || {});
    var res = unwrap(await request(path, { params: params }));
    return { rows: res.data || [], total: res.total || 0 };
  }

  return {
    request: request,
    get: get,
    post: post,
    put: put,
    del: del,
    page: page
  };
})();
