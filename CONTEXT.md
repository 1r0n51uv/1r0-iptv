# 1r0-iptv

App IPTV personalizzata per Android TV (Live TV + Film + Serie), con un pannello di gestione web integrato per configurare Sorgenti e contenuti senza usare il telecomando.

## Language

### Sorgenti e contenuti

**Sorgente**:
Una fonte di contenuti configurata dall'utente: una playlist M3U/M3U8 raggiungibile via URL, oppure un account Xtream Codes (host, username, password). Una Sorgente Xtream fornisce Canali, Film e Serie come tipi distinti e affidabili; una Sorgente M3U fornisce sempre Canali, con Film e Serie riconosciuti tramite euristica sul `group-title` (vedi ADR 0002). L'utente può configurare più Sorgenti contemporaneamente.
_Avoid_: Playlist, provider, lista

**Canale**:
Una voce Live TV riproducibile in diretta, con nome, logo, categoria ed eventuale Programma.
_Avoid_: Stream, voce, canale TV

**Programma**:
Una voce del palinsesto di un Canale (titolo, orario di inizio/fine), ottenuta dalle Sorgenti Xtream tramite le loro API EPG. Le Sorgenti M3U non hanno oggi un Programma associato (vedi Guida TV nella roadmap).
_Avoid_: Evento EPG, programma TV

**Film**:
Un contenuto VOD a riproduzione singola (non live), con eventuale posizione di ripresa salvata (vedi Visto).
_Avoid_: VOD, movie

**Serie**:
Un contenuto organizzato in Stagioni ed Episodi.

**Stagione**:
Un raggruppamento numerato di Episodi all'interno di una Serie. Da Sorgenti M3U può esistere una Stagione "senza numero" che raccoglie gli Episodi il cui titolo non rispetta il pattern atteso (es. `S01E02`).

**Episodio**:
Una singola voce riproducibile all'interno di una Serie, con eventuale posizione di ripresa salvata (vedi Visto).

### Gestione e personalizzazione

**Pannello Web**:
Interfaccia di configurazione raggiungibile via browser da un dispositivo sulla stessa rete locale della Android TV, servita da un server HTTP integrato nell'app stessa (nessun backend separato, nessuna autenticazione). Usata per gestire Sorgenti, Canali, Film e Serie, e per avviare la riproduzione di un contenuto direttamente sulla Android TV (vedi ADR 0005).
_Avoid_: Web panel, dashboard, backend

**Preferito**:
Un Canale, Film o Serie contrassegnato dall'utente per accesso rapido, in una lista unica che mescola tutti i tipi di contenuto.

**Personalizzazione**:
Impostazioni locali applicate dall'utente a un Canale/Film/Serie che non derivano dalla Sorgente: nascosto, Preferito e, solo per voci provenienti da Sorgenti M3U, il tipo riassegnato manualmente quando l'euristica sbaglia. Resta agganciata alla voce tramite la sua Chiave di Identità, quindi sopravvive ai refresh della Sorgente.
_Avoid_: Override, flag

**Chiave di Identità**:
L'identificatore stabile di un Canale/Film/Episodio tra un refresh e l'altro della Sorgente: il `tvg-id` se presente, altrimenti l'URL dello stream. Una Serie, che non ha un URL proprio, è identificata dal suo nome. Usata per riagganciare le Personalizzazioni e i Visti dopo un aggiornamento della Sorgente.

### Cronologia, scoperta e navigazione

**Visto**:
Un Film o Episodio che l'utente ha aperto nel player almeno una volta, con l'ultima posizione di riproduzione raggiunta. Creato automaticamente all'apertura, indipendente dal Preferito. I Canali non generano un Visto, essendo live e senza posizione da riprendere (vedi ADR 0004).
_Avoid_: Cronologia, storico, watched

**Dashboard**:
La schermata di atterraggio dell'app TV, costruita sui Visti da riprendere, sulla sezione Nuovi episodi e su un eventuale Suggerimento o sport live in evidenza. Sostituisce la precedente Home basata su righe Canali/Film/Serie, che diventa una destinazione di sfoglia separata.
_Avoid_: Home, schermata iniziale

**Sidebar**:
La barra di navigazione persistente dell'app TV, che dà accesso a Dashboard, Guida TV, Cerca, Preferiti e Impostazioni senza passare da un'unica schermata scrollabile.
_Avoid_: Menu, drawer

**Nuovi episodi**:
Sezione della Dashboard che elenca gli episodi arrivati con l'ultimo refresh di una Sorgente, limitata alle Serie con almeno un Visto o un Preferito.
_Avoid_: Notifica, notifica push

**Suggerimento**:
Un Film, Serie o Canale evidenziato in Dashboard da una chiamata a un'AI esterna, generato a partire dai Visti, dai Preferiti e da un campione del catalogo (vedi ADR 0003).
_Avoid_: Raccomandazione, consiglio

**Contenuto di default**:
Il contenuto configurabile su cui la Dashboard porta il focus all'avvio a freddo quando non esiste ancora un Visto o un'ultima riproduzione.
_Avoid_: Fallback, contenuto iniziale
