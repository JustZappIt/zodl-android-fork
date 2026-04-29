// Zapp onboarding — sharp, crisp, open
// borderRadius: 0 on all UI elements. Generous white space.

const MSG_SEED    = ['river','anchor','willow','lantern','silver','harbor','maple','breeze','copper','quartz','canyon','ember'];
const WALLET_SEED = ['forest','orange','mellow','signal','rapid','corner','voyage','flame','beach','candle','prism','quiet'];
const TOTAL_STEPS = 6;

const OnbProgress = ({ t, step, total }) => (
  <div style={{ display:'flex', gap:3, flexShrink:0 }}>
    {Array.from({ length:total }).map((_,i) => (
      <div key={i} style={{ flex:1, height:2, background: i<step ? t.accent : t.border }}/>
    ))}
  </div>
);

const GhostNum = ({ n, t }) => (
  <div style={{
    position:'absolute', right:-4, top:-28, zIndex:0,
    fontSize:130, fontWeight:900, lineHeight:1, letterSpacing:-6,
    color:t.surfaceAlt, userSelect:'none', pointerEvents:'none',
    fontVariantNumeric:'tabular-nums',
  }}>{String(n).padStart(2,'0')}</div>
);

// ── Logo mark ─────────────────────────────────────────────────
const ZappLogo = ({ size=44 }) => (
  <div style={{ width:size, height:size, background:'#ff9417', overflow:'hidden', position:'relative', flexShrink:0 }}>
    <svg width={size} height={size} viewBox="0 0 100 100" fill="none" style={{ position:'absolute', inset:0 }}>
      <path d="M78 -8 L-8 78 L-8 92 L92 -8 Z" fill="#fff"/>
      <path d="M108 18 L18 108 L32 108 L108 32 Z" fill="#fff"/>
      <path d="M44 95 L72 5" stroke="#fff" strokeWidth="6" strokeLinecap="square"/>
      <path d="M51 99 L79 9" stroke="#fff" strokeWidth="6" strokeLinecap="square"/>
      <path d="M58 103 L86 13" stroke="#fff" strokeWidth="6" strokeLinecap="square"/>
    </svg>
  </div>
);

// ── Welcome ───────────────────────────────────────────────────
const OnbWelcome = ({ t, onNew, onRestore }) => (
  <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
    <div style={{ flex:1, padding:'36px 28px 0', display:'flex', flexDirection:'column', justifyContent:'center', overflow:'hidden' }}>
      <div style={{ display:'flex', alignItems:'center', gap:12, marginBottom:40 }}>
        <ZappLogo size={40}/>
        <div style={{ fontSize:22, fontWeight:900, letterSpacing:-0.4 }}>Zapp</div>
      </div>

      <div style={{ fontSize:54, fontWeight:900, lineHeight:0.95, letterSpacing:-2.8, color:t.text }}>
        Chat<br/>privately.
      </div>
      <div style={{ fontSize:54, fontWeight:900, lineHeight:0.95, letterSpacing:-2.8, color:t.accent, marginTop:6 }}>
        Send<br/>instantly.
      </div>

      <div style={{ width:36, height:3, background:t.text, margin:'24px 0 20px' }}/>

      <div style={{ fontSize:14, color:t.textMuted, lineHeight:1.6, maxWidth:260 }}>
        End-to-end encrypted messaging with a self-custody wallet. No phone number. No email.
      </div>
    </div>

    <div style={{ padding:'16px 28px 24px', flexShrink:0, borderTop:`1px solid ${t.text}`, display:'flex', flexDirection:'column', gap:0 }}>
      <button onClick={onNew} style={{ height:52, background:t.accent, color:'#fff', border:'none', cursor:'pointer', fontSize:15, fontWeight:900, letterSpacing:0.2, fontFamily:ZAPP_FONT, borderBottom:`1px solid ${t.border}` }}>
        Create new account
      </button>
      <button onClick={onRestore} style={{ height:48, background:'transparent', color:t.textMuted, border:`1px solid ${t.border}`, borderTop:'none', cursor:'pointer', fontSize:13, fontWeight:800, fontFamily:ZAPP_FONT }}>
        Restore with recovery phrases
      </button>
      <div style={{ fontSize:10, color:t.textSubtle, textAlign:'center', letterSpacing:0.3, marginTop:12 }}>
        By continuing you accept our terms &amp; privacy policy.
      </div>
    </div>
  </div>
);

