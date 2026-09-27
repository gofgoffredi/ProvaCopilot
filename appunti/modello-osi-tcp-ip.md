# Modello OSI e modello TCP/IP: fondamenti delle reti

> Appunti didattici orientati ai quiz per concorsi informatici nella Pubblica Amministrazione.

## 1. Perché servono i modelli a livelli

### Definizione

Un **modello di rete a livelli** è una rappresentazione concettuale che divide la comunicazione fra sistemi in più strati, ciascuno con responsabilità precise.

Invece di considerare Internet come un unico meccanismo complesso, i modelli distinguono funzioni come:

- trasmettere segnali sul mezzo fisico;
- identificare i dispositivi nella rete locale;
- instradare i pacchetti fra reti differenti;
- garantire, quando necessario, che i dati arrivino correttamente;
- offrire servizi alle applicazioni, come Web, posta elettronica e DNS.

### Perché vengono utilizzati

I modelli a livelli consentono a tecnologie diverse di cooperare. Un browser Web può funzionare indipendentemente dal fatto che il PC sia collegato tramite:

- Ethernet;
- Wi-Fi;
- fibra;
- rete mobile.

Il browser usa soprattutto i livelli alti; il collegamento concreto è gestito dai livelli inferiori.

---

## 2. Incapsulamento e decapsulamento

### Definizione

L'**incapsulamento** è il processo con cui, durante l'invio, ogni livello aggiunge ai dati ricevuti dal livello superiore informazioni di controllo proprie.

Al destinatario avviene il **decapsulamento**: ciascun livello legge e rimuove le proprie informazioni prima di consegnare i dati al livello superiore.

### Schema generale

```text
Applicazione genera i dati
          |
          v
[ Livello applicativo ]       Dati
          |
          v
[ Livello trasporto ]         Segmento / Datagramma
          |
          v
[ Livello rete ]              Pacchetto IP
          |
          v
[ Collegamento dati ]         Frame (o trama)
          |
          v
[ Livello fisico ]            Bit / segnali elettrici, ottici o radio
```

### Esempio: apertura di un sito HTTPS

```text
Dati HTTP
  + intestazione TLS
  + intestazione TCP
  + intestazione IP
  + intestazione Ethernet/Wi-Fi
  --> bit trasmessi sulla rete
```

Sul destinatario avviene il processo inverso:

```text
bit
  --> frame Ethernet/Wi-Fi
  --> pacchetto IP
  --> segmento TCP
  --> dati TLS/HTTP
  --> applicazione
```

### PDU: Protocol Data Unit

| Livello | PDU tipica |
|---|---|
| Applicazione | dati / messaggio |
| Trasporto | segmento TCP; datagramma UDP |
| Rete | pacchetto IP |
| Collegamento dati | frame o trama |
| Fisico | bit |

> Nei quiz il termine “pacchetto” è spesso usato genericamente. In senso rigoroso, un pacchetto è normalmente associato al livello IP; il livello 2 usa frame.

---

# 3. Modello OSI

## 3.1 Definizione

Il **modello OSI** (*Open Systems Interconnection*) è un modello di riferimento elaborato da ISO che descrive la comunicazione di rete attraverso **sette livelli**.

Non coincide perfettamente con lo stack realmente impiegato su Internet, ma è essenziale per:

- classificare protocolli e dispositivi;
- individuare problemi di comunicazione;
- comprendere l'architettura delle reti;
- rispondere correttamente ai quiz.

## 3.2 Struttura del modello OSI

```text
+---------------------------------------------------+
| 7. Applicazione     | Servizi alle applicazioni   |
+---------------------------------------------------+
| 6. Presentazione    | Formati, cifratura, codifica|
+---------------------------------------------------+
| 5. Sessione         | Gestione dialogo/sessioni   |
+---------------------------------------------------+
| 4. Trasporto        | TCP, UDP, porte, affidabilità|
+---------------------------------------------------+
| 3. Rete             | IP, routing, router         |
+---------------------------------------------------+
| 2. Collegamento dati| Ethernet, MAC, switch       |
+---------------------------------------------------+
| 1. Fisico           | Bit, cavi, onde radio       |
+---------------------------------------------------+
```

Da memorizzare, dal basso verso l'alto:

```text
Fisico → Collegamento dati → Rete → Trasporto → Sessione → Presentazione → Applicazione
```

---

## 3.3 Livello 1 OSI — Fisico (*Physical Layer*)

### Definizione

Il livello fisico definisce il modo in cui i **bit** sono trasmessi materialmente su un mezzo di comunicazione.

### Componenti

- cavi in rame;
- fibra ottica;
- antenne e onde radio;
- connettori;
- hub;
- ripetitori;
- caratteristiche elettriche, ottiche, radio e meccaniche.

### Struttura

```text
Bit logici:  1 0 1 1 0
             |
             v
Segnale: elettrico / ottico / radio
             |
             v
Mezzo trasmissivo
```

### Funzionamento

1. Il livello 2 consegna una sequenza di bit al livello fisico.
2. Il livello fisico converte i bit in segnali.
3. I segnali attraversano il mezzo.
4. Il ricevente converte i segnali di nuovo in bit.

### Standard coinvolti

- IEEE 802.3: Ethernet;
- IEEE 802.11: Wi-Fi;
- standard di cablaggio e connettività fisica.

### Vantaggi

- rende possibile l'interoperabilità fisica;
- permette comunicazioni su mezzi diversi;
- definisce velocità e caratteristiche del collegamento.

### Limiti

- non instrada dati;
- non usa indirizzi IP o MAC;
- non identifica applicazioni;
- non garantisce consegna end-to-end;
- non cifra autonomamente il traffico.

### Differenza dal livello 2

| Livello fisico | Collegamento dati |
|---|---|
| Trasporta bit e segnali | Organizza bit in frame |
| Riguarda cavi, radio e segnali | Riguarda MAC, Ethernet e controllo locale |
| Non usa indirizzi MAC | Usa indirizzi MAC |

