# Cloud Computing: guida completa per concorsi informatici

> **Obiettivo:** capire i concetti, riconoscerli nei quiz e distinguere la risposta esatta da quella solo plausibile.

---

## 1. CHE COS'È IL CLOUD COMPUTING

Il **cloud computing** è un modello con cui risorse informatiche — server, capacità di calcolo, memoria, archivi dati, reti, database e software — sono rese disponibili **via rete**, di norma Internet, e utilizzabili quando servono.

Invece di comprare, installare e gestire in sede tutti i server, un ente può richiedere risorse a un fornitore cloud e pagare in funzione dell'uso o della capacità riservata. Il cloud non significa semplicemente “dati su Internet”: significa ottenere risorse IT **configurabili, condivise, automatizzate e rapidamente assegnabili o rilasciabili**.

### 2. A COSA SERVE

Risolve soprattutto questi problemi:

- evitare grandi investimenti iniziali in hardware (CAPEX);
- ottenere risorse in tempi brevi invece di acquistare e installare macchine;
- adattare le risorse ai picchi di domanda;
- aumentare affidabilità e continuità operativa;
- usare servizi gestiti, ad esempio database e backup, riducendo il lavoro operativo.

### 3. CONCETTI FONDAMENTALI

Le cinque caratteristiche essenziali comunemente associate al cloud sono:

1. **On-demand self-service**: l'utente può attivare risorse senza intervento manuale del fornitore.
2. **Broad network access**: le risorse sono accessibili tramite rete e protocolli standard.
3. **Resource pooling**: le risorse fisiche del provider sono condivise tra più clienti, ma logicamente separate.
4. **Rapid elasticity**: le risorse possono crescere o diminuire rapidamente.
5. **Measured service**: l'uso è misurato e monitorato, spesso per fatturazione e controllo dei consumi.

### 4. COME FUNZIONA

1. Il provider possiede data center con server, dischi e reti.
2. Attraverso virtualizzazione e automazione, divide tali risorse in unità assegnabili ai clienti.
3. L'ente richiede una risorsa tramite portale, API o codice di configurazione.
4. Il provider la configura, applica regole di accesso e la rende disponibile.
5. L'ente usa, modifica o elimina la risorsa; il consumo è tracciato.

### 5. ESEMPIO CONCRETO

Un Comune pubblica online il servizio per iscrivere i cittadini a un concorso. Nei giorni vicini alla scadenza gli accessi aumentano molto. Nel cloud può aumentare temporaneamente il numero di istanze applicative e lo spazio del database, per poi ridurli dopo la scadenza.

### 6. DIFFERENZE IMPORTANTI

| Concetto | Non significa |
|---|---|
| Cloud computing | qualsiasi applicazione raggiungibile via Internet |
| Hosting tradizionale | necessariamente elasticità o self-service |
| Virtualizzazione | cloud: la virtualizzazione è una tecnologia abilitante, il cloud è un modello di erogazione |
| Outsourcing | sempre cloud: un fornitore può gestire sistemi dedicati senza offrire caratteristiche cloud |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** “Cloud significa che i dati non sono su server fisici.” I dati sono sempre memorizzati su infrastrutture fisiche, solo gestite dal provider.
- **Falso:** “Il cloud elimina ogni responsabilità dell'ente.” Cambia la ripartizione delle responsabilità; non scompare.
- **Vero:** la misurazione del servizio supporta sia il pagamento sia il controllo dei consumi.

### 8. COSA DEVO MEMORIZZARE

**Cloud = risorse IT via rete, on demand, condivise, configurabili, rapidamente assegnabili/rilasciabili e misurate.** Le cinque caratteristiche: self-service, accesso via rete, pooling, elasticità, misurazione.

---

# 2. TECNOLOGIE E PROPRIETÀ DELL'INFRASTRUTTURA

## Virtualizzazione

### 1. CHE COS'È

La **virtualizzazione** è la tecnica che crea risorse logiche indipendenti dall'hardware fisico. Un singolo server fisico può ospitare più **macchine virtuali** (VM), ciascuna con sistema operativo e applicazioni propri.

Un componente chiamato **hypervisor** (o virtual machine monitor) assegna CPU, memoria, rete e disco alle VM e le isola tra loro.

### 2. A COSA SERVE

Serve a usare meglio l'hardware, isolare ambienti diversi, creare o eliminare macchine più rapidamente e migrare carichi di lavoro. È una base tecnologica molto importante del cloud, ma non coincide con esso.

### 3. CONCETTI FONDAMENTALI

- **Host**: server fisico che ospita le VM.
- **Guest**: sistema operativo dentro una VM.
- **Hypervisor di tipo 1**: eseguito direttamente sull'hardware; tipico dei data center.
- **Hypervisor di tipo 2**: eseguito sopra un sistema operativo ospite; più comune su PC personali.

### 4. COME FUNZIONA

L'hypervisor intercetta e gestisce l'accesso delle VM alle risorse fisiche. Ogni VM vede CPU, RAM, dischi e schede di rete “virtuali”, mentre l'hypervisor le mappa sulle risorse reali.

### 5. ESEMPIO CONCRETO

Un server del CED regionale può ospitare una VM per il protocollo, una per il portale istituzionale e una per un ambiente di test, mantenendole separate.

