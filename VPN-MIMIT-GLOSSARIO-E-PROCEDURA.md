# Glossario e procedura VPN MIMIT

## Obiettivo del documento
Questo documento spiega in modo chiaro:

1. i termini tecnici usati nella procedura VPN;
2. il significato dei comandi;
3. il flusso operativo completo per collegarsi alla rete MIMIT dal server Linux di sviluppo/integrazione.

L’obiettivo è capire **cosa fa ogni comando** e **perché viene eseguito**.

---

# 1. Concetti fondamentali

## 1.1 VPN
VPN significa **Virtual Private Network**, cioè **rete privata virtuale**.

È una tecnologia che permette a una macchina di collegarsi a una rete remota in modo sicuro, come se fosse fisicamente dentro quella rete.

### A cosa serve
- Protegge il traffico con cifratura.
- Permette di raggiungere server e servizi interni non esposti pubblicamente.
- Consente l’accesso a risorse private da una macchina remota autorizzata.

### Nel vostro caso
La VPN serve a permettere al server Linux di sviluppo/integrazione di raggiungere le risorse interne del MIMIT.

---

## 1.2 Client VPN
Il **client VPN** è il programma che avvia la connessione verso la VPN.

### Funzione
Il client:
- contatta il gateway VPN;
- invia le credenziali;
- avvia la connessione che porterà alla creazione del tunnel;
- mantiene attivo il collegamento mentre la VPN è in uso.

### Nel vostro caso
Il client è il comando:

```bash
startct
```

Questo è il programma usato per aprire la VPN dal server Linux.

---

## 1.3 Gateway VPN
Il **gateway VPN** è il punto di ingresso della VPN.

### Che cos’è un gateway, in generale
Un gateway è un punto di passaggio tra due reti diverse. In una VPN, il gateway è il “cancello” di ingresso verso la rete privata remota.

### Funzione del gateway VPN
È il server che:
- riceve la connessione del client;
- verifica username, password e OTP/token;
- autorizza o rifiuta l’accesso;
- termina la connessione VPN sul lato remoto e consente l’accesso alla rete interna autorizzata.

### Nel vostro caso
Il gateway è:

```bash
consulenti.mise.gov.it
```

Quando il client si collega a questo indirizzo, sta contattando il punto di accesso della VPN MIMIT.

### Importante
Il gateway **non è** il database finale o il server applicativo interno.  
È il punto di ingresso alla rete remota.

---

## 1.4 Tunnel VPN
Il **tunnel** è il collegamento protetto stabilito tra il client VPN e il gateway VPN.

### Funzione
Il tunnel:
- cifra il traffico;
- trasporta i pacchetti tra il server Linux e il gateway VPN;
- permette di raggiungere le risorse interne autorizzate.

### Immagine mentale
Puoi immaginarlo come un corridoio protetto tra due reti.

### Nel sistema Linux
Il tunnel compare spesso come un’interfaccia di rete virtuale, ad esempio:

```bash
tun0
```

---

## 1.5 Interfaccia di rete
Un’interfaccia di rete è il punto attraverso cui il sistema operativo invia e riceve traffico di rete.

### Due casi comuni
- **Interfaccia fisica**: una scheda di rete reale, collegata a un cavo o a una scheda wireless.
- **Interfaccia virtuale**: una porta software gestita dal sistema operativo o da un programma, non legata a un cavo fisico.

### Nel vostro caso
`tun0` è un’interfaccia di rete virtuale usata dalla VPN.

---

## 1.6 Interfaccia `tun0`
`tun0` è una tipica interfaccia di rete virtuale usata dalle VPN.

### A cosa serve
Quando la VPN è attiva, il traffico destinato alla rete interna passa su questa interfaccia.

### Perché è importante
Se il DNS o le rotte non sono impostate bene su `tun0`, il server potrebbe non raggiungere i nomi interni MIMIT.

### Idea semplice
`tun0` è il punto di uscita/ingresso locale del traffico VPN sul server Linux.

Non è il gateway, non è il server remoto e non è il database: è una porta software locale usata per instradare il traffico del tunnel.

---

## 1.7 DNS
Il DNS è il sistema che traduce i nomi host in indirizzi IP.

### Esempio
Il nome:

```bash
mysqlclustertest.risorse.mise
```

deve essere trasformato in un indirizzo IP per poter essere raggiunto in rete.

### Nel vostro caso
Serve un DNS interno del MIMIT, perché i nomi privati non si risolvono con i DNS pubblici.

---

## 1.8 `resolvectl`
`resolvectl` è un comando di Linux che permette di vedere e configurare lo stato della risoluzione DNS gestita dal sistema.

### A cosa serve in questo contesto
Serve per controllare quali DNS sono in uso e, se necessario, associare un DNS specifico a una certa interfaccia, ad esempio `tun0`.

---

## 1.9 `resolvectl status`
Il comando:

