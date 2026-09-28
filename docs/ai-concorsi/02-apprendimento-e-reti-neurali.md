# 2. Apprendimento automatico e reti neurali

## 1. CHE COS'È

Il **Machine Learning (ML)** permette a un sistema di ricavare regolarità dai dati. Le tre grandi modalità da distinguere nei quiz sono:

1. **apprendimento supervisionato**: gli esempi hanno già la risposta corretta;
2. **apprendimento non supervisionato**: gli esempi non hanno una risposta/etichetta e si cercano strutture nei dati;
3. **reinforcement learning (RL)**: un agente compie azioni e impara da ricompense o penalità.

Il **Deep Learning (DL)** è ML basato su reti neurali con molti strati; è particolarmente efficace per dati complessi e non strutturati, come immagini, audio e testo.

---

## 2. A COSA SERVE

| Tecnica | Problema che risolve | Dati necessari | Output tipico |
|---|---|---|---|
| Supervisionato | prevedere una risposta nota | input + etichetta corretta | classe, probabilità, valore numerico |
| Non supervisionato | scoprire strutture sconosciute | solo input | gruppi, anomalie, rappresentazioni |
| Reinforcement learning | scegliere azioni in sequenza | stato, azioni, ricompense/interazioni | politica di decisione |
| Deep Learning | apprendere pattern complessi | molti dati, spesso immagini/testi/audio | classificazione, estrazione, generazione |

---

## 3. CONCETTI FONDAMENTALI

### A. Apprendimento supervisionato

#### Che cosa è

Nel supervisionato ogni esempio di addestramento contiene una coppia:

```text
input → risposta corretta (etichetta)
```

Esempio: `testo di una PEC → ufficio competente`. Il modello osserva molti casi già assegnati e impara a predire la categoria di un nuovo caso.

#### Problemi principali

- **Classificazione**: l'output è una categoria discreta.
  - Esempi: spam/non spam; pratica completa/incompleta; documento edilizio/tributario.
- **Regressione**: l'output è un valore numerico continuo.
  - Esempi: stimare giorni necessari per istruire una pratica; previsione del consumo energetico.

> **Trappola**: una classificazione può avere molte classi, non solo due. Una regressione non è «un risultato approssimato»: è un problema in cui il target è quantitativo.

#### Come apprende: passo per passo

1. Si raccolgono esempi rappresentativi e correttamente etichettati.
2. Il modello produce una previsione.
3. Si calcola una **funzione di perdita** (*loss*), cioè quanto la previsione differisce dalla risposta corretta.
4. L'algoritmo modifica i parametri per ridurre la perdita.
5. Si verifica su dati nuovi che la regola imparata generalizzi.

#### Vantaggi

- Obiettivo chiaro e metriche ben definibili.
- Spesso ottime prestazioni se etichette e dati sono di qualità.
- Utile per automatizzare una previsione ripetitiva.

#### Limiti

- Etichettare dati può essere costoso, lento e soggettivo.
- Replica gli errori delle etichette storiche.
- Non garantisce causalità: può cogliere correlazioni spurie.

### B. Apprendimento non supervisionato

#### Che cosa è

Nel non supervisionato il dataset non contiene l'etichetta da prevedere. Il sistema cerca autonomamente pattern, gruppi, relazioni o casi insoliti.

#### Tecniche da conoscere

- **Clustering**: divide esempi simili in gruppi (*cluster*).
  - Esempio: raggruppare segnalazioni dei cittadini per argomento senza categorie predefinite.
- **Riduzione della dimensionalità**: riduce molte variabili a poche dimensioni informative, utile per visualizzare, comprimere o eliminare rumore.
- **Anomaly detection**: individua osservazioni rare o anomale.
  - Esempio: rilevare richieste di rimborso con combinazioni insolite di dati.

#### Come apprende

Non confronta una previsione con una risposta corretta nota. Ottimizza una nozione di somiglianza, densità, distanza, compattezza dei gruppi o capacità di ricostruire i dati.

