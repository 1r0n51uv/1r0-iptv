# Classificazione Film/Serie su Sorgenti M3U tramite euristica sul group-title

Il formato M3U non ha un campo "tipo" strutturato: a differenza di Xtream Codes, non c'è modo affidabile di sapere se una voce è un Canale live, un Film o un episodio di Serie. Abbiamo scelto comunque di classificarle euristicamente in base al `group-title` (contiene "film"/"movie"/"vod" → Film, contiene "serie"/"series" → Serie, altrimenti Canale) e di raggruppare gli Episodi di Serie tramite pattern `SxxExx` nel titolo, invece di limitare Film/Serie alle sole Sorgenti Xtream.

Trade-off accettato: la classificazione può sbagliare (falsi positivi/negativi), quindi introduciamo la Personalizzazione "tipo riassegnato manualmente" dal Pannello Web come via di correzione. Le voci che non rispettano il pattern `SxxExx` dentro un gruppo Serie restano nella Serie come episodi senza numero, invece di essere scartate o retrocesse a Film/Canale.
