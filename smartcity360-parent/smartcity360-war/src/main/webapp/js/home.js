/* Home page live figures (Phase 8). Uses api(), h() from api.js. Quietly does nothing if the page has no #kpiBox. */
(() => {
    const box = document.getElementById('kpiBox');
    if (!box) return;
    const PALETTE = ['#2563eb', '#d97706', '#dc2626', '#16a34a', '#6b7280'];
    const BADGE = { OPEN: 'warn', IN_PROGRESS: 'warn', ESCALATED: 'crit', RESOLVED: 'ok', CLOSED: 'off' };
    const SEV = { LOW: 'off', MEDIUM: 'warn', HIGH: 'crit', CRITICAL: 'crit' };
    const nf = new Intl.NumberFormat();

    const kpi = (label, value, sub) => h('div', { class: 'kpi' },
        h('div', { class: 'k-label' }, label), h('div', { class: 'k-value' }, value), h('div', { class: 'k-sub' }, sub));

    function render(d) {
        const c = d.complaintsByStatus;
        const open = (c.OPEN || 0) + (c.IN_PROGRESS || 0) + (c.ESCALATED || 0);
        const total = Object.values(c).reduce((a, b) => a + b, 0);
        if (d.scope === 'city') {
            const l = d.lightsByStatus;
            const lights = Object.values(l).reduce((a, b) => a + b, 0);
            box.replaceChildren(
                kpi('Open complaints', nf.format(open), (c.ESCALATED || 0) + ' escalated'),
                kpi('Meters online', d.metersActive + ' / ' + d.metersTotal, 'reporting readings'),
                kpi('Energy, last 24 h', nf.format(d.energy24h) + ' kWh', 'all active meters'),
                kpi('Streetlights', nf.format(lights), (l.FAULTY || 0) + ' faulty, ' + (l.OFF || 0) + ' off'),
                kpi('Policy violations', nf.format(d.violations24h), 'last 24 hours'),
                kpi('Active sensors', nf.format(d.sensorsActive), 'noise, heat, air'));
        } else {
            box.replaceChildren(
                kpi('My open complaints', nf.format(open), 'being handled'),
                kpi('Resolved', nf.format((c.RESOLVED || 0) + (c.CLOSED || 0)), 'closed out'),
                kpi('Total raised', nf.format(total), 'all time'));
        }
        recent(d.recentComplaints);
        chart(c);
    }

    function recent(list) {
        const body = document.getElementById('recentRows');
        if (!body) return;
        body.replaceChildren(...(list.length ? list.map((r) => h('tr', {},
            h('td', { class: 'muted' }, '#' + r.id),
            h('td', {}, r.title),
            h('td', {}, h('span', { class: 'badge ' + (SEV[r.severity] || 'off') }, r.severity)),
            h('td', {}, h('span', { class: 'badge ' + (BADGE[r.status] || 'off') }, r.status.replace('_', ' '))))
        ) : [h('tr', {}, h('td', { colspan: 4, class: 'empty' }, 'No complaints yet.'))]));
    }

    function chart(c) {
        const canvas = document.getElementById('cStatus');
        if (!canvas || typeof Chart === 'undefined') { if (canvas) canvas.parentElement.hidden = true; return; }
        const labels = Object.keys(c).map((k) => k.replace('_', ' '));
        new Chart(canvas, {
            type: 'doughnut',
            data: { labels, datasets: [{ data: Object.values(c), backgroundColor: PALETTE, borderWidth: 1 }] },
            options: { responsive: true, maintainAspectRatio: false, cutout: '62%',
                plugins: { legend: { position: 'bottom' } } }
        });
    }

    api('GET', '/dashboard/summary').then(render).catch((e) => {
        box.replaceChildren(h('div', { class: 'muted small' }, 'Live figures are unavailable right now.'));
        console.warn(e);
    });
})();
