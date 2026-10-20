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

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
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
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
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
method shows or focuses a single `HelpWindow`. The `F1` shortcut and Help menu use the same method.

`HelpContent` holds each command's description, syntax, notes and examples, using shared command words,
prefixes and validation messages where appropriate. `HelpWindow` creates a `CommandHelpCard` for each entry.
The cards are display-only and sit inside a scrolling pane, with the heading and User Guide footer outside it.
The layout and styles are defined in `HelpWindow.fxml`, `CommandHelpCard.fxml` and `HelpWindow.css`.

The reference is bundled with the app and works offline. The footer's Copy URL button copies the RESIDEX User Guide URL.
Update `HelpContent` and the User Guide together when commands change. `HelpContentTest` checks that the documented
examples parse as the intended commands without executing them or starting JavaFX.

### Find command

`FindCommandParser` accepts exactly one of the shared `CliSyntax` prefixes: `n/`, `e/`, `p/`, `l/`, or `i/`.
For example, `find n/Alice Bob` searches names; prefix letters are case-insensitive.
Name, email, phone, and location searches use case-insensitive substring matching with OR semantics across keywords.
`LocationContainsKeywordsPredicate` checks every entry in `Person#getProperties()` and returns a client once if any
property location contains any keyword. Clients without properties do not match a location search.
For example, `find l/clementi` matches clients with locations such as `Clementi Ave 3` or `Woodlands Clementi`.
Intention searches accept exactly one value, `buyer` or `seller`, case-insensitively.
Missing keywords, repeated or mixed prefixes, and unsupported selectors are rejected.
`FindCommand` replaces the filter on the complete client list without modifying stored clients.

### Duplicate client feedback

`AddCommand` and `EditCommand` search the complete stored client list using `Person#isSamePerson(Person)` when checking for a conflict.
This keeps the name-and-phone identity rule unchanged and finds conflicting clients outside the current filter.
An edit that retains the selected client's identity is allowed without treating that client as its own duplicate.
Both commands use the shared `Messages.MESSAGE_DUPLICATE_PERSON` format to identify the stored client's name and phone number and explain that name matching ignores capitalisation and extra spaces.
The feedback does not use an index from the unfiltered list, since it could identify a different client in the displayed results.
Conflicts throw `CommandException` before any model update or save occurs.

### Budget feature

#### Implementation

A client's budget is optional.
`Budget` stores a non-negative whole-dollar amount.
`BudgetBand` classifies that amount.

The bands are:

* **Low:** $800,000 and below.
* **Middle:** above $800,000, up to and including $2,500,000.
* **High:** above $2,500,000.

`add` and `edit` accept the amount with the `b/` prefix.
Omitting `b/` on `add` stores no budget.
Omitting `b/` on `edit` keeps the client's current budget.
An invalid amount, such as a decimal, a comma, or a leading zero, is rejected and the stored client is left unchanged.

`JsonAdaptedPerson` writes the amount as a whole-dollar string.
A saved client with no `budget` field loads with no budget.
`PersonCard` shows the amount on the right of the card.
Low is red, Middle is yellow, and High is green.
A client with no budget has no amount on the card.

`RemarkCommand` copies the existing budget onto the updated client, so adding a remark does not remove it.

#### Design considerations

**Aspect: Whether every client must have a budget**

* **Alternative 1 (current choice):** The budget is optional.
  * Pros: Sellers and existing saved files can omit it. Old data files still load.
  * Cons: The card does not show an amount for every client.
* **Alternative 2:** Require a budget in every `add` command.
  * Pros: Every card shows a coloured amount.
  * Cons: Existing data files without a budget cannot be loaded, and a seller must be given an amount that may not mean anything.

**Aspect: How the amount is stored**

* **Alternative 1 (current choice):** Store the whole-dollar amount and derive the colour from `BudgetBand`.
  * Pros: The user can enter any valid amount. The colour stays consistent with the thresholds.
  * Cons: The colour is not stored, so changing a threshold changes the colour of existing clients.
* **Alternative 2:** Store the colour the user picks.
  * Pros: The displayed colour stays as the user left it.
  * Cons: The colour can disagree with the amount.

### Client property model

The property model, storage, numbered client-card display and `addp`, `delp` and `editp` commands are implemented.
Existing client `add` and `edit` commands retain their syntax; property fields belong to the separate property commands.

#### Fields and validation

`Property` is an immutable value object in `seedu.address.model.property`.
Each entry describes one client's interest in buying or selling a property.

