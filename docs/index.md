---
  layout: default.md
  title: ""
---

# RESIDEX

[![Java CI](https://github.com/AY2627S1-CS2103T-W11-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W11-4/tp/actions/workflows/gradle.yml)

![RESIDEX client list with derived roles, property prices and transaction statuses](images/Ui.png)

**RESIDEX is a desktop client manager for property agents who manage buyers and sellers.**
Type commands to manage client contact details, remarks and associated properties, while the GUI displays the results.

* Each property records its type, Buying or Selling intention, location, transaction status, and an optional whole-dollar price and number of bedrooms.
* Client roles are derived from property intentions: Buyer, Seller, Buyer / Seller or Not specified.
* Prices belong to individual properties. There is no separate client budget amount or coloured budget band.
* Use `view INDEX` to inspect one client's full details and `list` to return to all clients.
* Find clients by name, email, phone, location, intention, transaction stage, property type, price range or number of bedrooms.
* Reverse a mistaken change with `undo`, and apply it again with `redo`.

The screenshot uses bundled sample clients with example remarks and property statuses.

* If you are interested in using RESIDEX, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing RESIDEX, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.


**Acknowledgements**

* RESIDEX is based on the [AddressBook-Level3](https://se-education.org/addressbook-level3/) project from the [SE-EDU initiative](https://se-education.org/).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
* Documentation is built with [MarkBind](https://markbind.org/) and diagrams are drawn with [PlantUML](https://plantuml.com/).
