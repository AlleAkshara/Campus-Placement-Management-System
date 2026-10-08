/** The single built-in administrator (ID 0, "College Admin"). */
public class Admin extends User {

    public Admin() {
        super(0, "College Admin");
    }

    @Override
    public void showProfile() {
        System.out.println("Admin ID: " + getId() + " | Name: " + getName());
    }
}
