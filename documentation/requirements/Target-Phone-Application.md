# Who’s In Today — Target phone application

Who’s In Today is an offline attendance register for one organiser and one association. It runs on Android and iOS. The organiser starts today’s event, marks people present as they arrive, and finishes check-in when attendance is complete. Each change is saved immediately, so the organiser can put the phone away, reopen the app later, and pick up where they left off.

## Features

### People

Build the roster by entering members’ names. Add new members and correct existing names as needed. Archive a member who leaves the association to exclude them from future checklists while preserving their attendance in past records.

### Start or resume an event

Today’s date is selected by default. The organiser can choose an earlier date to enter attendance after an event has taken place. The first version supports one event per date. Selecting a date with an existing record opens that record.

When an event is created, the app saves a snapshot of which members are active at that moment. Later roster changes leave that event’s membership intact. A member added later therefore has no attendance entry for older events that did not include them.

### Check in attendees

Tap anywhere on a person’s row to mark them present or undo the mark. The checklist stays alphabetical as people are ticked and shows a live count such as **8 of 12 present**. **Finish check-in** stays visible at the bottom.

An unticked name is still unchecked while check-in is in progress. Selecting **Finish check-in** completes the event and records the remaining unchecked members as absent.

### Review and correct attendance

The **Events** screen lists events newest first with their dates and attendance counts. Open a completed event to see who was present and who was absent. Select **Edit attendance** to correct the record. The organiser can also reopen an in-progress event and resume check-in.

### Export attendance

Export all completed events as a CSV and open the phone’s native share sheet to save or share the file. In-progress events are excluded. The CSV has one row per member and one column per completed event, ordered from oldest to newest. Event headers use the `YYYY-MM-DD` date format.

| Cell | Meaning |
| --- | --- |
| `1` | The member was present. |
| `0` | The member was on the event roster and was absent. |
| Blank | The member was outside the event roster. |

```csv
Name,2026-10-21,2026-11-04,2026-11-18
Amélie Rey,1,1,1
Jonas Berger,1,1,0
Mia Arnold,,,1
```

## Navigation and UI proposals

| Proposal | Opening screen | Navigation | Best suited to |
| --- | --- | --- | --- |
| A · Check-in first | Today’s checklist or **Start today’s event** | Check-in · Events · People · Export | Checking in people as they arrive |
| B · Events first | In-progress and past events with a prominent **New event** button | Events · People · Export | Entering attendance afterwards or frequently looking up past events |

The first version targets **Check-in first**. The organiser can start or resume today’s checklist from the opening screen. To enter attendance for an earlier date, use **Events → New event**.

The [Check-in first prototype](../ui-mockup/WhoIsInToday-Check-in-first.html) uses a sample roster and demonstrates ticking attendees, adding an event, editing a name, and downloading a sample CSV. Prototype data resets on reload.

## Technical requirements

Use Kotlin Multiplatform for shared attendance rules and CSV generation, and Compose Multiplatform for shared screens. Persist records on the phone in a local SQLite database. Use platform-specific code to open the native share sheet on Android and iOS.

| Table | Main fields |
| --- | --- |
| Member | Stable ID, display name, archived flag |
| Event | Stable ID, unique local calendar date, completion status |
| Attendance | Event ID, member ID, present flag, with one row per member in the event’s roster |

Create the event and its attendance rows in a single database transaction. Each tick or untick immediately updates the corresponding attendance row. The event’s completion status determines whether an unset present flag means unchecked or absent. Store event dates as calendar dates so a change of time zone leaves the recorded date intact.

## First-version scope

The first version supports one organiser using one device at a time. Attendance data is stored on that device and the core workflow works offline. Accessing the same records on another Android or iOS device would require a future sync or backup-and-restore feature.
