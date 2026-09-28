# 4. AI Act, normativa italiana e Intelligenza Artificiale nella PA

> **Aggiornato al 28 settembre 2026.** Questo file separa i concetti tecnici dalle regole giuridiche: nei quiz è essenziale non confondere i due piani.
>
> Un sistema tecnicamente possibile o accurato non è automaticamente lecito, appropriato o utilizzabile per adottare una decisione amministrativa.

---

# PARTE I — CONCETTI TECNICI

## 1. CHE COS'È

Un sistema di IA può creare rischi di tre tipi:

- **tecnici**: errori, allucinazioni, perdita di accuratezza nel tempo (*drift*), indisponibilità, attacchi informatici;
- **per le persone e i diritti**: discriminazione, violazione della privacy, esclusione digitale, decisioni non contestabili;
- **organizzativi**: nessuno controlla il sistema, documentazione insufficiente, affidamento cieco dell'operatore sull'output.

L'AI Act adotta quindi un approccio **basato sul rischio**: gli obblighi aumentano quando il possibile impatto su salute, sicurezza e diritti fondamentali è più elevato.

## 2. A COSA SERVE la governance dell'IA

La governance serve a rispondere, prima e durante l'uso, a domande pratiche:

1. Quale problema pubblico risolve il sistema?
2. L'uso dell'IA è necessario e proporzionato?
3. Quali dati usa e sono leciti, pertinenti, aggiornati e affidabili?
4. Chi è responsabile nel ciclo di vita?
5. Che cosa fa il sistema e che cosa resta in capo alla persona?
6. Come si controlla, spiega, corregge e contesta l'output?
7. Come sono protetti dati, modello e infrastruttura?

## 3. CONCETTI FONDAMENTALI

| Concetto | Significato | Esempio nella PA |
|---|---|---|
| **Supervisione umana** (*human oversight*) | una persona competente può capire i limiti, controllare e intervenire | operatore verifica la categoria proposta per una pratica |
| **Human in the loop** | una persona approva/decide nel flusso | una bozza parte solo dopo approvazione |
| **Human on the loop** | una persona sorveglia e può intervenire, ma non riesamina ogni singolo output | monitoraggio di un sistema di anomalie |
| **Human in command** | controllo umano sugli obiettivi, sulle autorizzazioni e sulla possibilità di fermare il sistema | dirigente stabilisce regole di impiego e disattivazione |
| **Tracciabilità** | possibilità di ricostruire dati, versioni, operazioni e output | log con fonti RAG, modello, operatore e decisione |
| **Spiegabilità** | capacità di fornire ragioni comprensibili dell'output o del funzionamento | fattori rilevanti di uno score di rischio |
| **Robustezza** | comportamento adeguato in condizioni previste e ragionevoli | non classifica in modo erratico documenti appena diversi |
| **Cybersicurezza** | protezione contro accesso indebito, alterazione e indisponibilità | accessi minimi, API protette, gestione credenziali |
| **Data governance** | governo di provenienza, qualità, aggiornamento e accesso ai dati | dataset documentato, non raccolta casuale di file |

> **Trappola:** la supervisione umana non è una firma formale. La persona deve avere informazioni, competenza, tempo e potere effettivo di non seguire l'output.

## 4. COME FUNZIONA: valutazione pratica del rischio

1. Definire finalità e processo: ad esempio, «aiutare a trovare una FAQ», non «usare un chatbot perché è innovativo».
2. Individuare interessati e conseguenze: cittadini, dipendenti, candidati; effetti lievi o effetti su diritti e servizi.
3. Mappare dati, modello e fornitori: dati personali? cloud? API? modello esterno?
4. Classificare il caso d'uso secondo AI Act e altre norme applicabili.
5. Definire misure: autorizzazioni, log, test, supervisione, informative, formazione e reclami.
6. Testare prima del rilascio: qualità, errori, bias, accessibilità, sicurezza e casi eccezionali.
7. Monitorare: incidenti, aggiornamenti, cambiamento di dati e prestazioni.

---

# PARTE II — AI ACT DELL'UNIONE EUROPEA

