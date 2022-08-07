package application;
// Code used by Dr Tasse in CS1083
//hacked up by OFK, March 2020
//last modified March 25, 2022

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.text.Font;


import java.sql.*;
import javax.sql.*;
import java.util.*;

public class Main2 extends Application
{
TextArea mainText;
TextField Username;
TextField Password;
TextField sourceCode;
Button OKbutton;
Button RanklistButton;

ChoiceBox<String> firstLetterLastNameChoice;
ChoiceBox<String> langChoices;

String[] letterChoices = {"N"};
String[] LanguageChoices = {"Java"};

Connection con = null;



public void start(Stage primaryStage)
{
   Font mainFont = new Font("courier", 24);
   mainText = new TextArea();
   mainText.setFont(mainFont);
   mainText.setPrefRowCount(6);
   mainText.setPrefColumnCount(30);
   mainText.setWrapText(true);

   // open the JDBC connection, get problem names
   // On exception write error msg to mainText.

    try {
    	 Class.forName("com.mysql.cj.jdbc.Driver");
    	 con=DriverManager.getConnection("jdbc:mysql://pizza.unbsj.ca:3306/Woodman1103lab?useSSL=false","Woodman1103","3728641");
        
        Statement s=con.createStatement();
        ResultSet r=s.executeQuery("select distinct P_ID as Name from Problem;");

        ArrayList<String> problemChoices = new ArrayList<>();

        while (r.next())
        	problemChoices.add(r.getString("Name")); 

        // bad style: should be in a finally block, really
        r.close(); s.close();
        
        s=con.createStatement();
        r=s.executeQuery("select distinct Lang as Language from Submissions;");

        ArrayList<String> langChoices = new ArrayList<>();

        while (r.next())
        	langChoices.add(r.getString("Language")); 
        
     // bad style: should be in a finally block, really
        r.close(); s.close();
        
        langChoices.add("Malbolge");

        // move them over into an array
        letterChoices = new String[problemChoices.size()];
        LanguageChoices = new String[langChoices.size()];
	   int ctr=0;
        for (String choice : problemChoices) {
          letterChoices[ctr++]=choice;
        }
        
        ctr = 0;
        for (String choice : langChoices) {
          LanguageChoices[ctr++]=choice;
        }
 
    } catch (Exception e) {
        mainText.setText("error "+e);
    }

    sourceCode = new TextField();
    sourceCode.setFont(mainFont);
    sourceCode.setMinWidth(500);
    sourceCode.setText("input source code here");
    
    Username = new TextField();
    Username.setFont(mainFont);
    Username.setMinWidth(500);
    Username.setText("input Username here (andrew-roberts)");
    
    Password = new TextField();
    Password.setFont(mainFont);
    Password.setMinWidth(500);
    Password.setText("input Password here (pass for andrew-roberts is Password1)");
    
    

   // code for choice box      
   firstLetterLastNameChoice = new ChoiceBox<String>();
   firstLetterLastNameChoice.setStyle("-fx-font: 24px \"Courier\";");  
   firstLetterLastNameChoice.getItems().addAll(letterChoices);
   firstLetterLastNameChoice.getSelectionModel().select(0);
   
   langChoices = new ChoiceBox<String>();
   langChoices.setStyle("-fx-font: 24px \"Courier\";");  
   langChoices.getItems().addAll(LanguageChoices);
   langChoices.getSelectionModel().select(0);

   // code for VendorSearch  button and label
   // The action for this button will eventually search for vendors in the specified
   // area code whose last names start with the chosen letter

   // Only create a functioning button if we have a connection
   if (con != null) {
     OKbutton = new Button("Submit Problem");
     OKbutton.setFont(mainFont);
     OKbutton.setOnAction(arg0 -> {
		try {
			processButton(arg0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	});
   }
   else {
      OKbutton = new Button("Oops No Connection");
      OKbutton.setFont(mainFont);
   }
   
   if (con != null) {
	   RanklistButton = new Button("See Ranklist");
	   RanklistButton.setFont(mainFont);
	   RanklistButton.setOnAction(arg0 -> {
			try {
				processRanklistButton(arg0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	   }
	   else {
		   RanklistButton = new Button("Oops No Connection");
		   RanklistButton.setFont(mainFont);
	   }

   // putting it all together
   VBox pane = new VBox(Username, Password, sourceCode,
                        firstLetterLastNameChoice, langChoices,
                        OKbutton, RanklistButton, mainText);
   pane.setSpacing(10);
   Scene theScene = new Scene(pane, 1600, 800);
   primaryStage.setTitle("Problem Submission");
   primaryStage.setScene(theScene);

   primaryStage.show();
}


public void processRanklistButton(ActionEvent event) throws SQLException{
	String msg1 = "";
	
	try {
	       Statement s=con.createStatement();
	       s.executeUpdate("DROP VIEW IF EXISTS U_Score;");
	       s.executeUpdate("CREATE VIEW U_Score AS select Users.Username, Us_Name, Us_Ranking, Profile_Picture, Preferred_Language, Preferred_Timezone, Default_Prog_Lang, "
	       		+ "U_Password, Users.Con_Name, Email, U_Name, Sb_Name, C_Name, SUM(POWER(Problem.Difficulty, 3)) as Score "
	       		+ "from Problem join Submissions on Problem.P_ID = Submissions.P_ID join Users on Users.Username = Submissions.Username group by Username;");
	       
	       ResultSet r=s.executeQuery("SELECT Us_Name, Score FROM U_Score where Score > (select Score from U_Score where Username = " + "'" + Username.getText() + "'" + ") order by Score DESC limit 5;");
	       while (r.next()) {
	    	  msg1 = (msg1 + r.getString("Us_Name") + "\t");
	    	  msg1 = (msg1+ r.getString("Score") + "\n");
	       }
	       
	    	 r.close(); s.close();
	    	 
	   } catch (Exception e) {
	        msg1 = "error "+e;
	   }
	
	try {
	       Statement s=con.createStatement();
	       ResultSet r = s.executeQuery("select Score from U_Score where Username = " + "'" + Username.getText() + "'" + ";");
	       while (r.next()) {
	    	   msg1 = msg1 + "My Score: " + r.getString("Score") + "\n";
	       }
	    	 r.close(); s.close();
	    	 
	   } catch (Exception e) {
	        msg1 = "error "+e;
	   }
	
	try {
	       Statement s=con.createStatement();
	       ResultSet r=s.executeQuery("SELECT Us_Name, Score FROM U_Score where Score < (select Score from U_Score where Username = " + "'" + Username.getText() + "'" + ") order by Score DESC limit 2;");
	       while (r.next()) {
	    	  msg1 = (msg1 + r.getString("Us_Name") + "\t");
	    	  msg1 = (msg1+ r.getString("Score") + "\n");
	       }
	       
	    	 r.close(); s.close();
	    	 
	   } catch (Exception e) {
	        msg1 = "error "+e;
	   }
	
	mainText.setText(msg1);;
}

// code for the Submission Button
public void processButton(ActionEvent event) throws SQLException
{
   String msg = "";  // the formatted result of the query should be put in this string
   //the P_ID
   String usersChoiceForFirstLetterOfLastName = firstLetterLastNameChoice.getSelectionModel().getSelectedItem();
   // Language choice
   String languageChoiceForUser = langChoices.getSelectionModel().getSelectedItem();
   
// submit(User_Log, Pass, Problem_ID, Lang, Source_Code, Result)
   CallableStatement ps = null;
	   ps = con.prepareCall("CALL Submit(?, ?, ?, ?, ?, ?)");
	   ps.setString(1,Username.getText());
	   ps.setString(2,Password.getText());
	   ps.setString(3,usersChoiceForFirstLetterOfLastName); 
	   ps.setString(4,languageChoiceForUser); 
	   ps.setString(5,sourceCode.getText());
	   ps.registerOutParameter(6, Types.VARCHAR);

   try {
       Statement s=con.createStatement();
       ResultSet r=ps.executeQuery();
       //while (r.next()) {
       msg = ps.getString(6);
    	  System.out.println(msg);
       //}
       
    	 r.close(); s.close();
    	 
   } catch (Exception e) {
        msg = "error "+e;
   }

   //mainText.setText(); // to debug (shows the sql statement from prepared statement
   mainText.setText(msg);  // TO SEE THE OUTPUT
}

public static void main(String[] args)
{
   launch(args);
}
}

