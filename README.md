
# Security CRUD Flow

The project implements a university system in which the user has a certain role:
- Lecturer - 'ADMIN'
- Curator - 'MODERATOR'
- Student - 'USER'

Depending on the role, the user has access to different mappings. For example, admin can delete user or moderator accounts.


## Application Mappings

### Home page

```java
GET /home  
```

> Initializes the user or gets him if it is already present in the authentication map.

# 

### Sign up

```java
POST /register
```
> Validates user input, saves it to the database and sends a verification code to email.

# 

### Email verififcation

```java
POST /register/verify-email
```
> Validates user verification code and updates user role to USER.

> [!NOTE]
> This mapping is available only to users with USER_NOT_VERIFIED role.

# 

```java
GET /register/resend-verification-code
```
> Resends the verification code to the user via email.

# 

### Login

```java
GET /login
```

| Parameter | Required | Description                                   |
| :-------- | :------- | :---------------------------------------------|
| `email`   | `false`  | user's email when redirected from sign up page|

> Validates the user's credentials and redirects to the email verification page if the user is not verified.  

# 

```java
POST /login/reset-password
```

> Sends a password reset token to email.

# 

```java
GET /login/resend-password-token
```

| Parameter | Required | Description|
| :-------- | :------- | :--------  |
| `token`   | `true`   | old token  |

> Resends the password reset token to the user via email.

# 

```java
PATCH /login/save-password
```

> Allows the user to set a new password. The link with this mapping is sent to the user's email with a password token.

### Profile

```java
GET /profile
```

> Displays user profile.

# 

```java
PATCH /profile
```

> Allows the user to change profile information and redirects to the email verification page if the email address has been changed.

#

```java
GET /profile/parent-data
```

> Displays information about the user's parents.

> [!NOTE]
> This mapping is available to students only.

#

```java
PATCH /profile/parent-data
```

> Allows the user to change parent information.

> [!NOTE]
> This mapping is available to students only.

### Common

```java
PATCH /change-password
```

> Validates the old password and allows the user to set a new password.

### Student

```java
GET /students
```

> Displays all students.

#

```java
GET /students/{id}
```

| Parameter | Required | Description             |
| :-------- | :------- | :--------               |
| `id`      | `true`   | id of student to fetch  |

> Displays the student by id.

> [!NOTE]
> This mapping is not available to students.

#

```java
DELETE /students/{id}
```

| Parameter | Required | Description              |
| :-------- | :------- | :--------                |
| `id`      | `true`   | id of student to delete  |

> Deletes the student by id.

> [!NOTE]
> This mapping is available to lecturers only.

### Curator

```java
GET /curators
```

> Displays all curators.

#

```java
GET /curators/{id}
```

| Parameter | Required | Description             |
| :-------- | :------- | :--------               |
| `id`      | `true`   | id of curator to fetch  |

> Displays the curator by id.

#

```java
DELETE /curators/{id}
```

| Parameter | Required | Description              |
| :-------- | :------- | :--------                |
| `id`      | `true`   | id of curator to delete  |

> Deletes the curator by id.

> [!NOTE]
> By deleting a curator, you delete all students associated with him.  
> This mapping is available to lecturers only.

### Lecturer

```java
GET /lecturers
```

> Displays all lecturers.

#

```java
GET /lecturers/{id}
```

| Parameter | Required | Description              |
| :-------- | :------- | :--------                |
| `id`      | `true`   | id of lecturer to fetch  |

> Displays the lecturer by id.

## Features

- Email verification after registration
- Log in with Google or GitHub
- "Remember me" feature
- Concurrent multi-user access to the application
- Change the home page language using the request param ***?lang=***** (e.g. ***?lang=pl***)

## Run Locally

Clone the project

```bash
git clone https://github.com/sxlecquer/security-crud-flow.git
```

Open the cloned project in the IDE and go to `resources/application.yml` in client module.  
1. Configure connection to your database:
```yaml
datasource:
  url: // link to the database
  username: // your username
  password: // your password
  driver-class-name: // database driver name
```

2. Provide OAuth2.0 client-id and client-secret from your GitHub and Google accounts in security section:
```yaml
security:
  oauth2:
    client:
      registration:
        github:
          client-id: // your generated github client id
          client-secret: // and client secret
          scope:
            - user:email
            - read:user
        google:
          client-id: // your generated google client id
          client-secret: // and client secret
          scope:
            - openid
            - email
            - profile
```
&nbsp;&nbsp;&nbsp;&nbsp;How to do this for [Google](https://support.google.com/cloud/answer/6158849?hl=en#:~:text=Go%20to%20the%20Google%20Cloud%20Platform%20Console%20Credentials%20page.,to%20add%20a%20new%20secret.)
and [GitHub](https://docs.github.com/en/rest/authentication/authenticating-to-the-rest-api?apiVersion=2022-11-28#using-basic-authentication).
> [!IMPORTANT]
> If you skip this step, login using OAuth2.0 will not work.
3. Provide your email credentials from which you want to send emails to users with password reset links or email verification codes:
```yaml
mail:
  host: smtp.gmail.com
  port: 587
  username: // your email
  password: // your password
  properties:
    mail:
      smtp:
        auth: true
        starttls:
          enable: true
```

4. Go to `service/impl/CuratorServiceImpl` and comment out code in `fillCuratorTable()` method.  
Add `curatorService.fillCuratorTable()` to one of methods that are allowed for any user, e.g. in `homePage()` method:
```java
@GetMapping("/")
public String homePage() {
    curatorService.fillCuratorTable(); // add this line
    return "redirect:/home";
}
```
&nbsp;&nbsp;&nbsp;&nbsp;The same you can do for `service/impl/LecturerServiceImpl` and its `fillLecturerTable()` method.  
&nbsp;&nbsp;&nbsp;&nbsp;Now you can run the program, go to the browser and type [http://localhost:8080/](http://localhost:8080/).  
&nbsp;&nbsp;&nbsp;&nbsp;After that, new records appear in the `curator` and `lecturer` tables of the database.  

#### So now you can fully test this application. <br> Enjoy!!! 😋
