package vn.edu.hcmute.student;

import jakarta.persistence.*;
import java.time.Instant;

// The module owns student/group data. Topic and registration are integration tables.
@Entity @Table(name="sv_student")
class Student {
    @Id public String id;
    @Column(nullable=false) public String name;
    public String className;
    @Column(nullable=false) public String passwordHash;
    public Long groupId;
    protected Student() {}
    Student(String id, String name, String className, String hash) {
        this.id=id; this.name=name; this.className=className; this.passwordHash=hash;
    }
}

@Entity @Table(name="sv_group")
class StudentGroup {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,length=80) public String name;
    @Column(nullable=false) public String leaderId;
    public Instant createdAt;
    protected StudentGroup() {}
    StudentGroup(String name, String leaderId, Instant now) { this.name=name; this.leaderId=leaderId; this.createdAt=now; }
}

@Entity @Table(name="sv_invitation", uniqueConstraints=@UniqueConstraint(columnNames={"groupId","studentId"}))
class Invitation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long groupId;
    @Column(nullable=false) public String studentId;
    @Column(nullable=false) public String status;
    public Instant createdAt;
    protected Invitation() {}
    Invitation(Long groupId,String studentId,Instant now) {this.groupId=groupId;this.studentId=studentId;status="PENDING";createdAt=now;}
}

@Entity @Table(name="sv_topic")
class Topic {
    @Id public String id;
    @Column(nullable=false) public String title;
    @Column(length=3000) public String description;
    public String department;
    public String type;
    public String supervisor;
    public String technologies;
    public boolean published;
    public int capacity;
    public Instant opensAt;
    public Instant closesAt;
    public Instant reportDueAt;
}

@Entity @Table(name="sv_registration",uniqueConstraints=@UniqueConstraint(columnNames="groupId"))
class Registration {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long groupId;
    @Column(nullable=false) public String topicId;
    @Column(nullable=false) public String status;
    public Instant submittedAt;
    @Column(length=1000) public String feedback;
}

@Entity @Table(name="sv_report")
class Report {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long groupId;
    public String submittedBy;
    public String filename;
    public String contentType;
    public String stage;
    @Column(length=1000) public String note;
    public Instant submittedAt;
    public boolean late;
    @Lob @Column(length=10485760) public byte[] content;
}
