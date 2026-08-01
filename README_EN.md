# Edu Vault

**End-to-End Encrypted Desktop Password Manager (Zero-Knowledge Architecture & Key Wrapping)**

<p align="center">
  <a href="./README.md"><img src="https://img.shields.io/badge/Language-Türkçe-E31E24?style=for-the-badge" alt="Türkçe" /></a>
  <a href="./README_EN.md"><img src="https://img.shields.io/badge/Language-English-00247D?style=for-the-badge" alt="English" /></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Java_Swing-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java Swing" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/AES--256--GCM-00599C?style=for-the-badge&logo=sharp&logoColor=white" alt="AES-256-GCM" />
  <img src="https://img.shields.io/badge/BIP--39-F7931A?style=for-the-badge&logo=bitcoin&logoColor=white" alt="BIP39" />
  <img src="https://img.shields.io/badge/PBKDF2-4D4D4D?style=for-the-badge&logo=lock&logoColor=white" alt="PBKDF2" />
</p>

Edu Vault is a bank-grade, end-to-end encrypted desktop password management software developed in Java, built upon **Zero-Knowledge Architecture** and **Key Wrapping** principles, designed for individual users and organizations with high security requirements.

In the Edu Vault architecture, the user's Master Password or unencrypted vault data is **never** saved in plaintext in the database, on disk, or on servers. The system incorporates the **24-word BIP39 Seed Phrase** standard—used in modern blockchain (Bitcoin/Ethereum) hardware wallets—into password management discipline, guaranteeing complete data privacy and zero-data-loss recovery.

---

## Table of Contents

