// Directions 1-3: grounded in the app's OWN existing dark-theme tokens
// (#14161a bg, #ffb454 amber accent, badge colors) — structural inspiration
// from the two reference screenshots (Playbox hero+rows, IPTVify grid+stats).

function PosterTall({ title, sub, focused }) {
  const h = hueFor(title);
  return (
    <div style={{ width: 148, flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div style={{
        position: 'relative', width: 148, height: 210, borderRadius: 10, overflow: 'hidden',
        background: `linear-gradient(160deg, hsl(${h} 45% 22%), hsl(${(h + 40) % 360} 40% 10%))`,
        border: focused ? '3px solid #ffb454' : '3px solid transparent',
        boxShadow: focused ? '0 0 0 3px rgba(255,180,84,.28), 0 12px 28px rgba(0,0,0,.5)' : '0 8px 20px rgba(0,0,0,.35)',
      }}>
        {focused && (
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ width: 44, height: 44, borderRadius: 22, background: '#ffb454', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#14161a' }}>
              <Icon.play s={18} />
            </div>
          </div>
        )}
        <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: '18px 10px 8px', background: 'linear-gradient(to top, rgba(0,0,0,.85), transparent)' }}>
          <div style={{ color: '#f2f2f0', fontSize: 13, fontWeight: 600, lineHeight: 1.25 }}>{title}</div>
        </div>
      </div>
    </div>
  );
}

