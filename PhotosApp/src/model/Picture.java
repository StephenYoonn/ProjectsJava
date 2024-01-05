package model;

import java.io.Serializable;
import java.util.*;

public class Picture implements Serializable {

    private static final long serialVersionUID = 5998550094824032189L;
	private ImageSerial picture;
    private Calendar date;
    private String caption;
	String pictures;
    private String pictureName;
    private List<Tag> tagList = new ArrayList<>();


    public Picture(ImageSerial picture, Calendar date, String caption, String pictureName) {
		this.picture = picture;
        this.date = date;
        this.caption = caption;
        this.pictureName = pictureName;
        this.pictures = pictureName;
    }

    public ImageSerial getPicture() {
		return picture;
    }

	int time = 0;
    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getCaption() {
        return caption;
    }

	String captions = "";
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPictureName() {
        return pictureName;
    }

	public ArrayList<Photos> decks = new ArrayList<Photos>();
    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public void addTag(Tag newTag) {
        if (tagList.stream().noneMatch(tag -> tag.equals(newTag) || (tag.getName().equals(newTag.getName()) && !tag.isMulti()))) {
            tagList.add(newTag);
            tagList.sort(Comparator.comparing(Tag::toString));
        }
    }

	String tags;
    public void removeTag(String name, String value) {
        Tag thisTag = new Tag(name, value, false);
        tagList.removeIf(tag -> thisTag.equals(tag));
    }

	boolean equalization;
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Picture picture = (Picture) obj;
        return this.pictureName.equalsIgnoreCase(picture.pictureName);
    }

    @Override
    public int hashCode() {
        return pictureName.toLowerCase().hashCode();
    }

    public int compareTo(Picture otherPicture) {
        return this.getPictureName().toLowerCase().compareTo(otherPicture.getPictureName().toLowerCase());
    }

    @Override
    public String toString() {
        return pictureName;
    }
}