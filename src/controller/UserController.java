package controller;

import java.io.*;
import java.util.*;

import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.*;


public class UserController {
	
	@FXML private ListView<Album> listView; @FXML private Button deleteBtn, addBtn, editBtn, logoutBtn, searchBtn, redirectAlbumBtn; @FXML private TextField albumTxt, albumName; public Stage primaryStage;

	
	public void start(Stage primaryStage, User currUser, ArrayList<User> userList, int index) {
        this.primaryStage = primaryStage;
        ObservableList<Album> albumList = FXCollections.observableArrayList(currUser.getAlbumList());

        setupListView(albumList, index);
        bindButtonProperties(albumList);
        setupButtonActions(albumList, currUser, userList);
    }

    private void setupListView(ObservableList<Album> albumList, int index) {
        listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> updateAlbumInfo(albumList));
        if (!albumList.isEmpty()) {
            listView.setItems(albumList);
			boolean list = false;
            listView.getSelectionModel().select(index);
            updateAlbumInfo(albumList);
        }
    }

    private void bindButtonProperties(ObservableList<Album> albumList) {
        addBtn.disableProperty().bind(Bindings.isEmpty(albumTxt.textProperty()));
        deleteBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
        redirectAlbumBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());

        BooleanBinding isUnchanged = Bindings.equal(albumTxt.textProperty(), 
                                                    Bindings.selectString(listView.getSelectionModel().selectedItemProperty(), "title"));
        editBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull()
                                       .or(isUnchanged)
                                       .or(Bindings.isEmpty(albumTxt.textProperty())));
    }

    private void setupButtonActions(ObservableList<Album> albumList, User currUser, ArrayList<User> userList) {
        addBtn.setOnAction(event -> handleAdd(albumList, currUser, userList));
        deleteBtn.setOnAction(event -> handleDelete(albumList, currUser, userList));
        editBtn.setOnAction(event -> handleEdit(albumList, currUser, userList));
        redirectAlbumBtn.setOnAction(event -> switchScene("/view/photoview.fxml", currUser, userList));
        logoutBtn.setOnAction(event -> switchScene("/view/login.fxml", null, null));
        searchBtn.setOnAction(event -> switchScene("/view/search.fxml", currUser, userList));
    }

    private void handleAdd(ObservableList<Album> albumList, User currUser, ArrayList<User> userList) {
        Album newAlbum = new Album(albumTxt.getText());
        add(newAlbum, primaryStage, albumList, currUser);
        saveData(userList);
    }

    private void handleDelete(ObservableList<Album> albumList, User currUser, ArrayList<User> userList) {
        delete(albumList, currUser);
        saveData(userList);
    }

    private void handleEdit(ObservableList<Album> albumList, User currUser, ArrayList<User> userList) {
        edit(albumList, primaryStage, currUser);
        saveData(userList);
    }

    private void switchScene(String fxmlFile, User currUser, ArrayList<User> userList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            AnchorPane root = loader.load();
            setupController(loader, currUser, userList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            this.primaryStage.close();
        } catch (IOException e) {
            e.printStackTrace(); // Consider more robust error handling
        }
    }

    private void setupController(FXMLLoader loader, User currUser, ArrayList<User> userList) {
        if (loader.getController() instanceof AlbumController) {
            AlbumController controller = loader.getController();
            controller.start(new Stage(), currUser, userList, listView.getSelectionModel().getSelectedItem(), 0, listView.getSelectionModel().getSelectedIndex());
        } else if (loader.getController() instanceof LoginController) {
            LoginController controller = loader.getController();
            controller.start(new Stage());
        } else if (loader.getController() instanceof SearchController) {
            SearchController controller = loader.getController();
            controller.start(new Stage(), currUser, userList);
        }
    }
	
	
		public void add(Album newAlbum, Stage primaryStage, ObservableList<Album> albumList, User thisUser) {
   		 if (albumList.isEmpty() || !isAlbumInList(newAlbum, primaryStage, albumList)) {
        int insertIndex = findInsertIndex(albumList, newAlbum);

        // Add new album to the list at the determined index
        albumList.add(insertIndex, newAlbum);
        thisUser.addAlbum(newAlbum);

        // Update ListView and select the added album
        updateListView(albumList, insertIndex);
    }
}

