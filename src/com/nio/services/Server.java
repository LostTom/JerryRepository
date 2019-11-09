package com.nio.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {
	public static void main(String[] args) throws IOException {
		// 1������serversocketchannel���������÷�����
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		// 2�����ö˿�
		serverSocketChannel.socket().bind(new InetSocketAddress(9000));
		// 3������selector
		Selector acceptselector = Selector.open();
		// 4����serversocketchannelע�ᵽselector,����ע��accept�¼�
		serverSocketChannel.register(acceptselector, SelectionKey.OP_ACCEPT);
		while (true) {
			// 5��select()->��ȡselectedkey���е���
			int z = acceptselector.select();
			if (z == 0)
				continue;
			Set<SelectionKey> keys = acceptselector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isAcceptable()) {
					// ��ȡsockeychannel�������÷�����
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					// ע����¼�
					socketChannel.register(acceptselector, SelectionKey.OP_READ);
				}
				if (key.isReadable()) {
					// ��ȡsocketchannel
					SocketChannel sc = (SocketChannel) key.channel();
					ByteBuffer bytebuffer = ByteBuffer.allocateDirect(1024);
					byte[] bucket = new byte[1024];
					int length = 0;
					List<String> lists = new ArrayList<String>();// ���ö�����HTTPЭ��
					while ((length = sc.read(bytebuffer)) != -1 && length != 0) {
						bytebuffer.clear();
						bytebuffer.get(bucket, 0, length);
						lists.add(new String(bucket));
						bytebuffer.clear();
					}
					// ��÷���·��
					String[] firstline = lists.get(0).split("\\s");
					String path = firstline[1];
					// ע��writer
					sc.register(acceptselector, SelectionKey.OP_WRITE, path);
				}
				if (key.isWritable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					String path = (String) key.attachment();
					if ("/".equals(path)) {
						path = "/index.html";// ����Ĭ�Ϸ���·��
					}
					path = path.substring(1);// ȥ��/
					File file = new File(path);
					String content = "";// ���ص�httpЭ��
					if (!file.exists()) {
						content = "HTTP/1.0 404 \r\n Content-Type: text/html\r\n\r\n";
					} else if (path.lastIndexOf(".mp4") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: video/mp4\r\n\r\n";
					} else if (path.lastIndexOf(".png") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: image/png\r\n\r\n";
					} else if (path.lastIndexOf(".jpg") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: image/jpeg\r\n\r\n";
					} else if (path.lastIndexOf(".css") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: text/css \r\n\r\n";
					} else if (path.lastIndexOf(".js") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-type: application/x-javascript\r\n\r\n";
					} else if (path.lastIndexOf(".html") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
					}else if (path.lastIndexOf(".svg") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
					}else if (path.lastIndexOf(".gif") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
					}else if (path.lastIndexOf(".jpeg") != -1) {
						content = "HTTP/1.0 200 OK\r\n Content-Type: text/html; charset=UTF-8\r\n\r\n";
					}
					ByteBuffer byteBuffer = ByteBuffer.wrap(content.getBytes());
					sc.write(byteBuffer);// д��Э��
					// д���ļ�
					if (file.exists()) {
						FileChannel fc = new FileInputStream(file).getChannel();
						fc.transferTo(0, file.length(), sc);
						fc.close();
					}
					sc.close();
				}
			}
			System.out.println("123456");
		}

	}
}
