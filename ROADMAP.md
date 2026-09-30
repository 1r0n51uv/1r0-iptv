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
- [x] **Alcune Serie non si riproducono (es. One Piece dai Preferiti)**: confermato sul dispositivo e risolto. La causa reale non era il sospetto originale (regex `S01E02` limitata a 3 cifre) ma un decoder hardware vendor (`OMX.MS.AVC.Decoder` su questa TV Xiaomi/Mediatek) che va in errore irreversibile su quello stream specifico pur inizializzando correttamente: ExoPlayer riprovava all'infinito sullo stesso decoder, schermo nero fisso, nessun messaggio d'errore. `PlayerScreen` ora esclude il decoder appena fallito dal tentativo successivo (fallback al prossimo disponibile, tipicamente software) e non salva più una posizione a zero quando i retry si esauriscono — altrimenti il contenuto mai davvero riprodotto risaliva comunque in cima a "Continua a guardare" solo per l'orario del salvataggio, mascherando contenuti con un avanzamento reale.
- [x] **GIÙ dal pulsante Riprendi non raggiungeva la riga sotto**: la ricerca 2D di focus di default di Compose, da un elemento largo come l'hero dentro una Column scorrevole, non trovava la card sotto (restava ferma sul pulsante). Risolto con un `FocusRequester` esplicito sulla prima card della prima riga con contenuti (`focusProperties { down = ... }`), invece di lasciare indovinare alla ricerca di default.
- [x] **Focus sulla card sbagliata quando lo stesso contenuto compare in più righe**: es. una Serie sia nell'hero "Continua a guardare" sia tra i Preferiti — lo stesso `FocusRequester` condiviso veniva attaccato a entrambe le card, e Compose dava il focus a quella sbagliata (l'ultima composta) subito dopo l'apertura della Dashboard, prima di qualunque tasto premuto (facendo sembrare "bloccato" ogni D-pad successivo). Risolto scegliendo UNA sola riga bersaglio per il ripristino del focus, mai una riga diversa dall'hero se l'hero stesso corrisponde già alla chiave da rifocalizzare.

## Verificato sulla TV

Il modulo `:app` non era compilabile nell'ambiente in cui è stato scritto (l'Android Gradle
Plugin sta su `dl.google.com`, irraggiungibile lì): questa è stata la prima vera prova sul
dispositivo (TV Xiaomi/Mediatek), fatta build via adb e navigazione via `input keyevent`.

- [x] **Ripresa della riproduzione**: verificata su un contenuto con stream funzionante (I Simpson) — riparte esattamente dalla posizione salvata, "Riprendi S1 E1" sul Dettaglio corrisponde, la barra di avanzamento e "Continua a guardare" si aggiornano dopo il ritorno alla Dashboard.
- [x] **Fascia sport**: degrada correttamente senza chiave API impostata ("Nessun evento in diretta al momento. Attiva la chiave API sport dalle Impostazioni."), nessun crash, nessuna rottura della Dashboard — non è stato possibile verificare la forma delle risposte del provider con una chiave reale.
- [x] **Navigazione col D-pad e ripristino del focus**: trovati e risolti due bug reali (dettagli in Fase 10) — GIÙ dal pulsante Riprendi non raggiungeva la riga sotto, e un contenuto presente in più righe (es. hero + Preferiti) faceva atterrare il focus sulla card sbagliata subito dopo l'apertura, prima di qualunque tasto premuto. Navigazione orizzontale nelle righe e nella Sidebar già corretta prima di questi fix.
- [x] **Riproduzione di One Piece dai Preferiti**: bug reale confermato e risolto, vedi Fase 10 — non era il sospetto sulla regex `S01E02`, ma un decoder hardware che falliva su quello stream specifico.

Osservato ma non ancora affrontato: l'app riavvia la sincronizzazione automatica del catalogo
ad ogni riapertura dell'Activity (anche tornando dal background, non solo a un vero cold
start) — coerente con l'item Fase 10 "Dashboard sempre all'apertura" ancora aperto, ha reso
più lenta ogni prova ripetuta sul dispositivo durante questa sessione.

## Fase 11 — Sorgente singola, sincronizzazione visibile e stabilità dopo settimane d'uso reale

Nata da settimane di test di usabilità sul dispositivo reale (Xiaomi/Mediatek `croods`,
Android 11): verificata con `adb` collegato alla TV in rete, inclusi log reali (`logcat`,
`dumpsys dropbox`) e i file dell'app (`sorgenti.json`, `visti.json`) letti da dispositivo.

