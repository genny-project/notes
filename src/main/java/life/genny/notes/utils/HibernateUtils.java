package life.genny.notes.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.ServiceRegistry;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;

@SuppressWarnings("unused")
@ApplicationScoped
public class HibernateUtils {




	public HibernateUtils() {}
	
	@Transactional
	static public List<Object[]> query(EntityManager em,final String sql) {
		
		
		List<Object[]> rows = em.createNativeQuery(sql).getResultList();



		return rows;
	}
}