| Field | Type | Rules |
| --- | --- | --- |
| Type | `PropertyType` | Required: `HDB`, `CONDOMINIUM`, or `LANDED`. `parse` accepts full names and `h`, `c`, or `l`, ignoring case and surrounding whitespace. |
| Intention | `Intention` | Required: `BUYER` or `SELLER`. This belongs to the property and is independent of the client's existing intention. `Intention.parse` accepts exactly `Buyer` or `Seller`. |
| Location | `PropertyLocation` | Required, nonblank text. Surrounding whitespace is removed; case and internal whitespace are preserved. |
| Price | `PropertyPrice` | Optional. A supplied price must be a positive whole-dollar SGD amount up to `Long.MAX_VALUE`, without commas, decimals, leading zeros, or surrounding whitespace. |

Use the three-argument `Property` constructor when the price is unknown, or pass `null` as the fourth argument.
`getPrice()` returns `Optional.empty()` for an unknown price. Zero is invalid for a property price.
The existing client budget remains a separate field with its existing validation.

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
`PropertyParserUtil` recognises `t/`, `i/`, `l/` and `pr/` case-insensitively without changing field values.
Unknown and repeated fields are rejected, including repeated prefixes with different capitalisation.
Whitespace-separated alphabetic words ending in `/` are reserved as prefixes and cannot be used as literal location text.
Property type aliases and Buyer/Seller values ignore case in property commands; the underlying property intention enum remains unchanged.

`addp CLIENT_INDEX t/TYPE i/INTENTION l/LOCATION [pr/PRICE]` requires a type, intention and nonblank location.
The parser validates every field before constructing a complete `Property`; an omitted price is unknown.
A supplied empty price is invalid when adding.
`delp CLIENT_INDEX PROPERTY_INDEX` accepts exactly two positive indexes and no field arguments.
`editp CLIENT_INDEX PROPERTY_INDEX [t/TYPE] [i/INTENTION] [l/LOCATION] [pr/PRICE]` requires at least one field.
`EditPropertyDescriptor` copies the requested edits and distinguishes an omitted price from an explicitly cleared price.
An empty `pr/` clears the price on edit, while empty required fields are rejected.

`PropertyCommandUtil` resolves the client from `model.getFilteredPersonList()` and validates the property index within that client's list.
Both indexes use the existing `Index` class and errors are reported through `CommandException`.
Commands preserve the active client filter and all unrelated fields.
`EditPropertyCommand` constructs a replacement using the current values for omitted fields.
Only after successful validation does it call `model.setPerson` once.
An equal replacement reports no change and leaves the original `Person` in the model.
Invalid parses or indexes leave both the model and saved data unchanged.

The add-property command applies the immutable model update after validating the displayed client index:

```java
Property property = new Property(
        PropertyType.HDB, Intention.BUYER, new PropertyLocation("Clementi Ave 3"));
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
The original six-argument `Person` constructor creates a client with no properties; the seven-argument constructor
accepts the property list as its final argument.

#### Storage and compatibility

Each saved client has a `properties` array. `JsonAdaptedProperty` stores `type`, `intention`, and `location` as strings,
and `price` as a whole-dollar string or `null` when unknown. Property order and duplicate entries survive saving and
loading. A missing `price` also loads as unknown.

`JsonAdaptedPerson` treats a missing or null `properties` field as an empty list, so valid v1.3 files load without
migration. Null property entries, missing required fields, and invalid values are rejected with `IllegalValueException`;
the error includes the property's one-based index. File loading reports these errors as `DataLoadingException`.
Invalid entries are not silently discarded.

The regression tests cover field validation, case-insensitive parsing, price clearing, index changes, independent clients and preservation through client edits and remarks.
Integration tests execute command strings through the real parser, model and JSON storage, checking unchanged data after rejected operations and persistence after valid operations.
Old-file compatibility and property-list immutability are also covered.

#### Property display

`PersonCard` renders one wrapped label per property, using its one-based list position and `Property#toDisplay()`.
The property list is hidden when empty, and `PersonListPanel` measures each card at its available width so longer locations expand the row.
Type, property intention, location and a known price are displayed together; an unknown price has no amount.
`HelpContent` includes the three property commands and its examples are parsed by `HelpContentTest`.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current client data state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous client data state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone client data state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial client data state, and the `currentStatePointer` pointing to that single client data state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in RESIDEX. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the client data after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted client data state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified client data state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the client data state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous client data state, and restores the client data to that state.

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

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest client data state, then there are no undone client data states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the client data, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `delete 1`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all client data states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

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
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
   addp 1 t/HDB i/Buyer l/Clementi pr/800000
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
* tracks property viewings, follow-ups, leads, and ongoing transactions
* prefers fast CLI-based interactions for managing information
* needs to filter clients by categories such as buyer, seller, tenant, or landlord

