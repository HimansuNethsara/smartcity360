<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "My profile"); request.setAttribute("active", "profile"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Account</span><span>/</span><b>My profile</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg></span>My profile</h1>
        <p class="lead">Your details and password. Changing your password signs out every other device.</p>
    </div>
</div>

<div class="row2">
    <section class="card">
        <div class="card-head"><h2>Details</h2></div>
        <div class="field"><label>Username</label><div id="pUser" class="mono"></div></div>
        <div class="field"><label>Role</label><div id="pRoles" class="rolechips"></div></div>
        <form id="profileForm" novalidate>
            <div class="field"><label for="pName">Full name</label>
                <input type="text" id="pName" maxlength="100" required></div>
            <div class="field"><label for="pEmail">Email</label>
                <input type="email" id="pEmail" maxlength="120" required></div>
            <div class="field"><label for="pPhone">Phone</label>
                <input type="tel" id="pPhone" maxlength="20"></div>
            <button class="btn primary" type="submit">Save details</button>
        </form>
    </section>

    <section class="card">
        <div class="card-head"><h2>Change password</h2></div>
        <form id="pwForm" novalidate>
            <div class="field"><label for="pwOld">Current password</label>
                <input type="password" id="pwOld" autocomplete="current-password" maxlength="72" required></div>
            <div class="field"><label for="pwNew">New password</label>
                <input type="password" id="pwNew" autocomplete="new-password" maxlength="72" required></div>
            <div class="field"><label for="pwNew2">Repeat new password</label>
                <input type="password" id="pwNew2" autocomplete="new-password" maxlength="72" required></div>
            <div class="hint">At least 8 characters with a letter and a digit.</div>
            <button class="btn primary" type="submit">Change password</button>
        </form>
    </section>
</div>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

