package us.arvatosystems.com.yaas.helper;

import java.util.Arrays;

import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;

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

	public static DiagnosticContext newContext()
	{
		// FIXME instead of these dummy values we should forward the yaas request id and hop header value
		return new DiagnosticContext("not implemented yet", Integer.valueOf(0));
	}

	public static AuthorizationScope newScope(final String... scopes)
	{
		return new AuthorizationScope(Arrays.asList(scopes));
	}
}
