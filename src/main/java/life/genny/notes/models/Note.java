package life.genny.notes.models;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import life.genny.notes.utils.LocalDateTimeAdapter;
import life.genny.qwanda.entity.BaseEntity;

/*
* @author      Adam Crow
* @version     %I%, %G%
* @since       1.0
*/
@Entity
//@Indexed
@Table(name = "note")
@RegisterForReflection
public class Note extends PanacheEntity {

	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	public static final String DEFAULT_TAG = "default";

	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime created = LocalDateTime.now(ZoneId.of("UTC"));
	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime updated;
	// public Date updated = new Date();
	@ElementCollection
	@Column(name = "note_tag")
    @CollectionTable(name = "tag")
	@JoinColumn(name = "note_id")
	@OnDelete(action= OnDeleteAction.CASCADE)
	public Set<Tag> tags = new HashSet<>();

	@NotEmpty
	@JsonbTransient
	public String realm;

	// @FullTextField(analyzer = "english")
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

	public Note(final String realm, final BaseEntity sourceBE, final BaseEntity targetBE, final Set<Tag> tags,
			final String content) {
		// this.created = LocalDateTime.now(ZoneId.of("UTC"));
		// this.updated = LocalDateTime.now(ZoneId.of("UTC"));
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

	public static QNoteMessage findByTags(final String realm, final List<Tag> tags, Page page) {
		List<String> tagStringList = tags.stream().collect(Collectors.mapping(p -> p.getName(), Collectors.toList()));

		PanacheQuery<Note> notes = null;
		Long total = 0L;
		if (!tagStringList.isEmpty()) {
			notes = Note.find(
					"select n from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags)  order by n.created",
					Parameters.with("realm", realm).and("tags", tagStringList));
			
			if (notes.count()>0 ) {
				total = Note.count("from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags)",
						Parameters.with("realm", realm).and("tags", tagStringList));
			}
		} else {
			notes = Note.find("select n from Note n  where n.realm = :realm  order by n.created",
					Parameters.with("realm", realm));
			if (notes.count()>0 ) {
				total = Note.count("realm",realm);
			}

		}		
		
		QNoteMessage noteMsg = new QNoteMessage( notes.page(page).list(),total);
		return noteMsg;
	}

	public static QNoteMessage findByTargetAndTags(String realm, final List<Tag> tags, final String targetCode,
			Page page) {
		List<String> tagStringList = tags.stream().collect(Collectors.mapping(p -> p.getName(), Collectors.toList()));

		PanacheQuery<Note> notes = null;
		Long total = 0L;


		if (!tagStringList.isEmpty()) {
			notes = Note.find(
					"select n from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags) and n.targetCode = :targetCode  order by n.created",
					Parameters.with("realm", realm).and("targetCode", targetCode).and("tags", tagStringList));
			if (notes.count()>0 ) {
				total = Note.count("from Note n JOIN n.tags t where n.realm = :realm and t.name in (:tags) and n.targetCode = :targetCode ",
						Parameters.with("realm", realm).and("targetCode", targetCode).and("tags", tagStringList));
			}

		} else {
			notes = Note.find(
					"select n from Note n  where n.realm = :realm  and n.targetCode = :targetCode  order by n.created",
					Parameters.with("realm", realm).and("targetCode", targetCode));
			if (notes.count()>0 ) {
				total = Note.count("realm = :realm  and targetCode = :targetCode",
						Parameters.with("realm", realm).and("targetCode", targetCode));
			}

		}
		
		QNoteMessage noteMsg = new QNoteMessage( notes.page(page).list(),total);
		return noteMsg;


	}
}
