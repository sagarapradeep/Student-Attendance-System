package lk.ijse.dep10.app;

import javafx.application.Application;
import javafx.stage.Stage;
import lk.ijse.dep10.app.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        /*close database connection when exit from the app*/
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!DBConnection.getInstance().getConnection().isClosed()) {
                    DBConnection.getInstance().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        generateSchemaIfNotExist();
    }

    private void generateSchemaIfNotExist() {

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet showTables = statement.executeQuery("SHOW TABLES ");

            ArrayList<String> tableNameList = new ArrayList<>();

            while (showTables.next()) {
                tableNameList.add(showTables.getString(1));
            }

            boolean tableExist = tableNameList.containsAll(Set.of("Attendance", "Picture", "Student", "User"));

            readDBScript();

            if (!tableExist) {
                statement.execute(readDBScript());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String readDBScript() {
        InputStream is = getClass().getResourceAsStream("/schema.sql");

        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));

            StringBuilder dbScript = new StringBuilder();

            String line = null;

            while ((line = bf.readLine()) != null) {
                dbScript.append(line + "\n");
            }

            return dbScript.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