**Value proposition**: RESIDEX provides property agents with a fast and organised way to manage buyers, sellers, property listings, viewings, and follow-ups in one place. Its efficient CLI-based commands allow agents to quickly retrieve, update, and filter information, reducing administrative work and helping them organise clients by category.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

The general user story for updating client information is an epic that is further refined by the more specific user stories below.

| Priority | As a …                              | I want to …                                                | So that I can …                                               |
|----------|--------------------------------------|------------------------------------------------------------|----------------------------------------------------------------|
| `* * *`  | property agent who types quickly     | perform client-management actions through a CLI            | complete routine tasks quickly                                |
| `* * *`  | property agent                       | add a new client                                           | keep track of the people I serve                              |
| `* * *`  | property agent                       | record whether a client is a buyer or seller               | manage the client according to their property needs           |
| `* * *`  | property agent                       | view all the clients I am currently serving                | keep track of my active clients                               |
| `* * *`  | property agent managing many clients | search for clients by name, phone number, or email         | retrieve their information quickly                            |
| `* * *`  | property agent                       | filter clients by category, buying status, property requirements, or priority | focus on clients relevant to my current task       |
| `* * *`  | property agent                       | view a client's profile                                    | review their contact details, requirements, status, priority, and property information |
| `* * *`  | property agent                       | update a client's information, such as interests or status | keep the client's record accurate                             |
| `* * *`  | property agent                       | record a client's contact details                          | contact the client when needed                                |
| `* * *`  | property agent                       | record a client's preferred property type and location     | recommend properties that match the client's preferences      |
| `* * *`  | property agent                       | record a buyer's budget                                    | recommend properties within the buyer's budget                |
| `* * *`  | property agent                       | record a buyer's preferred number of bedrooms              | exclude properties that do not meet the buyer's requirements |
| `* * *`  | property agent                       | record property information under a client's profile       | keep track of the properties relevant to that client         |
| `* * *`  | property agent                       | remove selected optional information from a client's profile | correct outdated information without deleting the client   |
| `* * *`  | property agent                       | assign priorities to clients                               | determine which clients to focus on                           |
| `* * *`  | property agent                       | set or update a client's next follow-up date               | know when I should contact the client again                   |
| `* * *`  | property agent                       | view upcoming and overdue follow-ups                       | avoid missing important follow-up actions                     |
| `* * *`  | property agent                       | view a client's recorded information history               | review how the client's information has changed over time     |
| `* * *`  | property agent                       | delete a client                                            | remove client records that I no longer need                   |
| `* * *`  | property agent                       | have my changes saved automatically                        | retain my client records between application sessions        |
| `* * *`  | new property agent                   | view the available commands                                | learn how to use the application correctly                    |
| `* *`    | property agent                       | schedule a property viewing for a client                   | keep track of the client's viewing appointment                |
| `* *`    | property agent                       | attach an internal note to a client                        | retain useful details without displaying them in the client list |
| `* *`    | property agent                       | view clients who are interested in a particular property   | identify potential leads for that property                    |
| `* *`    | property agent                       | filter clients whose profiles contain active property listings | focus on active sellers                                    |

### Use cases

For all use cases below, the **System** is `RESIDEX` and the **Actor** is a `property agent`.
`MSS` stands for Main Success Scenario. These use cases describe product requirements, including
features planned for future iterations; they do not indicate that a feature is already implemented.

A client is a buyer or seller. Two clients are duplicates when both their normalised names and
normalised phone numbers match, following the Add Client feature specification.
Names are compared case-insensitively after trimming surrounding spaces and replacing consecutive spaces with one space.
Phone input is trimmed during parsing; valid phone numbers contain only digits and are compared exactly, including leading zeros.
Email, intention, budget and remark do not affect duplicate detection.
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

1. Property agent requests to add a client with their name, phone number, email and purpose
   (Buyer or Seller).
2. RESIDEX adds the client and displays the updated client list.

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

1. Property agent requests to find clients by name, phone number, email, property location or intention,
   supplying search keywords.
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

**Use case: UC03 — Filter clients for follow-up**

**MSS**

1. Property agent requests to filter clients by criteria such as buyer/seller category, buying
   status, property requirements or priority.
2. RESIDEX displays the clients satisfying the criteria.
3. Property agent requests to view a listed client's profile.
4. RESIDEX displays the client's details, including recorded requirements, buying status,
   priority and associated property details.

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
   such as contact details, budget, preferred location, property type, required number of bedrooms,
   buying status, priority or associated property details.
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
5. Property agent requests to remove selected information, such as an interest, buying status
   or a specific associated property.
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

