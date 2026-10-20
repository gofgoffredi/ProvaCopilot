# Guida: individuare la configurazione database usata da `mse-air-api` su Tomcat

## Obiettivo
Capire **quale file di configurazione** viene realmente usato dall'applicazione `mse-air-api` per la connessione al database, in presenza di più file `application.properties`.

---

## Contesto iniziale
Sul server erano presenti:

- file `application.properties` dentro le webapp (`WEB-INF/classes`)
- file esterni in `conf/Catalina/localhost/`
- descriptor di contesto Tomcat per ogni app (`mse-air-api.xml`)

L'ipotesi iniziale era: "Tomcat prende il DB dal file dentro la webapp".

La verifica ha mostrato che, per `mse-air-api`, è configurato un file esterno.

---

## 1) Verifica dei file `application.properties` nelle webapp

### Comando
```bash
find . -name application.properties
```

### Cosa fa
- `find .` cerca nella directory corrente e sotto-directory
- `-name application.properties` filtra solo i file con quel nome

### Output (estratto)
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
```bash
cat ./mse-air-api/WEB-INF/classes/application.properties
```

Output:
```properties
server.port=8443
#server.port=8080
server.servlet.context-path=/mse-air-api
spring.datasource.url=jdbc:mysql://10.72.116.37:3306/ispettori
spring.datasource.username=ispettori_user
spring.datasource.password=.....
```

**Risultato parziale:** nel WAR è presente una configurazione DB che punta a `ispettori`.

---

## 2) Verifica configurazioni esterne in Tomcat

### Comando
```bash
ls /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost
```

### Cosa fa
- elenca i file di configurazione di contesto per le app deployate in Tomcat

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

Output:
```properties
#CLIENT DB
server.port=8443
server.servlet.context-path=/mse-air-api
spring.datasource.url=jdbc:mysql://10.72.116.37:3306/investigazione?allowLoadLocalInfile=true&rewriteBatchedStatements=true&cachePrepStmts=true&useServerPrepStmts=true
spring.datasource.username=investigazione
spring.datasource.password=.....
```

**Risultato parziale:** esiste una seconda configurazione DB (esterna) che punta a `investigazione`.

---

## 3) Punto decisivo: il descriptor di contesto `mse-air-api.xml`

### Comando usato
```bash
cat /home/tomcat/apache-tomcat-10.1.9/conf/Catalina/localhost/mse-air-api.xml
```

### Cosa fa
- mostra la configurazione di contesto Tomcat associata all'app `mse-air-api`

### Output
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="spring.config.location" value="file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties" type="java.lang.String"/>
</Context>
```

### Interpretazione
La webapp riceve (via `Environment` JNDI di Tomcat) il valore:

- `spring.config.location = file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties`

Quindi, per `mse-air-api`, la configurazione esterna indicata viene usata come sorgente di configurazione Spring.

---

## 4) Identificazione del PID corretto di Tomcat

Con `ps -ef | grep -i '[t]omcat'` comparivano molti processi (Tomcat, websocket standalone, Elasticsearch, SSH, ecc.).

### Processo corretto individuato
PID: **1858907**

Perché corretto:
- contiene `org.apache.catalina.startup.Bootstrap start`
- contiene `-Dcatalina.base=/home/tomcat/apache-tomcat-10.1.9`
- contiene `-Dcatalina.home=/home/tomcat/apache-tomcat-10.1.9`

### Comando di verifica argomenti processo
```bash
tr '\0' '\n' < /proc/1858907/cmdline | grep -E 'catalina\.base|catalina\.home|spring\.config\.location'
```

Output:
```text
-Dcatalina.base=/home/tomcat/apache-tomcat-10.1.9
-Dcatalina.home=/home/tomcat/apache-tomcat-10.1.9
```

### Nota importante
`spring.config.location` **non compare necessariamente nella cmdline Java** quando è iniettato via `<Environment .../>` nel context XML Tomcat (JNDI/app context), come nel tuo caso.

---

## 5) Verifica log: comando iniziale troppo ampio

### Comando iniziale
```bash
grep -RiE 'spring.config.location|application-air.properties|datasource|Hikari|jdbc:mysql' /home/tomcat/latest/logs /home/tomcat/latest/webapps/mse-air-api/WEB-INF 2>/dev/null | tail -n 100
```

### Problema
- `grep -R` su directory grandi può essere lento
- `tail -n 100` limita solo l'output finale, **non** il lavoro che `grep` deve fare prima

Il comando è stato interrotto (`^C`).

---

## 6) Verifica log più mirata

### Comando
```bash
tail -n 5000 /home/tomcat/latest/logs/catalina.out |
  grep -iE 'mse-air|application-air|spring\.config|Hikari|jdbc:mysql|investigazione|ispettori' |
  tail -n 80
```

### Cosa fa
- `tail -n 5000 catalina.out`: prende solo le ultime 5000 righe
- pipe a `grep`: cerca parole chiave utili
- `tail -n 80`: mostra le ultime 80 righe tra i match

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
- Errore di **timeout nel recupero connessione dal pool** (30s)
- Il frammento da solo non prova quale DB (`investigazione` vs `ispettori`) stesse usando in quel momento

---

## 7) Stato della verifica (conclusione)

### Conclusione forte (documentata)
Per `mse-air-api` è configurato in Tomcat:

- `spring.config.location -> file:/home/tomcat/latest/conf/Catalina/localhost/application-air.properties`

Quindi la configurazione esterna è quella dichiarata dal context descriptor.

### Cosa resta da verificare al 100% runtime
Per dimostrare in modo definitivo il JDBC URL effettivo in uso durante le richieste, conviene leggere il log applicativo dedicato:

```bash
tail -n 150 /home/tomcat/latest/logs/mse-air-api.log
```

(Comando non ancora eseguito in questa sessione.)

---

## Allegato: comandi usati (riepilogo rapido)

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

# 6) Elenco processi (rumoroso)
ps -ef | grep -i '[t]omcat'

# 7) Verifica argomenti processo Tomcat corretto
tr '\0' '\n' < /proc/1858907/cmdline | grep -E 'catalina\.base|catalina\.home|spring\.config\.location'

# 8) Tentativo grep ampio (interrotto)
grep -RiE 'spring.config.location|application-air.properties|datasource|Hikari|jdbc:mysql' /home/tomcat/latest/logs /home/tomcat/latest/webapps/mse-air-api/WEB-INF 2>/dev/null | tail -n 100

# 9) Ricerca più mirata in coda a catalina.out
tail -n 5000 /home/tomcat/latest/logs/catalina.out |
  grep -iE 'mse-air|application-air|spring\.config|Hikari|jdbc:mysql|investigazione|ispettori' |
  tail -n 80

# 10) Elenco log disponibili
ls -lht /home/tomcat/latest/logs/ | head -n 25

# 11) Prossimo step consigliato
# tail -n 150 /home/tomcat/latest/logs/mse-air-api.log
```