// ── Phase intro ───────────────────────────────────────────────
const OnbPhaseIntro = ({ t, step, total, badge, title, sub, points, onNext, onBack }) => (
  <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
    <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
      <OnbProgress t={t} step={step} total={total}/>
    </div>
    <div style={{ flex:1, padding:'24px 28px 0', overflow:'hidden', display:'flex', flexDirection:'column', justifyContent:'center' }}>
      <div style={{ position:'relative', overflow:'visible' }}>
        <GhostNum n={step} t={t}/>
        <div style={{ position:'relative', zIndex:1 }}>
          <div style={{ fontSize:10, fontWeight:900, letterSpacing:2.5, color:t.accent, textTransform:'uppercase', marginBottom:14 }}>{badge}</div>
          <div style={{ fontSize:42, fontWeight:900, letterSpacing:-1.8, lineHeight:1.02, color:t.text, maxWidth:260 }}>{title}</div>
          <div style={{ fontSize:13, color:t.textMuted, marginTop:16, lineHeight:1.65, maxWidth:270 }}>{sub}</div>
        </div>
      </div>
      <div style={{ marginTop:32, display:'flex', flexDirection:'column', gap:0 }}>
        {points.map((p, i) => (
          <div key={p.label} style={{ display:'flex', gap:16, padding:'14px 0', borderTop: i===0 ? `1px solid ${t.border}` : 'none', borderBottom:`1px solid ${t.border}` }}>
            <div style={{ width:3, background:t.accent, flexShrink:0, alignSelf:'stretch' }}/>
            <div>
              <div style={{ fontSize:14, fontWeight:900, letterSpacing:-0.2 }}>{p.label}</div>
              {p.sub && <div style={{ fontSize:12, color:t.textMuted, marginTop:3 }}>{p.sub}</div>}
            </div>
          </div>
        ))}
      </div>
    </div>
    <BottomDock t={t} cta="Continue" onCta={onNext} back onBack={onBack}/>
  </div>
);

