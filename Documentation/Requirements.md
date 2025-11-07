## Waiter Wallet: Functional Requirements Document

### 1. Introduction

This document outlines the functional requirements for the "Waiter Wallet" Android mobile application. This application is designed to be a comprehensive tool for waiters to track their daily turnover, tips, and monthly commissions, providing them with a clear overview of their earnings.

**1.1. Purpose**

The primary purpose of this application is to provide waiters with a simple and efficient way to monitor their income, set financial goals, and gain insights into their earnings over time.

**1.2. Scope**

The initial version of the application will focus on the core functionalities of user authentication, data entry for earnings, automated calculations, data visualization through a calendar and overview screen, and a notification system for reminders and goal achievements.

**1.3. Target Audience**

The target audience for this application is waiters and other service industry professionals who receive tips and commissions as a significant portion of their income.

### 2. User Authentication and Security

To ensure the security and privacy of user data, the application will implement modern security standards for user accounts.

**2.1. User Registration**

*   **FR-001:** The system shall allow new users to register for an account using a valid email address and a password.
*   **FR-002:** Passwords must be at least 12 characters long and include a combination of uppercase letters, lowercase letters, numbers, and special characters.
*   **FR-003:** The system shall implement real-time password strength validation and provide feedback to the user.
*   **FR-004:** Upon successful registration, the system shall send a verification email to the user's provided email address. The user must click a verification link to activate their account.
*   **FR-005:** All user data, especially passwords, will be securely hashed and salted before being stored in the database.

**2.2. User Login**

*   **FR-006:** Registered users shall be able to log in to their accounts using their email address and password.
*   **FR-007:** The system shall provide a "Forgot Password" functionality that allows users to reset their password via a secure link sent to their registered email address.
*   **FR-008:** To enhance security, the system will support biometric authentication (fingerprint or face recognition) for logging in, if the user's device supports it.
*   **FR-009:** The application will implement secure token-based authentication to manage user sessions.

**2.3. User Profile Management**

*   **FR-010:** Users shall be able to view and edit their profile information, including their name and email address.
*   **FR-011:** Users shall be able to change their password from within the application after successful authentication.

### 3. Earnings Data Entry

The application will provide a user-friendly interface for waiters to input their daily earnings and monthly commissions.

**3.1. Daily Turnover and Tips**

*   **FR-012:** From the main dashboard, users shall be able to select a date to enter their total turnover and total tips for that day.
*   **FR-013:** The input fields for turnover and tips shall accept numeric values.
*   **FR-014:** The system shall allow users to differentiate between cash tips and credit card tips (as an optional feature).
*   **FR-015:** Users shall be able to add notes to each daily entry (e.g., "Busy night," "Worked a double shift").

**3.2. Monthly Commission**

*   **FR-016:** At the end of each month, users shall be able to enter their total commission received.
*   **FR-017:** The input field for commission shall accept numeric values.

### 4. Earnings Calculation and Display

The application will automatically calculate and display key earnings metrics to provide users with valuable insights into their income.

**4.1. Total Tips Calculation**

*   **FR-018:** The system shall calculate and display the user's total tips for the current month by summing up all daily tip entries for that month.
*   **FR-019:** The total tips for the month shall be updated in real-time as new entries are added.

**4.2. Commission Calculation**

*   **FR-020:** The system shall calculate the user's commission earned so far for the current month by taking 1% of their total turnover for the month.
*   **FR-021:** This "estimated commission" will be clearly labeled to distinguish it from the final commission entered by the user.

### 5. Data Visualization

The application will feature intuitive visualizations to help users understand their earnings at a glance.

**5.1. Calendar View**

*   **FR-022:** The app shall provide a calendar view where users can see an overview of their earnings for the month.
*   **FR-023:** Each day on the calendar with an entry shall be visually distinct.
*   **FR-024:** Tapping on a specific date in the calendar will display a detailed view of the earnings for that day, including total turnover, total tips, and any notes.

**5.2. Overview Page**

*   **FR-025:** The app shall have a dedicated overview or dashboard page that displays a summary of the user's earnings.
*   **FR-026:** The overview page shall display the following information:
    *   Total tips so far for the current month.
    *   Estimated commission so far for the current month.
    *   Total turnover for the current month.
    *   A summary of earnings for the previous month.
*   **FR-027:** The overview page may include charts or graphs to visualize earnings trends over time (e.g., a bar chart showing daily tips for the week).

### 6. Notifications and Reminders

To encourage consistent usage and goal tracking, the app will incorporate a notification system.

**6.1. Daily Reminders**

*   **FR-028:** The app shall send a daily local notification to remind the user to input their turnover and tips for the day.
*   **FR-029:** Users shall be able to enable or disable these reminders in the app settings.
*   **FR-030:** Users shall be able to set their preferred time for receiving the daily reminder.

**6.2. Goal Achievement Notifications**

*   **FR-031:** The app shall allow users to set monthly earnings goals (e.g., for total tips).
*   **FR-032:** The system shall send a push notification to the user when they have reached a new milestone or their set goal.

### 7. Additional Features and Edge Cases

To enhance the user experience and cater to various scenarios, the following additional features and edge cases will be considered.

**7.1. Tip-Outs**

*   **AF-001:** An optional feature to allow users to record "tip-outs" to other staff members (e.g., bussers, bartenders). This would provide a more accurate reflection of their net tips.

**7.2. Multiple Jobs**

*   **AF-002:** The ability for users to create and manage earnings from multiple jobs, each with its own turnover, tips, and commission structure.

**7.3. Customizable Commission Rates**

*   **AF-003:** Allow users to set a custom commission percentage instead of the default 1%, as this can vary between establishments.

**7.4. Data Export**

*   **AF-004:** The ability for users to export their earnings data to a CSV or PDF file for tax purposes or personal records.

**7.5. Offline Mode**

*   **AF-005:** The application should allow users to enter their daily turnover and tips even when they are offline. The data will be synced with the server once an internet connection is available.

**7.6. Currency Customization**

*   **AF-006:** The ability for users to select their local currency.


**7.8. Error Handling**

*   **EC-001:** The system shall display clear and user-friendly error messages for invalid inputs (e.g., non-numeric values in earnings fields).
*   **EC-002:** Graceful handling of network errors with clear communication to the user.

### 8. Non-Functional Requirements

**8.1. Performance**

*   **NFR-001:** The application should be responsive and have a smooth user interface.
*   **NFR-002:** Data entry and calculations should be performed quickly.

**8.2. Usability**

*   **NFR-003:** The user interface should be intuitive and easy to navigate for non-technical users.
*   **NFR-004:** The design should be clean and visually appealing.

**8.3. Reliability**

*   **NFR-005:** The application should be stable and have minimal crashes.
*   **NFR-006:** Data should be stored accurately and consistently.

**8.4. Compatibility**

*   **NFR-007:** The application should be compatible with a wide range of Android devices running on recent versions of the Android OS.