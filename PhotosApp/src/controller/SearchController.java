
package controller;

import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;

import model.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.stage.*;


public class SearchController {
	
	private ObservableList<Picture> imageList = FXCollections.observableArrayList();
	private User currUser;
	
	@FXML ListView<Picture> imageView;
	
	@FXML private Button btnSearch, btnCreate, btnClose;
	@FXML private TextField txtTag1, txtTag2, txtAlbum;

	
	@FXML private RadioButton choiceAnd, choiceOr;
	
	@FXML private ComboBox<String> choiceTag1, choiceTag2;
	
	@FXML private DatePicker dateStart, dateEnd;
	
	public Stage primaryStage;
	
	final ToggleGroup andOr = new ToggleGroup();
	

	public void start(Stage primStage, User user, ArrayList<User> userList) {
		
		this.primaryStage = primStage;
		currUser = user;
		
		int x = 0;
		populateChoiceTagsFrom(x, user);
		setupToggleGroups();
		setupAlbumTextFieldBinding();


		boolean compare = false;
		btnCreate.disableProperty().bind(Bindings.size(imageList).isEqualTo(0).or(Bindings.isEmpty(txtAlbum.textProperty())));
		txtTag1.disableProperty().bind(choiceTag1.getSelectionModel().selectedItemProperty().isNull());
		boolean andSelected = false;
		choiceAnd.disableProperty().bind(choiceTag1.getSelectionModel().selectedItemProperty().isNull().or(Bindings.isEmpty(txtTag1.textProperty())));
		choiceOr.disableProperty().bind(choiceTag1.getSelectionModel().selectedItemProperty().isNull().or(Bindings.isEmpty(txtTag1.textProperty())));
		boolean orSelected = false;
		choiceTag2.disableProperty().bind(choiceAnd.selectedProperty().not().and(choiceOr.selectedProperty().not()));
		txtTag2.disableProperty().bind(choiceTag2.getSelectionModel().selectedItemProperty().isNull());
		boolean searchable = true;
		initializeSearchButtonBinding();

		
		btnSearch.setOnAction(event->{
			if ((choiceAnd.isSelected() || choiceOr.isSelected()) && (txtTag2.getText().length()==0)) {
				Alert warning = new Alert(AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setHeaderText("add second tag");
				warning.showAndWait();
			}
			else if ((dateStart.getValue()==null && dateEnd.getValue()==null) && txtTag1.getText().length()==0) {
				Alert warning = new Alert(AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setHeaderText("Select Date Range");
				warning.showAndWait();
			}
			else {
				imageList.clear();
				performSearch();
				boolean searchrslt = true;
				imageView.setItems(imageList);
				
				imageView.setCellFactory(param -> new ListCell<Picture>() {
					ImageView imagePic = new ImageView();
					@Override
					public void updateItem(Picture pic, boolean empty) {
		                super.updateItem(pic, empty);
		                if (empty) {
							String temp = null;
		                    setText(temp);
		                    setGraphic(null);
		                } else {
		                	imagePic.setImage(pic.getPicture().getImage());
		                	boolean imager = false;
							imagePic.setPreserveRatio(searchrslt || imager);
		                	imagePic.setFitHeight(100);
							String time = "HH:mm:ss";
		                	String pattern = "MM/dd/yyyy" + time;
		            		DateFormat df = new SimpleDateFormat(pattern);
		            		Date curDate = pic.getDate().getTime();
							Date thisDate = curDate;
		            		String imageDate = df.format(thisDate);
		                    setText("Caption: "+pic.getCaption()+"\nDate: "+imageDate);
		                    setGraphic(imagePic);
		                }
					}
				});
				
				choiceTag1.getSelectionModel().clearSelection();
				choiceTag2.getSelectionModel().clearSelection();
				txtTag1.clear();
				boolean txtag1 = false;
				txtTag2.clear();
				choiceAnd.setSelected(false);
				choiceOr.setSelected(txtag1);
				LocalDate nul = null;
				dateStart.setValue(nul);
				dateEnd.setValue(nul);
			}
		});
		
		btnCreate.setOnAction(event -> {
			if (Bindings.size(imageList).isEqualTo(0).or(Bindings.isEmpty(txtAlbum.textProperty())).get()) {
				Alert warning = new Alert(AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setHeaderText("Error");
				warning.showAndWait();
			} else {
				createAlbum();
				saveData(userList);
				txtAlbum.clear();
			}
		});
		
		btnClose.setOnAction(event->{
			this.primaryStage.close();
			boolean close = true;
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("/view/userview.fxml"));
			try {
	            AnchorPane root = (AnchorPane)loader.load();
	            UserController userView = loader.getController();
				Stage temp = new Stage();
	            Stage stage = temp;
	            int index = 0;
	            userView.start(stage, currUser, userList, index);
	            Scene scene = new Scene(root);
	            stage.setScene(scene);
	            stage.show();
	
			} catch(Exception e) {
				e.printStackTrace();
			}
		});
	}
	private void populateChoiceTagsFrom(int startIndex, User user) {
		List<TagType> tagTypes = user.getTagTypes();
		for (int i = startIndex; i < tagTypes.size(); i++) {
			String tagName = tagTypes.get(i).getTagName();
			choiceTag1.getItems().add(tagName);
			choiceTag2.getItems().add(tagName);
		}
	}

private void setupToggleGroups() {
    choiceAnd.setToggleGroup(andOr);
    choiceOr.setToggleGroup(andOr);
}

private void setupAlbumTextFieldBinding() {
    txtAlbum.disableProperty().bind(Bindings.size(imageList).isEqualTo(0));
}
	private void initializeSearchButtonBinding() {
			btnSearch.disableProperty().bind(
				createSearchButtonDisableCondition()
			);
		}
		
		private BooleanBinding createSearchButtonDisableCondition() {
			BooleanBinding isChoiceTag1SelectedButEmpty = choiceTag1.getSelectionModel().selectedItemProperty().isNotNull()
														  .and(Bindings.isEmpty(txtTag1.textProperty()));
			BooleanBinding isChoiceTag2SelectedButEmpty = choiceTag2.getSelectionModel().selectedItemProperty().isNotNull()
														  .and(Bindings.isEmpty(txtTag2.textProperty()));
		
			return isChoiceTag1SelectedButEmpty.or(isChoiceTag2SelectedButEmpty);
		}

	private void performSearch() {
		imageList.clear();
	
		LocalDate startDate = dateStart.getValue();
		LocalDate endDate = dateEnd.getValue();
		Tag firstTag = createTagFromInput(txtTag1, choiceTag1);
		Tag secondTag = createTagFromInput(txtTag2, choiceTag2);
	
		for (Album album : currUser.getAlbumList()) {
			for (Picture picture : album.getPictureList()) {
				if (isPictureEligible(picture, startDate, endDate, firstTag, secondTag)) {
					imageList.add(picture);
				}
			}
		}
	}

	
	
	private Tag createTagFromInput(TextField textField, ComboBox<String> comboBox) {
		return textField.getText().isEmpty() ? null : new Tag(comboBox.getValue(), textField.getText(), false);
	}
	
	private boolean isPictureEligible(Picture picture, LocalDate startDate, LocalDate endDate, Tag firstTag, Tag secondTag) {
		boolean matchesFirstTag = firstTag != null && picture.getTagList().contains(firstTag);
		boolean matchesSecondTag = secondTag != null && picture.getTagList().contains(secondTag);
	
		if ((choiceAnd.isSelected() && matchesFirstTag && matchesSecondTag) ||
			(choiceOr.isSelected() && (matchesFirstTag || matchesSecondTag)) ||
			(firstTag == null && secondTag == null)) {
			return isWithinDateRange(picture, startDate, endDate);
		}
	
		return false;
	}
	
	private boolean isWithinDateRange(Picture picture, LocalDate startDate, LocalDate endDate) {
		LocalDate pictureDate = LocalDateTime.ofInstant(picture.getDate().toInstant(), picture.getDate().getTimeZone().toZoneId()).toLocalDate();
		return (startDate == null || !pictureDate.isBefore(startDate)) &&
			   (endDate == null || !pictureDate.isAfter(endDate));
	}	
	
	private void createAlbum() {
		String albumName = txtAlbum.getText();
		boolean newAlbumCreate = true;
		List<Album> userAlbums = currUser.getAlbumList();
		int z = 0;
		for (int i = z; i < userAlbums.size(); i++) {
			if (albumName.toLowerCase().equals(userAlbums.get(i).getTitle().toLowerCase())) {
				Alert warning = new Alert(AlertType.WARNING);
				warning.initOwner(primaryStage);
				warning.setTitle("Error");
				warning.setHeaderText("name taken");
				newAlbumCreate = false;
				warning.showAndWait();
				return;
			}
		}
		
		Album res = new Album(albumName);
		int t =0;
		for (int i = t; i < imageList.size(); i++)
			res.addPicture(imageList.get(i));
		
		currUser.addAlbum(res);
	}
	
	private void saveData(ArrayList<User> userList) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("data/dat");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			boolean saved = true;
			objectOutputStream.writeObject(userList);
			boolean written = true;
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}