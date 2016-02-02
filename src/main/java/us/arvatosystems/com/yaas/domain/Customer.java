package us.arvatosystems.com.yaas.domain;

public class Customer
{
	private String name;
	private String email;

	public Customer()
	{
		super();
	}

	public Customer(final String name, final String email)
	{
		super();
		this.email = email;
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

}
