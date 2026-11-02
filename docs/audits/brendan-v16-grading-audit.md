---
  layout: default.md
  title: "Brendan v1.6 grading audit"
  pageNav: 2
---

# RESIDEX — Brendan v1.6 grading audit

Audit of team master `ecc0b424f86ec6361a7a3b66f61abee51ac7aea9`, fetched on 2 November 2026 SGT.
Deadline: **3 November 2026, 14:00 SGT**. Author under audit: `Brendan-Lim`.
This report and its improvements were prepared with OpenAI Codex. Existing reuse and AI acknowledgements are preserved.
This is an evidence-based inspection, not an official grade or a prediction of deductions.

## A. Executive summary

The clearest remaining personal Implementation risks are missing public-constructor descriptions, a partial Javadoc
parameter list, the long/shared find parser, and two predicates that implement value equality without matching hash codes.
The hash defects were reproduced as failed set lookups; no failing end-user workflow using those hash lookups was found.
There is no evidence here that either defect loses client data or misdirects a displayed-index command.

Safe improvements completed locally:

* Three regression tests: literal-slash location partitions, and role-changing property edits across ranked searches,
  undo/redo, persistence and a fresh model reload.
* Corrected overbroad location guidance and the glossary’s search-keyword definition.
* Made the DG undo scenario independent of pre-existing properties, remarks and search indexes.
* Regenerated the Help screenshot to match the latest name-validation message.

**No file under `src/main` was modified.** The branch consumes **0 additional functional LOC**.
The final submitted test source passes 873 tests, with no failures, errors or skips; Checkstyle and coverage generation pass.
The documentation build and internal-link checks pass. A deliberately failing temporary probe was removed before final validation;
its source and results remain outside the repository for reproducibility.

Before submission, the team still needs a matching v1.6 source tag/release, executable and deployed guides, plus Canvas verification.
The latest inspected GitHub CI run has successful Linux and Windows jobs but a cancelled macOS job. Local macOS checks pass;
this is an unresolved hosted-readiness check, not evidence of a Java test failure.

The latest inspected RepoSense snapshot attributes **562 functional LOC** to Brendan, rather than the older 558.
The allowance remains **150 LOC**. Its freeze dashboard credits 4 surviving changed functional lines; the existing
freeze commit has a conservative Git diff of 4 additions + 2 deletions = 6. These measurements must not be conflated.

## B. Official grading requirements

The following criterion identifiers are used throughout. Each finding links back to its applicable official source.
The short summaries below are paraphrases; the linked course pages govern interpretation.

| ID | Category and marks | Criteria used |
| --- | --- | --- |
| R-PD | Product Design — 5 | Target-user fit, cohesive features and feature flaws. |
| R-I | Implementation — 15 | Claimed-code quality and implementation effort; coding standards, SLAP, duplication, course principles and evidence of error-handling techniques. |
| R-Q | QA — 10 | Own-feature tests and received functionality bugs; system/acceptance testing demonstrated in the PE. |
| R-D | Documentation — 10 | UG/DG quality and attributable contribution; accuracy, examples, visuals, navigation, UML, requirements and manual instructions. |
| R-PM | Project Management — 10 | Iterative work, timely tasks, GitHub/process quality and shared team tasks; commits in four of weeks 7–12. |

These five criteria come from the [official grading rubric](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).
Its effort and documentation assessments also use peer evidence; this audit cannot substitute for peer evaluations.

Additional official sources reviewed:

* [Week 12 and feature freeze](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html): freeze begins
  30 October at 10:00 SGT; the dashboard measures each member’s edited attributed functional lines over the whole period.
  Comments and blanks in code files count. The user’s stricter prohibition on **all** `src/main` changes governs this branch.
* [Deliverables](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html): matching executable/source/website,
  current UG/DG, manual-testing appendix and accurate reuse acknowledgements. PPP is optional this semester; the demo deliverable is removed.
* [Java basic + intermediate standard](https://se-education.org/guides/conventions/java/intermediate.html): names, boolean
  wording, visibility, layout, comments and public API documentation, including documented exemptions.
* [Code quality](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html): readability,
  abstraction levels, nesting, duplication, scope, obvious code, comments, dead code and premature optimization.
  The discussion of methods over 30 LOC is guidance to consider shortening, not a hard grading cutoff.
* [Error handling](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/errorHandling.html): exception
  handling, assertion purpose, diagnostic logging and proportionate defensive coding.
* [Project expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html): automated-test evidence
  and enhancement-related documentation. There is **no minimum coverage percentage**.
* [Practical-exam guidance](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html): target-user/CLI evaluation,
  fit-for-purpose UG/DG evaluation and evidence-based bug reporting/triage.
* [Reuse/AI policies](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html): acknowledgements checked
  against existing declarations; this audit does not certify the provenance of every historic line.

Interpretation boundaries: a maintainability concern is not automatically a course violation; documented limitations can still
be assessed as design weaknesses, but documentation does not establish their severity. Coverage is diagnostic evidence,
not a grade. Dates, release status and dashboards are snapshots. No exact mark deductions are predicted.

## C. Personal contribution inventory

### Ownership method and limits

The inventory combines both known Brendan email identities, 43 non-merge commits reachable from audited master since
18 September, their complete file-change lists, current blame, original patches, merged PR metadata and RepoSense line authorship.
It excludes the unrelated upstream author **Brendan Koh**. Merge commits and mechanical reverts are not treated as original feature authorship.
The original checkout’s `output/` and `plugin/` directories and every pre-existing worktree were preserved.

The full dashboard was generated **2 November, 22:29:20 SGT**; the freeze dashboard was generated **18:40:19 SGT**.
Both were rechecked on 3 November and still cover through 2 November; neither is the final v1.6 snapshot. Full totals are 562 functional,
2,883 test and 922 documentation lines. These are surviving attribution counts, not accumulated effort or ownership of whole files.
Sources: [code dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/),
[full summary JSON](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/summary.json),
[team commits data](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/AY2627S1-CS2103T-W11-4_tp_master/commits.json),
[line authorship](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/AY2627S1-CS2103T-W11-4_tp_master/authorship.json).

The table includes historically touched production files even where current attribution is zero.
Java production paths are relative to `src/main/java/seedu/address/`; the FXML row uses its full repository path.
Test names resolve to the full paths in the next table. Scope descriptions distinguish original work from inherited/shared code.
Commit links show Brendan’s earliest and latest direct changes to each file; intervening commits are listed in the history table below.

### Production inventory

| File | Methods / contribution scope | Dashboard LOC | Ownership evidence | Associated tests |
| --- | --- | ---: | --- | --- |
| `MainApp.java` | Path-injection constructors; lifecycle branding and initialization seams. Data-load protection and instance locking are shared/team work. | 23 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) → [3a38b330](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3a38b3306fa147267001b3c59fb4a1f2f110ed10) | MainAppTest; ResidexUiTest |
| `commons/core/LogsCenter.java` | RESIDEX logger/file naming; existing handler infrastructure is inherited. | 3 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) | EditPropertyStatusLoggingTest; HelpContentTest |
| `logic/Messages.java` | User-facing naming/feedback; underlying AB3 formatting is inherited. | 2 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | DuplicateClientFeedbackTest; FindCommandParserTest |
| `logic/commands/AddCommand.java` | Usage/branding and adapting client construction to derived roles. | 3 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) → [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | AddCommandParserTest; LogicManagerTest |
| `logic/commands/AddPropertyCommand.java` | Property-intention/status usage; original property command is team work. | 2 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | PropertyCommandParserTest; PropertyCommandsIntegrationTest |
| `logic/commands/EditCommand.java` | createEditedPerson: preserve properties and remove independent role. | 1 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) → [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | EditCommandTest; PersonDerivedIntentionTest; IntentionSearchIntegrationTest |
| `logic/commands/EditPropertyCommand.java` | Status descriptor/copy/apply/equality and execute transition diagnostics; base edit machinery is shared. | 22 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | PropertyCommandTest; PropertyCommandsIntegrationTest; EditPropertyStatusLoggingTest |
| `logic/commands/ExitCommand.java` | Exit feedback branding. | 1 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) | MainAppTest; ReadOnlyCommandsDoNotSaveIntegrationTest (team) |
| `logic/commands/FindCommand.java` | General Predicate constructor/field; selector usage and diagnostic constants, including unknown guidance. | 24 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) → [923d5501](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/923d55012d67066d9fef72d19c833f37a59c2814) | FindCommandTest; FindCommandParserTest; IntentionSearchIntegrationTest; PropertyStatusSearchIntegrationTest |
| `logic/commands/RemarkCommand.java` | execute replacement preserves properties; existing command is largely teammate-authored in master. | 1 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | RemarkCommandTest; PropertyCommandsIntegrationTest; IntentionSearchIntegrationTest |
| `logic/parser/AddCommandParser.java` | parse: reject manual intention and construct a role-free client. | 2 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | AddCommandParserTest; PropertyCommandsIntegrationTest |
| `logic/parser/AddPropertyCommandParser.java` | parse: status omission/default and property intention adaptation. | 5 | [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | PropertyCommandParserTest; PropertyCommandsIntegrationTest |
| `logic/parser/CliSyntax.java` | Property status prefix. | 1 | [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | FindCommandParserTest; PropertyCommandParserTest |
| `logic/parser/EditCommandParser.java` | parse: reject manual client intention. | 1 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | EditCommandParserTest; PropertyCommandsIntegrationTest |
| `logic/parser/EditPropertyCommandParser.java` | parse: status field conversion and descriptor setter. | 4 | [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | PropertyCommandParserTest; PropertyCommandsIntegrationTest |
| `logic/parser/FindCommandParser.java` | parse: short selectors, phone, derived intention and status; status alias; shared parser later extended by teammates. | 55 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) → [2de9803f](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/2de9803f6549e30b6560cb00693efc24191fa106) | FindCommandParserTest; FindCommandTest; intention/status integration tests |
| `logic/parser/ParserUtil.java` | rejectClientIntention and its pattern; other validation is inherited/shared. | 10 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | ParserUtilTest; AddCommandParserTest; EditCommandParserTest |
| `logic/parser/PropertyParserUtil.java` | parseIntention and parseStatus exception translation; shared prefix parser. | 16 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | PropertyCommandParserTest; FindCommandParserTest |
| `model/Model.java` | Displayed-list contract and intention-order documentation; original API is AB3. | 2 | [9edecbe7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9edecbe73f50486edc818a1432059f8e8a582395) | ModelManagerIntentionOrderTest; IntentionSearchIntegrationTest |
| `model/ModelManager.java` | Constructor transformation pipeline, displayed list, setPersonFilter ranking, getSourcePosition cache and equality adaptation. | 35 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) → [5ffbd057](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/5ffbd057d02dacfa45d5f1324f1559ea5aded68f) | ModelManagerIntentionOrderTest; IntentionSearchIntegrationTest; PropertyStatusSearchIntegrationTest |
| `model/person/ClientIntention.java` | Entire derived-role enum and display names. | 22 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | ClientIntentionTest; PersonDerivedIntentionTest |
| `model/person/Intention.java` | Former role type changed/replaced by ClientIntention and PropertyIntention; deleted from current source. | 0 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | Historical role tests; current ClientIntentionTest/PropertyIntentionTest |
| `model/person/IntentionMatchesPredicate.java` | Shared predicate: derived-role matching and getComparator; original equality/constructor came from Ray. | 14 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) → [9edecbe7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9edecbe73f50486edc818a1432059f8e8a582395) | IntentionMatchesPredicateTest; ModelManagerIntentionOrderTest |
| `model/person/LocationContainsKeywordsPredicate.java` | Revert-only history: Brendan reverted an incomplete location PR. Original feature belongs to Ray; no current attributed lines. | 0 | [10adfb8e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/10adfb8eb6c5e4657052bed2af1ea926dbe6aba2) | LocationContainsKeywordsPredicateTest (primarily teammate work) |
| `model/person/Person.java` | getIntention, constructor adaptation, copy identity assertion and diagnostic role; original AB3 entity/property-copy machinery is shared. | 24 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) → [db4b9529](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/db4b9529f079fedfde4403df99b9467b0bd48518) | PersonDerivedIntentionTest; PersonPropertiesTest; PersonTest |
| `model/person/PhoneContainsKeywordsPredicate.java` | Original phone-substring predicate, constructor, test, equality, toString; later phone normalization is Sean’s work. | 40 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) | PhoneContainsKeywordsPredicateTest; FindCommandParserTest; FindCommandTest |
| `model/person/PropertyStatusMatchesPredicate.java` | Entire status predicate including constructor, test, equals, hashCode and toString. | 42 | [f8235664](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/f8235664277c7635e7b0adb884781c1d553449a3) → [7edfabe2](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/7edfabe2c4b74fb5a7276febe887a6cb4065d212) | PropertyStatusMatchesPredicateTest; PropertyStatusSearchIntegrationTest |
| `model/property/Property.java` | Intention type and status constructors/getter/display/equality/hash; original property model and later bedrooms are shared. | 24 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) | PropertyTest; PersonDerivedIntentionTest; property integration tests |
| `model/property/PropertyIntention.java` | Entire Buying/Selling enum, parse and display. | 42 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) | PropertyIntentionTest; PropertyCommandParserTest; JsonAdaptedPropertyTest |
| `model/property/PropertyStatus.java` | Entire transaction-stage enum, parse and display. | 47 | [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) | PropertyStatusTest; PropertyStatusMatchesPredicateTest; property command/storage integration tests |
| `model/util/SampleDataUtil.java` | Representative property roles and Viewing sample; later bedrooms, remarks and email changes are team work. | 13 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) → [ca63e93c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/ca63e93c998349d8cb629eeaef616bac7922c85e) | SampleDataUtilTest; MainAppTest |
| `storage/JsonAdaptedPerson.java` | Legacy client-role input ignored and omitted on save; original adapter is AB3/team work. | 4 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | JsonAdaptedPersonTest; JsonAddressBookStorageTest |
| `storage/JsonAdaptedProperty.java` | Legacy intention migration; status defaults/parsing/canonical serialization. Later case normalization and bedrooms are shared. | 24 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) | JsonAdaptedPropertyTest; JsonAddressBookStorageTest; PropertyCommandsIntegrationTest |
| `storage/JsonSerializableAddressBook.java` | Constructor retains missing/null list; toModelType explicit missing-list rejection and defensive copy. | 9 | [daeb9a0e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/daeb9a0ea3ce6371b61c5c251c85e603f630b579) → [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) | JsonSerializableAddressBookTest; JsonAddressBookStorageTest; MainAppTest; ResidexUiTest |
| `ui/HelpContent.java` | Role, property intention/status and find help updates; original help infrastructure is teammate work. | 43 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [e6bc823d](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e6bc823d133af3949731e74fda494caf20fce8b7) | HelpContentTest; ResidexUiTest |
| `src/main/resources/view/MainWindow.fxml` | RESIDEX title only; UI layout is teammate/shared work. | 1 | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) | MainAppTest; ResidexUiTest |

