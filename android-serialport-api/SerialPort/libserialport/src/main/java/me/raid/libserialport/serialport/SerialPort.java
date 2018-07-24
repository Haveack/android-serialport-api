/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package me.raid.libserialport.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	private File device;
	private int baudrate;
	private int flags;
	private boolean rtscts;

	public SerialPort(File device) {
		this(device, 9600, 0);
	}

	public SerialPort(File device, int baudrate, int flags) {
	    this(device, baudrate, flags, false);
	}

	public SerialPort(File device, int baudrate, int flags, boolean rtscts) {
		this.device = device;
		this.baudrate = baudrate;
		this.flags = flags;
		this.rtscts = rtscts;
	}

	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void open() throws IOException, SecurityException {
		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				//su = Runtime.getRuntime().exec("/system/bin/su");// /system/bin/su
				su = Runtime.getRuntime().exec("/system/xbin/su");// /system/bin/su
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
					+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if (su.waitFor() != 0) {
					throw new SecurityException("request permission failed");
				}
				if (!device.canRead() || !device.canWrite()) {
					throw new SecurityException("serial not found or permission denied");
				}
			} catch (Exception e) {
				throw new SecurityException(e);
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags, rtscts);
		if (mFd == null) {
			throw new IOException("native open returns null");
		}
		mFileInputStream = new FileInputStream(mFd);//the feedback from the serial port, chen, 20130922
		mFileOutputStream = new FileOutputStream(mFd);//the output to the serial port, chen, 20130922
	}


	public int getBaudrate() {
		return baudrate;
	}


	public File getDevice() {
		return device;
	}


	public int getFlags() {
		return flags;
	}


	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags, boolean rtscts);
	public void close() {
		if (mFd != null) {
			nativeClose();
		}
	}
	private native void nativeClose();
	static {
		System.loadLibrary("serial_port");
	}
}
