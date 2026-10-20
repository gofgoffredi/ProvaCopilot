# Guida: individuare la configurazione database usata da `mse-air-api` su Tomcat

## Obiettivo
Individuare il file di configurazione database dichiarato per `mse-air-api`, distinguendo il file incluso nella webapp da quello esterno indicato nel contesto Tomcat.

## Spiegazione iniziale: chi legge la configurazione?

La configurazione viene letta da un file di properties, ma **non necessariamente da quello dentro la webapp**. In Spring Boot, il file incluso nel WAR può essere affiancato o sostituito da configurazioni esterne. È **Spring** a interpretare le proprietà `spring.datasource.*`, non Tomcat direttamente.

Riferimento: [Spring Boot — Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html).

### Cosa mostrano i file esaminati

Sono presenti due configurazioni diverse per `mse-air-api`:

| File (percorso completo) | Database indicato | Utente |
| --- | --- | --- |
| `/home/tomcat/latest/webapps/mse-air-api/WEB-INF/classes/application.properties` | `ispettori` | `ispettori_user` |
| `/home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/application-air.properties` | `investigazione` | `investigazione` |

Dal comando `ls -lrt`, eseguito nella home dell'utente `tomcat`, risulta questo collegamento simbolico (estratto dell'output):

```text
lrwxrwxrwx  1 root   root        20 Jun  6  2023 latest -> apache-tomcat-10.1.9
```

Quindi `/home/tomcat/latest` e `/home/tomcat/apache-tomcat-10.1.9` indicano la stessa installazione su disco. Occorre comunque verificare il `CATALINA_BASE` del processo in esecuzione: il controllo è riportato al punto 4.

La sola presenza di `application-air.properties` in quella cartella **non dimostra che venga caricato**. Non è una posizione di ricerca predefinita di Spring Boot: serve un riferimento esplicito oppure del codice applicativo che lo carichi.

Riferimento: [Spring Boot — External Application Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files).

### Il primo file da controllare è `mse-air-api.xml`

Tomcat può definire la configurazione di ciascuna webapp tramite un descrittore in `/home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/`, includendo parametri del contesto o voci JNDI. Il candidato principale al collegamento con il file esterno è quindi `mse-air-api.xml`: un'ipotesi da verificare leggendolo.

