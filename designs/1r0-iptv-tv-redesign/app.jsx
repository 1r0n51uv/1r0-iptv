function App() {
  return (
    <DesignCanvas>
      <DCSection id="home" title="Home — 6 direzioni" subtitle="Stessa schermata, sei trattamenti visivi diversi. Scegline una da portare avanti su tutte le schermate.">
        <DCArtboard id="d1" label="1 · Cinematic Poster Wall" width={1280} height={720}><Direction1 /></DCArtboard>
        <DCArtboard id="d2" label="2 · Vivid Grid Dashboard" width={1280} height={720}><Direction2 /></DCArtboard>
        <DCArtboard id="d3" label="3 · Glass Neon Streaming" width={1280} height={720}><Direction3 /></DCArtboard>
        <DCArtboard id="d4" label="4 · Editorial Cinema" width={1280} height={720}><Direction4 /></DCArtboard>
        <DCArtboard id="d5" label="5 · Neon Retro-Futurism" width={1280} height={720}><Direction5 /></DCArtboard>
        <DCArtboard id="d6" label="6 · Minimal Luxury" width={1280} height={720}><Direction6 /></DCArtboard>
      </DCSection>
      <DCPostIt top={-64} left={44} width={520} rotate={-1.5}>
        1-3 estendono i colori/componenti già usati nell'app (sidebar, badge, #ffb454).{'\n'}
        4-6 sono direzioni più audaci (frontend-design): editoriale, neon retro, lusso minimale.{'\n'}
        Arte poster = placeholder a gradiente, nessuna immagine reale generata.
      </DCPostIt>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
