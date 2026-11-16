# Law Firm Case Manager

A Java application for a small law firm with **two interfaces to the same program: a console menu and a web page (GUI)**. Office staff can register clients and lawyers, open cases, log billable hours, choose how each case is billed, close cases with an outcome and list cases in different orders. All data is saved to a JSON file and loaded again on the next start (the console saves when you exit, the web page after every change).

**Problem it solves:** small firms often keep this information in spreadsheets, which makes it easy to give a lawyer too many cases, miss a conflict of interest, or calculate a fee wrongly. This application keeps everything in one place and checks the firm's rules automatically:

- a lawyer can lead at most **5 active cases** at the same time;
- a lawyer cannot take a case **against one of their own clients** (conflict of interest);
- hours cannot be logged on a closed case, and a case cannot be closed twice.

User stories with acceptance criteria are in [`docs/user-stories.md`](docs/user-stories.md).

## Prerequisites

- **JDK 25** (the project compiles with `release 25`). Check with `java -version`.
- Maven is **not** required: the project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), which downloads the correct Maven version the first time it runs. An internet connection is needed for that first run.

## Build, test and run

Run these commands from the project root folder.

| What | Windows | macOS / Linux |
|---|---|---|
| Run all tests | `mvnw.cmd clean test` | `sh mvnw clean test` |
| Build the jar (also runs the tests) | `mvnw.cmd clean package` | `sh mvnw clean package` |
| Start the console menu | `java -jar target\law-firm-case-manager-1.0.0.jar` | `java -jar target/law-firm-case-manager-1.0.0.jar` |
| Start the web page (GUI) | `java -jar target\law-firm-case-manager-1.0.0.jar --web` | `java -jar target/law-firm-case-manager-1.0.0.jar --web` |

In IntelliJ IDEA you can also open the project as a Maven project, run `Main` directly, and run the tests by right-clicking `src/test/java` → *Run 'All Tests'*.

### Two interfaces

| | Console menu | Web page (GUI) |
|---|---|---|
| Start | no argument | `--web`, then open http://localhost:8080 |
| What it is | a numbered text menu (`ConsoleMenu`) | an HTML page with Bootstrap, forms and tables, served by `WebServer` |
| Features | every action: add clients and lawyers, open cases, log hours, choose billing, close cases, list cases sorted | the same actions, plus a sortable case table and the lists of clients and lawyers |
| Saving | when you exit with option 0 | after every change; stop with Ctrl+C |

Both interfaces call the same `LawFirmService` and use the same save file, so the rules are written once and the data is the same in both. The web page loads Bootstrap from the internet, so it needs a connection to look styled.

Saved data is written as a readable JSON file, `data/lawfirm-data.json` (relative to the folder the program is started from). This folder is ignored by Git. Starting the program with `--ser` uses the older Java-serialization file `data/lawfirm-data.ser` instead (it can be combined with `--web`).

## Sample usage (console menu)

User input is shown after each prompt.

```
=== Law Firm Case Manager ===
Loaded 0 clients, 0 lawyers and 0 cases.

1. Add client
2. Add lawyer
3. List clients and lawyers
4. Open a case
5. Log hours on a case
6. Choose billing method for a case
7. Close a case
8. List cases (sorted)
0. Save and exit
Choose an option: 1
Name: Anna Kovacs
Email: anna@example.com
Phone: +36 30 123 4567
Added Client #1: Anna Kovacs (anna@example.com, +36 30 123 4567)

Choose an option: 2
Name: Peter Nagy
Hourly rate (EUR): 100
Added Lawyer #1: Peter Nagy (100.00 EUR/hour)

Choose an option: 4
Case type: 1 = Criminal, 2 = Family, 3 = Corporate
Type: 1
Title: Shoplifting charge
Client id: 1
Opposing party: State
Deadline (YYYY-MM-DD): 2026-12-15
Lead lawyer id: 1
Charge: theft
Opened Criminal case #1: Shoplifting charge | client: Anna Kovacs vs State | lead: Peter Nagy | deadline: 2026-12-15 | OPEN | Hourly | fee: 0.00 EUR | charge: theft

Choose an option: 5
Case id: 1
Date (YYYY-MM-DD): 2026-11-04
Hours: 3
Description: First meeting
Logged. Total hours on this case: 3.0

Choose an option: 8
Sort by: 1 = Deadline, 2 = Fee (highest first), 3 = Client name
Sort order: 2
  Criminal case #1: Shoplifting charge | client: Anna Kovacs vs State | lead: Peter Nagy | deadline: 2026-12-15 | OPEN | Hourly | fee: 450.00 EUR | charge: theft

Choose an option: 0
Data saved.
Goodbye!
```

