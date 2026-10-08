import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads and writes the text files in the data folder (UTF-8, one record per line). */
public class FileManager {
    private final Path dataDir;
    private final Path studentsFile;
    private final Path jobsFile;
    private final Path applicationsFile;
    private final Path reportFile;

    public FileManager(String folder) {
        dataDir = Paths.get(folder);
        studentsFile = dataDir.resolve("students.txt");
        jobsFile = dataDir.resolve("jobs.txt");
        applicationsFile = dataDir.resolve("applications.txt");
        reportFile = dataDir.resolve("report.txt");
    }

    // ---------------------------------------------------------------- load

    public ArrayList<Student> loadStudents() throws IOException {
        ArrayList<Student> list = new ArrayList<>();
        for (String[] p : readRecords(studentsFile, 5)) {
            try {
                String[] markParts = p[3].split(",", -1);
                int[] marks = new int[markParts.length];
                for (int i = 0; i < markParts.length; i++) {
                    marks[i] = Integer.parseInt(markParts[i].trim());
                }
                Set<String> skills = new HashSet<>();
                if (!p[4].trim().isEmpty()) {
                    skills.addAll(Arrays.asList(p[4].split(",")));
                }
                list.add(new Student(Integer.parseInt(p[0].trim()), p[1], p[2], marks, skills));
            } catch (IllegalArgumentException e) {
                throw new IOException("Bad record in " + studentsFile + ": " + String.join("|", p)
                        + " (" + e.getMessage() + ")");
            }
        }
        return list;
    }

    public ArrayList<Job> loadJobs() throws IOException {
        ArrayList<Job> list = new ArrayList<>();
        for (String[] p : readRecords(jobsFile, 4)) {
            try {
                list.add(new Job(Integer.parseInt(p[0].trim()), p[1], p[2], Double.parseDouble(p[3].trim())));
            } catch (IllegalArgumentException e) {
                throw new IOException("Bad record in " + jobsFile + ": " + String.join("|", p)
                        + " (" + e.getMessage() + ")");
            }
        }
        return list;
    }

    public ArrayList<JobApplication> loadApplications() throws IOException {
        ArrayList<JobApplication> list = new ArrayList<>();
        for (String[] p : readRecords(applicationsFile, 4)) {
            try {
                list.add(new JobApplication(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                        Integer.parseInt(p[2].trim()), ApplicationStatus.valueOf(p[3].trim())));
            } catch (IllegalArgumentException e) {
                throw new IOException("Bad record in " + applicationsFile + ": " + String.join("|", p)
                        + " (" + e.getMessage() + ")");
            }
        }
        return list;
    }

    /** Reads non-blank lines and splits them on '|'. A missing file simply means "no records yet". */
    private List<String[]> readRecords(Path file, int expectedFields) throws IOException {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length != expectedFields) {
                    throw new IOException("Bad line " + lineNo + " in " + file
                            + ": expected " + expectedFields + " fields but found " + parts.length + ".");
                }
                records.add(parts);
            }
        }
        return records;
    }

    // ---------------------------------------------------------------- save

    public void saveStudents(List<Student> students) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Student s : students) {
            StringBuilder marks = new StringBuilder();
            int[] m = s.getMarks();
            for (int i = 0; i < m.length; i++) {
                if (i > 0) {
                    marks.append(',');
                }
                marks.append(m[i]);
            }
            lines.add(s.getId() + "|" + s.getName() + "|" + s.getDepartment() + "|" + marks
                    + "|" + String.join(",", s.getSkills()));
        }
        writeLines(studentsFile, lines);
    }

    public void saveJobs(List<Job> jobs) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Job j : jobs) {
            lines.add(j.getId() + "|" + j.getCompanyName() + "|" + j.getTitle() + "|" + j.getMinPercentage());
        }
        writeLines(jobsFile, lines);
    }

    public void saveApplications(List<JobApplication> applications) throws IOException {
        List<String> lines = new ArrayList<>();
        for (JobApplication a : applications) {
            lines.add(a.getId() + "|" + a.getStudentId() + "|" + a.getJobId() + "|" + a.getStatus());
        }
        writeLines(applicationsFile, lines);
    }

    public void saveReport(List<String> reportLines) throws IOException {
        writeLines(reportFile, reportLines);
    }

    public Path getReportFile() {
        return reportFile;
    }

    private void writeLines(Path file, List<String> lines) throws IOException {
        Files.createDirectories(dataDir);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
