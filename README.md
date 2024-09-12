
# 🚀 Security CRUD Flow
**Check out the live application here: [ec2-3-70-53-252.eu-central-1.compute.amazonaws.com](http://ec2-3-70-53-252.eu-central-1.compute.amazonaws.com:8080/home)**

<table>
  <tr>
    <td width="70%">
      <b>Security CRUD Flow</b> is a university management system that provides role-based access control. Users can be assigned one of these roles:<br><br>
  
  - **Lecturer** (`ADMIN`)
  - **Curator** (`MODERATOR`)
  - **Student** (`USER`)
  
  Depending on the user's role, different functionalities and access rights are available.<br>
  For example, lecturers can manage curators and students, while curators have the ability to manage students.<br>
    </td>

  <td width="40%">
    <p align="center">
      <img src="https://github.com/user-attachments/assets/7ce84171-03d5-4bc0-9acf-7ad5441f77f8" width="270" height="270">
    </p>
  </td>
  </tr>
</table>

## Features

- ***OAuth2 Login***: Allows login with Google or GitHub accounts.
- ***Email Verification***: Ensures users validate their email address upon registration.
- ***Remember Me***: Enables persistent login sessions.
- ***Multi-user Access***: Supports concurrent access by multiple users.
- ***Internationalization***: Change the home page language using the request parameter *?lang=*** (e.g. *?lang=pl*).

## Application endpoints

#### Home page

```java
GET /home  
```
Initializes the user session or retrieves the user if already present in the authentication map.

---

#### Sign up

```java
POST /register
```
Validates user input, saves it to the database, and sends a verification code to the provided email.

---

#### Email verification

```java
POST /register/verify-email
```
Validates the email verification code and updates the user's role to `USER`.

> [!NOTE]
> Available only to users with `USER_NOT_VERIFIED` role.

# 

```java
GET /register/resend-verification-code
```
Resends the verification code to the user via email.

---

#### Login

```java
GET /login
```

| Parameter | Required | Description                                   |
| :-------- | :------- | :---------------------------------------------|
| `email`   | `false`  | User's email when redirected from sign up page|

Validates user credentials and redirects to the email verification page if the user is not yet verified.

# 

```java
POST /login/reset-password
```
Sends a password reset token to the user's email.

# 

```java
GET /login/resend-password-token
```

| Parameter | Required | Description                  |
| :-------- | :------- | :--------------------------  |
| `token`   | `true`   | The old token for resending  |

Resends the password reset token to the user via email.

# 

```java
PATCH /login/save-password
```
Allows the user to set a new password. A link with this endpoint is sent to the user's email with a password token.

---

#### Profile

```java
GET /profile
```
Displays the user's profile.

# 

```java
PATCH /profile
```
Allows the user to change their profile information. Redirects to email verification if the email address has been changed.

#

```java
GET /profile/parent-data
```
Displays information about the user's parents.

> [!NOTE]
> Available only to students.

#

```java
PATCH /profile/parent-data
```
Allows the user to change parent information.

> [!NOTE]
> Available only to students.

---

#### Change password

```java
PATCH /change-password
```
Validates the old password and allows the user to set a new one.

---

#### Student management

```java
GET /students
```
Displays a list of all students.

#

```java
GET /students/{id}
```

| Parameter | Required | Description                 |
| :-------- | :------- | :-------------------------  |
| `id`      | `true`   | ID of the student to fetch  |

Displays the student by ID.

> [!NOTE]
> Not available to students.

#

```java
DELETE /students/{id}
```

| Parameter | Required | Description                  |
| :-------- | :------- | :--------------------------  |
| `id`      | `true`   | ID of the student to delete  |

Deletes the student by ID.

> [!NOTE]
> Available only to lecturers.

---

#### Curator management

```java
GET /curators
```
Displays a list of all curators.

#

```java
GET /curators/{id}
```

| Parameter | Required | Description                 |
| :-------- | :------- | :-------------------------  |
| `id`      | `true`   | ID of the curator to fetch  |

Displays the curator by ID.

#

```java
DELETE /curators/{id}
```

| Parameter | Required | Description                  |
| :-------- | :------- | :--------------------------  |
| `id`      | `true`   | ID of the curator to delete  |

Deletes the curator by ID and “hires” a new curator by assigning all associated students of the deleted curator to the new one.

> [!NOTE]  
> Available only to lecturers.

---

#### Lecturer management

```java
GET /lecturers
```
Displays a list of all lecturers.

#

```java
GET /lecturers/{id}
```

| Parameter | Required | Description                  |
| :-------- | :------- | :--------------------------  |
| `id`      | `true`   | ID of the lecturer to fetch  |

Displays the lecturer by ID.

#
