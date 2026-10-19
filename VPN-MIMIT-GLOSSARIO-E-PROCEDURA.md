# Glossario e procedura VPN MIMIT

## Obiettivo del documento
Questo documento spiega in modo chiaro:

1. i termini tecnici usati nella procedura VPN;
2. il significato dei comandi;
3. il flusso operativo completo per collegarsi alla rete MIMIT dal server Linux di sviluppo/integrazione;
4. le verifiche fatte per capire cosa succede la sera/notte alla VPN.

L’obiettivo è capire **cosa fa ogni comando**, **perché viene eseguito** e **come interpretare i risultati**.

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
- contatta il server VPN;
- invia le credenziali;
- crea il collegamento protetto;
- mantiene attivo il tunnel.

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

# 3. Verifiche fatte la sera/notte per capire cosa succede alla VPN

Questa sezione riassume i controlli eseguiti sul server per capire perché la VPN viene rifatta ogni mattina.

---

## 3.1 Controllo dei processi della VPN

```bash
ps -auxf | grep [s]tartct
ps -auxf | grep [M]IMIT-VPN
ps -p 2738134,2738145,2738191 -o pid,ppid,pgid,sid,tpgid,tty,stat,lstart,etime,comm
pstree -p 2738145
```

### Che cosa mostrano
- `ps -auxf`: elenca i processi con una vista ad albero;
- `grep [s]tartct`: cerca il comando `startct` senza includere la riga di `grep`;
- `grep [M]IMIT-VPN`: cerca la sessione o il terminale tmux associato;
- `ps -p ... -o ...`: mostra i dettagli di PID, padre, gruppo, sessione, stato e comando;
- `pstree -p ...`: mostra la relazione padre/figlio tra i processi.

### Cosa abbiamo osservato
Il processo `startct` risultava sospeso (`T`), mentre il processo Java figlio risultava anch’esso sospeso (`Tl`).

### Significato
La shell o lo script di avvio non erano il vero componente che gestiva la VPN in quel momento.

---

## 3.2 Identificazione del processo che usa davvero il tunnel

```bash
sudo lsof -nP /dev/net/tun
ps -p 2738228 -o pid,ppid,pgid,sid,stat,lstart,etime,cmd
```

### Che cosa fanno
- `lsof -nP /dev/net/tun`: mostra quale processo ha aperto il dispositivo TUN;
- `ps -p 2738228 ...`: mostra i dettagli del processo trovato.

### Cosa abbiamo scoperto
Il processo che usava realmente il tunnel era:

```text
/usr/local/Aventail/AvConnect --app-dir=/home/tomcat/.sonicwall -p44655
```

### Significato
`AvConnect` è il processo effettivo che mantiene la VPN attiva.

---

## 3.3 Verifica dell’interfaccia VPN e delle rotte

```bash
ip addr show tun0
ip link show tun0
ip route
ip route get 10.184.42.135
```

### Che cosa fanno
- `ip addr show tun0`: mostra l’indirizzo e lo stato dell’interfaccia `tun0`;
- `ip link show tun0`: mostra le proprietà del link;
- `ip route`: mostra le rotte presenti;
- `ip route get 10.184.42.135`: mostra quale interfaccia verrebbe usata per raggiungere quell’IP.

### Cosa abbiamo osservato
- `tun0` era presente e `UP`;
- l’indirizzo assegnato era `10.145.16.11`;
- il traffico verso `10.184.42.135` veniva instradato su `tun0`.

### Significato
La VPN risultava attiva e il traffico verso il database interno passava effettivamente nel tunnel.

---

## 3.4 Controllo della configurazione DNS

```bash
resolvectl status
```

### Cosa abbiamo osservato
La risoluzione DNS mostrava l’interfaccia `tun0` con il DNS `10.184.3.133`.

### Significato
Il sistema stava usando il DNS interno del MIMIT per la rete raggiunta tramite VPN.

---

## 3.5 Verifica di risoluzione del nome e porta del database

```bash
nslookup mysqlclustertest.risorse.mise
nc -vz mysqlclustertest.risorse.mise 3306
```

### Cosa abbiamo osservato
- il nome `mysqlclustertest.risorse.mise` veniva risolto in `10.184.42.135`;
- la connessione TCP alla porta `3306` riusciva.