## 1. CHE COS'È

L'**AI Act** è il **Regolamento (UE) 2024/1689**, che stabilisce regole armonizzate sull'intelligenza artificiale nell'Unione europea. Essendo un regolamento, è direttamente applicabile negli Stati membri; le norme nazionali completano aspetti organizzativi e di coordinamento.

È entrato in vigore il **1° agosto 2024** e ha un'applicazione graduale.

## 2. A COSA SERVE

L'obiettivo è promuovere un'IA affidabile e compatibile con salute, sicurezza, diritti fondamentali, democrazia e Stato di diritto, senza bloccare l'innovazione. Lo strumento centrale è la classificazione per rischio.

## 3. SISTEMI, MODELLI E RUOLI

### Sistema di IA e GPAI

- Un **sistema di IA** riceve input e genera output quali previsioni, contenuti, raccomandazioni o decisioni.
- Un **modello di IA per finalità generali** (**GPAI**, *General-Purpose AI*) ha capacità generali ed è integrabile in molte applicazioni. Un grande modello linguistico può esserne un esempio.

> **Trappola:** GPAI è un **modello**; un chatbot, assistente documentale o sistema di selezione è un **sistema/applicazione** che può incorporare un modello GPAI.

### Ruoli essenziali

| Ruolo | Significato semplice | Esempio |
|---|---|---|
| **Provider / fornitore** | sviluppa un sistema/modello e lo immette sul mercato o lo mette in servizio con proprio nome | azienda che sviluppa un software AI |
| **Deployer / utilizzatore** | usa il sistema sotto la propria autorità | Comune che usa un classificatore di segnalazioni |
| **Importatore** | soggetto UE che immette sul mercato un sistema di soggetto extra-UE | importatore di software AI statunitense |
| **Distributore** | rende disponibile il sistema nella catena commerciale | rivenditore di software |
| **Interessato** | persona sulla quale l'IA può avere impatto | cittadino, lavoratore o candidato |

Una PA è normalmente **deployer**, ma può essere **provider** se sviluppa o mette in servizio un sistema con il proprio nome, oppure se modifica sostanzialmente il sistema. Il ruolo dipende dai fatti, non dalla sola etichetta contrattuale.

---

## 4. CLASSIFICAZIONI DEL RISCHIO

### A. Rischio inaccettabile: pratiche vietate

Le pratiche dell'articolo 5 non sono sistemi «ad alto rischio»: sono **vietate**.

Esempi rilevanti:

- tecniche manipolative o ingannevoli che possono provocare danno significativo;
- sfruttamento di vulnerabilità dovute, tra l'altro, a età, disabilità o condizione socioeconomica, con possibile danno significativo;
- **social scoring** che determina trattamenti sfavorevoli ingiustificati o sproporzionati;
- previsione del rischio di reato basata unicamente su profilazione o tratti della personalità;
- scraping non mirato di immagini facciali per creare o ampliare banche dati di riconoscimento facciale;
- riconoscimento delle emozioni in luoghi di lavoro e istituti di istruzione, salvo eccezioni limitate;
- categorizzazione biometrica per dedurre dati sensibili, salvo eccezioni ristrette;
- identificazione biometrica remota in tempo reale in spazi pubblici per law enforcement, salvo eccezioni tassative e condizioni rigorose.

### B. Alto rischio

Un sistema è ad **alto rischio** non perché è complesso, usa deep learning o è acquistato dalla PA, ma perché rientra nelle ipotesi normative:

1. è componente di sicurezza di determinati prodotti regolamentati o è esso stesso tale prodotto (art. 6, par. 1 e Allegato I);
2. rientra nell'**Allegato III**, alle condizioni previste (art. 6, par. 2).

Gli ambiti dell'Allegato III includono, in sintesi:

- biometria;
- infrastrutture critiche;
- istruzione e formazione;
- occupazione e gestione dei lavoratori;
- accesso a servizi essenziali pubblici e privati e benefici essenziali;
- law enforcement;
- migrazione, asilo e frontiere;
- giustizia e processi democratici.

