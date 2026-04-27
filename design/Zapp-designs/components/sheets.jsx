// Zapp sheets — sharp, crisp, open. borderRadius: 0 throughout.

const SendScreen = ({ t, onBack, contact, onSent }) => {
  const [amount, setAmount] = React.useState('');
  const [note, setNote] = React.useState('');
  const [currency, setCurrency] = React.useState('USD');
  const num = parseFloat(amount) || 0;
  const zpRate = 2.48;
  const equiv = currency==='USD' ? (num/zpRate).toFixed(4) : (num*zpRate).toFixed(2);

  const onKey = k => {
    if (k==='.'){if(!amount.includes('.')) setAmount(a=>a?a+'.':'0.'); return;}
    if (k==='⌫'){setAmount(a=>a.slice(0,-1)); return;}
    if (amount==='0') setAmount(k); else if (amount.length<9) setAmount(a=>a+k);
  };

  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      {/* recipient */}
      <div style={{ padding:'14px 24px 12px', borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
        <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:8 }}>Send to</div>
        {contact ? (
          <div style={{ display:'flex', alignItems:'center', gap:10 }}>
            <Avatar name={contact.name} size={26} hue={contact.hue||30}/>
            <span style={{ fontSize:15, fontWeight:900, letterSpacing:-0.3 }}>{contact.name}</span>
            <Icon name="check" size={13} style={{ color:t.success }}/>
          </div>
        ) : (
          <div style={{ display:'flex', alignItems:'center', gap:10, borderBottom:`1.5px solid ${t.text}`, paddingBottom:8 }}>
            <input placeholder="Address or @handle" style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:13, fontFamily:ZAPP_MONO, color:t.text }}/>
            <button style={{ border:'none', background:'transparent', color:t.accent, cursor:'pointer', display:'flex' }}><Icon name="scan" size={17}/></button>
          </div>
        )}
      </div>

      {/* amount */}
      <div style={{ flexShrink:0, display:'flex', flexDirection:'column', alignItems:'center', padding:'18px 24px 12px' }}>
        <div style={{ display:'flex', alignItems:'baseline', gap:8 }}>
          <span style={{ fontSize:60, fontWeight:900, letterSpacing:-3.5, lineHeight:1, color:num>0?t.text:t.border }}>
            {currency==='USD'?'$':''}{amount||'0'}
          </span>
          <button onClick={()=>setCurrency(currency==='USD'?'ZP':'USD')} style={{ border:'none', background:'transparent', color:t.accent, cursor:'pointer', fontSize:15, fontWeight:900, display:'inline-flex', alignItems:'center', gap:2 }}>
            {currency}<Icon name="chevron-down" size={12}/>
          </button>
        </div>
        <div style={{ fontSize:11, color:t.textMuted, marginTop:6, display:'flex', alignItems:'center', gap:5 }}>
          <Icon name="shuffle" size={11} style={{ color:t.accent }}/>
          ≈ {currency==='USD'?`${equiv} ZP`:`$${equiv}`}
        </div>
      </div>

      {/* note + fee */}
      <div style={{ padding:'0 24px', flexShrink:0, display:'flex', justifyContent:'space-between', alignItems:'center', gap:16, marginBottom:12 }}>
        <div style={{ flex:1, display:'flex', alignItems:'center', gap:8, borderBottom:`1px solid ${t.border}`, paddingBottom:7 }}>
          <Icon name="edit" size={12} style={{ color:t.textSubtle }}/>
          <input value={note} onChange={e=>setNote(e.target.value)} placeholder="Note (optional)"
            style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:12, color:t.text }}/>
        </div>
        <div style={{ fontSize:10, color:t.textMuted, whiteSpace:'nowrap', fontWeight:800 }}>Fee: $0.02</div>
      </div>

      {/* keypad */}
      <div style={{ flex:1, display:'grid', gridTemplateRows:'repeat(4,1fr)', padding:'0 20px', gap:2 }}>
        {[['1','2','3'],['4','5','6'],['7','8','9'],['.','0','⌫']].map((row, ri) => (
          <div key={ri} style={{ display:'flex', gap:2 }}>
            {row.map(k => (
              <button key={k} onClick={()=>onKey(k)} style={{ flex:1, border:'none', background:t.surfaceAlt, fontSize:20, fontWeight:700, color:t.text, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center', minHeight:0 }}>
                {k==='⌫'?<Icon name="backspace" size={18}/>:k}
              </button>
            ))}
          </div>
        ))}
      </div>

      <BottomDock t={t} cta={`Send${num>0?` ${currency==='USD'?'$'+amount:amount+' ZP'}`:''}`} ctaDisabled={num===0} onCta={onSent} back onBack={onBack} noBorder/>
    </div>
  );
};

