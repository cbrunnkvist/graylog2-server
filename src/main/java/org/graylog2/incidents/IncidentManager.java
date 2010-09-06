/**
 * Copyright 2010 Lennart Koopmann <lennart@socketfeed.com>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.graylog2.incidents;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Iterator;
import org.graylog2.Log;
import org.graylog2.Tools;
import org.graylog2.database.MongoBridge;

/**
 * IncidentManager.java: Aug 28, 2010 12:13:01 AM
 *
 * Master class to fetch Incidents and IncidentDescriptions
 *
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class IncidentManager {

    /**
     * Get all incident descriptions from MongoDB 'incidents' collection.
     * @return ArrayList of all IncidentDescriptions
     */
    public static ArrayList<IncidentDescription> fetchIncidentDescriptions() {
        ArrayList<IncidentDescription> descriptions = new ArrayList<IncidentDescription>();

        MongoBridge mongo = new MongoBridge();
        DBCollection coll = mongo.getIncidentColl();

        DBCursor cursor = coll.find();

        // Fetch all descriptions and place IncidentDescription objects in list.
        while(cursor.hasNext()) {
            DBObject dbObj = cursor.next();

            // Validate structure of Mongo document.
            if (!IncidentManager.validateIncidentDocumentStructure(dbObj)) {
                Log.info("Skipping incident description with incomplete fields.");
                continue;
            }

            // Create IncidentDescription object with basic information.
            IncidentDescription description = new IncidentDescription();
            description.setTitle(dbObj.get("name").toString());
            description.setTimerange(Tools.stringToInt(dbObj.get("timerange").toString()));

            // Get conditions.
            BasicDBList conditionList = (BasicDBList) dbObj.get("conditions");
            Iterator<Object> iter = conditionList.iterator();
            ArrayList<IncidentCondition> conditions = new ArrayList<IncidentCondition>();

            // Go through every condition and store it in conditions list.
            while(iter.hasNext()) {
                DBObject conditionDoc = (DBObject) iter.next();

                // Check condition.
                if (!IncidentManager.validateConditionDocumentStructure(conditionDoc)) {
                    Log.info("Skipping incident condition list with incomplete fields.");
                    continue;
                }

                // Add conditions.
                IncidentCondition condition = new IncidentCondition();
                condition.setType(Tools.stringToInt(conditionDoc.get("type").toString()));
                condition.setSubtype(Tools.stringToInt(conditionDoc.get("subtype").toString()));
                condition.setValue(conditionDoc.get("value"));
                conditions.add(condition);
            }

            description.setConditions(conditions);

            descriptions.add(description);
        }

        return descriptions;
    }

    /**
     * Validates the MongoDB document structure of an incident.
     *
     * @param doc The MongoDB DBObject to validate
     * @return boolean
     */
    public static boolean validateIncidentDocumentStructure(DBObject doc) {
        if (doc.containsField("name") && doc.containsField("timerange") && doc.containsField("conditions")) {
            return true;
        }
        
        return false;
    }

    /**
     * Validates the MongoDB document structure of an incident condition.
     *
     * @param doc The MongoDB DBObject to validate
     * @return boolean
     */
    public static boolean validateConditionDocumentStructure(DBObject doc) {
        // Make sure all required fields are set.
        if (!doc.containsField("type") || !doc.containsField("subtype") || !doc.containsField("value")) {
            return false;
        }

        // Make sure type is allowed.
        int type = Tools.stringToInt(doc.get("type").toString());
        if (type != IncidentCondition.TYPE_AND && type != IncidentCondition.TYPE_OR) {
            return false;
        }

        // Make sure subtype is allowed.
        int subtype = Tools.stringToInt(doc.get("subtype").toString());
        if (subtype != IncidentCondition.SUBTYPE_NONE
                && subtype != IncidentCondition.SUBTYPE_SUBSTRING
                && subtype != IncidentCondition.SUBTYPE_HOST
                && subtype != IncidentCondition.SUBTYPE_SEVERITY
                && subtype != IncidentCondition.SUBTYPE_REGEX) {
            return false;
        }

        // Value must be integer or string.
        Object value = doc.get("value");
        if (!value.getClass().getName().equals("java.lang.String") && !value.getClass().getName().equals("java.lang.Double")) {
            return false;
        }
        
        return true;
    }

}