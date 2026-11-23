<%--
  Created by IntelliJ IDEA.
  User: DRACULA
  Date: 11/1/2026
  Time: 9:50 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Energy meters"); request.setAttribute("active", "meters"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Operations</span><span>/</span><b>Energy meters</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 4 14h7l-1 8 9-12h-7z"/></svg></span>Energy meters <span class="sub"> &middot; Decorator</span></h1>
        <p class="lead">Each meter's readings travel through security layers (authentication, compression, encryption).
            Add, remove or switch off a layer and the very next send uses the new chain, with no code change.</p>
    </div>
</div>

<div class="grid">
    <section class="card">
        <div class="card-head">
            <h2>Meters</h2>
            <button class="btn small" id="btnNewMeter" type="button" ${scAdmin ? '' : 'hidden'}>+ New meter</button>
        </div>
        <form class="inline" id="newMeterForm" hidden>
            <input type="text" id="newCode" placeholder="Meter code, e.g. MTR-009" maxlength="30" required>
            <select id="newZone" required></select>
            <button class="btn small primary" type="submit">Create</button>
        </form>
        <table class="clickable">
            <thead><tr><th>Code</th><th>Building</th><th>Status</th><th>Stream</th></tr></thead>
            <tbody id="meterRows"><tr><td colspan="4" class="muted">Loading...</td></tr></tbody>
        </table>
    </section>

    <section class="card">
        <p id="emptyDetail" class="muted">Select a meter to see and change its stream.</p>

        <div id="detail" hidden>
            <div class="card-head">
                <h2 id="dTitle"></h2>
                <span id="dStatus"></span>
            </div>

            <h3>Stream</h3>
            <div class="flow" id="flow"></div>
            <div class="small muted" id="flowNote"></div>

            <h3>Layers</h3>
            <c:if test="${not scManager}"><p class="muted small">Only admins and city managers can change layers. You can still send readings.</p></c:if>
            <table>
                <thead><tr><th>Position</th><th>Type</th><th>Config</th><th>On</th><th></th></tr></thead>
                <tbody id="layerRows"></tbody>
            </table>
            <form class="inline" id="layerForm" ${scManager ? '' : 'hidden'}>
                <select id="layerType">
                    <option value="AUTHENTICATION">AUTHENTICATION</option>
                    <option value="COMPRESSION">COMPRESSION</option>
                    <option value="ENCRYPTION">ENCRYPTION</option>
                </select>
                <input type="number" id="layerOrder" min="1" max="20" title="Position (1 is applied first)" required>
                <select id="layerConfig"></select>
                <button class="btn small" type="submit">Add layer</button>
            </form>

            <div class="actions">
                <button class="btn primary" id="btnTransmit" type="button">Send latest 24 readings</button>
            </div>
            <div class="result" id="result" hidden></div>

            <h3>Audit trail</h3>
            <table>
                <thead><tr><th>#</th><th>Time</th><th>Layers applied</th><th class="num">Raw B</th><th class="num">Wire B</th></tr></thead>
                <tbody id="auditRows"></tbody>
            </table>
        </div>
    </section>
</div>

<script src="${pageContext.request.contextPath}/js/meters.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>