### 6. DIFFERENZE IMPORTANTI

| VM | Container |
|---|---|
| Include un sistema operativo guest completo | Condivide il kernel del sistema operativo host |
| Più isolata ma generalmente più pesante | Più leggera e veloce da avviare |
| Virtualizza una macchina | Isola soprattutto processi e dipendenze applicative |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** una VM è un server fisico.
- **Falso:** cloud e virtualizzazione sono sinonimi.
- **Vero:** più VM possono condividere lo stesso host fisico, restando logicamente separate.

### 8. COSA DEVO MEMORIZZARE

**Virtualizzazione = astrazione dell'hardware.** Hypervisor = componente che crea e gestisce VM. Cloud usa spesso la virtualizzazione, ma aggiunge erogazione on demand, automazione e misurazione.

---

## Elasticità, scalabilità e disponibilità

### 1. CHE COS'È

- **Elasticità**: capacità di aumentare **e diminuire** automaticamente o rapidamente le risorse in base alla domanda.
- **Scalabilità**: capacità di sostenere un aumento di carico aggiungendo o potenziando risorse, mantenendo prestazioni accettabili.
- **Disponibilità**: probabilità o percentuale di tempo in cui un servizio è operativo e raggiungibile.

### 2. A COSA SERVE

Consentono di servire molti utenti senza degradare il servizio e senza mantenere inutilmente risorse sovradimensionate.

### 3. CONCETTI FONDAMENTALI

- **Scalabilità verticale (scale up)**: si potenzia una singola macchina, ad esempio più RAM o CPU.
- **Scalabilità orizzontale (scale out)**: si aggiungono più macchine o istanze.
- **Alta disponibilità (HA)**: progettazione con ridondanza per limitare le interruzioni.
- **SLA**: Service Level Agreement; accordo con livelli di servizio, ad esempio disponibilità minima.

### 4. COME FUNZIONA

Un sistema monitora metriche come CPU, numero di richieste o tempo di risposta. Quando supera una soglia, una regola di autoscaling avvia nuove istanze; quando il carico cala, le riduce. La disponibilità è aumentata distribuendo componenti ridondanti in zone o sedi differenti.

### 5. ESEMPIO CONCRETO

Il sito per il pagamento di tributi locali passa da 5.000 a 50.000 accessi nell'ultimo giorno utile. Con scale out vengono avviate nuove istanze del portale; se una istanza si guasta, le altre continuano a rispondere.

### 6. DIFFERENZE IMPORTANTI

| Termine | Idea chiave | Domanda tipica |
|---|---|---|
| Scalabilità | capacità di crescere | “Può reggere più utenti?” |
| Elasticità | cresce **e decresce** secondo necessità | “Può adattarsi dinamicamente al carico?” |
| Disponibilità | servizio accessibile | “Il servizio resta operativo in caso di guasto?” |
| Prestazione | velocità/tempo di risposta | “Risponde rapidamente?” |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- Scalabilità non implica automaticamente elasticità: si può aggiungere capacità manualmente.
- Alta disponibilità non equivale a disaster recovery: HA affronta soprattutto guasti locali e continuità immediata; DR ripristina dopo eventi gravi.
- **Falso:** “la scalabilità verticale aggiunge nodi.” Aggiunge risorse a un nodo.

### 8. COSA DEVO MEMORIZZARE

**Scale up = macchina più potente; scale out = più macchine. Elasticità = scalabilità dinamica e reversibile. Disponibilità = servizio operativo nel tempo.**

---

## Load balancing

### 1. CHE COS'È

Il **load balancing** è la distribuzione delle richieste in arrivo tra più server o istanze, mediante un componente chiamato **load balancer**.

### 2. A COSA SERVE

Evita che un solo server sia sovraccarico, migliora prestazioni e disponibilità e consente di togliere un nodo guasto dal servizio.

### 3. CONCETTI FONDAMENTALI

- **Health check**: controllo periodico dello stato dei server.
- **Backend**: server che ricevono il traffico dal bilanciatore.
- Algoritmi: **round robin** (a turno), least connections (meno connessioni), pesati (server più potenti ricevono più traffico).

### 4. COME FUNZIONA

1. Il cittadino invia una richiesta al portale.
2. La richiesta arriva al load balancer, non direttamente alle singole istanze.
3. Il load balancer controlla quali backend sono sani e meno impegnati.
4. Inoltra la richiesta a uno di essi.
5. Se un backend fallisce l'health check, non riceve più traffico.

### 5. ESEMPIO CONCRETO

Tre istanze del portale SUAP ricevono richieste tramite un unico indirizzo pubblico. Il bilanciatore le ripartisce tra le istanze e isola quella non funzionante.

### 6. DIFFERENZE IMPORTANTI

Il load balancing **distribuisce traffico**; l'autoscaling **crea o rimuove capacità**. Spesso lavorano insieme, ma sono funzioni diverse.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** il load balancer aumenta da solo il numero di server. Può integrarsi con autoscaling, ma non è la stessa cosa.
- **Falso:** bilanciare il carico garantisce da solo il backup dei dati.

### 8. COSA DEVO MEMORIZZARE

**Load balancer = punto di ingresso che ripartisce richieste tra backend sani. Health check = rilevazione dei nodi non disponibili.**

---

# 3. MODELLI DI SERVIZIO: IaaS, PaaS, SaaS

