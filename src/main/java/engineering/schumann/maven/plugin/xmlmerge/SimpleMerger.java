package engineering.schumann.maven.plugin.xmlmerge;


import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.dom4j.Document;
import org.dom4j.Node;

import be.hikage.maven.plugin.xmlmerge.XmlMerger;


public class SimpleMerger
	implements
	XmlMerger
{
	/*
	 * ====================
	 * 
	 * CONSTANTS
	 * 
	 * ====================
	 */
	private static final Log LOG = new SystemStreamLog();


	/*
	 * ====================
	 * 
	 * METHODS (From XmlMerger)
	 * 
	 * ====================
	 */
	public Document mergeXml(
		Document inputDocument,
		Document mergeData
	)
	{
		// === SETUP ===
		// clone document
		var targetDoc      = (Document) inputDocument.clone();
		var targetRoot     = targetDoc.getRootElement();
		var sourceDoc      = (Document) mergeData.clone();
		var sourceRoot     = sourceDoc.getRootElement();
		var sourceElements = sourceRoot.elements();

		// === BODY ===
		// @INFO
		LOG.info("found %d elements to merge".formatted(sourceElements.size()));

		for (var sourceElementObj : sourceElements)
		{
			// FIXME how is this done correctly? why does elements() return List
			// and not List<Element>?
			var sourceElement = (Node) sourceElementObj;

			// @INFO
			LOG
				.info(
					"merging node of type '%s': '%s'"
						.formatted(
							sourceElement.toString(),
							sourceElement.getNodeTypeName()
						)
				);

			// change parent
			targetRoot.add(sourceElement.detach());
		}

		// === SUCCESS ===
		return targetDoc;
	}
}
