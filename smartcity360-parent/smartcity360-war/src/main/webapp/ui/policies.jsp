<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Policies"); request.setAttribute("active", "policies"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="page-head">
    <div>
        <nav class="crumbs" aria-label="Breadcrumb"><a href="${pageContext.request.contextPath}/">Home</a><span>/</span><span>Services</span><span>/</span><b>City policies</b></nav>
        <h1><span class="title-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></svg></span>City policies <span class="sub"> &middot; Interpreter</span></h1>
        <p class="lead">A policy is a small rule written in plain text. The system reads it, checks it, and applies it to a
            zone's sensor readings. Every edit is kept as a version, so any earlier wording can be restored.</p>
    </div>
</div>

<div class="grid">
    <div>
        <section class="card">
            <div class="card-head">
                <h2 id="edTitle">New policy</h2>
                <button class="btn small" id="btnNew" type="button" ${scManager ? '' : 'hidden'}>+ New</button>
            </div>
            <form id="edForm">
                <div class="field"><label for="pName">Name</label>
                    <input type="text" id="pName" maxlength="100" required></div>
                <div class="field"><label for="pDesc">Description</label>
                    <input type="text" id="pDesc" maxlength="255"></div>
                <div class="field"><label for="pDsl">Rule</label>
                    <textarea id="pDsl" class="mono" maxlength="500" spellcheck="false" required
                              placeholder='IF noise > 70 AND zone = "RESIDENTIAL" THEN FINE 5000'></textarea></div>
                <div class="chips" id="examples"></div>
                <div id="vBox" class="vbox" hidden></div>
                <div class="btnrow">
                    <button class="btn primary" type="submit" id="btnSave" ${scManager ? '' : 'hidden'}>Save policy</button>
                    <button class="btn" type="button" id="btnToggle" hidden></button>
                </div>
            </form>
            <h3>Rule language</h3>
            <p class="small muted" style="margin:0">
                <span class="mono">IF</span> a condition <span class="mono">THEN FINE</span> an amount, or
                <span class="mono">THEN ALERT</span> "a message". Conditions compare
                <span class="mono">noise</span>, <span class="mono">temperature</span>,
                <span class="mono">air_quality</span>, <span class="mono">hour</span> (numbers, 0-23 for hour) or
                <span class="mono">zone</span> (text) using <span class="mono">&gt; &gt;= &lt; &lt;= = !=</span>.
                Join them with <span class="mono">AND</span>, <span class="mono">OR</span>,
                <span class="mono">NOT</span> and brackets. <span class="mono">AND</span> binds tighter than
                <span class="mono">OR</span>.</p>
        </section>

        <section class="card" style="margin-top:20px" id="verCard" hidden>
            <div class="card-head"><h2>Version history</h2></div>
            <table>
                <thead><tr><th>#</th><th>Rule</th><th>By</th><th></th></tr></thead>
                <tbody id="verRows"></tbody>
            </table>
        </section>
    </div>

    <div>
        <section class="card">
            <div class="card-head"><h2>All policies</h2>
                <button class="btn small" id="btnRunAll" type="button" ${scSupervisor ? '' : 'hidden'}>Run all enabled</button>
            <table class="clickable">
                <thead><tr><th>Name</th><th>Rule</th><th>Status</th><th class="num">Ver.</th></tr></thead>
                <tbody id="rows"><tr><td colspan="4" class="muted">Loading...</td></tr></tbody>
            </table>
            <div id="runBox" class="result" hidden></div>
        </section>

        <section class="card" style="margin-top:20px" id="evalCard" hidden>
            <div class="card-head"><h2>Try it: <span id="evName"></span></h2></div>
            <p class="hint">Pick a zone to use its live sensor readings, and/or type values to test "what if".
                Typed values win over live ones.</p>
            <form id="evForm">
                <div class="field"><label for="eZone">Zone (live readings)</label>
                    <select id="eZone"></select></div>
                <div class="evgrid">
                    <div class="field"><label for="eNoise">Noise (dB)</label><input type="number" step="any" id="eNoise"></div>
                    <div class="field"><label for="eTemp">Temperature (&deg;C)</label><input type="number" step="any" id="eTemp"></div>
                    <div class="field"><label for="eAir">Air quality</label><input type="number" step="any" id="eAir"></div>
                    <div class="field"><label for="eHour">Hour (0-23)</label><input type="number" min="0" max="23" id="eHour"></div>
                    <div class="field"><label for="eLand">Land use</label>
                        <select id="eLand"><option value="">(from zone)</option><option>RESIDENTIAL</option>
                            <option>COMMERCIAL</option><option>INDUSTRIAL</option><option>MIXED</option></select></div>
                </div>
                <button class="btn primary" type="submit">Evaluate</button>
            </form>
            <div id="evResult" class="result" hidden></div>
        </section>

        <section class="card" style="margin-top:20px">
            <div class="card-head"><h2>Recent evaluations</h2></div>
            <table>
                <thead><tr><th>When</th><th>Policy</th><th>Zone</th><th>Outcome</th><th>Values used</th></tr></thead>
                <tbody id="evRows"><tr><td colspan="5" class="muted">Loading...</td></tr></tbody>
            </table>
        </section>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/policies.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