#### Requirements considered outside the MVP

The following requirements were considered during ideation and are outside the current MVP.
They are documented for future reference, without committing to their implementation.

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
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

*{More to be added}*

### Glossary

* **Active property listing**: A property listing recorded under a seller's client profile that is currently available for sale.
* **Associated property**: A property linked to a client's profile, such as a property the client is interested in or is selling.
* **Budget**: The amount of money a buyer is prepared to spend on a property.
* **Budget band**: The category derived from a client's recorded budget: Low ($800,000 and below), Middle (above $800,000, up to and including $2,500,000), or High (above $2,500,000).
* **Buyer**: A client who wants to purchase a property.
* **Buying status**: How close a client is to agreeing on a property.
* **Client**: A buyer or seller whose details a property agent stores in RESIDEX.
* **Client information history**: Previously recorded information or changes associated with a client, available for review.
* **Client profile**: The stored information about a client, including contact details, requirements, buying status, priority and associated properties.
* **Duplicate client**: A client whose normalised name and normalised phone number both match those of an existing client.
* **Filtered client list**: The clients currently displayed after a search. Each `find` command searches all stored clients and replaces this list; `list` restores all clients.
* **Follow-up**: A planned next contact with a client. A follow-up may be upcoming or overdue.
* **Intention**: Whether a client or property is on the buying or selling side.
* **Internal note**: A note attached to a client profile that is not displayed in the client list. It does not imply additional access control or encryption.
* **Keyword**: A search term supplied after a `find` prefix. Whitespace separates keywords, and any matching keyword returns a client.
* **Lead**: A potential client or transaction that the property agent is currently pursuing
* **Mainstream OS**: Windows, Linux, Unix, or macOS.
* **Minimum Viable Product (MVP)**: The initial version of RESIDEX containing the features selected to meet its core user needs.
* **Prefix**: A field selector such as `n/` for a name or `l/` for a property location. The `find` command accepts exactly one selector.
* **Priority**: An indication of a client's importance or urgency, assigned by the property agent to help decide whom to attend to first.
* **Private client note**: A note attached to a client's profile whose contents are hidden from the client list.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Property agent**: A user who manages buyers and sellers, and who prefers typing commands to using the mouse.
* **Property entry**: A record in a client's ordered property list containing a type, intention, location and optional price.
* **Property index**: The one-based position of a property entry within a selected client's property list. It is not a permanent identifier: deleting an earlier property shifts subsequent indexes down by one.
* **Property listing**: A record containing information about a property available for sale or rent
* **Property price**: An optional, positive whole-dollar amount recorded for a property, separate from the client's budget.
* **Property type**: The HDB, Condominium or Landed category of a property entry.
* **Property location**: Nonblank descriptive text stored on a property entry, such as `Clementi Ave 3`. The `find l/` search checks these locations.
* **Property viewing**: An appointment for a client to inspect a property
* **Purpose**: A client's reason for engaging the property agent, recorded as Buyer or Seller.
* **Remark**: Text recorded for a client using the `remark` command and displayed on the client card.
* **Requirement**: A condition a client wants in a property, such as budget, preferred location, property type, or number of bedrooms.
* **Seller**: A client who wants to sell a property.
* **Substring matching**: Matching text that contains a keyword anywhere, ignoring case; for example, `clementi` matches both `Clementi Ave 3` and `Woodlands Clementi`.
* **Transaction**: The process of completing a property sale or rental
* **Viewing**: A scheduled visit to a property with a client.


--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Viewing command help

1. Opening and reopening help

   1. Test case: `help`<br>
      Expected: A separate, resizable window titled `RESIDEX Help` opens with command-reference cards.

   1. With help already open, enter `help` again in the main window.<br>
      Expected: The existing help window is focused; no second help window is created.

   1. Close help, focus the main command box and press `F1`. Also try **Help > Help**.<br>
      Expected: Each action opens the same command-reference window.

1. Reading the reference

   1. Scroll through the cards.<br>
      Expected: Cards for `add`, `edit`, `addp`, `delp`, `editp`, `find`, `list`, `delete`, `remark`, `help` and `exit` show their formats, details and examples.
      The heading and User Guide footer remain visible while the cards scroll.

   1. Resize the window narrower and wider.<br>
      Expected: Long text and examples wrap inside their cards. The cards can be read without horizontal scrolling.

1. Copying the guide URL

   1. Click **Copy URL** and paste the clipboard contents into a text editor.<br>
      Expected: `https://ay2627s1-cs2103t-w11-4.github.io/tp/UserGuide.html` is pasted.

### Detecting duplicate clients