Esempi da riconoscere:

- valutare l'accesso, l'ammissibilità o la priorità di un beneficio essenziale: può essere alto rischio;
- ammettere, valutare o selezionare candidati a un concorso: area tipicamente ad alto rischio;
- chatbot che recupera informazioni pubbliche senza incidere sulla decisione: non è alto rischio soltanto perché è un chatbot.

> **Trappola:** una PA non usa automaticamente sistemi ad alto rischio; conta la funzione concreta e la classificazione legale.

### C. Rischio di trasparenza

In alcuni casi occorrono informazioni specifiche per far sapere alle persone che interagiscono con una macchina o che il contenuto è artificiale/manipolato:

- chatbot: informazione che si interagisce con IA, salvo quando sia evidente dal contesto;
- contenuti sintetici/manipolati: marcature leggibili dalla macchina quando previsto;
- deepfake: obblighi di informazione/etichettatura applicabili;
- testo pubblicato su materie di interesse pubblico: indicazione di generazione/manipolazione IA, secondo condizioni ed eccezioni previste.

### D. Rischio minimo o nullo

La maggior parte dei sistemi ricade qui. Non vi sono gli obblighi speciali più gravosi degli alti rischi, ma restano applicabili GDPR, cybersicurezza, diritto amministrativo, diritto d'autore, contratti e altre norme.

> **Trappola:** «rischio minimo» non significa «nessun obbligo giuridico».

---

## 5. OBBLIGHI PER I SISTEMI AD ALTO RISCHIO

Prima della messa sul mercato o in servizio, tra i requisiti principali vi sono:

1. sistema di gestione dei rischi nel ciclo di vita;
2. governance e qualità dei dati;
3. documentazione tecnica;
4. log e tracciabilità;
5. trasparenza e istruzioni per il deployer;
6. supervisione umana appropriata;
7. accuratezza, robustezza e cybersicurezza.

Il provider deve curare valutazione di conformità, documentazione e registrazione nei casi previsti. Il deployer deve usare il sistema secondo istruzioni, assegnare la supervisione a persone competenti, controllare l'input e il contesto d'uso, gestire log e incidenti quando applicabile.

### FRIA e DPIA: non sono la stessa cosa

Per determinati deployer di sistemi ad alto rischio, in particolare organismi di diritto pubblico e privati che erogano servizi pubblici, può essere necessaria la **valutazione d'impatto sui diritti fondamentali** (**FRIA**) prima dell'uso.

- **FRIA**: impatti dell'uso concreto sui diritti fondamentali.
- **DPIA** ex art. 35 GDPR: rischi del trattamento di dati personali e misure di mitigazione.

Possono entrambe essere necessarie e coordinate, ma sono strumenti diversi.

## 6. GPAI: obblighi per modelli di finalità generali

Per i provider GPAI, in sintesi:

- documentazione tecnica;
- informazioni per gli integratori a valle su capacità e limiti;
- policy per il rispetto del diritto d'autore UE;
- sintesi sufficientemente dettagliata dei contenuti di addestramento;
- rappresentante autorizzato UE, quando richiesto, per provider extra-UE.

I GPAI a **rischio sistemico** hanno obblighi ulteriori: valutazione e mitigazione dei rischi sistemici, test, segnalazione di incidenti gravi e cybersicurezza.

> **Trappola:** una PA che usa un LLM esterno non è automaticamente provider del modello GPAI; può però essere deployer del sistema che incorpora quel modello.

## 7. DATE DA MEMORIZZARE

| Data | Regole applicabili |
|---|---|
| **1 agosto 2024** | entrata in vigore dell'AI Act |
| **2 febbraio 2025** | pratiche vietate e AI literacy |
| **2 agosto 2025** | governance e obblighi GPAI |
| **2 agosto 2026** | applicazione generale di molte disposizioni, enforcement e obblighi di trasparenza |
| **2 dicembre 2026** | ulteriori divieti su generazione/manipolazione di materiale intimo non consensuale e materiale di abuso sessuale su minori |
| **2 dicembre 2027** | regole per alto rischio dell'Allegato III |
| **2 agosto 2028** | regole per alto rischio collegato a prodotti regolamentati dell'Allegato I |

