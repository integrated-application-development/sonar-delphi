Unit IdentifierCapitalisationTest;

interface

const
    MAX_LINES = 3;
    CRUDE_PI  = 22/7;
    HELLO     = 'Hello World';
    LETTERS   = ['A'..'Z', 'a'..'z'];
    DECISION  = True;

var
    MyApple : String;

implementation

 begin
    //Variable identifier test
    MyApple := 'Apple'; //compliant
    myApple := 'Apple'; //Non-compliant
    Myapple := 'Apple'; //Non-compliant
    MYAPPle := 'Apple';
    showMessage(myappLE); // Non-compliant

    //Constant identifier test
    showMessage(HELLO);//compliant
    showMessage(hELLO);//non-compliant
 end.
