class ImportantValue{
    private int card;
    private int say;
    private int target;
    private boolean pass;
    private boolean guess;
    private boolean pushCard;
    private boolean backActive;
    private static ImportantValue value = new ImportantValue();

    private ImportantValue(){
        card = -1;
        say = -1;
        target = -1;
        backActive = false;
    }

    static ImportantValue getInstance(){
        return value;
    }

    int getCard(){
        return card;
    }
    int getSay(){
        return say;
    }
    int getTarget(){
        return target;
    }
    boolean getPass(){
        return pass;
    }
    boolean getGuess(){
        return guess;
    }
    boolean getPushCard(){
        return pushCard;
    }
    boolean getBackActive(){
        return backActive;
    }


    void setCard(int card){
        this.card = card;
    }
    void setSay(int say){
        this.say = say;
    }
    void setTarget(int target){
        this.target = target;
    }
    void setPass(boolean pass){
        this.pass = pass;
    }
    void setGuess(boolean guess){
        this.guess = guess;
    }
    void setPushCard(boolean pushCard){
        this.pushCard = pushCard;
    }
    void setBackActive(boolean backActive){
        this.backActive = backActive;
    }
}