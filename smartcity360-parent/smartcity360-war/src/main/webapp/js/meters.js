/* Meters page (Part A, Decorator). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const CONFIGS = { AUTHENTICATION: ['TOKEN'], COMPRESSION: ['GZIP'], ENCRYPTION: ['AES-128', 'AES-256'] };

    let meters = [];
    let selectedId = null;

    const current = () => meters.find((m) => m.id === selectedId);
    const fail = (e) => toast(e.message, true);
    const fmtTime = (t) => t.replace('T', ' ').slice(0, 19) + ' UTC';
    const badge = (status) =>
        h('span', { class: 'badge ' + (status === 'ACTIVE' ? 'ok' : status === 'FAULTY' ? 'warn' : 'off') }, status);

    // ------------------------------------------------------------ meter list

    async function loadMeters() {
        meters = await api('GET', '/meters');
        renderMeters();
        const m = current();
        if (m) renderDetail(m);
    }

    function renderMeters() {
        const body = $('meterRows');
        body.replaceChildren();
        for (const m of meters) {
            body.append(h('tr', { class: m.id === selectedId ? 'sel' : '', onclick: () => select(m.id) },
                h('td', {}, h('b', {}, m.meterCode)),
                h('td', {}, m.zoneName),
                h('td', {}, badge(m.status)),
                h('td', { class: 'mono small' }, m.chain)));
        }
    }

    function select(id) {
        selectedId = id;
        $('result').hidden = true;
        renderMeters();
        renderDetail(current());
        loadAudit(id).catch(fail);
    }

    // ------------------------------------------------------------ detail panel

    function renderDetail(m) {
        $('emptyDetail').hidden = true;
        $('detail').hidden = false;
        $('dTitle').textContent = m.meterCode + ' · ' + m.zoneName;
        $('dStatus').replaceChildren(badge(m.status));
        renderFlow(m);
        renderLayers(m);
        const next = m.layers.reduce((max, l) => Math.max(max, l.layerOrder), 0) + 1;
        $('layerOrder').value = Math.min(next, 20);
    }

    function renderFlow(m) {
        const box = $('flow');
        box.replaceChildren();
        const arrow = () => h('span', { class: 'arrow' }, '→');
        box.append(h('span', { class: 'pill core' }, 'Readings'));
        if (m.chain.startsWith('INVALID')) {
            box.append(arrow(), h('span', { class: 'pill bad' }, m.chain));
            $('flowNote').textContent = 'A layer has an unsupported config. Fix or remove it.';
            return;
        }
        const layers = m.chain.split('>').slice(1); // first item is "Raw"
        for (const name of layers) {
            box.append(arrow(), h('span', { class: 'pill ' + name.toLowerCase() }, name));
        }
        box.append(arrow(), h('span', { class: 'pill core' }, 'City platform'));
        $('flowNote').textContent = layers.length
            ? 'Left to right is the order the layers touch the data on the way out.'
            : 'No layers: the data travels as plain bytes.';
    }

    function renderLayers(m) {
        const body = $('layerRows');
        body.replaceChildren();
        if (!m.layers.length) {
            body.append(h('tr', {}, h('td', { colspan: 5, class: 'muted' }, 'No layers yet.')));
            return;
        }
        for (const l of m.layers) {
            body.append(h('tr', {},
                h('td', {}, String(l.layerOrder)),
                h('td', {}, l.layerType),
                h('td', { class: 'mono' }, l.config || ''),
                h('td', {}, h('input', { type: 'checkbox', checked: l.enabled, onchange: (e) => setEnabled(l, e.target.checked) })),
                h('td', {}, h('button', { class: 'btn small danger', type: 'button', onclick: () => removeLayer(l) }, 'Remove'))));
        }
    }

    async function setEnabled(layer, enabled) {
        try {
            await api('PUT', `/meters/${selectedId}/layers/${layer.id}`, { enabled });
            toast(layer.layerType + (enabled ? ' switched on' : ' switched off'));
        } catch (e) { fail(e); }
        await loadMeters();
    }

    async function removeLayer(layer) {
        if (!confirm('Remove the ' + layer.layerType + ' layer?')) return;
        try {
            await api('DELETE', `/meters/${selectedId}/layers/${layer.id}`);
            toast(layer.layerType + ' layer removed');
        } catch (e) { fail(e); }
        await loadMeters();
    }

    function fillConfigs() {
        const sel = $('layerConfig');
        sel.replaceChildren(...CONFIGS[$('layerType').value].map((c) => h('option', { value: c }, c)));
    }

    async function addLayer(ev) {
        ev.preventDefault();
        try {
            await api('POST', `/meters/${selectedId}/layers`, {
                layerType: $('layerType').value,
                layerOrder: Number($('layerOrder').value),
                config: $('layerConfig').value,
                enabled: true
            });
            toast($('layerType').value + ' layer added');
            await loadMeters();
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ transmit + audit

    async function transmit() {
        const btn = $('btnTransmit');
        btn.disabled = true;
        try {
            const r = await api('POST', `/meters/${selectedId}/transmit`);
            renderResult(r);
            await loadAudit(selectedId);
        } catch (e) {
            $('result').hidden = true;
            fail(e);
        } finally {
            btn.disabled = false;
        }
    }

    function bar(label, bytes, percent) {
        return h('div', { class: 'bar-row' },
            h('span', { class: 'small' }, label),
            h('div', { class: 'bar' }, h('div', { class: 'bar-fill', style: 'width:' + percent + '%' })),
            h('span', { class: 'mono small num' }, bytes + ' B'));
    }

    function renderResult(r) {
        const grew = r.finalBytes > r.rawBytes;
        const box = $('result');
        box.hidden = false;
        box.replaceChildren(
            h('div', { class: 'res-head' },
                h('b', {}, r.meterCode),
                h('span', { class: 'mono small' }, r.chain),
                h('span', { class: 'badge ' + (r.roundTripOk ? 'ok' : 'warn') }, r.roundTripOk ? 'round trip verified' : 'MISMATCH')),
            bar('Raw payload', r.rawBytes, 100),
            bar('On the wire', r.finalBytes, Math.min(r.sizePercent, 100)),
            h('p', { class: 'small' },
                `${r.finalBytes} of ${r.rawBytes} bytes (${r.sizePercent}%)` +
                (grew ? ': larger than the original, the layers added overhead.' : '.')),
            h('div', { class: 'small muted' }, 'First 32 bytes on the wire (hex)'),
            h('div', { class: 'mono hex' }, r.wirePreviewHex),
            h('div', { class: 'small muted' }, 'Audit record #' + r.auditId));
    }

    async function loadAudit(id) {
        const rows = await api('GET', `/meters/${id}/audit?limit=10`);
        const body = $('auditRows');
        body.replaceChildren();
        if (!rows.length) {
            body.append(h('tr', {}, h('td', { colspan: 5, class: 'muted' }, 'Nothing sent yet.')));
            return;
        }
        for (const a of rows) {
            body.append(h('tr', {},
                h('td', {}, '#' + a.id),
                h('td', { class: 'small' }, fmtTime(a.processedAt)),
                h('td', { class: 'mono small' }, a.layersApplied),
                h('td', { class: 'num' }, String(a.rawBytes)),
                h('td', { class: 'num' }, String(a.finalBytes))));
        }
    }

    // ------------------------------------------------------------ new meter

    async function loadZones() {
        const zones = await api('GET', '/meters/building-zones');
        $('newZone').replaceChildren(...zones.map((z) => h('option', { value: z.id }, `${z.name} (${z.code})`)));
    }

    async function createMeter(ev) {
        ev.preventDefault();
        try {
            const created = await api('POST', '/meters', {
                meterCode: $('newCode').value.trim(),
                zoneId: Number($('newZone').value)
            });
            toast('Meter ' + created.meterCode + ' created');
            $('newMeterForm').hidden = true;
            $('newMeterForm').reset();
            selectedId = created.id;
            await loadMeters();
            select(created.id);
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ start

    $('btnNewMeter').addEventListener('click', () => { $('newMeterForm').hidden = !$('newMeterForm').hidden; });
    $('newMeterForm').addEventListener('submit', createMeter);
    $('layerForm').addEventListener('submit', addLayer);
    $('layerType').addEventListener('change', fillConfigs);
    $('btnTransmit').addEventListener('click', transmit);
    fillConfigs();
    loadMeters().catch(fail);
    loadZones().catch(fail);
})();