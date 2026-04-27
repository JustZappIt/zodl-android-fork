// Zapp — main app composition (phone with tabs + flows)

const ZappApp = ({ t, username = 'zapp_user', navStyle = 'pill', navSide = 'right', dark, onDark }) => {
  const [tab, setTab] = React.useState('wallet');
  const [thread, setThread] = React.useState(null);
  const [attachOpen, setAttachOpen] = React.useState(false);
  const [addContact, setAddContact] = React.useState(false);
  const [editContact, setEditContact] = React.useState(null);
  const [send, setSend] = React.useState(null); // { contact? }
  const [receive, setReceive] = React.useState(false);
  const [txDetail, setTxDetail] = React.useState(null);
  const [toast, setToast] = React.useState(null);

  const isFloating = navStyle === 'floating';
  const floatingOffset = isFloating ? 86 : 16;

  // If a modal-ish flow is open (chat thread, send, receive) hide bottom nav
  const modalScreen = thread || send || receive;

  const pushToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(null), 2000);
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', position: 'relative', minHeight: 0, background: t.bg, color: t.text }}>
      {/* Main tab content */}
      {!modalScreen && (
        <>
          {tab === 'wallet' && (
            <WalletScreen t={t} floatingOffset={floatingOffset}
              onSend={() => setSend({})} onReceive={() => setReceive(true)}
              onScan={() => pushToast('Scanner opened')}
              onTx={(tx) => setTxDetail(tx)}
            />
          )}
          {tab === 'chats' && (
            <ChatsScreen t={t} floatingOffset={floatingOffset} onOpenChat={setThread}/>
          )}
          {tab === 'contacts' && (
            <ContactsScreen t={t} floatingOffset={floatingOffset} onAdd={() => setAddContact(true)} onEdit={setEditContact}/>
          )}
          {tab === 'settings' && (
            <SettingsScreen t={t} username={username} dark={dark} onDark={onDark} floatingOffset={floatingOffset}/>
          )}
        </>
      )}

      {/* Flow screens (push over tabs) */}
      {thread && !send && (
        <ChatThreadScreen t={t} chat={thread} onBack={() => setThread(null)} onPlus={() => setAttachOpen(true)}/>
      )}
      {send && (
        <SendScreen t={t} contact={send.contact} onBack={() => setSend(null)}
          onSent={() => { pushToast('Sent!'); setSend(null); }}
        />
      )}
      {receive && (
        <ReceiveScreen t={t} username={username} onBack={() => setReceive(false)}/>
      )}

      {/* Bottom nav */}
      {!modalScreen && (
        <BottomNav t={t} style={navStyle} side={navSide} active={tab} onTab={setTab}/>
      )}

      {/* FAB on contacts */}
      {!modalScreen && tab === 'contacts' && (
        <div style={{ position: 'absolute', bottom: isFloating ? 82 : 88, [navSide === 'left' ? 'left' : 'right']: 18, zIndex: 30 }}>
          <Fab t={t} icon="user-plus" onClick={() => setAddContact(true)}/>
        </div>
      )}
      {/* FAB on chats */}
      {!modalScreen && tab === 'chats' && (
        <div style={{ position: 'absolute', bottom: isFloating ? 82 : 88, [navSide === 'left' ? 'left' : 'right']: 18, zIndex: 30 }}>
          <Fab t={t} icon="plus"/>
        </div>
      )}
      {/* FAB column on wallet */}
      {!modalScreen && tab === 'wallet' && (
        <div style={{ position: 'absolute', bottom: isFloating ? 82 : 88, [navSide === 'left' ? 'left' : 'right']: 18, zIndex: 30, display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Fab t={t} icon="send" onClick={() => setSend({})}/>
          <Fab t={t} icon="receive" onClick={() => setReceive(true)}/>
          <Fab t={t} icon="scan" onClick={() => pushToast('Scanner opened')}/>
        </div>
      )}

      {/* Sheets */}
      <AttachSheet t={t} open={attachOpen} onClose={() => setAttachOpen(false)}
        onShareAddr={() => { setAttachOpen(false); setReceive(true); }}
        onSend={() => { setAttachOpen(false); setSend({ contact: thread }); }}
      />
      <AddContactSheet t={t} open={addContact} onClose={() => setAddContact(false)}
        onSave={() => { setAddContact(false); pushToast('Contact added'); }}
      />
      <AddContactSheet t={t} open={!!editContact} initial={editContact} onClose={() => setEditContact(null)}
        onSave={() => { setEditContact(null); pushToast('Saved'); }}
      />
      <TxDetailSheet t={t} open={!!txDetail} onClose={() => setTxDetail(null)} tx={txDetail}/>

      {/* Toast */}
      {toast && (
        <div style={{
          position: 'absolute', left: '50%', bottom: 120, transform: 'translateX(-50%)',
          background: t.text, color: t.bg, padding: '9px 18px', borderRadius: 999,
          fontSize: 13, fontWeight: 600, zIndex: 100, animation: 'fade-up .2s',
          boxShadow: t.shadow,
        }}>{toast}</div>
      )}
    </div>
  );
};

