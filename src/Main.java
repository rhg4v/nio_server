import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        try (ServerSocketChannel socketChannel = ServerSocketChannel.open()) {
            Selector selector = Selector.open();
            setSocketChannel(socketChannel);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                Iterator<SelectionKey> iterator = getSelectionKeyIterator(selector);

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {

                        SocketChannel client = setSocketChanelForTransferSelectKey(key);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {

                        ByteBuffer buffer = setByteBufferSetting();
                        SocketChannel client = readSocketBuffer(key, buffer);
                        writeSocketBuffer(buffer, client);
                    }
                    iterator.remove();
                }
            }

        }
    }

    private static void writeSocketBuffer(ByteBuffer buffer, SocketChannel client) throws IOException {
        if(buffer.position() != 0) {
            buffer.flip();
            System.out.println("go: " + new String(buffer.array(), StandardCharsets.UTF_8));
            client.write(buffer);
            buffer.clear();
        }
    }

    private static SocketChannel readSocketBuffer(SelectionKey key, ByteBuffer buffer) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        client.read(buffer);
        System.out.println("receive: "+new String(buffer.array(), StandardCharsets.UTF_8));

        return client;
    }

    private static ByteBuffer setByteBufferSetting() {
        ByteBuffer buffer = ByteBuffer.allocate(500);

        return buffer;
    }

    private static SocketChannel setSocketChanelForTransferSelectKey(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannelAcceptable = (ServerSocketChannel) key.channel();
        SocketChannel client = serverSocketChannelAcceptable.accept();
        client.configureBlocking(false);

        return client;
    }

    private static Iterator<SelectionKey> getSelectionKeyIterator(Selector selector) throws IOException {
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        return iterator;
    }

    private static void setSocketChannel(ServerSocketChannel socketChannel) throws IOException {
        socketChannel.bind(new InetSocketAddress("localhost", 8083));
        socketChannel.configureBlocking(false);
    }
}