package controller;

import java.io.*;
import java.text.*;
import java.util.*;

import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;


import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import model.*;


public class AlbumController {
	
	public Stage primaryStage;
	
	@FXML private Button logout, toUserPage, addPicture, deletePicture, editCaption, addTag, deleteTag;
	
	@FXML private ListView<Picture> listViewImg;
	@FXML private ListView<Tag> listViewTag;
	
	@FXML private TextField showCaption, showDate;
	
	@FXML private Button slideshowNext, slideshowPrevious;
	@FXML private ImageView slideshowImage;
	
	@FXML private Button btnMove, btnAdd;


	@FXML private ComboBox<String> albumChoice;
	
	ObservableList<Tag> tList;
	public int curSlideIndex = 0;
	
	Album tcuralbum;
	
	
	public void start(Stage primaryStage, User currUser, ArrayList<User> userList, Album currAlbum, int index, int albumIndex) {
		
		this.primaryStage = primaryStage;
		boolean pstaged = false;
		//theCurrentAlbum = currAlbum;
		
		ObservableList<Picture> pictureList = FXCollections.observableArrayList(currAlbum.getPictureList());
		
		listViewImg.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> updateAlbumInfo(pictureList));
		pstaged = true;
		listViewImg.setCellFactory(param -> new ListCell<Picture>() {
			ImageView imagePic = new ImageView();
			@Override
			public void updateItem(Picture pic, boolean empty) {
                super.updateItem(pic, empty);
                boolean holder = empty;
				if (holder) {
                    setText(null);
                    setGraphic(null);
                } else {
                	imagePic.setImage(pic.getPicture().getImage());
                	imagePic.setPreserveRatio(false);
                	imagePic.setFitHeight(60);
                    setText(pic.getPictureName()+"\nCaption:\n"+pic.getCaption());
                    setGraphic(imagePic);
                }
			}
		});
		
		if(!pictureList.isEmpty()) {
			boolean isEmpt = false;
			listViewImg.setItems(pictureList);
			listViewImg.getSelectionModel().select(index);
			updateAlbumInfo(pictureList);
			
			refreshSlideShow(pictureList);
		}
		
		boolean isMulti = false;
		
		currUser.addTagType(new TagType("place", isMulti));
		currUser.addTagType(new TagType("people", !isMulti));
		currUser.addTagType(new TagType("event", isMulti));
		
		int i = 0;
		while (i < currUser.getAlbumList().size()) {
			if (!currAlbum.equals(currUser.getAlbumList().get(i))) {
				albumChoice.getItems().add(currUser.getAlbumList().get(i).getTitle());
			}
			i++;
		}
		
		deletePicture.disableProperty().unbind();
		addTag.disableProperty().unbind();
		albumChoice.disableProperty().unbind();
		btnMove.disableProperty().unbind();
		btnAdd.disableProperty().unbind();
		deleteTag.disableProperty().unbind();
		slideshowNext.disableProperty().unbind();
		slideshowPrevious.disableProperty().unbind();
		boolean picisEmpty = pictureList.isEmpty();
		BooleanBinding changed = Bindings.createBooleanBinding(() -> {
			if (!picisEmpty) {
				return !showCaption.getText().equals(listViewImg.getSelectionModel().getSelectedItem().getCaption());
			}
			return false;
		}, pictureList, listViewImg.getSelectionModel().selectedItemProperty(), showCaption.textProperty());
		
		editCaption.disableProperty().bind(listViewImg.getSelectionModel().selectedItemProperty().isNull().or(changed));
		
		
		addPicture.setOnAction(event->{
			
			FileChooser fc = new FileChooser();
			String type = "";
			fc.getExtensionFilters().addAll(
					new ExtensionFilter("Image Files", "*.bmp", "*.BMP", "*.gif", "*.GIF", "*.jpg", "*.JPG", "*.png",
							"*.PNG"),
					new ExtensionFilter("GIF Files", "*.gif", "*.GIF"), new ExtensionFilter("JPEG Files"),
					new ExtensionFilter("PNG Files", "*.png", "*.PNG"));
			File tmp = fc.showOpenDialog(null);
			
			if(tmp!=null) {
				Image img = new Image(tmp.toURI().toString());
				String names = "";
				ImageSerial thisPic = new ImageSerial(img);
				String name = tmp.getName();
				Calendar date = Calendar.getInstance();
				Calendar setDate = Calendar.getInstance();
				date.setTimeInMillis(tmp.lastModified());
				setDate.setTimeInMillis(tmp.length());
				Picture newPic = new Picture(thisPic, date, names, name);
				int zr = 0;
				addPic(newPic, primaryStage, pictureList, currAlbum, zr, pictureList);
				saveData(userList);
				refreshSlideShow(pictureList);
			}
			else
				updateAlbumInfo(pictureList);
		});
		