### Test and fixture inventory

All paths below are relative to `src/test/java/seedu/address/`. A zero does not erase earlier work; it means the inspected
dashboard currently assigns none of that file to Brendan. These files were inspected as feature groups rather than assuming
every assertion in a historically touched file was personally written. Unchanged AB3 test helpers are not claimed as new work.

| Test / fixture | Dashboard LOC | Earliest → latest personal file-change evidence |
| --- | ---: | --- |
| `MainAppTest.java` | 202 | [3a38b330](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3a38b3306fa147267001b3c59fb4a1f2f110ed10) → [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) |
| `logic/DuplicateClientFeedbackTest.java` | 4 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/IntentionSearchIntegrationTest.java` | 256 | [9edecbe7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9edecbe73f50486edc818a1432059f8e8a582395) |
| `logic/LogicManagerTest.java` | 15 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/PropertyCommandsIntegrationTest.java` | 184 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) |
| `logic/PropertyStatusSearchIntegrationTest.java` | 260 | [f8235664](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/f8235664277c7635e7b0adb884781c1d553449a3) → [2de9803f](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/2de9803f6549e30b6560cb00693efc24191fa106) |
| `logic/commands/CommandTestUtil.java` | 0 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/commands/EditCommandTest.java` | 8 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/commands/EditPropertyStatusLoggingTest.java` | 176 | [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) → [27f0a2b9](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/27f0a2b95780514a364ba3bb92223b107824712a) |
| `logic/commands/FindCommandTest.java` | 184 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) → [10adfb8e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/10adfb8eb6c5e4657052bed2af1ea926dbe6aba2) |
| `logic/commands/PropertyCommandTest.java` | 44 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) |
| `logic/commands/RemarkCommandTest.java` | 10 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/parser/AddCommandParserTest.java` | 26 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/parser/AddressBookParserTest.java` | 1 | [d2bb4a8c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/d2bb4a8c43965d5efac937d21ce5e417bcca103d) |
| `logic/parser/EditCommandParserTest.java` | 11 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/parser/FindCommandParserTest.java` | 245 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) → [923d5501](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/923d55012d67066d9fef72d19c833f37a59c2814) |
| `logic/parser/ParserUtilTest.java` | 11 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `logic/parser/PropertyCommandParserTest.java` | 88 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) |
| `model/ModelManagerIntentionOrderTest.java` | 135 | [5ffbd057](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/5ffbd057d02dacfa45d5f1324f1559ea5aded68f) |
| `model/person/ClientIntentionTest.java` | 15 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `model/person/IntentionMatchesPredicateTest.java` | 42 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) → [9edecbe7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9edecbe73f50486edc818a1432059f8e8a582395) |
| `model/person/LocationContainsKeywordsPredicateTest.java` | 2 | [10adfb8e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/10adfb8eb6c5e4657052bed2af1ea926dbe6aba2) |
| `model/person/PersonDerivedIntentionTest.java` | 69 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `model/person/PersonPropertiesTest.java` | 1 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `model/person/PersonTest.java` | 16 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `model/person/PhoneContainsKeywordsPredicateTest.java` | 81 | [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) |
| `model/person/PropertyStatusMatchesPredicateTest.java` | 107 | [f8235664](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/f8235664277c7635e7b0adb884781c1d553449a3) → [7edfabe2](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/7edfabe2c4b74fb5a7276febe887a6cb4065d212) |
| `model/property/PropertyIntentionTest.java` | 64 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) |
| `model/property/PropertyStatusTest.java` | 58 | [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) |
| `model/property/PropertyTest.java` | 50 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) |
| `model/util/SampleDataUtilTest.java` | 117 | [3c31e46a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3c31e46a1bbd365d95d9b23dbdeeb5c7f1489372) → [ca63e93c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/ca63e93c998349d8cb629eeaef616bac7922c85e) |
| `storage/JsonAdaptedPersonTest.java` | 38 | [55468bdd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/55468bddac498055805d085b1ecf0198858cb7cb) → [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) |
| `storage/JsonAdaptedPropertyTest.java` | 67 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) |
| `storage/JsonAddressBookStorageTest.java` | 196 | [55468bdd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/55468bddac498055805d085b1ecf0198858cb7cb) → [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) |
| `storage/JsonSerializableAddressBookTest.java` | 15 | [daeb9a0e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/daeb9a0ea3ce6371b61c5c251c85e603f630b579) → [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) |
| `testutil/PersonBuilder.java` | 0 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `testutil/PersonUtil.java` | 0 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `testutil/TypicalPersons.java` | 0 | [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) |
| `testutil/TypicalProperties.java` | 4 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) |
| `ui/HelpContentTest.java` | 46 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [10adfb8e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/10adfb8eb6c5e4657052bed2af1ea926dbe6aba2) |
| `ui/ResidexUiTest.java` | 34 | [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) → [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) |

### Documentation and documentation-tooling inventory

| Repository path | Original / shared contribution evidence |
| --- | --- |
| `docs/AboutUs.md` | [0bcd3ac3](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/0bcd3ac311b88acef6d78e0b35deff7a309f60bc); 6 surviving dashboard lines |
| `docs/DeveloperGuide.md` | [4b3a3c9d](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/4b3a3c9d38fb5d6d02bec649b373875b3ece9500) → [3253a8b9](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3253a8b9e4adfd74af5fd2aef5a529b920aa3c4e); 712 surviving dashboard lines |
| `docs/Documentation.md` | [42ef0d71](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/42ef0d71d158ded4fc4a8cf037889fd1fb924e5c); 24 surviving dashboard lines |
| `docs/Logging.md` | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723); 1 surviving dashboard lines |
| `docs/SettingUp.md` | [00b374b6](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/00b374b672bffbc06832c000f1e50905e2e782d0); 1 surviving dashboard lines |
| `docs/UserGuide.md` | [d2bb4a8c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/d2bb4a8c43965d5efac937d21ce5e417bcca103d) → [9d4e2571](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9d4e2571831dd5c372debbeed2a205aa909e3375); 146 surviving dashboard lines |
| `docs/_markbind/layouts/default.md` | [df4a0fb5](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df4a0fb5aa2f0a6af9b42bd42cf6b477db6211b8); 4 surviving dashboard lines |
| `docs/diagrams/ModelClassDiagram.puml` | [863e9510](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/863e9510ad34d78cdd76817bd222833b7c7880db) → [d512b646](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/d512b64690b0fc901b2d48d34cf030a69aece365); 27 surviving dashboard lines |
| `docs/diagrams/UndoRedoState5.puml` | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723); 1 surviving dashboard lines |
| `docs/images/brendan-lim.png` | [25394d0e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/25394d0ed3ada46ff2f7f6e3e1460939d65e176d); 0 surviving dashboard lines |
| `docs/package.json` | [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) → [42ef0d71](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/42ef0d71d158ded4fc4a8cf037889fd1fb924e5c); 0 surviving dashboard lines |
| `docs/scripts/markbind.cjs` | [42ef0d71](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/42ef0d71d158ded4fc4a8cf037889fd1fb924e5c); 0 surviving dashboard lines |
| `docs/site.json` | [42ef0d71](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/42ef0d71d158ded4fc4a8cf037889fd1fb924e5c); 0 surviving dashboard lines |

Substantive document contributions include use cases/glossary and navigation, derived client roles, property directions,
stages and transitions, ranking/index semantics, migration and storage recovery, sample-data guidance, diagrams, manual tests,
logging instructions, build prerequisites, Java setup and AI disclosure. Subsequent teammate corrections were reviewed against code.

### Original work absent from current attribution

* Phone search was introduced in PR #62; Sean subsequently added number normalization. Audit both the original predicate
  contract and current behavior, rather than attributing normalization to Brendan.
* PRs #109/#111/#113 changed shared parsers, model objects, storage and tests. Later bedroom, undo and UI work did not remove
  the original role/status contribution. The original `Intention` class is now deleted; its responsibilities were separated.
* `LocationContainsKeywordsPredicate` appears in Brendan’s history because of the #114 revert; the original location feature
  and later restoration belong to Ray. A revert is project-management evidence, not authorship of the reverted feature.
* [PR #10](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/10) (remark tutorial) and
  [PR #45](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/45) are **closed, unmerged**. Their patches were retrieved.
  #45 also carries earlier tutorial-branch changes. #46 delivers the use-case update on master. Do not claim either unmerged
  PR as an additional delivered feature, or assume a similar teammate implementation was copied from that branch.

The unmerged tutorial has the following additional historical file scope. This is provenance evidence, not current line credit:

* `docs/DeveloperGuide.md` — PR #45.
* `src/main/java/seedu/address/logic/Messages.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/commands/EditCommand.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/commands/RemarkCommand.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/parser/AddCommandParser.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/parser/AddressBookParser.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/parser/CliSyntax.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/parser/ParserUtil.java` — PR #10 / #45.
* `src/main/java/seedu/address/logic/parser/RemarkCommandParser.java` — PR #10 / #45.
* `src/main/java/seedu/address/model/person/Person.java` — PR #10 / #45.
* `src/main/java/seedu/address/model/person/Remark.java` — PR #10 / #45.
* `src/main/java/seedu/address/model/util/SampleDataUtil.java` — PR #10 / #45.
* `src/main/java/seedu/address/storage/JsonAdaptedPerson.java` — PR #10 / #45.
* `src/main/java/seedu/address/ui/PersonCard.java` — PR #10 / #45.
* `src/main/resources/view/PersonListCard.fxml` — PR #10 / #45.
* `src/test/java/seedu/address/logic/commands/CommandTestUtil.java` — PR #10 / #45.
* `src/test/java/seedu/address/logic/commands/RemarkCommandTest.java` — PR #10 / #45.
* `src/test/java/seedu/address/logic/parser/RemarkCommandParserTest.java` — PR #10 / #45.
* `src/test/java/seedu/address/model/person/PersonTest.java` — PR #10 / #45.
* `src/test/java/seedu/address/model/person/RemarkTest.java` — PR #10 / #45.
* `src/test/java/seedu/address/storage/JsonAdaptedPersonTest.java` — PR #10 / #45.
* `src/test/java/seedu/address/testutil/PersonBuilder.java` — PR #10 / #45.

Archived tutorial inspection: its original remark parser allowed omitted `r/` to clear a remark; master now rejects that
case (issue #171 was addressed). Its original tests and lack of property preservation are not the current implementation.
The current remark flow was rechecked independently. No production change to the archived branch is warranted.

### Personal direct-commit history used for ownership

This list is deliberately distinct from PR merges and current line attribution. The author dates below are SGT.

| Commit | Date | Subject |
| --- | --- | --- |
| [25394d0e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/25394d0ed3ada46ff2f7f6e3e1460939d65e176d) | 2026-09-28 | Add files via upload |
| [0bcd3ac3](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/0bcd3ac311b88acef6d78e0b35deff7a309f60bc) | 2026-09-28 | Update team member details in AboutUs.md |
| [4b3a3c9d](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/4b3a3c9d38fb5d6d02bec649b373875b3ece9500) | 2026-10-02 | Update RESIDEX use cases and glossary |
| [df4a0fb5](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df4a0fb5aa2f0a6af9b42bd42cf6b477db6211b8) | 2026-10-02 | Improve RESIDEX navbar link labels |
| [df53805a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/df53805a5c81ef06a6c612bf61a3bee6572c0bf7) | 2026-10-05 | Support find by phone number |
| [d2bb4a8c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/d2bb4a8c43965d5efac937d21ce5e417bcca103d) | 2026-10-13 | Standardise Find command prefixes |
| [a2da5679](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/a2da567912558556d5f1f865649be618db7ce723) | 2026-10-14 | Update guides and messages to RESIDEX branding |
| [3a38b330](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3a38b3306fa147267001b3c59fb4a1f2f110ed10) | 2026-10-14 | Cover application startup and shutdown paths |
| [42ef0d71](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/42ef0d71d158ded4fc4a8cf037889fd1fb924e5c) | 2026-10-14 | Check diagram dependencies before building documentation |
| [e83caadd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e83caadd66fe3c1aafb0b7dd5847c03868d3223f) | 2026-10-20 | Property: Separate property intention |
| [55468bdd](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/55468bddac498055805d085b1ecf0198858cb7cb) | 2026-10-20 | Storage: Migrate property intention values |
| [863e9510](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/863e9510ad34d78cdd76817bd222833b7c7880db) | 2026-10-20 | Docs: Explain separate property intentions |
| [53df1f27](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/53df1f272f4053d35aa6809b4930e8381575062a) | 2026-10-20 | Client: Derive intention from properties |
| [9edecbe7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9edecbe73f50486edc818a1432059f8e8a582395) | 2026-10-20 | Find: Order derived client intentions |
| [b20866a0](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b20866a0df263d016d8942681226f42b65748dbb) | 2026-10-20 | Docs: Explain derived client intentions |
| [15060cf7](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/15060cf7598809f0782f34b42433f7e26c39a03f) | 2026-10-20 | Property: Add transaction status |
| [43d7bef4](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/43d7bef4f52d74be5e517fcae2bbebd02a5892f8) | 2026-10-20 | Property commands: Edit transaction status |
| [f8235664](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/f8235664277c7635e7b0adb884781c1d553449a3) | 2026-10-20 | Find: Filter clients by property status |
| [d512b646](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/d512b64690b0fc901b2d48d34cf030a69aece365) | 2026-10-20 | Docs: Explain property transaction stages |
| [10adfb8e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/10adfb8eb6c5e4657052bed2af1ea926dbe6aba2) | 2026-10-21 | Revert "Add location filtering to the find command" |
| [9980b735](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9980b73547b5354b9307f2409e9e761041dd737c) | 2026-10-21 | Document property intention rollback limitations |
| [3c31e46a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3c31e46a1bbd365d95d9b23dbdeeb5c7f1489372) | 2026-10-21 | Add sample properties for derived intentions |
| [5ffbd057](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/5ffbd057d02dacfa45d5f1324f1559ea5aded68f) | 2026-10-21 | Avoid linear scans in intention search sorting |
| [db4b9529](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/db4b9529f079fedfde4403df99b9467b0bd48518) | 2026-10-21 | Clarify property-presence boolean names |
| [27f0a2b9](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/27f0a2b95780514a364ba3bb92223b107824712a) | 2026-10-21 | Test status edits that remove filtered clients |
| [7edfabe2](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/7edfabe2c4b74fb5a7276febe887a6cb4065d212) | 2026-10-21 | Align status predicate equality and hashing |
| [9300a2b3](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9300a2b3b63ab04ef62536ca63809ba2d2aee4a5) | 2026-10-21 | Reuse status parsing in find commands |
| [e6bc823d](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e6bc823d133af3949731e74fda494caf20fce8b7) | 2026-10-28 | Find: Support displayed unspecified status |
| [e84ebf96](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e84ebf9692d82eb89ce3fba54e7196be2998cc3b) | 2026-10-28 | Sample data: Demonstrate the Viewing stage |
| [cadfcd61](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/cadfcd61516c9d2b710c1d1d2dfd0816ba39ddd6) | 2026-10-28 | Docs: Clarify status search alias parsing |
| [daeb9a0e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/daeb9a0ea3ce6371b61c5c251c85e603f630b579) | 2026-10-28 | Storage: Explain missing client lists |
| [64bded3d](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/64bded3dcf7ea8318a776608b7406964faa583e8) | 2026-10-28 | Docs: Place sample search results in Quick Start |
| [2de9803f](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/2de9803f6549e30b6560cb00693efc24191fa106) | 2026-10-28 | Docs: Credit localized AI assistance |
| [ca63e93c](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/ca63e93c998349d8cb629eeaef616bac7922c85e) | 2026-10-28 | Docs: Credit localized AI assistance |
| [b759f041](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/b759f041813ff9d4ec93a3e145018e32b82b07f1) | 2026-10-28 | Docs: Credit localized AI assistance |
| [755aca94](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/755aca941ad0bad911caa910a61eda5fd2b53360) | 2026-11-02 | Docs: Clarify unspecified property status syntax |
| [e7a6929f](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/e7a6929f0f884726de60b323fd137062014b9568) | 2026-11-02 | Docs: Explain unsupported periods in client names |
| [ead3235a](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/ead3235a690a48dc23570c903665a3fadd88c4be) | 2026-11-02 | Docs: Correct legacy data save expectations |
| [f83e661e](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/f83e661e8eacf5b87144b07aa6be5a5725c3e415) | 2026-11-02 | Docs: Complete Help and bedroom test checklists |
| [9d4e2571](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/9d4e2571831dd5c372debbeed2a205aa909e3375) | 2026-11-02 | Docs: Clarify find keyword constraints |
| [00b374b6](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/00b374b672bffbc06832c000f1e50905e2e782d0) | 2026-11-02 | Docs: Correct required Java version in setup guide |
| [923d5501](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/923d55012d67066d9fef72d19c833f37a59c2814) | 2026-11-02 | Fix: Include unknown in find validation errors |
| [3253a8b9](https://github.com/AY2627S1-CS2103T-W11-4/tp/commit/3253a8b9e4adfd74af5fd2aef5a529b920aa3c4e) | 2026-11-02 | Docs: Declare Brendan's AI-assisted contributions |

## D. Implementation audit

Line references in this section are to the recorded base SHA; production source is identical at branch completion.
“Functional LOC” estimates below conservatively count added + deleted physical lines, including comments and blanks.
They are planning estimates, not promises about the official dashboard calculation. **Every proposed production edit remains unimplemented.**

### I1 — Missing descriptions on public constructors

* **Location/methods:** `model/person/PropertyStatusMatchesPredicate.java:16` constructor (entirely Brendan),
  `model/person/IntentionMatchesPredicate.java:16` constructor and `logic/commands/FindCommand.java:81` constructor
  (shared/inherited declarations whose types Brendan changed).
* **Criterion:** [R-I](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
  [Java standard, Comments](https://se-education.org/guides/conventions/java/intermediate.html#comments).
* **Evidence:** each public constructor begins immediately after fields, with no descriptive header. The getter/setter,
  override and test exemptions do not cover constructors. Treat the status predicate as the clearest personally introduced instance;
  the other two are shared inherited debt, not entirely original violations.
* **Minimal proposal:** add a concise contract immediately above each constructor, e.g. the requested status/role and non-null expectation.
  No extra prose on obvious getters or overrides is needed.
* **Classification:** Explicit requirement; **Low**; Functional code. **Fix only with approval**, preferably the 4-line status-predicate
  comment first. Estimate **4 additions per constructor; 12 total**. Regression risk: very low executable risk, but all comments consume allowance.

### I2 — Partial `@param` list on the personally added status constructor

* **Location/method:** `model/property/Property.java:40–47`, five-argument constructor; `@param price` at line 43.
* **Criterion:** [R-I](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
  [Java standard, Javadoc parameter guidance](https://se-education.org/guides/conventions/java/intermediate.html#comments).
* **Evidence:** five parameters but only one tag. The prose already identifies price as optional. The four- and six-argument
  shared constructors have the same partial-tag pattern (lines 31–36 and 50–58); those are not all original Brendan code.
* **Minimal proposal:** remove the redundant `@param price` and preceding blank line from the five-argument overload, relying on its prose.
  Alternatively document every parameter, at greater cost. This is a documentation-standard issue inside production source, not a behavior bug.
* **Classification:** Explicit requirement; **Low**; Functional code. **Fix only with approval**. Estimate **2 deletions** for Brendan’s
  added overload; another 5 deletions if the team elects to clean the other two. Regression risk: very low. Worth considering with I1, not alone.

### I3 — Long find parser mixes dispatch and field-specific parsing, with repeated validation

* **Location/method:** `logic/parser/FindCommandParser.java:47–153`, `parse`; notably the intention loop at 69–76,
  alias/exception conversion at 80–94, and repeated single-value checks at 64, 85, 98, 111 and 122.
* **Criterion:** [R-I](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
  [Code quality](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html), SLAP/readability/duplication.
* **Evidence:** the 107-line method splits raw strings, dispatches selectors, validates cardinality, maps domain enums and
  translates errors. Brendan wrote substantial selector/intention/status portions; teammates added type/price/bedroom branches.
  Guard clauses keep nesting moderate, so length alone is not the finding. The mixture of detail levels and repeated cardinality checks is.
* **Minimal proposal:** extract intention and status parsing into small helpers, and consider one shared single-value helper.
  Preserve alias-after-selector-validation behavior and diagnostic precedence. Do not introduce a registry/framework just to dispatch nine fields.
* **Classification:** Strongly relevant; **Medium**; Functional code / Requires team coordination. **Defer until after v1.6** unless
  specifically approved after reviewing a concrete diff. Estimate **80–140 changed lines** for a clean, modest extraction, excluding tests.
  Regression risk: medium, especially validation precedence and search-only aliases. This is poor allowance value immediately before release.

### I4 — Value-equal predicates lack value-based hash codes

* **Location/methods:** `model/person/PhoneContainsKeywordsPredicate.java:50–60`, `equals`, no `hashCode` in the class;
  `model/person/IntentionMatchesPredicate.java:35–45`, `equals`, likewise no `hashCode`.
* **Criterion:** [R-I](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), general code correctness/maintainability;
  technical contract established by [Java 25 Object API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html#hashCode()).
  The course does not list hash codes as a separate grading item.
* **Evidence:** independently constructed equal phone predicates and equal role predicates fail `HashSet.contains` in audit probes.
  Phone equality was part of Brendan’s original class; intention equality predates his substantial role/comparator changes.
  The status predicate already implements both methods correctly. Similar inherited AB3 patterns elsewhere do not justify widening this fix.
* **Minimal proposal:** override `hashCode` using the same immutable logical state as equality (`keywords` and `intention`).
  Also consider copying the phone keyword list at construction, since it is currently retained by reference while digit keywords are cached.
  Actual parser callers supply immutable lists; caller mutation was not demonstrated as a UI bug and copying is an optional hardening change.
* **Classification:** Strongly relevant; **Low**; Functional code. **Fix only with approval**. Estimate **6 additions per hash method,
  12 total**; optional phone-list copying adds roughly 2–4 conservative changed lines. Regression risk: low after equality/set regressions.
  No present hash-collection use in end-user command execution was found, so practical-exam exposure is low; code inspection exposure is higher.

### Checklist conclusions that do not justify more production changes

| Area | Evidence inspected | Conclusion |
| --- | --- | --- |
| Naming / booleans | `Person.getIntention` has `hasBuyingProperty`/`hasSellingProperty`; selector helpers use `is…`; private immutable domain state | No confirmed personally introduced boolean-name violation. Do not rename inherited classes merely for branding. |
| Scope / magic literals | Local enum derivation and 0/1 comparator ranks; prefix constants reused | No case made for replacing obvious rank/index literals just to increase changes. |
| Cohesion / coupling | Distinct property and client intention enums; derived roles; predicates; model-owned displayed order | Responsibilities are coherent. Model’s JavaFX collection dependency is established architecture, not a new violation. |
| Encapsulation | `Person` uses `List.copyOf`; immutable `Property`; displayed list is unmodifiable; descriptor copied | Important mutation boundaries are defended. Optional phone-input copying is discussed under I4. |
| Exceptions | `PropertyParserUtil.parseIntention/parseStatus`; `JsonAdaptedProperty.toModelType`; missing-list `IllegalValueException` | User/data errors propagate through established checked boundaries; causes retained where translated. No swallowed error found in Brendan’s new paths. |
| Assertions | `Person.copyWithProperties` identity invariant; property count/replacement assertions; no user-input assertions in new role/status code | Existing meaningful evidence satisfies the technique review; no reason to add assertions to every branch. The unsupported enum default is defensive/unreachable for current enum values. |
| Logging | `EditPropertyCommand.execute:77–80` FINE stage-transition record contains indexes/enums; logging regressions restore handler state | Diagnostic value exists, and no new client data in the transition log. Do not log every predicate evaluation. |
| Existing sensitive logs | `LogicManager.execute:50` logs raw commands; `ModelManager.java:45` includes domain/preferences debug output | The raw command logger is inherited/team code. Local logs can contain contact/remark/location data. No explicit privacy NFR or demonstrated breach makes this a definite course violation. Optional team review after release; no blanket logging rewrite proposed. |
| Rollback / persistence | Missing/null-list refusal, canonical migration, read-only behavior, shared save rollback and redo preservation | Current command/reload tests pass. Earlier issues are not counted again as unresolved. |
| Complexity / optimization | `getSourcePosition` invalidation cache and 1,000-client equality-call regression | Cache has a documented ordering/performance purpose and resets on source changes. No evidence justifies replacing it during freeze. |

The governing principles are [code quality](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/codeQuality.html)
and [error handling](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/errorHandling.html).
These conclusions are inspection results, not guarantees that every possible misuse is handled.

## E. QA audit

### E&E and test quality

The personally contributed test groups demonstrate substantive partitions: all four derived roles; empty property lists;
buying/selling order and duplicates; exact/mixed-role rankings; first/last/out-of-range displayed indexes; all six statuses;
all 36 status-transition pairs; omitted/empty/invalid/default/reset status; locale independence; legacy/current/mixed persistence;
missing/null/empty client lists; unchanged input files on refusal; and diagnostics after, rather than before, model mutation.
This is stronger evidence than counting methods. [R-Q](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html)
and [testing expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html) govern this assessment.

The ranking fixture deliberately places mixed clients before exact matches in source order, so sorted-index tests cannot
accidentally pass using source indexes. Integration tests check source data and saved JSON, not just feedback strings.
Logging tests use teardown; locale tests restore the default locale. No observed order dependence or need for a new testing abstraction was found.

Some parser tests derive a valid expected enum from the same enum parser (`PropertyCommandParserTest.java:34–39`).
Those are wiring tests; they would be weak as the only domain oracle. Independent literal enum/domain tests already cover the
mapping, so this is not treated as a confirmed defect. Production message constants are appropriate when checking error selection;
they do not verify message wording. Brendan’s latest unknown-value regression uses explicit expected text, addressing that limitation.

### Q1 — Missing interaction coverage, now improved

**Criterion:** [R-Q](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).
**Classification:** Strongly relevant; **Medium**; Test code; **Fix before v1.6 — fixed**; functional cost 0; regression risk low.
Existing undo tests cover ordinary filters and data changes, but did not combine the role-ranking cache, a role-changing property edit,
undo/redo, source-order persistence and a fresh-model reload. Added
`IntentionSearchIntegrationTest.execute_roleChangeUndoRedoAndReload_preservesRankedIndexesAndStoredOrder`.
It checks membership and exact ordering after every relevant transition, saved source order, and a new session’s empty undo history.
It adds a distinct interaction scenario, not a duplicate getter or equality test. No coverage-percentage increase is claimed.

### Q2 — Location-prefix boundary coverage, now improved

**Criterion:** [R-Q](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).
**Classification:** Strongly relevant; **Low**; Test code; **Fix before v1.6 — fixed**; functional cost 0; regression risk low.
`PropertyCommandParserTest` had numeric slash coverage but not the contrast between first-token alphabetic slashes and
prefix-like text after whitespace. Added one table-driven acceptance test for both add/edit and one rejection test with
an explicit expected diagnostic. These establish the exact behavior behind D1, without broadening production syntax.

### Confirmed API bugs: I4 reproductions

For each class independently, execute the following in a Java test with the normal project classpath:

```java
var a = new PhoneContainsKeywordsPredicate(List.of("9100"));
var b = new PhoneContainsKeywordsPredicate(List.of("9100"));
assertEquals(a, b); // passes
assertTrue(new HashSet<>(List.of(a)).contains(b)); // fails
```

Repeat with `new IntentionMatchesPredicate(ClientIntention.BUYER)` for both `a` and `b`; the same failure occurs.
Expected: equal predicates are interchangeable hash keys. Actual: identity hash codes are used and lookup fails.
Ownership, severity, fix cost and grading classification are in I4. No matching issue or active PR was found in the inspected
272 issue/PR records; issue #223 is a broader test-readability task, not this contract defect.
These are API-level correctness bugs; no client-facing command reproduction is claimed.
Fixing them requires functional code, so no failing test was committed.

### Confirmed documentation bug: original undo sequence

This reproduces a residual case after the earlier #262 correction, not a claim that its already-merged fix is still absent.
Use an empty disposable data file (`{"persons": []}`), then:

```text
add n/Existing Client p/91001111 e/client@example.com
addp 1 t/HDB i/Buying l/Bishan
# Record this as the state before the old manual sequence.
addp 1 t/HDB i/Buying l/Clementi
editp 1 1 l/Bishan
remark 1 r/hello
undo
undo
undo
```

Expected under the old DG: original Bishan property restored. Actual: `editp` reports unchanged, so the third undo removes
the setup property; the client has no properties. The application follows its documented no-op semantics correctly.
This is D2, not a production undo bug. Severity Medium for repeatability; practical-exam exposure comes from copied DG instructions.
The related #262 PR is merged and no active PR was present. Corrected instructions are committed; functional fix required: **no**.

### Independent command-level exploration

A disposable real `LogicManager` + parser + `ModelManager` + JSON storage probe exercised all requested command families.
It checked rejection types (`ParseException`/`CommandException`), unchanged data/bytes after invalid input, and saved/reloaded data.
It was not a hand-operated desktop session. The fresh-model reload models session restart; a separate manually controlled packaged-JAR
restart was not performed. JavaFX integration tests and screenshot capture provide additional UI/lifecycle evidence.

| Scenario | Exact inputs / checks | Result |
| --- | --- | --- |
| Property boundaries and case | `ADDP 1 T/h I/buying L/Tampines Street 1/2 PR/1 BD/1 S/Viewing`; second property at price `9223372036854775807`, bedrooms `20`, Completed | Accepted, values retained. |
| Filter combinations | `find p/+6591001111`, `find l/1/2`, `find s/viewing`, `find i/both`, `find pr/1-1`, `find pr/9223372036854775807-`, `find bd/20`, then `view 1` | Expected single client; reads do not change saved data. |
| Invalid client/index syntax | `add`, `edit 1`, `delete 0`, `delete 01`, `delete 2`, `view 2`, `remark 1`, `delp 1 3` | Expected parse/command errors; no source or file change. |
| Invalid property values | `addp 1 t/HDB i/Buying l/A pr/0`; `editp 1 1 bd/0`, `bd/21`, `pr/9223372036854775808`, `s/Not specified` | Rejected; existing detail data preserved. |
| Invalid searches / extra inputs | `find s/Not specified extra`, `find s/viewing n/A`, `find bd/0`, `find pr/2-1`; `help 1`, `exit now`, `list 1`, `undo 1`, `redo 1` | Rejected with expected exception families; no stored change. |
| Optional clearing and history | `editp 1 1 pr/ bd/ s/unspecified`; `find s/Not specified`; `help`; `undo`; `redo` | Optional fields cleared/restored and alias accepted. |
| Client update / deletion / reload | `find p/91001111`; `edit 1 n/Updated Client`; `remark 1 r/Call next week`; `delp 1 2`; `exit`; JSON reload; `list`; `delete 1`; `undo`; `redo` | Saved data survives reload; new session has no undo states; final deletion redo leaves empty data. |
| Alphabetic slash distinction | `addp 1 t/HDB i/Buying l/N/A` and `l/Park/Connector`; rejection of `l/Block MRT/Exit` | First-token literals accepted; whitespace-prefixed field-like token rejected. |

No additional user-facing production bug was independently reproduced in this matrix. This does not certify every possible
GUI interaction, filesystem failure, timing issue, platform or input. Existing save-failure regressions were also run in the full suite.
Test evidence is stored locally in `/private/tmp/residex-audit-evidence/GradingAuditProbeTest.java`, `probe-results.xml` and
`probe-output.txt`. The probe run intentionally has three failed assertions: the two hash bugs and old DG undo expectation.

### Coverage and significant limits

Final JaCoCo reports are at `build/reports/jacoco/coverage/html/index.html` and `build/reports/jacoco/coverage/coverage.xml`.
The figures below are class-level measurements, including shared code; they are not claimed as personal line coverage.

| Class/group | Lines covered | Branches covered | Meaningful remaining gap |
| --- | --- | --- | --- |
| FindCommandParser | 105/105 | 78/78 | Path coverage does not prove every input partition or diagnostic wording. |
| PropertyParserUtil | 49/49 | 18/18 | New slash tests add semantic partitions despite existing branch coverage. |
| ModelManager | 77/77 | 27/28 | Remaining equality branch, not demonstrated ranking failure. |
| Person | 67/68 | 35/37 | Defensive unsupported-enum path is unreachable with current enum; do not fabricate a state to inflate coverage. |
| IntentionMatchesPredicate | 12/12 | 16/16 | Missing hash method is invisible to coverage; I4 demonstrates the specification gap. |
| PhoneContainsKeywordsPredicate | 21/21 | 10/10 | Same absent-contract limitation. |
| PropertyStatusMatchesPredicate | 9/9 | 8/8 | No significant uncovered status path found. |
| PropertyIntention / PropertyStatus | 12/12 and 15/15 | 3/3 and no measured status branches | Literal domain and locale tests supplement coverage. |
| JsonAdaptedPerson / JsonAdaptedProperty / JsonSerializableAddressBook | 47/47, 54/54, 18/18 | 20/20, 21/21, 10/10 | No measured adapter gap; full lifecycle still requires integration evidence. |
| EditPropertyCommand | 37/37 | 20/20 | Shared descriptor separately measured in the report. |
| MainApp | 76/84 | 12/12 | Error-dialog lifecycle lambdas and a lock-release path remain uncovered; startup/UI tests cover related behavior separately. |
| Entire application | 2,265/2,358 (96.06%) | 860/903 (95.24%) | High totals do not remove I4 or guarantee no bugs. |

No blanket expansion of tests for uncovered `equals` permutations, trivial getters or unreachable enum states is recommended.
The course’s [testing expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html) do not set a coverage target.
PE bug-finding/triage performance remains future evidence and cannot be earned by this local self-audit.

## F. Documentation audit

### D1 — Overbroad property-location restriction, corrected

**Criterion:** [R-D](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html) and
[UG accuracy](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).
**Location:** `docs/UserGuide.md:266–267` at base; `PropertyParserUtil.parseFields:33–61` explains the boundary.
The old blanket warning against `MRT/Exit` anywhere in a location omitted that text immediately after `l/` is accepted.
Now specifies a preceding-whitespace boundary, rejected `l/Block MRT/Exit`, accepted `l/N/A`/`l/Park/Connector` and numeric slashes.
The existing Known issues statement distinguishing entry from `find l/N/A` was **correct** and retained.
Ownership: shared UG wording, primarily teammate text; this audit contributes the refinement, not original entry parsing.
Explicit requirement; Low; Documentation + Test code; **fixed before v1.6**; functional cost 0; regression risk low.
Related #228 is closed/merged; the residual general rule was outside its corrected Known issues sentence.

### D2 — Manual undo sequence depended on existing data, corrected

**Criterion:** [R-D](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html) and
[manual-test accuracy](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).
**Location:** `docs/DeveloperGuide.md:1908–1925` at base. Reproduction is in E.
Replaced the implicit first-client assumption with a newly created, phone-filtered Undo Test Client having no properties/remark.
The later read-only/no-op tests reselect this client after undo resets the display to all clients. This prevents stale indexes and
guarantees three actual changes in the initial sequence. The new creation is outside those three undo steps.
Ownership: Sean’s original sequence and Nathan’s recent expectation correction; no attribution removed.
Explicit requirement; Medium; Documentation; **fixed before v1.6**; functional cost 0; regression risk low.

### D3 — Glossary implied all find fields accept OR keywords, corrected

**Criterion:** [R-D](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).
**Location:** `docs/DeveloperGuide.md:1292` at base, Keyword definition.
The definition previously generalized whitespace/OR behavior to every `find` prefix, contradicting numeric/status single-value rules.
It now names `n/`, `e/`, `p/`, `l/` and contrasts one logical value, including `s/Not specified`.
This follows through the same distinction fixed earlier in UG PR #258; that PR did not correct this glossary sentence.
Explicit requirement; Low; Documentation; **fixed before v1.6**; functional cost 0; regression risk low.

### D4 — Help screenshot did not show the latest validation guidance, corrected

**Criterion:** [R-D](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
[website/screenshot consistency](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).
**Location:** `docs/images/helpMessage.png`, embedded by `docs/UserGuide.md:107`.
The base image’s name-rule text predates #272’s requirement that names start with a letter/digit. Generated a fresh image using
the established JavaFX capture task and inspected its actual text/buttons. No pixels were edited by hand.
The other three generated captures differed in viewport/scale or example status; their existing images did not demonstrate a
material accuracy problem, so they were restored byte-for-byte. No cosmetic image refresh is claimed as grading value.
Explicit requirement; Low; Documentation; **fixed before v1.6**; functional cost 0; regression risk low.

### D5 — Performance NFR is difficult to judge objectively

**Criterion:** [R-D](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), NFR scoping.
**Location:** `docs/DeveloperGuide.md:1270–1271` at base: “noticeable sluggishness”, “typical usage” and “most of the tasks”
do not establish a workload/platform or repeatable acceptance procedure. The 1,000-client ordering test counts equality calls;
it does not establish responsiveness of the GUI, persistence or every task.
Minimal recommendation: agree with the team on a small representative command workload and a realistic acceptance procedure,
then document what was actually measured. Do not invent a latency guarantee or claim a benchmark was run.
Explicit requirement relevance; Low; Documentation / Requires team coordination; **worth fixing if time permits**;
functional cost 0; risk medium if an unverified promise is written. Left unchanged because the acceptance target needs team agreement.

### Other UG/DG review results

* All 14 requested commands appear in the UG, command summary and Help/manual coverage. Current role/property direction,
  missing/optional fields, price/bedroom boundaries, reset syntax, case behavior, duplicate identity and failed-load recovery
  were cross-checked. No new command feature was added to the documentation.
* Recent fixes are present: search-only `Not specified`; source-order versus role-ranked results; read-only migration behavior;
  JDK 25 setup; Help undo/redo; bedrooms in display/model/storage documentation; save rollback/redo preservation; and detail restoration.
  These are not outstanding findings just because older issues mention them.
* Role/stage manual scenarios explicitly start from an empty JSON list and reselect clients after disappearing matches.
  The stage sequence correctly removes Stage Second from Viewing after its final matching property changes, then rejects client index 2.
  The derived-role sequence correctly reorders mixed roles before using the next displayed index. D2 was the remaining demonstrated sequence failure.
* Architecture/Logic/Model/Storage text, property/role ownership, adapter fields and undo APIs match the relevant implementation.
  The diagrams are intentionally partial. Omission of a low-level method or the VersionedAddressBook subclass from a high-level
  diagram is not automatically an error. No speculative UML rewrite was made.
* PlantUML source and generated diagrams were inspected, including Model, Storage and the undo Logic sequence. Bedrooms and
  `execute(model)` are present; the earlier #230/#231 concerns are addressed. Images render without Graphviz error placeholders.
* Implemented/planned user stories and use cases are clearly separated. No missing three-part story or wrongly labeled future
  feature was confirmed. UC11 contains low-level display/scrolling detail; trimming it is an optional presentation choice unless it
  hinders the reader. No cosmetic use-case rewrite was made.
* The glossary contains planned terms labeled accordingly. Removing all planned terms/stories would contradict the course’s
  allowance to retain upcoming requirements. The redundant/ambiguous Keyword definition was corrected specifically.
* Existing AB3, AB4, third-party and AI acknowledgements remain intact. Brendan’s broad AI statement already covers this kind of
  audit/test/documentation assistance; this report also discloses it. No new reuse source was silently introduced.
* Internal links/anchors/assets pass. One pre-existing external AboutUs portfolio link returns 404; see PM3. External pages were
  checked for availability, not re-audited for the correctness of every claim they contain.

## G. Product Design audit

The UG identifies property agents managing buyers and sellers, and a CLI value proposition. Contacts, per-client properties,
derived roles, stages, searches and undo support one coherent record-management workflow. `view` uses the same displayed indexes;
Help supplies copyable examples; stage corrections and no-op feedback are useful for data correction.
These observations support the [R-PD review](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html);
they do not establish how every target user would rate the product or how the team’s effort compares with AB3.

### P1 — Name restrictions exclude plausible Singapore clients

**Evidence:** `model/person/Name.java` validation and UG Known issues reject `李伟`, `Zoë Müller`, periods and slash-containing
names such as `Tan s/o Lim`; #233/#234/#236 are already tracked/closed as documented limitations. Real property agents may encounter
these names, so accurate documentation does not eliminate the usability tradeoff.
**Criterion:** [R-PD](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), target-user fit.
Strongly relevant; Medium as a workflow concern, not a proven official PE severity; Functional code / Requires team coordination.
**Defer until after v1.6**. Unicode-only validation/message expansion may cost roughly **4–12 changed lines** before tests,
but proper slash support affects prefix parsing and could cost **40+**, with wider regression exposure. These are team-owned validation
choices, not primarily Brendan’s original feature code. Do not add ad hoc aliases or quotation syntax during this audit.

### P2 — Multi-criterion property search is not provided

An agent might want type + price + bedrooms together. `find` supports one field and each query starts from all clients,
so sequential searches do not intersect results. This behavior is documented accurately. Its effect on usefulness needs target-user
evidence; no unimplemented multi-field promise was found in the current UG.
**Criterion:** [R-PD](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).
Optional improvement; Low; Functional code / Requires team coordination; **defer until after v1.6**.
A reliable conjunction design/parser would likely exceed **100 changed lines** across parser/predicates/help before tests;
no precise patch was prepared and no allowance should be reserved from this rough estimate. This is a feature suggestion, not a confirmed bug.

Other choices were not promoted to defects: permissive backward stage corrections, retaining Completed properties, independent
duplicate property entries and whole-dollar prices all match the declared product model. Do not enforce an invented transaction workflow.

## H. Project Management audit

### Contribution and review evidence

GitHub metadata confirms **18 authored PRs: 16 merged and 2 closed unmerged**, plus reviews on 17 PRs returned by the review search.
Review search counts PRs, not substantive review quality or individual review events. The following is the complete authored-PR list:

| PR | Outcome | Contribution |
| --- | --- | --- |
| [#10](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/10) | Closed, unmerged | Add remark command |
| [#19](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/19) | Merged | Add Brendan's Photo |
| [#31](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/31) | Merged | Update team member details in AboutUs.md |
| [#45](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/45) | Closed, unmerged | Update Developer Guide Use Cases |
| [#46](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/46) | Merged | Update RESIDEX use cases and glossary |
| [#48](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/48) | Merged | Update RESIDEX navbar |
| [#62](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/62) | Merged | Support find by phone number |
| [#84](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/84) | Merged | Standardise shortform commands |
| [#91](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/91) | Merged | Update guides and messages to RESIDEX branding |
| [#109](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/109) | Merged | Separate property intention from client intention |
| [#111](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/111) | Merged | Derive client intention from property intentions |
| [#113](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/113) | Merged | Track and filter property transaction status |
| [#114](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/114) | Merged | Revert "Add location filtering to the find command" |
| [#182](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/182) | Merged | Fix #152: Handle displayed unspecified status |
| [#183](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/183) | Merged | Fix #154: Add Viewing to sample properties |
| [#184](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/184) | Merged | Fix #151: Report missing client lists clearly |
| [#258](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/258) | Merged | Docs: Correct v1.6 command, migration and setup guidance |
| [#260](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/260) | Merged | Fix: Include unknown in Find errors and declare AI assistance |

Representative substantive review evidence:

* [#203 location-search review](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/203#discussion_r4409467030): identified
  alphabetic-slash limitations and requested explicit documentation/regression evidence. The teammate responded and the limitation is documented.
* [#203 readability comment](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/203#discussion_r4409474116): explained the
  unclear boolean helper argument; a named location helper is now present. This is concrete review impact, not just approval volume.
* [#261 review](https://github.com/AY2627S1-CS2103T-W11-4/tp/pull/261#pullrequestreview-5734580672): checked unchanged
  layout values, reported minimum-size Mac inspection, and queried the apparent test-count/skips discrepancy. The author clarified
  the POSIX-only tests. This is historical reviewer evidence; this audit does not claim to have performed that earlier manual inspection.

Current authored PR descriptions #182/#183/#184/#258/#260 give root causes, scope, validation and review focus.
They acknowledge unavailable manual checks instead of claiming them as passed. #10 has no description but was an unmerged tutorial;
it is not evidence that the final substantive PRs lack descriptions. The large role/status PRs are logically related features with
focused constituent commits, rather than a reason to artificially split more PRs now.

Brendan’s authored issues include feature definitions #61/#83/#108/#110/#112, testing findings #145–#149/#170 and
v1.6 findings #251–#253. Several #145/#146 records are exploratory-testing tasks, not automatically accepted unique bugs.
Milestones are present from v1.1 through v1.6; the inspected v1.6 milestone has 42 closed records and zero open records.
Closed issues do not by themselves prove every acceptance condition was met; current code/tests were used to cross-check relevant fixes.

| Course week | SGT dates | Direct non-merge commits on audited master |
| --- | --- | ---: |
| 7 | 28 Sep–4 Oct | 4 |
| 8 | 5–11 Oct | 1 |
| 9 | 12–18 Oct | 4 |
| 10 | 19–25 Oct | 18 |
| 11 | 26 Oct–1 Nov | 8 |
| 12 | 2 Nov to audit base | 8 |

This satisfies the visible four-week commit-presence criterion; it does not establish equal team-task effort by itself.
There is a week-10 implementation concentration, but documentation/QA/review evidence spans other weeks. No artificial activity is recommended.
Source: [R-PM](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html).

### PM1 — Final release/submission verification remains outstanding

**Criterion:** [R-PM](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
[Week 12 submissions](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html),
[deliverables](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).
**Evidence:** inspected releases are v1.3, v1.4, v1.4.1 and v1.5; no v1.6 release yet. v1.5 has one `Residex.jar` asset.
This is a pending pre-deadline task, **not an already missed deadline**. Canvas is not connected, so its submission cannot be verified.
Minimal action: team verifies the final matching source tag, single cross-platform executable, deployed UG/DG and correctly named
Canvas artifact (`[TEAM_ID][ProductName].jar`) before 3 November 14:00 SGT; confirm the uploaded file and timestamp.
Explicit requirement; High if left unfinished; Project management / Requires team coordination; **fix before v1.6**; functional cost 0.
No push, release, upload or external write was performed. PPP is optional; do not fabricate one to satisfy an obsolete expectation.

### PM2 — Hosted macOS job did not complete

**Criterion:** [R-PM](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), release readiness under
[Week 12 guidance](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html).
**Evidence:** [run 41466332847](https://github.com/AY2627S1-CS2103T-W11-4/tp/actions/runs/41466332847) for base SHA has
successful Windows/Linux jobs, while macOS is cancelled with no executed steps in the jobs response. The run-level state reports failure.
The MarkBind run is successful. Local Java 25/macOS full checks and screenshot capture pass.
Minimal action: verify a completed hosted macOS run or document/resolve runner availability with the team. No evidence justifies changing
source, dependencies or CI configuration. Strongly relevant; Medium; Project management / Requires team coordination;
**fix before v1.6 if practical**; functional cost 0. No remote workflow rerun was triggered by this audit.

### PM3 — AboutUs contains a broken teammate portfolio URL

**Criterion:** [R-D/R-PM](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html),
[product website](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html).
**Location:** `docs/AboutUs.md`, Ray’s portfolio link `https://rayyngg.github.io/tp/`; direct availability check returned HTTP 404.
The team’s own UG/DG and all checked internal targets are available. This is not a Brendan-authored portfolio link.
Minimal action: ask the owner for a valid target or remove the obsolete optional link after coordination. A missing optional PPP is
not itself a breach this semester. Strongly relevant; Low; Documentation / Requires team coordination; **fix before v1.6 if target is known**;
functional cost 0. Left unchanged because the intended correct destination is unverified.

