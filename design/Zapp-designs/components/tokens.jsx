// Zapp design tokens — light + dark
// Light/minimal with orange accent. Mirrors the reference style without copying it.

const ZAPP_LIGHT = {
  bg: '#ffffff',
  surface: '#ffffff',
  surfaceAlt: '#f4f2ee',        // soft warm gray
  surfaceInput: '#f6f4f0',
  border: '#ebe7e0',
  borderStrong: '#d9d4ca',
  text: '#15120d',
  textMuted: '#6b645a',
  textSubtle: '#9a9288',
  accent: '#ff9417',            // brand orange
  accentSoft: '#ffe7cc',
  accentText: '#a65500',
  success: '#2f9d6a',
  successSoft: '#d7f0e3',
  danger: '#d94545',
  dangerSoft: '#fde2e0',
  chipBg: '#efece5',
  overlay: 'rgba(20,18,14,0.45)',
  navPill: '#ecebe5',
  shadow: '0 6px 24px rgba(20,18,14,0.08)',
  bubbleMe: '#ff9417',
  bubbleMeText: '#ffffff',
  bubbleThem: '#f1eee7',
  bubbleThemText: '#15120d',
};

const ZAPP_DARK = {
  bg: '#0f0e0c',
  surface: '#171512',
  surfaceAlt: '#1b1916',
  surfaceInput: '#201d19',
  border: '#2a2622',
  borderStrong: '#3a342d',
  text: '#f6f2ea',
  textMuted: '#a59c90',
  textSubtle: '#726a60',
  accent: '#ff9417',
  accentSoft: '#3a2713',
  accentText: '#ffb26b',
  success: '#5fd49c',
  successSoft: '#1a2e24',
  danger: '#ef6a5f',
  dangerSoft: '#2e1a18',
  chipBg: '#1f1c18',
  overlay: 'rgba(0,0,0,0.55)',
  navPill: '#1c1a16',
  shadow: '0 10px 30px rgba(0,0,0,0.5)',
  bubbleMe: '#ff9417',
  bubbleMeText: '#1a140b',
  bubbleThem: '#1f1c18',
  bubbleThemText: '#f6f2ea',
};

const ZAPP_FONT = `'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif`;
const ZAPP_MONO = `'JetBrains Mono', ui-monospace, 'SF Mono', Menlo, monospace`;

