# Mini ATM DevOps POC

## 1. Project Overview

This is a learning project that demonstrates a simple ATM
application using a browser frontend and a Java backend.

The project is deployed on an Azure Virtual Machine.

This is a demo application, not a real banking system.

## 2. Technologies Used

- HTML - webpage structure
- CSS - webpage design
- JavaScript - browser interactions
- Java - backend HTTP server and login API
- Azure Virtual Machine - application hosting
- Azure Network Security Group (NSG) - network access rules
- Git and GitHub - source code management

## 3. Project Structure

```text
Mini-ATM-DevOps-POC/
├── browser-frontend/
│   └── index.html
├── java-backend/
│   ├── MiniATMServer.java
│   └── MiniATMServer.java.backup
└── README.md
```

Additional files may exist in the project directory.

## 4. Frontend Features

- ATM login page
- ATM dashboard
- Check balance
- Deposit demo amount
- Withdraw demo amount
- Mini statement
- Transfer money demo
- Change PIN demo
- Logout

Note: Balance and transaction history are currently stored
in browser memory. They are not saved permanently by the backend.

## 5. Backend Features

- GET /api/health
  Checks whether the Java server is running.

- POST /api/login
  Checks the demo account number and PIN.

- GET /
  Serves the Mini ATM webpage.

The current demo credentials are hard-coded in Java.
Do not use real banking credentials.

## 6. Demo Login

Account Number: 1234567890
PIN: 1234

These are demo credentials only.

## 7. Requirements

- Java JDK
- Azure VM with SSH access
- Git
- Web browser

Maven is not required.

## 8. Run the Application

Go to the Java backend directory:

```bash
cd java-backend
```

Compile the Java file:

```bash
javac MiniATMServer.java
```

Start the server:

```bash
java MiniATMServer
```

The server listens on 127.0.0.1:8080.

Open the application through an appropriate local connection
or SSH tunnel:

http://127.0.0.1:8080/

## 9. Health Check

Run this command on the VM:

```bash
curl http://127.0.0.1:8080/api/health
```

Expected response:

```json
{"status":"running","application":"Mini ATM"}
```

## 10. Current Limitations

- This is a learning demo only.
- Login credentials are hard-coded.
- Balance and transactions are not persisted by the backend.
- Transfer Money does not perform a real transfer.
- Change PIN is not implemented in the backend.
- No database is connected.
- The application is not suitable for real banking use.

## 11. Future Improvements

- Move balance and transactions to the Java backend.
- Add database storage.
- Improve authentication and PIN security.
- Add automated tests.
- Create a Docker image.
- Add a CI/CD pipeline using GitHub Actions.
- Deploy through Azure services.

## 12. Author

Mini ATM DevOps POC - Learning Project
