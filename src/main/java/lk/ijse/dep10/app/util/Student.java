package lk.ijse.dep10.app.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.text.Element;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

public class Student implements Serializable {
    private String id;
    private String name;
    private Blob picture;

    public Student(String id, String name, Blob picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public Student() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
        return picture;
    }

    public ImageView getPicture1() {
        ImageView imageView = null;
        try {

            Image image;
            if (picture == null) {
                image = new Image("/img/user.png", 50, 50, false, false);
                return new ImageView(image);

            }

            image = new Image(picture.getBinaryStream(), 50, 50, false, false);
            imageView = new ImageView(image);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



        return imageView;
    }
    public void setPicture(Blob picture) {
        this.picture = picture;
    }
}