### Casi d'uso

- fibra tra sedi;
- cavo Ethernet tra PC e switch;
- trasmissione radio fra client e access point Wi-Fi.

---

## 3.4 Livello 2 OSI — Collegamento dati (*Data Link Layer*)

### Definizione

Il livello di collegamento dati consente la comunicazione **all'interno della stessa rete locale o dello stesso collegamento**. Organizza i bit in **frame** e usa tipicamente gli **indirizzi MAC**.

### Componenti

- schede di rete;
- switch;
- bridge;
- access point per le funzioni di collegamento;
- indirizzi MAC;
- frame Ethernet;
- VLAN.

### Struttura: frame Ethernet

```text
+----------------+----------------+----------+-------------------+------+
| MAC destinazione| MAC sorgente   | Tipo     | Dati              | FCS  |
+----------------+----------------+----------+-------------------+------+
```

- **MAC destinazione**: identifica la scheda destinataria nella LAN.
- **MAC sorgente**: identifica la scheda mittente nella LAN.
- **Tipo**: indica il protocollo incapsulato, ad esempio IPv4 o IPv6.
- **Dati**: contiene normalmente un pacchetto IP.
- **FCS**: controllo di errore del frame.

### Funzionamento

1. Un host deve inviare un pacchetto IP.
2. Determina il MAC del destinatario locale o del gateway predefinito.
3. Incapsula il pacchetto IP in un frame Ethernet.
4. Lo switch esamina il MAC di destinazione.
5. Lo switch inoltra il frame verso la porta opportuna.

### Indirizzo MAC

Un indirizzo **MAC** (*Media Access Control*) identifica una specifica interfaccia di rete al livello 2.

Esempio:

```text
00:1A:2B:3C:4D:5E
```

Caratteristiche:

- normalmente è lungo **48 bit** (6 byte);
- è usato soprattutto nella rete locale;
- non serve per instradare pacchetti attraverso Internet;
- può essere modificato o falsificato (*spoofing*) via software.

### Switch

Uno **switch** opera principalmente al livello 2.

```text
PC-A ----\
PC-B ----- [ SWITCH ] ----- Server
PC-C ----/
```

Lo switch apprende associazioni:

```text
MAC address  <-->  porta dello switch
```

Esempio:

```text
00:11:22:33:44:55 --> porta 1
AA:BB:CC:DD:EE:FF --> porta 4
```

Quando riceve un frame:

- apprende il MAC sorgente;
- cerca il MAC di destinazione nella tabella;
- se lo conosce, inoltra solo sulla porta corretta;
- se non lo conosce, effettua *flooding* sulle porte della VLAN interessata.

### VLAN

Una **VLAN** (*Virtual Local Area Network*) separa logicamente una rete di livello 2, pur usando apparati fisici condivisi.

```text
                  +----------------+
PC Ufficio A -----|                |
PC Ufficio B -----| Switch gestito |---- VLAN 10: Amministrazione
PC Ufficio C -----|                |---- VLAN 20: Personale
PC Ospiti --------|                |---- VLAN 30: Ospiti
                  +----------------+
```

Dispositivi collocati in VLAN differenti:

- appartengono a domini di broadcast diversi;
- non comunicano direttamente a livello 2;
- richiedono routing inter-VLAN, normalmente tramite router o switch di livello 3.

### Standard coinvolti

- IEEE 802.3: Ethernet;
- IEEE 802.11: Wi-Fi;
- IEEE 802.1Q: VLAN tagging.

### Vantaggi

- comunicazione efficiente nella LAN;
- inoltro selettivo da parte degli switch;
- segmentazione logica tramite VLAN;
- rilevazione di alcuni errori tramite FCS.

### Limiti

- non sostituisce il routing IP;
- il MAC non consente di raggiungere host remoti su Internet;
- una VLAN, da sola, non è una protezione completa;
- FCS rileva errori del frame, ma non garantisce consegna end-to-end.

### Hub, switch e router

| Dispositivo | Livello prevalente | Funzione |
|---|---:|---|
| Hub | 1 | Ripete il segnale verso tutte le porte |
| Switch | 2 | Inoltra frame in base al MAC |
| Router | 3 | Instrada pacchetti IP fra reti diverse |

### Casi d'uso

- LAN di un ente pubblico;
- separazione di rete interna, rete ospiti e IoT tramite VLAN;
- collegamento di PC, stampanti e server a uno switch.

---

## 3.5 Livello 3 OSI — Rete (*Network Layer*)

### Definizione

Il livello di rete trasferisce pacchetti fra **reti differenti**, selezionando un percorso. Il protocollo fondamentale è **IP** (*Internet Protocol*).

### Componenti

- IPv4 e IPv6;
- router;
- tabelle di routing;
- indirizzi IP;
- ICMP;
- protocolli di routing, come OSPF e BGP.

### Struttura

```text
Rete A                    Rete B                     Rete C
PC ---- Switch ---- Router 1 ---- Router 2 ---- Switch ---- Server
        192.168.1.0/24          Internet              10.0.0.0/24
```

Un router collega reti diverse; ogni sua interfaccia appartiene normalmente a una rete IP differente.

### Indirizzo IP

Un indirizzo IP identifica logicamente un'interfaccia di rete in una rete IP.

```text
IPv4: 192.168.1.25
IPv6: 2001:db8:1234::25
```

> L'IP è logico e gerarchico; il MAC è usato per il trasferimento locale dei frame.

### Funzionamento del routing

Un PC `192.168.1.10/24` vuole contattare `8.8.8.8`:

```text
1. Il PC confronta la propria rete con quella della destinazione.
2. Rileva che 8.8.8.8 non appartiene alla LAN 192.168.1.0/24.
3. Invia il frame al MAC del default gateway.
4. Nel frame è contenuto un pacchetto IP:
   sorgente: 192.168.1.10
   destinazione: 8.8.8.8
5. Il router rimuove l'intestazione di livello 2.
6. Consulta la tabella di routing.
7. Crea un nuovo frame per il collegamento successivo.
8. Inoltra il pacchetto al router seguente.
```

