/* Login, register and profile pages. Uses api(), h() and toast() from api.js. */
(() => {
  const $ = (id) => document.getElementById(id);
  const ctx = document.querySelector('meta[name="ctx"]').content;
  const LABEL = { ADMIN: 'Admin', CITY_MANAGER: 'City manager', SUPERVISOR: 'Supervisor', STAFF: 'Staff', CITIZEN: 'Citizen' };

  function nextUrl() {
    const n = new URLSearchParams(location.search).get('next');
    const ok = n && n.startsWith(ctx + '/') && !n.startsWith('//') && !n.includes('://') && !n.includes('\\');
    return ok ? n : ctx + '/index.jsp';
  }

    let countdown = null;

    function message(text, good) {
        const m = $('formMsg');
        if (!m) { toast(text, !good); return; }
        clearInterval(countdown);
        m.className = 'form-msg' + (good ? ' ok' : '');
        // "Too many failed attempts. Try again in 42 seconds." -> keep counting down on the page
        const lock = /^(.*in )(\d+)( seconds\.)$/.exec(text);
        if (!good && lock) {
            let left = Number(lock[2]);
            const show = () => { m.textContent = lock[1] + left + lock[3]; };
            show();
            countdown = setInterval(() => {
                left--;
                if (left <= 0) {
                    clearInterval(countdown);
                    m.textContent = 'You can try again now.';
                    m.className = 'form-msg ok';
                } else {
                    show();
                }
            }, 1000);
            return;
        }
        m.textContent = text;
    }

  async function submit(button, work) {
    button.disabled = true;
    try { await work(); } catch (e) { message(e.message, false); } finally { button.disabled = false; }
  }

  // ------------------------------------------------------------ login
  if ($('loginForm')) {
    $('loginForm').addEventListener('submit', (ev) => {
      ev.preventDefault();
      message('', true);
      submit($('btnLogin'), async () => {
        const user = $('lUser').value.trim();
        const pass = $('lPass').value;
        if (!user || !pass) throw new Error('Enter your username and password.');
        await api('POST', '/auth/login', { username: user, password: pass });
        location.href = nextUrl();
      });
    });
  }

  // ------------------------------------------------------------ register
  if ($('registerForm')) {
    $('registerForm').addEventListener('submit', (ev) => {
      ev.preventDefault();
      message('', true);
      submit($('btnRegister'), async () => {
        if ($('rPass').value !== $('rPass2').value) throw new Error('The two passwords do not match.');
        await api('POST', '/auth/register', {
          fullName: $('rName').value.trim(), username: $('rUser').value.trim(),
          email: $('rEmail').value.trim(), phone: $('rPhone').value.trim(), password: $('rPass').value });
        location.href = ctx + '/index.jsp';
      });
    });
  }

  // ------------------------------------------------------------ profile + password
  if ($('profileForm')) {
    const fill = (me) => {
      $('pUser').textContent = me.username;
      $('pRoles').replaceChildren(...me.roles.map((r) => h('span', { class: 'pill core' }, LABEL[r] || r)));
      $('pName').value = me.fullName || '';
      $('pEmail').value = me.email || '';
      $('pPhone').value = me.phone || '';
    };
    api('GET', '/auth/me').then(fill).catch((e) => toast(e.message, true));

    $('profileForm').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        fill(await api('PUT', '/auth/profile', { fullName: $('pName').value.trim(),
          email: $('pEmail').value.trim(), phone: $('pPhone').value.trim() }));
        toast('Details saved');
      } catch (e) { toast(e.message, true); }
    });

    $('pwForm').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      if ($('pwNew').value !== $('pwNew2').value) { toast('The two new passwords do not match.', true); return; }
      try {
        await api('POST', '/auth/change-password',
          { currentPassword: $('pwOld').value, newPassword: $('pwNew').value });
        $('pwForm').reset();
        toast('Password changed. Other devices are signed out.');
      } catch (e) { toast(e.message, true); }
    });
  }
})();
