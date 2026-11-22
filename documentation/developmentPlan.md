| # | Subtask | Done when | Depends on |
| ---: | --- | --- | --- |
| 1 | Set up the shared app shell | Compose navigation provides Events, People, and Export tabs. | — |
| 2 | Add cross-platform SQLite | Android and iOS can open the local database, with a migration path. | — |
| 3 | Define attendance records | Member, Event, and Attendance tables enforce unique dates and event/member pairs; event dates are timezone-safe calendar dates. | 2 |
| 4 | Implement member management | Add and rename members; trim and validate names; archive and restore without losing historical records. | 3 |
| 5 | Build the People screen | Alphabetical roster with add/edit flows and clear archive/restore actions and save feedback. | 1, 4 |
| 6 | Create events and snapshot the roster | Start an event for a selected date; if that date already exists, open it. Create the event and active-member attendance rows in one transaction. | 4 |
| 7 | Implement check-in persistence | Each tap immediately saves presence; reopening an in-progress event restores the saved checklist. | 6 |
| 8 | Build the event attendance screen | Show the date, alphabetical whole-row checkboxes, live present/total count, progress, and a bottom-pinned Finish button. | 1, 7 |
| 9 | Resume and finish check-in | Selecting an in-progress event from Events resumes its saved checklist. Finishing makes unchecked members absent and completes the event. | 8 |
| 10 | Build Events and event details | List in-progress events separately from past events, newest first; show date and attendance count. Completed details display present/absent status and offer Edit attendance. | 9 |
| 11 | Support historical edits | Explicitly enter edit mode for completed events; save corrections immediately without changing the original roster snapshot. | 7, 10 |
| 12 | Generate the CSV | Export completed events only, oldest date first; use 1, 0, or blank for outside-roster members, with correct CSV escaping and UTF-8 output. | 3, 9 |
| 13 | Build Export and native sharing | Show a CSV preview and share the generated file through Android and iOS share sheets. | 12 |
| 14 | Verify the full flow on both platforms | Check roster changes, resume after restart, backdated and duplicate dates, edits, export, and the mockup’s key layouts and interactions. | 5, 11, 13 |
