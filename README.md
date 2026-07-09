# SpeakMate AI Friend

## 🚀 Overview

SpeakMate AI Friend is an AI-powered English speaking practice platform designed to help users improve their communication skills through interactive conversations, interview simulations, and personalized feedback.

The application provides a structured environment where users can practice English speaking, participate in AI-driven sessions, receive performance scores, and track their progress over time.

---

## ✨ Features

### 👤 User Management

* User Registration & Login
* JWT-Based Authentication & Authorization
* Role-Based Access Control (USER / ADMIN)
* Email Verification using OTP
* Secure Password Encryption with BCrypt

### 🎙️ Speaking Practice Sessions

* Create and manage speaking sessions
* Multiple conversation modes:

  * Interview Mode
  * Friend Mode
  * English Coach Mode
* Difficulty Levels:

  * Beginner
  * Intermediate
  * Advanced

### 💬 Conversation Management

* AI-generated questions
* User response tracking
* AI feedback generation
* Conversation scoring system
* Session-wise conversation history

### 📊 Analytics & Reports

* Total conversation count
* Session score calculation
* Average score analysis
* AI-generated performance reports

### 📧 Email Services

* OTP Generation & Verification
* Email Verification Workflow
* Secure Account Activation

### 🛡️ Security

* Spring Security Integration
* JWT Authentication Filter
* Stateless Authentication
* Secure API Endpoints
* Exception Handling & Validation

---

## 🏗️ System Architecture

```text
Client Application
        │
        ▼
REST Controllers
        │
        ▼
Service Layer
        │
        ▼
Repository Layer
        │
        ▼
MySQL Database
```

The application follows a layered architecture to ensure maintainability, scalability, and clean separation of concerns.

---

## 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT Authentication

### Database

* MySQL

### Utilities & Libraries

* Lombok
* Jakarta Validation
* Mailtrap / SMTP Integration

### Tools

* Maven
* Git & GitHub
* Postman
* Swagger

---

## 📂 Project Structure

```text
src/main/java/com/SpeakMate/Ai/friend

├── config
├── controller
├── dto
├── entities
├── enumeration
├── exception
├── mapper
├── repository
├── security
├── service
└── serviceImpl
```

---

## 🗄️ Database Entities

### User

Stores user information including:

* Name
* Username
* Email
* Password
* Mobile Number
* Education
* Occupation
* Country
* Role

### Session

Stores practice session details:

* Topic
* Mode
* Difficulty Level
* Status
* Start Time

### Conversation

Stores conversation records:

* AI Question
* User Answer
* AI Feedback
* Score

### EmailVerificationOtp

Stores OTP verification data:

* Email
* OTP
* Expiry Information

---

## 🔐 Authentication Flow

1. User registers an account.
2. OTP is sent to the registered email.
3. User verifies OTP.
4. JWT token is generated upon successful login.
5. Protected APIs require a valid JWT token.

---

## 📡 REST API Modules

### Authentication

* Register User
* Verify OTP
* Login User

### Session Management

* Create Session
* Get User Sessions
* Complete Session

### Conversation Management

* Submit Answer
* Generate Feedback
* Retrieve Session Conversations

### Reports

* Session Summary
* Performance Analysis
* AI Session Report

---

## ⚙️ Getting Started

### Prerequisites

* Java 21+
* Maven 3.9+
* MySQL 8+
* Git

### Clone Repository

```bash
git clone https://github.com/your-username/speakmate-ai-friend.git
cd speakmate-ai-friend
```

### Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/speakmate
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Configure JWT

```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Configure Email

```properties
spring.mail.host=your-mail-host
spring.mail.port=587
spring.mail.username=your-email
spring.mail.password=your-password
```

### Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Application will start at:

```text
http://localhost:8080
```

---

## 🧪 API Testing

Use:

* Postman
* Swagger UI

Swagger URL:

```text
http://localhost:8080/swagger-ui.html
```

---

## 🔮 Future Enhancements

* AI Integration using Groq / Gemini
* Speech-to-Text Support
* Text-to-Speech Responses
* Real-Time Voice Conversations
* Advanced Analytics Dashboard
* Admin Management Panel
* Progress Tracking & Learning Insights

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push the branch
5. Open a Pull Request

---

## 📄 License

This project is intended for educational and portfolio purposes.

---

## 👨‍💻 Author

**Kunal Ananda Sagar**

Java Full Stack Developer

LinkedIn: https://www.linkedin.com/in/kunal-sagar-9b8b25354/

GitHub: https://github.com/your-github-username

---

### Empowering learners to practice, improve, and gain confidence in English communication through AI-driven conversations.