function Direction1() {
  return (
    <div style={{ width: 1280, height: 720, background: '#14161a', color: '#f2f2f0', fontFamily: 'system-ui, sans-serif', display: 'flex', overflow: 'hidden' }}>
      <Sidebar active={0} />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 28, padding: '18px 36px', borderBottom: '1px solid #22262e' }}>
          <div style={{ fontSize: 18, fontWeight: 700 }}>1r0 <span style={{ color: '#ffb454' }}>IPTV</span></div>
          <div style={{ display: 'flex', gap: 6, background: '#1c1f26', borderRadius: 22, padding: 4 }}>
            {['Canali', 'Film', 'Serie'].map((t, i) => (
              <div key={t} style={{ padding: '8px 18px', borderRadius: 18, fontSize: 13, fontWeight: 600, background: i === 1 ? '#ffb454' : 'transparent', color: i === 1 ? '#14161a' : '#c7cad0' }}>{t}</div>
            ))}
          </div>
          <div style={{ flex: 1 }} />
          <div style={{ fontSize: 13, color: '#9aa0aa' }}>Giovedì, 9 Aprile</div>
        </div>

        <div style={{ position: 'relative', height: 280, flexShrink: 0, overflow: 'hidden', background: 'linear-gradient(120deg, hsl(28 55% 18%), #14161a 70%)' }}>
          <div style={{ position: 'absolute', right: -20, top: '50%', transform: 'translateY(-50%)', opacity: .18 }}>
            <svg width={260} height={260} viewBox="0 0 24 24" fill="none" stroke="#ffb454" strokeWidth="0.6"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 8h18M3 16h18M9 3v18"/></svg>
          </div>
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(100deg, rgba(20,22,26,.96) 34%, rgba(20,22,26,.5) 66%, transparent)' }} />
          <div style={{ position: 'relative', height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 8, padding: '0 36px', maxWidth: 600 }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '.06em', color: '#ffb454' }}>{HERO.eyebrow}</div>
            <div style={{ fontSize: 30, fontWeight: 800, lineHeight: 1.1 }}>{HERO.title}</div>
            <div style={{ fontSize: 13, color: '#9aa0aa' }}>{HERO.meta}</div>
            <div style={{ fontSize: 14, color: '#c7cad0', lineHeight: 1.5 }}>{HERO.plot}</div>
            <div style={{ display: 'flex', gap: 10, marginTop: 10 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: '#ffb454', color: '#14161a', fontWeight: 700, fontSize: 13, padding: '10px 18px', borderRadius: 8 }}><Icon.play s={13}/>Riprendi S02E04</div>
              <div style={{ background: 'rgba(255,255,255,.1)', fontWeight: 600, fontSize: 13, padding: '10px 18px', borderRadius: 8 }}>Dettagli</div>
            </div>
          </div>
        </div>

        <div style={{ flex: 1, padding: '18px 36px', display: 'flex', flexDirection: 'column', gap: 16, overflow: 'hidden' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 12 }}>
              <div style={{ fontSize: 17, fontWeight: 700 }}>Ultimi aggiunti</div>
              <div style={{ fontSize: 12, color: '#9aa0aa' }}>Vedi tutti →</div>
            </div>
            <div style={{ display: 'flex', gap: 14 }}>
              {FILM.map((f, i) => <PosterTall key={f.title} title={f.title} focused={i === 0} />)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function StatChip({ label, value }) {
  return (
    <div style={{ background: '#1c1f26', borderRadius: 10, padding: '12px 20px', display: 'flex', flexDirection: 'column', gap: 2 }}>
      <div style={{ fontSize: 20, fontWeight: 800, color: '#f2f2f0' }}>{value}</div>
      <div style={{ fontSize: 11, color: '#9aa0aa', textTransform: 'uppercase', letterSpacing: '.04em' }}>{label}</div>
    </div>
  );
}

function GridTile({ title, sub, badge, badgeColor }) {
  const h = hueFor(title);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      <div style={{ position: 'relative', height: 72, borderRadius: 8, background: `linear-gradient(145deg, hsl(${h} 40% 24%), hsl(${(h+50)%360} 35% 12%))` }}>
        {badge && <div style={{ position: 'absolute', top: 6, left: 6, fontSize: 9, fontWeight: 700, letterSpacing: '.03em', padding: '2px 6px', borderRadius: 4, background: badgeColor, color: '#14161a' }}>{badge}</div>}
      </div>
      <div style={{ fontSize: 12, fontWeight: 600, color: '#f2f2f0', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
      {sub && <div style={{ fontSize: 10.5, color: '#9aa0aa', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{sub}</div>}
    </div>
  );
}

function Direction2() {
  const sidebarIcons = [Icon.home, Icon.tv, Icon.search, Icon.grid, Icon.heart];
  return (
    <div style={{ width: 1280, height: 720, background: '#14161a', color: '#f2f2f0', fontFamily: 'system-ui, sans-serif', display: 'flex', overflow: 'hidden' }}>
      <div style={{ width: 88, flexShrink: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 18, padding: '24px 0', background: '#191c22', borderRight: '1px solid #262b33' }}>
        {sidebarIcons.map((I, i) => (
          <div key={i} style={{ width: 40, height: 40, borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', background: i === 0 ? '#ffb454' : 'transparent', color: i === 0 ? '#14161a' : '#9aa0aa' }}><I s={19}/></div>
        ))}
        <div style={{ flex: 1 }} />
        <div style={{ width: 40, height: 40, borderRadius: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9aa0aa' }}><Icon.settings s={19}/></div>
      </div>

      <div style={{ flex: 1, padding: '22px 32px', display: 'flex', flexDirection: 'column', gap: 18, overflow: 'hidden' }}>
        <div style={{ display: 'flex', gap: 12 }}>
          <StatChip label="Canali" value="128" />
          <StatChip label="Film" value="340" />
          <StatChip label="Serie" value="96" />
          <div style={{ flex: 1 }} />
          <div style={{ display: 'flex', gap: 8 }}>
            {['Tutti', 'Sport', 'Bambini', 'News'].map((c, i) => (
              <div key={c} style={{ padding: '9px 14px', borderRadius: 18, fontSize: 12, fontWeight: 600, background: i === 0 ? '#ffb454' : '#1c1f26', color: i === 0 ? '#14161a' : '#c7cad0' }}>{c}</div>
            ))}
          </div>
        </div>

        <div style={{ background: '#1c1f26', borderRadius: 12, padding: '14px 18px', display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '.05em', color: '#ffb454' }}>IN ONDA ORA</div>
          {[{ ch: 'Rai 1 HD', prog: 'Il Commissario Ricciardi', pct: 62 }, { ch: 'Sky Sport', prog: 'Champions League Live', pct: 35 }].map((r) => (
            <div key={r.ch} style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
              <div style={{ width: 88, fontSize: 13, fontWeight: 600 }}>{r.ch}</div>
              <div style={{ flex: 1, fontSize: 13, color: '#c7cad0' }}>{r.prog}</div>
              <div style={{ width: 160, height: 4, borderRadius: 2, background: 'rgba(255,255,255,.12)', position: 'relative' }}>
                <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: r.pct + '%', background: '#ffb454', borderRadius: 2 }} />
              </div>
            </div>
          ))}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, minmax(0,1fr))', gap: 12, overflow: 'hidden' }}>
          {CANALI.concat(CANALI).slice(0, 8).map((c, i) => (
            <GridTile key={i} title={c.title} sub={c.sub} badge="LIVE" badgeColor="#e0483c" />
          ))}
        </div>
      </div>
    </div>
  );
}

function GlassCard({ title, imageUrl, w = 168, h = 96 }) {
  const hh = hueFor(title);
  return (
    <div style={{ width: w, flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div style={{
        height: h, borderRadius: 14, background: `linear-gradient(150deg, hsla(${hh},60%,45%,.25), hsla(${(hh+60)%360},60%,30%,.15))`,
        border: '1px solid rgba(255,255,255,.14)', backdropFilter: 'blur(6px)',
      }} />
      <div style={{ fontSize: 13, fontWeight: 600, color: '#f2f2f0' }}>{title}</div>
    </div>
  );
}

function Direction3() {
  return (
    <div style={{ width: 1280, height: 720, position: 'relative', background: '#0e0f13', color: '#f2f2f0', fontFamily: 'system-ui, sans-serif', overflow: 'hidden', display: 'flex' }}>
      <div style={{ position: 'absolute', width: 500, height: 500, borderRadius: '50%', background: 'radial-gradient(circle, rgba(255,180,84,.22), transparent 70%)', top: -160, right: -100 }} />
      <div style={{ position: 'absolute', width: 460, height: 460, borderRadius: '50%', background: 'radial-gradient(circle, rgba(139,92,246,.18), transparent 70%)', bottom: -180, left: 120 }} />

      <div style={{ width: 92, flexShrink: 0, position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 18, padding: '26px 0', background: 'rgba(255,255,255,.04)', backdropFilter: 'blur(10px)', borderRight: '1px solid rgba(255,255,255,.08)' }}>
        {[Icon.home, Icon.tv, Icon.search, Icon.grid, Icon.heart].map((I, i) => (
          <div key={i} style={{ width: 42, height: 42, borderRadius: 12, display: 'flex', alignItems: 'center', justifyContent: 'center', background: i === 0 ? 'rgba(255,180,84,.9)' : 'transparent', color: i === 0 ? '#14161a' : 'rgba(255,255,255,.65)' }}><I s={19}/></div>
        ))}
      </div>

      <div style={{ flex: 1, position: 'relative', zIndex: 1, padding: '30px 40px', display: 'flex', flexDirection: 'column', gap: 22, overflow: 'hidden' }}>
        <div style={{
          borderRadius: 20, padding: '26px 32px', background: 'rgba(255,255,255,.05)', border: '1px solid rgba(255,255,255,.1)',
          backdropFilter: 'blur(14px)', display: 'flex', flexDirection: 'column', gap: 8, maxWidth: 620,
        }}>
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '.06em', color: '#ffb454' }}>{HERO.eyebrow}</div>
          <div style={{ fontSize: 28, fontWeight: 700 }}>{HERO.title}</div>
          <div style={{ fontSize: 13, color: 'rgba(255,255,255,.55)' }}>{HERO.meta}</div>
          <div style={{ fontSize: 13.5, color: 'rgba(255,255,255,.75)', lineHeight: 1.55 }}>{HERO.plot}</div>
          <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: '#ffb454', color: '#14161a', fontWeight: 700, fontSize: 13, padding: '10px 18px', borderRadius: 10 }}><Icon.play s={13}/>Riprendi</div>
            <div style={{ background: 'rgba(255,255,255,.1)', border: '1px solid rgba(255,255,255,.14)', fontWeight: 600, fontSize: 13, padding: '10px 18px', borderRadius: 10 }}>Dettagli</div>
          </div>
        </div>

        <div>
          <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 12, color: 'rgba(255,255,255,.9)' }}>Canali in evidenza</div>
          <div style={{ display: 'flex', gap: 14 }}>{CANALI.map((c) => <GlassCard key={c.title} title={c.title} />)}</div>
        </div>
        <div>
          <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 12, color: 'rgba(255,255,255,.9)' }}>Film</div>
          <div style={{ display: 'flex', gap: 14 }}>{FILM.slice(0,6).map((c) => <GlassCard key={c.title} title={c.title} />)}</div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { Direction1, Direction2, Direction3 });
