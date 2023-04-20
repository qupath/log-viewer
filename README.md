# Log Viewer

This project aims to replace QuPath's existing log viewer with something more powerful and searchable.

In doing so, the goal is to avoid depending upon anything QuPath-specific so as to have a more general, open-source, permissively-licensed log viewer that could be used in different JavaFX applications.

## Scope

The main aim is to support displaying 'live' logs generated using `slf4j-api` and (currently) `logback-classic`.

We might try to support different logging frameworks in the future.

## Approach

Log messages are shown in a JavaFX `TableView` (this *could* switch to a `TreeTableViewer` if needed).
These should be searchable, and include useful info regarding threads and exceptions.

Any extra text (e.g. stack traces) can be shown below the main table.
