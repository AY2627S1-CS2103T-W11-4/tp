---
  layout: default.md
  title: "Documentation guide"
  pageNav: 3
---

# Documentation Guide

* We use [**MarkBind**](https://markbind.org/) to manage documentation.
* The `docs/` folder contains the source files for the documentation website.
* To learn how set it up and maintain the project website, follow the guide [[se-edu/guides] Working with Forked MarkBind sites](https://se-education.org/guides/tutorials/markbind-forked-sites.html) for project documentation.

### Building locally

The Developer Guide uses PlantUML diagrams, which require Java and Graphviz to render.
Install the project's JDK and ensure `java -version` works. Install Graphviz with
`brew install graphviz` on macOS, or `sudo apt-get install graphviz` on Ubuntu.
Check the installation with `dot -V`.

From the `docs/` folder, run:

```bash
npm ci
npm run serve
```

Use `npm run build` to generate the website without starting a preview server.
Both commands check the dependencies before rendering and pass the detected `dot`
path to PlantUML. For a Graphviz installation outside `PATH`, set `GRAPHVIZ_DOT`
to the full path of its executable.

If diagrams show "Dot executable does not exist" or "Cannot find Graphviz",
the documentation build could not find Graphviz. Stop any running preview server,
install Graphviz, and restart with `npm run serve` to regenerate the images.
Refresh the browser and export the PDF again; an existing PDF keeps the old error images.

**Style guidance:**

* Follow the [**_Google developer documentation style guide_**](https://developers.google.com/style).
* Also relevant is the [_se-edu/guides **Markdown coding standard**_](https://se-education.org/guides/conventions/markdown.html).

### Refreshing UI screenshots

The README, homepage and User Guide share screenshots captured from the application's actual FXML and CSS.
Regenerate them after changes to the interface or command syntax, rather than editing amounts or labels into an old image.

From the repository root on a graphical desktop, run:

```bash
./gradlew captureDocumentationScreenshots
```

On Windows, use `.\gradlew.bat captureDocumentationScreenshots`.
On Linux without a desktop, use `xvfb-run -a ./gradlew captureDocumentationScreenshots`.

`DocumentationScreenshots` starts with bundled sample clients and adds example remarks and property statuses through
the command box. It captures the UI without opening a visible window and uses isolated sample storage under
`build/documentation-screenshots/data`, leaving the user's client data untouched.

| Image | Screen and documentation usage |
| --- | --- |
| `docs/images/Ui.png` | All clients, used by the README, homepage and Quick start |
| `docs/images/clientDetails.png` | `view 3`, used by the full-detail command section |
| `docs/images/findAlexDavidResult.png` | `find n/Alex David`, used by the search section |
| `docs/images/helpMessage.png` | The offline command reference, used by the help section |

Inspect all generated images before committing: client cards have no budget amount, prices remain on properties,
derived roles and transaction statuses are visible, and the detail screen contains the return-to-list reminder.
Keep the User Guide's formats, command summary and legacy-data notes consistent with the screenshots.


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
