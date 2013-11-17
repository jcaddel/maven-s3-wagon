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
