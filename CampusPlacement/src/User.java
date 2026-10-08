/** Abstract base class for everyone who uses the system (Abstraction + Inheritance). */
public abstract class User {
    private final int id;          // set once, never changed (Encapsulation)
    private final String name;

    protected User(int id, String name) {
        this.id = id;
        this.name = cleanText(name, "Name");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Each child class supplies its own version (Polymorphism). */
    public abstract void showProfile();

    /**
     * Trims a text field and rejects blank values, '|' and line breaks,
     * because '|' separates fields in the data files.
     */
    static String cleanText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        if (value.indexOf('|') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(fieldName + " cannot contain '|' or line breaks.");
        }
        return value.trim();
    }
}