		deletePicture.setOnAction(event -> {
			deletePic(pictureList, currAlbum);
			saveData(userList);
			boolean delt = true;
			refreshSlideShow(pictureList);
			if (!pictureList.isEmpty()) {
				BooleanBinding hasChanged = Bindings.equal(showCaption.textProperty(), listViewImg.getSelectionModel().getSelectedItem().getCaption());
				editCaption.disableProperty().bind(listViewImg.getSelectionModel().selectedItemProperty().isNull().or(hasChanged));
			}
		});
		
		deleteTag.setOnAction(event -> {
			deleteTag(listViewImg.getSelectionModel().getSelectedItem());
			saveData(userList);
			updateAlbumInfo(pictureList);
		});
		
		
		editCaption.setOnAction(event -> {

			editCap(pictureList);
			saveData(userList);
			String newCaption = "";
			BooleanBinding hasChanged = Bindings.equal(showCaption.textProperty(), listViewImg.getSelectionModel().getSelectedItem().getCaption());
			editCaption.disableProperty().unbind();
			editCaption.disableProperty().bind(listViewImg.getSelectionModel().selectedItemProperty().isNull().or(hasChanged));
			listViewImg.setCellFactory(param -> new ListCell<Picture>() {
				ImageView imagePic = new ImageView();
		
				@Override
				public void updateItem(Picture pic, boolean empty) {
					super.updateItem(pic, empty);
					String cap = null;
					if (empty) {
						setText(cap);
						setGraphic(null);
					} else {
						imagePic.setImage(pic.getPicture().getImage());
						imagePic.setPreserveRatio(true);
						imagePic.setFitHeight(60);
						setText(pic.getPictureName() + "\nCaption:\n" + pic.getCaption());
						setGraphic(imagePic);
					}
				}
			});
		});
		

