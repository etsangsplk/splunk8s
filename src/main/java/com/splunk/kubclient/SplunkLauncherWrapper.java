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

	public static void main(String[] args) throws UnknownHostException, IOException {

		if (args.length < 1) {
			System.out.println("Usage: " + "[shc-cap|shc-mbr] [optional_args]");
			System.exit(1);
		}
		
		String[] otherArgs = Arrays.asList(args).subList(1, args.length).toArray(new String[]{});

		String type = args[0];
		if(type.equals(MEMBER)){
			SHCMemberLauncher.main(otherArgs);
		}else if(type.equals(CAPTAIN)){
			SHCCaptainLauncher.main(args);
		}else{
			throw new RuntimeException("Error in "+SplunkLauncherWrapper.class.getName()+", unknown splunk node type: " + type);
		}
	}

}