## 1. CHE COS'È

I modelli indicano **quale livello della pila tecnologica è fornito e gestito dal provider**.

| Modello | Il provider offre soprattutto | Il cliente gestisce soprattutto |
|---|---|---|
| **IaaS** (Infrastructure as a Service) | hardware, rete, storage, virtualizzazione | sistemi operativi, middleware, applicazioni, dati |
| **PaaS** (Platform as a Service) | infrastruttura, OS, runtime, middleware, spesso database gestiti | applicazioni e dati |
| **SaaS** (Software as a Service) | applicazione pronta all'uso e piattaforma sottostante | dati, utenti, configurazioni e uso corretto |

### 2. A COSA SERVE

Permettono di scegliere il compromesso tra **controllo** e **onere gestionale**. Salendo da IaaS a SaaS diminuisce ciò che l'ente deve amministrare, ma diminuisce anche la libertà di configurazione tecnica.

### 3. CONCETTI FONDAMENTALI

- In tutti i modelli l'ente mantiene responsabilità su dati, identità, autorizzazioni e configurazioni di propria competenza.
- Il confine preciso può variare nel contratto e nel servizio; per il quiz conta il principio generale.

### 4. COME FUNZIONA

- Con **IaaS** si crea una VM e si installano applicazione e sistema operativo.
- Con **PaaS** si distribuisce il codice su un ambiente già predisposto.
- Con **SaaS** si usa un'applicazione dal browser o da app, configurando utenti e dati.

### 5. ESEMPIO CONCRETO

- IaaS: VM cloud su cui il Comune installa un proprio gestionale legacy.
- PaaS: piattaforma gestita su cui il Comune pubblica una nuova API per i cittadini.
- SaaS: servizio di posta elettronica o gestione documentale erogato come applicazione pronta.

### 6. DIFFERENZE IMPORTANTI

| Domanda | Risposta |
|---|---|
| “Chi aggiorna il sistema operativo della VM?” | Normalmente il cliente in IaaS; il provider in PaaS/SaaS |
| “Chi distribuisce il proprio codice?” | Cliente in IaaS e PaaS; in SaaS normalmente usa software già pronto |
| “SaaS significa nessuna responsabilità?” | No: il cliente governa account, accessi, dati e configurazione |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** in IaaS il provider gestisce il sistema operativo guest.
- **Falso:** PaaS è un semplice server virtuale. PaaS fornisce una piattaforma/runtime gestiti.
- **Falso:** SaaS è sinonimo di applicazione installata localmente.

### 8. COSA DEVO MEMORIZZARE

**IaaS: infrastruttura. PaaS: piattaforma per sviluppare/eseguire. SaaS: software pronto all'uso.** All'aumentare del servizio gestito diminuisce la gestione tecnica del cliente.

---

# 4. MODELLI DI DISTRIBUZIONE DEL CLOUD

## Public, private, hybrid, community e multicloud

### 1. CHE COS'È

- **Public cloud**: infrastruttura di un provider esterno, condivisa logicamente tra più clienti (multi-tenant).
- **Private cloud**: infrastruttura usata esclusivamente da una singola organizzazione; può essere nel suo data center o ospitata da terzi.
- **Hybrid cloud**: uso coordinato di almeno un private cloud e un public cloud, con integrazione/portabilità dei carichi o dei dati.
- **Community cloud**: infrastruttura condivisa da organizzazioni con esigenze comuni, ad esempio requisiti normativi o di missione.
- **Multicloud**: uso di servizi cloud di più provider; non richiede necessariamente un private cloud né integrazione stretta.

### 2. A COSA SERVE

La scelta dipende da requisiti di sicurezza, sovranità/localizzazione del dato, compatibilità con sistemi esistenti, costi, resilienza e rischio di dipendenza da un solo fornitore (**vendor lock-in**).

### 3. CONCETTI FONDAMENTALI

**Multi-tenant** non vuol dire che gli utenti di organizzazioni diverse possono vedere i rispettivi dati: condividono infrastruttura fisica, ma devono essere separati logicamente.

### 4. COME FUNZIONA

In un modello ibrido, ad esempio, l'anagrafe resta in un ambiente privato mentre il portale pubblico è nel cloud pubblico; una connessione sicura e API controllate permettono il dialogo fra i due ambienti.

### 5. ESEMPIO CONCRETO

Una Regione usa un cloud pubblico per ospitare un portale informativo con traffico variabile e un ambiente privato per un'applicazione interna legacy. Questo è **hybrid cloud** solo se i due ambienti sono integrati e operano come soluzione coordinata; il semplice possesso di entrambi non basta a dimostrarlo.

### 6. DIFFERENZE IMPORTANTI

| Concetti confondibili | Differenza decisiva |
|---|---|
| Private cloud vs on-premises | “On-premises” indica dove risiede fisicamente l'infrastruttura; private indica uso esclusivo. Un private cloud può essere ospitato fuori sede. |
| Hybrid cloud vs multicloud | Hybrid = privato + pubblico integrati; multicloud = più provider, anche tutti pubblici. |
| Public cloud vs community cloud | Public è destinato al mercato generale; community è riservato a una comunità con esigenze comuni. |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** private cloud significa necessariamente server nell'edificio dell'ente.
- **Falso:** multicloud e hybrid cloud sono sinonimi.
- **Falso:** public cloud implica dati pubblici; “public” descrive il modello di erogazione, non la visibilità dei dati.