Da memorizzare:

```text
MAC address: cambia a ogni tratto locale (hop)
IP address: resta normalmente sorgente/destinazione end-to-end
```

Eccezione importante: **NAT**, che può modificare IP e/o porte.

### IP

#### Cosa fa

- indirizzamento logico;
- trasporto di pacchetti tra reti;
- instradamento tramite router;
- in IPv4, limite di vita tramite TTL.

#### Dove opera

- **Livello 3 OSI**;
- livello **Internet** TCP/IP.

#### Perché viene utilizzato

Permette l'interconnessione di reti eterogenee ed è la base di Internet.

#### Cosa non fa

IP da solo:

- non garantisce consegna;
- non garantisce l'ordine dei dati;
- non gestisce applicazioni;
- non cifra automaticamente il traffico;
- non instaura una connessione affidabile.

#### Con cosa viene confuso

- **TCP**: TCP fornisce affidabilità e porte; IP instrada.
- **MAC address**: MAC opera nella LAN; IP viene usato tra reti.
- **DNS**: DNS traduce nomi in IP; non trasporta pacchetti.

### IPv4 e IPv6

| Caratteristica | IPv4 | IPv6 |
|---|---|---|
| Lunghezza indirizzo | 32 bit | 128 bit |
| Esempio | `192.168.1.10` | `2001:db8::10` |
| Indirizzi teorici | circa 4,3 miliardi | enormemente maggiore |
| NAT | molto diffuso | non necessario per scarsità di indirizzi |
| Broadcast | presente | non previsto; multicast/anycast |

### ICMP

**ICMP** (*Internet Control Message Protocol*) è un protocollo di controllo associato al livello IP.

#### Cosa fa

- comunica errori e condizioni di rete;
- supporta `ping`;
- contribuisce a `traceroute`/`tracert`.

#### Dove opera

- Livello 3 OSI;
- livello Internet TCP/IP;
- incapsulato in IP.

#### Perché viene utilizzato

Serve per diagnosi e segnalazione di errori, ad esempio rete o destinazione irraggiungibile.

#### Cosa non fa

- non trasporta pagine Web;
- non sostituisce TCP;
- non usa porte TCP o UDP;
- un ping riuscito non prova che un sito Web sia funzionante.

#### Con cosa viene confuso

- **ping**: ping è uno strumento che usa ICMP.
- **TCP**: ICMP non apre connessioni TCP.

### Vantaggi del livello rete

- interconnette reti differenti;
- rende possibile Internet;
- supporta percorsi dinamici e ridondanti;
- separa la comunicazione globale dalla tecnologia della LAN.

### Limiti

- IP è *best effort*;
- non offre sicurezza o confidenzialità per impostazione predefinita;
- un routing errato può rendere reti irraggiungibili.

### Casi d'uso

- collegamento tra sedi della PA;
- accesso a servizi cloud;
- routing fra VLAN;
- accesso a Internet da una LAN.

---

## 3.6 Livello 4 OSI — Trasporto (*Transport Layer*)

### Definizione

Il livello di trasporto consente la comunicazione **tra processi/applicazioni** su host differenti. Usa le **porte** per distinguere i servizi.

Protocolli principali:

- **TCP**: affidabile e orientato alla connessione;
- **UDP**: leggero, senza connessione e senza garanzie intrinseche.

### Componenti

- TCP;
- UDP;
- porte TCP e UDP;
- segmenti TCP;
- datagrammi UDP;
- meccanismi TCP di conferma, ritrasmissione e controllo di flusso.

### Porte

Una porta identifica un servizio o processo su un host.

```text
Indirizzo IP: 203.0.113.10
Porta TCP:    443
Servizio:     HTTPS
```

Una comunicazione è identificabile mediante la quadrupla:

```text
IP sorgente : porta sorgente <--> IP destinazione : porta destinazione
```

Esempio:

```text
192.168.1.10:51520 <--> 93.184.216.34:443
```

> Le porte non sono porte fisiche: sono identificatori logici da 0 a 65535.

### TCP

#### Definizione

**TCP** (*Transmission Control Protocol*) è un protocollo affidabile e orientato alla connessione.

#### Cosa fa

- instaura una connessione logica;
- garantisce consegna affidabile nell'ambito della connessione;
- riordina segmenti ricevuti;
- ritrasmette dati mancanti;
- usa numeri di sequenza e ACK;
- applica controllo di flusso e congestione;
- usa porte sorgente e destinazione.

#### Dove opera

- Livello 4 OSI;
- Trasporto TCP/IP.

#### Perché viene utilizzato

Quando è importante ricevere dati completi e ordinati, anche a costo di maggiore overhead.

#### Cosa non fa

- non assegna IP;
- non sceglie il percorso;
- non cifra automaticamente;
- non traduce nomi DNS;
- non garantisce che il software remoto elabori correttamente i dati.

#### Con cosa viene confuso

- **IP**: IP instrada; TCP rende affidabile il trasporto.
- **HTTP**: HTTP è applicativo e spesso usa TCP.
- **TLS**: TLS cifra/autentica; TCP non cifra.
- **UDP**: UDP non garantisce ordine e consegna.

#### Three-way handshake

```text
Client                                  Server
  | -------- SYN ----------------------> |
  | <----- SYN + ACK ------------------- |
  | -------- ACK ----------------------> |
  |                                       |
  | ===== Connessione TCP stabilita ===== |
```

1. **SYN**: il client avvia la connessione.
2. **SYN-ACK**: il server accetta e conferma.
3. **ACK**: il client conferma.

#### Chiusura TCP semplificata

```text
Client                                  Server
  | -------- FIN ----------------------> |
  | <------- ACK ----------------------- |
  | <------- FIN ----------------------- |
  | -------- ACK ----------------------> |
```

### UDP

#### Definizione

**UDP** (*User Datagram Protocol*) è un protocollo senza connessione, leggero e con minore overhead rispetto a TCP.

