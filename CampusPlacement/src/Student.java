import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** A student with six semester marks (array) and unique skills (HashSet). */
public class Student extends User {
    public static final int SEMESTERS = 6;

    private final String department;
    private final int[] marks;                       // exactly six values, 0-100
    private final HashSet<String> skills = new HashSet<>();

    public Student(int id, String name, String department, int[] marks, Collection<String> skillInput) {
        super(id, name);
        this.department = cleanText(department, "Department");

        if (marks == null || marks.length != SEMESTERS) {
            throw new IllegalArgumentException("Exactly " + SEMESTERS + " semester marks are required.");
        }
        for (int mark : marks) {
            if (mark < 0 || mark > 100) {
                throw new IllegalArgumentException("Each mark must be an integer from 0 to 100.");
            }
        }
        this.marks = marks.clone();                  // defensive copy

        if (skillInput != null) {
            for (String raw : skillInput) {
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                if (raw.indexOf(',') >= 0 || raw.indexOf('|') >= 0
                        || raw.indexOf('\n') >= 0 || raw.indexOf('\r') >= 0) {
                    throw new IllegalArgumentException("Skill names cannot contain ',', '|' or line breaks.");
                }
                skills.add(raw.trim().toUpperCase(Locale.ROOT));   // java + JAVA -> one JAVA
            }
        }
    }

    public String getDepartment() {
        return department;
    }

    /** Returns a copy so outside code cannot change the stored marks. */
    public int[] getMarks() {
        return marks.clone();
    }

    /** Returns a sorted copy of the skills (sorted only for neat display and stable files). */
    public Set<String> getSkills() {
        return new TreeSet<>(skills);
    }

    /** Simple arithmetic mean of the six percentages (not a CGPA conversion). */
    public double getAverage() {
        int total = 0;
        for (int mark : marks) {
            total += mark;
        }
        return (double) total / marks.length;
    }

    @Override
    public void showProfile() {
        System.out.println("Student ID : " + getId());
        System.out.println("Name       : " + getName());
        System.out.println("Department : " + department);
        for (int i = 0; i < marks.length; i++) {
            System.out.println("Semester " + (i + 1) + "  : " + marks[i] + "%");
        }
        System.out.printf("Average    : %.2f%n", getAverage());
        System.out.println("Skills     : " + (skills.isEmpty() ? "None" : String.join(", ", getSkills())));
    }
}
