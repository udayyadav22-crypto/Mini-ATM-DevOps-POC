# Mini ATM DevOps POC

## 1. Project Overview

Mini ATM is a learning project that demonstrates a simple ATM
application using a browser frontend and a Java backend.

The application is hosted on an Azure Virtual Machine.

This project is for learning and testing only. It is not a real
banking application.

## 2. Project Architecture

```text
User's Browser
      |
      v
HTML, CSS and JavaScript
      |
      v
Java HTTP Server
      |
      +---- GET  /api/health
      |
      +---- POST /api/login
      |
      +---- GET  /
      |
      v
Azure Virtual Machine
```

The browser displays the ATM interface. The Java server provides
the login API and serves the frontend webpage.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| HTML | Creates the webpage structure |
| CSS | Styles the webpage |
| JavaScript | Handles browser interactions |
| Java | Runs the backend HTTP server |
| Azure Virtual Machine | Hosts the application |
| Azure NSG | Controls network access |
| Git | Tracks source-code changes |
| GitHub | Stores and shares the source code |
| Docker | Container configuration is provided |
| Azure Pipelines | Pipeline configuration is provided |

Note: Docker and Azure Pipelines files are included in the project.
Their complete execution is not confirmed by this documentation.

## 4. Project Structure

```text
Mini-ATM-DevOps-POC/
├── browser-frontend/
│   └── index.html
├── java-backend/
│   └── MiniATMServer.java
├── MiniATM.java
├── Dockerfile
├── azure-pipelines.yml
└── README.md
```

## 5. What Each File Does

### browser-frontend/index.html

Contains the ATM webpage, styling and browser-side JavaScript.

Features include:
- Login page
- ATM dashboard
- Check balance
- Deposit and withdraw demo amounts
- Mini statement
- Transfer money demo
- Change PIN demo
- Logout

### java-backend/MiniATMServer.java

Runs the Java HTTP server.

It provides:
- GET /api/health - checks server status
- POST /api/login - validates the demo account and PIN
- GET / - serves the ATM webpage

### MiniATM.java

Contains the original Java console-based ATM application.

### Dockerfile

Contains instructions for building a Docker image.
The container build should be tested separately.

### azure-pipelines.yml

Contains Azure Pipeline configuration.
The pipeline should be reviewed and tested before use.

## 6. Demo Login

Account Number: `1234567890`

PIN: `1234`

These credentials are hard-coded for demonstration only.
Never use real banking credentials in this application.

## 7. Requirements

- Java JDK
- Azure VM with SSH access
- Git
- A web browser

Maven is not required.

## 8. Run the Application

Go to the project directory:

```bash
cd /home/sachin/Mini-ATM-DevOps-POC/java-backend
```

Compile the Java server:

```bash
javac MiniATMServer.java
```

Start the server:

```bash
java MiniATMServer
```

The server listens on `127.0.0.1:8080`.

For secure testing, use an SSH tunnel from your computer:

```powershell
ssh -N -L 8080:127.0.0.1:8080 sachin@YOUR_VM_PUBLIC_IP
```

Keep the SSH session running and open this URL in your browser:

`http://127.0.0.1:8080/`

Replace `YOUR_VM_PUBLIC_IP` with the VM's current public IP address.

## 9. Test the Backend

Check server health:

```bash
curl http://127.0.0.1:8080/api/health
```

Expected response:

```json
{"status":"running","application":"Mini ATM"}
```

Test login:

```bash
curl -i -X POST http://127.0.0.1:8080/api/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "account=1234567890&pin=1234"
```

## 10. Current Limitations

- This is a learning demo, not a real banking system.
- Account number and PIN are hard-coded.
- Balance and transaction history are stored in browser memory.
- Balance and transactions are not saved permanently by the backend.
- Transfer Money is a visual demo only.
- Change PIN is not implemented in the backend.
- No database is connected.
- Authentication needs stronger security before any real-world use.

## 11. Future Improvements

- Manage balance and transactions through backend APIs.
- Add persistent storage or a database.
- Improve authentication and PIN security.
- Add automated tests.
- Test and improve the Docker setup.
- Test and improve the Azure Pipeline.
- Add a CI/CD workflow for deployment.

## 12. Project Repository

GitHub:
https://github.com/yadavsachin98563/Mini-ATM-DevOps-POC

## 13. Author

Mini ATM DevOps POC - Learning Project
