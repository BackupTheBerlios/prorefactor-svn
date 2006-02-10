/* Created on Mar 17, 2005
 * Authors: John Green
 *
 * Copyright (C) 2005 Prolint.org Contributors
 * This file is part of Prolint.
 *
 * Prolint is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Prolint is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Prolint; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.prolint.core;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.exception.GenericJDBCException;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;


/** A temp-table for storing all TempMarker objects until
 * we are ready to create Eclipse markers for them all.
 * See TempMarker class notes for rationale.
 * This table of TempMarker objects is only used by LintRun,
 * and it is not intended to allow for multiple concurrent instances.
 */
class TempMarkerTable {

	public TempMarkerTable() throws HibernateException {
		File dbDirFile = new File(DBDIR);
		dbDirFile.mkdirs();
		Configuration cfg = new Configuration();
		cfg.configure(TempMarkerTable.class.getClassLoader().getResource(CONFIGFILE));
		SchemaExport schemaExport = new SchemaExport(cfg);
		schemaExport.drop(false, true);
		schemaExport.create(false, true);
		sessionFactory = cfg.buildSessionFactory();
	}

	private static String CONFIGFILE = "org/prolint/core/tempmarker.cfg.xml";
	private static String DBDIR = "./prolint/tempmarkerdb/";
	private SessionFactory sessionFactory;

	class QuerySession {
		QuerySession() throws HibernateException { }
		private Session session = sessionFactory.openSession();
		private Iterator iterator;
		Iterator iterator() { return iterator; }
		void close() throws HibernateException { session.close(); }
	}
	
	QuerySession getAll() throws HibernateException {
		QuerySession it = new QuerySession();
		it.iterator = it.session.iterate("from TempMarker");
		return it;
	}

	/** Store persistently any non-duplicate markers from the input list. */
	void storeMarkers(List markerList) throws HibernateException {
		if (markerList==null || markerList.size()==0) return;
		Session session = sessionFactory.openSession();
		for (Iterator it = markerList.iterator(); it.hasNext();) {
			Transaction transaction = session.beginTransaction();
			TempMarker marker = (TempMarker) it.next();
			try {
				session.save(marker);
				transaction.commit();
			} catch (GenericJDBCException e) {
				// Might not be unique. We don't care.
				transaction.rollback();
			}
		}
		session.close();
	}
	
}
