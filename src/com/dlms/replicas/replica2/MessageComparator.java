package com.dlms.replicas.replica2;

import java.util.Comparator;

public class MessageComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub

		String m1 = (String) o1;
		String m2 = (String) o2;

		String message1[] = m1.split(",");
		String message2[] = m2.split(",");

		int seq1 = Integer.parseInt((message1[0]));
		int seq2 = Integer.parseInt((message2[0]));
		;

		if (seq1 < seq2) {

			return -1;

		} else if (seq1 > seq2) {

			return 1;
		} else {

			return 0;

		}

	}

}
