# Linux e Windows: guida pratica per concorsi informatici

> **Obiettivo:** capire i concetti, riconoscere le parole chiave nei quiz e distinguere una risposta corretta da una solo plausibile.

---

## 1. CHE COS'È

**Linux** e **Windows** sono sistemi operativi (SO): il software di base che fa da intermediario tra hardware, programmi e utente.

- L'hardware è la parte fisica: CPU, RAM, disco, scheda di rete, periferiche.
- Il sistema operativo gestisce queste risorse e fornisce servizi ai programmi.
- Le applicazioni (browser, protocollo, gestionale, elaboratore testi) non dovrebbero controllare direttamente l'hardware: chiedono al sistema operativo di farlo.

### Sistema operativo e kernel: non sono sinonimi

Il **kernel** è il nucleo centrale del sistema operativo. Decide, tra l'altro:

- quale processo usa la CPU;
- come viene usata la memoria RAM;
- come leggere e scrivere sui dischi;
- come usare rete e periferiche;
- come applicare le autorizzazioni.

In senso pratico:

- **Linux** indica spesso un'intera famiglia di sistemi (per esempio Ubuntu, Debian, Red Hat, SUSE), costruiti attorno al kernel Linux e a molti strumenti di sistema;
- **Windows** è il sistema operativo di Microsoft, con un proprio kernel (famiglia Windows NT).

Nei quiz, dire che «Linux è solo un kernel» è **incompleto**: tecnicamente Linux è il kernel, ma nell'uso comune Linux indica anche una distribuzione completa. Dire che «un sistema operativo è soltanto il kernel» è **falso**: comprende anche strumenti, librerie, servizi e interfacce.

---

## 2. A COSA SERVE

Un sistema operativo risolve il problema della **condivisione ordinata e sicura delle risorse**.

Senza sistema operativo, ogni programma dovrebbe conoscere direttamente i dettagli di CPU, RAM, disco e dispositivi. Con il sistema operativo, un programma può chiedere: «apri questo file», «crea un processo», «invia dati in rete», «stampa questo documento».

Serve quindi a:

1. **Eseguire programmi** e assegnare loro tempo di CPU.
2. **Gestire la memoria**, evitando per quanto possibile che un programma invada lo spazio di un altro.
3. **Organizzare file e directory** sui dispositivi di archiviazione.
4. **Autenticare utenti** e applicare permessi.
5. **Erogare servizi di rete**, come condivisioni file, DNS, siti web, accesso remoto.
6. **Amministrare il sistema**, installando aggiornamenti, controllando log, backup e sicurezza.

Esempio PA: un server che ospita il protocollo informatico deve consentire al servizio applicativo di leggere i propri documenti, impedire a utenti non autorizzati di leggerli, rispondere sulla rete e registrare le operazioni nei log.

---

## 3. CONCETTI FONDAMENTALI

## 3.1 Sistema operativo

### Linux
Linux è usato molto su server, cloud, apparati di rete, supercomputer e sistemi embedded. Esistono molte **distribuzioni**: pacchetti coerenti di kernel, programmi, gestore dei pacchetti e configurazioni.

- Debian/Ubuntu usano tipicamente `apt` e pacchetti `.deb`.
- Red Hat/Fedora/Rocky Linux usano tipicamente `dnf` o `yum` e pacchetti `.rpm`.

### Windows
Windows è diffuso su PC aziendali e postazioni utente; Windows Server è orientato ai servizi infrastrutturali. L'amministrazione può usare interfacce grafiche, strumenti MMC, Prompt dei comandi e soprattutto **PowerShell**.

**Da quiz:** una distribuzione Linux non è un semplice programma applicativo: è un sistema completo che include il kernel Linux e strumenti aggiuntivi.

## 3.2 Kernel

Il kernel lavora in una modalità privilegiata, detta **kernel mode**, mentre le normali applicazioni lavorano normalmente in **user mode**.

- Un'applicazione che deve accedere al disco o alla rete effettua una richiesta al kernel, detta **system call**.
- La separazione aumenta stabilità e sicurezza: un programma ordinario non deve poter modificare direttamente memoria del kernel o dispositivi.

Linux è generalmente descritto come kernel **monolitico modulare**: molte funzioni sono nel kernel, ma possono essere caricate come moduli. Windows NT usa un'architettura spesso definita **ibrida**.

**Trappola:** non confondere il kernel con la shell. La **shell** è l'interprete dei comandi; Bash è una shell. Il kernel è il nucleo che gestisce le risorse.

## 3.3 Processi

Un **processo** è un programma in esecuzione, con il proprio contesto: memoria, identificativo, risorse aperte e stato.

Un file eseguibile sul disco non è ancora un processo. Quando viene avviato, il sistema crea un processo.

Elementi chiave:

- **PID**: Process ID, identificativo numerico del processo.
- **PPID**: Parent PID, identificativo del processo padre.
- **scheduler**: componente del kernel che stabilisce quale processo/thread riceve CPU.
- **foreground**: processo associato al terminale, che normalmente riceve input.
- **background**: processo in esecuzione senza occupare il terminale.
- **daemon** (Linux) / servizio: processo che resta in esecuzione e fornisce una funzione di sistema.

Stati semplificati: pronto/eseguibile, in esecuzione, in attesa (ad esempio di I/O), terminato.

### Comandi Linux sui processi

#### `ps`
- **Sintassi:** `ps [opzioni]`
- **Funzione:** mostra un'istantanea dei processi.
- **Esempio:** `ps aux` mostra processi di tutti gli utenti con informazioni estese.
- **Errore frequente:** confondere `ps` con una visualizzazione aggiornata continuamente: è una fotografia, non un monitor in tempo reale.
- **Differenza:** `top`/`htop` aggiornano continuamente; `ps` no.

#### `top`
- **Sintassi:** `top`
- **Funzione:** monitora in tempo reale CPU, memoria e processi.
- **Esempio:** `top` e poi `q` per uscire.
- **Errore frequente:** pensare che il valore di CPU sia sempre limitato a 100%: sui sistemi multicore alcune visualizzazioni possono aggregare l'uso in modo diverso.
- **Differenza:** `top` è interattivo; `ps` produce una lista statica.

