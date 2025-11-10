# Arhitecturi Software Distribuite: Monolit Distribuit vs. Microservicii

## Cuprins

1. [Introducere](#1-introducere)
2. [Studiu Comparativ](#2-studiul-comparativ)
3. [Conceptul și Detalierea](#3-conceptul-și-detalierea)
4. [Exemple și Implementare](#4-exemple-și-implementare)
5. [Concluzii](#5-concluzii)

---

## 1. Introducere

### 1.1 Contextul Problemei

În era digitală actuală, aplicațiile software moderne se confruntă cu provocări semnificative legate de scalabilitate, mentenabilitate și dezvoltare distribuită. Pe măsură ce organizațiile cresc și echipele de dezvoltare devin mai mari și mai distribuite geografic, arhitectura tradițională monolitică începe să prezinte limitări critice.

Problemele principale cu care se confruntă organizațiile moderne includ:

**1. Echipe Mari și Distribuite Geografic**
- Echipe de dezvoltare care lucrează în fusuri orare diferite
- Necesitatea de a permite dezvoltare paralelă fără conflicte
- Dificultăți în coordonarea deploy-urilor și a versiunilor

**2. Cod Distribuit și Complexitate Crescută**
- Baze de cod care cresc exponențial
- Dependențe complexe între module
- Dificultăți în testare și izolarea bug-urilor

**3. Scalabilitate și Performanță**
- Necesitatea de a scala doar anumite părți ale aplicației
- Dificultăți în optimizarea resurselor
- Puncte unice de eșec (single points of failure)

**4. Cicluri de Dezvoltare Lungi**
- Deploy-uri care necesită sincronizare între toate modulele
- Risc ridicat la fiecare release
- Timp de recuperare lung în caz de erori

**5. Tehnologii și Stack-uri Diverse**
- Necesitatea de a adopta tehnologii noi pentru anumite funcționalități
- Dificultăți în integrarea de tehnologii diferite într-un monolit

### 1.2 Soluții Arhitecturale

Pentru a rezolva aceste probleme, au apărut două abordări principale:

1. **Monolit Distribuit (Distributed Monolith)** - o arhitectură care păstrează simplitatea unui monolit dar permite distribuția pe mai multe instanțe
2. **Microservicii (Microservices)** - o arhitectură care descompune aplicația în servicii independente, fiecare cu responsabilități clare

Acest raport analizează ambele abordări prin implementarea unei aplicații de web scraping, comparând avantajele și dezavantajele fiecărei arhitecturi.

---

## 2. Studiu Comparativ

### 2.1 Monolit Distribuit

#### 2.1.1 Definiție și Caracteristici

Un **monolit distribuit** este o aplicație care păstrează structura internă a unui monolit tradițional (toate modulele sunt parte din aceeași aplicație), dar poate fi rulată pe mai multe instanțe pentru a obține scalabilitate orizontală.

**Caracteristici principale:**
- Toate modulele sunt parte din aceeași aplicație
- Baza de cod este unitară
- Deploy-ul se face ca o singură unitate
- Scalabilitate prin replicarea întregii aplicații
- Comunicare internă prin apeluri de metode (sincrone)

#### 2.1.2 Avantaje

✅ **Simplitate de Dezvoltare**
- Un singur repository de cod
- Debugging mai simplu (stack trace complet)
- Testare mai ușoară (toate componentele în același proces)

✅ **Consistență Tranzacțională**
- Tranzacții ACID pe întreaga aplicație
- Fără probleme de consistență distribuită
- Rollback simplu în caz de erori

✅ **Performanță**
- Comunicare directă între module (fără overhead de rețea)
- Latency minimă pentru operațiuni interne
- Optimizări globale posibile

✅ **Deploy Simplu**
- Un singur artefact de deploy
- Versionare simplă
- Fără probleme de compatibilitate între servicii

✅ **Costuri Reduse**
- Fără infrastructură complexă de mesagerie
- Fără gateway-uri API
- Monitoring mai simplu

#### 2.1.3 Dezavantaje

❌ **Scalabilitate Limită**
- Trebuie să scalezi întreaga aplicație, chiar dacă doar o parte necesită mai multe resurse
- Resurse irosite pentru componente care nu necesită scalare

❌ **Cuplare Strânsă**
- Modificările într-un modul pot afecta altele
- Dificultăți în refactorizare
- Dependențe circulare posibile

❌ **Tehnologii Limitante**
- Toate modulele trebuie să folosească același stack tehnologic
- Dificultăți în adoptarea de tehnologii noi

❌ **Dezvoltare Paralelă Dificilă**
- Conflicturi în cod când mai multe echipe lucrează simultan
- Merge conflicts frecvente
- Necesitate de sincronizare între echipe

❌ **Punct Unic de Eșec**
- Dacă o parte a aplicației eșuează, întreaga aplicație poate fi afectată
- Deploy-uri riscante (toate componentele se actualizează simultan)

### 2.2 Microservicii

#### 2.2.1 Definiție și Caracteristici

**Microserviciile** sunt o arhitectură care descompune o aplicație în servicii independente, fiecare având responsabilități clare și comunicând prin interfețe bine definite (de obicei REST APIs sau message queues).

**Caracteristici principale:**
- Servicii independente și autonome
- Fiecare serviciu are propriul repository de cod
- Deploy independent pentru fiecare serviciu
- Scalabilitate granulară (fiecare serviciu poate fi scalat independent)
- Comunicare prin rețea (HTTP, message queues)

#### 2.2.2 Avantaje

✅ **Scalabilitate Granulară**
- Poți scala doar serviciile care necesită mai multe resurse
- Optimizare eficientă a costurilor
- Răspuns rapid la variații de trafic

✅ **Dezvoltare Independentă**
- Echipe diferite pot lucra pe servicii diferite
- Deploy-uri independente
- Cicluri de release mai rapide

✅ **Tehnologii Diverse**
- Fiecare serviciu poate folosi tehnologia cea mai potrivită
- Adoptare mai ușoară a tehnologiilor noi
- Stack-uri optimizate pentru fiecare serviciu

✅ **Izolare a Eșecurilor**
- Eșecul unui serviciu nu afectează neapărat celelalte
- Resiliență mai bună
- Degradare grațioasă posibilă

✅ **Reutilizare**
- Serviciile pot fi reutilizate în alte aplicații
- API-uri clare și documentate
- Compoziție flexibilă

#### 2.2.3 Dezavantaje

❌ **Complexitate Crescută**
- Infrastructură complexă (service discovery, API gateway, load balancing)
- Networking și latence între servicii
- Debugging distribuit dificil

❌ **Consistență Distribuită**
- Fără tranzacții ACID globale
- Necesitate de pattern-uri complexe (Saga, Event Sourcing)
- Eventual consistency

❌ **Overhead de Operare**
- Monitoring și logging distribuit
- Deployment orchestration complex
- Necesitate de DevOps expertise

❌ **Testare Complexă**
- Testare de integrare între servicii
- Necesitate de environment-uri de test complexe
- Mocking și stubbing pentru servicii dependente

❌ **Costuri Infrastructură**
- Mai multe containere/servicii de gestionat
- Message brokers și gateway-uri
- Resurse de rețea suplimentare

### 2.3 Tabel Comparativ

| Aspect | Monolit Distribuit | Microservicii |
|--------|-------------------|---------------|
| **Complexitate Dezvoltare** | Scăzută | Ridicată |
| **Complexitate Operare** | Scăzută | Ridicată |
| **Scalabilitate** | Limită (toată aplicația) | Granulară (per serviciu) |
| **Dezvoltare Paralelă** | Dificilă | Ușoară |
| **Deploy** | Un singur artefact | Independent per serviciu |
| **Performanță** | Optimă (comunicare directă) | Overhead de rețea |
| **Consistență Date** | ACID complet | Eventual consistency |
| **Izolare Eșecuri** | Scăzută | Ridicată |
| **Costuri Infrastructură** | Scăzute | Ridicate |
| **Tehnologii** | Un singur stack | Stack-uri diverse |
| **Debugging** | Simplu | Complex |
| **Testare** | Simplă | Complexă |

### 2.4 Când să Alegi Fiecare Arhitectură?

**Alege Monolit Distribuit când:**
- Echipă mică sau medie
- Aplicație cu complexitate moderată
- Necesități de scalabilitate predictibile
- Baza de cod nu este foarte mare
- Vrei să minimizezi complexitatea operațională

**Alege Microservicii când:**
- Echipe mari și distribuite
- Necesități de scalabilitate variabile pe componente
- Diferite părți ale aplicației au cerințe tehnologice diferite
- Necesități de disponibilitate ridicată
- Echipă cu experiență în DevOps și arhitecturi distribuite

---

## 3. Conceptul și Detalierea

### 3.1 Arhitectura Monolit Distribuit

#### 3.1.1 Structura Aplicației

Aplicația de web scraping implementată ca monolit distribuit conține toate funcționalitățile într-o singură aplicație Spring Boot:

```
Distributed Monolith Application
├── Controller Layer (ScraperController)
│   └── REST API endpoints
├── Service Layer
│   ├── WebScraperService (scraping logic)
│   └── ProductService (business logic + database)
├── Repository Layer
│   └── ProductDetailsRepository (data access)
└── Entity Layer
    └── ProductDetails (database model)
```

**Fluxul de Date:**
1. Client trimite request la `/api/scraper/start`
2. `ScraperController` primește request-ul
3. `WebScraperService` extrage produsele de pe site-uri
4. `ProductService` salvează produsele în baza de date
5. Răspuns sincron către client

#### 3.1.2 Comunicare Internă

În monolitul distribuit, comunicarea între module se face prin:
- **Apeluri de metode directe** (sincrone)
- **CompletableFuture** pentru procesare asincronă în cadrul aceluiași proces
- **Shared memory** pentru date

**Exemplu din cod:**
```java
// Comunicare directă între servicii
Map<String, ProductInfo> products = scraperService.scrapeProductsFromListing(url, maxPages);
productService.saveProduct(url, info); // Apel direct de metodă
```

#### 3.1.3 Scalabilitate

Scalabilitatea se realizează prin:
- **Replicarea întregii aplicații** pe mai multe instanțe
- **Load balancer** în față pentru distribuirea traficului
- **Baza de date partajată** între toate instanțele

**Limitări:**
- Nu poți scala doar partea de scraping sau doar partea de salvare
- Toate instanțele trebuie să aibă resurse pentru toate funcționalitățile

### 3.2 Arhitectura Microservicii

#### 3.2.1 Structura Aplicației

Aplicația este descompusă în două servicii independente:

```
Microservices Architecture
├── Producer Service
│   ├── ProducerController (REST API)
│   ├── WebScraperService (scraping logic)
│   └── MessageProducerService (RabbitMQ publisher)
│
├── Consumer Service
│   ├── ConsumerController (REST API)
│   ├── UrlMessageListener (RabbitMQ consumer)
│   ├── ProductExtractorService (extraction logic)
│   └── ProductService (database operations)
│
└── Infrastructure
    ├── RabbitMQ (message broker)
    └── PostgreSQL (shared database)
```

**Fluxul de Date:**
1. Client trimite request la Producer Service `/api/producer/start`
2. Producer Service extrage URL-urile produselor
3. Producer Service publică URL-urile în RabbitMQ queue
4. Consumer Service consumă URL-urile din queue
5. Consumer Service extrage informațiile produselor
6. Consumer Service salvează în baza de date
7. Răspuns asincron (Producer răspunde imediat, procesarea continuă în background)

#### 3.2.2 Comunicare între Servicii

Comunicarea se face prin:
- **Message Queue (RabbitMQ)** pentru decuplare asincronă
- **REST APIs** pentru interacțiuni sincrone (dacă necesare)
- **Shared Database** pentru date (în acest caz, Consumer Service scrie în DB)

**Exemplu din cod:**
```java
// Producer Service
List<String> urls = scraperService.scrapeProductUrls(startingUrl, maxPages);
messageProducerService.sendUrls(urls); // Publică în RabbitMQ

// Consumer Service
@RabbitListener(queues = "${queue.name}")
public void handleMessage(Message message) {
    String url = new String(message.getBody());
    ProductInfo productInfo = extractorService.extractProductInfo(url);
    productService.saveProduct(url, productInfo); // Salvează în DB
}
```

#### 3.2.3 Scalabilitate

Scalabilitatea se realizează prin:
- **Scalare independentă** a Producer Service (dacă trebuie să scrapeze mai mult)
- **Scalare independentă** a Consumer Service (dacă trebuie să proceseze mai mult)
- **RabbitMQ** gestionează coada de mesaje și distribuie workload-ul
- **Multiple Consumer instances** pot procesa mesaje în paralel

**Avantaje:**
- Poți scala doar serviciul care are nevoie de mai multe resurse
- RabbitMQ distribuie automat mesajele între consumatori
- Resiliență: dacă un consumer eșuează, alții continuă procesarea

### 3.3 Pattern-uri Utilizate

#### 3.3.1 Monolit Distribuit

- **Layered Architecture**: Separare clară între controller, service, repository
- **Async Processing**: CompletableFuture pentru operațiuni asincrone în același proces
- **Repository Pattern**: Abstrahie pentru accesul la date

#### 3.3.2 Microservicii

- **Producer-Consumer Pattern**: Producer publică mesaje, Consumer le procesează
- **Message Queue Pattern**: Decuplare asincronă între servicii
- **API Gateway Pattern**: (implicit prin separarea serviciilor)
- **Database per Service**: (parțial - Consumer are acces exclusiv la DB pentru scriere)

---

## 4. Exemple și Implementare

### 4.1 Sisteme Reale care Folosesc Microservicii

#### 4.1.1 Netflix

Netflix este unul dintre cei mai cunoscuți adoptatori ai arhitecturii de microservicii. Sistemul lor include:

- **Hundreds of microservices** pentru diferite funcționalități:
  - User service (gestionare utilizatori)
  - Recommendation service (recomandări personalizate)
  - Video streaming service (transmitere video)
  - Billing service (facturare)
  - Content delivery service (distribuire conținut)

**De ce microservicii?**
- Trafic masiv (milioane de utilizatori simultan)
- Necesități de scalabilitate variabile (streaming vs. recomandări)
- Echipe distribuite global
- Disponibilitate 99.99%

#### 4.1.2 Amazon

Amazon folosește microservicii pentru:
- **Product catalog service**
- **Shopping cart service**
- **Payment service**
- **Order fulfillment service**
- **Recommendation service**

**De ce microservicii?**
- Complexitate enormă (milioane de produse)
- Necesități de scalabilitate extreme (Black Friday)
- Echipe independente pentru fiecare domeniu
- Tehnologii diverse (unele servicii în Java, altele în Go, Python)

#### 4.1.3 Uber

Uber a migrat de la monolit la microservicii pentru:
- **Ride matching service**
- **Driver service**
- **Payment service**
- **Maps service**
- **Notification service**

**De ce microservicii?**
- Creștere rapidă (de la startup la platformă globală)
- Necesități regionale diferite (regulamente diferite)
- Scalabilitate pentru evenimente (concerts, sports events)

### 4.2 Sisteme care Folosesc Monolit Distribuit

#### 4.2.1 GitHub (inițial)

GitHub a început ca monolit distribuit:
- Toate funcționalitățile într-o singură aplicație Ruby on Rails
- Scalabilitate prin replicarea aplicației
- Migrare graduală către microservicii pentru anumite componente

#### 4.2.2 Shopify

Shopify folosește o abordare de monolit distribuit:
- Aplicație principală Ruby on Rails
- Scalabilitate prin replicare
- Microservicii doar pentru componente specifice (payments, shipping)

### 4.3 Implementarea Noastră: Web Scraper

#### 4.3.1 Descompunerea în Microservicii

Pentru aplicația de web scraping, am identificat două domenii naturale:

**1. Producer Service (Scraping)**
- **Responsabilitate**: Extragerea URL-urilor produselor de pe site-uri
- **Input**: URL de start, număr de pagini
- **Output**: Listă de URL-uri publicate în RabbitMQ
- **Tehnologii**: Spring Boot, JSoup, RabbitMQ
- **Port**: 8081

**2. Consumer Service (Processing & Storage)**
- **Responsabilitate**: Procesarea URL-urilor, extragerea detaliilor, salvare în DB
- **Input**: URL-uri din RabbitMQ queue
- **Output**: Produse salvate în PostgreSQL
- **Tehnologii**: Spring Boot, JSoup, RabbitMQ, PostgreSQL, JPA
- **Port**: 8082

**De ce această descompunere?**
- **Separation of Concerns**: Scraping și procesare sunt operațiuni diferite
- **Scalabilitate Independentă**: 
  - Dacă trebuie să scrapezi mai mult → scalezi Producer
  - Dacă trebuie să procesezi mai mult → scalezi Consumer
- **Resiliență**: Dacă scraping-ul eșuează, procesarea continuă cu URL-urile existente
- **Dezvoltare Paralelă**: Echipe diferite pot lucra pe fiecare serviciu

#### 4.3.2 Fluxul Complet

**Monolit Distribuit:**
```
Client → POST /api/scraper/start
  ↓
ScraperController
  ↓
WebScraperService.scrapeProductsFromListing()
  ↓ (sincron, în același proces)
ProductService.saveProduct() × N (async cu CompletableFuture)
  ↓
PostgreSQL
  ↓
Response: "Scraping completed, products being saved"
```

**Microservicii:**
```
Client → POST /api/producer/start
  ↓
ProducerController
  ↓
WebScraperService.scrapeProductUrls()
  ↓
MessageProducerService.sendUrls() → RabbitMQ Queue
  ↓
Response: "URLs published to queue" (immediate)

[Asincron, în paralel]
RabbitMQ Queue
  ↓
UrlMessageListener.handleMessage() (Consumer Service)
  ↓
ProductExtractorService.extractProductInfo()
  ↓
ProductService.saveProduct()
  ↓
PostgreSQL
```

#### 4.3.3 Docker Compose Configurație

**Monolit Distribuit:**
```yaml
services:
  db:
    image: postgres:15-alpine
    ports: ["5433:5432"]
  
  scraper-app:
    image: maven:3.9.6-eclipse-temurin-21
    ports: ["8080:8080"]
    depends_on: [db]
```

**Microservicii:**
```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports: ["5672:5672", "15672:15672"]
  
  db:
    image: postgres:15-alpine
    ports: ["5434:5432"]
  
  producer-service:
    image: maven:3.9.6-eclipse-temurin-21
    ports: ["8081:8081"]
    depends_on: [rabbitmq]
  
  consumer-service:
    image: maven:3.9.6-eclipse-temurin-21
    ports: ["8082:8082"]
    depends_on: [rabbitmq, db]
```

#### 4.3.4 Metrici de Performanță

Pentru a compara cele două arhitecturi, am implementat teste de performanță care măsoară:

**Metrici pentru Monolit Distribuit:**
- Timp de răspuns pentru scraping (sincron)
- Throughput (produse/second)
- Timp pentru query-uri la baza de date
- Performanță la request-uri concurente

**Metrici pentru Microservicii:**
- Timp de răspuns Producer Service
- Timp de răspuns Consumer Service
- Latency mesaj (publish → consume)
- Throughput end-to-end (Producer → Queue → Consumer → DB)
- Scalabilitate (multiple consumer instances)

**Rezultate Așteptate:**

| Metrică | Monolit Distribuit | Microservicii |
|---------|-------------------|---------------|
| **Latency Scraping** | Mai mică (comunicare directă) | Mai mare (overhead rețea) |
| **Throughput Scraping** | Similar | Similar |
| **Latency End-to-End** | Mai mică (sincron) | Mai mare (asincron + queue) |
| **Scalabilitate** | Limită (toată aplicația) | Granulară (per serviciu) |
| **Resiliență** | Scăzută (single point of failure) | Ridicată (izolare eșecuri) |

### 4.4 Teste de Performanță

Am creat scripturi de testare pentru ambele arhitecturi:

**Teste Monolit Distribuit:**
- Health check (warm-up)
- Extragere produs singular
- Scraping performanță (2 pagini, 5 pagini)
- Request-uri concurente (10 paralel)
- Performanță query-uri database

**Teste Microservicii:**
- Health checks (Producer + Consumer)
- Publishing URL singular
- Scraping și publishing (2 pagini, 5 pagini)
- Latency mesaj (publish → consume)
- Query-uri database
- Request-uri concurente
- Throughput end-to-end

**Cum se Rulează Testele:**

```bash
# Pentru Monolit Distribuit
cd performance-tests
chmod +x test-monolith.sh
./test-monolith.sh

# Pentru Microservicii
chmod +x test-microservices.sh
./test-microservices.sh
```

---

## 5. Concluzii

### 5.1 Rezumat Comparativ

După analiza teoretică și implementarea practică a ambelor arhitecturi, putem trage următoarele concluzii:

#### 5.1.1 Monolit Distribuit - Când este Potrivit

Monolitul distribuit este o alegere excelentă când:
- **Echipă mică sau medie** (< 10 dezvoltatori)
- **Aplicație cu complexitate moderată** (nu sute de funcționalități)
- **Necesități de scalabilitate predictibile** (trafic relativ constant)
- **Vrei să minimizezi complexitatea** operațională și de dezvoltare
- **Baza de cod nu este foarte mare** (< 100k linii de cod)
- **Cicluri de release mai rare** (lunar sau trimestrial)

**Avantaje principale:**
- Dezvoltare rapidă și simplă
- Debugging ușor
- Performanță optimă (fără overhead de rețea)
- Costuri reduse de infrastructură

#### 5.1.2 Microservicii - Când este Potrivit

Microserviciile sunt potrivite când:
- **Echipe mari și distribuite** (> 10 dezvoltatori, multiple echipe)
- **Necesități de scalabilitate variabile** (unele componente necesită mai multe resurse)
- **Diferite cerințe tehnologice** pentru diferite componente
- **Necesități de disponibilitate ridicată** (99.9%+)
- **Cicluri de release frecvente** (săptămânal sau zilnic)
- **Organizație matură** cu expertise în DevOps

**Avantaje principale:**
- Scalabilitate granulară
- Dezvoltare independentă
- Izolare a eșecurilor
- Flexibilitate tehnologică

### 5.2 Lecții Învățate

#### 5.2.1 Nu Există O Soluție Universală

Arhitectura potrivită depinde de:
- Mărimea echipei
- Complexitatea aplicației
- Necesitățile de scalabilitate
- Maturitatea organizației
- Resursele disponibile

#### 5.2.2 Evoluție Graduală

Majoritatea organizațiilor încep cu un monolit și evoluează către microservicii când:
- Echipă crește
- Complexitatea crește
- Necesitățile de scalabilitate devin critice
- Devin mai maturi în DevOps

**Strategie recomandată:**
1. Începe cu monolit (sau monolit distribuit)
2. Identifică limitele
3. Extrage gradual componente în microservicii
4. Nu extrage prea devreme (premature optimization)

#### 5.2.3 Complexitatea are un Cost

Microserviciile oferă flexibilitate și scalabilitate, dar:
- Complexitatea operațională crește semnificativ
- Necesită expertise în DevOps
- Costuri de infrastructură mai mari
- Timp mai mare pentru debugging și testare

**Regula de aur:** Nu alege microservicii doar pentru că sunt "cool" - alege-le când ai nevoie reală de beneficiile lor.

### 5.3 Recomandări pentru Proiectul Viitor

Pentru aplicația de web scraping analizată:

**Dacă ești la început:**
- **Monolit Distribuit** este alegerea corectă
- Dezvoltare rapidă
- Testare simplă
- Deploy ușor

**Când să migrezi la Microservicii:**
- Când ai nevoie să scalezi scraping-ul independent de procesare
- Când ai echipe diferite pentru scraping vs. procesare
- Când vrei să adaugi mai multe tipuri de consumatori (ex: notificări, analiză)
- Când aplicația crește și devine dificil de gestionat ca monolit

**Strategie de Migrare:**
1. Păstrează monolitul funcțional
2. Extrage Producer Service (scraping) ca primul microserviciu
3. Extrage Consumer Service (procesare) ca al doilea microserviciu
4. Adaugă noi consumatori pentru funcționalități noi (ex: analytics, notifications)

### 5.4 Concluzie Finală

Ambele arhitecturi au locul lor în dezvoltarea software modernă. **Monolitul distribuit** oferă simplitate și viteză de dezvoltare, în timp ce **microserviciile** oferă scalabilitate și flexibilitate la costul complexității.

Alegerea corectă depinde de contextul specific al proiectului tău. Cel mai important este să:
- **Înțelegi** avantajele și dezavantajele fiecărei arhitecturi
- **Evaluezi** nevoile tale specifice
- **Evoluezi** gradual, nu face schimbări premature
- **Măsori** performanța și ajustezi în consecință

În lumea software-ului, nu există "one size fits all" - există doar soluții potrivite pentru probleme specifice.

---

## Referințe

1. Martin Fowler - "Microservices" (martinfowler.com)
2. Sam Newman - "Building Microservices" (O'Reilly, 2015)
3. Chris Richardson - "Microservices Patterns" (Manning, 2018)
4. Netflix Tech Blog - "Microservices at Netflix"
5. Amazon Architecture - "Microservices on AWS"
6. Spring Boot Documentation - spring.io/projects/spring-boot
7. RabbitMQ Documentation - rabbitmq.com/documentation.html

---

**Autor:** [Nume Student]  
**Data:** [Data]  
**Universitate:** [Nume Universitate]  
**Facultate:** [Nume Facultate]

