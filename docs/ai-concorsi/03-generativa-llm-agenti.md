# 3. IA generativa, LLM, agenti e sistemi multiagente

## 1. CHE COS'È

### AI generativa

L'**Intelligenza Artificiale generativa** (*Generative AI*) è una famiglia di sistemi capaci di produrre nuovi contenuti: testo, immagini, audio, video, codice, dati sintetici o combinazioni di essi. «Nuovi» non significa necessariamente originali, corretti o privi di riferimenti ai dati di addestramento: significa che il sistema genera un output, non si limita a scegliere una classe predefinita.

Esempi: redigere una bozza, tradurre, riassumere, generare un'immagine, completare codice, estrarre informazioni in forma discorsiva.

### Large Language Model (LLM)

Un **Large Language Model** è un grande modello linguistico, di solito basato su Transformer, addestrato su grandi collezioni di testo. Riceve testo suddiviso in **token** (pezzi di parole, parole o simboli) e impara a stimare quale token sia più probabile nel contesto.

Un LLM è quindi:

- un modello di linguaggio;
- spesso generativo;
- spesso costruito con architettura Transformer;
- non necessariamente un agente;
- non una banca dati affidabile di verità né un motore giuridico autonomo.

### Prompt

Il **prompt** è l'istruzione e il contesto forniti al modello. Può contenere obiettivo, fonti, vincoli, ruolo, formato desiderato ed esempi.

Un prompt migliore può migliorare l'output, ma **non trasforma** un modello in una fonte certa, né sostituisce la verifica di dati e norme.

---

## 2. A COSA SERVE

| Tecnologia | Problema che risolve | Dati/risorse necessarie | Output |
|---|---|---|---|
| AI generativa | creare o trasformare contenuti | dati di addestramento; prompt; talvolta fonti recuperate | testo, immagine, codice, audio ecc. |
| LLM | comprendere/trasformare/generare linguaggio | token testuali; modello pre-addestrato; contesto | testo, estrazioni, classificazioni, sintesi |
| RAG | rispondere usando fonti esterne pertinenti | corpus documentale indicizzato + LLM | risposta con base documentale recuperata |
| Agente AI | raggiungere un obiettivo eseguendo azioni | modello, strumenti, memoria/stato, policy | piano, chiamate a strumenti, risultato |
| Sistema multiagente | suddividere un compito tra più entità | più agenti, ruoli, protocollo di coordinamento | soluzione coordinata |

---

## 3. CONCETTI FONDAMENTALI

### Come un LLM genera testo

In modo molto semplificato:

1. il testo in ingresso viene spezzato in token;
2. ogni token è convertito in embedding;
3. il Transformer usa attenzione e contesto per calcolare una distribuzione di probabilità sui possibili token successivi;
4. un metodo di selezione (*decoding*) sceglie il token;
5. il ciclo continua finché termina la risposta.

Il modello non consulta automaticamente Internet, un archivio aggiornato o una fonte normativa. Può farlo solo se l'applicazione gli collega strumenti o documenti.

### Pre-training, fine-tuning e istruzioni

- **Pre-training**: addestramento generale su grandi quantità di testo, spesso con obiettivo di previsione del token successivo o ricostruzione di parti mancanti.
- **Fine-tuning**: ulteriore addestramento su esempi di uno specifico compito o dominio.
- **Instruction tuning**: fine-tuning per seguire istruzioni in modo utile.
- **Allineamento**: insieme di metodi per orientare comportamento, sicurezza e qualità rispetto a preferenze/criteri umani. Non è una garanzia assoluta.

### Allucinazioni

Un'**allucinazione** è un output apparentemente plausibile ma falso, inventato o non supportato da fonti. Può riguardare fatti, citazioni, riferimenti normativi, numeri o passaggi logici.

Non è necessariamente un «bug eccezionale»: deriva dal fatto che il modello ottimizza la produzione di testo probabile/coerente, non la verità garantita. Il rischio aumenta con richieste ambigue, fonti assenti, domande molto specifiche o informazioni aggiornate.

### RAG — Retrieval-Augmented Generation

Il **RAG** (*generazione aumentata dal recupero*) unisce recupero di documenti e generazione:

1. si divide un corpus in porzioni e se ne calcolano embeddings;
2. alla domanda dell'utente si cercano le porzioni semanticamente più pertinenti;
3. le porzioni recuperate vengono inserite nel contesto dell'LLM;
4. il modello formula la risposta, idealmente con citazioni o riferimenti verificabili.

**Problema risolto**: collegare il modello a documenti aggiornabili e specifici senza dover riaddestrare il modello per ogni modifica.

**Vantaggi**: maggiore tracciabilità, aggiornabilità del contenuto, riduzione (non eliminazione) delle allucinazioni.

**Limiti**: se il recupero trova documenti errati, incompleti o obsoleti, l'output resta inaffidabile; le fonti vanno governate e l'LLM può interpretarle male.

> **Trappola**: RAG non è fine-tuning. RAG aggiunge fonti al contesto al momento della domanda; fine-tuning modifica i parametri del modello tramite addestramento.

### Tool use e function calling

Un LLM può essere integrato con strumenti: ricerca su documenti autorizzati, calcolo, sistemi gestionali, database o API. Il modello può proporre una chiamata strutturata; l'applicazione esegue l'azione secondo autorizzazioni e controlli.

Il fatto che un modello possa invocare uno strumento non significa che debba avere accesso illimitato. In un contesto PA valgono principio di minima autorizzazione, separazione dei ruoli, registrazione delle operazioni e conferme per azioni rilevanti.

---

## 4. COME FUNZIONA: agente e sistema multiagente

