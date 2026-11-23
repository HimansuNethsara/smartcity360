<%--
  Created by IntelliJ IDEA.
  User: DRACULA
  Date: 11/1/2026
  Time: 11:04 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Complaints"); request.setAttribute("active", "complaints"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Services</span><span>/</span><b>Complaints</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>Complaints <span class="sub"> &middot; Chain of Responsibility</span></h1>
        <p class="lead">A complaint enters a chain of handlers. Each one either takes it or passes it on.
            If nobody resolves it before its deadline, it moves one step up the chain by itself.</p>
    </div>
</div>
<div class="flow" style="margin-bottom:20px">
    <span class="pill auth">Staff</span><span class="arrow">&rarr;</span>
    <span class="pill compress">Supervisor</span><span class="arrow">&rarr;</span>
    <span class="pill encrypt">City manager</span>
</div>

<div class="grid">
    <div>
        <section class="card">
            <div class="card-head"><h2>New complaint</h2></div>
            <form id="newForm">
                <div class="field"><label for="fCitizen">On behalf of citizen.</label>
                    <select id="fCitizen" required></select></div>
                <div class="field"><label for="fZone">Zone (optional)</label>
                    <select id="fZone"></select></div>
                <div class="field"><label for="fCategory">Category</label>
                    <select id="fCategory"></select></div>
                <div class="field"><label for="fSeverity">Severity</label>
                    <select id="fSeverity"></select></div>
                <div class="hint" id="sevHint"></div>
                <div class="field"><label for="fTitle">Title</label>
                    <input type="text" id="fTitle" maxlength="150" required></div>
                <div class="field"><label for="fDesc">Description</label>
                    <textarea id="fDesc" maxlength="2000" required></textarea></div>
                <button class="btn primary" type="submit">Submit complaint</button>
            </form>
        </section>

        <section class="card" style="margin-top:20px">
            <div class="card-head"><h2>Escalation policy</h2></div>
            <table>
                <thead><tr><th>Severity</th><th>Starts at</th><th class="num">Staff min</th><th class="num">Supervisor min</th></tr></thead>
                <tbody id="policyRows"></tbody>
            </table>
        </section>
    </div>

    <div>
        <section class="card">
            <div class="card-head">
                <h2>All complaints</h2>
                <button class="btn small" id="btnTimeouts" type="button" ${scManager ? '' : 'hidden'}>Run timeout check</button>
            </div>
            <form class="inline" id="filters">
                <select id="fStatus"><option value="">Any status</option></select>
                <select id="fLevel"><option value="">Any level</option></select>
            </form>
            <table class="clickable">
                <thead><tr><th>#</th><th>Title</th><th>Severity</th><th>Level</th><th>Status</th><th>Due</th></tr></thead>
                <tbody id="rows"><tr><td colspan="6" class="muted">Loading...</td></tr></tbody>
            </table>
        </section>

        <section class="card" id="detail" style="margin-top:20px" hidden></section>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/complaints.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
