// Zapp main screens — sharp, crisp, open
// borderRadius: 0 everywhere. Generous padding. Crisp rules.

const Sparkline = ({ t, data, height=54 }) => {
  const W=320, H=height, min=Math.min(...data), max=Math.max(...data), range=max-min||1;
  const pts = data.map((v,i) => [i*W/(data.length-1), H-((v-min)/range)*(H-6)-3]);
  const path = pts.map((p,i)=>(i?'L':'M')+p.join(' ')).join(' ');
  const area = `${path} L${W} ${H} L0 ${H}Z`;
  return (
    <svg viewBox={`0 0 ${W} ${H}`} width="100%" height={H} preserveAspectRatio="none" style={{ display:'block' }}>
      <defs>
        <linearGradient id="sg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={t.accent} stopOpacity=".1"/>
          <stop offset="100%" stopColor={t.accent} stopOpacity="0"/>
        </linearGradient>
      </defs>
      <path d={area} fill="url(#sg)"/>
      <path d={path} fill="none" stroke={t.accent} strokeWidth="1.5" strokeLinejoin="round"/>
      <circle cx={pts[pts.length-1][0]} cy={pts[pts.length-1][1]} r="3" fill={t.accent}/>
    </svg>
  );
};

const MOCK_CHART = [212,218,215,225,230,228,240,252,248,260,272,268,280,285,278,290,305,300,310,318,325,320,332,342,338,350,360,355,368,372];
const MOCK_TX = [
  { id:1, type:'in',  label:'noor.zapp', note:'lunch split',    amount:24.50,   time:'Today 14:22' },
  { id:2, type:'out', label:'coffee_co', note:'morning latte',  amount:4.80,    time:'Today 08:14' },
  { id:3, type:'in',  label:'big_boi',   note:'rent sep',       amount:620.00,  time:'Apr 14' },
  { id:4, type:'out', label:'mav_ph',    note:'concert ticket', amount:88.00,   time:'Apr 12' },
];
const MOCK_CHATS = [
  { id:'c1', name:'Zapp Line',   hue:30, last:'Report issues, share feedback', time:'now',    unread:0, online:true },
  { id:'c2', name:'Noor Ahmadi', hue:15, last:'split that lunch?',             time:'14:22',  unread:2, online:true },
  { id:'c3', name:'big_boi',     hue:40, last:'[Photo] looks 🔥',              time:'12:10',  unread:0 },
  { id:'c4', name:'mav_ph',      hue:20, last:'↗ sent you 0.04 ZP',            time:'Mon',    unread:0 },
  { id:'c5', name:'coffee_co',   hue:45, last:'receipt attached',              time:'Apr 14', unread:0 },
  { id:'c6', name:'jamal.42',    hue:55, last:'ok see you sat',                time:'Apr 12', unread:0 },
  { id:'c7', name:'fe9eac1a',    hue:25, last:'waiting for peer…',             time:'Apr 9',  unread:0 },
];
const MOCK_CONTACTS = [
  { id:1, name:'Noor Ahmadi',  handle:'@noor.zapp',  hue:15, key:'zp1q7s...3f8e' },
  { id:2, name:'big_boi',      handle:'@big_boi',    hue:40, key:'zp1qxy...ab12' },
  { id:3, name:'mav_ph',       handle:'@mav_ph',     hue:20, key:'zp1qma...7c4d' },
  { id:4, name:'Coffee Co',    handle:'@coffee_co',  hue:45, key:'zp1qcf...9102' },
  { id:5, name:'Jamal Rivers', handle:'@jamal.42',   hue:55, key:'zp1qjm...ef88' },
  { id:6, name:'Home Rent',    handle:'@home_rent',  hue:30, key:'zp1qhm...0055' },
];
const MOCK_THREAD = [
  { id:1, from:'them', text:'yo are we still on for friday',     time:'3:20 PM' },
  { id:2, from:'me',   text:'yeah — 8pm at the usual',           time:'3:22 PM' },
  { id:3, from:'them', text:'cool. can u send me the ticket $?', time:'3:24 PM' },
  { id:4, from:'me',   kind:'send', amount:24.00, text:'gotchu', time:'3:25 PM' },
  { id:5, from:'them', text:'🙏 legend',                         time:'3:26 PM' },
  { id:6, from:'them', kind:'photo',                             time:'3:30 PM' },
  { id:7, from:'me',   text:'looks sick. see u then',            time:'3:32 PM' },
];

