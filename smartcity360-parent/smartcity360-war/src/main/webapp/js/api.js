/* Shared helpers for every SmartCity360 page. */

async function api(method, path, body) {
    const base = document.querySelector('meta[name="ctx"]').content + '/api';
    const res = await fetch(base + path, {
        method,
        headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    if (res.status === 401 && !/^\/auth\/(login|register)/.test(path)) {
        // not logged in (or the login expired): go to the login page and come back afterward
        location.href = document.querySelector('meta[name="ctx"]').content
            + '/login.jsp?next=' + encodeURIComponent(location.pathname + location.search);
        throw new Error('Please log in.');
    }
    if (res.status === 204) return null;
    const text = await res.text();
    let data = null;
    if (text) {
        try { data = JSON.parse(text); } catch (e) { /* not JSON */ }
    }
    if (!res.ok) {
        throw new Error((data && data.message) || res.status + ' ' + res.statusText);
    }
    return data;
}

function h(tag, attrs, ...kids) {
    const el = document.createElement(tag);
    for (const [k, v] of Object.entries(attrs || {})) {
        if (k === 'class') el.className = v;
        else if (k.startsWith('on')) el.addEventListener(k.slice(2), v);
        else if (v === true) el.setAttribute(k, '');
        else if (v !== false && v !== null && v !== undefined) el.setAttribute(k, v);
    }
    for (const kid of kids.flat()) {
        el.append(kid instanceof Node ? kid : document.createTextNode(kid ?? ''));
    }
    return el;
}

let toastTimer;
function toast(message, isError) {
    const t = document.getElementById('toast');
    t.textContent = message;
    t.className = 'show' + (isError ? ' error' : '');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.className = ''; }, isError ? 6000 : 3000);
}

async function logout() {
    try { await api('POST', '/auth/logout', {}); } catch (e) { /* already logged out */ }
    location.href = document.querySelector('meta[name="ctx"]').content + '/login.jsp';
}

async function refreshBell() {
    const el = document.getElementById('bellCount');
    if (!el) return;
    try {
        const r = await api('GET', '/notifications/unread-count');
        el.textContent = r.unread;
        el.hidden = !r.unread;
    } catch (e) { /* the bell is optional */ }
}
document.addEventListener('DOMContentLoaded', refreshBell);