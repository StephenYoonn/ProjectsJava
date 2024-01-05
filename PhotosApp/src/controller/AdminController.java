package controller;

import java.io.*;
import java.util.*;
import javafx.beans.binding.Bindings;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.*;


public class AdminController {
    @FXML private ListView<User> listView;
    @FXML private Button deleteBtn, addBtn, logoutBtn;
    @FXML private TextField usernameTxt;
    private Stage primaryStage;

    public void start(Stage stage, List<User> users) throws IOException {
        ObservableList<User> usersList = FXCollections.observableArrayList(users);
        this.primaryStage = stage;

        setupListView(usersList);
        setupButtonBindings(usersList);

        addBtn.setOnAction(event -> addUser(usersList));
        deleteBtn.setOnAction(event -> deleteUser(usersList));
        logoutBtn.setOnAction(event -> logout());
    }

    private void setupListView(ObservableList<User> usersList) {
        listView.getSelectionModel().selectedIndexProperty()
            .addListener((obs, oldVal, newVal) -> displayUserInfo(usersList));

        if (!usersList.isEmpty()) {
            listView.setItems(usersList);
            listView.getSelectionModel().selectFirst();
            displayUserInfo(usersList);
        }
    }

    private void setupButtonBindings(ObservableList<User> usersList) {
        addBtn.disableProperty().bind(Bindings.isEmpty(usernameTxt.textProperty()));
        deleteBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
    }

    private void addUser(ObservableList<User> usersList) {
        User newUser = new User(usernameTxt.getText());
        if (!userExists(newUser, usersList)) {
            newUser.addAlbum(createStockAlbum());
            usersList.add(newUser);
            listView.setItems(usersList);
            listView.getSelectionModel().select(newUser);
            displayUserInfo(usersList);
            saveData();
        }
    }

    private void deleteUser(ObservableList<User> usersList) {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            usersList.remove(selectedIndex);
            listView.setItems(usersList);
            if (!usersList.isEmpty()) {
                listView.getSelectionModel().select(Math.min(selectedIndex, usersList.size() - 1));
                displayUserInfo(usersList);
            } else {
                usernameTxt.clear();
            }
            saveData();
        }
    }

    private boolean userExists(User newUser, ObservableList<User> usersList) {
        for (User user : usersList) {
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                showWarning("User already exists");
                return true;
            }
        }
        return false;
    }

    private void displayUserInfo(ObservableList<User> usersList) {
        if (!usersList.isEmpty()) {
            User selectedUser = listView.getSelectionModel().getSelectedItem();
            usernameTxt.setText(selectedUser.getUsername());
        }
    }
  
    private Album createStockAlbum() {
        Album stockAlbum = new Album("stock");
        File photoDirectory = new File("data");
        File[] photos = photoDirectory.listFiles((dir, name) -> name.endsWith(".jpg"));

        if (photos != null) {
            for (File photo : photos) {
                Image image = new Image(photo.toURI().toString());
                ImageSerial imageSerial = new ImageSerial(image);
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(photo.lastModified());
                Picture picture = new Picture(imageSerial, date, "Stock Photo", photo.getName());
                stockAlbum.addPicture(picture);
            }
        }
        return stockAlbum;
    }

    private void logout() {
        try {
            primaryStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            AnchorPane root = loader.load();
            LoginController loginController = loader.getController();
            Stage loginStage = new Stage();
            loginController.start(loginStage);
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void saveData() {
        try (FileOutputStream fos = new FileOutputStream("data/dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(new ArrayList<>(listView.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(primaryStage);
        alert.setTitle("Warning");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
