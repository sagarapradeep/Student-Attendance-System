package lk.ijse.dep10.app.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.util.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.Optional;

public class StudentViewController {

    public ImageView imgPicture;
    public Label lblTitle;
    public AnchorPane root;
    @FXML
    private Button btnBrows;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private TableView<Student> tblStudent;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtStudentId;

    @FXML
    private TextField txtStudentName;


    public void initialize() {
        Platform.runLater(btnNewStudent::fire);

        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("picture1"));
        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));


        loadData();

        tblStudent.getSelectionModel().selectedItemProperty().addListener((pv,value,current) -> {
            if (current == null) {
                btnDelete.setDisable(true);
                return;
            }

            btnDelete.setDisable(false);
            txtStudentName.setText(current.getName());
            txtStudentId.setText(current.getId());
            Image image = null;
            if (current.getPicture() == null) {
                image = new Image("/img/user.png");
                btnClear.setDisable(true);
            } else {
                try {
                    image = new Image(current.getPicture().getBinaryStream());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                btnClear.setDisable(false);
            }

            imgPicture.setImage(image);

        });

        txtSearch.textProperty().addListener((pv, value,current) -> {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                if (txtSearch.getText().isEmpty()||txtSearch.getText().isBlank()) {
                    loadData();
                    return;
                }




                PreparedStatement stmStudent = connection.prepareStatement("SELECT *FROM Student WHERE id LIKE ? OR name LIKE ?");
                stmStudent.setString(1, "%" + current + "%");
                stmStudent.setString(2, "%" + current + "%");
                ResultSet rstStudent = stmStudent.executeQuery();

                ObservableList<Student> studentList = tblStudent.getItems();
                studentList.clear();

                PreparedStatement stmPicture = connection.prepareStatement("SELECT *FROM Picture WHERE id=?");


                while (rstStudent.next()) {
                    Blob image = null;
                    String id = rstStudent.getString(1);
                    String name = rstStudent.getString(2);

                    stmPicture.setString(1, id);
                    ResultSet rstPicture = stmPicture.executeQuery();

                    if (rstPicture.next()) {
                        image = rstPicture.getBlob(2);

                    }
                    studentList.add(new Student(id, name, image));

                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

        root.setOnKeyPressed((keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                btnSave.fire();

            }
        }));

    }

    private void loadData() {
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement stmStudent = connection.createStatement();
            ResultSet rst1 = stmStudent.executeQuery("SELECT *FROM Student");

            PreparedStatement stmPicture = connection.prepareStatement("SELECT *FROM Picture WHERE id=?");

            while (rst1.next()) {
                String id = rst1.getString(1);
                String name = rst1.getString(2);

                Blob picture = null;
                stmPicture.setString(1, id);
                ResultSet rst2 = stmPicture.executeQuery();

                if (rst2.next()) {
                    picture = rst2.getBlob(2);
                }
                Student student = new Student(id, name, picture);
                tblStudent.getItems().add(student);

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void btnBrowsOnAction(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select picture to update");
        fileChooser.getExtensionFilters().
                add(new FileChooser.ExtensionFilter("*.jpeg", "Image files", "*jpg", "*.png", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(btnBrows.getScene().getWindow());

        if (file == null) {
            imgPicture.setImage(new Image("/img/user.png"));
        } else {
            try {
                imgPicture.setImage(new Image(file.toURI().toURL().toString()));
                btnClear.setDisable(false);
            } catch (MalformedURLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to load the selected Image! Try again").showAndWait();
                throw new RuntimeException(e);
            }

        }

    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        imgPicture.setImage(new Image("/img/user.png"));
        btnClear.setDisable(true);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Connection connection = DBConnection.getInstance().getConnection();
        Student selectedStudent = tblStudent.getSelectionModel().getSelectedItem();
        String id = selectedStudent.getId();

        Optional<ButtonType> optional = new Alert(Alert.AlertType.CONFIRMATION, "Are sure to delete selected entity", ButtonType.YES, ButtonType.NO).showAndWait();

        if ((optional.get() == ButtonType.NO)||optional.get()==null) {
            return;

        }

        try {

            if (!(selectedStudent.getPicture() == null)) {
                PreparedStatement stmPicture = connection.prepareStatement("DELETE FROM Picture WHERE id=?");
                stmPicture.setString(1, id);
                stmPicture.executeUpdate();
            }

            PreparedStatement stmStudent = connection.prepareStatement("DELETE FROM Student WHERE id=?");
            stmStudent.setString(1, id);
            stmStudent.executeUpdate();

            tblStudent.getItems().remove(selectedStudent);
            tblStudent.refresh();

            if (tblStudent.getItems().isEmpty()) {
                btnNewStudent.fire();
            }




        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to connect database try again!").showAndWait();

        }


    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        txtSearch.clear();
        txtStudentName.clear();
        tblStudent.getSelectionModel().clearSelection();
        txtStudentId.setText(generateID());
        txtStudentName.requestFocus();
        btnClear.setDisable(true);
        btnDelete.setDisable(true);
        imgPicture.setImage(new Image("/img/user.png"));


    }

    private String generateID() {
        ObservableList<Student> studentList = tblStudent.getItems();
        if (studentList.isEmpty()) {
            return "DEP-10/S-001";
        }
        String lastId = studentList.get(studentList.size() - 1).getId();
        lastId = lastId.substring(9);
        int newId = Integer.parseInt(lastId);
        String newID2 = String.format("DEP-10/S-%03d", (newId + 1));

        return newID2;
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) {
            return;
        }

        Connection connection = DBConnection.getInstance().getConnection();
        String id = txtStudentId.getText();
        String name = txtStudentName.getText();


        try {

            connection.setAutoCommit(false);

            /*save changes*/
            if (!(tblStudent.getSelectionModel().getSelectedItem() == null)) {

                Student selectedStudent = tblStudent.getSelectionModel().getSelectedItem();


                PreparedStatement stmModifyStudent = connection.prepareStatement("UPDATE Student SET name=? WHERE id=?");
                stmModifyStudent.setString(1, txtStudentName.getText());
                stmModifyStudent.setString(2, txtStudentId.getText());
                stmModifyStudent.executeUpdate();

                Blob image = null;
                if (!btnClear.isDisable()) {
                    PreparedStatement stmUpdatePicture = connection.prepareStatement("INSERT INTO Picture(id,picture) values(?,?)");
                    image = new SerialBlob(fxImgToBlob(stmUpdatePicture));
                    System.out.println("Image "+image);
                    stmUpdatePicture.setString(1, txtStudentId.getText());
                    stmUpdatePicture.setBlob(2  , image);
                    stmUpdatePicture.executeUpdate();

                }

                selectedStudent.setName(txtStudentName.getText());
                selectedStudent.setPicture(image);

                tblStudent.refresh();
                btnNewStudent.fire();

                new Alert(Alert.AlertType.INFORMATION, "Modified entity successfully saved to database!").show();


                return;

            }


            /*save new student*/

            PreparedStatement stmStudent = connection.prepareStatement("INSERT INTO Student(id, name) VALUES (?,?)");
            stmStudent.setString(1, id);
            stmStudent.setString(2, name);
            stmStudent.executeUpdate();


            Student newStudent = new Student(id, name, null);

            if (!btnClear.isDisable()) {
                PreparedStatement stmPicture = connection.prepareStatement("INSERT INTO Picture (id, picture) VALUES (?,?)");

                Blob image = new SerialBlob(fxImgToBlob(stmPicture));

                stmPicture.setString(1, id);
                stmPicture.setBlob(2, image);
                stmPicture.executeUpdate();
                newStudent.setPicture(image);
            }

            tblStudent.getItems().add(newStudent);

            btnNewStudent.fire();


        } catch (Throwable throwable) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        new Alert(Alert.AlertType.INFORMATION, "New entity successfully saved!").show();

    }

    public byte[] fxImgToBlob(PreparedStatement stmPicture) {
        Image picture = imgPicture.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(picture, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bos);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save the image try again!").showAndWait();
            btnNewStudent.fire();

        }
        byte[] bytes = bos.toByteArray();

        return bytes;

    }

    private boolean isDataValid() {
        boolean dataValid = true;
        String name = txtStudentName.getText();

        if (!name.matches("[A-Za-z]{3,}( [A-Za-z]{3,})?")) {
            dataValid = false;
            txtStudentName.requestFocus();
            txtStudentName.selectAll();
            new Alert(Alert.AlertType.ERROR, "Invalid name!").show();
        }
        System.out.println(dataValid);

        return dataValid;
    }

}