#### Cosa fa

- trasporta datagrammi tra processi tramite porte;
- consente trasmissioni rapide;
- include un checksum;
- non richiede la creazione preventiva di una connessione.

#### Dove opera

- Livello 4 OSI;
- Trasporto TCP/IP.

#### Perché viene utilizzato

Quando sono prioritari bassa latenza e semplicità, o quando l'applicazione gestisce da sé recupero e affidabilità.

#### Cosa non fa

- non garantisce consegna;
- non garantisce ordine;
- non ritrasmette;
- non stabilisce una connessione come TCP;
- non cifra.

#### Con cosa viene confuso

- **TCP**: TCP è affidabile e connection-oriented; UDP no.
- **IP**: UDP usa IP, ma aggiunge le porte.
- **DNS**: DNS è applicativo e usa spesso UDP.

### TCP e UDP a confronto

| Caratteristica | TCP | UDP |
|---|---|---|
| Livello | Trasporto | Trasporto |
| Connessione | Sì | No |
| Affidabilità | Sì, con ACK e ritrasmissioni | No, non intrinseca |
| Ordine | Garantito | Non garantito |
| Overhead | Maggiore | Minore |
| Esempi | HTTPS, SMTP, SSH | DNS, VoIP, streaming, giochi |

> Dire che “UDP è sempre più veloce” è una semplificazione: ha minore overhead, ma le prestazioni dipendono dalla rete e dall'applicazione.

### Casi d'uso

**TCP:** HTTPS, SMTP, trasferimento file, SSH.

**UDP:** DNS, VoIP, comunicazioni audio/video in tempo reale, giochi, DHCP, NTP.

---

## 3.7 Livello 5 OSI — Sessione (*Session Layer*)

### Definizione

Il livello di sessione gestisce il dialogo tra applicazioni: avvio, mantenimento, sincronizzazione e chiusura delle sessioni.

### Componenti e funzioni

- instaurazione sessione;
- gestione del dialogo;
- sincronizzazione;
- eventuali punti di ripristino (*checkpoint*).

### Funzionamento

```text
1. Le applicazioni avviano una sessione.
2. La sessione coordina lo scambio.
3. Se necessario, gestisce punti di sincronizzazione.
4. La sessione viene chiusa.
```

### Standard e protocolli

Nello stack Internet moderno questo livello raramente è separato in modo netto: molte funzioni sono assorbite da protocolli applicativi, librerie e meccanismi di autenticazione.

### Vantaggi e limiti

- separa concettualmente dialogo e trasporto;
- utile per comprendere sessioni utente e autenticazione;
- nella pratica TCP/IP spesso non è implementato come strato autonomo.

### Differenza da TCP

| Sessione | TCP |
|---|---|
| Gestisce il dialogo logico applicativo | Gestisce trasporto affidabile tra processi |
| Concetto OSI spesso assorbito dall'applicazione | Protocollo concreto |
| Può riguardare una sessione utente | Usa porte, segmenti, ACK e ritrasmissioni |

### Caso d'uso

Una sessione autenticata su un portale può usare cookie o token. Non va confusa con una connessione TCP: una sessione utente può attraversare più connessioni TCP.

---

## 3.8 Livello 6 OSI — Presentazione (*Presentation Layer*)

### Definizione

Il livello di presentazione cura il modo in cui i dati sono **rappresentati**, convertiti, compressi o cifrati, così che mittente e destinatario possano interpretarli correttamente.

### Componenti e funzioni

- codifica caratteri: UTF-8, Unicode;
- serializzazione e formati: JSON, XML;
- compressione;
- cifratura;
- conversione di formati.

### Esempio

```text
Applicazione: oggetto "utente"
        |
        v
Presentazione: conversione in JSON UTF-8
        |
        v
Trasporto: invio tramite TCP
```

### Standard coinvolti

- UTF-8 e Unicode;
- JSON;
- XML;
- JPEG, PNG, PDF;
- TLS per la cifratura nella pratica moderna.

> TLS viene spesso associato ai livelli 5-6 OSI; nello stack TCP/IP opera sopra il trasporto e sotto protocolli applicativi come HTTP.

### Vantaggi

- interoperabilità;
- gestione coerente di codifica, compressione e cifratura;
- riduzione dei problemi di interpretazione dei dati.

### Limiti

- spesso non è implementato come livello separato;
- cifratura e compressione aumentano complessità e uso della CPU.

### Differenza dal livello applicativo

| Presentazione | Applicazione |
|---|---|
| Come sono codificati/protetti i dati | Quale servizio è fornito |
| UTF-8, JSON, TLS | HTTP, SMTP, DNS |
| Si occupa del formato | Si occupa della funzione |

### Casi d'uso

- browser che interpreta UTF-8;
- API JSON;
- HTTP protetto con TLS;
- compressione di contenuti Web.

---

## 3.9 Livello 7 OSI — Applicazione (*Application Layer*)

### Definizione

Il livello applicativo fornisce servizi di rete direttamente utilizzabili dalle applicazioni dell'utente o dai programmi di sistema.

### Componenti

- browser;
- client e server di posta;
- server Web;
- resolver DNS;
- protocolli come HTTP, DNS, SMTP, FTP e DHCP.

### Funzionamento

```text
Browser
  |
  | richiesta HTTP
  v
Server Web
```

Il browser non invia direttamente bit in rete: utilizza tutti i livelli sottostanti.

### Vantaggi

- fornisce servizi concreti: Web, e-mail, DNS, trasferimento file;
- standardizza la comunicazione applicativa;
- separa logica applicativa e infrastruttura.

### Limiti

- dipende dai livelli sottostanti;
- protocolli applicativi non sicuri possono esporre dati;
- non sostituisce IP o TCP.

### Casi d'uso

- browser: HTTP/HTTPS;
- client e-mail: SMTP, IMAP, POP3;
- risoluzione nomi: DNS;
- configurazione client: DHCP.

---

# 4. Modello TCP/IP

