package rpc;

import api.UniterestService;
import com.google.common.collect.Maps;
import org.apache.dubbo.common.io.Bytes;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.remoting.buffer.ChannelBufferOutputStream;
import org.apache.dubbo.remoting.buffer.HeapChannelBuffer;
import org.apache.dubbo.rpc.service.GenericService;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;
import java.util.Set;


public class BioClient {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        // 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("yyy");

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://172.16.208.180:2181");

        // NettyClient  -- 指定方法调用
         normal(application, registry);

        // NettyClient  -- 泛型调用
         generic(application, registry);

        // 以下是 socket套接字

        // 套接字 -- 泛型调用
        // genericMethodSendBytes();

        // 套接字 -- 指定方法调用
        normalMethodSendBytes();
    }

    private static void genericMethodSendBytes() {
        try {

            // byteBuffer
            // header.
            byte[] header = new byte[16];
            // set magic number.
            final short MAGIC = (short) 0xdabb;
            Bytes.short2bytes(MAGIC, header);

            // 请求类型 协议
            final byte FLAG_REQUEST = (byte) 0x80;
            byte HESSIAN2_SERIALIZATION_ID = 2;
            header[2] = (byte) (FLAG_REQUEST | HESSIAN2_SERIALIZATION_ID);

            // 双通
            final byte FLAG_TWOWAY = (byte) 0x40;
            header[2] |= FLAG_TWOWAY;

            // 请求ID
            Bytes.long2bytes(System.currentTimeMillis(), header, 4);

            // 上线构建请求头

            HeapChannelBuffer buffer = new HeapChannelBuffer(400);

            // 越过头步，方便后面保存 payload数据长度
            buffer.writerIndex(header.length);

            // 序列化请求数据
            ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
            org.apache.dubbo.common.serialize.ObjectOutput  oout = new Hessian2ObjectOutput(bos);;
            oout.writeUTF("2.0.2");
            oout.writeUTF("com.youapp.adonis.service.UniterestService");
            oout.writeUTF("1.0.0");

            // 泛化
            oout.writeUTF("$invoke");
            // ReflectUtils.getDesc(inv.getParameterTypes())
            oout.writeUTF("Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/Object;");
            oout.writeObject("queryByOpenId");
            oout.writeObject(new String[]{"java.lang.String"});
            oout.writeObject(new Object[]{"ochvq0JflYIxCFpS869AupBacfbo"});
            // 附件
            Map<String, String> map = Maps.newHashMap();
            map.put("path","com.youapp.adonis.service.UniterestService");
            map.put("interface","com.youapp.adonis.service.UniterestService");
            map.put("version","1.0.0");
            map.put("generic","true");
            map.put("timeout","3000");
            map.put("group","PROD");
            oout.writeObject(map);
            // 刷到channel缓冲区
            oout.flushBuffer();
            // channel 刷到内存
            bos.flush();
            bos.close();
            // 计算字节数
            int len = bos.writtenBytes();
            System.out.println("总计需要发送字节数："+(len+header.length));
            // 偏移12字节处开始，写入4字节的有效数据长度
            Bytes.int2bytes(len, header, 12);
            buffer.writerIndex(0);
            // 写入头
            buffer.writeBytes(header);
            // 写指针归位
            buffer.writerIndex(header.length + len);

            Socket socket = new Socket("172.16.248.198",21000);
            socket.setTcpNoDelay(true);
            socket.setSendBufferSize(400);
            socket.setKeepAlive(false);
            socket.setSoTimeout(300000);

            //构建IO
            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write(buffer.array(), 0, len+16);
            System.out.println("写入完毕"+ (len+16));
            out.flush();

            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            System.out.println("服务器："+mess);
            System.out.println("字节"+ mess.getBytes());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private static void normalMethodSendBytes() {
        try {
            // byteBuffer
            // header.
            byte[] header = new byte[16];
            // set magic number.
            final short MAGIC = (short) 0xdabb;
            Bytes.short2bytes(MAGIC, header);

            // 请求类型 协议
            final byte FLAG_REQUEST = (byte) 0x80;
            byte HESSIAN2_SERIALIZATION_ID = 2;
            header[2] = (byte) (FLAG_REQUEST | HESSIAN2_SERIALIZATION_ID);

            // 双通
            final byte FLAG_TWOWAY = (byte) 0x40;
            header[2] |= FLAG_TWOWAY;

            // 请求ID
            Bytes.long2bytes(System.currentTimeMillis(), header, 4);

            // 上线构建请求头

            HeapChannelBuffer buffer = new HeapChannelBuffer(400);

            // 越过头步，方便后面保存 payload数据长度
            buffer.writerIndex(header.length);

            // 序列化请求数据
            ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
            ObjectOutput  oout = new Hessian2ObjectOutput(bos);;
            oout.writeUTF("2.0.2");
            oout.writeUTF("com.youapp.adonis.service.UniterestService");
            oout.writeUTF("1.0.0");

            // 方法名
            oout.writeUTF("queryByOpenId");
            // ReflectUtils.getDesc(inv.getParameterTypes())
            ReflectUtils.getDesc(String.class);
            // 参数类型
            oout.writeUTF("Ljava/lang/String;");
            // 参数
            oout.writeObject("ochvq0MZHVPkbIyJU1n0Nbn4R-UY");

            // 附件 5
            Map<String, String> map = Maps.newHashMap();
            map.put("path","com.youapp.adonis.service.UniterestService");
            map.put("interface","com.youapp.adonis.service.UniterestService");
            map.put("version","1.0.0");
            map.put("timeout","3000");
            map.put("group","PROD");
            oout.writeObject(map);
            // 刷到channel缓冲区
            oout.flushBuffer();
            // channel 刷到内存
            bos.flush();
            bos.close();
            // 计算字节数
            int len = bos.writtenBytes();
            System.out.println("总计需要发送字节数："+(len+header.length));
            // 偏移12字节处开始，写入4字节的有效数据长度
            Bytes.int2bytes(len, header, 12);
            buffer.writerIndex(0);
            // 写入头
            buffer.writeBytes(header);
            // 写指针归位
            buffer.writerIndex(header.length + len);

            Socket socket = new Socket("172.16.248.197",21000);
            // 只要滑动窗口允许，立即发送
            socket.setTcpNoDelay(true);
            socket.setSendBufferSize(400);
            socket.setKeepAlive(false);
            socket.setSoTimeout(300000);

            // 构建IO输出流
            OutputStream out = socket.getOutputStream();
            out.write(buffer.array(), 0, len+16);
            System.out.println("写入字节数："+ (len+16));
            // Socket in Java sends data, but it doesn't get to destination application
            // C 语言
            // http://users.pja.edu.pl/~jms/qnx/help/tcpip_4.25_en/prog_guide/sock_advanced_tut.html
            out.flush();
            String response = getResponse(socket);
            System.out.println("服务器："+response);
            System.out.println("字节数"+ response.getBytes().length);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String getResponse(Socket socket) throws IOException{
        String response;

        StringBuilder sb = new StringBuilder(100);
        InputStream in = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int bl;
        // 对端是长连接，堵塞到这里，等待结束或者超时
        while( (bl = in.read(bytes)) > 0){
            sb.append(new String(bytes,0,bl,"UTF-8"));
            System.out.println(sb.toString());
        }
        response = sb.toString();

        return response;
    }


    private static void normal(ApplicationConfig application, RegistryConfig registry) {
        ReferenceConfig<UniterestService> reference = new ReferenceConfig<UniterestService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface(UniterestService.class);
        reference.setVersion("1.0.0");
        reference.setGroup("PROD");
        reference.setTimeout(3000000);

        // 和本地bean一样使用xxxService
        UniterestService xxxService = reference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        Set<String> ochvq0JflYIxCFpS869AupBacfbo = xxxService.queryByOpenId("ochvq0JflYIxCFpS869AupBacfbo");
        System.out.println("NettyClient  -- 指定方法调用,result=" + ochvq0JflYIxCFpS869AupBacfbo);
    }


    @SuppressWarnings("unchecked")
    private static void generic(ApplicationConfig application, RegistryConfig registry) {
        ReferenceConfig<GenericService> gReference = new ReferenceConfig<GenericService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        gReference.setApplication(application);
        gReference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        gReference.setInterface(UniterestService.class);
        gReference.setVersion("1.0.0");
        gReference.setGroup("PROD");
        gReference.setGeneric(true);
        gReference.setTimeout(30000000);

        // 和本地bean一样使用xxxService
        GenericService xxxService2 = gReference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        String[] parameterTypes = new String[]{"java.lang.String"};
        Set<String> ochvq0JflYIxCFpS869AupBacfbo2 =
                (Set<String>) xxxService2.$invoke(
                        "queryByOpenId",
                                parameterTypes,
                        new Object[]{"ochvq0JflYIxCFpS869AupBacfbo"});
        System.out.println("NettyClient  -- 泛型调用,result="+ochvq0JflYIxCFpS869AupBacfbo2);
    }

    public static void main12(String[] args) {
        for (Method declaredMethod : BioClient.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                System.out.println(parameterType.getCanonicalName());
            }

        }
        System.out.println();
    }

    public void getOpenID(String openId){
        return ;
    }
}