#### `kill`
- **Sintassi:** `kill [-SEGNale] PID`
- **Funzione:** invia un segnale a un processo.
- **Esempio:** `kill 1234`; se necessario, `kill -9 1234` invia SIGKILL.
- **Errore frequente:** usare subito `kill -9`. SIGKILL non permette al processo di chiudere ordinatamente file o liberare risorse.
- **Differenza:** senza opzione si invia di norma `SIGTERM` (richiesta di terminazione); `-9`/SIGKILL forza la chiusura e non può essere intercettato.

#### `jobs`, `bg`, `fg`, `&`
- **Sintassi:** `comando &`, `jobs`, `bg %n`, `fg %n`
- **Funzione:** gestiscono i job della shell.
- **Esempio:** `backup.sh &` avvia lo script in background; `fg %1` riporta il job 1 in foreground.
- **Errore frequente:** confondere un **job della shell** con un processo in generale. Un job è un concetto della shell che può riferirsi a uno o più processi.
- **Differenza:** `kill` lavora normalmente con PID; `fg`/`bg` lavorano con job della shell.

### Windows: processi e gestione

Strumenti importanti:

- **Gestione attività (Task Manager):** processi, prestazioni, app di avvio, utenti, dettagli, servizi.
- **Process Explorer** (Sysinternals): analisi più approfondita dei processi e degli handle.
- `tasklist`: elenca processi nel Prompt.
- `taskkill /PID 1234 /F`: termina forzatamente un processo.
- PowerShell: `Get-Process`, `Stop-Process -Id 1234`.

**Da quiz:** chiudere un'applicazione non coincide sempre con spegnere un servizio. Un'app è un processo visibile all'utente; un servizio è progettato per operare in background e può avviarsi senza login.

## 3.4 Thread

Un **thread** è un flusso di esecuzione all'interno di un processo. Un processo può avere uno o più thread.

I thread dello stesso processo condividono tipicamente codice, dati e spazio di memoria; ciascun thread mantiene il proprio contesto di esecuzione, incluso lo stack.

Perché esistono:

- un programma può mantenere reattiva l'interfaccia mentre un thread esegue un calcolo;
- un server può gestire più richieste contemporaneamente;
- il sistema può sfruttare più core della CPU.

**Punto essenziale:** processi diversi sono più isolati; thread dello stesso processo comunicano più facilmente perché condividono memoria, ma richiedono sincronizzazione (mutex, semafori) per evitare race condition.

**Trappola:** «un thread ha sempre memoria completamente separata dagli altri thread» è falso. In genere condivide gran parte della memoria con i thread del suo processo.

## 3.5 Memoria

### RAM, memoria virtuale e swap/page file

La **RAM** è memoria volatile e veloce usata dai programmi in esecuzione. La **memoria virtuale** fa apparire a ciascun processo uno spazio di indirizzamento proprio e continuo, anche se la memoria fisica è condivisa.

Il kernel usa tabelle di pagine per tradurre indirizzi virtuali in indirizzi fisici. La memoria è gestita a blocchi detti **pagine**.

Quando la RAM non basta:

- Linux può usare **swap**, spazio su disco;
- Windows usa il **page file** (`pagefile.sys`).

Usare il disco come estensione della RAM è molto più lento. Un eccessivo scambio di pagine, chiamato **thrashing**, degrada drasticamente le prestazioni.

### Linux: comandi per memoria

#### `free`
- **Sintassi:** `free [opzioni]`
- **Funzione:** mostra RAM e swap utilizzate/disponibili.
- **Esempio:** `free -h` mostra valori leggibili (MiB/GiB).
- **Errore frequente:** considerare la memoria usata per cache come automaticamente sprecata. Linux usa RAM libera per cache, che può essere recuperata quando serve.
- **Differenza:** `free` sintetizza la memoria; `top` collega consumo di memoria ai processi.

#### `vmstat`
- **Sintassi:** `vmstat [intervallo [conteggio]]`
- **Funzione:** fornisce statistiche su processi, memoria, paging, I/O e CPU.
- **Esempio:** `vmstat 1 5` mostra cinque rilevazioni, una al secondo.
- **Errore frequente:** interpretare solo la prima riga come misura istantanea: può rappresentare statistiche dall'avvio; le righe successive mostrano l'intervallo.
- **Differenza:** è più diagnostico e sintetico di `free`.

### Windows: strumenti di memoria

- **Gestione attività > Prestazioni > Memoria:** RAM, memoria impegnata, cache, velocità e slot.
- **Monitoraggio risorse (Resource Monitor):** dettaglio per processo e hard fault.
- **Monitor prestazioni (Performance Monitor / perfmon):** contatori e trend.

**Da quiz:** la memoria virtuale non è sinonimo di memoria fisica; è un'astrazione di indirizzamento supportata dal sistema, che può usare RAM e, se necessario, disco.

## 3.6 File system

Il **file system** stabilisce come file e directory vengono nominati, organizzati, memorizzati e protetti su un supporto.

### Linux

La struttura è un unico albero che parte dalla radice `/`.

Directory da riconoscere:

- `/`: radice del file system; non è la home di root.
- `/home`: home directory degli utenti ordinari.
- `/root`: home dell'utente amministratore root.
- `/etc`: configurazioni di sistema.
- `/var`: dati variabili, ad esempio log e code.
- `/tmp`: file temporanei.
- `/usr`: programmi, librerie e dati condivisi per utenti.
- `/bin`, `/sbin`: comandi essenziali (in molti sistemi moderni possono essere collegamenti a `/usr/bin` e `/usr/sbin`).
- `/dev`: file speciali che rappresentano dispositivi.
- `/proc`: file system virtuale con informazioni su kernel e processi.
- `/mnt`, `/media`: punti comuni di montaggio.

Un dispositivo non è automaticamente visibile in una lettera diversa: viene **montato** in una directory dell'albero, detta mount point.

File system comuni: ext4, XFS, Btrfs; può leggere/scrivere anche altri secondo supporto e configurazione.

### Windows