Riferimento: [Tomcat — The Context Container](https://tomcat.apache.org/tomcat-10.1-doc/config/context.html).

Il comando legge soltanto il file:

```bash
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/mse-air-api.xml
```

Il contenuto e la sua interpretazione sono riportati al punto 3.

---

## Contesto iniziale

Sul server erano presenti:

- file `application.properties` dentro le webapp, sotto `/home/tomcat/latest/webapps/`; per AIR: `/home/tomcat/latest/webapps/mse-air-api/WEB-INF/classes/application.properties`;
- file esterni nella directory `/home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/`; per AIR: `/home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/application-air.properties`;
- descrittori di contesto Tomcat nella stessa directory; per AIR: `/home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/mse-air-api.xml`.

L'ipotesi iniziale era: "Tomcat prende il DB dal file dentro la webapp".

La verifica ha mostrato che, per `mse-air-api`, è configurato un riferimento a un file esterno.

Gli output riportati provengono dai controlli condivisi il **20 ottobre 2026**. I PID sono quelli osservati in quella sessione e possono cambiare dopo un riavvio. Le password sono oscurate.

---

## 1) Verifica dei file `application.properties` nelle webapp

### Comando

Eseguito dalla directory `/home/tomcat/latest/webapps`:

```bash
find . -name application.properties
```

### Cosa fa

- `find .` cerca nella directory corrente e nelle sottodirectory.
- `-name application.properties` seleziona le voci con quel nome.

### Output

```text
./mse-ws-api/WEB-INF/classes/application.properties
./mse-pc-api/WEB-INF/classes/application.properties
./mse-wf-manager-api/WEB-INF/classes/application.properties
./mse-gp-api/WEB-INF/classes/application.properties
./mse-demat-api/WEB-INF/classes/application.properties
./mse-inv-api/WEB-INF/classes/application.properties
./mse-folium-reader/WEB-INF/classes/application.properties
./mse-dc-api/WEB-INF/classes/application.properties
./mse-air-api/WEB-INF/classes/application.properties
./mse-anagrafica-api/WEB-INF/classes/application.properties
./societa-api/WEB-INF/classes/application.properties
./mse-contributi-api/WEB-INF/classes/application.properties
```

### Verifica del file interno AIR

Sempre dalla directory `/home/tomcat/latest/webapps`:

```bash
cat ./mse-air-api/WEB-INF/classes/application.properties
```

`cat` mostra il contenuto del file senza modificarlo.

Output:

```properties
server.port=8443
#server.port=8080
server.servlet.context-path=/mse-air-api
spring.datasource.url=jdbc:mysql://10.72.116.37:3306/ispettori
spring.datasource.username=ispettori_user
spring.datasource.password=.....
```

**Risultato parziale:** nella webapp estratta dal WAR è presente una configurazione DB che punta a `ispettori`.

---

## 2) Verifica configurazioni esterne in Tomcat

### Comando

```bash
ls /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost
```

### Cosa fa

Elenca il contenuto della directory dei contesti, dove in questa installazione sono presenti anche file di properties esterni.

### Output (estratto rilevante)

```text
application-air.properties
mse-air-api.xml
...
```

### Lettura del file esterno AIR

```bash
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/application-air.properties
```

Il comando mostra il contenuto del file esterno senza modificarlo.

Output:

```properties
#CLIENT DB
server.port=8443
server.servlet.context-path=/mse-air-api
spring.datasource.url=jdbc:mysql://10.72.116.37:3306/investigazione?allowLoadLocalInfile=true&rewriteBatchedStatements=true&cachePrepStmts=true&useServerPrepStmts=true
spring.datasource.username=investigazione
spring.datasource.password=.....
```

**Risultato parziale:** esiste una seconda configurazione DB, esterna, che punta a `investigazione`.

---

## 3) Punto decisivo: il descrittore di contesto `mse-air-api.xml`

### Comando usato

```bash
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/mse-air-api.xml
```

### Cosa fa

Mostra la configurazione di contesto Tomcat associata all'app `mse-air-api`.

### Output

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="spring.config.location" value="file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties" type="java.lang.String"/>
</Context>
```

### Interpretazione: cosa significa «via Environment JNDI di Tomcat»?

**JNDI** significa *Java Naming and Directory Interface*. In questo caso si può immaginare come un registro di nomi e valori che Tomcat mette a disposizione della singola applicazione.

L'elemento XML `<Environment>` registra una voce:

| Attributo | Significato nel nostro caso |
| --- | --- |
| `name="spring.config.location"` | Nome con cui l'applicazione può trovare il valore. |
| `value="file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties"` | Valore associato al nome: l'indirizzo del file esterno. |
| `type="java.lang.String"` | Il valore è una stringa di testo. |

Tomcat espone la voce nello spazio JNDI dell'applicazione con il nome `java:comp/env/spring.config.location`.

**Non è una variabile d'ambiente della shell** impostata con `export`, né un argomento Java `-D...`: è una voce disponibile attraverso Tomcat all'interno della webapp.

Riferimento: [Tomcat — Environment Entries](https://tomcat.apache.org/tomcat-10.1-doc/config/context.html#Environment_Entries).

Spring Boot può consultare le voci JNDI del contenitore. `spring.config.location` indica dove cercare la configurazione; `file:` specifica una risorsa del filesystem.

Il collegamento dichiarato è quindi:

1. `mse-air-api.xml` contiene la voce JNDI.
2. La voce indica `application-air.properties`.
3. Il file contiene le proprietà `spring.datasource.*`.

**Tomcat comunica dove si trova il file; Spring interpreta la configurazione del datasource.**

Riferimento: [Spring Boot — Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html). Il comportamento preciso dipende dalla versione e dall'avvio dell'applicazione; il riscontro qui documentato è il riferimento presente nell'XML.

---

## 4) Identificazione del PID corretto di Tomcat

### Comando usato

```bash
ps -ef | grep -i '[t]omcat'
```

### Cosa fa

- `ps -ef` elenca i processi con informazioni estese, inclusi utente, PID e comando.
- `|` passa l'output al comando successivo.
- `grep -i` seleziona le righe contenenti il testo cercato senza distinguere maiuscole e minuscole.
- `[t]omcat` cerca il testo `tomcat`, normalmente evitando di selezionare il comando `grep` stesso. Nell'output condiviso compariva comunque anche quella riga.

Il filtro è ampio: seleziona anche i processi dell'utente `tomcat` e quelli con `/home/tomcat/` nel percorso, non soltanto il server Tomcat.

### Risultati rilevanti dell'output osservato

| PID | Identificazione |
| --- | --- |
| `743` | Applicazione websocket avviata come JAR separato. |
| `585365`, `585425` | Processi Elasticsearch. |
| `1858907` | Tomcat dell'installazione esaminata. |

### Processo corretto individuato

PID: **1858907** nella sessione osservata.

Perché corretto:

- contiene `org.apache.catalina.startup.Bootstrap start`;
- contiene `-Dcatalina.base=/home/tomcat/apache-tomcat-10.1.9`;
- contiene `-Dcatalina.home=/home/tomcat/apache-tomcat-10.1.9`.

### Comando di verifica argomenti processo

```bash
tr '\0' '\n' < /proc/1858907/cmdline | grep -E 'catalina\.base|catalina\.home|spring\.config\.location'
```

### Cosa fa

- `/proc/1858907/cmdline` contiene gli argomenti del processo, separati da caratteri nulli.
- `<` passa il contenuto del file a `tr`.
- `tr '\0' '\n'` trasforma i separatori nulli in ritorni a capo, rendendo leggibili gli argomenti.
- `grep -E` seleziona i tre nomi indicati; `|` nella sua espressione significa «oppure» e `\.` indica un punto letterale.

Output:

```text
-Dcatalina.base=/home/tomcat/apache-tomcat-10.1.9
-Dcatalina.home=/home/tomcat/apache-tomcat-10.1.9
```

Il risultato conferma il `CATALINA_BASE` dell'installazione esaminata.

### Nota importante

La voce configurata con `<Environment>` non è un argomento della JVM: la sua assenza dalla cmdline è quindi attesa e non dimostra che il file esterno non venga caricato.

---

## 5) Verifica log più mirata

### Comando

```bash
tail -n 5000 /home/tomcat/latest/logs/catalina.out |
  grep -iE 'mse-air|application-air|spring\.config|Hikari|jdbc:mysql|investigazione|ispettori' |
  tail -n 80
```

### Cosa fa

- `tail -n 5000` legge le ultime 5000 righe del file: il limite è applicato prima della ricerca.
- `grep -iE` cerca le parole chiave senza distinguere maiuscole e minuscole; `|` nell'espressione separa le alternative.
- `tail -n 80` mostra le ultime 80 righe corrispondenti.

### Output ottenuto

```text
2026-10-20 09:06:45.321 ERROR 1858907 --- [io-443-exec-460] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-4 - Connection is not available, request timed out after 30000ms.
Caused by: java.sql.SQLTransientConnectionException: HikariPool-4 - Connection is not available, request timed out after 30000ms.
        at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:696) ~[HikariCP-4.0.3.jar:na]
        at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197) ~[HikariCP-4.0.3.jar:na]
        at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:162) ~[HikariCP-4.0.3.jar:na]
        at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:128) ~[HikariCP-4.0.3.jar:na]
