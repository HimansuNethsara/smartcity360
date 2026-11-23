<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Notifications"); request.setAttribute("active", "notifications"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Account</span><span>/</span><b>Notifications</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 8-3 8h18s-3-1-3-8M10.3 20a2 2 0 0 0 3.4 0"/></svg></span>Notifications</h1>
        <p class="lead">Updates about your complaints and the work assigned to you. They arrive through a message queue,
            so they can appear a moment after the action that caused them.</p>
    </div>
</div>

<section class="card">
    <div class="card-head">
        <h2>Inbox</h2>
        <button class="btn small" id="btnReadAll" type="button">Mark all as read</button>
    </div>
    <div class="btnrow">
        <select id="fUnread">
            <option value="">All notifications</option>
            <option value="true">Unread only</option>
        </select>
        <button class="btn small" id="btnRefresh" type="button">Refresh</button>
    </div>
    <table>
        <thead>
        <tr><th>When</th><th>Type</th><th>Notification</th><th></th></tr>
        </thead>
        <tbody id="noteRows"><tr><td colspan="4" class="muted">Loading...</td></tr></tbody>
    </table>
</section>

<script src="${pageContext.request.contextPath}/js/notifications.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