Windows usa normalmente lettere di unità, ad esempio `C:\`, `D:\`. File system comuni:

- **NTFS:** supporta permessi ACL, journaling, compressione e altre funzionalità.
- **FAT32:** molto compatibile, ma ha limiti importanti (ad esempio file singolo fino a 4 GiB).
- **exFAT:** adatto spesso a memorie rimovibili e file grandi; meno funzionalità di sicurezza di NTFS.

Il **Registro di sistema (Registry)** è un database gerarchico di configurazioni di Windows e applicazioni; non è un file system e non sostituisce le directory.

### Comandi Linux per file system

#### `df`
- **Sintassi:** `df [opzioni] [percorso]`
- **Funzione:** mostra spazio libero/occupato dei file system montati.
- **Esempio:** `df -h /var`.
- **Errore frequente:** confondere `df` con `du`: `df` misura il file system, non una singola directory.
- **Differenza:** `du` misura lo spazio occupato da file e directory.

#### `du`
- **Sintassi:** `du [opzioni] [percorso]`
- **Funzione:** stima spazio occupato da directory e file.
- **Esempio:** `du -sh /var/log`.
- **Errore frequente:** usare `du` senza `-s` su grandi alberi e ottenere moltissimo output.
- **Differenza:** `df -h` risponde «quanto spazio resta sul disco?»; `du -sh cartella` risponde «quanto occupa questa cartella?».

#### `mount` e `umount`
- **Sintassi:** `mount dispositivo punto_di_mount`; `umount punto_di_mount`
- **Funzione:** collega/scollega un file system dall'albero delle directory.
- **Esempio:** `sudo mount /dev/sdb1 /mnt/usb`; `sudo umount /mnt/usb`.
- **Errore frequente:** scrivere `unmount`: il comando corretto è `umount` senza la lettera `n`.
- **Differenza:** montare non significa copiare dati; rende accessibile il file system in un punto dell'albero.

## 3.7 Utenti, gruppi e permessi

L'autenticazione risponde a «chi sei?»; l'autorizzazione risponde a «che cosa puoi fare?».

### Linux

Ogni utente ha un UID; ogni gruppo ha un GID. L'utente speciale **root** è l'amministratore con UID 0.

I permessi classici di un file sono riferiti a:

1. proprietario (user);
2. gruppo (group);
3. altri utenti (others).

Permessi base:

- `r` = read, lettura;
- `w` = write, scrittura;
- `x` = execute, esecuzione. Sulle directory `x` consente di attraversarle/accedere ai nomi interni.

Esempio di `ls -l`:

`-rwxr-x--- 1 mario ufficio 1200 report.txt`

- `-` iniziale: file normale (`d` indicherebbe una directory);
- `rwx`: proprietario `mario` può leggere, scrivere, eseguire;
- `r-x`: gruppo `ufficio` può leggere ed eseguire;
- `---`: gli altri non hanno permessi.

Numeri ottali: `r=4`, `w=2`, `x=1`; si sommano. Quindi `755` = proprietario `rwx` (7), gruppo `r-x` (5), altri `r-x` (5). `644` = proprietario lettura/scrittura, altri sola lettura.

### Comandi Linux per utenti e permessi

#### `whoami` e `id`
- **Sintassi:** `whoami`; `id [utente]`
- **Funzione:** mostrano identità effettiva e, con `id`, UID/GID e gruppi.
- **Esempio:** `id mario`.
- **Errore frequente:** confondere `whoami` con l'elenco degli utenti collegati.
- **Differenza:** per utenti collegati si usano, ad esempio, `who` o `w`.

#### `chmod`
- **Sintassi:** `chmod modalità file`; `chmod [ugoa][+-=][rwx] file`
- **Funzione:** modifica i permessi.
- **Esempio:** `chmod 640 report.txt`; `chmod u+x script.sh`.
- **Errore frequente:** credere che `chmod 777` sia una normale soluzione ai problemi di accesso. È spesso insicuro perché dà scrittura/esecuzione a tutti.
- **Differenza:** `chmod` cambia i permessi; `chown` cambia proprietario/gruppo.

#### `chown` e `chgrp`
- **Sintassi:** `chown utente[:gruppo] file`; `chgrp gruppo file`
- **Funzione:** cambiano proprietario e/o gruppo.
- **Esempio:** `sudo chown mario:ufficio report.txt`.
- **Errore frequente:** pensare che un utente ordinario possa assegnare un file a qualunque proprietario: normalmente serve privilegio amministrativo.
- **Differenza:** `chown` può impostare anche il gruppo; `chgrp` modifica solo il gruppo.

#### `sudo`
- **Sintassi:** `sudo comando`
- **Funzione:** esegue un comando con privilegi autorizzati, di norma amministrativi.
- **Esempio:** `sudo systemctl restart nginx`.
- **Errore frequente:** confondere `sudo` con il login permanente come root. `sudo` eleva il singolo comando; le autorizzazioni sono definite in configurazione.
- **Differenza:** `su` cambia utente/shell; `sudo` esegue un comando come un altro utente, tipicamente root.

### Windows: utenti e permessi

Windows può gestire account locali o account di dominio tramite **Active Directory Domain Services (AD DS)**. Gli utenti possono appartenere a gruppi, e i permessi vengono assegnati preferibilmente ai gruppi.

NTFS usa **ACL** (Access Control List): elenco di regole di autorizzazione. I principali permessi comprendono lettura, scrittura, modifica, controllo completo. Esistono permessi espliciti ed ereditati dalla cartella superiore.

Strumenti da conoscere:

- **Computer Management > Local Users and Groups** (nelle edizioni che lo supportano);
- `lusrmgr.msc` per utenti/gruppi locali;
- `secpol.msc` per criteri di sicurezza locali (edizioni supportate);
- scheda **Security** nelle proprietà di file/cartella;
- `icacls` per consultare/modificare ACL da riga di comando;
- PowerShell: `Get-LocalUser`, `Get-LocalGroup`, `Get-Acl`.

**Trappola:** condividere una cartella in rete richiede considerare sia i **permessi di condivisione** sia i **permessi NTFS**. L'accesso effettivo è limitato dalla combinazione più restrittiva.

## 3.8 Servizi

Un servizio è un programma in background che offre una funzione senza richiedere un utente davanti allo schermo: web server, database, stampa, DNS, aggiornamenti.

### Linux: systemd e servizi

In molte distribuzioni moderne il gestore dei servizi è **systemd**. Una sua unità di servizio viene spesso descritta in un file `.service`.

#### `systemctl`
- **Sintassi:** `systemctl azione nome-servizio`
- **Funzione:** gestisce servizi/unità systemd.
- **Esempio:** `sudo systemctl status ssh`; `sudo systemctl enable --now nginx`.
- **Errore frequente:** confondere `start` con `enable`. `start` avvia ora; `enable` configura l'avvio automatico secondo le dipendenze/target.
- **Differenza:** `restart` riavvia; `reload` ricarica configurazione solo se il servizio lo supporta.

Azioni ricorrenti: `start`, `stop`, `restart`, `reload`, `status`, `enable`, `disable`, `is-active`.

#### `journalctl`
- **Sintassi:** `journalctl [opzioni]`
- **Funzione:** consulta il journal di systemd.
- **Esempio:** `journalctl -u nginx -n 50`; `journalctl -p err -b`.
- **Errore frequente:** cercare sempre i log solo in `/var/log`: con systemd molti log sono disponibili nel journal.
- **Differenza:** `journalctl` legge il journal centralizzato; `tail -f /var/log/...` segue uno specifico file di log.

### Windows: Services e amministrazione

Lo snap-in **Services** (`services.msc`) permette di vedere e gestire servizi. Tipi di avvio tipici: automatico, automatico con avvio ritardato, manuale, disabilitato.

PowerShell:

- `Get-Service` elenca servizi;
- `Start-Service Nome` avvia;
- `Stop-Service Nome` arresta;
- `Set-Service` modifica proprietà compatibili.

**Trappola:** un servizio impostato su «Automatico» non è necessariamente già in esecuzione in quell'istante; descrive soprattutto la politica di avvio.

## 3.9 Rete

La rete consente lo scambio di dati tramite protocolli. Concetti indispensabili:

- **IP address:** identifica logicamente un'interfaccia in rete.
- **Subnet mask/prefisso CIDR:** distingue parte di rete e parte host.
- **Default gateway:** router usato per raggiungere reti esterne alla propria rete locale.
- **DNS:** traduce nomi (es. `intranet.ente.it`) in indirizzi IP.
- **DHCP:** assegna automaticamente configurazioni IP, spesso IP, maschera, gateway e DNS.
- **TCP:** protocollo orientato alla connessione e affidabile.
- **UDP:** protocollo senza connessione, più leggero, senza garanzia intrinseca di consegna/ordine.
- **Porta:** numero che identifica un servizio/applicazione su un host; non identifica un PC nella rete.

### Linux: comandi di rete

#### `ip`
- **Sintassi:** `ip indirizzo|link|route [opzioni]`
- **Funzione:** consulta/configura interfacce, indirizzi e rotte.
- **Esempio:** `ip addr`; `ip route`.
- **Errore frequente:** usare `ifconfig` come unico comando universale. È storico e può non essere installato; `ip` è l'alternativa moderna comune.
- **Differenza:** `ip addr` mostra indirizzi; `ip route` mostra tabella di instradamento e gateway predefinito.

#### `ping`
- **Sintassi:** `ping [opzioni] host`
- **Funzione:** verifica raggiungibilità IP tramite ICMP e misura tempi di risposta.
- **Esempio:** `ping -c 4 8.8.8.8`.
- **Errore frequente:** concludere che un host sia spento se non risponde. ICMP può essere filtrato da firewall o configurazioni di rete.
- **Differenza:** `ping` non verifica direttamente che un servizio TCP (ad esempio HTTPS) sia disponibile.

#### `ss`
- **Sintassi:** `ss [opzioni]`
- **Funzione:** visualizza socket e porte in ascolto/connessioni.
- **Esempio:** `ss -tuln` mostra socket TCP/UDP in ascolto con porte numeriche.
- **Errore frequente:** confondere una porta in ascolto con una connessione già stabilita.
- **Differenza:** `ss` è il sostituto moderno frequente di `netstat`.

#### `curl`
- **Sintassi:** `curl [opzioni] URL`
- **Funzione:** effettua richieste a URL/protocolli supportati, utile per test applicativi HTTP(S).
- **Esempio:** `curl -I https://www.example.org` richiede solo le intestazioni HTTP.
- **Errore frequente:** usare `ping` per verificare il funzionamento di un sito web; il sito può essere raggiungibile via IP ma il servizio HTTP può fallire.
- **Differenza:** `ping` testa ICMP; `curl` testa l'accesso applicativo a un URL.

