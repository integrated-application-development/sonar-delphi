/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.StaxParser.XmlStreamHandler;
import org.sonar.plugins.delphi.codecoverage.CoverageFileData;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiCoverageToolParserStreamHandler implements XmlStreamHandler
{
	private final Project project;
	private final SensorContext context;
			
	public DelphiCoverageToolParserStreamHandler(Project project, SensorContext context) {
		this.project = project;
		this.context = context;
	}

	public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
		rootCursor.advance();      
		SMInputCursor fileCursor = rootCursor.descendantElementCursor("srcfile");

		while(fileCursor.getNext() != null) {    	    	  
			CoverageFileData data = collectCoverageData(fileCursor);
			saveCoverageData(data);
		}
	}
	
	private void saveCoverageData(CoverageFileData data) {
		if(data == null) {
			return;
		}
		Measure overallCoverage = new Measure(CoreMetrics.COVERAGE, data.getCoverage());
		Measure lineCoverage = new Measure(CoreMetrics.LINE_COVERAGE, data.getCoverage());
		Measure linesToCover = new Measure(CoreMetrics.LINES_TO_COVER, data.getTotalLines());
		Measure uncoveredLines = new Measure(CoreMetrics.UNCOVERED_LINES, data.getUncoveredLines());
		Measure lineHits = data.getLineHitsBuilder().build().setPersistenceMode(PersistenceMode.DATABASE);
		context.saveMeasure(data.getResource(), overallCoverage); // save overall file coverage
		context.saveMeasure(data.getResource(), lineCoverage); // save file coverage
		context.saveMeasure(data.getResource(), linesToCover); // save total lines to cover
		context.saveMeasure(data.getResource(), uncoveredLines); // save uncovered lines
		context.saveMeasure(data.getResource(), lineHits); // save line hits data
		DelphiUtils.LOG.debug("Saving coverage to: " + data.getResource().getName());
	}

	private CoverageFileData collectCoverageData(SMInputCursor fileCursor) {  	  
		try {
			String fileName = fileCursor.getAttrValue("name");    	
			File sourceFile = getSourceFileFromName(fileName);
			DelphiFile resource = DelphiFile.fromIOFile(sourceFile, project.getFileSystem().getSourceDirs(), false);
			if(resource == null) {
				DelphiUtils.LOG.warn("File not found for code coverage: " + fileName);
				return null;
			}
			int totalLines = 0;
			int coveredLines = 0;
			
			CoverageFileData data = new CoverageFileData(resource);        
			SMInputCursor lineCursor = fileCursor.descendantElementCursor("line");        
			while (lineCursor.getNext() != null) {          
				if(!lineCursor.asEvent().isStartElement()) {
					continue;
				}
				String lineNumber = lineCursor.getAttrValue("number");
				boolean isCovered = Boolean.valueOf(lineCursor.getAttrValue("covered"));
				data.getLineHitsBuilder().add(lineNumber, isCovered ? 1 : 0);
				coveredLines += isCovered ? 1 : 0;
				++totalLines;
			}
			
			data.setTotalLines(totalLines);
			data.setCoverage(coveredLines);
			data.setUncoveredLines(totalLines - coveredLines);						
			DelphiUtils.LOG.debug("Coverage (" + fileName + "): " + coveredLines + "/" + totalLines);
			return data;
		}
		catch(Exception e) {
			return null;
		}
	}

	private File getSourceFileFromName(String fileName) {
		for(File dir : project.getFileSystem().getSourceDirs()) {
			for(File file : dir.listFiles()) {
				if(file.getName().equalsIgnoreCase(fileName)) {
					return file;
				}        		
			}
		}
		return null;
	}

}