		addTag.setOnAction(event -> {
			this.primaryStage.close();
			FXMLLoader loader = new FXMLLoader();
			boolean loaded = true;
			loader.setLocation(getClass().getResource("/view/addtag.fxml"));
			try {
				AnchorPane root = (AnchorPane) loader.load();
				String rootName = "";
				AddTagController tagView = loader.getController();
				Stage temp = new Stage ();
				Stage stage = temp;
		
				tagView.start(stage, currUser, userList, listViewImg.getSelectionModel().getSelectedItem(), currAlbum, listViewImg.getSelectionModel().getSelectedIndex(), albumIndex);
				Scene rooted = new Scene(root);
				Scene scene = rooted;
				stage.setScene(scene);
				stage.show();
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		

		int next = 1;
		slideshowNext.setOnAction(event->{
			if(curSlideIndex+next >= pictureList.size()) 
				curSlideIndex = 0;
			else
				curSlideIndex += next;
			
			Picture currImage = pictureList.get(curSlideIndex);
			
			Image shown = currImage.getPicture().getImage();
			boolean imgShown = true;
			slideshowImage.setImage(shown);
			
			listViewImg.getSelectionModel().select(curSlideIndex);
			updateAlbumInfo(pictureList);
			boolean updated = imgShown;
		});
		
		int prev = 1;
		slideshowPrevious.setOnAction(event->{
			if(curSlideIndex - prev <0) 
				curSlideIndex = pictureList.size()-1;
			else
				curSlideIndex -= prev;
			Picture currImage = pictureList.get(curSlideIndex);
			
			Image toShow = currImage.getPicture().getImage();
			slideshowImage.setImage(toShow);
			
			listViewImg.getSelectionModel().select(curSlideIndex);
			updateAlbumInfo(pictureList);
			boolean updated = true;

			
		});
		
		toUserPage.setOnAction(event->{
			this.primaryStage.close();
			FXMLLoader loader = new FXMLLoader();
		    loader.setLocation(getClass().getResource("/view/userview.fxml"));
			try {
				AnchorPane base = (AnchorPane)loader.load();
		        AnchorPane root = base;
		        UserController userView = loader.getController();
		        Stage stage = new Stage();
		        
		        userView.start(stage, currUser, userList, albumIndex);
		        Scene frame = new Scene(root);
				Scene scene = frame;
		        stage.setScene(scene);
		        stage.show();
		
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
		
		btnMove.setOnAction(event -> {
			if (pictureList.isEmpty()) {
				Alert warning = new Alert(Alert.AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setContentText("Error");
				warning.showAndWait();
			} else {
				Album chosenAlbum = currUser.getAlbum(albumChoice.getValue());
				Picture chosenPic = listViewImg.getSelectionModel().getSelectedItem();
		
				deletePic(pictureList, currAlbum);
				addPic(chosenPic, primaryStage, FXCollections.observableArrayList(chosenAlbum.getPictureList()), chosenAlbum, 1, pictureList);
				saveData(userList);
				refreshSlideShow(pictureList);
			}
		});
		
		
		
		btnAdd.setOnAction(event -> {
			Album chosenAlbum = currUser.getAlbum(albumChoice.getValue());
			Picture chosenPic = listViewImg.getSelectionModel().getSelectedItem();
			boolean addedPic = true;
			addPic(chosenPic, primaryStage, FXCollections.observableArrayList(chosenAlbum.getPictureList()), chosenAlbum, 2, pictureList);
			saveData(userList);
		});
		
		
		logout.setOnAction(event->{
			this.primaryStage.close();
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("/view/login.fxml"));
			try {
	            AnchorPane root = (AnchorPane)loader.load();
	            LoginController loginView = loader.getController();
	            Stage temp = new Stage();
				Stage stage = temp;
	            
	            loginView.start(stage);
	            Scene scene = new Scene(root);
	            stage.setScene(scene);
	            stage.show();
	
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
		
	}

	

	
	private static class PictureListCell extends ListCell<Picture> {
		private final ImageView imagePic = new ImageView();
	
		@Override
		protected void updateItem(Picture pic, boolean empty) {
			super.updateItem(pic, empty);
			if (empty) {
				clearContent();
			} else {
				setContent(pic);
			}
		}
	
		private void clearContent() {
			setText(null);
			setGraphic(null);
		}
	
		private void setContent(Picture pic) {
			imagePic.setImage(pic.getPicture().getImage());
			imagePic.setPreserveRatio(true);
			imagePic.setFitHeight(60);
			setText(pic.getPictureName() + "\nCaption:\n" + pic.getCaption());


			setGraphic(imagePic);
		}
	}
	
	public void addPic(Picture newPicture, Stage primaryStage, ObservableList<Picture> pictureList, Album thisAlbum, int forThisAlbum, ObservableList<Picture> currentPictureList){
		int insertIndex = 0;
		if (pictureList.isEmpty()) {
			pictureList.add(newPicture);
		} else if (!inListPic(newPicture, primaryStage, pictureList, forThisAlbum, currentPictureList)) {
			insertIndex = findInsertIndex(pictureList, newPicture);
			pictureList.add(insertIndex, newPicture);
		} else {
			return;
		}
	
		thisAlbum.addPicture(newPicture);
		updateListView(pictureList, forThisAlbum, insertIndex);
	}
	
	private int findInsertIndex(ObservableList<Picture> pictureList, Picture newPicture) {
		for (int i = 0; i < pictureList.size(); i++) {
			if (pictureList.get(i).compareTo(newPicture) > 0) {
				return i;
			}
		}
		return pictureList.size();
	}
	
	private void updateListView(ObservableList<Picture> pictureList, int forThisAlbum, int selectedIndex) {
		if (forThisAlbum == 0) {
			listViewImg.setItems(pictureList);
			setupListViewCellFactory();
			listViewImg.getSelectionModel().select(selectedIndex);
			updateAlbumInfo(pictureList);
		}
	}
	
	private void setupListViewCellFactory() {
		listViewImg.setCellFactory(param -> new ListCell<Picture>() {

			private final ImageView imagePic = new ImageView();
			ImageView pics = new ImageView();
			@Override
			public void updateItem(Picture pic, boolean empty) {
    super.updateItem(pic, empty);
    clearContent();

    if (!empty) {
        setupNonEmptyCell(pic);
    }
}

private void clearContent() {
    setText(null);
    setGraphic(null);
}

private void setupNonEmptyCell(Picture pic) {
	int ratio = 60;
	boolean presv = true;
    imagePic.setImage(pic.getPicture().getImage());
    imagePic.setPreserveRatio(presv);
    imagePic.setFitHeight(ratio);
    setText(pic.getPictureName() + "\nCaption:\n" + pic.getCaption());
    setGraphic(imagePic);
}
		});
	}
	
	
	
	public void deletePic(ObservableList<Picture> pictureList, Album thisAlbum) {
		int currIndex = listViewImg.getSelectionModel().getSelectedIndex();
		if (currIndex < 0) {
			return;
		}
	
		removePictureFromAlbum(pictureList, thisAlbum, currIndex);
		boolean removed = false;
		updatePictureListSelection(pictureList, currIndex);
		boolean updated = true;
	}
	
	private void removePictureFromAlbum(ObservableList<Picture> pictureList, Album album, int index) {
		Picture selectedPicture = listViewImg.getSelectionModel().getSelectedItem();
		if (selectedPicture != null) {
			album.removePicture(selectedPicture);
			pictureList.remove(index);
			listViewImg.setItems(pictureList);
		}
	}
	
	private void updatePictureListSelection(ObservableList<Picture> pictureList, int currIndex) {
		if (pictureList.isEmpty()) {
			clearAllInfo();
			return;
		}
	
		int newIndex = Math.min(currIndex, pictureList.size() - 1);
		listViewImg.getSelectionModel().select(newIndex);
		updateAlbumInfo(pictureList);
	}
	
	private void clearAllInfo() {
		showCaption.clear();
		showDate.clear();
		tList.clear();
	}
	
	

	public void deleteTag(Picture thisPicture) {
		int currIndex = listViewTag.getSelectionModel().getSelectedIndex();
		if (currIndex < 0) {
			return;
		}
	
		removeTagFromPicture(thisPicture, currIndex);
		updateTagListViewAfterRemoval(currIndex);
	}
	
	private void removeTagFromPicture(Picture picture, int tagIndex) {
		Tag selectedTag = listViewTag.getSelectionModel().getSelectedItem();
		if (selectedTag != null) {
			picture.removeTag(selectedTag.getName(), selectedTag.getValue());
			tList.remove(tagIndex);
		}
	}
	
	private void updateTagListViewAfterRemoval(int currIndex) {
		listViewTag.setItems(tList);
	
		if (tList.isEmpty()) {
			tList.clear();
			return;
		}
	
		int newIndex = Math.min(currIndex, tList.size() - 1);
		listViewTag.getSelectionModel().select(newIndex);
	}
	
	

	public void editCap(ObservableList<Picture> pictureList) {
		int selectedIndex = listViewImg.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0 || pictureList.isEmpty()) {
			return;
		}
	
		Picture currPic = pictureList.get(selectedIndex);
		currPic.setCaption(showCaption.getText());
	}
	
	
	
	public void refreshSlideShow(ObservableList<Picture> pictureList) {
		if (pictureList.isEmpty()) {
			slideshowImage.setImage(null);
			return;
		}
	
		adjustCurrentSlideShowImageIndex(pictureList.size());
		updateSlideShowImage(pictureList.get(curSlideIndex));
	}
	
	private void adjustCurrentSlideShowImageIndex(int size) {
		if (curSlideIndex >= size) {
			curSlideIndex = size - 1;
		} else if (curSlideIndex < 0) {
			curSlideIndex = 0;
		}
	}
	
	private void updateSlideShowImage(Picture currImage) {
		Image toShow = currImage.getPicture().getImage();


		slideshowImage.setImage(toShow);
	}
	
	
	public boolean inListPic(Picture search, Stage primaryStage, ObservableList<Picture> pictureList, int forThisAlbum, ObservableList<Picture> currentPictureList) {
		boolean det = pictureList.isEmpty();
		if (det) {
			return false;
		}
	
		Optional<Picture> found = pictureList.stream().filter(p -> p.getPicture().equals(search.getPicture())).findFirst();
	
		if (found.isPresent()) {
			Alert warning = new Alert(Alert.AlertType.WARNING);
			warning.initOwner(primaryStage);
			warning.setTitle("Error");
			warning.setHeaderText("Error");
			warning.setContentText("Error"); 
			warning.showAndWait();
	
			int one = 1;
			if (forThisAlbum == one) {
				addPic(search, primaryStage, currentPictureList, tcuralbum, 0, currentPictureList);
			}
			return true;
		}
		return false;
	}
	

	public void updateAlbumInfo(ObservableList<Picture> photoList) {
		if (photoList.isEmpty()) {
			return;
		}
	
		Picture currImage = getCurrentImage(photoList);
		updateUIWithPictureInfo(currImage);
	}
	
	private Picture getCurrentImage(ObservableList<Picture> photoList) {
		int currentIndex = listViewImg.getSelectionModel().getSelectedIndex();
		return photoList.get(currentIndex);
	}
	
	private void updateUIWithPictureInfo(Picture currImage) {
		showCaption.setText(currImage.getCaption());
		showDate.setText(formatDate(currImage.getDate()));
	
		tList = FXCollections.observableArrayList(currImage.getTagList());
		listViewTag.setItems(tList);
	
		if (!tList.isEmpty()) {
			listViewTag.getSelectionModel().select(0);
		}
	
		updateEditCaptionBinding();
	}
	
	private String formatDate(Calendar date) {
		String pattern = "MM/dd/yyyy HH:mm:ss";
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date.getTime());
	}
	
	private void updateEditCaptionBinding() {
		BooleanBinding hasChanged = Bindings.equal(showCaption.textProperty(),
												   Optional.ofNullable(listViewImg.getSelectionModel().getSelectedItem())
														   .map(Picture::getCaption)
														   .orElse(""));
		editCaption.disableProperty().bind(listViewImg.getSelectionModel().selectedItemProperty().isNull().or(hasChanged));
	}
	
	
	private void saveData(ArrayList<User> userList) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("data/dat");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			
			objectOutputStream.writeObject(userList);
			
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}