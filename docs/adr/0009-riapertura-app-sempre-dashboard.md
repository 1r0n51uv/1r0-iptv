# Riapertura dell'app sempre sulla Dashboard, mai dritta sul Player

La Fase 9 (Lifecycle app) aveva scelto di riaprire l'app direttamente sul Player con l'ultimo contenuto e l'ultima posizione, per non far perdere il filo a chi era stato interrotto mentre guardava qualcosa. Con l'uso reale la scelta si è rivelata sbagliata: l'utente si aspetta comunque di ripartire dalla Dashboard, anche dopo essere stato interrotto, e usa il pulsante "Riprendi" per rientrare nel contenuto quando vuole.

Si ribalta quindi la decisione della Fase 9: l'app torna sempre alla Dashboard all'apertura, indipendentemente da cosa fosse aperto quando è andata in background. `UltimoPlayerRepository`, che esisteva solo per riaprire dritti sul Player, viene rimosso. La posizione di ripresa non si perde: resta salvata tramite il Visto come già accadeva, cambia solo dove l'utente atterra.

Trade-off accettato: chi viene interrotto a metà episodio deve ripassare dalla Dashboard e premere Riprendi invece di ritrovarsi già nel Player. Si è scelta la prevedibilità (si parte sempre dallo stesso posto) alla scorciatoia.