### Significato
La risoluzione DNS e la raggiungibilità del database risultavano corrette in quel momento.

---

## 3.6 Verifica dei log della VPN

I log più utili erano:

- `/home/tomcat/.sonicwall/AventailConnect/library/logs/SnwlConnect.log`
- `/home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log`

### Comandi usati

```bash
tail -n 100 /home/tomcat/.sonicwall/AventailConnect/library/logs/SnwlConnect.log
tail -n 100 /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log
```

```bash
grep -nE 'AventailConnect\(|Starting TunThrd|Tunnel Connection Stale|Connection timed out|Resumption Timedout|Ending TunThrd|Closing Log' \
  /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log \
  /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log.old \
  | tail -n 180
```

### Significato dei comandi
- `tail -n 100`: mostra le ultime 100 righe del log;
- `grep -nE`: cerca più pattern e mostra i numeri di riga;
- `tail -n 180`: mantiene solo le ultime righe utili del risultato.

### Cosa abbiamo visto nei log
Nei log comparivano eventi ripetitivi di questo tipo:

- `Starting TunThrd`
- `Tunnel Connection Stale triggering Resume`
- `Connection timed out`
- `Resumption Timedout[35]s`
- `Ending TunThrd`
- `Closing Log`

### Esempio di log osservato

```text
2026-10-15 08:06:27.722 INFO main - ConnectApplication - ================ init() START ================================
2026-10-15 08:06:28.031 INFO main - ConnectApplication - App Mode: [CONSOLE]
2026-10-15 08:06:28.540 INFO Aventail Connect - AvCHandler - Starting Aventail Connect
2026-10-15 08:07:28.154 INFO pool-1-thread-3 - AvVpnServiceManager - REPC not enabled
2026-10-15 08:07:29.139 INFO pool-1-thread-3 - LogonAgent - GetAPIVersion() ...
2026-10-16 00:00:19.668 [0x7fe558c12640] I - evpnproto.cpp:SendKA - Tunnel Connection Stale triggering Resume
2026-10-16 00:00:56.776 [0x7fe55973e740] I - AventailConnect.cpp:HandleTunnel - Resumption Timedout[35]s
2026-10-16 00:00:57.813 [0x7fe558c12640] I - vpntunnel.cpp:EndTunSession - Ending TunThrd[0x7fe558c12640]
2026-10-16 00:00:58.912 [0x7fe55973e740] I - Closing Log
```

### Interpretazione
- la VPN viene avviata e stabilita;
- nelle ore notturne il tunnel viene considerato obsoleto o non più recuperabile;
- dopo circa 35 secondi di tentativo di recupero il client chiude la sessione.

### Osservazione importante
Le sessioni non cadevano sempre alla stessa distanza temporale dall’avvio: a volte dopo poche ore, a volte più tardi. La ricorrenza forte era invece l’orario notturno, vicino a mezzanotte o poco dopo.

---

## 3.7 Verifica di attività pianificate sul server

```bash
journalctl --since "2026-10-15 23:55" --until "2026-10-16 00:05" --no-pager
systemctl list-timers --all
crontab -l
cat /etc/crontab
ls -l /etc/cron.d /etc/cron.daily
```

### Che cosa abbiamo cercato
- eventi di sistema attorno a mezzanotte;
- timer systemd programmati;
- crontab dell’utente `tomcat`;
- crontab di sistema;
- script in `/etc/cron.d` e `/etc/cron.daily`.

### Cosa abbiamo trovato
- `crontab -l` per `tomcat` non mostrava lavori personali;
- `systemctl list-timers --all` mostrava timer di sistema, ma nessuno chiaramente associato alla VPN;
- nel `journalctl` comparivano attività come `logrotate` e backup `dpkg`, ma queste risultavano successive al primo segnale di problema della VPN.

### Esempio di output rilevante del journal

```text
Oct 16 00:00:30 mimit-tomcat-svil systemd[1]: Starting Daily dpkg database backup service...
Oct 16 00:00:30 mimit-tomcat-svil systemd[1]: Starting Rotate log files...
Oct 16 00:00:32 mimit-tomcat-svil systemd[1]: Finished Rotate log files.
Oct 16 00:00:57 mimit-tomcat-svil systemd-networkd[2109264]: tun0: Link DOWN
Oct 16 00:00:57 mimit-tomcat-svil systemd-networkd[2109264]: tun0: Lost carrier
```