// ── Username ──────────────────────────────────────────────────
const OnbUsername = ({ t, username, setUsername, onNext, onBack, step=2, total=TOTAL_STEPS, badge='Part 1 · Username' }) => {
  const valid = /^[a-z0-9_]{3,20}$/.test(username);
  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
        <OnbProgress t={t} step={step} total={total}/>
      </div>
      <div style={{ flex:1, padding:'24px 28px 0', overflow:'hidden', display:'flex', flexDirection:'column', justifyContent:'center' }}>
        <div style={{ position:'relative', overflow:'visible' }}>
          <GhostNum n={step} t={t}/>
          <div style={{ position:'relative', zIndex:1 }}>
            <div style={{ fontSize:10, fontWeight:900, letterSpacing:2.5, color:t.accent, textTransform:'uppercase', marginBottom:14 }}>{badge}</div>
            <div style={{ fontSize:42, fontWeight:900, letterSpacing:-1.8, lineHeight:1.02 }}>Choose a<br/>username</div>
            <div style={{ fontSize:13, color:t.textMuted, marginTop:14, lineHeight:1.6 }}>
              This is how friends find you. It cannot be changed later.
            </div>
          </div>
        </div>

        <div style={{ marginTop:32 }}>
          <div style={{ display:'flex', alignItems:'baseline', gap:2, borderBottom:`2px solid ${username ? t.text : t.border}`, paddingBottom:12, transition:'border-color .2s' }}>
            <span style={{ fontSize:28, fontWeight:900, color:t.textSubtle }}>@</span>
            <input value={username} onChange={e => setUsername(e.target.value.toLowerCase().replace(/[^a-z0-9_]/g,''))}
              placeholder="your_handle" autoFocus
              style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:28, fontWeight:900, letterSpacing:-0.8, color:t.text, fontFamily:ZAPP_FONT }}/>
            {valid && <Icon name="check" size={22} style={{ color:t.success }}/>}
          </div>
          <div style={{ display:'flex', gap:14, marginTop:12 }}>
            {[
              { ok:username.length>=3, l:'3+ chars' },
              { ok:username.length<=20, l:'≤20 chars' },
              { ok:/^[a-z0-9_]*$/.test(username), l:'a–z 0–9 _' },
            ].map(r => (
              <div key={r.l} style={{ display:'flex', alignItems:'center', gap:4, fontSize:11, fontWeight:800, color:(r.ok&&username)?t.success:t.textSubtle }}>
                <Icon name={r.ok&&username?'check':'x'} size={11} stroke={3}/>{r.l}
              </div>
            ))}
          </div>
        </div>

        <div style={{ marginTop:20, padding:'12px 0', borderTop:`1px solid ${t.border}`, display:'flex', gap:10, alignItems:'flex-start' }}>
          <Icon name="shield" size={13} style={{ color:t.accent, flexShrink:0, marginTop:1 }}/>
          <div style={{ fontSize:12, color:t.textSubtle, lineHeight:1.55 }}>
            Zapp generates a local keypair. No server ever sees your private key.
          </div>
        </div>
      </div>
      <BottomDock t={t} cta="Continue" ctaDisabled={!valid} onCta={onNext} back onBack={onBack}/>
    </div>
  );
};

// ── Wallet choice ─────────────────────────────────────────────
const OnbWallet = ({ t, onNew, onRestore, onBack, step=4, total=TOTAL_STEPS }) => (
  <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
    <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
      <OnbProgress t={t} step={step} total={total}/>
    </div>
    <div style={{ flex:1, padding:'24px 28px 0', overflow:'hidden', display:'flex', flexDirection:'column', justifyContent:'center' }}>
      <div style={{ position:'relative', overflow:'visible' }}>
        <GhostNum n={step} t={t}/>
        <div style={{ position:'relative', zIndex:1 }}>
          <div style={{ fontSize:10, fontWeight:900, letterSpacing:2.5, color:t.accent, textTransform:'uppercase', marginBottom:14 }}>Part 2 · Wallet</div>
          <div style={{ fontSize:42, fontWeight:900, letterSpacing:-1.8, lineHeight:1.02 }}>Set up<br/>your wallet</div>
          <div style={{ fontSize:13, color:t.textMuted, marginTop:14, lineHeight:1.6 }}>Your wallet lives on this device. Only you hold the keys.</div>
        </div>
      </div>
      <div style={{ marginTop:32, display:'flex', flexDirection:'column', border:`1px solid ${t.border}`, overflow:'hidden' }}>
        {[
          { icon:'sparkle', title:'Create new wallet',   sub:'Get a fresh 12-word recovery phrase', onClick:onNew,     hi:true },
          { icon:'key',     title:'Restore from phrase', sub:'Use your existing 12 or 24-word phrase', onClick:onRestore, hi:false },
        ].map((o, i) => (
          <button key={o.title} onClick={o.onClick} style={{
            display:'flex', alignItems:'center', gap:14, padding:'18px 18px',
            border:'none', background: o.hi ? t.accentSoft : t.bg,
            borderTop: i>0 ? `1px solid ${t.border}` : 'none',
            cursor:'pointer', textAlign:'left', color:t.text, fontFamily:ZAPP_FONT,
          }}>
            <div style={{ width:36, height:36, flexShrink:0, background: o.hi ? t.accent : t.surfaceAlt, color: o.hi ? '#fff' : t.text, display:'flex', alignItems:'center', justifyContent:'center' }}>
              <Icon name={o.icon} size={17}/>
            </div>
            <div style={{ flex:1 }}>
              <div style={{ fontSize:14, fontWeight:900 }}>{o.title}</div>
              <div style={{ fontSize:12, color:t.textMuted, marginTop:2 }}>{o.sub}</div>
            </div>
            <Icon name="chevron-right" size={15} style={{ color:t.textSubtle }}/>
          </button>
        ))}
      </div>
    </div>
    <BottomDock t={t} cta="Back" onCta={onBack}/>
  </div>
);

