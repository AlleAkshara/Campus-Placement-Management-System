/** A job posted by the admin. */
public class Job {
    private final int id;
    private final String companyName;
    private final String title;
    private final double minPercentage;

    public Job(int id, String companyName, String title, double minPercentage) {
        this.id = id;
        this.companyName = User.cleanText(companyName, "Company name");
        this.title = User.cleanText(title, "Job title");
        if (!(minPercentage >= 0 && minPercentage <= 100)) {   // also rejects NaN
            throw new IllegalArgumentException("Minimum percentage must be from 0 to 100.");
        }
        this.minPercentage = minPercentage;
    }

    public int getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getTitle() {
        return title;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public void showDetails() {
        System.out.printf("Job %d | %s | %s | Minimum average: %.2f%n",
                id, companyName, title, minPercentage);
    }
}
