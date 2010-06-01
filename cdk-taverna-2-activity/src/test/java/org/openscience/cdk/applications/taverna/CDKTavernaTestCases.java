/* 
 * $Author: egonw $
 * $Date: 2008-05-05 12:58:11 +0200 (Mo, 05 Mai 2008) $
 * $Revision: 10819 $
 * 
 * Copyright (C) 2006 - 2007 by Thomas Kuhn <thomaskuhn@users.sourceforge.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.openscience.cdk.applications.taverna;

import junit.framework.TestCase;

import org.openscience.cdk.tools.LoggingTool;

/**
 * Super class for <b>all</b> CDK-Taverna TestCase implementations that ensures that
 * the LoggingTool is configured.
 *
 *
 */
public class CDKTavernaTestCases extends TestCase {

    static {
        LoggingTool.configureLog4j();
    }
    
    public CDKTavernaTestCases() {
        super();
    }
    
    public CDKTavernaTestCases(String name) {
        super(name);
    } 
    
    /**
     * Determines of slow JUnit tests are to be run. You can set this
     * from the command line when running Ant: 
     * <pre>
     *   ant -f build.xml -DrunSlowTests=false test-all
     * </pre>
     * 
     * @return
     */
    public boolean runSlowTests() {
    	if (System.getProperty("runSlowTests", "true").equals("false")) 
    		return false;
    	return true;
    }     
}
