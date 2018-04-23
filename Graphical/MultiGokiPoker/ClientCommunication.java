import java.net.Socket;
import java.net.InetAddress;
import java.io.*;
import java.net.UnknownHostException;

class ClientCommunication{
    private static final String end = "END";

    private static Socket socket = null;
    private static BufferedReader reader = null;
    private static PrintWriter writer = null;
    private InetAddress addr = null;
    
    ClientCommunication(InetAddress addr, int PORT){
        this.addr = addr;
        try{
            socket = new Socket(addr, PORT);

            reader = 
            new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            );
            
            writer = 
            new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream()
                    )
                ), true
            );
        }catch(IOException e){
            System.err.println(e);
        }
    }

    String readSingleMessage(){
        String str = null;
        try{
            while(true){
                str = reader.readLine();
                if(str != null){
                    break;
                }
            }
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }
        return str;
    }  //単一データ受信

    String[] readMultiMessages(int size){
        String[] strings = new String[size];
        String str = null;
        int i = 0;
        try{
            while(true){
                while(true){
                    str = reader.readLine();
                    if(str != null){
                        break;
                    }
                }

                if(str.equals(end)){
                    break;
                }
                strings[i++] = str;
                str = null;
            }
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }

        return strings;
    }  //複数データ受信

    void write(String message){
        writer.println(message);
    }

    void write(int number){
        writer.println(number);
    }

    int[] readHandCards(){
        int[] handCards = new int[GUIClient.INSECTNUM];
        for(int i = 0; i < GUIClient.INSECTNUM; ++i){
            handCards[i] = 0;
        }

        String str = null;
        try{
            while(true){
                while(true){
                    str = reader.readLine();
                    if(str != null){
                        break;
                    }
                }
                if(str.equals(end)){
                    break;
                }
                handCards[Integer.parseInt(str)]++;
                str = null;
            }  //受信番号: R10, 手持ちのカードを受信

        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }

        return handCards;
    }


    int[] readSumOfHandCards(){
        int[] sumOfHandCards = new int[GUIClient.INSECTNUM];
        
        for(int i = 0; i < GUIClient.INSECTNUM; ++i){
            sumOfHandCards[i] = 0;
        }

        String str = null;
        try{
            int i = 0;
            while(true){
                while(true){
                    str = reader.readLine();
                    if(str != null){
                        break;
                    }
                }
                if(str.equals(end)){
                    break;
                }
                sumOfHandCards[i++] = Integer.parseInt(str);
                str = null;
            }  //受信番号: R36, 各プレイヤーの初期カード枚数を受信

        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }
        return sumOfHandCards;
    }
}