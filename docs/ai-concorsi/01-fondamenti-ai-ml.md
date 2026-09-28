# 1. Fondamenti: AI, Machine Learning, conoscenza e dati

## 1. CHE COS'È

### Intelligenza Artificiale (AI o IA)

L'**Intelligenza Artificiale** è la disciplina che progetta sistemi capaci di svolgere compiti che, se eseguiti da una persona, richiederebbero capacità come percepire, classificare, ragionare, apprendere, pianificare, comprendere o produrre linguaggio.

La parola decisiva è **compito**, non «coscienza». Un sistema di IA può essere molto efficace in un compito delimitato senza essere intelligente in senso umano generale.

Esempi: filtro antispam, riconoscimento di una targa, previsione di un ritardo, chatbot informativo, rilevazione di frodi, motore che applica regole tributarie.

### AI debole e AI forte

- **AI debole** (o *narrow AI*): sistema costruito per uno o pochi compiti specifici. È l'IA realmente usata oggi: un assistente linguistico, un classificatore di documenti, un sistema di visione.
- **AI forte** (o *AGI*, Artificial General Intelligence, nell'uso comune): ipotesi di una macchina con capacità cognitive generali comparabili a quelle umane, trasferibili autonomamente tra problemi molto diversi. Non è un risultato consolidato della tecnologia corrente.

> **Trappola**: «debole» non significa inutile o poco potente. Un sistema ristretto può superare l'uomo in un singolo compito.

### Machine Learning (ML)

Il **Machine Learning** è un sottoinsieme dell'AI in cui un modello ricava dai dati una regola o una funzione utile, invece di ricevere tutte le istruzioni esplicite scritte da un programmatore.

Nel software tradizionale, semplificando: `regole + dati → risultato`.
Nel ML: `dati di esempio + risultati desiderati → modello`; poi `modello + nuovi dati → previsione/decisione`.

### Statistical Learning

Lo **Statistical Learning** è il quadro matematico-statistico che studia come inferire relazioni e fare previsioni da dati incerti e finiti. Molto ML è statistical learning: regressione, classificazione, stima della probabilità, validazione e generalizzazione.

Non coincide con tutte le AI: un sistema esperto puramente a regole può essere AI senza apprendere statisticamente.

### Knowledge Representation and Reasoning (KRR)

**Knowledge Representation and Reasoning** significa **rappresentazione della conoscenza e ragionamento**. Invece di partire soprattutto da grandi quantità di esempi, si descrivono fatti, concetti, relazioni e regole; il sistema deriva conclusioni con un motore di inferenza.

Esempio:

```text
Fatto: Mario ha presentato la domanda entro il termine.
Regola: se la domanda è completa e presentata entro il termine, allora è ammissibile.
Fatto: la domanda di Mario è completa.
Conclusione: la domanda di Mario è ammissibile.
```

KRR è utile quando le regole devono essere esplicite, verificabili e motivate. Non elimina il bisogno di interpretare correttamente norme e dati.

---

## 2. A COSA SERVE

L'AI serve ad affrontare problemi per cui le regole sono troppe, poco chiare, variabili o ricavabili meglio da esempi. Le principali famiglie di problemi sono:

| Problema | Esempio | Tecniche tipiche |
|---|---|---|
| Classificare | email spam/non spam; pratica corretta/incompleta | ML supervisionato |
| Prevedere un valore | tempo di attesa; consumo energetico | regressione, ML |
| Scoprire gruppi | utenti con comportamenti simili | ML non supervisionato |
| Decidere una sequenza di azioni | gestione semafori; allocazione risorse | reinforcement learning, pianificazione |
| Interpretare testo, immagini o audio | estrazione dati da una domanda; OCR | deep learning, NLP, visione |
| Applicare regole e motivare conclusioni | verifica requisiti di un bando | KRR, sistemi esperti |
| Creare contenuti | bozza di risposta, riassunto, codice | AI generativa |

Nella PA l'obiettivo corretto non è «mettere l'AI ovunque», ma migliorare un processo definito: ridurre tempi, aumentare accessibilità, assistere operatori, intercettare anomalie o rendere più semplice l'accesso a informazioni pubbliche.

---

## 3. CONCETTI FONDAMENTALI

### Dati, caratteristiche, etichetta e modello

- **Dato**: osservazione disponibile, per esempio età della pratica, comune, testo della richiesta, immagine allegata.
- **Feature** (*caratteristica* o variabile di input): informazione usata dal modello per decidere o stimare. Esempio: numero di allegati.
- **Etichetta/target**: risposta nota da imparare nel supervisionato. Esempio: «pratica completa» / «incompleta».
- **Modello**: struttura matematica/computazionale che trasforma input in output.
- **Parametri**: valori interni appresi dal modello (per esempio i pesi di una rete neurale).
- **Inferenza**: uso del modello addestrato su un nuovo caso. Non è sinonimo di addestramento.

### Addestramento, validazione e test

Per evitare di valutare un modello sugli stessi esempi con cui ha imparato, si separano normalmente i dati:

1. **training set**: il modello apprende i parametri;
2. **validation set**: si scelgono configurazione e soglie, controllando il comportamento durante lo sviluppo;
3. **test set**: verifica finale su dati non usati per le decisioni di sviluppo.

Il principio da ricordare è la **generalizzazione**: un modello è utile se funziona anche su dati nuovi, non solo se ricorda quelli visti.

### Overfitting e underfitting

- **Overfitting** (*sovra-adattamento*): il modello memorizza dettagli e rumore dei dati di addestramento; prestazione alta sul training, peggiore sui casi nuovi.
- **Underfitting** (*sotto-adattamento*): modello troppo semplice o poco addestrato; non coglie neppure la relazione principale.

> Trappola: più complesso non significa automaticamente migliore. Può aumentare l'overfitting, il costo, l'opacità e il fabbisogno di dati.

### Qualità, bias e rappresentatività

Un modello non rende magicamente corretti dati errati. Dati incompleti, obsoleti, sbilanciati o raccolti con criteri distorti possono produrre risultati discriminatori o inaccurati.

**Bias** può indicare due cose da distinguere:

1. in statistica, una distorsione sistematica della stima;
2. nell'uso socio-tecnico, una disparità o distorsione ingiusta prodotta o amplificata dal sistema e dal contesto.

La qualità non riguarda solo il formato: comprende provenienza, liceità, aggiornamento, completezza, accuratezza, documentazione e pertinenza rispetto allo scopo.

### Metriche: non basta «accuratezza»

In una classificazione binaria:

- **vero positivo (TP)**: il sistema segnala correttamente un caso positivo;
- **falso positivo (FP)**: segnala positivo un caso che non lo è;
- **falso negativo (FN)**: non segnala un caso che lo è;
- **vero negativo (TN)**: riconosce correttamente un caso negativo.

| Metrica | Idea | Quando conta |
|---|---|---|
| Accuracy | quota complessiva di risposte corrette | classi bilanciate e costi simili degli errori |
| Precision | tra i positivi segnalati, quanti sono davvero positivi | quando falsi allarmi sono costosi |
| Recall/sensibilità | tra i positivi reali, quanti vengono trovati | quando perdere un caso è grave |
| F1-score | equilibrio tra precision e recall | quando entrambe contano |

Esempio: se solo l'1% delle pratiche è fraudolento, un sistema che dice sempre «non fraudolenta» ha 99% di accuracy ma è inutile per trovare frodi. È una classica domanda da concorso.

---

## 4. COME FUNZIONA: ciclo di vita generale di un sistema AI

1. **Definire il problema e lo scopo**: quale decisione o assistenza serve? Quale risultato è accettabile?
2. **Valutare liceità, rischi e processo**: prima di scegliere la tecnologia; per la PA contano finalità pubblica, diritti, trasparenza, protezione dei dati e responsabilità.
3. **Raccogliere e preparare i dati**: pulizia, anonimizzazione/pseudonimizzazione quando pertinente, controllo qualità e documentazione.
4. **Scegliere l'approccio**: regole/KRR se la logica è esplicita; ML se vi sono esempi; generativa se occorre produrre contenuto, ma con controlli.
5. **Addestrare o configurare** il modello.
6. **Valutare** prestazioni, errori, equità, robustezza e sicurezza su casi rappresentativi.
7. **Mettere in esercizio** con ruoli chiari, log, supervisione e canali di segnalazione.
8. **Monitorare**: i dati e il contesto cambiano (*data drift*); occorre verificare che il modello resti adeguato.

---

## 5. ESEMPIO CONCRETO: supporto alla protocollazione nella PA

Un ente riceve migliaia di PEC con documenti. Può usare:

- **OCR/visione** per estrarre il testo dai PDF scansionati;
- **classificatore supervisionato** per proporre categoria e ufficio competente, addestrato su pratiche già classificate;
- **KRR** per applicare regole formali di instradamento (per esempio competenza territoriale);
- **LLM con recupero di fonti** per assistere l'operatore nella lettura, non per decidere autonomamente l'esito di un diritto del cittadino.

L'operatore controlla e conferma. I vantaggi sono velocità e uniformità; i limiti sono documenti atipici, qualità dell'OCR, dati storici errati e rischio di affidamento eccessivo alla proposta automatica.

---

## 6. DIFFERENZE IMPORTANTI

| Concetto | Che cosa è | Da non confondere con |
|---|---|---|
| AI | campo ampio di sistemi che svolgono compiti intelligenti | non equivale a ML o a reti neurali |
| ML | AI che apprende pattern dai dati | non richiede necessariamente deep learning |
| Statistical Learning | fondamento statistico dell'apprendere dai dati | non è l'intera AI |
| KRR | conoscenza esplicita + inferenza | non apprende per forza da grandi dataset |
| Algoritmo | procedura finita per risolvere un problema | un algoritmo non è automaticamente AI |
| Modello | funzione/struttura che produce output da input | non è sinonimo di dataset |
| Addestramento | fase in cui si stimano parametri | non è l'inferenza operativa |

---

## 7. ERRORI E TRAPPOLE DA CONCORSO

1. **«Il ML è programmato senza dati»**: falso. I dati sono centrali; il codice definisce metodo e obiettivo, ma i parametri si apprendono dai dati.
2. **«Un sistema a regole non è AI»**: falso. KRR e sistemi esperti sono approcci classici dell'AI.
3. **«Se l'accuracy è alta, il sistema è certamente valido»**: falso; dipende da squilibrio delle classi, costi di FP/FN e contesto.
4. **«L'AI elimina la responsabilità umana»**: falso. Chi progetta, fornisce, adotta o usa il sistema conserva responsabilità secondo il proprio ruolo.
5. **«I dati storici sono oggettivi per definizione»**: falso; possono contenere errori, scelte passate e discriminazioni.
6. **«Spiegabile significa infallibile»**: falso. Spiegabilità e accuratezza sono proprietà diverse.

---

## 8. COSA DEVO MEMORIZZARE

- **AI**: campo generale; include approcci simbolici, statistici e ML.
- **AI debole**: specializzata; è quella concretamente diffusa.
- **AI forte/AGI**: concetto di capacità generale umana; non è il nome corretto dei comuni chatbot.
- **ML**: apprendimento dai dati per generalizzare su casi nuovi.
- **KRR**: fatti + relazioni + regole + inferenza.
- **Generalizzazione**: capacità di funzionare su dati non visti.
- **Overfitting**: ottimo sui dati noti, scarso sui nuovi.
- **Training / validation / test**: apprendere / scegliere e controllare / valutare in modo finale.
- **Precision ≠ recall**: la prima riduce falsi positivi, la seconda riduce falsi negativi.