- [x] **Una sola Sorgente configurabile**: `SorgenteRepository.aggiungi` ora sostituisce la
  Sorgente esistente invece di accumularle; il Pannello Web mostra "Sorgente" al singolare,
  "+ Aggiungi" sparisce a favore di "Sostituisci Sorgente" appena una e' configurata.
- [x] **Forza sincronizzazione dal Pannello Web**: nuova rotta `POST /sincronizza`, che passa
  da `PonteTv` (stesso schema di "Riproduci sulla TV", ADR 0005) a un contatore osservato dalla
  TV — stesso effetto del tocco su "Aggiorna catalogo" in Sidebar.
- [x] **Sincronizzazione Xtream fallita in silenzio**: confermato sul dispositivo — la Sorgente
  configurata (account Xtream reale) falliva con HTTP 521 (server del provider giu'), ma
  `catalogoDaXtream` avvolge ogni chiamata in un `tryOrEmpty` senza log, quindi l'esito era un
  catalogo vuoto senza traccia ne' nel Registro dell'app ne' altrove. La prima chiamata
  (`get_live_categories`) ora non e' piu' protetta: un fallimento risale, viene registrato e
  compare nel Pannello Web ("Ultima sincronizzazione: Fallita alle ..."), con l'URL ripulito da
  username/password prima di comparire li'.
- [x] **Crash reale durante la navigazione col D-pad**: confermato da `dumpsys dropbox`
  (`data_app_crash`) — `IllegalStateException: FocusRequester is not initialized` sollevata da
  Compose stesso durante `focusSearch`, per una race sul `FocusRequester` condiviso che
  `Sidebar` riattacca a un'icona diversa ad ogni cambio Destinazione. `MainActivity` ora
  sovrascrive `dispatchKeyEvent` e ignora solo questa specifica eccezione (consumando il tasto),
  senza mascherare altri crash.
- [x] **Jank periodico durante la riproduzione**: osservato in `logcat` un frame da oltre un
  secondo proprio durante la visione. Causa: il salvataggio della posizione ogni 10 secondi
  (`VistoRepository.registraProgresso`, fino a 200 Visti su disco) girava sul thread di UI dentro
  `PlayerScreen`. Il salvataggio periodico ora passa per `Dispatchers.IO`; il salvataggio finale
  (cambio schermata) resta sincrono per non introdurre un'altra corsa con la lettura dei Visti in
  Dashboard.
- [x] **L'ultimo guardato non sempre prende il posto nella card di ripresa**: confermato dal
  `visti.json` reale del dispositivo — un Episodio appena finito (99.9% della durata) veniva
  escluso da "Continua a guardare" da `completato()`, facendo ricomparire in cima un contenuto
  guardato ore prima. `RegistroVisti.continuaAGuardare` ora esclude i completati solo per i Film
  (che non hanno un seguito): una Serie appena finita resta la card di ripresa, e aprirla richiama
  gia' `ProssimaVisioneResolver`, che calcola da solo l'Episodio successivo.
- [x] **Pannello Web piu' responsivo**: una riga di 3-4 pulsanti in testa alla pagina Sorgente
  (Impostazioni, Riproduci sulla TV, Forza sincronizzazione, Sostituisci Sorgente) sfondava lo
  schermo su un telefono stretto invece di andare a capo; un URL o un messaggio d'errore lungo
  (senza spazi, quindi non a capo da solo) nella colonna "Ultima sincronizzazione" sfondava la
  card verso destra per lo stesso motivo. Entrambi corretti con CSS (`flex-wrap`, `overflow-wrap`).
- [x] **Errori di sincronizzazione propagati come Toast in app**: un fallimento (vedi il punto
  sopra su Xtream) prima era visibile solo nel Registro dell'app o nel Pannello Web — mai sulla
  TV stessa. Ora `MainActivity` mostra un Toast quando una Sorgente fallisce, sia per la sync
  automatica sia per quella forzata dal Pannello Web.
- [x] **OutOfMemoryError reale passando a una Sorgente M3U**: scoperto verificando i punti sopra
  sul dispositivo (l'utente ha sostituito la Sorgente Xtream con l'M3U dello stesso account) —
  `ContentFetcher.scarica` leggeva l'intera playlist M3U in un'unica String (centinaia di MB per
  un provider reale con panel), lo stesso rischio gia' evitato per Xtream con `streamJsonArray`
  (ADR-meno formale, ma stesso principio) ma mai applicato a M3U. `M3uParser.parse` accetta ora
  anche una `Sequence<String>`, e `ContentFetcher` scarica e la passa riga per riga dallo stream
  di rete (`scaricaRighe`), senza mai avere l'intera playlist in memoria in una volta sola.

## Fase 12 — Preferiti/Visti persi, uscita dall'app e pulizia della cronologia

Nata continuando a usare l'app dopo la Fase 11: il provider di questo account ruota
periodicamente username/password (a volte anche l'host) restando lo stesso streaming, e questo
ha fatto emergere un problema strutturale mai notato prima.

- [x] **Preferiti e Visti persi dopo un cambio di credenziali Xtream**: causa reale confermata
  confrontando `personalizzazioni.json`/`visti.json` col nuovo host — la Chiave di Identita' di
  Canali, Film ed Episodi Xtream era l'URL completo dello stream, che incorpora host/username/
  password e quindi cambia ad ogni rotazione, orfanizzando tutto cio' che era salvato con la
  Chiave vecchia. La nuova Chiave (`ChiaveIdentitaXtream`, dominio) usa l'id numerico dello
  stream, stabile a prescindere da host/credenziali; le vecchie Chiavi gia' su disco vengono
  riconosciute e riscritte al volo alla lettura (`VistoRepository`, `PersonalizzazioneRepository`),
  senza bisogno di un passaggio di migrazione esplicito. Le Sorgenti M3U, senza un id migliore
  dell'URL, restano come prima (stesso limite di sempre, non introdotto da questo fix).
- [x] **Catalogo azzerato da una sincronizzazione fallita**: scoperto verificando il punto sopra
  sul dispositivo — una Sorgente irraggiungibile restituisce un catalogo vuoto, e prima quel
  vuoto sovrascriveva senza condizioni sia lo schermo sia la copia locale (ADR 0008) anche
  quando c'era gia' un catalogo buono, trasformando un problema di rete transitorio in "nessun
  contenuto" fino alla sincronizzazione successiva. Ora un fallimento che risulterebbe in un
  catalogo vuoto non tocca ne' schermo ne' copia locale quando c'e' gia' qualcosa di buono da
  tenere (il Toast della Fase 11 resta l'unico segnale); al primissimo avvio, senza nessuna copia
  locale, il risultato si mostra comunque, vuoto o no.
- [x] **Nessun modo di uscire con conferma**: Back sulla Dashboard (radice dell'app) non faceva
  nulla di proposito, per non rischiare un dialogo di sistema "Uscire dall'app?" che in realta',
  verificato sul dispositivo reale, questa Android TV (Xiaomi/Mediatek) non mostra affatto: Back
  finiva l'Activity in silenzio, senza nessuna conferma. Aggiunto un dialogo di conferma proprio
  dell'app ("Uscire dall'app?", focus di default su "Annulla" per non uscire per sbaglio),
  verificato sul dispositivo con screenshot: compare su Back, un secondo Back lo annulla senza
  uscire, confermare "Esci" chiude davvero l'Activity.
- [x] **Segna come non vista un'intera Serie**: Episodio e Stagione avevano gia' l'azione (Fase
  9, menu a pressione lunga nel Dettaglio Serie); per l'intera Serie esisteva solo "Togli da
  Continua a guardare" sulla card, e solo quando la Serie vi compariva gia'. Aggiunto un pulsante
  "Segna serie come non vista" direttamente sulla pagina di Dettaglio Serie (accanto a
  Preferiti), visibile appena c'e' almeno un Episodio visto: utile per un rewatch o per pulizia
  senza dover prima trovare la card giusta altrove.

## Fase 13 — Sorgente cambiata a runtime ignorata, e playlist M3U reali lente

Nata da un caso reale: il provider dell'account ha smesso di rispondere su `player_api.php`
(Xtream) a meta' giornata, l'utente ha sostituito la Sorgente con l'M3U dello stesso account dal
Pannello Web, ma l'app ha continuato a fallire esattamente come prima. Il player_api.php? Si':
confermato con una chiamata diretta (`HTTP 521`, Cloudflare "server giu'"), mentre lo stesso
account su `get.php` (M3U-plus) rispondeva regolarmente.

- [x] **Una Sorgente cambiata dal Pannello Web restava ignorata finche' l'app non si riavviava**:
  causa reale trovata in `MainActivity` — il poll di `sorgenti.json` si fermava per sempre alla
  prima lettura non vuota (`while (sorgenti.isEmpty())`), quindi sostituire o modificare la
  Sorgente a runtime (esattamente il caso reale: da Xtream rotto a M3U funzionante) non aveva
  alcun effetto - la sincronizzazione ripeteva la vecchia Sorgente all'infinito. Il poll ora
  continua per tutta la vita dell'app e aggiorna lo stato solo quando la lista e' davvero
  cambiata (confrontata per valore), verificato sul dispositivo: la Sorgente nuova viene presa
  in carico entro un paio di secondi, senza dover chiudere e riaprire l'app.
- [x] **Parsing M3U lentissimo su una playlist reale grande**: scoperto verificando il punto
  sopra — una playlist da 75MB (account con panel reale, migliaia di Canali) impiegava diversi
  minuti a essere elaborata. Causa: `M3uParser` compilava tre `Regex` nuove (tvg-id, tvg-logo,
  group-title) per ogni singola riga invece che una volta sola, moltiplicato per decine di
  migliaia di righe. Precompilate come costanti condivise: stesso comportamento, molto piu'
  veloce - verificato sul dispositivo con la stessa playlist da 75MB, sincronizzazione completata
  senza errori ne' rallentamenti eccessivi.

## Fase 14 — Preferiti/Visti riagganciati dopo un cambio di formato Sorgente

Nata dallo stesso caso della Fase 13: passando dall'account Xtream all'M3U dello stesso
provider, Preferiti e Visti di Canali/Film sono spariti. Non un crash: un limite strutturale
della Chiave di Identita', mai affrontato prima.

- [x] **Chiave di Identita' stabile anche cambiando formato Sorgente**: `ContentCard.Canale`/
  `Film` ora preferiscono il `tvg-id` (M3U) / `epg_channel_id` (Xtream) - un id assegnato dal
  provider stesso, indipendente sia dall'URL sia dal formato con cui e' configurata la Sorgente -
  all'id-stream Xtream della Fase 13, che a sua volta resta il ripiego prima dell'URL grezzo.
  `ContentFetcher` gia' portava questo id fino a `M3uEntry` (usato altrove per l'IdentityKeyResolver
  mai collegato), ma lo scartava costruendo la card finale: ora non piu'. Vale per le Sorgenti
  future; non recupera da solo Preferiti/Visti gia' orfani prima di questo fix (serve comunque la
  riconciliazione per titolo sotto).
- [x] **Riconciliazione per titolo di Preferiti e Visti dopo ogni sincronizzazione riuscita**: un
  Visto porta gia' il titolo del contenuto; un Preferito no (aggiunto ora - una Personalizzazione
  precedente a questo fix non ha nulla da abbinare e resta orfana, unico limite non risolvibile
  retroattivamente). Dopo ogni sync, `RiconciliazioneVisti`/`RiconciliazionePersonalizzazioni`
  (dominio, con test) cercano nel catalogo fresco un contenuto con lo stesso titolo per ogni
  Chiave ormai sparita, e riagganciano solo quando ce n'e' uno solo - altrimenti niente, per non
  agganciare per sbaglio al contenuto sbagliato. Verificato sul dispositivo con un Film reale
  gia' orfano: nessun Visto ne' Preferito perso durante la riconciliazione (125 Visti, tutti
  preservati) anche quando il titolo non trova corrispondenza nel catalogo M3U (es. Serie assenti
  da quel provider in formato M3U).

