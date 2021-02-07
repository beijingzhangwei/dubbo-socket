package rpc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BioSocketServer implements Runnable {

    private Socket client = null;
    public BioSocketServer(Socket client){
        this.client = client;  
    }

    public static void main(String[] args) throws Exception{
        //服务端在20006端口监听客户端请求的TCP连接
        ServerSocket server = new ServerSocket(21000);
        Socket client = null;
        boolean f = true;
        while(f){
            //等待客户端的连接，如果没有获取连接
            client = server.accept();
            client.setTcpNoDelay(true);
            System.out.println("与客户端连接成功！");
            //为每个客户端连接开启一个线程
            new Thread(new BioSocketServer(client)).start();
        }
        server.close();
    }


    @Override
    public void run() {
        try{
            //获取Socket的输出流，用来向客户端发送数据
            PrintStream out = new PrintStream(client.getOutputStream());
            StringBuilder sb = new StringBuilder(100);

            InputStream in = client.getInputStream();
            byte[] bytes = new byte[1024];
            int bytesRead = 0;
            while(in.read(bytes) > 0){
                sb.append(new String(bytes,0,bytesRead,"UTF-8"));
            }
            String response = sb.toString();
            System.out.println("接收到数据：" + response);
            out.println(response);
            out.flush();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}  