### Windows: rete e strumenti amministrativi

- `ipconfig /all`: configurazione IP dettagliata;
- `ipconfig /release` e `/renew`: rilascio/rinnovo DHCP;
- `ipconfig /flushdns`: svuota cache DNS locale;
- `ping`: test ICMP;
- `tracert`: traccia gli hop di rete;
- `nslookup`: interrogazioni DNS;
- `netstat -ano`: connessioni/porte/PID;
- PowerShell: `Get-NetIPConfiguration`, `Test-NetConnection`, `Get-NetTCPConnection`.

**Trappola:** DNS non assegna normalmente l'indirizzo IP al client: questo è compito tipico di DHCP. DNS risolve nomi in IP.

## 3.10 Gestione dei file

### Linux: percorsi e comandi Bash fondamentali

Un percorso può essere:

- **assoluto**, parte da `/`, es. `/home/mario/documenti/file.txt`;
- **relativo**, parte dalla directory corrente, es. `documenti/file.txt`.

`~` rappresenta la home dell'utente corrente; `.` la directory corrente; `..` la directory padre.

#### `pwd`
- **Sintassi:** `pwd`
- **Funzione:** mostra la directory corrente.
- **Esempio:** `pwd` può restituire `/home/mario`.
- **Errore frequente:** pensare che cambi directory: non la cambia.
- **Differenza:** `cd` cambia directory; `pwd` la visualizza.

#### `ls`
- **Sintassi:** `ls [opzioni] [percorso]`
- **Funzione:** elenca contenuti di una directory.
- **Esempio:** `ls -la /etc` mostra anche file nascosti e dettagli.
- **Errore frequente:** credere che i file nascosti abbiano un attributo speciale come in Windows: in Unix/Linux, per convenzione il nome inizia con `.`.
- **Differenza:** `ls -l` mostra dettagli; `ls -a` include nomi che iniziano con punto.

