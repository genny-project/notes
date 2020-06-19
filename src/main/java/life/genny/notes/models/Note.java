package life.genny.notes.models;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import life.genny.qwanda.entity.BaseEntity;

/*
* @author      Adam Crow
* @version     %I%, %G%
* @since       1.0
*/
@Entity
@Indexed
@Table(name = "note")
public class Note extends PanacheEntity {

	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")		
	public LocalDateTime created  = LocalDateTime.now(ZoneId.of("UTC"));
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")		
	public LocalDateTime updated;


	@FullTextField(analyzer = "english")
	@Column(name = "content")
	@NotEmpty
	public String content;

	
//    @ManyToOne(targetEntity = life.genny.qwanda.entity.BaseEntity.class)
//    @JoinColumn(name = "source_id")
//    @JsonbTransient    
//  //  @NotNull
//    private BaseEntity source;
//
//    @ManyToOne(targetEntity = life.genny.qwanda.entity.BaseEntity.class)
//    @JoinColumn(name = "target_id")
//    @JsonbTransient   
// //   @NotNull
//    private BaseEntity target;

	@NotEmpty
	@Column(name = "source_code")
    public String sourceCode;
    
	@NotEmpty
	@Column(name = "target_code")
    public String targetCode;

	@SuppressWarnings("unused")
	public Note() {
	}

	public Note(final BaseEntity sourceBE, BaseEntity targetBE, final String content) {
		this.created = LocalDateTime.now(ZoneId.of("UTC"));
		this.updated = LocalDateTime.now(ZoneId.of("UTC"));
		this.content = content;
	//	this.setSource(sourceBE);
	//	this.setTarget(targetBE);
	}


	
	
//	/**
//	 * @return the source
//	 */
//	public BaseEntity getSource() {
//		return source;
//	}
//
//	/**
//	 * @param source the source to set
//	 */
//	public void setSource(BaseEntity source) {
//		this.source = source;
//		this.sourceCode = source.getCode();
//	}
//
//	/**
//	 * @return the target
//	 */
//	public BaseEntity getTarget() {
//		return target;
//	}
//
//	/**
//	 * @param target the target to set
//	 */
//	public void setTarget(BaseEntity target) {
//		this.target = target;
//		this.targetCode = target.getCode();
//	}



	public static Note findById(Long id) {
		return find("id", id).firstResult();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((sourceCode == null) ? 0 : sourceCode.hashCode());
		result = prime * result + ((targetCode == null) ? 0 : targetCode.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (sourceCode == null) {
			if (other.sourceCode != null)
				return false;
		} else if (!sourceCode.equals(other.sourceCode))
			return false;
		if (targetCode == null) {
			if (other.targetCode != null)
				return false;
		} else if (!targetCode.equals(other.targetCode))
			return false;
		if (updated == null) {
			if (other.updated != null)
				return false;
		} else if (!updated.equals(other.updated))
			return false;
		return true;
	}

	public static long deleteById(final Long id) {
		return delete("id", id);
	}



}
