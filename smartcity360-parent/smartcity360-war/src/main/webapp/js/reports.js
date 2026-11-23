/* Reports page (Phase 6 async jobs, Phase 7 charts). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const NAME = {
        ENERGY_USAGE: 'Energy usage', COMPLAINT_SUMMARY: 'Complaint summary',
        STREETLIGHT_STATUS: 'Streetlight status', POLICY_VIOLATIONS: 'Policy violations'
    };
    const BADGE = { QUEUED: 'off', RUNNING: 'warn', DONE: 'ok', FAILED: 'crit' };
    const PALETTE = ['#2563eb', '#16a34a', '#d97706', '#dc2626', '#7c3aed', '#0891b2', '#db2777', '#65a30d'];
    const fail = (e) => toast(e.message || String(e), true);
    const t = (s) => (s ? s.replace('T', ' ').slice(0, 19) : '-');
    const charts = {};
    let timer = null;

    async function load() {
        const jobs = await api('GET', '/reports');
        $('jobRows').replaceChildren(...(jobs.length ? jobs.map(row)
            : [h('tr', {}, h('td', { colspan: 6, class: 'muted' }, 'No reports yet.'))]));
        const busy = jobs.some((j) => j.status === 'QUEUED' || j.status === 'RUNNING');
        clearTimeout(timer);
        if (busy) timer = setTimeout(() => load().catch(fail), 2000);   // poll only while a job is running
        refreshBell();
    }

    function row(j) {
        return h('tr', {},
            h('td', {}, String(j.id)),
            h('td', {}, NAME[j.reportType] || j.reportType),
            h('td', {}, h('span', { class: 'badge ' + (BADGE[j.status] || 'off') }, j.status),
                j.errorMessage ? h('div', { class: 'small muted' }, j.errorMessage) : ''),
            h('td', { class: 'small' }, t(j.requestedAt)),
            h('td', { class: 'small' }, t(j.completedAt)),
            h('td', {}, j.status === 'DONE'
                ? h('button', { class: 'btn small', type: 'button', onclick: () => show(j.id) }, 'View') : ''));
    }

    async function show(id) {
        try {
            const j = await api('GET', '/reports/' + id);
            const r = JSON.parse(j.resultJson);
            $('resultTitle').textContent = r.title;
            $('resultMeta').textContent = 'Report #' + id + ', generated ' + t(r.generatedAt) + ' UTC';
            draw('chartMain', r);
            $('mainTable').replaceChildren(table(r));
            const s = r.secondary;
            $('secondBlock').hidden = !s;
            if (s) {
                $('secondTitle').textContent = s.title;
                draw('chartSecond', s);
                $('secondTable').replaceChildren(table(s));
            } else if (charts.chartSecond) {
                charts.chartSecond.destroy();
                delete charts.chartSecond;
            }
            $('resultCard').hidden = false;
            $('resultCard').scrollIntoView({ behavior: 'smooth' });
        } catch (e) { fail(e); }
    }

    function draw(canvasId, r) {
        if (charts[canvasId]) { charts[canvasId].destroy(); delete charts[canvasId]; }
        const box = $(canvasId).parentElement;
        if (typeof Chart === 'undefined') { box.hidden = true; return; }
        box.hidden = !r.labels.length;
        const pie = r.chartType === 'doughnut';
        const ds = r.datasets[0];
        charts[canvasId] = new Chart($(canvasId), {
            type: r.chartType,
            data: {
                labels: r.labels,
                datasets: [{
                    label: ds.label,
                    data: ds.data,
                    backgroundColor: pie ? r.labels.map((_, i) => PALETTE[i % PALETTE.length]) : PALETTE[0],
                    borderWidth: pie ? 1 : 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: false,                                  // crisp output when printing
                plugins: { legend: { display: pie }, title: { display: false } },
                scales: pie ? {} : { y: { beginAtZero: true, title: { display: true, text: ds.label } } }
            }
        });
    }

    function table(r) {
        const unit = r.datasets[0].label;
        const body = r.labels.length
            ? r.labels.map((l, i) => h('tr', {}, h('td', {}, l), h('td', { class: 'num' }, String(r.datasets[0].data[i]))))
            : [h('tr', {}, h('td', { colspan: 2, class: 'muted' }, 'No data for this report.'))];
        return h('table', {}, h('thead', {}, h('tr', {}, h('th', {}, 'Item'), h('th', {}, unit))), h('tbody', {}, body));
    }

    $('reqForm').addEventListener('submit', async (ev) => {
        ev.preventDefault();
        $('btnRequest').disabled = true;
        try {
            const j = await api('POST', '/reports', {
                reportType: $('rType').value, hours: Number($('rHours').value) || 24
            });
            toast('Report #' + j.id + ' queued');
            await load();
        } catch (e) { fail(e); } finally { $('btnRequest').disabled = false; }
    });
    $('btnRefresh').addEventListener('click', () => load().catch(fail));
    $('btnPrint').addEventListener('click', () => window.print());
    load().catch(fail);
})();
