/* Notifications page (Phase 6, JMS). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const TYPE_LABEL = {
        COMPLAINT_UPDATE: 'Complaint', COMPLAINT_ESCALATED: 'Escalated',
        POLICY_VIOLATION: 'Policy', REPORT_READY: 'Report', SYSTEM: 'System'
    };
    const TYPE_BADGE = { COMPLAINT_ESCALATED: 'warn', POLICY_VIOLATION: 'warn', REPORT_READY: 'ok' };

    const fail = (e) => toast(e.message || String(e), true);
    const when = (s) => (s ? new Date(s + 'Z').toLocaleString() : '-');

    async function load() {
        const unread = $('fUnread').value === 'true';
        const list = await api('GET', '/notifications' + (unread ? '?unread=true' : ''));
        $('noteRows').replaceChildren(...(list.length ? list.map(row)
            : [h('tr', {}, h('td', { colspan: 4, class: 'muted' }, 'Nothing here.'))]));
        refreshBell();
    }

    function row(n) {
        return h('tr', {},
            h('td', { class: 'small' }, when(n.createdAt)),
            h('td', {}, h('span', { class: 'badge ' + (TYPE_BADGE[n.type] || 'off') }, TYPE_LABEL[n.type] || n.type)),
            h('td', {}, h('div', {}, n.read ? n.title : h('b', {}, n.title)),
                h('div', { class: 'small muted' }, n.message)),
            h('td', {}, n.read ? '' : h('button', { class: 'btn small', type: 'button',
                onclick: () => markRead(n.id) }, 'Mark read')));
    }

    async function markRead(id) {
        try { await api('POST', '/notifications/' + id + '/read', {}); await load(); } catch (e) { fail(e); }
    }

    async function readAll() {
        try {
            const r = await api('POST', '/notifications/read-all', {});
            toast(r.changed + ' marked as read');
            await load();
        } catch (e) { fail(e); }
    }

    $('fUnread').addEventListener('change', () => load().catch(fail));
    $('btnRefresh').addEventListener('click', () => load().catch(fail));
    $('btnReadAll').addEventListener('click', readAll);
    load().catch(fail);
})();
