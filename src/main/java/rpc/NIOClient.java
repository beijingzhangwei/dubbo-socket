package rpc;

import com.google.common.collect.Maps;
import org.apache.dubbo.common.io.Bytes;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;
import org.apache.dubbo.remoting.buffer.ChannelBufferOutputStream;
import org.apache.dubbo.remoting.buffer.HeapChannelBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class NIOClient {



    public static void main(String[] args) throws IOException {

        // byteBuffer
        // header.
        byte[] header = new byte[16];
        // set 魔数，2个字节
        final short MAGIC = (short) 0xdabb;
        Bytes.short2bytes(MAGIC, header);

        // 请求类型 协议 1个字节
        final byte FLAG_REQUEST = (byte) 0x80;
        byte HESSIAN2_SERIALIZATION_ID = 2;
        header[2] = (byte) (FLAG_REQUEST | HESSIAN2_SERIALIZATION_ID);

        // 双通 第三个字节的第三位
        final byte FLAG_TWOWAY = (byte) 0x40;
        header[2] |= FLAG_TWOWAY;

        // 请求ID 第四字节开始
        long requestId = System.currentTimeMillis();
        Bytes.long2bytes(requestId, header, 4);
//        System.out.println("【请求ID】requestInIDRequest "+ requestId);

        // 上线构建请求头
        HeapChannelBuffer buffer = new HeapChannelBuffer(400);
        // 越过头步，方便后面保存 payload数据长度
        buffer.writerIndex(header.length);

        // hession2 序列化请求数据
        ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
        ObjectOutput oout = new Hessian2ObjectOutput(bos);;
        oout.writeUTF("2.0.2");
        // 换成自己的接口
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
        // System.out.println("总计需要发送字节数："+(len+header.length));
        // 偏移12字节处开始，写入4字节的有效数据长度
        Bytes.int2bytes(len, header, 12);
        buffer.writerIndex(0);
        // 写入头
        buffer.writeBytes(header);
        // 写指针归位
        buffer.writerIndex(header.length + len);

        ByteBuffer buf = ByteBuffer.allocate(480);
        buf.clear();
        buf.put(buffer.array(),0, len+16);

        buf.flip();

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("172.16.248.197", 21000));
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.socket().setSoTimeout(300000);
        while(buf.hasRemaining()) {
            socketChannel.write(buf);
        }

        // 发送不出去
        // https://stackoverflow.com/questions/1106273/socketchannel-in-java-sends-data-but-it-doesnt-get-to-destination-application
        // https://community.oracle.com/tech/developers/discussion/1147471/socketchannel-not-sending-data-until-closed

        ByteBuffer bufR = ByteBuffer.allocate(290);
        int bytesRead = -1;
        do {
            bufR.clear();
            bytesRead = socketChannel.read(bufR);
            // 带协议头部 分析 requestID 和 responseID
            byte[] allBytes = new byte[bytesRead];
            System.arraycopy(bufR.array(),0 , allBytes, 0, bytesRead);
            String all = new String(allBytes, StandardCharsets.UTF_8 );
            System.out.println(Arrays.toString(allBytes));
            System.out.println("java NIOClient 调用结果,(header+body)result="+ all);

            // 头部
            byte[] headerBytes = new byte[16];
            System.arraycopy(bufR.array(),0 , headerBytes, 0, 16);
            System.out.println(Arrays.toString(headerBytes));
            String headStr = new String(headerBytes, StandardCharsets.UTF_8 );
            System.out.println("java NIOClient 调用结果,(头部)result=" + headStr);

            // 协议投中的请求ID获取
            byte[] b = new byte[8];
            System.arraycopy(headerBytes,4 , b, 0, 8);
            long requestInIDResponse = bytesToLong(b);
//            System.out.println("【请求ID】requestInIDResponse =  "+ requestInIDResponse);


            // body
            byte[] bodyBytes = new byte[bufR.position() - 16];
            System.arraycopy(bufR.array(),16 , bodyBytes, 0, bufR.position() - 16);
            System.out.println(Arrays.toString(bodyBytes));
            String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8 );
            System.out.println("java NIOClient 调用结果,(body)result="+ bodyStr);

            // 解码  ...
        }while (bytesRead > 0);

    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
