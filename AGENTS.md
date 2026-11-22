# Architecture

Who’s In Today is a Kotlin Multiplatform app that shares its Android and iOS UI through Compose Multiplatform. Follow MVVM, unidirectional data flow (UDF), and Clean Architecture.

- Keep composables focused on rendering state and forwarding user actions to the ViewModel.
- Let ViewModels own and expose screen state, handle user actions, and coordinate application logic and repository calls.
- Keep domain models and business rules independent of Compose and SQLite. Put persistence queries and storage details in the data layer.

See [the project architecture notes](documentation/architecture.md).

# Local pre-PR checks

Run `./scripts/pre-pr.sh` after making code changes and before creating a pull request. Use its exit status to verify the local checks and report any test failures or environment limitations that prevented a complete run.
