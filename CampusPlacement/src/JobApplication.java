/** One student's application for one job. */
public class JobApplication {
    private final int id;
    private final int studentId;
    private final int jobId;
    private ApplicationStatus status;

    public JobApplication(int id, int studentId, int jobId, ApplicationStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.jobId = jobId;
        this.status = (status == null) ? ApplicationStatus.PENDING : status;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getJobId() {
        return jobId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    /**
     * Only a PENDING application can become SELECTED or REJECTED.
     * Returns false (and changes nothing) for any other change.
     */
    public boolean updateStatus(ApplicationStatus newStatus) {
        if (status != ApplicationStatus.PENDING
                || newStatus == null
                || newStatus == ApplicationStatus.PENDING) {
            return false;
        }
        status = newStatus;
        return true;
    }
}