(The menu is printed again before every "Choose an option"; it is left out above to keep the example short.) The fee is 3 hours × 100 EUR × 1.5, because criminal cases have a 1.5 rate multiplier.

Examples of rules being enforced:

```
Refused: Peter Nagy already has 5 active cases.
Refused: Peter Nagy already represents Anna Kovacs and cannot act against them.
Error: Cannot log hours on a closed case.
Error: Email must contain '@'.
```

## Project structure

```
src/main/java/com/mahadin/lawfirm/
├── Main.java          creates the objects and starts the console menu (or the web page with --web, or the old file format with --ser)
├── domain/            Person, Client, Lawyer, LegalCase and its subclasses, TimeEntry, CaseStatus
├── billing/           BillingStrategy and its three implementations (Strategy pattern)
├── exception/         the application's own exception types
├── service/           LawFirmService: the firm's rules and the lists of data
├── persistence/       FirmData, the FirmRepository interface, JsonFirmRepository and DataStore (the two storage formats), RepositoryFactory
└── ui/                ConsoleMenu: the console interface; WebServer: the web interface (GUI)
src/main/resources/web/index.html   the Bootstrap page served by WebServer
src/test/java/...      JUnit 5 tests, in the same packages as the classes they test
docs/                  user stories and UML class diagram
```

## Implemented requirements

| Requirement | Where |
|---|---|
| **Encapsulation** | All fields are `private`; values are checked in constructors and setters (e.g. `Client.setEmail`, `Lawyer.setHourlyRate`). Lists are returned with `Collections.unmodifiableList`, so other classes cannot change them directly. |
| **Abstraction** | `Person` and `LegalCase` are abstract classes; `BillingStrategy` is an interface. |
| **Inheritance** | `Client` and `Lawyer` extend `Person`. `CriminalCase`, `FamilyCase` and `CorporateCase` extend `LegalCase`. The exceptions extend `LawFirmException` or `RuntimeException`. |
| **Runtime polymorphism** | `LegalCase.getRateMultiplier()` and `getCaseType()` are overridden in each case type. `calculateFee()` calls `BillingStrategy.calculateFee()`, and which implementation runs depends on the object chosen at runtime. |
| **Relationships and multiplicities** | A case has exactly 1 client and exactly 1 lead lawyer; a client or lawyer can have 0..* cases; a case has 0..* time entries (composition) and exactly 1 billing strategy. |
| **Collections and generics** | `List<Client>`, `List<Lawyer>`, `List<LegalCase>`, `List<TimeEntry>`. |
| **Sorting** | `LawFirmService.getCasesSorted(Comparator<LegalCase>)` with lambda comparators in `ConsoleMenu` (by deadline, by fee highest first, by client name). |
| **Exception handling** | Own exception types in the `exception` package (see below). Invalid numbers and dates in the menu are caught and the user is asked again. Load and save problems are reported without crashing. |
| **Persistence** | `FirmRepository` is the storage interface. `JsonFirmRepository` saves and loads a readable JSON file (Gson); `DataStore` does the same with Java serialization, using try-with-resources so the file streams are always closed. |
| **Design patterns** | Strategy for billing, Repository for storage, Factory for creating objects (see below). |
| **Build tool** | Maven with the Maven Wrapper. |
| **Tests** | JUnit 5 tests for fees, case rules, validation, sorting and saving/loading. |

### Exception types

| Exception | Kind | Used when |
|---|---|---|
| `ValidationException` | unchecked | invalid input, e.g. an empty name, an email without `@`, negative hours, unknown id |
| `CaseClosedException` | unchecked | logging hours on a closed case, or closing a case twice |
| `LawFirmException` | checked | parent class of the two firm-rule exceptions below |
| `CaseLoadExceededException` | checked | the lawyer already leads 5 active cases |
| `ConflictOfInterestException` | checked | the opposing party is one of the lawyer's own clients |

The two firm-rule exceptions are **checked** because breaking a firm rule is a normal situation that the caller must handle (the menu shows "Refused: …"). Invalid input is **unchecked** because it is a mistake in the values passed in, and the menu shows it as "Error: …".

## Design pattern: Strategy

**Problem:** the firm bills cases in three different ways: by the hour, with a fixed fee, or as a percentage of the settlement if the case is won. The billing method can also change while a case is open. Putting all three calculations in `LegalCase` with `if`/`switch` statements would make the class longer every time a new billing method is added.

**Solution:** each billing method is its own class that implements the `BillingStrategy` interface:

| Class | Role |
|---|---|
| `BillingStrategy` | the strategy interface: `calculateFee(LegalCase)` and `getName()` |
| `HourlyBilling` | total hours × lawyer's hourly rate × case-type multiplier (default for new cases) |
| `FlatFeeBilling` | a fixed amount, no matter how many hours |
| `ContingencyBilling` | a percentage of the settlement, but only if the case status is `WON` |
| `LegalCase` | the context: it holds one `BillingStrategy` and its `calculateFee()` just calls the strategy |