// ── Wallet ────────────────────────────────────────────────────
const WalletScreen = ({ t, onSend, onReceive, onScan, onTx, floatingOffset }) => {
  const [range, setRange] = React.useState('1M');
  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>

      {/* header */}
      <div style={{ padding:'18px 28px 10px', display:'flex', alignItems:'center', justifyContent:'space-between', flexShrink:0 }}>
        <div style={{ fontSize:22, fontWeight:900, letterSpacing:-0.6 }}>Wallet</div>
        <div style={{ display:'flex', alignItems:'center', gap:7, fontSize:11, color:t.textMuted, fontWeight:800 }}>
          <div style={{ width:5, height:5, background:t.success }}/>
          Synced
        </div>
      </div>

      {/* balance */}
      <div style={{ padding:'0 28px 12px', flexShrink:0 }}>
        <div style={{ fontSize:10, fontWeight:800, letterSpacing:1.8, color:t.textSubtle, textTransform:'uppercase', marginBottom:5 }}>Total balance</div>
        <div style={{ display:'flex', alignItems:'baseline', gap:2 }}>
          <span style={{ fontSize:52, fontWeight:900, letterSpacing:-3, lineHeight:1, color:t.text }}>$1,284</span>
          <span style={{ fontSize:26, fontWeight:700, letterSpacing:-1, color:t.textMuted }}>.52</span>
        </div>
        <div style={{ fontSize:12, fontWeight:800, color:t.success, marginTop:3, display:'flex', gap:8 }}>
          ▲ +3.4%
          <span style={{ color:t.textMuted, fontWeight:600 }}>+$42.18 this month</span>
        </div>
      </div>

      {/* chart */}
      <div style={{ flexShrink:0 }}>
        <Sparkline t={t} data={MOCK_CHART} height={54}/>
      </div>

      {/* range */}
      <div style={{ display:'flex', borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
        {['1D','1W','1M','3M','1Y','ALL'].map(r => (
          <button key={r} onClick={() => setRange(r)} style={{
            flex:1, border:'none', background:'transparent', cursor:'pointer', padding:'8px 0',
            fontSize:10, fontWeight:900, letterSpacing:0.8,
            color:range===r?t.accent:t.textSubtle,
            borderBottom:`2px solid ${range===r?t.accent:'transparent'}`,
            marginBottom:-1,
          }}>{r}</button>
        ))}
      </div>

      {/* quick actions */}
      <div style={{ display:'flex', borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
        {[
          { icon:'send',    label:'Send',    onClick:onSend    },
          { icon:'receive', label:'Receive', onClick:onReceive },
          { icon:'scan',    label:'Scan',    onClick:onScan    },
        ].map((a, i) => (
          <button key={a.label} onClick={a.onClick} style={{
            flex:1, border:'none', borderRight:i<2?`1px solid ${t.border}`:'none',
            background:t.bg, padding:'14px 8px', cursor:'pointer', color:t.text,
            display:'flex', flexDirection:'column', alignItems:'center', gap:5,
          }}>
            <Icon name={a.icon} size={18} style={{ color:t.accent }}/>
            <span style={{ fontSize:10, fontWeight:900, letterSpacing:0.8, textTransform:'uppercase' }}>{a.label}</span>
          </button>
        ))}
      </div>

      {/* activity header */}
      <div style={{ padding:'10px 28px 6px', display:'flex', justifyContent:'space-between', alignItems:'center', flexShrink:0 }}>
        <div style={{ fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase' }}>Activity</div>
        <button style={{ border:'none', background:'transparent', color:t.accent, fontSize:11, fontWeight:900, cursor:'pointer', letterSpacing:0.4 }}>All ›</button>
      </div>

      {/* tx rows — flex:1 */}
      <div style={{ flex:1, display:'flex', flexDirection:'column' }}>
        {MOCK_TX.map((tx, i) => (
          <button key={tx.id} onClick={() => onTx?.(tx)} style={{
            flex:1, border:'none', background:'transparent', cursor:'pointer',
            display:'flex', alignItems:'center', gap:14, padding:'0 28px',
            borderTop:`1px solid ${t.border}`, color:t.text, textAlign:'left', fontFamily:ZAPP_FONT, minHeight:0,
          }}>
            <div style={{ width:5, height:5, flexShrink:0, background:tx.type==='in'?t.success:t.textSubtle }}/>
            <div style={{ flex:1, minWidth:0 }}>
              <div style={{ fontSize:14, fontWeight:900, letterSpacing:-0.3, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{tx.label}</div>
              <div style={{ fontSize:10, color:t.textMuted, marginTop:1, letterSpacing:0.1 }}>{tx.time} · {tx.note}</div>
            </div>
            <div style={{ fontSize:15, fontWeight:900, letterSpacing:-0.5, color:tx.type==='in'?t.success:t.text, flexShrink:0 }}>
              {tx.type==='in'?'+':'−'}${tx.amount.toFixed(2)}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};

// ── Chats ─────────────────────────────────────────────────────
const ChatsScreen = ({ t, onOpenChat, floatingOffset }) => (
  <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>
    <div style={{ padding:'18px 28px 14px', display:'flex', alignItems:'center', justifyContent:'space-between', flexShrink:0, borderBottom:`1px solid ${t.border}` }}>
      <div style={{ fontSize:22, fontWeight:900, letterSpacing:-0.6 }}>Chats</div>
      <div style={{ display:'flex', alignItems:'center', gap:6 }}>
        <div style={{ width:5, height:5, background:t.success }}/>
        <span style={{ fontSize:10, fontWeight:900, color:t.textMuted, letterSpacing:0.6, textTransform:'uppercase' }}>Online</span>
      </div>
    </div>
    <div style={{ flex:1, display:'flex', flexDirection:'column' }}>
      {MOCK_CHATS.map((c, i) => (
        <button key={c.id} onClick={() => onOpenChat(c)} style={{
          flex:1, border:'none', background:'transparent', cursor:'pointer',
          display:'flex', alignItems:'center', gap:14, padding:'0 24px',
          borderBottom:i<MOCK_CHATS.length-1?`1px solid ${t.border}`:'none',
          color:t.text, fontFamily:ZAPP_FONT, textAlign:'left', minHeight:0,
        }}>
          <div style={{ position:'relative', flexShrink:0 }}>
            <Avatar name={c.name} size={36} hue={c.hue}/>
            {c.online && <div style={{ position:'absolute', bottom:0, right:0, width:8, height:8, background:t.success, border:`2px solid ${t.bg}` }}/>}
          </div>
          <div style={{ flex:1, minWidth:0 }}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'baseline', gap:8 }}>
              <span style={{ fontSize:13, fontWeight:900, letterSpacing:-0.3, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{c.name}</span>
              <span style={{ fontSize:10, color:t.textSubtle, fontWeight:700, flexShrink:0, letterSpacing:0.2 }}>{c.time}</span>
            </div>
            <div style={{ fontSize:11, color:t.textMuted, marginTop:1, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis' }}>{c.last}</div>
          </div>
          {c.unread>0 && (
            <div style={{ background:t.accent, color:'#fff', fontSize:10, fontWeight:900, minWidth:18, height:18, padding:'0 5px', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, letterSpacing:0 }}>
              {c.unread}
            </div>
          )}
        </button>
      ))}
    </div>
  </div>
);

// ── Chat thread ───────────────────────────────────────────────
const ChatThreadScreen = ({ t, chat, onBack, onPlus }) => {
  const [text, setText] = React.useState('');
  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', background:t.bg, overflow:'hidden' }}>
      {/* header */}
      <div style={{ display:'flex', alignItems:'center', gap:10, padding:'10px 18px', borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
        <Avatar name={chat.name} size={30} hue={chat.hue}/>
        <div style={{ flex:1, minWidth:0 }}>
          <div style={{ fontSize:13, fontWeight:900, letterSpacing:-0.2 }}>{chat.name}</div>
          <div style={{ fontSize:10, fontWeight:800, color:chat.online?t.success:t.textMuted, textTransform:'uppercase', letterSpacing:0.5 }}>{chat.online?'Online':'Away'}</div>
        </div>
        <button style={{ border:'none', background:'transparent', color:t.textMuted, padding:6, cursor:'pointer' }}>
          <Icon name="dots" size={18}/>
        </button>
      </div>

      {/* messages */}
      <div style={{ flex:1, overflow:'auto', padding:'14px 18px', display:'flex', flexDirection:'column', gap:4 }}>
        <div style={{ textAlign:'center', fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', marginBottom:10 }}>Today</div>
        {MOCK_THREAD.map((m, i) => {
          const mine = m.from === 'me';
          const showTime = i===MOCK_THREAD.length-1 || MOCK_THREAD[i+1]?.from!==m.from;
          return (
            <div key={m.id} style={{ display:'flex', flexDirection:'column', alignItems:mine?'flex-end':'flex-start', maxWidth:'80%', alignSelf:mine?'flex-end':'flex-start' }}>
              {m.kind==='photo' ? (
                <div style={{ width:180, height:130, background:t.surfaceAlt, border:`1px solid ${t.border}`, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:6 }}>
                  <Icon name="image" size={22} style={{ color:t.textMuted }}/>
                  <span style={{ fontSize:9, fontFamily:ZAPP_MONO, color:t.textSubtle }}>IMG_3402.JPG</span>
                </div>
              ) : m.kind==='send' ? (
                <div style={{ background:t.accent, color:'#fff', padding:'12px 14px', minWidth:150 }}>
                  <div style={{ fontSize:9, fontWeight:900, letterSpacing:1.5, opacity:0.7, textTransform:'uppercase' }}>Sent</div>
                  <div style={{ fontSize:24, fontWeight:900, letterSpacing:-1, lineHeight:1.1, marginTop:2 }}>${m.amount.toFixed(2)}</div>
                  <div style={{ fontSize:12, opacity:0.85, marginTop:3 }}>{m.text}</div>
                </div>
              ) : (
                <div style={{ background:mine?t.text:t.surfaceAlt, color:mine?t.bg:t.text, padding:'8px 12px', fontSize:14, lineHeight:1.4 }}>
                  {m.text}
                </div>
              )}
              {showTime && <div style={{ fontSize:9, color:t.textSubtle, margin:'2px 2px 4px', display:'flex', gap:4 }}>{m.time}{mine&&<Icon name="check" size={10}/>}</div>}
            </div>
          );
        })}
      </div>

      {/* composer */}
      <div style={{ padding:'10px 16px 12px', borderTop:`1px solid ${t.border}`, display:'flex', alignItems:'center', gap:8, flexShrink:0 }}>
        <button onClick={onPlus} style={{ width:36, height:36, border:`1px solid ${t.border}`, background:'transparent', color:t.accent, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
          <Icon name="plus" size={18}/>
        </button>
        <div style={{ flex:1, display:'flex', alignItems:'center', background:t.surfaceAlt, padding:'8px 12px' }}>
          <input value={text} onChange={e=>setText(e.target.value)} placeholder="Message"
            style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:14, color:t.text }}/>
        </div>
        <button style={{ width:36, height:36, border:'none', background:text?t.accent:t.surfaceAlt, cursor:'pointer', color:text?'#fff':t.textMuted, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, transition:'background .12s' }}>
          <Icon name="send" size={16}/>
        </button>
      </div>
    </div>
  );
};

// ── Contacts ──────────────────────────────────────────────────
const ContactsScreen = ({ t, onAdd, onEdit, floatingOffset }) => {
  const [q, setQ] = React.useState('');
  const filtered = MOCK_CONTACTS.filter(c => c.name.toLowerCase().includes(q.toLowerCase())||c.handle.includes(q));
  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>
      <div style={{ padding:'18px 28px 14px', flexShrink:0, borderBottom:`1px solid ${t.border}` }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:14 }}>
          <div style={{ fontSize:22, fontWeight:900, letterSpacing:-0.6 }}>Contacts</div>
          <span style={{ fontSize:10, fontWeight:900, color:t.textMuted, letterSpacing:0.6 }}>{MOCK_CONTACTS.length} SAVED</span>
        </div>
        <div style={{ display:'flex', alignItems:'center', gap:8, borderBottom:`1.5px solid ${t.text}`, paddingBottom:8 }}>
          <Icon name="search" size={14} style={{ color:t.textSubtle }}/>
          <input value={q} onChange={e=>setQ(e.target.value)} placeholder="Search contacts"
            style={{ flex:1, border:'none', outline:'none', background:'transparent', fontSize:14, fontWeight:800, color:t.text }}/>
        </div>
      </div>
      <div style={{ flex:1, display:'flex', flexDirection:'column' }}>
        {filtered.map((c, i) => (
          <button key={c.id} onClick={() => onEdit(c)} style={{
            flex:1, border:'none', background:'transparent', cursor:'pointer',
            display:'flex', alignItems:'center', gap:14, padding:'0 28px',
            borderBottom:i<filtered.length-1?`1px solid ${t.border}`:'none',
            color:t.text, fontFamily:ZAPP_FONT, textAlign:'left', minHeight:0,
          }}>
            <Avatar name={c.name} size={32} hue={c.hue}/>
            <div style={{ flex:1, minWidth:0 }}>
              <div style={{ fontSize:13, fontWeight:900, letterSpacing:-0.2 }}>{c.name}</div>
              <div style={{ fontSize:11, color:t.textMuted, fontFamily:ZAPP_MONO, marginTop:2 }}>{c.key}</div>
            </div>
            <Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/>
          </button>
        ))}
      </div>
    </div>
  );
};

// ── Settings ──────────────────────────────────────────────────
const SettingsScreen = ({ t, username, onDark, dark, floatingOffset }) => (
  <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>

    {/* profile */}
    <div style={{ padding:'16px 28px 16px', display:'flex', alignItems:'center', gap:14, borderBottom:`1px solid ${t.border}`, flexShrink:0 }}>
      <Avatar name={username} size={44} hue={30}/>
      <div style={{ flex:1, minWidth:0 }}>
        <div style={{ fontSize:17, fontWeight:900, letterSpacing:-0.4 }}>@{username}</div>
        <div style={{ display:'flex', alignItems:'center', gap:6, marginTop:3 }}>
          <span style={{ fontSize:10, fontFamily:ZAPP_MONO, color:t.textMuted }}>zp1q7s...3f8e</span>
          <Icon name="copy" size={10} style={{ color:t.accent }}/>
        </div>
      </div>
      <button style={{ border:`1px solid ${t.border}`, background:'transparent', padding:'7px 14px', color:t.text, cursor:'pointer', fontSize:12, fontWeight:900, fontFamily:ZAPP_FONT, letterSpacing:0.3 }}>EDIT</button>
    </div>

    {/* settings groups */}
    <div style={{ flex:1, display:'flex', flexDirection:'column' }}>
      {[
        { label:'Security', rows:[
          { icon:'fingerprint', title:'Biometric unlock', sub:'Enabled',       right:<Toggle t={t} on/> },
          { icon:'key',         title:'Change PIN',                             right:<Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/> },
        ]},
        { label:'Wallet', rows:[
          { icon:'shield', title:'Backup phrase',  sub:'12-word recovery', right:<Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/> },
          { icon:'cloud',  title:'Server',         sub:'zapp.relay.one',  right:<Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/> },
        ]},
        { label:'App', rows:[
          { icon:'sparkle', title:'Dark mode',    sub:dark?'On':'Off',  right:<Toggle t={t} on={dark} onClick={onDark}/> },
          { icon:'bell',    title:'Notifications',                       right:<Toggle t={t} on/> },
          { icon:'globe',   title:'Language',     sub:'English (US)',   right:<Icon name="chevron-right" size={13} style={{ color:t.textSubtle }}/> },
        ]},
      ].map(g => (
        <div key={g.label} style={{ flex:1, display:'flex', flexDirection:'column', borderBottom:`1px solid ${t.border}` }}>
          <div style={{ padding:'8px 28px 4px', fontSize:9, fontWeight:900, letterSpacing:2, color:t.textSubtle, textTransform:'uppercase', flexShrink:0 }}>{g.label}</div>
          {g.rows.map((r, i) => (
            <div key={i} style={{ flex:1, display:'flex', alignItems:'center', gap:12, padding:'0 28px', borderTop:`1px solid ${t.border}`, minHeight:0 }}>
              <Icon name={r.icon} size={14} style={{ color:t.textSubtle, flexShrink:0 }}/>
              <div style={{ flex:1, minWidth:0 }}>
                <div style={{ fontSize:13, fontWeight:800, letterSpacing:-0.1 }}>{r.title}</div>
                {r.sub && <div style={{ fontSize:10, color:t.textMuted, marginTop:1 }}>{r.sub}</div>}
              </div>
              {r.right}
            </div>
          ))}
        </div>
      ))}

      {/* sign out */}
      <div style={{ flexShrink:0, padding:'14px 28px 16px' }}>
        <button style={{ width:'100%', height:44, background:t.dangerSoft, color:t.danger, border:'none', cursor:'pointer', fontSize:13, fontWeight:900, display:'flex', alignItems:'center', justifyContent:'center', gap:8, fontFamily:ZAPP_FONT, letterSpacing:0.3 }}>
          <Icon name="logout" size={15}/>SIGN OUT
        </button>
      </div>
    </div>
  </div>
);

Object.assign(window, {
  WalletScreen, ChatsScreen, ChatThreadScreen, ContactsScreen, SettingsScreen,
  Sparkline, MOCK_CHATS, MOCK_CONTACTS, MOCK_TX, MOCK_THREAD, MOCK_CHART,
});
