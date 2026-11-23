# Design decisions and known limitations

## Design decisions

- **Layers:** the user interfaces (`ConsoleMenu` and `WebServer`) only read input and show output. The firm's rules are in `LawFirmService`, the data classes are in `domain`, and file handling is in `persistence`. This keeps each class focused on one job.
- **`Person` base class:** `Client` and `Lawyer` both have an id and a name with the same checks, so this code is written once in `Person`.
- **Rate multiplier per case type:** criminal 1.5, family 1.0, corporate 1.3. Each subclass returns its own value, so hourly billing works the same way for every case type.
- **Equality by id:** two clients (or two lawyers, or two cases) are equal if they have the same id. Ids are given out by `LawFirmService` and continue correctly after loading saved data.
- **Console menu and web page:** the console menu is the main interface; the web page is a second, optional one. `WebServer` uses the HTTP server that comes with the JDK and calls the same `LawFirmService` as the console menu, so the rules are not written twice and the web page needs no library of its own.
- **One save file:** all data is wrapped in a single `FirmData` object and written to one file, so the relationships between objects are saved and restored together. In the JSON file a case refers to its client and lawyer by id.
- **Objects know how to save themselves:** `Client`, `Lawyer`, `TimeEntry`, `LegalCase` and the billing classes each have a `toJson()` method, and the subclasses of `LegalCase` add their own detail by overriding it. Gson reads the fields of `Client`, `Lawyer` and `TimeEntry` itself; a case and a billing method are written by hand because a case saves its client and lawyer as ids and the billing method has to be told apart when loading. The factories (`CaseFactory`, `BillingFactory`, `ClientFactory`, `LawyerFactory`, `TimeEntryFactory`) create the objects again from the JSON through their constructors, so every rule is checked again when a file is loaded. `JsonFactory` creates the one configured Gson object that the whole project uses. `JsonFirmRepository` only loops over the lists and handles the file, instead of writing every property by hand.
- **JSON with Gson:** the file can be read and checked by a person, and Gson (used for the save file and in the `toJson`/`fromJson` methods) is the only added dependency.

## Known limitations

- In the console menu, data is only saved when the user exits with option 0 (the web page saves after every change). If the program is closed in another way (for example the window is closed), the changes from that session are lost.
- If the save file is damaged, it cannot be loaded. The program then starts with empty data, and saving on exit replaces the old file.
- Clients and lawyers cannot be edited or deleted from the menu, and cases cannot be reopened.
- The conflict-of-interest check compares names (ignoring upper/lower case), so two different people with the same name are treated as the same person.
- Only one lead lawyer per case is stored.
- The console menu is the main interface. The optional web page has no login and is meant to be run on your own computer only, and it needs an internet connection for its Bootstrap styling.
