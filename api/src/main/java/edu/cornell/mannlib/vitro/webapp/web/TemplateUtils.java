/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.utility.DeepUnwrap;

/**
 * TODO
 */
public class TemplateUtils {
	private static final Log log = LogFactory.getLog(TemplateUtils.class);

	public static class DropFromSequence implements TemplateMethodModelEx {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object exec(List args) throws TemplateModelException {
			if (args.size() != 2) {
				throw new TemplateModelException("Wrong number of arguments");
			}

			TemplateModel sequenceWrapper = (TemplateModel) args.get(0);
			if (!(sequenceWrapper instanceof TemplateSequenceModel)
					&& !(sequenceWrapper instanceof TemplateCollectionModel)) {
				throw new TemplateModelException(
						"First argument must be a sequence or a collection");
			}
			TemplateModel unwantedWrapper = (TemplateModel) args.get(1);
			if (!(unwantedWrapper instanceof TemplateScalarModel)) {
				throw new TemplateModelException(
						"Second argument must be a string");
			}

			List<String> sequence = (List<String>) DeepUnwrap
					.unwrap(sequenceWrapper);
			String unwanted = (String) DeepUnwrap.unwrap(unwantedWrapper);

			sequence.remove(unwanted);
			return sequence;
		}

	}
}
