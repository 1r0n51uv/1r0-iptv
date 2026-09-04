# 1r0-iptv

App IPTV personalizzata per Android TV (Live TV + Film + Serie), con un pannello di gestione web integrato per configurare Sorgenti e contenuti senza usare il telecomando.

## Language

### Sorgenti e contenuti

**Sorgente**:
Una fonte di contenuti configurata dall'utente: una playlist M3U/M3U8 raggiungibile via URL, oppure un account Xtream Codes (host, username, password). Una Sorgente Xtream fornisce Canali, Film e Serie come tipi distinti e affidabili; una Sorgente M3U fornisce sempre Canali, con Film e Serie riconosciuti tramite euristica sul `group-title` (vedi ADR 0002). L'utente può configurare più Sorgenti contemporaneamente.
_Avoid_: Playlist, provider, lista

**Canale**:
Una voce Live TV riproducibile in diretta, con nome, logo, categoria ed eventuale Programma EPG.
_Avoid_: Stream, voce, canale TV

**Film**:
Un contenuto VOD a riproduzione singola (non live), con eventuale posizione di ripresa salvata.
_Avoid_: VOD, movie

**Serie**:
Un contenuto organizzato in Stagioni ed Episodi.

**Stagione**:
Un raggruppamento numerato di Episodi all'interno di una Serie. Da Sorgenti M3U può esistere una Stagione "senza numero" che raccoglie gli Episodi il cui titolo non rispetta il pattern atteso (es. `S01E02`).

**Episodio**:
Una singola voce riproducibile all'interno di una Serie, con eventuale posizione di ripresa salvata.

### Gestione e personalizzazione

**Pannello Web**:
Interfaccia di configurazione raggiungibile via browser da un dispositivo sulla stessa rete locale della Android TV, servita da un server HTTP integrato nell'app stessa (nessun backend separato, nessuna autenticazione). Usata per gestire Sorgenti, Canali, Film e Serie.
_Avoid_: Web panel, dashboard, backend

**Preferito**:
Un Canale, Film o Serie contrassegnato dall'utente per accesso rapido, in una lista unica che mescola tutti i tipi di contenuto.

**Personalizzazione**:
Impostazioni locali applicate dall'utente a un Canale/Film/Serie che non derivano dalla Sorgente: nascosto, Preferito e, solo per voci provenienti da Sorgenti M3U, il tipo riassegnato manualmente quando l'euristica sbaglia. Resta agganciata alla voce tramite la sua Chiave di Identità, quindi sopravvive ai refresh della Sorgente.
_Avoid_: Override, flag

**Chiave di Identità**:
L'identificatore stabile di un Canale/Film/Episodio tra un refresh e l'altro della Sorgente: il `tvg-id` se presente, altrimenti l'URL dello stream. Usata per riagganciare le Personalizzazioni dopo un aggiornamento della Sorgente.
