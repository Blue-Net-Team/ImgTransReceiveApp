package app.imgtrans.receiver;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Objects;

import app.imgtrans.R;

public class Receiver {
    private int UDP_PORT = 6000; // UDP端口
    private static final int MAX_UDP_SIZE = 65535; // UDP最大数据包大小
    private ImageView imageView;

    // 对端ip
    private String serverIp;

    private boolean receiveFlag = true;

    /**
     * 构造函数
     * @param imageView 显示图片的ImageView
     * @param serverIp  对端ip
     * @param port      UDP端口
     */
    public Receiver(ImageView imageView, String serverIp, int port) {
        this.imageView = imageView;
        this.serverIp = serverIp;
        this.UDP_PORT = port;
    }

    /**
     * 开始接收数据
     */
    public void startReceiving() {
        Toast.makeText(
                imageView.getContext(),
                "Waiting for data...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(UDP_PORT);
                    socket.setSoTimeout(3000);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    while (receiveFlag) {
                        try {
                            byte[] buffer = new byte[MAX_UDP_SIZE];
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                            // 接收UDP数据包
                            socket.receive(packet);

                            if (!Objects.equals(packet.getAddress().getHostAddress(), serverIp)) {
                                continue; // 如果来源地址不匹配，忽略数据包
                            }

                            // 前4个字节是packet的长度
                            int packetLength_target = (buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8) | ((buffer[2] & 0xff) << 16) | ((buffer[3] & 0xff) << 24);
                            // 读取到的长度
                            int packetLength = packet.getLength() - 4 - 3;
                            // 末尾3个字节是包尾b'EOF'
                            if (buffer[buffer.length - 1] == 'F' && buffer[buffer.length - 2] == 'O' && buffer[buffer.length - 3] == 'E' && packetLength == packetLength_target) {
                                Log.w("Receiver", "run: packetLength_target: " + packetLength_target + " packetLength: " + packetLength);
                                return;
                            }

                            //取出除了前4个字节和末尾3个字节的数据
                            byte[] data = new byte[packetLength];
                            System.arraycopy(buffer, 4, data, 0, packetLength);

                            // 将数据写入字节数组输出流
                            baos.write(data);

                            // 将接收到的字节数据转换为Bitmap
                            byte[] imageData = baos.toByteArray();
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                            if (receiveFlag) {
                                // 在主线程更新UI
                                imageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bitmap);
                                    }
                                });
                            }

                            // 重置字节数组输出流
                            baos.reset();
                        } catch (SocketTimeoutException e){
                            // 超时处理，加载timeout.jpg
                            final Bitmap timeoutBitmap = BitmapFactory.decodeResource(imageView.getResources(), R.drawable.timeout);
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(timeoutBitmap);
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    Log.e("Receiver", "run: ", e);
                    String error = e.getMessage();
                    Toast.makeText(
                            imageView.getContext(),
                            "Error: " + error,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }).start();
    }

    /**
     * 停止接收数据
     */
    public void close() {
        final Bitmap closedBitmap = BitmapFactory.decodeResource(imageView.getResources(), R.drawable.closed);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(closedBitmap);
            }
        });
        receiveFlag = false;
    }
}