### 8. COSA DEVO MEMORIZZARE

**Public = provider esterno condiviso logicamente; private = dedicato a una organizzazione; hybrid = privato + pubblico integrati; community = organizzazioni con esigenze comuni; multicloud = più provider.**

---

# 5. CLOUD-NATIVE, MICROSERVIZI, CONTAINER E SERVERLESS

## Cloud-native

### 1. CHE COS'È

Un'applicazione **cloud-native** è progettata per sfruttare le caratteristiche del cloud: automazione, API, elasticità, distribuzione rapida, osservabilità e resilienza. Non basta spostare un'applicazione esistente su una VM cloud per definirla cloud-native.

### 2. A COSA SERVE

Serve a rilasciare modifiche frequenti, scalare componenti separatamente e rendere il sistema più resiliente.

### 3. CONCETTI FONDAMENTALI

Sono comuni: container, microservizi, orchestrazione, CI/CD, Infrastructure as Code e monitoraggio. Non sono obbligatori tutti insieme in ogni progetto.

### 4–8. ESEMPIO, DIFFERENZE E TRAPPOLE

Un portale cloud-native può avere componenti indipendenti per autenticazione, pagamenti e notifiche, rilasciati automaticamente. **Trappola:** cloud-native non è un prodotto cloud specifico e non è sinonimo automatico di microservizi.

**Da memorizzare:** cloud-native = progettato per sfruttare il cloud, non semplicemente “ospitato nel cloud”.

---

## Microservizi

### 1. CHE COS'È

L'architettura a **microservizi** divide un'applicazione in piccoli servizi autonomi, ciascuno dedicato a una capacità di business e comunicante tramite rete/API o messaggi.

### 2. A COSA SERVE

Permette rilasci e scalabilità indipendenti: si può aggiornare il servizio notifiche senza distribuire nuovamente tutto il portale.

### 3. CONCETTI FONDAMENTALI

Ogni servizio dovrebbe avere una responsabilità chiara e contratti di comunicazione definiti. L'autonomia non significa assenza di coordinamento: servono monitoraggio, gestione delle versioni, sicurezza delle API e gestione degli errori di rete.

### 4. COME FUNZIONA

Il frontend chiama un gateway/API; i microservizi elaborano funzioni specifiche e possono comunicare in modo sincrono (richiesta-risposta) o asincrono (code/eventi).

### 5. ESEMPIO CONCRETO

Nel sistema di prenotazione appuntamenti: servizio cittadini, agenda, notifiche SMS/e-mail e pagamenti sono separati.

### 6. DIFFERENZE IMPORTANTI

| Monolite | Microservizi |
|---|---|
| Applicazione distribuita come unità unica | Più servizi distribuibili separatamente |
| Più semplice da avviare | Più flessibile, ma più complesso da operare |
| Scalabilità spesso dell'intera applicazione | Scalabilità mirata del singolo servizio |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** ogni microservizio deve obbligatoriamente avere un database separato; è un principio frequente di autonomia, non un vincolo assoluto universale.
- **Falso:** microservizi eliminano la complessità; spostano parte della complessità su rete, monitoraggio e distribuzione.

### 8. COSA DEVO MEMORIZZARE

**Microservizio = servizio piccolo, autonomo, focalizzato su una funzione di business e comunicante via rete.** Non è sinonimo di container.

---

## Container

### 1. CHE COS'È

Un **container** è un'unità di esecuzione isolata che contiene applicazione, librerie e dipendenze necessarie, condividendo il kernel del sistema operativo host.

### 2. A COSA SERVE

Riduce il problema “funziona sul mio computer”: lo stesso pacchetto può essere eseguito in sviluppo, test e produzione con comportamento più uniforme.

### 3. CONCETTI FONDAMENTALI

- **Immagine**: modello immutabile da cui avviare container.
- **Container**: istanza in esecuzione dell'immagine.
- **Registry**: deposito di immagini.
- **Orchestratore**: sistema che distribuisce, scala e sostituisce container, ad esempio Kubernetes.

### 4. COME FUNZIONA

Si costruisce un'immagine, la si pubblica in un registry e la piattaforma avvia uno o più container. L'orchestratore può riavviarli, aumentarne il numero e collegarli in rete.

### 5. ESEMPIO CONCRETO

Il servizio notifiche di un ente viene distribuito come immagine container; quando cresce il numero di richieste, l'orchestratore avvia ulteriori repliche.

### 6. DIFFERENZE IMPORTANTI

Container non significa microservizio: un monolite può essere containerizzato e un microservizio può essere eseguito senza container.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** ogni container contiene un intero sistema operativo guest come una VM.
- **Falso:** un'immagine è necessariamente un container in esecuzione.

### 8. COSA DEVO MEMORIZZARE

**Immagine = modello; container = istanza in esecuzione.** Container condivide il kernel host; VM include OS guest.

---

## Serverless

### 1. CHE COS'È

**Serverless** è un modello in cui il provider gestisce server, provisioning, scalabilità e disponibilità dell'ambiente di esecuzione. Il programmatore distribuisce funzioni o codice e paga tipicamente per invocazioni/durata d'esecuzione.