#### Vantaggi

- Non richiede etichette.
- Utile per esplorare dati non ancora compresi.
- Può essere un primo passo per costruire un sistema supervisionato.

#### Limiti

- Un gruppo scoperto non è automaticamente significativo o utile.
- Il numero di cluster e la misura di distanza richiedono scelte progettuali.
- Valutare la qualità è più difficile che nel supervisionato.

> **Trappola**: clustering non significa classificazione. Nella classificazione le classi sono note in addestramento; nel clustering i gruppi vengono scoperti dal sistema.

### C. Reinforcement Learning (apprendimento per rinforzo)

#### Che cosa è

Nel RL un **agente** osserva lo **stato** di un ambiente, sceglie un'**azione**, riceve una **ricompensa** (o penalità) e passa a un nuovo stato. L'obiettivo è imparare una **policy**, ossia una strategia che massimizzi la ricompensa cumulativa nel tempo.

```text
stato → azione → ricompensa + nuovo stato → aggiornamento della strategia
```

#### Esempio

Un sistema che regola i semafori può scegliere la durata delle fasi. Una ricompensa può diminuire quando diminuiscono attese e code. Non riceve per ogni incrocio la «risposta giusta» pronta: prova azioni e osserva conseguenze.

#### Concetti essenziali

- **Agente**: chi decide;
- **Ambiente**: ciò con cui interagisce;
- **Stato**: descrizione della situazione corrente;
- **Azione**: scelta possibile;
- **Ricompensa**: segnale numerico di utilità;
- **Policy**: regola che associa stati e azioni;
- **esplorazione**: provare azioni meno note;
- **sfruttamento**: usare l'azione che sembra migliore.

#### Vantaggi

- Adatto a decisioni sequenziali, in cui un'azione modifica le situazioni future.
- Può ottimizzare obiettivi di lungo periodo.

#### Limiti

- Addestramento potenzialmente costoso e instabile.
- La funzione di ricompensa può essere progettata male: l'agente può ottimizzare il numero sbagliato.
- In contesti pubblici reali non è accettabile «sperimentare» liberamente su cittadini o servizi senza forti garanzie; sono spesso necessarie simulazioni, limiti e supervisione.

> **Trappola**: RL non è supervisionato. Entrambi non usano necessariamente etichette classiche, ma nel RL c'è un segnale di ricompensa legato alle azioni e alle conseguenze nel tempo.

---

## 4. COME FUNZIONA: Deep Learning e reti neurali

### Rete neurale artificiale

Una **rete neurale artificiale** è un modello composto da unità computazionali collegate. Ogni unità combina input numerici tramite **pesi**, aggiunge un valore di soglia (*bias*) e applica una trasformazione non lineare detta **funzione di attivazione**.

Non è un cervello umano in miniatura: l'ispirazione biologica è storica e semplificata. È soprattutto un modello matematico parametrico.

Struttura semplificata:

```text
input → strati nascosti (hidden layers) → output
```

- **input layer**: riceve le caratteristiche;
- **hidden layers**: apprendono rappresentazioni intermedie;
- **output layer**: restituisce classe, valore, token successivo o altro output.

### Perché «deep»

Una rete è detta **profonda** quando ha molteplici strati nascosti. Gli strati iniziali possono imparare caratteristiche semplici; quelli successivi combinazioni più astratte.

In un'immagine, per esempio: bordi → forme → parti di oggetti → oggetto. Non è però una regola rigida né una spiegazione completa di ogni rete.

### Apprendimento: forward pass, loss, backpropagation

1. **Forward pass**: l'input attraversa la rete e produce un output.
2. **Loss**: si misura l'errore rispetto al target (se supervisionato).
3. **Backpropagation**: mediante la regola della catena del calcolo differenziale, si calcola come ogni peso ha contribuito all'errore.
4. **Ottimizzazione**: spesso con *gradient descent*, i pesi vengono aggiornati per ridurre la loss.
5. Il ciclo si ripete per molti esempi e più passaggi completi sul dataset (**epoche**).