### AI literacy

L'**AI literacy** impone a provider e deployer misure per garantire un livello sufficiente di competenza del personale e di chi opera per loro conto, proporzionato a ruolo, esperienza e contesto d'uso.

Non significa che tutti debbano essere data scientist: chi usa o governa IA deve conoscerne funzionamento essenziale, limiti, rischi, procedure di escalation e divieti.

---

# PARTE III — NORMATIVA ITALIANA

## 1. CHE COS'È

La principale disciplina nazionale è la **legge 23 settembre 2025, n. 132**, *Disposizioni e deleghe al Governo in materia di intelligenza artificiale*, entrata in vigore il **10 ottobre 2025**.

La legge italiana segue un approccio antropocentrico, corretto, trasparente e responsabile e va applicata in conformità con l'AI Act.

## 2. A COSA SERVE

La legge n. 132/2025 non sostituisce il regolamento europeo: integra il quadro italiano con principi, governance, disposizioni per settori pubblici e privati, autorità e deleghe legislative.

## 3. PA, autorità e protezione dei dati

### IA e Pubblica Amministrazione

Per la PA, il principio da ricordare è: l'IA può essere usata per attività **strumentali e di supporto** all'attività amministrativa e ai provvedimenti; la persona fisica resta responsabile del procedimento e del provvedimento.

Concetti essenziali:

- conoscibilità del funzionamento e tracciabilità dell'uso;
- responsabilità umana e istituzionale;
- tutela dei diritti;
- uso sicuro, non discriminatorio e proporzionato.

> **Trappola:** l'IA può assistere, ma non consente alla PA di delegare integralmente la responsabilità di un provvedimento a un algoritmo.

### AgID e ACN

La legge individua **AgID** e **ACN** come autorità nazionali in materia di IA, con funzioni distinte.

- **AgID**: promozione dell'innovazione e sviluppo IA; compiti connessi a notifica, valutazione, accreditamento e monitoraggio dei soggetti di valutazione della conformità, oltre a spazi di sperimentazione entro le competenze attribuite.
- **ACN**: presidio di cybersicurezza, vigilanza del mercato e punto di contatto secondo il quadro nazionale e unionale previsto.

Restano rilevanti le autorità di settore e il **Garante per la protezione dei dati personali** per i profili privacy.

### CAD, linee guida e GDPR

Il **Codice dell'Amministrazione Digitale (CAD)** è una base della trasformazione digitale nella PA. Le linee guida AgID sull'IA nella PA seguono il procedimento dell'art. 71 CAD: non vanno confuse con regolamenti UE direttamente applicabili.

Quando si trattano dati personali operano anche GDPR e Codice privacy. Principi chiave:

- liceità, correttezza e trasparenza;
- limitazione della finalità;
- minimizzazione;
- esattezza;
- limitazione della conservazione;
- integrità e riservatezza;
- **accountability**.

L'art. 22 GDPR riguarda decisioni basate **unicamente** su trattamento automatizzato, inclusa la profilazione, che producono effetti giuridici o incidono in modo analogo significativamente. Non vieta ogni automazione senza eccezioni, ma richiede specifici presupposti e garanzie nel caso concreto.

> **Trappola:** aggiungere una firma umana simbolica non risolve automaticamente il problema dell'art. 22. Il controllo deve essere autentico e significativo.

Oltre alla privacy, la PA deve rispettare legalità, imparzialità, buon andamento, trasparenza, motivazione, partecipazione e tutela giurisdizionale.

---

# PARTE IV — AI NELLA PUBBLICA AMMINISTRAZIONE

## 1. CHE COS'È

AI nella PA significa usare IA per migliorare servizi, processi e supporto alle decisioni. Non coincide con digitalizzazione: convertire un modulo cartaceo in PDF non è IA; estrarre, classificare, prevedere o generare tramite modello può esserlo.

## 2. A COSA SERVE

Usi possibili:

