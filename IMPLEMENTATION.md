# Implementation Notes — Final_project (Giảng viên & Quản lý đề tài)

## 1. Architecture Overview

```text
Browser (HTML / CSS / JS + Fetch API)
  │
  │ REST JSON + Cookie CSRF
  ▼
Spring MVC Controllers (ApiTopicController, ApiDepartmentTopicController, ApiStudentGroupController, ApiAuthController)
  │
  ▼
Service Layer (TopicService, DepartmentTopicService, StudentGroupService, UserService)
  │
  ▼
Spring Data JPA Repositories (TopicRepository, StudentGroupRepository, UserRepository, DepartmentRepository)
  │
  ▼
Hibernate 6 ORM ──▶ MySQL 8 Database
```

## 2. Dynamic Verification & Build Status

Chạy lệnh build và test:

```powershell
mvn -B clean package
```

Toàn bộ lớp mã nguồn Java, Controller, Service, Repository, DTO, Entity, Security, HTML/CSS/JS frontend và Unit Tests đã được đóng gói hoàn chỉnh trong gói WAR `Final_project.war`.
