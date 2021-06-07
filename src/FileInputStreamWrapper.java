import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileInputStreamWrapper {
    private FileInputStream fin;
    private File fileObject;
    long fileSize;
    long bytesRead;

    FileInputStreamWrapper(String filename){
        try {
            fin = new FileInputStream(filename);
            fileObject = new File(filename);
            fileSize = fileObject.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean EOF(){
        if(fileSize > bytesRead){
            return false;
        }
        return true;
    }

    public int read(byte[] buffer, int off, int len){
        //Puts data into supplied buffer, return value is number of bytes read in this operation
        try {
            if((long)len > fileSize - bytesRead){
                len = (int)(fileSize - bytesRead);
            }
            fin.read(buffer, off, len);
            bytesRead += len;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return len;
    }

    public void close(){
        try {
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