- ricerca in regolamenti e banche dati;
- classificazione e instradamento documenti;
- OCR ed estrazione strutturata;
- accessibilità e semplificazione linguistica;
- supporto cybersecurity;
- analisi flussi e previsione della domanda;
- individuazione di anomalie da verificare;
- chatbot informativi fondati su fonti ufficiali.

Benefici: velocità, uniformità, accessibilità, capacità di gestire volumi elevati. Rischi: automatizzare errori, rendere opache scelte pubbliche, escludere persone vulnerabili o amplificare bias storici.

## 3. ESEMPIO: domande di contributo comunale

### Uso prudente

- estrazione di dati dai documenti;
- controllo della completezza formale;
- proposta di classificazione;
- ricerca di norme e modulistica;
- bozza istruttoria e segnalazione anomalie.

L'istruttore controlla i documenti, valuta il caso individuale e adotta o propone l'atto. Il cittadino può ricevere informazioni comprensibili e usare gli ordinari strumenti di partecipazione e tutela.

### Uso più rischioso

- punteggio automatico che determina esclusione o priorità;
- rigetto automatico senza controllo effettivo;
- dati non pertinenti o storicamente distorti;
- LLM generico privo di fonti governate per interpretare requisiti normativi.

Aumentano così le questioni su AI Act, GDPR/art. 22, qualità dei dati, motivazione, controllo umano e rimedi.

## 4. CHECKLIST DI ADOZIONE

### Prima dell'acquisto/sviluppo

1. Definire processo, finalità e misura del successo.
2. Stabilire se l'IA è necessaria e proporzionata.
3. Individuare ruoli AI Act: deployer, provider o entrambi.
4. Classificare rischio e verificare divieti.
5. Valutare GDPR/DPIA e, se pertinente, FRIA.
6. Mappare dati, base giuridica, conservazione, trasferimenti e accessi.
7. Definire sicurezza, log, audit, accessibilità, interoperabilità e continuità.
8. Nel capitolato richiedere documentazione, metriche, test, gestione incidenti, aggiornamenti, auditabilità e uscita/reversibilità.

### Durante l'esercizio

1. Formare il personale: AI literacy.
2. Informare utenti e operatori quando necessario.
3. Limitare privilegi e proteggere dati, API e credenziali.
4. Controllare errori, drift, bias, reclami e aggiornamenti.
5. Conservare evidenze utili alla tracciabilità nei limiti applicabili.
6. Gestire incidenti e sospendere l'uso se necessario.
7. Non trattare output generativi come fonti definitive: verificare sempre fonti, aggiornamento e pertinenza.

## 5. DIFFERENZE IMPORTANTI NELLA PA

| Situazione | Inquadramento corretto |
|---|---|
| Chatbot su orari e modulistica | non è automaticamente alto rischio; possono applicarsi trasparenza, privacy e sicurezza |
| LLM che prepara una bozza per un funzionario | supporto; il funzionario deve verificare contenuto e fonti |
| Algoritmo che esclude automaticamente una domanda di beneficio | alto impatto: attenzione a AI Act, art. 22 GDPR, motivazione, controllo umano e rimedi |
| Sistema che seleziona candidati a concorso | area tipicamente ad alto rischio |
| Sistema che raggruppa richieste per argomento | clustering: supporto utile, non decisione giuridica di per sé |
| OCR su modulo | possibile automazione/IA, ma non equivale a decisione automatizzata sulla persona |

---

# PARTE V — ERRORI E TRAPPOLE DA CONCORSO

