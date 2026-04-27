// Zapp shell — sharp, crisp, open
// Design rules: 0 border-radius on all UI (except avatar circles + phone bezel)
// Generous whitespace. Thick rules. Orange as sharp graphic element.

const ZappKeyframes = () => (
  <style>{`
    @keyframes fade-in  { from{opacity:0}         to{opacity:1} }
    @keyframes slide-up { from{transform:translateY(100%)} to{transform:translateY(0)} }
    @keyframes fade-up  { from{opacity:0;transform:translateY(10px)} to{opacity:1;transform:translateY(0)} }
    @keyframes pulse    { 0%,100%{transform:scale(1)} 50%{transform:scale(1.06)} }
    * { -webkit-font-smoothing:antialiased; box-sizing:border-box; }
    *::-webkit-scrollbar{width:0;height:0}
    button{font-family:inherit;-webkit-tap-highlight-color:transparent;}
    input,textarea{font-family:inherit;}
  `}</style>
);

// ── Status bar ────────────────────────────────────────────────
const ZStatusBar = ({ t, time = '9:41' }) => (
  <div style={{ height:38, padding:'0 22px 0 26px', display:'flex', alignItems:'center', justifyContent:'space-between', color:t.text, flexShrink:0, fontFamily:ZAPP_FONT }}>
    <span style={{ fontSize:13, fontWeight:800, letterSpacing:-0.2 }}>{time}</span>
    <div style={{ position:'absolute', left:'50%', top:8, transform:'translateX(-50%)', width:20, height:20, borderRadius:10, background:'#0f0e0c' }}/>
    <div style={{ display:'flex', alignItems:'center', gap:5 }}>
      <svg width="14" height="10" viewBox="0 0 14 10"><path d="M7 9L.5 2.5a9 9 0 0 1 13 0z" fill="currentColor"/></svg>
      <svg width="14" height="10" viewBox="0 0 14 10"><path d="M13.5 9.5V.5L.5 9.5z" fill="currentColor"/></svg>
      <svg width="22" height="11" viewBox="0 0 22 11"><rect x=".5" y=".5" width="18" height="10" rx="0" fill="none" stroke="currentColor" strokeOpacity=".35"/><rect x="2" y="2" width="14" height="7" rx="0" fill="currentColor"/><path d="M20 3.5v4a2 2 0 0 0 0-4z" fill="currentColor" fillOpacity=".4"/></svg>
    </div>
  </div>
);

