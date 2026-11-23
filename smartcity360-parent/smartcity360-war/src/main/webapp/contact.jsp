<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Contact us"); request.setAttribute("active", "contact"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div><h1>Contact us</h1><p class="lead">Questions, faults or feedback? Our operations centre is happy to help.</p></div>
</div>

<div class="contact-grid">
    <section class="card">
        <div class="card-head"><h2>Get in touch</h2></div>
        <ul class="contact-list">
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s7-6.1 7-11a7 7 0 1 0-14 0c0 4.9 7 11 7 11z"/><circle cx="12" cy="10" r="2.5"/></svg><div><b>Operations Centre</b><span class="muted">42 Galle Road, Colombo 03, Sri Lanka</span></div></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2z"/></svg><div><b>+94 11 234 5678</b><span class="muted">24/7 emergency line: 1919</span></div></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/></svg><div><b>support@smartcity360.lk</b><span class="muted">We reply within one working day</span></div></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg><div><b>Opening hours</b><span class="muted">Monday to Friday, 8:00 am to 5:00 pm<br>Saturday, 8:00 am to 12:00 noon</span></div></li>
        </ul>
        <h3>District offices</h3>
        <table>
            <tbody>
            <tr><td>North District</td><td class="muted small">12 Lake Road</td><td class="muted small">011 234 5001</td></tr>
            <tr><td>Central District</td><td class="muted small">8 Station Road</td><td class="muted small">011 234 5002</td></tr>
            <tr><td>Port District</td><td class="muted small">3 Harbour Avenue</td><td class="muted small">011 234 5003</td></tr>
            </tbody>
        </table>
    </section>

    <section class="card">
        <div class="card-head"><h2>Send us a message</h2></div>
        <form id="contactForm">
            <div class="twocol">
                <div class="field"><label for="cName">Your name</label><input type="text" id="cName" maxlength="80" required></div>
                <div class="field"><label for="cEmail">Email</label><input type="email" id="cEmail" maxlength="120" required></div>
            </div>
            <div class="field"><label for="cTopic">Topic</label>
                <select id="cTopic"><option>General question</option><option>Report a fault</option>
                    <option>Account help</option><option>Feedback</option></select></div>
            <div class="field"><label for="cMsg">Message</label><textarea id="cMsg" maxlength="1000" required></textarea></div>
            <div class="btnrow"><button class="btn primary" type="submit">Send message</button></div>
            <p class="hint">For a street-level problem such as a broken light or noise, please use <a href="${pageContext.request.contextPath}/ui/complaints.jsp">Complaints</a> so it is tracked.</p>
        </form>
    </section>
</div>

<script>
    document.getElementById('contactForm').addEventListener('submit', function (e) {
        e.preventDefault();
        this.reset();
        toast('Thank you. Your message has been sent (demo: nothing is stored).');
    });
</script>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
