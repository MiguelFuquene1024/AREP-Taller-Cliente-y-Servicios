/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.escuelaing.arep.clientesyservicios.http;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
/**
 *
 * @author Miguel
 */
public class HttpServer {
    private static final HttpServer instance = new HttpServer();
    private static final HashMap<String,String> contentList = new HashMap<String,String>();


    public static HttpServer getInstance(){
        contentList.put("html","text/html");
        contentList.put("css","text/css");
        contentList.put("js","text/javascript");
        contentList.put("jpeg","image/jpeg");
        contentList.put("jpg","image/jpg");
        contentList.put("png","image/png");
        return instance;
    }

    private HttpServer(){
        
    }

    public void start() throws IOException{
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(getPort());
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Ready to deploy");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            try {
                serverConnection(clientSocket);
            } catch (URISyntaxException e) {
                System.err.println("URI incorrect.");
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    public void serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
        OutputStream outStream=clientSocket.getOutputStream();
		PrintWriter out = new PrintWriter(outStream, true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream()));
        String inputLine, outputLine;
        ArrayList<String> request = new ArrayList<>();
        

        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            request.add(inputLine);
            if (!in.ready()) {
                break;
            }
        }

        String uriContentType="";
		String uri="";
		try {
			
			uriContentType=request.get(0).split(" ")[1];

            URI resource = new URI(uriContentType);
			
			uri=resource.getPath().split("/")[1];
        }catch(Exception e){
            System.out.println(e);
        }
        outputLine = getResource( uri, outStream);
        out.println(outputLine);
        out.close();
        in.close();
        clientSocket.close();
    }
    public String getResource( String uri, OutputStream outStream) throws URISyntaxException{
        if(uri.contains("jpg") || uri.contains("jpeg")){
            return computeImageResponse(uri, outStream);
        }else{
            return computeContentResponse(uri);
        }
    }

    public String computeContentResponse(String uriContentType){
        String extensionUri = uriContentType.substring(uriContentType.lastIndexOf(".") + 1);
        String content = "HTTP/1.1 200 OK \r\n" 
                            + "Content-Type: "+ contentList.get(extensionUri) + "\r\n"
                            + "\r\n";
        File file = new File("src/main/resources/"+uriContentType);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line =  br.readLine()) != null) content += line; 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public String computeImageResponse(String uriImgType, OutputStream outStream){
        uriImgType=uriImgType.replace("/img","");
        
        String extensionUri = uriImgType.substring(uriImgType.lastIndexOf(".") + 1);

        String content = "HTTP/1.1 200 OK \r\n" 
                            + "Content-Type: "+ contentList.get(extensionUri) + "\r\n"
                            + "\r\n";
        System.out.println("uriImgType " + uriImgType);
        File file = new File("src/main/resources/img/"+uriImgType);
        System.out.println("file "+file);
        try {
            BufferedImage bi = ImageIO.read(file);
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            DataOutputStream dataOutputStream= new DataOutputStream(outStream); 
            ImageIO.write(bi, extensionUri, byteArrayOutputStream);
            dataOutputStream.writeBytes(content);
            dataOutputStream.write(byteArrayOutputStream.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public String computeDefaultResponse(){
        String outputLine =
            "HTTP/1.1 200 OK\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<title>Title of the document</title>\n"
                + "</head>"
                + "<body>"
                + "My Web Site"
                + "</body>"
                + "</html>";
        return outputLine;
    }
    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000; //returns default port if heroku-port isn't set (i.e. on localhost)
    }
    public static void main(String[] args) throws IOException {
        HttpServer.getInstance().start();
    }
}
