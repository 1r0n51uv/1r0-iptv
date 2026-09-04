# 1r0 IPTV

App IPTV personalizzata per Android TV (Kotlin, Compose for TV, Media3), con un pannello di gestione web integrato per configurare Sorgenti (playlist M3U o account Xtream Codes) senza usare il telecomando.

Vedi [`CONTEXT.md`](CONTEXT.md) per il glossario di dominio e [`docs/adr/`](docs/adr/) per le decisioni architetturali.

## Struttura del progetto

- `app/` — app Android TV (Compose, Media3, pannello web embedded). Al momento contiene solo uno scaffold "Hello World" per validare il toolchain su un dispositivo reale; le schermate vere arriveranno modulo per modulo.
- `domain/` — logica di dominio pura (Kotlin/JVM, nessuna dipendenza Android), sviluppata in TDD: parsing M3U, classificazione Canale/Film/Serie, raggruppamento Stagioni/Episodi, chiave di identità e merge delle Personalizzazioni, mapping Xtream Codes.
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

### 3. Compila l'app Android TV

Richiede l'SDK Android installato (via Android Studio → SDK Manager) e `ANDROID_HOME`/`local.properties` configurati:

```
./gradlew :app:assembleDebug
```

L'APK debug viene generato in `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Installa sulla tua Android TV via rete (ADB)

Procedura verificata su **Android 11** (compatibile anche con altre versioni ≥ API 21):

**Sulla TV:**
1. Impostazioni → Preferenze dispositivo → Informazioni → tocca ripetutamente la voce "Build" finché non compare "Sei ora uno sviluppatore"
2. Torna indietro → Preferenze dispositivo → Opzioni sviluppatore
3. Abilita "Debug USB" e "Debug ADB via rete" (nomi esatti leggermente diversi a seconda del produttore)
4. Annota l'indirizzo IP della TV, mostrato in Opzioni sviluppatore → Debug ADB via rete (o in Rete e Internet → stato connessione)

**Dal computer, sulla stessa rete Wi-Fi:**

```
adb connect <ip-tv>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

In alternativa, da Android Studio: seleziona la TV come dispositivo di destinazione (comparirà nell'elenco device una volta connessa via `adb connect`) e premi "Run".

L'app compare nella riga delle app del launcher Android TV (grazie al banner dichiarato in `AndroidManifest.xml`) con il titolo "1r0 IPTV", e all'apertura mostra "Hello, 1r0 IPTV!".

## Compatibilità

`minSdk` 21, `compileSdk`/`targetSdk` 34: compatibile con Android 11 (API 30) e versioni successive di Android TV.

## Nota sull'ambiente di sviluppo

Se stai continuando lo sviluppo in un ambiente sandbox senza accesso al repository Maven di Google (`dl.google.com`), `:app` non può essere compilato: `./gradlew :domain:test` funziona comunque in isolamento grazie a `org.gradle.configureondemand=true` in `gradle.properties`, che evita di configurare `:app` quando non richiesto.
