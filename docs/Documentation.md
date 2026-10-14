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


**Converting to PDF**

* See the guide [_se-edu/guides **Saving web documents as PDF files**_](https://se-education.org/guides/tutorials/savingPdf.html).