// ── Receive ───────────────────────────────────────────────────
const ReceiveScreen = ({ t, onBack, username='you' }) => {
  const [amount, setAmount] = React.useState('');
  return (
    <div style={{ height:'100%', display:'flex', flexDirection:'column', background:t.bg, color:t.text, overflow:'hidden' }}>
      <div style={{ padding:'14px 24px 14px', borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
        <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:5 }}>Receive</div>
        <div style={{ fontSize:24, fontWeight:900, letterSpacing:-0.6 }}>@{username}</div>
      </div>
      <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', padding:'20px 24px', gap:16 }}>
        <FakeQR t={t} size={196}/>
        <div style={{ width:'100%', display:'flex', alignItems:'center', gap:10, background:t.surfaceAlt, padding:'10px 14px' }}>
          <span style={{ flex:1, fontSize:12, fontFamily:ZAPP_MONO, color:t.text, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
            zp1q7s3mxkl8dyrvjpq2aa...3f8e
          </span>
          <button style={{ border:'none', background:'transparent', color:t.accent, cursor:'pointer', display:'flex', padding:4 }}>
            <Icon name="copy" size={14}/>
          </button>
        </div>
        <div style={{ width:'100%' }}>
          <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:8 }}>Request amount (optional)</div>
          <div style={{ display:'flex', alignItems:'baseline', gap:4, borderBottom:`1.5px solid ${t.text}`, paddingBottom:8 }}>
            <span style={{ fontSize:24, fontWeight:900, color:t.textSubtle }}>$</span>
            <input value={amount} onChange={e=>setAmount(e.target.value.replace(/[^0-9.]/g,''))} placeholder="0.00"
              style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:24, fontWeight:900, letterSpacing:-0.5, color:t.text }}/>
          </div>
        </div>
      </div>
      <BottomDock t={t} cta="Share address" onCta={()=>{}} back onBack={onBack}/>
    </div>
  );
};

// ── Fake QR ───────────────────────────────────────────────────
const FakeQR = ({ t, size=200 }) => {
  const n=21;
  const seed=(i,j)=>{const x=Math.sin(i*17.3+j*29.7)*43758.5;return x-Math.floor(x)>0.5;};
  const finder=(i,j,ri,rj)=>{const di=i-ri,dj=j-rj;if(di<0||di>6||dj<0||dj>6)return null;return di===0||di===6||dj===0||dj===6||(di>=2&&di<=4&&dj>=2&&dj<=4);};
  const cells=[];
  for(let i=0;i<n;i++) for(let j=0;j<n;j++){
    const f=finder(i,j,0,0)??finder(i,j,0,n-7)??finder(i,j,n-7,0);
    if(f!==null?f:seed(i,j)) cells.push([i,j]);
  }
  return (
    <div style={{ width:size, height:size, background:'#fff', padding:10, border:`1px solid ${t.border}` }}>
      <svg viewBox={`0 0 ${n} ${n}`} width="100%" height="100%" shapeRendering="crispEdges">
        {cells.map(([i,j])=><rect key={`${i}-${j}`} x={j} y={i} width="1" height="1" fill="#0f0e0c"/>)}
        <rect x={n/2-2.5} y={n/2-2.5} width="5" height="5" fill="#fff"/>
        <rect x={n/2-2} y={n/2-2} width="4" height="4" fill="#ff9417"/>
      </svg>
    </div>
  );
};

