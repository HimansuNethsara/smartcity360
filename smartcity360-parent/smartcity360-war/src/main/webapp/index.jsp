<%--
  Created by IntelliJ IDEA.
  User: DRACULA
  Date: 10/31/2026
  Time: 8:25 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Home"); request.setAttribute("active", "home"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="hero">
    <c:choose>
        <c:when test="${not empty scUsername}">
            <span class="role-chip"><c:out value="${scRole}"/></span>
            <h1>Welcome back, <c:out value="${scUsername}"/></h1>
            <p>Everything you need to run and follow the city's services, in one place.
                <c:choose>
                    <c:when test="${scStaff}">Check your open complaints, review the latest reports, or manage the city's infrastructure.</c:when>
                    <c:otherwise>Report a problem in your neighbourhood and follow it until it is fixed.</c:otherwise>
                </c:choose></p>
            <div class="btnrow">
                <a class="btn primary" href="${pageContext.request.contextPath}/ui/complaints.jsp">
                    <c:choose><c:when test="${scStaff}">Open complaints</c:when><c:otherwise>Report a problem</c:otherwise></c:choose></a>
                <c:if test="${scStaff}"><a class="btn" href="${pageContext.request.contextPath}/ui/reports.jsp">View reports</a></c:if>
                <a class="btn" href="${pageContext.request.contextPath}/ui/notifications.jsp">My notifications</a>
            </div>
        </c:when>
        <c:otherwise>
            <span class="role-chip">Smart city platform</span>
            <h1>A smarter, safer, better-served city</h1>
            <p>SmartCity360 connects the city's energy meters, streetlights, sensors and resident services, so problems
                are seen early and fixed fast.</p>
            <div class="btnrow">
                <a class="btn primary" href="${pageContext.request.contextPath}/login.jsp">Log in</a>
                <a class="btn" href="${pageContext.request.contextPath}/register.jsp">Create a resident account</a>
                <a class="btn" href="${pageContext.request.contextPath}/about.jsp">Learn more</a>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<c:if test="${not empty scUsername}">
    <div class="section-title" style="margin-top:0">
        <div><h2>
            <c:choose><c:when test="${scStaff}">City at a glance</c:when><c:otherwise>Your requests</c:otherwise></c:choose>
        </h2><p>Live figures, refreshed each time you open this page.</p></div>
    </div>
    <div class="kpis" id="kpiBox"><div class="muted small">Loading...</div></div>
    <div class="row2">
        <section class="card">
            <div class="card-head"><h2>Complaints by status</h2></div>
            <div class="chartbox"><canvas id="cStatus" aria-label="Complaints by status" role="img"></canvas></div>
        </section>
        <section class="card">
            <div class="card-head"><h2>Latest complaints</h2>
                <a class="btn small" href="${pageContext.request.contextPath}/ui/complaints.jsp">View all</a></div>
            <table>
                <thead><tr><th>#</th><th>Title</th><th>Severity</th><th>Status</th></tr></thead>
                <tbody id="recentRows"><tr><td colspan="4" class="muted">Loading...</td></tr></tbody>
            </table>
        </section>
    </div>

    <div class="section-title" style="margin-top:34px">
        <div><h2>Your workspace</h2><p>Jump straight to the tools your role can use.</p></div>
    </div>
    <div class="cards3">
        <c:if test="${scStaff}">
            <a class="card" href="${pageContext.request.contextPath}/ui/meters.jsp">
                <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 4 14h7l-1 8 9-12h-7z"/></svg></div>
                <h2>Energy meters</h2>
                <p class="muted small">Every meter sends its readings through its own security layers. Add or change a layer in seconds.</p>
                <span class="go">Open &rarr;</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/ui/zones.jsp">
                <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3 9 5-9 5-9-5z"/><path d="m3 13 9 5 9-5"/></svg></div>
                <h2>City zones</h2>
                <p class="muted small">See the whole city as a tree and switch power off or on for a district, street or single building.</p>
                <span class="go">Open &rarr;</span>
            </a>

        </c:if>
        <a class="card" href="${pageContext.request.contextPath}/ui/complaints.jsp">
            <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
            <h2>Complaints</h2>
            <p class="muted small">Raise a request and follow it from first contact to resolution. Overdue items move up automatically.</p>
            <span class="go">Open &rarr;</span>
        </a>

        <c:if test="${scStaff}">
            <a class="card" href="${pageContext.request.contextPath}/ui/streetlights.jsp">
                <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg></div>
                <h2>Streetlights</h2>
                <p class="muted small">Thousands of lights, a handful of lamp models. Check status, brightness and power draw.</p>
                <span class="go">Open &rarr;</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/ui/policies.jsp">
                <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></svg></div>
                <h2>City policies</h2>
                <p class="muted small">Write a rule in plain words, such as a noise limit, and check it against live readings.</p>
                <span class="go">Open &rarr;</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/ui/reports.jsp">
                <div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg></div>
                <h2>Reports</h2>
                <p class="muted small">Request energy, complaint, streetlight and policy reports. They are built in the background.</p>
                <span class="go">Open &rarr;</span>
            </a>

        </c:if>
    </div>

    <div class="row2" style="margin-top:34px">
        <section class="card">
            <div class="card-head"><h2>City bulletin</h2><span class="badge off">Sample</span></div>
            <ul class="bulletin">
                <li><div class="date"><b>03</b><span>Nov</span></div><div><strong>Planned maintenance, Port District</strong>
                    <p>Streetlight firmware upgrade on Harbour Avenue between 11 pm and 2 am. Some lights may flicker.</p></div></li>
                <li><div class="date"><b>01</b><span>Nov</span></div><div><strong>Residential noise limit reviewed</strong>
                    <p>The night-time limit for residential zones stays at 70 dB. Fines apply above it.</p></div></li>
                <li><div class="date"><b>28</b><span>Oct</span></div><div><strong>Faster complaint handling</strong>
                    <p>High-severity complaints now go straight to a supervisor and escalate after one hour.</p></div></li>
                <li><div class="date"><b>20</b><span>Oct</span></div><div><strong>Meter roll-out reaches Central District</strong>
                    <p>All commercial buildings on Market Street now report usage every minute.</p></div></li>
            </ul>
        </section>
        <section class="card">
            <div class="card-head"><h2>Getting started</h2></div>
            <ol class="steps">
                <li><b>Check your inbox.</b><br><span class="muted small">The bell in the top bar shows new updates about your requests.</span></li>
                <li><b>Open a service.</b><br><span class="muted small">Use the menus at the top: Operations, Services and Support.</span></li>
                <li><b>Keep your details current.</b><br><span class="muted small">Update your phone and email under your profile.</span></li>
                <li><b>Need a hand?</b><br><span class="muted small">The <a href="${pageContext.request.contextPath}/help.jsp">Help centre</a> explains who can do what.</span></li>
            </ol>
        </section>
    </div>
</c:if>

<c:if test="${empty scUsername}">
    <div class="section-title"><div><h2>What the platform does</h2><p>Four services, one login.</p></div></div>
    <div class="cards3">
        <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 4 14h7l-1 8 9-12h-7z"/></svg></div><h2>Energy monitoring</h2>
            <p class="muted small">Live readings from every meter, sent securely and summarised in reports.</p></div>
        <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg></div><h2>Street lighting</h2>
            <p class="muted small">Track every light in the city, spot faults early and control brightness.</p></div>
        <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div><h2>Resident requests</h2>
            <p class="muted small">Report a problem and follow it. Nothing gets stuck: late items escalate by themselves.</p></div>
        <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></svg></div><h2>City rules</h2>
            <p class="muted small">Noise, heat and air-quality limits written as policies and checked against live sensors.</p></div>
    </div>
</c:if>

<c:if test="${not empty scUsername}">
    <script src="${pageContext.request.contextPath}/js/vendor/chart.umd.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/home.js"></script>
</c:if>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>




