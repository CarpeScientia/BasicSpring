package util;

import org.junit.Test;
import org.cs.springbase.util.UserUtil;
import org.junit.Assert;

public class UserUtilTest {
	@Test
	public void underscoresToSpacesTest() {
		//extra cast to prevent deprecation warning 
		Assert.assertEquals((Object)null,UserUtil.underscoresToSpaces( null ));
		Assert.assertEquals( "first name last name",
				UserUtil.underscoresToSpaces("first_name_last_name"));	
	}
}
