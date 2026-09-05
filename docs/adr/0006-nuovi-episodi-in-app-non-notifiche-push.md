# Nuovi episodi come sezione in-app, non notifiche di sistema

Le Android TV non offrono un modo affidabile di eseguire lavoro in background quando l'app è chiusa, e l'app non ha un componente server-side che potrebbe fare da mittente push (ADR 0001). Invece di una notifica di sistema, i nuovi episodi vengono mostrati come sezione "Nuovi episodi" in Dashboard, calcolata confrontando l'ultimo refresh di una Sorgente con l'ultima visita, e limitata alle Serie con almeno un Visto o un Preferito.

Trade-off accettato: l'utente scopre i nuovi episodi solo riaprendo l'app, non mentre la TV è spenta o l'app è in background.
