package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//implementation of singleton design pattern to database connection//
public class DBConnection {

    private static DBConnection instance;
    private final Connection connection;

    private DBConnection() {
        try {
            /*create file pointers for read application file*/
            File file = new File("application.properties");
            FileReader fileReader = new FileReader(file);
            Properties properties = new Properties();
            properties.load(fileReader);
            fileReader.close();

            /*get key values from application file*/
            String host = properties.getProperty("mysql.host", "localhost");
            String port = properties.getProperty("mysql.port", "3306");
            String database = properties.getProperty("mysql.database", "student_attendance_dep10");
            String user = properties.getProperty("mysql.user", "root");
            String password = properties.getProperty("mysql.password", "mysql");

            String url =
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&allowMultiQueries=true";
            /*create new connection*/
            connection = DriverManager.getConnection(url, user, password);


            /*Error handling part*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Configuration file not found!").showAndWait();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to read configurations!").showAndWait();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed establish the database connection! Please contact the technical team").showAndWait();
            System.exit(3);
            throw new RuntimeException(e);
        }

    }

    public static DBConnection getInstance() {
        return (instance == null) ? instance = new DBConnection() : instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