## Fase 15 — Buffering visibile, sincronizzazione piu' onesta, dropdown Stagioni centrato

Implementata ma non ancora verificata sul dispositivo: build e test passano, l'APK e' pronto,
in attesa del via libera per l'installazione (l'utente testa lui stesso i tempi).

- [ ] **Percentuale di buffering nel Player**: uno streaming che si blocca durante la visione
  restava senza nessun segnale a schermo del perche' (schermo semplicemente fermo). `PlayerScreen`
  ora mostra un overlay con la percentuale (`ExoPlayer.bufferedPercentage`) quando lo stato e'
  `STATE_BUFFERING` - sia al caricamento iniziale sia per un ribuffering a meta' visione, stesso
  stato in entrambi i casi. Anello indefinito finche' la percentuale non e' ancora un numero
  sensato (resta a 0 finche' non si e' scaricato nulla).
- [ ] **Icona di sincronizzazione che riflette l'avanzamento reale**: con una sola Sorgente
  configurabile (Fase 11) l'anello indice/totale saltava subito al 100% e ci restava per l'intera
  sincronizzazione, invece di riempirsi con l'avanzamento vero. `ContentFetcher` ora riporta un
  progresso frazionale: a byte scaricati (Content-Length del server) per M3U, a passi completati
  (le tre liste di categorie piu' le tre di contenuti) per Xtream. L'icona in Sidebar mostra
  quella frazione, smussata con `animateFloatAsState`.
- [ ] **Dropdown Stagioni centrato sulla Stagione aperta**: aprendolo dalla decima Stagione di
  venti, il dropdown partiva sempre scrollato in cima invece che sulla voce gia' scelta. Ora si
  centra da solo sulla Stagione corrente all'apertura, usando il parametro `scrollState` di
  `DropdownMenu` (Material3) invece di lasciare lo scroll di default.
