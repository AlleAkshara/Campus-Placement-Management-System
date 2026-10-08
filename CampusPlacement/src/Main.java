import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Console menus and input. Business logic lives in PlacementService. */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static PlacementService service;
    public static void main(String[] args) {
        service = new PlacementService(new FileManager("data"));
        try {
            service.loadAll();
            System.out.println("Records loaded.");
        } catch (IOException e) {
            // Stop here so a bad file is never overwritten by an empty session.
            System.out.println("Error: could not load data files. " + e.getMessage());
            System.out.println("Fix or remove the file in the data folder and start again.");
            return;
        }
        try {
            showMainMenu();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ended. Exiting.");
        }
    }

    // ---------------------------------------------------------------- menus

    private static void showMainMenu() {
        while (true) {
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("1. Register student");
            System.out.println("2. Admin menu");
            System.out.println("3. Student menu");
            System.out.println("0. Exit");
            switch (readInt("Enter choice: ")) {
                case 1 -> registerStudent();
                case 2 -> showAdminMenu();
                case 3 -> {
                    int id = readInt("Enter your student ID: ");
                    if (service.findStudent(id) == null) {
                        System.out.println("Student not found.");
                    } else {
                        showStudentMenu(id);
                    }
                }
                case 0 -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showAdminMenu() {
        Admin admin = new Admin();
        User user = admin;                       // polymorphism: User reference, Admin object
        user.showProfile();
        while (true) {
            System.out.println();
            System.out.println("ADMIN MENU");
            System.out.println("1. View all students");
            System.out.println("2. Add a job");
            System.out.println("3. View all jobs");
            System.out.println("4. View all applications");
            System.out.println("5. Update application status");
            System.out.println("6. View reports");
            System.out.println("0. Back to main menu");
            switch (readInt("Enter choice: ")) {
                case 1 -> service.showStudents();
                case 2 -> addJob();
                case 3 -> service.showJobs();
                case 4 -> service.showAllApplications();
                case 5 -> updateApplicationStatus();
                case 6 -> showReports();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showStudentMenu(int studentId) {
        while (true) {
            System.out.println();
            System.out.println("STUDENT MENU (Student ID " + studentId + ")");
            System.out.println("1. View my profile");
            System.out.println("2. View jobs");
            System.out.println("3. Apply for a job");
            System.out.println("4. View my applications");
            System.out.println("0. Back to main menu");
            switch (readInt("Enter choice: ")) {
                case 1 -> {
                    User user = service.findStudent(studentId);   // polymorphism: Student version runs
                    user.showProfile();
                }
                case 2 -> service.showJobs();
                case 3 -> {
                    service.showJobs();
                    service.applyForJob(studentId, readInt("Enter job ID: "));
                }
                case 4 -> service.showStudentApplications(studentId);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ---------------------------------------------------------------- actions with input

    private static void registerStudent() {
        System.out.println("Register student (leave a field empty to cancel)");
        String name = readText("Name: ");
        if (name.isEmpty()) {
            System.out.println("Registration cancelled.");
            return;
        }
        String department = readText("Department: ");
        if (department.isEmpty()) {
            System.out.println("Registration cancelled.");
            return;
        }
        int[] marks = readMarks();
        if (marks == null) {
            System.out.println("Registration cancelled.");
            return;
        }
        String skillLine = readText("Skills (comma separated, optional): ");
        List<String> skills = new ArrayList<>();
        for (String part : skillLine.split(",")) {
            skills.add(part);
        }
        try {
            Student student = service.registerStudent(name, department, marks, skills);
            System.out.println("Student ID: " + student.getId());
            System.out.printf("Average: %.2f%n", student.getAverage());
            System.out.println("Unique skills: "
                    + (student.getSkills().isEmpty() ? "None" : String.join(", ", student.getSkills())));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + " Student was not created.");
        }
    }

    /** Asks until six valid marks are entered; an empty line cancels (returns null). */
    private static int[] readMarks() {
        while (true) {
            String line = readText("Semester marks (6 whole numbers 0-100, separated by spaces): ");
            if (line.isEmpty()) {
                return null;
            }
            String[] parts = line.split("[\\s,]+");
            if (parts.length != Student.SEMESTERS) {
                System.out.println("Error: enter exactly " + Student.SEMESTERS + " marks.");
                continue;
            }
            int[] marks = new int[Student.SEMESTERS];
            boolean valid = true;
            for (int i = 0; i < parts.length && valid; i++) {
                try {
                    marks[i] = Integer.parseInt(parts[i]);
                    if (marks[i] < 0 || marks[i] > 100) {
                        System.out.println("Error: each mark must be from 0 to 100.");
                        valid = false;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: '" + parts[i] + "' is not a whole number.");
                    valid = false;
                }
            }
            if (valid) {
                return marks;
            }
        }
    }

    private static void addJob() {
        String company = readText("Company name: ");
        String title = readText("Job title: ");
        double min = readPercentage("Minimum average percentage (0-100): ");
        try {
            Job job = service.addJob(company, title, min);
            System.out.println("Job ID: " + job.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + " Job was not created.");
        }
    }

    private static void updateApplicationStatus() {
        service.showAllApplications();
        int id = readInt("Enter application ID: ");
        JobApplication application = service.findApplication(id);
        if (application == null) {
            System.out.println("Application not found.");
            return;
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("Cannot change: application " + id + " is already " + application.getStatus() + ".");
            return;
        }
        System.out.println("1. SELECTED");
        System.out.println("2. REJECTED");
        System.out.println("0. Cancel");
        switch (readInt("New status: ")) {
            case 1 -> service.updateStatus(id, ApplicationStatus.SELECTED);
            case 2 -> service.updateStatus(id, ApplicationStatus.REJECTED);
            case 0 -> System.out.println("Cancelled.");
            default -> System.out.println("Invalid choice. No change made.");
        }
    }

    private static void showReports() {
        service.showReport();
        String answer = readText("Save report to report.txt? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            service.saveReport();
        }
    }

    // ---------------------------------------------------------------- input helpers

    private static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            String line = readText(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readPercentage(String prompt) {
        while (true) {
            String line = readText(prompt);
            try {
                double value = Double.parseDouble(line);
                if (value >= 0 && value <= 100) {
                    return value;
                }
                System.out.println("Percentage must be from 0 to 100.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number such as 70 or 62.5.");
            }
        }
    }
}
