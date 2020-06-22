package life.genny.notes.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QNoteMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Note[] items;
	private Long total;
	/**
	 * @param items
	 * @param total
	 */
	public QNoteMessage(List<Note> items, Long total) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<Note>();
		}

		this.items = items.toArray(new Note[0]);
		this.total = total;
	}
	/**
	 * @return the items
	 */
	public Note[] getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(Note[] items) {
		this.items = items;
	}
	/**
	 * @return the total
	 */
	public Long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(Long total) {
		this.total = total;
	}
	
	
}
