# Architecture

Use MVVM with unidirectional data flow (UDF) and Clean Architecture.

## Localization

Android and iOS share [Compose string resources](../shared/src/commonMain/composeResources/values/strings.xml).
English is the fallback. Add a language in a sibling directory such as `values-de`.
Keep the resource names and numbered placeholders, use the target language’s plural
categories, and preserve the month and weekday array order. When shipping a new
language, declare it in the iOS app’s supported localizations and include it in the
Android locale configuration if the app exposes a system language picker.

Event date input and CSV exports use ISO dates (`YYYY-MM-DD`). Display dates and
user-facing validation messages use shared resources.
