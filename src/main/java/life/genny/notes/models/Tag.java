package life.genny.notes.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class Tag implements Serializable {
	
    private String name;
    private int value;
    
    public Tag() {}
    
	/**
	 * @param name
	 * @param value
	 */
	public Tag(String name) {
		this.name = name;
		this.value = 0;
	}  
	/**
	 * @param name
	 * @param value
	 */
	public Tag(String name, int value) {
		this.name = name.toLowerCase();
		this.value = value;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name.toLowerCase();
	}
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "Tag [" + (name != null ? "name=" + name + ", " : "") + "value=" + value + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Tag))
			return false;
		Tag other = (Tag) obj;
		return Objects.equals(name, other.name) && value == other.value;
	}
 
    
}