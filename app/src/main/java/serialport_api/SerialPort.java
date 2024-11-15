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

package serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";

    private static final String DEFAULT_SU_PATH = "/system/bin/su";

    private static String sSuPath = DEFAULT_SU_PATH;

    /**
     * Set the su binary path, the default su binary path is {@link #DEFAULT_SU_PATH}
     *
     * @param suPath su binary path
     */
    public static void setSuPath(String suPath) {
        if (suPath == null) {
            return;
        }
        sSuPath = suPath;
    }

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {
        this(device, baudrate, 1, 8, 0, 0, flags);
    }

    public SerialPort(File device, int baudrate, int parity, int flags) throws SecurityException, IOException {
        this(device, baudrate, 1, 8, parity, 0, flags);
    }

    public SerialPort(File device, int baudrate, int stopBits, int dataBits, int parity, int flowCon, int flags)
            throws SecurityException, IOException {
        /* Check access permission */  // 检查是否获取了指定串口的读写权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                // 如果没有获取指定串口的读写权限，则通过挂在到linux的方式修改串口的权限为可读写
                Process su;
                su = Runtime.getRuntime().exec(sSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, stopBits, dataBits, parity, flowCon, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * JNI：调用java本地接口，实现串口的打开和关闭
     * 串口有五个重要的参数：串口设备名，波特率，检验位，数据位，停止位
     * 其中检验位一般默认位NONE,数据位一般默认为8，停止位默认为1
     *
     * @param path     串口设备的据对路径
     * @param baudrate 波特率
     * @param stopBits 停止位 - 停止位，1 或 2  （默认 1）
     * @param dataBits 数据位 - 数据位，5 ~ 8  （默认8）
     * @param parity   校验位 - 奇偶校验，0 None（默认）； 1 Odd； 2 Even
     * @param flowCon  流控 不使用流控(NONE), 硬件流控(RTS/CTS), 软件流控(XON/XOFF); 默认不使用流控
     * @param flags    O_RDWR  读写方式打开 | O_NOCTTY  不允许进程管理串口 | O_NDELAY   非阻塞
     * @return
     */
    private native static FileDescriptor open(String path, int baudrate, int stopBits, int dataBits, int parity, int flowCon, int flags); //打开串口

    public native void close();

    static {
        System.loadLibrary("serial_port");
    }
}