> **Da ricordare**: la backpropagation non è il modello; è il metodo usato per calcolare efficientemente il contributo dei parametri all'errore durante l'addestramento.

### Vantaggi del Deep Learning

- Può imparare automaticamente molte caratteristiche dai dati grezzi.
- Ottimo per visione, voce, linguaggio e segnali complessi.
- Le architetture pre-addestrate possono essere riusate/adattate (*transfer learning*).

### Limiti

- Richiede spesso grandi dataset, capacità di calcolo ed energia.
- Può essere meno interpretabile di modelli semplici.
- È vulnerabile a dati non rappresentativi, errori di distribuzione e attacchi/adversarial examples.
- Non elimina controlli di qualità, sicurezza e responsabilità.

---

## 5. ARCHITETTURE DA RICONOSCERE

### CNN — Convolutional Neural Network

**Problema che risolve**: analisi di immagini e dati con struttura spaziale locale.

**Dati**: immagini, video, talvolta segnali o matrici.

**Idea**: applica piccoli filtri (*kernel*) che scorrono sull'immagine per trovare pattern locali, come bordi e texture. La condivisione dei pesi riduce i parametri rispetto a una rete pienamente connessa.

**Esempio PA**: riconoscere se una scansione allegata è un documento d'identità, una ricevuta o un modulo; supporto al controllo della qualità delle immagini.

**Vantaggi**: sfrutta vicinanza e struttura spaziale; efficace ed efficiente nella visione.

**Limiti**: non basta da sola per ragionare sul contenuto; la qualità dei dati e l'uso corretto del contesto restano decisivi.

**Differenza**: CNN è specializzata nella struttura locale/spaziale; una RNN gestisce sequenze e un Transformer usa meccanismi di attenzione.

### RNN — Recurrent Neural Network

**Problema che risolve**: dati sequenziali, in cui l'ordine conta.

**Dati**: testo, serie temporali, audio, sequenze di eventi.

**Idea**: conserva uno **stato nascosto** che passa da un elemento al successivo, così l'output corrente dipende anche da elementi precedenti.

**Esempio**: stimare il carico di richieste di un call center pubblico da una serie temporale.

**Vantaggi**: modellano naturalmente l'ordine.

**Limiti**: le RNN tradizionali faticano con dipendenze molto lontane e sono meno parallelizzabili; LSTM e GRU mitigano alcuni problemi, ma oggi molti compiti linguistici usano Transformer.

**Differenza**: RNN elabora tipicamente passo dopo passo; Transformer usa attenzione e può elaborare molti elementi in parallelo durante l'addestramento.

### Transformer

**Problema che risolve**: modellare relazioni tra elementi di una sequenza, anche distanti tra loro; oggi è centrale nel linguaggio e diffuso anche in immagini e altri domini.

**Dati**: token di testo, porzioni di immagini, sequenze e rappresentazioni numeriche.

**Idea chiave — attenzione (*self-attention*)**: per elaborare un token, il modello assegna pesi agli altri token rilevanti nella sequenza. In questo modo può collegare parole lontane.

Poiché l'attenzione da sola non conosce l'ordine, si aggiunge un'informazione di posizione (**positional encoding** o embedding posizionale).

**Esempio**: analisi e sintesi di documenti lunghi, classificazione di istanze, traduzione, LLM.

**Vantaggi**: gestisce bene dipendenze a lunga distanza; addestramento altamente parallelizzabile; grande scalabilità.

**Limiti**: costi di calcolo e memoria; limite di contesto; non garantisce veridicità o comprensione umana.

> **Trappola**: Transformer non è sinonimo di LLM. Un LLM è spesso costruito con Transformer, ma Transformer è un'architettura utilizzabile in più tipi di modello e compiti.

### Embeddings

