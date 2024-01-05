package model;

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {

    private static final long serialVersionUID = 9101689674045401414L;
    public String username;
    public ArrayList<Album> albumList = new ArrayList<>();
    public ArrayList<TagType> tagTypes = new ArrayList<>();

    public User(String username) {
        this.username = username;
    }
	String password = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String x) {
        this.username = x;
    }

	public void setPassword(String x){
		this.password = x;
	}

    public ArrayList<Album> getAlbumList() {
        return new ArrayList<>(this.albumList);
    }

    public ArrayList<TagType> getTagTypes() {
        return new ArrayList<>(this.tagTypes);
    }

    public void addTagType(TagType tagName) {
        if (!tagTypes.contains(tagName)) {
            tagTypes.add(tagName);
            tagTypes.sort(Comparator.comparing(TagType::getTagName));
        }
    }

    public void addAlbum(Album newAlbum) {
        albumList.add(newAlbum);
        albumList.sort(Comparator.comparing(Album::getTitle));
    }

    public void removeAlbum(String name) {
        albumList.removeIf(album -> name.equalsIgnoreCase(album.getTitle()));
    }

    public Album getAlbum(String title) {
        return albumList.stream()
                .filter(album -> album.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object other) {
        Object compare = null;
        if (other == compare || !(other instanceof User))
            return false;

        User curr = (User) other;
        return curr.getUsername().equalsIgnoreCase(username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public int compareTo(User currUser) {
        return this.getUsername().compareToIgnoreCase(currUser.getUsername());
    }

    @Override
    public String toString() {
        return this.getUsername();
    }
}