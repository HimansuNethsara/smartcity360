<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Register"); request.setAttribute("active", "register"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="auth-split">
    <aside class="auth-side">
        <a class="brand" href="${pageContext.request.contextPath}/"><span class="logo" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V9l5-3v15"/><path d="M10 21V4l9 4v13"/><path d="M13 11h3M13 15h3"/></svg></span><span>SmartCity<b>360</b></span></a>
        <h2>Join SmartCity360</h2>
        <p>Create a resident account to raise requests and track them until they are fixed.</p>
        <ul>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg><span><b>Report and follow problems</b><br>Every complaint has an owner and a deadline.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg><span><b>Stay informed</b><br>Updates arrive in your notifications as soon as they happen.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3 4 6v6c0 5 3.4 8 8 9 4.6-1 8-4 8-9V6z"/><path d="m9 12 2 2 4-4"/></svg><span><b>Your data stays yours</b><br>Access depends on your role, and sessions end on account changes.</span></li>
        </ul>
    </aside>
    <section class="authcard">
        <h1>Create an account</h1>
        <p class="muted small">Resident accounts can raise and follow complaints.</p>
        <form id="registerForm" novalidate>
            <div class="field"><label for="rName">Full name</label>
                <input type="text" id="rName" autocomplete="name" maxlength="100" required autofocus></div>
            <div class="twocol">
                <div class="field"><label for="rUser">Username</label>
                    <input type="text" id="rUser" autocomplete="username" maxlength="30" required></div>
                <div class="field"><label for="rPhone">Phone (optional)</label>
                    <input type="tel" id="rPhone" autocomplete="tel" maxlength="20"></div>
            </div>
            <div class="field"><label for="rEmail">Email</label>
                <input type="email" id="rEmail" autocomplete="email" maxlength="120" required></div>
            <div class="twocol">
                <div class="field"><label for="rPass">Password</label>
                    <div class="pw"><input type="password" id="rPass" autocomplete="new-password" maxlength="72" required><button class="pw-toggle" type="button" aria-label="Show or hide password" data-target="rPass"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
                <div class="field"><label for="rPass2">Repeat password</label>
                    <div class="pw"><input type="password" id="rPass2" autocomplete="new-password" maxlength="72" required><button class="pw-toggle" type="button" aria-label="Show or hide password" data-target="rPass2"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
            </div>
            <div class="strength" id="strength" aria-hidden="true"><i></i><i></i><i></i><i></i></div>
            <div class="hint" id="pwHint">Username: 3-30 letters, digits, dot, dash or underscore. Password: at least 8 characters with a letter and a digit.</div>
            <div class="form-msg" id="formMsg" role="alert"></div>
            <button class="btn primary block" type="submit" id="btnRegister">Create account</button>
        </form>
        <p class="authlinks">Already registered? <a href="${pageContext.request.contextPath}/login.jsp">Log in</a></p>
    </section>
</div>

<script src="${pageContext.request.contextPath}/js/auth.js"></script>
<script>
    document.querySelectorAll('.pw-toggle').forEach(function (b) {
        b.addEventListener('click', function () {
            var el = document.getElementById(b.dataset.target);
            el.type = el.type === 'password' ? 'text' : 'password';
        });
    });
</script>
<script>
    /* Password strength bar: length, letter, digit, symbol. Guidance only; the server enforces the real rules. */
    (function () {
        var pw = document.getElementById('rPass'), bars = document.querySelectorAll('#strength i');
        pw.addEventListener('input', function () {
            var v = pw.value, score = 0;
            if (v.length >= 8) score++;
            if (/[A-Za-z]/.test(v) && /\d/.test(v)) score++;
            if (v.length >= 12) score++;
            if (/[^A-Za-z0-9]/.test(v)) score++;
            bars.forEach(function (b, i) { b.className = i < score ? 's' + score : ''; });
        });
    })();
</script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
