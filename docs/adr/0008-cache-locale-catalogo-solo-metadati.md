# Copia locale del catalogo limitata ai metadati, niente pre-download degli stream

L'avvio a freddo blocca oggi sulla rete prima di mostrare qualunque contenuto. Per velocizzarlo si persiste su disco solo il catalogo testuale (Canali, Film, Serie: nomi, immagini, categorie), letto subito all'avvio e aggiornato in background contattando le Sorgenti. Si è scelto di non estendere la cache ai file audio/video degli stream: la app non introduce un vero download manager né una modalità di riproduzione offline, solo una copia locale dell'elenco.

Trade-off accettato: l'app resta comunque dipendente dalla rete per guardare qualunque contenuto; il vantaggio riguarda solo la velocità con cui il catalogo appare e si naviga, non la disponibilità offline dei contenuti stessi.