const ZNavGesture = ({ t }) => (
  <div style={{ height:22, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
    <div style={{ width:100, height:3, borderRadius:0, background:t.text, opacity:0.18 }}/>
  </div>
);

// ── Phone frame ───────────────────────────────────────────────
const Phone = ({ t, children, width=380, height=780, statusTime }) => (
  <div style={{
    width, height, borderRadius:48,
    background:t.bg, color:t.text,
    border:'10px solid #0f0e0c',
    boxShadow:'0 40px 100px rgba(15,14,12,0.28)',
    overflow:'hidden', display:'flex', flexDirection:'column',
    fontFamily:ZAPP_FONT, position:'relative',
  }}>
    <ZStatusBar t={t} time={statusTime}/>
    <div style={{ flex:1, display:'flex', flexDirection:'column', minHeight:0, position:'relative', overflow:'hidden' }}>
      {children}
    </div>
    <ZNavGesture t={t}/>
  </div>
);

// ── Bottom dock — sharp ───────────────────────────────────────
const BottomDock = ({ t, cta, ctaDisabled, onCta, back, onBack, noBorder }) => (
  <div style={{
    padding:'16px 24px 20px', flexShrink:0,
    borderTop: noBorder ? 'none' : `1px solid ${t.text}`,
    background:t.bg,
  }}>
    <div style={{ display:'flex', gap:0 }}>
      {back && (
        <button onClick={onBack} style={{
          width:52, height:52, flexShrink:0,
          border:`1px solid ${t.border}`, borderRight:'none',
          background:'transparent', color:t.text, cursor:'pointer',
          display:'flex', alignItems:'center', justifyContent:'center',
        }}>
          <Icon name="back" size={20}/>
        </button>
      )}
      <button onClick={ctaDisabled ? undefined : onCta} disabled={ctaDisabled} style={{
        flex:1, height:52,
        background: ctaDisabled ? t.surfaceAlt : t.accent,
        color: ctaDisabled ? t.textSubtle : '#fff',
        border: ctaDisabled ? `1px solid ${t.border}` : 'none',
        cursor: ctaDisabled ? 'not-allowed' : 'pointer',
        fontSize:15, fontWeight:800, letterSpacing:0.2,
        fontFamily:ZAPP_FONT,
      }}>{cta}</button>
    </div>
  </div>
);

// ── Bottom nav ────────────────────────────────────────────────
// Sharp tab bar — orange 2px top-border indicator on active
const BottomNav = ({ t, active, onTab, style='pill', side='right' }) => {
  const tabs = [
    { id:'wallet',   icon:'wallet',   label:'Wallet'   },
    { id:'chats',    icon:'chat',     label:'Chats'    },
    { id:'contacts', icon:'contacts', label:'Contacts' },
    { id:'settings', icon:'settings', label:'Settings' },
  ];

  if (style === 'floating') {
    const justify = side==='left' ? 'flex-start' : side==='right' ? 'flex-end' : 'center';
    return (
      <div style={{ position:'absolute', bottom:14, left:14, right:14, display:'flex', justifyContent:justify, zIndex:20, pointerEvents:'none' }}>
        <div style={{ display:'flex', border:`1px solid ${t.border}`, background:t.bg, pointerEvents:'auto', boxShadow:'0 4px 20px rgba(15,14,12,0.12)' }}>
          {tabs.map(tab => {
            const on = active === tab.id;
            return (
              <button key={tab.id} onClick={() => onTab(tab.id)} style={{
                border:'none', borderRight:`1px solid ${t.border}`, cursor:'pointer',
                width:52, height:52,
                display:'flex', alignItems:'center', justifyContent:'center',
                background: on ? t.accent : t.bg,
                color: on ? '#fff' : t.textMuted,
                transition:'background .12s, color .12s',
              }}><Icon name={tab.icon} size={20}/></button>
            );
          })}
        </div>
      </div>
    );
  }

  // Default: sharp tab bar (same for 'pill' and 'bar')
  return (
    <div style={{ display:'flex', borderTop:`1px solid ${t.border}`, background:t.bg, flexShrink:0 }}>
      {tabs.map(tab => {
        const on = active === tab.id;
        return (
          <button key={tab.id} onClick={() => onTab(tab.id)} style={{
            flex:1, border:'none', background:'transparent', cursor:'pointer',
            display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center',
            padding:'10px 4px 12px', gap:4,
            color: on ? t.accent : t.textSubtle,
            borderTop:`2px solid ${on ? t.accent : 'transparent'}`,
            transition:'color .12s',
          }}>
            <Icon name={tab.icon} size={20} stroke={on ? 2.2 : 1.6}/>
            <span style={{ fontSize:9, fontWeight:800, letterSpacing:0.8, textTransform:'uppercase' }}>{tab.label}</span>
          </button>
        );
      })}
    </div>
  );
};

// ── Screen header ─────────────────────────────────────────────
const ScreenHeader = ({ t, title, subtitle, right }) => (
  <div style={{ padding:'22px 28px 14px', display:'flex', alignItems:'flex-end', justifyContent:'space-between', flexShrink:0 }}>
    <div>
      {subtitle && <div style={{ fontSize:10, fontWeight:800, letterSpacing:1.8, color:t.textSubtle, textTransform:'uppercase', marginBottom:5 }}>{subtitle}</div>}
      <div style={{ fontSize:26, fontWeight:900, letterSpacing:-0.8, color:t.text, lineHeight:1 }}>{title}</div>
    </div>
    {right}
  </div>
);

// ── Section label ─────────────────────────────────────────────
const SectionLabel = ({ t, children }) => (
  <div style={{ padding:'16px 28px 6px', fontSize:9, fontWeight:800, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase' }}>
    {children}
  </div>
);

// ── Btn — sharp ───────────────────────────────────────────────
const Btn = ({ t, variant='primary', children, onClick, full, icon, disabled }) => {
  const vs = {
    primary:     { bg:t.accent,      fg:'#fff',       border:'none' },
    ghost:       { bg:'transparent', fg:t.text,       border:`1px solid ${t.border}` },
    danger:      { bg:t.dangerSoft,  fg:t.danger,     border:'none' },
    accentGhost: { bg:'transparent', fg:t.accent,     border:`1px solid ${t.accent}` },
    muted:       { bg:t.surfaceAlt,  fg:t.textMuted,  border:'none' },
  }[variant];
  return (
    <button onClick={disabled ? undefined : onClick} style={{
      width:full ? '100%' : 'auto',
      height:48, borderRadius:0,
      background: disabled ? t.surfaceAlt : vs.bg,
      color: disabled ? t.textSubtle : vs.fg,
      border: disabled ? `1px solid ${t.border}` : vs.border,
      cursor: disabled ? 'not-allowed' : 'pointer',
      fontSize:14, fontWeight:800, letterSpacing:0.1,
      display:'inline-flex', alignItems:'center', justifyContent:'center', gap:8,
      fontFamily:ZAPP_FONT,
    }}>
      {icon && <Icon name={icon} size={16}/>}
      {children}
    </button>
  );
};

// ── FAB — sharp ───────────────────────────────────────────────
const Fab = ({ t, icon, onClick, size=52 }) => (
  <button onClick={onClick} style={{
    width:size, height:size, borderRadius:0,
    background:t.accent, color:'#fff', border:'none', cursor:'pointer',
    display:'flex', alignItems:'center', justifyContent:'center',
    boxShadow:'3px 3px 0 rgba(15,14,12,0.2)',
  }}>
    <Icon name={icon} size={20}/>
  </button>
);

// ── Avatar — keep circle ──────────────────────────────────────
const Avatar = ({ name, size=40, hue=30 }) => {
  const initials = (name||'?').split(/[\s_\-.]+/).filter(Boolean).slice(0,2).map(s => s[0].toUpperCase()).join('');
  return (
    <div style={{
      width:size, height:size, borderRadius:'50%', flexShrink:0,
      background:`oklch(80% 0.12 ${hue})`,
      display:'flex', alignItems:'center', justifyContent:'center',
      fontWeight:900, fontSize:size*0.35, letterSpacing:-0.5,
      color:`oklch(35% 0.10 ${hue})`,
    }}>{initials}</div>
  );
};

// ── Chip — sharp ──────────────────────────────────────────────
const Chip = ({ t, children, color='muted' }) => {
  const p = { muted:[t.surfaceAlt,t.textMuted], success:[t.successSoft,t.success], accent:[t.accentSoft,t.accentText], danger:[t.dangerSoft,t.danger] }[color];
  return (
    <span style={{ display:'inline-flex', alignItems:'center', gap:5, background:p[0], color:p[1], padding:'3px 8px', borderRadius:0, fontSize:11, fontWeight:800, letterSpacing:0.4 }}>
      {children}
    </span>
  );
};

// ── Field — sharp ─────────────────────────────────────────────
const Field = ({ t, label, icon, right, value, onChange, placeholder, type='text', monospace }) => (
  <div>
    {label && <div style={{ fontSize:9, fontWeight:800, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:10 }}>{label}</div>}
    <div style={{ display:'flex', alignItems:'center', gap:10, borderBottom:`1.5px solid ${t.text}`, padding:'10px 0' }}>
      {icon && <div style={{ color:t.accent, display:'flex', flexShrink:0 }}><Icon name={icon} size={17}/></div>}
      <input type={type} value={value} onChange={e => onChange?.(e.target.value)} placeholder={placeholder}
        style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:16, fontWeight:700, color:t.text, fontFamily: monospace ? ZAPP_MONO : ZAPP_FONT }}/>
      {right}
    </div>
  </div>
);

// ── Row ───────────────────────────────────────────────────────
const Row = ({ t, icon, title, sub, right, onClick, iconColor, compact }) => (
  <div onClick={onClick} role={onClick?'button':undefined} tabIndex={onClick?0:undefined} style={{
    display:'flex', alignItems:'center', gap:14,
    padding: compact ? '12px 28px' : '16px 28px',
    cursor: onClick ? 'pointer' : 'default',
    color:t.text, borderBottom:`1px solid ${t.border}`,
  }}>
    {icon && <div style={{ color:iconColor||t.textSubtle, flexShrink:0 }}><Icon name={icon} size={16}/></div>}
    <div style={{ flex:1, minWidth:0 }}>
      <div style={{ fontSize:14, fontWeight:800, letterSpacing:-0.1 }}>{title}</div>
      {sub && <div style={{ fontSize:11, color:t.textMuted, marginTop:1 }}>{sub}</div>}
    </div>
    {right}
  </div>
);

// ── Bottom sheet — sharp ──────────────────────────────────────
const BottomSheet = ({ t, open, onClose, children }) => {
  if (!open) return null;
  return (
    <div style={{ position:'absolute', inset:0, zIndex:50, display:'flex', flexDirection:'column', justifyContent:'flex-end' }}>
      <div onClick={onClose} style={{ position:'absolute', inset:0, background:t.overlay, animation:'fade-in .2s' }}/>
      <div style={{
        position:'relative', background:t.bg,
        borderTop:`2px solid ${t.text}`,
        maxHeight:'88%', overflow:'hidden',
        animation:'slide-up .22s cubic-bezier(.2,.8,.2,1)',
        display:'flex', flexDirection:'column',
      }}>
        {/* drag handle */}
        <div style={{ height:4, width:40, background:t.borderStrong, margin:'14px auto 0' }}/>
        {children}
      </div>
    </div>
  );
};

// ── Toggle ────────────────────────────────────────────────────
const Toggle = ({ t, on, onClick }) => (
  <div onClick={e => {e.stopPropagation();onClick?.();}} role="switch" aria-checked={!!on} tabIndex={0}
    onKeyDown={e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();e.stopPropagation();onClick?.();}}}
    style={{ width:44, height:24, background: on?t.accent:t.borderStrong, cursor:'pointer', position:'relative', transition:'background .2s', flexShrink:0, borderRadius:0 }}>
    <div style={{ position:'absolute', top:2, left:on?22:2, width:20, height:20, background:'#fff', transition:'left .18s', boxShadow:'1px 1px 0 rgba(0,0,0,0.15)' }}/>
  </div>
);

// ── Sep ───────────────────────────────────────────────────────
const Sep = ({ t }) => <div style={{ height:1, background:t.border }}/>;

Object.assign(window, {
  Phone, ZStatusBar, ZNavGesture, BottomNav, ScreenHeader, SectionLabel,
  BottomDock, Btn, Fab, Avatar, Chip, Field, Row, BottomSheet, Toggle, Sep, ZappKeyframes,
});
