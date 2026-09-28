# Intelligenza Artificiale — guida per concorsi informatici

> **Obiettivo**: capire i concetti, riconoscerli in un quiz a risposta multipla e distinguere l'affermazione corretta da quella soltanto plausibile.
>
> **Aggiornamento normativo verificato al 28 settembre 2026**: per le date e gli obblighi giuridici leggere anche il file `04-normativa-ai-act-pa-e-quiz.md`.

## Come usare questa guida

L'ordine è intenzionale: prima la mappa generale, poi i metodi di apprendimento, poi l'IA generativa e infine norme e PA. Non memorizzare subito le sigle: per ciascuna chiediti sempre:

1. **Quale problema risolve?**
2. **Che dati riceve?**
3. **Come ricava una soluzione?**
4. **Che cosa produce in uscita?**
5. **Quale limite rende sbagliata una risposta troppo assoluta?**

## Percorso di studio

| File | Contenuto |
|---|---|
| `01-fondamenti-ai-ml.md` | AI, AI debole/forte, ML, Statistical Learning, KRR, dati e ciclo di vita |
| `02-apprendimento-e-reti-neurali.md` | supervisionato, non supervisionato, reinforcement learning, deep learning, reti neurali, CNN, RNN, Transformer, embeddings |
| `03-generativa-llm-agenti.md` | IA generativa, LLM, prompt, RAG, allucinazioni, agenti e sistemi multiagente |
| `04-normativa-ai-act-pa-e-quiz.md` | AI Act, normativa italiana, IA nella PA, classificazioni di rischio, obblighi, tabelle e trappole da concorso |

## La mappa in una frase

**L'Intelligenza Artificiale (AI/IA) è l'area più ampia; il Machine Learning (ML) è un modo per costruire sistemi di IA facendo apprendere schemi dai dati; il Deep Learning (DL) è una famiglia di ML basata su reti neurali profonde; l'IA generativa crea nuovi contenuti; un LLM è un particolare modello generativo specializzato nel linguaggio.**

## Schema gerarchico essenziale

```text
Intelligenza artificiale (AI)
├─ approcci simbolici / basati sulla conoscenza
│  ├─ Knowledge Representation and Reasoning (KRR)
│  └─ sistemi esperti, regole, inferenza, pianificazione
├─ approcci statistici
│  └─ Statistical Learning
├─ Machine Learning (ML)
│  ├─ supervisionato
│  ├─ non supervisionato
│  ├─ reinforcement learning
│  └─ Deep Learning (DL)
│     ├─ reti neurali profonde
│     ├─ CNN
│     ├─ RNN
│     └─ Transformer
└─ AI generativa
   ├─ testo, immagini, audio, codice, video
   └─ Large Language Models (LLM): modelli generativi per il linguaggio
      └─ possono essere componenti di agenti e sistemi multiagente
```

## Regola d'oro per i quiz

Evita le equivalenze assolute:

- «AI = reti neurali» è **falso**: esistono anche sistemi a regole, ricerca, pianificazione e modelli statistici non neurali.
- «ML = AI» è **falso**: ML è un sottoinsieme dell'AI.
- «Deep Learning = qualsiasi ML» è **falso**: è una famiglia particolare di ML.
- «Un LLM capisce come un essere umano» è **falso o almeno improprio**: genera testo stimando regolarità statistiche e può produrre errori convincenti.
- «Un output dell'AI è automaticamente una decisione valida della PA» è **falso**: servono base giuridica, controlli, responsabilità, qualità dei dati e, quando richiesto, supervisione umana.
