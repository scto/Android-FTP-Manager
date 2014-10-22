package kr.dragshare.androidftpmanagertest;

import java.io.File;

import kr.dragshare.network.FTPNetworkManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	

	public class FTPTask extends AsyncTask<String, Long, Void> {
		FTPNetworkManager network;
		
		final String 	host = "165.132.107.90";
		final int		port = 21;
		final String 	id	 = "msl";
		final String	pw	 = "0";
		
		final String	targetPath = "/";
		
		long fileSize;
		
		@Override
		protected Void doInBackground(String... params) {
			// 파일 크기를 계산
			fileSize = (new File(params[0])).length();
			
			network = new FTPNetworkManager();
			network.setFTPTask(this);						// to process upload progress, transfer this instance to FTPNetworkManager 
			
			network.initialize(host, port, id, pw);
			
			// AsyncTask 클래스 내부에서는 메인 UI를 제어할 수 없습니다. Toast도 안돼요^^
//			Toast.makeText(getApplicationContext(), "Uploading starts", Toast.LENGTH_SHORT).show();
			
			if(!network.send(params[0], targetPath + getFileName(params[0]))){
				Log.e("NetworkManager", "Sending Failed");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Notify done
			Toast.makeText(getApplicationContext(), "FTP Upload Done", Toast.LENGTH_SHORT).show();
			
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Long... values) {
			// progress bar 등을 설정하시면 됩니다.
			long transferredBytes = values[0].longValue();
			
			((TextView)findViewById(R.id.textView1)).setText("Progress: "+ String.format("%.2f", 100.0 * transferredBytes / fileSize) + "%" );
			super.onProgressUpdate(values);
		}
		
		public void callPublishProgress(long value) {
			publishProgress(Long.valueOf(value));
		}
	}

	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        findViewById(R.id.button1).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
	        	// Initialize FTP Task instance
	        	FTPTask ftp = new FTPTask();
	        	
	        	// Get the file name of last picture 
	        	final String lastPicture = getLastPictureName();

	        	// Notify
	        	Toast.makeText(getApplicationContext(), "Starts FTP Task with file: " + lastPicture, Toast.LENGTH_SHORT).show();
	        	
	        	// Go and upload
	        	ftp.execute(lastPicture);
			}
        });
    }
    
	private final String getLastPictureName() {
		// Find the last picture
		String[] projection = new String[]{
		    MediaStore.Images.ImageColumns._ID,
		    MediaStore.Images.ImageColumns.DATA,
		    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
		    MediaStore.Images.ImageColumns.DATE_TAKEN,
		    MediaStore.Images.ImageColumns.MIME_TYPE
		    };
		final Cursor cursor = getContentResolver()
		        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, 
		               null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		// Put it in the image view
		if (cursor.moveToFirst()) {
		    String imageLocation = cursor.getString(1);

		    cursor.close();
		    
		    return imageLocation;
		} 
	    cursor.close();
	    
	    return null;
	}
	
	private String getFileName(String fullPath) {
		int S = fullPath.lastIndexOf("/");
		int M = fullPath.lastIndexOf(".");
		int E = fullPath.length();
		
		String filename = fullPath.substring(S+1, M);
		String extname = fullPath.substring(M+1, E);
		
		String extractFileName = filename + "." + extname;
		return extractFileName;
	}

	private String uriToPath(Intent intent) {
		Uri imageUri =(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		
		final String[] filePathColumn = {MediaStore.Images.Media.DATA};
		final Cursor imageCursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
		
		imageCursor.moveToFirst();
		
		final int columnIndex = imageCursor.getColumnIndex(filePathColumn[0]);
		final String imagePath = imageCursor.getString(columnIndex);
		imageCursor.close();
		return imagePath;
	}
}