// ── Icons (stroke, 24px, currentColor) ──────────────────────────────
const Icon = ({ name, size = 22, stroke = 1.8, style }) => {
  const common = {
    width: size, height: size, viewBox: '0 0 24 24',
    fill: 'none', stroke: 'currentColor',
    strokeWidth: stroke, strokeLinecap: 'round', strokeLinejoin: 'round',
    style,
  };
  switch (name) {
    case 'wallet':
      return <svg {...common}><path d="M3 7.5A2.5 2.5 0 0 1 5.5 5h13A2.5 2.5 0 0 1 21 7.5V8"/><path d="M3 8v10.5A2.5 2.5 0 0 0 5.5 21h13a2.5 2.5 0 0 0 2.5-2.5V12H6a3 3 0 0 1-3-3"/><circle cx="16.5" cy="15" r="1" fill="currentColor" stroke="none"/></svg>;
    case 'chat':
      return <svg {...common}><path d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v8A2.5 2.5 0 0 1 17.5 17H12l-4 3.5V17H6.5A2.5 2.5 0 0 1 4 14.5z"/></svg>;
    case 'portfolio':
      return <svg {...common}><rect x="3" y="6" width="18" height="14" rx="2"/><path d="M8 6V4.5A1.5 1.5 0 0 1 9.5 3h5A1.5 1.5 0 0 1 16 4.5V6"/><path d="M3 12h18"/></svg>;
    case 'contacts':
      return <svg {...common}><rect x="4" y="3.5" width="14" height="17" rx="2"/><circle cx="11" cy="10" r="2.3"/><path d="M7.5 16.5c.6-1.8 2-2.8 3.5-2.8s2.9 1 3.5 2.8"/><path d="M20 7v10"/></svg>;
    case 'settings':
      return <svg {...common}><circle cx="12" cy="12" r="3"/><path d="M19.4 13.7a1.5 1.5 0 0 0 .3 1.6l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.5 1.5 0 0 0-1.6-.3 1.5 1.5 0 0 0-.9 1.4V19a2 2 0 1 1-4 0v-.1a1.5 1.5 0 0 0-1-1.4 1.5 1.5 0 0 0-1.6.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.5 1.5 0 0 0 .3-1.6 1.5 1.5 0 0 0-1.4-.9H5a2 2 0 1 1 0-4h.1a1.5 1.5 0 0 0 1.4-1 1.5 1.5 0 0 0-.3-1.6l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.5 1.5 0 0 0 1.6.3H11a1.5 1.5 0 0 0 .9-1.4V5a2 2 0 1 1 4 0v.1a1.5 1.5 0 0 0 .9 1.4 1.5 1.5 0 0 0 1.6-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.5 1.5 0 0 0-.3 1.6V11a1.5 1.5 0 0 0 1.4.9H21a2 2 0 1 1 0 4h-.1a1.5 1.5 0 0 0-1.4.9z"/></svg>;
    case 'send':
      return <svg {...common}><path d="M4 12l16-8-6 16-2-6-6-2z"/></svg>;
    case 'receive':
      return <svg {...common}><path d="M12 4v14"/><path d="M5 13l7 7 7-7"/></svg>;
    case 'scan':
      return <svg {...common}><path d="M4 8V5.5A1.5 1.5 0 0 1 5.5 4H8"/><path d="M16 4h2.5A1.5 1.5 0 0 1 20 5.5V8"/><path d="M20 16v2.5a1.5 1.5 0 0 1-1.5 1.5H16"/><path d="M8 20H5.5A1.5 1.5 0 0 1 4 18.5V16"/><path d="M8 8h3v3H8zM13 8h3v3h-3zM8 13h3v3H8zM13 13h3v3h-3z" strokeWidth="1.4"/></svg>;
    case 'plus':
      return <svg {...common}><path d="M12 5v14M5 12h14"/></svg>;
    case 'chevron-right':
      return <svg {...common}><path d="M9 6l6 6-6 6"/></svg>;
    case 'chevron-left':
    case 'back':
      return <svg {...common}><path d="M15 6l-6 6 6 6"/></svg>;
    case 'chevron-down':
      return <svg {...common}><path d="M6 9l6 6 6-6"/></svg>;
    case 'copy':
      return <svg {...common}><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/></svg>;
    case 'paperclip':
      return <svg {...common}><path d="M21 11.5l-8.5 8.5a5 5 0 0 1-7-7l9-9a3.5 3.5 0 0 1 5 5L11 18a2 2 0 0 1-3-3l7.5-7.5"/></svg>;
    case 'image':
      return <svg {...common}><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="10" r="1.6"/><path d="M4 18l5-5 4 4 3-3 4 4"/></svg>;
    case 'camera':
      return <svg {...common}><path d="M4 8.5A1.5 1.5 0 0 1 5.5 7h2l1.5-2h6L16.5 7h2A1.5 1.5 0 0 1 20 8.5v9A1.5 1.5 0 0 1 18.5 19h-13A1.5 1.5 0 0 1 4 17.5z"/><circle cx="12" cy="13" r="3.5"/></svg>;
    case 'fingerprint':
      return <svg {...common}><path d="M12 4a8 8 0 0 0-8 8"/><path d="M6.5 19.5a10 10 0 0 1-.8-3"/><path d="M7 9a6 6 0 0 1 10 4c0 1.6-.2 3.2-.6 4.7"/><path d="M10 7.5a4 4 0 0 1 5 3.8c0 3 .2 6-1 8.5"/><path d="M12 11v2c0 2.5-.3 5-1.2 7"/><path d="M20 13c0 1.6-.2 3.2-.5 4.8"/></svg>;
    case 'face':
      return <svg {...common}><rect x="3.5" y="3.5" width="17" height="17" rx="4"/><circle cx="9" cy="10" r="1" fill="currentColor"/><circle cx="15" cy="10" r="1" fill="currentColor"/><path d="M9 15c.8.8 1.8 1.2 3 1.2s2.2-.4 3-1.2"/></svg>;
    case 'key':
      return <svg {...common}><circle cx="8" cy="14" r="4"/><path d="M11 12l8-8"/><path d="M16 7l3 3"/><path d="M14 9l2 2"/></svg>;
    case 'shield':
      return <svg {...common}><path d="M12 3l8 3v6c0 4.5-3.3 8.3-8 9-4.7-.7-8-4.5-8-9V6z"/><path d="M9 12l2 2 4-4"/></svg>;
    case 'eye':
      return <svg {...common}><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg>;
    case 'eye-off':
      return <svg {...common}><path d="M3 3l18 18"/><path d="M10.6 6.2A10.4 10.4 0 0 1 12 6c6.5 0 10 6 10 6a16.6 16.6 0 0 1-3.5 4.2"/><path d="M6.2 7.2A16.6 16.6 0 0 0 2 12s3.5 6 10 6a10 10 0 0 0 3.4-.6"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/></svg>;
    case 'qr':
      return <svg {...common}><rect x="3.5" y="3.5" width="7" height="7"/><rect x="13.5" y="3.5" width="7" height="7"/><rect x="3.5" y="13.5" width="7" height="7"/><rect x="16" y="16" width="4.5" height="4.5"/><path d="M13.5 13.5h3M13.5 19.5h2"/></svg>;
    case 'check':
      return <svg {...common}><path d="M5 12l5 5 10-11"/></svg>;
    case 'x':
      return <svg {...common}><path d="M6 6l12 12M18 6L6 18"/></svg>;
    case 'trash':
      return <svg {...common}><path d="M4 7h16M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/><path d="M6 7l1 13a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-13"/></svg>;
    case 'edit':
      return <svg {...common}><path d="M4 20h4L20 8l-4-4L4 16z"/><path d="M14 6l4 4"/></svg>;
    case 'cloud':
      return <svg {...common}><path d="M7 17a4 4 0 1 1 .9-7.9A6 6 0 0 1 19 12a3.5 3.5 0 0 1-.5 7z"/></svg>;
    case 'bell':
      return <svg {...common}><path d="M6 17l-1 2h14l-1-2V11a6 6 0 0 0-12 0z"/><path d="M10 21a2 2 0 0 0 4 0"/></svg>;
    case 'globe':
      return <svg {...common}><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c3 3.5 3 14 0 18M12 3c-3 3.5-3 14 0 18"/></svg>;
    case 'help':
      return <svg {...common}><circle cx="12" cy="12" r="9"/><path d="M9.5 9.5a2.5 2.5 0 0 1 5 .2c0 1.8-2.5 2.2-2.5 4"/><circle cx="12" cy="17" r="0.6" fill="currentColor"/></svg>;
    case 'logout':
      return <svg {...common}><path d="M10 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h5"/><path d="M14 8l4 4-4 4M18 12H9"/></svg>;
    case 'user':
      return <svg {...common}><circle cx="12" cy="8" r="4"/><path d="M4 20c1.2-3.6 4-6 8-6s6.8 2.4 8 6"/></svg>;
    case 'user-plus':
      return <svg {...common}><circle cx="10" cy="8" r="3.5"/><path d="M3 20c1-3 3.5-5 7-5s6 2 7 5"/><path d="M18 6v6M15 9h6"/></svg>;
    case 'arrow-up-right':
      return <svg {...common}><path d="M7 17L17 7M9 7h8v8"/></svg>;
    case 'arrow-down-left':
      return <svg {...common}><path d="M17 7L7 17M15 17H7V9"/></svg>;
    case 'shuffle':
      return <svg {...common}><path d="M4 7h3l10 10h3"/><path d="M17 7h3M4 17h3l3-3"/><path d="M14 10l3-3M17 17l3 3M17 7l3-3"/></svg>;
    case 'backspace':
      return <svg {...common}><path d="M9 5h11a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H9l-6-7z"/><path d="M13 10l4 4M17 10l-4 4"/></svg>;
    case 'delete':
      return <svg {...common}><path d="M20 5H9l-6 7 6 7h11a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1z"/><path d="M14 10l-4 4M10 10l4 4" stroke="#fff"/></svg>;
    case 'dots':
      return <svg {...common}><circle cx="12" cy="5" r="1.5" fill="currentColor"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/><circle cx="12" cy="19" r="1.5" fill="currentColor"/></svg>;
    case 'search':
      return <svg {...common}><circle cx="11" cy="11" r="6.5"/><path d="M20 20l-4.5-4.5"/></svg>;
    case 'info':
      return <svg {...common}><circle cx="12" cy="12" r="9"/><path d="M12 11v6"/><circle cx="12" cy="8" r="0.6" fill="currentColor"/></svg>;
    case 'clock':
      return <svg {...common}><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>;
    case 'sparkle':
      return <svg {...common}><path d="M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5L18 18M6 18l2.5-2.5M15.5 8.5L18 6"/></svg>;
    default:
      return <svg {...common}><rect x="5" y="5" width="14" height="14" rx="2"/></svg>;
  }
};

Object.assign(window, { ZAPP_LIGHT, ZAPP_DARK, ZAPP_FONT, ZAPP_MONO, Icon });
