package org.apache.commons.io.filefilter;


import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;


/**
 * common-io extension to filter paths.
 * 
 * NOTE 1: path aren't checked per se. Only files are. There must be one file in
 * a path to trigger this filter.
 * 
 * NOTE 2: it assumes Unix-style paths, since "\" has meaning in regular
 * expressions and correct escaping is just cumbersome.
 */
public class RegexPathFilter
	extends
	AbstractFileFilter
{
	/*
	 * ====================
	 * 
	 * NESTED CLASSIFIERS
	 * 
	 * ====================
	 */
	public enum FilterMode
	{
		ACCEPT, REJECT
	}


	/*
	 * ====================
	 * 
	 * CONSTANTS
	 * 
	 * ====================
	 */
	/*
	private static final Log   LOG = new SystemStreamLog();
	*/

	/*
	 * ====================
	 * 
	 * FIELDS
	 * 
	 * ====================
	 */
	protected final Pattern    f_filterExpression;

	protected final FilterMode f_filterMode;


	/*
	 * ====================
	 * 
	 * CONSTRUCTOR
	 * 
	 * ====================
	 */
	public RegexPathFilter(
		String filterRegExpression
	)
	{
		this(filterRegExpression, FilterMode.ACCEPT);
	}


	public RegexPathFilter(
		String filterRegExpression,
		FilterMode filterMode
	)
	{
		super();

		f_filterMode = filterMode;

		// pre-compile regular expression
		f_filterExpression = Pattern.compile(filterRegExpression);

	}


	/*
	 * ====================
	 * 
	 * METHODS (From AbstractFileFilter)
	 * 
	 * ====================
	 */
	@Override
	public boolean accept(
		File file
	)
	{
		return accept(file.getPath());
	}


	@Override
	public boolean accept(
		File dir,
		String name
	)
	{
		// use normalize to account for possible double separators or windows
		// paths which use \
		return accept(dir.getPath());
	}


	/*
	 * ====================
	 * 
	 * METHODS
	 * 
	 * ====================
	 */
	protected boolean accept(
		String path
	)
	{
		// normalize path
		var haystack = FilenameUtils
			.separatorsToUnix(FilenameUtils.normalize(path));

		// match with regular expression
		var matcher  = f_filterExpression.matcher(haystack);

		// accept = did we find a match?
		// NOTE: the consequence is handled by the super-constructor initialized
		// in the constructor
		var result   = matcher.find();

		/*
		// @DEBUG
		LOG
			.debug(
				"%s = %s for '%s' on '%s'"
					.formatted(
						f_filterMode.toString(),
						result,
						f_filterExpression.pattern(),
						haystack
					)
			);
		*/

		// === RESULT ===
		switch (f_filterMode)
		{
			case REJECT:
				return !result;

			case ACCEPT:
			default:
				return result;
		}
	}
}
