package life.genny.notes.models;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import io.quarkus.panache.common.Parameters;
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
	public static final String DEFAULT_TAG = "default";

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	public LocalDateTime created = LocalDateTime.now(ZoneId.of("UTC"));

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	public LocalDateTime updated;

	@ElementCollection
	public List<Tag> tags = new ArrayList<>();

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

	@NotEmpty
	@Column(name = "target_code")
	public String targetCode;

	@SuppressWarnings("unused")
	public Note() {
	}

	public Note(final String realm, final BaseEntity sourceBE, final BaseEntity targetBE, final List<Tag> tags,
			final String content) {
		this.created = LocalDateTime.now(ZoneId.of("UTC"));
		this.updated = LocalDateTime.now(ZoneId.of("UTC"));
		this.realm = realm;
		this.content = content;
//		if (tags.isEmpty()) {
//			tags.add(new Tag(DEFAULT_TAG));
//		}
		this.tags = tags;
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

	public static Note findById(Long id) {
		return find("id", id).firstResult();
	}

	public static long deleteById(final Long id) {
		return delete("id", id);
	}

	public static List<Note> findByTags(final String realm, final List<Tag> tags, Page page) {
		List<String> tagStringList = tags.stream().collect(Collectors.mapping(p -> p.getName(), Collectors.toList()));

		// PanacheQuery<Note> notes = Note.find("select n from Note n JOIN n.tags t
		// where n.realm = ?1 and t.name in (?2) order by n.created", realm,tags);
		PanacheQuery<Note> notes = Note.find(
				"select n from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags) order by n.created",
				Parameters.with("realm", realm).and("tags", tagStringList));

		return notes.page(page).list();
	}

	public static List<Note> findByTargetAndTags(String realm, final List<Tag> tags, final String targetCode,
			Page page) {
		List<String> tagStringList = tags.stream().collect(Collectors.mapping(p -> p.getName(), Collectors.toList()));

		PanacheQuery<Note> notes = null;
//		PanacheQuery<Note> notes = Note.find("select n from Note n JOIN n.tags t where n.realm = ?1 and t.name in (?2) and n.targetCode = ?3  order by n.created", realm,tagStringList,targetCode);
		
//		if (!tagStringList.isEmpty()) {
//			tagStringList.add(DEFAULT_TAG);
//		}
		if (!tagStringList.isEmpty()) {
			notes = Note.find(
					"select n from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags) and n.targetCode = :targetCode  order by n.created",
					Parameters.with("realm", realm).and("targetCode", targetCode).and("tags", tagStringList));
		} else {
			notes = Note.find(
					"select n from Note n  where n.realm = :realm  and n.targetCode = :targetCode  order by n.created",
					Parameters.with("realm", realm).and("targetCode", targetCode));
		}
		return notes.page(page).list();

	}
}
