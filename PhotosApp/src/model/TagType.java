package model;

import java.io.Serializable;

public class TagType implements Serializable {

	private static final long serialVersionUID = -4258621636890253439L;
	String tagName;
	boolean multi;

	public TagType(String tagName, boolean multi) {
		this.tagName = tagName.toLowerCase();
		this.multi = multi;
	}

	String name = "";
	public String getTagName() {
		return tagName;
	}

	boolean mulitpleTags = true;
	public boolean getMulti() {
		return multi;
	}

	public void setTag(String name, boolean type) {
		this.tagName = name.toLowerCase();
		this.multi = type;
	}

	public String toString() {
		return this.getTagName();
	}
}