### Interpretazione
Il problema del tunnel appare prima della chiusura di `tun0`. Questo suggerisce che la perdita dell’interfaccia è una conseguenza della chiusura della VPN, non la causa iniziale.

### Nota
Questi controlli servono a capire se esiste un’attività locale programmata che possa spiegare la caduta. Nei dati raccolti finora non è emersa una prova di un cron o timer locale che spieghi da solo il comportamento.

---

# 4. Cosa abbiamo imparato dalle verifiche

## 4.1 La VPN è reale e funzionante quando è attiva
Il tunnel è attivo, `tun0` è configurata, il DNS interno è presente e il database è raggiungibile.

## 4.2 Il processo che conta davvero è `AvConnect`
`startct` è il launcher iniziale, ma il processo che gestisce realmente il tunnel è `AvConnect`.

## 4.3 La caduta avviene tipicamente la sera/notte
Dai log, il tunnel tende a chiudersi in fascia notturna, spesso vicino a mezzanotte o poco dopo.

## 4.4 La chiusura del tunnel è seguita da `Resumption Timedout` e `Closing Log`
Questo indica che il client tenta un recupero per un tempo limitato, poi termina la sessione.

## 4.5 Non abbiamo trovato, nei controlli fatti, una causa locale evidente
Non è emersa una crontab dell’utente `tomcat` né un timer chiaramente associato alla VPN.

---

# 5. Riassunto molto breve

- `PuTTY` serve per entrare nel server Linux.
- `startct` è il client VPN.
- `consulenti.mise.gov.it` è il gateway VPN.
- `tmux` mantiene viva la sessione.
- `tun0` è l’interfaccia del tunnel.
- `resolvectl dns` configura il DNS interno.
- `nslookup` e `nc` verificano che tutto funzioni.
- `AvConnect` è il processo che gestisce realmente la VPN.
- I log mostrano che la VPN tende a cadere la sera/notte e poi viene rifatta il mattino successivo.

---

# 6. Conclusione
La procedura serve a collegare il server Linux alla rete interna MIMIT attraverso una VPN, mantenere viva la connessione, configurare il DNS corretto per i nomi interni e rendere disponibili le applicazioni Tomcat verso le risorse private.

Le verifiche fatte sul server hanno mostrato che la VPN funziona quando è attiva, ma tende a chiudersi nelle ore serali/notturne. I log indicano che, dopo il fallimento del tentativo di recupero, il client chiude la sessione e il mattino successivo è necessario riavviare la connessione.

---

# 7. Appendice: comandi usati per la diagnosi serale/notturna

Per comodità, qui sono raccolti i comandi usati per capire cosa succede alla VPN:

```bash
ps -auxf | grep [s]tartct
ps -auxf | grep [M]IMIT-VPN
ps -p 2738134,2738145,2738191 -o pid,ppid,pgid,sid,tpgid,tty,stat,lstart,etime,comm
pstree -p 2738145
sudo lsof -nP /dev/net/tun
ps -p 2738228 -o pid,ppid,pgid,sid,stat,lstart,etime,cmd
ip addr show tun0
ip link show tun0
ip route
ip route get 10.184.42.135
resolvectl status
nslookup mysqlclustertest.risorse.mise
nc -vz mysqlclustertest.risorse.mise 3306
journalctl --since "2026-10-15 23:55" --until "2026-10-16 00:05" --no-pager
systemctl list-timers --all
crontab -l
cat /etc/crontab
ls -l /etc/cron.d /etc/cron.daily
tail -n 100 /home/tomcat/.sonicwall/AventailConnect/library/logs/SnwlConnect.log
tail -n 100 /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log
grep -nE 'AventailConnect\(|Starting TunThrd|Tunnel Connection Stale|Connection timed out|Resumption Timedout|Ending TunThrd|Closing Log' \
  /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log \
  /home/tomcat/.sonicwall/AventailConnect/library/logs/AventailConnect.log.old \
  | tail -n 180
```
