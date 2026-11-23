<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Users"); request.setAttribute("active", "users"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Administration</span><span>/</span><b>Users</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 4.5a3.5 3.5 0 0 1 0 7M18 14.5a6.5 6.5 0 0 1 3.5 5.5"/></svg></span>Users</h1>
        <p class="lead">Create accounts, switch them on or off, and reset passwords. The built-in admin account
            cannot be disabled, and you cannot disable or demote yourself.</p>
    </div>
</div>

<section class="card spaced" id="newCard" hidden>
    <div class="card-head"><h2>New user</h2></div>
    <form id="newForm">
        <div class="twocol">
            <div class="field"><label for="nUser">Username</label>
                <input type="text" id="nUser" maxlength="30" autocomplete="off" required></div>
            <div class="field"><label for="nPass">Password</label>
                <input type="password" id="nPass" maxlength="72" autocomplete="new-password" required></div>
            <div class="field"><label for="nName">Full name</label>
                <input type="text" id="nName" maxlength="100" required></div>
            <div class="field"><label for="nEmail">Email</label>
                <input type="email" id="nEmail" maxlength="120" required></div>
            <div class="field"><label for="nPhone">Phone</label>
                <input type="tel" id="nPhone" maxlength="20"></div>
        </div>
        <div class="field">
            <label>Roles</label>
            <div class="rolechips" id="nRoles">
                <label class="chip"><input type="checkbox" value="ADMIN"> ADMIN</label>
                <label class="chip"><input type="checkbox" value="CITY_MANAGER"> CITY_MANAGER</label>
                <label class="chip"><input type="checkbox" value="SUPERVISOR"> SUPERVISOR</label>
                <label class="chip"><input type="checkbox" value="STAFF"> STAFF</label>
                <label class="chip"><input type="checkbox" value="CITIZEN"> CITIZEN</label>
            </div>
        </div>
        <div class="hint">Password: at least 8 characters with a letter and a digit. Pick at least one role.</div>
        <div class="btnrow">
            <button class="btn primary" type="submit" id="btnCreate">Create user</button>
            <button class="btn" type="button" id="btnCancel">Cancel</button>
        </div>
    </form>
</section>

<section class="card">
    <div class="card-head">
        <h2>All users</h2>
        <button class="btn small" id="btnNew" type="button">+ New user</button>
    </div>
    <div class="btnrow">
        <select id="fRole">
            <option value="">All roles</option>
            <option>ADMIN</option>
            <option>CITY_MANAGER</option>
            <option>SUPERVISOR</option>
            <option>STAFF</option>
            <option>CITIZEN</option>
        </select>
        <select id="fEnabled">
            <option value="">Enabled and disabled</option>
            <option value="true">Enabled</option>
            <option value="false">Disabled</option>
        </select>
        <input type="text" id="fSearch" maxlength="60" placeholder="Search username, name or email">
    </div>
    <table>
        <thead>
        <tr><th>Username</th><th>Full name</th><th>Email</th><th>Phone</th><th>Roles</th><th>Enabled</th><th></th></tr>
        </thead>
        <tbody id="userRows"><tr><td colspan="7" class="muted">Loading...</td></tr></tbody>
    </table>
</section>

<script src="${pageContext.request.contextPath}/js/users.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