```

### Interpretazione

Il messaggio riporta un timeout nel recupero di una connessione dal pool dopo 30 secondi. Il frammento non identifica il database e non permette di attribuire con certezza `HikariPool-4` a `mse-air-api`.

---

## 6) Stato della verifica (conclusione)

### Conclusione forte (documentata)

Per `mse-air-api` è configurato in Tomcat:

- `spring.config.location -> file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties`

Quindi la configurazione esterna è quella dichiarata dal context descriptor.

---

## Allegato: comandi usati (riepilogo rapido)

La numerazione è aggiornata dopo la rimozione dei precedenti comandi 7 e 11. I comandi relativi (`find .` e `cat ./...`) vanno eseguiti da `/home/tomcat/latest/webapps`.

```bash
# 1) Cerca i properties nelle webapp
find . -name application.properties

# 2) Legge il properties interno AIR
cat ./mse-air-api/WEB-INF/classes/application.properties

# 3) Elenco config contesti Tomcat
ls /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost

# 4) Legge il properties esterno AIR
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/application-air.properties

# 5) Legge il context XML AIR (punto decisivo)
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/mse-air-api.xml

# 6) Elenco processi (filtro ampio)
ps -ef | grep -i '[t]omcat'

# 7) Tentativo grep ampio (interrotto; mantenuto come storico, non consigliato)
# -R: ricerca ricorsiva; -i: ignora maiuscole; -E: espressione regolare estesa.
# 2>/dev/null nasconde gli errori. tail limita l'output finale, non la scansione.
grep -RiE 'spring.config.location|application-air.properties|datasource|Hikari|jdbc:mysql' /home/tomcat/latest/logs /home/tomcat/latest/webapps/mse-air-api/WEB-INF 2>/dev/null | tail -n 100

# 8) Ricerca più mirata in coda a catalina.out
tail -n 5000 /home/tomcat/latest/logs/catalina.out |
  grep -iE 'mse-air|application-air|spring\.config|Hikari|jdbc:mysql|investigazione|ispettori' |
  tail -n 80

# 9) Elenco log disponibili
# -l: dettagli; -h: dimensioni leggibili; -t: più recenti prima.
# head -n 25 mostra le prime 25 righe dell'elenco.
ls -lht /home/tomcat/latest/logs/ | head -n 25
```