// ── Onboarding flow wrapper ──────────────────────────────────
// Two clearly separate phases:
//   Phase A — Messaging identity: username + messaging recovery phrase (or restore)
//   Phase B — Wallet: create + wallet recovery phrase (or restore)
//   Phase C — App lock: biometrics or PIN
const TOTAL_STEPS = 6;

const MSG_SEED = ['river','anchor','willow','lantern','silver','harbor','maple','breeze','copper','quartz','canyon','ember'];
const WALLET_SEED = ['forest','orange','mellow','signal','rapid','corner','voyage','flame','beach','candle','prism','quiet'];

const ZappOnboarding = ({ t, onComplete }) => {
  const [step, setStep] = React.useState('welcome');
  const [username, setUsername] = React.useState('');

  switch (step) {
    case 'welcome':
      return <OnbWelcome t={t}
        onNew={() => setStep('msgIntro')}
        onRestore={() => setStep('msgRestore')}/>;

    // ── Phase A — Messaging identity ───────────────────────
    case 'msgIntro':
      return <OnbPhaseIntro t={t} step={1} total={TOTAL_STEPS}
        badge="Part 1 of 3 · Messaging account"
        title="Create your messaging identity"
        sub="Your identity is a username plus a 12‑word phrase that lets you restore your chats on a new device."
        points={[
          { icon: 'user', label: 'Pick a username', sub: 'How friends find and message you' },
          { icon: 'key', label: 'Save a recovery phrase', sub: 'Restore your account if you lose this phone' },
        ]}
        onBack={() => setStep('welcome')}
        onNext={() => setStep('msgUsername')}/>;

    case 'msgUsername':
      return <OnbUsername t={t} username={username} setUsername={setUsername}
        step={2} total={TOTAL_STEPS} badge="Part 1 · Username"
        onBack={() => setStep('msgIntro')}
        onNext={() => setStep('msgSeed')}/>;

    case 'msgSeed':
      return <OnbSeedShow t={t}
        step={3} total={TOTAL_STEPS}
        title="Messaging recovery phrase"
        sub="These 12 words restore your chats. Save them somewhere safe — they're different from your wallet phrase."
        seed={MSG_SEED}
        onBack={() => setStep('msgUsername')}
        onNext={() => setStep('walletIntro')}/>;

    case 'msgRestore':
      return <OnbRestore t={t}
        step={2} total={TOTAL_STEPS}
        title="Restore messaging account"
        sub="Enter your 12‑word messaging phrase to recover your username and chats."
        onBack={() => setStep('welcome')}
        onNext={() => setStep('walletIntro')}/>;

    // ── Phase B — Wallet ───────────────────────────────────
    case 'walletIntro':
      return <OnbPhaseIntro t={t} step={4} total={TOTAL_STEPS}
        badge="Part 2 of 3 · Wallet"
        title="Now set up your wallet"
        sub="Your wallet is separate from your messaging identity. It has its own 12‑word phrase so you can back up funds independently."
        points={[
          { icon: 'sparkle', label: 'Create a new wallet', sub: 'Or restore one you already have' },
          { icon: 'shield', label: 'Save a second recovery phrase', sub: 'Different words — keep both safe' },
        ]}
        onBack={() => setStep('welcome')}
        onNext={() => setStep('walletChoice')}/>;

    case 'walletChoice':
      return <OnbWallet t={t}
        onBack={() => setStep('walletIntro')}
        onNew={() => setStep('walletSeed')}
        onRestore={() => setStep('walletRestore')}/>;

    case 'walletSeed':
      return <OnbSeedShow t={t}
        step={5} total={TOTAL_STEPS}
        title="Wallet recovery phrase"
        sub="These 12 words restore your funds. They're different from your messaging phrase — write them down separately."
        seed={WALLET_SEED}
        onBack={() => setStep('walletChoice')}
        onNext={() => setStep('twoFa')}/>;

    case 'walletRestore':
      return <OnbRestore t={t}
        step={5} total={TOTAL_STEPS}
        title="Restore wallet"
        sub="Enter your 12 or 24‑word wallet recovery phrase."
        onBack={() => setStep('walletChoice')}
        onNext={() => setStep('twoFa')}/>;

    // ── Phase C — App lock ─────────────────────────────────
    case 'twoFa':
      return <Onb2FA t={t}
        step={6} total={TOTAL_STEPS} badge="Part 3 of 3 · Secure Zapp"
        onBack={() => setStep('walletChoice')}
        onNext={() => onComplete(username || 'you')}/>;

    default:
      return null;
  }
};

Object.assign(window, { ZappApp, ZappOnboarding });
