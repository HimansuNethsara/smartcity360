<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Log in"); request.setAttribute("active", "login"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="auth-split">
    <aside class="auth-side">
        <a class="brand" href="${pageContext.request.contextPath}/"><span class="logo" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V9l5-3v15"/><path d="M10 21V4l9 4v13"/><path d="M13 11h3M13 15h3"/></svg></span><span>SmartCity<b>360</b></span></a>
        <h2>Welcome back</h2>
        <p>Log in to follow your requests and run the city's services.</p>
        <ul>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg><span><b>Report and follow problems</b><br>Every complaint has an owner and a deadline.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg><span><b>Stay informed</b><br>Updates arrive in your notifications as soon as they happen.</span></li>
            <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3 4 6v6c0 5 3.4 8 8 9 4.6-1 8-4 8-9V6z"/><path d="m9 12 2 2 4-4"/></svg><span><b>Your data stays yours</b><br>Access depends on your role, and sessions end on account changes.</span></li>
        </ul>
    </aside>
    <section class="authcard">
        <h1>Log in</h1>
        <p class="muted small">Use your SmartCity360 account.</p>
        <form id="loginForm" novalidate>
            <div class="field"><label for="lUser">Username</label>
                <input type="text" id="lUser" autocomplete="username" maxlength="50" required autofocus></div>
            <div class="field"><label for="lPass">Password</label>
                <div class="pw"><input type="password" id="lPass" autocomplete="current-password" maxlength="72" required><button class="pw-toggle" type="button" aria-label="Show or hide password" data-target="lPass"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
            <div class="form-msg" id="formMsg" role="alert"></div>
            <button class="btn primary block" type="submit" id="btnLogin">Log in</button>
        </form>
        <p class="authlinks">New resident? <a href="${pageContext.request.contextPath}/register.jsp">Create an account</a></p>
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
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
