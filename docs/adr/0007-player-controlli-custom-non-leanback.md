# Controlli del player custom per TV, non i controlli di default Media3

Il player usa oggi un `PlayerView` di Media3 senza alcuna personalizzazione, con i controlli di default della libreria (pensati per il tocco, non per il telecomando). Per gestire in modo esplicito il D-pad — seek con sinistra/destra, un comando "prossimo episodio" nella barra di riproduzione, cambio di episodio in coda a fine riproduzione — si costruisce un overlay di controlli custom invece di adottare l'estensione leanback di Media3 o di continuare a personalizzare i controlli di default.

Trade-off accettato: più codice UI da scrivere e mantenere rispetto a usare controlli pronti della libreria, in cambio del controllo pieno sulla semantica del D-pad e sulla coda di episodi, che l'app richiede e che i controlli di default non offrono.