“Serverless” non significa che non esistano server: significa che il cliente non li gestisce direttamente.

### 2. A COSA SERVE

È utile per carichi intermittenti e basati su eventi, come elaborare un documento appena caricato o inviare una notifica quando cambia lo stato di una pratica.

### 3. CONCETTI FONDAMENTALI

- **FaaS** (Function as a Service): funzioni attivate da eventi.
- **Evento/trigger**: causa dell'esecuzione, ad esempio caricamento file o messaggio in coda.
- **Cold start**: ritardo possibile nell'avvio di un ambiente non già attivo.

### 4. COME FUNZIONA

Un evento attiva una funzione; la piattaforma crea o riusa l'ambiente, esegue il codice e scala il numero di esecuzioni in base agli eventi.

### 5. ESEMPIO CONCRETO

Quando un cittadino carica un allegato alla pratica edilizia, una funzione serverless verifica il formato, salva metadati e invia una notifica al protocollo.

### 6. DIFFERENZE IMPORTANTI

Serverless è un modello di esecuzione e non coincide con PaaS, anche se entrambi riducono la gestione dell'infrastruttura. Un container può essere eseguito in modalità serverless.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** serverless elimina i server fisici.
- **Falso:** è sempre più economico: per carichi costanti molto elevati può essere meno conveniente di risorse dedicate.

### 8. COSA DEVO MEMORIZZARE

**Serverless = nessuna gestione diretta dei server; FaaS = funzioni attivate da eventi; possibile pagamento per uso.**

---

# 6. AUTOMAZIONE: IaC E GITOPS

## Infrastructure as Code (IaC)

### 1. CHE COS'È

L'**Infrastructure as Code** descrive e crea infrastrutture tramite file di codice/configurazione versionati, invece di configurarle manualmente dal pannello web.

### 2. A COSA SERVE

Rende l'infrastruttura ripetibile, revisionabile, documentabile e meno esposta a errori manuali.

### 3. CONCETTI FONDAMENTALI

- **Dichiarativo**: si descrive lo stato desiderato (“voglio tre istanze”); lo strumento decide come ottenerlo.
- **Imperativo**: si elencano i comandi/passaggi (“crea rete, poi istanza…”).
- **Idempotenza**: applicare più volte la stessa definizione porta allo stesso stato desiderato, senza duplicare inutilmente risorse.

### 4. COME FUNZIONA

Un file definisce rete, regole firewall, database e istanze. Lo strumento confronta stato desiderato e stato reale, quindi applica le modifiche necessarie.

### 5. ESEMPIO CONCRETO

Un ente mantiene in un repository la definizione della rete del portale, con subnet private, load balancer e database. Un ambiente di test viene ricreato in modo coerente in pochi minuti.

### 6. DIFFERENZE IMPORTANTI

IaC automatizza la **provisioning/configurazione dell'infrastruttura**; CI/CD automatizza soprattutto build, test e rilascio del software.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** IaC significa scrivere il codice dell'applicazione.
- **Falso:** IaC elimina il bisogno di revisione: una configurazione errata può propagarsi velocemente.

### 8. COSA DEVO MEMORIZZARE

**IaC = infrastruttura descritta in file versionati e applicata automaticamente.** Vantaggi: ripetibilità, audit, coerenza.

---

## GitOps

### 1. CHE COS'È

**GitOps** è un approccio operativo in cui un repository Git è la fonte di verità dello **stato desiderato** di applicazioni e infrastruttura. Un agente automatico confronta lo stato reale con quello dichiarato in Git e lo riconcilia.

### 2. A COSA SERVE

Porta revisioni, tracciabilità, approvazioni e rollback tipici del codice anche nelle operazioni infrastrutturali.

### 3. CONCETTI FONDAMENTALI

- **Git come source of truth**: la configurazione approvata risiede nel repository.
- **Pull request**: revisione e approvazione della modifica.
- **Reconciliation**: l'agente riporta l'ambiente allo stato definito.
- **Drift**: differenza non autorizzata tra configurazione reale e desiderata.

### 4. COME FUNZIONA

1. Un operatore modifica la configurazione nel repository.
2. La modifica è revisionata e integrata.
3. L'agente rileva il nuovo stato desiderato.
4. Applica o riconcilia la modifica nell'ambiente.

### 5. ESEMPIO CONCRETO

Per aumentare le repliche del servizio di prenotazione da 3 a 6, si modifica un file nel repository, si approva la pull request e l'agente aggiorna il cluster.

### 6. DIFFERENZE IMPORTANTI

GitOps usa spesso IaC, ma è più specifico: non è solo “salvare script in Git”; richiede Git come fonte autorevole e riconciliazione automatica.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** GitOps consiste nel fare deploy manuale dopo un commit.
- **Falso:** ogni progetto con Git usa automaticamente GitOps.

### 8. COSA DEVO MEMORIZZARE

**GitOps = Git come fonte di verità + modifiche revisionate + agente che riconcilia lo stato reale.**

---

# 7. DATI NEL CLOUD: DATA WAREHOUSE, DATA LAKE, LAKEHOUSE

## Data Warehouse

### 1. CHE COS'È

Un **Data Warehouse (DWH)** è un archivio integrato, storico e organizzato per analisi e reportistica. I dati sono tipicamente puliti, trasformati e modellati prima o durante il caricamento.