Prerequisite: Use a fresh data folder with no clients named Jordan Lee or Taylor Lim.

1. Adding clients

   1. Test case: `add n/Jordan Lee p/91001001 e/jordan@example.com i/Buyer`<br>
      Expected: Jordan is added.

   1. Test case: `add n/JORDAN   LEE p/91001001 e/other@example.com i/Seller`<br>
      Expected: The error identifies the existing client as `Jordan Lee (91001001)` and explains that both name and phone match.
      It also explains that name matching ignores capitalisation and extra spaces.
      No client is added.
      Capitalisation, spacing, email and intention do not distinguish clients with the same name and phone.

   1. Test case: `add n/Jordan Lee p/91001002 e/anotherjordan@example.com i/Seller`<br>
      Expected: A second Jordan is added because the phone number differs.

   1. Test case: `add n/Taylor Lim p/91001001 e/taylor@example.com i/Buyer`<br>
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

   1. Enter `add n/Property Test Client p/91009001 e/propertytest@example.com i/Buyer b/900000`, then `find p/91009001`.<br>
      Expected: Exactly one client is shown, with displayed index 1.

   1. Enter `ADDP 1 T/h I/buyer L/Clementi Ave 3 PR/800000`.<br>
      Expected: Property 1 appears under this client as HDB, Buyer, Clementi Ave 3, $800,000.
      The client filter remains active and the client's budget remains $900,000.

   1. Enter `addp 1 t/c i/Seller l/Bishan`.<br>
      Expected: Property 2 appears as Condominium, Seller, Bishan with no price.
      The client's own intention remains Buyer.

1. Editing and clearing price

   1. Enter `editp 1 2 l/Toa Payoh pr/1500000`.<br>
      Expected: Only property 2's location and price change; its type, property intention and position are retained.

   1. Enter `editp 1 2 pr/`.<br>
      Expected: Property 2's price is removed; all other fields remain unchanged.

   1. Enter `editp 1 2 l/Toa Payoh`.<br>
      Expected: The feedback reports an unchanged property and the original entry is retained.

1. Rejecting invalid requests

   1. Try `editp 1 2 l/`, `editp 1 2 l/Bishan pr/1.5`, `editp 1 2 l/Bishan L/Tampines`, and `addp 1 t/HDB i/Buyer l/Bishan p/800000`.<br>
      Expected: Each command reports an input error; no property or saved data is changed.
      Price uses `pr/`, not `p/`.

   1. Try `addp 2 t/HDB i/Buyer l/Bishan`, `editp 1 99 l/Bishan`, and `delp 1 0`.<br>
      Expected: Invalid client/property indexes are rejected without modifying any client or property.

1. Deleting and saving

   1. Enter `delp 1 1`.<br>
      Expected: The Clementi property is removed; the Toa Payoh property becomes property 1.
      The client remains in the filtered list.

   1. Exit and relaunch the app, then enter `find p/91009001`.<br>
      Expected: The single Condominium/Seller/Toa Payoh entry remains, without a price.
      The client's contact details, Buyer intention and $900,000 budget are retained.

### Recording a budget

1. Adding a client with a budget

   1. Test case: `add n/Chloe Tan p/91234567 e/chloe@example.com i/Buyer b/500000`<br>
      Expected: Chloe is added. `$500,000` is shown on the right of the card in red.

   1. Test case: `add n/Marcus Lim p/92345678 e/marcus@example.com i/Buyer b/1500000`<br>
      Expected: `$1,500,000` is shown in yellow.

   1. Test case: `add n/Ava Ng p/93456789 e/ava@example.com i/Seller b/3000000`<br>
      Expected: `$3,000,000` is shown in green.

   1. Test case: `add n/Ben Ong p/94567890 e/ben@example.com i/Seller`<br>
      Expected: Ben is added. The card shows no budget amount.

   1. Test case: `add n/Invalid Budget p/95678901 e/bad@example.com i/Buyer b/800,000`<br>
      Expected: No client is added. The error states that the budget must be a whole-dollar amount without commas.

1. Editing a budget

   1. Prerequisites: At least one client is shown.

   1. Test case: `edit 1 b/900000`<br>
      Expected: The first client's amount becomes `$900,000` in yellow. The other fields stay the same.

   1. Test case: `edit 1 p/90000000`<br>
      Expected: The phone number changes. The budget stays as it was.

1. Saving a budget

   1. Add or edit a budget, exit, and launch the app again.<br>
      Expected: The same amount and colour are still shown.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

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
   2. Restart, then add a client using `add n/Alice p/91234567 e/alice@example.com i/Buyer`.
      Expected: RESIDEX starts normally with an empty list, and the added client persists after restarting.