Other management limits: teammate agreement about relative effort, private mentoring, tutor observations, hidden Canvas artifacts and
the upcoming PE cannot be inferred from GitHub counts. Existing configured Git identity matches Brendan’s previous linked GitHub commit
author (`Brendan-Lim`, `brendanlimkh04@gmail.com`); it was reused without changing configuration or claiming new signature verification.

## I. Prioritized action plan

Priority reflects evidence and deadline risk, not predicted marks. Each ID refers to the criterion-linked finding above.

| Priority | Finding | Grading criterion | File/line | Recommended fix | Change type | Functional LOC estimate | Regression risk | Fixed? |
| --- | --- | --- | --- | --- | --- | ---: | --- | --- |
| 1 | PM1 final matching release/submissions | R-PM; deliverables | GitHub releases / Canvas / deployed guides | Verify and submit before 3 Nov 14:00 SGT | Project management; team coordination | 0 | High deadline impact if omitted | Pending, external submission not authorized |
| 2 | PM2 cancelled hosted macOS check | R-PM; release readiness | Base CI run 41466332847 | Obtain completed run or resolve runner status | Project management; team coordination | 0 | Low source risk | No; local macOS passes |
| 3 | D2 undo instructions can undo setup state | R-D; manual testing | DG base 1908–1925 | Dedicated new client and reselect after filter changes | Documentation | 0 | Low | Yes |
| 4 | I1 public-constructor descriptions | R-I; Java Comments | Status predicate:16; intention predicate:16; FindCommand:81 | Add concise headers, only with approval | Functional code | 4 each; 12 total | Very low | No, prohibited scope |
| 5 | I4 equal predicate hash failures | R-I; Java contract | Phone predicate:50; intention predicate:35 | Matching hashCode methods and regressions | Functional code | 12 total | Low | No, prohibited scope |
| 6 | I2 partial parameter tags | R-I; Javadoc convention | Property:40–47 | Remove redundant partial tag | Functional code | 2; optional shared cleanup +5 | Very low | No, prohibited scope |
| 7 | Q1 ranked history/reload interaction gap | R-Q | IntentionSearchIntegrationTest:173 | Assert source/ranked order across history and reload | Test code | 0 | Low | Yes |
| 8 | D1/Q2 slash boundary guidance/tests | R-D / R-Q | UG base 266; PropertyCommandParserTest:69 | Exact whitespace boundary and regression partitions | Documentation; test code | 0 | Low | Yes |
| 9 | D3 glossary overgeneralizes OR | R-D | DG base 1292 | Restrict keyword rule to four fields | Documentation | 0 | Low | Yes |
| 10 | D4 screenshot validation text stale | R-D; website | helpMessage.png / UG:107 | Established capture task; inspect output | Documentation | 0 | Low | Yes |
| 11 | D5 vague NFR acceptance conditions | R-D; NFR scoping | DG base 1270–1271 | Agree a realistic measured acceptance procedure | Documentation; team coordination | 0 | Medium if promise is unverified | No |
| 12 | PM3 broken optional portfolio link | R-D / product website | AboutUs, Ray portfolio | Owner supplies correct URL | Documentation; team coordination | 0 | Low | No |
| 13 | I3 find parser SLAP/duplication | R-I; code quality | FindCommandParser:47–153 | Extract focused helpers after release | Functional code; team coordination | 80–140 | Medium | No; defer |
| 14 | P1 local-name limitations | R-PD; target users | Name validator / UG Known issues | Review validation/prefix policy after release | Functional code; team coordination | 4–12 Unicode-only; 40+ slash support | Medium/high | No; defer |
| 15 | P2 combined search workflow | R-PD, optional | FindCommandParser / UG find | Validate need and design after release | Functional code; team coordination | Roughly 100+; not a prepared patch | High | No; defer |