## Definizione

Il **modello TCP/IP** è il modello pratico e storico su cui si fonda Internet. Il suo nome deriva da:

- **TCP** (*Transmission Control Protocol*);
- **IP** (*Internet Protocol*).

A differenza del modello OSI, non nasce soprattutto come modello teorico: descrive una famiglia concreta di protocolli realmente usati nelle reti IP.

## Struttura a quattro livelli

```text
+-----------------------------------------------+
| Applicazione                                  |
| HTTP, HTTPS, DNS, SMTP, DHCP, SSH, FTP...     |
+-----------------------------------------------+
| Trasporto                                     |
| TCP, UDP                                      |
+-----------------------------------------------+
| Internet                                      |
| IP, ICMP, routing                             |
+-----------------------------------------------+
| Accesso alla rete / Link                      |
| Ethernet, Wi-Fi, ARP, fisico                  |
+-----------------------------------------------+
```

Alcuni testi dividono “Accesso alla rete” in collegamento dati e fisico: in questo caso illustrano un modello TCP/IP a cinque livelli.

## Corrispondenza OSI/TCP-IP

| OSI | TCP/IP | Esempi |
|---|---|---|
| 7. Applicazione | Applicazione | HTTP, DNS, SMTP, DHCP |
| 6. Presentazione | Applicazione | TLS, UTF-8, JSON |
| 5. Sessione | Applicazione | gestione sessioni applicative |
| 4. Trasporto | Trasporto | TCP, UDP |
| 3. Rete | Internet | IPv4, IPv6, ICMP |
| 2. Collegamento dati | Accesso rete | Ethernet, Wi-Fi, VLAN |
| 1. Fisico | Accesso rete | cavi, fibra, radio |

Da memorizzare:

```text
OSI: 7 livelli.
TCP/IP: normalmente 4 livelli.
OSI: modello di riferimento didattico.
TCP/IP: modello pratico associato a Internet.
```

---

# 5. Protocolli applicativi fondamentali

## 5.1 DNS — Domain Name System

### Definizione

Il **DNS** traduce nomi di dominio in informazioni utilizzabili dalla rete, soprattutto indirizzi IP.

```text
www.ente.it --> 203.0.113.25
```

### Cosa fa

- risolve nomi di dominio in IP;
- può fare risoluzione inversa da IP a nome;
- gestisce record come MX per la posta;
- usa un'architettura distribuita e gerarchica.

### Dove opera

- Livello 7 OSI;
- Applicazione TCP/IP;
- normalmente UDP porta 53;
- anche TCP porta 53, ad esempio per trasferimenti di zona o risposte grandi.

### Perché viene utilizzato

Gli utenti ricordano nomi; le reti instradano tramite indirizzi IP.

### Cosa non fa

- non trasferisce pagine Web;
- non assegna IP ai client;
- non instrada pacchetti;
- il DNS tradizionale non cifra automaticamente le richieste.

### Con cosa viene confuso

- **DHCP**: assegna configurazione; DNS risolve nomi.
- **HTTP**: trasferisce contenuti Web; DNS trova l'IP.
- **URL**: contiene un dominio, ma non è DNS.

### Sequenza

```text
Utente digita: https://www.ente.it/servizi
                     |
                     v
Browser chiede al DNS: qual è l'IP di www.ente.it?
                     |
                     v
DNS risponde: 203.0.113.25
                     |
                     v
Browser apre la connessione al server indicato
```

---

## 5.2 DHCP — Dynamic Host Configuration Protocol

### Definizione

**DHCP** assegna automaticamente i parametri di configurazione IP ai dispositivi.

### Cosa fa

Può assegnare:

- indirizzo IP;
- subnet mask/prefisso;
- gateway predefinito;
- server DNS;
- durata della concessione (*lease*).

### Dove opera

- Livello 7 OSI;
- Applicazione TCP/IP;
- UDP 67 lato server e UDP 68 lato client.

### Perché viene utilizzato

Evita configurazioni manuali ripetitive e riduce errori amministrativi.

### Cosa non fa

- non risolve nomi;
- non instrada;
- non protegge automaticamente la rete;
- non sostituisce DNS.

### Con cosa viene confuso

- **DNS**: DNS risolve nomi; DHCP assegna parametri.
- **NAT**: NAT traduce indirizzi/porte; DHCP assegna IP.
- **ARP**: ARP associa IP già noto a MAC; DHCP assegna l'IP.

### Sequenza DORA

```text
Client                         Server DHCP
  | ---- DHCP Discover ------> |  "C'è un server DHCP?"
  | <----- DHCP Offer -------- |  "Ti propongo questo IP"
  | ---- DHCP Request -------> |  "Richiedo quell'IP"
  | <----- DHCP ACK ---------- |  "Assegnazione confermata"
```

### Caso d'uso

Quando un dipendente collega un portatile alla rete dell'ufficio, DHCP configura IP, gateway e DNS.

---

## 5.3 HTTP e HTTPS

### Definizione

**HTTP** (*Hypertext Transfer Protocol*) è il protocollo applicativo usato per la comunicazione Web fra client e server.

**HTTPS** è HTTP protetto tramite **TLS**.

### Cosa fa HTTP

- definisce richieste e risposte Web;
- consente la richiesta di risorse;
- consente al server di restituire contenuti e codici di stato.

```text
Client:
GET /servizi HTTP/1.1
Host: www.ente.it

Server:
HTTP/1.1 200 OK
Content-Type: text/html

<html>...</html>
```

### Dove opera

- Livello 7 OSI;
- Applicazione TCP/IP.

Porte tipiche:

| Protocollo | Porta tipica |
|---|---:|
| HTTP | TCP 80 |
| HTTPS | TCP 443 |

> HTTP/3 usa QUIC su UDP, tipicamente sulla porta 443. Nei quiz di base la risposta attesa è normalmente “HTTPS usa TCP 443”, ma è utile conoscere questa evoluzione.

### Perché viene utilizzato

