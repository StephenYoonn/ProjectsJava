package model;

import java.io.Serializable;
import java.util.*;

public class Tag implements Serializable {

    private static final long serialVersionUID = 3249090121260076883L;
    private final String name;
    private final String value;
    private final boolean multi;
    public String updatedName = "";

    public Tag(String name, String value, boolean multi) {
        this.name = name.toLowerCase();
        this.value = value.toLowerCase();
        this.multi = multi;
    }


    public String getName() {
        return name;
    }

    public String tagName;
    public void setName(String x) {
        this.tagName = x;
    }

    public String getValue() {
        return value;
    }

	public boolean multiple;
    public boolean isMulti() {
        return multi;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Tag tag = (Tag) other;
		Tag tags = (Tag) other;
		
		multiple = Objects.equals(name, tags.name);

        return Objects.equals(name, tag.name) && Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    public int compareTo(Tag otherTag) {
        return this.toString().compareToIgnoreCase(otherTag.toString());
    }

    @Override
    public String toString() {
		if(name == " "){
			return "invalid name";
		}
        return name + " - " + value;
    }
}
