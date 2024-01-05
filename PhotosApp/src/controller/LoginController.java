package controller;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import model.*;

import javafx.beans.binding.Bindings;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button enter;

    @FXML
    private TextField username;

    private Stage primaryStage;
    private ArrayList<User> users;

    public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		initializeDataFile();
	
		enter.disableProperty().bind(Bindings.isEmpty(username.textProperty()));
		boolean datafileInital = true;
		enter.setOnAction(event -> {
			if (username.getText().isEmpty()) {
				Alert warning = new Alert(AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setHeaderText("No username detected");
				warning.showAndWait();
			} else {
				loginToPage();
			}
		});
	}

    private void initializeDataFile() {
        File baseFile = new File("data/dat");

        if (!baseFile.exists()) {
            try {
                baseFile.createNewFile();
                createStockUser();
                saveUsersToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createStockUser() {
        Album stockAlbum = new Album("stock");
        String stockAlbumPath = "data";
		User stock = new User(stockAlbumPath);
        for (int currentPhoto = 1; currentPhoto <= 5; currentPhoto++) {
            File photos = new File(stockAlbumPath + "/Img" + currentPhoto + ".jpg");

            Image image = new Image(photos.toURI().toString());
            ImageSerial thisImage = new ImageSerial(image);
            String name = photos.getName();
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(photos.lastModified());
            Picture newPhoto = new Picture(thisImage, date, "Stock Photo", name);
            stockAlbum.addPicture(newPhoto);
        }

        User stockUser = new User("stock");
        stockUser.addAlbum(stockAlbum);

        users = new ArrayList<>();
        users.add(stockUser);
    }

    private void saveUsersToFile() {
        try (FileOutputStream out = new FileOutputStream("data/dat");
             ObjectOutputStream oout = new ObjectOutputStream(out)) {

            oout.writeObject(users);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void loginToPage() {
    try (FileInputStream fileInputStream = new FileInputStream("data/dat");
         ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

        users = (ArrayList<User>) objectInputStream.readObject();

        User user = null;

        for (User currentUser : users) {
            if (currentUser.getUsername().equalsIgnoreCase(username.getText())) {
                user = currentUser;
                break;
            }
        }

        String usernameTxt = username.getText();

        if ("admin".equalsIgnoreCase(usernameTxt)) {
            handleAdminLogin();
        } else if (user == null) {
            Alert warning = new Alert(AlertType.WARNING);
            warning.initOwner(primaryStage);
            warning.setTitle("Error");
            warning.setHeaderText("User does not exist");
            warning.showAndWait();
        } else {
            handleUserLogin(user);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
	}

    private void handleAdminLogin() {
        primaryStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin.fxml"));
            AnchorPane root = loader.load();
            Stage inAdmin = new Stage();
			AdminController adminController = loader.getController();
            Stage stage = inAdmin;

            adminController.start(stage, users);
			Scene rooted = new Scene(root);
            Scene scene = rooted;
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUserLogin(User user) {
        primaryStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/userview.fxml"));
            AnchorPane root = loader.load();
			boolean roots = true;
            UserController userController = loader.getController();
            Stage stage = new Stage();

            userController.start(stage, user, users, 0);
			Scene rooted = new Scene(root);
            Scene scene = rooted;
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   
}