// ── Seed phrase — 3-col grid, sharp ───────────────────────────
const OnbSeedShow = ({ t, onNext, onBack, step=3, total=TOTAL_STEPS, title='Recovery phrase', sub, seed }) => {
  const phrase = seed || WALLET_SEED;
  const [revealed, setRevealed] = React.useState(false);
  const [saved, setSaved] = React.useState(false);
  const rows = [phrase.slice(0,3), phrase.slice(3,6), phrase.slice(6,9), phrase.slice(9,12)];
  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
        <OnbProgress t={t} step={step} total={total}/>
      </div>
      <div style={{ flex:1, padding:'18px 28px 0', display:'flex', flexDirection:'column', overflow:'hidden' }}>
        <div style={{ fontSize:26, fontWeight:900, letterSpacing:-0.8, lineHeight:1.05, flexShrink:0 }}>{title}</div>
        <div style={{ fontSize:12, color:t.textMuted, marginTop:8, lineHeight:1.6, flexShrink:0 }}>
          {sub || 'Write these 12 words in order. Anyone with this phrase can access your account.'}
        </div>

        {/* 3-col phrase grid */}
        <div style={{ marginTop:16, position:'relative', flexShrink:0 }}>
          <div style={{ border:`1px solid ${t.border}`, overflow:'hidden', filter:revealed?'none':'blur(7px)', transition:'filter .3s' }}>
            {rows.map((row, ri) => (
              <div key={ri} style={{ display:'flex', borderTop:ri>0?`1px solid ${t.border}`:'none' }}>
                {row.map((w, wi) => {
                  const idx = ri*3+wi;
                  return (
                    <div key={wi} style={{ flex:1, padding:'10px 10px', borderLeft:wi>0?`1px solid ${t.border}`:'none', background:ri%2===0?t.bg:t.surfaceAlt, display:'flex', alignItems:'center', gap:7 }}>
                      <span style={{ fontSize:9, fontFamily:ZAPP_MONO, color:t.textSubtle, flexShrink:0, width:14 }}>{String(idx+1).padStart(2,'0')}</span>
                      <span style={{ fontSize:13, fontWeight:800, letterSpacing:-0.1 }}>{w}</span>
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
          {!revealed && (
            <button onClick={() => setRevealed(true)} style={{ position:'absolute', inset:0, background:'transparent', border:'none', cursor:'pointer', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:8 }}>
              <div style={{ width:44, height:44, background:t.text, color:t.bg, display:'flex', alignItems:'center', justifyContent:'center' }}>
                <Icon name="eye" size={19}/>
              </div>
              <span style={{ fontSize:12, fontWeight:900, color:t.text, letterSpacing:0.2 }}>Tap to reveal</span>
            </button>
          )}
        </div>

        <button onClick={() => setSaved(!saved)} style={{ marginTop:16, display:'flex', alignItems:'flex-start', gap:12, border:'none', background:'transparent', cursor:'pointer', color:t.text, textAlign:'left', padding:0, flexShrink:0 }}>
          <div style={{ width:20, height:20, flexShrink:0, marginTop:1, border:`2px solid ${saved?t.accent:t.borderStrong}`, background:saved?t.accent:'transparent', display:'flex', alignItems:'center', justifyContent:'center', transition:'all .15s' }}>
            {saved && <Icon name="check" size={12} stroke={3} style={{ color:'#fff' }}/>}
          </div>
          <span style={{ fontSize:12, color:t.textMuted, lineHeight:1.6 }}>
            I've written all 12 words in order. I understand this phrase cannot be recovered if lost.
          </span>
        </button>
      </div>
      <BottomDock t={t} cta="I've saved it" ctaDisabled={!revealed||!saved} onCta={onNext} back onBack={onBack}/>
    </div>
  );
};

// ── Restore — 3-col grid ──────────────────────────────────────
const OnbRestore = ({ t, onNext, onBack, title='Restore account', sub='Enter your 12-word recovery phrase.', step=2, total=TOTAL_STEPS }) => {
  const [words, setWords] = React.useState(Array(12).fill(''));
  const filled = words.filter(Boolean).length;
  const rows = [words.slice(0,3), words.slice(3,6), words.slice(6,9), words.slice(9,12)];
  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
        <OnbProgress t={t} step={step} total={total}/>
      </div>
      <div style={{ flex:1, padding:'18px 28px 0', display:'flex', flexDirection:'column', overflow:'hidden' }}>
        <div style={{ fontSize:26, fontWeight:900, letterSpacing:-0.8, lineHeight:1.05, flexShrink:0 }}>{title}</div>
        <div style={{ fontSize:12, color:t.textMuted, marginTop:8, lineHeight:1.6, flexShrink:0 }}>{sub}</div>
        <div style={{ marginTop:16, flexShrink:0, border:`1px solid ${t.border}`, overflow:'hidden' }}>
          {rows.map((row, ri) => (
            <div key={ri} style={{ display:'flex', borderTop:ri>0?`1px solid ${t.border}`:'none' }}>
              {row.map((w, wi) => {
                const idx = ri*3+wi;
                return (
                  <div key={wi} style={{ flex:1, padding:'9px 8px', borderLeft:wi>0?`1px solid ${t.border}`:'none', background:w?t.accentSoft:(ri%2===0?t.bg:t.surfaceAlt), display:'flex', alignItems:'center', gap:5 }}>
                    <span style={{ fontSize:9, fontFamily:ZAPP_MONO, color:t.textSubtle, flexShrink:0, width:14 }}>{String(idx+1).padStart(2,'0')}</span>
                    <input value={w} onChange={e => { const n=[...words]; n[idx]=e.target.value.toLowerCase().replace(/[^a-z]/g,''); setWords(n); }}
                      style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:13, fontWeight:800, color:w?t.accentText:t.text, width:'100%', minWidth:0 }}/>
                  </div>
                );
              })}
            </div>
          ))}
        </div>
        <button style={{ marginTop:10, border:'none', background:'transparent', color:t.accent, fontSize:12, fontWeight:800, cursor:'pointer', padding:'2px 0', textAlign:'left', flexShrink:0, letterSpacing:0.2 }}>
          + Switch to 24-word phrase
        </button>
      </div>
      <BottomDock t={t} cta={`Restore${filled===12?'':` (${filled}/12)`}`} ctaDisabled={filled<12} onCta={onNext} back onBack={onBack}/>
    </div>
  );
};

// ── 2FA ───────────────────────────────────────────────────────
const Onb2FA = ({ t, onNext, onBack, step=6, total=TOTAL_STEPS, badge='Part 3 of 3 · Secure Zapp' }) => {
  const [phase, setPhase] = React.useState('choose');
  const [mode, setMode] = React.useState(null);
  const [pin, setPin] = React.useState('');
  const [confirm, setConfirm] = React.useState('');

  if (phase === 'choose') return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
        <OnbProgress t={t} step={step} total={total}/>
      </div>
      <div style={{ flex:1, padding:'24px 28px 0', display:'flex', flexDirection:'column', justifyContent:'center', overflow:'hidden' }}>
        <div style={{ position:'relative', overflow:'visible' }}>
          <GhostNum n={step} t={t}/>
          <div style={{ position:'relative', zIndex:1 }}>
            <div style={{ fontSize:10, fontWeight:900, letterSpacing:2.5, color:t.accent, textTransform:'uppercase', marginBottom:14 }}>{badge}</div>
            <div style={{ fontSize:42, fontWeight:900, letterSpacing:-1.8, lineHeight:1.02 }}>Secure<br/>your app</div>
            <div style={{ fontSize:13, color:t.textMuted, marginTop:14, lineHeight:1.6 }}>Choose how you unlock Zapp and authorise payments.</div>
          </div>
        </div>
        <div style={{ marginTop:32, display:'flex', flexDirection:'column', border:`1px solid ${t.border}`, overflow:'hidden' }}>
          {[
            { icon:'fingerprint', title:'Biometric',   sub:'Fingerprint or face — fastest', onClick:()=>{setMode('bio');setPhase('scanning');}, hi:true },
            { icon:'key',         title:'6-digit PIN', sub:'A passcode you remember',       onClick:()=>{setMode('pin');setPhase('set');},     hi:false },
          ].map((o, i) => (
            <button key={o.title} onClick={o.onClick} style={{
              display:'flex', alignItems:'center', gap:14, padding:'18px 18px',
              border:'none', background:o.hi?t.accentSoft:t.bg,
              borderTop:i>0?`1px solid ${t.border}`:'none',
              cursor:'pointer', textAlign:'left', color:t.text, fontFamily:ZAPP_FONT,
            }}>
              <div style={{ width:36, height:36, flexShrink:0, background:o.hi?t.accent:t.surfaceAlt, color:o.hi?'#fff':t.text, display:'flex', alignItems:'center', justifyContent:'center' }}>
                <Icon name={o.icon} size={17}/>
              </div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:14, fontWeight:900 }}>{o.title}</div>
                <div style={{ fontSize:12, color:t.textMuted, marginTop:2 }}>{o.sub}</div>
              </div>
              <Icon name="chevron-right" size={15} style={{ color:t.textSubtle }}/>
            </button>
          ))}
        </div>
      </div>
      <BottomDock t={t} cta="Back" onCta={onBack}/>
    </div>
  );

  if (phase === 'scanning') return <BioScan t={t} onDone={() => setPhase('done')} onCancel={() => setPhase('choose')}/>;

  if (phase === 'set' || phase === 'confirm') {
    const isSet = phase === 'set';
    const value = isSet ? pin : confirm;
    const setValue = isSet ? setPin : setConfirm;
    const onDigit = d => {
      if (value.length >= 6) return;
      const nv = value + d;
      setValue(nv);
      if (nv.length === 6) {
        if (isSet) setTimeout(() => setPhase('confirm'), 180);
        else if (nv === pin) setTimeout(() => setPhase('done'), 200);
        else setTimeout(() => setConfirm(''), 350);
      }
    };
    return (
      <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
        <div style={{ padding:'20px 28px 0', flexShrink:0 }}>
          <OnbProgress t={t} step={step} total={total}/>
        </div>
        <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:6, padding:'0 28px' }}>
          <div style={{ fontSize:30, fontWeight:900, letterSpacing:-0.8, textAlign:'center' }}>
            {isSet ? 'Create a PIN' : 'Confirm PIN'}
          </div>
          <div style={{ fontSize:13, color:t.textMuted, textAlign:'center', marginBottom:22, lineHeight:1.5 }}>
            {isSet ? 'Used to unlock Zapp and confirm payments.' : 'Enter the same PIN again.'}
          </div>
          <div style={{ display:'flex', gap:16, marginBottom:8 }}>
            {Array.from({length:6}).map((_,i) => (
              <div key={i} style={{ width:14, height:14, background:i<value.length?t.accent:'transparent', border:`2px solid ${i<value.length?t.accent:t.borderStrong}`, transition:'all .12s' }}/>
            ))}
          </div>
        </div>
        <div style={{ padding:'0 28px', flexShrink:0 }}>
          <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:2 }}>
            {['1','2','3','4','5','6','7','8','9','','0','⌫'].map((k, i) => (
              <button key={i} onClick={() => k==='⌫'?setValue(value.slice(0,-1)):k&&onDigit(k)} style={{
                height:54, border:'none', background:k?t.surfaceAlt:'transparent', borderRadius:0,
                fontSize:22, fontWeight:700, color:t.text, cursor:k?'pointer':'default',
                display:'flex', alignItems:'center', justifyContent:'center', opacity:k?1:0,
              }}>
                {k==='⌫' ? <Icon name="backspace" size={20}/> : k}
              </button>
            ))}
          </div>
        </div>
        <BottomDock t={t} cta="Back" onCta={() => { isSet?setPhase('choose'):(setConfirm(''),setPhase('set'),setPin('')); }} noBorder/>
      </div>
    );
  }

  if (phase === 'done') return <OnbDone t={t} onNext={onNext} mode={mode}/>;
  return null;
};