**Benefit:** `LegalCase` does not need to know how any fee is calculated. The billing method can be changed at runtime with `setBillingStrategy(...)` (menu option 6). A new billing method can be added by writing one new class, without changing `LegalCase` or the other strategies.

## Design pattern: Repository

**Problem:** the service and the user interfaces should not depend on how the data is stored. If they called file code directly, changing the format would mean changing them all.

**Solution:** the `FirmRepository` interface has two methods, `save(FirmData)` and `load()`. `JsonFirmRepository` (JSON file) and `DataStore` (Java serialization) both implement it. `ConsoleMenu` and `WebServer` only know the interface. `JsonFirmRepository` converts the objects to JSON by hand, because the domain classes should not contain storage code, and it rebuilds them through their constructors and the factories, so every rule is checked again when a file is loaded.

**Benefit:** the storage format can be swapped without touching the service or the UI (`--ser` switches back to the old format).

## Design pattern: Factory

**Problem:** creating a case or a billing strategy means choosing a subclass from a text or menu choice. That `if`/`switch` was repeated in the console menu, the web server and the loading code.

**Solution:** three small factories each hold that choice in one place:

| Factory | Input | Result |
|---|---|---|
| `CaseFactory.create(type, ...)` | `criminal`, `family` or `corporate` | the matching `LegalCase` subclass |
| `BillingFactory.create(method, ...)` | `hourly`, `flat` or `contingency` | the matching `BillingStrategy` |
| `RepositoryFactory.create(format)` | `json` or `ser` | the matching `FirmRepository` |

An unknown name gives a `ValidationException`. `LawFirmService.openCase(...)` uses `CaseFactory`, the console menu and web page use `BillingFactory`, and `Main` uses `RepositoryFactory`. A new case type is added in `CaseFactory` only.

## Design decisions

- **Layers:** the user interfaces (`ConsoleMenu` and `WebServer`) only read input and show output. The firm's rules are in `LawFirmService`, the data classes are in `domain`, and file handling is in `persistence`. This keeps each class focused on one job.
- **`Person` base class:** `Client` and `Lawyer` both have an id and a name with the same checks, so this code is written once in `Person`.
- **Rate multiplier per case type:** criminal 1.5, family 1.0, corporate 1.3. Each subclass returns its own value, so hourly billing works the same way for every case type.
- **Equality by id:** two clients (or two lawyers, or two cases) are equal if they have the same id. Ids are given out by `LawFirmService` and continue correctly after loading saved data.
- **Console menu and web page:** the console menu is the main interface; the web page is a second, optional one. `WebServer` uses the HTTP server that comes with the JDK and calls the same `LawFirmService` as the console menu, so the rules are not written twice and the web page needs no library of its own.
- **One save file:** all data is wrapped in a single `FirmData` object and written to one file, so the relationships between objects are saved and restored together. In the JSON file a case refers to its client and lawyer by id.
- **JSON with Gson:** the file can be read and checked by a person, and Gson (used only for the save file) is the only added dependency. The default format changed from Java serialization to JSON; the serialization code is kept as a second implementation of `FirmRepository`.

## Known limitations

- In the console menu, data is only saved when the user exits with option 0 (the web page saves after every change). If the program is closed in another way (for example the window is closed), the changes from that session are lost.
- If the save file is damaged, or is an old `.ser` file while the program runs without `--ser`, it cannot be loaded. The program then starts with empty data, and saving on exit replaces the old file.
- Clients and lawyers cannot be edited or deleted from the menu, and cases cannot be reopened.
- The conflict-of-interest check compares names (ignoring upper/lower case), so two different people with the same name are treated as the same person.
- Only one lead lawyer per case is stored.
- The console menu is the main interface. The optional web page has no login and is meant to be run on your own computer only, and it needs an internet connection for its Bootstrap styling.

## UML class diagram

The class diagrams are in the [`docs/`](docs/) folder as three views. The editable source is [`docs/class-diagram.drawio`](docs/class-diagram.drawio) (open it at diagrams.net, one page per view).

**Domain model and billing** (Strategy pattern: `LegalCase` uses a `BillingStrategy`)

![Domain model and billing](docs/class-diagram-domain.svg)

**Application layers**

![Application layers](docs/class-diagram-application.svg)

**Exception hierarchy**

![Exception hierarchy](docs/class-diagram-exceptions.svg)

## Contributor

- Hussein ALMahadin

## License

This project is licensed under the MIT License; see [LICENSE](LICENSE).
