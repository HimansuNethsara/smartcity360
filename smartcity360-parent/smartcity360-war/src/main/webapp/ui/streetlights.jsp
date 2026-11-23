<%--
  Created by IntelliJ IDEA.
  User: DRACULA
  Date: 11/2/2026
  Time: 1:09 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Streetlights"); request.setAttribute("active", "streetlights"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Operations</span><span>/</span><b>Streetlights</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg></span>Streetlights <span class="sub">  &middot; Flyweight</span></h1>
        <p class="lead">Every light stores only its own place, status and brightness. The data shared by all lamps of one model
            (wattage, lumens, firmware) lives in a single shared object per model, however many lights use it.</p>
    </div>
</div>

<div class="stats" id="stats"></div>

<div class="row2">
    <section class="card">
        <div class="card-head">
            <h2>Shared lamp models</h2>
            <button class="btn small" id="btnReset" type="button" title="Forget the cached models" ${scAdmin ? '' : 'hidden'}>Reset pool</button>
        </div>
        <table>
            <thead><tr><th>Model</th><th>Type</th><th class="num">Watts</th><th class="num">Lights using it</th></tr></thead>
            <tbody id="typeRows"></tbody>
        </table>
        <p class="small muted" id="poolNote"></p>
    </section>

    <section class="card">
        <div class="card-head"><h2>Memory: with and without sharing</h2></div>
        <form class="inline" id="memForm">
            <label class="small muted" for="memCount">Lights</label>
            <input type="number" id="memCount" min="1" max="200000" value="10000" style="width:110px">
            <button class="btn small primary" type="submit">Compare</button>
        </form>
        <div id="memResult"><p class="small muted">Builds the same lights twice, with and without sharing, and compares the cost.</p></div>
    </section>
</div>

<section class="card" style="margin-bottom:20px" ${scAdmin ? '' : 'hidden'}>
    <div class="card-head"><h2>Add lights</h2></div>
    <p class="small muted" style="margin-top:0">Lights can only stand in street zones. Generated lights are synthetic demo data
        and can be removed again without touching the real ones.</p>
    <form class="inline" id="genForm">
        <input type="number" id="genCount" min="1" max="20000" value="10000" style="width:110px" required>
        <select id="genZone"></select>
        <button class="btn primary" type="submit">Generate</button>
        <button class="btn danger" type="button" id="btnRemove">Remove generated</button>
    </form>
</section>

<section class="card">
    <div class="card-head"><h2>Lights</h2><span class="small muted" id="total"></span></div>
    <form class="inline" id="filters">
        <select id="fZone"></select>
        <select id="fStatus"><option value="">Any status</option></select>
    </form>
    <table>
        <thead><tr><th>#</th><th>Model</th><th>Street</th><th>Status</th><th>Brightness</th>
            <th class="num">Watts now</th><th class="num">Lumens now</th><th></th></tr></thead>
        <tbody id="rows"><tr><td colspan="8" class="muted">Loading...</td></tr></tbody>
    </table>
    <div class="pager">
        <button class="btn small" id="prev" type="button">&larr; Previous</button>
        <span class="small muted" id="pageInfo"></span>
        <button class="btn small" id="next" type="button">Next &rarr;</button>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/streetlights.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>


