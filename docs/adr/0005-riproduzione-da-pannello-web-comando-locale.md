# Riproduzione da Pannello Web come comando locale, non un protocollo di cast

Il Pannello Web (config-only per ADR 0001, stesso processo dell'app TV) guadagna un'azione "Riproduci sulla TV": il browser invia un comando HTTP al server già in esecuzione nell'app, che apre direttamente il Player sul contenuto scelto, invece di implementare un protocollo di cast/streaming di rete generico. Funziona solo perché Pannello Web e app TV vivono sempre sullo stesso dispositivo/processo.

Trade-off accettato: non è possibile avviare un contenuto su una Android TV diversa da quella che serve il Pannello Web da cui si naviga. Servirebbe un vero protocollo di cast se in futuro si vorrà controllare più TV dallo stesso Pannello Web o da un dispositivo diverso.
