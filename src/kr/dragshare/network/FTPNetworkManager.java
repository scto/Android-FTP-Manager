/**
 * 
 */
package kr.dragshare.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import kr.dragshare.androidftpmanagertest.MainActivity;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * FTP 작업을 수행하는 Wrapper Class 입니다.
 * initialize, send 등의 작업을 쉽게 구현하였습니다.
 * 좀 더 자세한 작업은 FTPClient를 이용하여 수행하면 됩니다.
 * FTPClient는 Apache Commons 프로젝트를 사용하였습니다.
 * 자세한 내용은 http://commons.apache.org/proper/commons-net/ 를 참고하세요.
 * FTP Client 예제: http://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
 * @author Jonghoon Seo
 *
 */
public class FTPNetworkManager implements NetworkManager, CopyStreamListener {
	public FTPClient ftp;
	public long transferred;

	/**
	 * 
	 */
	public FTPNetworkManager() {
		ftp = new FTPClient();
		
		ftp.setCopyStreamListener(this);
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#initialize(java.lang.String, int, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean initialize(String host, int port, String username, String password) {
		boolean isOK = true;;
		// 호스트에 연결 
		try {
			ftp.connect(host, port);
			
	        // After connection attempt, you should check the reply code to verify
	        // success.
	        int reply = ftp.getReplyCode();

	        if (!FTPReply.isPositiveCompletion(reply))
	        {
	            ftp.disconnect();
	            System.err.println("Error: FTP server refused connection.");
	            return false;
	        }
		} catch (SocketException e) {
			System.err.println("Error: socket timeout could not be set.");
			System.err.println(e.getMessage());
			
            return false;
		} catch (IOException e) {
			System.err.println("Error: the socket could not be opened. In most cases you will only want to catch IOException since SocketException is derived from it.");
			System.err.println(e.getMessage());

            return false;		
		}
		
		// Log in
		try {
            if (!ftp.login(username, password))
            {
                ftp.logout();
                
                return false;
            }
		} catch (IOException e) {
			System.err.println("Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			System.err.println(e.getMessage());

			return false;
		} 
		
		return isOK;
	}

	
	
	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.io.File, java.lang.String)
	 */
	@Override
	public boolean send(File file, String targetFileName) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(file); 
		} catch (FileNotFoundException e) {
			System.err.println("Error: sending file can not be openned.");
			System.err.println(e.getMessage());
			
            return false;
		}
		return send(stream, targetFileName);
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean send(String name, String targetFileName) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(name); 
		} catch (FileNotFoundException e) {
			System.err.println("Error: sending file can not be openned.");
			System.err.println(e.getMessage());
			
            return false;
		}
		return send(stream, targetFileName);
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#send(java.io.FileInputStream, java.lang.String)
	 */
	@Override
	public boolean send(FileInputStream stream, String targetFileName) {
		try {
			ftp.storeFile(targetFileName, stream);
			stream.close();
			
			System.out.println("Success: sending is success.");
		} catch (IOException e) {
			System.err.println("Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			System.err.println(e.getMessage());
			
            return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#receive(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean receive(String sourceFileName, String targetFileName) {
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(targetFileName); 
		} catch (FileNotFoundException e) {
			System.err.println("Error: receiving file may be exist in local host.");
			System.err.println(e.getMessage());
			
            return false;
		}
		return receive(sourceFileName, stream);
	}
	
	@Override
	public boolean receive(String sourceFileName,
			FileOutputStream targetOutputStream) {
        try {
			ftp.retrieveFile(sourceFileName, targetOutputStream);
			targetOutputStream.close();
		} catch (IOException e) {
			System.err.println("Error: an I/O error occurs while either sending a command to the server or receiving a reply from the server.");
			System.err.println(e.getMessage());
			
            return false;
		}
        return true;
	}

	/* (non-Javadoc)
	 * @see kr.dragshare.NetworkManager#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return ftp.isConnected();
	}

	@Override
	public boolean close() {
        try
        {
            ftp.noop(); // check that control connection is working OK
            ftp.logout();
            ftp.disconnect();
        }
        catch (IOException f)
        {
    		return false;
        }
		return true;
	}
	
	
	// progress update를 위하여 FTPNetworkManager에서 FTPTask를 받아와서 publishProgress를 불러줌
	private MainActivity.FTPTask ftpTask;
	public void setFTPTask(MainActivity.FTPTask task){
		ftpTask = task;
	}

	@Override
	public void bytesTransferred(CopyStreamEvent event) {
		bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
	}

	@Override
	public void bytesTransferred(long arg0, int arg1, long arg2) {
		ftpTask.callPublishProgress(arg0);
		transferred	= arg0;
	}
}
