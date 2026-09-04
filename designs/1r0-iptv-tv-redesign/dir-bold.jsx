// Directions 4-6: bold aesthetic explorations per frontend-design.md — each
// commits to one extreme tone, genuinely different from the other five and
// from each other (editorial / retro-futurist / minimal-luxury).

function EditorialPoster({ title, index }) {
  const h = hueFor(title);
  return (
    <div style={{ width: 152, flexShrink: 0 }}>
      <div style={{
        height: 204, background: `linear-gradient(175deg, hsl(${h} 18% 30%), hsl(${h} 22% 12%))`,
        filter: 'grayscale(.35) sepia(.12)', position: 'relative',
      }}>
        <div style={{ position: 'absolute', top: 10, left: 10, fontFamily: '"Newsreader", serif', fontSize: 30, color: 'rgba(245,240,230,.5)' }}>{String(index + 1).padStart(2, '0')}</div>
      </div>
      <div style={{ marginTop: 10, fontFamily: '"Newsreader", serif', fontSize: 15, fontStyle: 'italic', color: '#f5f0e6' }}>{title}</div>
    </div>
  );
}

function Direction4() {
  return (
    <div style={{ width: 1280, height: 720, background: '#161412', color: '#f5f0e6', fontFamily: '"Public Sans", sans-serif', overflow: 'hidden', position: 'relative' }}>
      <div style={{ position: 'absolute', top: 36, left: 44, right: 44, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(245,240,230,.18)', paddingBottom: 14 }}>
        <div style={{ fontFamily: '"Newsreader", serif', fontSize: 20, letterSpacing: '.02em' }}>1r0 <em>IPTV</em></div>
        <div style={{ fontSize: 11, letterSpacing: '.14em', textTransform: 'uppercase', color: 'rgba(245,240,230,.55)' }}>Edizione · Giovedì 9 Aprile</div>
      </div>

      <div style={{ position: 'absolute', top: 108, left: 44, width: 540 }}>
        <div style={{ fontSize: 11, letterSpacing: '.16em', textTransform: 'uppercase', color: '#c9793a', marginBottom: 14 }}>N.04 — Serie della settimana</div>
        <div style={{ fontFamily: '"Newsreader", serif', fontSize: 56, lineHeight: 1.02, fontWeight: 500 }}>{HERO.title}</div>
        <div style={{ fontSize: 13, letterSpacing: '.04em', color: 'rgba(245,240,230,.55)', marginTop: 16, marginBottom: 14 }}>{HERO.meta.toUpperCase()}</div>
        <div style={{ fontSize: 15, lineHeight: 1.7, color: 'rgba(245,240,230,.82)', maxWidth: 480 }}>{HERO.plot}</div>
        <div style={{ marginTop: 22, display: 'inline-flex', alignItems: 'center', gap: 10, borderBottom: '1px solid #f5f0e6', paddingBottom: 4, fontSize: 13, letterSpacing: '.06em', textTransform: 'uppercase', cursor: 'pointer' }}>
          Continua a leggere <span>→</span>
        </div>
      </div>

      <div style={{
        position: 'absolute', top: 60, right: 44, width: 380, height: 460,
        background: `linear-gradient(200deg, hsl(${hueFor(HERO.title)} 22% 26%), hsl(${hueFor(HERO.title)} 24% 10%))`,
        filter: 'grayscale(.3) sepia(.15)',
      }} />

      <div style={{ position: 'absolute', bottom: 40, left: 44, right: 44 }}>
        <div style={{ fontSize: 11, letterSpacing: '.14em', textTransform: 'uppercase', color: 'rgba(245,240,230,.55)', marginBottom: 16, borderTop: '1px solid rgba(245,240,230,.18)', paddingTop: 16 }}>In sala questa settimana</div>
        <div style={{ display: 'flex', gap: 22 }}>
          {FILM.slice(0, 6).map((f, i) => <EditorialPoster key={f.title} title={f.title} index={i} />)}
        </div>
      </div>
    </div>
  );
}

function NeonTile({ title, sub, wide }) {
  const h = [320, 185, 45][hueFor(title) % 3];
  return (
    <div style={{ width: wide ? 240 : 168, flexShrink: 0 }}>
      <div style={{
        height: 100, position: 'relative', clipPath: 'polygon(10px 0,100% 0,100% calc(100% - 10px),calc(100% - 10px) 100%,0 100%,0 10px)',
        background: `linear-gradient(160deg, hsl(${h} 90% 14%), #0a0a0f)`,
        border: `1px solid hsl(${h} 100% 55%)`, boxShadow: `0 0 18px hsl(${h} 100% 45% / .45), inset 0 0 24px hsl(${h} 100% 40% / .2)`,
      }} />
      <div style={{ marginTop: 8, fontFamily: '"Space Mono", monospace', fontSize: 12.5, color: '#f2f2f0' }}>{title}</div>
      {sub && <div style={{ fontFamily: '"Space Mono", monospace', fontSize: 10.5, color: `hsl(${h} 100% 65%)` }}>{sub}</div>}
    </div>
  );
}

function Direction5() {
  return (
    <div style={{ width: 1280, height: 720, background: '#050507', color: '#f2f2f0', fontFamily: '"Space Mono", monospace', overflow: 'hidden', position: 'relative' }}>
      <div style={{
        position: 'absolute', inset: 0, backgroundImage: 'repeating-linear-gradient(0deg, rgba(255,255,255,.035) 0px, rgba(255,255,255,.035) 1px, transparent 1px, transparent 3px)',
        pointerEvents: 'none',
      }} />
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0, height: 260,
        background: 'linear-gradient(180deg, transparent, rgba(255,46,136,.08))',
        backgroundImage: 'linear-gradient(rgba(0,229,255,.18) 1px, transparent 1px), linear-gradient(90deg, rgba(0,229,255,.18) 1px, transparent 1px)',
        backgroundSize: '54px 54px', maskImage: 'linear-gradient(to top, black, transparent)',
      }} />

      <div style={{ position: 'relative', padding: '28px 40px', display: 'flex', alignItems: 'center', gap: 20 }}>
        <div style={{ fontSize: 20, fontWeight: 700, color: '#00e5ff', textShadow: '0 0 14px rgba(0,229,255,.8)' }}>1R0::IPTV</div>
        <div style={{ flex: 1 }} />
        <div style={{ fontSize: 12, color: '#ff2e88', textShadow: '0 0 10px rgba(255,46,136,.7)' }}>● LIVE</div>
        <div style={{ fontSize: 12, color: 'rgba(255,255,255,.5)' }}>21:47</div>
      </div>

      <div style={{ position: 'relative', padding: '10px 40px 26px', maxWidth: 640 }}>
        <div style={{ fontSize: 11, letterSpacing: '.12em', color: '#ff2e88' }}>{'>'} SERIE_IN_EVIDENZA</div>
        <div style={{ fontSize: 40, fontWeight: 700, margin: '10px 0', color: '#fff', textShadow: '0 0 22px rgba(0,229,255,.55)' }}>{HERO.title}</div>
        <div style={{ fontSize: 13, color: 'rgba(255,255,255,.65)', lineHeight: 1.6 }}>{HERO.plot}</div>
        <div style={{ marginTop: 16, display: 'inline-flex', alignItems: 'center', gap: 8, padding: '10px 20px', background: '#ff2e88', color: '#050507', fontWeight: 700, fontSize: 13, clipPath: 'polygon(8px 0,100% 0,100% calc(100% - 8px),calc(100% - 8px) 100%,0 100%,0 8px)' }}>
          <Icon.play s={13}/> RIPRENDI_S02E04
        </div>
      </div>

      <div style={{ position: 'relative', padding: '4px 40px' }}>
        <div style={{ fontSize: 11, letterSpacing: '.12em', color: '#00e5ff', marginBottom: 12 }}>{'>'} CANALI</div>
        <div style={{ display: 'flex', gap: 16 }}>{CANALI.map((c) => <NeonTile key={c.title} title={c.title} sub={c.sub} />)}</div>
      </div>
    </div>
  );
}