// ── Bio scan ──────────────────────────────────────────────────
const BioScan = ({ t, onDone, onCancel }) => {
  const [p, setP] = React.useState(0);
  React.useEffect(() => {
    const iv = setInterval(() => setP(v => { if(v>=100){clearInterval(iv);setTimeout(onDone,300);return 100;} return v+5; }), 50);
    return () => clearInterval(iv);
  }, []);
  const r=68, circ=2*Math.PI*r;
  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:20 }}>
        <div style={{ position:'relative', width:160, height:160 }}>
          <svg width="160" height="160" style={{ position:'absolute', inset:0, transform:'rotate(-90deg)' }}>
            <circle cx="80" cy="80" r={r} fill="none" stroke={t.border} strokeWidth="3"/>
            <circle cx="80" cy="80" r={r} fill="none" stroke={t.accent} strokeWidth="3" strokeLinecap="square"
              strokeDasharray={circ} strokeDashoffset={circ*(1-p/100)} style={{ transition:'stroke-dashoffset .08s' }}/>
          </svg>
          <div style={{ position:'absolute', inset:0, display:'flex', alignItems:'center', justifyContent:'center', color:t.accent, animation:'pulse 1.4s ease-in-out infinite' }}>
            <Icon name="fingerprint" size={72} stroke={1.4}/>
          </div>
        </div>
        <div style={{ textAlign:'center' }}>
          <div style={{ fontSize:28, fontWeight:900, letterSpacing:-0.8 }}>Scanning</div>
          <div style={{ fontSize:13, color:t.textMuted, marginTop:6 }}>Place your finger on the sensor</div>
        </div>
      </div>
      <BottomDock t={t} cta="Cancel" onCta={onCancel}/>
    </div>
  );
};

