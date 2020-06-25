package life.genny.notes.endpoints;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.identity.SecurityIdentity;
import life.genny.notes.models.DataTable;
import life.genny.notes.models.Note;
import life.genny.notes.models.QNoteMessage;
import life.genny.notes.models.Tag;
import life.genny.qwanda.entity.BaseEntity;

@Path("/v7/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteResource {

	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@ConfigProperty(name = "default.realm", defaultValue = "genny")
	String defaultRealm;

	@ConfigProperty(name = "quarkus.datasource.password")
	String mysqlPassword;
	
	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	EntityManager em;

	/**
	 * Injection point for the ID Token issued by the OpenID Connect Provider
	 */
	@Inject
	@IdToken
	JsonWebToken idToken;

	/**
	 * Injection point for the Access Token issued by the OpenID Connect Provider
	 */
	@Inject
	JsonWebToken accessToken;

	/**
	 * Injection point for the Refresh Token issued by the OpenID Connect Provider
	 */
	@Inject
	RefreshToken refreshToken;

	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	// @RolesAllowed({"user"})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNotesByTags(@QueryParam("tags") String tags,
			@QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
			@QueryParam("pageSize") @DefaultValue("20") Integer pageSize) {
		String realm = securityIdentity.getAttribute("aud"); // realm
		if (realm == null) {
			realm = defaultRealm;
		}

		List<Tag> tagList = null;
		if (tags != null) {
			List<String> tagStringList = Arrays.asList(StringUtils.splitPreserveAllTokens(tags, ","));
			tagList = tagStringList.stream().collect(Collectors.mapping(p -> new Tag(p), Collectors.toList()));
		} else {
			tagList = new ArrayList<Tag>();
		}
		QNoteMessage notes = Note.findByTags(realm, tagList, Page.of(pageIndex, pageSize));

		
		return Response.status(Status.OK).entity(notes).build();
	}

	@Transactional
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response newNote(@Context UriInfo uriInfo, @Valid Note note) {
		note.id = null;

		String realm = securityIdentity.getAttribute("aud"); // realm
		if (realm == null) {
			realm = defaultRealm;
		}
		note.realm = realm;
		// Fetch the base entities
		BaseEntity sourceBE = null;
		BaseEntity targetBE = null;

		try {
			sourceBE = (BaseEntity) em
					.createQuery("SELECT be FROM BaseEntity be where be.realm=:realmStr and be.code=:code")
					.setParameter("realmStr", realm).setParameter("code", note.sourceCode).getSingleResult();
			if (sourceBE == null) {
				throw new WebApplicationException("BaseEntity with code " + note.sourceCode + " does not exist.",
						Status.NOT_FOUND);
			}
		} catch (Exception e) {
			throw new WebApplicationException("BaseEntity with code " + note.sourceCode + " does not exist.",
					Status.NOT_FOUND);
		}

		try {
			targetBE = (BaseEntity) em
					.createQuery("SELECT be FROM BaseEntity be where be.realm=:realmStr and be.code=:code")
					.setParameter("realmStr", realm).setParameter("code", note.targetCode).getSingleResult();

			if (targetBE == null) {
				throw new WebApplicationException("BaseEntity with code " + note.targetCode + " does not exist.",
						Status.NOT_FOUND);
			}
		} catch (Exception e) {
			throw new WebApplicationException("BaseEntity with code " + note.targetCode + " does not exist.",
					Status.NOT_FOUND);
		}

		note.setSource(sourceBE);
		note.setTarget(targetBE);
		note.persist();

		URI uri = uriInfo.getAbsolutePathBuilder().path(NoteResource.class, "findById").build(note.id);
		return Response.created(uri).build();
	}

	@Path("/id/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") final String id) {
		Note note = Note.findById(id);
		if (note == null) {
			throw new WebApplicationException("Note with id of " + id + " does not exist.", Status.NOT_FOUND);
		}

		return Response.status(Status.OK).entity(note).build();
	}

	@Path("/{targetCode}")
	@GET
	// @RolesAllowed({"Everyone"})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNotesByTargetCodeAndTags(@PathParam("targetCode") final String targetCode,
			@QueryParam("tags") @DefaultValue("") String tags,
			@QueryParam("pageIndex") @DefaultValue("0") Integer pageIndex,
			@QueryParam("pageSize") @DefaultValue("20") Integer pageSize) {
		Object userName = this.idToken.getClaim("preferred_username");

		String realm = securityIdentity.getAttribute("aud"); // realm
		if (realm == null) {
			realm = defaultRealm;
		}

		List<String> tagStringList = Arrays.asList(StringUtils.splitPreserveAllTokens(tags, ","));
		List<Tag> tagList = tagStringList.stream().collect(Collectors.mapping(p -> new Tag(p), Collectors.toList()));
		QNoteMessage notes  = Note.findByTargetAndTags(realm, tagList, targetCode, Page.of(pageIndex, pageSize));

		return Response.status(Status.OK).entity(notes).build();
	}

	@Path("{id}")
	@PUT
	@Transactional
	public Response updateNote(@PathParam("id") final String id, @Valid Note note) {
		Note existed = Note.findById(id);
		if (note == null) {
			throw new WebApplicationException("Note with id of " + id + " does not exist.", Status.NOT_FOUND);
		}

		existed.content = note.content;
		existed.updated = LocalDateTime.now();
		existed.persist();

		return Response.status(Status.OK).entity(existed).build();
	}

	@Path("{id}")
	@DELETE
	@Transactional
	public Response deleteNote(@PathParam("id") final Long id) {
		Note n = Note.findById(id);
		if (n == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		
		Note.deleteById(id);
		return Response.status(Status.OK).build();
	}

	@GET
	@Path("/datatable")
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable<Note> datatable(@QueryParam(value = "draw") int draw, @QueryParam(value = "start") int start,
			@QueryParam(value = "length") int length, @QueryParam(value = "search[value]") String searchVal

	) {
		Object userName = this.idToken.getClaim("preferred_username");

		log.info("User name is " + idToken.getName());
		log.info("Ideentity is " + this.securityIdentity.getAttribute("preferred_username"));

		searchVal = "";
		life.genny.notes.models.DataTable<Note> result = new DataTable<>();
		result.setDraw(draw);

		PanacheQuery<Note> filteredDevice;

		if (searchVal != null && !searchVal.isEmpty()) {
			filteredDevice = Note.<Note>find("content like :search", Parameters.with("search", "%" + searchVal + "%"));
		} else {
			filteredDevice = Note.findAll();
		}

		int page_number = 0;
		if (length > 0) {
			page_number = start / length;
		}
		filteredDevice.page(page_number, length);

		log.info("/datatable: search=[" + searchVal + "],start=" + start + ",length=" + length + ",result#="
				+ filteredDevice.count());

		result.setRecordsFiltered(filteredDevice.count());
		result.setData(filteredDevice.list());
		result.setRecordsTotal(Note.count());

		return result;

	}

	@Transactional
	void onStart(@Observes StartupEvent ev) {
		log.info("Note Endpoint starting");
		log.info("MySQL Password = "+mysqlPassword);
		// Creating some test
		// Fetch the base entities
		BaseEntity sourceBE = (BaseEntity) em
				.createQuery("SELECT be FROM BaseEntity be where be.realm=:realmStr and be.code=:code")
				.setParameter("realmStr", "internmatch").setParameter("code", "PER_USER1").getSingleResult();

		if (sourceBE != null) {

			if (Note.count() == 0) {

				Set<Tag> tags1 = Stream.of(new Tag("phone", 1), new Tag("intern", 3)).collect(Collectors.toSet());

				Set<Tag> tags2 = Stream.of(new Tag("intern", 1), new Tag("rating", 5)).collect(Collectors.toSet());

				Note test1 = new Note(defaultRealm, sourceBE, sourceBE, tags1, "This is the first note!");
				test1.persist();

				Note test2 = new Note(defaultRealm, sourceBE, sourceBE, tags2, "This is the second note!");
				test2.persist();
			}
		} else {
			log.error("No Baseentitys set up yet in Database");
		}

	}

	@Transactional
	void onShutdown(@Observes ShutdownEvent ev) {
		log.info("Note Endpoint Shutting down");
	}
}