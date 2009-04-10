package com.netifera.platform.util.comparators;

import java.util.Comparator;

@Deprecated // kevin will do it better
public class HostnameComparator implements Comparator<String> {

	private String normalize(String name) {
		int start = name.charAt(0) == '.' ? 1 : 0;
		int end = name.length();
		if (name.charAt(name.length()-1) == '.') {
			end -= 1;
		}
		return name.substring(start, end);
	}
	
	public int compare(String name1, String name2) {
		name1 = normalize(name1);
		name2 = normalize(name2);
		if (name1.compareTo(name2) == 0) {
			return 0;
		}
		String[] members1 = name1.split("\\.+");
		String[] members2 = name2.split("\\.+");
		int i1 = 0, i2 = 0;
		for (; i1 < members1.length && i2 < members2.length; i1++, i2++) {
			int r = members1[members1.length - i1 - 1].compareToIgnoreCase(members2[members2.length - i2 - 1]);
			if (r != 0) {
				return r > 0 ? 1 : -1;
			}
		}
		if (members1.length == members2.length) {
			return 0;
		}
		return members1.length > members2.length ? -1 : 1;
	}
}
