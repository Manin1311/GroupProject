public class Complaint {
    private int complaintId;
    private int userId;
    private String area;
    private String type;
    private String description;
    private String status;
    private int officerId;
    private String filedOn;

    // Constructor
    public Complaint(int complaintId, int userId, String area, String type, String description, String status, int officerId, String filedOn) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.area = area;
        this.type = type;
        this.description = description;
        this.status = status;
        this.officerId = officerId;
        this.filedOn = filedOn;
    }

    public int getComplaintId()
    {
        return complaintId;
    }

    @Override
    public String toString() {
        return "Complaint ID: " + complaintId +
                "\nArea      : " + area +
                "\nType      : " + type +
                "\nDescription: " + description +
                "\nStatus    : " + status +
                "\nOfficer ID: " + officerId +
                "\nFiled On  : " + filedOn +
                "\n-----------------------------";
    }

    public void display() {
        System.out.println("Complaint ID: " + complaintId);
        System.out.println("Type: " + type);
        System.out.println("Area: " + area);
        System.out.println("Status: " + status);
        System.out.println("Description: " + description);
        System.out.println("Filed On: " + filedOn);
        System.out.println("Officer ID: " + officerId);
        System.out.println("-------------");
    }
}