function LuxRow({ title, index }) {
  const h = hueFor(title);
  return (
    <div style={{ width: 176, flexShrink: 0 }}>
      <div style={{ height: 110, background: `linear-gradient(180deg, hsl(${h} 8% 16%), hsl(${h} 8% 7%))` }} />
      <div style={{ marginTop: 10, fontFamily: '"Jost", sans-serif', fontSize: 13, fontWeight: 400, letterSpacing: '.02em', color: '#e9e4da' }}>{title}</div>
    </div>
  );
}

function Direction6() {
  return (
    <div style={{ width: 1280, height: 720, background: '#0a0a0a', color: '#e9e4da', fontFamily: '"Jost", sans-serif', overflow: 'hidden' }}>
      <div style={{ padding: '48px 64px 0' }}>
        <div style={{ fontFamily: '"Cormorant", serif', fontWeight: 300, fontSize: 26, letterSpacing: '.08em' }}>1R0 IPTV</div>
      </div>

      <div style={{ padding: '46px 64px 0', maxWidth: 620 }}>
        <div style={{ fontSize: 11, letterSpacing: '.22em', textTransform: 'uppercase', color: '#b08d4f', marginBottom: 18 }}>Serie in evidenza</div>
        <div style={{ fontFamily: '"Cormorant", serif', fontWeight: 300, fontSize: 64, lineHeight: 1, marginBottom: 20 }}>{HERO.title}</div>
        <div style={{ height: 1, width: 64, background: '#b08d4f', marginBottom: 20 }} />
        <div style={{ fontSize: 14, lineHeight: 1.9, color: 'rgba(233,228,218,.72)', fontWeight: 300 }}>{HERO.plot}</div>
        <div style={{ marginTop: 26, fontSize: 12, letterSpacing: '.14em', textTransform: 'uppercase', color: '#e9e4da', display: 'inline-flex', alignItems: 'center', gap: 12, cursor: 'pointer' }}>
          Riprendi la visione <span style={{ color: '#b08d4f' }}>—</span> S02E04
        </div>
      </div>

      <div style={{ padding: '54px 64px 0' }}>
        <div style={{ fontSize: 11, letterSpacing: '.22em', textTransform: 'uppercase', color: 'rgba(233,228,218,.5)', marginBottom: 18 }}>Film selezionati</div>
        <div style={{ display: 'flex', gap: 30 }}>{FILM.slice(0, 6).map((f, i) => <LuxRow key={f.title} title={f.title} index={i} />)}</div>
      </div>
    </div>
  );
}

Object.assign(window, { Direction4, Direction5, Direction6 });
