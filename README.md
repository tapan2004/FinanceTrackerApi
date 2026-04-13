# 💰 Personal Finance Tracker (Backend System)

## 🚀 Project Overview

The **Personal Finance Tracker** is a secure backend application designed to help users manage their daily financial activities, track expenses, set budgets, and analyze spending patterns.

This project demonstrates real-world backend development practices using **Java Spring Boot**, focusing on scalable architecture, secure authentication, and automated financial insights. The system supports transaction management, budgeting, reporting, and notifications, simulating the backend of a modern fintech-style application.

---

## 🎯 Problem Statement

Many individuals struggle to manage personal finances due to:

* Lack of visibility into daily expenses
* Difficulty tracking income and spending trends
* Poor budgeting and financial planning
* Manual record-keeping errors
* No automated financial alerts

Existing tools can be complex or require manual calculations. This project solves these problems by providing an automated backend system for tracking and analyzing financial data.

---

## 💡 Solution

I developed a **secure RESTful backend system** that allows users to manage their financial transactions and receive automated insights about their spending behavior.

The system:

* Tracks income and expense transactions
* Manages monthly budgets
* Generates financial summaries and reports
* Sends budget alert notifications
* Secures user data using JWT authentication

---

## 🏗️ System Architecture

Client Application
|
v
Spring Boot REST API (Java)
|
v
Service Layer (Business Logic)
|
v
Repository Layer (Data Access)
|
v
MySQL Database

### Architecture Highlights

* Layered architecture (Controller → Service → Repository)
* RESTful API design
* Stateless session management
* Secure authentication using JWT tokens
* Scalable and modular backend structure

---

## ⚙️ Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA / Hibernate
* REST APIs

### Database

* MySQL

### Features & Integrations

* JWT Authentication
* Role-based Authorization
* Email Verification (JavaMailSender)
* Scheduled Tasks (Spring Scheduler)
* Excel Report Generation (Apache POI)
* Cloud Image Upload (Cloudinary)

### Tools

* Git & GitHub
* Postman
* Maven
* Swagger API Documentation
* Docker (Basic)

---

## 🔐 Security Features

* JWT-based authentication
* Password encryption
* Role-based authorization
* Secure REST endpoints
* Input validation
* Exception handling

---

## 📊 Core Features

### 1. User Authentication

* User registration and login
* Secure JWT token generation
* Role-based access control

### 2. Transaction Management

* Add income and expense transactions
* Update and delete transactions
* Categorize expenses

### 3. Budget Management

* Set monthly spending limits
* Monitor budget usage
* Trigger alerts when budget exceeds threshold

### 4. Financial Analytics

* Monthly income and expense summary
* Category-wise spending analysis
* Financial trend tracking

### 5. Notifications

* Budget alert notifications
* Email verification system

---

## 📈 Example API Modules

* Authentication API
* User Profile API
* Transaction API
* Budget API
* Notification API
* Report Generation API

---

## 🧪 Testing

* API testing using **Postman**
* Validation testing
* Error handling testing
* Security testing

---

## 📦 Installation & Setup

### Clone the Repository

```
git clone https://github.com/tapan2004/Personal-Finance-Tracker.git
cd Personal-Finance-Tracker
```

### Run the Application

```
mvn clean install
mvn spring-boot:run
```

---

## 🔄 API Workflow Example

1. User registers and logs in
2. System generates JWT token
3. User adds income or expense transactions
4. Data is stored in MySQL database
5. System analyzes spending and generates reports
6. Notifications are triggered if budget limits are exceeded

---

## 📌 Real-World Use Cases

* Personal finance management
* Budget tracking applications
* Expense monitoring systems
* Fintech backend services

---

## 🧩 Future Improvements

* Add mobile or web frontend (React)
* Implement data visualization dashboards
* Integrate payment APIs
* Add cloud deployment (AWS / Render)
* Implement multi-currency support

---

## 👨‍💻 My Role in This Project

I independently designed and developed this backend system from scratch.

Responsibilities included:

* Designing system architecture
* Developing REST APIs using Spring Boot
* Implementing JWT authentication and authorization
* Designing database schema and relationships
* Integrating email notifications and scheduled tasks
* Testing APIs using Postman
* Optimizing database queries

---

## 📎 GitHub Repository

[https://github.com/tapan2004/Personal-Finance-Tracker](https://github.com/tapan2004/Personal-Finance-Tracker)

---

## 📬 Contact

**Tapan Manna**
Java Backend Developer | Backend & System Design Enthusiast

Email: [mannatapan588@gmail.com](mailto:mannatapan588@gmail.com)
LinkedIn: [https://www.linkedin.com/in/tapan-manna/](https://www.linkedin.com/in/tapan-manna/)
GitHub: [https://github.com/tapan2004](https://github.com/tapan2004)

---

## ⭐ Why This Project Stands Out

* Production-style backend architecture
* Secure authentication and authorization
* Real-world financial use case
* Automated reporting and notifications
* Designed using industry best practices
