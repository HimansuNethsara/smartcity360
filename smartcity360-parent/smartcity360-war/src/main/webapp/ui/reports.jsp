<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Reports"); request.setAttribute("active", "reports"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Services</span><span>/</span><b>Reports</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg></span>Reports</h1>
        <p class="lead noprint">Reports are built in the background. Request one, carry on working, and it shows up as DONE
            here (and in your notifications) when it is ready. Open it to see the charts, and print it if you need a copy.</p>
    </div>
</div>

<section class="card spaced noprint">
    <div class="card-head"><h2>Request a report</h2></div>
    <form id="reqForm">
        <div class="twocol">
            <div class="field"><label for="rType">Report</label>
                <select id="rType">
                    <option value="ENERGY_USAGE">Energy usage per meter</option>
                    <option value="COMPLAINT_SUMMARY">Complaint summary</option>
                    <option value="STREETLIGHT_STATUS">Streetlight status</option>
                    <c:if test="${scSupervisor}"><option value="POLICY_VIOLATIONS" id="optViol">Policy violations</option></c:if>
                </select></div>
            <div class="field"><label for="rHours">Look back (hours, 1 to 168)</label>
                <input type="number" id="rHours" min="1" max="168" value="24"></div>
        </div>
        <div class="hint">The look-back applies to the energy and policy reports only.</div>
        <div class="btnrow"><button class="btn primary" type="submit" id="btnRequest">Request report</button></div>
    </form>
</section>

<section class="card spaced noprint">
    <div class="card-head"><h2>My reports</h2>
        <button class="btn small" id="btnRefresh" type="button">Refresh</button></div>
    <table>
        <thead><tr><th>#</th><th>Report</th><th>Status</th><th>Requested (UTC)</th><th>Finished (UTC)</th><th></th></tr></thead>
        <tbody id="jobRows"><tr><td colspan="6" class="muted">Loading...</td></tr></tbody>
    </table>
</section>

<section class="card" id="resultCard" hidden>
    <div class="card-head">
        <h2 id="resultTitle">Result</h2>
        <button class="btn small noprint" id="btnPrint" type="button">Print</button>
    </div>
    <div class="small muted" id="resultMeta"></div>
    <div class="chartbox"><canvas id="chartMain" aria-label="Main chart" role="img"></canvas></div>
    <div id="mainTable"></div>
    <div id="secondBlock" hidden>
        <h3 id="secondTitle"></h3>
        <div class="chartbox"><canvas id="chartSecond" aria-label="Second chart" role="img"></canvas></div>
        <div id="secondTable"></div>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/vendor/chart.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/js/reports.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