1. **«L'AI Act disciplina solo gli LLM.»** — Falso: disciplina sistemi IA in generale e contiene regole anche per GPAI.
2. **«Un sistema complesso è automaticamente alto rischio.»** — Falso: dipende dalla funzione e dalle condizioni normative.
3. **«Tutti i sistemi della PA sono ad alto rischio.»** — Falso.
4. **«Alto rischio significa vietato.»** — Falso: sono categorie diverse.
5. **«Rischio minimo significa assenza di norme.»** — Falso: restano GDPR, CAD, sicurezza, diritto amministrativo e altre discipline.
6. **«La PA trasferisce la responsabilità del provvedimento al fornitore.»** — Falso.
7. **«Supervisione umana equivale a firma a posteriori.»** — Falso.
8. **«Art. 22 GDPR vieta qualsiasi automazione.»** — Falso: riguarda decisioni esclusivamente automatizzate con effetti giuridici o analogamente significativi e prevede condizioni/garanzie.
9. **«RAG elimina le allucinazioni.»** — Falso: può ridurle, non annullarle.
10. **«AI literacy è un corso identico per tutti.»** — Falso: deve essere proporzionata a ruolo e rischio.
11. **«AI Act e legge n. 132/2025 sono la stessa fonte.»** — Falso: il primo è regolamento UE; la seconda è legge italiana.
12. **«Un chatbot deve dichiarare sempre di essere IA senza eccezioni.»** — Affermazione incompleta: l'obbligo opera nei casi previsti, considerando anche quando l'interazione sia già evidente dal contesto.

---

# PARTE VI — TABELLA COMPARATIVA FINALE

| Livello/concetto | Che cosa descrive/regola | Parola chiave | Esempio |
|---|---|---|---|
| AI | campo generale | sistemi intelligenti | classificatore, sistema esperto, LLM |
| ML | apprendimento dai dati | generalizzazione | previsione tempi istruttoria |
| Deep Learning | ML con reti neurali profonde | reti neurali | visione, voce, linguaggio |
| AI generativa | produzione contenuti | generare | bozza di testo/immagine |
| LLM | grande modello linguistico | token + Transformer | assistente testuale |
| Agente | sistema orientato a un obiettivo che usa azioni | osserva-decide-agisce | ricerca fonti e prepara bozza |
| AI Act | regolamento UE basato sul rischio | conformità | divieti, trasparenza, obblighi |
| Legge n. 132/2025 | cornice nazionale italiana | governance | principi, PA, autorità |
| GDPR | dati personali | liceità e diritti | dati di cittadini in servizio AI |
| CAD/AgID | amministrazione digitale | indirizzi per PA | linee guida, Piano Triennale |

---

# PARTE VII — COSA DEVO MEMORIZZARE

- **AI Act** = Regolamento (UE) 2024/1689.
- **Approccio AI Act** = basato sul rischio.
- **Pratica vietata** ≠ **sistema ad alto rischio**.
- **Provider** sviluppa/immette sul mercato o mette in servizio con proprio nome; **deployer** usa sotto la propria autorità.
- **GPAI** = modello generale; non coincide con ogni sistema che lo usa.
- **FRIA** ≠ **DPIA**.
- **AI literacy** = competenza proporzionata di chi fornisce, usa o governa IA.
- Legge italiana n. 132/2025: **23 settembre 2025**, in vigore dal **10 ottobre 2025**.

## Formula mentale per i quiz PA

> **Finalità legittima + dati corretti + ruolo chiaro + rischio classificato + controllo umano effettivo + trasparenza e tracciabilità + privacy e sicurezza + responsabilità amministrativa.**

Se una risposta propone «decisione integralmente automatica», «nessuna verifica», «dati qualunque», «fornitore unico responsabile» o «AI sempre neutrale», è con alta probabilità falsa o incompleta.

---

# Fonti istituzionali per il ripasso normativo

- AI Act, Regolamento (UE) 2024/1689 (EUR-Lex): https://eur-lex.europa.eu/eli/reg/2024/1689/oj
- Commissione europea, quadro e calendario AI Act: https://digital-strategy.ec.europa.eu/en/policies/regulatory-framework-ai
- Commissione europea, enforcement AI Act: https://digital-strategy.ec.europa.eu/en/policies/enforcement-ai-act
- Legge 23 settembre 2025, n. 132 (Normattiva): https://www.normattiva.it/
- AgID, Intelligenza artificiale: https://www.agid.gov.it/it/ambiti-intervento/intelligenza-artificiale
- Garante privacy, decisioni automatizzate: https://www.garanteprivacy.it/