- [About the Project](#about-the-project)
- [Target Audience & Industry Comparison](#target-audience--industry-comparison)
- [Cryptographic Security Architecture & Technological Advantages](#cryptographic-security-architecture--technological-advantages)
- [10-Layer Background Security Mechanism](#10-layer-background-security-mechanism)
- [Brute-Force Prevention & Progressive Lockout Architecture](#brute-force-prevention--progressive-lockout-architecture)
- [All Features and Modules](#all-features-and-modules)
- [Screenshots](#screenshots)
- [Database Examples](#database-examples)
- [Technology Stack & Rationale](#technology-stack--rationale)
- [Software Architecture](#software-architecture)
- [Database Schema & Relational Structure](#database-schema--relational-structure)
- [Installation and Setup Guide](#installation-and-setup-guide)
- [Project Structure & Directory Tree](#project-structure--directory-tree)
- [Version History](#version-history)
- [License and Developer Information](#license-and-developer-information)

---

## About the Project

Edu Vault is a desktop application allowing users to consolidate usernames, passwords, web addresses, and private notes for hundreds of digital platforms into a single secure vault.

Unlike conventional password managers, Edu Vault v2.0 transitions to a **Key Wrapping** architecture. In this architecture, the user's Master Password is not used to encrypt vault entries directly. Instead, the system operates as follows:

1. Upon user registration, an independent, high-entropy 256-bit **Vault Key** is generated using `SecureRandom`.
2. All password records in the vault are encrypted using this Vault Key.
3. The Vault Key itself is dual-wrapped and written to the database using both a **Master Wrap Key** (derived from the Master Password) and a **Recovery Wrap Key** (derived from the BIP39 seed phrase).

Thanks to this design, changing the Master Password does not require re-encrypting every entry in the vault; only the outer wrap key securing the Vault Key is updated (**Re-Wrapping**). This mechanism reduces operation performance overhead to $O(1)$ while ensuring strict cryptographic isolation between keys.

---

## Target Audience & Industry Comparison

| User Segment | Challenge | Edu Vault Solution |
|--------------|-----------|--------------------|
| **Individual Users** | Forgetting complex passwords & insecure storage methods | Consolidating all data into an end-to-end encrypted local vault |
| **Developers & System Administrators** | Reluctance to trust 3rd-party cloud servers for data security | Memory-protected architecture with complete local DB ownership |
| **Security Professionals** | Total data loss on forgotten master password or risk of backdoors | Data recovery via 24-word BIP39 Seed Phrase without security backdoors |

---

## Cryptographic Security Architecture & Technological Advantages

Algorithms and standards implemented in Edu Vault are selected from international security standards approved by NIST (National Institute of Standards and Technology) and IETF (Internet Engineering Task Force).

### 1. AES-256-GCM (Galois/Counter Mode)
- **256-Bit Symmetric Key:** Offers $2^{256}$ possible key combinations. Immune to brute-force attacks via any known supercomputer or quantum computing method.
- **AEAD (Authenticated Encryption with Associated Data):** Unlike traditional CBC mode, it generates a 128-bit `Authentication Tag` during encryption. If even a single bit of encrypted database content is tampered with, the system throws an `AEADBadTagException` and rejects decryption (**Tampering Resistance**).

### 2. PBKDF2 (Password-Based Key Derivation Function 2)
- **PBKDF2WithHmacSHA256 (65,536 Iterations):** Derives password hash and wrap keys over 65,536 hashing rounds, drastically escalating the computational cost of GPU and ASIC brute-force/rainbow table attacks.
- **PBKDF2WithHmacSHA512 (2,048 Iterations):** Used to derive 512-bit seeds from BIP39 recovery words.

### 3. BIP39 Standard (Bitcoin Improvement Proposal 0039)
- Official key derivation standard used in blockchain infrastructures.
- Combines 256-bit cryptographic entropy with an 8-bit SHA-256 checksum ($256 + 8 = 264$ bits) to form a human-readable 24-word phrase. Ensures zero-data-loss vault recovery if the master password is forgotten.

### 4. Cryptographically Secure Pseudo-Random Number Generator (SecureRandom)
- Replaces deterministic `java.util.Random` with `java.security.SecureRandom`, utilizing the operating system's Hardware Entropy Pool. All IVs, Salts, and Vault Keys are generated this way.

---

## 10-Layer Background Security Mechanism

Edu Vault executes 10 distinct background security layers simultaneously:

```
+---------------------------------------------------------------------------------------+
|                             EDU VAULT 10-LAYER SECURITY ARCHITECTURE                  |
+---------------------------------------------------------------------------------------+
| Layer 1  : PBKDF2-HMAC-SHA256 Login Hashing (65,536 Rounds + 16-byte Salt)            |
| Layer 2  : Isolated Master Wrap Key Derivation (Independent Master Wrap Salt)        |
| Layer 3  : 256-bit SecureRandom Vault Key & AES-256-GCM Key Wrapping                |
| Layer 4  : 256-bit Entropy + 8-bit SHA-256 Checksum 24-Word BIP39 Generation          |
| Layer 5  : PBKDF2-HMAC-SHA512 & HMAC-SHA256 Recovery Wrap Key Derivation             |
| Layer 6  : Dynamic 12-byte IV & 128-bit AEAD Tag Encryption (AES-256-GCM)            |
| Layer 7  : Re-Wrapping (Key Packaging with Zero Re-encryption Overhead)              |
| Layer 8  : RAM Sanitization (Byte-level Zeroing in Memory via Arrays.fill)            |
| Layer 9  : Progressive Brute-Force Lockout & Mandatory Seed Freeze Mode               |
| Layer 10 : PreparedStatement (100% SQL Injection Protection) & Strict Input Regex     |
+---------------------------------------------------------------------------------------+
```

### Operational Principles of the Layers

#### Layer 1 — Authentication Layer
The Master Password is never saved as plaintext. Upon registration, a 16-byte random salt is generated. The password is hashed using `PBKDF2WithHmacSHA256` for 65,536 rounds, and the resulting 256-bit hash is stored in `users.password_hash`.

#### Layer 2 — Master Wrap Key Isolation Layer
A `Master Wrap Key` is derived from the Master Password using a second `master_wrap_salt`, completely independent of the login hash salt. This cryptographically isolates authentication verification from vault decryption keys.

#### Layer 3 — Vault Key & Key Wrapping Layer
An independent 256-bit `Vault Key` is generated via `SecureRandom`. All records in the vault are encrypted with this Vault Key. The Vault Key itself is wrapped using `AES-256-GCM` with the Master Wrap Key (`vault_key_master_encrypted`).

#### Layer 4 — BIP39 24-Word Recovery & Checksum Layer
256 bits of entropy are generated. The first 8 bits of its SHA-256 hash are appended as a checksum ($256 + 8 = 264$ bits). The 264-bit array is split into twenty-four 11-bit chunks, each indexing a word in the official 2048-word BIP39 English dictionary. Checksum verification prevents invalid word sequence entry.

#### Layer 5 — BIP39 Recovery Wrap Key Derivation Layer
The 24-word phrase is converted to a 512-bit seed via `PBKDF2WithHmacSHA512` (2,048 iterations). This seed is processed via `HmacSHA256` with a static key `EduVault-Recovery-Wrap` to derive a 256-bit `Recovery Wrap Key`. The Vault Key is wrapped with this key as well and stored in `vault_key_recovery_encrypted`.

#### Layer 6 — Dynamic IV & AEAD Integrity Layer
For every vault entry, a fresh 12-byte (96-bit) IV is generated via `SecureRandom`. Encryption is executed using `AES-256-GCM` creating a 128-bit Authentication Tag.

#### Layer 7 — Re-Wrapping (Zero Re-Encryption Cost) Layer
When changing the Master Password, vault records are not re-encrypted. Only the Vault Key residing in RAM is re-wrapped with a new Master Wrap Key derived from the new Master Password. Operation complexity is $O(1)$.

#### Layer 8 — Memory Security (RAM Sanitization)
Because Java `String` objects are immutable and persist in heap memory, Edu Vault holds the active Vault Key in a `byte[]` array. Upon logout or a 10-minute idle timeout:
```java
Arrays.fill(currentVaultKeyBytes, (byte) 0);
currentVaultKeyBytes = null;
```
physically overwrites memory with zeroes.

#### Layer 9 — Progressive Brute-Force & Lockout Layer
Failed login attempts are logged in `login_attempts`. Every 5 failed attempts trigger progressive lockouts:
- Lockout 1: 5 Minutes
- Lockout 2: 15 Minutes
- Lockout 3: 40 Minutes
- Lockout 4: 100 Minutes
- 5 Lockout Cycles (25 Failed Attempts): Account permanently frozen. Access can only be unlocked using the 24-word BIP39 Seed Phrase.

#### Layer 10 — PreparedStatement & Input Validation Layer
- All DB queries execute via parameterized `PreparedStatement` (100% SQL Injection protection).
- Username: `^[a-zA-Z0-9_]+$` (Min 7 chars).
- Email: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` RFC format validation.
- Phone: `^\d+$` (10 or 11 digits).
- Password Strength: Min 10 characters, 1 Uppercase, 1 Lowercase, 1 Number, and 1 Special Character.

---

## Brute-Force Prevention & Progressive Lockout Architecture

```
[Failed Login Attempt]
         │
         ▼
 5 Failed Attempts ---> Lockout 1: 5 Minutes Wait
         │
 10 Failed Attempts ---> Lockout 2: 15 Minutes Wait
         │
 15 Failed Attempts ---> Lockout 3: 40 Minutes Wait
         │
 20 Failed Attempts ---> Lockout 4: 100 Minutes Wait
         │
 25 Failed Attempts (5 Cycles) ---> ACCOUNT FROZEN
                                       │
                                       ▼
                         [Can Only Be Recovered via]
                         [24-Word BIP39 Seed Phrase]
```

---

## All Features and Modules

- **Secure Registration & Authentication:** Input validation and PBKDF2 password hashing.
- **Zero-Knowledge Key Wrapping:** Dual wrapping of the master vault key.
- **BIP39 Seed Phrase Recovery:** Lossless master password reset via 24-word standard mnemonic list.
- **Vault Entry CRUD Operations:** Create, read, edit, and delete site name, URL, username, password, and notes.
- **AES-256-GCM Encryption:** Fresh 12-byte IV & 128-bit AEAD tag per entry.
- **Real-Time Search Filter:** Filter entries dynamically by site name or username.
- **Strong Password Generator:**
  - Minimum 8 characters with dynamic length configuration.
  - Uppercase (`A-Z`), Lowercase (`a-z`), Numbers (`0-9`), Special Characters (`!@#$%^&*()-_=+[]{};:,.<>?`).
  - Guaranteed inclusion of at least 1 character from each selected category, fully shuffled via `Collections.shuffle`.
- **Show / Hide Password:** Toggle password visibility.
- **Session Timeout (10-Minute Idle Timeout):** Managed via `javax.swing.Timer` for automatic logout.
- **RAM Sanitization:** Wipes Vault Key byte array using `Arrays.fill` upon logout/timeout.
- **Re-Wrapping Master Password Change:** Update master password without re-encrypting vault records.
- **Progressive Brute-Force Prevention:** Progressive locks every 5 attempts and account freeze at 25 attempts.

---

## Screenshots

<table>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/login.png">
        <img src="screenshots/login.png" width="380" alt="Login Form"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/register.png">
        <img src="screenshots/register.png" width="380" alt="Register Form"/>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/recoveryseedphrase.png">
        <img src="screenshots/recoveryseedphrase.png" width="380" alt="BIP39 Recovery Phrase"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/reset_password.png">
        <img src="screenshots/reset_password.png" width="380" alt="Reset Password"/>
      </a>
    </td>
  </tr>
</table>
<table>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/dashboard.png">
        <img src="screenshots/dashboard.png" width="380" alt="Dashboard Masked"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/dashboard2.png">
        <img src="screenshots/dashboard2.png" width="380" alt="Dashboard Unmasked"/>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/add_password.png">
        <img src="screenshots/add_password.png" width="380" alt="Add Password"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/edit_password.png">
        <img src="screenshots/edit_password.png" width="380" alt="Edit Password"/>
      </a>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/password_generator.png">
        <img src="screenshots/password_generator.png" width="380" alt="Password Generator"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/change_master_password.png">
        <img src="screenshots/change_master_password.png" width="380" alt="Change Master Password"/>
      </a>
    </td>
  </tr>
</table>

---

## Database Examples

<table>
  <tr>
    <td align="center"></td>
    <td align="center"></td>
  </tr>
  <tr>
    <td align="center">
      <a href="screenshots/database_schema.png">
        <img src="screenshots/database_schema.png" width="380" alt="Database Schema 1"/>
      </a>
    </td>
    <td align="center">
      <a href="screenshots/database_schema2.png">
        <img src="screenshots/database_schema2.png" width="380" alt="Database Schema 2"/>
      </a>
    </td>
  </tr>
</table>

---

## Technology Stack & Rationale

| Layer | Technology / Library | Description |
|-------|----------------------|-------------|
| **User Interface (GUI)** | Java Swing / AWT | Desktop forms and dialog management |
| **Development Language** | Java (JDK 17+) | Object-oriented core business logic |
| **Database** | MySQL 8.0 / JDBC | Relational database & secure PreparedStatement queries |
| **Symmetric Encryption** | AES-256-GCM | Authenticated Encryption with Associated Data (AEAD) |
| **Key Derivation (KDF)** | PBKDF2WithHmacSHA256 | Master Hash & Master Wrap Key derivation (65,536 iterations) |
| **Recovery KDF** | PBKDF2WithHmacSHA512 | BIP39 Seed derivation (2,048 iterations, 512-bit output) |
| **HMAC Algorithm** | HmacSHA256 | Recovery Wrap Key HMAC derivation |
| **Recovery Standard** | BIP39 Standard | 24-Word English mnemonic dictionary (2048 words) |
| **Random Generator** | `java.security.SecureRandom` | Cryptographically secure IV, Salt, and Vault Key generation |
| **Development Environment** | NetBeans IDE | Project structure, GUI Builder, and Ant build tools |

---

## Software Architecture

Edu Vault is designed according to Layered Architecture principles:

```
┌─────────────────────────────────────────────────────────────┐
│                    Java Swing UI Layer                      │
│   LoginForm | RegisterForm | MainForm | RecoveryForm etc.   │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                         │
│   AuthService | VaultService | VaultKeyService              │
│   Bip39Service | RecoveryService | SessionManager           │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                         DAO Layer                           │
│       UserDAO | VaultEntryDAO | LoginAttemptDAO             │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       Database Layer                        │
│                   MySQL (DatabaseConnection)                │
└──────────────────────────────┬──────────────────────────────┘
```

---

## Database Schema & Relational Structure

The database consists of `users`, `vault_entries`, and `login_attempts` tables.

### Complete SQL Setup Script

```sql
CREATE DATABASE IF NOT EXISTS eduvault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eduvault;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    master_wrap_salt VARCHAR(255) NOT NULL,
    vault_key_master_encrypted TEXT NOT NULL,
    vault_key_master_iv VARCHAR(255) NOT NULL,
    vault_key_recovery_encrypted TEXT NOT NULL,
    vault_key_recovery_iv VARCHAR(255) NOT NULL,
    recovery_words_count INT DEFAULT 24,
    recovery_version VARCHAR(50) DEFAULT 'BIP39_EN_V1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vault_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    site_url VARCHAR(255),
    account_username VARCHAR(100) NOT NULL,
    encrypted_password TEXT NOT NULL,
    iv VARCHAR(255) NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS login_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## Installation and Setup Guide

System requirements, software dependencies, and step-by-step installation instructions to set up and run Edu Vault locally are detailed below.

### System Requirements & Prerequisites

The following software must be installed on your machine for proper execution:

1. **Java Development Kit (JDK 17 or Higher):**
   - Developed using Java Swing and Java Cryptography Architecture (`javax.crypto`).
   - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [Eclipse Temurin OpenJDK](https://adoptium.net/) is required.

2. **MySQL Server (v8.0 or Higher):**
   - MySQL database server is required to store relational encrypted data.
   - [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) can be downloaded and installed.

3. **Database Management Tool (Optional):**
   - [MySQL Workbench](https://dev.mysql.com/downloads/workbench/), DBeaver, or phpMyAdmin can be used to manage tables and run SQL scripts.

4. **IDE or Git:**
   - [NetBeans IDE](https://netbeans.apache.org/) (or IntelliJ IDEA / Eclipse) is recommended to compile and run the project.
   - [Git](https://git-scm.com/) must be installed to clone the repository.

5. **MySQL Connector/J (JDBC Driver):**
   - `mysql-connector-j-8.x.x.jar` must be present in the project classpath (included by default in NetBeans project libraries).

---

### Step-by-Step Installation Instructions

#### Step 1: Clone the Repository

Open Command Prompt or Terminal and clone the repository:

```bash
git clone https://github.com/erdemozbalta/Edu_Vault.git
cd Edu_Vault
```

#### Step 2: Create Database & Tables

Connect to your MySQL database server (`mysql -u root -p` or via MySQL Workbench).
Execute the SQL script below to create `eduvault` database and its 3 tables (`users`, `vault_entries`, `login_attempts`):

```sql
CREATE DATABASE IF NOT EXISTS eduvault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eduvault;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    master_wrap_salt VARCHAR(255) NOT NULL,
    vault_key_master_encrypted TEXT NOT NULL,
    vault_key_master_iv VARCHAR(255) NOT NULL,
    vault_key_recovery_encrypted TEXT NOT NULL,
    vault_key_recovery_iv VARCHAR(255) NOT NULL,
    recovery_words_count INT DEFAULT 24,
    recovery_version VARCHAR(50) DEFAULT 'BIP39_EN_V1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vault_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    site_url VARCHAR(255),
    account_username VARCHAR(100) NOT NULL,
    encrypted_password TEXT NOT NULL,
    iv VARCHAR(255) NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS login_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### Step 3: Configure Database Connection Credentials

Open `src/eduvault/db/DatabaseConnection.java` in an editor or IDE and configure your local MySQL credentials:

```java
package eduvault.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/eduvault";
    private static final String USER = "root";       // Your MySQL Username
    private static final String PASSWORD = "yourpassword";   // Your MySQL Password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

#### Step 4: Compile and Run the Application

**Method A: Running with NetBeans IDE (Recommended)**
1. Open NetBeans IDE.
2. Go to `File -> Open Project` and select the `Edu_Vault` directory.
3. Right-click `Edu_Vault` in the left panel and click **Clean and Build**.
4. Open `src/eduvault/Eduvault.java` and press `Shift + F6` or click **Run**.

**Method B: Command Line (Terminal)**
1. Navigate to the project root directory.
2. Compile the source code:
   ```bash
   javac -d build/classes -srcdir src src/eduvault/Eduvault.java
   ```
3. Run the application with MySQL Connector JAR in classpath:
   ```bash
   java -cp "build/classes;lib/mysql-connector-j-8.x.x.jar" eduvault.Eduvault
   ```

---

## Project Structure & Directory Tree

```
eduvault/
├── assets/                          # Project media and logos
├── screenshots/                     # README screenshots
├── src/
│   └── eduvault/
│       ├── Eduvault.java            # Main application entry point
│       ├── dao/                     # Data Access Objects
│       │   ├── LoginAttemptDAO.java # Login attempt logging & brute-force DAO
│       │   ├── UserDAO.java         # User operations DAO
│       │   └── VaultEntryDAO.java   # Vault CRUD DAO
│       ├── db/
│       │   └── DatabaseConnection.java # MySQL JDBC connection manager
│       ├── model/                   # Data Models / Entities
│       │   ├── RegisterResult.java  # Registration & recovery result model
│       │   ├── User.java            # User model
│       │   └── VaultEntry.java      # Vault entry model
│       ├── resources/
│       │   └── bip39_english.txt    # BIP39 official 2048-word list
│       ├── service/                 # Cryptography & Business Logic Services
│       │   ├── AuthService.java     # Authentication, brute-force & registration service
│       │   ├── Bip39Service.java    # BIP39 entropy, checksum & seed derivation service
│       │   ├── CryptoService.java   # AES-256-GCM core encryption/decryption service
│       │   ├── HashService.java     # PBKDF2 master password hashing service
│       │   ├── PasswordGeneratorService.java # SecureRandom password generator
│       │   ├── RecoveryService.java # BIP39 seed recovery wrap key service
│       │   ├── SessionManager.java  # Memory security & 10-min idle timeout service
│       │   ├── VaultKeyService.java # Random Vault Key & Master Wrap Key service
│       │   └── VaultService.java    # Encryption & saving vault credentials service
│       └── ui/                      # Swing GUI Forms & Dialogs
│           ├── AddEntryDialog.java  # New password entry window
│           ├── ChangeMasterPasswordDialog.java # Change master password window
│           ├── EditEntryDialog.java # Edit password window
│           ├── LoginForm.java       # Login screen
│           ├── MainForm.java        # Main Dashboard panel
│           ├── RecoveryForm.java    # Password reset via BIP39 Seed Phrase screen
│           └── RegisterForm.java    # User registration screen
├── build.xml                        # Ant build script
├── README.md                        # Turkish project documentation
├── README_EN.md                     # English project documentation
└── LICENSE                          # License file
```

---

## Version History

### v2.0 — Vault Key Architecture + BIP39 Seed Phrase Recovery
- **Key Wrapping Architecture:** Encryption architecture completely overhauled. The Master Password is used to wrap a 256-bit Vault Key rather than directly encrypting data entries.
- **Random Vault Key:** Independent 256-bit symmetric key generation per user using `SecureRandom`.
- **BIP39 Recovery System:** Integrated 24-word standard BIP39 Seed Phrase recovery infrastructure (`PBKDF2WithHmacSHA512` + `HmacSHA256`).
- **Advanced Brute-Force Prevention:** Exponentially increasing lockouts every 5 failed attempts (5 min → 15 min → 40 min → 100 min) and mandatory seed recovery mode after 5 lockout cycles.
- **Automatic Idle Timeout:** Automatic session logout and RAM sanitization after 10 minutes of inactivity.
- **SessionManager RAM Sanitization:** Clears Vault Key byte array via `Arrays.fill` with zero bytes on logout/timeout.
- **Master Password Re-Wrapping:** Supports re-wrapping the Vault Key when updating the master password from within an active session.

### v1.0 — Initial Release
- Core user registration and authentication infrastructure.
- Direct password encryption using AES-256-GCM.
- Master password hashing via PBKDF2WithHmacSHA256.
- Basic Vault CRUD operations.

---

## License

This project is not published under any open-source license. All rights reserved.

The source code, design, business logic, and associated documentation of this software are protected by copyright law. Unauthorized copying, distribution, modification, or commercial use without written permission is prohibited.

---

## Developer

<p align="center">
  Copyright &copy; 2026 Erdem Özbalta. All rights reserved.<br/>
  <a href="mailto:erdemozbalta@gmail.com">erdemozbalta@gmail.com</a> | <a href="https://github.com/erdemozbalta">github.com/erdemozbalta</a>
</p>
