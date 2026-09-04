# Pannello di gestione web integrato nell'app, non un backend separato

Il Pannello Web gira come server HTTP embedded dentro l'app Android TV stessa (stesso processo, stesso database locale), invece di un backend standalone con storage proprio. Scelto per evitare infrastruttura aggiuntiva e logica di sincronizzazione in uno scenario a singola TV; il limite accettato è che il Pannello Web sia raggiungibile solo mentre l'app è aperta e solo dalla rete locale (nessun accesso remoto).