// ── Done ──────────────────────────────────────────────────────
const OnbDone = ({ t, onNext, mode }) => (
  <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
    <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', padding:'0 28px', animation:'fade-up .4s' }}>
      <div style={{ fontSize:88, fontWeight:900, color:t.accent, lineHeight:1, letterSpacing:-4, marginBottom:24 }}>✓</div>
      <div style={{ fontSize:42, fontWeight:900, letterSpacing:-1.8, lineHeight:1.02, textAlign:'center' }}>You're<br/>all set.</div>
      <div style={{ width:36, height:3, background:t.accent, margin:'20px auto' }}/>
      <div style={{ fontSize:13, color:t.textMuted, lineHeight:1.65, maxWidth:260, textAlign:'center' }}>
        Identity created, wallet ready, secured with {mode==='bio'?'biometrics':'a PIN'}.
      </div>
    </div>
    <BottomDock t={t} cta="Enter Zapp →" onCta={onNext}/>
  </div>
);

const Keypad = () => null;

Object.assign(window, {
  OnbWelcome, OnbPhaseIntro, OnbUsername, OnbWallet,
  OnbSeedShow, OnbRestore, Onb2FA, OnbDone, BioScan,
  ZappLogo, Keypad, OnbProgress, GhostNum,
  MSG_SEED, WALLET_SEED, TOTAL_STEPS,
});
