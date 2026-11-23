<%--
  Created by IntelliJ IDEA.
  User: DRACULA
  Date: 11/1/2026
  Time: 10:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "City zones"); request.setAttribute("active", "zones"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Operations</span><span>/</span><b>City zones</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3 9 5-9 5-9-5z"/><path d="m3 13 9 5 9-5"/></svg></span>City zones <span class="sub"> &middot; Composite</span></h1>
        <p class="lead">The city is a tree: city, districts, streets, buildings. One command works on any node.
            Shutting down a district switches off every street and building under it, and its usage drops to zero.</p>
    </div>
</div>

<div class="stats" id="stats"></div>

<div class="card" id="resultCard" hidden>
    <div class="card-head"><h2>Last action</h2></div>
    <div id="result"></div>
</div>
<div class="card" style="margin-top:20px">
    <div class="card-head">
        <h2>Zone tree</h2>
        <span class="small muted" id="windowNote"></span>
    </div>
    <div id="tree"><p class="muted">Loading...</p></div>
</div>

<script src="${pageContext.request.contextPath}/js/zones.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>


