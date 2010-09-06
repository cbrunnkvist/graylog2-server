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

package org.graylog2.incidents.scanstrategies;

/**
 * IncidentScanStrategyIF.java: Aug 30, 2010 10:15:49 PM
 *
 * Incident scan strategy interface
 *
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public interface IncidentScanStrategyIF {

    /**
     * Perform the scan. Get the result via getResult()
     * @throws InvalidStrategyException
     */
    public void scan() throws InvalidStrategyException;

    /**
     * Get the result of the scan. Call scan() before.
     * @return
     */
    public boolean getResult();

    /**
     * Calls an INFO log with prepended information of current scan.
     * @param msg The message to log.
     */
    public void incidentLog(String msg);

}