#### `cd`
- **Sintassi:** `cd [directory]`
- **Funzione:** cambia directory corrente della shell.
- **Esempio:** `cd /var/log`; `cd ..`; `cd ~`.
- **Errore frequente:** usare `cd /` credendo di entrare nella home di root: entra nella radice del file system.
- **Differenza:** `/root` è la home dell'utente root; `/` è la radice dell'intero albero.

#### `mkdir` e `rmdir`
- **Sintassi:** `mkdir [opzioni] directory`; `rmdir directory`
- **Funzione:** creano/rimuovono directory vuote.
- **Esempio:** `mkdir -p progetti/2026/report`; `rmdir cartella_vuota`.
- **Errore frequente:** usare `rmdir` su una directory non vuota.
- **Differenza:** `rm -r directory` rimuove ricorsivamente anche contenuti, quindi è molto più pericoloso.

#### `cp` e `mv`
- **Sintassi:** `cp sorgente destinazione`; `mv sorgente destinazione`
- **Funzione:** `cp` copia; `mv` sposta o rinomina.
- **Esempio:** `cp report.pdf archivio/`; `mv bozza.txt versione_finale.txt`.
- **Errore frequente:** invertire origine e destinazione oppure sovrascrivere un file omonimo senza accorgersene.
- **Differenza:** `cp` lascia l'originale; `mv` normalmente non lo lascia nella posizione iniziale.

#### `rm`
- **Sintassi:** `rm [opzioni] file`
- **Funzione:** elimina file; con `-r` elimina ricorsivamente directory.
- **Esempio:** `rm bozza.txt`; `rm -r cartella`.
- **Errore frequente:** `rm -rf` eseguito sul percorso errato. In shell non equivale al Cestino e normalmente non offre recupero immediato.
- **Differenza:** `rmdir` elimina solo directory vuote; `rm -r` può eliminare una struttura intera.

#### `find`
- **Sintassi:** `find percorso criteri`
- **Funzione:** cerca file nell'albero delle directory.
- **Esempio:** `find /var/log -type f -name '*.log'`.
- **Errore frequente:** non quotare il wildcard: `-name '*.log'` evita che la shell lo espanda prima di `find`.
- **Differenza:** `find` cerca in tempo reale nel file system; `locate` usa in genere un indice, può essere più veloce ma non aggiornato.

#### `grep`
- **Sintassi:** `grep [opzioni] modello file`
- **Funzione:** cerca righe che corrispondono a un testo o espressione regolare.
- **Esempio:** `grep -i 'errore' /var/log/app.log`; `grep -r 'password' /etc`.
- **Errore frequente:** dimenticare che il modello può essere un'espressione regolare; caratteri come `.` hanno significato speciale salvo opzioni/escape adeguati.
- **Differenza:** `find` individua file; `grep` cerca contenuto dentro file.

#### `cat`, `less`, `head`, `tail`
- **Sintassi:** `cat file`; `less file`; `head -n N file`; `tail -n N file`
- **Funzione:** leggono contenuti o porzioni di file.
- **Esempio:** `tail -f /var/log/syslog` segue nuove righe del log; `head -n 20 file.txt` mostra le prime 20.
- **Errore frequente:** usare `cat` per file enormi o binari.
- **Differenza:** `less` è paginato/interattivo; `tail -f` è utile per monitorare un log in crescita.

#### Redirezioni e pipe
- **Sintassi:** `comando > file`; `comando >> file`; `comando1 | comando2`
- **Funzione:** `>` reindirizza e sovrascrive output; `>>` aggiunge; `|` passa l'output del primo comando come input del secondo.
- **Esempio:** `grep -i errore app.log | sort | uniq -c > conteggio.txt`.
- **Errore frequente:** usare `>` quando si voleva aggiungere, sovrascrivendo il file.
- **Differenza:** una pipe collega comandi; una redirezione invia input/output a file o flussi.

### Windows: gestione file

Concetti e strumenti da quiz:

- **Esplora file:** copia, spostamento, proprietà, condivisione, visualizzazione estensioni e file nascosti.
- **Cestino:** elimina normalmente in modo recuperabile fino allo svuotamento (con eccezioni, per esempio uso di combinazioni o supporti).
- `dir`: elenca directory nel Prompt; analogo concettuale di `ls`.
- `cd`: cambia directory anche nel Prompt.
- `copy`, `move`, `del`, `mkdir`/`md`, `rmdir`/`rd`.
- `robocopy`: copia robusta, utile in amministrazione e migrazioni.
- PowerShell: `Get-ChildItem`, `Copy-Item`, `Move-Item`, `Remove-Item`, `Get-Content`.

**Trappola:** estensione e tipo di file sono collegati, ma in Windows l'estensione è spesso nascosta nell'interfaccia. Rinominare soltanto l'estensione non converte davvero il formato di un file.

## 3.11 Amministrazione del sistema

L'amministrazione consiste nel mantenere un sistema sicuro, aggiornato, funzionante e osservabile.

Attività tipiche:

- gestione account e gruppi;
- aggiornamenti di sistema e applicazioni;
- installazione/rimozione software;
- configurazione rete;
- gestione dischi, partizioni e file system;
- controllo servizi e processi;
- log e monitoraggio;
- backup e ripristino;
- applicazione di policy di sicurezza.

### Linux: pacchetti e amministrazione

#### `apt`
- **Sintassi:** `sudo apt update`; `sudo apt install pacchetto`; `sudo apt upgrade`
- **Funzione:** gestisce pacchetti nelle distribuzioni Debian/Ubuntu.
- **Esempio:** `sudo apt update && sudo apt install nginx`.
- **Errore frequente:** confondere `apt update` con aggiornamento dei pacchetti installati: aggiorna l'indice dei pacchetti; `apt upgrade` installa aggiornamenti disponibili.
- **Differenza:** su famiglie Red Hat sono comuni `dnf install pacchetto` o, su sistemi meno recenti, `yum`.