// ── Attach sheet ──────────────────────────────────────────────
const AttachSheet = ({ t, open, onClose, onShareAddr, onSend }) => (
  <BottomSheet t={t} open={open} onClose={onClose}>
    <div style={{ padding:'8px 0 20px' }}>
      <div style={{ padding:'4px 24px 12px', fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase' }}>Attach</div>
      {[
        { icon:'qr',        title:'Share my address',  sub:'Let them pay you',      onClick:onShareAddr },
        { icon:'send',      title:'Send funds',         sub:'Pay from your wallet',  onClick:onSend },
        { icon:'image',     title:'Photo',              sub:'From gallery' },
        { icon:'camera',    title:'Camera',             sub:'Take a photo' },
        { icon:'paperclip', title:'File',               sub:'Any format, up to 25 MB' },
      ].map((r, i) => (
        <button key={r.title} onClick={r.onClick} style={{
          width:'100%', textAlign:'left', border:'none', background:'transparent', cursor:'pointer',
          display:'flex', alignItems:'center', gap:14, padding:'12px 24px',
          borderTop:i>0?`1px solid ${t.border}`:'none', color:t.text, fontFamily:ZAPP_FONT,
        }}>
          <div style={{ width:32, height:32, flexShrink:0, background:t.surfaceAlt, color:t.text, display:'flex', alignItems:'center', justifyContent:'center' }}>
            <Icon name={r.icon} size={15}/>
          </div>
          <div style={{ flex:1 }}>
            <div style={{ fontSize:14, fontWeight:900, letterSpacing:-0.2 }}>{r.title}</div>
            {r.sub && <div style={{ fontSize:11, color:t.textMuted, marginTop:1 }}>{r.sub}</div>}
          </div>
          <Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/>
        </button>
      ))}
    </div>
  </BottomSheet>
);

// ── Add / Edit contact ────────────────────────────────────────
const AddContactSheet = ({ t, open, onClose, onSave, initial }) => {
  const [name, setName] = React.useState(initial?.name||'');
  const [key, setKey] = React.useState(initial?.key||'');
  React.useEffect(()=>{setName(initial?.name||'');setKey(initial?.key||'');},[initial,open]);
  const valid = name.trim().length>0 && key.trim().length>0;
  return (
    <BottomSheet t={t} open={open} onClose={onClose}>
      <div style={{ padding:'8px 24px 0' }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:22 }}>
          <div style={{ fontSize:19, fontWeight:900, letterSpacing:-0.5, color:t.text }}>
            {initial?'Edit contact':'Add contact'}
          </div>
          {initial && <Avatar name={initial.name} size={38} hue={initial.hue||30}/>}
        </div>
        <div style={{ display:'flex', flexDirection:'column', gap:20 }}>
          <div>
            <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:8 }}>Name</div>
            <div style={{ borderBottom:`1.5px solid ${name?t.text:t.border}`, paddingBottom:8, transition:'border-color .2s' }}>
              <input value={name} onChange={e=>setName(e.target.value)} placeholder="Display name"
                style={{ width:'100%', border:'none', outline:'none', background:'transparent', fontSize:16, fontWeight:900, color:t.text }}/>
            </div>
          </div>
          <div>
            <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:8 }}>Address or handle</div>
            <div style={{ display:'flex', alignItems:'center', gap:8, borderBottom:`1.5px solid ${key?t.text:t.border}`, paddingBottom:8 }}>
              <input value={key} onChange={e=>setKey(e.target.value)} placeholder="zp1q… or @handle"
                style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:13, fontWeight:700, color:t.text, fontFamily:ZAPP_MONO }}/>
              <button style={{ border:'none', background:'transparent', color:t.accent, cursor:'pointer', padding:4 }}>
                <Icon name="scan" size={16}/>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div style={{ padding:'20px 24px', display:'flex', gap:2 }}>
        {initial && (
          <button onClick={onClose} style={{ width:52, height:52, border:`1px solid ${t.danger}`, background:'transparent', color:t.danger, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
            <Icon name="trash" size={16}/>
          </button>
        )}
        <button onClick={valid?()=>onSave({name,key}):undefined} style={{
          flex:1, height:52, background:valid?t.accent:t.surfaceAlt, color:valid?'#fff':t.textSubtle,
          border:'none', cursor:valid?'pointer':'not-allowed', fontSize:14, fontWeight:900,
          display:'flex', alignItems:'center', justifyContent:'center', gap:8, fontFamily:ZAPP_FONT, letterSpacing:0.2,
        }}>
          <Icon name={initial?'check':'user-plus'} size={16}/>
          {initial?'Save changes':'Add contact'}
        </button>
      </div>
    </BottomSheet>
  );
};

