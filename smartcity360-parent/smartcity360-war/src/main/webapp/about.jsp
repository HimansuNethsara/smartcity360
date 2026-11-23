<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "About us"); request.setAttribute("active", "about"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="hero">
    <span class="role-chip">About us</span>
    <h1>Running a city should feel simple</h1>
    <p>SmartCity360 Metropolitan Services brings the city's energy, lighting and resident services onto one platform,
        so staff see the whole picture and residents get answers.</p>
</section>

<div class="kpis">
    <div class="kpi"><div class="k-label">Buildings metered</div><div class="k-value">1,240</div><div class="k-sub">across 3 districts</div></div>
    <div class="kpi"><div class="k-label">Streetlights managed</div><div class="k-value">38,500</div><div class="k-sub">12 lamp models</div></div>
    <div class="kpi"><div class="k-label">Complaints resolved</div><div class="k-value">96%</div><div class="k-sub">within target time</div></div>
    <div class="kpi"><div class="k-label">Staff on the platform</div><div class="k-value">210</div><div class="k-sub">4 service teams</div></div>
</div>
<p class="small muted" style="margin-top:-8px">Figures on this page are sample data for demonstration.</p>

<div class="section-title"><div><h2>What we do</h2><p>Six services that keep the city running.</p></div></div>
<div class="cards3">
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2 4 14h7l-1 8 9-12h-7z"/></svg></div><h2>Energy</h2><p class="muted small">Secure, per-meter data streams and usage reports for every building.</p></div>
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m12 3 9 5-9 5-9-5z"/><path d="m3 13 9 5 9-5"/></svg></div><h2>Zones</h2><p class="muted small">The city mapped from district to building, with one-click power control.</p></div>
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg></div><h2>Lighting</h2><p class="muted small">A single view of every streetlight, its model, status and brightness.</p></div>
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div><h2>Resident care</h2><p class="muted small">Complaints routed to the right team and escalated before they go stale.</p></div>
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></svg></div><h2>Policy</h2><p class="muted small">City limits and rules written in plain language and checked against live data.</p></div>
    <div class="card"><div class="modicon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg></div><h2>Insight</h2><p class="muted small">Reports and charts for managers, ready when they are needed.</p></div>
</div>

<div class="row2" style="margin-top:34px">
    <section class="card">
        <div class="card-head"><h2>Our story</h2></div>
        <ul class="timeline">
            <li><b>2024</b><span class="muted">First energy meters installed in the North District.</span></li>
            <li><b>2025</b><span class="muted">Streetlight fleet and noise sensors added. Complaints move online.</span></li>
            <li><b>2026</b><span class="muted">City policies go live, with automatic checks against sensor data.</span></li>
        </ul>
    </section>
    <section class="card">
        <div class="card-head"><h2>Our commitments</h2></div>
        <ol class="steps">
            <li><b>Answer every resident.</b><br><span class="muted small">Each complaint has an owner and a deadline.</span></li>
            <li><b>Be open about the data.</b><br><span class="muted small">Policies are readable and every change is kept.</span></li>
            <li><b>Protect what we collect.</b><br><span class="muted small">Access depends on role, and sessions end when accounts change.</span></li>
        </ol>
    </section>
</div>

<div class="section-title"><div><h2>Leadership team</h2><p>The people behind the service.</p></div></div>
<div class="people">
    <div class="card person"><div class="avatar">NP</div><b>Nimali Perera</b><p class="muted small">City Manager</p></div>
    <div class="card person"><div class="avatar">KS</div><b>Kasun Silva</b><p class="muted small">Head of Operations</p></div>
    <div class="card person"><div class="avatar">AF</div><b>Amal Fernando</b><p class="muted small">Field Services Lead</p></div>
    <div class="card person"><div class="avatar">DJ</div><b>Dilani Jayawardena</b><p class="muted small">Resident Support Lead</p></div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
