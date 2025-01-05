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

package be.hikage.maven.plugin.xmlmerge;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.dom4j.Document;

import be.hikage.maven.plugin.xmlmerge.utils.Dom4JUtils;
import engineering.schumann.maven.plugin.xmlmerge.AbstractMergeXmlMojo;


/**
 * Goal which merge XML file
 *
 * @goal mergexml
 * @phase prepare-package
 */
public class MergeTwoXmlIntoOneXmlMojo
	extends
	AbstractMergeXmlMojo
{
	@Override
	public void execute() throws MojoExecutionException
	{
		getLog().info("EXECUTE on " + outputDirectory.getAbsolutePath());
		getLog().info("Process prolog : " + processProlog);

		List<File> xmlFiles = new ArrayList<File>();

		Pattern    regex    = Pattern.compile(mergeFilenamePattern);

		getLog().info("Search file that match " + mergeFilenamePattern);
		findXmlToMerge(inputDirectory, xmlFiles);

		getLog().info("Number of file found to merge :" + xmlFiles.size());

		try
		{
			for (File fileToMerge : xmlFiles)
			{
				Matcher matcher = regex.matcher(fileToMerge.getName());
				if (matcher.matches() && matcher.groupCount() == 2)
				{

					String baseFileName = matcher.group(2);

					File   basefile     = getBaseFile(
						fileToMerge,
						baseFileName
					);
					File   outputFile   = getOutputFile(
						fileToMerge,
						baseFileName
					);

					getLog().debug("Merge Base :" + basefile.getAbsolutePath());
					getLog()
						.debug(
							"Merge Transform :" + fileToMerge.getAbsolutePath()
						);
					getLog()
						.debug("Merge Output :" + outputFile.getAbsolutePath());

					if (basefile.exists())
					{

						StringBuilder prologHeader = processProlog
							? new StringBuilder()
							: null;
						Document      documentBase = Dom4JUtils
							.readDocument(
								basefile.toURI().toURL(),
								prologHeader
							);
						Document      result       = xmlMerger
							.mergeXml(documentBase, loadXml(fileToMerge));

						writeMergedXml(outputFile, result, prologHeader);

						if (removeMergeDocumentAfterProcessing)
						{
							boolean fileDeleted = fileToMerge.delete();
							if (!fileDeleted)
								getLog()
									.warn(
										"Unable to delete file :"
											+ fileToMerge.getAbsolutePath()
									);
						}

					}
					else
					{
						getLog()
							.warn(
								"No filebase found for "
									+ fileToMerge.getAbsolutePath()
							);
					}

				}
				else
				{
					throw new MojoExecutionException(
						"The file do not matches regex"
					);

				}
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Unable to merge xml", e);
		}

	}
}
