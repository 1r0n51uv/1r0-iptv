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

- [x] **Overlay controlli player**: overlay custom per TV al posto dei controlli di default di Media3 (ADR 0007). Sinistra/destra saltano sempre avanti/indietro di un passo fisso con un indicatore visivo; su' porta il focus sul pulsante "Prossimo episodio" (quando c'e' un Episodio in coda), centro conferma o alterna play/pausa. Risolve anche il player bloccato sull'ultimo frame a fine episodio (il `PlayerView` non viene mai ri-agganciato al nuovo `ExoPlayer` quando la coda avanza).
- [x] **Reset Visto granulare**: rimozione del Visto per singolo Episodio, per intera Stagione o per intera Serie, esposta con la stessa pressione lunga già usata sulle card altrove nell'app. Nessun reset globale "cancella tutto". Per Episodio e Stagione, che non avevano ancora nessun menu contestuale, la pressione lunga apre un piccolo menu dedicato (`MenuEpisodio`, `MenuStagione`); per la Serie resta quello già esistente sulle card di Dashboard/Sfoglia.
- [x] **Skeleton sul Dettaglio Film**: come già presente sul Dettaglio Serie, mentre arrivano trama e metadati estesi. Il pannello cast/regista/genere mostra uno scheletro finché il DettaglioEsteso non arriva (solo Sorgenti Xtream); resta vuoto, senza scheletro, per le Sorgenti M3U che non lo forniscono affatto.
- [x] **Cache di sessione sul Dettaglio**: i dati di Stagioni/Episodi di una Serie e i metadati estesi di un Film restano in memoria (`DettaglioCache`) per la sessione corrente, invece di essere ricaricati da zero ogni volta che si torna dal Player. Svuotata ad ogni "Aggiorna catalogo", cosi' una Serie con Nuovi episodi non resta bloccata sui dati vecchi.
- [x] **Copia locale del catalogo**: catalogo testuale persistito su disco (`CatalogoRepository`, ADR 0008), letto subito all'avvio invece di attendere la rete — il fetch dalla Sorgente parte comunque subito dopo e sostituisce la copia locale non appena pronto, stessa logica di "il catalogo vecchio resta a schermo" gia' usata per "Aggiorna catalogo". Il pulsante "Aggiorna catalogo" in Sidebar ruota mentre una sincronizzazione e' in corso, automatica o manuale. Le righe Nuovi episodi/Suggeriti AI/Sport restano legate a un vero fetch di rete (mai alla sola copia locale), per non raddoppiare le chiamate a pagamento o soggette a rate limit ad ogni avvio.
- [ ] **Esclusione dalla sincronizzazione dal Pannello Web**: poter escludere dalla sincronizzazione automatica un'intera Sorgente, una categoria o un singolo contenuto, per alleggerire il caricamento. Non ancora implementato: richiede un nuovo endpoint nel Pannello Web, impostazioni persistite e un filtro nella costruzione del catalogo — scorporato dalla copia locale del catalogo perche' e' un lavoro a se', piu' grosso.
- [x] **Lifecycle app**: la riproduzione si ferma esplicitamente quando l'app va in background (`PlayerScreen` osserva `ON_STOP` del lifecycle, oggi non c'era nessun codice del genere). ~~Riaprendo l'app si torna direttamente sul Player con lo stesso contenuto e l'ultima posizione salvata (`UltimoPlayerRepository`)~~ — ribaltato in Fase 10 dopo l'uso reale: si torna sempre alla Dashboard (vedi ADR 0009), `UltimoPlayerRepository` rimosso. Resta invariato lo stop esplicito della riproduzione in background.

Parcheggiato: **Suggeriti da AI** — cache/rate-limiting delle chiamate e pulsante di rigenerazione manuale, da riprendere dopo aver provato la funzione esistente sul dispositivo reale.

## Fase 10 — Rifiniture e osservabilità dopo il primo uso reale su TV

Nata da una sessione di grilling su bug e richieste raccolte continuando a usare l'app.

- [ ] **Dashboard sempre all'apertura**: mai più dritti sul Player alla riapertura (ribalta la scelta della Fase 9, vedi ADR 0009); la sincronizzazione automatica riparte solo se l'app era stata davvero chiusa (`savedInstanceState == null`), non se si tornava dal background.
- [ ] **Riprendi in Dashboard riproduce subito**: il pulsante hero "Riprendi" avvia la riproduzione direttamente (per un Episodio, attendendo il caricamento della Serie se serve per la coda dei prossimi episodi), senza passare dalla pagina di Dettaglio.
- [ ] **Hover Sidebar più rapido**: l'ombra dietro l'icona a fuoco si accorcia, per non sembrare in ritardo scorrendo veloce tra le sezioni.
- [ ] **Avanzamento sync visibile**: l'icona "Aggiorna catalogo" in Sidebar mostra quante Sorgenti mancano (i/N) invece della sola rotazione continua, sia per la sync automatica al cold-start sia per quella manuale; la schermata di caricamento del primissimo avvio mostra anche il nome della Sorgente in corso.
- [ ] **Carousel episodi non più tagliato**: il Dettaglio Serie non taglia più i bordi delle card degli episodi quando prendono il focus (mancava il `contentPadding` orizzontale sul carousel).
- [ ] **Registro dell'app dalle Impostazioni**: una vista dei log (crash ed errori) raggiungibile dalle Impostazioni, per capire quando e perché l'app si blocca senza dover collegare un computer. Oggi non esiste nessuna infrastruttura di logging: da introdurre da zero.
- [ ] **Alcune Serie non si riproducono (es. One Piece dai Preferiti)**: nessun episodio avvia la riproduzione. Sospetto principale: il riconoscimento `S01E02` nelle Sorgenti M3U accetta solo numeri di episodio fino a 3 cifre, e One Piece è oltre i 1000 episodi — da confermare sul dispositivo prima di dire che è la causa vera.

## Da verificare sulla TV

Il modulo `:app` non è compilabile nell'ambiente in cui è stato scritto (l'Android Gradle
Plugin sta su `dl.google.com`, irraggiungibile lì), quindi la prova sul dispositivo è la
prima vera verifica per: navigazione col D-pad e ripristino del focus, ripresa della
riproduzione, e la fascia sport — di quest'ultima non è stato possibile verificare la forma
esatta delle risposte del provider, e il parsing è scritto per far sparire la fascia invece
di rompere la Dashboard.
