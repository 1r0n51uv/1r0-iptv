# Roadmap — Prossimi passi

Backlog nato da una sessione di grilling sui prossimi passi del progetto. Ogni voce fa riferimento al [`CONTEXT.md`](CONTEXT.md) per i termini di dominio e alle [ADR](docs/adr/) per le decisioni con un trade-off reale dietro. Le fasi riflettono le dipendenze tra gli item, non necessariamente l'ordine di importanza percepita.

## Fase 1 — Fondamenta: Visto e Preferito in app

Sbloccano quasi tutto il resto (resume, Dashboard, Nuovi episodi, focus memory).

- [ ] **Visto**: nuova entità di dominio + persistenza locale (Film/Episodio, posizione di riproduzione). Vedi ADR 0004 per lo scope (niente Canale).
- [ ] **Better player (resume)**: `PlayerScreen` salva/legge la posizione tramite Visto; ripresa automatica quando si riapre lo stesso contenuto.
- [ ] **Pulsante Preferiti in app**: primo toggle Preferito nella UI TV (oggi esiste solo il campo dati `ContentCustomization.favorite`, nessuna UI in app).

## Fase 2 — Pagina di Dettaglio (Film + Serie)

Dipende dalla Fase 1 per i pulsanti Riproduci/Continua e Preferiti.

- [ ] **Dettaglio Film**: oggi cliccare un Film avvia subito il player come un Canale; introdurre uno step di Dettaglio (cover, plot, categoria, Riproduci/Continua, Preferiti), il Canale resta invariato.
- [ ] **Upgrade grafico Dettaglio Serie**: hero con cover/badge/plot/meta, tab Stagione, pulsante Riproduci/Continua, pulsante Preferiti.
- [ ] **Carousel episodi**: sostituisce l'attuale lista verticale con un carousel orizzontale di card (immagine, titolo, eventuale barra di progresso), filtrato per Stagione selezionata.
- [ ] **Immagine per-episodio da Xtream**: estende `XtreamEpisodeDto`/`XtreamMapper` per catturare l'immagine per-episodio quando il provider la espone; M3U resta sul poster della Serie (nessun campo equivalente).
- [ ] **Riproduci con**: azione esplicita (es. long-press) che apre lo stream in un player esterno via intent chooser Android, alternativa al player interno.
- [ ] **Rimuovi nome canale dal player**: da rivedere in dettaglio quando ci si arriva — con il Dettaglio che ora mostra già il titolo prima del player, va deciso se l'overlay va tolto ovunque o solo per Film/Episodio (il Canale non ha un Dettaglio che lo anticipi).

## Fase 3 — Navigazione: Sidebar e Dashboard

Ridisegna l'impalcatura di navigazione dell'app (oggi un'unica schermata scrollabile, `Screen.kt` con Home/SeriesDetail/Player).

- [ ] **Sidebar**: barra di navigazione persistente (Dashboard, Guida TV, Cerca, Preferiti, Impostazioni).
- [ ] **Dashboard**: sostituisce Home come schermata di atterraggio; riga "continua a guardare" basata sui Visti.
- [ ] **Ricerca**: collegare la schermata già wireframata (`TvSearch`) alla Sidebar.
- [ ] **Preferiti (schermata)**: collegare la griglia mista già wireframata (`TvFavorites`) alla Sidebar.
- [ ] **On open focus / back-focus**: cold start focalizza l'ultimo Visto/riprodotto (fallback: Contenuto di default configurabile); tornare da Dettaglio/Player rifocalizza sempre la card di provenienza. Stesso stato "ultimo focus" per entrambi.
- [ ] **On close app keep content selected**: l'app ricorda contenuto e posizione (via Visto) e riprende silenziosamente al riavvio; niente playback in background (nessun `MediaSessionService`, coerente con ADR 0001).

## Fase 4 — Nuovi episodi e Suggerimenti

Dipende dalla Dashboard (dove vivono) e da Visto/Preferito (chi viene coperto).

- [ ] **Nuovi episodi**: sezione in Dashboard per le Serie con almeno un Visto/Preferito, calcolata sul refresh delle Sorgenti. Vedi ADR 0006 (niente notifiche di sistema).
- [ ] **Suggeriti da AI**: riga Dashboard alimentata da una chiamata diretta a un'AI esterna (es. Claude) con titoli di Visti/Preferiti + campione di catalogo. Vedi ADR 0003 (chiave sul device, nessun backend). Richiede un punto nelle Impostazioni per inserire la chiave API.

## Fase 5 — Guida TV

Introduce l'entità **Programma**, oggi assente dal modello dati.

- [ ] **Guida TV**: fetch dei programmi dalle API EPG di Xtream (`get_short_epg`/`get_simple_data_table`); i Canali da M3U restano senza palinsesto per ora, stesso pattern di degradazione di ADR 0002. XMLTV per M3U resta backlog futuro, non in questa fase.

## Fase 6 — Sport live e dashboard dinamica

- [ ] **Ricerca provider API sport**: 1-2 opzioni di dati calcistici live (costo/rate limit), da validare prima di implementare.
- [ ] **Sport (now playing) + evidenza in Dashboard**: una volta scelto il provider, riga/evidenza in Dashboard per 1-2 partite in corso o un programma in evidenza sui Canali, dietro un'impostazione attivabile.

## Fase 7 — Riproduzione da web (bassa priorità, esplicitamente rimandata)

- [ ] **Play content from web**: azione "Riproduci sulla TV" nel Pannello Web, stessa libreria gestita oggi. Vedi ADR 0005 (comando HTTP locale, non un vero protocollo di cast).

## Fase 8 — Rifinitura (da dettagliare item per item quando ci si arriva)

Nessuna decisione architetturale dietro, solo grilling leggero al momento dell'implementazione.

- [ ] **Impostazioni**: schermata raggiungibile da Sidebar; contiene almeno Contenuto di default, Color settings, chiave API AI (Fase 4).
- [ ] **Color settings**
- [ ] **Better loading**
- [ ] **Refresh button** (in app, oltre al già esistente "Aggiorna ora" del Pannello Web — da chiarire dove va collocato)

---

Le decisioni dietro le voci con un vero trade-off architetturale sono in [`docs/adr/`](docs/adr/) (0003–0006, oltre alle preesistenti 0001–0002). I nuovi termini di dominio introdotti da questa roadmap sono in [`CONTEXT.md`](CONTEXT.md) sotto "Cronologia, scoperta e navigazione".
