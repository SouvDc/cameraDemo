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

package jerome.com.usbcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Usb {

	private static final String TAG = "Usb";

	/**
	 *
	 * @param device  /dev/video0
	 * @throws SecurityException
	 * @throws IOException
	 */
	public Usb(File device) throws InterruptedException, SecurityException, IOException {

		checkDevice(device);

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			/* Missing read/write permission, trying to chmod the file */
			Process su;
			su = Runtime.getRuntime().exec("/system/xbin/su");
			String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());

			if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
				throw new SecurityException(device.getAbsolutePath() + "节点不支持读写操作");
			}

		}

	}

	/**
	 *
	 * @param device1  /dev/video0
	 * @param device2  /dev/video0
	 * @throws SecurityException
	 * @throws IOException
	 */
	public Usb(File device1, File device2) throws InterruptedException, SecurityException, IOException {

		checkDevice(device1);
		checkDevice(device2);

		/* Check access permission */
		checkPermission(device1);
		checkPermission(device2);

	}

	private void checkPermission(File device) throws IOException, InterruptedException {
		if (!device.canRead() || !device.canWrite()) {
			/* Missing read/write permission, trying to chmod the file */
			Process su;
			su = Runtime.getRuntime().exec("/system/xbin/su");
			String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());

			if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
				throw new SecurityException(device.getAbsolutePath() + "节点不支持读写操作");
			}

		}
	}

	/**
	 * 检查USB摄像头是生成了video文件
	 * @param device
	 * @return
	 */
	private void checkDevice(File device) throws FileNotFoundException {
		//		boolean isFile = device.isFile();//该节点非文件
		if (device == null)
			throw new FileNotFoundException("未指定节点路径");

		if (!device.exists())
			throw new FileNotFoundException(device.getAbsolutePath() + "节点未生成");
	}

}
