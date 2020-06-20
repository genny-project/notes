package life.genny.notes.models;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

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
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import life.genny.qwanda.attribute.Attribute;
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
	public static final String DEFAULT_ATTRIBUTE_CODE = "PRI_TEXT";

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	public LocalDateTime created = LocalDateTime.now(ZoneId.of("UTC"));

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	public LocalDateTime updated;

	@NotEmpty
	@JsonbTransient
	public String realm;

	@FullTextField(analyzer = "english")
	@Column(name = "content")
	@NotEmpty
	public String content;

	@ManyToOne
	@JoinColumn(name = "source_id")
	@JsonbTransient
	@NotNull
	private BaseEntity source;

	@ManyToOne
	@JoinColumn(name = "target_id")
	@JsonbTransient
	@NotNull
	private BaseEntity target;

	@NotEmpty
	@Column(name = "source_code")
	public String sourceCode;

	@Column(name = "attribute_code")
	public String attributeCode = DEFAULT_ATTRIBUTE_CODE;

	@ManyToOne
	@JoinColumn(name = "attribute_id")
	@JsonbTransient
	@NotNull
	private Attribute attribute;

	@NotEmpty
	@Column(name = "target_code")
	public String targetCode;

	@SuppressWarnings("unused")
	public Note() {
	}

	public Note(final String realm, final BaseEntity sourceBE, final BaseEntity targetBE, final Attribute attribute,
			final String content) {
		this.created = LocalDateTime.now(ZoneId.of("UTC"));
		this.updated = LocalDateTime.now(ZoneId.of("UTC"));
		this.realm = realm;
		this.content = content;
		this.setAttribute(attribute);
		this.setSource(sourceBE);
		this.setTarget(targetBE);
	}

	/**
	 * @return the source
	 */
	public BaseEntity getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(BaseEntity source) {
		this.source = source;
		this.sourceCode = source.getCode();
	}

	/**
	 * @return the target
	 */
	public BaseEntity getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(BaseEntity target) {
		this.target = target;
		this.targetCode = target.getCode();
	}

	/**
	 * @return the attribute
	 */
	public Attribute getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
		this.attributeCode = attribute.getCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributeCode, created, realm, sourceCode, targetCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Note))
			return false;
		Note other = (Note) obj;
		return Objects.equals(attributeCode, other.attributeCode) && Objects.equals(created, other.created)
				&& Objects.equals(realm, other.realm) && Objects.equals(sourceCode, other.sourceCode)
				&& Objects.equals(targetCode, other.targetCode);
	}

	@Override
	public String toString() {
		return "Note [" + (created != null ? "created=" + created + ", " : "")
				+ (sourceCode != null ? "sourceCode=" + sourceCode + ", " : "")
				+ (targetCode != null ? "targetCode=" + targetCode + ", " : "")
				+ (attributeCode != null ? "attributeCode=" + attributeCode + ", " : "")
				+ (content != null ? "content=" + content : "") + "]";
	}

	public static Note findById(Long id) {
		return find("id", id).firstResult();
	}

	public static long deleteById(final Long id) {
		return delete("id", id);
	}

	public static List<Note> findByTargetCode(final String code, Page page) {
		List<Note> notes = Note.find("targetCode", code).page(page).list();
		return notes;
	}
	
	public static List<Note> findByTargetAndAttributeCode(String realm,final String targetCode, String attributeCode, Page page) {
		PanacheQuery<Note> notes = Note.find("select n from Note n where n.targetCode = ?1 and n.realm = ?2 and n.attributeCode = ?3 order by n.created", targetCode,realm,attributeCode);
		return notes.page(page).list();
	}


}
