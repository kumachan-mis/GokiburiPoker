class ThreadsSynchro{

    private int mainPlayerId;  //メインプレイヤーのid
    private int senderId;      //直前にカードを押し付けたプレイヤーのid
    private int loserId;       //敗者のid

    private int error;  //通信が途切れた人数
    private int judged;  //-1: 不定, 0: 不正解, 1:正解, 2:たらい回し
    private int[] ret = new int[2]; //本当のカードと宣言を格納
    private boolean in = false;
    private boolean out = true;
    private int waitnum;  //待機している人数

    private static ThreadsSynchro instance = new ThreadsSynchro();
    public static ThreadsSynchro getInstance(){
        return instance;
    }

    private ThreadsSynchro(){
        mainPlayerId = -1;
        senderId = -1;
        loserId = -1;

        error = 0;
        judged = -1;
        waitnum = 0;
    }

    public synchronized void setMainPlayerId(int id){
        mainPlayerId = id;
    }

    public synchronized int getMainPlayerId(){
        return mainPlayerId;
    }

    public synchronized void setSenderId(int id){
        senderId = id;
    }

    public synchronized  int getSenderId(){
        return senderId;
    }

    public synchronized void setLoserId(int id){
        loserId = id;
    }

    public synchronized int getLoserId(){
        return loserId;
    }

    public synchronized void addError(){
        error++;
    }

    public synchronized int getError(){
        return error;
    }

    public synchronized void setJudged(int judged){
        this.judged = judged;
    }

    public synchronized int getJudged(){
        return judged;
    }

    public synchronized int[] getRet(){
        return ret;
    }

    public synchronized void setRet(int[] ret){
        this.ret = ret;
    }

    private synchronized void in(){
        try{
            if(waitnum == PokerServer.PLAYER - error - 1){
                waitnum++;
                out = false;
                in= true;
                notifyAll();
                //System.err.println("notifiy: " + waitnum);
            }

            while(in == false){
                waitnum++;
                //System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    private synchronized void out(){
        try{
            //System.out.println("player : " + PokerServer.PLAYER);
            if(waitnum == 1){
                waitnum--;
                in = false;
                out = true;
                notifyAll();
                //System.err.println("notifiy: " + waitnum);
            }

            while(out == false){
                waitnum--;
                //System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    public void synchro(){
        in();
        out();
        //System.out.println("synchronized");
    }  //同期処理
}