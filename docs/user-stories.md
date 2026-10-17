# User Stories

**User:** firm staff (for example, the office manager of a small law firm).

**Problem:** small law firms often track clients, cases and billable hours in spreadsheets. This makes it easy to overload a lawyer, miss a conflict of interest, or bill a case incorrectly. Law Firm Case Manager is a console application that keeps this information in one place and enforces the firm's rules automatically.

---

## US1 – Register clients and lawyers
As firm staff, I want to register clients and lawyers (with their hourly rate) so the firm has the people it needs to open cases.

**Acceptance criteria**
- A client has a name, email and phone number.
- A lawyer has a name and a positive hourly rate.
- Invalid input (empty name, negative rate) is rejected with a clear message.

## US2 – Open a case and assign a lead lawyer
As firm staff, I want to open a case (criminal, family or corporate) for a client and assign a lead lawyer, with the system refusing if the lawyer already has 5 active cases or has a conflict of interest.

**Acceptance criteria**
- Each case belongs to exactly one client and has exactly one lead lawyer.
- Assigning a lawyer who already has 5 active cases is refused.
- Assigning a lawyer to a case whose opposing party is one of that lawyer's own clients is refused (conflict of interest).

## US3 – Log hours, choose billing and close a case
As firm staff, I want to log billable hours, choose a billing method (hourly, flat fee or contingency) and close the case with an outcome, so the correct fee is calculated.

**Acceptance criteria**
- Hours must be positive; each entry has a date and a description.
- Hourly billing = hours × lawyer rate × case-type multiplier.
- Flat fee billing = a fixed amount.
- Contingency billing = a percentage of the settlement, charged only if the case is won.
- A case can be closed as won, lost or settled.

## US4 – List and sort cases
As firm staff, I want to list cases sorted by deadline, fee or client name so I can prioritise work.

**Acceptance criteria**
- The user can choose the sort order from the menu.
- The list shows case type, client, lead lawyer, status and current fee.

## US5 – Save and load data
As firm staff, I want all data saved when I exit and loaded when I start again, so nothing is lost between runs.

**Acceptance criteria**
- Clients, lawyers and cases (including hours and billing method) are saved to a file on exit.
- The data is loaded automatically on start.
- A missing or unreadable file does not crash the program; the user is informed and starts with empty data.
