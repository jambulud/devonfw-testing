package com.capgemini.kabanos.database;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.capgemini.kabanos.common.domain.Implementation;
import com.capgemini.kabanos.common.domain.Preposition;
import com.capgemini.kabanos.common.domain.Project;
//import com.capgemini.kabanos.common.domain.Project;
import com.capgemini.kabanos.database.utils.HibernateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataBase {

	private Properties properties;

	public DataBase(Properties properties) {
		this.properties = properties;
	}

	public boolean savePrepositions(List<Preposition> prepositions) {
		boolean result = false;
		Session session = null;
		Transaction tran = null;

		/*
		 * objects must point to a proper reference- once to a new created object and
		 * once to a object taken form the database. if a predecessor is present in the
		 * DB then the object must point to a BDobject if it point to a wrong object the
		 * a "transient" error is thrown
		 */
		Map<String, Preposition> prepositionMap = new HashMap<>();

		try {
			session = HibernateUtils.getSessionFactory(this.properties).openSession();
			tran = session.beginTransaction();

			Project project = initProject(prepositions, session);

			for (Preposition prep : prepositions) {
				Preposition fromDB = this.getPreposition(project.getName(), prep.getFormattedLoggerStep(), session);
				prepositionMap.put(prep.getFormattedLoggerStep(), fromDB != null ? fromDB : prep);

				Set<Preposition> newPredecesors = new HashSet<>();
				for (Preposition pred : prep.getPredecessors()) {
					newPredecesors.add(prepositionMap.getOrDefault(pred.getFormattedLoggerStep(), pred));
				}
				prep.setPredecessors(newPredecesors);

				if (fromDB == null) {
					prep.setProject(project);
					session.save(prep);
					for (Implementation impl : prep.getImplementations()) {
						session.save(impl);
					}
				} else {
					fromDB.addTotalNumber(prep.getTotalNumber());
					fromDB.addImplementations(prep.getImplementations());
					fromDB.addPredecessors(prep.getPredecessors());
					session.update(fromDB);

					for (Implementation impl : fromDB.getImplementations()) {
						if (impl.getOccurrences() == 1)
							session.save(impl);
						else
							session.update(impl);
					}
				}
			}

			tran.commit();
			System.out.println("Saved: " + prepositions.size());
			result = true;
		} catch (Exception e) {
			result = false;
			if (tran != null) {
				System.out.println("Performing rollback");
				tran.rollback();
			}
			System.out.println(e);
		} finally {
			if (session != null)
				session.close();
		}

		return result;
	}

	private Project initProject(List<Preposition> prepositions, Session session) {
		Project project = null;
		if (!prepositions.isEmpty()) {
			if (project == null) {
				project = this.getProject(prepositions.get(0).getProject().getName(), session);

				if (project == null) {
					project = prepositions.get(0).getProject();
					session.save(project);
				}
			}
		}
		return project;
	}

	public boolean savePreposition(Preposition preposition) {
		List<Preposition> list = new ArrayList<Preposition>();
		list.add(preposition);
		return this.savePrepositions(list);
	}

	private Project getProject(String projectName, Session session) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Project> criteria = criteriaBuilder.createQuery(Project.class);

		Root<Project> root = criteria.from(Project.class);
		criteria.where(criteriaBuilder.equal(root.get("name"), projectName));

		return session.createQuery(criteria).uniqueResult();
	}

	private Preposition getPreposition(String projectName, String loggerStep, Session session) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Preposition> criteria = criteriaBuilder.createQuery(Preposition.class);

		Root<Preposition> root = criteria.from(Preposition.class);

		Predicate and = criteriaBuilder.and(criteriaBuilder.equal(root.get("project").get("name"), projectName),
				criteriaBuilder.equal(root.get("formattedLoggerStep"), loggerStep));
		criteria.where(and);

		return session.createQuery(criteria).uniqueResult();
	}

	public Preposition getPreposition(String projectName, String formattedLoggerStep) {
		Session session = HibernateUtils.getSessionFactory(this.properties).openSession();
		Preposition result = this.getPreposition(projectName, formattedLoggerStep, session);
		session.close();
		return result;
	}

	public List<Preposition> getAllPrepositions() {
		Session session = HibernateUtils.getSessionFactory(this.properties).openSession();

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Preposition> criteria = builder.createQuery(Preposition.class);

		Root<Preposition> root = criteria.from(Preposition.class);
		criteria.select(root);

		List<Preposition> result = session.createQuery(criteria).getResultList();

		if (session != null)
			session.close();

		return result;
	}

	public List<Preposition> getPrepositions(String projectName) {
		Session session = HibernateUtils.getSessionFactory(this.properties).openSession();

		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Preposition> criteria = builder.createQuery(Preposition.class);

		Root<Preposition> root = criteria.from(Preposition.class);

		criteria.where(builder.equal(root.get("project").get("name"), projectName));
		List<Preposition> result = session.createQuery(criteria).getResultList();

		if (session != null)
			session.close();

		return result;
	}

	public void shutdown() {
		HibernateUtils.shutdown();
	}
}