Un **embedding** è una rappresentazione numerica densa di un oggetto (parola, frase, documento, immagine, utente) in uno spazio vettoriale. Oggetti semanticamente o funzionalmente simili tendono ad avere vettori vicini secondo una misura come la similarità coseno.

**Problema**: confrontare e usare nel calcolo oggetti complessi, per esempio frasi con parole diverse ma significato simile.

**Esempio PA**: cercare tra FAQ, norme e procedimenti la documentazione più pertinente alla domanda in linguaggio naturale di un cittadino.

**Vantaggi**: ricerca semantica, clustering, raccomandazione, recupero documentale per RAG.

**Limiti**: «vicino nello spazio vettoriale» non equivale a vero, lecito, aggiornato o giuridicamente applicabile. L'embedding può incorporare bias e dipende dal modello e dal dominio.

---

## 6. ESEMPIO CONCRETO: gestione delle segnalazioni comunali

Un Comune riceve segnalazioni con testo, foto e geolocalizzazione.

1. **Supervisionato**: propone la categoria (illuminazione, rifiuti, strada) basandosi su segnalazioni già etichettate.
2. **CNN o modello di visione**: identifica se la foto contiene, ad esempio, un cassonetto o un dissesto stradale; il risultato è solo un supporto.
3. **Non supervisionato**: trova cluster emergenti di segnalazioni per individuare problemi ricorrenti non previsti dalle categorie.
4. **Embedding + ricerca semantica**: collega la segnalazione alle istruzioni e ai procedimenti pertinenti.
5. Un operatore verifica il risultato, soprattutto nei casi con effetti su priorità, sicurezza o diritti.

---

## 7. DIFFERENZE IMPORTANTI

| Concetti confondibili | Differenza da ricordare |
|---|---|
| Classificazione / clustering | classificazione: etichette note; clustering: gruppi scoperti senza etichette |
| Regressione / classificazione | regressione: valore numerico; classificazione: categoria |
| Supervisionato / RL | supervisionato: risposta corretta per esempio; RL: ricompensa da conseguenze di azioni sequenziali |
| Rete neurale / Deep Learning | una rete può essere semplice; DL indica reti con più strati e metodi/scalabilità connessi |
| CNN / RNN | CNN: pattern locali/spaziali; RNN: ordine sequenziale tramite stato ricorrente |
| RNN / Transformer | RNN: elaborazione ricorrente sequenziale; Transformer: attenzione, maggiore parallelismo |
| Embedding / database | embedding: vettore che cattura somiglianze apprese; database: sistema per memorizzare e interrogare dati |

---

## 8. ERRORI E TRAPPOLE DA CONCORSO

- «Nel non supervisionato i dati sono etichettati»: **falso**.
- «Il reinforcement learning riceve sempre la soluzione esatta per ogni azione»: **falso**; riceve tipicamente ricompense, anche ritardate.
- «CNN significa rete per il linguaggio»: **falso**; è associata soprattutto a dati spaziali/immagini, pur con possibili altri usi.
- «RNN e Transformer sono sinonimi»: **falso**.
- «Embedding è una parola codificata con un solo numero»: **falso**; è normalmente un vettore di numeri.
- «Una rete neurale sostituisce sempre feature engineering e controllo dei dati»: **falso**.
- «Il deep learning è sempre preferibile»: **falso**; un modello più semplice può essere più economico, spiegabile e adeguato.

---

## 9. COSA DEVO MEMORIZZARE

- **Supervisionato = input + etichetta**; classificazione o regressione.
- **Non supervisionato = solo input**; clustering, riduzione dimensionalità, anomalie.
- **RL = stato, azione, ricompensa, policy**; decisioni sequenziali.
- **DL = ML con reti neurali profonde**.
- **CNN = convoluzioni, dati spaziali/immagini**.
- **RNN = sequenze e stato ricorrente**.
- **Transformer = self-attention + posizione; base frequente degli LLM**.
- **Embedding = vettore che rappresenta somiglianza/relazioni, non una prova di verità**.
