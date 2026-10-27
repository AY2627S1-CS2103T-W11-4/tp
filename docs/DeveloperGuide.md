---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# RESIDEX Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* RESIDEX is adapted from the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project by the [SE-EDU initiative](https://se-education.org/). The overall architecture, the `Logic`/`Model`/`Storage`/`UI` split, the command parser structure and the undo/redo design follow AB3.
* Third-party libraries: [JavaFX](https://openjfx.io/) for the GUI, [Jackson](https://github.com/FasterXML/jackson) for JSON storage, and [JUnit 5](https://github.com/junit-team/junit5) for testing.
* Documentation tools: [MarkBind](https://markbind.org/) builds this guide and the [User Guide](UserGuide.md), and [PlantUML](https://plantuml.com/) draws the diagrams.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

`PersonListPanel` uses `PersonCard` for normal and filtered lists, and `PersonDetailsCard` for `view INDEX`.
Both display the client's derived intention; prices and transaction statuses belong to the property's display.
The detail card uses the client's name as its heading, labels each remaining field and shows the return-to-list hint.
`MainWindow` creates one `HelpWindow`, which constructs a `CommandHelpCard` for each supported command.
The current screens are illustrated in the [User Guide](UserGuide.html#reading-the-interface).

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a client).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="580" />


The `Model` component,

* stores the client data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores each client's properties as an ordered, unmodifiable list within `Person`.
* derives each client's `ClientIntention` from the immutable property list without storing a second role field.
* applies a `FilteredList<Person>` and then a `SortedList<Person>` to the source client list. It exposes one unmodifiable displayed `ObservableList<Person>` to both the UI and index-based commands.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-W11-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both client data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* uses `JsonAdaptedPerson` and `JsonAdaptedProperty` to preserve each client's property list and its order.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Protecting client data after a failed load

`MainApp#initModelManager()` propagates `DataLoadingException` when an existing client data file cannot
be loaded. `MainApp#init()` then provides an error UI without creating a model or command logic.
When the application starts, the error dialog shows the file's absolute path and recovery instructions,
then the application exits. `stop()` skips saving preferences when no model was initialized.

This prevents command-triggered automatic saving from overwriting the failed file with an empty list.
The user must repair or replace the file and restart. Moving the file aside explicitly selects the existing
first-run behavior, which loads sample clients. A valid file containing an empty client list still loads normally.

### Command reference help

`HelpCommand` returns a `CommandResult` with `showHelp` set to `true`. The existing `MainWindow#handleHelp()`
method shows or focuses a single `HelpWindow`. The `F1` shortcut uses the same method.

`HelpContent` holds each command's description, syntax, notes and examples, using shared command words,
prefixes and validation messages where appropriate. `HelpWindow` creates a `CommandHelpCard` for each entry.
The cards are display-only and sit inside a scrolling pane, with the heading and User Guide footer outside it.
The layout and styles are defined in `HelpWindow.fxml`, `CommandHelpCard.fxml` and `HelpWindow.css`.

The reference is bundled with the app and works offline. The footer's Copy URL button copies the RESIDEX User Guide URL.
Update `HelpContent` and the User Guide together when commands change. `HelpContentTest` checks that the documented
examples parse as the intended commands without executing them or starting JavaFX.

### Find command

`FindCommandParser` accepts exactly one of the shared `CliSyntax` prefixes: `n/`, `e/`, `p/`, `l/`, `i/`, `s/`, `t/`, `pr/` or `bd/`.
For example, `find n/Alice Bob` searches names; prefix letters are case-insensitive.
Name, email, phone, and location searches use case-insensitive substring matching with OR semantics across keywords.
`LocationContainsKeywordsPredicate` checks every entry in `Person#getProperties()` and returns a client once if any
property location contains any keyword. Clients without properties do not match a location search.
For example, `find l/clementi` matches clients with locations such as `Clementi Ave 3` or `Woodlands Clementi`.
Intention searches accept exactly one value, `buyer`, `seller` or `both`, case-insensitively.
Missing keywords, repeated or mixed prefixes, and unsupported selectors are rejected.
`find i/unspecified` is rejected. `IntentionMatchesPredicate` includes `BOTH` in buyer/seller searches and matches
only `BOTH` for the both query. Its comparator ranks exact-role matches before mixed-role matches.

`ModelManager` maintains the following live list pipeline:

```text
AddressBook source list -> FilteredList -> SortedList -> unmodifiable displayed list
```

For intention predicates, `updateFilteredPersonList` applies the role comparator and breaks ties using source-list
positions. The tie-breaker preserves group order when a property command replaces a client and changes their role.
An identity-based source-position lookup replaces repeated linear scans in the comparator. A source-list
invalidation clears the lookup before changes reach the transformation lists; the next tie comparison rebuilds it
in one pass. Later comparisons use constant-time lookups until the source changes again. This also releases old
replacement instances and handles additions, deletions and complete address-book resets without stale positions.
For every other predicate, including `list`, it clears the comparator, restoring source order.
Neither filtering nor sorting reorders the `AddressBook` or saved client list.

`getFilteredPersonList()` returns this single displayed list to `LogicManager` and the UI.
`EditCommand`, `DeleteCommand`, `RemarkCommand` and `PropertyCommandUtil` resolve client indexes from the same list.
Property commands preserve the active filter, so a role-changing update can move or remove the client from results.
`IntentionSearchIntegrationTest` verifies all six indexed operations against sorted results, first/last/invalid
displayed indexes, live role changes, normal-order restoration and saved source order.
`ModelManagerIntentionOrderTest` additionally checks replacements, source resets and 1,000 clients without
timing assertions: intention sorting must not scan clients using `Person#equals()` to resolve ties.

Status searches accept exactly one `PropertyStatus` token, ignoring case.
`FindCommandParser` shares `PropertyParserUtil.parseStatus()` with property commands while retaining
the find command's validation order and error messages.
`PropertyStatusMatchesPredicate` matches a client if any existing property has the requested status.
The predicate returns false for an empty property list, including when searching for `UNSPECIFIED`.
Multiple matching properties still produce one client result.
The predicate bases both equality and hashing on the requested status.

Status results retain source order through the displayed list; a status query clears the intention comparator.

Type and price searches each accept exactly one value. `find t/` reuses `PropertyParserUtil.parseType()`, so the same
tokens as `addp` are accepted (`HDB`, `Condominium`, `Landed`, or `h`, `c`, `l`), ignoring case.
`PropertyTypeMatchesPredicate` matches a client if any property has that type.
`find pr/` takes a range of whole-dollar amounts: `MIN-MAX`, `MIN-` or `-MAX`. `FindCommandParser` splits it on the
single `-`, validates each bound with `PropertyPrice.isValidPrice()`, and rejects empty, malformed or reversed ranges
with `FindCommand#MESSAGE_INVALID_PRICE_RANGE`.
`PropertyPriceInRangePredicate` compares the inclusive bounds with each property's price. Properties without a price
never match, and a client appears once even if several properties match.
`find bd/` accepts exactly one number and validates it with `PropertyParserUtil.parseBedrooms()`, so it shares the
1 to 20 range with `addp` and `editp`. `PropertyBedroomsMatchesPredicate` matches a client if any property has exactly
that number of bedrooms; properties with unknown bedrooms never match.
These predicates clear the intention comparator, so their results use normal client order.
Replacing a property re-evaluates the active status predicate, so clients may leave the displayed results.
`PropertyStatusSearchIntegrationTest` checks stages, selector coexistence, saved order and displayed-index targeting.

### Derived client intention

`Person#getIntention()` computes a `ClientIntention` from the current property list on demand.
Properties use `PropertyIntention.BUYING` and `PropertyIntention.SELLING` for their direction.

| Buying-direction property present | Selling-direction property present | Derived role | Display |
| --- | --- | --- | --- |
| No | No | `UNSPECIFIED` | Not specified |
| Yes | No | `BUYER` | Buyer |
| No | Yes | `SELLER` | Seller |
| Yes | Yes | `BOTH` | Buyer / Seller |

No client intention field or constructor parameter is stored in `Person`.
Equality and hashing use the properties, so they also reflect everything that determines the role.
`PersonBuilder` and sample clients without properties produce `UNSPECIFIED`; no placeholder property is inferred.
Fresh sample data gives Alex buying properties, Bernice selling properties and Charlotte both directions,
while David, Irfan and Roy have no properties. This demonstrates every derived role and intention-search ordering
without changing how real saved clients are loaded.
All property-copy methods derive the new role automatically while leaving the original `Person` unchanged.
`copyWithProperties` asserts that the replacement preserves client identity, an internal copy invariant.
This assertion does not validate user input or perform required work. Gradle tests enable assertions by default.

`AddCommandParser` accepts name, phone and email; the client is initially `UNSPECIFIED`.
Both client add and edit call `ParserUtil.rejectClientIntention` before tokenizing fields.
This shared check raises `ParseException` for any standalone `i/` field, including mixed case, blank and repeated
values, preventing it from being absorbed into another field's text. Property commands still accept their own `i/`.

`JsonAdaptedPerson` accepts the legacy JSON `intention` parameter as ignored input and stores no role field.
New JSON omits client intention; only properties determine the role after loading.
A missing, null or stale legacy client role does not prevent otherwise valid data from loading.
For example, legacy Buyer plus an empty property list becomes `UNSPECIFIED`; legacy Buyer plus only Selling
properties becomes `SELLER`. Property-value validation and indexed storage errors remain unchanged.
Round-trip tests cover all four roles, and legacy-file tests verify that loading does not rewrite the file.

### Duplicate client feedback

`AddCommand` and `EditCommand` search the complete stored client list using `Person#isSamePerson(Person)` when checking for a conflict.
This keeps the name-and-phone identity rule unchanged and finds conflicting clients outside the current filter.
An edit that retains the selected client's identity is allowed without treating that client as its own duplicate.
Both commands use the shared `Messages.MESSAGE_DUPLICATE_PERSON` format to identify the stored client's name and phone number and explain that name matching ignores capitalisation and extra spaces.
The feedback does not use an index from the unfiltered list, since it could identify a different client in the displayed results.
Conflicts throw `CommandException` before any model update or save occurs.

### Client fields and property amounts

Clients store their name, phone, email, remark and ordered properties. Their role is derived from those properties.
Monetary amounts belong only to property entries through `PropertyPrice` and `pr/`. `add` and `edit` call
`ParserUtil.rejectClientBudget` before tokenization to reject the removed client `b/` prefix in any position or case.
This prevents a removed prefix from being accepted as part of another field's value.

`JsonAdaptedPerson` no longer stores or writes client budgets. The existing JSON reader ignores unknown fields,
so older `budget` entries are discarded when loading and disappear on the next save. Other client fields,
property order, prices and transaction statuses are preserved, as is the client's derived role.
There is no client budget amount or colour band in either client list cards or the detail view.

### Client property model

The property model, storage, numbered client-card display and `addp`, `delp` and `editp` commands are implemented.
Client `add` omits intention, and client `add`/`edit` reject manual intention fields.
Property fields belong to the separate property commands.

#### Fields and validation

`Property` is an immutable value object in `seedu.address.model.property`.
Each entry describes one client's interest in buying or selling a property.

| Field | Type | Rules |
| --- | --- | --- |
| Type | `PropertyType` | Required: `HDB`, `CONDOMINIUM`, or `LANDED`. `parse` accepts full names and `h`, `c`, or `l`, ignoring case and surrounding whitespace. |
| Intention | `PropertyIntention` | Required: `BUYING` or `SELLING`. `parse` accepts `Buying` or `Selling`, ignoring case and surrounding whitespace. Client-role values are invalid. Property directions determine the derived `ClientIntention`. |
| Location | `PropertyLocation` | Required, nonblank text. Surrounding whitespace is removed; case and internal whitespace are preserved. |
| Price | `PropertyPrice` | Optional. A supplied price must be a positive whole-dollar SGD amount up to `Long.MAX_VALUE`, without commas, decimals, leading zeros, or surrounding whitespace. |
| Bedrooms | `PropertyBedrooms` | Optional. A whole number from 1 to 20 without leading zeros, a decimal point or a sign. `getBedrooms()` returns `Optional.empty()` when unknown. |
| Status | `PropertyStatus` | Always non-null internally. The six tokens ignore case and outer whitespace; omitted creation status defaults to `UNSPECIFIED`. Empty input is invalid. |

Use the three-argument `Property` constructor when the price is unknown, or pass `null` as the fourth argument.
`getPrice()` returns `Optional.empty()` for an unknown price. Zero is invalid for a property price.
The three- and four-argument constructors default status to `UNSPECIFIED`.
Use the five-argument constructor `(type, intention, location, price, status)` to record an explicit stage;
`price` may be null, but `status` may not. `getStatus()` returns the enum directly.
Amounts are stored only on property entries; there is no client budget field.
For a buying property, price records the asking/current listing price; for a selling property, the client's asking price.
Status edits do not change property prices automatically.

#### Transaction status

Each immutable `Property` owns one current status, independently of its direction and the client's other properties.
Status participates in equality, hashing, display and diagnostic strings.

| Enum / token | Display | Meaning |
| --- | --- | --- |
| `UNSPECIFIED` / `unspecified` | Not specified | No transaction stage has been recorded for this property yet. |
| `ENQUIRING` / `enquiring` | Enquiring | The client has shown interest in buying or selling the property but has not committed to proceeding. |
| `VIEWING` / `viewing` | Viewing | For a Buying property, the agent arranges a client visit so the client can assess whether to proceed. For a Selling property, the agent visits and assesses the seller's property before representing and marketing it to prospective buyers. The agent is the intermediary; RESIDEX does not require the seller and prospective buyers to deal directly with each other. |
| `NEGOTIATING` / `negotiating` | Negotiating | The client intends to proceed and the relevant parties work toward agreement on price and other transaction terms. |
| `CLOSING` / `closing` | Closing | The main transaction terms are agreed, but contracts, signing, payment or other final formalities remain incomplete. |
| `COMPLETED` / `completed` | Completed | Required documents are signed, payment is completed and the property has been handed over. |

This is a recorded assessment, not a one-way state machine. All old/new status pairs are permitted,
including Viewing to Enquiring, Closing to Negotiating and corrections from Completed.
Completed does not delete, archive or lock a property or client. Explicit `delp`/`delete` remains necessary.
There is no client-level status, stage history, viewing-date field or automatic transition.
The property-command token for Not specified is `unspecified`. Status search also accepts the display text
`Not specified`, ignoring case and whitespace between its words; `not-specified` remains invalid.
`FindCommandParser` handles this search alias after validating selectors, leaving property parsing and saved statuses unchanged.

`PropertyIntention` describes an individual property's buying or selling action.
It is separate from `ClientIntention`, the role derived from all of the client's properties.
Each `Property` has exactly one non-null intention, enforced by its constructor and the two-value enum.
Changing a property's intention recomputes the client role and can change how `find i/` matches or orders that client.

#### Ownership and indexes

`Person` owns a `List<Property>`, initially empty. The constructor copies incoming lists, rejects null lists and entries,
and `getProperties()` exposes an unmodifiable list. Properties have no owner reference or permanent ID.
Identical entries are allowed within a client and across clients; they are independent records.

Displayed property indexes start at 1 within each client. Appending adds to the end, replacing keeps the same position,
and removing an entry shifts later entries down by one. For example, deleting property 2 from a three-entry list makes
the former property 3 become property 2. The client-card display uses this same list order.

Property changes affect `Person.equals()` and `hashCode()`, including list order. They do not change the client's
identity as determined by `isSamePerson()`.

#### Commands and parsing

`AddressBookParser` normalises command words with `Locale.ROOT`, making command words case-insensitive.
`AddPropertyCommandParser`, `DeletePropertyCommandParser` and `EditPropertyCommandParser` parse the three property commands.
`PropertyParserUtil` recognises `t/`, `i/`, `l/`, `pr/`, `bd/` and `s/` case-insensitively without changing field values.
Unknown and repeated fields are rejected, including repeated prefixes with different capitalisation.
Whitespace-separated alphabetic words ending in `/` are reserved as prefixes and cannot be used as literal location text.
Property type aliases and Buying/Selling values ignore case in property commands.
`PropertyParserUtil.parseIntention` delegates to `PropertyIntention.parse` and translates invalid values into
`ParseException`. `Buyer`, `Seller`, `Both` and blank values are rejected; legacy values are accepted only by storage.

`addp CLIENT_INDEX t/TYPE i/INTENTION l/LOCATION [pr/PRICE] [bd/BEDROOMS] [s/STATUS]` requires a type, intention and nonblank location.
The parser validates every field before constructing a complete `Property`; an omitted price is unknown.
An omitted status defaults to `UNSPECIFIED`, and omitted bedrooms are unknown. A supplied empty price, bedrooms or status is invalid when adding.
`delp CLIENT_INDEX PROPERTY_INDEX` accepts exactly two positive indexes and no field arguments.
`editp CLIENT_INDEX PROPERTY_INDEX [t/TYPE] [i/INTENTION] [l/LOCATION] [pr/PRICE] [bd/BEDROOMS] [s/STATUS]` requires at least one field.
`EditPropertyDescriptor` copies the requested edits and distinguishes an omitted price or bedroom count from an explicitly cleared one.
An empty `pr/` clears the price and an empty `bd/` clears the bedrooms on edit, while empty required fields are rejected.
Omitted `s/` preserves status; empty `s/` is rejected. `s/unspecified` explicitly resets the stage.
The descriptor copies a supplied non-null status and accepts status-only or combined edits.
Invalid status tokens are translated from `IllegalArgumentException` to `ParseException` with the cause preserved.

`PropertyCommandUtil` resolves the client from `model.getFilteredPersonList()` and validates the property index within that client's list.
Both indexes use the existing `Index` class and errors are reported through `CommandException`.
Commands preserve the active client filter and all unrelated fields, including an omitted status.
The derived role and displayed position can change.
Changing or deleting a matching property can remove the client from status results; subsequent indexes use that new view.
`EditPropertyCommand` constructs a replacement using the current values for omitted fields.
Only after successful validation does it call `model.setPerson` once.
An equal replacement reports no change and leaves the original `Person` in the model.
Invalid parses or indexes leave both the model and saved data unchanged.

After a successful `model.setPerson`, `EditPropertyCommand` retains the generic FINE edit diagnostic and adds one
FINE transition record only when the status actually changes.
It uses the existing `LogsCenter` logger and a lazy message containing displayed client/property indexes and enum names:

```text
Property status changed for displayed client 2, property 1: VIEWING -> NEGOTIATING
```

The diagnostic contains no client name, phone, email, location or full command/domain object.
Same-status edits, changes to other fields, rejected indexes and failed model mutations produce no transition record.
The existing default INFO level is unchanged. `EditPropertyStatusLoggingTest` temporarily attaches a FINE handler,
checks that mutation precedes the record, and restores logger settings after each test.
The handler verifies the underlying source client, including when a parsed status edit removes that client
from `find s/viewing` results. The regression checks the original displayed indexes and a single PII-free record.

The add-property command applies the immutable model update after validating the displayed client index:

```java
Property property = new Property(
        PropertyType.HDB, PropertyIntention.BUYING, new PropertyLocation("Clementi Ave 3"));
Person original = model.getFilteredPersonList().get(personIndex.getZeroBased());
Person updated = original.withAddedProperty(property);
model.setPerson(original, updated);
```

For editing, call `original.withUpdatedProperty(propertyIndex, replacement)` with a complete replacement property.
The edit-property command preserves the original values for omitted fields.
For deletion, call `original.withoutProperty(propertyIndex)`. Both methods return a new person and throw
`IndexOutOfBoundsException` when the property index is outside this client's list.
All three methods preserve unrelated client fields and leave the original person unchanged.
Apply the returned person through `model.setPerson`; calling a method alone does not update the model.

Existing `EditCommand` and `RemarkCommand` carry the property list into the replacement person so that changing contact
details or remarks preserves all properties. Future code that reconstructs a `Person` must also preserve the list.
The five-argument `Person` constructor creates a client with no properties; the six-argument constructor
accepts the property list as its final argument.

#### Storage and compatibility

Each saved client has a `properties` array. `JsonAdaptedProperty` stores `type`, `intention`, and `location` as strings,
`price` as a whole-dollar string or `null` when unknown, and `status` as a canonical enum name such as `VIEWING`.
Property order and duplicate entries survive saving and loading. A missing `price` also loads as unknown.
Missing or null legacy status loads as `UNSPECIFIED`; otherwise the status must be a recognised token.
`bedrooms` is saved as a whole-number string, or `null` when unknown; a missing `bedrooms` field also loads as unknown, so older files still open. An invalid saved value is rejected like any other invalid property field.
The adapter retains its four-argument convenience constructor for old callers and uses a five-field JSON creator.
New saves always write a status for each property. Loading alone does not rewrite the file.

Property intentions are saved canonically as `Buying` or `Selling`.
The adapter also accepts exact legacy `Buyer` and `Seller` values, mapping them to `BUYING` and `SELLING`.
This compatibility rule stays in `JsonAdaptedProperty` and does not broaden command syntax.
Legacy client-level intention is ignored and omitted from new saves; the client role is derived from properties.
Mixed old/new property values can be loaded in one file.
Reading does not rewrite the file; saving writes all property intentions in the new canonical form.
Unlike CLI input, saved intention values are case-sensitive and do not permit surrounding whitespace.

`JsonAdaptedPerson` treats a missing or null `properties` field as an empty list, so valid v1.3 files load without
migration. Null property entries, missing required fields, and invalid values are rejected with `IllegalValueException`;
the error includes the property's one-based index. File loading reports these errors as `DataLoadingException`.
Invalid entries are not silently discarded.
An invalid or blank persisted status follows the same indexed `IllegalValueException`/`DataLoadingException` path;
it is not replaced with a default. File-level tests check that rejected loads leave the original bytes untouched.

The regression tests cover field validation, case-insensitive parsing, price clearing, index changes, independent clients and preservation through client edits and remarks.
Integration tests execute command strings through the real parser, model and JSON storage, checking unchanged data after rejected operations and persistence after valid operations.
Old-file compatibility and property-list immutability are also covered.
Storage tests assert canonical output, legacy read-save-read migration, mixed representations, and preserved order,
duplicates and client fields. Invalid property intentions retain the indexed `IllegalValueException` error path;
full-file reads fail without changing the input file.
Status coverage includes all six parse/display values, locale independence, all 36 old/new stage pairs,
omission/default/reset behavior, canonical round trips, invalid legacy data, Completed edits,
live status-filter membership and exact transition-log content/suppression.

#### Property display

`PersonCard` renders one wrapped label per property, using its one-based list position and `Property#toDisplay()`.
The property list is hidden when empty, and `PersonListPanel` measures each card at its available width so longer locations expand the row.
Type, property intention, location, a known price and readable status are displayed together;
an unknown price has no amount, while `UNSPECIFIED` displays as Not specified.
`HelpContent` includes the three property commands and its examples are parsed by `HelpContentTest`.

### Viewing complete client details

`ViewCommandParser` delegates index validation to `ParserUtil.parseIndex`. `ViewCommand` resolves that index
against `Model#getFilteredPersonList()` before changing the filter, so search-result indexes are respected.
An out-of-range index raises `CommandException` without changing the filter. A valid request filters by the
selected client's identity through `Model#showPersonDetails`. No client records are changed.
`ModelManager` owns an observable `ClientViewMode`: `ALL_CLIENTS`, `FILTERED_CLIENTS` or `CLIENT_DETAILS`.
`list` uses the existing show-all predicate to restore all clients in stored order and clear search mode.

`Logic` exposes the model's read-only display mode, which `MainWindow` passes to `PersonListPanel`.
The heading follows this mode, so a search matching all or no clients still shows `Filtered clients`.
Each cell observes detail mode with a weak listener
and immediately replaces its graphic with `PersonDetailsCard` or the usual `PersonCard`, including when
JavaFX reuses cells after `list`. This avoids leaving detail cards visible until a later layout pulse.
The panel displays the return hint above the scrollable list. The detail card uses the client's name as its heading
and lays out the other four scalar client fields and every property's six fields in labelled, wrapping rows.
The Bedrooms row shows the recorded count, or `Not recorded` when absent.
Missing optional values use explicit placeholders.
Property updates preserve detail mode and refresh the selected client; deleting that client restores all remaining
clients. Ordinary searches keep their predicates and displayed-index targeting when property data changes.
Commands that reset the filter use the normal cards; help and failed commands preserve the current layout.
The single displayed client has index 1, consistent with other filtered lists.

Assertions check the internal postcondition that viewing yields exactly the selected client and the UI precondition
that detail mode receives one client. User input errors are handled with exceptions, following the
[SE-EDU assertion guidance](https://se-education.org/se-book/errorHandling/index.html).
`ViewCommand` uses `LogsCenter` and `java.util.logging` at `FINE` level for the selected displayed index and
out-of-range failures, following the [SE-EDU logging conventions](https://se-education.org/guides/conventions/java/logging.html).
These messages omit client field values. `gradlew run` and the test task already enable assertions.

Parser and command tests cover malformed arguments, filtered and unfiltered indexes, empty lists, read-only
behaviour, repeated viewing and restoration. `ResidexUiTest` checks the actual command wiring, all field values,
optional placeholders, property order, transaction status, wrapping and the return hint.
`ViewCommandLoggingTest` captures the `FINE` records for success and invalid indexes and checks that
client details are absent. `ViewCommandTest` also simulates a model that fails to filter and verifies that
the runtime assertion detects the invalid display state. Ranked intention-search results are covered separately.

### Undo/redo feature

#### Implementation

The undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as a `states` list and a `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current client data state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous client data state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone client data state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Each data-changing command (`add`, `edit`, `delete`, `remark`, `addp`, `editp` and `delp`) calls `Model#commitAddressBook()` after it succeeds. A command that fails, or an `editp` that leaves the property unchanged, does not commit. `edit` and `remark` also skip the commit when the edited client equals the original, so an edit that changes nothing does not clear what can be redone. The history keeps the `VersionedAddressBook#MAX_UNDOABLE_CHANGES` (100) most recent changes, which is 101 saved states including the current one, dropping the oldest first. The `undo` and `redo` commands also reset the displayed list to show all clients.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial client data state, and the `currentStatePointer` pointing to that single client data state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th client in RESIDEX. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the client data after the `delete 5` command executes to be saved in the `states`, and the `currentStatePointer` is shifted to the newly inserted client data state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new client. The `add` command also calls `Model#commitAddressBook()`, causing another modified client data state to be saved into the `states`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the client data state will not be saved into the `states`.
</box>

Step 4. The user now decides that adding the client was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous client data state, and restores the client data to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial client data state, then there are no previous client data states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the client data to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `states.size() - 1`, pointing to the latest client data state, then there are no undone client data states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the client data, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `states` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `delete 1`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `states`, all client data states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire client list.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the client being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

### Viewing application logs in VS Code or Cursor

These steps use the integrated terminal in VS Code or Cursor.
IntelliJ IDEA procedures may differ and are not covered here.
Logging is available throughout RESIDEX through `LogsCenter`; the property commands below are an example.

1. Open the repository root in the editor, where `gradlew` and `build.gradle` are located.

2. Open `src/main/java/seedu/address/commons/core/LogsCenter.java`.
   The default `LOG_LEVEL` is `Level.INFO`.
   To see detailed debugging messages, temporarily change it to the following and save the file:

   ```java
   private static final Level LOG_LEVEL = Level.FINE;
   ```

3. Select **Terminal > New Terminal** and run the following from the repository root:

   ```bash
   ./gradlew run --console=plain
   ```

   On Windows, use `.\gradlew.bat run --console=plain` instead.
   If the app is already running, close it first and restart it using this command so the new logging level takes effect.
   Logs appear in the terminal that launches the app.

4. In the app, choose a test client with no properties.
   The following example assumes the client is displayed at index `1`; replace the client index if needed.
   Run these commands one at a time:

   ```text
   addp 1 t/HDB i/Buying l/Clementi pr/800000
   editp 1 1 l/Bishan
   editp 1 1 l/Bishan
   delp 1 1
   delp 1 99
   ```

5. Check the terminal for these `FINE` messages in order:

   ```text
   Added property 1 to displayed client 1
   Edited property 1 for displayed client 1
   Property 1 is unchanged for displayed client 1
   Deleted property 1 from displayed client 1
   ```

   The final command has an invalid property index, so it should show an error in the app without a successful deletion log.
   An `INFO` entry containing `[USER COMMAND]` records an attempt, including commands that fail validation.
   The property-operation messages record completed model changes or an unchanged edit; they do not confirm that saving to disk succeeded.

6. To review saved output, open `residex.log.0` in the repository root.
   Logs rotate between `residex.log.*` files, and concurrent app instances may use additional file suffixes.
   On macOS or Linux, you can also open a second terminal and follow the log live:

   ```bash
   tail -f residex.log.0
   ```

   Press **Ctrl+C** in that terminal to stop following the log.

7. After debugging, restore `LOG_LEVEL` to `Level.INFO` and save the file.
   Check the editor's Source Control view to ensure the temporary logging-level change is removed before committing.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a property agent who manages multiple clients and property listings
* needs to record, search, and update client preferences and property details efficiently
* tracks leads and ongoing transactions across several properties per client
* prefers fast CLI-based interactions for managing information
* needs to filter clients by categories such as buyer, seller, property type, price and number of bedrooms

**Value proposition**: RESIDEX provides property agents with a fast and organised way to manage buyers, sellers, their properties and transaction stages in one place. Its efficient CLI-based commands allow agents to quickly retrieve, update and recover client information, without navigating through multiple screens.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

The general user story for updating client information is an epic that is further refined by the more specific user stories below.

#### Implemented

| Priority | As a …                              | I want to …                                                | So that I can …                                               |
|----------|--------------------------------------|------------------------------------------------------------|----------------------------------------------------------------|
| `* * *`  | property agent who types quickly     | perform client-management actions through a CLI            | complete routine tasks quickly                                |
| `* * *`  | property agent                       | add a new client                                           | keep track of the people I serve                              |
| `* * *`  | property agent                       | identify whether a client buys, sells, both or neither       | manage the client according to their property needs           |
| `* * *`  | property agent                       | view all the clients I am currently serving                | keep track of my active clients                               |
| `* * *`  | property agent managing many clients | search for clients by name, phone number, email or property location | retrieve their information quickly                  |
| `* * *`  | property agent                       | filter clients by buying or selling role, property stage, property type, price range or number of bedrooms | focus on clients relevant to my current task |
| `* * *`  | property agent                       | view a client's profile                                    | review their contact details, remark and each property's details and stage |
| `* * *`  | property agent                       | update a client's information, including each property's stage | keep the client's record accurate                          |
| `* * *`  | property agent                       | record a client's contact details                          | contact the client when needed                                |
| `* * *`  | property agent                       | record the type, location, price and number of bedrooms of each property | recommend and track properties that match the client's needs |
| `* * *`  | property agent                       | record property information under a client's profile       | keep track of the properties relevant to that client         |
| `* * *`  | property agent                       | remove selected optional information from a client's profile | correct outdated information without deleting the client   |
| `* * *`  | property agent                       | undo and redo my recent changes                            | recover quickly from a mistaken command                       |
| `* * *`  | property agent                       | delete a client                                            | remove client records that I no longer need                   |
| `* * *`  | property agent                       | have my changes saved automatically                        | retain my client records between application sessions        |
| `* * *`  | new property agent                   | view the available commands                                | learn how to use the application correctly                    |

#### Planned (not yet implemented)

These stories are not part of the current release. They are kept to record the product direction.

| Priority | As a …                              | I want to …                                                | So that I can …                                               |
|----------|--------------------------------------|------------------------------------------------------------|----------------------------------------------------------------|
| `* * *`  | property agent                       | assign priorities to clients and filter by priority        | determine which clients to focus on                           |
| `* * *`  | property agent                       | set or update a client's next follow-up date               | know when I should contact the client again                   |
| `* * *`  | property agent                       | view upcoming and overdue follow-ups                       | avoid missing important follow-up actions                     |
| `* * *`  | property agent                       | view a client's recorded information history               | review how the client's information has changed over time     |
| `* *`    | property agent                       | schedule a property viewing for a client                   | keep track of the client's viewing appointment                |
| `* *`    | property agent                       | attach an internal note to a client                        | retain useful details without displaying them in the client list |
| `* *`    | property agent                       | view clients who are interested in a particular property   | identify potential leads for that property                    |
| `* *`    | property agent                       | filter clients whose profiles contain active property listings | focus on active sellers                                    |

### Use cases

For all use cases below, the **System** is `RESIDEX` and the **Actor** is a `property agent`.
`MSS` stands for Main Success Scenario. The MVP requirements describe features that are implemented. The section for requirements outside the MVP lists
features that are planned but not yet implemented.

A client can be Buyer, Seller, both or Not specified, derived from their properties.
Two clients are duplicates when both their normalised names and
normalised phone numbers match, following the Add Client feature specification.
Names are compared case-insensitively after trimming surrounding spaces and replacing consecutive spaces with one space.
Phone input is trimmed during parsing; valid phone numbers contain only digits and are compared exactly, including leading zeros.
Email, intention and remark do not affect duplicate detection.
`Person#isSamePerson(Person)` applies this rule to additions, edits and loading saved data.
`Person#equals(Object)` still compares all stored fields exactly.
A client selected for an operation must belong to the currently displayed list. For deletion,
the client is identified by their index in that list, including when it contains search results.

**Precondition for all use cases:** RESIDEX is running.

**Guarantees:** Read-only use cases leave stored client information unchanged. For requests to add, update or
delete information, invalid input is rejected without changing that information.

#### MVP requirements

**Use case: UC01 — Add a client**

**MSS**

1. Property agent requests to add a client with their name, phone number and email.
2. RESIDEX adds the client with no properties and intention Not specified, then displays the updated client list.

   Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

  * 1a1. RESIDEX explains the input error.
  * 1a2. Property agent supplies corrected client details.

  Steps 1a1–1a2 are repeated until the details are valid.
  Use case resumes at step 2.

* 1b. Both the normalised name and normalised phone number match an existing client.

  * 1b1. RESIDEX reports that the client already exists.

  Use case ends.

**Use case: UC02 — Find clients**

**MSS**

1. Property agent requests to find clients by name, phone number, email, property location, derived intention,
   property transaction stage, property type, price range or number of bedrooms.
   They supply keywords for contact/location fields, or one complete value for the other fields.
2. RESIDEX displays the matching clients and the number of matches.

   Use case ends.

**Extensions**

* 1a. The search field or keywords are missing, the field is unsupported, or more than one field is supplied.

  * 1a1. RESIDEX explains the input error.

  Use case resumes at step 1.

* 2a. No clients match.

  * 2a1. RESIDEX reports that no clients match and displays an empty result list.

  Use case ends.

* 2b. Property agent wants to refine the search.

  Use case resumes at step 1.

* 2c. Property agent wants to return to the full client list.

  * 2c1. Property agent requests to list all clients.
  * 2c2. RESIDEX displays the full client list.

  Use case ends.

**Use case: UC11 — View a client's full details**

**MSS**

1. Property agent requests to list or find clients.
2. RESIDEX displays the corresponding client list with indexes.
3. Property agent requests to view a client using their displayed index (`view INDEX`).
4. RESIDEX displays only that client's name, phone, email, intention, remark and all property fields.
   Missing optional values are explicitly indicated. RESIDEX displays
   `Type list to return to the original list`.
5. Property agent requests to list all clients (`list`).
6. RESIDEX restores the complete client list in its original order.

   Use case ends.

**Extensions**

* 2a. The displayed list is empty.

  Use case ends.

* 3a. The index is missing, malformed, non-positive or outside the displayed list, or extra arguments are supplied.

  * 3a1. RESIDEX explains the input or selection error and keeps the current display and stored data unchanged.

  Use case resumes at step 3.

* 4a. The details exceed the available window height.

  * 4a1. Property agent scrolls to review the remaining fields.

  Use case resumes at step 5.

* 5a. Property agent wants the earlier search results instead of all clients.

  * 5a1. Property agent repeats the earlier search.
  * 5a2. RESIDEX displays its matching clients.

  Use case ends.

**Use case: UC12 — Undo and redo a change**

**MSS**

1. Property agent enters a command that changes client information, such as `delete`.
2. RESIDEX applies the change.
3. Property agent realises the change was a mistake and requests to undo it.
4. RESIDEX restores the client information from before the change and shows all clients.
5. Property agent optionally requests to redo the change.
6. RESIDEX applies the change again and shows all clients.

   Use case ends.

**Extensions**

* 3a. There is nothing to undo, for example because RESIDEX was only just started.

  * 3a1. RESIDEX shows an error. No client information is changed.

  Use case ends.

* 5a. There is nothing to redo, for example because a new change was made after the undo.

  * 5a1. RESIDEX shows an error. No client information is changed.

  Use case ends.

**Use case: UC03 — Filter clients**

**MSS**

1. Property agent requests to filter clients by criteria such as buyer/seller category,
   property transaction stage, property type, price range or number of bedrooms.
2. RESIDEX displays the clients satisfying the criteria.
3. Property agent requests to view a listed client's profile.
4. RESIDEX displays the client's details, including each property's type, location, price, number of bedrooms
   and stage.

   Use case ends.

**Extensions**

* 1a. The filter criteria are invalid.

  * 1a1. RESIDEX shows an error explaining the accepted criteria. The displayed list remains unchanged.

  Use case resumes at step 1.

* 2a. No clients satisfy the criteria.

  * 2a1. RESIDEX displays an empty list.

  Use case ends.

* 3a. The selected client is not in the displayed list.

  * 3a1. RESIDEX shows an error. No client records are changed.

  Use case resumes at step 3.

**Use case: UC04 — Update a client's information**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests to update a listed client, supplying the new information,
   such as contact details, remark, or each property's type, location, price, number of bedrooms
   or transaction stage.
4. RESIDEX updates the client's information and displays the updated details.

   Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The selected client is not in the displayed list.

  * 3a1. RESIDEX shows an error. No client records are changed.

  Use case resumes at step 3.

* 3b. The supplied information is invalid or no information to update is supplied.

  * 3b1. RESIDEX explains the input error. The client's information remains unchanged.

  Use case resumes at step 3.

* 3c. Updating the name or phone number would create a duplicate under the Add Client rule.

  * 3c1. RESIDEX reports the duplicate. The client's information remains unchanged.

  Use case resumes at step 3.

**Use case: UC05 — Delete selected client information**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests to view a listed client's profile.
4. RESIDEX displays the client's profile and associated properties.
5. Property agent requests to remove selected information, such as an interest or a specific associated property.
   To reset a recorded stage while keeping its property, the agent updates that property to Not specified instead.
6. RESIDEX removes the selected information and displays the updated profile. The client
   and their other information are retained.

   Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The selected client is not in the displayed list.

  * 3a1. RESIDEX shows an error.

  Use case resumes at step 3.

* 5a. The selected information or property is not recorded for the client.

  * 5a1. RESIDEX reports that the selected information is unavailable. The profile remains unchanged.

  Use case resumes at step 5.

* 5b. Property agent requests to remove a required detail, such as the name, phone number,
  email or purpose.

  * 5b1. RESIDEX explains that the required detail cannot be removed. The profile remains unchanged.

  Use case resumes at step 5.

**Use case: UC06 — Delete a client**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests to delete a listed client.
4. RESIDEX deletes the client and their stored profile information, reports the deleted client's
   details and displays the updated list.

   Use case ends.

**Extensions**

* 1a. Property agent requests to search for clients instead of listing all clients.

  * 1a1. Property agent <u>finds clients (UC02)</u>.

  Use case resumes at step 2 with the search results as the displayed list.

* 2a. The displayed list is empty.

  Use case ends.

* 3a. The client selection is missing or invalid, or more than one client is selected.

  * 3a1. RESIDEX explains the selection error. No client is deleted and the list remains unchanged.

  Use case resumes at step 3.

* 3b. The selected client is not in the displayed list.

  * 3b1. RESIDEX reports that the selected client is not in the displayed list.
    No client is deleted and the list remains unchanged.

  Use case resumes at step 3.

#### Requirements considered outside the MVP

The following requirements were considered during ideation and are outside the current MVP.
They are documented for future reference, without committing to their implementation.

**Use case: UC07 — Review a client's information history**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests the information history of a listed client.
4. RESIDEX displays the client's recorded information history for review.

   Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The selected client is not in the displayed list.

  * 3a1. RESIDEX shows an error.

  Use case resumes at step 3.

* 3b. No history is recorded for the client.

  * 3b1. RESIDEX reports that no history is available.

  Use case ends.

**Use case: UC08 — Review upcoming or overdue client appointments and follow-ups**

**MSS**

1. Property agent requests to view recorded upcoming or overdue client appointments and follow-ups.
2. RESIDEX displays the relevant entries with their dates and associated clients.
3. Property agent requests to view the client associated with an entry.
4. RESIDEX displays that client's profile so the agent can prepare to contact them.

   Use case ends.

**Extensions**

* 2a. No relevant appointments or follow-ups are recorded.

  * 2a1. RESIDEX reports that there are no matching entries.

  Use case ends.

* 3a. The selected entry is invalid.

  * 3a1. RESIDEX shows an error.

  Use case resumes at step 3.

**Use case: UC09 — Schedule a property viewing**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests to schedule a viewing for a listed client, specifying the property
   and the viewing date and time.
4. RESIDEX records the viewing and reports its details.

   Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The selected client is not in the displayed list, or the property, date or time is missing or invalid.

  * 3a1. RESIDEX explains the input error. No viewing is recorded.

  Use case resumes at step 3.

**Use case: UC10 — Attach a private client note**

**MSS**

1. Property agent requests to list clients.
2. RESIDEX displays the client list.
3. Property agent requests to attach a private note to a listed client.
4. RESIDEX confirms that the note has been attached. Its contents remain hidden from the client list.

   Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The selected client is not in the displayed list or the note is empty.

  * 3a1. RESIDEX explains the input error. No note is recorded.

  Use case resumes at step 3.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 clients without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  Should store all data locally in a human-editable JSON file, and work without an internet connection.
5.  Should not lose or overwrite existing client data when the data file cannot be loaded.

### Glossary

* **Active property listing**: A property listing recorded under a seller's client profile that is currently available for sale.
* **Associated property**: A property linked to a client's profile, such as a property the client is interested in or is selling.
* **Buyer**: A client who wants to purchase a property.
* **Buying**: A property intention describing a client's interest in purchasing that property.
* **Buying status**: How close a client is to agreeing on a property.
* **Client**: A person whose details a property agent stores in RESIDEX; their property-derived role can be Buyer, Seller, both or Not specified.
* **Client intention**: The role derived from the client's property directions; no properties means Not specified, and both directions means Buyer / Seller.
* **Client detail view**: The single-client display opened by `view INDEX`, showing every stored client and property field, including placeholders for missing optional values. The displayed client has index 1; `list` returns to all clients.
* **Client index**: A client's one-based position in the currently displayed list, including search results or the detail view. It is not a permanent identifier.
* **Client information history** _(planned, not yet implemented)_: Previously recorded information or changes associated with a client, available for review.
* **Client profile**: The stored information about a client, including contact details, remark and associated properties with their individual transaction stages.
* **Duplicate client**: A client whose normalised name and normalised phone number both match those of an existing client.
* **Filtered client list**: The clients displayed after `find` or `view`. Each `find` searches all stored clients; `view` selects one displayed client. `list` restores all clients in stored order.
* **Follow-up** _(planned, not yet implemented)_: A planned next contact with a client. A follow-up may be upcoming or overdue.
* **Intention**: Whether a client or property is on the buying or selling side.
* **Internal note** _(planned, not yet implemented)_: A note attached to a client profile that is not displayed in the client list. It does not imply additional access control or encryption.
* **Keyword**: A search term supplied after a `find` prefix. Whitespace separates keywords, and any matching keyword returns a client.
* **Lead**: A potential client or transaction that the property agent is currently pursuing
* **Mainstream OS**: Windows, Linux, Unix, or macOS.
* **Minimum Viable Product (MVP)**: The initial version of RESIDEX containing the features selected to meet its core user needs.
* **Prefix**: A field selector such as `n/` for a name or `l/` for a property location. The `find` command accepts exactly one selector.
* **Priority** _(planned, not yet implemented)_: An indication of a client's importance or urgency, assigned by the property agent to help decide whom to attend to first.
* **Private client note** _(planned, not yet implemented)_: A note attached to a client's profile whose contents are hidden from the client list.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Property agent**: A user who manages buyers and sellers, and who prefers typing commands to using the mouse.
* **Property bedrooms**: An optional whole number from 1 to 20 recording how many bedrooms a property has.
* **Property entry**: A record in a client's ordered property list that stores information about an associated property.
* **Property index**: The one-based position of a property entry within a selected client's property list. It is not a permanent identifier: deleting an earlier property shifts subsequent indexes down by one.
* **Property intention**: The Buying or Selling action recorded for one property; these actions determine the client's derived role.
* **Property listing**: A record containing information about a property available for sale or rent
* **Property location**: Nonblank descriptive text stored on a property entry, such as `Clementi Ave 3`. The `find l/` search checks these locations.
* **Property price**: An optional, positive whole-dollar amount recorded for a property.
* **Property type**: The HDB, Condominium or Landed category of a property entry.
* **Property status**: A property's current recorded transaction stage: Not specified, Enquiring, Viewing, Negotiating, Closing or Completed. It can be corrected in either direction and is not a client-level field.
* **Property viewing** _(planned, not yet implemented)_: An appointment for a client to inspect a property
* **Purpose**: A client's buying or selling needs, represented by property intentions and summarised by the derived client role.
* **Redo**: Re-applying a change that `undo` reversed.
* **Remark**: Text recorded for a client using the `remark` command and displayed on the client card.
* **Requirement**: A condition a client wants in a property, such as preferred location, property type, or number of bedrooms.
* **Seller**: A client who wants to sell a property.
* **Selling**: A property intention describing a client's interest in selling that property.
* **Substring matching**: Matching text that contains a keyword anywhere, ignoring case; for example, `clementi` matches both `Clementi Ave 3` and `Woodlands Clementi`.
* **Transaction**: The process of completing a property sale or rental
* **Undo**: Reversing the most recent data-changing command. Up to 100 changes can be undone while RESIDEX is running.
* **Viewing**: A property stage covering a buying client's assessment visit or the agent's visit to assess a seller's property before representation and marketing. It does not record an appointment date.


--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder that you can write to.

   1. Open a terminal in that folder and run `java -jar Residex.jar`.<br>
      Expected: The GUI opens with a set of sample clients. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app with `java -jar Residex.jar` from the same folder.<br>
       Expected: The most recent window size and location are retained.

### Viewing command help

1. Opening and reopening help

   1. Test case: `help`<br>
      Expected: A separate, resizable window titled `RESIDEX Help` opens with command-reference cards.

   1. With help already open, enter `help` again in the main window.<br>
      Expected: The existing help window is focused; no second help window is created.

   1. Close help, focus the main command box and press `F1`.<br>
      Expected: The same command-reference window opens.

1. Reading the reference

   1. Scroll through the cards.<br>
      Expected: Cards for `add`, `edit`, `addp`, `delp`, `editp`, `find`, `list`, `view`, `delete`, `remark`, `help` and `exit` show their formats, details and examples.
      Client `add` and `edit` have no budget option; property commands retain `pr/PRICE` and transaction status.
      The heading and User Guide footer remain visible while the cards scroll.

   1. Resize the window narrower and wider.<br>
      Expected: Long text and examples wrap inside their cards. The cards can be read without horizontal scrolling.

1. Copying the guide URL

   1. Click **Copy URL** and paste the clipboard contents into a text editor.<br>
      Expected: `https://ay2627s1-cs2103t-w11-4.github.io/tp/UserGuide.html` is pasted.

### Viewing complete client details

Prerequisite: At least two clients exist. Choose one with a remark and multiple properties,
including properties with and without a price or bedroom count.

1. Run `list`, then `view 2`.<br>
   Expected: Only the second client is shown, now at index 1, under **Client details**.
   The client's name is the heading; phone, email, intention and remark are labelled below it.
   Every property is numbered in order
   and shows type, intention, location, status, price and bedrooms.
   An absent price or bedroom count reads `Not recorded`.
   The line `Type list to return to the original list` appears above the details.
2. Resize the window and scroll through the details.<br>
   Expected: Long field values wrap and every field, including each Bedrooms row, remains accessible by scrolling.
3. Run `view 2`, `view 0`, `view`, and `view 1 extra`.<br>
   Expected: Each command reports an error and preserves the current details and stored data.
4. Run `view 1`, then `list`.<br>
   Expected: Viewing again succeeds; `list` restores all clients in their previous order and hides the hint.
5. Run a `find` command, then `view 1`.<br>
   Expected: The first search result is shown. Run `list`; all stored clients return, including those
   outside the earlier search results.
   Run `view 1`, followed by `addp 1 t/HDB i/Buying l/Bishan`, `editp 1 1 l/Tampines`, and `delp 1 1`.<br>
   Expected: After each property command the updated details and return hint remain visible.
   Run `delete 1`.<br>
   Expected: All remaining clients appear under **All clients**, with the return hint hidden.
   Run `find e/@`, `find n/Nonexistent`, and `list`.<br>
   Expected: Both searches show **Filtered clients**, even if everyone or nobody matches;
   `list` restores **All clients** and the complete list.
6. View a client with no remark or properties.<br>
   Expected: Remark reads `Not recorded`, and properties read `No properties recorded`.

### Detecting duplicate clients

Prerequisite: Use a fresh data folder with no clients named Jordan Lee or Taylor Lim.

1. Adding clients

   1. Test case: `add n/Jordan Lee p/91001001 e/jordan@example.com`<br>
      Expected: Jordan is added.

   1. Test case: `add n/JORDAN   LEE p/91001001 e/other@example.com`<br>
      Expected: The error identifies the existing client as `Jordan Lee (91001001)` and explains that both name and phone match.
      It also explains that name matching ignores capitalisation and extra spaces.
      No client is added.
      Capitalisation, spacing and email do not distinguish clients with the same name and phone.

   1. Test case: `add n/Jordan Lee p/91001002 e/anotherjordan@example.com`<br>
      Expected: A second Jordan is added because the phone number differs.

   1. Test case: `add n/Taylor Lim p/91001001 e/taylor@example.com`<br>
      Expected: Taylor is added because the name differs, even though the phone number matches the first Jordan.

1. Editing a client in search results

   1. Test case: `find p/91001002`, followed by `edit 1 p/91001001`<br>
      Expected: The duplicate-client error is shown, even though the first Jordan is outside the search results.
      It identifies the stored client as `Jordan Lee (91001001)` and explains the name-matching rule.
      The second Jordan keeps phone number `91001002` and the saved data is unchanged.

   1. Test case: `edit 1 n/JORDAN   LEE`<br>
      Expected: The selected client's name changes successfully without being treated as a duplicate of itself.

1. Saving clients with the same name

   1. Exit and relaunch the app, then enter `find n/jordan`.<br>
      Expected: Both Jordan clients remain, with phone numbers `91001001` and `91001002`.

### Managing client properties

Prerequisite: Use a fresh data folder without a client named Property Test Client.
Run these steps in order; client indexes below refer to the filtered list.

1. Adding properties

   1. Enter `add n/Property Test Client p/91009001 e/propertytest@example.com`, then `find p/91009001`.<br>
      Expected: Exactly one client is shown, with displayed index 1.
      Their intention is Not specified because they have no properties.

   1. Enter `ADDP 1 T/h I/buying L/Clementi Ave 3 PR/800000`.<br>
      Expected: Property 1 appears under this client as HDB, Buying, Clementi Ave 3, $800,000, Not specified.
      The client filter remains active and the client's role becomes Buyer.

   1. Enter `addp 1 t/c i/Selling l/Bishan s/Viewing`.<br>
      Expected: Property 2 appears as Condominium, Selling, Bishan, Viewing with no price.
      The client's derived intention becomes Buyer / Seller.

1. Editing and clearing price

   1. Enter `editp 1 2 l/Toa Payoh pr/1500000`.<br>
      Expected: Only property 2's location and price change; its type, intention, Viewing status and position are retained.

   1. Enter `editp 1 2 pr/`.<br>
      Expected: Property 2's price is removed; all other fields remain unchanged.

   1. Enter `editp 1 2 bd/3`, then `editp 1 2 bd/`.<br>
      Expected: Property 2 shows 3 bedrooms, then the bedrooms are removed. Try `bd/0`, `bd/21` and `bd/abc`; each is rejected and nothing changes.

   1. Enter `editp 1 2 i/BUYING`, then `editp 1 2 i/sElLiNg`.<br>
      Expected: Property 2 changes from Selling to Buying and back to Selling.
      Its type, Toa Payoh location, unknown price and position remain unchanged.
      Property 1 is retained; the role changes to Buyer and back to Buyer / Seller.

   1. Enter `editp 1 2 l/Toa Payoh`.<br>
      Expected: The feedback reports an unchanged property and the original entry is retained.

1. Rejecting invalid requests

   1. Try `addp 1 t/HDB i/Buyer l/Bishan`, `addp 1 t/HDB i/Both l/Bishan`,
      `editp 1 2 i/Seller`, `editp 1 2 i/`, and `editp 1 2 i/Buying I/Selling`.<br>
      Expected: Legacy client-role values, blank intentions and duplicate prefixes are rejected.
      The client list, property entries, active filter and saved file remain unchanged.

   1. Try `editp 1 2 l/`, `editp 1 2 l/Bishan pr/1.5`, `editp 1 2 l/Bishan L/Tampines`, and `addp 1 t/HDB i/Buying l/Bishan p/800000`.<br>
      Expected: Each command reports an input error; no property or saved data is changed.
      Price uses `pr/`, not `p/`.

   1. Try `addp 2 t/HDB i/Buying l/Bishan`, `editp 1 99 l/Bishan`, and `delp 1 0`.<br>
      Expected: Invalid client/property indexes are rejected without modifying any client or property.

1. Deleting and saving

   1. Enter `delp 1 1`.<br>
      Expected: The Clementi property is removed; the Toa Payoh property becomes property 1.
      The client remains in the phone-filtered list and their role becomes Seller.

   1. Exit and relaunch the app, then enter `find p/91009001`.<br>
      Expected: The single Condominium/Selling/Toa Payoh/Viewing entry remains, without a price.
      The client's contact details are retained; the role is derived as Seller.

1. Loading legacy property intentions

   1. Exit the app and back up its data file. In the remaining property's JSON object, change only
      its `"intention": "Selling"` field to `"intention": "Seller"`, then relaunch.<br>
      Expected: The property displays Selling with its other fields unchanged; the role is derived as Seller.
      No client-level intention field needs to be added.

   1. After each legacy load, enter `list`, exit and inspect the saved file.<br>
      Expected: The property's intention uses canonical Selling or Buying respectively.
      Relaunch and verify that the same property details and derived Seller role are retained.
      Repeat these two steps with legacy property `"intention": "Buyer"`; expect Buying on display and save,
      and a derived Buyer role. Other client fields remain unchanged.

   1. Exit the app, back up the valid file, and change only the property's intention to `"Both"`.
      Relaunch and inspect the load error and file before entering any commands.<br>
      Expected: Loading fails through the normal data-loading error path and the read leaves the file unchanged.
      Missing, blank or noncanonical property intentions such as `"buying"` must also fail.
      Close the app without issuing commands and restore the valid backup before continuing.

### Deriving and finding client intentions

Prerequisite: Use a fresh data folder without clients named Role Mixed, Role Buyer, Role Seller or Role Empty.
Run the steps in order; use the displayed indexes after each search.

1. Preparing clients in source order

   1. Enter `add n/Role Mixed p/91008001 e/mixed@example.com`, then `find p/91008001`.<br>
      Expected: The new client has intention Not specified.

   1. Enter `addp 1 t/HDB i/Buying l/Clementi`, then `addp 1 t/Condominium i/Selling l/Bishan`.<br>
      Expected: The role changes to Buyer after the first addition and Buyer / Seller after the second.

   1. Enter `add n/Role Buyer p/91008002 e/buyer@example.com`, then `find p/91008002`,
      then `addp 1 t/HDB i/Buying l/Clementi`.<br>
      Expected: This client's role is Buyer.

   1. Enter `add n/Role Seller p/91008003 e/seller@example.com`, then `find p/91008003`,
      then `addp 1 t/Condominium i/Selling l/Bishan`.<br>
      Expected: This client's role is Seller.

   1. Enter `add n/Role Empty p/91008004 e/empty@example.com`, then `find n/Role`.<br>
      Expected: Role Mixed, Role Buyer, Role Seller and Role Empty appear in that source order.

1. Finding and restoring normal order

   1. Enter `find i/BUYER`.<br>
      Expected: Role Buyer is displayed first, then Role Mixed. Role Empty is excluded.

   1. Enter `find i/seller`.<br>
      Expected: Role Seller is displayed first, then Role Mixed.

   1. Enter `find I/bOtH`.<br>
      Expected: Only Role Mixed appears, once.

   1. Try `find i/unspecified`.<br>
      Expected: An input error; the previous Role Mixed result is unchanged.

   1. Enter `find n/Role`, then repeat `find i/buyer` followed by `list`.<br>
      Expected: The ordinary find returns all four Role clients in source order, and list restores the
      full source order. Intention searches do not change the saved order.

1. Checking displayed-index targeting and automatic updates

   1. Enter `find i/buyer`, then `edit 1 n/Role Buyer Updated`.<br>
      Expected: The Buyer client is renamed; Role Mixed is untouched even though it is earlier in source order.
      Client edit returns to the full list, so run the search again before using its indexes.

   1. Enter `find i/buyer`, then `addp 1 t/Condominium i/Selling l/Tampines`.<br>
      Expected: Role Buyer Updated becomes Buyer / Seller. Role Mixed is now displayed before it because
      both matches retain source order. Property commands keep the filter active.

   1. Enter `editp 1 1 i/Selling`.<br>
      Expected: This edits Role Mixed, the currently displayed client 1. That client becomes Seller and
      disappears from buyer results; Role Buyer Updated remains visible.

   1. Enter `delp 1 2`.<br>
      Expected: Only Role Buyer Updated's selling property is removed and its role becomes Buyer.
      Enter `delp 1 1` to remove its final property; it becomes Not specified and leaves buyer results.
      Enter `find n/Role` to verify that both clients still exist with the expected remaining properties.

1. Rejecting direct client intention and retaining persistence compatibility

   1. Try `edit 1 i/Buyer`, `edit 1 n/Changed i/Both`, and
      `add n/Rejected Role p/91008005 e/rejected@example.com i/Seller`.<br>
      Expected: Each command explains that client intention is derived. Names, properties, filter and saved data
      remain unchanged; no Rejected Role client is added.

   1. Exit and back up `data/addressbook.json`. Add an old client-level `"intention": "Buyer"` field to
      Role Empty, then relaunch and enter `find p/91008004`.<br>
      Expected: Role Empty remains Not specified; no property is invented from the old field.
      The same test with legacy Seller has the same result.

   1. Enter `list`, exit and inspect the saved JSON.<br>
      Expected: Client-level intention is absent. Property intentions remain present, and property and client
      order is preserved. Relaunch to verify all roles are derived from the same saved properties.

### Recording and finding property transaction stages

Prerequisite: Use a separate fresh data folder for this scenario, without Stage First, Stage Second or Stage Empty.
Run these steps in order. Client indexes always refer to the current displayed list.

1. Preparing independent property stages

   1. Enter `add n/Stage First p/91007001 e/first@stage.example.com`,
      then `find p/91007001`.<br>
      Expected: One client with no properties. Enter `find s/unspecified`; Stage First is excluded
      because no property exists yet. Use `find p/91007001` again before adding properties.

   1. Enter `addp 1 t/HDB i/Buying l/Clementi pr/800000`,
      then `addp 1 t/Condominium i/Selling l/Bishan s/Viewing`.<br>
      Expected: Property 1 is Not specified and property 2 is Viewing. The client role becomes Buyer / Seller
      and stages belong to properties independently.

   1. Enter `add n/Stage Second p/91007002 e/second@stage.example.com`, then `find p/91007002`.
      Enter `addp 1 t/HDB i/Selling l/Tampines pr/950000 s/Viewing`,
      then `addp 1 t/HDB i/Selling l/Pasir Ris s/viewing`.<br>
      Expected: Both properties are Viewing, with their own locations and optional prices.

   1. Enter `add n/Stage Empty p/91007003 e/empty@stage.example.com`, then `find n/Stage`.<br>
      Expected: Stage First, Stage Second and Stage Empty appear in that source order; Stage Empty has no properties.

1. Searching stages and replacing filters

   1. Enter `find S/VIEWING`.<br>
      Expected: Stage First then Stage Second, each once even though Stage Second has two matching properties.

   1. Enter `find s/unspecified`, then `find s/Not specified` and `find s/not SPECIFIED`.<br>
      Expected: Only Stage First. Stage Empty is excluded because it has no properties.

   1. Try `find s/enquiring`, `find s/negotiating`, `find s/closing` and `find s/completed`.<br>
      Expected: Empty results before any such stage has been recorded.

   1. Enter `find n/Stage`, `find e/stage.example.com`, `find p/91007` and `list` in turn.<br>
      Expected: Ordinary searches restore the three Stage clients in source order; list restores the full list.
      Repeat `find s/viewing` after each: it searches all clients again and returns Stage First then Stage Second.

1. Checking displayed indexes, duplicate matches and corrections

   1. Enter `find s/viewing`, then `editp 2 1 s/Negotiating`.<br>
      Expected: This edits Stage Second, displayed client 2, property 1. It still appears once because
      its property 2 remains Viewing. Stage First and both properties' other fields are unchanged.

   1. Enter `editp 2 2 s/Negotiating`.<br>
      Expected: Stage Second leaves Viewing results; only Stage First is displayed. A subsequent
      `editp 2 1 s/Closing` is rejected because displayed client 2 no longer exists.

   1. Enter `delp 1 2`.<br>
      Expected: Stage First's Viewing property is removed and Viewing results become empty.
      The client and its Not specified property remain stored.

   1. Enter `find s/negotiating`, then `editp 1 2 s/Viewing`.<br>
      Expected: Only Stage Second is selected. The backward correction succeeds; it still matches Negotiating
      through property 1 and also matches Viewing through property 2 when that status is searched.

   1. Enter `editp 1 1 s/Closing`, then `find s/closing`.<br>
      Expected: Stage Second is the only Closing result, in normal source order.

   1. Enter `editp 1 1 s/Completed`, then `find p/91007002`.<br>
      Expected: Both properties and the client still exist. Property 1 is Completed; property 2 remains Viewing.
      No automatic deletion, archive or lock occurs.

   1. Enter `editp 1 1 l/Tampines Central`, then `editp 1 1 s/Enquiring`.<br>
      Expected: Completed remains editable. The location edit preserves Completed, then the stage correction
      succeeds. The $950,000 price, other property, client details and source position remain unchanged.

   1. Enter `editp 1 1 s/unspecified`, then `editp 1 1 s/UNSPECIFIED`.<br>
      Expected: The first resets the stage to Not specified; the second reports unchanged.
      The property remains present. Stage First and Stage Empty are unaffected.

1. Rejecting invalid status input

   1. While Stage Second is phone-filtered, try `editp 1 1 s/`, `editp 1 1 s/unknown`,
      `editp 1 1 s/not-specified`, `editp 1 1 s/Viewing S/Closing`,
      and `addp 1 t/HDB i/Selling l/Bedok s/`.<br>
      Expected: Each input error leaves the properties, displayed filter and saved data unchanged.

   1. Try `find s/`, `find s/unknown`, `find s/Not specified extra`, `find s/viewing s/closing`
      and `find s/viewing n/Stage`.<br>
      Expected: Input errors. The previous phone-filtered client remains displayed.

1. Saving, legacy defaults and invalid persisted data

   1. Exit and relaunch, then enter `find n/Stage`.<br>
      Expected: All three clients remain in source order. Stage First has its one Not specified property;
      Stage Second has Not specified/Tampines Central/$950,000 then Viewing/Pasir Ris/no price;
      Stage Empty still has no properties.

   1. Exit and back up `data/addressbook.json`. Remove the `status` field from Stage Second's property 1,
      and set property 2's status to JSON `null`. Relaunch and inspect the file before entering any command.<br>
      Expected: Both properties load as Not specified, and loading alone leaves file contents unchanged.
      Enter `find p/91007002`, exit and inspect the saved file: both statuses are now canonical `UNSPECIFIED`.
      Relaunch to verify the same property order and unrelated client fields.

   1. Exit, retain a valid backup and replace one property's status with `"unknown"`. Relaunch.<br>
      Expected: Loading reports an invalid status through the normal data-loading error path; the file is unchanged.
      Do not enter commands after a failed load on this version. Restore the valid backup before continuing.

The automated logging test checks FINE diagnostics for successful status changes and their absence for unchanged,
unrelated or rejected edits. Normal INFO logging does not show these diagnostics.

### Finding clients by property type and price

1. Preparing the data

   1. Enter `add n/Type Test p/91008001 e/typetest@example.com`, then `find p/91008001`.<br>
      Expected: One client is shown at index 1.

   1. Enter `addp 1 t/HDB i/Buying l/Clementi pr/600000`, then `addp 1 t/Landed i/Selling l/Sentosa pr/3000000`,
      then `addp 1 t/Condominium i/Buying l/Bishan`.<br>
      Expected: Three properties are added. The Bishan condominium has no price.

1. Searching by type

   1. Enter `list`, then `find t/HDB` and `find T/h`.<br>
      Expected: Type Test is listed once for each search.

   1. Enter `find t/Landed`, `find t/condominium` and `find t/c`.<br>
      Expected: Type Test is listed each time. Searches for a type that no client has show no results.

   1. Try `find t/`, `find t/villa`, `find t/HDB Landed` and `find t/HDB n/Type`.<br>
      Expected: Each is rejected with an error and the displayed list is unchanged.

1. Searching by price

   1. Enter `find pr/500000-700000`.<br>
      Expected: Type Test is listed, because of the $600,000 HDB. The list uses normal client order.

   1. Enter `find pr/2000000-`, then `find pr/-650000`.<br>
      Expected: Type Test is listed for both. Try `find pr/700000-800000`: Type Test is not listed.

   1. Enter `find pr/600000-600000`.<br>
      Expected: Type Test is listed, because both bounds are inclusive.

   1. Try `find pr/`, `find pr/-`, `find pr/800000-500000`, `find pr/abc`, `find pr/1,000-2,000` and `find pr/500000`.<br>
      Expected: Each is rejected with an error and the displayed list is unchanged.

   1. Enter `addp 1 t/HDB i/Buying l/Yishun`, then `find pr/1-`.<br>
      Expected: The property without a price never matches by itself; Type Test appears once because of its priced
      properties.

### Finding clients by number of bedrooms

1. Preparing the data

   1. Enter `add n/Bedroom Test p/91008101 e/bedroomtest@example.com`, then `find p/91008101`.<br>
      Expected: One client is shown at index 1.

   1. Enter `addp 1 t/HDB i/Buying l/Clementi bd/3`, `addp 1 t/Condominium i/Buying l/Bishan bd/2` and
      `addp 1 t/Landed i/Selling l/Sentosa`.<br>
      Expected: Three properties are added. The Sentosa property has no number of bedrooms.

1. Searching

   1. Enter `list`, then `find bd/3`, `find bd/2` and `find BD/3`.<br>
      Expected: Bedroom Test is listed each time, once.

   1. Enter `find bd/4`.<br>
      Expected: Bedroom Test is not listed, because no property has exactly 4 bedrooms.

   1. Enter `editp 1 1 bd/`, then `find bd/3`.<br>
      Expected: Bedroom Test is not listed, because the only 3-bedroom property no longer has bedrooms recorded.

   1. Try `find bd/`, `find bd/0`, `find bd/21`, `find bd/abc`, `find bd/2 3` and `find bd/2 t/HDB`.<br>
      Expected: Each is rejected with an error and the displayed list is unchanged.

### Rejecting removed client budgets

1. Enter `add n/Budget Test p/95678901 e/budgettest@example.com b/800000`.<br>
   Expected: RESIDEX explains that client budgets are no longer supported. No client is added.
2. With a client displayed, enter `edit 1 n/Changed Name B/900000`.<br>
   Expected: The same error is shown. The client's name, properties and derived role remain unchanged.
3. Add or edit a property using `pr/800000 s/Viewing` and restart the app.<br>
   Expected: The property price and status are retained. There is no separate client amount.
4. Back up a valid file and add an obsolete `"budget": "900000"` field to a client, then restart.<br>
   Expected: All clients, property prices, statuses and derived roles load. After the next successful command
   saves the file, the obsolete client field is absent and property values are unchanged.

### Deleting a client

1. Deleting a client while all clients are being shown

   1. Prerequisites: List all clients using the `list` command, with multiple clients in the list.

   1. Test case: `delete 1`<br>
      Expected: The first client is deleted from the list. The status message shows the deleted client's details.

   1. Test case: `delete 0`<br>
      Expected: No client is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Undoing and redoing changes

1. Undoing and redoing a deleted client

   1. Prerequisites: Enter `list` and note the first client's name.

   1. Enter `delete 1`, then `undo`.<br>
      Expected: The first client is back in their original position, and all clients are shown.

   1. Enter `redo`.<br>
      Expected: The client is deleted again.

1. Undoing property and client changes

   1. Enter `addp 1 t/HDB i/Buying l/Clementi`, `editp 1 1 l/Bishan` and `remark 1 r/hello`.
      Then enter `undo` three times.<br>
      Expected: Each `undo` reverses one command, newest first. After the third, the client has no property and no remark.

   1. Enter `redo` three times.<br>
      Expected: The three changes are re-applied in order.

1. Commands that are not undoable steps

   1. Enter `list`, `find n/a`, `view 1` and `help`, then `undo`.<br>
      Expected: These commands do not create undo steps. `undo` reverses the last data-changing command, or reports
      "There is nothing to undo." if there was none since launch.

   1. Enter `edit 1 n/<the first client's current name>`, then `undo`.<br>
      Expected: An edit that changes nothing does not create an undo step.

1. Errors and the redo history

   1. Immediately after launching RESIDEX, enter `undo` and `redo`.<br>
      Expected: "There is nothing to undo." and "There is nothing to redo." Nothing changes.

   1. Enter `delete 1`, `undo`, then `remark 1 r/new`, then `redo`.<br>
      Expected: `redo` reports "There is nothing to redo.", because the new change cleared the redo history.

1. Saving and restarting

   1. Enter `delete 1`, `undo`, then close and relaunch RESIDEX.<br>
      Expected: The restored client is still present. Enter `undo`: it reports that there is nothing to undo,
      because the undo history is not saved.

### Saving data

Use a copy of the application in a separate folder for these tests, and keep a backup of its
`data/addressbook.json` file. Close RESIDEX before changing the file.

1. Failed loading:
   1. Replace the file contents with `invalid JSON`, then launch RESIDEX.
      Expected: An error dialog identifies the file, explains that it has not been changed, and gives
      recovery instructions. Closing the dialog exits RESIDEX without opening the main window.
   2. Open the file in a text editor.
      Expected: It still contains `invalid JSON`.
   3. Repeat with `null`, `{"persons": [null]}`, and a client with an invalid field value.
      Expected: The same error and file-preservation behavior.

2. Recovery:
   1. Restore the valid backup and restart RESIDEX.
      Expected: The saved clients appear. Changes made with commands persist after restarting.
   2. Close RESIDEX, rename the data file to `addressbook.backup.json`, then restart.
      Expected: Sample clients appear. Run `list`; a new data file is saved while the renamed file stays unchanged.

3. Valid empty data:
   1. Close RESIDEX and set the data file contents to `{"persons": []}`.
   2. Restart, then add a client using `add n/Alice p/91234567 e/alice@example.com`.
      Expected: RESIDEX starts normally with an empty list, and the added client persists after restarting.
