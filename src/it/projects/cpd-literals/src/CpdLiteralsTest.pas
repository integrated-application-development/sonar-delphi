unit CpdLiteralsTest;

interface
{This is a sample Delphi file.}
//testDefinitionsIncludes
{ testDefinitionsIncludes }
(* testDefinitionsIncludes *)

type
  TfDemo = class(TForm)
    bShowTracker: TButton;
    procedure bShowTrackerClick(Sender: TObject);
  private
  //testDefinitionsIncludes
  public
  {testDefinitionsIncludes}
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
  
  for i:=0 to 100 do
  begin
    a := 1;
    if b <> 12 then begin
      b := 86;
    end;
    c := 1;
    a := 4;
    if a < 3 then begin
      c := 6;
    end;
  end;

  for i:=0 to 100 do
  begin
    a := 4;
    if b <> 7 then begin
      b := 9;
    end;
    c := 12;
    a := 94;
    if a < 18 then begin
      c := 137;
    end;
  end;
  
end;

end.