```bash
resolvectl status
```

mostra lo stato attuale della configurazione DNS del sistema.

### Cosa mostra
Può mostrare, tra le altre cose:
- quali interfacce di rete sono presenti;
- quali server DNS sono associati a ciascuna interfaccia;
- quale DNS è attualmente utilizzato;
- eventuali domini di ricerca configurati.

### Cosa non fa
- non modifica la configurazione;
- non apre la VPN;
- non esegue una query DNS verso un host specifico.

### Perché è utile
Serve a capire **come è configurato il DNS in quel momento**, soprattutto dopo l’attivazione della VPN.

---

## 1.10 `resolvectl dns`
Il comando:

```bash
sudo resolvectl dns tun0 10.184.3.133
```

imposta il server DNS `10.184.3.133` per l’interfaccia `tun0`.

### Cosa significa in pratica
Dice al sistema:

> “Quando il traffico passa da `tun0`, usa questo server DNS per risolvere i nomi.”

### Cosa fa e cosa non fa
- **Fa**: configura il DNS associato a quella interfaccia.
- **Non fa**: non verifica da solo che il nome host sia risolvibile.

---

## 1.11 `tmux`
`tmux` è un terminal multiplexer.

### Funzione
Permette di:
- aprire una sessione di terminale persistente;
- lasciare in esecuzione un processo anche se ci si disconnette dalla shell SSH;
- ricollegarsi in seguito alla stessa sessione.

### Perché viene usato qui
Serve a mantenere viva la sessione in cui gira la VPN.

---

## 1.12 PuTTY
PuTTY è un client SSH.

### Funzione
Serve a collegarsi al server Linux remoto da cui si esegue la procedura.

### Nel vostro caso
Con PuTTY si entra nel server di sviluppo/integrazione come utente `tomcat`.

---

## 1.13 OTP / Authenticator / token
L’OTP è un codice temporaneo generato da un’app di autenticazione, come Google Authenticator.

### Funzione
È un secondo fattore di autenticazione, oltre a username e password.

### Nel vostro caso
Dopo aver avviato `startct`, il client può chiedere:
- username
- password
- codice OTP/Authenticator

---

## 1.14 Tomcat
Tomcat è un application server Java.

### Funzione
Ospita applicazioni web e API.

### Nel vostro caso
La procedura cita due applicazioni:
- `/mse-contributi-api`
- `/mse-inv-api`

---

# 2. Procedura di connessione spiegata passo per passo

## 2.1 Accesso iniziale al server Linux
Si accede al virtual host di sviluppo/integrazione usando PuTTY.

### Obiettivo
Entrare nel server dove verrà avviata la VPN.

### Utente
L’accesso avviene come utente `tomcat`.

### Cosa significa
Da questo momento i comandi vengono eseguiti sul server remoto, non sul PC locale.

---

## 2.2 Verifica di eventuali processi già presenti

```bash
ps -auxf | grep [M]IMIT-VPN
```

### Significato del comando
- `ps`: mostra i processi in esecuzione;
- `-a`, `-u`, `-x`, `-f`: opzioni che servono a mostrare più dettagli e processi di diversi utenti;
- `grep [M]IMIT-VPN`: filtra le righe contenenti `MIMIT-VPN`.

### Perché si usa `[M]`
Serve a evitare che `grep` trovi sé stesso nella lista dei processi.

### Scopo del comando
Capire se esiste già una precedente istanza o sessione legata alla VPN.

---

## 2.3 Terminazione del vecchio processo

```bash
kill -9 XXXXX
```

### Significato
- `XXXXX` va sostituito con il PID del processo trovato al passo precedente.
- `kill -9` termina in modo forzato il processo.

### Perché si usa
Per chiudere eventuali vecchie istanze rimaste bloccate.

### Attenzione
È un comando drastico: termina il processo immediatamente, senza un’uscita ordinata.

---

## 2.4 Creazione di una sessione tmux

```bash
tmux new -s MIMIT-VPN
```

### Significato
- `tmux new`: crea una nuova sessione tmux;
- `-s MIMIT-VPN`: assegna un nome alla sessione.

### Scopo
Mantenere attiva la sessione anche se ci si scollega dal terminale SSH.

### Perché è utile
Se la connessione PuTTY si chiude, la VPN può continuare a funzionare dentro tmux.

---

## 2.5 Avvio del client VPN

```bash
startct --mode console --name MIMIT --server consulenti.mise.gov.it --realm 'Consulenti'
```

### Significato dei parametri

#### `--mode console`
Usa la modalità testuale, senza interfaccia grafica.

#### `--name MIMIT`
Assegna un nome al profilo o alla connessione.

#### `--server consulenti.mise.gov.it`
Indica il gateway VPN a cui collegarsi.

#### `--realm 'Consulenti'`
Indica il gruppo o dominio di autenticazione usato dal sistema VPN.