I1/I2 are the clearest coding-standard observations. I4 is independently reproduced correctness debt with limited present UI exposure.
I3 is a defensible maintainability concern, not a strict line-count violation. P1/P2 do not justify implementing new functionality now.

## J. Changes actually made

| File | Minimal change and purpose |
| --- | --- |
| `src/test/java/seedu/address/logic/IntentionSearchIntegrationTest.java` | One real-logic/storage test for role-changing edit, ranked membership/order, undo/redo and reload; 36 added lines. |
| `src/test/java/seedu/address/logic/parser/PropertyCommandParserTest.java` | Two tests distinguish literal first-token slashes from whitespace-prefixed unknown fields; 23 added lines. |
| `docs/UserGuide.md` | Exact location-prefix boundary and contrasting examples; 3 additions, 2 deletions. |
| `docs/DeveloperGuide.md` | Accurate glossary and deterministic/reselected-index undo instructions; 9 additions, 5 deletions. |
| `docs/images/helpMessage.png` | Fresh actual Help capture matching current name constraint; 119,034 → 115,647 bytes. |
| `docs/audits/brendan-v16-grading-audit.md` | This report, inventory, findings, evidence, validation and LOC accounting. |

Local logical commits:

* `920819b4` — `Test: Cover ranked history reloads and literal location slashes`.
* `cf0f7541` — `Docs: Make undo checks deterministic and clarify location syntax`.
* The final report is committed separately as `Docs: Record Brendan v1.6 grading audit`; its resolved SHA is in the completion record.

