package vn.edu.hcmute.topicmanagement.domain;

public enum Department {
    SOFTWARE_ENGINEERING("Công nghệ phần mềm"),
    INFORMATION_SYSTEMS("Hệ thống thông tin"),
    COMPUTER_SCIENCE("Khoa học máy tính"),
    COMPUTER_NETWORKS("Mạng máy tính");

    private final String label;
    Department(String label) { this.label = label; }
    public String getLabel() { return label; }
}
