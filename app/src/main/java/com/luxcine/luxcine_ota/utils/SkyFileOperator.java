package com.luxcine.luxcine_ota.utils;

import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SkyFileOperator
{ private static String TAG = "SkyFileOperator";
  private static final String SKY_NANDKEY_LIST = "/sys/class/unifykeys/list";
  private static final String SKY_NANDKEY_NAME = "/sys/class/unifykeys/name";
  private static final String SKY_NANDKEY_READ = "/sys/class/unifykeys/read";
  private static final String SKY_NANDKEY_WRITE = "/sys/class/unifykeys/write";

  // ERROR //
  public static boolean Read(String path, byte[] paramArrayOfByte, int paramInt)
  {
	  int bytesum = 0;
	  int byteread = 0;
	  try {
		  InputStream inStream = new FileInputStream(path); // ����ԭ�ļ�
		  byte[] buffer = new byte[1444];
		  while ((byteread = inStream.read(buffer)) != -1) {
			  System.arraycopy(buffer, 0, paramArrayOfByte, bytesum, buffer.length);
			  bytesum += byteread; // �ֽ��� �ļ���С
			  System.out.println(bytesum);
		  }
		  inStream.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }

	  return true;
	  
  }

  public static boolean ReadBuff(String paramString, byte[] paramArrayOfByte)
  {
    return Read(paramString, paramArrayOfByte, -1);
  }

  // ERROR //
  public static boolean Write(String path, byte[] buffer, int paramInt)
  {
	  int bytesum = 0;
	  int byteread = 0;
	  try {
		  OutputStream outStream = new FileOutputStream(path); // ����ԭ�ļ�
		  outStream.write(buffer);
		  outStream.flush();
		  outStream.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }

	  return true;
  }

  static boolean WriteBuff(String paramString, byte[] paramArrayOfByte)
  {
    return Write(paramString, paramArrayOfByte, -1);
  }

  private static boolean containFile(String paramString1, String paramString2)
  {
    boolean i = false;
    File localFile = new File("/" + paramString1);
    if (!localFile.exists()) return false;
    File[] arrayOfFile = localFile.listFiles();
    if((arrayOfFile == null) || (arrayOfFile.length <= 0)) return true;
    int j = arrayOfFile.length;
    for (int k = 0; k<j ; ++k)
    {
      if (arrayOfFile[k].toString().endsWith(paramString2)){
    	  i = true;
    	  break;
      }
    }
    return i;
  }

  public static boolean isContainFile(String paramString1, String paramString2)
  {
    return containFile(paramString1, paramString2);
  }

  public static String readFromNandkey(String keyName)
  {
     String readValue = null;
     if ((keyName == null) || (keyName.isEmpty())){
         Log.d(TAG,"Invalid keyName.");
    	 return null;
     }
      if (!readStringFromFile(SKY_NANDKEY_LIST, "").contains(keyName)){
          Log.d(TAG,"Key list no this keyName:" + keyName);
        return null;
      }
      if (!writeStringToFileWithoutSync(SKY_NANDKEY_NAME, keyName)){
          Log.d(TAG,"write  error: " + keyName);
        return null;
      }
      try
      {
    	readValue = readStringFromFile(SKY_NANDKEY_READ, "");
          Log.d(TAG,"readValue " + readValue);
          if(readValue.length()<5) return "error";
        return readValue.substring(0, getUsidLen());//有两个byte是空
      }
      catch (Throwable localThrowable)
      {
          Log.d(TAG,"Exception", localThrowable);
        readValue = null;
      }
      return readValue;
  }

    private static int getUsidLen(){
        if(isMyProduct("M2")){ return 15;
        }else if(isMyProduct("M3")) { return 15;
        }else if(isMyProduct("p313")) { return 15;
        }else if(isMyProduct("marconi")||isMyProduct("atv")) { return 16;
        }else{return 15;}
    }

    private static boolean isMyProduct(String p){
        String productModel = SystemProperties.get("ro.product.board", "");
        if(productModel.equalsIgnoreCase(p))
            return true;
        else
            return false;
    }
  // ERROR //
  public static String readStringFromFile(String path, String def)
  {
	  BufferedReader reader = null;
      try {
      	StringBuffer fileData = new StringBuffer(100);
      	reader = new BufferedReader(new FileReader(path));
      	char[] buf = new char[100];
      	int numRead = 0;
      	while ((numRead = reader.read(buf)) != -1) {
      		String readData = String.valueOf(buf, 0, numRead);
      		fileData.append(readData);
      	}
      	reader.close();
      	//Log.d(TAG,"readStringFromFile", fileData.toString());
        return fileData.toString();

      } catch (Throwable e) {
          Log.d(TAG,"Exception", e);
      } finally {
      	if (null != reader)
      		try {
      			reader.close();
      			reader = null;
      		} catch (Throwable t) {
      			;
      		}
     }
      return def;
  }

  public static boolean writeNandkey(String keyName, String keyValue)
  {
    boolean i = false;
    if ((keyName == null) || (keyName.isEmpty())){
        Log.d(TAG,"Invalid keyName.");
      return i;
    }
    if ((keyValue == null) || (keyValue.isEmpty())){
        Log.d(TAG,"Invalid value.");
        return i;
    }
    /*
    String str = SkyStringOperator.toHexString(keyValue.getBytes());
    if ((str == null) || (str.isEmpty())){
        SkyLog.e("Invalid writeValue.");
        return i;
    }
    SkyLog.e("writeValue " + str);
    */
    if (!writeStringToFileWithoutSync(SKY_NANDKEY_NAME, keyName)){
        Log.d(TAG,"write" + keyName +" error");
        return i;
    }
    if (!writeStringToFileWithoutSync(SKY_NANDKEY_WRITE, keyValue)){
        Log.d(TAG,"write" + keyValue + "error");
        return i;
    }
    if (readFromNandkey(keyName).equalsIgnoreCase(keyValue))
    {
        Log.d(TAG,"write check  success.");
        i = true;
    }else{
        Log.d(TAG,"write check  error!");
    }
    return i;
  }

  // ERROR //
  public static void writeStringAddtion(String paramString1, String paramString2)
  {
	  String orgString = readStringFromFile(paramString1,"");
	  writeStringToFile(paramString1,orgString + paramString2);
  }

  // ERROR //
  public static boolean writeStringToFile(String path, String s)
  {
	   	FileOutputStream wrt = null;
    	try {
		    wrt = new FileOutputStream(path);
		    wrt.write(s.getBytes());
		    wrt.close();
		    wrt = null;
	
		    return true;
		} catch (Throwable t) {
            Log.d(TAG,"Exception " + t);
		} finally {
		    if (null != wrt)
		        try {
			    wrt.close();
			} catch (Throwable t) {
	
	                };
		}
	
		return false;
  }

  // ERROR //
  public static boolean writeStringToFileWithoutSync(String paramString1, String paramString2)
  {
	  return writeStringToFile(paramString1,paramString2);
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: new 133	java/io/FileOutputStream
    //   5: dup
    //   6: aload_0
    //   7: invokespecial 134	java/io/FileOutputStream:<init>	(Ljava/lang/String;)V
    //   10: astore_3
    //   11: aload_3
    //   12: aload_1
    //   13: invokevirtual 247	java/lang/String:getBytes	()[B
    //   16: invokevirtual 295	java/io/FileOutputStream:write	([B)V
    //   19: aload_3
    //   20: invokevirtual 170	java/io/FileOutputStream:close	()V
    //   23: iconst_1
    //   24: istore 8
    //   26: iconst_0
    //   27: ifeq +7 -> 34
    //   30: aconst_null
    //   31: invokevirtual 170	java/io/FileOutputStream:close	()V
    //   34: iload 8
    //   36: ireturn
    //   37: astore 4
    //   39: ldc 215
    //   41: aload 4
    //   43: invokestatic 51	com/skyworthdigital/autotest/common/SkyLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)I
    //   46: pop
    //   47: aload_2
    //   48: ifnull +7 -> 55
    //   51: aload_2
    //   52: invokevirtual 170	java/io/FileOutputStream:close	()V
    //   55: iconst_0
    //   56: istore 8
    //   58: goto -24 -> 34
    //   61: astore 5
    //   63: aload_2
    //   64: ifnull +7 -> 71
    //   67: aload_2
    //   68: invokevirtual 170	java/io/FileOutputStream:close	()V
    //   71: aload 5
    //   73: athrow
    //   74: astore 10
    //   76: goto -42 -> 34
    //   79: astore 9
    //   81: goto -26 -> 55
    //   84: astore 6
    //   86: goto -15 -> 71
    //   89: astore 5
    //   91: aload_3
    //   92: astore_2
    //   93: goto -30 -> 63
    //   96: astore 4
    //   98: aload_3
    //   99: astore_2
    //   100: goto -61 -> 39
    //
    // Exception table:
    //   from	to	target	type
    //   2	11	37	java/lang/Throwable
    //   2	11	61	finally
    //   39	47	61	finally
    //   30	34	74	java/lang/Throwable
    //   51	55	79	java/lang/Throwable
    //   67	71	84	java/lang/Throwable
    //   11	23	89	finally
    //   11	23	96	java/lang/Throwable
  }
}