No active PR existed in either inspected open-PR query, including immediately before completion. Related documentation/test tasks
were already merged; the fixes above address the residual examples described here. No teammate branch was changed.
No production implementation, failing test, temporary build script, new dependency, Gradle/CI configuration, FXML, CSS or production resource was committed.

## K. Validation

| Check | Result / limitation |
| --- | --- |
| Baseline `./gradlew --no-daemon check coverage` | Passed, 870 tests, no failures/errors/skips. |
| Final same command | Passed, 873 tests, no failures/errors/skips; Checkstyle main/test and coverage generation passed. |
| Focused tests | Both changed test classes pass in the full suite; 15 intention integration tests and 25 property-parser tests. |
| Formatting | `git diff --check` and established `.github/run-checks.sh` pass; final staged checks also run. |
| MarkBind | Existing `npm ci` and `npm run build` from `docs/Documentation.md`; dependency lock/config unchanged. Final build includes this audit page. |
| PlantUML | Generated PNG signatures/nonempty content verified for all referenced diagrams; Model/Storage/undo Logic rendered images visually inspected. |
| Local links | All 646 generated HTML local links/anchors/image/script targets pass across 12 pages, including this report. All 18 generated diagrams have valid PNG signatures. |
| External links | 48 pre-report external URLs checked by HTTP HEAD; 47 succeeded and one portfolio URL returned 404 (PM3). Redirects followed. Anchors on external pages not exhaustively validated. |
| Screenshots | Established capture task passed. Existing/new Help images compared visually; three other existing captures retained. Manual UI behavior for every OS/window size not claimed. |
| Repository safety | Original untracked directories/worktrees preserved; no `src/main` diff, push, PR, merge, issue change, release or remote workflow rerun. |

