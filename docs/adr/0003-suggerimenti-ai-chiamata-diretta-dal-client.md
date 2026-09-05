# Suggerimenti generati da una chiamata diretta del client a un'AI esterna

L'app non ha un backend separato (ADR 0001): per generare i Suggerimenti in Dashboard, l'app TV chiama direttamente un'API AI esterna (es. Claude) usando una chiave inserita dall'utente e salvata solo sul dispositivo, invece di introdurre un componente server-side che faccia da proxy. Alla chiamata si inviano solo metadati testuali — titoli/generi dei Visti, dei Preferiti e un campione del catalogo — mai l'intero catalogo, per contenere costo e limiti di contesto.

Trade-off accettato: la chiave AI vive sul device (nessun modo di revocarla o ruotarla centralmente, costo per singolo utente/TV) e ogni installazione instaura la propria integrazione. Un proxy server-side sarebbe più sicuro e centralizzabile, ma richiederebbe l'infrastruttura che l'app evita deliberatamente fin dall'ADR 0001.
