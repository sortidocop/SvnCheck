package com.svnlog.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


import generated.Log;
import generated.Log.Logentry;

public class Run {

	private final static String TAG_DEL = "TAG-";

	public enum SvnCommand {
		SVN("svn"), COMMAND1("log"), COMMAND2("-r"), TYPEFILE("--xml");

		private String name = "";

		SvnCommand(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	public static boolean isContainsThisChar(String string, String car) {
		if (string.isEmpty()) {
			return false;
		}
		if (string.indexOf(car) == -1) {
			return false;
		}

		return true;
	}

	public static String getStringOfRegulareExpressionPattern(String string, String pattern)
			throws PatternSyntaxException {
		if (string.isEmpty() || pattern.isEmpty()) {
			throw new IllegalArgumentException(" The pattern or the string to search is empty");
		}
		// Pattern : permet d'obtenir une version compilée d'une expression
		// régulière.
		final Pattern p = Pattern.compile(pattern);
		final String enter = string;
		// Matcher : permet d'analyser une chaîne en entrée à partir d'un
		// Pattern.
		final Matcher m = p.matcher(enter);
		final StringBuffer buffer = new StringBuffer();
		while (m.find()) {
			if (!(enter.substring(m.start(), m.end())).isEmpty()) {
				buffer.append(enter.substring(m.start(), m.end()));
			}
		}

		return buffer.toString();
	}

	public static void main(String[] args) {

		String url = "https://subvervions/branches/java/src";
		String dateString = "{2017-03-27}:{2017-04-21}";

		final Set<String> ticketAlreadyDone = new HashSet<String>();

		BufferedReader reader = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			String[] cmdArray = { SvnCommand.SVN.toString(), SvnCommand.COMMAND1.toString(), url,
					SvnCommand.COMMAND2.toString(), dateString, SvnCommand.TYPEFILE.toString() }; // etc
			Process process = runtime.exec(cmdArray);

			InputStream in = process.getInputStream();

			final JAXBContext jaxbContext = JAXBContext.newInstance(Log.class);

			final Unmarshaller jaxUnMarshaller = jaxbContext.createUnmarshaller();

			Log logObj = (Log) jaxUnMarshaller.unmarshal(in);

			for (Logentry logString : logObj.getLogentry()) {
				if (isContainsThisChar(logString.getMsg(), TAG_DEL)) {
					// AGL-[0-9]*
					String tutu = getStringOfRegulareExpressionPattern(logString.getMsg(), TAG_DEL + "[0-9]*");
					// System.out.println(logString.getMsg());
					if (!ticketAlreadyDone.contains(tutu)) {
						ticketAlreadyDone.add(tutu);
					}
				}
			}

			for (String ticket : ticketAlreadyDone) {
				System.out.println(ticket);
			}

			process.waitFor();

			System.out.println("done!");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
