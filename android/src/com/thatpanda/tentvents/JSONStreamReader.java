package com.thatpanda.tentvents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONStreamReader {
	public static JSONObject getJSON(InputStream in, String encoding) throws
			UnsupportedEncodingException,
			IOException,
			JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
		    builder.append(line).append("\n");
		}
		in.close();
		return new JSONObject(builder.toString());
	}
}
