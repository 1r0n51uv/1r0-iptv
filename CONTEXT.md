# 1r0-iptv

App IPTV personalizzata per Android TV, con un pannello di gestione web integrato per configurare canali e sorgenti senza usare il telecomando.

## Language

**Sorgente**:
Una fonte di canali configurata dall'utente: una playlist M3U/M3U8 raggiungibile via URL, oppure un account Xtream Codes (host, username, password). L'utente può configurare più Sorgenti contemporaneamente.
_Avoid_: Playlist, provider, lista

**Canale**:
Una singola voce riproducibile (stream) appartenente a una Sorgente, con nome, logo e categoria.
_Avoid_: Stream, voce, canale TV

**Pannello Web**:
Interfaccia di configurazione raggiungibile via browser da un dispositivo sulla stessa rete locale della Android TV, servita da un server HTTP integrato nell'app stessa (nessun backend separato). Usata per gestire Sorgenti e Canali.
_Avoid_: Web panel, dashboard, backend

**Preferito**:
Un Canale contrassegnato dall'utente per accesso rapido nell'app TV.