Environment details: Java 25.0.3 Zulu with JavaFX; project Gradle wrapper 9.6.1; existing MarkBind 5.1.0 and Graphviz 16.1.0.
Initial sandbox attempts could not write the Gradle cache or reach npm/course/Composio endpoints. Authorized executions using the
existing cache/network access completed the required checks; connected GitHub reads replaced blocked Composio access.
An unprivileged MarkBind invocation returned success while Java rendering produced empty files. It was **not counted as valid**;
rerunning with Java rendering access generated real PNGs. This was an observed local sandbox limitation, not a confirmed deployed-site bug.
No dependency versions were changed. An initial new-test Checkstyle wrap/line-length error was corrected before the final passing run.

Evidence and command logs are retained at `/private/tmp/residex-audit-evidence/` (outside the repository). Relevant filenames:
`gradle-baseline.log`, `gradle-final.log`, `npm-ci.log`, `markbind-final.log`, `screenshots.log`, `links.json`, `external-links.json`,
`inventory.json`, `personal-prs.json`, dashboard JSONs, production blame/line-history snapshots and the isolated probe artifacts.
The final `completion.json` records exact HEAD, clean status, line totals and link counts after the report commit.
Canvas submission, final v1.6 published artifacts, peer evaluations, the PE and a full hand-operated desktop acceptance session remain unverified.

