/**
 * 
 */
package kr.dragshare.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author Jonghoon Seo
 *
 */
public interface NetworkManager {
	public boolean initialize(String host, int port, String id, String pw);
	
	public boolean close();
	
	public boolean send(File file, 				String targetFileName);
	public boolean send(String name,			String targetFileName);
	public boolean send(FileInputStream stream, String targetFileName);
	
	public boolean receive(String sourceFileName, 	String targetFileName);
	public boolean receive(String sourceFileName,	FileOutputStream targetOutputStream);
	
	public boolean isConnected();
}
