<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Privacy and terms"); request.setAttribute("active", "help"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head"><div><h1>Privacy and terms</h1>
    <p class="lead">How we handle your information, and the rules for using SmartCity360. Sample text for demonstration.</p></div></div>

<section class="card prose" id="privacy">
    <h2 style="margin-top:0">Privacy policy</h2>
    <p>We collect only what we need to run city services: your name, contact details, the complaints you raise, and the activity
        needed to keep the platform secure.</p>
    <h2>How we use it</h2>
    <ul>
        <li>To route and resolve your complaints and keep you informed.</li>
        <li>To protect accounts, for example by locking logins after repeated failures.</li>
        <li>To produce reports for city managers, using counts and totals rather than personal details.</li>
    </ul>
    <h2>Who can see it</h2>
    <p>Residents see only their own complaints. Staff see the complaints assigned to or relevant to their role. Administrators manage
        accounts but cannot read passwords, which are stored only as one-way hashes.</p>
    <h2>Your choices</h2>
    <p>You can update your details at any time from your profile. To have your account removed, contact support.</p>
</section>

<section class="card prose" id="terms" style="margin-top:22px">
    <h2 style="margin-top:0">Terms of use</h2>
    <p>By using SmartCity360 you agree to use it only for its intended purpose: running and following city services.</p>
    <ul>
        <li>Keep your login private. You are responsible for activity under your account.</li>
        <li>Give accurate information when you raise a complaint. False reports may be closed.</li>
        <li>Do not try to access areas of the platform your role does not allow.</li>
        <li>We may change or pause services for maintenance, and will give notice where we can.</li>
    </ul>
    <p>Questions about these terms? <a href="${pageContext.request.contextPath}/contact.jsp">Contact us</a>.</p>
</section>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
