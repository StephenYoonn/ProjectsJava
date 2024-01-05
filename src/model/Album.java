package model;

import java.io.Serializable;
import java.text.*;
import java.util.*;


public class Album implements Serializable {
	
	private static final long serialVersionUID = 1784458408401180814L;

	public String title;

	public String header;
	public ArrayList<Picture> pictureList = new ArrayList<Picture>();
	
	
	public Album(String title) {
		this.title = title;
	}
	
	String name = "";
	public String getTitle() {
		return title;
	}

	public void setTitle(String x) {
		this.title = x;
	}

	public void setHeader(String x){
		this.header = x;
	}
	
	ArrayList<Picture> temp = new ArrayList<Picture>();
	public ArrayList<Picture> getPictureList(){
		return pictureList;
	}
	
	
	public void setList(ArrayList<Picture> newPictureList) {
		this.pictureList = newPictureList;
	}
	
	int picNum = 0; //count number of pictures added
	public void addPicture(Picture newPicture) {
        if (pictureList.stream().noneMatch(pic -> pic.getPicture().equals(newPicture.getPicture()))) {
            pictureList.add(newPicture);
            pictureList.sort(Comparator.comparing(Picture::getPictureName));
			picNum++;
        }
    }

    public void removePicture(Picture thisPicture) {
        pictureList.removeIf(pic -> thisPicture.getPicture().equals(pic.getPicture()));
		picNum--;
    }

    public ImageSerial getPicture(Picture name) {
        return pictureList.stream()
                .filter(pic -> pic.getPicture().equals(name.getPicture()))
                .map(Picture::getPicture)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Album album = (Album) other;
        return getTitle().equalsIgnoreCase(album.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle().toLowerCase());
    }

    public int compareTo(Album currAlbum) {
        return getTitle().toLowerCase().compareTo(currAlbum.getTitle().toLowerCase());
    }
	

	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getTitle())
		   .append("\n").append(this.pictureList.size()).append(" Photos")
		   .append("\nDate Range:\n");
	
		if (!this.pictureList.isEmpty()) {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String dates = "MM/DD/YY";
			DateFormat dateLessSeconds = new SimpleDateFormat(dates);
			Date maxDate = null;
			Date minDate = null;
	
			for (Picture picture : this.pictureList) {
				Date pictureDate = picture.getDate().getTime();
	
				if (maxDate == null || pictureDate.compareTo(maxDate) > 0) {
					maxDate = pictureDate;
				}
	
				if (minDate == null || pictureDate.compareTo(minDate) < 0) {
					minDate = pictureDate;
				}
			}
	
			ret.append(df.format(minDate)).append(" - ").append(df.format(maxDate));
		} else {
			ret.append("_");
		}
	
		return ret.toString();
	}
}