Per siti, portali, API e servizi Web.

### Cosa non fa

HTTP:

- non cifra necessariamente il traffico;
- non assegna IP;
- non risolve nomi;
- non esegue routing.

HTTPS:

- protegge il canale mediante TLS, ma non garantisce che ogni contenuto sia affidabile;
- non sostituisce firewall, antivirus o corretta gestione delle credenziali.

### Con cosa viene confuso

- **HTTP vs HTTPS**: HTTPS è HTTP su un canale protetto da TLS.
- **TLS vs TCP**: TCP trasporta affidabilmente; TLS cifra e autentica.
- **DNS**: DNS trova l'IP; HTTP/HTTPS scambia contenuti.

### Schema HTTPS

```text
Browser
  |
  | DNS: trova l'IP del sito
  v
Server Web
  |
  | TCP (oppure QUIC per HTTP/3)
  |
  | TLS: crea il canale protetto
  |
  | HTTP: richieste e risposte Web
```

---

## 5.4 TLS — Transport Layer Security

### Definizione

**TLS** è un protocollo crittografico per la protezione delle comunicazioni in rete.

### Cosa fa

- cifra i dati;
- protegge l'integrità;
- autentica normalmente il server con certificati digitali;
- può autenticare il client mediante certificato, se configurato.

### Dove opera

Nel modello OSI è spesso associato ai livelli 5-6. Nello stack TCP/IP opera di fatto tra applicazione e trasporto:

```text
Applicazione (HTTP)
        |
       TLS
        |
Trasporto (TCP)
```

### Perché viene utilizzato

Per evitare che terzi leggano o alterino facilmente il traffico, ad esempio durante l'accesso a un portale della PA.

### Cosa non fa

- non rende sicura un'applicazione vulnerabile;
- non elimina phishing o furto di password;
- non sostituisce autorizzazione, backup o controllo accessi;
- non garantisce da solo che l'utente sia legittimo.

### Con cosa viene confuso

- **HTTPS**: HTTPS è HTTP con TLS.
- **SSL**: predecessore storico ormai obsoleto; il protocollo moderno è TLS.
- **VPN**: TLS protegge una comunicazione/applicazione; una VPN può creare un tunnel per più tipi di traffico.

---

## 5.5 SMTP, POP3 e IMAP

### SMTP

**Definizione:** **SMTP** (*Simple Mail Transfer Protocol*) è usato principalmente per inviare e inoltrare e-mail.

- **Cosa fa:** invia messaggi dal client al server e tra server di posta.
- **Dove opera:** Livello 7 OSI / Applicazione TCP-IP; tipicamente TCP 25, 587 o 465.
- **Perché:** standard per invio e trasferimento della posta.
- **Cosa non fa:** non è il protocollo principale per leggere e sincronizzare la casella.
- **Confusione comune:** POP3 e IMAP, usati prevalentemente per accesso/ricezione.

### POP3

- **Cosa fa:** consente il download/accesso ai messaggi dal server, spesso in logica locale.
- **Dove opera:** Livello applicativo; TCP 110 o TCP 995 nella variante protetta.
- **Cosa non fa:** non è pensato principalmente per sincronizzazione su molti dispositivi.
- **Confusione comune:** IMAP.

### IMAP

- **Cosa fa:** consente accesso e sincronizzazione dei messaggi, che restano normalmente sul server.
- **Dove opera:** Livello applicativo; TCP 143 o TCP 993 nella variante protetta.
- **Cosa non fa:** non è il protocollo standard per inviare e-mail ad altri server.
- **Confusione comune:** SMTP e POP3.

| Protocollo | Funzione prevalente | Porta tipica |
|---|---|---:|
| SMTP | Invio/inoltro e-mail | TCP 25 / 587 / 465 |
| POP3 | Download/accesso messaggi | TCP 110 / 995 |
| IMAP | Accesso e sincronizzazione sul server | TCP 143 / 993 |

---

# 6. ARP: associazione tra IP e MAC

## Definizione

**ARP** (*Address Resolution Protocol*) associa, nelle reti IPv4, un indirizzo IP locale al corrispondente indirizzo MAC.

## Cosa fa

Quando un host conosce l'IP di un dispositivo nella LAN ma non il suo MAC, invia una richiesta ARP in broadcast:

```text
"Chi possiede l'indirizzo IP 192.168.1.1?"
```

Il proprietario risponde:

```text
"192.168.1.1 corrisponde al MAC AA:BB:CC:DD:EE:FF"
```

## Dove opera

ARP è generalmente collocato al confine fra livello 2 e livello 3. Nei quiz è spesso associato al livello 2 o considerato un protocollo di supporto al livello rete nella LAN IPv4.

## Perché viene utilizzato

Per incapsulare un pacchetto IP in un frame Ethernet, che richiede un MAC di destinazione locale.

## Cosa non fa

- non risolve nomi DNS;
- non trova il MAC di un host remoto su Internet;
- non instrada pacchetti;
- non è usato in IPv6: IPv6 usa NDP (*Neighbor Discovery Protocol*).

## Con cosa viene confuso

- **DNS**: nome → IP.
- **DHCP**: assegna parametri IP.
- **NAT**: traduce indirizzi/porte.

## Esempio pratico

```text
PC:     192.168.1.10
Gateway: 192.168.1.1

1. Il PC deve raggiungere Internet.
2. Sa di dover inviare i dati al gateway 192.168.1.1.
3. Esegue ARP per trovare il MAC del gateway.
4. Inserisce il MAC del gateway nel frame.
5. Mantiene l'IP remoto finale nel pacchetto IP.
```

---

# 7. NAT: traduzione degli indirizzi

## Definizione

Il **NAT** (*Network Address Translation*) è una tecnica in cui router o firewall modifica gli indirizzi IP e spesso anche le porte dei pacchetti in transito.

## Cosa fa

Caso tipico: **PAT** (*Port Address Translation*), spesso chiamato impropriamente NAT.

