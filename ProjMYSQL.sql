-- Made by: Sumitr Banik, Corey Woodman

DROP VIEW IF EXISTS P_FASTEST;
DROP VIEW IF EXISTS P_Total;
DROP VIEW IF EXISTS C_Score;
DROP VIEW IF EXISTS Sb_Score;
DROP VIEW IF EXISTS U_Score;
DROP VIEW IF EXISTS T_Score;

DROP TABLE IF EXISTS User_has_Team;
DROP TABLE IF EXISTS Submissions;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS University;
DROP TABLE IF EXISTS Subdivisions;
DROP TABLE IF EXISTS Country;
DROP TABLE IF EXISTS Job;
DROP TABLE IF EXISTS Problem;
DROP TABLE IF EXISTS Team;
DROP TABLE IF EXISTS Contest;

CREATE TABLE Country (
C_Name VARCHAR(45) NOT NULL UNIQUE, C_Ranking int NOT NULL, C_Flag BLOB NOT NULL,
PRIMARY KEY (C_Name)
);

CREATE TABLE Subdivisions (
Sb_Name VARCHAR(45) NOT NULL UNIQUE, C_Name VARCHAR(45) UNIQUE, Sb_Flag BLOB,
FOREIGN KEY (C_Name) REFERENCES Country(C_Name) ON DELETE CASCADE,
PRIMARY KEY (Sb_Name, C_Name)
);

CREATE TABLE University (
U_Name VARCHAR(45) NOT NULL UNIQUE, U_Ranking int NOT NULL, U_Flag BLOB NOT NULL, C_Name VARCHAR(45) NOT NULL UNIQUE, Sb_Name VARCHAR(45),
FOREIGN KEY (C_Name) REFERENCES Country(C_Name) ON UPDATE CASCADE,
FOREIGN KEY (Sb_Name) REFERENCES Subdivisions(Sb_Name) ON DELETE CASCADE,
PRIMARY KEY (U_Name, C_Name)
);

CREATE TABLE Contest (
Con_Name VARCHAR(45) NOT NULL UNIQUE, Start_Time DATETIME NOT NULL, Length DATETIME NOT NULL, Remaining DATETIME AS (DATEDIFF(DATEDIFF(End_Time, Start_Time), Length)) NOT NULL, 
End_Time DATETIME NOT NULL,
PRIMARY KEY (Con_Name)
);

CREATE TABLE Problem (
P_ID VARCHAR(45) NOT NULL UNIQUE, P_Name VARCHAR(45) NOT NULL, Memory_Limit int, CPU_Time_Limit int, P_DESC VARCHAR(45) NOT NULL, Difficulty DECIMAL(1) NOT NULL,
Author VARCHAR(45) NOT NULL, P_SOURCE VARCHAR(45) NOT NULL, LICENSE VARCHAR(45) NOT NULL, Con_Name VARCHAR(45),
FOREIGN KEY (Con_Name) REFERENCES Contest(Con_Name) ON UPDATE CASCADE,
PRIMARY KEY (P_ID)
);

CREATE TABLE Team (
T_Name VARCHAR(45) NOT NULL UNIQUE, T_Ranking int NOT NULL, Con_Name VARCHAR(45) NOT NULL,
FOREIGN KEY (Con_Name) REFERENCES Contest(Con_Name) ON UPDATE CASCADE,
PRIMARY KEY (T_Name, Con_Name)
);

CREATE TABLE Users (
Username VARCHAR(45) NOT NULL UNIQUE, Us_Name VARCHAR(45) NOT NULL, Us_Ranking int NOT NULL, Profile_Picture BLOB, Preferred_Language VARCHAR(45), Preferred_Timezone VARCHAR(45), 
Default_Prog_Lang VARCHAR(45), U_Password VARCHAR(45) NOT NULL, Con_Name VARCHAR(45), Email VARCHAR(45) NOT NULL, U_Name VARCHAR(45), Sb_Name VARCHAR(45), C_Name VARCHAR(45),
User_Score DECIMAL(1) DEFAULT ('1.0'),
FOREIGN KEY (C_Name) REFERENCES Country(C_Name) ON UPDATE CASCADE,
FOREIGN KEY (Con_Name) REFERENCES Contest(Con_Name) ON UPDATE CASCADE,
FOREIGN KEY (Sb_Name) REFERENCES Subdivisions(Sb_Name) ON UPDATE CASCADE,
FOREIGN KEY (U_Name) REFERENCES University(U_Name) ON UPDATE CASCADE,
PRIMARY KEY (Username)
);

CREATE TABLE User_has_Team (
Username VARCHAR(45) NOT NULL UNIQUE, T_Name VARCHAR(45) NOT NULL UNIQUE,
FOREIGN KEY (Username) REFERENCES Users(Username) ON UPDATE CASCADE,
FOREIGN KEY (T_Name) REFERENCES Team(T_Name) ON UPDATE CASCADE,
PRIMARY KEY(Username, T_Name)
);

CREATE TABLE Submissions (
Username VARCHAR(45) NOT NULL UNIQUE, P_ID VARCHAR(45) NOT NULL UNIQUE, Time_Taken DECIMAL(2) NOT NULL, Memory_Used int NOT NULL, T_Name VARCHAR(45), Con_Name VARCHAR(45),
Sub_Date DATETIME NOT NULL, Lang VARCHAR(45) NOT NULL,
FOREIGN KEY (T_Name) REFERENCES Team(T_Name) ON UPDATE CASCADE,
FOREIGN KEY (Con_Name) REFERENCES Contest(Con_Name) ON UPDATE CASCADE,
FOREIGN KEY (P_ID) REFERENCES Problem(P_ID) ON UPDATE CASCADE,
FOREIGN KEY (Username) REFERENCES Users(Username) ON UPDATE CASCADE,
PRIMARY KEY(Username, P_ID)
);

CREATE TABLE Job (
Job_Title varchar(45) NOT NULL, Company_Name varchar(45) NOT NULL, Company_Logo blob NOT NULL,
PRIMARY KEY (Job_Title)
);


-- Views are created for most derived attributes as some of them required information from tables that had to be made after them.
CREATE VIEW P_FASTEST AS SELECT MIN(Time_Taken) FROM Submissions;

CREATE VIEW P_Total AS SELECT COUNT(*) FROM Submissions WHERE P_ID in (select P_ID from Submissions);

CREATE VIEW C_Score AS SELECT AVG(User_Score) FROM Users WHERE C_Name in (select C_Name from Users);

CREATE VIEW Sb_Score AS SELECT AVG(User_Score) FROM Users WHERE Sb_Name in (select Sb_Name from Users);

CREATE VIEW U_Score AS SELECT AVG(User_Score) FROM Users WHERE U_Name in (select U_Name from Users);

CREATE VIEW T_Score AS SELECT AVG(User_Score) FROM Users WHERE U_Name in (select T_Name from User_has_Team);