## L. Functional LOC report

```text
Base SHA: ecc0b424f86ec6361a7a3b66f61abee51ac7aea9
Final HEAD: audit/v16-brendan-grading-quality (resolved immutable SHA in completion.json and delivery message)

Functional files changed: 0
Functional lines added: 0
Functional lines deleted: 0
Total functional diff: 0

Test lines added: 59
Test lines deleted: 0

Documentation lines added: 886
Documentation lines deleted: 7

RepoSense attributed functional LOC: 562 (full dashboard, 2 Nov 2026 22:29:20 SGT)
Previously changed functional LOC during v1.6: 4 attributed surviving lines; Git 4 added + 2 deleted = 6
Official functional LOC allowance: max(150, 0.15 × 562) = 150
Remaining allowance: 146 on inspected dashboard; 144 using conservative Git additions + deletions
Unverified figures: final v1.6 dashboard totals; later edits/re-attribution; final published release and Canvas submission
```

The full SHA of a commit cannot be embedded in a file contained by that same commit without changing the SHA again.
Accordingly, the committed report names the final branch reference; the separately written completion record and delivery message
give its exact resolved commit SHA. This avoids labeling the preceding improvement commit as the final report HEAD.
Documentation line additions include this report, UG and DG text; the screenshot is one binary documentation file and is listed separately,
not counted as zero-byte work. The conservative diff total is additions + deletions, not a claim that RepoSense uses that formula.

