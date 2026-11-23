/* Policies page (Part E, Interpreter). Uses api(), h() and toast() from api.js. */
(() => {
  const $ = (id) => document.getElementById(id);
  const EXAMPLES = [
    ['Noise at night', 'IF noise > 60 AND hour >= 22 THEN FINE 3000'],
    ['Heat alert', 'IF temperature > 35 THEN ALERT "Heat warning"'],
    ['Either-or', 'IF (noise > 75 OR air_quality > 150) AND NOT zone = "INDUSTRIAL" THEN FINE 2000']
  ];
  const OUTCOME = { PASS: 'ok', VIOLATION: 'crit', ERROR: 'warn' };

  let current = null;      // the policy being edited, or null for a new one
  let checkTimer = null;
  let checkSeq = 0;

  const fail = (e) => toast(e.message, true);
  const fmtTime = (t) => t ? t.replace('T', ' ').slice(0, 16) + ' UTC' : '-';
  const short = (s, n) => s && s.length > n ? s.slice(0, n - 1) + '…' : (s || '');
  const num = (id) => { const v = $(id).value.trim(); return v === '' ? null : Number(v); };

  // ------------------------------------------------------------ list

  async function loadList(selectId) {
    const list = await api('GET', '/policies');
    $('rows').replaceChildren(...list.map((p) => {
      const tr = h('tr', { class: p.id === selectId ? 'sel' : '', onclick: () => select(p.id) },
        h('td', {}, h('b', {}, p.name)),
        h('td', { class: 'mono small' }, short(p.dslText, 48)),
        h('td', {}, p.valid
          ? h('span', { class: 'badge ' + (p.enabled ? 'ok' : 'off') }, p.enabled ? 'Enabled' : 'Disabled')
          : h('span', { class: 'badge warn' }, 'Invalid')),
        h('td', { class: 'num' }, String(p.version)));
      return tr;
    }));
    if (!list.length) $('rows').replaceChildren(h('tr', {}, h('td', { colspan: 4, class: 'muted' }, 'No policies yet.')));
  }

  // ------------------------------------------------------------ editor

  function fillEditor(p) {
    current = p;
    $('edTitle').textContent = p ? 'Edit policy #' + p.id : 'New policy';
    $('pName').value = p ? p.name : '';
    $('pDesc').value = p && p.description ? p.description : '';
    $('pDsl').value = p ? p.dslText : '';
    $('btnToggle').hidden = !p;
    if (p) $('btnToggle').textContent = p.enabled ? 'Disable' : 'Enable';
    $('verCard').hidden = !p;
    $('evalCard').hidden = !p;
    if (p) $('evName').textContent = p.name;
    $('evResult').hidden = true;
    checkNow();
  }

  function newPolicy() {
    fillEditor(null);
    loadList(null).catch(fail);
  }

  async function select(id) {
    try {
      const p = await api('GET', '/policies/' + id);
      fillEditor(p);
      await Promise.all([loadList(id), loadVersions(id)]);
    } catch (e) { fail(e); }
  }

  // live check while typing: parse on the server, show the error position and the syntax tree
  function scheduleCheck() {
    clearTimeout(checkTimer);
    checkTimer = setTimeout(checkNow, 300);
  }

  async function checkNow() {
    const text = $('pDsl').value;
    const box = $('vBox');
    if (!text.trim()) { box.hidden = true; return; }
    const seq = ++checkSeq;
    try {
      const v = await api('POST', '/policies/validate', { dslText: text });
      if (seq !== checkSeq) return;       // an older answer arriving late
      box.hidden = false;
      if (v.valid) {
        box.className = 'vbox good';
        box.replaceChildren(
          h('div', {}, h('span', { class: 'badge ok' }, 'Valid'), '  ',
            h('span', { class: 'mono small' }, v.canonicalText)),
          h('div', { class: 'small muted', style: 'margin-top:8px' }, 'How the system reads it:'),
          astList(v.ast));
      } else {
        const at = Math.max(0, Math.min(v.position, text.length));
        box.className = 'vbox bad';
        box.replaceChildren(
          h('div', {}, h('span', { class: 'badge warn' }, 'Not valid')),
          h('pre', { class: 'caret mono small' }, text + '\n' + ' '.repeat(at) + '^'),
          h('div', { class: 'small' }, v.error));
      }
    } catch (e) { if (seq === checkSeq) box.hidden = true; }
  }

  function astList(node) {
    return h('ul', { class: 'ast' }, astItem(node));
  }
  function astItem(n) {
    const li = h('li', {}, h('span', { class: 'mono small' }, n.label));
    if (n.children && n.children.length) li.append(h('ul', {}, n.children.map(astItem)));
    return li;
  }

  async function save(ev) {
    ev.preventDefault();
    $('btnSave').disabled = true;
    try {
      const body = { name: $('pName').value.trim(), description: $('pDesc').value.trim() || null,
        dslText: $('pDsl').value.trim() };
      let p;
      if (current) {
        body.version = current.version;
        p = await api('PUT', '/policies/' + current.id, body);
        toast('Saved as a new version');
      } else {
        p = await api('POST', '/policies', body);
        toast('Policy created');
      }
      await select(p.id);
    } catch (e) { fail(e); }
    finally { $('btnSave').disabled = false; }
  }

  async function toggle() {
    try {
      const p = await api('PUT', '/policies/' + current.id + '/enabled', { enabled: !current.enabled });
      toast(p.enabled ? 'Policy enabled' : 'Policy disabled');
      await select(p.id);
    } catch (e) { fail(e); }
  }

  // ------------------------------------------------------------ versions

  async function loadVersions(id) {
    const vs = await api('GET', '/policies/' + id + '/versions');
    $('verRows').replaceChildren(...vs.map((v) => h('tr', {},
      h('td', {}, String(v.versionNo)),
      h('td', {}, h('div', { class: 'mono small' }, v.dslText),
        h('div', { class: 'small muted' }, fmtTime(v.changedAt))),
      h('td', { class: 'small' }, v.changedBy || '-'),
      h('td', {}, v.current
        ? h('span', { class: 'badge ok' }, 'Current')
        : h('button', { class: 'btn small', type: 'button', onclick: () => rollback(v.versionNo) }, 'Restore')))));
  }

  async function rollback(versionNo) {
    if (!confirm('Restore the wording of version ' + versionNo + '? It is saved as a new version.')) return;
    try {
      await api('POST', '/policies/' + current.id + '/rollback', { versionNo });
      toast('Version ' + versionNo + ' restored');
      await select(current.id);
    } catch (e) { fail(e); }
  }

  // ------------------------------------------------------------ evaluate

  function inputChips(json) {
    let o = {};
    try { o = JSON.parse(json); } catch (e) { return h('span', { class: 'muted' }, json || '-'); }
    return h('div', { class: 'chips', style: 'margin:0' },
      Object.entries(o).map(([k, v]) => h('span', { class: 'chip mono' }, k + ' ' + v)));
  }

  async function evaluate(ev) {
    ev.preventDefault();
    const body = { zoneId: $('eZone').value ? Number($('eZone').value) : null,
      noise: num('eNoise'), temperature: num('eTemp'), airQuality: num('eAir'), hour: num('eHour'),
      zone: $('eLand').value || null };
    try {
      const r = await api('POST', '/policies/' + current.id + '/evaluate', body);
      const box = $('evResult');
      box.hidden = false;
      box.replaceChildren(
        h('div', { class: 'res-head' },
          h('span', { class: 'badge ' + OUTCOME[r.outcome] }, r.outcome),
          r.actionResult ? h('b', {}, r.actionResult) : '',
          r.zoneName ? h('span', { class: 'muted' }, 'Zone: ' + r.zoneName) : ''),
        h('div', { class: 'small muted' }, 'Values used:'), inputChips(r.inputJson));
      loadEvaluations().catch(fail);
    } catch (e) { fail(e); }
  }

  async function loadEvaluations() {
    const list = await api('GET', '/policies/evaluations?limit=12');
    $('evRows').replaceChildren(...(list.length ? list.map((r) => h('tr', {},
      h('td', { class: 'small' }, fmtTime(r.evaluatedAt)),
      h('td', {}, r.policyName),
      h('td', {}, r.zoneName || h('span', { class: 'muted' }, '-')),
      h('td', {}, h('span', { class: 'badge ' + OUTCOME[r.outcome] }, r.outcome),
        r.actionResult ? h('div', { class: 'small' }, short(r.actionResult, 40)) : ''),
      h('td', {}, inputChips(r.inputJson))))
      : [h('tr', {}, h('td', { colspan: 5, class: 'muted' }, 'Nothing evaluated yet.'))]));
  }

  async function runAll() {
    $('btnRunAll').disabled = true;
    try {
      const s = await api('POST', '/policies/run-all', {});
      const box = $('runBox');
      box.hidden = false;
      box.replaceChildren(
        h('div', {}, `Checked ${s.policies} policies in ${s.zones} zones: `,
          h('b', {}, s.violations + ' violation(s)'), `, ${s.errors} error(s), `,
          `${s.skipped} not applicable (zone has no sensor for that value).`),
        ...s.findings.map((f) => h('div', { class: 'small', style: 'margin-top:6px' },
          h('span', { class: 'badge ' + OUTCOME[f.outcome] }, f.outcome), '  ',
          `${f.policyName} in ${f.zoneName}: ${f.actionResult || ''}`)));
      loadEvaluations().catch(fail);
    } catch (e) { fail(e); }
    finally { $('btnRunAll').disabled = false; }
  }

  // ------------------------------------------------------------ start

  async function init() {
    $('examples').replaceChildren(...EXAMPLES.map(([name, text]) =>
      h('button', { class: 'chip', type: 'button', onclick: () => { $('pDsl').value = text; checkNow(); } }, name)));
    $('pDsl').addEventListener('input', scheduleCheck);
    $('edForm').addEventListener('submit', save);
    $('evForm').addEventListener('submit', evaluate);
    $('btnNew').addEventListener('click', newPolicy);
    $('btnToggle').addEventListener('click', toggle);
    $('btnRunAll').addEventListener('click', runAll);
    try {
      const o = await api('GET', '/complaints/options');
      $('eZone').replaceChildren(h('option', { value: '' }, '(none, use typed values only)'),
        ...o.zones.map((z) => h('option', { value: z.id }, z.name)));
      await Promise.all([loadList(null), loadEvaluations()]);
    } catch (e) { fail(e); }
  }
  init();
})();
