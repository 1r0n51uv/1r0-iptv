# Roadmap — Prossimi passi

Nata da una sessione di grilling sulla lista dei prossimi passi, ora tutta implementata.
La logica sta nel modulo `:domain`, sviluppata in TDD; l'app resta da provare sulla TV.
I termini in maiuscolo sono definiti in [`CONTEXT.md`](CONTEXT.md), le decisioni con un
trade-off reale dietro sono in [`docs/adr/`](docs/adr/).

## Fase 1 — Visto e Preferito

- [x] **Visto**: entità di dominio e persistenza locale della posizione di riproduzione, solo Film ed Episodio (ADR 0004).
- [x] **Ripresa nel player**: il player riparte da dove si era rimasti e salva la posizione durante la riproduzione.
- [x] **Preferiti in app**: il toggle Preferito esiste anche fuori dal Pannello Web, sulla pagina di Dettaglio.

## Fase 2 — Pagina di Dettaglio

- [x] **Dettaglio Film**: un Film non parte più al volo, si ferma sulla sua pagina; il Canale resta a riproduzione immediata.
- [x] **Dettaglio Serie**: testata con copertina, trama e categoria, pulsante Riproduci/Continua guidato dal Visto, pulsante Preferiti.
- [x] **Carousel episodi**: card per Stagione con barra di avanzamento, al posto della lista verticale.
- [x] **Immagine per-episodio**: catturata da Xtream quando il provider la espone; le Sorgenti M3U ripiegano sul poster della Serie.
- [x] **Riproduci con**: pressione lunga su una card, o pulsante sul Dettaglio, per aprire lo stream in un player esterno.
- [x] **Nome del contenuto nel player**: overlay in alto a sinistra all'apertura, sparisce da solo dopo 5 secondi (`PlayerScreen`).

## Fase 3 — Sidebar e Dashboard

- [x] **Sidebar**: navigazione persistente verso Dashboard, Guida TV, Sfoglia, Cerca, Preferiti e Impostazioni.
- [x] **Dashboard**: schermata di atterraggio al posto della Home, apre con Continua a guardare; le righe vuote non compaiono.
- [x] **Sfoglia, Cerca e Preferiti**: le tre schermate di catalogo, raggiungibili dalla Sidebar.
- [x] **Memoria del focus**: all'avvio il focus va sempre sulla banda Riprendi/Continua a guardare (ripiego: Contenuto di default, poi la prima card); tornando da Dettaglio o player si ritrova la card di partenza.
- [x] **Contenuto selezionato alla chiusura**: l'app ricorda contenuto e posizione e riprende al riavvio, senza riproduzione in background.

## Fase 4 — Nuovi episodi e Suggerimenti

- [x] **Nuovi episodi**: riga in Dashboard per le Serie seguite, dal confronto con l'ultimo refresh (ADR 0006, niente notifiche di sistema).
- [x] **Suggeriti da AI**: riga in Dashboard da una chiamata diretta a Claude con la chiave dell'utente (ADR 0003).

## Fase 5 — Guida TV

- [x] **Guida TV**: palinsesto dalle API EPG di Xtream con evidenza del programma in onda. I Canali M3U restano senza guida (stesso degradare dell'ADR 0002); XMLTV per M3U resta lavoro futuro.

## Fase 6 — Sport live

- [x] **Provider**: football-data.org, piano gratuito con chiave via email, Serie A inclusa, dieci richieste al minuto.
- [x] **Due partite in evidenza**: fascia in cima alla Dashboard dietro un'impostazione; se un Canale nomina entrambe le squadre la partita è apribile.

## Fase 7 — Riproduzione dal Pannello Web

- [x] **Riproduci sulla TV**: dal browser si sfoglia lo stesso catalogo e si apre un contenuto sulla TV, con un comando locale invece di un protocollo di cast (ADR 0005).

## Fase 8 — Rifinitura

- [x] **Impostazioni**: chiavi API e Contenuto di default dal Pannello Web, colore di accento e interruttore sport dalla TV.
- [x] **Colore di accento**: quattro scelte, applicate a tutta l'app.
- [x] **Caricamento**: scheletro delle righe al posto della scritta al centro.
- [x] **Aggiorna catalogo**: voce in fondo alla Sidebar; il catalogo vecchio resta a schermo durante l'aggiornamento.

## Fase 9 — Player, cronologia e caricamento

Nata da una sessione di grilling su bug e rifiniture raccolte dopo l'uso reale dell'app.

- [ ] **Overlay controlli player**: overlay custom per TV al posto dei controlli di default di Media3 (ADR 0007). Sinistra/destra saltano sempre avanti/indietro di un passo fisso con un indicatore visivo; su' porta il focus sul pulsante "Prossimo episodio" (quando c'e' un Episodio in coda), centro conferma o alterna play/pausa. Risolve anche il player bloccato sull'ultimo frame a fine episodio (il `PlayerView` non viene mai ri-agganciato al nuovo `ExoPlayer` quando la coda avanza).
- [x] **Reset Visto granulare**: rimozione del Visto per singolo Episodio, per intera Stagione o per intera Serie, esposta con la stessa pressione lunga già usata sulle card altrove nell'app. Nessun reset globale "cancella tutto". Per Episodio e Stagione, che non avevano ancora nessun menu contestuale, la pressione lunga apre un piccolo menu dedicato (`MenuEpisodio`, `MenuStagione`); per la Serie resta quello già esistente sulle card di Dashboard/Sfoglia.
- [ ] **Skeleton sul Dettaglio Film**: come già presente sul Dettaglio Serie, mentre arrivano trama e metadati estesi.
- [ ] **Cache di sessione sul Dettaglio**: i dati di Stagioni/Episodi di una Serie e i metadati estesi di un Film restano in memoria per la sessione corrente, invece di essere ricaricati da zero ogni volta che si torna dal Player (bug confermato: oggi il Dettaglio viene smontato e ricomposto da capo).
- [ ] **Copia locale del catalogo**: catalogo testuale persistito su disco (ADR 0008), letto subito all'avvio invece di attendere la rete, aggiornato in background riusando il rilevamento "Nuovi episodi" già esistente. Il pulsante "Aggiorna catalogo" in Sidebar si anima ogni volta che una sincronizzazione è in corso, automatica o manuale. Dal Pannello Web si può escludere dalla sincronizzazione automatica un'intera Sorgente, una categoria o un singolo contenuto.
- [ ] **Lifecycle app**: la riproduzione si ferma esplicitamente quando l'app va in background (oggi nessun codice di lifecycle, comportamento casuale); riaprendo l'app si torna direttamente sul Player con lo stesso contenuto e la stessa posizione — da lì Indietro va alla Dashboard, senza ricostruire l'intero stack di navigazione.

Parcheggiato: **Suggeriti da AI** — cache/rate-limiting delle chiamate e pulsante di rigenerazione manuale, da riprendere dopo aver provato la funzione esistente sul dispositivo reale.

## Da verificare sulla TV

Il modulo `:app` non è compilabile nell'ambiente in cui è stato scritto (l'Android Gradle
Plugin sta su `dl.google.com`, irraggiungibile lì), quindi la prova sul dispositivo è la
prima vera verifica per: navigazione col D-pad e ripristino del focus, ripresa della
riproduzione, e la fascia sport — di quest'ultima non è stato possibile verificare la forma
esatta delle risposte del provider, e il parsing è scritto per far sparire la fascia invece
di rompere la Dashboard.