```text
Rete privata:
PC1 192.168.1.10
PC2 192.168.1.11

Router pubblico:
198.51.100.20
```

Molti host privati possono condividere un solo IP pubblico:

```text
192.168.1.10:50000 --> 198.51.100.20:40001
192.168.1.11:50000 --> 198.51.100.20:40002
```

## Dove opera

Principalmente:

- livello 3, modificando IP;
- livello 4, se modifica porte TCP/UDP.

## Perché viene utilizzato

- conservare indirizzi IPv4 pubblici;
- usare indirizzi privati nelle LAN;
- separare rete interna e pubblica;
- pubblicare selettivamente servizi con *port forwarding*.

## Cosa non fa

- non è un firewall completo;
- non cifra;
- non sostituisce una VPN;
- non è autenticazione;
- non elimina il bisogno di regole di sicurezza.

## Con cosa viene confuso

- **Firewall**: NAT traduce; firewall filtra secondo regole.
- **Proxy**: opera spesso al livello applicativo e può terminare/ricreare connessioni.
- **VPN**: crea tunnel protetti; NAT traduce indirizzi/porte.

---

# 8. Esempio completo: browser verso un sito HTTPS

Supponiamo che un utente voglia visitare:

```text
https://www.servizi-comune.example/login
```

## Sequenza passo-passo

```text
1. Il PC riceve configurazione tramite DHCP:
   IP locale, maschera/prefisso, gateway, DNS.

2. L'utente digita l'URL nel browser.

3. Il browser chiede al DNS l'IP di www.servizi-comune.example.

4. Il DNS restituisce, per esempio, 203.0.113.30.

5. Il PC rileva che 203.0.113.30 non è nella propria rete locale.

6. Il PC usa ARP per conoscere il MAC del gateway locale.

7. Il PC apre una connessione TCP verso 203.0.113.30:443.

8. Avviene il three-way handshake TCP.

9. Viene negoziata TLS:
   - il server presenta un certificato;
   - browser e server stabiliscono chiavi crittografiche;
   - il canale diventa cifrato.

10. Il browser invia una richiesta HTTP, ad esempio GET /login.

11. I router intermedi inoltrano pacchetti IP verso il server.

12. Il server invia la risposta HTTPS.

13. Il browser decifra i dati e visualizza la pagina.
```

## Incapsulamento semplificato

```text
+------------------------------------------------+
| HTTP: GET /login                               |
+------------------------------------------------+
| TLS: dati HTTP cifrati                         |
+------------------------------------------------+
| TCP: porta sorgente -> porta 443               |
+------------------------------------------------+
| IP: IP PC -> IP server                         |
+------------------------------------------------+
| Ethernet/Wi-Fi: MAC PC -> MAC gateway          |
+------------------------------------------------+
| Bit sul mezzo fisico                           |
+------------------------------------------------+
```

---

# 9. Dispositivi di rete e livelli OSI

| Dispositivo | Livello OSI prevalente | Cosa fa | Cosa non fa |
|---|---:|---|---|
| Ripetitore | 1 | Rigenera/amplifica segnale | Non interpreta MAC o IP |
| Hub | 1 | Ripete segnali su tutte le porte | Non seleziona il destinatario |
| Switch | 2 | Inoltra frame in base al MAC | Non effettua normalmente routing IP |
| Bridge | 2 | Collega segmenti LAN e filtra frame | Non sostituisce il router |
| Access point | 2 | Collega client Wi-Fi alla LAN | Non instrada necessariamente tra reti |
| Router | 3 | Instrada IP fra reti | Non interpreta normalmente HTTP |
| Firewall | 3/4/7 secondo tipo | Applica politiche di sicurezza | Non coincide automaticamente con NAT/proxy |
| Proxy | 7 normalmente | Intermedia richieste applicative | Non è un router generico |

> Uno switch multilayer o Layer 3 switch può svolgere anche funzioni di routing: non ogni switch opera esclusivamente al livello 2.

---

# 10. Domande-trappola da concorso

## 1. “Il router instrada i frame Ethernet.”

**Falso o impreciso.** Il router riceve un frame, rimuove l'intestazione di livello 2, esamina il pacchetto IP e lo incapsula in un nuovo frame per il collegamento successivo.

## 2. “L'indirizzo MAC permette di raggiungere un computer ovunque su Internet.”

**Falso.** Il MAC è usato nella comunicazione locale a livello 2; per reti diverse si usano IP e routing.

## 3. “TCP e IP sono entrambi protocolli del livello rete.”

**Falso.** IP: livello 3 OSI/Internet TCP-IP. TCP: livello 4 OSI/Trasporto TCP-IP.

## 4. “UDP non effettua alcun controllo.”

**Falso.** UDP non garantisce consegna, ordine o ritrasmissione, ma può usare un checksum per rilevare alterazioni.

## 5. “HTTPS usa un protocollo di rete completamente diverso da HTTP.”

**Parzialmente falso.** HTTPS usa HTTP protetto da TLS; tradizionalmente usa TCP, in genere porta 443.

## 6. “DNS assegna gli indirizzi IP ai computer.”

**Falso.** DHCP assegna normalmente IP, gateway e DNS; DNS traduce nomi in IP.

## 7. “ARP associa un nome di dominio a un indirizzo IP.”

**Falso.** Questo è DNS. ARP associa un IPv4 locale al MAC corrispondente.

## 8. “Il modello OSI è il protocollo usato da Internet.”

**Falso.** OSI è un modello di riferimento; Internet usa concretamente la suite TCP/IP.

## 9. “Firewall e NAT sono la stessa cosa.”

**Falso.** NAT traduce IP/porte; firewall filtra il traffico secondo regole. Un apparato può svolgere entrambe le funzioni.

## 10. “La porta 443 identifica fisicamente una porta dello switch.”

**Falso.** La 443 è una porta logica TCP/UDP, tipicamente HTTPS. Le porte di switch sono interfacce fisiche o logiche di livello 2.

## 11. “TCP garantisce che l'applicazione destinataria elabori correttamente i dati.”