### Agente AI

Un **agente** è un sistema che persegue un obiettivo osservando un contesto, pianificando o scegliendo passi, usando eventualmente strumenti, controllando gli esiti e aggiornando il proprio stato/memoria.

Un agente può usare un LLM come componente di ragionamento linguistico, ma i due concetti non coincidono:

- un LLM può limitarsi a rispondere a un prompt;
- un agente può pianificare, usare strumenti e compiere più passi;
- un agente non deve necessariamente usare un LLM (può essere basato su regole, pianificazione o RL).

Schema tipico:

```text
obiettivo → osservazione → piano/decisione → azione su strumento → osservazione esito → eventuale correzione → risultato
```

#### Esempio PA

Un agente di supporto interno può:

1. ricevere una domanda su un procedimento;
2. cercare nei documenti ufficiali autorizzati;
3. estrarre scadenze e modulistica;
4. preparare una bozza di risposta;
5. sottoporla all'operatore, che approva prima dell'invio.

Non dovrebbe autonomamente adottare provvedimenti, modificare fascicoli o inviare comunicazioni vincolanti senza regole, deleghe e controlli adeguati.

### Sistemi multiagente (MAS)

Un **sistema multiagente** (*Multi-Agent System*, MAS) contiene più agenti autonomi o semi-autonomi che cooperano, coordinano o talvolta competono.

Ogni agente può avere un ruolo: ricerca di fonti, estrazione dati, controllo qualità, pianificazione, verifica conformità. Serve un protocollo di coordinamento: chi fa che cosa, quale informazione condivide, chi risolve conflitti e chi è responsabile del risultato.

#### Vantaggi

- scomposizione di compiti complessi;
- specializzazione dei ruoli;
- possibile robustezza e parallelizzazione.

#### Limiti

- errori che si propagano da un agente all'altro;
- costi, difficoltà di audit e responsabilità;
- non basta avere più agenti per ottenere risposte più vere;
- il coordinamento non elimina l'obbligo di controllo umano e organizzativo.

---

## 5. ESEMPIO CONCRETO: assistente documentale per un ente

Un ente vuole assistere gli operatori che rispondono a domande frequenti su un servizio.

1. **Corpus governato**: regolamenti, moduli, FAQ e pagine istituzionali vengono selezionati, datati e versionati.
2. **RAG**: la domanda dell'operatore attiva la ricerca nei soli documenti autorizzati.
3. **LLM**: redige una bozza breve, evidenziando la fonte e dichiarando quando non trova elementi sufficienti.
4. **Regole**: impediscono la generazione di una risposta definitiva in casi che richiedono istruttoria individuale.
5. **Supervisione umana**: l'operatore controlla, integra e invia.
6. **Log e monitoraggio**: si conservano richieste, fonti recuperate, versione del modello e approvazione, nel rispetto delle regole applicabili.

Questo è più sicuro di un chatbot generico che risponde soltanto «in base a ciò che sa», ma non rende automaticamente la risposta corretta.

---

## 6. DIFFERENZE IMPORTANTI

| Concetti confondibili | Differenza essenziale |
|---|---|
| AI generativa / AI tradizionale | la prima genera contenuti; l'altra può anche classificare, prevedere, pianificare o applicare regole senza generare testo |
| LLM / AI generativa | un LLM è un tipo di modello generativo linguistico; AI generativa include anche immagini, audio, video e codice |
| LLM / chatbot | LLM è il modello; chatbot è l'applicazione/interfaccia conversazionale che può usare un LLM o regole |
| LLM / agente | LLM genera/elabora linguaggio; agente persegue obiettivi tramite ciclo di osservazione-decisione-azione |
| RAG / fine-tuning | RAG recupera fonti a runtime; fine-tuning addestra ulteriormente e cambia i parametri |
| Prompt / addestramento | prompt condiziona una singola interazione; addestramento modifica il comportamento appreso del modello |
| Embedding / generazione | embedding rappresenta oggetti come vettori; generazione produce un nuovo output |

---

## 7. ERRORI E TRAPPOLE DA CONCORSO

1. **«Un LLM è un database»**: falso. Può aver appreso informazioni, ma non interroga per definizione un archivio aggiornato e non garantisce citazioni corrette.
2. **«Un LLM verifica automaticamente la verità delle proprie frasi»**: falso.
3. **«RAG elimina le allucinazioni»**: falso; le riduce se fonti e recupero sono ben progettati, ma non le annulla.
4. **«Un agente è necessariamente un robot fisico»**: falso. Può essere software.
5. **«Un sistema multiagente è un unico grande modello»**: falso. È una composizione di più agenti che interagiscono.
6. **«Se l'output è ben scritto è corretto»**: falso; fluidità linguistica e accuratezza fattuale sono proprietà distinte.
7. **«Prompt engineering sostituisce autorizzazioni e controlli di sicurezza»**: falso.

---

## 8. COSA DEVO MEMORIZZARE

- **AI generativa**: produce nuovi contenuti.
- **LLM**: grande modello linguistico, spesso Transformer, che lavora su token e probabilità condizionate dal contesto.
- **Token**: unità in cui il testo viene rappresentato/elaborato.
- **Allucinazione**: contenuto plausibile ma falso o non supportato.
- **RAG**: recupera documenti pertinenti e li porta nel contesto della generazione; non è fine-tuning.
- **Agente**: percepisce/valuta/decide/agisce verso un obiettivo, anche con strumenti.
- **MAS**: più agenti coordinati; più agenti non significa automaticamente più affidabilità.
- Per usi amministrativi: **fonti governate, limitazione degli strumenti, supervisione, tracciabilità e verifica** sono concetti chiave.
