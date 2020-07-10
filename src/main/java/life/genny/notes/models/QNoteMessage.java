package life.genny.notes.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class QNoteMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Note[] items;
	private Long total;
	private String status;
	private String[] recipientCodeArray = null;
	
	/**
	 * @param items
	 * @param total
	 */
	public QNoteMessage() {}
	
	public QNoteMessage(Note item,NoteStatus status) {
		List<Note> notes = new ArrayList<Note>();
		notes.add(item);
		this.total = 1L;
		this.items = notes.toArray(new Note[0]);
		this.status = status.toString();

	}
	
	public QNoteMessage(List<Note> items, Long total) {
		this(items,total,NoteStatus.READ);
	}
	
	public QNoteMessage(List<Note> items, Long total, NoteStatus status) {
		if ((items == null) || (items.isEmpty())) {
			items = new ArrayList<Note>();
		}

		this.items = items.toArray(new Note[0]);
		this.total = total;
		this.status = status.toString();
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

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setRecipients(Set<String> recipientCodeList)
	{
		this.recipientCodeArray = recipientCodeList.toArray(new String[0]);
	}
}
