public abstract class User {
    protected int userId;
    protected String username;
    protected String password;
    protected String name;
    protected String countryCode;
    protected String phone;
    protected String state;
    protected String city;
    protected String landmark;
    protected String houseNo;
    protected int age;
    protected String email;

    protected int assignedCount;
    protected String area;

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // Full Constructor for Officer Profile - now without a separate 'name' parameter
    public User(int userId, String username, String password, int assignedCount, String area) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = username; // We will use the username as the display name
        this.assignedCount = assignedCount;
        this.area = area;
    }

    // Full constructor for Citizen registration
    public User(int userId, String username, String password, String name, String countryCode, String phone,
                String state, String city, String landmark, String houseNo, int age, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.countryCode = countryCode;
        this.phone = phone;
        this.state = state;
        this.city = city;
        this.landmark = landmark;
        this.houseNo = houseNo;
        this.age = age;
        this.email = email;
    }

    public abstract void showMenu();
}