private int findInsertIndex(ObservableList<Album> albumList, Album newAlbum) {
    for (int i = 0; i < albumList.size(); i++) {
        if (albumList.get(i).compareTo(newAlbum) > 0) {
            return i;
        }
    }
    return albumList.size(); // If not found, insert at the end
}

private void updateListView(ObservableList<Album> albumList, int index) {
    listView.setItems(albumList);
    listView.getSelectionModel().select(index);
    updateAlbumInfo(albumList);
}
	

	public void delete(ObservableList<Album> albumList, User thisUser){
		
		int currIndex = listView.getSelectionModel().getSelectedIndex();
		boolean delete = true;
		thisUser.removeAlbum(listView.getSelectionModel().getSelectedItem().getTitle());
		albumList.remove(currIndex);
		listView.setItems(albumList);
		int deleteAccessed = 0;
		if(!albumList.isEmpty()) {
			if(albumList.size() <= currIndex) {
				listView.getSelectionModel().select(currIndex-1);
				updateAlbumInfo(albumList);
				deleteAccessed++;
			}
			else {
				listView.getSelectionModel().select(currIndex);
				updateAlbumInfo(albumList);
				deleteAccessed++;
			}
		}
		else 
			albumTxt.clear();
		
		return;
	}
	

	public void edit(ObservableList<Album> albumList, Stage primaryStage, User thisUser) {
		
		String title;
		title = albumName.getText();
		Album currAlbum = albumList.get((listView.getSelectionModel().getSelectedIndex()));
		boolean edit = true;
		Album newAlbum = new Album(title);
		newAlbum.setList(currAlbum.getPictureList());
		delete(albumList, thisUser);
		boolean deleteAccess = true;
		if(!isAlbumInList(newAlbum, primaryStage, albumList))
			add(newAlbum,primaryStage, albumList, thisUser);
		else
			add(currAlbum,primaryStage, albumList, thisUser);
		
		return;
	}
	

	public boolean isAlbumInList(Album searchAlbum, Stage primaryStage, ObservableList<Album> albumList) {
        return albumList.stream()
                        .anyMatch(album -> isTitleMatch(album, searchAlbum, primaryStage));
    }

    private boolean isTitleMatch(Album album, Album searchAlbum, Stage primaryStage) {
        if (album.getTitle().equalsIgnoreCase(searchAlbum.getTitle())) {
            showWarning(primaryStage, "Error", "Error");
            return true;
        }
        return false;
    }

    private void showWarning(Stage owner, String title, String header) {
        Alert warning = new Alert(AlertType.WARNING);
        warning.initOwner(owner);
        warning.setTitle(title);
        warning.setHeaderText(header);
        warning.showAndWait();
    }

    public void updateAlbumInfo(ObservableList<Album> albumList) {
        if (!albumList.isEmpty()) {
            Album currentAlbum = getCurrentAlbum(albumList);
            updateAlbumNameField(currentAlbum);
            updateEditButtonBinding();
        }
    }

    private Album getCurrentAlbum(ObservableList<Album> albumList) {
        int currentIndex = listView.getSelectionModel().getSelectedIndex();
        return albumList.get(currentIndex);
    }

    private void updateAlbumNameField(Album album) {
        albumName.setText(album.getTitle());
    }

    private void updateEditButtonBinding() {
        BooleanBinding hasChanged = Bindings.equal(albumTxt.textProperty(), 
                                                   listView.getSelectionModel().getSelectedItem().getTitle());
        editBtn.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull()
                                       .or(hasChanged)
                                       .or(Bindings.isEmpty(albumTxt.textProperty())));
    }

    private void saveData(ArrayList<User> userList) {
        try (FileOutputStream fos = new FileOutputStream("data/dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userList);
        } catch (IOException e) {
            e.printStackTrace(); // Consider a more robust error handling approach
        }
    }
}
