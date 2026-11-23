/* Zones page (Part B, Composite). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const collapsed = new Set();
    let data = null;

    const fail = (e) => toast(e.message, true);
    const kwh = (v) => Number(v).toFixed(1);

    async function load() {
        data = await api('GET', '/zones/tree');
        renderStats();
        $('tree').replaceChildren(h('ul', { class: 'tree' }, nodeEl(data.root)));
        $('windowNote').textContent = 'Usage = newest ' + data.usageWindowReadings + ' readings of each active meter';
    }

    function renderStats() {
        const stat = (n, label) => h('div', { class: 'stat' }, h('div', { class: 'stat-n' }, String(n)),
            h('div', { class: 'small muted' }, label));
        const t = data.perType;
        $('stats').replaceChildren(
            stat(kwh(data.root.kwh) + ' kWh', 'City usage'),
            stat(data.totalZones, 'Zones in total'),
            stat(data.zonesOff, 'Zones off'),
            stat(data.levels, 'Levels deep'),
            stat(`${t.DISTRICT} / ${t.STREET} / ${t.BUILDING}`, 'Districts / streets / buildings'));
    }

    function nodeEl(n) {
        const hasKids = n.children.length > 0;
        const isOff = n.power === 'OFF';
        const open = !collapsed.has(n.id);
        const rootKwh = Number(data.root.kwh);
        const pct = rootKwh > 0 ? Math.min(100, Number(n.kwh) / rootKwh * 100) : 0;

        const row = h('div', { class: 'znode' + (isOff ? ' dim' : '') },
            hasKids
                ? h('button', { class: 'ztoggle', type: 'button', title: 'Expand or collapse', onclick: () => toggle(n.id) },
                    open ? '▾' : '▸')
                : h('span', {}),
            h('span', { class: 'zname' }, h('b', {}, n.name), ' ',
                h('span', { class: 'small muted' }, hasKids ? `(${n.buildings} buildings)` : '')),
            h('span', { class: 'small muted ztype' }, n.type),
            h('span', {}, h('span', { class: 'badge ' + (isOff ? 'off' : 'ok') }, n.power)),
            h('div', { class: 'zbar zbarcell' }, h('div', { style: 'width:' + pct + '%' })),
            h('span', { class: 'mono small num zkwh' }, kwh(n.kwh) + ' kWh'),
            h('button', {
                class: 'btn small' + (isOff ? '' : ' danger'), type: 'button',
                onclick: () => act(n, isOff ? 'restore' : 'shutdown')
            }, isOff ? 'Restore' : 'Shut down'));

        const li = h('li', {}, row);
        if (hasKids && open) {
            li.append(h('ul', {}, n.children.map(nodeEl)));
        }
        return li;
    }

    function toggle(id) {
        if (collapsed.has(id)) collapsed.delete(id); else collapsed.add(id);
        $('tree').replaceChildren(h('ul', { class: 'tree' }, nodeEl(data.root)));
    }

    async function act(n, kind) {
        const wide = n.children.length > 0
            ? ` This also affects ${n.buildings} building(s) below it.` : '';
        if (!confirm((kind === 'shutdown' ? 'Shut down ' : 'Restore power to ') + n.name + '?' + wide)) return;
        try {
            showResult(await api('POST', `/zones/${n.id}/${kind}`));
        } catch (e) { fail(e); }
        await load().catch(fail);
    }

    function showResult(r) {
        $('resultCard').hidden = false;
        $('result').replaceChildren(
            h('p', {}, h('b', {}, r.action === 'SHUTDOWN' ? 'Shut down ' : 'Restored '), r.zoneName, ': ',
                `${r.zonesChanged} zone(s) changed, usage ${kwh(r.kwhBefore)} → ${kwh(r.kwhAfter)} kWh.`),
            r.zonesChanged
                ? h('div', { class: 'chips' }, r.changed.map((z) => h('span', { class: 'chip' }, `${z.name} (${z.type})`)))
                : h('p', { class: 'small muted' }, 'Nothing needed changing.'));
        toast(r.zoneName + (r.action === 'SHUTDOWN' ? ' switched off' : ' restored'));
    }

    load().catch(fail);
})();