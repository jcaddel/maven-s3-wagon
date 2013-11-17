/**
 * Copyright 2010-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon.auth;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

public class Assert extends org.springframework.util.Assert {

	public static void noNulls(Object... objects) {
		for (Object object : objects) {
			notNull(object);
		}
	}

	public static void noBlanks(String... strings) {
		noBlanksWithMsg("blanks not allowed", strings);
	}

	public static void noBlanksWithMsg(String msg, String... strings) {
		noBlanksWithMsg(msg, ImmutableList.copyOf(strings));
	}

	public static void noBlanksWithMsg(String msg, List<String> strings) {
		notNull(strings);
		for (String string : strings) {
			if (StringUtils.isBlank(string)) {
				throw new IllegalArgumentException(msg);
			}
		}
	}

}
