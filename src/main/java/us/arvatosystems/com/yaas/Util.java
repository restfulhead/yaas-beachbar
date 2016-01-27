package us.arvatosystems.com.yaas;

public class Util
{
	public static String maskPhoneNo(final String phoneNo)
	{
		if (phoneNo == null || phoneNo.length() < 8)
		{
			return phoneNo;
		}

		return phoneNo.substring(0, 7).concat("****");
	}
}
