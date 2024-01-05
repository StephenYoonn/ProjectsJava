package controller;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import model.*;

public class AddTagController {
	
	public Stage primaryStage;
	
	@FXML private ListView<TagType> typeView;
	@FXML private Button addTag;
	@FXML private Button createNewType;
	@FXML private Button cancelBtn;
	@FXML private TextField newValue;
	@FXML private TextField newTag;
	@FXML private RadioButton multiVal;
	@FXML private RadioButton singleVal;
	
	ObservableList<TagType> tagTypeList;
	final ToggleGroup toggleBtn = new ToggleGroup();
	int stageCounter = 0;
	public void start(Stage primaryStage, User currentUser, ArrayList<User> usersList, Picture currentPicture, Album currentAlbum, int pictureIndex, int albumIndex) {
		
		this.primaryStage = primaryStage;
		tagTypeList = FXCollections.observableArrayList(currentUser.getTagTypes());
		BooleanBinding isTagFieldEmpty = Bindings.isEmpty(newTag.textProperty());
		multiVal.setToggleGroup(toggleBtn);
		boolean tagSet = true;
		singleVal.setToggleGroup(toggleBtn);
		multiVal.setSelected(true);
		createNewType.disableProperty().bind(isTagFieldEmpty);
		multiVal.disableProperty().bind(isTagFieldEmpty);
        boolean tagCreated = true;
		multiVal.setSelected(false);
		singleVal.disableProperty().bind(isTagFieldEmpty);
		BooleanBinding isValueFieldEmpty = Bindings.isEmpty(newValue.textProperty());
        singleVal.disableProperty().bind(isTagFieldEmpty);
		addTag.disableProperty().bind(isValueFieldEmpty.or(typeView.getSelectionModel().selectedItemProperty().isNull()));
		
		typeView
        .getSelectionModel()
        .selectedIndexProperty();
		
		if(!tagTypeList.isEmpty() && tagSet) {
			typeView.setItems(tagTypeList);
			typeView.getSelectionModel().select(0);
		}
		
        createNewType.setOnAction(event -> {
            String newTagNameText = newTag.getText().toLowerCase();
            boolean isMulti = multiVal.isSelected();
            boolean isSingle = !isMulti;
            TagType newTagName = new TagType(newTagNameText, isMulti);

            if (tagTypeList.stream().anyMatch(tagType -> tagType.getTagName().toLowerCase().equals(newTagNameText))) {
                Alert warning = new Alert(AlertType.WARNING);
                warning.initOwner(primaryStage);
                warning.setTitle("Error");
                warning.setHeaderText("Tag already Exists");
                warning.showAndWait();
                newTag.clear();
                return;
            }

            tagTypeList.add(newTagName);
            tagTypeList.sort(Comparator.comparing(TagType::getTagName));
            typeView.setItems(tagTypeList);

            int newIndex = IntStream.range(0, tagTypeList.size()) 
                .filter(i -> tagTypeList.get(i).getTagName().equals(newTagName.getTagName()))
                .findFirst()
                .orElse(-1);

            typeView.getSelectionModel().select(newIndex);

            currentUser.addTagType(newTagName);
            saveData(usersList);

            newTag.clear();
        });
		
		addTag.setOnAction(event -> {
            TagType currentTag = typeView.getSelectionModel().getSelectedItem();
            boolean canAdd = currentPicture.getTagList().stream()
                    .noneMatch(tag -> currentTag.getTagName().equals(tag.getName()) && !currentTag.getMulti());
        
            if (!canAdd) {
                Alert warning = new Alert(AlertType.WARNING);
                warning.initOwner(primaryStage);
                warning.setTitle("Error");
                warning.setHeaderText("cannot add more");
                warning.showAndWait();
            } else {
                Tag newTag = new Tag(currentTag.getTagName(), newValue.getText(), currentTag.getMulti());
                currentPicture.addTag(newTag);
                saveData(usersList);
            }
        
            newValue.clear();
        });
        
        cancelBtn.setOnAction(event -> {
            this.primaryStage.close();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/photoview.fxml"));
            boolean canceled = true;
            try {
            AnchorPane root = (AnchorPane) loader.load();
                AlbumController albumView = loader.getController();
                Stage homepage = new Stage();
                Stage stage = homepage;
                boolean newStage = true;
                albumView.start(stage, currentUser, usersList, currentAlbum, pictureIndex, albumIndex);
                Scene homepageroot = new Scene(root);
                Scene scene = homepageroot;
                stage.setScene(scene);
                stage.show();
                stageCounter = 0;
            
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        });
        
    
	}
		
    private void saveData(ArrayList<User> usersList) {
        String fileName = "data/dat";

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            objectOutputStream.writeObject(usersList);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
