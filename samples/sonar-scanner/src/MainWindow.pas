unit MainWindow;

interface

uses
  Windows, GlobalsTest, FunctionTest, CommentsTest, AccessorsTest, StatementTest, OverloadTest;

{** documented class **}
type
  TMainWindow = class(TForm)
  public
    procedure foo1;
    procedure foo2;
  private
  	field1: integer;
  protected
    { comment line }
    //another comment
    (* block comment *)
  end;

var
  window: TMainWindow;

implementation

{$R *.dfm}

procedure TMainWindow.foo1;
begin
	foo2;
end;

procedure TMainWindow.foo2;
begin
	foo1;
	globalProcedure();
	x := globalFunction;
	TOverloadTest.over1(5);
	fooZZ;
	bar;
	getField;
	fooXX;
	getPrivateField();
	TFunctionTest.setField(5);
	TFunctionTest.getField;
	TFunctionTest.foo;
	TFunctionTest.bar();
	rfcFunction(123);
end;

end.