The existing freeze change is commit `923d55012d67066d9fef72d19c833f37a59c2814` in `FindCommand.java`.
All earlier October 28 fixes precede the October 30 10:00 SGT freeze and are not added to this period’s allowance usage.
Source: [freeze dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-feature-freeze-dashboard/),
[team freeze data](https://nus-cs2103-ay2627-s1.github.io/tp-feature-freeze-dashboard/AY2627S1-CS2103T-W11-4_tp_master/commits.json),
[official freeze rule](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html).
Remaining capacity is a snapshot, not permission to consume it; this audit has consumed none.

## M. Final recommendations

### 1. Must fix before submission

* Complete PM1 with the team: final matching release/tag, single executable, correct Canvas artifact and deployed guides before
  **3 November 14:00 SGT**. Verify actual upload and deployment, not just closed issues.
* Review and integrate the already validated test/documentation commits if the team accepts them. They remain local by instruction;
  a local audit branch is not a submitted course deliverable.
* Resolve or explicitly verify the cancelled macOS hosted check, and recheck the final dashboard/source match.
  The clean local macOS suite is useful evidence but does not change the remote job’s recorded status.

### 2. Worth fixing if time permits

* With separate production approval, I1’s personally introduced status-constructor header (about 4 LOC) is the cheapest clear
  coding-standard correction. All three headers plus the redundant partial tag total about **14 conservative LOC**.
* I4’s two hash methods cost about **12 additional LOC** plus tests outside the functional limit. The strongest personal ownership
  is the phone predicate; the role predicate is shared debt. Both are low-risk, locally testable corrections, with modest present UI benefit.
  Together with the three headers/tag cleanup, the core proposals total about **26 LOC**, leaving about 118 by the conservative
  pre-audit accounting, if the estimates hold and no other changes occur. This is a proposed budget, not an implemented diff.
* Agree a defensible NFR acceptance procedure and repair the broken optional portfolio URL if its owner supplies a destination.
  Neither requires functional allowance. Avoid turning an unmeasured performance wish into a new guarantee.

### 3. Do not touch during feature freeze

* Do not spend most of the allowance refactoring the 107-line find dispatcher merely to satisfy a method-length heuristic.
  I3 deserves later maintenance, but 80–140 changed lines and parser-precedence regression risk are poor deadline tradeoffs.
* Do not expand names, add quoting/prefix escape rules, introduce multi-field search or impose a new one-way transaction workflow now.
* Do not add logging/assertions, cosmetic renames, extra getters/tests or abstractions solely to look active. Existing meaningful
  technique and E&E evidence is already present; the remaining concrete gaps are identified above.
* Do not alter archived branches, teammate worktrees, Gradle/CI/dependencies, existing acknowledgements or production code as part of this audit.

Any production follow-up should start from a separately reviewed minimal patch, include its own conservative and dashboard LOC check,
and be explicitly approved. **This branch’s functional-code change remains exactly zero.**
