package com.example.securemessaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

class StringCompressor 
{

	public static String compress(String text) 
	{
		System.out.println("input: " + text.length());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try 
		{
			OutputStream out = new DeflaterOutputStream(baos);
			out.write(text.getBytes("UTF-8"));
			out.close();
		} 
		catch (IOException e) 
		{
			throw new AssertionError(e);
		}
		byte []encodedBytes = baos.toByteArray();

		System.out.println("after compression: " + new String(encodedBytes).length());

		return new String(org.bouncycastle.util.encoders.Base64.encode(encodedBytes));
		//return encodedBytes;
	}

	public static String decompress(String b) 
	{
		byte[] bytes =  org.bouncycastle.util.encoders.Base64.decode(b);

		InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try 
		{
			byte[] buffer = new byte[8192];
			int len;
			while ((len = in.read(buffer))>0)
				baos.write(buffer, 0, len);
			
			System.out.println("after decompression: " + new String(baos.toByteArray(), "UTF-8"));
			
			return new String(baos.toByteArray(), "UTF-8");
		} catch (IOException e) 
		{
			throw new AssertionError(e);
		}
	}
}