/*
 * Copyright © 2011 The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package engineering.schumann.maven.plugin.xmlmerge;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import be.hikage.maven.plugin.xmlmerge.XmlMerger;
import be.hikage.maven.plugin.xmlmerge.utils.PathUtils;


/**
 * Goal which merge XML file
 */
public abstract class AbstractMergeXmlMojo
	extends
	AbstractMojo
{
	/**
	 * The input directory in which the XML Merge Data can be found.
	 *
	 * @parameter default-value="${basedir}/src/main/xmlmerge"
	 * @required
	 */
	protected File      inputDirectory;

	/**
	 * The output directory into which to copy the resources.
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File      outputDirectory;

	/**
	 * The input directory in which the XML Document base can be found.
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File      baseDirectory;

	/**
	 * Flag to indicate if the Merge Document must be deleted after processing
	 *
	 * @parameter default-value="false"
	 * @required
	 */
	protected Boolean   removeMergeDocumentAfterProcessing;

	/**
	 * The Xml Merge component instance that will be injected by the Plexus
	 * runtime.
	 *
	 * @component
	 */
	protected XmlMerger xmlMerger;

	/**
	 * Flag to indicate that the text contained in prolog must be filtered
	 * before applying
	 *
	 * @parameter default-value="false"
	 * @required
	 */
	protected Boolean   processProlog;

	/**
	 * The mergeFilenamePattern used to find XML Document to merge. It have to
	 * return two groups, including the second is the name of the file in which
	 * it must be merged. The default pattern assume that the XML Document to be
	 * merged has the same name as the base XML Document
	 *
	 * @parameter default-value="()(.*\[xX][mM][lL])"
	 * @required
	 */
	protected String    mergeFilenamePattern;


	public abstract void execute() throws MojoExecutionException;


	protected File getBaseFile(
		File fileToMerge,
		String baseFileName
	)
	{

		String relativePath = PathUtils
			.getRelativePath(fileToMerge, inputDirectory)
			.replace(fileToMerge.getName(), "");
		File   baseFile     = new File(
			baseDirectory,
			relativePath + baseFileName
		);

		return baseFile;
	}


	protected File getOutputFile(
		File fileToMerge,
		String baseFileName
	)
	{

		String relativePath = PathUtils
			.getRelativePath(fileToMerge, inputDirectory)
			.replace(fileToMerge.getName(), "");
		File   outputFile   = new File(
			outputDirectory,
			relativePath + baseFileName
		);
		return outputFile;
	}


	protected void findXmlToMerge(
		File fileToProcess,
		List<File> xmlFiles
	)
	{

		RegexFileFilter  filter2    = new RegexFileFilter(mergeFilenamePattern);

		Collection<File> filesFound = FileUtils
			.listFiles(fileToProcess, filter2, DirectoryFileFilter.DIRECTORY);

		xmlFiles.addAll(filesFound);

	}


	protected Document loadXml(
		File baseFile
	) throws DocumentException
	{
		SAXReader reader = new SAXReader();
		return reader.read(baseFile);
	}


	protected void deleteMergeFile(
		File fileToMerge
	)
	{
		fileToMerge.delete();

	}


	protected void writeMergedXml(
		File baseFile,
		Document base,
		StringBuilder prologHeader
	) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(baseFile);

		if (processProlog && prologHeader != null
			&& StringUtils.isNotEmpty(prologHeader.toString()))
		{
			fos.write(prologHeader.toString().getBytes());
		}

		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setSuppressDeclaration(true);
		format.setNewLineAfterDeclaration(false);
		XMLWriter writer = new XMLWriter(fos, format);
		writer.write(base);
		writer.flush();
		writer.close();

	}
}