// ── Tx detail ─────────────────────────────────────────────────
const TxDetailSheet = ({ t, open, onClose, tx }) => {
  if (!tx) return null;
  const isIn = tx.type==='in';
  return (
    <BottomSheet t={t} open={open} onClose={onClose}>
      <div style={{ display:'flex', flexDirection:'column' }}>
        <div style={{ padding:'14px 24px 18px', borderBottom:`1px solid ${t.border}`, textAlign:'center' }}>
          <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:5 }}>
            {isIn?'Received':'Sent'}
          </div>
          <div style={{ fontSize:54, fontWeight:900, letterSpacing:-3, lineHeight:1, color:isIn?t.success:t.text }}>
            {isIn?'+':'−'}${tx.amount.toFixed(2)}
          </div>
          <div style={{ fontSize:11, color:t.textMuted, marginTop:5 }}>≈ {(tx.amount/2.48).toFixed(4)} ZP</div>
        </div>
        {[
          {k:isIn?'From':'To', v:tx.label.replace(/^(From|To) /,'')},
          {k:'Note',   v:tx.note},
          {k:'Time',   v:tx.time},
          {k:'Fee',    v:'$0.02'},
          {k:'Hash',   v:'7b2a…91f0', mono:true},
          {k:'Status', v:'Confirmed', color:t.success},
        ].map((r,i)=>(
          <div key={i} style={{ display:'flex', justifyContent:'space-between', alignItems:'center', padding:'11px 24px', borderBottom:`1px solid ${t.border}` }}>
            <span style={{ fontSize:11, color:t.textMuted, fontWeight:800, letterSpacing:0.2 }}>{r.k}</span>
            <span style={{ fontSize:13, fontWeight:900, color:r.color||t.text, fontFamily:r.mono?ZAPP_MONO:ZAPP_FONT }}>{r.v}</span>
          </div>
        ))}
        <div style={{ padding:'14px 24px', display:'flex', gap:2 }}>
          <button style={{ flex:1, height:44, border:`1px solid ${t.border}`, background:'transparent', color:t.text, cursor:'pointer', fontSize:12, fontWeight:900, display:'flex', alignItems:'center', justifyContent:'center', gap:7, fontFamily:ZAPP_FONT, letterSpacing:0.2 }}>
            <Icon name="copy" size={14}/>COPY HASH
          </button>
          <button style={{ flex:1, height:44, border:`1px solid ${t.border}`, background:'transparent', color:t.text, cursor:'pointer', fontSize:12, fontWeight:900, display:'flex', alignItems:'center', justifyContent:'center', gap:7, fontFamily:ZAPP_FONT, letterSpacing:0.2 }}>
            <Icon name="arrow-up-right" size={14}/>EXPLORER
          </button>
        </div>
      </div>
    </BottomSheet>
  );
};

// ── Empty contacts ────────────────────────────────────────────
const ContactsEmpty = ({ t }) => (
  <div style={{ flex:1, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:10, padding:32, color:t.text }}>
    <div style={{ fontSize:56, fontWeight:900, color:t.border, lineHeight:1 }}>—</div>
    <div style={{ fontSize:18, fontWeight:900, letterSpacing:-0.4 }}>No contacts</div>
    <div style={{ fontSize:13, color:t.textMuted }}>Add someone to get started</div>
  </div>
);

Object.assign(window, {
  SendScreen, ReceiveScreen, FakeQR, AttachSheet, AddContactSheet, TxDetailSheet, ContactsEmpty,
});
