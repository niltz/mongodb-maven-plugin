/**
 * Copyright (c) 2012 Jeff Sawatzky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.github.niltz.maven.plugins.mongodb;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Mojo for updating databases.
 * 
 * @goal update
 */
public class MongoDBUpdateMojo extends AbstractMongoDBMojo {

	private DBCollection currentAppliedUpdatesCollection;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeInternal() throws MojoExecutionException, MojoFailureException {

		try {
			for (int i = 0; i < getDatabaseSettings().length; i++) {
				DatabaseSettings databaseSettings = getDatabaseSettings()[i];
				getLog().info("Updating '" + databaseSettings.getConnectionSettings().getDatabase() + "'");

				ConnectionSettings connectionSettings = databaseSettings.getConnectionSettings();
				Mongo mongo = openConnection(connectionSettings);
				DB db = getDatabase(connectionSettings, mongo);

				//
				// Get a list of currently applied updates
				//
				ArrayList<String> appliedUpdates = new ArrayList<String>();
				currentAppliedUpdatesCollection = db.getCollection(databaseSettings.getUpdatesCollection());
				DBCursor cursor = currentAppliedUpdatesCollection.find();
				while (cursor.hasNext()) {
					DBObject appliedupdate = cursor.next();
					appliedUpdates.add((String) appliedupdate.get(databaseSettings
							.getUpdatesCollectionScriptNameField()));
				}

				//
				// Execute the scripts in the directory ignoring the ones that
				// have been applied
				//
				executeScriptsInDirectory(connectionSettings, "update", db, appliedUpdates.toArray(new String[0]));
			}

		} catch (IOException ioe) {
			throw new MojoExecutionException("Error executing update scripts", ioe);
		}
	}
}