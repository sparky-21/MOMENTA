# MOMENTA — Phase 6: Google Calendar-Style Calendar + Events

This ZIP contains the MOMENTA project with Phase 5 functionality preserved and a redesigned Phase 6 Calendar.

## Phase 6 features

- Google Calendar-style monthly grid (7 columns × 6 weeks)
- Previous month / next month navigation
- Today button
- Selected date display
- Current day highlight
- Events displayed directly inside calendar date cells
- Tasks/deadlines from the existing Phase 5 Task module displayed on their deadline dates
- Add Event button
- Double-click an empty date cell to create an event for that date
- Click an event to edit it
- Event persistence through SQLite
- Existing DAO → Service → Controller architecture preserved
- Existing Phase 5 Goals, Projects and Tasks preserved

## Event flow

CalendarController → EventService → EventDAO → SQLite

Tasks are read from the existing TaskService/TaskDAO and are not duplicated in the Calendar module.

## Run

From the project root:

    mvn clean javafx:run

## Phase 6 Git workflow

1. Start from the Phase 5 branch:

    git switch momenta-phase-5-goals-projects
    git pull origin momenta-phase-5-goals-projects

2. Create the Phase 6 branch:

    git switch -c momenta-phase-6-calendar-events

3. Test the application, then commit:

    git add .
    git commit -m "Implement phase 6 Google Calendar style calendar"

4. Push:

    git push -u origin momenta-phase-6-calendar-events

5. Open a Pull Request from:

    momenta-phase-6-calendar-events

   into:

    momenta-phase-5-goals-projects

The file PHASE6_GIT_COMMANDS.txt contains the commands in order.