#### `uname` e `hostnamectl`
- **Sintassi:** `uname -a`; `hostnamectl`
- **Funzione:** mostrano informazioni sul kernel/sistema e sul nome host.
- **Esempio:** `uname -r` mostra la release del kernel.
- **Errore frequente:** confondere versione del kernel con versione della distribuzione.
- **Differenza:** `uname` riguarda soprattutto kernel; informazioni sulla distribuzione possono essere in `/etc/os-release`.

#### `crontab`
- **Sintassi:** `crontab -e`; `crontab -l`
- **Funzione:** gestisce attività pianificate dell'utente tramite cron.
- **Esempio:** `0 2 * * * /home/mario/backup.sh` esegue lo script ogni giorno alle 02:00.
- **Errore frequente:** sbagliare l'ordine dei cinque campi: minuto, ora, giorno del mese, mese, giorno della settimana.
- **Differenza:** cron è tradizionale per pianificazione; systemd offre anche timer con integrazione più moderna.

### Windows: funzionalità amministrative da conoscere

1. **Impostazioni e Pannello di controllo**: configurazioni generali; non sono perfettamente identici, ma molte funzioni storiche restano nel Pannello.
2. **Computer Management (`compmgmt.msc`)**: console che raggruppa Event Viewer, utenti/gruppi locali, Gestione dispositivi, Gestione dischi e servizi.
3. **Device Manager (`devmgmt.msc`)**: driver e stato delle periferiche; un driver permette al SO di interagire con l'hardware.
4. **Disk Management (`diskmgmt.msc`)**: inizializzazione dischi, partizioni/volumi, lettere di unità, formattazione. Formattare crea/prepara un file system e può cancellare i dati.
5. **Event Viewer (`eventvwr.msc`)**: log applicazione, sicurezza, sistema e altri registri; essenziale per diagnosi.
6. **Task Scheduler (`taskschd.msc`)**: esecuzione pianificata di attività, in risposta a orari o eventi.
7. **Windows Update**: aggiornamenti di sicurezza, qualità, driver e funzionalità secondo policy.
8. **Microsoft Defender Firewall with Advanced Security (`wf.msc`)**: regole in ingresso e uscita.
9. **BitLocker**: cifratura di unità; protegge dati a riposo, non sostituisce i permessi utente.
10. **Backup / File History / System Restore**: funzioni con scopi diversi. Un punto di ripristino non è un backup completo dei documenti.
11. **Criteri di gruppo (Group Policy, `gpedit.msc`)**: configurazioni centralizzate/locali; in dominio, le GPO sono fondamentali per applicare policy a utenti/computer.
12. **Active Directory**: directory service di Windows Server per identità, gruppi, computer e policy in un dominio. Non è presente come ruolo completo in un normale client Windows.
13. **PowerShell**: shell e linguaggio di automazione basato su oggetti; cmdlet come `Get-Service` restituiscono oggetti, non solo testo.

---

## 4. COME FUNZIONA: DAL LOGIN ALL'ACCESSO A UN FILE

Vediamo un flusso pratico, valido come modello mentale per Linux e Windows.

1. **Avvio:** firmware/UEFI avvia il boot loader; il boot loader carica il kernel.
2. **Inizializzazione:** il kernel rileva/inizializza componenti essenziali e avvia il gestore dei servizi. In Linux moderno spesso systemd; in Windows Service Control Manager gestisce i servizi.
3. **Autenticazione:** l'utente inserisce credenziali. Il sistema verifica l'identità locale o di dominio.
4. **Creazione della sessione:** il sistema carica profilo, ambiente utente e processi necessari alla sessione grafica o testuale.
5. **Avvio dell'applicazione:** il SO crea un processo, assegna PID, spazio di memoria virtuale e risorse iniziali.
6. **Richiesta di apertura file:** l'applicazione chiede al kernel di aprire un percorso.
7. **Verifica autorizzazioni:** il kernel confronta l'identità/gruppi dell'utente con permessi Unix o ACL NTFS/condivisione.
8. **Accesso al file system:** se autorizzato, il file system individua i dati sul dispositivo; il kernel coordina eventuale cache e I/O.
9. **Operazioni di rete, se necessarie:** per una cartella condivisa o un servizio remoto, entrano in gioco DNS, IP, protocollo di rete, autenticazione e permessi.
10. **Log:** il sistema e/o l'applicazione possono registrare l'evento: journal/syslog in Linux, Event Viewer o log applicativi in Windows.

---

## 5. ESEMPIO CONCRETO: GESTIONALE DOCUMENTALE DI UN COMUNE

Immaginiamo un Comune con un gestionale per protocollo e documenti.

### Scenario Linux

Il server Linux esegue:

- un servizio web, per esempio Nginx;
- un'applicazione documentale;
- un database;
- un servizio di backup notturno.

L'amministratore crea un gruppo `protocollo`, assegna al gruppo la cartella dei documenti e limita accesso a chi deve lavorarci:

```bash
sudo groupadd protocollo
sudo usermod -aG protocollo anna
sudo chown -R root:protocollo /srv/documenti-protocollo
sudo chmod -R 2770 /srv/documenti-protocollo
```

Significato operativo:

- `groupadd` crea il gruppo;
- `usermod -aG` aggiunge Anna al gruppo supplementare senza rimuovere gli altri gruppi;
- `chown -R` imposta proprietario/gruppo anche nei contenuti;
- `2770` assegna `rwx` a proprietario e gruppo, niente agli altri; il primo `2` è il bit **setgid** sulla directory, utile affinché nuovi file ereditino il gruppo della directory.

Per verificare un problema di servizio:

```bash
sudo systemctl status nginx
sudo journalctl -u nginx -n 50
sudo ss -tuln | grep ':80\|:443'
```

Interpretazione: prima controllo se il servizio è attivo, poi leggo gli ultimi log, infine verifico se una porta web è in ascolto. Non basta eseguire solo `ping`: il server può rispondere a ICMP ma il web server può essere fermo.

### Scenario Windows

In un file server Windows, la cartella `D:\Protocollo` è condivisa agli utenti dell'ufficio. L'amministratore:

1. crea/usa un gruppo di dominio, ad esempio `COMUNE\Protocollo`;
2. assegna al gruppo i permessi NTFS di modifica sulla cartella;
3. configura la condivisione SMB con permessi coerenti;
4. verifica Event Viewer in caso di accesso negato;
5. usa le policy di gruppo per mappare la cartella come unità di rete agli utenti autorizzati;
6. pianifica backup e controlla esito dei job.

