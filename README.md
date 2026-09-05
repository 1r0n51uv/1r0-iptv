# 1r0 IPTV

App IPTV personalizzata per Android TV (Kotlin, Compose for TV, Media3), con un pannello di gestione web integrato per configurare Sorgenti (playlist M3U o account Xtream Codes) senza usare il telecomando.

Vedi [`CONTEXT.md`](CONTEXT.md) per il glossario di dominio, [`docs/adr/`](docs/adr/) per le decisioni architetturali e [`ROADMAP.md`](ROADMAP.md) per lo stato delle funzionalità.

## Struttura del progetto

- `app/` — app Android TV (Compose, Media3, pannello web embedded): Sidebar, Dashboard, Dettaglio, Guida TV, Cerca, Preferiti, Impostazioni e player con ripresa.
- `domain/` — logica di dominio pura (Kotlin/JVM, nessuna dipendenza Android), sviluppata in TDD: parsing M3U, classificazione Canale/Film/Serie, raggruppamento Stagioni/Episodi, chiave di identità e merge delle Personalizzazioni, mapping Xtream Codes, Visti e ripresa, righe della Dashboard, ricerca, EPG e scelta delle partite in evidenza.
- `design/wireframe/` — sorgenti del wireframe navigabile delle schermate (app TV + pannello web).

## Requisiti

- JDK 17 o superiore
- Android Studio (consigliata l'ultima versione stabile), oppure Android SDK command-line con `ANDROID_HOME` configurato
- Per testare su device reale: una Android TV con Android 5.0 (API 21) o superiore

## Setup

### 1. Apri il progetto

```
git clone <repo-url>
cd 1r0-iptv
```

Aprendo la cartella in Android Studio i moduli `:app` e `:domain` vengono rilevati automaticamente.

### 2. Esegui i test del dominio

Il modulo `:domain` è puro Kotlin/JVM e non richiede l'SDK Android:

```
./gradlew :domain:test
```

### 3. Apri, configura e compila in Android Studio

1. **Installa Android Studio** (consigliata l'ultima versione stabile — Ladybug o successiva; deve supportare AGP 8.6.x e Kotlin 2.0.x, entrambi già configurati nel progetto).
2. **Apri il progetto**: File → Open… → seleziona la cartella `1r0-iptv` clonata. Non serve creare nulla da zero: Android Studio riconosce `settings.gradle.kts` e importa automaticamente sia `:app` sia `:domain`.
3. **Attendi la sincronizzazione Gradle** (barra di stato in basso): al primo avvio scarica l'Android Gradle Plugin, Kotlin e le dipendenze Compose — richiede connessione internet verso i repository Google/Maven Central.
4. **Se richiesto, installa l'SDK Android**: se in basso compare un banner "SDK not found" o simile:
   - Tools → SDK Manager
   - Nella scheda "SDK Platforms" seleziona **Android 14.0 ("UpsideDownCake"), API 34** (il `compileSdk`/`targetSdk` del progetto) e spunta la casella
   - Nella scheda "SDK Tools" verifica che sia installato "Android SDK Build-Tools" (versione più recente) e "Android SDK Platform-Tools" (contiene `adb`)
   - Applica: l'installazione richiede qualche minuto
   - Se compare "Android Licenses not accepted", accetta le licenze dalla stessa finestra
5. **Verifica la configurazione di run**: in alto, accanto al pulsante ▶️ Run, il dropdown della configurazione deve mostrare `app` (viene creata automaticamente al primo sync; se manca: Run → Edit Configurations… → + → Android App → Module: `app`).
6. **Compila senza eseguire** (facoltativo, solo per verificare che tutto compili): Build → Make Project (Ctrl+F9 / Cmd+F9), oppure da terminale integrato:
   ```
   ./gradlew :app:assembleDebug
   ```
   L'APK debug viene generato in `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Collega la tua Android TV e avviala da Android Studio

Sulla TV serve prima abilitare le opzioni sviluppatore (procedura verificata su **Android 11**, compatibile con altre versioni ≥ API 21):

1. Impostazioni → Preferenze dispositivo → Informazioni → tocca ripetutamente la voce "Build" finché non compare "Sei ora uno sviluppatore"
2. Torna indietro → Preferenze dispositivo → Opzioni sviluppatore
3. Abilita "Debug USB"

Da qui, due strade per collegarla al PC (stessa rete Wi-Fi di TV e computer):

**A. Wireless debugging nativo (se la tua TV lo espone, tipico su Android 11+ con interfaccia Google TV)**
1. Nelle Opzioni sviluppatore della TV cerca "Debug wireless"/"Wireless debugging" → attivalo → "Abbina dispositivo con codice di associazione" (mostra IP, porta e codice a 6 cifre)
2. In Android Studio: apri Device Manager (icona telefono nella barra laterale destra, o View → Tool Windows → Device Manager) → menu "Pair devices using Wi-Fi" → "Pair using QR code" oppure inserisci manualmente IP:porta e codice mostrati dalla TV
3. Una volta abbinata, la TV compare nel dropdown dei device in alto in Android Studio

**B. `adb connect` manuale (se la TV mostra un IP in "Debug ADB via rete" / "Network debugging" ma non un flusso di pairing)**
1. Annota l'IP della TV da Opzioni sviluppatore → Debug ADB via rete (o Rete e Internet → stato connessione)
2. Da terminale (anche quello integrato in Android Studio):
   ```
   adb connect <ip-tv>:5555
   ```
3. La TV compare nel dropdown dei device in Android Studio (potrebbe servire cliccare l'icona di refresh accanto al dropdown)

**Esegui l'app:**
1. Seleziona la TV connessa dal dropdown dei device in alto (accanto alla configurazione `app`)
2. Premi ▶️ Run (o Shift+F10): Android Studio compila, installa e avvia automaticamente `MainActivity` sulla TV
3. Sulla TV dovresti vedere "Hello, 1r0 IPTV!" su sfondo scuro; l'app resta installata e compare anche nella riga delle app del launcher Android TV (grazie al banner dichiarato in `AndroidManifest.xml`) con il titolo "1r0 IPTV"

**In alternativa senza Android Studio**, dopo aver compilato con `./gradlew :app:assembleDebug` e collegato la TV come sopra:
```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Compatibilità

`minSdk` 21, `compileSdk`/`targetSdk` 34: compatibile con Android 11 (API 30) e versioni successive di Android TV.

## Nota sull'ambiente di sviluppo

Se stai continuando lo sviluppo in un ambiente sandbox senza accesso al repository Maven di Google (`dl.google.com`), `:app` non può essere compilato: `./gradlew :domain:test` funziona comunque in isolamento grazie a `org.gradle.configureondemand=true` in `gradle.properties`, che evita di configurare `:app` quando non richiesto.
