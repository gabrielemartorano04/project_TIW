# 🎵 Music Playlist Web App — TIW Final Project (A.Y. 2024–2025)

Web application project developed for the **Tecnologie Informatiche per il Web** (*Web Information Technologies*) course at **Politecnico di Milano**.

* **University:** Politecnico di Milano — Department of Electronics, Information and Bioengineering (DEIB)
* **Professor:** Prof. Piero Fraternali
* **Authors:** [Gabriele Martorano](https://github.com/gabrielemartorano04) & [Luca Lorusso](https://github.com/Luc4Lo)
* **Course:** Tecnologie Informatiche per il Web (TIW)
* **Topic:** Music Playlist

---

## 📋 Project Overview

The project consists of a web application for managing personal music playlists. Users can register, log in, upload new songs (with metadata like album, artist, year, genre, and cover image), create custom playlists, and add or play songs. 

As required by the course specifications, the repository contains two distinct implementations of the same application:

### 1. Pure HTML Version
* **Architecture:** Traditional multi-page server-side rendered web application.
* **Tech Stack:** Java EE (Servlets), JDBC, Thymeleaf (Template Engine), HTML5, CSS3.
* **Key Features:** Server-side validation, session management, and pagination. Playlists display 5 songs at a time per block, utilizing server-rendered "Previous" and "Next" buttons without any client-side JavaScript.

### 2. RIA Version (Rich Internet Application)
* **Architecture:** Single Page Application (SPA) with a REST-like backend.
* **Tech Stack:** Java EE (Servlets), JDBC, HTML5, CSS3, Vanilla JavaScript (AJAX).
* **Key Features:** 
  * Seamless asynchronous navigation without full page reloads using a custom `makeCall` AJAX utility.
  * Custom **drag-and-drop playlist reordering** inside a modal, allowing users to define and save a personalized sorting sequence instead of the default alphabetical/chronological order.
  * Local state management (DOM manipulation and session storage).

---

## 🗄️ Database Schema

The relational database (MySQL) is structured around 4 main entities to ensure data normalization and integrity:
* **User:** `username`, `password`, `name`, `surname`
* **Song:** `title`, `genre`, `mp3`, `album_title`, `album_artist`, `album_year`, `album_image`, `user`
* **Playlist:** `title`, `date`, `user`
* **PlaylistSongs:** `playlist`, `song`, `pos` (Handles the Many-to-Many relationship between playlists and songs, as well as the custom ordering for the RIA version).

---

## 📁 Repository Structure

The codebase follows the standard structure for Java Web projects (Eclipse/IntelliJ):

```text
.
├── src/main/java/it/polimi/tiw/project/  # Backend source code (MVC Architecture)
│   ├── beans/                            # Data models (User.java, Song.java, Playlist.java, Genres.java)
│   ├── controller/                       # Routing Servlets (LoginServlet, PlaylistServlet, etc.)
│   └── dao/                              # Data Access Objects for MySQL database queries
├── src/main/webapp/                      # Frontend resources and configurations
│   ├── WEB-INF/
│   │   ├── lib/                          # External libraries (Thymeleaf .jar, MySQL Connector, etc.)
│   │   └── web.xml                       # Application deployment descriptor
│   ├── *.html                            # Thymeleaf dynamic templates (home.html, playlist.html, etc.)
│   └── style.css                         # Global stylesheet
├── report.pdf                            # Final technical report and project documentation
└── README.md                             # Repository documentation
```

---

## 🚀 Setup & Execution

### Prerequisites
* **Java Development Kit (JDK):** Version 11 or higher.
* **Application Server:** Apache Tomcat 9.x (or compatible).
* **Database:** MySQL Server 8.0+.
* **IDE:** Eclipse IDE for Enterprise Java / IntelliJ IDEA Ultimate.

### Installation Steps
1. **Database Setup:** 
   * Create a new MySQL database instance.
   * Update the database credentials (`db.url`, `db.user`, `db.password`) inside the Java DAO context configuration.
2. **Server Configuration:**
   * Open the project in your IDE and configure it as a "Dynamic Web Module".
   * Add Apache Tomcat to the server runtime environments.
   * Ensure the necessary `.jar` dependencies (MySQL Connector, Thymeleaf, and Gson/JSON libraries for the RIA backend) are included in the `WEB-INF/lib` folder.
3. **Run the App:**
   * Deploy the projects on Tomcat and start the server.
   * Access the applications at `http://localhost:8080/project_TIW_PureHTML/` and `http://localhost:8080/project_TIW_RIA/` (URLs may vary depending on your Tomcat configuration).