### 2. A COSA SERVE

Supporta business intelligence, cruscotti e analisi affidabili su dati provenienti da più sistemi.

### 3. CONCETTI FONDAMENTALI

- **OLTP**: sistemi transazionali operativi, ad esempio registrazione di una pratica.
- **OLAP**: analisi su molti dati, ad esempio numero di pratiche per anno e Comune.
- **ETL**: Extract, Transform, Load; estrarre, trasformare e caricare dati.
- **Schema-on-write**: struttura/modello definito prima del caricamento o dell'uso analitico.

### 4. COME FUNZIONA

I dati dai sistemi sorgente vengono estratti, puliti, uniformati e caricati in tabelle analitiche ottimizzate per interrogazioni.

### 5. ESEMPIO CONCRETO

Una Regione integra dati da sanità, trasporti e bilancio per produrre report mensili aggregati per territorio.

### 6. DIFFERENZE IMPORTANTI

Un DWH è ottimizzato per analisi strutturate e dati governati; non è normalmente il database operativo che registra la singola transazione in tempo reale.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** DWH e database transazionale hanno lo stesso scopo.
- **Falso:** il DWH contiene necessariamente solo dati in tempo reale.

### 8. COSA DEVO MEMORIZZARE

**DWH = dati integrati e strutturati per analisi/BI; OLTP = operatività; OLAP = analisi.**

---

## Data Lake

### 1. CHE COS'È

Un **Data Lake** conserva grandi quantità di dati grezzi, eterogenei e spesso a basso costo: strutturati, semi-strutturati o non strutturati (tabelle, JSON, log, immagini, PDF).

### 2. A COSA SERVE

È utile quando non si conoscono in anticipo tutte le analisi future o quando si vogliono conservare dati nella loro forma originale per data science, machine learning o nuove elaborazioni.

### 3. CONCETTI FONDAMENTALI

- **Schema-on-read**: struttura interpretata al momento della lettura.
- La governance resta essenziale: senza catalogo, qualità, permessi e metadati, il lake può diventare un **data swamp**, un archivio poco utilizzabile.

### 4. COME FUNZIONA

I dati vengono raccolti da varie fonti e memorizzati; quando un analista li usa, applica schema e trasformazioni necessari alla specifica analisi.

### 5. ESEMPIO CONCRETO

Una PA conserva nel lake log applicativi, documenti, dati da sensori ambientali e dataset aperti per analisi future.

### 6. DIFFERENZE IMPORTANTI

| Data Warehouse | Data Lake |
|---|---|
| Dati curati e strutturati per BI | Dati grezzi e di molti tipi |
| Schema-on-write | Schema-on-read |
| Forte ottimizzazione per report | Flessibilità esplorativa/data science |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** un Data Lake contiene solo dati non strutturati.
- **Falso:** un lake non richiede governance perché conserva dati grezzi.

### 8. COSA DEVO MEMORIZZARE

**Data Lake = dati eterogenei e grezzi, schema-on-read. Data Warehouse = dati modellati per analisi, schema-on-write.**

---

## Lakehouse

### 1. CHE COS'È

Un **Lakehouse** cerca di combinare la flessibilità e il costo del Data Lake con le capacità di gestione, affidabilità e analisi tipiche del Data Warehouse.

### 2. A COSA SERVE

Riduce la necessità di copiare dati tra lake e warehouse e permette analisi/AI su dati governati.

### 3. CONCETTI FONDAMENTALI

Un lakehouse punta a offrire tabelle affidabili, transazioni/controlli di coerenza, catalogo e governance su storage di tipo lake.

### 4–8. ESEMPIO, DIFFERENZE E TRAPPOLE

Un ente conserva dati grezzi in object storage, ma pubblica tabelle governate e interrogabili per report e analisi avanzate. **Trappola:** lakehouse non è semplicemente un altro nome per Data Lake; aggiunge caratteristiche di gestione e analisi “da warehouse”.

**Da memorizzare:** Lakehouse = lake + funzioni di warehouse (governance, tabelle affidabili, analisi).

---

# 8. IAM, SICUREZZA E CIFRATURA

## IAM (Identity and Access Management)

### 1. CHE COS'È

L'**IAM** è l'insieme di processi e strumenti per gestire identità digitali e controllare chi può accedere a quali risorse e con quali azioni.

### 2. A COSA SERVE

Evita accessi non autorizzati e applica il principio del **minimo privilegio**: ogni utente o servizio riceve solo i permessi necessari.

### 3. CONCETTI FONDAMENTALI

- **Autenticazione**: verifica chi sei (password, certificato, MFA).
- **Autorizzazione**: verifica cosa puoi fare dopo esserti autenticato.
- **MFA**: autenticazione a più fattori.
- **Ruolo**: insieme di permessi assumibile da un utente o servizio.
- **RBAC**: controllo accessi basato sui ruoli.
- **Principio del minimo privilegio**: permessi minimi indispensabili.

### 4. COME FUNZIONA

Un dipendente si autentica con MFA. IAM valuta ruoli e policy: può leggere i documenti della propria area ma non cancellare database o gestire account amministrativi.

### 5. ESEMPIO CONCRETO

Nel portale di una PA, l'operatore può vedere le pratiche assegnate; il responsabile può approvare; l'amministratore può gestire configurazioni, ma non necessariamente leggere tutti i contenuti applicativi.