**Falso.** TCP garantisce il trasporto dei byte nella connessione, non l'assenza di errori dell'applicazione server.

## 12. “Ping verifica che un sito Web sia funzionante.”

**Falso.** Ping usa ICMP per verificare la raggiungibilità, se ICMP è consentito. HTTP/HTTPS potrebbe non funzionare anche con ping positivo, e viceversa.

---

# 11. Tabella comparativa delle nozioni principali

| Elemento | Livello OSI | Livello TCP/IP | Funzione | Identificatore principale | Da non confondere con |
|---|---:|---|---|---|---|
| Cavo/fibra/radio | 1 Fisico | Accesso rete | Trasmissione segnali | Bit | Ethernet, IP |
| Ethernet | 2 Collegamento | Accesso rete | Comunicazione LAN con frame | MAC | IP |
| Wi-Fi | 1-2 | Accesso rete | Connessione radio locale | MAC/SSID | Internet o IP |
| Switch | 2 | Accesso rete | Inoltra frame nella LAN | MAC | Router |
| VLAN | 2 | Accesso rete | Segmenta logicamente LAN | VLAN ID | Subnet IP |
| ARP | 2-3 | Accesso rete/Internet | Associa IPv4 locale a MAC | IP ↔ MAC | DNS, DHCP |
| IP | 3 Rete | Internet | Indirizzamento e routing | IP address | TCP, MAC |
| Router | 3 Rete | Internet | Collega reti e instrada IP | Tabella routing | Switch |
| ICMP | 3 Rete | Internet | Diagnostica/errori IP | Tipi/codici ICMP | TCP, ping |
| TCP | 4 Trasporto | Trasporto | Trasporto affidabile | Porte TCP | IP, TLS |
| UDP | 4 Trasporto | Trasporto | Trasporto senza garanzie | Porte UDP | TCP |
| DNS | 7 Applicazione | Applicazione | Dominio → IP | Dominio | DHCP, ARP |
| DHCP | 7 Applicazione | Applicazione | Configura automaticamente client | Lease/IP | DNS |
| HTTP | 7 Applicazione | Applicazione | Contenuti Web | URL/metodi HTTP | HTTPS, TCP |
| HTTPS | 7 + TLS | Applicazione | HTTP protetto con TLS | 443, tipicamente | HTTP semplice |
| TLS | 5-6 circa | Tra app e trasporto | Cifratura, integrità, autenticazione | Certificati | TCP, VPN |
| SMTP | 7 Applicazione | Applicazione | Invio/inoltro e-mail | Porte SMTP | IMAP, POP3 |
| IMAP | 7 Applicazione | Applicazione | Accesso/sincronizzazione e-mail | Porta IMAP | SMTP, POP3 |
| NAT | 3/4 | Internet/Trasporto | Traduzione IP/porte | IP e porte | Firewall, VPN |

---

# 12. Flashcard: 10 domande e risposte

## 1. Quanti livelli ha il modello OSI?

**Risposta:** Sette: fisico, collegamento dati, rete, trasporto, sessione, presentazione, applicazione.

## 2. Quanti livelli ha normalmente il modello TCP/IP?

**Risposta:** Quattro: accesso alla rete, Internet, trasporto, applicazione.

## 3. Qual è la differenza essenziale tra MAC e IP?

**Risposta:** Il MAC serve soprattutto per la consegna locale dei frame nella LAN; l'IP identifica logicamente un'interfaccia e permette il routing fra reti differenti.

## 4. A quale livello OSI opera un router?

**Risposta:** Principalmente al livello 3, rete, perché instrada pacchetti IP fra reti diverse.

## 5. A quale livello OSI opera uno switch tradizionale?

**Risposta:** Al livello 2, collegamento dati, perché inoltra frame in base agli indirizzi MAC.

## 6. Qual è la differenza fra TCP e UDP?

**Risposta:** TCP è connection-oriented e affidabile; UDP è connectionless, leggero, ma non garantisce consegna né ordine.

## 7. Quale protocollo traduce un dominio in un indirizzo IP?

**Risposta:** DNS.

## 8. Quale protocollo assegna IP, gateway e DNS a un client?

**Risposta:** DHCP.

## 9. Che cosa fa ARP?

**Risposta:** In una rete IPv4 locale associa un indirizzo IP al MAC corrispondente.

## 10. Che cosa aggiunge HTTPS rispetto a HTTP?

**Risposta:** Usa TLS per cifrare il traffico, proteggerne l'integrità e autenticare normalmente il server.

---

# 13. Le 15 nozioni da memorizzare

1. Il modello **OSI ha 7 livelli**; il modello **TCP/IP ne ha normalmente 4**.
2. I livelli OSI 5, 6 e 7 corrispondono generalmente al livello **Applicazione** di TCP/IP.
3. Il livello fisico trasmette **bit e segnali** su rame, fibra o radio.
4. Il livello collegamento dati usa **frame** e indirizzi **MAC**.
5. Uno **switch** opera principalmente al livello 2 e inoltra frame secondo MAC.
6. Un **router** opera principalmente al livello 3 e instrada pacchetti IP fra reti.
7. **IP** fornisce indirizzamento e routing, ma non garantisce consegna o ordine.
8. **TCP** usa porte e garantisce consegna ordinata mediante ACK e ritrasmissioni.
9. **UDP** usa porte ma non garantisce consegna, ordine o ritrasmissione.
10. **DNS** traduce domini in IP; non assegna indirizzi ai client.
11. **DHCP** assegna IP, gateway, DNS e altri parametri.
12. **ARP** associa IPv4 locale a MAC; non traduce nomi di dominio.
13. **HTTP** è il protocollo del Web; **HTTPS** è HTTP protetto con **TLS**.
14. **TLS** offre cifratura, integrità e autenticazione del server; non è TCP né VPN.
15. Attraversando un router, i **MAC cambiano a ogni hop**, mentre gli IP sorgente/destinazione restano normalmente invariati, salvo NAT.
