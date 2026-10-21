---
  layout: default.md
  title: ""
---

# RESIDEX

[![Java CI](https://github.com/AY2627S1-CS2103T-W11-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W11-4/tp/actions/workflows/gradle.yml)

![RESIDEX client list with derived roles, property prices and transaction statuses](images/Ui.png)

**RESIDEX is a desktop client manager for property agents who manage buyers and sellers.**
Type commands to manage client contact details, remarks and associated properties, while the GUI displays the results.

* Each property records its type, Buying or Selling intention, location, optional whole-dollar price and transaction status.
* Client roles are derived from property intentions: Buyer, Seller, Buyer / Seller or Not specified.
* Prices belong to individual properties. There is no separate client budget amount or coloured budget band.
* Use `view INDEX` to inspect one client's full details and `list` to return to all clients.

The screenshot uses bundled sample clients with example remarks and property statuses.

* If you are interested in using RESIDEX, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing RESIDEX, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.


**Acknowledgements**

* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
