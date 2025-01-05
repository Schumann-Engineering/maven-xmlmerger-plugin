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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.dom4j.Document;

import be.hikage.maven.plugin.xmlmerge.utils.Dom4JUtils;


/**
 * Goal which merge multiple XML files into one
 *
 * @goal mergeAllToOne
 * @phase prepare-package
 */
public class MergeMultipleXmlToSingleXmlMojo
	extends
	AbstractMergeXmlMojo
{
	/*
	 * ====================
	 * 
	 * FIELDS
	 * 
	 * ====================
	 */
	/**
	 * 
	 * @parameter default-value="true"
	 * @required
	 */
	protected boolean cleanOutputFile;

	/**
	 * 
	 * @parameter default-value="true"
	 * @required
	 */
	protected boolean failIfNoneFound;


	/*
	 * ====================
	 * 
	 * METHODS
	 * 
	 * ====================
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		// @INFO
		getLog()
			.info(
				"Output directory:              %s"
					.formatted(outputDirectory.getAbsolutePath())
			);
		// @INFO
		getLog()
			.info("Process prolog:                %s".formatted(processProlog));
		// @INFO
		getLog()
			.info(
				"Search file matching:          %s"
					.formatted(mergeFilenamePattern)
			);

		/*
		 * search files
		 */
		var regex    = Pattern.compile(mergeFilenamePattern);
		var xmlFiles = new ArrayList<File>();

		// BEWARE: byRef is used: xmlFiles
		findXmlToMerge(inputDirectory, xmlFiles);
		getLog()
			.info(
				"Number of file found to merge: %d".formatted(xmlFiles.size())
			);

		if (xmlFiles.size() == 0 && !failIfNoneFound)
			// === SUCCESS ===
			return;
		if (xmlFiles.size() == 0 && failIfNoneFound)
			// === FAIL ===
			throw new MojoExecutionException("no XML files found to merge");

		/*
		 * merge files
		 */
		var fileGroups = new HashSet<String>();
		try
		{
			// iterate over all XML files found
			for (var fileToMerge : xmlFiles)
			{
				// match the file name using the pattern
				// NOTE: the idea here is to only merge all files with a common
				// pattern.
				var matcher = regex.matcher(fileToMerge.getName());
				if (!matcher.matches() || matcher.groupCount() == 0)
					throw new MojoExecutionException(
						"The file do not matches regex"
					);

				/*
				 * determine file to merge
				 */
				getLog()
					.info(
						"XML file found for merging:    %s"
							.formatted(fileToMerge.getAbsolutePath())
					);

				/*
				 * determine file group
				 */
				// ... take last group by default
				var fileGroup = matcher.group(matcher.groupCount());
				if (mergeFilenamePattern.contains("(?<fileGroup>"))
					fileGroup = matcher.group("fileGroup");
				getLog()
					.info(
						"... file group:                %s".formatted(fileGroup)
					);

				/*
				 * determine base file
				 */
				// .. ?
				var baseFileName = EnsureFileExtension(fileGroup, ".xml");
				// ... file itself
				var baseFile = getBaseFile(fileToMerge, baseFileName);
				if (baseFile.exists())
					getLog()
						.info(
							"Base file found:               %s"
								.formatted(baseFile.getAbsolutePath())
						);
				else
					getLog().info("Base file NOT FOUND.");

				/*
				 * determine output file
				 */
				// ... use file group
				var  outputFileName = EnsureFileExtension(fileGroup, ".xml");
				// ... file itself
				File outputFile     = getOutputFile(fileToMerge, outputFileName);
				// delete existing file, if needed
				if (cleanOutputFile && !fileGroups.contains(fileGroup)
					&& outputFile.exists())
					outputFile.delete();
				// copy base file if one exists
				if (baseFile.exists())
					FileUtils.copyFile(baseFile, outputFile);

				// add group to list of discovered groups
				fileGroups.add(fileGroup);

				getLog()
					.info(
						"Output file:                   %s"
							.formatted(baseFile.getAbsolutePath())
					);

				/*
				 * MERGE FILE - nothing to merge
				 * 
				 * if output file does not exists than this is the first merge.
				 * since 1+0=1, we can just copy the file.
				 */
				if (!outputFile.exists())
				{
					FileUtils.copyFile(fileToMerge, outputFile);

					getLog().info("... simple copy performed");

					// === NEXT ===
					continue;
				}

				/*
				 * MERGE FILE - default
				 * 
				 * an output file already exists. now we have to merge.
				 */
				var prologHeader = processProlog ? new StringBuilder() : null;
				// read the (existing) output file
				var documentBase = Dom4JUtils
					.readDocument(outputFile.toURI().toURL(), prologHeader);
				// merge with current file in loop
				var result       = xmlMerger
					.mergeXml(documentBase, loadXml(fileToMerge));
				// write it back to output file
				writeMergedXml(outputFile, result, prologHeader);
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unable to merge xml", e);
		}

	}


	/**
	 * Override method to change it's behavior.
	 * 
	 * The original behavior assumes a matching folder structure between base
	 * and input directory.
	 * 
	 * This is very restrictive. The new implementation tries two strategies:
	 * 
	 * 1. check if there is a base file according to original strategy. 2. check
	 * if there is matching file without sub-folders
	 * 
	 * @param fileToMerge
	 * @param baseFileName
	 * @return
	 */
	@Override
	protected File getBaseFile(
		File fileToMerge,
		String baseFileName
	)
	{
		/*
		 * Strategy 1: original behavior
		 */
		var baseFile = super.getBaseFile(fileToMerge, baseFileName);
		if (baseFile != null && baseFile.exists())
			// === SUCCESS ===
			return baseFile;

		/*
		 * Strategy 2: just use the base file name
		 */
		baseFile = new File(baseDirectory, baseFileName);
		// === SUCCESS ===
		return baseFile;
	}


	/**
	 * 
	 * @param input
	 * @param extension
	 * @return
	 */
	protected static String EnsureFileExtension(
		String input,
		String extension
	)
	{
		if (input.toLowerCase().endsWith(extension))
			// === SUCCESS (nothing to do) ===
			return input;

		// === SUCCESS ===
		return "%s%s".formatted(input, extension);
	}

}
