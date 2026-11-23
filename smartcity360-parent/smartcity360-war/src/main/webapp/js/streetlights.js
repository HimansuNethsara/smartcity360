/* Streetlights page (Part D, Flyweight). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const STATUSES = ['ON', 'OFF', 'FAULTY'];
    const BADGE = { ON: 'ok', OFF: 'off', FAULTY: 'warn' };
    const PAGE_SIZE = 15;
    const isAdmin = document.body.dataset.admin === 'true';
    let page = 0;
    let total = 0;

    const fail = (e) => toast(e.message, true);
    const n = (v) => Number(v).toLocaleString();
    const bytes = (b) => b >= 1048576 ? (b / 1048576).toFixed(2) + ' MB' : (b / 1024).toFixed(1) + ' KB';

    // ------------------------------------------------------------ summary

    async function loadSummary() {
        const s = await api('GET', '/streetlights/summary');
        const stat = (value, label) => h('div', { class: 'stat' }, h('div', { class: 'stat-n' }, value),
            h('div', { class: 'small muted' }, label));
        $('stats').replaceChildren(
            stat(n(s.totalLights), 'Lights in the city'),
            stat((s.totalWatts / 1000).toFixed(1) + ' kW', 'Power draw now'),
            stat((s.totalLumens / 1e6).toFixed(2) + ' M lm', 'Light output now'),
            stat(`${n(s.byStatus.ON)} / ${n(s.byStatus.OFF)} / ${n(s.byStatus.FAULTY)}`, 'On / off / faulty'),
            stat(s.byType.length, 'Shared lamp models'));

        $('typeRows').replaceChildren(...s.byType.map((t) => h('tr', {},
            h('td', {}, h('b', {}, t.typeCode), ' ', h('span', { class: 'small muted' }, t.model)),
            h('td', {}, t.lampType),
            h('td', { class: 'num' }, String(t.wattage)),
            h('td', { class: 'num' }, n(t.lights)))));

        const p = s.pool;
        $('poolNote').textContent = p.requests
            ? `The shared pool holds ${p.poolSize} model object(s). It was asked for a model ${n(p.requests)} times ` +
            `and had to build one only ${n(p.created)} times; ${n(p.hits)} requests reused an existing one.`
            : 'The pool is empty. It fills the first time lights are read.';
    }

    // ------------------------------------------------------------ memory comparison

    async function compare(ev) {
        ev.preventDefault();
        const btn = ev.submitter;
        btn.disabled = true;
        $('memResult').replaceChildren(h('p', { class: 'small muted' }, 'Measuring...'));
        try {
            const m = await api('GET', '/streetlights/memory?count=' + encodeURIComponent($('memCount').value));
            const pct = (m.flyweightEstimatedBytes / m.naiveEstimatedBytes * 100).toFixed(1);
            const bar = (label, text, width, alt) => h('div', { class: 'bar-row wide' },
                h('span', { class: 'small' }, label),
                h('div', { class: 'bar' }, h('div', { class: 'bar-fill' + (alt ? ' alt' : ''), style: 'width:' + width + '%' })),
                h('span', { class: 'mono small num' }, text));
            $('memResult').replaceChildren(
                h('p', { class: 'small' }, h('b', {}, n(m.lights)), ' lights, ', h('b', {}, String(m.typeObjectsInFlyweight)),
                    ' shared model objects instead of ', h('b', {}, n(m.lights)), ' copies.'),
                bar('Without sharing', bytes(m.naiveEstimatedBytes), 100, true),
                bar('With sharing', bytes(m.flyweightEstimatedBytes), pct, false),
                h('p', { class: 'small' }, `Estimated saving: ${m.estimatedSavedPercent}%.`),
                h('p', { class: 'small muted' }, m.naiveMeasuredBytes > 0 && m.flyweightMeasuredBytes > 0
                    ? `Measured on the real heap: ${bytes(m.naiveMeasuredBytes)} without sharing, ${bytes(m.flyweightMeasuredBytes)} with. ` +
                    'The measured figures vary from run to run; the estimate uses a fixed model.'
                    : 'The server could not measure the real heap on this run (the garbage collector moved memory around). ' +
                    'The estimate uses a fixed model, so it is still valid.'));
        } catch (e) {
            $('memResult').replaceChildren();
            fail(e);
        } finally {
            btn.disabled = false;
        }
    }

    // ------------------------------------------------------------ generator

    async function generate(ev) {
        ev.preventDefault();
        const btn = ev.submitter;
        btn.disabled = true;
        try {
            const r = await api('POST', '/streetlights/generate', {
                count: Number($('genCount').value),
                zoneId: $('genZone').value ? Number($('genZone').value) : null
            });
            toast(`${n(r.created)} lights added in ${r.streetZonesUsed} street zone(s). City total: ${n(r.totalLights)}`);
            page = 0;
            await refresh();
        } catch (e) { fail(e); } finally { btn.disabled = false; }
    }

    async function removeGenerated() {
        if (!confirm('Remove every generated light? The original lights stay.')) return;
        try {
            const r = await api('DELETE', '/streetlights/generated');
            toast(`${n(-r.created)} generated lights removed. City total: ${n(r.totalLights)}`);
            page = 0;
            await refresh();
        } catch (e) { fail(e); }
    }

    async function resetPool() {
        try {
            await api('POST', '/streetlights/pool/reset');
            toast('Shared pool emptied');
            await loadSummary();
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ light list

    async function loadZones() {
        const zones = await api('GET', '/streetlights/street-zones');
        $('genZone').replaceChildren(h('option', { value: '' }, 'All street zones'),
            ...zones.map((z) => h('option', { value: z.id }, z.name)));
        $('fZone').replaceChildren(h('option', { value: '' }, 'Any street'),
            ...zones.map((z) => h('option', { value: z.id }, z.name)));
    }

    async function loadPage() {
        const qs = new URLSearchParams({ page, size: PAGE_SIZE });
        if ($('fZone').value) qs.set('zone', $('fZone').value);
        if ($('fStatus').value) qs.set('status', $('fStatus').value);
        const r = await api('GET', '/streetlights?' + qs);
        total = r.total;
        const body = $('rows');
        body.replaceChildren();
        if (!r.items.length) {
            body.append(h('tr', {}, h('td', { colspan: 8, class: 'muted' }, 'No lights match.')));
        }
        for (const l of r.items) {
            const status = h('select', { disabled: !isAdmin }, STATUSES.map((s) => h('option', { value: s, selected: s === l.status }, s)));
            const bright = h('input', { type: 'number', min: 0, max: 100, value: l.brightness, disabled: !isAdmin });
            body.append(h('tr', {},
                h('td', {}, '#' + l.id),
                h('td', {}, l.typeCode),
                h('td', {}, l.zoneName),
                h('td', {}, status),
                h('td', {}, bright, ' %'),
                h('td', { class: 'num' }, String(l.powerWatts)),
                h('td', { class: 'num' }, n(l.lumens)),
                h('td', {}, isAdmin ? h('button', { class: 'btn small', type: 'button', onclick: () => save(l.id, status, bright) }, 'Save') : '')));
        }
        const from = total === 0 ? 0 : page * PAGE_SIZE + 1;
        $('pageInfo').textContent = `${n(from)}–${n(Math.min((page + 1) * PAGE_SIZE, total))} of ${n(total)}`;
        $('total').textContent = n(total) + ' lights';
        $('prev').disabled = page === 0;
        $('next').disabled = (page + 1) * PAGE_SIZE >= total;
    }

    async function save(id, statusEl, brightEl) {
        try {
            await api('PUT', '/streetlights/' + id, { status: statusEl.value, brightness: Number(brightEl.value) });
            toast('Light #' + id + ' updated');
            await Promise.all([loadPage(), loadSummary()]);
        } catch (e) { fail(e); }
    }

    const refresh = () => Promise.all([loadSummary(), loadPage()]);

    // ------------------------------------------------------------ start

    $('fStatus').append(...STATUSES.map((s) => h('option', { value: s }, s)));
    $('memForm').addEventListener('submit', compare);
    $('genForm').addEventListener('submit', generate);
    $('btnRemove').addEventListener('click', removeGenerated);
    $('btnReset').addEventListener('click', resetPool);
    $('fZone').addEventListener('change', () => { page = 0; loadPage().catch(fail); });
    $('fStatus').addEventListener('change', () => { page = 0; loadPage().catch(fail); });
    $('prev').addEventListener('click', () => { page--; loadPage().catch(fail); });
    $('next').addEventListener('click', () => { page++; loadPage().catch(fail); });
    loadZones().catch(fail);
    refresh().catch(fail);
})();