### 6. DIFFERENZE IMPORTANTI

| Autenticazione | Autorizzazione |
|---|---|
| Risponde a “chi sei?” | Risponde a “cosa puoi fare?” |
| Login, MFA, certificato | Policy, ruoli, permessi |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** autenticazione e autorizzazione sono la stessa cosa.
- **Falso:** un account amministratore condiviso migliora la tracciabilità. La peggiora.
- **Vero:** MFA rafforza l'autenticazione ma non sostituisce la corretta autorizzazione.

### 8. COSA DEVO MEMORIZZARE

**IAM gestisce identità e accessi. Autenticazione = chi sei; autorizzazione = cosa puoi fare. Minimo privilegio e MFA sono principi fondamentali.**

---

## Sicurezza cloud e modello di responsabilità condivisa

### 1. CHE COS'È

La sicurezza cloud comprende misure tecniche, organizzative e procedurali per proteggere riservatezza, integrità e disponibilità di dati e servizi cloud.

Nel **modello di responsabilità condivisa**, provider e cliente hanno responsabilità diverse: il provider protegge tipicamente la sicurezza **del cloud** (data center, hardware, servizi di base); il cliente protegge la sicurezza **nel cloud** (dati, identità, configurazioni, applicazioni), con confini variabili secondo IaaS/PaaS/SaaS.

### 2. A COSA SERVE

Previene accessi impropri, perdite di dati, indisponibilità, configurazioni errate e violazioni normative.

### 3. CONCETTI FONDAMENTALI

- **CIA triad**: Confidentiality (riservatezza), Integrity (integrità), Availability (disponibilità).
- Segmentazione di rete, logging, monitoraggio, patching, backup, gestione delle vulnerabilità.
- La configurazione errata di un servizio cloud è una causa comune di esposizione dati.

### 4. COME FUNZIONA

Si definiscono account separati, ruoli minimi, reti private, regole firewall restrittive, cifratura, log centralizzati, backup e controlli continui.

### 5. ESEMPIO CONCRETO

Il provider gestisce sicurezza fisica del data center; l'ente deve evitare che un archivio di documenti sia configurato per l'accesso pubblico e deve governare chi può leggerlo.

### 6. DIFFERENZE IMPORTANTI

Il provider può garantire sicurezza dell'infrastruttura, ma non può rendere sicura una password debole scelta dal cliente o una policy che concede accesso pubblico ai dati.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** trasferendo dati nel cloud, ogni obbligo di sicurezza passa al provider.
- **Falso:** la cifratura sostituisce IAM, backup e monitoraggio.

### 8. COSA DEVO MEMORIZZARE

**Responsabilità condivisa: provider = sicurezza del cloud; cliente = sicurezza nel cloud, secondo il servizio usato. CIA = riservatezza, integrità, disponibilità.**

---

## Cifratura

### 1. CHE COS'È

La **cifratura** trasforma dati leggibili (**testo in chiaro**) in dati non leggibili (**testo cifrato**) usando un algoritmo e una chiave. Solo chi possiede la chiave corretta può decifrare, salvo gli altri controlli previsti.

### 2. A COSA SERVE

Protegge la riservatezza dei dati se supporti, reti o archivi sono intercettati o consultati senza autorizzazione.

### 3. CONCETTI FONDAMENTALI

- **Cifratura a riposo (at rest)**: dati in dischi, database, backup, object storage.
- **Cifratura in transito (in transit)**: dati mentre viaggiano in rete, tipicamente con TLS.
- **Simmetrica**: stessa chiave per cifrare e decifrare; efficiente per grandi volumi.
- **Asimmetrica**: coppia chiave pubblica/privata; usata ad esempio per scambio sicuro di chiavi e firme.
- **Gestione delle chiavi**: generazione, custodia, rotazione, revoca e controllo accessi alle chiavi.

### 4. COME FUNZIONA

Per un collegamento HTTPS, il client verifica l'identità del server tramite certificato e negozia chiavi di sessione; i dati della sessione sono poi protetti in transito. Per i dati archiviati, il servizio cifra i contenuti con chiavi gestite secondo policy definite.

### 5. ESEMPIO CONCRETO

I documenti sanitari sono cifrati nel repository (at rest) e il portale usa HTTPS/TLS per il trasferimento verso operatori autorizzati (in transit).

### 6. DIFFERENZE IMPORTANTI

Cifratura non è hash: l'**hash** è una trasformazione a senso unico, usata ad esempio per verificare integrità o memorizzare password in modo appropriato; la cifratura è reversibile con chiave.

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** HTTPS cifra i dati a riposo nel database.
- **Falso:** cifrare dati senza proteggere le chiavi risolve il problema.
- **Falso:** hashing e cifratura sono sinonimi.

### 8. COSA DEVO MEMORIZZARE

**At rest = dati archiviati; in transit = dati in rete. Simmetrica = stessa chiave; asimmetrica = chiave pubblica/privata. Le chiavi sono parte critica della sicurezza.**

---

# 9. DISASTER RECOVERY (DR)

### 1. CHE COS'È

Il **disaster recovery** è l'insieme di strategie, procedure, persone e tecnologie per ripristinare sistemi e dati dopo un evento grave: incendio, indisponibilità di una sede, attacco ransomware, grave errore operativo, guasto esteso.

