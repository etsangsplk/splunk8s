/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 *
 * @author ghendrey
 */
public class SplunkLauncherWrapper {

	public static String MEMBER = "shc-mbr";
	public static String CAPTAIN = "shc-cap";

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println("Usage: " + "[shc-cap|shc-mbr] [optional_args]");
			System.exit(1);
		}
		
		//strip off the first arg (shc-cap|shc-member) and pass the otherArgs
		//on to the captain or member launcher respectively
		String[] otherArgs = Arrays.asList(args).subList(1, args.length).toArray(new String[]{});

		String type = args[0];
		if(type.equals(MEMBER)){
			System.out.println("First arg is " + MEMBER+ ": launching shc member");
			System.out.println("remaining args passed to member: " + Arrays.asList(otherArgs));
			SHCMemberLauncher.main(otherArgs);
		}else if(type.equals(CAPTAIN)){
			System.out.println("First arg is " + CAPTAIN+ ": launching shc captain");
			System.out.println("remaining args passed to captain: " + Arrays.asList(otherArgs));
			SHCCaptainLauncher.main(otherArgs);
		}else{
			throw new RuntimeException("Error in "+SplunkLauncherWrapper.class.getName()+", unknown splunk node type: " + type);
		}
	}

}
