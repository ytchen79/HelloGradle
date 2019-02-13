package com.cyt.utils.socket;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @Description: TODO
 * @Function List: TODO
 * @author: ytchen
 * @Date: 2018/9/18
 */
public class Server {
    public static void main(String args[]) {
        Scanner sc=new Scanner(System.in);
        DataOutputStream dos=null;
        DataInputStream dis=null;
        try {
            ServerSocket ss=new ServerSocket(8888);
//监听客户端的连接，socket就代表和客户端的连接
            Socket socket=ss.accept();
            new MessageThread(socket).start();
//获取客户端的ip地址对象
            //打开输出流，向客户端输出信息
            dos=new DataOutputStream(socket.getOutputStream());
            System.out.println("请输入您想说的话:");
            String s=sc.nextLine();
            while(!s.equals("bye")) {
                dos.writeUTF(s);
                System.out.println("请输入您想说的话:");
                s=sc.nextLine();
            }
        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                dos.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    private static class MessageThread extends Thread{
        Socket socket;
        public MessageThread(Socket socket) {
            this.socket=socket;
        }
        public void run() {
            DataInputStream dis=null;
            try {
                dis=new DataInputStream(socket.getInputStream());
                String s=dis.readUTF();
                while(true) {
                    System.out.println("client:"+s);
                    if(s.equals("bye")) {
                        break;
                    }
                    s=dis.readUTF();
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                try {
                    dis.close();
                } catch (IOException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}