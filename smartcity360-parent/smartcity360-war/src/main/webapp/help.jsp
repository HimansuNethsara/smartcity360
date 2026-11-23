<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Help centre"); request.setAttribute("active", "help"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div><h1>Help centre</h1><p class="lead">Quick answers, and a clear guide to what each role can do.</p></div>
    <a class="btn" href="${pageContext.request.contextPath}/contact.jsp">Contact support</a>
</div>

<div class="row2">
    <section class="card">
        <div class="card-head"><h2>Getting started</h2></div>
        <ol class="steps">
            <li><b>Create an account or log in.</b><br><span class="muted small">Residents can register themselves. Staff accounts are created by an administrator.</span></li>
            <li><b>Find your service.</b><br><span class="muted small">Use Operations, Services and Support in the top bar.</span></li>
            <li><b>Watch the bell.</b><br><span class="muted small">Updates about your complaints and reports arrive there.</span></li>
        </ol>
    </section>
    <section>
        <div class="section-title" style="margin-top:0"><h2>Frequently asked questions</h2></div>
    <details class="faq"><summary>How do I report a problem?</summary><p>Open Complaints, choose a category and severity, describe the problem and submit. You will get a notification whenever it moves.</p></details>
    <details class="faq"><summary>What happens if nobody responds?</summary><p>Each complaint has a deadline. When it passes, the complaint moves up one level (staff, then supervisor, then city manager) and the next person is told.</p></details>
    <details class="faq"><summary>Why did my report take a moment to appear?</summary><p>Reports are built in the background so you can keep working. The status changes to DONE and you get a notification when it is ready.</p></details>
    <details class="faq"><summary>Why can I not see a menu or button?</summary><p>Access depends on your role. The table below shows who can do what. If you need more access, contact an administrator.</p></details>
    <details class="faq"><summary>How do I change my password?</summary><p>Open your profile from the menu at the top right. Changing it signs you out on every other device.</p></details>
    <details class="faq"><summary>Can I switch to dark mode?</summary><p>Yes. Use the moon or sun button in the top bar. Your choice is remembered on this device.</p></details>
    </section>
</div>

<div class="section-title"><div><h2>Who can do what</h2><p>Access by role. The server enforces this, not only the menus.</p></div></div>
<section class="card">
    <table class="matrix">
        <thead><tr><th>Action</th><th>Admin</th><th>City manager</th><th>Supervisor</th><th>Staff</th><th>Citizen</th></tr></thead>
        <tbody>
            <tr><td>View meters, zones, streetlights, policies</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Request reports</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Policy violation reports, run policies</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Change meter layers, zone power, edit policies</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Create meters, edit streetlights, generate lights</td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Manage users</td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td><td><span class="no">&ndash;</span></td></tr>
            <tr><td>Raise and follow own complaints</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td></tr>
            <tr><td>Handle and escalate any complaint</td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="yes">&#10003;</span></td><td><span class="no">&ndash;</span></td></tr>
        </tbody>
    </table>
</section>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