### Cosa succede dopo
Il programma chiede le credenziali:
- username
- password
- codice Authenticator / token

Se i dati sono corretti, si stabilisce il collegamento VPN.

---

## 2.6 Sospensione del processo e messa in background

La procedura dice:

```text
Ctrl+Z
bg
```

### `Ctrl+Z`
Sospende il processo attivo in primo piano.

### `bg`
Riprende il processo sospeso facendolo continuare in background.

### Perché si usa
Per liberare il terminale e lasciare la VPN in esecuzione.

---

## 2.7 Distacco dalla sessione tmux

```text
Ctrl+b
d
```

### Significato
- `Ctrl+b` è il prefisso dei comandi di tmux;
- `d` significa detach, cioè staccarsi dalla sessione.

### Effetto
La sessione continua a esistere e il processo resta attivo.

### Perché è importante
Così la VPN non si interrompe quando chiudi la finestra terminale.

---

## 2.8 Reload delle applicazioni Tomcat

La procedura indica il reload di:

- `/mse-contributi-api`
- `/mse-inv-api`

### Significato
Il reload ricarica l’applicazione web senza riavviare necessariamente tutto Tomcat.

### Perché si fa
Per far ripartire le applicazioni dopo l’attivazione della VPN o per riallineare connessioni e configurazioni.

### Cosa non fa
Non apre la VPN e non configura il DNS.

---

## 2.9 Configurazione del DNS per il tunnel VPN

```bash
resolvectl status
sudo resolvectl dns tun0 10.184.3.133
```

### `resolvectl status`
Mostra lo stato attuale della configurazione DNS del sistema.

### `sudo resolvectl dns tun0 10.184.3.133`
Imposta il DNS `10.184.3.133` per l’interfaccia `tun0`.

### Scopo
Far sì che i nomi interni MIMIT vengano risolti correttamente quando il traffico passa nella VPN.

### Perché serve
Senza il DNS corretto, host come `mysqlclustertest.risorse.mise` potrebbero non essere traducibili in un IP raggiungibile.

---

## 2.10 Verifica della connettività

### Verifica del nome host

```bash
nslookup mysqlclustertest.risorse.mise
```

#### Significato
Chiede al DNS di risolvere il nome host.

#### Scopo
Verificare che il nome interno MIMIT sia risolvibile.

### Verifica della porta

```bash
nc -vz mysqlclustertest.risorse.mise 3306
```

#### Significato
- `nc` è netcat;
- `-v` abilita l’output dettagliato;
- `-z` fa un controllo della porta senza inviare dati applicativi.

#### Scopo
Verificare che la porta MySQL `3306` sia raggiungibile.

### Differenza tra i due controlli
- `nslookup` verifica il DNS;
- `nc -vz` verifica la raggiungibilità della porta.

---

# 3. Flusso logico completo della procedura

Il flusso completo è questo:

1. Si accede al server Linux tramite PuTTY.
2. Si controlla se ci sono vecchi processi VPN.
3. Se necessario, si termina il processo precedente.
4. Si apre una sessione `tmux`.
5. Si avvia il client VPN `startct`.
6. Si inseriscono le credenziali richieste.
7. Si ottiene il tunnel VPN.
8. Si configura il DNS sul tunnel `tun0`.
9. Si verificano la risoluzione DNS e la connettività verso gli host interni MIMIT.
10. Si ricaricano le applicazioni Tomcat.

---

# 4. Relazione tra i vari elementi

## Client VPN
È il programma usato sul server Linux per aprire la connessione.

## Gateway VPN
È il server MIMIT a cui il client si collega inizialmente.

## Tunnel VPN
È il collegamento sicuro che si crea dopo l’autenticazione.

## Interfaccia di rete
È il punto software o hardware attraverso cui il sistema invia e riceve traffico di rete.

## `tun0`
È l’interfaccia di rete virtuale usata dalla VPN sul server Linux.

## DNS
Serve per risolvere i nomi interni della rete MIMIT.

## Tomcat
Esegue le applicazioni che devono usare la rete interna.

---

# 5. Riassunto molto breve

- `PuTTY` serve per entrare nel server Linux.
- `startct` è il client VPN.
- `consulenti.mise.gov.it` è il gateway VPN.
- `tmux` mantiene viva la sessione.
- `tun0` è l’interfaccia di rete virtuale della VPN.
- `resolvectl status` mostra la configurazione DNS.
- `sudo resolvectl dns tun0 10.184.3.133` associa un DNS a `tun0`.
- `nslookup` e `nc` verificano che tutto funzioni.
- `Tomcat reload` riavvia le app che devono usare la rete interna.

---

# 6. Conclusione
La procedura serve a collegare il server Linux alla rete interna MIMIT attraverso una VPN, mantenere viva la connessione, configurare correttamente il DNS per i nomi interni e rendere disponibili le applicazioni Tomcat verso le risorse private.