Risposta da quiz corretta: i permessi vanno attribuiti preferibilmente a gruppi, non assegnati singolarmente a ogni utente, perché semplifica gestione, audit e revoca.

---

## 6. DIFFERENZE IMPORTANTI

| Aspetto | Linux | Windows | Punto da ricordare per il quiz |
|---|---|---|---|
| Sistema operativo | Famiglia di distribuzioni attorno al kernel Linux | Sistema Microsoft basato sulla famiglia NT | Linux può indicare kernel o sistema completo secondo contesto |
| Kernel | Linux, monolitico modulare | Windows NT, architettura ibrida | Kernel ≠ shell/interfaccia grafica |
| Shell comune | Bash (ma esistono zsh, fish ecc.) | PowerShell e Prompt dei comandi | Bash e PowerShell non sono il sistema operativo |
| Radice file system | `/` | Unità con lettere, es. `C:\` | `/` non è `/root` |
| Home utente | `/home/nome`; root: `/root` | Tipicamente `C:\Users\nome` | Utente root Linux e cartella radice non sono la stessa cosa |
| File system comuni | ext4, XFS, Btrfs | NTFS, FAT32, exFAT | FAT32 ha limite di 4 GiB per file singolo |
| Permessi | user/group/others; rwx; ACL possibili | ACL NTFS, eredità e gruppi | In rete Windows: contano sia share sia NTFS |
| Amministratore | root; delega con `sudo` | Administrator / membri Administrators; UAC | `sudo` non significa accesso root permanente |
| Servizi | systemd, `systemctl` | Service Control Manager, `services.msc`, PowerShell | Avvio automatico ≠ servizio sicuramente attivo ora |
| Processi | `ps`, `top`, segnali con `kill` | Task Manager, `tasklist`, `taskkill`, PowerShell | Processo ≠ thread; servizio è normalmente un processo/background |
| Log | journal (`journalctl`), `/var/log` | Event Viewer | I log sono fondamentali per diagnosi e audit |
| Pacchetti/software | repository e gestori come apt/dnf | installer, Microsoft Store, winget, strumenti di gestione | `apt update` aggiorna indice; non equivale a `apt upgrade` |
| Configurazione | molti file testuali in `/etc` | Registro, file/configurazioni, Group Policy | Il Registry è un database di configurazione, non un file system |
| Pianificazione | cron, systemd timers | Task Scheduler | Entrambi automatizzano attività a orario/evento |
| Rete | `ip`, `ss`, `ping`, `curl` | `ipconfig`, `netstat`, `ping`, `Test-NetConnection` | DNS risolve nomi; DHCP assegna configurazione IP |

### Distinzioni che confondono spesso

| Concetti | Differenza essenziale |
|---|---|
| Programma vs processo | Il programma è il file/codice; il processo è l'istanza in esecuzione. |
| Processo vs thread | Un processo ha proprio spazio di indirizzamento; i thread dello stesso processo condividono molte risorse. |
| Autenticazione vs autorizzazione | Autenticazione = identità; autorizzazione = permessi dopo l'identificazione. |
| File system vs disco/partizione | Disco è supporto fisico; partizione è divisione logica; file system organizza dati nella partizione/volume. |
| Formattazione vs partizionamento | Partizionare divide il disco; formattare crea il file system nel volume/partizione. |
| `df` vs `du` | `df` spazio del file system; `du` occupazione di file/directory. |
| `cp` vs `mv` | `cp` copia; `mv` sposta o rinomina. |
| `rm` vs Cestino | `rm` normalmente non usa un Cestino. |
| `start` vs `enable` in systemd | `start` ora; `enable` all'avvio. |
| DNS vs DHCP | DNS nome→IP; DHCP assegna parametri di rete. |
| TCP vs UDP | TCP affidabile e orientato alla connessione; UDP leggero, senza garanzie intrinseche. |
| Firewall vs antivirus | Firewall filtra traffico; antivirus rileva/contrasta software malevolo. |
| Backup vs punto di ripristino | Il backup conserva copie recuperabili dei dati; il ripristino sistema non è necessariamente backup documentale. |

---

## 7. ERRORI E TRAPPOLE DA CONCORSO

1. **«Linux è una distribuzione.»** Non sempre: Linux propriamente è il kernel; Ubuntu è una distribuzione Linux.
2. **«Bash è il kernel Linux.»** Falso: Bash è una shell/interprete dei comandi.
3. **«Un programma è un processo.»** Non esattamente: il processo è il programma caricato e in esecuzione.
4. **«Ogni processo ha un solo thread.»** Falso: può averne molti.
5. **«Thread diversi non condividono memoria.»** Falso se appartengono allo stesso processo: condividono in genere spazio di indirizzamento e risorse comuni.
6. **«La memoria virtuale è solo spazio su disco.»** Falso: è soprattutto un'astrazione di indirizzamento; il disco può intervenire tramite swap/page file.
7. **«Se Linux mostra poca RAM libera, il sistema ha necessariamente un problema.»** Falso: la cache usa RAM liberabile e può migliorare prestazioni.
8. **«`df` indica quali file occupano più spazio.»** Falso: per cartelle/file usare `du` o strumenti più specifici.
9. **«`rm` sposta nel Cestino.»** Normalmente falso.
10. **«`chmod 777` è una buona soluzione standard.»** Falso: è spesso una grave apertura di sicurezza.
11. **«Il permesso `x` su una directory significa soltanto eseguire la directory.»** Impreciso: consente attraversamento/accesso ai contenuti in base agli altri permessi.
12. **«Root è la directory `/`.** Falso: `/` è la radice del file system; root è l'utente amministratore e `/root` è tipicamente la sua home.
13. **«`sudo` e `su` sono identici.»** Falso: `sudo` esegue un comando con privilegi delegati; `su` cambia identità/shell.
14. **«`kill -9` è il modo normale per chiudere un processo.»** Falso: è una forzatura, da usare quando terminazione ordinata non funziona.
15. **«Avviare un servizio e abilitarlo all'avvio sono la stessa operazione.»** Falso: `start` e `enable` hanno ruoli diversi.
16. **«Se ping risponde, il sito web funziona.»** Falso: ICMP può rispondere mentre HTTP/HTTPS o l'applicazione sono guasti.
17. **«Se ping non risponde, il server è certamente spento.»** Falso: ICMP può essere bloccato.
18. **«DNS assegna gli IP.»** In genere falso: DHCP assegna parametri IP; DNS risolve nomi.
19. **«Una porta identifica un computer.»** Falso: identifica un endpoint/servizio su un host; l'host è individuato dall'indirizzo IP.
20. **«FAT32 supporta file di qualunque dimensione.»** Falso: il file singolo è limitato a 4 GiB.
21. **«NTFS e permessi di condivisione sono identici.»** Falso: sono due livelli distinti da combinare.
22. **«Un account amministratore deve essere usato per tutte le attività quotidiane.»** Cattiva pratica: si applica il principio del minimo privilegio.
23. **«Il Registro di Windows contiene tutti i file dell'utente.»** Falso: conserva configurazioni, non sostituisce il file system.
24. **«Task Manager gestisce solo le applicazioni grafiche.»** Falso: mostra anche processi, prestazioni, servizi e avvio.
25. **«Un punto di ripristino è un backup completo.»** Falso.

### Metodo per risolvere un quiz

Quando leggi una risposta, chiediti:

1. Sta confondendo **identità** e **permessi**?
2. Sta confondendo **programma**, **processo** e **thread**?
3. Sta confondendo **file system**, **disco**, **partizione** e **directory**?
4. Sta attribuendo a DNS ciò che fa DHCP, o a ping ciò che fa HTTP?
5. Sta trasformando una regola «spesso vera» in una regola assoluta («sempre», «solo», «certamente»)? Nei quiz queste parole sono spesso segnali di risposta falsa.
6. Sta confondendo l'azione immediata (`start`) con la configurazione persistente (`enable`, avvio automatico)?

---

## 8. COSA DEVO MEMORIZZARE

### Definizioni essenziali

- **Sistema operativo:** software che gestisce hardware e offre servizi a programmi e utenti.
- **Kernel:** nucleo privilegiato del sistema operativo; gestisce CPU, memoria, dispositivi, file system, rete e sicurezza.
- **Shell:** interprete dei comandi; Bash è una shell Linux, PowerShell è una shell/ambiente di automazione Windows.
- **Processo:** programma in esecuzione con PID e risorse proprie.
- **Thread:** flusso di esecuzione di un processo; thread dello stesso processo condividono molte risorse.
- **Memoria virtuale:** astrazione che assegna spazi di indirizzamento virtuali ai processi; può usare RAM e swap/page file.
- **File system:** struttura/logica per memorizzare e organizzare file e directory.
- **Permesso:** autorizzazione a leggere, scrivere, eseguire o amministrare una risorsa.
- **Servizio:** programma in background che fornisce una funzione di sistema/rete.
- **DNS:** traduce nomi in indirizzi IP.
- **DHCP:** assegna automaticamente configurazione di rete.
- **Gateway predefinito:** router per reti esterne alla subnet locale.

### Linux: associazioni rapide

- Radice: `/`; home utenti: `/home`; home root: `/root`; configurazioni: `/etc`; log/dati variabili: `/var`; temporanei: `/tmp`.
- Permessi: `r=4`, `w=2`, `x=1`; `755 = rwxr-xr-x`; `644 = rw-r--r--`.
- `ps` = istantanea processi; `top` = monitor in tempo reale; `kill` = segnale al PID.
- `free -h` = RAM/swap; `df -h` = spazio dei file system; `du -sh` = dimensione di una directory.
- `chmod` = permessi; `chown` = proprietario/gruppo; `sudo` = esecuzione privilegiata delegata.
- `systemctl start` = avvio ora; `systemctl enable` = avvio automatico configurato.
- `journalctl` = log systemd.
- `ip addr` = indirizzi; `ip route` = rotte/gateway; `ss -tuln` = porte/socket in ascolto.
- `find` cerca file; `grep` cerca testo nei file.
- `>` sovrascrive output; `>>` aggiunge; `|` collega output e input di due comandi.
- `apt update` aggiorna l'indice; `apt upgrade` aggiorna pacchetti installati.

### Windows: associazioni rapide

- **Task Manager:** processi, prestazioni, avvio, servizi.
- **Event Viewer:** log e diagnosi.
- **Device Manager:** hardware e driver.
- **Disk Management:** dischi, partizioni/volumi, formattazione, lettere unità.
- **Services (`services.msc`):** servizi e tipo di avvio.
- **Task Scheduler:** attività pianificate.
- **NTFS:** ACL e sicurezza; **FAT32:** massima compatibilità ma file singolo massimo 4 GiB; **exFAT:** utile per supporti rimovibili e file grandi.
- **Active Directory:** gestione centralizzata di identità, computer, gruppi e policy in un dominio Windows Server.
- **Group Policy:** applica impostazioni/policy a utenti e computer, soprattutto in dominio.
- `ipconfig /all` = rete dettagliata; `ipconfig /flushdns` = svuota cache DNS; `nslookup` = verifica DNS; `netstat -ano` = connessioni/porte/PID.
- Permessi di **condivisione** e **NTFS** sono distinti: prevale la restrizione effettiva più forte.

### Regole finali da ricordare

1. Kernel, shell e interfaccia grafica sono componenti diversi.
2. Programma, processo e thread non sono sinonimi.
3. DNS risolve nomi; DHCP assegna parametri IP.
4. Un file system organizza dati; non coincide con il disco fisico.
5. In Linux `/` e `/root` sono cose diverse.
6. In Linux `start` ed `enable` sono cose diverse.
7. In Windows un servizio, un processo e un'app visibile non coincidono necessariamente.
8. La sicurezza efficace usa minimo privilegio, gruppi, aggiornamenti, backup, log e controllo degli accessi.
9. Un singolo test di rete raramente dimostra tutto: ping, DNS, porte e servizio applicativo verificano livelli diversi.
10. Nei quiz, diffida delle frasi assolute e delle risposte che confondono due livelli diversi dello stesso sistema.