### 2. A COSA SERVE

Riduce l'impatto delle interruzioni e permette la ripresa del servizio entro tempi e con perdita dati accettabili, definiti prima dell'emergenza.

### 3. CONCETTI FONDAMENTALI

- **Business continuity**: capacità complessiva dell'organizzazione di continuare funzioni critiche; DR è una componente tecnica/operativa della continuità.
- **RTO (Recovery Time Objective)**: tempo massimo accettabile per ripristinare il servizio.
- **RPO (Recovery Point Objective)**: massima perdita di dati accettabile, espressa in tempo.
- **Backup**: copia per recuperare dati; non basta da solo a costituire un piano DR.
- **Replica**: copia sincronizzata o quasi sincronizzata verso altra sede/zona.

### 4. COME FUNZIONA

1. Si identificano servizi critici e dipendenze.
2. Si definiscono RTO e RPO.
3. Si sceglie una strategia: backup/ripristino, sito pilota (pilot light), warm standby o active-active.
4. Si realizzano backup, repliche, runbook e ruoli di emergenza.
5. Si effettuano test periodici di ripristino.

### 5. ESEMPIO CONCRETO

Per il sistema di protocollo, RTO = 4 ore e RPO = 15 minuti. Significa che, dopo un disastro, il servizio deve essere ripristinato entro quattro ore e sono accettabili al massimo 15 minuti di dati non ancora replicati.

### 6. DIFFERENZE IMPORTANTI

| Concetto | Significato |
|---|---|
| Backup | Copia dei dati per recuperarli |
| Alta disponibilità | Riduce fermo per guasti ordinari/locali attraverso ridondanza |
| Disaster recovery | Ripristino dopo eventi gravi/estesi |
| RTO | Quanto tempo può restare fermo il servizio |
| RPO | Quanti dati (in termini temporali) si può perdere |

### 7. ERRORI E TRAPPOLE DA CONCORSO

- **Falso:** RTO misura la quantità di dati perdibili. Quello è RPO.
- **Falso:** fare backup senza provarne il ripristino garantisce il DR.
- **Falso:** alta disponibilità sostituisce sempre il disaster recovery.
- Attenzione: RPO pari a zero richiede nessuna perdita di dati tollerata, quindi soluzioni più complesse e costose.

### 8. COSA DEVO MEMORIZZARE

**RTO = tempo massimo di ripristino; RPO = perdita massima di dati espressa come intervallo di tempo.** Backup è necessario ma non sempre sufficiente; il piano DR va testato.

---

# 10. MAPPA FINALE PER I QUIZ

## Relazioni da riconoscere subito

| Se una domanda parla di… | Risposta/concept corretto più probabile |
|---|---|
| Risorse attivabili dal portale senza intervento umano | On-demand self-service |
| Risorse condivise ma isolate tra clienti | Resource pooling / multi-tenancy |
| Aumento e riduzione automatica delle risorse | Elasticità |
| Aggiungere macchine per sostenere carico | Scalabilità orizzontale (scale out) |
| Potenziare una sola macchina | Scalabilità verticale (scale up) |
| Distribuire richieste su più server | Load balancing |
| VM e hypervisor | Virtualizzazione |
| Software pronto da usare via web | SaaS |
| Runtime/piattaforma gestita per distribuire codice | PaaS |
| VM, rete e dischi come risorse base | IaaS |
| Più provider cloud | Multicloud |
| Privato + pubblico coordinati | Hybrid cloud |
| Applicazione progettata per automazione e scalabilità cloud | Cloud-native |
| Piccoli servizi autonomi via API/eventi | Microservizi |
| Pacchetto applicativo con dipendenze che condivide kernel host | Container |
| Funzioni attivate da eventi, server gestiti dal provider | Serverless/FaaS |
| Infrastruttura definita in file versionati | IaC |
| Git fonte di verità e riconciliazione automatica | GitOps |
| Dati curati per report/BI | Data Warehouse |
| Dati grezzi, eterogenei e schema-on-read | Data Lake |
| Lake con capacità governate/analitiche da warehouse | Lakehouse |
| Chi sei? | Autenticazione |
| Cosa puoi fare? | Autorizzazione |
| Cifratura su dischi/database | At rest |
| Cifratura su rete/HTTPS | In transit |
| Tempo massimo per tornare operativi | RTO |
| Perdita massima di dati tollerata | RPO |

## Dieci affermazioni da ricordare

1. Il cloud non elimina i server fisici: li astrae e li offre come servizio.
2. Virtualizzazione e cloud non sono sinonimi.
3. Elasticità implica anche riduzione delle risorse; scalabilità indica capacità di crescere.
4. Load balancing distribuisce traffico; autoscaling modifica la capacità.
5. IaaS, PaaS e SaaS differiscono per ciò che gestisce provider e cliente.
6. Public cloud non significa dati pubblici; private cloud non significa necessariamente on-premises.
7. Container e VM non sono la stessa cosa; container e microservizi non sono la stessa cosa.
8. Serverless non significa assenza di server, ma assenza della loro gestione diretta.
9. La sicurezza cloud segue un modello di responsabilità condivisa.
10. RTO riguarda il tempo; RPO riguarda la perdita di dati.
