# Campus Placement Console Application (Java 21)

Menu-driven console app: students register, view jobs, apply, and check status;
the admin adds jobs, records results, and views/saves reports. Data is stored in
text files inside the `data/` folder (created automatically on first save).

## Structure
```
CampusPlacement/
├── src/   Main, PlacementService, FileManager, User (abstract), Admin, Student,
│          Job, JobApplication, ApplicationStatus (enum)
└── data/  students.txt, jobs.txt, applications.txt, report.txt (auto-created)
```

## Run in IntelliJ IDEA
1. File > Open... and choose the `CampusPlacement` folder (Create project from existing sources if asked).
2. File > Project Structure > Project: set SDK to JDK 21 (Add SDK > Download JDK if needed), Language level 21.
3. Project Structure > Modules > Sources: make sure `src` is marked blue (Sources).
4. Open `src/Main.java`, click the green arrow next to `main` > Run 'Main.main()'.
5. Run > Edit Configurations: confirm *Working directory* is the project root, so `data/` is created beside `src/`.

## Run from a terminal
```
javac -d out src/*.java
java -cp out Main
```
