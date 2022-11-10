package com.example.db;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.example.model.Email;

import org.hibernate.SessionFactory;

public class DbTest {
	private static final String TEST_TEST_COM = "yoyosssubham@highradius.com";
	private static SessionFactory factory;

	public static void main(String[] args) {

		try {
			Configuration cfg = new Configuration();
			cfg.addAnnotatedClass(com.example.model.Email.class);
			cfg.configure();
			factory = cfg.configure("hibernate.cfg.xml").buildSessionFactory();
		} catch (Exception ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		}

		DbTest dbTest = new DbTest();
		dbTest.addEmail(TEST_TEST_COM, TEST_TEST_COM, TEST_TEST_COM, "test", "test");
		dbTest.addEmail(TEST_TEST_COM, null, null, "test", "");
		dbTest.listEmails();
		dbTest.updateEmails(1, "qgfyqefv");
		dbTest.deleteEmail(3);
		dbTest.listEmails();
	}

	public void addEmail(String emailTo, String emailCc, String emailBcc, String emailSubject, String emailBody) {
		Session session = factory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			Email email = new Email();
			email.setEmailTo(emailTo);
			email.setEmailCc(emailCc);
			email.setEmailBcc(emailBcc);
			email.setEmailSubject(emailSubject);
			email.setEmailBody(emailBody);
			session.save(email);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	public void listEmails() {
		Session session = factory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			List<Email> emails = session.createQuery("FROM Email", Email.class).list();
			while (!emails.isEmpty()) {
				Email email = emails.iterator().next();
				System.out.println("Email To: " + email.getEmailTo());
				System.out.println("Email Cc: " + email.getEmailBcc());
				System.out.println("Email Bcc: " + email.getEmailCc());
				System.out.println("Email Subject: " + email.getEmailSubject());
				System.out.println("Email Body: " + email.getEmailBody());
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	/* Method to UPDATE body for an email */
	public void updateEmails(Integer emailID, String body) {
		Session session = factory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			Email email = session.get(Email.class, emailID);
			email.setEmailBody(body);
			session.update(email);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	public void deleteEmail(Integer emailID) {
		Session session = factory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			Email email = session.get(Email.class, emailID);
			session.delete(email);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}