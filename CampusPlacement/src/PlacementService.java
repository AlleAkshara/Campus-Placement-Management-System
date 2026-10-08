import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Holds the three record lists and performs the project actions. */
public class PlacementService {
    private final FileManager fileManager;
    private ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Job> jobs = new ArrayList<>();
    private ArrayList<JobApplication> applications = new ArrayList<>();

    public PlacementService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /** Loads all records; throws IOException (with a clear message) if any file cannot be read. */
    public void loadAll() throws IOException {
        ArrayList<Student> s = fileManager.loadStudents();
        ArrayList<Job> j = fileManager.loadJobs();
        ArrayList<JobApplication> a = fileManager.loadApplications();
        students = s;
        jobs = j;
        applications = a;
    }

    // ---------------------------------------------------------------- lookups

    public Student findStudent(int id) {
        for (Student s : students) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public Job findJob(int id) {
        for (Job j : jobs) {
            if (j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    public JobApplication findApplication(int id) {
        for (JobApplication a : applications) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    // IDs continue after the highest existing ID, so they stay correct after a restart.
    private int nextStudentId() {
        int max = 0;
        for (Student s : students) {
            max = Math.max(max, s.getId());
        }
        return max + 1;
    }

    private int nextJobId() {
        int max = 0;
        for (Job j : jobs) {
            max = Math.max(max, j.getId());
        }
        return max + 1;
    }

    private int nextApplicationId() {
        int max = 0;
        for (JobApplication a : applications) {
            max = Math.max(max, a.getId());
        }
        return max + 1;
    }

    // ---------------------------------------------------------------- actions

    /** Creates and saves a student. Throws IllegalArgumentException if any input is invalid. */
    public Student registerStudent(String name, String department, int[] marks, Collection<String> skills) {
        Student student = new Student(nextStudentId(), name, department, marks, skills);
        students.add(student);
        try {
            fileManager.saveStudents(students);
            System.out.println("Record saved.");
        } catch (IOException e) {
            reportSaveFailure(e);
        }
        return student;
    }

    public Job addJob(String company, String title, double minPercentage) {
        Job job = new Job(nextJobId(), company, title, minPercentage);
        jobs.add(job);
        try {
            fileManager.saveJobs(jobs);
            System.out.println("Record saved.");
        } catch (IOException e) {
            reportSaveFailure(e);
        }
        return job;
    }

    public void applyForJob(int studentId, int jobId) {
        Student student = findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }
        Job job = findJob(jobId);
        if (job == null) {
            System.out.println("Job not found.");
            return;
        }
        double average = student.getAverage();
        if (average < job.getMinPercentage()) {
            System.out.printf("Not eligible: your average %.2f is below the minimum %.2f.%n",
                    average, job.getMinPercentage());
            return;
        }
        for (JobApplication a : applications) {
            if (a.getStudentId() == studentId && a.getJobId() == jobId) {
                System.out.println("You have already applied for this job.");
                return;
            }
        }
        JobApplication application = new JobApplication(nextApplicationId(), studentId, jobId,
                ApplicationStatus.PENDING);
        applications.add(application);
        System.out.printf("Minimum average: %.2f%n", job.getMinPercentage());
        try {
            fileManager.saveApplications(applications);
            System.out.println("Application " + application.getId() + " saved. Status: "
                    + application.getStatus());
        } catch (IOException e) {
            reportSaveFailure(e);
            System.out.println("Application " + application.getId() + " created in memory. Status: "
                    + application.getStatus());
        }
    }

    public void updateStatus(int applicationId, ApplicationStatus newStatus) {
        JobApplication application = findApplication(applicationId);
        if (application == null) {
            System.out.println("Application not found.");
            return;
        }
        if (!application.updateStatus(newStatus)) {
            System.out.println("Cannot change: only a PENDING application can be set to SELECTED or REJECTED.");
            return;
        }
        try {
            fileManager.saveApplications(applications);
            System.out.println("Application status saved.");
        } catch (IOException e) {
            reportSaveFailure(e);
        }
    }

    // ---------------------------------------------------------------- display

    public void showStudents() {
        if (students.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Student s : students) {
            System.out.printf("Student %d | %s | %s | Average: %.2f%n",
                    s.getId(), s.getName(), s.getDepartment(), s.getAverage());
        }
    }

    public void showJobs() {
        if (jobs.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Job j : jobs) {
            j.showDetails();
        }
    }

    public void showAllApplications() {
        if (applications.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (JobApplication a : applications) {
            Student s = findStudent(a.getStudentId());
            Job j = findJob(a.getJobId());
            System.out.println("Application " + a.getId()
                    + " | Student " + a.getStudentId() + " " + (s == null ? "?" : s.getName())
                    + " | Job " + a.getJobId() + " " + (j == null ? "?" : j.getCompanyName() + " - " + j.getTitle())
                    + " | " + a.getStatus());
        }
    }

    public void showStudentApplications(int studentId) {
        boolean found = false;
        for (JobApplication a : applications) {
            if (a.getStudentId() == studentId) {
                Job j = findJob(a.getJobId());
                System.out.println("Application " + a.getId() + " | "
                        + (j == null ? "Job " + a.getJobId() : j.getTitle() + " (" + j.getCompanyName() + ")")
                        + " | " + a.getStatus());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No records found.");
        }
    }

    // ---------------------------------------------------------------- reports

    private List<String> buildReportLines() {
        int pending = 0;
        int selected = 0;
        int rejected = 0;
        for (JobApplication a : applications) {
            switch (a.getStatus()) {
                case PENDING -> pending++;
                case SELECTED -> selected++;
                case REJECTED -> rejected++;
            }
        }
        List<String> lines = new ArrayList<>();
        lines.add("Total students: " + students.size());
        lines.add("Total jobs: " + jobs.size());
        lines.add("Total applications: " + applications.size());
        lines.add("PENDING applications: " + pending);
        lines.add("SELECTED applications: " + selected);
        lines.add("REJECTED applications: " + rejected);
        return lines;
    }

    public void showReport() {
        for (String line : buildReportLines()) {
            System.out.println(line);
        }
    }

    public void saveReport() {
        try {
            fileManager.saveReport(buildReportLines());
            System.out.println("Report saved to " + fileManager.getReportFile() + ".");
        } catch (IOException e) {
            System.out.println("Error: report was NOT saved (" + e.getMessage() + ").");
        }
    }

    private void reportSaveFailure(IOException e) {
        System.out.println("Error: could not save to file (" + e.getMessage() + ").");
        System.out.println("The change exists only in memory for this session.");
    }
}
