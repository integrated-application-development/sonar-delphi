unit DemoForm;

interface

//test
{ test }
(* test *)

type
  TfDemo = class(TForm)
    bShowTracker: TButton;
    procedure bShowTrackerClick(Sender: TObject);
  private
	//test
  public
	{test}
  end;
  
type
  TfDemoSecond = class (TForm)
    bShowTracker: TButton;
    procedure bShowTrackerClick(Sender: TObject);
  private

  public

  end;
  
var
  fDemo: TfDemo;

implementation

procedure TfDemo.bShowTrackerClick(Sender: TObject);
begin
  (* long
  multiline
  comment
  *)
  
  string = "sample string";
  
  for i:=0 to 100 do
  begin
  	a := b;
  	b := c;
  	c := d;
  	a := b;
  	if a < b then
  		c := b;
  end;

  for i:=0 to 100 do
  begin
  	a := b;
  	b := c;
  	c := d;
  	a := b;
  	if a < b then
  		c := b;
  end;
  
end;

end.