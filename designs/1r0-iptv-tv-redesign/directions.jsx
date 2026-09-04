// Shared content + helpers for all 6 direction artboards.
// Deterministic per-title hue so poster placeholders read as "different
// artwork" across a row without any real (possibly copyrighted) images.
function hueFor(title) {
  let h = 0;
  for (let i = 0; i < title.length; i++) h = (h * 31 + title.charCodeAt(i)) % 360;
  return h;
}

const CANALI = [
  { title: 'Rai 1 HD', sub: 'Il Commissario Ricciardi', initials: 'R1' },
  { title: 'Rai 2 HD', sub: 'Che Tempo Che Fa', initials: 'R2' },
  { title: 'Canale 5', sub: 'Grande Fratello', initials: 'C5' },
  { title: 'Sky Sport', sub: 'Champions League Live', initials: 'SS' },
  { title: 'La7', sub: 'Atlantide', initials: 'L7' },
  { title: 'Rete 4', sub: 'Quarta Repubblica', initials: 'R4' },
];

const FILM = [
  { title: 'Oppenheimer' },
  { title: 'Povere Creature!' },
  { title: 'Past Lives' },
  { title: 'Anatomia di una Caduta' },
  { title: 'Dune: Parte Due' },
  { title: 'La Zona d’Interesse' },
];

const SERIE = [
  { title: 'The Bear', sub: 'S02' },
  { title: 'Slow Horses', sub: 'S04' },
  { title: 'Shogun', sub: 'S01' },
  { title: 'Fargo', sub: 'S05' },
  { title: 'C’è ancora domani' },
];

const HERO = {
  eyebrow: 'SERIE · IN EVIDENZA',
  title: 'The Bear',
  meta: '2022–2024 · Commedia drammatica · 3 stagioni',
  plot: 'Carmy, giovane chef stellato, torna a Chicago per gestire la trattoria di famiglia dopo un lutto, tra caos in cucina e rapporti familiari complicati.',
};

// ---- Icons (inline stroke SVGs, one consistent style) ----
const Icon = {
  home: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M3 11l9-7 9 7"/><path d="M5 10v10h14V10"/></svg>,
  tv: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M8 21h8M12 19v2"/></svg>,
  search: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>,
  grid: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="4" width="18" height="17" rx="2"/><path d="M3 9h18M8 3v3M16 3v3"/></svg>,
  heart: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M12 20s-7-4.35-9.3-8.8C1.2 8 3 5 6.2 5c1.8 0 3.1 1 3.8 2.1C10.7 6 12 5 13.8 5 17 5 18.8 8 17.3 11.2 15 15.65 12 20 12 20z"/></svg>,
  settings: (p) => <svg width={p.s||20} height={p.s||20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 00.3 1.9l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.9-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1-1.6 1.7 1.7 0 00-1.9.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.9 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1 1.7 1.7 0 00-.3-1.9l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.9.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.9-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.9V9c.2.6.7 1.1 1.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z"/></svg>,
  play: (p) => <svg width={p.s||16} height={p.s||16} viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>,
};

// Shared left sidebar rail — Home / Canali / Cerca / Guida TV / Preferiti,
// Impostazioni pinned to the bottom. `active` is the index of the lit icon.
function Sidebar({ active = 0 }) {
  const icons = [Icon.home, Icon.tv, Icon.search, Icon.grid, Icon.heart];
  return (
    <div style={{ width: 88, flexShrink: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 18, padding: '24px 0', background: '#191c22', borderRight: '1px solid #262b33' }}>
      {icons.map((I, i) => (
        <div key={i} style={{ width: 40, height: 40, borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', background: i === active ? '#ffb454' : 'transparent', color: i === active ? '#14161a' : '#9aa0aa' }}><I s={19} /></div>
      ))}
      <div style={{ flex: 1 }} />
      <div style={{ width: 40, height: 40, borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9aa0aa' }}><Icon.settings s={19} /></div>
    </div>
  );
}

Object.assign(window, { hueFor, CANALI, FILM, SERIE, HERO, Icon, Sidebar });
