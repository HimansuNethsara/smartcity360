/* Complaints page (Part C, Chain of Responsibility). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const CATEGORIES = ['STREETLIGHT', 'NOISE', 'POWER', 'PARKING', 'OTHER'];
    const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const STATUSES = ['OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED'];
    const LEVELS = ['STAFF', 'SUPERVISOR', 'CITY_MANAGER'];
    const NEXT = { OPEN: ['IN_PROGRESS', 'RESOLVED'], ESCALATED: ['IN_PROGRESS', 'RESOLVED'],
        IN_PROGRESS: ['RESOLVED'], RESOLVED: ['CLOSED'], CLOSED: [] };
    const PILL = { STAFF: 'auth', SUPERVISOR: 'compress', CITY_MANAGER: 'encrypt' };
    const SEV_BADGE = { LOW: 'off', MEDIUM: 'ok', HIGH: 'warn', CRITICAL: 'crit' };
    const STATUS_BADGE = { OPEN: 'ok', IN_PROGRESS: 'ok', ESCALATED: 'warn', RESOLVED: 'off', CLOSED: 'off' };
    const STAFF = document.body.dataset.staff === 'true';

    let policies = [];
    let selectedId = null;
    let lastTrace = null;

    const fail = (e) => toast(e.message, true);
    const label = (s) => s.replace('_', ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase());
    const badge = (cls, text) => h('span', { class: 'badge ' + cls }, label(text));
    const levelPill = (l) => h('span', { class: 'pill ' + PILL[l] }, label(l));
    const fmtTime = (t) => t ? t.replace('T', ' ').slice(0, 16) + ' UTC' : '-';

    function dueText(c) {
        const m = c.minutesLeft;
        if (m === null || m === undefined) return h('span', { class: 'muted' }, '-');
        if (m < 0) return h('span', { class: 'due late' }, 'overdue ' + (-m) + ' min');
        return h('span', { class: 'due' }, m < 60 ? m + ' min' : Math.floor(m / 60) + ' h ' + (m % 60) + ' min');
    }

    // ------------------------------------------------------------ form + policy

    async function loadOptions() {
        const o = await api('GET', '/complaints/options');
        policies = o.policies;
        $('fCitizen').replaceChildren(...o.citizens.map((c) => h('option', { value: c.id }, `${c.name} (${c.code})`)));
        $('fZone').replaceChildren(h('option', { value: '' }, '(none)'),
            ...o.zones.map((z) => h('option', { value: z.id }, z.name)));
        $('policyRows').replaceChildren(...policies.map((p) => h('tr', {},
            h('td', {}, badge(SEV_BADGE[p.severity], p.severity)),
            h('td', {}, levelPill(p.startLevel)),
            h('td', { class: 'num' }, String(p.staffTimeoutMin)),
            h('td', { class: 'num' }, String(p.supervisorTimeoutMin)))));
        if (!STAFF) {
            $('fCitizen').closest('.field').hidden = true;   // a citizen always files as themselves
            $('fCitizen').required = false;
            $('btnTimeouts').hidden = true;
        }
        showHint();
    }

    function showHint() {
        const p = policies.find((x) => x.severity === $('fSeverity').value);
        $('sevHint').textContent = p
            ? `${label(p.severity)} complaints start at ${label(p.startLevel)}.` : '';
    }

    async function submitNew(ev) {
        ev.preventDefault();
        try {
            const c = await api('POST', '/complaints', {
                citizenId: STAFF ? Number($('fCitizen').value) : null,
                zoneId: $('fZone').value ? Number($('fZone').value) : null,
                category: $('fCategory').value,
                severity: $('fSeverity').value,
                title: $('fTitle').value,
                description: $('fDesc').value
            });
            toast('Complaint #' + c.id + ' sent to ' + label(c.currentLevel));
            $('fTitle').value = '';
            $('fDesc').value = '';
            selectedId = c.id;
            lastTrace = c.trace;
            await loadList();
            renderDetail(c);
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ list

    async function loadList() {
        const qs = new URLSearchParams();
        if ($('fStatus').value) qs.set('status', $('fStatus').value);
        if ($('fLevel').value) qs.set('level', $('fLevel').value);
        const rows = await api('GET', '/complaints' + (qs.toString() ? '?' + qs : ''));
        const body = $('rows');
        body.replaceChildren();
        if (!rows.length) {
            body.append(h('tr', {}, h('td', { colspan: 6, class: 'muted' }, 'No complaints match.')));
            return;
        }
        for (const c of rows) {
            body.append(h('tr', { class: c.id === selectedId ? 'sel' : '', onclick: () => select(c.id) },
                h('td', {}, '#' + c.id),
                h('td', {}, c.title),
                h('td', {}, badge(SEV_BADGE[c.severity], c.severity)),
                h('td', {}, levelPill(c.currentLevel)),
                h('td', {}, badge(STATUS_BADGE[c.status], c.status)),
                h('td', {}, dueText(c))));
        }
    }

    async function select(id) {
        selectedId = id;
        lastTrace = null;
        try {
            renderDetail(await api('GET', '/complaints/' + id));
            await loadList();
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ detail + actions

    function renderDetail(c) {
        const box = $('detail');
        box.hidden = false;
        const open = c.status !== 'RESOLVED' && c.status !== 'CLOSED';
        const canEscalate = open && c.currentLevel !== 'CITY_MANAGER';

        const parts = [
            h('div', { class: 'card-head' },
                h('h2', {}, `#${c.id} ${c.title}`),
                h('span', {}, badge(STATUS_BADGE[c.status], c.status))),
            h('div', { class: 'flow' }, badge(SEV_BADGE[c.severity], c.severity), levelPill(c.currentLevel),
                h('span', { class: 'small muted' }, c.category.toLowerCase() + (c.zoneName ? ' · ' + c.zoneName : ''))),
            h('p', {}, c.description),
            h('p', { class: 'small' }, h('span', { class: 'muted' }, 'Assigned to: '), c.assignedTo || 'nobody available',
                h('span', { class: 'muted' }, '  ·  Raised by: '), c.citizenName),
            h('p', { class: 'small' }, h('span', { class: 'muted' }, 'Deadline: '),
                c.escalateAt && open ? fmtTime(c.escalateAt) + ' (' : 'none', c.escalateAt && open ? dueText(c) : '',
                c.escalateAt && open ? ')' : '')
        ];

        if (lastTrace && lastTrace.length) {
            parts.push(h('div', { class: 'small muted' }, 'How the chain handled it'),
                h('ul', { class: 'trace' }, lastTrace.map((t) => h('li', { class: t.endsWith('accepted') ? 'ok' : '' }, t))));
        }

        if (open && STAFF) {
            const note = h('input', { type: 'text', id: 'escNote', placeholder: 'Reason for escalating (optional)', maxlength: 255 });
            const row = h('div', { class: 'btnrow' });
            if (canEscalate) {
                row.append(note, h('button', { class: 'btn small', type: 'button', onclick: () => escalate(c.id) },
                    'Escalate to ' + label(LEVELS[LEVELS.indexOf(c.currentLevel) + 1])));
            }
            parts.push(row);
            const st = h('div', { class: 'btnrow' }, h('span', { class: 'small muted' }, 'Move to:'),
                NEXT[c.status].map((s) => h('button', { class: 'btn small', type: 'button', onclick: () => setStatus(c.id, s) }, label(s))));
            if (c.escalateAt && canEscalate) {
                st.append(h('button', { class: 'btn small danger', type: 'button', title: 'Demo only',
                    onclick: () => makeOverdue(c.id) }, 'Demo: make deadline pass'));
            }
            parts.push(st);
        } else if (c.status === 'RESOLVED' && STAFF) {
            parts.push(h('div', { class: 'btnrow' },
                h('button', { class: 'btn small', type: 'button', onclick: () => setStatus(c.id, 'CLOSED') }, 'Close')));
        }

        parts.push(h('h3', {}, 'History'), historyTable(c.history || []));
        box.replaceChildren(...parts);
    }

    function historyTable(rows) {
        return h('table', {},
            h('thead', {}, h('tr', {}, h('th', {}, 'When'), h('th', {}, 'From'), h('th', {}, 'To'), h('th', {}, 'Handler'),
                h('th', {}, 'Reason'), h('th', {}, 'Note'))),
            h('tbody', {}, rows.map((e) => h('tr', {},
                h('td', { class: 'small' }, fmtTime(e.createdAt)),
                h('td', {}, e.fromLevel ? label(e.fromLevel) : '-'),
                h('td', {}, label(e.toLevel)),
                h('td', { class: 'small' }, e.handledBy || '-'),
                h('td', {}, label(e.reason)),
                h('td', { class: 'small' }, e.note || '')))));
    }

    async function run(action) {
        try {
            await action();
        } catch (e) { fail(e); }
        try {
            await loadList();
            if (selectedId) renderDetail(await api('GET', '/complaints/' + selectedId));
        } catch (e) { fail(e); }
    }

    const escalate = (id) => run(async () => {
        const r = await api('POST', `/complaints/${id}/escalate`, { note: $('escNote').value });
        lastTrace = r.trace;
        toast('Escalated to ' + label(r.currentLevel));
    });

    const setStatus = (id, status) => run(async () => {
        await api('POST', `/complaints/${id}/status`, { status });
        lastTrace = null;
        toast('Status: ' + label(status));
    });

    const makeOverdue = (id) => run(async () => {
        await api('POST', `/complaints/${id}/make-overdue`);
        toast('Deadline moved into the past. Now press "Run timeout check".');
    });

    async function runTimeouts() {
        try {
            const r = await api('POST', '/complaints/process-timeouts');
            toast(r.escalated ? `${r.escalated} complaint(s) escalated by timeout (#${r.complaintIds.join(', #')})`
                : 'No complaint is past its deadline');
            lastTrace = null;
            await loadList();
            if (selectedId) renderDetail(await api('GET', '/complaints/' + selectedId));
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ start

    const fill = (sel, values) => sel.append(...values.map((v) => h('option', { value: v }, label(v))));
    fill($('fCategory'), CATEGORIES);
    fill($('fSeverity'), SEVERITIES);
    fill($('fStatus'), STATUSES);
    fill($('fLevel'), LEVELS);
    $('fSeverity').addEventListener('change', showHint);
    $('newForm').addEventListener('submit', submitNew);
    $('fStatus').addEventListener('change', () => loadList().catch(fail));
    $('fLevel').addEventListener('change', () => loadList().catch(fail));
    $('btnTimeouts').addEventListener('click', runTimeouts);
    loadOptions().catch(fail);
    loadList().catch(fail);
})();