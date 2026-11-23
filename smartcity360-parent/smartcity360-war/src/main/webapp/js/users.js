/* Users page (Phase 5 Part D). Uses api(), h() and toast() from api.js. */
(() => {
    const $ = (id) => document.getElementById(id);
    const PROTECTED = 'admin';

    let me = null;           // the logged-in user, from /auth/me
    let searchTimer = null;
    let loadSeq = 0;

    const fail = (e) => toast(e.message, true);

    // ------------------------------------------------------------ list

    async function loadUsers() {
        const seq = ++loadSeq;
        const qs = new URLSearchParams();
        if ($('fRole').value) qs.set('role', $('fRole').value);
        if ($('fEnabled').value) qs.set('enabled', $('fEnabled').value);
        const q = $('fSearch').value.trim();
        if (q) qs.set('q', q);
        const users = await api('GET', '/users' + (qs.toString() ? '?' + qs.toString() : ''));
        if (seq !== loadSeq) return;       // an older answer arriving late
        render(users);
    }

    function render(users) {
        const body = $('userRows');
        if (!users.length) {
            body.replaceChildren(h('tr', {}, h('td', { colspan: 7, class: 'muted' }, 'No users match.')));
            return;
        }
        body.replaceChildren(...users.map(row));
    }

    function row(u) {
        const isMe = !!me && u.id === me.id;
        const isRoot = u.username.toLowerCase() === PROTECTED;
        const lockToggle = isMe || isRoot;
        const why = isRoot ? 'The admin account cannot be disabled'
            : isMe ? 'You cannot disable your own account' : undefined;
        const canReset = !isRoot || isMe;       // only admin may reset the admin password
        return h('tr', {},
            h('td', {}, h('b', {}, u.username), isMe ? h('span', { class: 'muted small' }, ' (you)') : ''),
            h('td', {}, u.fullName),
            h('td', { class: 'small' }, u.email),
            h('td', { class: 'small' }, u.phone || '-'),
            h('td', {}, h('div', { class: 'rolechips' }, u.roles.map((r) => h('span', { class: 'chip' }, r)))),
            h('td', {}, h('input', {
                type: 'checkbox', checked: u.enabled, disabled: lockToggle, title: why,
                onchange: (e) => toggle(u, e.target)
            })),
            h('td', {}, canReset
                ? h('button', { class: 'btn small', type: 'button', onclick: () => resetPassword(u) }, 'Reset password')
                : ''));
    }

    // ------------------------------------------------------------ enable / disable

    async function toggle(u, box) {
        box.disabled = true;
        try {
            const saved = await api('PUT', '/users/' + u.id, { enabled: !u.enabled });
            toast(saved.username + (saved.enabled ? ' enabled' : ' disabled'));
        } catch (e) { fail(e); }
        await loadUsers().catch(fail);       // redraw so the box always shows the real state
    }

    // ------------------------------------------------------------ reset password

    async function resetPassword(u) {
        const pw = prompt('New password for ' + u.username + '\n(at least 8 characters, with a letter and a digit)');
        if (pw === null) return;
        try {
            await api('POST', '/users/' + u.id + '/reset-password', { newPassword: pw });
            toast('Password reset for ' + u.username);
        } catch (e) { fail(e); }
    }

    // ------------------------------------------------------------ create

    async function createUser(ev) {
        ev.preventDefault();
        const roles = [...document.querySelectorAll('#nRoles input:checked')].map((c) => c.value);
        if (!roles.length) { toast('Choose at least one role.', true); return; }
        const btn = $('btnCreate');
        btn.disabled = true;
        try {
            const u = await api('POST', '/users', {
                username: $('nUser').value.trim(),
                password: $('nPass').value,
                fullName: $('nName').value.trim(),
                email: $('nEmail').value.trim(),
                phone: $('nPhone').value.trim() || null,
                roles
            });
            toast('User ' + u.username + ' created');
            $('newForm').reset();
            $('newCard').hidden = true;
            await loadUsers();
        } catch (e) { fail(e); }
        finally { btn.disabled = false; }
    }

    // ------------------------------------------------------------ start

    async function init() {
        $('btnNew').addEventListener('click', () => { $('newCard').hidden = !$('newCard').hidden; });
        $('btnCancel').addEventListener('click', () => { $('newForm').reset(); $('newCard').hidden = true; });
        $('newForm').addEventListener('submit', createUser);
        $('fRole').addEventListener('change', () => loadUsers().catch(fail));
        $('fEnabled').addEventListener('change', () => loadUsers().catch(fail));
        $('fSearch').addEventListener('input', () => {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(() => loadUsers().catch(fail), 300);
        });
        try { me = await api('GET', '/auth/me'); } catch (e) { /* api() already redirects on 401 */ }
        await loadUsers().catch(fail);
    }
    init();
})();